/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbSalaryCategories extends BaseDb {

    public DbSalaryCategories() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI) throws SQLException {
        String sql = "SELECT c.id, ifnull(concat(c.parent_code,'.',c.code),c.code) as code, c.name FROM hr_salary_category sc "
                + "left join acc_category as c on sc.acc_category_id = c.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(result.getString("c.name"));
            item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(result.getString("code"));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int school_id) throws SQLException {
        String sql = "SELECT c.id, IFNULL(CONCAT(c.parent_code, '.', c.code), c.code) AS code, sc.name, sc.role_visibility " +
                "FROM hr_salary_category sc " +
                "LEFT JOIN acc_category AS c ON sc.acc_category_id = c.parent_id and school_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("c.id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(result.getString("sc.name"));
            item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(result.getString("code"));
        }
        return container;
    }

    public int exec_update(int cat_id, int id) throws SQLException {
        String sql = "update hr_salary_category set acc_category_id = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cat_id);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }
}
