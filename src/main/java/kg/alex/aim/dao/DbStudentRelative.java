/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.domain.Attachment;
import kg.alex.aim.domain.StudentRelative;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.ui.StudentDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * @author alex
 */
public class DbStudentRelative extends BaseDb {

    public DbStudentRelative() throws Exception {
        super();
    }

    public IndexedContainer execSQL_St_Rel(MyVaadinUI myUi, int stud_id,
                                           StudentDefinitionView dw) throws SQLException {

        String sql = "SELECT sr.id, sr.student_id, sr.fullname, sr.given_by, sr.issue_date, "
                + "sr.phone, sr.address, sr.passport, sr.inn, sr.is_main, sr.relatives_id, "
                + "a.id, a.name, a.extension, a.unique_name "
                + "FROM student_relatives as sr "
                + "left join attachments as a on a.id = sr.attachment_id "
                + "where sr.student_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.prepareRelativesContainer();
        while (result.next()) {
            String id = result.getString("sr.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    dw.createButton(myUi.getMessage(Messages.DeleteButton), id,
                            Settings.dbStudentRelatives, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                    dw.createTextField(result.getString("sr.fullname"),
                            myUi.getMessage(Messages.FullName), id, new StringLengthValidator(
                                    myUi.getMessage(Messages.NotificationWrongValue),
                                    1, 250, false), true));
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            CheckBox cb = dw.createCheckBox(result.getBoolean("sr.is_main"),
                    myUi.getMessage(Messages.Responsible), id);
            hl.addComponent(cb);
            hl.setComponentAlignment(cb, Alignment.MIDDLE_LEFT);
            Button b = dw.createButton(myUi.getMessage(Messages.DownLoad), id,
                    Settings.download_button, FontAwesome.DOWNLOAD);
            b.setStyleName("unread");
            b.addStyleName(ValoTheme.BUTTON_SMALL);
            b.setEnabled(false);
            b.setData(null);
            if (result.getInt("a.id") != 0) {
                Attachment a = new Attachment();
                a.setId(result.getInt("a.id"));
                a.setUnique_name(result.getString("a.unique_name"));
                a.setExtension(result.getString("a.extension"));
                a.setName(result.getString("a.name"));
                b.setData(a);
                b.setEnabled(true);
                b.setStyleName(ValoTheme.BUTTON_FRIENDLY);
                b.addStyleName(ValoTheme.BUTTON_SMALL);
            }
            hl.addComponent(b);

            Upload upload = dw.createUpload("", false);
            upload.setId(id);
            upload.setData(b);
            hl.addComponent(upload);
            item.getItemProperty(myUi.getMessage(Messages.Responsible)).setValue(hl);
            if (result.getBoolean("sr.is_main")) {
                item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                        dw.createTextField(result.getString("sr.address"),
                                myUi.getMessage(Messages.Address), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        1, 300, false), true));
                item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                        dw.createTextField(result.getString("sr.phone"),
                                myUi.getMessage(Messages.Phone), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        1, 100, false), true));
                item.getItemProperty(myUi.getMessage(Messages.GivenBy)).setValue(
                        dw.createTextField(result.getString("sr.given_by"),
                                myUi.getMessage(Messages.GivenBy), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        1, 100, false), true));
                item.getItemProperty(myUi.getMessage(Messages.IssueDate)).setValue(
                        dw.createDateField(result.getDate("sr.issue_date"),
                                myUi.getMessage(Messages.IssueDate), id, true, false,
                                Settings.datePattern, Resolution.DAY));
                item.getItemProperty(myUi.getMessage(Messages.Passport)).setValue(
                        dw.createTextField(result.getString("sr.passport"),
                                myUi.getMessage(Messages.Passport), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        1, 50, false), true));
                item.getItemProperty(myUi.getMessage(Messages.INN)).setValue(
                        dw.createTextField(result.getString("sr.inn"),
                                myUi.getMessage(Messages.INN), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        1, 20, false), true));
            } else {
                item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                        dw.createTextField(result.getString("sr.address"),
                                myUi.getMessage(Messages.Address), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        null, 300, true), false));
                item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                        dw.createTextField(result.getString("sr.phone"),
                                myUi.getMessage(Messages.Phone), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        null, 100, true), false));
                item.getItemProperty(myUi.getMessage(Messages.GivenBy)).setValue(
                        dw.createTextField(result.getString("sr.given_by"),
                                myUi.getMessage(Messages.GivenBy), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        null, 100, true), false));
                item.getItemProperty(myUi.getMessage(Messages.IssueDate)).setValue(
                        dw.createDateField(result.getDate("sr.issue_date"),
                                myUi.getMessage(Messages.IssueDate), id, true, false,
                                Settings.datePattern, Resolution.DAY));
                item.getItemProperty(myUi.getMessage(Messages.Passport)).setValue(
                        dw.createTextField(result.getString("sr.passport"),
                                myUi.getMessage(Messages.Passport), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        null, 50, true), false));
                item.getItemProperty(myUi.getMessage(Messages.INN)).setValue(
                        dw.createTextField(result.getString("sr.inn"),
                                myUi.getMessage(Messages.INN), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        null, 20, true), false));
            }
            item.getItemProperty(myUi.getMessage(Messages.RelativeType)).setValue(
                    dw.createCombobox(result.getInt("sr.relatives_id"),
                            myUi.getMessage(Messages.RelativeType),
                            id, Settings.dbRelatives, false));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
        }
        return container;
    }

    public List<StudentRelative> allRelativesByStudentId(int stud_id) throws SQLException {

        String sql = "select distinct(r.id) as id, r.name, sr.fullname, sr.phone, sr.is_main from student_relatives as sr left join relatives as r on sr.relatives_id = r.id where sr.student_id = ? order by sr.is_main desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        List<StudentRelative> list = new ArrayList<>();
        while (result.next()) {
            StudentRelative studentRelative = new StudentRelative();
            studentRelative.setFullName(result.getString("sr.fullname"));
            studentRelative.setPhone(result.getString("sr.phone"));
            studentRelative.setRelativeTitle(result.getString("r.name"));
            list.add(studentRelative);
        }
        return list;
    }

    public int exec_insert(StudentRelative sr) throws SQLException {
        String sql = "INSERT INTO student_relatives (student_id, fullname, "
                + "given_by, phone, address, passport, inn, is_main, "
                + "relatives_id, issue_date, attachment_id) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sr.getStudent_id());
        stat.setString(2, sr.getFullName());
        stat.setString(3, sr.getGivenBy());
        stat.setString(4, sr.getPhone());
        stat.setString(5, sr.getAddress());
        stat.setString(6, sr.getPassport());
        stat.setString(7, sr.getInn());
        stat.setInt(8, sr.getIs_main());
        stat.setInt(9, sr.getRelative_id());
        if (sr.getIssueDate() != null) {
            stat.setDate(10, new java.sql.Date(sr.getIssueDate().getTime()));
        } else {
            stat.setNull(10, Types.DATE);
        }
        if (sr.getAttachment_id() != 0) {
            stat.setInt(11, sr.getAttachment_id());
        } else {
            stat.setNull(11, Types.INTEGER);
        }
        return stat.executeUpdate();
    }

    public int exec_update(StudentRelative sr) throws SQLException {
        String sql = "update student_relatives set student_id = ?, "
                + "fullname = ?, given_by = ?, phone = ?, address = ?, "
                + "passport = ?, is_main = ?, relatives_id = ?, issue_date = ?, inn = ?, "
                + "attachment_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sr.getStudent_id());
        stat.setString(2, sr.getFullName());
        stat.setString(3, sr.getGivenBy());
        stat.setString(4, sr.getPhone());
        stat.setString(5, sr.getAddress());
        stat.setString(6, sr.getPassport());
        stat.setInt(7, sr.getIs_main());
        stat.setInt(8, sr.getRelative_id());
        if (sr.getIssueDate() != null) {
            stat.setDate(9, new java.sql.Date(sr.getIssueDate().getTime()));
        } else {
            stat.setNull(9, Types.DATE);
        }
        stat.setString(10, sr.getInn());
        if (sr.getAttachment_id() != 0) {
            stat.setInt(11, sr.getAttachment_id());
        } else {
            stat.setNull(11, Types.INTEGER);
        }
        stat.setString(12, sr.getId());
        return stat.executeUpdate();
    }

    public String exec_get_who_paid(int stud_id) throws SQLException {
        String sql = "SELECT fullname FROM student_relatives where student_id = ? and is_main = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        String p = "";
        if (result.next()) {
            p = result.getString("fullname");
        }
        return p;
    }
}
