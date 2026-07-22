/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.domain.StudentDiscount;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.reports.students.ClassDiscountsReport;
import kg.alex.abl.reports.students.SchoolDiscountsReport;
import kg.alex.abl.ui.StudentDefinitionView;
import kg.alex.abl.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Set;

public class DbStudentDiscount extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbStudentDiscount.class);

    public DbStudentDiscount() throws Exception {
        super();
    }

    public int exec_insert_st_discount(StudentDiscount d) throws SQLException {
        String sql = "INSERT INTO student_discount (free_entry_amount,discount_id,"
                + "student_id,year_id,employee_id,modification_date,note,"
                + "discount_value,creation_date) VALUES(?,?,?,?,?,NOW(),?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (d.getFree_entry_amount() == 0.0) {
            stat.setNull(1, Types.DOUBLE);
        } else {
            stat.setDouble(1, d.getFree_entry_amount());
        }
        stat.setInt(2, d.getDiscount_id());
        stat.setInt(3, d.getStudent_id());
        stat.setInt(4, d.getYear_id());
        stat.setInt(5, d.getEmployee_id());
        stat.setString(6, d.getNote());
        stat.setDouble(7, d.getDiscount_value());
        logger.info(d);
        logger.info(">>> INSERT DISCOUNT " + stat);
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(StudentDiscount sd) throws SQLException {
        String sql = "update student_discount set free_entry_amount = ?, "
                + "discount_id = ?, student_id = ?, year_id = ?, employee_id = ?, "
                + "modification_date=NOW(), note = ?, discount_value = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDouble(1, sd.getFree_entry_amount());
        stat.setDouble(2, sd.getDiscount_id());
        stat.setInt(3, sd.getStudent_id());
        stat.setInt(4, sd.getYear_id());
        stat.setInt(5, sd.getEmployee_id());
        stat.setString(6, sd.getNote());
        stat.setDouble(7, sd.getDiscount_value());
        stat.setString(8, sd.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer exec_disc_strCont(MyVaadinUI myUI, int stud_id,
                                              int year_id) throws SQLException {

        String sql = "SELECT sd.id, d.discount_type_id, d.amount, sd.free_entry_amount "
                + "FROM student_discount as sd "
                + "left join discount as d on sd.discount_id = d.id "
                + "where sd.student_id = ? and sd.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.discount_type_id, Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.FreeAmount), Double.class, 0.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sd.id"));
            item.getItemProperty(Settings.discount_type_id)
                    .setValue(result.getInt("d.discount_type_id"));
            item.getItemProperty(myUI.getMessage(Messages.Amount))
                    .setValue(result.getDouble("d.amount"));
            item.getItemProperty(myUI.getMessage(Messages.FreeAmount))
                    .setValue(result.getDouble("sd.free_entry_amount"));
        }
        return container;
    }

    public IndexedContainer execSQL_St_Discounts(MyVaadinUI myUI, int stud_id, int year_id,
                                                 StudentDefinitionView dw) throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT sd.id, sd.free_entry_amount, sd.discount_id, sd.note, "
                + "d.discount_type_id, d.id, d.amount "
                + "FROM student_discount as sd "
                + "left join discount as d on d.id = sd.discount_id "
                + "where sd.student_id = ? and sd.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.prepareDiscountsContainer();
        while (result.next()) {
            String id = result.getString("sd.id");
            Item item = container.addItem(id);
            Button b = dw.createButton(myUI.getMessage(Messages.DeleteButton), id,
                    Settings.dbStudentDiscount, FontAwesome.MINUS_SQUARE);
            if (!currentUser.isPermitted(Settings.discountsTable + ":" + Settings.actDelete)) {
                b.setEnabled(false);
            }
            item.getItemProperty(Settings.button).setValue(b);
            ComboBox cb = dw.createComboboxDisc(result.getInt("d.id"),
                    myUI.getMessage(Messages.Title), id);
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(cb);
            if (result.getString("d.discount_type_id").equals("1")
                    || result.getString("d.discount_type_id").equals("2")) {
                item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                        dw.createTextFieldDisc(result.getDouble("d.amount"), null,
                                myUI.getMessage(Messages.Amount), id, true));
            } else if (result.getString("d.discount_type_id").equals("3")
                    || result.getString("d.discount_type_id").equals("4")) {
                item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                        dw.createTextFieldDisc(result.getDouble("sd.free_entry_amount"),
                                (Double) cb.getContainerProperty(result.getInt("d.id"),
                                        myUI.getMessage(Messages.Amount)).getValue(),
                                myUI.getMessage(Messages.Amount), id,
                                !currentUser.isPermitted(Settings.discountsTable + ":" + Settings.actModify)));
            }
            TextField tf = dw.createTextField(result.getString("sd.note"),
                    myUI.getMessage(Messages.Note), id, new StringLengthValidator(
                            myUI.getMessage(Messages.NotificationWrongValue),
                            null, 150, true), false);
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(tf);
            if (!currentUser.isPermitted(Settings.discountsTable + ":" + Settings.actModify)) {
                tf.setEnabled(false);
                cb.setEnabled(false);
            }
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public void execSQL_Discounts_by_classes(MyVaadinUI myUI, int year_id,
                                             String edu_statuses_ids, ClassDiscountsReport cdr) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT sd.discount_id, COUNT(sd.discount_id) AS disc_quantity, "
                + "SUM(sd.discount_value) AS disc_amount, "
                + "SUM(c.amount) AS contr_amount");
        Iterator<?> class_iter = ((Set<?>) cdr.classTable.getValue()).iterator();
        while (class_iter.hasNext()) {
            Object next = class_iter.next();
            sql.append(", COUNT(IF(vcs.class_name_id = ")
                    .append(next).append(", 1, NULL)) AS disc_quantity")
                    .append(next).append(", ").append("SUM(IF(vcs.class_name_id = ")
                    .append(next).append(", sd.discount_value, 0)) AS disc_amount")
                    .append(next).append(", ").append("SUM(IF(vcs.class_name_id = ")
                    .append(next).append(", c.amount, 0)) AS contr_amount").append(next).append(" ");
        }
        sql.append(" FROM student_discount AS sd "
                        + "LEFT JOIN student AS st ON sd.student_id = st.id "
                        + "LEFT JOIN student_contract AS sc ON sd.student_id = sc.student_id AND sd.year_id = sc.year_id "
                        + "LEFT JOIN contract AS c ON c.id = sc.contract_id "
                        + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                        + "WHERE sd.year_id = ? AND vcs.class_name_id IN (")
                .append(Settings.convertCollectionToStr(((Set<?>) cdr.classTable.getValue())))
                .append(") ").append("AND sd.discount_id IN (")
                .append(Settings.convertCollectionToStr(((Set<?>) cdr.discountsTable.getValue())))
                .append(") ").append("AND vcs.education_status_id IN (")
                .append(edu_statuses_ids).append(") ")
                .append("GROUP BY sd.discount_id");
        PreparedStatement stat = dbCon.prepareStatement(sql.toString());
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Discount), String.class, 0);
        class_iter = ((Set<?>) cdr.classTable.getValue()).iterator();
        while (class_iter.hasNext()) {
            Object next = class_iter.next();
            container.addContainerProperty(cdr.classTable.getContainerProperty(
                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                    + myUI.getMessage(Messages.Students), Integer.class, 0);
            container.addContainerProperty(cdr.classTable.getContainerProperty(
                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                    + myUI.getMessage(Messages.DiscountAmount), Double.class, 0.0);
            container.addContainerProperty(cdr.classTable.getContainerProperty(
                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                    + myUI.getMessage(Messages.Average) + "%", Double.class, 0.0);
        }
        container.addContainerProperty(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.Students), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.DiscountAmount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.Average) + "%", Double.class, 0.0);
        cdr.dataTable.setContainerDataSource(container);
        for (Object next : (Set<?>) cdr.discountsTable.getValue()) {
            Item item = container.addItem(next);
            item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(
                    cdr.discountsTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue());
        }
        cdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Discount),
                myUI.getMessage(Messages.Total));
        cdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.Average) + "%", "100.00");
        int counter = 0;
        while (result.next()) {
            Item item = container.getItem(result.getInt("sd.discount_id"));
            class_iter = ((Set<?>) cdr.classTable.getValue()).iterator();
            String footerVal;
            while (class_iter.hasNext()) {
                Object next = class_iter.next();
                item.getItemProperty(cdr.classTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                        + myUI.getMessage(Messages.Students)).setValue(
                        result.getInt("disc_quantity" + next));
                footerVal = cdr.dataTable.getColumnFooter(cdr.classTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                        + myUI.getMessage(Messages.Students));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                            + myUI.getMessage(Messages.Students), (Integer.parseInt(footerVal)
                            + result.getInt("disc_quantity" + next)) + "");
                } else {
                    cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                                    + myUI.getMessage(Messages.Students),
                            result.getInt("disc_quantity" + next) + "");
                }
                item.getItemProperty(cdr.classTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                        + myUI.getMessage(Messages.DiscountAmount)).setValue(
                        result.getDouble("disc_amount" + next));
                footerVal = cdr.dataTable.getColumnFooter(
                        cdr.classTable.getContainerProperty(
                                next, myUI.getMessage(Messages.Title)).getValue() + " "
                                + myUI.getMessage(Messages.DiscountAmount));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    try {
                        cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                                        + myUI.getMessage(Messages.DiscountAmount),
                                Settings.dFormat2.format(Settings.dFormat2.parse(footerVal).doubleValue()
                                        + result.getDouble("disc_amount" + next)));
                    } catch (ParseException e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                } else {
                    cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                                    + myUI.getMessage(Messages.DiscountAmount),
                            Settings.dFormat2.format(result.getDouble("disc_amount" + next)));
                }
                if (result.getDouble("contr_amount" + next) != 0.0) {
                    double val = result.getDouble("disc_amount" + next)
                            / result.getDouble("contr_amount" + next) * 100.0;
                    item.getItemProperty(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                            + myUI.getMessage(Messages.Average) + "%").setValue(val);
                    footerVal = cdr.dataTable.getColumnFooter(cdr.classTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                            + myUI.getMessage(Messages.Average) + "%");
                    if (footerVal == null) {
                        footerVal = "0";
                    }
                    if (counter != 0) {
                        try {
                            cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                                            + myUI.getMessage(Messages.Average) + "%",
                                    Settings.dFormat2.format(Settings.dFormat2.parse(footerVal).doubleValue() + val));
                        } catch (ParseException e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        cdr.dataTable.setColumnFooter(cdr.classTable.getContainerProperty(
                                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                                        + myUI.getMessage(Messages.Average) + "%",
                                Settings.dFormat2.format(val));
                    }
                }
            }
            item.getItemProperty(myUI.getMessage(Messages.Total) + " "
                    + myUI.getMessage(Messages.Students)).setValue(
                    result.getInt("disc_quantity"));
            footerVal = cdr.dataTable.getColumnFooter(
                    myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.Students));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                cdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                        + myUI.getMessage(Messages.Students), (Integer.parseInt(footerVal)
                        + result.getInt("disc_quantity")) + "");
            } else {
                cdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                                + myUI.getMessage(Messages.Students),
                        result.getInt("disc_quantity") + "");
            }
            item.getItemProperty(myUI.getMessage(Messages.Total) + " "
                    + myUI.getMessage(Messages.DiscountAmount)).setValue(
                    result.getDouble("disc_amount"));
            footerVal = cdr.dataTable.getColumnFooter(
                    myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.DiscountAmount));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                try {
                    cdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                                    + myUI.getMessage(Messages.DiscountAmount),
                            Settings.dFormat2.format(Settings.dFormat2.parse(footerVal).doubleValue()
                                    + result.getDouble("disc_amount")));
                } catch (ParseException e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                cdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                                + myUI.getMessage(Messages.DiscountAmount),
                        Settings.dFormat2.format(result.getDouble("disc_amount")));
            }
            counter++;
        }
        Iterator<?> iter = cdr.dataTable.getContainerDataSource().getItemIds().iterator();
        double totalDiscAmount = 0.0;
        String footerVal = cdr.dataTable.getColumnFooter(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.DiscountAmount));
        if (footerVal != null) {
            try {
                totalDiscAmount = Settings.dFormat2.parse(footerVal).doubleValue();
            } catch (ParseException e) {
                logger.error(e);
                logger.catching(e);
            }
            while (iter.hasNext()) {
                Object next = iter.next();
                if (totalDiscAmount != 0.0) {
                    cdr.dataTable.getContainerProperty(next, myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.Average) + "%").setValue((Double) cdr.dataTable.getContainerProperty(next, myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.DiscountAmount)).getValue() / (totalDiscAmount / 100));
                }
            }
        }
    }

    public void execSQL_Discounts_by_schools(MyVaadinUI myUI, int year_id,
                                             String edu_statuses_ids, SchoolDiscountsReport sdr) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT sd.discount_id, COUNT(sd.discount_id) AS disc_quantity, "
                        + "SUM(sd.discount_value) AS disc_amount, "
                        + "SUM(c.amount) AS contr_amount");
        Iterator<?> school_iter = ((Set<?>) sdr.schoolTable.getValue()).iterator();
        while (school_iter.hasNext()) {
            Object next = school_iter.next();
            sql.append(", COUNT(IF(st.school_id = ").append(next).append(", 1, NULL)) AS disc_quantity")
                    .append(next).append(", ").append("SUM(IF(st.school_id = ").append(next)
                    .append(", sd.discount_value, 0)) AS disc_amount").append(next).append(", ")
                    .append("SUM(IF(st.school_id = ").append(next).append(", c.amount, 0)) AS contr_amount")
                    .append(next).append(" ");
        }
        sql.append(" FROM student_discount AS sd "
                        + "LEFT JOIN student AS st ON sd.student_id = st.id "
                        + "LEFT JOIN student_contract AS sc ON sd.student_id = sc.student_id AND sd.year_id = sc.year_id "
                        + "LEFT JOIN contract AS c ON c.id = sc.contract_id "
                        + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                        + "WHERE sd.year_id = ? AND st.school_id IN (")
                .append(Settings.convertCollectionToStr(((Set<?>) sdr.schoolTable.getValue())))
                .append(") ").append("AND sd.discount_id IN (")
                .append(Settings.convertCollectionToStr(((Set<?>) sdr.discountsTable.getValue()))).append(") ")
                .append("AND vcs.education_status_id IN (").append(edu_statuses_ids).append(") ")
                .append("GROUP BY sd.discount_id");
        PreparedStatement stat = dbCon.prepareStatement(sql.toString());
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Discount), String.class, 0);
        school_iter = ((Set<?>) sdr.schoolTable.getValue()).iterator();
        while (school_iter.hasNext()) {
            Object next = school_iter.next();
            container.addContainerProperty(sdr.schoolTable.getContainerProperty(
                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                    + myUI.getMessage(Messages.Students), Integer.class, 0);
            container.addContainerProperty(sdr.schoolTable.getContainerProperty(
                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                    + myUI.getMessage(Messages.DiscountAmount), Double.class, 0.0);
            container.addContainerProperty(sdr.schoolTable.getContainerProperty(
                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                    + myUI.getMessage(Messages.Average) + "%", Double.class, 0.0);
        }
        container.addContainerProperty(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.Students), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.DiscountAmount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.Average) + "%", Double.class, 0.0);
        sdr.dataTable.setContainerDataSource(container);
        for (Object next : (Set<?>) sdr.discountsTable.getValue()) {
            Item item = container.addItem(next);
            item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(
                    sdr.discountsTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue());
        }
        sdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Discount),
                myUI.getMessage(Messages.Total));
        sdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.Average) + "%", "100.00");
        int counter = 0;
        while (result.next()) {
            Item item = container.getItem(result.getInt("sd.discount_id"));
            school_iter = ((Set<?>) sdr.schoolTable.getValue()).iterator();
            String footerVal;
            while (school_iter.hasNext()) {
                Object next = school_iter.next();
                item.getItemProperty(sdr.schoolTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                        + myUI.getMessage(Messages.Students)).setValue(
                        result.getInt("disc_quantity" + next));
                footerVal = sdr.dataTable.getColumnFooter(sdr.schoolTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                        + myUI.getMessage(Messages.Students));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                            + myUI.getMessage(Messages.Students), (Integer.parseInt(footerVal)
                            + result.getInt("disc_quantity" + next)) + "");
                } else {
                    sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                                    + myUI.getMessage(Messages.Students),
                            result.getInt("disc_quantity" + next) + "");
                }
                item.getItemProperty(sdr.schoolTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                        + myUI.getMessage(Messages.DiscountAmount)).setValue(
                        result.getDouble("disc_amount" + next));
                footerVal = sdr.dataTable.getColumnFooter(
                        sdr.schoolTable.getContainerProperty(
                                next, myUI.getMessage(Messages.Title)).getValue() + " "
                                + myUI.getMessage(Messages.DiscountAmount));
                if (footerVal == null) {
                    footerVal = "0";
                }
                if (counter != 0) {
                    try {
                        sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                                        + myUI.getMessage(Messages.DiscountAmount),
                                Settings.dFormat2.format(Settings.dFormat2.parse(footerVal).doubleValue()
                                        + result.getDouble("disc_amount" + next)));
                    } catch (ParseException e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                } else {
                    sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                                    next, myUI.getMessage(Messages.Title)).getValue() + " "
                                    + myUI.getMessage(Messages.DiscountAmount),
                            Settings.dFormat2.format(result.getDouble("disc_amount" + next)));
                }
                if (result.getDouble("contr_amount" + next) != 0.0) {
                    double val = result.getDouble("disc_amount" + next)
                            / result.getDouble("contr_amount" + next) * 100.0;
                    item.getItemProperty(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                            + myUI.getMessage(Messages.Average) + "%").setValue(val);
                    footerVal = sdr.dataTable.getColumnFooter(sdr.schoolTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                            + myUI.getMessage(Messages.Average) + "%");
                    if (footerVal == null) {
                        footerVal = "0";
                    }
                    if (counter != 0) {
                        try {
                            sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                                            next, myUI.getMessage(Messages.Title)).getValue() + " "
                                            + myUI.getMessage(Messages.Average) + "%",
                                    Settings.dFormat2.format(Settings.dFormat2.parse(footerVal).doubleValue()
                                            + val));
                        } catch (ParseException e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        sdr.dataTable.setColumnFooter(sdr.schoolTable.getContainerProperty(
                                        next, myUI.getMessage(Messages.Title)).getValue() + " "
                                        + myUI.getMessage(Messages.Average) + "%",
                                Settings.dFormat2.format(val));
                    }
                }
            }
            item.getItemProperty(myUI.getMessage(Messages.Total) + " "
                    + myUI.getMessage(Messages.Students)).setValue(
                    result.getInt("disc_quantity"));
            footerVal = sdr.dataTable.getColumnFooter(
                    myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.Students));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                sdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                        + myUI.getMessage(Messages.Students), (Integer.parseInt(footerVal)
                        + result.getInt("disc_quantity")) + "");
            } else {
                sdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                                + myUI.getMessage(Messages.Students),
                        result.getInt("disc_quantity") + "");
            }
            item.getItemProperty(myUI.getMessage(Messages.Total) + " "
                    + myUI.getMessage(Messages.DiscountAmount)).setValue(
                    result.getDouble("disc_amount"));
            footerVal = sdr.dataTable.getColumnFooter(
                    myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.DiscountAmount));
            if (footerVal == null) {
                footerVal = "0";
            }
            if (counter != 0) {
                try {
                    sdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                                    + myUI.getMessage(Messages.DiscountAmount),
                            Settings.dFormat2.format(Settings.dFormat2.parse(footerVal).doubleValue()
                                    + result.getDouble("disc_amount")));
                } catch (ParseException e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                sdr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total) + " "
                                + myUI.getMessage(Messages.DiscountAmount),
                        Settings.dFormat2.format(result.getDouble("disc_amount")));
            }

            counter++;
        }
        Iterator<?> iter = sdr.dataTable.getContainerDataSource().getItemIds().iterator();
        double totalDiscAmount = 0.0;
        String footerVal = sdr.dataTable.getColumnFooter(myUI.getMessage(Messages.Total) + " "
                + myUI.getMessage(Messages.DiscountAmount));
        if (footerVal != null) {
            try {
                totalDiscAmount = Settings.dFormat2.parse(footerVal).doubleValue();
            } catch (ParseException e) {
                logger.error(e);
                logger.catching(e);
            }
            while (iter.hasNext()) {
                Object next = iter.next();
                if (totalDiscAmount != 0.0) {
                    sdr.dataTable.getContainerProperty(next, myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.Average) + "%").setValue((Double) sdr.dataTable.getContainerProperty(next, myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.DiscountAmount)).getValue() / (totalDiscAmount / 100));
                }
            }
        }
    }

    public int exec_disc_count(int id) throws SQLException {
        String sql = "SELECT count(discount_id) FROM student_discount "
                + "where discount_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        int i = 0;
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            i = result.getInt("count(discount_id)");
        }
        return i;
    }

    public int exec_update_emp_id(int emp_id, String id) throws SQLException {
        String sql = "UPDATE student_discount SET employee_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, emp_id);
        stat.setString(2, id);
        return stat.executeUpdate();
    }
}
