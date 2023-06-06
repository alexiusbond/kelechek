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
import kg.alex.indigo.domain.Room;
import kg.alex.indigo.i18n.IndigoMessages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbRoom extends BaseDb {

    public DbRoom() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id) throws SQLException {

        String sql = "SELECT r.id, r.name, r.description, r.block_id, b.name, r.floor_id, " +
                "f.name, r.activity_status_id, ac.name " +
                "FROM dm_room as r " +
                "left join activity_status as ac on ac.id = r.activity_status_id " +
                "left join dm_floor as f on f.id = r.floor_id " +
                "left join dm_block as b on b.id = r.block_id " +
                "where b.school_id = ? " +
                "order by r.block_id, r.floor_id, r.name, b.activity_status_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Block), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Floor), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Description), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Status), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(Settings.block_id, Integer.class, 0);
        container.addContainerProperty(Settings.floor_id, Integer.class, 0);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("r.id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Block)).setValue(
                    result.getString("b.name"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Floor)).setValue(
                    result.getString("f.name"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(
                    result.getString("r.name"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Description)).setValue(
                    result.getString("r.description"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("r.activity_status_id"));
            item.getItemProperty(Settings.floor_id).setValue(
                    result.getInt("r.floor_id"));
            item.getItemProperty(Settings.block_id).setValue(
                    result.getInt("r.block_id"));
            item.getItemProperty(Settings.id).setValue(result.getInt("r.id"));
        }
        return container;
    }

    public int exec_insert(Room room) throws SQLException {
        String sql = "INSERT IGNORE INTO dm_room (name, description, block_id, floor_id, " +
                "activity_status_id) "
                + "VALUES(?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, room.getName());
        stat.setString(2, room.getDescription());
        stat.setInt(3, room.getBlock_id());
        stat.setInt(4, room.getBlock_id());
        stat.setInt(5, room.getActivity_status_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Room room) throws SQLException {
        String sql = "UPDATE dm_room SET name = ?, description = ?, block_id = ?, " +
                "floor_id = ?, activity_status_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, room.getName());
        stat.setString(2, room.getDescription());
        stat.setInt(3, room.getBlock_id());
        stat.setInt(4, room.getFloor_id());
        stat.setInt(5, room.getActivity_status_id());
        stat.setInt(6, room.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int block_id,
                                            int floor_id) throws SQLException {
        String sql = "select t.id, t.name, CONCAT(t.name, ' ', CASE "
                + "WHEN t.description IS NULL OR t.description = '-' THEN ''"
                + "ELSE t.description END) AS room from dm_room as t where t.block_id = ? "
                + "and t.floor_id = ? and t.activity_status_id = 2 "
                + "order by t.block_id, t.floor_id, t.name";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, block_id);
        stat.setInt(2, floor_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Room), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(
                    result.getString("room"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Room)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }
}
