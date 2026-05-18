/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.domain.AccBalanceSettings;
import kg.alex.ispa.domain.Definition;
import kg.alex.ispa.domain.Month;
import kg.alex.ispa.i18n.Messages;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author alex
 */
public class DbDefinition extends BaseDb {

    public DbDefinition() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, String dbTableName, boolean withActivityStatus, boolean itemIdAsName) throws SQLException {

        String sql = "select t.id, t.name ";
        if (withActivityStatus) {
            sql += ", st.id, st.name ";
        }
        sql += "from ";
        sql += dbTableName + " as t ";
        if (withActivityStatus) {
            sql += " left join activity_status as st on st.id=t.activity_status_id ";
        }
        sql += "order by t.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);
        if (withActivityStatus) {
            container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
            container.addContainerProperty(Settings.activity_status_id, Integer.class, null);
        }

        while (result.next()) {
            Item item;
            if (itemIdAsName) {
                item = container.addItem(result.getString("t.name"));
            } else {
                item = container.addItem(result.getInt("t.id"));
            }
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(Settings.id).setValue(result.getInt("t.id"));
            if (withActivityStatus) {
                item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                        result.getString("st.name"));
                item.getItemProperty(Settings.activity_status_id).setValue(result.getInt("st.id"));
            }
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, String dbTableName, boolean isDesc) throws SQLException {
        String sql = "select t.id, t.name from " + dbTableName + " as t order by t.id ";
        if (isDesc) {
            sql += "desc";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, String dbTableName,
                                            int school_id, boolean isDesc) throws SQLException {
        String sql = "select t.id, t.name from " + dbTableName + " as t where t.school_id = ? "
                + "and t.activity_status_id = 2 order by t.id";
        if (isDesc) {
            sql += " desc";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, String dbTableName,
                                            List<String> containerProperties, int id) throws SQLException {
        StringBuilder sql = new StringBuilder("select t.id, t.name, ");
        for (int i = 0; i < containerProperties.size(); i++) {
            sql.append(containerProperties.get(i));
            if (i != containerProperties.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(" from ").append(dbTableName).append(" as t where 1");
        if (id != 0) {
            sql.append(" AND id = ?");
        }
        PreparedStatement stat = dbCon.prepareStatement(sql.toString());
        if (id != 0) {
            stat.setInt(1, id);
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        for (String property : containerProperties) {
            container.addContainerProperty(property, Integer.class, null);
        }
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(result.getString("t.name"));
            for (String containerProperty : containerProperties) {
                item.getItemProperty(containerProperty).setValue(
                        result.getInt(containerProperty));
            }
        }
        return container;
    }

    public IndexedContainer exec_for_select_general_working_statuses(MyVaadinUI myUi) throws SQLException {
        String sql = "select t.id, t.name from working_status as t where t.is_general = 1 order by t.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
        }
        return container;
    }

    public IndexedContainer exec_order_for_sel(MyVaadinUI myUi) throws SQLException {

        String sql = "select o.id, o.name, o.education_status_id, es.name from orders as o "
                + "left join education_status as es on o.education_status_id = es.id "
                + "where o.id < 4";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.EducationStatus), String.class, null);
        container.addContainerProperty(Settings.education_status_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("o.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("o.name"));
            item.getItemProperty(myUi.getMessage(Messages.EducationStatus)).setValue(
                    result.getString("es.name"));
            item.getItemProperty(Settings.education_status_id).setValue(
                    result.getInt("o.education_status_id"));
        }
        return container;
    }

    public IndexedContainer exec_positions_for_select(
            MyVaadinUI myUi, boolean withAdmin, boolean withHR) throws SQLException {

        String sql = "select t.id, t.name, t.default_permissions, p.id from hr_position as t "
                + "left join position as p on p.hr_position_id = t.id "
                + "where t.activity_status_id = 2 ";
        if (!withAdmin) {
            sql += "and t.id != 5 ";
        }
        if (!withHR && !withAdmin) {
            sql += "and t.id != 25 ";
        }
        sql += "order by t.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.position_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Permissions), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(myUi.getMessage(Messages.Permissions)).setValue(
                    result.getString("t.default_permissions"));
            item.getItemProperty(Settings.position_id).setValue(
                    result.getInt("p.id"));
        }
        return container;
    }

    public IndexedContainer exec_years_with_dates(MyVaadinUI myUi) throws SQLException {
        String sql = "select t.id, t.name, t.start_date, t.end_date from year as t order by t.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.StartDate), Date.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.TillDate), Date.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(myUi.getMessage(Messages.StartDate)).setValue(
                    result.getDate("t.start_date"));
            item.getItemProperty(myUi.getMessage(Messages.TillDate)).setValue(
                    result.getDate("t.end_date"));
        }
        return container;
    }

    public IndexedContainer exec_years_for_select(MyVaadinUI myUi, int current_year_id) throws SQLException {
        String sql = "select t.id, t.name, t.start_date, t.end_date, t.is_last, t.installment_date_limit from year as t " +
                "where t.id between ? and ? order by t.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, current_year_id - 1);
        stat.setInt(2, current_year_id + 1);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.titleShort, String.class, null);
        container.addContainerProperty(Settings.installmentDateLimit, Long.class, 0L);
        container.addContainerProperty(Settings.is_last, Boolean.class, false);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(result.getString("t.name") + " (" +
                    myUi.getMessage(Messages.From) + " " + Settings.df.format(result.getDate("t.start_date")) + " " +
                    myUi.getMessage(Messages.To) + " " + Settings.df.format(result.getDate("t.end_date")) + ")");
            item.getItemProperty(Settings.titleShort).setValue(result.getString("t.name"));
            item.getItemProperty(Settings.installmentDateLimit).setValue(result.getLong("t.installment_date_limit"));
            item.getItemProperty(Settings.is_last).setValue(result.getBoolean("t.is_last"));
        }
        return container;
    }

    public int exec_insert(Definition def, String dbTableName, boolean withActivityStatus) throws SQLException {
        String sql = "INSERT IGNORE INTO " + dbTableName + " (name ";
        if (withActivityStatus) {
            sql += ", activity_status_id";
        }
        sql += ") VALUES(?";
        if (withActivityStatus) {
            sql += ",?";
        }
        sql += ")";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, def.getName());
        if (withActivityStatus) {
            stat.setInt(2, def.getActivity_status_id());
        }

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Definition def, String dbTableName, boolean withActivityStatus) throws SQLException {
        String sql = "UPDATE " + dbTableName + " SET name = ? ";
        if (withActivityStatus) {
            sql += ",activity_status_id = ? ";
        }
        sql += "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, def.getName());
        if (withActivityStatus) {
            stat.setInt(2, def.getActivity_status_id());
            stat.setInt(3, def.getId());
        } else {
            stat.setInt(2, def.getId());
        }
        int status = stat.executeUpdate();
        return status;
    }

    public int exec_update(String dbTableName, String dbColumn, int old_id, int new_id) throws SQLException {
        String sql = "UPDATE " + dbTableName + " SET " + dbColumn + " = ? WHERE " + dbColumn + " = ? ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, new_id);
        stat.setInt(2, old_id);
        return stat.executeUpdate();
    }

    public int exec_update_emp_id(int id, int emp_id, String dbTableName) throws SQLException {
        String sql = "UPDATE " + dbTableName + " SET employee_id = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, emp_id);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public int exec_delete(int id, String dbTableName) throws SQLException {
        String sql = "DELETE FROM " + dbTableName + " WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        return stat.executeUpdate();
    }

    public int exec_delete_not_referenced(String dbTableName) throws SQLException {
        String sql = "DELETE IGNORE FROM " + dbTableName;
        PreparedStatement stat = dbCon.prepareStatement(sql);
        return stat.executeUpdate();
    }

    public int exec_delete(String id, String dbTableName) throws SQLException {
        String sql = "DELETE FROM " + dbTableName + " WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public int exec_delete(String id, String dbTableName, String dbColumnName) throws SQLException {
        String sql = "DELETE FROM " + dbTableName + " WHERE " + dbColumnName + " = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execPermissionSQL(MyVaadinUI myUi) throws SQLException {
        String sql = "SELECT p.java_class_name, p.caption, p.permissions FROM permissions as p " +
                "order by p.caption";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.ClassName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ClassCaption), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Value), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Functions), ComboBoxMultiselect.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getString("p.java_class_name"));
            item.getItemProperty(myUi.getMessage(Messages.ClassName)).setValue(
                    result.getString("p.java_class_name"));
            item.getItemProperty(myUi.getMessage(Messages.ClassCaption)).setValue(
                    result.getString("p.caption"));
            item.getItemProperty(myUi.getMessage(Messages.Value)).setValue(
                    result.getString("p.permissions"));

        }
        return container;
    }

    public IndexedContainer execSQL_statuses_with_count(MyVaadinUI myUi, String table, boolean withTotal) throws SQLException {

        String sql = "SELECT id, name FROM " + table;
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.count, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));
            item.getItemProperty(Settings.id).setValue(
                    result.getInt("id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("name"));
        }
        if (withTotal) {
            Item item = container.addItem(6);
            item.getItemProperty(Settings.id).setValue(6);
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(myUi.getMessage(Messages.Total));
        }

        return container;
    }


    public IndexedContainer execSQL_yes_no(MyVaadinUI myUi) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);

        Item item = container.addItem(1);
        item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(myUi.getMessage(Messages.Yes));
        item = container.addItem(0);
        item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(myUi.getMessage(Messages.No));

        return container;
    }

    public int execSQL_login() throws SQLException {
        String sql = "SELECT l.login FROM logins AS l LEFT JOIN employee AS e ON l.login = e.login " +
                "WHERE e.login IS NULL LIMIT 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("l.login");
        }
        return 0;
    }

    public IndexedContainer exec_correction_types(MyVaadinUI myUi) throws SQLException {
        String sql = "select t.id, concat('(', t.type, ') ', t.name) as name, t.type " +
                "from correction_type as t order by t.type, t.name";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.correction_type_id, Double.class, 1.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("name"));
            if (result.getString("t.type").equals("-")) {
                item.getItemProperty(Settings.correction_type_id).setValue(-1.0);
            }
        }
        return container;
    }

    public List<AccBalanceSettings> exec_balance_settings(int acc_type_id) throws SQLException {
        String sql = "SELECT bs.id, cat.id, cat.name, CASE WHEN bs.year_preferences = 1 THEN " +
                "CONCAT((SELECT t.name FROM year AS t WHERE t.is_last = 1), ' ', bs.prefix) " +
                "WHEN bs.year_preferences = - 1 THEN " +
                "CONCAT((SELECT t.name FROM year AS t WHERE t.id = " +
                "(SELECT t.id - 1 FROM year AS t WHERE t.is_last = 1)), ' ', bs.prefix) " +
                "ELSE bs.prefix END AS prefix, bs.postfix, bs.text_field_preferences " +
                "FROM acc_balance_settings AS bs " +
                "LEFT JOIN acc_category AS cat ON cat.id = bs.acc_cactegory_id " +
                "WHERE cat.acc_type_id = ? AND bs.activity_status_id = 2";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_type_id);
        ResultSet result = stat.executeQuery();
        List<AccBalanceSettings> list = new ArrayList<>();
        while (result.next()) {
            AccBalanceSettings balanceSettings = new AccBalanceSettings();
            balanceSettings.setId(result.getInt("bs.id"));
            balanceSettings.setAccCategoryId(result.getInt("cat.id"));
            balanceSettings.setAccCategory(result.getString("cat.name"));
            balanceSettings.setPrefix(result.getString("prefix"));
            balanceSettings.setPostfix(result.getString("bs.postfix"));
            balanceSettings.setWithTextField(result.getBoolean("bs.text_field_preferences"));
            list.add(balanceSettings);
        }
        return list;
    }


    public List<Month> exec_months() throws SQLException {
        String sql = "SELECT t.id, t.name FROM months as t order by t.order_num";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        List<Month> list = new ArrayList<>();
        while (result.next()) {
            list.add(new Month(result.getInt("t.id"), result.getString("t.name")));
        }
        return list;
    }

    public int search_id(String table, String column, String value) throws SQLException {
        String sql = "select id from " + table + " where " + column + " = " + "?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, value);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("id");
        }
        return 0;
    }
}
