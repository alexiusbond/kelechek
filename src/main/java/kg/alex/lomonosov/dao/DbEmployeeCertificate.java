/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.Attachment;
import kg.alex.lomonosov.domain.Definition;
import kg.alex.lomonosov.domain.EmployeeCertificate;
import kg.alex.lomonosov.i18n.Messages;
import kg.alex.lomonosov.ui.EmployeeDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DbEmployeeCertificate extends BaseDb {
    static final Logger logger = LogManager.getLogger(DbEmployeeCertificate.class);

    public DbEmployeeCertificate() throws Exception {
        super();
    }

    public int exec_insert(EmployeeCertificate ec) throws SQLException {
        String sql = "INSERT INTO hr_employee_certificate "
                + "(employee_id,note,given_by,date_of_issue,certificate_id,attachment_id) "
                + "VALUES(?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getEmployee_id());
        if (ec.getNote() != null) {
            stat.setString(2, ec.getNote());
        } else {
            stat.setNull(2, Types.VARCHAR);
        }
        stat.setString(3, ec.getGiven_by());
        stat.setDate(4, new Date(ec.getDate_of_issue().getTime()));
        stat.setInt(5, ec.getCertificate_id());
        stat.setInt(6, ec.getAttachment_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeCertificate ec) throws SQLException {
        String sql = "update hr_employee_certificate set "
                + "note = ?, given_by = ?, date_of_issue = ?, certificate_id = ?, attachment_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (ec.getNote() != null) {
            stat.setString(1, ec.getNote());
        } else {
            stat.setNull(1, Types.VARCHAR);
        }
        stat.setString(2, ec.getGiven_by());
        stat.setDate(3, new Date(ec.getDate_of_issue().getTime()));
        stat.setInt(4, ec.getCertificate_id());
        stat.setInt(5, ec.getAttachment_id());
        stat.setInt(6, ec.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ec.id, ec.note, ec.given_by, ec.date_of_issue, ec.certificate_id, " +
                "a.id, a.name, a.extension, a.unique_name " +
                "FROM hr_employee_certificate as ec " +
                "left join attachments as a on a.id = ec.attachment_id where ec.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareCertificateContainer();
        while (result.next()) {
            String id = result.getString("ec.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeCertificate, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                    edv.createTextField(result.getString("ec.note"),
                            myUI.getMessage(Messages.Note),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 250, true), false));
            item.getItemProperty(myUI.getMessage(Messages.GivenBy)).setValue(
                    edv.createTextField(result.getString("ec.given_by"),
                            myUI.getMessage(Messages.GivenBy),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(Messages.IssueDate)).setValue(
                    edv.createDateField(result.getDate("ec.date_of_issue"),
                            myUI.getMessage(Messages.IssueDate), null,
                            true, Settings.datePattern, Resolution.DAY));
            final ComboBox cb2 = edv.createCombobox(result.getInt("ec.certificate_id"),
                    myUI.getMessage(Messages.Certificate), Settings.dbCertificateTable, true);
            cb2.setNewItemsAllowed(true);
            cb2.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbCertificateTable, false);
                    dbd.close();
                    if (id1 != 0) {
                        for (Object next : container.getItemIds()) {
                            Item item1 = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                    myUI.getMessage(Messages.Certificate)).getValue()).getContainerDataSource()).addItem(id1);
                            item1.getItemProperty(myUI.getMessage(Messages.Title)).setValue(newItemCaption);
                            cb2.setValue(id1);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            });
            item.getItemProperty(myUI.getMessage(Messages.Certificate)).setValue(cb2);

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);

            Attachment a = new Attachment();
            a.setId(result.getInt("a.id"));
            a.setUnique_name(result.getString("a.unique_name"));
            a.setExtension(result.getString("a.extension"));
            a.setName(result.getString("a.name"));
            Button b = edv.createButton(myUI.getMessage(Messages.DownLoad), id, Settings.download_button, FontAwesome.DOWNLOAD);
            b.setStyleName(ValoTheme.BUTTON_SMALL);
            b.setData(a);
            hl.addComponent(b);

            Upload upload = edv.createUpload("", false);
            upload.setId(id);
            upload.setData(b);
            hl.addComponent(upload);
            item.getItemProperty(myUI.getMessage(Messages.Document)).setValue(hl);
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id) throws SQLException {

        String sql = "SELECT ec.id, ec.note, ec.given_by, ec.date_of_issue, c.name FROM hr_employee_certificate AS ec " +
                "LEFT JOIN hr_certificate AS c ON c.id = ec.certificate_id WHERE ec.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Certificate), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.GivenBy), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.IssueDate), String.class, null);
        while (result.next()) {
            String id = result.getString("ec.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("ec.note"));
            item.getItemProperty(myUI.getMessage(Messages.GivenBy)).setValue(result.getString("ec.given_by"));
            item.getItemProperty(myUI.getMessage(Messages.IssueDate)).setValue(Settings.df.format(result.getDate("ec.date_of_issue")));
            item.getItemProperty(myUI.getMessage(Messages.Certificate)).setValue(result.getString("c.name"));
        }
        return container;
    }
}
