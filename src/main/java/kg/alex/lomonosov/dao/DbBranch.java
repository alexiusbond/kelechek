/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.Branch;
import kg.alex.lomonosov.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbBranch extends BaseDb {

    public DbBranch() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {

        String sql = "SELECT b.id, b.name, b.code, b.activity_status_id, ac.name FROM hr_branch as b "
                + "left join activity_status as ac on ac.id = b.activity_status_id "
                + "order by b.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Code), String.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("b.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("b.name"));
            item.getItemProperty(myUi.getMessage(Messages.Code)).setValue(
                    result.getString("b.code"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("ac.name"));
            item.getItemProperty(Settings.status_id).setValue(
                    result.getInt("b.activity_status_id"));
            item.getItemProperty(Settings.id).setValue(result.getInt("b.id"));
        }
        return container;
    }

    public int exec_insert(Branch branch) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_branch (name,code,activity_status_id) VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, branch.getName());
        stat.setString(2, branch.getCode());
        stat.setInt(3, branch.getStatus_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Branch branch) throws SQLException {
        String sql = "UPDATE hr_branch SET name = ?, code = ?, activity_status_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, branch.getName());
        stat.setString(2, branch.getCode());
        stat.setInt(3, branch.getStatus_id());
        stat.setInt(4, branch.getId());
        return stat.executeUpdate();
    }

}
