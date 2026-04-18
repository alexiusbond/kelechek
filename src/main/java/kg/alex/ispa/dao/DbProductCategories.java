/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbProductCategories extends BaseDb {

    public DbProductCategories() throws Exception {
        super();
    }

    public IndexedContainer execSQL_for_select(MyVaadinUI myUI) throws SQLException {
        String sql = "SELECT id, name, acc_category_id "
                + "FROM dp_product_category order by id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, 0);
        while (result.next()) {
            Item item = container.addItem(result.getInt("acc_category_id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(result.getString("name"));
        }
        return container;
    }

    public int exec_update(int cat_id, int id) throws SQLException {
        String sql = "update dp_product_category set acc_category_id = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cat_id);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }
}
