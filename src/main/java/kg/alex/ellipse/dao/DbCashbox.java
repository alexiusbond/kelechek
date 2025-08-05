package kg.alex.ellipse.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbCashbox extends BaseDb {

    public DbCashbox() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi)
            throws SQLException {

        String sql = "SELECT c.id, c.name, c.acc_currency_id, cur.name FROM acc_cashbox as c " +
                     "LEFT JOIN acc_currency as cur on cur.id = c.acc_currency_id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Currency), String.class, null);
        container.addContainerProperty(Settings.acc_currency_id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("c.name"));
            item.getItemProperty(myUi.getMessage(Messages.Currency)).setValue(
                    result.getString("cur.name"));
            item.getItemProperty(Settings.acc_currency_id).setValue(
                    result.getInt("c.acc_currency_id"));
        }
        return container;
    }


    public int getCashboxByCurrencyAndType(int currencyId, int paymentTypeId)
            throws SQLException {
        int id = 0;
        String sql = "SELECT c.id FROM acc_cashbox as c " +
                     "WHERE c.acc_currency_id = ? and payment_type_id = ?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currencyId);
        stat.setInt(2, paymentTypeId);
        ResultSet result = stat.executeQuery();

        if (result.next()) {
            id = result.getInt("c.id");
        }
        return id;
    }
}
