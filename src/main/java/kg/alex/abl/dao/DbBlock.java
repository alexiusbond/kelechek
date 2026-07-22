/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.domain.Block;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.utils.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbBlock extends BaseDb {

    public DbBlock() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id) throws SQLException {


        String sql = "SELECT b.id, b.name, b.school_id, b.activity_status_id, ac.name " +
                "FROM dm_block as b " +
                "left join activity_status as ac on ac.id = b.activity_status_id " +
                "where b.school_id = ? order by b.name, b.activity_status_id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(Settings.school_id, Integer.class, 0);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("b.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("b.name"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("b.activity_status_id"));
            item.getItemProperty(Settings.school_id).setValue(
                    result.getInt("b.school_id"));
            item.getItemProperty(Settings.id).setValue(result.getInt("b.id"));
        }
        return container;
    }

    public int exec_insert(Block block) throws SQLException {
        String sql = "INSERT IGNORE INTO dm_block (name, school_id, activity_status_id) "
                + "VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, block.getName());
        stat.setInt(2, block.getSchool_id());
        stat.setInt(3, block.getStatus_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Block block) throws SQLException {
        String sql = "UPDATE dm_block SET name = ?, school_id = ?, activity_status_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, block.getName());
        stat.setInt(2, block.getSchool_id());
        stat.setInt(3, block.getStatus_id());
        stat.setInt(4, block.getId());
        return stat.executeUpdate();
    }
}
