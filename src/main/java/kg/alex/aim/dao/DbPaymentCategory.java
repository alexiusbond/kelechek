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
import kg.alex.aim.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbPaymentCategory extends BaseDb {

    public DbPaymentCategory() throws Exception {
        super();
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, boolean withInitPay) throws SQLException {
        String sql;
        if (withInitPay) {
            sql = "select t.id, t.name, t.acc_category_id, t.acc_type_id from payment_category as t "
                    + "order by t.id asc";
        } else {
            sql = "select t.id, t.name, t.acc_category_id, t.acc_type_id from payment_category as t where t.id != 1 "
                    + "order by t.id asc";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(Settings.acc_category_id, Integer.class, 0);
        container.addContainerProperty(Settings.acc_type_id, Integer.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("t.name"));
            item.getItemProperty(Settings.acc_category_id).setValue(
                    result.getInt("t.acc_category_id"));
            item.getItemProperty(Settings.acc_type_id).setValue(
                    result.getInt("t.acc_type_id"));
        }
        return container;
    }

    public int exec_update(int cat_id, int id) throws SQLException {
        String sql = "update payment_category set acc_category_id = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cat_id);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public int get_initial_payment_category_id() throws SQLException {
        String sql = "SELECT acc_category_id FROM payment_category where id = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("acc_category_id");
        }
        return 0;
    }
}
