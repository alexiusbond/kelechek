/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.domain.StockMovement;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.ui.StockIncomeView;
import kg.alex.abl.ui.StockOutcomeView;
import kg.alex.abl.utils.FormattedTreeTable;
import kg.alex.abl.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class DbStockMovements extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbStockMovements.class);

    public DbStockMovements() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, StockIncomeView v)
            throws SQLException {

        String sql = "SELECT t.id, t.amount, t.remain, t.price, t.acc_category_id, t.dp_measurement_id, t.note, t.currency_rate, rmn.remain "
                + "FROM dp_stock_movements as t "
                + "left join dp_invoice as inv on inv.id = t.invoice_id "
                + "left join view_stock_remains as rmn on rmn.acc_category_id = t.acc_category_id and rmn.stock_id = inv.to_stock_id and rmn.dp_measurement_id = t.dp_measurement_id "
                + "where t.invoice_id = ? order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareMovementsContainer();
        v.prepareOriginalContainer();
        double total = 0;
        while (result.next()) {
            boolean is_modifyable = result.getDouble("t.amount") == result.getDouble("t.remain");
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    v.createButton(myUi.getMessage(Messages.DeleteButton), id, Settings.dbStockMovement, is_modifyable));
            ComboBox cb = v.createCombobox(0, myUi.getMessage(Messages.Product), null, true, is_modifyable);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi, v.getProductCategorySelect().getValue() + ""));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(Messages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(Messages.Product)).setValue(cb);
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(v.createTextField(
                    result.getString("t.note"), myUi.getMessage(Messages.Note),
                    new StringLengthValidator(myUi.getMessage(Messages.NotificationWrongValue), null, 250, false), true));
            cb = v.createCombobox(result.getInt("t.dp_measurement_id"), myUi.getMessage(Messages.Measurement),
                    Settings.dbMeasurement, true, is_modifyable);
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(Messages.Measurement)).setValue(cb);
            item.getItemProperty(Settings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            double minVal = 0.01;
            if (result.getDouble("t.remain") < result.getDouble("t.amount")) {
                minVal = result.getDouble("t.amount") - result.getDouble("t.remain");
            }
            TextField tf = v.createTextFieldWithProperty(
                    result.getDouble("t.amount"), myUi.getMessage(Messages.Quantity),
                    new DoubleRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), minVal, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), (result.getDouble("t.remain") > 0));
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(Messages.Quantity));
            item.getItemProperty(myUi.getMessage(Messages.Quantity)).setValue(tf);
            item.getItemProperty(Settings.quantity_id).setValue(result.getDouble("t.amount"));
            tf = v.createTextFieldWithProperty(
                    result.getDouble("t.price"), myUi.getMessage(Messages.Price),
                    new DoubleRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), 0.01, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), is_modifyable);
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(Messages.Price));
            item.getItemProperty(myUi.getMessage(Messages.Price)).setValue(tf);
            item.getItemProperty(myUi.getMessage(Messages.Rate)).setValue(result.getDouble("t.currency_rate"));
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(
                    result.getDouble("t.amount") * result.getDouble("t.price"));
            item.getItemProperty(myUi.getMessage(Messages.Remain)).setValue(result.getDouble("rmn.remain"));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
            item.getItemProperty(Settings.is_modifiable).setValue(is_modifyable);
            total += result.getDouble("t.amount") * result.getDouble("t.price");

            item = v.getOriginalCont().addItem(id);
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(Settings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            item.getItemProperty(Settings.quantity_id).setValue(result.getDouble("t.amount"));
        }
        v.setMovementsFooter(total);
        return container;
    }

    public ArrayList<StockMovement> execSQL(Integer invoice_id, Integer acc_category_id, Integer measurement_id) throws SQLException {
        ArrayList<StockMovement> list = new ArrayList<>();
        String sql = "SELECT t.id, t.amount, t.dp_stock_movements_id FROM dp_stock_movements as t "
                + "left join dp_invoice as inv on inv.id = t.invoice_id where t.invoice_id = ? ";
        if (acc_category_id != null) {
            sql += "and t.acc_category_id = " + acc_category_id + " ";
        }
        if (measurement_id != null) {
            sql += "and t.dp_measurement_id = " + measurement_id + " ";
        }
        sql += "order by t.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            StockMovement sm = new StockMovement();
            sm.setId(result.getInt("t.id"));
            sm.setStock_movement_id(result.getInt("t.dp_stock_movements_id"));
            sm.setQuantity(result.getDouble("t.amount"));
            list.add(sm);
        }
        return list;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, StockOutcomeView v) throws SQLException {


        String sql = "SELECT t.id, sum(t.amount) as amount, sum(t.amount * t.price) as total, "
                + "t.acc_category_id, t.dp_measurement_id, t.note, "
                + "sum(t.currency_rate*t.amount) as currency_rate, rmn.remain, "
                + "t.dp_stock_movements_id FROM dp_stock_movements as t "
                + "left join dp_invoice as inv on inv.id = t.invoice_id "
                + "left join view_stock_remains as rmn "
                + "on rmn.acc_category_id = t.acc_category_id and rmn.stock_id = inv.from_stock_id and rmn.dp_measurement_id = t.dp_measurement_id "
                + "where t.invoice_id = ? group by t.order_number order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareMovementsContainer();
        v.prepareOriginalContainer();
        double total = 0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    v.createButton(myUi.getMessage(Messages.DeleteButton), id, Settings.dbStockMovement));
            ComboBox cb = v.createCombobox(0, myUi.getMessage(Messages.Product), null, true);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi, v.getProductCategorySelect().getValue() + ""));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(Messages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(Messages.Product)).setValue(cb);
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(v.createTextField(
                    result.getString("t.note"), myUi.getMessage(Messages.Note),
                    new StringLengthValidator(myUi.getMessage(Messages.NotificationWrongValue), null, 250, false), true));
            cb = v.createCombobox(result.getInt("t.dp_measurement_id"), myUi.getMessage(Messages.Measurement), Settings.dbMeasurement, true);
            cb.setId(id);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(Messages.Measurement)).setValue(cb);
            item.getItemProperty(Settings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            TextField tf = v.createTextFieldWithProperty(
                    result.getDouble("amount"), myUi.getMessage(Messages.Quantity),
                    new DoubleRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), 0.01, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2));
            tf.addValueChangeListener(v);
            tf.setId(id);
            tf.setData(myUi.getMessage(Messages.Quantity));
            item.getItemProperty(myUi.getMessage(Messages.Quantity)).setValue(tf);
            item.getItemProperty(Settings.quantity_id).setValue(result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(Messages.Price)).setValue(result.getDouble("total") / result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(Messages.Rate)).setValue(result.getDouble("currency_rate") / result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(result.getDouble("total"));
            item.getItemProperty(myUi.getMessage(Messages.Remain)).setValue(result.getDouble("rmn.remain"));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
            total += result.getDouble("total");

            item = v.getOriginalCont().addItem(id);
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(Settings.measurement_id).setValue(result.getInt("t.dp_measurement_id"));
            item.getItemProperty(Settings.quantity_id).setValue(result.getDouble("amount"));
        }
        v.setMovementsFooter(total);
        return container;
    }

    public int exec_delete(int invoice_id) throws SQLException {
        String sql = "DELETE FROM dp_stock_movements WHERE invoice_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        return stat.executeUpdate();
    }

    public int exec_insert(StockMovement smv) throws SQLException {
        String sql = "INSERT INTO dp_stock_movements (invoice_id,acc_category_id,"
                + "dp_measurement_id,amount,price,note,currency_rate,remain,dp_stock_movements_id,order_number) VALUES(?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, smv.getInvoice_id());
        stat.setInt(2, smv.getAcc_category_id());
        stat.setInt(3, smv.getMeasurement_id());
        stat.setDouble(4, smv.getQuantity());
        stat.setDouble(5, smv.getPrice());
        if (smv.getNote() != null) {
            stat.setString(6, smv.getNote());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        stat.setDouble(7, smv.getRate());
        stat.setDouble(8, smv.getQuantity());
        if (smv.getStock_movement_id() != 0) {
            stat.setInt(9, smv.getStock_movement_id());
            exec_update_remain(smv.getStock_movement_id(), -smv.getQuantity());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        stat.setInt(10, smv.getOrder_number());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(StockMovement smv) throws SQLException {
        String sql = "update dp_stock_movements set "
                + "acc_category_id = ?,dp_measurement_id = ?, remain = remain - amount + ?, amount = ?,price = ?, note = ?, dp_stock_movements_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, smv.getAcc_category_id());
        stat.setInt(2, smv.getMeasurement_id());
        stat.setDouble(3, smv.getQuantity());
        stat.setDouble(4, smv.getQuantity());
        stat.setDouble(5, smv.getPrice());
        stat.setString(6, smv.getNote());
        if (smv.getStock_movement_id() != 0) {
            stat.setInt(7, smv.getStock_movement_id());
        } else {
            stat.setNull(7, Types.INTEGER);
        }
        stat.setInt(8, smv.getId());
        return stat.executeUpdate();
    }

    public int exec_update_remain(int id, double amount) throws SQLException {
        String sql = "update dp_stock_movements set remain = remain + ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDouble(1, amount);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public double execSQL_remain(int acc_category_id, int measurement_id, int stock_id) throws SQLException {
        String sql = "SELECT remain FROM view_stock_remains where acc_category_id = ? and dp_measurement_id = ? and stock_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setDouble(2, measurement_id);
        stat.setInt(3, stock_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getDouble("remain");
        }
        return 0.0;
    }

    public double execSQL_remain(int acc_category_id, int measurement_id, int stock_id, Date date) throws SQLException {

        String sql = "SELECT SUM(mv.remain) AS remain FROM dp_stock_movements mv "
                + "LEFT JOIN dp_invoice inv ON inv.id = mv.invoice_id "
                + "WHERE inv.service_type_id = 1 AND DATE(inv.creation_date) <= ? AND mv.acc_category_id = ? "
                + "AND inv.to_stock_id = ?";
        if (measurement_id != 0) {
            sql += " AND mv.dp_measurement_id = " + measurement_id;
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(date.getTime()));
        stat.setInt(2, acc_category_id);
        stat.setInt(3, stock_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getDouble("remain");
        }
        return 0.0;
    }

    public double execSQL_category_balance(int acc_category_id, String stock_ids, Date date) throws SQLException {
        String sql = "SELECT SUM(IF(inv.service_type_id = 1 AND DATE(inv.creation_date) < ?, sm.amount, 0.0)) " +
                "- SUM(IF(inv.service_type_id = 2 AND DATE(inv.creation_date) < ?, sm.amount, 0.0)) AS balance " +
                "FROM dp_stock_movements AS sm " +
                "LEFT JOIN acc_category AS cat ON sm.acc_category_id = cat.id " +
                "LEFT JOIN dp_invoice AS inv ON sm.invoice_id = inv.id " +
                "WHERE sm.acc_category_id = ? AND (inv.to_stock_id IN (" + stock_ids + ") OR inv.from_stock_id IN (" + stock_ids + ")) " +
                "GROUP BY cat.id ORDER BY CONCAT(IFNULL(CONCAT(cat.parent_code, '.', cat.code), cat.code), ' - ', cat.name) ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(date.getTime()));
        stat.setDate(2, new java.sql.Date(date.getTime()));
        stat.setInt(3, acc_category_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getDouble("balance");
        }
        return 0.0;
    }

    public boolean execSQL_allowNewInsert(int acc_category_id, int measurement_id, int stock_id, Date date) throws SQLException {

        String sql = "SELECT if(mv.remain<mv.amount, 0, 1) AS isOk FROM dp_stock_movements mv "
                + "LEFT JOIN dp_invoice inv ON inv.id = mv.invoice_id "
                + "WHERE inv.service_type_id = 1 AND DATE(inv.creation_date) > ? AND mv.acc_category_id = ? "
                + "AND mv.dp_measurement_id = ? AND inv.to_stock_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(date.getTime()));
        stat.setInt(2, acc_category_id);
        stat.setDouble(3, measurement_id);
        stat.setInt(4, stock_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            if (result.getInt("isOk") == 0) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<StockMovement> execSQL_remains(int acc_category_id, int measurement_id, int stock_id) throws SQLException {
        String sql = "SELECT sm.id, remain, price, currency_rate, sm.acc_category_id, dp_measurement_id "
                + "FROM dp_stock_movements AS sm LEFT JOIN dp_invoice AS inv ON inv.id = sm.invoice_id "
                + "WHERE sm.remain > 0 AND inv.service_type_id = 1 AND sm.acc_category_id = ? AND sm.dp_measurement_id = ? "
                + "AND inv.to_stock_id = ? ORDER BY inv.creation_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setInt(2, measurement_id);
        stat.setInt(3, stock_id);
        ResultSet result = stat.executeQuery();
        ArrayList<StockMovement> list = new ArrayList<>();
        while (result.next()) {
            StockMovement stMov = new StockMovement();
            stMov.setPrice(result.getDouble("price"));
            stMov.setRate(result.getDouble("currency_rate"));
            stMov.setRemain(result.getDouble("remain"));
            stMov.setId(result.getInt("sm.id"));
            list.add(stMov);
        }
        return list;
    }

    public void exec_stock_operations(MyVaadinUI myUI, FilterTreeTable categoriesTable, Date from, Date till, int service_type_id,
                                      String stock_ids, FormattedTreeTable t) throws SQLException {


        Set<Integer> selectedCategoryIds = Settings.getChild_ids(
                (HierarchicalContainer) categoriesTable.getContainerDataSource(),
                (Set<?>) categoriesTable.getValue());
        String sql = "SELECT cat.id, CONCAT(IFNULL(CONCAT(cat.parent_code, '.', cat.code), cat.code), ' - ', cat.name) AS name, " +
                "group_concat(DISTINCT m.name ORDER BY m.id ASC separator ', ') as measurement, " +
                "SUM(sm.amount) AS amount, SUM(sm.amount * sm.price) AS total, SUM(sm.currency_rate * sm.amount) AS currency_rate " +
                "FROM dp_stock_movements AS sm " +
                "LEFT JOIN acc_category AS cat ON sm.acc_category_id = cat.id " +
                "LEFT JOIN dp_measurement AS m ON sm.dp_measurement_id = m.id " +
                "LEFT JOIN dp_invoice AS inv ON sm.invoice_id = inv.id " +
                "WHERE sm.acc_category_id IN (" + Settings.convertCollectionToStr(selectedCategoryIds) + ") AND " +
                "(DATE(inv.creation_date) BETWEEN ? AND ?) AND ";
        if (service_type_id == 1) {
            sql += "inv.to_stock_id ";
        } else {
            sql += "inv.from_stock_id ";
        }
        sql += "IN (" + stock_ids + ") AND inv.service_type_id = ? " +
                "GROUP BY cat.id ORDER BY CONCAT(IFNULL(CONCAT(cat.parent_code, '.', cat.code), cat.code), ' - ', cat.name)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setInt(3, service_type_id);
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Quantity), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Measurement), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.AveragePrice), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.AverageRate), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
        t.setContainerDataSource(container);
        double total_amount = 0.0;
        for (Object catNext : categoriesTable.getContainerDataSource().getItemIds()) {
            if (selectedCategoryIds.contains(catNext)) {
                Item item = container.addItem(catNext);
                item.getItemProperty(myUI.getMessage(Messages.Title))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Title)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = categoriesTable.getContainerDataSource().getParent(catNext);
                if (parent != null) {
                    if (container.getItem(parent) == null) {
                        item = container.addItem(parent);
                        item.getItemProperty(myUI.getMessage(Messages.Title))
                                .setValue(categoriesTable.getContainerProperty(parent, myUI.getMessage(Messages.Title)).getValue().toString());
                    } else {
                        item.getItemProperty(myUI.getMessage(Messages.AverageRate)).setValue(0.0);
                        item.getItemProperty(myUI.getMessage(Messages.Quantity)).setValue(0.0);
                        item.getItemProperty(myUI.getMessage(Messages.AveragePrice)).setValue(0.0);
                    }
                    container.setParent(catNext, parent);
                }
                if (categoriesTable.getContainerDataSource().getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
                if (categoriesTable.getContainerDataSource().getChildren(parent) != null) {
                    container.setChildrenAllowed(parent, true);
                    t.setCollapsed(parent, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            item.getItemProperty(myUI.getMessage(Messages.Quantity)).setValue(result.getDouble("amount"));
            item.getItemProperty(myUI.getMessage(Messages.Measurement)).setValue(result.getString("measurement"));
            total_amount += result.getDouble("total");
            if (result.getDouble("amount") != 0.0) {
                item.getItemProperty(myUI.getMessage(Messages.AveragePrice)).setValue(
                        result.getDouble("total") / result.getDouble("amount"));
                item.getItemProperty(myUI.getMessage(Messages.AverageRate)).setValue(
                        result.getDouble("currency_rate") / result.getDouble("amount"));
            }
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(result.getDouble("total"));

            Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
            while (parent_id != null) {
                item = container.getItem(parent_id);
                item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(Messages.Amount)).getValue() + result.getDouble("total"));
                parent_id = (Integer) container.getParent(parent_id);
            }
        }
        t.setColumnFooter(myUI.getMessage(Messages.Amount), Settings.dFormat2.format(total_amount));
    }

    public void exec_stock_balance(MyVaadinUI myUI, FilterTreeTable categoriesTable, Date from, Date till,
                                   String stock_ids, FormattedTreeTable t) throws SQLException {


        Set<Integer> selectedCategoryIds = Settings.getChild_ids(
                (HierarchicalContainer) categoriesTable.getContainerDataSource(),
                (Set<?>) categoriesTable.getValue());
        String sql = "SELECT cat.id, CONCAT(IFNULL(CONCAT(cat.parent_code, '.', cat.code), cat.code), ' - ', cat.name) AS name, " +
                "SUM(IF(inv.service_type_id = 1 AND DATE(inv.creation_date) between ? AND ?, sm.amount, 0.0)) AS in_amount, " +
                "SUM(IF(inv.service_type_id = 2 AND DATE(inv.creation_date) between ? AND ?, sm.amount, 0.0)) AS out_amount, " +
                "SUM(IF(inv.service_type_id = 1 AND DATE(inv.creation_date) between ? AND ?, sm.amount * sm.price, 0.0)) AS in_total, " +
                "SUM(IF(inv.service_type_id = 2 AND DATE(inv.creation_date) between ? AND ?, sm.amount * sm.price, 0.0)) AS out_total, " +
                "SUM(IF(inv.service_type_id = 1 AND DATE(inv.creation_date) < ?, sm.amount, 0.0)) " +
                "- SUM(IF(inv.service_type_id = 2 AND DATE(inv.creation_date) < ?, sm.amount, 0.0)) AS balance " +
                "FROM dp_stock_movements AS sm " +
                "LEFT JOIN acc_category AS cat ON sm.acc_category_id = cat.id " +
                "LEFT JOIN dp_invoice AS inv ON sm.invoice_id = inv.id " +
                "WHERE sm.acc_category_id IN (" + Settings.convertCollectionToStr(selectedCategoryIds) + ") AND " +
                "(inv.to_stock_id IN (" + stock_ids + ") OR inv.from_stock_id IN (" + stock_ids + ")) " +
                "GROUP BY cat.id ORDER BY cat.parent_code, CAST(cat.code AS UNSIGNED)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setDate(3, new java.sql.Date(from.getTime()));
        stat.setDate(4, new java.sql.Date(till.getTime()));
        stat.setDate(5, new java.sql.Date(from.getTime()));
        stat.setDate(6, new java.sql.Date(till.getTime()));
        stat.setDate(7, new java.sql.Date(from.getTime()));
        stat.setDate(8, new java.sql.Date(till.getTime()));
        stat.setDate(9, new java.sql.Date(from.getTime()));
        stat.setDate(10, new java.sql.Date(from.getTime()));
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.StockIncome) + " - " + myUI.getMessage(Messages.Quantity), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.StockIncome) + " - " + myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.StockOutcome) + " - " + myUI.getMessage(Messages.Quantity), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.StockOutcome) + " - " + myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.PreviousBalance) + " " +
                myUI.getMessage(Messages.ToThe).toLowerCase() + " " + Settings.df.format(till), Double.class, null);
        t.setContainerDataSource(container);
        double total_in_amount = 0.0, total_out_amount = 0.0;
        for (Object catNext : categoriesTable.getContainerDataSource().getItemIds()) {
            if (selectedCategoryIds.contains(catNext)) {
                Item item = container.addItem(catNext);
                item.getItemProperty(myUI.getMessage(Messages.Title))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Title)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = categoriesTable.getContainerDataSource().getParent(catNext);
                if (parent != null) {
                    if (container.getItem(parent) == null) {
                        item = container.addItem(parent);
                        item.getItemProperty(myUI.getMessage(Messages.Title))
                                .setValue(categoriesTable.getContainerProperty(parent, myUI.getMessage(Messages.Title)).getValue().toString());
                    } else {
                        item.getItemProperty(myUI.getMessage(Messages.StockIncome) + " - " + myUI.getMessage(Messages.Quantity)).setValue(0.0);
                        item.getItemProperty(myUI.getMessage(Messages.StockOutcome) + " - " + myUI.getMessage(Messages.Quantity)).setValue(0.0);
                        item.getItemProperty(myUI.getMessage(Messages.PreviousBalance) + " " +
                                myUI.getMessage(Messages.ToThe).toLowerCase() + " " + Settings.df.format(till)).setValue(0.0);
                    }
                    container.setParent(catNext, parent);
                }
                if (categoriesTable.getContainerDataSource().getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
                if (categoriesTable.getContainerDataSource().getChildren(parent) != null) {
                    container.setChildrenAllowed(parent, true);
                    t.setCollapsed(parent, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            item.getItemProperty(myUI.getMessage(Messages.StockIncome)
                    + " - " + myUI.getMessage(Messages.Quantity)).setValue(result.getDouble("in_amount"));
            item.getItemProperty(myUI.getMessage(Messages.StockOutcome)
                    + " - " + myUI.getMessage(Messages.Quantity)).setValue(result.getDouble("out_amount"));
            item.getItemProperty(myUI.getMessage(Messages.StockIncome)
                    + " - " + myUI.getMessage(Messages.Amount)).setValue(result.getDouble("in_total"));
            item.getItemProperty(myUI.getMessage(Messages.StockOutcome)
                    + " - " + myUI.getMessage(Messages.Amount)).setValue(result.getDouble("out_total"));
            item.getItemProperty(myUI.getMessage(Messages.PreviousBalance) + " " +
                    myUI.getMessage(Messages.ToThe).toLowerCase() + " " + Settings.df.format(till)).setValue(result.getDouble("balance")
                    + result.getDouble("in_amount") - result.getDouble("out_amount"));

            total_in_amount += result.getDouble("in_total");
            total_out_amount += result.getDouble("out_total");

            Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
            while (parent_id != null) {
                item = container.getItem(parent_id);
                item.getItemProperty(myUI.getMessage(Messages.StockIncome) + " - " + myUI.getMessage(Messages.Amount)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(Messages.StockIncome)
                                + " - " + myUI.getMessage(Messages.Amount)).getValue() + result.getDouble("in_total"));
                item.getItemProperty(myUI.getMessage(Messages.StockOutcome) + " - " + myUI.getMessage(Messages.Amount)).setValue(
                        (Double) item.getItemProperty(myUI.getMessage(Messages.StockOutcome)
                                + " - " + myUI.getMessage(Messages.Amount)).getValue() + result.getDouble("out_total"));
                parent_id = (Integer) container.getParent(parent_id);
            }
        }
        t.setColumnFooter(myUI.getMessage(Messages.StockIncome) + " - " + myUI.getMessage(Messages.Amount), Settings.dFormat2.format(total_in_amount));
        t.setColumnFooter(myUI.getMessage(Messages.StockOutcome) + " - " + myUI.getMessage(Messages.Amount), Settings.dFormat2.format(total_out_amount));
    }

    public void exec_product_movements(MyVaadinUI myUI, int acc_category_id, Date from, Date till,
                                       Table t, int stock_id) throws SQLException {


        String sql = "SELECT inv.creation_date, LPAD(inv.invoice_number, 7, 0) as invoice_number, "
                + "sm.note, sum(sm.amount) as amount, sum(sm.amount * sm.price) as total, "
                + "sum(sm.currency_rate * sm.amount) as currency_rate, inv.service_type_id, "
                + "m.name, tp.name FROM dp_stock_movements AS sm "
                + "LEFT JOIN dp_invoice AS inv ON sm.invoice_id = inv.id "
                + "LEFT JOIN dp_measurement AS m ON sm.dp_measurement_id = m.id "
                + "LEFT JOIN dp_service_type AS tp ON inv.service_type_id = tp.id "
                + "WHERE sm.acc_category_id = ? AND (date(inv.creation_date) BETWEEN ? AND ?) "
                + "AND (inv.to_stock_id = ? OR inv.from_stock_id = ?) "
                + "group by inv.id, order_number ORDER BY inv.creation_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setDate(2, new java.sql.Date(from.getTime()));
        stat.setDate(3, new java.sql.Date(till.getTime()));
        stat.setInt(4, stock_id);
        stat.setInt(5, stock_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.InvoiceNumber), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.MovementType), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Measurement), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Price), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.StockIncome), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.StockOutcome), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PreviousBalance), Double.class, 0.0);
        int i = 0;
        double balance = execSQL_category_balance(acc_category_id, stock_id + "", from),
                totalOut = 0.0, totalIn = 0.0, netAmount = 0.0;
        while (result.next()) {
            Item item = container.addItem(++i);
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format(result.getDate("inv.creation_date")));
            item.getItemProperty(myUI.getMessage(Messages.InvoiceNumber)).setValue(result.getString("invoice_number"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("sm.note"));
            item.getItemProperty(myUI.getMessage(Messages.MovementType)).setValue(result.getString("tp.name"));
            item.getItemProperty(myUI.getMessage(Messages.Measurement)).setValue(result.getString("m.name"));
            if (result.getDouble("amount") != 0.0) {
                item.getItemProperty(myUI.getMessage(Messages.Price)).setValue(
                        result.getDouble("total") / result.getDouble("amount"));
                item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(
                        result.getDouble("currency_rate") / result.getDouble("amount"));
            }
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(result.getDouble("total"));

            if (result.getInt("inv.service_type_id") == 2) {
                item.getItemProperty(myUI.getMessage(Messages.StockOutcome)).setValue(result.getDouble("amount"));
                balance -= result.getDouble("amount");
                totalOut += result.getDouble("amount");
                netAmount -= result.getDouble("total");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.StockIncome)).setValue(result.getDouble("amount"));
                balance += result.getDouble("amount");
                totalIn += result.getDouble("amount");
                netAmount += result.getDouble("total");
            }
            item.getItemProperty(myUI.getMessage(Messages.PreviousBalance)).setValue(balance);
        }
        t.setColumnFooter(myUI.getMessage(Messages.StockIncome), Settings.dFormat2.format(totalIn));
        t.setColumnFooter(myUI.getMessage(Messages.StockOutcome), Settings.dFormat2.format(totalOut));
        t.setColumnFooter(myUI.getMessage(Messages.Amount), Settings.dFormat2.format(netAmount));
        t.setColumnFooter(myUI.getMessage(Messages.PreviousBalance), Settings.dFormat2.format(balance));
        t.setContainerDataSource(container);
    }


    public int exec_delete(StockMovement sm) throws SQLException {
        exec_update_remain(sm.getStock_movement_id(), sm.getQuantity());
        String sql = "DELETE FROM dp_stock_movements WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, sm.getId());
        return stat.executeUpdate();
    }
}
