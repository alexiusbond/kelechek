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
import kg.alex.aim.domain.Contract;
import kg.alex.aim.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author alex
 */
public class DbContract extends BaseDb {

    public DbContract() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id, String amountTitle)
            throws SQLException {

        String sql = "SELECT c.id, c.name, c.amount, c.year_id, y.name, c.school_id, "
                     + "sc.name_ru, sc.name_ru, c.activity_status_id, ac.name "
                     + "FROM contract as c "
                     + "left join year as y on y.id = c.year_id "
                     + "left join school as sc on sc.id = c.school_id "
                     + "left join activity_status as ac on ac.id = c.activity_status_id "
                     + "where c.school_id = ? order by y.id DESC, c.id DESC";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(amountTitle, Double.class, 0.0);
        container.addContainerProperty(Settings.year_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Year), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.school_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.School), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(amountTitle).setValue(result.getDouble("c.amount"));
            item.getItemProperty(Settings.year_id).setValue(
                    result.getInt("c.year_id"));
            item.getItemProperty(myUi.getMessage(Messages.Year)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("c.activity_status_id"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.school_id).setValue(
                    result.getInt("c.school_id"));
            item.getItemProperty(myUi.getMessage(Messages.School)).setValue(
                    result.getString("sc.name_ru"));
            item.getItemProperty(Settings.id).setValue(result.getInt("c.id"));
        }
        return container;
    }

    public int exec_insert(Contract c) throws SQLException {
        String sql = "INSERT IGNORE INTO contract " +
                     "(name, amount, year_id, school_id, activity_status_id, employee_id) " +
                     "VALUES(?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, c.getName());
        stat.setDouble(2, c.getValue());
        stat.setInt(3, c.getYear_id());
        stat.setInt(4, c.getSchool_id());
        stat.setInt(5, c.getStatus_id());
        stat.setInt(6, c.getEmployee_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Contract c) throws SQLException {
        String sql = "UPDATE contract SET name = ?, amount = ?, activity_status_id = ?, employee_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, c.getName());
        stat.setDouble(2, c.getValue());
        stat.setInt(3, c.getStatus_id());
        stat.setInt(4, c.getEmployee_id());
        stat.setInt(5, c.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_for_year_sel(MyVaadinUI myUi, int cur_year,
                                                 int scl_id) throws SQLException {
        String sql = "SELECT distinct(d.year_id), y.name "
                     + "FROM contract as d left join year as y on y.id = d.year_id "
                     + "where d.year_id != ? and d.school_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cur_year);
        stat.setInt(2, scl_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("d.year_id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("y.name"));
        }
        return container;
    }

    public int exec_copy(int selected_year, MyVaadinUI myUi) throws SQLException {
        String sql = "insert ignore into contract (name, amount, year_id, school_id, "
                     + "activity_status_id, employee_id) select name, amount, ? as year_id,school_id, "
                     + "activity_status_id, ? from contract where year_id = ? and activity_status_id = 2 "
                     + "and school_id = ? ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, myUi.getUser().getId());
        stat.setInt(3, selected_year);
        stat.setInt(4, myUi.getUser().getSchool().getId());
        return stat.executeUpdate();
    }

    public IndexedContainer exec_contr_select(
            MyVaadinUI myUi, int year_id, int school_id, int contract_id)
            throws SQLException {
        String sql = "select t.id, t.name, t.amount, y.name " +
                     "from contract as t " +
                     "left join year as y on t.year_id = y.id " +
                     "where t.year_id = ? and t.school_id = ? " +
                     "and (t.activity_status_id = 2 or t.id = ?) order by t.name, t.amount";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, school_id);
        stat.setInt(3, contract_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.ShortTitle), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Amount), Double.class, 0.0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.ShortTitle)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name") + " " + Settings.dFormat2.format(result.getDouble("t.amount"))
                    + " " + Settings.USD + " (" + result.getString("y.name") + ")");
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(
                    result.getDouble("t.amount"));
        }
        return container;
    }
}
