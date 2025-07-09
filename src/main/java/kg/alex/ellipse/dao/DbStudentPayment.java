/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.domain.StudentPayment;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.reports.students.ClassPaymentsReport;
import kg.alex.ellipse.reports.students.InstallmentPlanPaymentsReport;
import kg.alex.ellipse.ui.StudentDefinitionView;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class DbStudentPayment extends BaseDb {

    public DbStudentPayment() throws Exception {
        super();
    }

    public StudentPayment exec_recount_payment(int stud_id, int year_id, int currency_id) throws SQLException {
        String sql = "SELECT sum(if(sp.payment_category_id != 3, " +
                "CASE WHEN ? = sp.acc_currency_id THEN sp.amount WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount * sp.dollar_rate END, 0.0)) - "
                + "sum(if(sp.payment_category_id = 3, CASE WHEN ? = sp.acc_currency_id THEN sp.amount WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount * sp.dollar_rate END, 0.0)) as ttl_payment "
                + "FROM student_payments as sp where sp.student_id = ? and year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currency_id);
        stat.setInt(2, currency_id);
        stat.setInt(3, stud_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        StudentPayment sp = new StudentPayment();
        if (result.next()) {
            sp.setTtl_pay(result.getDouble("ttl_payment"));
        }
        return sp;
    }

    public IndexedContainer execSQL_St_Payments(MyVaadinUI myUI, int stud_id, int year_id,
                                                StudentDefinitionView dw) throws SQLException {

        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT sp.id, sp.amount, sp.dollar_rate, sp.payment_type_id, sp.payment_category_id, "
                + "sp.who_paid, sp.note, sp.modification_date, bank_transaction_id, sp.acc_currency_id, "
                + "if(sp.modification_date <= concat(date(now()), ' 19:00:00') or bank_transaction_id is not null,true, false) as isDisabled "
                + "FROM student_payments as sp where sp.student_id = ? and sp.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.preparePaymentsContainer();
        while (result.next()) {
            boolean isDisabled = false;
            if (!currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeOldTransactions)) {
                isDisabled = result.getBoolean("isDisabled");
            }
            if (result.getInt("bank_transaction_id") != 0) {
                isDisabled = true;
            }
            String id = result.getString("sp.id");
            Item item = container.addItem(id);
            Button btn = dw.createButton(myUI.getMessage(Messages.DeleteButton), id,
                    Settings.dbStudentPayments, FontAwesome.MINUS_SQUARE);
            btn.setEnabled(!isDisabled);
            if (!currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.actDelete)) {
                btn.setEnabled(false);
            }
            item.getItemProperty(Settings.button).setValue(btn);
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
            if (!currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.actModify)) {
                isDisabled = true;
            }
            ComboBox cb = dw.createComboboxPayment(result.getInt("sp.payment_category_id"),
                    myUI.getMessage(Messages.PaymentCategoryType), id);
            cb.setId(myUI.getMessage(Messages.Payments));
            cb.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(Messages.PaymentCategoryType)).setValue(cb);
            item.getItemProperty(myUI.getMessage(Messages.PaymentType)).setValue(
                    dw.createCombobox(result.getInt("sp.payment_type_id"), myUI.getMessage(Messages.PaymentType), id,
                            Settings.dbPaymentType, false, false, false, isDisabled));
            TextField amountUSDTf = dw.createTextFieldDouble(result.getDouble("sp.amount"), 2, myUI.getMessage(Messages.AmountUSD), id);
            amountUSDTf.setId(myUI.getMessage(Messages.Payments));
            amountUSDTf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(Messages.AmountUSD)).setValue(amountUSDTf);
            TextField amountKGSTf = dw.createTextFieldDouble(result.getDouble("sp.amount"), 2, myUI.getMessage(Messages.AmountKGS), id);
            amountKGSTf.setId(myUI.getMessage(Messages.Payments));
            amountKGSTf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(Messages.AmountKGS)).setValue(amountKGSTf);
            if (result.getInt("sp.acc_currency_id") == 1) {
                amountKGSTf.setRequired(true);
                amountUSDTf.removeValueChangeListener(dw);
                amountUSDTf.setValue(null);
                amountUSDTf.setRequired(false);
                amountUSDTf.addValueChangeListener(dw);
            } else {
                amountUSDTf.setRequired(true);
                amountKGSTf.removeValueChangeListener(dw);
                amountKGSTf.setValue(null);
                amountKGSTf.setRequired(false);
                amountKGSTf.addValueChangeListener(dw);
            }
            TextField tf = dw.createTextFieldDouble(result.getDouble("sp.dollar_rate"), 4, myUI.getMessage(Messages.Rate), id);
            tf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(tf);
            tf = dw.createTextField(result.getString("sp.who_paid"), myUI.getMessage(Messages.WhoPaid), id,
                    new StringLengthValidator(
                    myUI.getMessage(Messages.NotificationWrongValue),
                    1, 120, false),  true);
            tf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(Messages.WhoPaid)).setValue(tf);
            DateField df = dw.createDateField(result.getTimestamp("sp.modification_date"),
                    myUI.getMessage(Messages.Date), id, false, true, Settings.dateTimeMinPattern, Resolution.MINUTE);
            df.setId(myUI.getMessage(Messages.Payments));
            df.setEnabled(!isDisabled);
            if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeOldTransactions)) {
                df.setRangeStart(myUI.getUser().getTransactions_start_date());
            } else if (!isDisabled) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, -1441);
                df.setRangeStart(calendar.getTime());
                df.addValidator(new DateRangeValidator(myUI.getMessage(Messages.NotificationWrongValue),
                        df.getRangeStart(), df.getRangeEnd(), Resolution.MINUTE));
            }
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(df);
            tf = dw.createTextField(result.getString("sp.note"), myUI.getMessage(Messages.Note), id,
                    new StringLengthValidator(
                            myUI.getMessage(Messages.NotificationWrongValue),
                            null, 150, true), false);
            tf.setEnabled(!isDisabled);
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(tf);
            Button b = dw.createButton(myUI.getMessage(Messages.Print), id,
                    myUI.getMessage(Messages.Invoice), FontAwesome.PRINT);
            b.setEnabled(currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.actPrint));
            item.getItemProperty(myUI.getMessage(Messages.Print)).setValue(b);
            item.getItemProperty(Settings.acc_currency_id).setValue(result.getInt("sp.acc_currency_id"));
            item.getItemProperty(Settings.old_amount).setValue(result.getDouble("sp.amount"));
            item.getItemProperty(Settings.old_date).setValue(result.getDate("sp.modification_date"));
            item.getItemProperty(Settings.old_category).setValue(result.getInt("sp.payment_category_id"));
        }
        return container;
    }

    public int exec_update(StudentPayment sp) throws SQLException {
        String sql = "update student_payments set year_id = ?, "
                + "amount = ?, payment_type_id = ?, payment_category_id = ?, "
                + "who_paid = ?, note = ?, modification_date = ?, dollar_rate = ?, acc_currency_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sp.getYear_id());
        stat.setDouble(2, sp.getAmount());
        stat.setInt(3, sp.getPayment_type_id());
        stat.setInt(4, sp.getPayment_cat_type_id());
        stat.setString(5, sp.getWho_paid());
        stat.setString(6, sp.getNote());
        stat.setTimestamp(7, new java.sql.Timestamp(sp.getModification_date().getTime()));
        stat.setDouble(8, sp.getRate());
        stat.setInt(9, sp.getCurrency_id());
        stat.setInt(10, sp.getId());
        return stat.executeUpdate();
    }

    public int exec_insert(StudentPayment sp, int order_num) throws SQLException {
        String sql = "INSERT INTO student_payments (student_id, year_id, "
                + "amount, payment_type_id, payment_category_id, employee_id, "
                + "who_paid, order_number, note, modification_date, dollar_rate, acc_currency_id) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sp.getStudent_id());
        stat.setInt(2, sp.getYear_id());
        stat.setDouble(3, sp.getAmount());
        stat.setInt(4, sp.getPayment_type_id());
        stat.setInt(5, sp.getPayment_cat_type_id());
        stat.setInt(6, sp.getEmployee_id());
        stat.setString(7, sp.getWho_paid());
        stat.setInt(8, order_num);
        stat.setString(9, sp.getNote());
        stat.setTimestamp(10, new java.sql.Timestamp(sp.getModification_date().getTime()));
        stat.setDouble(11, sp.getRate());
        stat.setInt(12, sp.getCurrency_id());
        stat.executeUpdate();
        return getLastInsertedId();
    }

    public int exec_delete(String id) throws SQLException {
        String sql = "DELETE FROM student_payments WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public int getMaxOrderNum(int id) throws SQLException {
        int maxValue;
        String sql = "select (max(sp.order_number)+1) as max_plus1 "
                + "from student_payments as sp "
                + "left join student as s on s.id = sp.student_id where "
                + "s.school_id = (SELECT school_id FROM student where id = ?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            maxValue = (result.getInt("max_plus1"));
        } else {
            maxValue = 1;
        }
        return maxValue;
    }

    public String getOrderNum(String id) throws SQLException {
        String orderNum;
        String sql = "select order_number from student_payments where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            orderNum = (result.getString("order_number"));
        } else {
            orderNum = "1";
        }
        return orderNum;
    }

    public IndexedContainer execSQL_Payment(MyVaadinUI myUI, int stud_id, int year_id,
                                            InstallmentPlanPaymentsReport ip) throws SQLException {

        String sql = "SELECT sp.id, sp.amount, sp.who_paid, sp.modification_date, sp.dollar_rate, "
                + "pc.id, pc.name, sp.acc_currency_id, cur.name FROM student_payments as sp "
                + "left join payment_category as pc on sp.payment_category_id = pc.id "
                + "left join acc_currency as cur on sp.acc_currency_id = cur.id "
                + "where sp.student_id = ? and sp.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Currency), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.WhoPaid), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PaymentCategoryType), String.class, null);
        container.addContainerProperty(Settings.payment_category_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sp.id"));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(
                    Settings.df.format((result.getDate("sp.modification_date"))));
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(
                    result.getDouble("sp.dollar_rate"));
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                    result.getDouble("sp.amount"));
            item.getItemProperty(myUI.getMessage(Messages.Currency)).setValue(
                    result.getString("cur.name"));
            item.getItemProperty(myUI.getMessage(Messages.WhoPaid)).setValue(
                    result.getString("sp.who_paid"));
            item.getItemProperty(myUI.getMessage(Messages.PaymentCategoryType)).setValue(
                    result.getString("pc.name"));
            item.getItemProperty(Settings.payment_category_id).setValue(
                    result.getInt("pc.id"));
            double amount;
            if (myUI.getUser().getSchool().getCurrency_id() == result.getInt("acc_currency_id")) {
                amount = result.getDouble("sp.amount");
            } else if (result.getInt("acc_currency_id") == 1) {
                amount = result.getDouble("sp.amount") / result.getDouble("sp.dollar_rate");
            } else {
                amount = result.getDouble("sp.amount") * result.getDouble("sp.dollar_rate");
            }
            if (result.getInt("pc.id") != 3) {
                ip.total_pay += amount;
            } else {
                ip.total_pay -= amount;
            }
        }
        return container;
    }

    public IndexedContainer execSQL_PaymentsByClass(MyVaadinUI myUI, Date from,
                                                    Date till, int year_id, String class_ids, String edu_statuses_ids,
                                                    ClassPaymentsReport cpr) throws SQLException {

        String sql = "SELECT sp.id, sp.modification_date, vcs.class_name, cur.name, sp.dollar_rate, sp.acc_currency_id, "
                + "st.name, st.surname, sp.amount, pc.name, sp.who_paid, sp.payment_category_id "
                + "FROM student_payments AS sp "
                + "LEFT JOIN student AS st ON st.id = sp.student_id "
                + "LEFT JOIN acc_currency AS cur ON cur.id = sp.acc_currency_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "LEFT JOIN payment_category AS pc ON sp.payment_category_id = pc.id "
                + "WHERE vcs.class_name_id IN (" + class_ids + ") "
                + "AND DATE(sp.modification_date) >= ? AND DATE(sp.modification_date) <= ? "
                + "AND sp.year_id = ? AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "ORDER BY vcs.class_number_id, vcs.class_name_id, st.name, st.surname, sp.modification_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setDate(2, new java.sql.Date(from.getTime()));
        stat.setDate(3, new java.sql.Date(till.getTime()));
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Currency), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PaymentCategoryType), String.class, null);
        container.addContainerProperty(Settings.payment_category_id, Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.WhoPaid), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sp.id"));
            item.getItemProperty(myUI.getMessage(Messages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(Messages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    result.getString("vcs.class_name"));
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(
                    result.getDouble("sp.dollar_rate"));
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                    result.getDouble("sp.amount"));
            item.getItemProperty(myUI.getMessage(Messages.Currency)).setValue(
                    result.getString("cur.name"));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(
                    Settings.df.format((result.getDate("sp.modification_date"))));
            item.getItemProperty(myUI.getMessage(Messages.PaymentCategoryType)).setValue(
                    result.getString("pc.name"));
            item.getItemProperty(Settings.payment_category_id).setValue(
                    result.getInt("sp.payment_category_id"));
            item.getItemProperty(myUI.getMessage(Messages.WhoPaid)).setValue(
                    result.getString("sp.who_paid"));
            double amount;
            if (myUI.getUser().getSchool().getCurrency_id() == result.getInt("acc_currency_id")) {
                amount = result.getDouble("sp.amount");
            } else if (result.getInt("acc_currency_id") == 1) {
                amount = result.getDouble("sp.amount") / result.getDouble("sp.dollar_rate");
            } else {
                amount = result.getDouble("sp.amount") * result.getDouble("sp.dollar_rate");
            }
            if (result.getInt("sp.payment_category_id") != 3) {
                cpr.total += amount;
            } else {
                cpr.total -= amount;
            }
        }
        return container;
    }

    public double exec_get_difference(int st_id, int year_id, int currency_id) throws SQLException {
        double ip = 0;
        String sql = "SELECT ifnull(SUM(IF(payment_category_id != 3, CASE WHEN ? = sp.acc_currency_id THEN sp.amount WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount * sp.dollar_rate END, 0)) - "
                + "SUM(IF(payment_category_id = 3, CASE WHEN ? = sp.acc_currency_id THEN sp.amount WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount * sp.dollar_rate END, 0)), 0.0)  as total "
                + "FROM student_payments as sp where sp.student_id = ? and sp.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currency_id);
        stat.setInt(2, currency_id);
        stat.setInt(3, st_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            ip = (result.getDouble("total"));
        }
        return ip;
    }

    public String execGetWeeklyPaid(String students, int scl_id, int year_id)
            throws SQLException {
        String sql = "SELECT count(distinct st.id) as st, "
                + "ifnull(sum(if(sp.payment_category_id = 3, -sp.amount, sp.amount)),0.00) as week_paid FROM student_payments as sp "
                + "left join student as st on st.id = sp.student_id "
                + "where st.school_id = ? and sp.year_id = ? and "
                + "yearweek(date(sp.modification_date), 1) = YEARWEEK(curdate(),1)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        String s = null;
        while (result.next()) {
            s = result.getString("st") + " " + students + " / " + Settings.dFormat2.format(result.getDouble("week_paid"));
        }
        return s;
    }

    public String execGetMonthlyPaid(String students, int scl_id, int year_id)
            throws SQLException {

        String sql = "SELECT count(distinct st.id) as st, "
                + "ifnull(sum(if(sp.payment_category_id = 3, -sp.amount, sp.amount)),0.00) as month_paid FROM student_payments as sp "
                + "left join student as st on st.id = sp.student_id "
                + "where st.school_id = ? and sp.year_id = ? and "
                + "MONTH(sp.modification_date) = MONTH(CURRENT_DATE())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        String s = null;
        while (result.next()) {
            s = result.getString("st") + " " + students + " / " + Settings.dFormat2.format(result.getDouble("month_paid"));
        }
        return s;
    }

    public int exec_update_emp_id(int emp_id, String id) throws SQLException {
        String sql = "UPDATE student_payments SET employee_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, emp_id);
        stat.setString(2, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_Payments(MyVaadinUI myUI, int currency_id, int school_id, Date from, Date till, Table t) throws SQLException {

        String sql = "SELECT sp.id, sp.bank_transaction_id, sp.modification_date, sp.dollar_rate, " +
                "if(sp.acc_currency_id = 1 and sp.dollar_rate != 0.0, sp.amount * sp.dollar_rate,  sp.amount) as amount, c.name, st.login, " +
                "CONCAT(st.surname, ' ', st.name, ' ', IFNULL(st.middle_name, '')) AS fullname " +
                "FROM student_payments AS sp " +
                "LEFT JOIN student AS st ON st.id = sp.student_id " +
                "LEFT JOIN acc_currency AS c ON c.id = sp.acc_currency_id " +
                "WHERE sp.bank_transaction_id IS NOT NULL AND sp.acc_currency_id = ? AND st.school_id = ? " +
                "AND DATE(sp.modification_date) BETWEEN date(?) AND date(?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currency_id);
        stat.setInt(2, school_id);
        stat.setDate(3, new java.sql.Date(from.getTime()));
        stat.setDate(4, new java.sql.Date(till.getTime()));
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.StudentId), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Currency), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.TransactionNumber), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Status), String.class, "Успешно");
        t.setContainerDataSource(container);
        double total = 0.0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("sp.id"));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.dtmf.format((result.getTimestamp("sp.modification_date"))));
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(result.getDouble("amount"));
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(result.getDouble("sp.dollar_rate"));
            item.getItemProperty(myUI.getMessage(Messages.Currency)).setValue(result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(Messages.TransactionNumber)).setValue(result.getString("sp.bank_transaction_id"));
            item.getItemProperty(myUI.getMessage(Messages.StudentId)).setValue(result.getString("st.login"));
            item.getItemProperty(myUI.getMessage(Messages.FullName)).setValue(result.getString("fullname"));
            total += result.getDouble("amount");
        }
        t.setColumnFooter(myUI.getMessage(Messages.Amount), Settings.dFormat2.format(total));
        t.setColumnFooter(myUI.getMessage(Messages.TransactionNumber), container.size() + "");
        return container;
    }

    public IndexedContainer execSQL_Payments_group_by_date(MyVaadinUI myUI, int currency_id, int school_id, Date from, Date till, Table t) throws SQLException {

        String sql = "SELECT sp.modification_date, " +
                "sum(if(sp.acc_currency_id = 1 and sp.dollar_rate != 0.0, sp.amount * sp.dollar_rate,  sp.amount)) as amount, " +
                "count(sp.id) as quantity, c.name FROM student_payments AS sp " +
                "LEFT JOIN student AS st ON st.id = sp.student_id " +
                "left join acc_currency as c on c.id = sp.acc_currency_id " +
                "WHERE sp.bank_transaction_id IS NOT NULL AND sp.acc_currency_id = ? AND st.school_id = ? " +
                "AND DATE(sp.modification_date) BETWEEN date(?) AND date(?) " +
                "group by DATE(sp.modification_date)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currency_id);
        stat.setInt(2, school_id);
        stat.setDate(3, new java.sql.Date(from.getTime()));
        stat.setDate(4, new java.sql.Date(till.getTime()));
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Currency), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.TransactionsQuantity), Integer.class, 0);
        t.setContainerDataSource(container);
        double totalAmount = 0.0;
        int totalQuantity = 0;
        while (result.next()) {
            Item item = container.addItem(Settings.df.format((result.getDate("sp.modification_date"))));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format((result.getDate("sp.modification_date"))));
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(result.getDouble("amount"));
            item.getItemProperty(myUI.getMessage(Messages.TransactionsQuantity)).setValue(result.getInt("quantity"));
            item.getItemProperty(myUI.getMessage(Messages.Currency)).setValue(result.getString("c.name"));
            totalAmount += result.getDouble("amount");
            totalQuantity += result.getDouble("quantity");
        }
        t.setColumnFooter(myUI.getMessage(Messages.Amount), Settings.dFormat2.format(totalAmount));
        t.setColumnFooter(myUI.getMessage(Messages.TransactionsQuantity), totalQuantity + "");
        return container;
    }
}
