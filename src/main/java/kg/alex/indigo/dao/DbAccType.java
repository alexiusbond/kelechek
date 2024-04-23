/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.i18n.IndigoMessages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbAccType extends BaseDb {

    public DbAccType() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI) throws SQLException {
        String sql = "SELECT c.id, ifnull(concat(c.parent_code,'.',c.code),c.code) as code, c.name FROM acc_type t " +
                "left join acc_category as c on t.acc_category_id = c.id " +
                "where t.id = 5";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Code), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(IndigoMessages.Code)).setValue(result.getString("code"));
        }
        return container;
    }

    public int exec_update(int cat_id, int id) throws SQLException {
        String sql = "update acc_type set acc_category_id = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cat_id);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }
}
