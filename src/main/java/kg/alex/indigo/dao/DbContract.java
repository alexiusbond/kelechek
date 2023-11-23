/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.Contract;
import kg.alex.indigo.i18n.IndigoMessages;

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
                + "sc.name_ru, sc.name_ru, c.activity_status_id, ac.name, c.duration "
                + "FROM contract as c "
                + "left join year as y on y.id = c.year_id "
                + "left join school as sc on sc.id = c.school_id "
                + "left join activity_status as ac on ac.id = c.activity_status_id "
                + "where c.school_id = ? order by y.id DESC, c.id DESC";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(amountTitle, Double.class, 0.0);
        container.addContainerProperty(Settings.year_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Year), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.DurationInMonths), Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Status), String.class, null);
        container.addContainerProperty(Settings.school_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.School), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(amountTitle).setValue(result.getDouble("c.amount"));
            item.getItemProperty(Settings.year_id).setValue(
                    result.getInt("c.year_id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Year)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.DurationInMonths)).setValue(
                    result.getInt("c.duration"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("c.activity_status_id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.school_id).setValue(
                    result.getInt("c.school_id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.School)).setValue(
                    result.getString("sc.name_ru"));
            item.getItemProperty(Settings.id).setValue(result.getInt("c.id"));
        }
        return container;
    }

    public int exec_insert(Contract c) throws SQLException {
        String sql = "INSERT IGNORE INTO contract (name,amount,"
                + "year_id,school_id,activity_status_id,employee_id,duration) "
                + "VALUES(?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, c.getName());
        stat.setDouble(2, c.getValue());
        stat.setInt(3, c.getYear_id());
        stat.setInt(4, c.getSchool_id());
        stat.setInt(5, c.getStatus_id());
        stat.setInt(6, c.getEmployee_id());
        stat.setInt(7, c.getDuration());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Contract c) throws SQLException {
        String sql = "UPDATE contract SET name = ?, amount = ?,activity_status_id = ?, employee_id = ?, "
                + "duration = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, c.getName());
        stat.setDouble(2, c.getValue());
        stat.setInt(3, c.getStatus_id());
        stat.setInt(4, c.getEmployee_id());
        stat.setInt(5, c.getDuration());
        stat.setInt(6, c.getId());
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
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("d.year_id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(
                    result.getString("y.name"));
        }
        return container;
    }

    public int exec_copy(int selected_year, MyVaadinUI myUi) throws SQLException {
        String sql = "insert ignore into contract (name, amount, year_id, school_id, "
                + "activity_status_id, employee_id, duration) select name, amount, ? as year_id,school_id, "
                + "activity_status_id, ?, duration from contract where year_id = ? and activity_status_id = 2 "
                + "and school_id = ? ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, myUi.getUser().getId());
        stat.setInt(3, selected_year);
        stat.setInt(4, myUi.getUser().getSchool().getId());
        return stat.executeUpdate();
    }

    public IndexedContainer exec_contr_select(MyVaadinUI myUi, int year_id, int school_id,
                                              int contr_id)
            throws SQLException {
        String sql = "select t.id, t.name, t.amount, t.duration, y.name, cur.name " +
                "from contract as t " +
                "left join school as sch on sch.id = t.school_id " +
                "left join acc_currency as cur on cur.id = sch.acc_currency_id " +
                "left join year as y on t.year_id = y.id " +
                "where t.year_id = ? and t.school_id = ? " +
                "and (t.activity_status_id = 2 or t.id = ?) order by t.name, t.amount";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, school_id);
        stat.setInt(3, contr_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.DurationInMonths), Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(
                    result.getString("t.name") + " - [" + result.getInt("t.duration") + " мес.] "
                            + Settings.dFormat2.format(result.getDouble("t.amount"))
                            + " " + result.getString("cur.name") + " (" + result.getString("y.name") + ")");
            item.getItemProperty(myUi.getMessage(IndigoMessages.Amount)).setValue(
                    result.getDouble("t.amount"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.DurationInMonths)).setValue(
                    result.getInt("t.duration"));
        }
        return container;
    }
}
