/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.utils.Settings;
import kg.alex.star.domain.Definition;
import kg.alex.star.domain.InventoryOrganization;
import kg.alex.star.i18n.Messages;
import kg.alex.star.ui.InventoryOrganizationView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInventoryOrganization extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbInventoryOrganization.class);

    public DbInventoryOrganization() throws Exception {
        super();
    }

    public IndexedContainer execSQL_for_select(MyVaadinUI myUi, int room_id, int invoice_id)
            throws SQLException {

        String sql = "SELECT io.id, concat('[', io.code, '] ', category.name, ' - ', brand.name, " +
                "' - ',  title.name) as title, r.remain, io.code, " +
                "(select t.quantity from dm_inventory_liquidation as t where t.invoice_id = ? and t.inventory_id = io.id) as quantity " +
                "FROM dm_invoice AS t " +
                "LEFT JOIN dm_inventory_organization AS io ON io.invoice_id = t.id " +
                "LEFT JOIN view_inventory_remains AS r ON r.inventory_id = io.id " +
                "LEFT JOIN dm_title AS title ON io.title_id = title.id " +
                "LEFT JOIN dm_brand AS brand ON io.brand_id = brand.id " +
                "LEFT JOIN dm_inventory_category AS category ON io.inventory_category_id = category.id " +
                "WHERE t.room_id = ? and t.activity_status_id = 2 " +
                "ORDER BY io.inventory_category_id, brand.id, title.name";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        stat.setInt(2, room_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Remain), Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Quantity), Integer.class, 0);
        container.addContainerProperty(Settings.id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getString("io.code").toLowerCase());
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("title"));
            item.getItemProperty(myUi.getMessage(Messages.Remain)).setValue(
                    result.getInt("r.remain"));
            item.getItemProperty(myUi.getMessage(Messages.Quantity)).setValue(
                    result.getInt("quantity"));
            item.getItemProperty(Settings.id).setValue(result.getInt("io.id"));
        }
        return container;
    }


    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, InventoryOrganizationView v)
            throws SQLException {

        String sql = "SELECT t.id, t.quantity, t.price, t.inventory_category_id, t.title_id, "
                + "t.brand_id, r.remain, t.code, t.purchase_date, t.life_time "
                + "FROM dm_inventory_organization as t "
                + "left join view_inventory_remains as r on r.inventory_id = t.id "
                + "where t.invoice_id = ? order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareInventoriesContainer();
        double totalAmount = 0.0;
        int totalQuantity = 0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    v.createButton(myUi.getMessage(Messages.DeleteButton), id, Settings.dbInventoryOrganization, true));
            item.getItemProperty(myUi.getMessage(Messages.Category)).setValue(
                    v.createCombobox(result.getInt("t.inventory_category_id"),
                            myUi.getMessage(Messages.Category),
                            Settings.dbInventoryCategoryTable, true, true));
            final ComboBox cb = v.createCombobox(result.getInt("t.brand_id"), myUi.getMessage(Messages.Brand),
                    Settings.dbInventoryBrandTable, true, true);
            cb.setNewItemsAllowed(true);
            cb.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbInventoryBrandTable, false);
                    dbd.close();
                    if (id1 != 0) {
                        for (Object next : container.getItemIds()) {
                            Item item1 = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                    myUi.getMessage(Messages.Brand)).getValue()).getContainerDataSource()).addItem(id1);
                            item1.getItemProperty(myUi.getMessage(Messages.Title)).setValue(newItemCaption);
                            cb.setValue(id1);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            });
            item.getItemProperty(myUi.getMessage(Messages.Brand)).setValue(cb);
            final ComboBox cb2 = v.createCombobox(result.getInt("t.title_id"), myUi.getMessage(Messages.Title),
                    Settings.dbInventoryTitleTable, true, true);
            cb2.setNewItemsAllowed(true);
            cb2.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id12 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbInventoryTitleTable, false);
                    dbd.close();
                    if (id12 != 0) {
                        for (Object next : container.getItemIds()) {
                            Item item12 = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                    myUi.getMessage(Messages.Title)).getValue()).getContainerDataSource()).addItem(id12);
                            item12.getItemProperty(myUi.getMessage(Messages.Title)).setValue(newItemCaption);
                            cb2.setValue(id12);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            });
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(cb2);
            item.getItemProperty(myUi.getMessage(Messages.PurchaseYear)).setValue(
                    v.createDateFiled(result.getDate("t.purchase_date"),
                            myUi.getMessage(Messages.PurchaseYear), null, Resolution.YEAR, Settings.yearPattern));
            item.getItemProperty(myUi.getMessage(Messages.LifeTime)).setValue(
                    v.createTextFieldWithProperty(
                            result.getInt("t.life_time"), myUi.getMessage(Messages.LifeTime),
                            new IntegerRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), 1, null),
                            new ObjectProperty<>(0), Settings.getStringToIntegerConverter(), true));
            int minVal = 1;
            if (result.getInt("r.remain") < result.getInt("t.quantity")) {
                minVal = result.getInt("t.quantity") - result.getInt("r.remain");
            }
            TextField tf = v.createTextFieldWithProperty(
                    result.getInt("t.quantity"), myUi.getMessage(Messages.Quantity),
                    new IntegerRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), minVal, null),
                    new ObjectProperty<>(0), Settings.getStringToIntegerConverter(), true);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(Messages.Quantity));
            item.getItemProperty(myUi.getMessage(Messages.Quantity)).setValue(tf);
            item.getItemProperty(Settings.quantity_id).setValue(result.getInt("t.quantity"));
            tf = v.createTextFieldWithProperty(
                    result.getDouble("t.price"), myUi.getMessage(Messages.Price),
                    new DoubleRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), 0.01, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), true);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(Messages.Price));
            item.getItemProperty(myUi.getMessage(Messages.Price)).setValue(tf);
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(
                    result.getInt("t.quantity") * result.getDouble("t.price"));
            item.getItemProperty(myUi.getMessage(Messages.Remain)).setValue(result.getInt("r.remain"));
            item.getItemProperty(myUi.getMessage(Messages.Code)).setValue(result.getString("t.code"));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
            totalAmount += result.getInt("t.quantity") * result.getDouble("t.price");
            totalQuantity += result.getInt("t.quantity");
        }
        v.setInventoriesFooter(totalAmount, totalQuantity);
        return container;
    }

    public int exec_delete(int invoice_id) throws SQLException {
        String sql = "DELETE FROM dm_inventory_organization WHERE invoice_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        return stat.executeUpdate();
    }

    public int exec_insert(InventoryOrganization inventoryOrganization) throws SQLException {
        String sql = "INSERT INTO dm_inventory_organization (invoice_id,inventory_category_id, "
                + "title_id,brand_id,quantity,price,code,purchase_date,life_time," +
                "creation_date) VALUES(?,?,?,?,?,?,?,?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryOrganization.getInvoice_id());
        stat.setInt(2, inventoryOrganization.getInventory_category_id());
        stat.setInt(3, inventoryOrganization.getTitle_id());
        stat.setInt(4, inventoryOrganization.getBrand_id());
        stat.setInt(5, inventoryOrganization.getQuantity());
        stat.setDouble(6, inventoryOrganization.getPrice());
        stat.setString(7, inventoryOrganization.getCode());
        stat.setDate(8, new Date(inventoryOrganization.getPurchase_date().getTime()));
        stat.setInt(9, inventoryOrganization.getLifeTime());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(InventoryOrganization inventoryOrganization) throws SQLException {
        String sql = "update dm_inventory_organization set "
                + "inventory_category_id = ?, title_id = ?, brand_id = ?, "
                + "quantity = ?, price = ?, code = ?, purchase_date = ?, life_time = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryOrganization.getInventory_category_id());
        stat.setInt(2, inventoryOrganization.getTitle_id());
        stat.setInt(3, inventoryOrganization.getBrand_id());
        stat.setInt(4, inventoryOrganization.getQuantity());
        stat.setDouble(5, inventoryOrganization.getPrice());
        stat.setString(6, inventoryOrganization.getCode());
        stat.setDate(7, new Date(inventoryOrganization.getPurchase_date().getTime()));
        stat.setInt(8, inventoryOrganization.getLifeTime());
        stat.setInt(9, inventoryOrganization.getId());
        return stat.executeUpdate();
    }

    public int exec_delete(InventoryOrganization inventoryOrganization) throws SQLException {
        String sql = "DELETE FROM dm_inventory_organization WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inventoryOrganization.getId());
        return stat.executeUpdate();
    }
}
