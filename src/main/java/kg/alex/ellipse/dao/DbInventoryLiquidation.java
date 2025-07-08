/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.domain.InventoryLiquidation;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.ui.InventoryLiquidationView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInventoryLiquidation extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbInventoryLiquidation.class);

    public DbInventoryLiquidation() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, InventoryLiquidationView v)
            throws SQLException {

        String sql = "SELECT t.id, t.quantity, i.room_id, t.inventory_id, r.remain, io.code " +
                "FROM dm_inventory_liquidation as t " +
                "left join dm_inventory_organization as io on io.id = t.inventory_id " +
                "left join view_inventory_remains as r on io.id = r.inventory_id " +
                "left join dm_invoice as i on i.id = io.invoice_id " +
                "where t.invoice_id = ? order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareInventoriesContainer();
        int totalQuantity = 0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    v.createButton(myUi.getMessage(Messages.DeleteButton), id, Settings.dbInventoryLiquidation, true));
            ComboBox cb = v.createCombobox(0, myUi.getMessage(Messages.InventoryItem),
                    null, true, true, true);
            try {
                DbInventoryOrganization dbCon = new DbInventoryOrganization();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.execSQL_for_select(myUi, result.getInt("i.room_id"), invoice_id));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setValue(result.getString("io.code").toLowerCase());
            cb.addValueChangeListener(v);
            cb.setId(id);
            cb.setData(myUi.getMessage(Messages.InventoryItem));
            item.getItemProperty(myUi.getMessage(Messages.InventoryItem)).setValue(cb);
            TextField tf = v.createTextFieldWithProperty(
                    result.getInt("t.quantity"), myUi.getMessage(Messages.Quantity),
                    new IntegerRangeValidator(myUi.getMessage(Messages.NotificationWrongValue),
                            1, result.getInt("r.remain") + result.getInt("t.quantity")),
                    new ObjectProperty<>(0), Settings.getStringToIntegerConverter(), true);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(Messages.Quantity));
            item.getItemProperty(myUi.getMessage(Messages.Quantity)).setValue(tf);
            item.getItemProperty(myUi.getMessage(Messages.Remain)).setValue(result.getInt("r.remain"));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
            totalQuantity += result.getInt("t.quantity");
        }
        v.setInventoriesFooter(totalQuantity);
        return container;
    }

    public int exec_delete(int invoice_id) throws SQLException {
        String sql = "DELETE FROM dm_inventory_liquidation WHERE invoice_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        return stat.executeUpdate();
    }

    public int exec_insert(InventoryLiquidation inventoryLiquidation) throws SQLException {
        String sql = "INSERT INTO dm_inventory_liquidation (invoice_id,quantity," +
                "inventory_id,creation_date) VALUES(?,?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryLiquidation.getInvoice_id());
        stat.setInt(2, inventoryLiquidation.getQuantity());
        stat.setInt(3, inventoryLiquidation.getInventory_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(InventoryLiquidation inventoryLiquidation) throws SQLException {
        String sql = "update dm_inventory_liquidation set "
                + "quantity = ?, inventory_id = ? WHERE id= ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryLiquidation.getQuantity());
        stat.setInt(2, inventoryLiquidation.getInventory_id());
        stat.setInt(3, inventoryLiquidation.getId());
        return stat.executeUpdate();
    }

    public int exec_delete(InventoryLiquidation inventoryLiquidation) throws SQLException {
        String sql = "DELETE FROM dm_inventory_liquidation WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryLiquidation.getId());
        return stat.executeUpdate();
    }
}
