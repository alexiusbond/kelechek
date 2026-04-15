package kg.alex.star.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.domain.Discount;
import kg.alex.star.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbDiscount extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbDiscount.class);

    public DbDiscount() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {

        String sql = "SELECT d.id, d.name, d.amount, d.activity_status_id, dt.name, d.discount_type_id, " +
                     "ac.name, d.year_id, y.name FROM discount as d " +
                     "left join discount_type as dt on dt.id = d.discount_type_id " +
                     "left join year as y on y.id = d.year_id " +
                     "left join activity_status as ac on ac.id = d.activity_status_id " +
                     "order by y.id DESC, d.id DESC";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Value), Double.class, 0.0);
        container.addContainerProperty(Settings.discount_type_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.DiscountType), String.class, null);
        container.addContainerProperty(Settings.year_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Year), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("d.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("d.name"));
            item.getItemProperty(myUi.getMessage(Messages.Value)).setValue(
                    result.getDouble("d.amount"));
            item.getItemProperty(Settings.discount_type_id).setValue(
                    result.getInt("d.discount_type_id"));
            item.getItemProperty(myUi.getMessage(Messages.DiscountType)).setValue(
                    result.getString("dt.name"));
            item.getItemProperty(Settings.year_id).setValue(
                    result.getInt("d.year_id"));
            item.getItemProperty(myUi.getMessage(Messages.Year)).setValue(
                    result.getString("y.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("d.activity_status_id"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.id).setValue(result.getInt("d.id"));
        }
        return container;
    }

    public int exec_insert(Discount d) throws SQLException {
        String sql = "INSERT IGNORE INTO discount (name, amount, activity_status_id, discount_type_id, year_id) "
                     + "VALUES(?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, d.getName());
        stat.setDouble(2, d.getAmount());
        stat.setInt(3, d.getStatus_id());
        stat.setInt(4, d.getDisc_type_id());
        stat.setInt(5, d.getYear_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Discount d) throws SQLException {
        String sql = "UPDATE discount SET name = ?, amount = ?, activity_status_id = ?, "
                     + "discount_type_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, d.getName());
        stat.setDouble(2, d.getAmount());
        stat.setInt(3, d.getStatus_id());
        stat.setInt(4, d.getDisc_type_id());
        stat.setInt(5, d.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL_for_year_sel(MyVaadinUI myUi, int cur_year) throws SQLException {
        String sql = "SELECT  distinct(d.year_id), y.name FROM discount as d left join year as y on y.id = d.year_id where d.year_id != ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cur_year);
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

    public int exec_copy(int curr_year, int selected_year) throws SQLException {
        String sql = "insert ignore into discount (name, amount, "
                     + "discount_type_id, year_id, activity_status_id) select name, "
                     + "amount,discount_type_id, ? as year_id, activity_status_id "
                     + "from discount where year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, curr_year);
        stat.setInt(2, selected_year);
        return stat.executeUpdate();
    }

    public IndexedContainer exec_disc_select(MyVaadinUI myUi, int year_id) throws SQLException {

        String sql = "select t.id, t.name, t.amount, t.discount_type_id " +
                     "from discount as t " +
                     "where t.year_id = ? and t.activity_status_id = 2 order by t.name, t.amount";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(Settings.discount_type_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            if (result.getInt("t.discount_type_id") == 3) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("t.name") + " - (max " + result.getString("t.amount") + "%)");
            } else if (result.getInt("t.discount_type_id") == 4) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("t.name") + " - (max " + result.getString("t.amount")
                        + " " + Settings.USD + ")");
            } else if (result.getInt("t.discount_type_id") == 1) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("t.name") + " - " + result.getString("t.amount") + "%");
            } else if (result.getInt("t.discount_type_id") == 2) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("t.name") + " - " + result.getString("t.amount")
                        + " " + Settings.USD + ")");
            }
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(
                    result.getDouble("t.amount"));
            item.getItemProperty(Settings.discount_type_id).setValue(
                    result.getInt("t.discount_type_id"));
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int year_id, int discount_id, int student_id,
                                            String studentFullName) throws SQLException {
        double orderAmountDiscount = 0.0, orderPercentDiscount = 0.0;
        try {
            DbOrderMessage dbCon = new DbOrderMessage();
            dbCon.connect();
            orderAmountDiscount = dbCon.execSQL_discountAmount(
                    year_id, student_id, studentFullName, "2,3");
            orderPercentDiscount = dbCon.execSQL_discountAmount(
                    year_id, student_id, studentFullName, "1");
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        String sql = "SELECT d.id, d.name, " +
                     "(CASE WHEN d.discount_unit_id = 2 AND ? IS NOT NULL THEN ? " +
                     "WHEN d.discount_unit_id = 1 AND ? IS NOT NULL THEN ? ELSE d.amount END) as amount, " +
                     "d.discount_type_id, d.discount_unit_id FROM discount as d " +
                     "where d.year_id = ? and (d.activity_status_id = 2 or d.id = ?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (orderAmountDiscount != 0.0) {
            stat.setDouble(1, orderAmountDiscount);
        } else {
            stat.setNull(1, Types.DOUBLE);
        }
        stat.setDouble(2, orderAmountDiscount);
        if (orderPercentDiscount != 0.0) {
            stat.setDouble(3, orderPercentDiscount);
        } else {
            stat.setNull(3, Types.DOUBLE);
        }
        stat.setDouble(4, orderPercentDiscount);
        stat.setInt(5, year_id);
        stat.setInt(6, discount_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUi.getMessage(Messages.DiscountType), Integer.class, null);
        container.addContainerProperty(Settings.is_order_discount, Boolean.class, false);
        while (result.next()) {
            Item item = container.addItem(result.getInt("d.id"));
            if (result.getInt("d.discount_type_id") == 3) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("d.name") + " - (max "
                        + Settings.dFormat2.format(result.getDouble("amount")) + "%)");
            } else if (result.getInt("d.discount_type_id") == 4) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("d.name") + " - (max "
                        + Settings.dFormat2.format(result.getDouble("amount"))
                        + " " + Settings.USD + ")");
            } else if (result.getInt("d.discount_type_id") == 1) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("d.name") + " - "
                        + Settings.dFormat2.format(result.getDouble("amount")) + "%");
            } else if (result.getInt("d.discount_type_id") == 2) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        result.getString("d.name") + " - "
                        + Settings.dFormat2.format(result.getDouble("amount"))
                        + " " + Settings.USD);
            }
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(Settings.round(
                    result.getDouble("amount"), 2));
            item.getItemProperty(myUi.getMessage(Messages.DiscountType)).setValue(
                    result.getInt("d.discount_type_id"));
            item.getItemProperty(Settings.is_order_discount).setValue(
                    result.getInt("d.discount_unit_id") != 0);
        }
        return container;
    }
}
