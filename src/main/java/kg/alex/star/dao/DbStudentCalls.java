/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.utils.Settings;
import kg.alex.star.i18n.Messages;
import kg.alex.star.reports.students.CallsReport;
import kg.alex.star.ui.StudentDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class DbStudentCalls extends BaseDb {

    public DbStudentCalls() throws Exception {
        super();
    }

    public int exec_insert(int st_id, int year_id, int emp_id, String note)
            throws SQLException {
        String sql = "INSERT INTO student_calls (student_id,year_id,employee_id,"
                + "note, modification_date) VALUES(?,?,?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, year_id);
        stat.setInt(3, emp_id);
        stat.setString(4, note);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_St_Calls(MyVaadinUI myUI, int stud_id, int year_id,
                                             StudentDefinitionView dw) throws SQLException {


        String sql = "SELECT sc.id, sc.note, concat(e.name, ' ', e.surname) as fullname, sc.modification_date FROM student_calls as sc "
                + "left join employee as e on sc.employee_id = e.id "
                + "where sc.student_id = ? and sc.year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.prepareCallsContainer();
        while (result.next()) {
            String id = result.getString("sc.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    dw.createButton(myUI.getMessage(Messages.DeleteButton), id,
                            Settings.dbStudentCalls, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(
                    Settings.df.format(result.getDate("sc.modification_date")));
            item.getItemProperty(myUI.getMessage(Messages.WhoCalled)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                    dw.createTextField(result.getString("sc.note"),
                            myUI.getMessage(Messages.Note), id, new StringLengthValidator(
                                    myUI.getMessage(Messages.NotificationWrongValue),
                                    null, 250, true), false));
            item.getItemProperty(Settings.crud_status)
                    .setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public int exec_delete(String id) throws SQLException {
        String sql = "DELETE from student_calls where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public int exec_update(String note, int id) throws SQLException {
        String sql = "update student_calls set note = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, note);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_getCallsReport(MyVaadinUI myUI, Date from,
                                                   Date till, int year_id, String class_ids, String edu_statuses_ids,
                                                   CallsReport cr) throws SQLException {

        String sql = "SELECT sc.id, st.name, st.surname, DATE(sc.modification_date) AS date, "
                + "vcs.class_name, sc.note, concat(e.name, ' ', e.surname) as fullname "
                + "FROM student_calls AS sc "
                + "LEFT JOIN student AS st ON sc.student_id = st.id "
                + "LEFT JOIN employee AS e ON sc.employee_id = e.id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "WHERE vcs.class_name_id IN (" + class_ids + ") AND DATE(sc.modification_date) >= ? "
                + "AND DATE(sc.modification_date) <= ? AND sc.year_id = ? "
                + "AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "ORDER BY vcs.class_number_id, vcs.class_name_id, st.name, st.surname, sc.modification_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setDate(2, new java.sql.Date(from.getTime()));
        stat.setDate(3, new java.sql.Date(till.getTime()));
        stat.setInt(4, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.WhoCalled), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("sc.id"));
            item.getItemProperty(myUI.getMessage(Messages.FirstName)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUI.getMessage(Messages.LastName)).setValue(
                    result.getString("st.surname"));
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    result.getString("vcs.class_name"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                    result.getString("sc.note"));
            item.getItemProperty(myUI.getMessage(Messages.WhoCalled)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(
                    Settings.df.format((result.getDate("date"))));
            cr.total++;
        }
        return container;
    }

    public String exec_getLastCall(int st_id) throws SQLException {
        String sql = "SELECT CONCAT(DATE(modification_date), IF((note IS NOT NULL AND note != ''), CONCAT(' (', note, ')'), '')) as last_call "
                + "FROM student_calls "
                + "WHERE student_id = ? order by id desc limit 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        ResultSet result = stat.executeQuery();
        String c = "";
        while (result.next()) {
            c = result.getString("last_call");
        }
        return c;

    }
}
