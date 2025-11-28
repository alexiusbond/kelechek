/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.domain.StockInvoice;
import kg.alex.aim.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbStockInvoice extends BaseDb {

    public DbStockInvoice() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, int service_type_id) throws SQLException {


        String sql = "SELECT t.id, LPAD(t.invoice_number, 7, 0) as inv_num, t.creation_date, sum(mov.amount * mov.price) as amount, "
                + "stock.name, stock.id, from_e.id, to_e.id, t.note, t.acc_category_id, "
                + "CONCAT(from_e.surname, ' ', from_e.name, ' ', IFNULL(from_e.middle_name, '')) AS from_employee, "
                + "CONCAT(to_e.surname, ' ', to_e.name, ' ', IFNULL(to_e.middle_name, '')) AS to_employee "
                + "FROM dp_invoice AS t "
                + "LEFT JOIN dp_stock AS stock ON stock.id = IF(service_type_id = 1, t.to_stock_id, t.from_stock_id) "
                + "LEFT JOIN employee AS from_e ON from_e.id = t.from_employee_id "
                + "LEFT JOIN employee AS to_e ON to_e.id = t.to_employee_id "
                + "LEFT JOIN dp_stock_movements AS mov ON mov.invoice_id = t.id "
                + "WHERE t.school_id = ? and t.service_type_id = ? group by t.id ORDER BY t.invoice_number DESC";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        stat.setInt(2, service_type_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.InvoiceNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Stock), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(Messages.FromEmployee), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ToEmployee), String.class, null);
        container.addContainerProperty(Settings.stock_id, Integer.class, 0);
        container.addContainerProperty(Settings.from_employee_id, Integer.class, 0);
        container.addContainerProperty(Settings.to_employee_id, Integer.class, 0);
        container.addContainerProperty(Settings.acc_category_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Note), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.InvoiceNumber)).setValue(result.getString("inv_num"));
            item.getItemProperty(myUi.getMessage(Messages.Stock)).setValue(result.getString("stock.name"));
            item.getItemProperty(myUi.getMessage(Messages.FromEmployee)).setValue(result.getString("from_employee"));
            item.getItemProperty(myUi.getMessage(Messages.ToEmployee)).setValue(result.getString("to_employee"));
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(result.getDouble("amount"));
            item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.dtmf.format(result.getTimestamp("t.creation_date")));
            item.getItemProperty(Settings.stock_id).setValue(result.getInt("stock.id"));
            item.getItemProperty(Settings.from_employee_id).setValue(result.getInt("from_e.id"));
            item.getItemProperty(Settings.to_employee_id).setValue(result.getInt("to_e.id"));
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(result.getString("t.note"));
        }
        return container;
    }

    public String execSQL_invoice_number(int id) throws SQLException {
        String sql = "SELECT LPAD(invoice_number, 7, 0) as inv_num FROM dp_invoice WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getString("inv_num");
        }
        return null;
    }

    public int execSQL_max_invoice_number(int school_id, int service_type_id) throws SQLException {
        String sql = "SELECT max(invoice_number) as inv_num FROM dp_invoice WHERE school_id = ? and service_type_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, service_type_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("inv_num");
        }
        return 0;
    }

    public int exec_insert(StockInvoice inv) throws SQLException {
        String sql = "INSERT IGNORE INTO dp_invoice (invoice_number,creation_date,"
                + "note,to_stock_id,from_stock_id,from_employee_id,to_employee_id,service_type_id,school_id,employee_id,"
                + "modification_date,acc_category_id) VALUES(?,?,?,?,?,?,?,?,?,?,NOW(),?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, inv.getInvoice_number());
        stat.setTimestamp(2, new java.sql.Timestamp(inv.getCreation_date().getTime()));
        if (inv.getNote() != null) {
            stat.setString(3, inv.getNote());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (inv.getService_type_id() == 1) {
            stat.setInt(4, inv.getStock_id());
            stat.setNull(5, Types.INTEGER);
        } else {
            stat.setNull(4, Types.INTEGER);
            stat.setInt(5, inv.getStock_id());
        }
        stat.setInt(6, inv.getFrom_employee_id());
        stat.setInt(7, inv.getTo_employee_id());
        stat.setInt(8, inv.getService_type_id());
        stat.setInt(9, inv.getSchool_id());
        stat.setInt(10, inv.getEmployee_id());
        stat.setInt(11, inv.getAcc_category_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(StockInvoice inv) throws SQLException {
        String sql = "UPDATE dp_invoice SET creation_date = ?,"
                + "note = ?,to_stock_id = ?,from_stock_id = ?,from_employee_id = ?,to_employee_id = ?,service_type_id = ?,employee_id = ?, "
                + "acc_category_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setTimestamp(1, new java.sql.Timestamp(inv.getCreation_date().getTime()));
        if (inv.getNote() != null) {
            stat.setString(2, inv.getNote());
        } else {
            stat.setNull(2, Types.VARCHAR);
        }
        if (inv.getService_type_id() == 1) {
            stat.setInt(3, inv.getStock_id());
            stat.setNull(4, Types.INTEGER);
        } else {
            stat.setNull(3, Types.INTEGER);
            stat.setInt(4, inv.getStock_id());
        }
        stat.setInt(5, inv.getFrom_employee_id());
        stat.setInt(6, inv.getTo_employee_id());
        stat.setInt(7, inv.getService_type_id());
        stat.setInt(8, inv.getEmployee_id());
        stat.setInt(9, inv.getAcc_category_id());
        stat.setInt(10, inv.getId());
        return stat.executeUpdate();
    }

}
