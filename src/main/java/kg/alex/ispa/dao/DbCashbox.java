package kg.alex.ispa.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.domain.CashBox;
import kg.alex.ispa.i18n.Messages;

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

    public IndexedContainer execSQLForStudentPayments(MyVaadinUI myUi)
            throws SQLException {

        String sql = "SELECT c.id, c.name, c.acc_currency_id, c.payment_type_id, cur.name, p.name FROM acc_cashbox as c " +
                "LEFT JOIN acc_currency as cur on cur.id = c.acc_currency_id " +
                "LEFT JOIN payment_type as p on p.id = c.payment_type_id " +
                "where c.payment_type_id is not null";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Currency), String.class, null);
        container.addContainerProperty(Settings.acc_currency_id, Integer.class, 0);
        container.addContainerProperty(Settings.payment_type_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.PaymentType), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(result.getString("c.name"));
            item.getItemProperty(myUi.getMessage(Messages.Currency)).setValue(result.getString("cur.name"));
            item.getItemProperty(myUi.getMessage(Messages.PaymentType)).setValue(result.getString("p.name"));
            item.getItemProperty(Settings.acc_currency_id).setValue(result.getInt("c.acc_currency_id"));
            item.getItemProperty(Settings.payment_type_id).setValue(result.getInt("c.payment_type_id"));
        }
        return container;
    }

    public CashBox getCashboxByCurrencyAndType(int currencyId, int paymentTypeId)
            throws SQLException {
        String sql = "SELECT c.id, c.acc_currency_id FROM acc_cashbox as c " +
                "WHERE c.acc_currency_id = ? and payment_type_id = ?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currencyId);
        stat.setInt(2, paymentTypeId);
        ResultSet result = stat.executeQuery();

        if (result.next()) {
            return new CashBox(result.getInt("c.id"),
                    result.getInt("c.acc_currency_id"));
        }
        return null;
    }

    public CashBox getCashboxById(int id)
            throws SQLException {
        String sql = "SELECT c.id, c.acc_currency_id FROM acc_cashbox as c WHERE c.id = ?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();

        if (result.next()) {
            return new CashBox(result.getInt("c.id"),
                    result.getInt("c.acc_currency_id"));
        }
        return null;
    }
}
