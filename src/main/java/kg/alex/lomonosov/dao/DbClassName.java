/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.ClassName;
import kg.alex.lomonosov.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbClassName extends BaseDb {

    public DbClassName() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id) throws SQLException {

        String sql = "SELECT cn.id, cn.class_type_id, cn.name, cn.school_id, sc.name_ru, cnum.id, cnum.name, "
                + "class_type.name, cn.activity_status_id, ac.name FROM class_name as cn "
                + "left join class_number as cnum on cnum.id = cn.class_number_id "
                + "left join class_type on class_type.id = cn.class_type_id "
                + "left join school as sc on sc.id = cn.school_id "
                + "left join activity_status as ac on ac.id=cn.activity_status_id "
                + "where sc.id = ? "
                + "order by cn.class_number_id and cn.name";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Number), String.class, null);
        container.addContainerProperty(Settings.number_id, Integer.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Language), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Type), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(Settings.class_type_id, Integer.class, 0);
        container.addContainerProperty(Settings.school_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.School), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("cn.id"));
            item.getItemProperty(myUi.getMessage(Messages.Number)).setValue(
                    result.getString("cnum.name"));
            item.getItemProperty(Settings.number_id).setValue(
                    result.getInt("cnum.id"));
            item.getItemProperty(myUi.getMessage(Messages.Type)).setValue(
                    result.getString("class_type.name"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("cn.name"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("cn.activity_status_id"));
            item.getItemProperty(Settings.class_type_id).setValue(
                    result.getInt("cn.class_type_id"));
            item.getItemProperty(Settings.school_id).setValue(
                    result.getInt("cn.school_id"));
            item.getItemProperty(myUi.getMessage(Messages.School)).setValue(
                    result.getString("sc.name_ru"));
            item.getItemProperty(Settings.id).setValue(result.getInt("cn.id"));
        }
        return container;
    }

    public int exec_insert(ClassName def) throws SQLException {
        String sql = "INSERT IGNORE INTO class_name (name,school_id,"
                + "class_number_id,activity_status_id,class_type_id) "
                + "VALUES(?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, def.getName());
        stat.setInt(2, def.getSchool_id());
        stat.setInt(3, def.getClass_number_id());
        stat.setInt(4, def.getStatus_id());
        stat.setInt(5, def.getClass_type_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(ClassName cl) throws SQLException {
        String sql = "UPDATE class_name SET name = ?, school_id = ?,class_number_id = ?, "
                + "activity_status_id = ?, class_type_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, cl.getName());
        stat.setInt(2, cl.getSchool_id());
        stat.setInt(3, cl.getClass_number_id());
        stat.setInt(4, cl.getStatus_id());
        stat.setInt(5, cl.getClass_type_id());
        stat.setInt(6, cl.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execClass_sel(MyVaadinUI myUi, int scl_id) throws SQLException {
        String sql = "select cn.id, cn.class_type_id, cnu.name, cnu.order_num, el.order_number_max, el.order_number_min, "
                + "concat(cnu.name,' - ',cn.name) as cl_name from class_name as cn "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "left join class_type as el on cn.class_type_id = el.id "
                + "where cn.school_id = ? order by cnu.name, cn.name";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.class_order_number, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.ClassNumber), Integer.class, 0);
        container.addContainerProperty(Settings.max, Integer.class, 0);
        container.addContainerProperty(Settings.min, Integer.class, 0);
        container.addContainerProperty(Settings.class_type_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("cn.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("cl_name"));
            item.getItemProperty(Settings.class_order_number).setValue(
                    result.getInt("cnu.order_num"));
            item.getItemProperty(myUi.getMessage(Messages.ClassNumber)).setValue(
                    result.getInt("cnu.name"));
            item.getItemProperty(Settings.max).setValue(
                    result.getInt("el.order_number_max"));
            item.getItemProperty(Settings.min).setValue(
                    result.getInt("el.order_number_min"));
            item.getItemProperty(Settings.class_type_id).setValue(
                    result.getInt("cn.class_type_id"));
        }
        return container;
    }

    public IndexedContainer execClass_for_import(int scl_id) throws SQLException {


        String sql = "select cn.id, concat(cnu.name,' - ',cn.name) as cl_name "
                + "from class_name as cn "
                + "left join class_number as cnu on cn.class_number_id = cnu.id "
                + "where cn.school_id = ? order by cnu.name, cn.name";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getString("cl_name"));
            item.getItemProperty(Settings.id).setValue(
                    result.getInt("cn.id"));
        }
        return container;
    }
}
