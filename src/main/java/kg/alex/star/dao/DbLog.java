/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbLog extends BaseDb {

    public DbLog() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int scl_id, int days_interval, String logType) throws SQLException {

        String sql = "select dl.id, concat(e.name, ' ', e.surname) as fullname, dl.table_name, dl.column_name, dl.action, "
                + "dl.old_field, dl.new_field, "
                + "dl.datetime from data_log as dl "
                + "left join employee as e on e.id = dl.employee_id "
                + "left join hr_employee_order as eo on eo.employee_id=e.id and "
                + "(eo.to_date IS NULL or date(eo.to_date) >=  date(dl.datetime)) and eo.hr_orders_id = 1 "
                + "left join hr_orders as ord on ord.id=eo.hr_orders_id "
                + "where eo.school_id = ? ";
        if (logType.equals(myUi.getMessage(Messages.SystemLogs))) {
            sql += "and dl.table_name != 'transactions' ";
        } else {
            sql += "and dl.table_name = 'transactions' ";
        }
        if (days_interval != 0) {
            sql += "AND dl.datetime >= (CURDATE() - INTERVAL ? DAY) and ord.working_status_id IS NOT NULL ";
        }
        sql += "group by dl.id order by dl.datetime desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        if (days_interval != 0) {
            stat.setInt(2, days_interval);
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Employee), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.TableName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ColumnName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Action), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.OldField), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.NewField), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("dl.id"));
            item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(
                    Settings.df.format(result.getDate("dl.datetime")));
            item.getItemProperty(myUi.getMessage(Messages.Employee)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUi.getMessage(Messages.TableName)).setValue(
                    result.getString("dl.table_name"));
            item.getItemProperty(myUi.getMessage(Messages.ColumnName)).setValue(
                    result.getString("dl.column_name"));
            item.getItemProperty(myUi.getMessage(Messages.Action)).setValue(
                    result.getString("dl.action"));
            item.getItemProperty(myUi.getMessage(Messages.OldField)).setValue(
                    result.getString("dl.old_field"));
            item.getItemProperty(myUi.getMessage(Messages.NewField)).setValue(
                    result.getString("dl.new_field"));
        }
        return container;
    }
}
