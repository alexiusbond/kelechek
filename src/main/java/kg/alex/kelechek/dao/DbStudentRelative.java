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
                + "sr.phone, sr.address, sr.is_main, sr.relatives_id "
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
                + "phone, address, is_main, relatives_id) "
                + "VALUES(?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sr.getStudent_id());
        stat.setString(2, sr.getFullName());
        stat.setString(3, sr.getPhone());
        stat.setString(4, sr.getAddress());
        stat.setInt(5, sr.getIs_main());
        stat.setInt(6, sr.getRelative_id());
        return stat.executeUpdate();
    }

    public int exec_update(StudentRelative sr) throws SQLException {
        String sql = "update student_relatives set student_id = ?, "
                + "fullname = ?, phone = ?, address = ?, is_main = ?, relatives_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sr.getStudent_id());
        stat.setString(2, sr.getFullName());
        stat.setString(3, sr.getPhone());
        stat.setString(4, sr.getAddress());
        stat.setInt(5, sr.getIs_main());
        stat.setInt(6, sr.getRelative_id());

        stat.setString(7, sr.getId());
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

        String sql = "SELECT sr.id, sr.student_id, sr.fullname, sr.phone, sr.address, "
                + "sr.is_main, sr.relatives_id FROM student_relatives sr "
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

                    relatives.add(relative);
                }
            }
        }
        return relatives;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int stud_id) throws SQLException {

        String sql = "SELECT sr.fullname, sr.phone, "
                + "sr.address, sr.relatives_id, sr.is_main "
                + "FROM student_relatives as sr where sr.student_id = ? "
                + "group by sr.relatives_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Address), String.class, null);
        container.addContainerProperty(Settings.is_main, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("sr.relatives_id"));
            item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                    result.getString("sr.fullname"));
            item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                    result.getString("sr.address"));
            item.getItemProperty(Settings.is_main).setValue(
                    result.getInt("sr.is_main"));
        }
        return container;
    }
}