package kg.alex.abl.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInstallmentPlanType extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbInstallmentPlanType.class);

    public DbInstallmentPlanType() throws Exception {
        super();
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi) throws SQLException {

        String sql = "SELECT t.id, t.code, t.name, t.payment_count, " +
                "t.payment_interval_months, t.start_month, t.due_day, " +
                "t.divide_equal, t.is_custom, t.description " +
                "FROM installment_plan_type as t " +
                "ORDER BY t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);

        container.addContainerProperty(Settings.id, Integer.class, null);
        container.addContainerProperty(Settings.installment_plan_type_code, String.class, null);
        container.addContainerProperty(Settings.installment_payment_count, Integer.class, null);
        container.addContainerProperty(Settings.installment_payment_interval_months, Integer.class, null);
        container.addContainerProperty(Settings.installment_start_month, Integer.class, null);
        container.addContainerProperty(Settings.installment_due_day, Integer.class, null);
        container.addContainerProperty(Settings.installment_divide_equal, Boolean.class, false);
        container.addContainerProperty(Settings.installment_is_custom, Boolean.class, false);
        container.addContainerProperty(Settings.installment_description, String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));

            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));

            item.getItemProperty(Settings.id).setValue(
                    result.getInt("t.id"));

            item.getItemProperty(Settings.installment_plan_type_code).setValue(
                    result.getString("t.code"));

            item.getItemProperty(Settings.installment_payment_count).setValue(
                    getNullableInt(result, "t.payment_count"));

            item.getItemProperty(Settings.installment_payment_interval_months).setValue(
                    getNullableInt(result, "t.payment_interval_months"));

            item.getItemProperty(Settings.installment_start_month).setValue(
                    getNullableInt(result, "t.start_month"));

            item.getItemProperty(Settings.installment_due_day).setValue(
                    getNullableInt(result, "t.due_day"));

            item.getItemProperty(Settings.installment_divide_equal).setValue(
                    result.getInt("t.divide_equal") == 1);

            item.getItemProperty(Settings.installment_is_custom).setValue(
                    result.getInt("t.is_custom") == 1);

            item.getItemProperty(Settings.installment_description).setValue(
                    result.getString("t.description"));
        }

        return container;
    }

    private Integer getNullableInt(ResultSet result, String columnName) throws SQLException {
        int value = result.getInt(columnName);
        return result.wasNull() ? null : value;
    }
}