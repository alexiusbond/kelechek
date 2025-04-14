/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.EmployeeLanguage;
import kg.alex.lomonosov.i18n.Messages;
import kg.alex.lomonosov.ui.EmployeeDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeLanguage extends BaseDb {

    public DbEmployeeLanguage() throws Exception {
        super();
    }

    public int exec_insert(EmployeeLanguage el) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_language (employee_id,hr_language_id,hr_language_level_id) "
                + "VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, el.getEmployee_id());
        stat.setInt(2, el.getLanguage_id());
        stat.setInt(3, el.getLevel_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeLanguage el) throws SQLException {
        String sql = "update hr_employee_language set "
                + "hr_language_id = ?, hr_language_level_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, el.getLanguage_id());
        stat.setInt(2, el.getLevel_id());
        stat.setInt(3, el.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {


        String sql = "SELECT el.id, el.hr_language_id, el.hr_language_level_id FROM hr_employee_language as el "
                + "where el.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareLanguageContainer();
        while (result.next()) {
            String id = result.getString("el.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeLanguage, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Language)).setValue(
                    edv.createCombobox(result.getInt("el.hr_language_id"),
                            myUI.getMessage(Messages.Language), Settings.dbLanguageTable, true));
            item.getItemProperty(myUI.getMessage(Messages.Level)).setValue(
                    edv.createCombobox(result.getInt("el.hr_language_level_id"),
                            myUI.getMessage(Messages.Level), Settings.dbLanguageLevelTable, true));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }
}
