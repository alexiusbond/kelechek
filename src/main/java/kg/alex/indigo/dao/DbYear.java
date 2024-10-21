package kg.alex.indigo.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.Year;
import kg.alex.indigo.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbYear extends BaseDb {

    public DbYear() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {


        String sql = "SELECT y.id, y.name, y.period, y.period_kg, y.start_date, y.end_date "
                + "FROM year as y order by y.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Period), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.PeriodKg), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.StartDate), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.EndDate), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("y.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(myUi.getMessage(Messages.Period)).setValue(
                    result.getString("y.period"));
            item.getItemProperty(myUi.getMessage(Messages.PeriodKg)).setValue(
                    result.getString("y.period_kg"));
            item.getItemProperty(myUi.getMessage(Messages.StartDate)).setValue(
                    Settings.df.format(result.getDate("y.start_date")));
            item.getItemProperty(myUi.getMessage(Messages.EndDate)).setValue(
                    Settings.df.format(result.getDate("y.end_date")));
            item.getItemProperty(Settings.id).setValue(result.getInt("y.id"));
        }
        return container;
    }

    public int getMaxId() throws SQLException {

        String sql = "SELECT MAX(id) FROM year";
        int maxId = 0;
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            maxId = result.getInt("max(id)");
        }
        return maxId;
    }

    public int exec_insert(Year y) throws SQLException {
        int id = getMaxId();
        String sql = "INSERT IGNORE INTO year (id, name, period,"
                + "period_kg, start_date, end_date) "
                + "VALUES(?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id + 1);
        stat.setString(2, y.getName());
        stat.setString(3, y.getPeriod());
        stat.setString(4, y.getPeriod_kg());
        stat.setDate(5, new java.sql.Date(y.getStart_date().getTime()));
        stat.setDate(6, new java.sql.Date(y.getEnd_date().getTime()));

        int st = stat.executeUpdate();
        if (st != 0) {
            return id + 1;
        } else {
            return 0;
        }
    }

    public int exec_update(Year y) throws SQLException {
        String sql = "UPDATE year SET name = ?, period = ?, period_kg = ?,"
                + "start_date = ?, end_date = ?  "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, y.getName());
        stat.setString(2, y.getPeriod());
        stat.setString(3, y.getPeriod_kg());
        stat.setDate(4, new java.sql.Date(y.getStart_date().getTime()));
        stat.setDate(5, new java.sql.Date(y.getEnd_date().getTime()));
        stat.setInt(6, y.getId());
        return stat.executeUpdate();
    }
}
