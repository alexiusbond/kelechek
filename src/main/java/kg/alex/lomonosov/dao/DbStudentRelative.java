/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.StudentRelative;
import kg.alex.lomonosov.i18n.Messages;
import kg.alex.lomonosov.ui.StudentDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author alex
 */
public class DbStudentRelative extends BaseDb {

    public DbStudentRelative() throws Exception {
        super();
    }

    public IndexedContainer execSQL_St_Rel(MyVaadinUI myUi, int stud_id,
                                           StudentDefinitionView dw) throws SQLException {


        String sql = "SELECT sr.id, sr.student_id, sr.fullname, sr.work_place, "
                + "sr.phone, sr.address, sr.passport, sr.is_main, sr.relatives_id "
                + "FROM student_relatives as sr where sr.student_id = ?";
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
            if (result.getInt("sr.is_main") == 1) {
                item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                        dw.createTextField(result.getString("sr.fullname"),
                                myUi.getMessage(Messages.FullName), id, false, true));
                item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                        dw.createTextField(result.getString("sr.address"),
                                myUi.getMessage(Messages.Address), id, true, true));
                item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                        dw.createTextField(result.getString("sr.phone"),
                                myUi.getMessage(Messages.Phone), id, true, true));
                item.getItemProperty(myUi.getMessage(Messages.WorkPlace)).setValue(
                        dw.createTextField(result.getString("sr.work_place"),
                                myUi.getMessage(Messages.WorkPlace), id, true, true));
                item.getItemProperty(myUi.getMessage(Messages.Passport)).setValue(
                        dw.createTextField(result.getString("sr.passport"),
                                myUi.getMessage(Messages.Passport), id, true, true));
                item.getItemProperty(myUi.getMessage(Messages.Responsible)).setValue(
                        dw.createCheckBox(true, myUi.getMessage(Messages.Responsible), id));
            } else {
                item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                        dw.createTextField(result.getString("sr.fullname"),
                                myUi.getMessage(Messages.FullName), id, false, false));
                item.getItemProperty(myUi.getMessage(Messages.Address)).setValue(
                        dw.createTextField(result.getString("sr.address"),
                                myUi.getMessage(Messages.Address), id, true, false));
                item.getItemProperty(myUi.getMessage(Messages.Phone)).setValue(
                        dw.createTextField(result.getString("sr.phone"),
                                myUi.getMessage(Messages.Phone), id, true, false));
                item.getItemProperty(myUi.getMessage(Messages.WorkPlace)).setValue(
                        dw.createTextField(result.getString("sr.work_place"),
                                myUi.getMessage(Messages.WorkPlace), id, true, false));
                item.getItemProperty(myUi.getMessage(Messages.Passport)).setValue(
                        dw.createTextField(result.getString("sr.passport"),
                                myUi.getMessage(Messages.Passport), id, true, false));
                item.getItemProperty(myUi.getMessage(Messages.Responsible)).setValue(
                        dw.createCheckBox(false, myUi.getMessage(Messages.Responsible), id));
            }
            item.getItemProperty(myUi.getMessage(Messages.RelativeType)).setValue(
                    dw.createCombobox(result.getInt("sr.relatives_id"),
                            myUi.getMessage(Messages.RelativeType),
                            id, Settings.dbRelatives, false, false, false, false));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
        }
        return container;
    }

    public int exec_insert(StudentRelative r) throws SQLException {
        String sql = "INSERT INTO student_relatives (student_id, fullname, "
                + "work_place, phone, address, passport, is_main, relatives_id) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, r.getStudent_id());
        stat.setString(2, r.getFullName());
        stat.setString(3, r.getWork_place());
        stat.setString(4, r.getPhone());
        stat.setString(5, r.getAddress());
        stat.setString(6, r.getPassport());
        stat.setInt(7, r.getIs_main());
        stat.setInt(8, r.getRelative_id());
        return stat.executeUpdate();
    }

    public int exec_delete(String id) throws SQLException {
        String sql = "DELETE FROM student_relatives WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public int exec_update(StudentRelative sr) throws SQLException {
        String sql = "Update student_relatives set student_id = ?, "
                + "fullname = ?, work_place = ?, phone = ?, address = ?, "
                + "passport = ?, is_main = ?, relatives_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sr.getStudent_id());
        stat.setString(2, sr.getFullName());
        stat.setString(3, sr.getWork_place());
        stat.setString(4, sr.getPhone());
        stat.setString(5, sr.getAddress());
        stat.setString(6, sr.getPassport());
        stat.setInt(7, sr.getIs_main());
        stat.setInt(8, sr.getRelative_id());
        stat.setInt(9, sr.getId());
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
