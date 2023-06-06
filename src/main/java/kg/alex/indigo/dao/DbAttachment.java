/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.Attachment;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.ui.EmployeeDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbAttachment extends BaseDb {

    public DbAttachment() throws Exception {
        super();
    }

    public int exec_insert(Attachment a) throws SQLException {
        String sql = "INSERT INTO attachments (name,extension,unique_name) VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, a.getName());
        stat.setString(2, a.getExtension());
        stat.setString(3, a.getUnique_name());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int employee_id, EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT a.id, a.name, a.unique_name, concat(c.name, ' - ', ec.given_by) as details, 'Сертификат' as type " +
                "FROM hr_employee_certificate AS ec " +
                "LEFT JOIN hr_certificate c ON c.id = ec.certificate_id " +
                "LEFT JOIN attachments a ON a.id = ec.attachment_id WHERE ec.employee_id = ? " +
                "UNION " +
                "SELECT a.id, a.name, a.unique_name, e.name as details, 'Экзамен' as type " +
                "FROM hr_employee_exam AS ex " +
                "LEFT JOIN hr_exam e ON e.id = ex.hr_exam_id " +
                "LEFT JOIN attachments a ON a.id = ex.attachment_id WHERE ex.employee_id = ? " +
                "UNION " +
                "SELECT a.id, a.name, a.unique_name, concat(u.name, ' - ', el.name) as details, 'Образование' as type " +
                "FROM hr_employee_education AS ed " +
                "LEFT JOIN hr_university u ON u.id = ed.hr_university_id " +
                "LEFT JOIN hr_education_level el ON el.id = ed.education_level_id " +
                "LEFT JOIN attachments a ON a.id = ed.attachment_id " +
                "WHERE ed.employee_id = ? AND hr_own_id = 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, employee_id);
        stat.setInt(3, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Details), String.class, null);
        container.addContainerProperty(myUi.getMessage(IndigoMessages.Type), String.class, null);
        container.addContainerProperty(Settings.button, Button.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("id"));

            item.getItemProperty(myUi.getMessage(IndigoMessages.Title)).setValue(result.getString("name"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Type)).setValue(result.getString("type"));
            item.getItemProperty(myUi.getMessage(IndigoMessages.Details)).setValue(result.getString("details"));
            Attachment a = new Attachment();
            a.setId(result.getInt("id"));
            a.setUnique_name(result.getString("unique_name"));
            a.setName(result.getString("name"));
            com.vaadin.ui.Button b = edv.createButton(myUi.getMessage(IndigoMessages.DownLoad), a.getId() + "",
                    Settings.download_button, FontAwesome.DOWNLOAD);
            b.setStyleName(ValoTheme.BUTTON_SMALL);
            b.setData(a);
            item.getItemProperty(Settings.button).setValue(b);

        }
        return container;
    }
}
