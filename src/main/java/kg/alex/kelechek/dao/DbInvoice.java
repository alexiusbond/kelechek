/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.dao;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.domain.Invoice;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.utils.Settings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.sql.*;

public class DbInvoice extends BaseDb {

    public DbInvoice() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, int invoice_type_id, String viewName,
                                    Property.ValueChangeListener listener) throws SQLException {

        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT inv.id, LPAD(inv.invoice_number, 7, 0) as inv_num, inv.creation_date, inv.is_confirmed, "
                + "sum(if(acr.acc_currency_id != 1, acr.amount*acr.currency_rate, acr.amount)) as amount, inv.note, inv.note2 "
                + "FROM acc_invoice AS inv "
                + "LEFT JOIN acc_transfers AS acr ON acr.invoice_id = inv.id "
                + "WHERE inv.school_id = ? and inv.acc_invoice_type_id = ? "
                + "group by inv.id ORDER BY inv.invoice_number DESC";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, invoice_type_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.InvoiceNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Note) + " 2", String.class, null);
        container.addContainerProperty(Settings.button, CheckBox.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("inv.id"));
            item.getItemProperty(myUi.getMessage(Messages.InvoiceNumber)).setValue(result.getString("inv_num"));
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(result.getDouble("amount"));
            if (invoice_type_id != 1) {
                item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.ymdf.format(result.getTimestamp("inv.creation_date")));
            } else {
                item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.dtmf.format(result.getTimestamp("inv.creation_date")));
            }
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(result.getString("inv.note"));
            item.getItemProperty(myUi.getMessage(Messages.Note) + " 2").setValue(result.getString("inv.note2"));
            CheckBox cb = new CheckBox();
            cb.setStyleName(ValoTheme.CHECKBOX_SMALL);
            cb.setData(result.getInt("inv.id"));
            if (result.getInt("inv.is_confirmed") == 1) {
                cb.setValue(true);
            }
            if (!currentUser.isPermitted(viewName + ":" + Settings.prmConfirmationControl)) {
                cb.setEnabled(false);
            }
            cb.addValueChangeListener(listener);
            item.getItemProperty(Settings.button).setValue(cb);
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, Property.ValueChangeListener listener) throws SQLException {

        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT inv.id, LPAD(inv.invoice_number, 7, 0) as inv_num, inv.creation_date, inv.is_confirmed, "
                + "sum(if(cat.acc_type_id = 3, if(acr.acc_currency_id != 1, acr.amount*acr.currency_rate, acr.amount),"
                + "-if(acr.acc_currency_id != 1, acr.amount*acr.currency_rate, acr.amount))) as amount, inv.note, inv.note2 "
                + "FROM acc_invoice AS inv "
                + "LEFT JOIN acc_transfers AS acr ON acr.invoice_id = inv.id "
                + "LEFT JOIN acc_category AS cat ON acr.acc_category_id = cat.id "
                + "WHERE inv.school_id = ? and inv.acc_invoice_type_id = 5 "
                + "group by inv.id ORDER BY inv.invoice_number DESC";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.InvoiceNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Note) + " 2", String.class, null);
        container.addContainerProperty(Settings.button, CheckBox.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("inv.id"));
            item.getItemProperty(myUi.getMessage(Messages.InvoiceNumber)).setValue(result.getString("inv_num"));
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.ymdf.format(result.getTimestamp("inv.creation_date")));
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(result.getString("inv.note"));
            item.getItemProperty(myUi.getMessage(Messages.Note) + " 2").setValue(result.getString("inv.note2"));
            CheckBox cb = new CheckBox();
            cb.setStyleName(ValoTheme.CHECKBOX_SMALL);
            cb.setData(result.getInt("inv.id"));
            if (result.getInt("inv.is_confirmed") == 1) {
                cb.setValue(true);
            }
            if (!currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.prmConfirmationControl)) {
                cb.setEnabled(false);
            }
            cb.addValueChangeListener(listener);
            item.getItemProperty(Settings.button).setValue(cb);
        }
        return container;
    }

    public boolean isExists(int school_id, int invoice_type_id, java.util.Date date, int id) throws SQLException {
        String sql = "SELECT inv.id FROM acc_invoice AS inv "
                + "WHERE inv.school_id = ? and inv.acc_invoice_type_id = ? "
                + "and YEAR(inv.creation_date) = YEAR(?) and MONTH(inv.creation_date) = MONTH(?) and inv.id != ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, invoice_type_id);
        stat.setDate(3, new Date(date.getTime()));
        stat.setDate(4, new Date(date.getTime()));
        stat.setInt(5, id);
        ResultSet result = stat.executeQuery();

        return result.next();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, int invoice_type_id,
                                    Property.ValueChangeListener listener) throws SQLException {

        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT inv.id, LPAD(inv.invoice_number, 7, 0) as inv_num, inv.creation_date, inv.is_confirmed, "
                + "sum(if(c.acc_currency_id != 1, tr.amount*tr.currency_rate, tr.amount)) as amount, inv.note "
                + "FROM acc_invoice AS inv "
                + "LEFT JOIN acc_transactions AS tr ON tr.acc_invoice_id = inv.id "
                + "LEFT JOIN acc_cashbox AS c ON c.id = tr.acc_cashbox_id "
                + "WHERE inv.school_id = ? and inv.acc_invoice_type_id = ? group by inv.id ORDER BY inv.invoice_number DESC";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, invoice_type_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.InvoiceNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(Settings.button, CheckBox.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("inv.id"));
            item.getItemProperty(myUi.getMessage(Messages.InvoiceNumber)).setValue(result.getString("inv_num"));
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.dtmf.format(result.getTimestamp("inv.creation_date")));
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(result.getString("inv.note"));
            CheckBox cb = new CheckBox();
            cb.setStyleName(ValoTheme.CHECKBOX_SMALL);
            cb.setData(result.getInt("inv.id"));
            if (result.getInt("inv.is_confirmed") == 1) {
                cb.setValue(true);
            }
            if (!currentUser.isPermitted(Settings.cnPayoutsView + ":" + Settings.prmConfirmationControl)) {
                cb.setEnabled(false);
            }
            cb.addValueChangeListener(listener);
            item.getItemProperty(Settings.button).setValue(cb);
        }
        return container;
    }

    public String execSQL_invoice_number(int id) throws SQLException {
        String sql = "SELECT LPAD(invoice_number, 7, 0) as inv_num FROM acc_invoice WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getString("inv_num");
        }
        return null;
    }

    public String execSQL_Note2(int school_id, int acc_invoice_type_id, Date date) throws SQLException {
        String sql = "SELECT note2 FROM acc_invoice WHERE school_id = ? and acc_invoice_type_id = ? and creation_date = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, acc_invoice_type_id);
        stat.setDate(3, date);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getString("note2");
        }
        return null;
    }

    public int execSQL_max_invoice_number(int school_id, int acc_invoice_type_id) throws SQLException {
        String sql = "SELECT max(invoice_number) as inv_num FROM acc_invoice WHERE school_id = ? and acc_invoice_type_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, acc_invoice_type_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("inv_num");
        }
        return 0;
    }

    public int exec_insert(Invoice inv) throws SQLException {
        String sql = "INSERT IGNORE INTO acc_invoice (invoice_number,creation_date,note,school_id,employee_id,"
                + "modification_date,acc_invoice_type_id,note2) "
                + "VALUES(?,?,?,?,?,NOW(),?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inv.getInvoice_number());
        stat.setTimestamp(2, new java.sql.Timestamp(inv.getCreation_date().getTime()));
        if (inv.getNote() != null) {
            stat.setString(3, inv.getNote());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setInt(4, inv.getSchool_id());
        stat.setInt(5, inv.getEmployee_id());
        stat.setInt(6, inv.getAcc_invoice_type_id());
        if (inv.getNote2() != null) {
            stat.setString(7, inv.getNote2());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Invoice inv) throws SQLException {
        String sql = "UPDATE acc_invoice SET creation_date = ?,"
                + "note = ?,note2 = ?,employee_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setTimestamp(1, new java.sql.Timestamp(inv.getCreation_date().getTime()));
        if (inv.getNote() != null) {
            stat.setString(2, inv.getNote());
        } else {
            stat.setNull(2, Types.VARCHAR);
        }
        if (inv.getNote2() != null) {
            stat.setString(3, inv.getNote2());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setInt(4, inv.getEmployee_id());
        stat.setInt(5, inv.getId());
        return stat.executeUpdate();
    }

    public int exec_update(int id, int is_confirmed) throws SQLException {
        String sql = "UPDATE acc_invoice SET is_confirmed = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, is_confirmed);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }
}
