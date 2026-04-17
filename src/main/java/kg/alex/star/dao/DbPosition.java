/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.utils.Settings;
import kg.alex.star.domain.Position;
import kg.alex.star.i18n.Messages;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbPosition extends BaseDb {

    public DbPosition() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi) throws SQLException {

        String sql = "SELECT p.id, p.name, st.id, st.name, pc.id, pc.name, p.default_permissions, pos.id "
                + "FROM hr_position AS p "
                + "LEFT JOIN activity_status AS st ON p.activity_status_id = st.id "
                + "LEFT JOIN hr_position_category AS pc ON p.hr_position_category_id = pc.id "
                + "LEFT JOIN position AS pos ON pos.hr_position_id = p.id "
                + "order by p.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.activity_status_id, Integer.class, 0);
        container.addContainerProperty(Settings.hr_position_category_id, Integer.class, 0);
        container.addContainerProperty(Settings.position_id, Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.Category), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Permissions), String.class, null);
        container.addContainerProperty(Settings.id, Integer.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("p.id"));

            item.getItemProperty(Settings.activity_status_id).setValue(
                    result.getInt("st.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("p.name"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(
                    result.getString("st.name"));
            item.getItemProperty(myUi.getMessage(Messages.Category)).setValue(
                    result.getString("pc.name"));
            item.getItemProperty(myUi.getMessage(Messages.Permissions)).setValue(
                    result.getString("p.default_permissions"));
            item.getItemProperty(Settings.hr_position_category_id).setValue(
                    result.getInt("pc.id"));
            item.getItemProperty(Settings.position_id).setValue(
                    result.getInt("pos.id"));
            item.getItemProperty(Settings.id).setValue(
                    result.getInt("p.id"));

        }
        return container;
    }

    public int exec_insert(Position p) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_position (name, "
                + "hr_position_category_id,activity_status_id, default_permissions) "
                + "VALUES(?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, p.getName());
        stat.setInt(2, p.getPosition_category_id());
        stat.setInt(3, p.getActivity_status_id());
        if (p.getPermissions() != null) {
            stat.setString(4, p.getPermissions());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }

        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(Position p) throws SQLException {
        String sql = "UPDATE hr_position SET name = ?, hr_position_category_id = ?,"
                + "activity_status_id = ?, default_permissions = ? "
                + "WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, p.getName());
        stat.setInt(2, p.getPosition_category_id());
        stat.setInt(3, p.getActivity_status_id());
        if (p.getPermissions() != null) {
            stat.setString(4, p.getPermissions());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setInt(5, p.getId());
        return stat.executeUpdate();
    }

    public int exec_update(int position_id, int id) throws SQLException {
        String sql = "update position set hr_position_id = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, position_id);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }
}
