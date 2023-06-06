/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.SchoolAccounting;
import kg.alex.indigo.domain.Transfer;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.ui.TransfersView;
import kg.alex.indigo.utils.FormattedTreeTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DbTransfers extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbTransfers.class);

    public DbTransfers() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, int school_id,
                                    int acc_invoice_type_id, TransfersView v) throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();

        String sql = "SELECT t.id, t.amount, t.acc_category_id, t.acc_currency_id, t.currency_rate, t.note "
                + "FROM acc_transfers as t where t.invoice_id = ? order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = v.prepareTransfersContainer();
        double totalUSD = 0.0, totalKGS = 0.0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    v.createButton(myUi.getMessage(IndigoMessages.DeleteButton), id, Settings.dbTransfers));
            ComboBox cb = v.createCombobox(0, myUi.getMessage(IndigoMessages.Category), null,
                    true, acc_invoice_type_id == 1);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                cb.setContainerDataSource(dbCon.exec_for_select(myUi, v.getAcc_category_type_id(), school_id,
                        acc_invoice_type_id != 1));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setItemCaptionPropertyId(myUi.getMessage(IndigoMessages.FullName));
            cb.setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Category)).setValue(cb);
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            cb = v.createCombobox(result.getInt("t.acc_currency_id"), myUi.getMessage(IndigoMessages.Currency), Settings.dbAcc_currency, true, false);
            cb.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(IndigoMessages.Currency)).setValue(cb);
            item.getItemProperty(Settings.acc_currency_id).setValue(result.getInt("t.acc_currency_id"));
            TextField tf = v.createTextFieldWithProperty(
                    result.getDouble("t.amount"), myUi.getMessage(IndigoMessages.Amount),
                    new DoubleRangeValidator(myUi.getMessage(IndigoMessages.NotificationWrongValue), null, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), true);
            tf.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(IndigoMessages.Amount)).setValue(tf);
            tf = v.createTextFieldWithProperty(
                    result.getDouble("t.currency_rate"), myUi.getMessage(IndigoMessages.Rate),
                    new DoubleRangeValidator(myUi.getMessage(IndigoMessages.NotificationWrongValue), 0.01, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(4),
                    currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate));
            tf.addValueChangeListener(v);
            item.getItemProperty(myUi.getMessage(IndigoMessages.Rate)).setValue(tf);
            item.getItemProperty(myUi.getMessage(IndigoMessages.Note)).setValue(v.createTextField(
                    result.getString("t.note"), id, new StringLengthValidator(myUi.getMessage(IndigoMessages.NotificationWrongValue),
                            null, 250, acc_invoice_type_id == 1), acc_invoice_type_id != 1));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(IndigoMessages.Update));
            if (result.getInt("t.acc_currency_id") == 1) {
                totalUSD += result.getDouble("t.amount") / result.getDouble("t.currency_rate");
                totalKGS += result.getDouble("t.amount");
            } else {
                totalUSD += result.getDouble("t.amount");
                totalKGS += result.getDouble("t.amount") * result.getDouble("t.currency_rate");
            }
        }
        v.setTransfersFooter(totalUSD, totalKGS);
        return container;
    }

    public Map<Integer, Transfer> execSQL(int invoice_id)
            throws SQLException {
        String sql = "SELECT t.id, t.amount, t.acc_balance_settings_id, t.note "
                + "FROM acc_transfers as t where t.invoice_id = ? order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        Map<Integer, Transfer> map = new HashMap<>();
        while (result.next()) {
            Transfer transfer = new Transfer();
            transfer.setId(result.getInt("t.id"));
            transfer.setAmount(result.getDouble("t.amount"));
            transfer.setNote(result.getString("t.note"));
            map.put(result.getInt("t.acc_balance_settings_id"), transfer);
        }
        return map;
    }

    public void exec_report_by_date(MyVaadinUI myUI, String type_ids, int school_id, Date from_date, Date till_date,
                                    FormattedTreeTable t, String selectedCategoryIds) throws SQLException {
        String sql = "SELECT ac.id, concat(ifnull(concat(ac.parent_code,'.',ac.code), ac.code), ' - ', ac.name) as category, " +
                "acu.name, t.acc_currency_id, t.currency_rate, t.amount, t.note, concat(e.name, ' ', e.surname) as fullname " +
                "FROM acc_transfers as t " +
                "left join acc_invoice as inv on inv.id = t.invoice_id " +
                "left join acc_category as ac on ac.id = t.acc_category_id " +
                "left join acc_currency as acu on acu.id = t.acc_currency_id " +
                "left join employee as e on e.id = inv.employee_id " +
                "where inv.school_id = ? and date(inv.creation_date) >= ? and date(inv.creation_date) <= ? " +
                "and ac.acc_type_id in(" + type_ids + ") and t.acc_category_id in ( " +
                selectedCategoryIds + ") and inv.is_confirmed = 1 " +
                "order by inv.creation_date asc, ac.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setDate(2, new java.sql.Date(from_date.getTime()));
        stat.setDate(3, new java.sql.Date(till_date.getTime()));
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Currency), String.class, "USD");
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Rate), Double.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.FullName), String.class, null);
        t.setContainerDataSource(container);

        int id = 1;
        while (result.next()) {
            Item item;
            if (container.getItem(result.getString("ac.id")) == null) {
                item = container.addItem(result.getString("ac.id"));
                item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(result.getString("category"));
                item.getItemProperty(myUI.getMessage(IndigoMessages.Currency)).setValue("USD");
                if (result.getInt("t.acc_currency_id") == 1) {
                    if (result.getDouble("t.currency_rate") != 0) {
                        container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(IndigoMessages.Amount)).setValue(
                                result.getDouble("t.amount") / result.getDouble("t.currency_rate"));
                    }
                } else {
                    container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(IndigoMessages.Amount)).setValue(
                            result.getDouble("t.amount"));
                }
                container.setChildrenAllowed(result.getString("ac.id"), true);
                t.setCollapsed(result.getString("ac.id"), false);
            } else {
                if (result.getInt("t.acc_currency_id") == 1) {
                    if (result.getDouble("t.currency_rate") != 0) {
                        container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(IndigoMessages.Amount)).setValue(
                                (Double) container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(IndigoMessages.Amount)).getValue()
                                        + (result.getDouble("t.amount") / result.getDouble("t.currency_rate")));
                    }
                } else {
                    container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(IndigoMessages.Amount)).setValue(
                            (Double) container.getItem(result.getString("ac.id")).getItemProperty(myUI.getMessage(IndigoMessages.Amount)).getValue()
                                    + result.getDouble("t.amount"));
                }
            }
            item = container.addItem(Integer.toString(id));
            container.setChildrenAllowed(Integer.toString(id), false);
            container.setParent(Integer.toString(id), result.getString("ac.id"));
            item.getItemProperty(myUI.getMessage(IndigoMessages.Currency)).setValue(result.getString("acu.name"));
            item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).setValue(result.getDouble("t.currency_rate"));
            item.getItemProperty(myUI.getMessage(IndigoMessages.Amount)).setValue(result.getDouble("t.amount"));
            item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(result.getString("t.note"));
            item.getItemProperty(myUI.getMessage(IndigoMessages.FullName)).setValue(result.getString("fullname"));
            id++;
        }
    }

    public SchoolAccounting exec_get_ttls(int scl_id, Date from, Date till, String cat_ids) throws SQLException {
        String sql = "SELECT "
                + "SUM(IF(cat.acc_type_id = 3 AND DATE(inv.creation_date) >= ? AND DATE(inv.creation_date) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), 0.0)) AS assersTtl, "
                + "SUM(IF(cat.acc_type_id = 4 AND DATE(inv.creation_date) >= ? AND DATE(inv.creation_date) <= ?, "
                + "if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), 0.0)) AS debtsTtl, "
                + "SUM(IF(DATE(inv.creation_date) < ?, IF(cat.acc_type_id = 3, if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)), "
                + "-(if(tr.acc_currency_id = 2, tr.amount, ROUND(tr.amount/tr.currency_rate,2)))), 0.0)) AS prev_balance "
                + "FROM acc_transfers AS tr LEFT JOIN acc_category AS cat ON cat.id = tr.acc_category_id "
                + "LEFT JOIN acc_invoice AS inv ON inv.id = tr.invoice_id "
                + "WHERE inv.school_id = ? and inv.is_confirmed = 1 ";
        if (cat_ids != null) {
            sql += "and tr.acc_category_id in (" + cat_ids + ")";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setDate(3, new java.sql.Date(from.getTime()));
        stat.setDate(4, new java.sql.Date(till.getTime()));
        stat.setDate(5, new java.sql.Date(from.getTime()));
        stat.setInt(6, scl_id);
        ResultSet result = stat.executeQuery();
        SchoolAccounting acc = new SchoolAccounting();
        while (result.next()) {
            acc.setTotal_income(result.getDouble("assersTtl"));
            acc.setTotal_outcome(result.getDouble("debtsTtl"));
            acc.setPrevious_balance(result.getDouble("prev_balance"));
        }
        return acc;
    }

    public int exec_insert(Transfer acr) throws SQLException {
        String sql = "INSERT INTO acc_transfers (invoice_id,acc_category_id,"
                + "acc_currency_id,amount,currency_rate,note,acc_balance_settings_id) VALUES(?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acr.getInvoice_id());
        stat.setInt(2, acr.getAcc_category_id());
        stat.setInt(3, acr.getCurrency_id());
        stat.setDouble(4, acr.getAmount());
        stat.setDouble(5, acr.getRate());
        if (acr.getNote() != null && !acr.getNote().equals("")) {
            stat.setString(6, acr.getNote());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        if (acr.getAcc_balance_settings_id() != 0) {
            stat.setInt(7, acr.getAcc_balance_settings_id());
        } else {
            stat.setNull(7, Types.INTEGER);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Transfer acr) throws SQLException {
        String sql = "update acc_transfers set "
                + "acc_category_id = ?, acc_currency_id = ?, amount = ?, currency_rate = ?, note = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acr.getAcc_category_id());
        stat.setInt(2, acr.getCurrency_id());
        stat.setDouble(3, acr.getAmount());
        stat.setDouble(4, acr.getRate());
        if (acr.getNote() != null && !acr.getNote().equals("")) {
            stat.setString(5, acr.getNote());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setInt(6, acr.getId());
        return stat.executeUpdate();
    }
}
