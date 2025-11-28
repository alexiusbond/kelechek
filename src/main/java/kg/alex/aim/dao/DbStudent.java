/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.domain.EducationStatus;
import kg.alex.aim.domain.Student;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.reports.students.StatusesReport;
import kg.alex.aim.ui.CallsView;
import kg.alex.aim.ui.IssueOrderView;
import kg.alex.aim.ui.StudentDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class DbStudent extends BaseDb {

    public DbStudent() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, int year_id,
                                    StudentDefinitionView sdv, String edu_sts) throws SQLException {

        if (edu_sts.isEmpty()) {
            edu_sts = "-1";
        }
        String sql = "SELECT s.id, s.login, s.name, s.surname, s.address, s.middle_name, s.entering_year_id, " +
                     "s.date_of_birth, s.photo, s.gender_id, y.name, sr.fullname, sr.phone, rel.name, " +
                     "ifnull(vcs.education_status, vlcs.education_status) as education_status, " +
                     "ifnull(vcs.class_name, vlcs.class_name) as class_name, " +
                     "ifnull(vcs.class_name_id, vlcs.class_name_id) as class_name_id, " +
                     "ifnull(vcs.education_status_id, vlcs.education_status_id) as education_status_id " +
                     "FROM student as s " +
                     "LEFT JOIN student_relatives AS sr ON s.id = sr.student_id AND sr.is_main = 1 " +
                     "LEFT JOIN relatives AS rel ON sr.relatives_id = rel.id " +
                     "left join view_student_class_status as vcs on s.id = vcs.student_id and vcs.year_id = ? " +
                     "left join view_student_last_class_status as vlcs on s.id = vlcs.student_id " +
                     "left join year as y on s.entering_year_id = y.id " +
                     "WHERE s.school_id = ? and s.entering_year_id <= ? and (vcs.education_status_id in (" + edu_sts + ") " +
                     "or vcs.education_status_id IS NULL and vlcs.education_status_id in (" + edu_sts + ")) " +
                     "GROUP BY s.id ORDER BY vcs.education_status_id, s.name, s.surname";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, scl_id);
        stat.setInt(3, myUi.getUser().getCurrent_year().getId());
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.StudentId), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Relative), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MiddleName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.DateOfBirth), Date.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Photo), String.class, null);
        container.addContainerProperty(Settings.gender_id, Integer.class, 0);
        container.addContainerProperty(Settings.education_status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.EducationStatus), String.class, null);
        container.addContainerProperty(Settings.class_name_id, Integer.class, 0);
        container.addContainerProperty(Settings.entering_year_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.EnteringYear), String.class, null);
        int total = 0;
        sdv.eduStatCont.getContainerProperty(1, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(2, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(3, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(4, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(5, Settings.count).setValue(0);
        sdv.eduStatCont.getContainerProperty(6, Settings.count).setValue(0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUi.getMessage(Messages.StudentId)).setValue(
                    result.getString("s.login"));
            item.getItemProperty(myUi.getMessage(Messages.FirstName)).setValue(
                    result.getString("s.name"));
            item.getItemProperty(myUi.getMessage(Messages.LastName)).setValue(
                    result.getString("s.surname"));
            item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                    result.getString("s.address"));
            if (result.getString("rel.name") != null) {
                item.getItemProperty(myUi.getMessage(Messages.Relative)).setValue(
                        result.getString("rel.name") + " - " + result.getString("sr.fullname"));
                item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                        result.getString("sr.phone"));
            }
            item.getItemProperty(myUi.getMessage(Messages.MiddleName)).setValue(
                    result.getString("s.middle_name"));
            item.getItemProperty(myUi.getMessage(Messages.DateOfBirth)).setValue(
                    result.getDate("s.date_of_birth"));
            item.getItemProperty(myUi.getMessage(Messages.Photo)).setValue(result.getString("s.photo"));
            item.getItemProperty(Settings.gender_id).setValue(result.getInt("s.gender_id"));
            item.getItemProperty(Settings.education_status_id).setValue(result.getInt("education_status_id"));
            sdv.eduStatCont.getContainerProperty(result.getInt("education_status_id"), Settings.count)
                    .setValue(((Integer) sdv.eduStatCont.getContainerProperty(
                            result.getInt("education_status_id"), Settings.count).getValue()) + 1);
            item.getItemProperty(myUi.getMessage(Messages.EducationStatus)).setValue(
                    result.getString("education_status"));
            item.getItemProperty(Settings.class_name_id).setValue(
                    result.getInt("class_name_id"));
            item.getItemProperty(myUi.getMessage(Messages.ClassName)).setValue(
                    result.getString("class_name"));
            item.getItemProperty(myUi.getMessage(Messages.EnteringYear)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(Settings.entering_year_id).setValue(
                    result.getInt("s.entering_year_id"));
            total++;
        }
        sdv.eduStatCont.getContainerProperty(6, Settings.count).setValue(total);
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int scl_id, int year_id, String edu_sts)
            throws SQLException {

        String sql = "SELECT s.id, s.name, s.surname, " +
                     "IFNULL(vcs.class_number, vlcs.class_number) as class_number, " +
                     "IFNULL(vcs.class_name, vlcs.class_name) as class_name " +
                     "FROM student as s " +
                     "left join view_student_class_status as vcs on vcs.student_id = s.id and vcs.year_id = ? " +
                     "left join view_student_last_class_status as vlcs on vlcs.student_id = s.id " +
                     "WHERE s.school_id = ? and vcs.education_status_id in (" + edu_sts + ") " +
                     "ORDER BY vcs.class_number_id, vcs.class_name_id, s.name, s.surname";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ClassNumber), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                    result.getString("s.surname")
                    + " " + result.getString("s.name"));
            item.getItemProperty(myUi.getMessage(Messages.ClassNumber)).setValue(
                    result.getString("class_number"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("s.surname") + " " +
                    result.getString("s.name") + " - " +
                    result.getString("class_name"));
        }
        return container;
    }

    public IndexedContainer execSQL_for_orders(MyVaadinUI myUi, int school_id, int year_id,
                                               IssueOrderView iv) throws SQLException {

        String sql = "SELECT s.id, s.login, s.name, s.surname, s.entering_year_id, " +
                     "ifnull(vcs.education_status, vlcs.education_status) as education_status, " +
                     "ifnull(vcs.class_name, vlcs.class_name) as class_name, " +
                     "ifnull(vcs.class_name_id, vlcs.class_name_id) as class_name_id, " +
                     "ifnull(vcs.education_status_id, vlcs.education_status_id) as education_status_id " +
                     "FROM student as s " +
                     "left join view_student_class_status as vcs on s.id = vcs.student_id and vcs.year_id = ? " +
                     "left join view_student_last_class_status as vlcs on s.id = vlcs.student_id " +
                     "WHERE s.school_id = ? and s.entering_year_id <= ? " +
                     "ORDER BY vcs.education_status_id, s.name, s.surname";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, school_id);
        stat.setInt(3, myUi.getUser().getCurrent_year().getId());
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.button, Button.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.EducationStatus), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(Settings.class_id, Integer.class, 0);
        container.addContainerProperty(Settings.education_status_id, Integer.class, 0);
        container.addContainerProperty(Settings.entering_year_id, Integer.class, 0);
        container.addContainerProperty(Settings.id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("s.id"));
            item.getItemProperty(Settings.button).setValue(
                    iv.createButton(myUi.getMessage(Messages.Details),
                            result.getString("s.id"), FontAwesome.INFO));
            item.getItemProperty(myUi.getMessage(Messages.Id)).setValue(
                    result.getString("s.login"));
            item.getItemProperty(myUi.getMessage(Messages.FirstName)).setValue(
                    result.getString("s.name"));
            item.getItemProperty(myUi.getMessage(Messages.LastName)).setValue(
                    result.getString("s.surname"));
            item.getItemProperty(myUi.getMessage(Messages.EducationStatus)).setValue(
                    result.getString("education_status"));
            item.getItemProperty(myUi.getMessage(Messages.ClassName)).setValue(
                    result.getString("class_name"));
            item.getItemProperty(Settings.class_id).setValue(
                    result.getInt("class_name_id"));
            item.getItemProperty(Settings.education_status_id).setValue(
                    result.getInt("education_status_id"));
            item.getItemProperty(Settings.entering_year_id).setValue(
                    result.getInt("s.entering_year_id"));
        }
        return container;
    }

    public int exec_insert(Student s) throws SQLException {
        String sql = "INSERT ignore INTO student (login, password, name, "
                     + "surname, middle_name, date_of_birth, photo, school_id, gender_id, "
                     + "entering_year_id, employee_id, modification_date, address) "
                     + "VALUES(?,?,?,?,?,?,?,?,?,?,?,NOW(),?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, s.getLogin());
        stat.setString(2, s.getPassword());
        stat.setString(3, s.getName().trim());
        stat.setString(4, s.getSurname().trim());
        if (s.getMiddle_name() != null && !s.getMiddle_name().isEmpty()) {
            stat.setString(5, s.getMiddle_name().trim());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setDate(6, new java.sql.Date(s.getBirth_date().getTime()));
        stat.setString(7, s.getPhoto());
        stat.setInt(8, s.getSchool_id());
        stat.setInt(9, s.getGender_id());
        stat.setInt(10, s.getEntering_year_id());
        stat.setInt(11, s.getEmployee_id());
        stat.setString(12, s.getAddress());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Student s) throws SQLException {
        String sql = "UPDATE student SET login = ?, name = ?, surname = ?, middle_name = ?, " +
                     "date_of_birth = ?, photo = ?, gender_id = ?, employee_id = ?, modification_date = NOW(), " +
                     "address = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, s.getLogin());
        stat.setString(2, s.getName().trim());
        stat.setString(3, s.getSurname().trim());
        if (s.getMiddle_name() != null && !s.getMiddle_name().isEmpty()) {
            stat.setString(4, s.getMiddle_name().trim());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setDate(5, new java.sql.Date(s.getBirth_date().getTime()));
        stat.setString(6, s.getPhoto());
        stat.setInt(7, s.getGender_id());
        stat.setInt(8, s.getEmployee_id());
        stat.setString(9, s.getAddress());
        stat.setInt(10, s.getId());
        return stat.executeUpdate();
    }

    public int exec_updateLogin(int student_id, String login) throws SQLException {
        String sql = "UPDATE student SET login = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        stat.setInt(2, student_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execStud_sel(MyVaadinUI myUi, int cl_id, int year_id)
            throws SQLException {
        String sql = "SELECT st.id, st.name, st.surname, st.middle_name "
                     + "FROM student AS st "
                     + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                     + "WHERE vcs.class_name_id = ?  and st.entering_year_id <= ? ORDER BY st.name, st.surname";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, cl_id);
        stat.setInt(3, year_id);

        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            String fullname;
            fullname = result.getString("st.name") + " " + result.getString("st.surname");
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(fullname);
        }
        return container;
    }

    public int exec_delete(int id) throws SQLException {
        String sql = "delete sc, sd, ip, sp, sr, sa, so, sca, cr from student as st "
                     + "left join student_payments as sp on sp.student_id = st.id "
                     + "left join student_contract as sc on sc.student_id = st.id "
                     + "left join student_discount as sd on sd.student_id = st.id "
                     + "left join student_installement_plan as ip on ip.student_id = st.id "
                     + "left join student_relatives as sr on sr.student_id = st.id "
                     + "left join student_accessories as sa on sa.student_id = st.id "
                     + "left join student_orders as so on so.student_id = st.id "
                     + "left join student_calls as sca on sca.student_id = st.id "
                     + "left join student_correction as cr on cr.student_id = st.id "
                     + "where st.id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQLCalls(MyVaadinUI myUi, int year_id, String class_ids,
                                         String edu_statuses_ids, CallsView cv) throws SQLException {

        String sql = "select st.id, st.login, st.name, st.surname, vcs.class_name, "
                     + "concat(sr.phone,' (',sr.fullname,')') "
                     + "as is_main, MAX(IF(ip.is_visible = 1, ip.date_of_payment, NULL)) AS plan_debt_date, "
                     + "ifnull((sum(ip.amount) - sc.net_payments),0.0) as plan_debt, "
                     + "sc.contr_with_disc + sc.debt + ifnull(vc.amount, 0.0) - sc.net_payments as remain, "
                     + "(SELECT concat(FORMAT(sp.amount, 2), ' (', date(sp.modification_date),')') FROM student_payments sp "
                     + "where sp.student_id = st.id and sp.year_id = ? and sp.payment_category_id != 3 order by sp.id desc limit 1) as last_payment, "
                     + "(SELECT CONCAT(DATE_FORMAT(modification_date, '%d-%m-%Y'), IF((note IS NOT NULL AND note != ''), CONCAT(' (', note, ')'), '')) "
                     + "FROM student_calls as sc WHERE student_id = st.id order by sc.id desc limit 1) AS last_call "
                     + "from student as st "
                     + "left join student_relatives as sr on st.id = sr.student_id "
                     + "left join student_contract as sc on st.id = sc.student_id "
                     + "LEFT JOIN view_corrections AS vc ON vc.student_id = sc.student_id and vc.year_id = sc.year_id "
                     + "left join student_installement_plan as ip on st.id = ip.student_id "
                     + "and sc.year_id = ip.year_id AND ip.date_of_payment <= NOW() "
                     + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                     + "where sr.is_main = 1 and sc.year_id = ? "
                     + "and vcs.class_name_id in(" + class_ids + ") AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                     + "group by st.id having plan_debt > 0 order by vcs.class_number_id, vcs.class_name_id, st.name, st.surname";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = cv.prepareContainer();
        while (result.next()) {
            Item item = container.addItem(result.getInt("st.id"));
            item.getItemProperty(myUi.getMessage(Messages.Id)).setValue(result.getString("st.login"));
            item.getItemProperty(myUi.getMessage(Messages.FirstName)).setValue(result.getString("st.name"));
            item.getItemProperty(myUi.getMessage(Messages.LastName)).setValue(result.getString("st.surname"));
            item.getItemProperty(myUi.getMessage(Messages.ClassName)).setValue(
                    result.getString("vcs.class_name"));
            item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                    result.getString("is_main"));
            if (result.getDate("plan_debt_date") != null) {
                item.getItemProperty(myUi.getMessage(Messages.PlanDebtDate)).setValue(
                        Settings.df.format(result.getDate("plan_debt_date")));
            }
            item.getItemProperty(myUi.getMessage(Messages.InstPlanDebt)).setValue(
                    result.getDouble("plan_debt"));
            item.getItemProperty(myUi.getMessage(Messages.Remain)).setValue(result.getDouble("remain"));
            cv.total += result.getDouble("plan_debt");
            if (result.getString("last_call") != null) {
                item.getItemProperty(myUi.getMessage(Messages.LastCall)).setValue(result.getString("last_call"));
            }
            if (result.getString("last_payment") != null) {
                item.getItemProperty(myUi.getMessage(Messages.LastPayment)).setValue(result.getString("last_payment"));
            }
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(
                    cv.createTextField(result.getInt("st.id")));
            item.getItemProperty(Settings.button).setValue(cv.createButton(result.getInt("st.id")));
        }
        return container;
    }

    public EducationStatus execEduCount(int scl_id, int year_id)
            throws SQLException {
        String sql = "SELECT count(*) as ttl, "
                     + "count(if(vcs.education_status_id = 1, vcs.education_status_id, null)) as prereg, "
                     + "count(if(vcs.education_status_id = 2, vcs.education_status_id, null)) as active, "
                     + "count(if(vcs.education_status_id = 3, vcs.education_status_id, null)) as notcon, "
                     + "count(if(vcs.education_status_id = 4, vcs.education_status_id, null)) as outof, "
                     + "count(if(vcs.education_status_id = 5, vcs.education_status_id, null)) as graduated "
                     + "FROM student as st "
                     + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                     + "where st.school_id = ? and st.entering_year_id <= ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, scl_id);
        stat.setInt(3, year_id);
        ResultSet result = stat.executeQuery();
        EducationStatus e = new EducationStatus();
        while (result.next()) {
            e.setPre_registered(result.getString("prereg"));
            e.setActive(result.getString("active"));
            e.setNot_confirmed(result.getString("notcon"));
            e.setOutOf(result.getString("outof"));
            e.setGraduated(result.getString("graduated"));
            e.setTotal(result.getString("ttl"));
        }
        return e;
    }

    public void execSQL_Statuses_by_classes(MyVaadinUI myUI, int year_id,
                                            StatusesReport sr) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT sch.id, sch.name_ru, COUNT(IF(st.entering_year_id <= "
                                              + year_id + " AND vcs.class_number_id IN ("
                                              + Settings.convertCollectionToStr(((Set<?>) sr.classTable.getValue())) + ") "
                                              + "AND vcs.education_status_id IN ("
                                              + Settings.convertCollectionToStr(((Set<?>) sr.statusMS.getValue())) + "),1,NULL)) AS quantity");
        Iterator<?> class_iter = ((Set<?>) sr.classTable.getValue()).iterator();
        Iterator<?> status_iter;
        while (class_iter.hasNext()) {
            Object nextClass = class_iter.next();
            status_iter = ((Set<?>) sr.statusMS.getValue()).iterator();
            while (status_iter.hasNext()) {
                Object nextStatus = status_iter.next();
                sql.append(", COUNT(IF(st.entering_year_id <= ")
                        .append(year_id).append(" and vcs.class_number_id = ")
                        .append(nextClass).append(" and vcs.education_status_id = ")
                        .append(nextStatus).append(", 1, NULL)) AS quantity")
                        .append(nextClass).append("_").append(nextStatus).append(" ");
            }
        }
        sql.append(" FROM school as sch "
                   + "LEFT JOIN student AS st on st.school_id = sch.id "
                   + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                   + "WHERE sch.id IN (")
                .append(Settings.convertCollectionToStr(((Set<?>) sr.schoolsTable.getValue()))).append(") ")
                .append("GROUP BY sch.id");
        PreparedStatement stat = dbCon.prepareStatement(sql.toString());
        stat.setInt(1, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.School), String.class, null);
        class_iter = ((Set<?>) sr.classTable.getValue()).iterator();
        while (class_iter.hasNext()) {
            Object nextClass = class_iter.next();
            status_iter = ((Set<?>) sr.statusMS.getValue()).iterator();
            while (status_iter.hasNext()) {
                Object nextStatus = status_iter.next();
                {
                    container.addContainerProperty(sr.classTable.getContainerProperty(
                                    nextClass, myUI.getMessage(Messages.Title)).getValue() + " "
                                                   + myUI.getMessage(Messages.ClassName) + " "
                                                   + sr.statusMS.getContainerProperty(
                                    nextStatus, myUI.getMessage(Messages.Title)).getValue(),
                            Integer.class, 0);
                }
            }
        }
        container.addContainerProperty(myUI.getMessage(Messages.Total), Integer.class, 0);
        sr.dataTable.setContainerDataSource(container);
        sr.dataTable.setColumnFooter(myUI.getMessage(Messages.School),
                myUI.getMessage(Messages.Total));
        int counter = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("sch.id"));
            item.getItemProperty(myUI.getMessage(Messages.School)).setValue(
                    result.getString("sch.name_ru"));
            class_iter = ((Set<?>) sr.classTable.getValue()).iterator();
            String footerVal;
            while (class_iter.hasNext()) {
                Object nextClass = class_iter.next();
                status_iter = ((Set<?>) sr.statusMS.getValue()).iterator();
                while (status_iter.hasNext()) {
                    Object nextStatus = status_iter.next();
                    item.getItemProperty(sr.classTable.getContainerProperty(
                            nextClass, myUI.getMessage(Messages.Title)).getValue() + " "
                                         + myUI.getMessage(Messages.ClassName) + " "
                                         + sr.statusMS.getContainerProperty(
                            nextStatus, myUI.getMessage(Messages.Title)).getValue()).setValue(
                            result.getInt("quantity" + nextClass + "_" + nextStatus));
                    footerVal = sr.dataTable.getColumnFooter(sr.classTable.getContainerProperty(
                            nextClass, myUI.getMessage(Messages.Title)).getValue() + " "
                                                             + myUI.getMessage(Messages.ClassName) + " "
                                                             + sr.statusMS.getContainerProperty(
                            nextStatus, myUI.getMessage(Messages.Title)).getValue());
                    if (counter != 0) {
                        sr.dataTable.setColumnFooter(sr.classTable.getContainerProperty(
                                        nextClass, myUI.getMessage(Messages.Title)).getValue() + " "
                                                     + myUI.getMessage(Messages.ClassName) + " "
                                                     + sr.statusMS.getContainerProperty(
                                        nextStatus, myUI.getMessage(Messages.Title)).getValue(),
                                (Integer.parseInt(footerVal)
                                 + result.getInt("quantity" + nextClass + "_" + nextStatus)) + "");
                    } else {
                        sr.dataTable.setColumnFooter(sr.classTable.getContainerProperty(
                                        nextClass, myUI.getMessage(Messages.Title)).getValue() + " "
                                                     + myUI.getMessage(Messages.ClassName) + " "
                                                     + sr.statusMS.getContainerProperty(
                                        nextStatus, myUI.getMessage(Messages.Title)).getValue(),
                                result.getInt("quantity" + nextClass + "_" + nextStatus) + "");
                    }
                }
            }
            item.getItemProperty(myUI.getMessage(Messages.Total)).setValue(
                    result.getInt("quantity"));
            footerVal = sr.dataTable.getColumnFooter(
                    myUI.getMessage(Messages.Total));
            if (counter != 0) {
                sr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total),
                        (Integer.parseInt(footerVal)
                         + result.getInt("quantity")) + "");
            } else {
                sr.dataTable.setColumnFooter(myUI.getMessage(Messages.Total),
                        result.getInt("quantity") + "");
            }
            counter++;
        }
    }

    public int execSQL_login(MyVaadinUI myUi, int year_id, int school_id, int class_type_id, int order_num,
                             int min, int max, String school_level) throws SQLException {
        String sql = "SELECT IFNULL(MAX(CAST(RIGHT(st.login, 3) AS UNSIGNED)), ?) + ? AS num " +
                     "FROM student AS st " +
                     "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? " +
                     "LEFT JOIN class_name AS cn ON cn.id = vcs.class_name_id " +
                     "LEFT JOIN class_number AS cnu ON cnu.id = cn.class_number_id " +
                     "WHERE st.entering_year_id = ? AND st.school_id = ? AND cn.class_type_id = ? " +
                     "AND LENGTH(st.login) = 8 AND CAST(RIGHT(st.login, 3) AS UNSIGNED) BETWEEN ? AND ? ";
        if (school_level != null && school_level.equals(myUi.getMessage(Messages.PrimaryCode))) {
            sql += "AND cnu.name < 7";
        } else if (school_level != null && school_level.equals(myUi.getMessage(Messages.SecondaryCode))) {
            sql += "AND cnu.name >= 7";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, min);
        stat.setInt(2, order_num);
        stat.setInt(3, year_id);
        stat.setInt(4, year_id);
        stat.setInt(5, school_id);
        stat.setInt(6, class_type_id);
        stat.setInt(7, min);
        stat.setInt(8, max - 1);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("num");
        }
        return 0;
    }

    public boolean isLoginExists(String login) throws SQLException {
        String sql = "SELECT st.login FROM student AS st where st.login = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        ResultSet result = stat.executeQuery();
        return result.next();
    }
}
