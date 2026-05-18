/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.domain.Accessories;
import kg.alex.ispa.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbAccessories extends BaseDb {

    public DbAccessories() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {


        String sql = "SELECT ac.id, ac.name, ac.activity_status_id, a.name, "
                + "ac.accessories_category_id, c.name FROM accessories as ac "
                + "left join activity_status as a on a.id = ac.activity_status_id "
                + "left join accessories_category as c on c.id = ac.accessories_category_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Category), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(Settings.category_id, Integer.class, 0);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("ac.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("a.name"));
            item.getItemProperty(myUi.getMessage(Messages.Category)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("ac.activity_status_id"));
            item.getItemProperty(Settings.category_id).setValue(
                    result.getInt("ac.accessories_category_id"));
            item.getItemProperty(Settings.id).setValue(result.getInt("ac.id"));
        }
        return container;
    }

    public int exec_insert(Accessories acc) throws SQLException {
        String sql = "INSERT IGNORE INTO accessories (name,activity_status_id,accessories_category_id) "
                + "VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, acc.getName());
        stat.setInt(2, acc.getStatus_id());
        stat.setInt(3, acc.getCategory_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Accessories acc) throws SQLException {
        String sql = "UPDATE accessories SET name = ?, activity_status_id = ?, "
                + "accessories_category_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, acc.getName());
        stat.setInt(2, acc.getStatus_id());
        stat.setInt(3, acc.getCategory_id());
        stat.setInt(4, acc.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int cat_id) throws SQLException {
        String sql = "select t.id, t.name from accessories as t "
                + "where activity_status_id = 2 and accessories_category_id = ? "
                + "order by t.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cat_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getString("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }
}
