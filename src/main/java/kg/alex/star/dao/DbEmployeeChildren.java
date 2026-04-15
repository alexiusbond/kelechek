/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.domain.EmployeeChildren;
import kg.alex.star.i18n.Messages;
import kg.alex.star.ui.EmployeeDefinitionView;

import java.sql.*;

public class DbEmployeeChildren extends BaseDb {

    public DbEmployeeChildren() throws Exception {
        super();
    }

    public int exec_insert(EmployeeChildren ech) throws SQLException {
        String sql = "INSERT INTO hr_employee_children (employee_id,fullname,"
                + "date_of_birth,institution,hr_education_status_id,hr_health_status_id) "
                + "VALUES(?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ech.getEmployee_id());
        stat.setString(2, ech.getFullName());
        stat.setDate(3, new Date(ech.getDate_of_birth().getTime()));
        if (!ech.getInstitution().isEmpty() && ech.getInstitution() != null) {
            stat.setString(4, ech.getInstitution());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        if (ech.getEducation_status_id() != 0) {
            stat.setInt(5, ech.getEducation_status_id());
        } else {
            stat.setNull(5, Types.INTEGER);
        }
        stat.setInt(6, ech.getHealth_status_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeChildren ech) throws SQLException {
        String sql = "update hr_employee_children set "
                + "fullname = ?, date_of_birth = ?, institution = ?, hr_education_status_id = ?, hr_health_status_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, ech.getFullName());
        stat.setDate(2, new Date(ech.getDate_of_birth().getTime()));
        if (!ech.getInstitution().isEmpty() && ech.getInstitution() != null) {
            stat.setString(3, ech.getInstitution());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        if (ech.getEducation_status_id() != 0) {
            stat.setInt(4, ech.getEducation_status_id());
        } else {
            stat.setNull(4, Types.INTEGER);
        }
        stat.setInt(5, ech.getHealth_status_id());
        stat.setInt(6, ech.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ech.id, ech.fullname, ech.date_of_birth, ech.institution, " +
                "ech.hr_education_status_id, ech.hr_health_status_id " +
                "FROM hr_employee_children as ech where ech.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareChildrenContainer();
        while (result.next()) {
            String id = result.getString("ech.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeChildren, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.FullName)).setValue(
                    edv.createTextField(result.getString("ech.fullname"),
                            myUI.getMessage(Messages.FullName),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 200, true), true));
            item.getItemProperty(myUI.getMessage(Messages.DateOfBirth)).setValue(
                    edv.createDateField(result.getDate("ech.date_of_birth"),
                            myUI.getMessage(Messages.DateOfBirth), null,
                            true, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(Messages.Institution)).setValue(
                    edv.createTextField(result.getString("ech.institution"),
                            myUI.getMessage(Messages.Institution),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 300, true), false));
            item.getItemProperty(myUI.getMessage(Messages.EducationStatus)).setValue(
                    edv.createCombobox(result.getInt("ech.hr_education_status_id"),
                            myUI.getMessage(Messages.EducationStatus), Settings.dbHrEducationStatus, false));
            item.getItemProperty(myUI.getMessage(Messages.HealthStatus)).setValue(
                    edv.createCombobox(result.getInt("ech.hr_health_status_id"),
                            myUI.getMessage(Messages.HealthStatus), Settings.dbHealthStatus, true));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id) throws SQLException {

        String sql = "SELECT ech.id, ech.fullname, ech.date_of_birth, ech.institution, es.name, h.name " +
                "FROM hr_employee_children AS ech " +
                "LEFT JOIN hr_health_status AS h ON h.id = ech.hr_health_status_id " +
                "LEFT JOIN hr_education_status AS es ON es.id = ech.hr_education_status_id " +
                "WHERE ech.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.DateOfBirth), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Institution), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.EducationStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.HealthStatus), String.class, null);
        while (result.next()) {
            String id = result.getString("ech.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.FullName)).setValue(result.getString("ech.fullname"));
            item.getItemProperty(myUI.getMessage(Messages.DateOfBirth)).setValue(Settings.df.format(result.getDate("ech.date_of_birth")));
            item.getItemProperty(myUI.getMessage(Messages.Institution)).setValue(result.getString("ech.institution"));
            item.getItemProperty(myUI.getMessage(Messages.EducationStatus)).setValue(result.getString("es.name"));
            item.getItemProperty(myUI.getMessage(Messages.HealthStatus)).setValue(result.getString("h.name"));
        }
        return container;
    }
}
