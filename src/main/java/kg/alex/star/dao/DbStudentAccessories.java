/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.domain.StudentAccessories;
import kg.alex.star.i18n.Messages;
import kg.alex.star.ui.StudentDefinitionView;
import kg.alex.star.utils.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author alex
 */
public class DbStudentAccessories extends BaseDb {

    public DbStudentAccessories() throws Exception {
        super();
    }

    public int exec_insert(StudentAccessories a) throws SQLException {
        String sql = "INSERT INTO student_accessories (student_id, year_id, "
                + "accessories_id, employee_id, modification_date) "
                + "VALUES(?,?,?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, a.getStudent_id());
        stat.setInt(2, a.getYear_id());
        stat.setInt(3, a.getAccessories_id());
        stat.setInt(4, a.getEmployee_id());
        stat.executeUpdate();
        return getLastInsertedSt_id();
    }

    public int getLastInsertedSt_id() throws SQLException {

        String sql = "select last_insert_id() as id";
        int id = 0;

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            id = result.getInt("id");
        }
        return id;
    }

    public int exec_delete(int stud_id, int year_id, int cat_id) throws SQLException {
        String sql = "DELETE sa from student_accessories as sa "
                + "left join accessories as a on a.id = sa.accessories_id "
                + "left join accessories_category as ac on a.accessories_category_id = ac.id "
                + "WHERE student_id = ? and year_id = ? and  ac.id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        stat.setInt(3, cat_id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_St_Acs(MyVaadinUI myUi, int stud_id,
                                           StudentDefinitionView dw, int cat_id) throws SQLException {


        String sql = "SELECT sa.id, sa.student_id, sa.year_id, "
                + "group_concat(sa.accessories_id separator ',') as accessories, sa.employee_id, "
                + "sa.modification_date FROM student_accessories as sa "
                + "left join accessories as a on a.id = sa.accessories_id "
                + "where sa.student_id = ? and a.accessories_category_id = ? group by sa.year_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, cat_id);
        ResultSet result = stat.executeQuery();
        int give = 1;
        int receive = 2;
        IndexedContainer container = null;
        if (cat_id == give) {
            container = dw.prepareMaterialsGivContainer();
        } else if (cat_id == receive) {
            container = dw.prepareMaterialsRecContainer();
        }
        while (result.next()) {
            String id = result.getString("sa.year_id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    dw.createButton(myUi.getMessage(Messages.DeleteButton), id,
                            Settings.dbStudentCalls, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUi.getMessage(Messages.Year)).setValue(
                    dw.createCombobox(result.getInt("sa.year_id"),
                            myUi.getMessage(Messages.Year), id, Settings.dbYear, false));
            item.getItemProperty(myUi.getMessage(Messages.Materials)).setValue(
                    dw.createComboboxMultiAcs(result.getString("accessories"), cat_id));
        }
        return container;
    }
}
