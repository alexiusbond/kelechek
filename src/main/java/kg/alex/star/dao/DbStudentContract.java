/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.domain.ContractInfo;
import kg.alex.star.domain.Month;
import kg.alex.star.domain.StudentContract;
import kg.alex.star.i18n.Messages;
import kg.alex.star.reports.students.ClassListReport;
import kg.alex.star.reports.students.DebtReport;
import kg.alex.star.reports.students.DiscountsReport;
import kg.alex.star.reports.students.YearMonthReport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author alex
 */
public class DbStudentContract extends BaseDb {

    public DbStudentContract() throws Exception {
        super();
    }

    public int exec_insert_st_contract(MyVaadinUI myUi, StudentContract c) throws SQLException {
        String sql = "INSERT IGNORE INTO student_contract (student_id, year_id, contract_id, debt, employee_id, " +
                "modification_date, activity_status_id, contr_with_disc, contract_number, creation_date) "
                + "VALUES(?,?,?,?,?,NOW(),?,?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, c.getStudent_id());
        stat.setInt(2, c.getYear_id());
        stat.setInt(3, c.getContract_id());
        stat.setDouble(4, c.getDebt());
        stat.setInt(5, c.getEmployee_id());
        stat.setInt(6, c.getStatus_id());
        stat.setDouble(7, c.getContr_with_disc());
        stat.setInt(8, exec_next_contract_number(myUi.getUser().getSchool().getId(),
                myUi.getUser().getCurrent_year().getId()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update_st_contract(StudentContract c)
            throws SQLException {
        String sql = "UPDATE student_contract SET contract_id = ?,debt = ?,employee_id = ?,"
                + "modification_date=NOW(),activity_status_id = ?,contr_with_disc = ? "
                + "where student_id = ? and year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, c.getContract_id());
        stat.setDouble(2, c.getDebt());
        stat.setInt(3, c.getEmployee_id());
        stat.setInt(4, c.getStatus_id());
        stat.setDouble(5, c.getContr_with_disc());
        stat.setInt(6, c.getStudent_id());
        stat.setInt(7, c.getYear_id());
        return stat.executeUpdate();
    }

    public int exec_delete(int stud_id, int year_id) throws SQLException {
        String sql = "DELETE FROM student_contract WHERE student_id = ? and year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        return stat.executeUpdate();
    }

    public StudentContract exec_recount_contract(int stud_id, int year_id) throws SQLException {
        String sql = "SELECT sc.contract_id, sc.creation_date, sc.debt, c.amount, sc.contr_with_disc, vc.details, vc.amount, "
                + "sum(ip.amount) as plan_debt FROM student_contract as sc "
                + "left join contract as c on c.id = sc.contract_id "
                + "LEFT JOIN view_corrections as vc on vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN student_installement_plan as ip on sc.student_id = ip.student_id "
                + "and sc.year_id = ip.year_id AND ((ip.is_visible=1 and ip.date_of_payment <= now()) or ip.is_visible=0) "
                + "where sc.student_id = ? and sc.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        StudentContract c = new StudentContract();
        if (result.next()) {
            c.setContract_id(result.getInt("sc.contract_id"));
            c.setDebt(result.getDouble("sc.debt"));
            c.setAmount(result.getDouble("c.amount"));
            c.setContr_with_disc(result.getDouble("sc.contr_with_disc"));
            c.setPlan_debt(result.getDouble("plan_debt"));
            c.setCorrection(result.getDouble("vc.amount"));
            c.setCorrectionDetails(result.getString("vc.details"));
            c.setCreationDate(result.getDate("sc.creation_date"));
        }
        return c;
    }

    public int exec_update_status(int student_id, int activity_status, int employee_id)
            throws SQLException {
        String sql = "UPDATE student_contract SET activity_status_id = ?,"
                + "employee_id = ?, modification_date = NOW() WHERE student_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, activity_status);
        stat.setInt(2, employee_id);
        stat.setInt(3, student_id);
        return stat.executeUpdate();
    }

    public int exec_update_status_by_id(int student_id, int activity_status, int employee_id)
            throws SQLException {
        String sql = "UPDATE student_contract SET activity_status_id = ?,"
                + "employee_id = ?, modification_date=NOW() WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, activity_status);
        stat.setInt(2, employee_id);
        stat.setInt(3, exec_last_by_student_id(student_id));
        return stat.executeUpdate();
    }

    public int exec_last_by_student_id(int student_id) throws SQLException {
        int id = 0;
        String sql = "SELECT max(id) from student_contract where student_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, student_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            if (!result.wasNull()) {
                id = result.getInt("max(id)");
            }
        }
        return id;
    }

    public int execSQL_get_st_contract(int st_id, int year_id) throws SQLException {
        String sql = "SELECT sc.contract_id FROM student_contract as sc "
                + "where sc.student_id = ? and sc.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        int contr_id = 0;
        if (result.next()) {
            contr_id = result.getInt("sc.contract_id");
        }
        return contr_id;
    }

    public Double exec_get_debt(int st_id, int year_id) throws SQLException {
        double debt = 0.0;
        String sql = "SELECT ROUND(IFNULL((SELECT SUM(sc.contr_with_disc) FROM student_contract AS sc "
                + "WHERE sc.student_id = ? and sc.year_id < ?), 0.0), 2) + IFNULL((SELECT SUM(vc.amount) FROM view_corrections AS vc WHERE vc.student_id = ? and vc.year_id < ?), 0.0) "
                + "- ROUND(IFNULL((SELECT SUM(if(sp.payment_category_id != 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) - "
                + "sum(if(sp.payment_category_id = 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) "
                + "FROM student_payments AS sp "
                + "WHERE sp.student_id = ? and sp.year_id < ?), 0.0), 2) AS debt";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        stat.setInt(3, st_id);
        stat.setInt(4, year_id);
        stat.setInt(5, st_id);
        stat.setInt(6, year_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            if (!result.wasNull()) {
                debt = result.getDouble("debt");
            }
        }
        return debt;
    }

    public int execUpdateNetPayments(double ttl_pay, int stud_id, int year_id)
            throws SQLException {
        String sql = "update student_contract set net_payments = ? where student_id = ? and year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDouble(1, ttl_pay);
        stat.setInt(2, stud_id);
        stat.setInt(3, year_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_ClassList(MyVaadinUI myUI, String class_ids, int year_id, Date from_date, Date till_date,
                                              String edu_statuses_ids, ClassListReport clr) throws SQLException {
        String currency = Settings.USD;
        clr.activeStudents = 0;
        clr.discountedStudents = 0;
        clr.prevYearDebts = 0;
        clr.prevYearOverpays = 0;
        clr.nets = 0;
        clr.paid_amounts = 0;
        clr.contracts = 0;
        clr.discounts = 0;
        clr.corrections = 0;
        clr.debts = 0;
        clr.overPays = 0;
        clr.males = 0;
        String sql = "SELECT st.id, st.login, st.name, st.surname, st.gender_id, edu.name, ";
        if (from_date != null && till_date != null) {
            sql += "IF(sc.creation_date >= ? AND sc.creation_date <= ?, c.amount, 0.0) AS contract_amount, ";
        } else if (from_date != null) {
            sql += "IF(sc.creation_date >= ?, c.amount, 0.0) AS contract_amount, ";
        } else if (till_date != null) {
            sql += "IF(sc.creation_date <= ?, c.amount, 0.0) AS contract_amount, ";
        } else {
            sql += "c.amount AS contract_amount, ";
        }
        sql += "IFNULL(sc.debt, " +
                "IFNULL((SELECT SUM(contr_with_disc) FROM student_contract " +
                "WHERE student_id = st.id AND year_id < ?), 0.0) + " +
                "IFNULL((SELECT SUM(amount) FROM view_corrections " +
                "WHERE student_id = st.id AND year_id < ?), 0.0) - " +
                "IFNULL((SELECT SUM(IF(payment_category_id != 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) - " +
                "SUM(IF(payment_category_id = 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) FROM student_payments as sp " +
                "WHERE student_id = st.id AND year_id < ?), 0.0)) AS prev_debt, vc.amount, vc.full_details, " +
                "stud_pay.amount AS net_payments, edu.id, sr.fullname, sr.phone, sr.address, rel.name, ";
        if (from_date != null && till_date != null) {
            sql += "(select get_contract_with_discounts(c.amount, st.id, ?, ?, ?)) as contr_with_disc, " +
                    "GROUP_CONCAT(DISTINCT " +
                    "CASE WHEN (sd.creation_date < ? OR sd.creation_date > ?) THEN NULL " +
                    "WHEN (d.discount_type_id = 1) THEN CONCAT(d.name, ' - ', d.amount, '%') " +
                    "WHEN (d.discount_type_id = 2) THEN CONCAT(d.name, ' - ', d.amount, ' " + currency + "') " +
                    "WHEN (d.discount_type_id = 3) THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') " +
                    "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, ' " + currency + "')  END  ORDER BY sd.id  SEPARATOR '; ') AS disc, ";
        } else if (from_date != null) {
            sql += "(select get_contract_with_discounts(c.amount, st.id, ?, ?, NULL)) as contr_with_disc, " +
                    "GROUP_CONCAT(DISTINCT " +
                    "CASE WHEN (sd.creation_date < ?) THEN NULL " +
                    "WHEN (d.discount_type_id = 1) THEN CONCAT(d.name, ' - ', d.amount, '%') " +
                    "WHEN (d.discount_type_id = 2) THEN CONCAT(d.name, ' - ', d.amount, ' " + currency + "') " +
                    "WHEN (d.discount_type_id = 3) THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') " +
                    "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, ' " + currency + "')  END  ORDER BY sd.id  SEPARATOR '; ') AS disc, ";
        } else if (till_date != null) {
            sql += "(select get_contract_with_discounts(c.amount, st.id, ?, NULL, ?)) as contr_with_disc, " +
                    "GROUP_CONCAT(DISTINCT " +
                    "CASE WHEN (sd.creation_date > ?) THEN NULL " +
                    "WHEN (d.discount_type_id = 1) THEN CONCAT(d.name, ' - ', d.amount, '%') " +
                    "WHEN (d.discount_type_id = 2) THEN CONCAT(d.name, ' - ', d.amount, ' " + currency + "') " +
                    "WHEN (d.discount_type_id = 3) THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') " +
                    "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, ' " + currency + "')  END  ORDER BY sd.id  SEPARATOR '; ') AS disc, ";
        } else {
            sql += "sc.contr_with_disc as contr_with_disc, GROUP_CONCAT(DISTINCT " +
                    "CASE WHEN (d.discount_type_id = 1) THEN CONCAT(d.name, ' - ', d.amount, '%') " +
                    "WHEN (d.discount_type_id = 2) THEN CONCAT(d.name, ' - ', d.amount, ' " + currency + "') " +
                    "WHEN (d.discount_type_id = 3) THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') " +
                    "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, ' " + currency + "')  END  ORDER BY sd.id  SEPARATOR '; ') AS disc, ";
        }
        sql += "CONCAT(cl.name, ' - ', cln.name) AS class FROM student AS st " +
                "LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id " +
                "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 ";
        if (from_date != null && till_date != null) {
            sql += "AND DATE(so.modification_date) >= ? AND DATE(so.modification_date) <= ? ";
        } else if (from_date != null) {
            sql += "AND DATE(so.modification_date) >= ? ";
        } else if (till_date != null) {
            sql += "AND DATE(so.modification_date) <= ? ";
        }
        sql += "GROUP BY so.student_id) AS o_temp " +
                "ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid ";
        if (from_date != null || till_date != null) {
            sql += "LEFT JOIN education_status AS edu ON edu.id = stud_o.to_education_status_id " +
                    "LEFT JOIN class_name AS cln ON cln.id = stud_o.to_class_name_id ";
        } else {
            sql += "LEFT JOIN education_status AS edu ON edu.id = IFNULL(stud_o.to_education_status_id, 1) " +
                    "LEFT JOIN class_name AS cln ON cln.id = IFNULL(stud_o.to_class_name_id, 200) ";
        }
        sql += "LEFT JOIN class_number AS cl ON cl.id = cln.class_number_id " +
                "LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = ? " +
                "LEFT JOIN contract AS c ON c.id = sc.contract_id " +
                "LEFT JOIN " +
                "(SELECT scc.student_id as student_id, GROUP_CONCAT(DISTINCT '(', amr_t.type, ') ', amr_t.name, ' ', " +
                "scc.amount, ' " + Settings.USD + "' ORDER BY amr_t.id ASC SEPARATOR ', ') AS full_details, " +
                "SUM(IF(amr_t.type = '+', scc.amount, - scc.amount)) AS amount " +
                "FROM student_correction scc " +
                "LEFT JOIN correction_type amr_t ON scc.correction_type_id = amr_t.id " +
                "LEFT JOIN student st ON st.id = scc.student_id " +
                "WHERE year_id = ? ";
        if (from_date != null) {
            sql += "AND scc.creation_date >= ? ";
        }
        if (till_date != null) {
            sql += "AND scc.creation_date <= ? ";
        }
        sql += " GROUP BY scc.student_id) AS vc ON vc.student_id = sc.student_id " +
                "LEFT JOIN " +
                "(SELECT sp.student_id AS student_id, (SUM(IF(sp.payment_category_id != 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) " +
                "- SUM(IF(sp.payment_category_id = 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0))) AS amount " +
                "FROM student_payments as sp WHERE sp.year_id = ? ";
        if (from_date != null) {
            sql += "AND DATE(sp.modification_date) >= ? ";
        }
        if (till_date != null) {
            sql += "AND DATE(sp.modification_date) <= ? ";
        }
        sql += "GROUP BY sp.student_id) AS stud_pay ON stud_pay.student_id = sc.student_id " +
                "LEFT JOIN student_discount AS sd ON sd.student_id = st.id AND sd.year_id = ? " +
                "LEFT JOIN discount AS d ON d.id = sd.discount_id " +
                "LEFT JOIN student_relatives AS sr ON st.id = sr.student_id AND sr.is_main = 1 " +
                "LEFT JOIN relatives AS rel ON sr.relatives_id = rel.id " +
                "WHERE cln.id IN (" + class_ids + ") and st.entering_year_id <= ? " +
                "AND edu.id IN (" + edu_statuses_ids + ") " +
                "GROUP BY st.id ORDER BY cl.id, cln.id, st.name, st.surname";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        int counter = 0;
        if (from_date != null && till_date != null) {
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
        } else if (from_date != null) {
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(from_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
        } else if (till_date != null) {
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setDate(++counter, new java.sql.Date(till_date.getTime()));
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
        } else {
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
            stat.setInt(++counter, year_id);
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.EducationStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Gender), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Contract), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.DiscountType), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Discount), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.CorrectionType), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Correction), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PreviousYearDebt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PreviousYearOverpay), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Net), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Paid), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Debt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.OverPay), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Relative), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Phone), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Address), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUI.getMessage(Messages.EducationStatus)).setValue(
                    result.getString("edu.name"));
            item.getItemProperty(myUI.getMessage(Messages.Id)).setValue(
                    result.getString("st.login"));
            item.getItemProperty(myUI.getMessage(Messages.FirstName)).setValue(result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(Messages.LastName)).setValue(result.getString("st.surname"));
            if (result.getInt("st.gender_id") == 1) {
                clr.males++;
                item.getItemProperty(myUI.getMessage(Messages.Gender)).setValue("М");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Gender)).setValue("Ж");
            }
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    result.getString("class"));
            if (result.getString("rel.name") != null) {
                item.getItemProperty(myUI.getMessage(Messages.Relative)).setValue(
                        result.getString("rel.name") + " - " + result.getString("sr.fullname"));
                item.getItemProperty(myUI.getMessage(Messages.Phone)).setValue(
                        result.getString("sr.phone"));
                item.getItemProperty(myUI.getMessage(Messages.Address)).setValue(
                        result.getString("sr.address"));
            }
            double prevYearDebt = result.getDouble("prev_debt");
            if (prevYearDebt >= 0) {
                item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).setValue(prevYearDebt);
                item.getItemProperty(myUI.getMessage(Messages.PreviousYearOverpay)).setValue(0.0);
            } else {
                item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).setValue(0.0);
                item.getItemProperty(myUI.getMessage(Messages.PreviousYearOverpay)).setValue(prevYearDebt);
            }
            clr.prevYearDebts += (Double) item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).getValue();
            clr.prevYearOverpays += (Double) item.getItemProperty(myUI.getMessage(Messages.PreviousYearOverpay)).getValue();
            if (result.getDouble("contract_amount") != 0.0) {
                item.getItemProperty(myUI.getMessage(Messages.Contract)).setValue(result.getDouble("contract_amount"));
                clr.contracts += (Double) item.getItemProperty(myUI.getMessage(Messages.Contract)).getValue();
                if (result.getString("disc") != null) {
                    item.getItemProperty(myUI.getMessage(Messages.DiscountType)).setValue(result.getString("disc"));
                    item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(
                            result.getDouble("contract_amount") - result.getDouble("contr_with_disc"));
                    clr.discounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Discount)).getValue();
                    clr.discountedStudents++;
                } else {
                    item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(0.0);
                }
                item.getItemProperty(myUI.getMessage(Messages.CorrectionType)).setValue(result.getString("vc.full_details"));
                item.getItemProperty(myUI.getMessage(Messages.Correction)).setValue(result.getDouble("vc.amount"));
                clr.corrections += (Double) item.getItemProperty(myUI.getMessage(Messages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Net)).setValue(result.getDouble("contr_with_disc")
                        + result.getDouble("prev_debt") + result.getDouble("vc.amount"));
                clr.nets += (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue();
            } else if (result.getDouble("prev_debt") != 0.0) {
                item.getItemProperty(myUI.getMessage(Messages.Net)).setValue(result.getDouble("prev_debt"));
                clr.nets += (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue();
            }
            item.getItemProperty(myUI.getMessage(Messages.Paid)).setValue(result.getDouble("net_payments"));
            clr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue();
            double debt = (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue()
                    - result.getDouble("net_payments");
            if (debt >= 0) {
                item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(debt);
                item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(0.0);
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(0.0);
                item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(debt);
            }
            clr.debts += (Double) item.getItemProperty(myUI.getMessage(Messages.Debt)).getValue();
            clr.overPays += (Double) item.getItemProperty(myUI.getMessage(Messages.OverPay)).getValue();
            if (result.getInt("edu.id") == 2) {
                clr.activeStudents++;
            }
        }
        return container;
    }

    public IndexedContainer execSQL_Discounts(MyVaadinUI myUI, String class_ids,
                                              String discounts_ids, int year_id,
                                              String edu_statuses_ids, DiscountsReport dr) throws SQLException {
        String currency = Settings.USD;
        dr.activeStudents = 0;
        dr.discountedStudents = 0;
        dr.debts = 0;
        dr.nets = 0;
        dr.paid_amounts = 0;
        dr.contracts = 0;
        dr.discounts = 0;
        dr.corrections = 0;
        dr.lefts = 0;
        String sql = "SELECT st.id, st.login, st.name, st.surname, vcs.education_status, c.amount, sc.debt, "
                + "vc.amount, vc.full_details, sc.contr_with_disc, sc.net_payments, vcs.education_status_id, "
                + "GROUP_CONCAT(DISTINCT "
                + "CASE d.discount_type_id WHEN 1 THEN CONCAT(d.name, ' - ', d.amount, '%') "
                + "WHEN 2 THEN CONCAT(d.name, ' - ', d.amount, ' " + currency + "') "
                + "WHEN 3 THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') "
                + "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, ' " + currency + "') END "
                + "ORDER BY sd.id SEPARATOR '; ') AS disc, vcs.class_name "
                + "FROM student AS st "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON c.id = sc.contract_id "
                + "LEFT JOIN student_discount AS sd ON sd.student_id = st.id AND sd.year_id = ? "
                + "LEFT JOIN discount AS d ON d.id = sd.discount_id "
                + "WHERE vcs.class_name_id IN (" + class_ids + ") and st.entering_year_id <= ? "
                + "AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "AND d.id IN (" + discounts_ids + ") "
                + "GROUP BY st.id ORDER BY disc, vcs.class_number_id, vcs.class_name_id, st.name, st.surname";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.EducationStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Contract), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.DiscountType), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Discount), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.CorrectionType), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Correction), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PreviousYearDebt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Net), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Paid), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Left), Double.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUI.getMessage(Messages.EducationStatus)).setValue(
                    result.getString("vcs.education_status"));
            item.getItemProperty(myUI.getMessage(Messages.Id)).setValue(
                    result.getString("st.login"));
            item.getItemProperty(myUI.getMessage(Messages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(Messages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    result.getString("vcs.class_name"));
            if (result.getDouble("c.amount") != 0.0) {
                item.getItemProperty(myUI.getMessage(Messages.Contract)).setValue(
                        result.getDouble("c.amount"));
                dr.contracts += (Double) item.getItemProperty(
                        myUI.getMessage(Messages.Contract)).getValue();
                if (result.getString("disc") != null) {
                    item.getItemProperty(myUI.getMessage(Messages.DiscountType)).setValue(
                            result.getString("disc"));
                    item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(
                            result.getDouble("c.amount") - result.getDouble("sc.contr_with_disc"));
                    dr.discounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Discount)).getValue();
                    dr.discountedStudents++;
                } else {
                    item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(0.0);
                }
                item.getItemProperty(myUI.getMessage(Messages.CorrectionType)).setValue(
                        result.getString("vc.full_details"));
                item.getItemProperty(myUI.getMessage(Messages.Correction)).setValue(
                        result.getDouble("vc.amount"));
                dr.corrections += (Double) item.getItemProperty(myUI.getMessage(Messages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).setValue(
                        result.getDouble("sc.debt"));
                dr.debts += (Double) item.getItemProperty(
                        myUI.getMessage(Messages.PreviousYearDebt)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Net)).setValue(
                        result.getDouble("sc.contr_with_disc") + result.getDouble("sc.debt")
                                + result.getDouble("vc.amount"));
                dr.nets += (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Paid)).setValue(
                        result.getDouble("sc.net_payments"));
                dr.paid_amounts += (Double) item.getItemProperty(
                        myUI.getMessage(Messages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Left)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue()
                                - result.getDouble("sc.net_payments"));
                dr.lefts += (Double) item.getItemProperty(
                        myUI.getMessage(Messages.Left)).getValue();
            }
            if (result.getInt("vcs.education_status_id") == 2) {
                dr.activeStudents++;
            }
        }
        return container;
    }

    public void execSQL_Yearly_by_classes(MyVaadinUI myUI, String school_ids, String edu_statuses_ids, int year_id,
                                          YearMonthReport ymr) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT t.class_id, t.school_id, sch.code, sch.name_ru, CONCAT(cl.name, ' - ', t.class_name) as cl, " +
                "COUNT(t.stud_id) as total_studs, COUNT(IF(t.status_id = 2, 1, NULL)) as active_studs, " +
                "SUM(IFNULL(t.contract_amount, 0.0)) as contract_amount, " +
                "SUM(IF(t.prev_debt IS NOT NULL AND t.prev_debt > 0.0, t.prev_debt, 0.0)) AS prev_debt, " +
                "SUM(IF(t.prev_debt IS NOT NULL AND t.prev_debt < 0.0, t.prev_debt, 0.0)) AS prev_overpay, " +
                "SUM(IFNULL(t.correction, 0.0)) as correction, SUM(IFNULL(t.net_payments, 0.0)) as net_payments, " +
                "SUM(IFNULL(t.contr_with_disc, 0.0)) as contr_with_disc, " +
                "SUM(CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN " +
                "(IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) " +
                "WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) as net, " +
                "SUM(IF((CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN " +
                "(IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) " +
                "WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - " +
                "IFNULL(t.net_payments, 0.0) > 0, (CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 " +
                "THEN (IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) " +
                "WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - " +
                "IFNULL(t.net_payments, 0.0), 0.0)) AS debt, SUM(IF((CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 " +
                "THEN (IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) " +
                "WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - " +
                "IFNULL(t.net_payments, 0.0) < 0, (CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN " +
                "(IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) " +
                "WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - " +
                "IFNULL(t.net_payments, 0.0), 0.0)) AS overpay ");
        for (Month month : ymr.getMonths()) {
            sql.append(", m_payments.p_").append(month.getName()).append(" ");
            sql.append(", m_installments.i_").append(month.getName()).append(" ");
        }
        sql.append("FROM (SELECT st.id AS stud_id, st.school_id as school_id, edu.id AS status_id, c.amount AS contract_amount, " + "IFNULL(sc.debt, IFNULL((SELECT SUM(contr_with_disc) FROM student_contract " + "WHERE student_id = st.id AND year_id < ?), 0.0) + " + "IFNULL((SELECT SUM(amount) FROM view_corrections " + "WHERE student_id = st.id AND year_id < ?), 0.0) - " + "IFNULL((SELECT SUM(IF(payment_category_id != 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) - " + "SUM(IF(payment_category_id = 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount ELSE sp.amount * sp.dollar_rate END, 0)) " + "FROM student_payments as sp " + "left join student on student.id = sp.student_id " + "left join school as sch on sch.id = student.school_id " + "WHERE sp.student_id = st.id AND sp.year_id < ?), 0.0)) AS prev_debt, " + "vc.amount AS correction, stud_pay.amount AS net_payments,  sc.contr_with_disc as contr_with_disc, " + "cln.id AS class_id, cln.class_number_id AS class_number_id, cln.name AS class_name " + "FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id " + "FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp " + "ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid " + "LEFT JOIN education_status AS edu ON edu.id = IFNULL(stud_o.to_education_status_id, 1) " + "LEFT JOIN class_name AS cln ON cln.id = IFNULL(stud_o.to_class_name_id, 200) " + "LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = ? " + "LEFT JOIN contract AS c ON c.id = sc.contract_id " + "LEFT JOIN " + "(SELECT scc.student_id as student_id, GROUP_CONCAT(DISTINCT '(', amr_t.type, ') ', amr_t.name, ' ', " + "scc.amount, ' " + Settings.USD + "' ORDER BY amr_t.id ASC SEPARATOR ', ') AS full_details, " + "SUM(IF(amr_t.type = '+', scc.amount, - scc.amount)) AS amount " + "FROM student_correction scc " + "LEFT JOIN correction_type amr_t ON scc.correction_type_id = amr_t.id " + "LEFT JOIN student st ON st.id = scc.student_id " + "WHERE year_id = ? GROUP BY scc.student_id) AS vc ON vc.student_id = sc.student_id " + "LEFT JOIN " + "(SELECT sp.student_id AS student_id, (SUM(IF(sp.payment_category_id != 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount ELSE sp.amount * sp.dollar_rate END, 0)) " + "- SUM(IF(sp.payment_category_id = 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount ELSE sp.amount * sp.dollar_rate END, 0))) AS amount " + "FROM student_payments as sp " + "left join student on student.id = sp.student_id " + "left join school as sch on sch.id = student.school_id " + "WHERE sp.year_id = ? GROUP BY sp.student_id) AS stud_pay ON stud_pay.student_id = sc.student_id " + "LEFT JOIN student_discount AS sd ON sd.student_id = st.id AND sd.year_id = ? " + "LEFT JOIN discount AS d ON d.id = sd.discount_id " + "WHERE st.school_id in (").append(school_ids).append(") AND st.entering_year_id <= ? ").append("AND edu.id IN (").append(edu_statuses_ids).append(") ").append("GROUP BY st.id) AS t LEFT JOIN(SELECT cln.id AS class_id");
        for (Month month : ymr.getMonths()) {
            sql.append(", ROUND(SUM(IF(MONTH(vsp.modification_date) = ").append(month.getId()).append(", vsp.normalized_amount, 0)), 2) AS p_").append(month.getName()).append(" ");
        }
        sql.append("FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid LEFT JOIN education_status AS edu ON edu.id = IFNULL(stud_o.to_education_status_id, 1) LEFT JOIN class_name AS cln ON cln.id = IFNULL(stud_o.to_class_name_id, 200) LEFT JOIN class_number AS cl ON cl.id = cln.class_number_id LEFT JOIN view_student_payment_amount AS vsp ON st.id = vsp.student_id AND vsp.year_id = ? GROUP BY cln.id) as m_payments on m_payments.class_id = t.class_id " + "LEFT JOIN (SELECT cln.id AS class_id");
        for (Month month : ymr.getMonths()) {
            sql.append(", ROUND(SUM(IF(MONTH(ip.date_of_payment) = ").append(month.getId()).append(", ip.amount, 0)), 2) AS i_").append(month.getName()).append(" ");
        }
        sql.append("FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid LEFT JOIN education_status AS edu ON edu.id = IFNULL(stud_o.to_education_status_id, 1) LEFT JOIN class_name AS cln ON cln.id = IFNULL(stud_o.to_class_name_id, 200) LEFT JOIN class_number AS cl ON cl.id = cln.class_number_id left join student_installement_plan as ip on st.id = ip.student_id and ip.year_id = ? GROUP BY cln.id) as m_installments on m_installments.class_id = t.class_id  " + "LEFT JOIN class_number AS cl ON cl.id = t.class_number_id " + "LEFT JOIN school AS sch ON sch.id = t.school_id " + "GROUP BY t.school_id, t.class_id ORDER BY t.school_id, cl.name, t.class_name");
        PreparedStatement stat = dbCon.prepareStatement(sql.toString());
        int counter = 0;
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        ResultSet result = stat.executeQuery();
        int school_id = 0;
        Table t = null;
        int i = 0;
        ymr.clearLayout();
        while (result.next()) {
            if (school_id != result.getInt("t.school_id")) {
                if (t != null) {
                    t.setColumnFooter(myUI.getMessage(Messages.Total_Active),
                            ymr.totalStudents + "/" + ymr.totalActive);
                    t.setColumnFooter(myUI.getMessage(Messages.Contract),
                            Settings.dFormat2.format(ymr.contracts));
                    t.setColumnFooter(myUI.getMessage(Messages.Discount),
                            Settings.dFormat2.format(ymr.discounts));
                    if (ymr.contracts != 0) {
                        t.setColumnFooter(myUI.getMessage(Messages.DiscountPercentage),
                                Settings.dFormat2.format((100 * ymr.discounts) / ymr.contracts));
                    }
                    t.setColumnFooter(myUI.getMessage(Messages.Correction),
                            Settings.dFormat2.format(ymr.corrections));
                    t.setColumnFooter(myUI.getMessage(Messages.PreviousYearDebt),
                            Settings.dFormat2.format(ymr.prevYearDebts));
                    t.setColumnFooter(myUI.getMessage(Messages.PreviousYearOverpay),
                            Settings.dFormat2.format(ymr.prevYearOverpays));
                    t.setColumnFooter(myUI.getMessage(Messages.Net),
                            Settings.dFormat2.format(ymr.nets));
                    t.setColumnFooter(myUI.getMessage(Messages.Paid),
                            Settings.dFormat2.format(ymr.paid_amounts));
                    t.setColumnFooter(myUI.getMessage(Messages.Debt),
                            Settings.dFormat2.format(ymr.debts));
                    t.setColumnFooter(myUI.getMessage(Messages.OverPay),
                            Settings.dFormat2.format(ymr.overpays));
                    for (Month month : ymr.getMonths()) {
                        t.setColumnFooter(myUI.getMessage(Messages.Payments) + " " + month.getName(),
                                Settings.dFormat2.format(month.getTotalPayments()));
                        t.setColumnFooter(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName(),
                                Settings.dFormat2.format(month.getTotalInstallments()));
                    }
                    if (ymr.nets != 0.0) {
                        t.setColumnFooter(Settings.percentage, Settings.dFormat2.format(ymr.paid_amounts * 100 / ymr.nets));
                    }
                    ymr.totalStudents = 0;
                    ymr.totalActive = 0;
                    ymr.contracts = 0.0;
                    ymr.discounts = 0.0;
                    ymr.prevYearDebts = 0.0;
                    ymr.prevYearOverpays = 0.0;
                    ymr.corrections = 0.0;
                    ymr.nets = 0.0;
                    ymr.paid_amounts = 0.0;
                    ymr.debts = 0.0;
                    for (Month month : ymr.getMonths()) {
                        month.setTotalPayments(0);
                        month.setTotalInstallments(0);
                    }
                    ymr.overpays = 0.0;
                }
                t = ymr.createTable(result.getString("sch.code")
                        + " - " + result.getString("sch.name_ru"));
                ymr.rightLay.addComponent(t);
                school_id = result.getInt("t.school_id");
            }
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                        result.getString("cl"));
                item.getItemProperty(myUI.getMessage(Messages.Total_Active)).setValue(
                        result.getInt("total_studs") + "/" + result.getInt("active_studs"));
                ymr.totalStudents += result.getInt("total_studs");
                ymr.totalActive += result.getInt("active_studs");
                item.getItemProperty(myUI.getMessage(Messages.Contract)).setValue(
                        result.getDouble("contract_amount"));
                ymr.contracts += (Double) item.getItemProperty(myUI.getMessage(Messages.Contract)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(
                        result.getDouble("contract_amount") - result.getDouble("contr_with_disc"));
                if (result.getDouble("contract_amount") != 0) {
                    item.getItemProperty(myUI.getMessage(Messages.DiscountPercentage)).setValue((100 *
                            (result.getDouble("contract_amount") - result.getDouble("contr_with_disc")))
                            / result.getDouble("contract_amount"));
                }
                ymr.discounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Discount)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Correction)).setValue(
                        result.getDouble("correction"));
                ymr.corrections += (Double) item.getItemProperty(myUI.getMessage(Messages.Correction)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).setValue(
                        result.getDouble("prev_debt"));
                ymr.prevYearDebts += (Double) item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.PreviousYearOverpay)).setValue(
                        result.getDouble("prev_overpay"));
                ymr.prevYearOverpays += (Double) item.getItemProperty(myUI.getMessage(Messages.PreviousYearOverpay)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Net)).setValue(result.getDouble("net"));
                ymr.nets += (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Paid)).setValue(
                        result.getDouble("net_payments"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(result.getDouble("debt"));
                ymr.debts += (Double) item.getItemProperty(myUI.getMessage(Messages.Debt)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(result.getDouble("overpay"));
                ymr.overpays += (Double) item.getItemProperty(myUI.getMessage(Messages.OverPay)).getValue();
                if ((Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue((Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue() * 100
                            / (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue());
                }
                for (Month month : ymr.getMonths()) {
                    item.getItemProperty(myUI.getMessage(Messages.Payments) + " " + month.getName()).setValue(
                            result.getDouble("p_" + month.getName()));
                    month.setTotalPayments(month.getTotalPayments() +
                            (Double) item.getItemProperty(myUI.getMessage(Messages.Payments) + " " + month.getName()).getValue());
                    item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName()).setValue(
                            result.getDouble("i_" + month.getName()));
                    month.setTotalInstallments(month.getTotalInstallments() +
                            (Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName()).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(Messages.Total_Active),
                    ymr.totalStudents + "/" + ymr.totalActive);
            t.setColumnFooter(myUI.getMessage(Messages.Contract),
                    Settings.dFormat2.format(ymr.contracts));
            t.setColumnFooter(myUI.getMessage(Messages.Discount),
                    Settings.dFormat2.format(ymr.discounts));
            if (ymr.contracts != 0) {
                t.setColumnFooter(myUI.getMessage(Messages.DiscountPercentage),
                        Settings.dFormat2.format((100 * ymr.discounts) / ymr.contracts));
            }
            t.setColumnFooter(myUI.getMessage(Messages.PreviousYearDebt),
                    Settings.dFormat2.format(ymr.prevYearDebts));
            t.setColumnFooter(myUI.getMessage(Messages.PreviousYearOverpay),
                    Settings.dFormat2.format(ymr.prevYearOverpays));
            t.setColumnFooter(myUI.getMessage(Messages.Correction),
                    Settings.dFormat2.format(ymr.corrections));
            t.setColumnFooter(myUI.getMessage(Messages.Net),
                    Settings.dFormat2.format(ymr.nets));
            t.setColumnFooter(myUI.getMessage(Messages.Paid),
                    Settings.dFormat2.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(Messages.Debt), Settings.dFormat2.format(ymr.debts));
            t.setColumnFooter(myUI.getMessage(Messages.OverPay), Settings.dFormat2.format(ymr.overpays));
            for (Month month : ymr.getMonths()) {
                t.setColumnFooter(myUI.getMessage(Messages.Payments) + " " + month.getName(), Settings.dFormat2.format(month.getTotalPayments()));
                t.setColumnFooter(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName(), Settings.dFormat2.format(month.getTotalInstallments()));
            }
            if (ymr.nets != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat2.format(
                        ymr.paid_amounts * 100 / ymr.nets));
            }
            ymr.totalStudents = 0;
            ymr.totalActive = 0;
            ymr.contracts = 0.0;
            ymr.discounts = 0.0;
            ymr.prevYearDebts = 0.0;
            ymr.prevYearOverpays = 0.0;
            ymr.corrections = 0.0;
            ymr.nets = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.debts = 0.0;
            ymr.overpays = 0.0;
            for (Month month : ymr.getMonths()) {
                month.setTotalPayments(0);
                month.setTotalInstallments(0);
            }
        }
        if (ymr.rightLay.getComponentCount() != 0) {
            execSQL_Yearly_by_class_numbers(myUI, school_ids, edu_statuses_ids, year_id, ymr);
        }
    }

    public void execSQL_Monthly_by_classes(MyVaadinUI myUI, String school_ids,
                                           String edu_statuses_ids, int year_id, YearMonthReport ymr) throws SQLException {

        String sql = "SELECT months.name, months.id, s_temp.id, s_temp.name_ru, s_temp.code, i_temp.amn, p_temp.amn FROM months "
                + "CROSS JOIN school AS s_temp LEFT JOIN "
                + "(SELECT sch.id AS s_id, sch.name_ru AS s_name, SUM(inst.amount) AS amn, MONTH(inst.date_of_payment) AS mnth "
                + "FROM student_installement_plan AS inst LEFT JOIN student AS st ON st.id = inst.student_id "
                + "LEFT JOIN school AS sch ON sch.id = st.school_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "WHERE inst.year_id = ? AND vcs.education_status_id IN (" + edu_statuses_ids + ") GROUP BY sch.id, "
                + "MONTH(inst.date_of_payment)) AS i_temp "
                + "ON i_temp.mnth = months.id AND s_temp.id = i_temp.s_id "
                + "LEFT JOIN (SELECT sch.id AS s_id, SUM(IF(pay.payment_category_id = 3, "
                + "-CASE WHEN pay.acc_currency_id = 1 THEN pay.amount / pay.dollar_rate "
                + "ELSE pay.amount END, "
                + "CASE WHEN pay.acc_currency_id = 1 THEN pay.amount / pay.dollar_rate ELSE pay.amount END)) AS amn, "
                + "MONTH(pay.modification_date) AS mnth "
                + "FROM student_payments as pay "
                + "LEFT JOIN student ON student.id = pay.student_id "
                + "LEFT JOIN school AS sch ON sch.id = student.school_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = student.id and vcs.year_id = ? "
                + "WHERE pay.year_id = ? AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "GROUP BY sch.id, MONTH(pay.modification_date)) AS p_temp ON p_temp.mnth = months.id "
                + "AND s_temp.id = p_temp.s_id WHERE s_temp.id IN (" + school_ids + ") ORDER BY "
                + "CAST(s_temp.code AS UNSIGNED), months.order_num";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        int school_id = 0;
        Table t = null;
        int i = 0;
        ymr.clearLayout();
        while (result.next()) {
            if (school_id != result.getInt("s_temp.id")) {
                if (t != null) {
                    t.setColumnFooter(myUI.getMessage(Messages.InstPlanDebt), Settings.dFormat2.format(ymr.inst_plans));
                    t.setColumnFooter(myUI.getMessage(Messages.Paid), Settings.dFormat2.format(ymr.paid_amounts));
                    t.setColumnFooter(myUI.getMessage(Messages.Debt), Settings.dFormat2.format(ymr.debts));
                    t.setColumnFooter(myUI.getMessage(Messages.OverPay), Settings.dFormat2.format(ymr.overpays));
                    if (ymr.inst_plans != 0.0) {
                        t.setColumnFooter(Settings.percentage, Settings.dFormat2.format(
                                ymr.paid_amounts * 100 / ymr.inst_plans));
                    }
                    ymr.inst_plans = 0.0;
                    ymr.paid_amounts = 0.0;
                    ymr.debts = 0.0;
                    ymr.overpays = 0.0;
                }
                t = ymr.createTable(result.getString("s_temp.code") + " - " + result.getString("s_temp.name_ru"));
                ymr.rightLay.addComponent(t);
                school_id = result.getInt("s_temp.id");
            }
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(Messages.Month)).setValue(
                        result.getString("months.name"));
                item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).setValue(
                        result.getDouble("i_temp.amn"));
                ymr.inst_plans += (Double) item.getItemProperty(
                        myUI.getMessage(Messages.InstPlanDebt)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Paid)).setValue(
                        result.getDouble("p_temp.amn"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue();
                double debt = (Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).getValue()
                        - (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue();
                if (debt > 0.0) {
                    item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(debt);
                    item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(0.0);
                    ymr.debts += (Double) item.getItemProperty(myUI.getMessage(Messages.Debt)).getValue();
                } else {
                    item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(0.0);
                    item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(debt);
                    ymr.overpays += (Double) item.getItemProperty(myUI.getMessage(Messages.OverPay)).getValue();
                }
                if ((Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue() * 100.0
                                    / (Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(Messages.InstPlanDebt), Settings.dFormat2.format(ymr.inst_plans));
            t.setColumnFooter(myUI.getMessage(Messages.Paid), Settings.dFormat2.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(Messages.Debt), Settings.dFormat2.format(ymr.debts));
            t.setColumnFooter(myUI.getMessage(Messages.OverPay), Settings.dFormat2.format(ymr.overpays));
            if (ymr.inst_plans != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat2.format(ymr.paid_amounts * 100 / ymr.inst_plans));
            }
            ymr.inst_plans = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.debts = 0.0;
            ymr.overpays = 0.0;
        }
        if (ymr.rightLay.getComponentCount() > 1) {
            execSQL_Monthly(myUI, school_ids, edu_statuses_ids, year_id, ymr);
        }
    }

    public void execSQL_Yearly_by_class_numbers(MyVaadinUI myUI, String school_ids, String edu_statuses_ids, int year_id,
                                                YearMonthReport ymr) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT t.class_id, t.school_id, cl.name as cl, COUNT(t.stud_id) as total_studs, COUNT(IF(t.status_id = 2, 1, NULL)) as active_studs, SUM(IFNULL(t.contract_amount, 0.0)) as contract_amount, SUM(IF(t.prev_debt IS NOT NULL AND t.prev_debt > 0.0, t.prev_debt, 0.0)) AS prev_debt, SUM(IF(t.prev_debt IS NOT NULL AND t.prev_debt < 0.0, t.prev_debt, 0.0)) AS prev_overpay, SUM(IFNULL(t.correction, 0.0)) as correction, SUM(IFNULL(t.net_payments, 0.0)) as net_payments, SUM(IFNULL(t.contr_with_disc, 0.0)) as contr_with_disc, SUM(CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN (IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) as net, SUM(IF((CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN (IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - IFNULL(t.net_payments, 0.0) > 0, (CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN (IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - IFNULL(t.net_payments, 0.0), 0.0)) AS debt, SUM(IF((CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN (IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - IFNULL(t.net_payments, 0.0) < 0, (CASE WHEN IFNULL(t.contract_amount, 0.0) != 0.0 THEN (IFNULL(t.contr_with_disc, 0.0) + IFNULL(t.prev_debt, 0.0) + IFNULL(t.correction, 0.0)) WHEN IFNULL(t.prev_debt, 0.0) != 0.0 THEN IFNULL(t.prev_debt, 0.0) ELSE 0.0 END) - IFNULL(t.net_payments, 0.0), 0.0)) AS overpay ");
        for (Month month : ymr.getMonths()) {
            sql.append(", m_payments.p_").append(month.getName()).append(" ");
            sql.append(", m_installments.i_").append(month.getName()).append(" ");
        }
        sql.append(" FROM (SELECT st.id AS stud_id, st.school_id as school_id, edu.id AS status_id, c.amount AS contract_amount, IFNULL(sc.debt, IFNULL((SELECT SUM(contr_with_disc) FROM student_contract WHERE student_id = st.id AND year_id < ?), 0.0) + IFNULL((SELECT SUM(amount) FROM view_corrections WHERE student_id = st.id AND year_id < ?), 0.0) - IFNULL( (SELECT SUM(IF(payment_category_id != 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) - SUM(IF(payment_category_id = 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount ELSE sp.amount * sp.dollar_rate END, 0)) FROM student_payments as sp left join student on student.id = sp.student_id left join school as sch on sch.id = student.school_id WHERE student_id = st.id AND year_id < ?), 0.0)) AS prev_debt, vc.amount AS correction, stud_pay.amount AS net_payments, sc.contr_with_disc as contr_with_disc, cln.id AS class_id, cln.class_number_id AS class_number_id, cln.name AS class_name FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid LEFT JOIN education_status AS edu ON edu.id = IFNULL(stud_o.to_education_status_id, 1) LEFT JOIN class_name AS cln ON cln.id = IFNULL(stud_o.to_class_name_id, 200) LEFT JOIN student_contract AS sc ON sc.student_id = st.id AND sc.year_id = ? LEFT JOIN contract AS c ON c.id = sc.contract_id LEFT JOIN (SELECT scc.student_id as student_id, GROUP_CONCAT(DISTINCT '(', amr_t.type, ') ', amr_t.name, ' ', scc.amount, ' " + Settings.USD + "' ORDER BY amr_t.id ASC SEPARATOR ', ') AS full_details, SUM(IF(amr_t.type = '+', scc.amount, - scc.amount)) AS amount FROM student_correction scc LEFT JOIN correction_type amr_t ON scc.correction_type_id = amr_t.id LEFT JOIN student st ON st.id = scc.student_id WHERE year_id = ? GROUP BY scc.student_id) AS vc ON vc.student_id = sc.student_id LEFT JOIN (SELECT sp.student_id AS student_id, (SUM(IF(sp.payment_category_id != 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount ELSE sp.amount * sp.dollar_rate END, 0)) - SUM(IF(sp.payment_category_id = 3, CASE WHEN sp.acc_currency_id = 1 THEN sp.amount ELSE sp.amount * sp.dollar_rate END, 0))) AS amount FROM student_payments as sp left join student on student.id = sp.student_id left join school as sch on sch.id = student.school_id WHERE sp.year_id = ? GROUP BY sp.student_id) AS stud_pay ON stud_pay.student_id = sc.student_id LEFT JOIN student_discount AS sd ON sd.student_id = st.id AND sd.year_id = ? LEFT JOIN discount AS d ON d.id = sd.discount_id WHERE st.school_id in (").append(school_ids).append(") AND st.entering_year_id <= ? AND edu.id IN (").append(edu_statuses_ids).append(") GROUP BY st.id) AS t LEFT JOIN(SELECT cl.id AS class_id ");
        for (Month month : ymr.getMonths()) {
            sql.append(", ROUND(SUM(IF(MONTH(vsp.modification_date) = ").append(month.getId()).append(", vsp.normalized_amount, 0)), 2) AS p_").append(month.getName()).append(" ");
        }
        sql.append(" FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid LEFT JOIN education_status AS edu ON edu.id = IFNULL(stud_o.to_education_status_id, 1) LEFT JOIN class_name AS cln ON cln.id = IFNULL(stud_o.to_class_name_id, 200) LEFT JOIN class_number AS cl ON cl.id = cln.class_number_id LEFT JOIN view_student_payment_amount AS vsp ON st.id = vsp.student_id AND vsp.year_id = ? GROUP BY cl.id) as m_payments on m_payments.class_id = t.class_number_id LEFT JOIN (SELECT cl.id AS class_id ");
        for (Month month : ymr.getMonths()) {
            sql.append(", ROUND(SUM(IF(MONTH(ip.date_of_payment) = ").append(month.getId()).append(", ip.amount, 0)), 2) AS i_").append(month.getName()).append(" ");
        }
        sql.append(" FROM student AS st LEFT JOIN (SELECT MAX(so.id) AS oid, so.student_id AS stud_id FROM student_orders AS so WHERE so.year_id = ? AND so.is_valid = 1 GROUP BY so.student_id) AS o_temp ON st.id = o_temp.stud_id LEFT JOIN student_orders AS stud_o ON stud_o.id = o_temp.oid LEFT JOIN education_status AS edu ON edu.id = IFNULL(stud_o.to_education_status_id, 1) LEFT JOIN class_name AS cln ON cln.id = IFNULL(stud_o.to_class_name_id, 200) LEFT JOIN class_number AS cl ON cl.id = cln.class_number_id left join student_installement_plan as ip on st.id = ip.student_id and ip.year_id = ? GROUP BY cl.id) as m_installments on m_installments.class_id = t.class_number_id LEFT JOIN class_number AS cl ON cl.id = t.class_number_id GROUP BY t.class_number_id ORDER BY t.class_number_id");
        PreparedStatement stat = dbCon.prepareStatement(sql.toString());
        int counter = 0;
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        stat.setInt(++counter, year_id);
        ResultSet result = stat.executeQuery();
        Table t;
        int i = 0;
        t = ymr.createTable(myUI.getMessage(Messages.Total));
        ymr.rightLay.addComponent(t);
        while (result.next()) {
            Item item = t.getContainerDataSource().addItem(i++);
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    result.getString("cl"));
            item.getItemProperty(myUI.getMessage(Messages.Total_Active)).setValue(
                    result.getInt("total_studs") + "/" + result.getInt("active_studs"));
            ymr.totalStudents += result.getInt("total_studs");
            ymr.totalActive += result.getInt("active_studs");
            item.getItemProperty(myUI.getMessage(Messages.Contract)).setValue(
                    result.getDouble("contract_amount"));
            ymr.contracts += (Double) item.getItemProperty(myUI.getMessage(Messages.Contract)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.Discount)).setValue(
                    result.getDouble("contract_amount") - result.getDouble("contr_with_disc"));
            if (result.getDouble("contract_amount") != 0) {
                item.getItemProperty(myUI.getMessage(Messages.DiscountPercentage)).setValue((100 *
                        (result.getDouble("contract_amount") - result.getDouble("contr_with_disc")))
                        / result.getDouble("contract_amount"));
            }
            ymr.discounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Discount)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.Correction)).setValue(
                    result.getDouble("correction"));
            ymr.corrections += (Double) item.getItemProperty(myUI.getMessage(Messages.Correction)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).setValue(
                    result.getDouble("prev_debt"));
            ymr.prevYearDebts += (Double) item.getItemProperty(myUI.getMessage(Messages.PreviousYearDebt)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.PreviousYearOverpay)).setValue(
                    result.getDouble("prev_overpay"));
            ymr.prevYearOverpays += (Double) item.getItemProperty(myUI.getMessage(Messages.PreviousYearOverpay)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.Net)).setValue(result.getDouble("net"));
            ymr.nets += (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.Paid)).setValue(
                    result.getDouble("net_payments"));
            ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(result.getDouble("debt"));
            ymr.debts += (Double) item.getItemProperty(myUI.getMessage(Messages.Debt)).getValue();
            item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(result.getDouble("overpay"));
            ymr.overpays += (Double) item.getItemProperty(myUI.getMessage(Messages.OverPay)).getValue();
            if ((Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue() != 0.0) {
                item.getItemProperty(Settings.percentage).setValue((Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue() * 100
                        / (Double) item.getItemProperty(myUI.getMessage(Messages.Net)).getValue());
            }
            for (Month month : ymr.getMonths()) {
                item.getItemProperty(myUI.getMessage(Messages.Payments) + " " + month.getName()).setValue(
                        result.getDouble("p_" + month.getName()));
                month.setTotalPayments(month.getTotalPayments() +
                        (Double) item.getItemProperty(myUI.getMessage(Messages.Payments) + " " + month.getName()).getValue());
                item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName()).setValue(
                        result.getDouble("i_" + month.getName()));
                month.setTotalInstallments(month.getTotalInstallments() +
                        (Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName()).getValue());
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(Messages.Total_Active), ymr.totalStudents + "/" + ymr.totalActive);
            t.setColumnFooter(myUI.getMessage(Messages.Contract), Settings.dFormat2.format(ymr.contracts));
            t.setColumnFooter(myUI.getMessage(Messages.Correction), Settings.dFormat2.format(ymr.corrections));
            t.setColumnFooter(myUI.getMessage(Messages.Discount), Settings.dFormat2.format(ymr.discounts));
            if (ymr.contracts != 0) {
                t.setColumnFooter(myUI.getMessage(Messages.DiscountPercentage),
                        Settings.dFormat2.format((100 * ymr.discounts) / ymr.contracts));
            }
            t.setColumnFooter(myUI.getMessage(Messages.PreviousYearDebt), Settings.dFormat2.format(ymr.prevYearDebts));
            t.setColumnFooter(myUI.getMessage(Messages.Net), Settings.dFormat2.format(ymr.nets));
            t.setColumnFooter(myUI.getMessage(Messages.Paid), Settings.dFormat2.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(Messages.Debt), Settings.dFormat2.format(ymr.debts));
            t.setColumnFooter(myUI.getMessage(Messages.OverPay), Settings.dFormat2.format(ymr.overpays));
            for (Month month : ymr.getMonths()) {
                t.setColumnFooter(myUI.getMessage(Messages.Payments) + " " + month.getName(), Settings.dFormat2.format(month.getTotalPayments()));
                t.setColumnFooter(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName(), Settings.dFormat2.format(month.getTotalInstallments()));
            }
            if (ymr.nets != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat2.format(ymr.paid_amounts * 100 / ymr.nets));
            }
            ymr.totalStudents = 0;
            ymr.totalActive = 0;
            ymr.contracts = 0.0;
            ymr.corrections = 0.0;
            ymr.discounts = 0.0;
            ymr.prevYearDebts = 0.0;
            ymr.nets = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.debts = 0.0;
            for (Month month : ymr.getMonths()) {
                month.setTotalPayments(0);
                month.setTotalInstallments(0);
            }
            ymr.overpays = 0.0;
        }
    }

    public void execSQL_Monthly(MyVaadinUI myUI, String school_ids, String edu_statuses_ids,
                                int year_id, YearMonthReport ymr) throws SQLException {

        String sql = "select months.name, months.id, i_temp.amn, p_temp.amn FROM months "
                + "left join ("
                + "select sum(inst.amount) as amn, month(inst.date_of_payment) as mnth "
                + "from student_installement_plan as inst "
                + "left join student as st on st.id = inst.student_id "
                + "left join school as sch on sch.id = st.school_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "where inst.year_id = ? and sch.id in (" + school_ids + ") "
                + "AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "group by month(inst.date_of_payment)) as i_temp on i_temp.mnth = months.id "
                + "left join ("
                + "select sum(if(pay.payment_category_id = 3, - CASE WHEN pay.acc_currency_id = 1 THEN pay.amount / pay.dollar_rate ELSE pay.amount END, CASE WHEN pay.acc_currency_id = 1 THEN pay.amount / pay.dollar_rate ELSE pay.amount END)) as amn, "
                + "month(pay.modification_date) as mnth from student_payments as pay "
                + "left join student on student.id = pay.student_id "
                + "left join school as sch on sch.id = student.school_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = student.id and vcs.year_id = ? "
                + "where pay.year_id = ? and sch.id in (" + school_ids + ") "
                + "AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "group by month(pay.modification_date)) as p_temp on p_temp.mnth = months.id "
                + "order by months.order_num";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        Table t;
        int i = 0;
        t = ymr.createTable(myUI.getMessage(Messages.Total));
        ymr.rightLay.addComponent(t);
        while (result.next()) {
            if (t != null) {
                Item item = t.getContainerDataSource().addItem(i++);
                item.getItemProperty(myUI.getMessage(Messages.Month)).setValue(
                        result.getString("months.name"));
                item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).setValue(
                        result.getDouble("i_temp.amn"));
                ymr.inst_plans += (Double) item.getItemProperty(
                        myUI.getMessage(Messages.InstPlanDebt)).getValue();
                item.getItemProperty(myUI.getMessage(Messages.Paid)).setValue(
                        result.getDouble("p_temp.amn"));
                ymr.paid_amounts += (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue();
                double debt = (Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).getValue()
                        - (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue();
                if (debt > 0.0) {
                    item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(debt);
                    item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(0.0);
                    ymr.debts += (Double) item.getItemProperty(myUI.getMessage(Messages.Debt)).getValue();
                } else {
                    item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(0.0);
                    item.getItemProperty(myUI.getMessage(Messages.OverPay)).setValue(debt);
                    ymr.overpays += (Double) item.getItemProperty(myUI.getMessage(Messages.OverPay)).getValue();
                }
                if ((Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).getValue() != 0.0) {
                    item.getItemProperty(Settings.percentage).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(Messages.Paid)).getValue() * 100
                                    / (Double) item.getItemProperty(myUI.getMessage(Messages.InstPlanDebt)).getValue());
                }
            }
        }
        if (t != null) {
            t.setColumnFooter(myUI.getMessage(Messages.InstPlanDebt),
                    Settings.dFormat2.format(ymr.nets));
            t.setColumnFooter(myUI.getMessage(Messages.Paid),
                    Settings.dFormat2.format(ymr.paid_amounts));
            t.setColumnFooter(myUI.getMessage(Messages.Debt),
                    Settings.dFormat2.format(ymr.debts));
            t.setColumnFooter(myUI.getMessage(Messages.OverPay),
                    Settings.dFormat2.format(ymr.overpays));
            t.setColumnFooter(myUI.getMessage(Messages.InstPlanDebt),
                    Settings.dFormat2.format(ymr.inst_plans));
            if (ymr.inst_plans != 0.0) {
                t.setColumnFooter(Settings.percentage, Settings.dFormat2.format(
                        ymr.paid_amounts * 100 / ymr.inst_plans));
            }
            ymr.inst_plans = 0.0;
            ymr.paid_amounts = 0.0;
            ymr.debts = 0.0;
            ymr.overpays = 0.0;
        }
    }

    public int exec_contr_count(int id) throws SQLException {
        String sql = "SELECT count(contract_id) FROM student_contract "
                + "where contract_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        int i = 0;
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            i = result.getInt("count(contract_id)");
        }
        return i;
    }

    public ContractInfo execSQLTotals(int scl_id, int year_id)
            throws SQLException {
        String sql = "SELECT sum(c.amount) as contract, sum(sc.debt) as debt, "
                + "(sum(c.amount)-sum(sc.contr_with_disc)) as disc, sum(vc.amount) as correction, "
                + "(sum(sc.net_payments)) as payment "
                + "FROM student_contract as sc "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "left join student as st on st.id = sc.student_id "
                + "left join contract as c on sc.contract_id = c.id "
                + "where st.school_id = ? and sc.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        ContractInfo ct = new ContractInfo();
        while (result.next()) {
            ct.setContract(result.getDouble("contract"));
            ct.setDebt(result.getDouble("debt"));
            ct.setDiscount(result.getDouble("disc"));
            ct.setCorrection(result.getDouble("correction"));
            ct.setPaid(result.getDouble("payment"));
            ct.setLeft(result.getDouble("debt") + result.getDouble("contract")
                    - result.getDouble("disc") - result.getDouble("payment")
                    + result.getDouble("correction"));
        }
        return ct;
    }

    public IndexedContainer execSQL_DebtsByClass(MyVaadinUI myUI, Date from,
                                                 Date till, int year_id, String class_ids, String edu_statuses_ids,
                                                 DebtReport dr) throws SQLException {
        String sql = "SELECT st.id, vcs.class_name, st.name, st.surname, ip_temp.amount AS inst_plan, sp_temp.amount AS paid "
                + "FROM student AS st LEFT JOIN "
                + "(SELECT ip.student_id AS stud_id, SUM(ip.amount) AS amount "
                + "FROM student_installement_plan AS ip "
                + "WHERE DATE(ip.date_of_payment) >= ? AND DATE(ip.date_of_payment) <= ? "
                + "AND ip.year_id = ? GROUP BY ip.student_id) AS ip_temp "
                + "ON st.id = ip_temp.stud_id "
                + "LEFT JOIN "
                + "(SELECT sp.student_id AS stud_id, "
                + "(SUM(IF(sp.payment_category_id != 3, "
                + "CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0)) "
                + "-SUM(IF(sp.payment_category_id = 3, "
                + "CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END, 0))) AS amount "
                + "FROM student_payments AS sp WHERE DATE(sp.modification_date) >= ? "
                + "AND DATE(sp.modification_date) <= ? AND sp.year_id = ? "
                + "GROUP BY sp.student_id) AS sp_temp "
                + "ON st.id = sp_temp.stud_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "WHERE vcs.class_name_id IN (" + class_ids + ") AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "and st.entering_year_id <= ? "
                + "ORDER BY vcs.class_number_id, vcs.class_name_id, st.name, st.surname";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setInt(3, year_id);
        stat.setDate(4, new java.sql.Date(from.getTime()));
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, year_id);
        stat.setInt(7, year_id);
        stat.setInt(8, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.InstallmentPlan), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Paid), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Debt), Double.class, 0.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUI.getMessage(Messages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(Messages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    result.getString("class_name"));
            item.getItemProperty(myUI.getMessage(Messages.InstallmentPlan)).setValue(
                    result.getDouble("inst_plan"));
            item.getItemProperty(myUI.getMessage(Messages.Paid)).setValue(
                    result.getDouble("paid"));
            item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(
                    result.getDouble("inst_plan") - result.getDouble("paid"));
            dr.inst_total += result.getDouble("inst_plan");
            dr.paid_total += result.getDouble("paid");
            dr.debt_total += result.getDouble("inst_plan") - result.getDouble("paid");
        }
        return container;
    }

    public ContractInfo execSQL_totalsByScl(MyVaadinUI myUI, int year_id, String edu_statuses_ids,
                                            int school_id) throws SQLException {
        String sql = "SELECT count(st.id) as ttl_students, "
                + "SUM(c.amount) AS contract, SUM(sc.debt) AS debt, "
                + "(SUM(c.amount) - SUM(sc.contr_with_disc)) AS disc,  "
                + "(SUM(sc.contr_with_disc) + SUM(sc.debt) + IFNULL(SUM(vc.amount), 0.0)) AS net, "
                + "SUM(vc.amount) AS correction, SUM(sc.net_payments) AS payment "
                + "FROM student AS st "
                + "LEFT JOIN student_contract AS sc ON st.id = sc.student_id and sc.year_id = ? "
                + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                + "LEFT JOIN contract AS c ON sc.contract_id = c.id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "WHERE st.school_id = ? AND vcs.education_status_id IN (" + edu_statuses_ids + ") and st.entering_year_id <= ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, school_id);
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Total), Double.class, 0.0);
        ContractInfo ct = new ContractInfo();
        while (result.next()) {
            ct.setStudents(result.getInt("ttl_students"));
            ct.setContract(result.getDouble("contract"));
            ct.setDebt(result.getDouble("debt"));
            ct.setDiscount(result.getDouble("disc"));
            ct.setCorrection(result.getDouble("correction"));
            ct.setPaid(result.getDouble("payment"));
            ct.setNet(result.getDouble("net"));
            ct.setLeft(result.getDouble("net") - result.getDouble("payment"));
        }
        return ct;
    }

    public int exec_next_contract_number(int school_id, int year_id) throws SQLException {
        String sql = "SELECT (ifnull(max(tr.contract_number), 0) + 1) as num FROM student_contract tr "
                + "LEFT JOIN student st ON st.id = tr.student_id where st.school_id = ? and tr.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("num");
        }
        return 0;
    }

    public void execFinancialHistory(MyVaadinUI myUI, int studentId, Table t) throws SQLException {
        String currency = Settings.USD;
        String sql = "SELECT * FROM (" +
                "(SELECT concat('sc', sc.id) as  id, y.name AS academic_year, sc.creation_date AS creation_date, " +
                "'Контракт' AS type, c.name AS note, c.amount AS amount FROM student_contract AS sc " +
                "LEFT JOIN year AS y ON sc.year_id = y.id " +
                "LEFT JOIN contract AS c ON sc.contract_id = c.id WHERE sc.student_id = ? " +
                "ORDER BY sc.year_id, sc.creation_date) " +
                "UNION " +
                "(SELECT concat('sd', sd.id) as  id, y.name AS academic_year, sd.creation_date AS creation_date, " +
                "'Скидка' AS type, " +
                "CASE d.discount_type_id WHEN 1 THEN CONCAT(d.name, ' - ', d.amount, '%') " +
                "WHEN 2 THEN CONCAT(d.name, ' - ', d.amount, ' " + currency + "') " +
                "WHEN 3 THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') " +
                "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, ' " + currency + "') END AS note, " +
                "sd.discount_value AS amount FROM student_discount AS sd " +
                "LEFT JOIN year AS y ON sd.year_id = y.id " +
                "LEFT JOIN discount AS d ON d.id = sd.discount_id " +
                "WHERE sd.student_id = ? ORDER BY sd.year_id, sd.creation_date) " +
                "UNION " +
                "(SELECT concat('scc', scc.id) as  id, y.name AS academic_year, scc.creation_date AS creation_date, " +
                "'Корректировка' AS type, CONCAT('(', amr_t.type, ') ', amr_t.name) AS note, " +
                "IF(amr_t.type = '+', scc.amount, - scc.amount) AS amount " +
                "FROM student_correction AS scc LEFT JOIN year AS y ON scc.year_id = y.id " +
                "LEFT JOIN correction_type AS amr_t ON scc.correction_type_id = amr_t.id " +
                "WHERE scc.student_id = ? ORDER BY scc.year_id, scc.creation_date) " +
                "UNION " +
                "(SELECT concat('sp', sp.id) as  id, y.name AS academic_year, DATE(sp.modification_date) AS creation_date, " +
                "'Оплата' AS type, CONCAT(pt.name, '; ', 'Курс - ', sp.dollar_rate) AS note, " +
                "CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END AS amount " +
                "FROM student_payments AS sp LEFT JOIN year AS y ON sp.year_id = y.id " +
                "LEFT JOIN payment_type AS pt ON sp.payment_type_id = pt.id " +
                "WHERE sp.student_id = ? and sp.payment_category_id != 3 ORDER BY sp.year_id, DATE(sp.modification_date)) " +
                "UNION " +
                "(SELECT concat('sp', sp.id) as  id, y.name AS academic_year, DATE(sp.modification_date) AS creation_date, " +
                "'Возврат' AS type, CONCAT(pt.name, '; ', 'Курс - ', sp.dollar_rate) AS note, " +
                "CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END AS amount " +
                "FROM student_payments AS sp LEFT JOIN year AS y ON sp.year_id = y.id " +
                "LEFT JOIN payment_type AS pt ON sp.payment_type_id = pt.id " +
                "WHERE sp.student_id = ? and sp.payment_category_id = 3 ORDER BY sp.year_id, DATE(sp.modification_date))) AS t " +
                "ORDER BY t.academic_year, t.creation_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, studentId);
        stat.setInt(2, studentId);
        stat.setInt(3, studentId);
        stat.setInt(4, studentId);
        stat.setInt(5, studentId);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.AcademicYear), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Type), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Debt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Repayment), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Balance), String.class, "0.00");
        double currentBalance = 0.0, totalDebt = 0.0;
        Item item;

        while (result.next()) {
            item = container.addItem(result.getString("t.id"));
            item.getItemProperty(myUI.getMessage(Messages.AcademicYear)).setValue(
                    result.getString("t.academic_year"));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format(result.getDate("t.creation_date")));
            item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(result.getString("t.type"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("t.note"));
            if (result.getString("t.type").equals(myUI.getMessage(Messages.Payment)) ||
                    result.getString("t.type").equals(myUI.getMessage(Messages.Discount)) ||
                    (result.getString("t.type").equals(myUI.getMessage(Messages.Correction))
                            && result.getDouble("t.amount") < 0.0)) {
                if (result.getDouble("t.amount") < 0.0) {
                    item.getItemProperty(myUI.getMessage(Messages.Repayment)).setValue(-1 * result.getDouble("t.amount"));
                    currentBalance -= (-1 * result.getDouble("t.amount"));
                } else {
                    item.getItemProperty(myUI.getMessage(Messages.Repayment)).setValue(result.getDouble("t.amount"));
                    currentBalance -= result.getDouble("t.amount");
                }

            } else {
                item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(result.getDouble("t.amount"));
                currentBalance += result.getDouble("t.amount");
                totalDebt += result.getDouble("t.amount");
            }
            if (currentBalance < 0) {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(
                        (Settings.dFormat2.format(currentBalance * -1))
                                + " (" + myUI.getMessage(Messages.Repayment).charAt(0) + ")");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(Settings.dFormat2.format(currentBalance)
                        + " (" + myUI.getMessage(Messages.Debt).charAt(0) + ")");
            }
            t.setColumnFooter(myUI.getMessage(Messages.Balance),
                    item.getItemProperty(myUI.getMessage(Messages.Balance)).getValue().toString());
        }
        t.setColumnFooter(myUI.getMessage(Messages.Debt), Settings.dFormat2.format(totalDebt));
        t.setColumnFooter(myUI.getMessage(Messages.Repayment), Settings.dFormat2.format(totalDebt - currentBalance));
        t.setContainerDataSource(container);
    }

    public void execDebtsAndRepayments(MyVaadinUI myUI, int yearId, String classIds, String educationStatusIds,
                                       Date fromDate, Date tillDate, Table t) throws SQLException {
        String currency = Settings.USD;
        String sql = " SELECT * FROM (" +
                "(SELECT concat('sc', sc.id) as  id, vcs.class_name AS class, " +
                "vcs.education_status AS education_status, st.login AS login, " +
                "CONCAT(st.name, ' ', st.surname) AS fullname, sc.creation_date AS creation_date, 'Контракт' AS type, " +
                "c.name AS note, c.amount AS amount FROM student_contract AS sc " +
                "LEFT JOIN student AS st ON st.id = sc.student_id LEFT JOIN year AS y ON sc.year_id = y.id " +
                "LEFT JOIN contract AS c ON sc.contract_id = c.id " +
                "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? " +
                "WHERE sc.year_id = ? AND vcs.class_name_id IN (" + classIds + ") AND vcs.education_status_id IN (" + educationStatusIds + ") " +
                "ORDER BY sc.creation_date) UNION " +
                "(SELECT concat('sd', sd.id) as  id, vcs.class_name AS class, " +
                "vcs.education_status AS education_status, st.login AS login, " +
                "CONCAT(st.name, ' ', st.surname) AS fullname, sd.creation_date AS creation_date, 'Скидка' AS type, " +
                "CASE d.discount_type_id WHEN 1 THEN CONCAT(d.name, ' - ', d.amount, '%') " +
                "WHEN 2 THEN CONCAT(d.name, ' - ', d.amount, ' " + currency + "') " +
                "WHEN 3 THEN CONCAT(d.name, ' - ', sd.free_entry_amount, '%') " +
                "ELSE CONCAT(d.name, ' - ', sd.free_entry_amount, ' " + currency + "') END AS note, sd.discount_value AS amount " +
                "FROM student_discount AS sd " +
                "LEFT JOIN student AS st ON st.id = sd.student_id LEFT JOIN year AS y ON sd.year_id = y.id " +
                "LEFT JOIN discount AS d ON d.id = sd.discount_id " +
                "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? " +
                "WHERE sd.year_id = ? AND vcs.class_name_id IN (" + classIds + ") AND vcs.education_status_id IN (" + educationStatusIds + ") " +
                "ORDER BY sd.creation_date) UNION (" +
                "SELECT concat('scc', scc.id) as  id, vcs.class_name AS class, " +
                "vcs.education_status AS education_status, st.login AS login, " +
                "CONCAT(st.name, ' ', st.surname) AS fullname, scc.creation_date AS creation_date, 'Корректировка' AS type, " +
                "CONCAT('(', amr_t.type, ') ', amr_t.name) AS note, " +
                "IF(amr_t.type = '+', scc.amount, - scc.amount) AS `amount` FROM student_correction AS scc " +
                "LEFT JOIN student AS st ON st.id = scc.student_id LEFT JOIN year AS y ON scc.year_id = y.id " +
                "LEFT JOIN correction_type AS amr_t ON scc.correction_type_id = amr_t.id " +
                "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? " +
                "WHERE scc.year_id = ? AND vcs.class_name_id IN (" + classIds + ") AND vcs.education_status_id IN (" + educationStatusIds + ") " +
                "ORDER BY scc.creation_date) UNION " +
                "(SELECT concat('sp', sp.id) as  id, vcs.class_name AS class, vcs.education_status AS education_status, st.login AS login, " +
                "CONCAT(st.name, ' ', st.surname) AS fullname, DATE(sp.modification_date) AS creation_date, " +
                "IF(sp.payment_category_id = 3, 'Возврат', 'Оплата') AS type, " +
                "CONCAT(pc.name, '; ', pt.name, '; ', 'Курс - ', sp.dollar_rate) AS note, " +
                "CASE WHEN sp.acc_currency_id = 1 THEN sp.amount / sp.dollar_rate ELSE sp.amount END AS amount " +
                "FROM student_payments AS sp LEFT JOIN student AS st ON st.id = sp.student_id " +
                "LEFT JOIN year AS y ON sp.year_id = y.id LEFT JOIN payment_type AS pt ON sp.payment_type_id = pt.id " +
                "LEFT JOIN payment_category AS pc ON sp.payment_category_id = pc.id " +
                "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? " +
                "WHERE sp.year_id = ? AND vcs.class_name_id IN (" + classIds + ") AND vcs.education_status_id IN (" + educationStatusIds + ") " +
                "ORDER BY DATE(sp.modification_date))) AS t WHERE 1 ";
        if (fromDate != null) {
            sql += "AND t.creation_date >= ? ";
        }
        if (tillDate != null) {
            sql += "AND t.creation_date <= ? ";
        }
        sql += "ORDER BY t.creation_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        int counter = 0;
        stat.setInt(++counter, yearId);
        stat.setInt(++counter, yearId);
        stat.setInt(++counter, yearId);
        stat.setInt(++counter, yearId);
        stat.setInt(++counter, yearId);
        stat.setInt(++counter, yearId);
        stat.setInt(++counter, yearId);
        stat.setInt(++counter, yearId);
        if (fromDate != null) {
            stat.setDate(++counter, new java.sql.Date(fromDate.getTime()));
        }
        if (tillDate != null) {
            stat.setDate(++counter, new java.sql.Date(tillDate.getTime()));
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.EducationStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Type), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Debt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Repayment), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Balance), String.class, "0.00");
        double currentBalance = 0.0, totalDebt = 0.0;
        Item item;

        while (result.next()) {
            item = container.addItem(result.getString("t.id"));
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    result.getString("t.class"));
            item.getItemProperty(myUI.getMessage(Messages.Id)).setValue(
                    result.getString("t.login"));
            item.getItemProperty(myUI.getMessage(Messages.FullName)).setValue(
                    result.getString("t.fullname"));
            item.getItemProperty(myUI.getMessage(Messages.EducationStatus)).setValue(
                    result.getString("t.education_status"));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format(result.getDate("t.creation_date")));
            item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(result.getString("t.type"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("t.note"));
            if (result.getString("t.type").equals(myUI.getMessage(Messages.Payment)) ||
                    result.getString("t.type").equals(myUI.getMessage(Messages.Discount)) ||
                    (result.getString("t.type").equals(myUI.getMessage(Messages.Correction))
                            && result.getDouble("t.amount") < 0.0)) {
                if (result.getDouble("t.amount") < 0.0) {
                    item.getItemProperty(myUI.getMessage(Messages.Repayment)).setValue(-1 * result.getDouble("t.amount"));
                    currentBalance -= (-1 * result.getDouble("t.amount"));
                } else {
                    item.getItemProperty(myUI.getMessage(Messages.Repayment)).setValue(result.getDouble("t.amount"));
                    currentBalance -= result.getDouble("t.amount");
                }

            } else {
                item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(result.getDouble("t.amount"));
                currentBalance += result.getDouble("t.amount");
                totalDebt += result.getDouble("t.amount");
            }
            if (currentBalance < 0) {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(
                        (Settings.dFormat2.format(currentBalance * -1))
                                + " (" + myUI.getMessage(Messages.Repayment).charAt(0) + ")");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(Settings.dFormat2.format(currentBalance)
                        + " (" + myUI.getMessage(Messages.Debt).charAt(0) + ")");
            }
            t.setColumnFooter(myUI.getMessage(Messages.Balance),
                    item.getItemProperty(myUI.getMessage(Messages.Balance)).getValue().toString());
        }
        t.setColumnFooter(myUI.getMessage(Messages.Debt), Settings.dFormat2.format(totalDebt));
        t.setColumnFooter(myUI.getMessage(Messages.Repayment), Settings.dFormat2.format(totalDebt - currentBalance));
        t.setContainerDataSource(container);
    }
}
