/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.domain.EmployeeOrder;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.ui.EmployeeDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

public class DbEmployeeOrder extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeOrder.class);

    public DbEmployeeOrder() throws Exception {
        super();
    }

    public int exec_insert(EmployeeOrder eo) throws SQLException {


        String sql = "INSERT INTO hr_employee_order (hr_orders_id, employee_id,"
                + "school_id, hr_position_id, class_name_id, "
                + "to_date, note, modification_date, m_employee_id, from_date, from_to_school_id, can_not_delete) "
                + "VALUES(?,?,?,?,?,?,?,NOW(),?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eo.getOrder_id());
        stat.setInt(2, eo.getEmployee_id());
        stat.setInt(3, eo.getSchool_id());
        stat.setInt(4, eo.getPosition_id());
        if (eo.getClass_name_id() != 0) {
            stat.setInt(5, eo.getClass_name_id());
        } else {
            stat.setNull(5, Types.INTEGER);
        }
        if (eo.getTo_date() != null) {
            stat.setDate(6, new java.sql.Date(eo.getTo_date().getTime()));
        } else {
            stat.setNull(6, Types.DATE);
        }
        if (eo.getNote() != null) {
            stat.setString(7, eo.getNote());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        stat.setInt(8, eo.getM_employee_id());
        stat.setDate(9, new java.sql.Date(eo.getFrom_date().getTime()));
        if (eo.getFrom_to_school_id() != 0) {
            stat.setInt(10, eo.getFrom_to_school_id());
        } else {
            stat.setNull(10, Types.INTEGER);
        }
        stat.setInt(11, eo.getCan_not_delete());
        int st = stat.executeUpdate();
        if (st != 0) {
            int last_id = getLastInsertedId();
            if (eo.getOrder_id() != 2 && eo.getOrder_id() != 3) {
                eo.setEffected_by_id(last_id);
                exec_update_after_insert(eo);
            }
            return last_id;
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeOrder eo) throws SQLException {
        String sql = "update hr_employee_order set school_id = ?, hr_position_id = ?, class_name_id = ?,"
                + "from_date = ?, to_date = ?, note = ?, modification_date=NOW(), m_employee_id = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        int counter = 0;
        stat.setInt(++counter, eo.getSchool_id());
        stat.setInt(++counter, eo.getPosition_id());
        if (eo.getClass_name_id() != 0) {
            stat.setInt(++counter, eo.getClass_name_id());
        } else {
            stat.setNull(++counter, Types.INTEGER);
        }
        stat.setDate(++counter, new java.sql.Date(eo.getFrom_date().getTime()));
        if (eo.getTo_date() != null) {
            stat.setDate(++counter, new java.sql.Date(eo.getTo_date().getTime()));
        } else {
            stat.setNull(++counter, Types.DATE);
        }
        if (eo.getNote() != null) {
            stat.setString(++counter, eo.getNote());
        } else {
            stat.setNull(++counter, Types.VARCHAR);
        }
        stat.setInt(++counter, eo.getM_employee_id());
        stat.setInt(++counter, eo.getId());
        int st = stat.executeUpdate();
        if (st != 0) {
            if (eo.getOrder_id() != 2 && eo.getOrder_id() != 3) {
                exec_update_after_update(eo.getId(), eo.getFrom_date());
            }
        }
        return st;
    }

    public int exec_update_after_insert(EmployeeOrder eo) throws SQLException {

        String sql = "update hr_employee_order as eo "
                + "left join hr_orders as o on o.id = eo.hr_orders_id "
                + "set eo.effected_by_id = ?, eo.to_date = ? "
                + "WHERE o.working_status_id IS NOT NULL and eo.id != ? and eo.to_date IS NULL and eo.employee_id = ? and eo.school_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eo.getEffected_by_id());
        stat.setDate(2, new java.sql.Date(eo.getFrom_date().getTime()));
        stat.setInt(3, eo.getEffected_by_id());
        stat.setInt(4, eo.getEmployee_id());
        stat.setInt(5, eo.getSchool_id());
        return stat.executeUpdate();
    }

    public int exec_update_after_update(int effected_by_id, Date to_date) throws SQLException {


        String sql = "update hr_employee_order as eo set eo.to_date = ? WHERE eo.effected_by_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(to_date.getTime()));
        stat.setInt(2, effected_by_id);
        return stat.executeUpdate();
    }

    public int exec_update_before_delete(String effected_by_id) throws SQLException {
        String sql = "update hr_employee_order as eo set eo.to_date=NULL "
                + "WHERE eo.effected_by_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, effected_by_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execOrderTypesSel(MyVaadinUI myUI, String except_ids) throws SQLException {
        String sql = "SELECT o.id, o.name, o.working_status_id, o.visible_hr_orders, ws.name from hr_orders as o "
                + "left join working_status as ws on ws.id = o.working_status_id where o.id != 3 ";
        if (except_ids != null) {
            sql += "and o.id IN (" + except_ids + ")";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.working_status_id, Integer.class, 0);
        container.addContainerProperty(Settings.visible_hr_orders, String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.WorkingStatus), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("o.id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                    result.getString("o.name"));
            item.getItemProperty(Settings.working_status_id).setValue(
                    result.getInt("o.working_status_id"));
            item.getItemProperty(Settings.visible_hr_orders).setValue(
                    result.getString("o.visible_hr_orders"));
            item.getItemProperty(myUI.getMessage(Messages.WorkingStatus)).setValue(
                    result.getString("ws.name"));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id, int school_id, EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT eo.id, o.id, o.name, eo.from_date, eo.to_date, eo.note, eo.effected_by_id, eo.can_not_delete, "
                + "CONCAT(cn.name, ' - ', cln.name) AS details, cln.id AS details_id "
                + "FROM hr_employee_order AS eo "
                + "LEFT JOIN hr_orders AS o ON o.id = eo.hr_orders_id "
                + "LEFT JOIN class_name AS cln ON cln.id = eo.class_name_id LEFT JOIN class_number AS cn ON cn.id = cln.class_number_id "
                + "WHERE eo.employee_id = ? and eo.school_id = ? and o.id = 3 order by eo.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareSupervisionContainer();
        while (result.next()) {
            String id = result.getString("eo.id");
            Item item = container.addItem(id);

            item.getItemProperty(Settings.button).setValue(edv.createButton(myUI.getMessage(Messages.DeleteButton),
                    id, Settings.dbEmployeeOrder, FontAwesome.MINUS_SQUARE));
            ComboBox cb = null;
            try {
                cb = edv.createCombobox(0, myUI.getMessage(Messages.ClassName), null, true);
                DbClassName dbcn = new DbClassName();
                dbcn.connect();
                cb.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
                dbcn.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            if (cb != null) {
                cb.setValue(result.getInt("details_id"));
                item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(cb);
            }
            DateField df = edv.createDateField(result.getDate("eo.from_date"),
                    myUI.getMessage(Messages.FromDate), null, true, Settings.datePattern, Resolution.DAY);
            df.setRangeEnd(new Date());
            if (result.getInt("o.id") == 8) {
                df.setEnabled(false);
            }
            item.getItemProperty(myUI.getMessage(Messages.FromDate)).setValue(df);
            df = edv.createDateField(result.getDate("eo.to_date"),
                    myUI.getMessage(Messages.TillDate), null, false, Settings.datePattern, Resolution.DAY);
            if (result.getInt("o.id") != 3 && result.getInt("o.id") != 2) {
                df.setEnabled(false);
            }
            item.getItemProperty(myUI.getMessage(Messages.TillDate)).setValue(df);
            TextField tf = edv.createTextField(result.getString("eo.Note"),
                    myUI.getMessage(Messages.Note),
                    new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 300, true), false);
            if (result.getInt("o.id") == 8) {
                tf.setEnabled(false);
            }
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(tf);
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id, int school_id, boolean isAdmin, boolean isHR,
                                    EmployeeDefinitionView edv) throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();

        String sql = "SELECT eo.id, o.id, o.name, eo.from_date, eo.to_date, eo.note, eo.effected_by_id, eo.can_not_delete, "
                + "CASE WHEN o.id IN (1, 2, 7) THEN p.name WHEN o.id = 3 THEN CONCAT(cn.name, ' - ', cln.name) "
                + "WHEN o.id IN (5, 8) THEN sch.name_ru ELSE NULL END AS details, CASE WHEN o.id IN (1, 2, 7) THEN p.id "
                + "WHEN o.id = 3 THEN cln.id WHEN o.id IN (5, 8) THEN sch.id ELSE NULL END AS details_id "
                + "FROM hr_employee_order AS eo "
                + "LEFT JOIN hr_orders AS o ON o.id = eo.hr_orders_id "
                + "LEFT JOIN school AS sch ON sch.id = eo.from_to_school_id LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN class_name AS cln ON cln.id = eo.class_name_id LEFT JOIN class_number AS cn ON cn.id = cln.class_number_id "
                + "WHERE eo.employee_id = ? and eo.school_id = ? and o.id != 3 order by eo.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareOrdersContainer();
        String last_id = null;
        while (result.next()) {
            String id = result.getString("eo.id");
            Item item = container.addItem(id);
            Button b = null;

            if (result.getInt("o.id") == 2 || result.getInt("o.id") == 3) {
                b = edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeOrder, FontAwesome.MINUS_SQUARE);
            } else if (result.getInt("eo.can_not_delete") != 1) {
                last_id = result.getString("eo.id");
            }
            item.getItemProperty(Settings.button).setValue(b);
            ComboBox cb = edv.createCombobox(0, myUI.getMessage(Messages.OrderType), null, true);
            cb.setEnabled(false);
            cb.setContainerDataSource(execOrderTypesSel(myUI, result.getString("o.id")));
            cb.setValue(result.getInt("o.id"));
            item.getItemProperty(myUI.getMessage(Messages.OrderType)).setValue(cb);
            cb = null;
            try {
                if (result.getInt("o.id") == 3) {
                    cb = edv.createCombobox(0, myUI.getMessage(Messages.ClassName), null, true);
                    DbClassName dbcn = new DbClassName();
                    dbcn.connect();
                    cb.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
                    dbcn.close();
                } else if (result.getInt("o.id") == 5 || result.getInt("o.id") == 8) {
                    cb = edv.createCombobox(0, myUI.getMessage(Messages.School), null, true);
                    if (result.getInt("o.id") == 8) {
                        cb.setEnabled(false);
                    }
                    DbSchool dbs = new DbSchool();
                    dbs.connect();
                    cb.setContainerDataSource(dbs.execSchoolSel(myUI, myUI.getUser().getSchool().getId()));
                    dbs.close();
                } else if (result.getInt("o.id") == 1 || result.getInt("o.id") == 2 || result.getInt("o.id") == 7) {
                    cb = edv.createCombobox(0, myUI.getMessage(Messages.Position), null, true);
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    cb.setContainerDataSource(dbd.exec_positions_for_select(myUI, isAdmin, isHR));
                    dbd.close();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            if (cb != null) {
                cb.setValue(result.getInt("details_id"));
                item.getItemProperty(myUI.getMessage(Messages.Details)).setValue(cb);
            }
            DateField df = edv.createDateField(result.getDate("eo.from_date"),
                    myUI.getMessage(Messages.FromDate), null, true, Settings.datePattern, Resolution.DAY);
            df.setRangeEnd(new Date());
            if (result.getInt("o.id") == 8) {
                df.setEnabled(false);
            }
            item.getItemProperty(myUI.getMessage(Messages.FromDate)).setValue(df);
            df = edv.createDateField(result.getDate("eo.to_date"),
                    myUI.getMessage(Messages.TillDate), null, false, Settings.datePattern, Resolution.DAY);
            if (result.getInt("o.id") != 3 && result.getInt("o.id") != 2) {
                df.setEnabled(false);
            }
            item.getItemProperty(myUI.getMessage(Messages.TillDate)).setValue(df);
            TextField tf = edv.createTextField(result.getString("eo.Note"),
                    myUI.getMessage(Messages.Note),
                    new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 300, true), false);
            if (result.getInt("o.id") == 8) {
                tf.setEnabled(false);
            }
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(tf);
            item.getItemProperty(Settings.effected_by_id).setValue(result.getString("eo.effected_by_id"));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        if (last_id != null) {
            Item item = container.getItem(last_id);
            if (item != null) {
                if ((Integer) ((ComboBox) item.getItemProperty(myUI.getMessage(Messages.OrderType)).getValue()).getValue() == 8) {
                    last_id = "_" + last_id;
                }
                if (((Integer) ((ComboBox) item.getItemProperty(myUI.getMessage(Messages.OrderType)).getValue()).getValue() != 8
                        && (Integer) ((ComboBox) item.getItemProperty(myUI.getMessage(Messages.OrderType)).getValue()).getValue() != 5)
                        || currentUser.isPermitted(Settings.cnEmployeeTransferView + ":" + Settings.actDelete)) {
                    item.getItemProperty(Settings.button).setValue(edv.createButton(myUI.getMessage(Messages.DeleteButton),
                            last_id, Settings.dbEmployeeOrder, FontAwesome.MINUS_SQUARE));
                }
            }
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id) throws SQLException {


        String sql = "SELECT eo.id, o.name, eo.from_date, eo.to_date, eo.note, "
                + "CASE WHEN o.id IN (1, 2, 7) THEN p.name WHEN o.id = 3 THEN CONCAT(cn.name, ' - ', cln.name) "
                + "WHEN o.id IN (5, 8) THEN sch.name_ru ELSE NULL END AS details "
                + "FROM hr_employee_order AS eo LEFT JOIN hr_orders AS o ON o.id = eo.hr_orders_id "
                + "LEFT JOIN school AS sch ON sch.id = eo.from_to_school_id LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN class_name AS cln ON cln.id = eo.class_name_id LEFT JOIN class_number AS cn ON cn.id = cln.class_number_id "
                + "WHERE eo.employee_id = ? order by eo.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.OrderType), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Details), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FromDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.TillDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getString("eo.id"));
            item.getItemProperty(myUI.getMessage(Messages.OrderType)).setValue(result.getString("o.name"));
            item.getItemProperty(myUI.getMessage(Messages.Details)).setValue(result.getString("details"));
            item.getItemProperty(myUI.getMessage(Messages.FromDate)).setValue(Settings.df.format(result.getDate("eo.from_date")));
            if (result.getDate("eo.to_date") != null) {
                item.getItemProperty(myUI.getMessage(Messages.TillDate)).setValue(Settings.df.format(result.getDate("eo.to_date")));
            }
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getDate("eo.note"));
        }
        return container;
    }
}
