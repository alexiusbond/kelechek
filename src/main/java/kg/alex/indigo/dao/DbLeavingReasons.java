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
import kg.alex.indigo.domain.LeavingReason;
import kg.alex.indigo.i18n.IndigoMessages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbLeavingReasons extends BaseDb {

    public DbLeavingReasons() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi)
            throws SQLException {
        

        String sql = "SELECT lr.id, lr.name, lr.activity_status_id, ac.name FROM order_reason as lr "
                + "left join activity_status as ac on ac.id = lr.activity_status_id "
                + "order by lr.id asc, lr.activity_status_id desc";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Status), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("lr.id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(
                    result.getString("lr.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("lr.activity_status_id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.id).setValue(
                    result.getInt("lr.id"));
        }
        return container;
    }

    public int exec_insert(LeavingReason c) throws SQLException {
        String sql = "INSERT INTO order_reason (name,activity_status_id) VALUES(?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, c.getName());
        stat.setInt(2, c.getStatus_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(LeavingReason c) throws SQLException {
        String sql = "UPDATE order_reason SET name = ?, activity_status_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, c.getName());
        stat.setInt(2, c.getStatus_id());
        stat.setInt(3, c.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, boolean all) throws SQLException {
        String sql = "select t.id, t.name from order_reason as t ";
        if (!all) {
            sql += "where t.activity_status_id = 2 ";
        }
        sql += "order by t.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }
}
