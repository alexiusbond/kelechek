/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.domain.InventoryInvoice;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.utils.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbInventoryInvoice extends BaseDb {

    public DbInventoryInvoice() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, int activity_status_id) throws SQLException {

        String sql = "SELECT t.id, LPAD(t.invoice_number, 7, 0) AS inv_num, t.creation_date, " +
                "SUM(mov.quantity) AS quantity, block.name, block.id, floor.name, floor.id, " +
                "CONCAT(room.name, ' ', CASE " +
                "WHEN room.description IS NULL OR room.description = '-' THEN '' " +
                "ELSE room.description END) AS room, room.id, e.id, t.note, " +
                "CONCAT(e.surname, ' ', e.name, ' ', IFNULL(e.middle_name, '')) AS employee " +
                "FROM dm_invoice AS t " +
                "LEFT JOIN dm_room AS room ON room.id = t.room_id " +
                "LEFT JOIN dm_block AS block ON block.id = room.block_id " +
                "LEFT JOIN dm_floor AS floor ON floor.id = room.floor_id " +
                "LEFT JOIN employee AS e ON e.id = t.employee_id " +
                "LEFT JOIN dm_inventory_organization AS mov ON mov.invoice_id = t.id " +
                "WHERE t.school_id = ? AND t.activity_status_id = ? " +
                "GROUP BY t.id ORDER BY t.invoice_number desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, activity_status_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.InvoiceNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Block), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Floor), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Room), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Quantity), Integer.class, 0.0);
        container.addContainerProperty(myUi.getMessage(Messages.Employee), String.class, null);
        container.addContainerProperty(Settings.block_id, Integer.class, 0);
        container.addContainerProperty(Settings.floor_id, Integer.class, 0);
        container.addContainerProperty(Settings.room_id, Integer.class, 0);
        container.addContainerProperty(Settings.employee_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Note), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.InvoiceNumber)).setValue(result.getString("inv_num"));
            item.getItemProperty(myUi.getMessage(Messages.Block)).setValue(result.getString("block.name"));
            item.getItemProperty(myUi.getMessage(Messages.Floor)).setValue(result.getString("floor.name"));
            item.getItemProperty(myUi.getMessage(Messages.Room)).setValue(result.getString("room"));
            item.getItemProperty(myUi.getMessage(Messages.Employee)).setValue(result.getString("employee"));
            item.getItemProperty(myUi.getMessage(Messages.Quantity)).setValue(result.getInt("quantity"));
            item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.dtmf.format(
                    result.getTimestamp("t.creation_date")));
            item.getItemProperty(Settings.block_id).setValue(result.getInt("block.id"));
            item.getItemProperty(Settings.floor_id).setValue(result.getInt("floor.id"));
            item.getItemProperty(Settings.room_id).setValue(result.getInt("room.id"));
            item.getItemProperty(Settings.employee_id).setValue(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(result.getString("t.note"));
        }
        return container;
    }

    public String execSQL_invoice_number(int id) throws SQLException {
        String sql = "SELECT LPAD(invoice_number, 7, 0) as inv_num FROM dm_invoice WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getString("inv_num");
        }
        return null;
    }

    public int execSQL_max_invoice_number(int school_id, int activity_status_id) throws SQLException {
        String sql = "SELECT max(invoice_number) as inv_num FROM dm_invoice " +
                "WHERE school_id = ? and activity_status_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, activity_status_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("inv_num");
        }
        return 0;
    }

    public int exec_insert(InventoryInvoice inv) throws SQLException {
        String sql = "INSERT IGNORE INTO dm_invoice (invoice_number,creation_date,"
                + "note,room_id,activity_status_id,school_id,employee_id,"
                + "modification_date) VALUES(?,?,?,?,?,?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inv.getInvoice_number());
        stat.setTimestamp(2, new java.sql.Timestamp(inv.getCreation_date().getTime()));
        if (inv.getNote() != null) {
            stat.setString(3, inv.getNote());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setInt(4, inv.getRoom_id());
        stat.setInt(5, inv.getActivity_status_id());
        stat.setInt(6, inv.getSchool_id());
        stat.setInt(7, inv.getEmployee_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(InventoryInvoice inv) throws SQLException {
        String sql = "UPDATE dm_invoice SET creation_date = ?,"
                + "note = ?, room_id = ?, employee_id = ?, modification_date = NOW() WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setTimestamp(1, new java.sql.Timestamp(inv.getCreation_date().getTime()));
        if (inv.getNote() != null) {
            stat.setString(2, inv.getNote());
        } else {
            stat.setNull(2, Types.VARCHAR);
        }
        stat.setInt(3, inv.getRoom_id());
        stat.setInt(4, inv.getEmployee_id());
        stat.setInt(5, inv.getId());
        return stat.executeUpdate();
    }

    public boolean isUsed(int block_id, int room_id) throws SQLException {

        String sql = "SELECT * FROM dm_invoice as inv " +
                "left join dm_room as r on r.id = inv.room_id " +
                "left join dm_block as b on b.id = r.block_id " +
                "where 1";
        if (block_id != 0) {
            sql += " and b.id = ?";
        }
        if (room_id != 0) {
            sql += " and r.id = ?";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        int counter = 0;
        if (block_id != 0) {
            stat.setInt(++counter, block_id);
        }
        if (room_id != 0) {
            stat.setInt(++counter, room_id);
        }
        ResultSet result = stat.executeQuery();
        return result.next();
    }
}
