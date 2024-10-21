/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.RegexpValidator;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.EmployeePhoneNumber;
import kg.alex.indigo.i18n.Messages;
import kg.alex.indigo.ui.EmployeeDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeePhoneNumber extends BaseDb {

    public DbEmployeePhoneNumber() throws Exception {
        super();
    }

    public int exec_insert(EmployeePhoneNumber epn) throws SQLException {
        String sql = "INSERT INTO hr_employee_phone_number (employee_id,hr_phone_type_id,"
                + "number) VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, epn.getEmployee_id());
        stat.setInt(2, epn.getPhone_type_id());
        stat.setString(3, epn.getNumber().trim());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeePhoneNumber epn) throws SQLException {
        String sql = "update hr_employee_phone_number set "
                + "hr_phone_type_id = ?, number = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, epn.getPhone_type_id());
        stat.setString(2, epn.getNumber().trim());
        stat.setInt(3, epn.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {


        String sql = "SELECT epn.id, epn.hr_phone_type_id, epn.number "
                + "FROM hr_employee_phone_number as epn where epn.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.preparePhonesContainer();
        while (result.next()) {
            String id = result.getString("epn.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, null, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(
                    edv.createCombobox(result.getInt("epn.hr_phone_type_id"),
                            myUI.getMessage(Messages.Type), Settings.dbPhoneType, true));
            item.getItemProperty(myUI.getMessage(Messages.Number)).setValue(
                    edv.createTextField(result.getString("epn.number"),
                            myUI.getMessage(Messages.Number),
                            new RegexpValidator("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s0-9]*$", true,
                                    myUI.getMessage(Messages.NotificationWrongValue)), true));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }
}
