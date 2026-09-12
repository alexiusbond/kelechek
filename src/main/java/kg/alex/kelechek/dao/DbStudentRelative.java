package kg.alex.kelechek.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.CheckBox;

import kg.alex.kelechek.MyVaadinUI;


import kg.alex.kelechek.domain.StudentRelative;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.ui.StudentDefinitionView;
import kg.alex.kelechek.utils.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
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

        String sql = "SELECT sr.id, sr.student_id, sr.fullname, "
                + "sr.phone, sr.address, sr.is_main, sr.relatives_id, "
                + "sr.work_place, sr.passport, sr.passport_issue_place, sr.passport_issue_date "
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
            }
            item.getItemProperty(myUi.getMessage(Messages.RelativeType)).setValue(
                    dw.createCombobox(result.getInt("sr.relatives_id"),
                            myUi.getMessage(Messages.RelativeType),
                            id, Settings.dbRelatives, false));
            StudentRelative relative = new StudentRelative();
            readAdditionalFields(result, relative);
            dw.fillRelativeAdditionalFields(item, id, relative);
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
        }
        return container;
    }

    public List<StudentRelative> allRelativesByStudentId(int stud_id) throws SQLException {

        String sql = "select distinct(r.id) as id, r.name, sr.fullname, sr.phone, sr.is_main, " +
                "sr.work_place, sr.passport, sr.passport_issue_place, sr.passport_issue_date " +
                "from student_relatives as sr " +
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
            readAdditionalFields(result, studentRelative);
            list.add(studentRelative);
        }
        return list;
    }

    public int exec_insert(StudentRelative sr) throws SQLException {
        String sql = "INSERT INTO student_relatives (student_id, fullname, "
                + "phone, address, is_main, relatives_id, work_place, passport, "
                + "passport_issue_place, passport_issue_date) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement stat = dbCon.prepareStatement(sql)) {
            stat.setInt(1, sr.getStudent_id());
            stat.setString(2, sr.getFullName());
            stat.setString(3, sr.getPhone());
            stat.setString(4, sr.getAddress());
            stat.setInt(5, sr.getIs_main());
            stat.setInt(6, sr.getRelative_id());
            bindAdditionalFields(stat, sr);
            return stat.executeUpdate();
        }
    }

    public int exec_update(StudentRelative sr) throws SQLException {
        String sql = "UPDATE student_relatives SET student_id = ?, "
                + "fullname = ?, phone = ?, address = ?, is_main = ?, relatives_id = ?, "
                + "work_place = ?, passport = ?, passport_issue_place = ?, passport_issue_date = ? "
                + "WHERE id = ?";
        try (PreparedStatement stat = dbCon.prepareStatement(sql)) {
            stat.setInt(1, sr.getStudent_id());
            stat.setString(2, sr.getFullName());
            stat.setString(3, sr.getPhone());
            stat.setString(4, sr.getAddress());
            stat.setInt(5, sr.getIs_main());
            stat.setInt(6, sr.getRelative_id());
            bindAdditionalFields(stat, sr);
            stat.setString(11, sr.getId());
            return stat.executeUpdate();
        }
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

        String sql = "SELECT sr.id, sr.student_id, sr.fullname, sr.phone, sr.address, "
                + "sr.is_main, sr.relatives_id, sr.work_place, sr.passport, "
                + "sr.passport_issue_place, sr.passport_issue_date FROM student_relatives sr "
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
                    relative.setPhone(result.getString("phone"));
                    relative.setAddress(result.getString("address"));
                    relative.setIs_main(result.getInt("is_main"));
                    relative.setRelative_id(result.getInt("relatives_id"));
                    readAdditionalFields(result, relative);

                    relatives.add(relative);
                }
            }
        }
        return relatives;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int stud_id) throws SQLException {

        String sql = "SELECT sr.fullname, sr.phone, "
                + "sr.address, sr.relatives_id, sr.is_main, sr.work_place, sr.passport, "
                + "sr.passport_issue_place, sr.passport_issue_date "
                + "FROM student_relatives as sr where sr.student_id = ? "
                + "group by sr.relatives_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.WorkPlace), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Passport), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.PassportGiven), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.PassportDate), Date.class, null);
        container.addContainerProperty(Settings.is_main, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("sr.relatives_id"));
            item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                    result.getString("sr.fullname"));
            item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                    result.getString("sr.address"));
            item.getItemProperty(myUi.getMessage(Messages.WorkPlace)).setValue(
                    result.getString("work_place"));
            item.getItemProperty(myUi.getMessage(Messages.Passport)).setValue(
                    result.getString("passport"));
            item.getItemProperty(myUi.getMessage(Messages.PassportGiven)).setValue(
                    result.getString("passport_issue_place"));
            item.getItemProperty(myUi.getMessage(Messages.PassportDate)).setValue(
                    result.getDate("passport_issue_date"));
            item.getItemProperty(Settings.is_main).setValue(
                    result.getInt("sr.is_main"));
        }
        return container;
    }

    private void readAdditionalFields(ResultSet result, StudentRelative relative) throws SQLException {
        relative.setWork_place(result.getString("work_place"));
        relative.setPassport(result.getString("passport"));
        relative.setPassport_issue_place(result.getString("passport_issue_place"));
        relative.setPassport_issue_date(result.getDate("passport_issue_date"));
    }

    /**
     * Parameters 7-10 are shared by INSERT and UPDATE.
     */
    private void bindAdditionalFields(PreparedStatement stat, StudentRelative relative) throws SQLException {
        setNullableString(stat, 7, relative.getWork_place());
        setNullableString(stat, 8, relative.getPassport());
        setNullableString(stat, 9, relative.getPassport_issue_place());
        Date issueDate = relative.getPassport_issue_date();
        if (issueDate == null) {
            stat.setNull(10, Types.DATE);
        } else {
            stat.setDate(10, new java.sql.Date(issueDate.getTime()));
        }
    }

    private void setNullableString(PreparedStatement stat, int index, String value) throws SQLException {
        // Clearing an optional field must clear the corresponding database value.
        if (value == null || value.trim().isEmpty()) {
            stat.setNull(index, Types.VARCHAR);
        } else {
            stat.setString(index, value);
        }
    }
}