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
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.EmployeeSeminar;
import kg.alex.lomonosov.i18n.Messages;
import kg.alex.lomonosov.ui.EmployeeDefinitionView;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeSeminar extends BaseDb {

    public DbEmployeeSeminar() throws Exception {
        super();
    }

    public int exec_insert(EmployeeSeminar es) throws SQLException {
        String sql = "INSERT INTO hr_employee_seminar (employee_id,name,subject,note,date_of_issue) "
                + "VALUES(?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, es.getEmployee_id());
        stat.setString(2, es.getName());
        stat.setString(3, es.getSubject());
        stat.setString(4, es.getNote());
        stat.setDate(5, new Date(es.getDate_of_issue().getTime()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeSeminar es) throws SQLException {
        String sql = "update hr_employee_seminar set "
                + "name = ?, subject = ?, note = ?, date_of_issue = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, es.getName());
        stat.setString(2, es.getSubject());
        stat.setString(3, es.getNote());
        stat.setDate(4, new Date(es.getDate_of_issue().getTime()));
        stat.setInt(5, es.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT es.id, es.name, es.subject, es.note, es.date_of_issue FROM hr_employee_seminar as es "
                + "where es.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareSeminarContainer();
        while (result.next()) {
            String id = result.getString("es.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeSeminar, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                    edv.createTextField(result.getString("es.name"),
                            myUI.getMessage(Messages.Title),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(Messages.Subject)).setValue(
                    edv.createTextField(result.getString("es.subject"),
                            myUI.getMessage(Messages.Subject),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                    edv.createTextField(result.getString("es.note"),
                            myUI.getMessage(Messages.Note),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 200, true), false));
            item.getItemProperty(myUI.getMessage(Messages.IssueDate)).setValue(
                    edv.createDateField(result.getDate("es.date_of_issue"),
                            myUI.getMessage(Messages.IssueDate), null,
                            true, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id) throws SQLException {

        String sql = "SELECT es.id, es.name, es.subject, es.note, es.date_of_issue FROM hr_employee_seminar as es "
                + "where es.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Subject), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.IssueDate), String.class, null);
        while (result.next()) {
            String id = result.getString("es.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(result.getString("es.name"));
            item.getItemProperty(myUI.getMessage(Messages.Subject)).setValue(result.getString("es.subject"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("es.note"));
            item.getItemProperty(myUI.getMessage(Messages.IssueDate)).setValue(Settings.df.format(result.getDate("es.date_of_issue")));
        }
        return container;
    }
}
