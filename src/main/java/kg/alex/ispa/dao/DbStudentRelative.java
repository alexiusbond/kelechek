package kg.alex.ispa.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.CheckBox;

import kg.alex.ispa.MyVaadinUI;


import kg.alex.ispa.domain.StudentRelative;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.ui.StudentDefinitionView;
import kg.alex.ispa.utils.Settings;

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
                + "sr.phone, sr.address, sr.passport, sr.work_place, sr.is_main, sr.relatives_id "

                + "FROM student_relatives as sr "

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


            CheckBox cb = dw.createCheckBox(result.getBoolean("sr.is_main"),
                    myUi.getMessage(Messages.Responsible), id);
            item.getItemProperty(myUi.getMessage(Messages.Responsible)).setValue(cb);


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
                item.getItemProperty(myUi.getMessage(Messages.WorkPlace)).setValue(
                        dw.createTextField(result.getString("sr.work_place"),
                                myUi.getMessage(Messages.WorkPlace), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        1, 250, false), true));
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
                item.getItemProperty(myUi.getMessage(Messages.WorkPlace)).setValue(
                        dw.createTextField(result.getString("sr.work_place"),
                                myUi.getMessage(Messages.WorkPlace), id, new StringLengthValidator(
                                        myUi.getMessage(Messages.NotificationWrongValue),
                                        null, 250, true), false));
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

        String sql = "select distinct(r.id) as id, r.name, sr.fullname, sr.phone, sr.is_main from student_relatives as sr " +
                "left join relatives as r on sr.relatives_id = r.id " +
                "where sr.student_id = ? order by sr.is_main desc";

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
                + "given_by, phone, address, passport, work_place, is_main, "
                + "relatives_id, issue_date) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sr.getStudent_id());
        stat.setString(2, sr.getFullName());
        stat.setString(3, sr.getGivenBy());
        stat.setString(4, sr.getPhone());
        stat.setString(5, sr.getAddress());
        stat.setString(6, sr.getPassport());
        stat.setString(7, sr.getWorkPlace());
        stat.setInt(8, sr.getIs_main());
        stat.setInt(9, sr.getRelative_id());
        if (sr.getIssueDate() != null) {
            stat.setDate(10, new java.sql.Date(sr.getIssueDate().getTime()));
        } else {
            stat.setNull(10, Types.DATE);


        }
        return stat.executeUpdate();
    }

    public int exec_update(StudentRelative sr) throws SQLException {
        String sql = "update student_relatives set student_id = ?, "
                + "fullname = ?, given_by = ?, phone = ?, address = ?, "
                + "passport = ?, is_main = ?, relatives_id = ?, issue_date = ?, work_place = ? "
                + "WHERE id = ?";
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
        stat.setString(10, sr.getWorkPlace());


        stat.setString(11, sr.getId());
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

    public List<StudentRelative> getStudentRelatives(int studId) throws SQLException {

        String sql = "SELECT sr.id, sr.student_id, sr.fullname, sr.given_by, "
                + "sr.issue_date, sr.phone, sr.address, sr.passport, "
                + "sr.work_place, sr.is_main, sr.relatives_id "
                + "FROM student_relatives sr "
                + "WHERE sr.student_id = ?";

        List<StudentRelative> relatives = new ArrayList<>();

        try (PreparedStatement stat = dbCon.prepareStatement(sql)) {
            stat.setInt(1, studId);

            try (ResultSet result = stat.executeQuery()) {
                while (result.next()) {
                    StudentRelative relative = new StudentRelative();

                    relative.setId(result.getString("id"));
                    relative.setStudent_id(result.getInt("student_id"));
                    relative.setFullName(result.getString("fullname"));
                    relative.setGivenBy(result.getString("given_by"));
                    relative.setIssueDate(result.getDate("issue_date"));
                    relative.setPhone(result.getString("phone"));
                    relative.setAddress(result.getString("address"));
                    relative.setPassport(result.getString("passport"));
                    relative.setWorkPlace(result.getString("work_place"));
                    relative.setIs_main(result.getInt("is_main"));
                    relative.setRelative_id(result.getInt("relatives_id"));

                    relatives.add(relative);
                }
            }
        }
        return relatives;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int stud_id) throws SQLException {

        String sql = "SELECT sr.fullname, sr.given_by, sr.phone, "
                + "sr.address, sr.passport, sr.work_place, sr.relatives_id, sr.is_main "
                + "FROM student_relatives as sr where sr.student_id = ? "
                + "group by sr.relatives_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.GivenBy), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Passport), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.WorkPlace), String.class, null);
        container.addContainerProperty(Settings.is_main, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("sr.relatives_id"));
            item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                    result.getString("sr.fullname"));
            item.getItemProperty(myUi.getMessage(Messages.GivenBy)).setValue(
                    result.getString("sr.given_by"));
            item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                    result.getString("sr.address"));
            item.getItemProperty(myUi.getMessage(Messages.Passport)).setValue(
                    result.getString("sr.passport"));
            item.getItemProperty(myUi.getMessage(Messages.WorkPlace)).setValue(
                    result.getString("sr.work_place"));
            item.getItemProperty(Settings.is_main).setValue(
                    result.getInt("sr.is_main"));
        }
        return container;
    }
}