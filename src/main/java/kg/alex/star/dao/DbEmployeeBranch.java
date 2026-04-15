/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.domain.EmployeeBranch;
import kg.alex.star.i18n.Messages;
import kg.alex.star.ui.EmployeeDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeBranch extends BaseDb {

    public DbEmployeeBranch() throws Exception {
        super();
    }

    public int exec_insert(EmployeeBranch eb) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_branch (employee_id,hr_branch_id,hr_importance_id) "
                + "VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eb.getEmployee_id());
        stat.setInt(2, eb.getBranch_id());
        if (eb.isMain()) {
            stat.setInt(3, 1);
        } else {
            stat.setInt(3, 2);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeBranch eb) throws SQLException {
        String sql = "update hr_employee_branch set "
                + "hr_branch_id = ?, hr_importance_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eb.getBranch_id());
        if (eb.isMain()) {
            stat.setInt(2, 1);
        } else {
            stat.setInt(2, 2);
        }
        stat.setInt(3, eb.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ex.id, ex.hr_branch_id, ex.hr_importance_id FROM hr_employee_branch as ex "
                + "where ex.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareBranchContainer();
        while (result.next()) {
            String id = result.getString("ex.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeBranch, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Branch)).setValue(
                    edv.createCombobox(result.getInt("ex.hr_branch_id"),
                            myUI.getMessage(Messages.Branch), Settings.dbBranchTable, true));
            if (result.getInt("ex.hr_importance_id") == 1) {
                item.getItemProperty(myUI.getMessage(Messages.Main)).setValue(
                        edv.createCheckBox(true, myUI.getMessage(Messages.Main)));
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Main)).setValue(
                        edv.createCheckBox(false, myUI.getMessage(Messages.Main)));
            }
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }
}
