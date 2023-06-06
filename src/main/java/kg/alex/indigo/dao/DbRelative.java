/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.i18n.IndigoMessages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbRelative extends BaseDb {

    public DbRelative() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int stud_id) throws SQLException {

        String sql = "SELECT sr.fullname, sr.work_place, sr.phone, "
                + "sr.address, sr.passport, sr.relatives_id, sr.is_main "
                + "FROM student_relatives as sr where sr.student_id = ? "
                + "and (sr.relatives_id = 1 or sr.relatives_id = 2) "
                + "group by sr.relatives_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.WorkPlace), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Phone), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Address), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Passport), String.class, null);
        container.addContainerProperty(Settings.is_main, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("sr.relatives_id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.FullName)).setValue(
                    result.getString("sr.fullname"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.WorkPlace)).setValue(
                    result.getString("sr.work_place"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Phone)).setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Address)).setValue(
                    result.getString("sr.address"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Passport)).setValue(
                    result.getString("sr.passport"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Phone)).setValue(
                    result.getString("sr.phone"));
            item.getItemProperty(Settings.is_main).setValue(
                    result.getInt("sr.is_main"));
        }
        return container;
    }
}
