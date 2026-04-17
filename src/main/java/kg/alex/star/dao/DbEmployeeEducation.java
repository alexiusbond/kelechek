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
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.domain.Attachment;
import kg.alex.star.domain.Definition;
import kg.alex.star.domain.EmployeeEducation;
import kg.alex.star.i18n.Messages;
import kg.alex.star.ui.EmployeeDefinitionView;
import kg.alex.star.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbEmployeeEducation extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeEducation.class);

    public DbEmployeeEducation() throws Exception {
        super();
    }

    public int exec_insert(EmployeeEducation ed) throws SQLException {
        String sql = "INSERT INTO hr_employee_education "
                + "(employee_id, hr_university_id, hr_own_id, department, start_date, "
                + "end_date, country_id, education_level_id,attachment_id) "
                + "VALUES(?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ed.getEmployee_id());
        stat.setInt(2, ed.getUniversity_id());
        stat.setInt(3, ed.getOwn_id());
        stat.setString(4, ed.getDepartment());
        stat.setString(5, Settings.mysql_only_year.format(ed.getStart()));
        stat.setString(6, Settings.mysql_only_year.format(ed.getEnd()));
        stat.setInt(7, ed.getCountry_id());
        stat.setInt(8, ed.getEducation_level_id());
        if (ed.getAttachment_id() != 0) {
            stat.setInt(9, ed.getAttachment_id());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeEducation ed) throws SQLException {
        String sql = "update hr_employee_education set "
                + "hr_university_id = ?, department = ?, start_date = ?, end_date = ?, country_id = ?, education_level_id = ?, attachment_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ed.getUniversity_id());
        stat.setString(2, ed.getDepartment());
        stat.setString(3, Settings.mysql_only_year.format(ed.getStart()));
        stat.setString(4, Settings.mysql_only_year.format(ed.getEnd()));
        stat.setInt(5, ed.getCountry_id());
        stat.setInt(6, ed.getEducation_level_id());
        if (ed.getAttachment_id() != 0) {
            stat.setInt(7, ed.getAttachment_id());
        } else {
            stat.setNull(7, Types.INTEGER);
        }
        stat.setInt(8, ed.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ed.id, ed.hr_university_id, ed.department, ed.start_date, ed.end_date, ed.country_id, "
                + "ed.education_level_id, a.id, a.name, a.extension, a.unique_name FROM hr_employee_education as ed "
                + "left join attachments as a on a.id = ed.attachment_id "
                + "where ed.employee_id = ? and ed.hr_own_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, own_id);
        ResultSet result = stat.executeQuery();
        final IndexedContainer container = edv.prepareEducationContainer(own_id);
        while (result.next()) {
            String id = result.getString("ed.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeEducation, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Department)).setValue(
                    edv.createTextField(result.getString("ed.department"),
                            myUI.getMessage(Messages.Department),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 250, true), true));
            item.getItemProperty(myUI.getMessage(Messages.Start)).setValue(
                    edv.createDateField(result.getDate("ed.start_date"),
                            myUI.getMessage(Messages.Start), null, true,
                            Settings.yearPattern, Resolution.YEAR));
            item.getItemProperty(myUI.getMessage(Messages.End)).setValue(
                    edv.createDateField(result.getDate("ed.end_date"),
                            myUI.getMessage(Messages.End), null, true,
                            Settings.yearPattern, Resolution.YEAR));
            item.getItemProperty(myUI.getMessage(Messages.Country)).setValue(
                    edv.createCombobox(result.getInt("ed.country_id"),
                            myUI.getMessage(Messages.Country), Settings.dbCountry, true));
            item.getItemProperty(myUI.getMessage(Messages.EduLevel)).setValue(
                    edv.createCombobox(result.getInt("ed.education_level_id"),
                            myUI.getMessage(Messages.EduLevel), Settings.dbEduLevel, true));
            final ComboBox cb = edv.createCombobox(result.getInt("ed.hr_university_id"),
                    myUI.getMessage(Messages.University), Settings.dbUniversityTable, true);
            cb.setNewItemsAllowed(true);
            cb.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbUniversityTable, false);
                    dbd.close();
                    if (id1 != 0) {
                        for (Object next : container.getItemIds()) {
                            Item item1 = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                    myUI.getMessage(Messages.University)).getValue()).getContainerDataSource()).addItem(id1);
                            item1.getItemProperty(myUI.getMessage(Messages.Title)).setValue(newItemCaption);
                            cb.setValue(id1);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            });
            item.getItemProperty(myUI.getMessage(Messages.University)).setValue(cb);

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);

            if (result.getInt("a.id") != 0) {
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
            }
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id) throws SQLException {

        String sql = "SELECT 0 as id, null as country, CASE WHEN gs.id is not null and gs.school_id is null THEN 'Другое' ELSE s.name_ru END as org, " +
                "null as dep, 'Среднее образование' as level, " +
                "CONCAT(YEAR(gs.start_date), '-', YEAR(gs.end_date)) AS period " +
                "FROM hr_employee_grad_school as gs " +
                "left join school as s on s.id = gs.school_id " +
                "where gs.employee_id = ? " +
                "UNION SELECT ed.id as id, c.name as country, u.name as org, ed.department as dep, l.name as level, " +
                "CONCAT(YEAR(ed.start_date), '-', YEAR(ed.end_date)) AS period " +
                "FROM hr_employee_education AS ed " +
                "LEFT JOIN hr_country AS c ON c.id = ed.country_id " +
                "LEFT JOIN hr_university AS u ON u.id = ed.hr_university_id " +
                "LEFT JOIN hr_education_level AS l ON l.id = ed.education_level_id " +
                "WHERE ed.employee_id = ? AND ed.hr_own_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, employee_id);
        stat.setInt(3, own_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.EduLevel), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.EducationalOrganization), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Department), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Country), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Period), String.class, null);
        while (result.next()) {
            if (own_id == 2 && result.getInt("id") == 0) {
                continue;
            }
            String id = result.getString("id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.Department)).setValue(result.getString("dep"));
            item.getItemProperty(myUI.getMessage(Messages.Period)).setValue(result.getString("period"));
            item.getItemProperty(myUI.getMessage(Messages.Country)).setValue(result.getString("country"));
            item.getItemProperty(myUI.getMessage(Messages.EducationalOrganization)).setValue(result.getString("org"));
            item.getItemProperty(myUI.getMessage(Messages.EduLevel)).setValue(result.getString("level"));
        }
        return container;
    }
}
