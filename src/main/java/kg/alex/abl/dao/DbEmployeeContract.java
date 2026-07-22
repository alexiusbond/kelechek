/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.domain.EmployeeContract;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.ui.EmployeeDefinitionView;
import kg.alex.abl.utils.Settings;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.*;

public class DbEmployeeContract extends BaseDb {

    public DbEmployeeContract() throws Exception {
        super();
    }

    public int exec_insert(EmployeeContract ec) throws SQLException {
        String sql = "INSERT INTO hr_employee_contract (employee_id, contract_type_id, salary, from_date, " +
                "till_date, creation_date) VALUES(?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getEmployee_id());
        stat.setInt(2, ec.getContract_type_id());
        stat.setString(3, ec.getSalary());
        stat.setDate(4, new Date(ec.getFromDate().getTime()));
        stat.setDate(5, new Date(ec.getTillDate().getTime()));
        stat.setDate(6, new Date(ec.getCreationDate().getTime()));
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeContract ec) throws SQLException {
        String sql = "update hr_employee_contract set "
                + "contract_type_id = ?, salary = ?, from_date = ?, till_date = ?, creation_date = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ec.getContract_type_id());
        stat.setString(2, ec.getSalary());
        stat.setDate(3, new Date(ec.getFromDate().getTime()));
        stat.setDate(4, new Date(ec.getTillDate().getTime()));
        stat.setDate(5, new Date(ec.getCreationDate().getTime()));
        stat.setInt(6, ec.getId());
        return stat.executeUpdate();
    }

    public int exec_update(EmployeeContract ec, int id) throws SQLException {
        String sql = "update hr_employee_contract set " +
                "year_id = ?, probationary_period = ?, working_days = ?, working_hours = ?, " +
                "salary_day = ?, patent_date = ?, patent = ?, equipment = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (ec.getYearId() != 0) {
            stat.setInt(1, ec.getYearId());
        } else {
            stat.setNull(1, Types.INTEGER);
        }
        if (ec.getProbationaryPeriod() != 0) {
            stat.setInt(2, ec.getProbationaryPeriod());
        } else {
            stat.setNull(2, Types.INTEGER);
        }
        if (ec.getWorkingDays() != 0) {
            stat.setInt(3, ec.getWorkingDays());
        } else {
            stat.setNull(3, Types.INTEGER);
        }
        if (ec.getWorkingHours() != 0) {
            stat.setInt(4, ec.getWorkingHours());
        } else {
            stat.setNull(4, Types.INTEGER);
        }
        if (ec.getSalaryDay() != 0) {
            stat.setInt(5, ec.getSalaryDay());
        } else {
            stat.setNull(5, Types.INTEGER);
        }
        if (ec.getPatentDate() != null) {
            stat.setDate(6, new Date(ec.getPatentDate().getTime()));
        } else {
            stat.setNull(6, Types.DATE);
        }
        if (ec.getPatent() != null) {
            stat.setString(7, ec.getPatent());
        } else {
            stat.setNull(7, Types.VARCHAR);
        }
        if (ec.getEquipment() != null) {
            stat.setString(8, ec.getEquipment());
        } else {
            stat.setNull(8, Types.VARCHAR);
        }
        stat.setInt(9, id);
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ec.id, ec.contract_type_id, ec.salary, ec.from_date, ec.till_date, ec.creation_date, " +
                "ec.year_id, ec.probationary_period, ec.salary_day, ec.working_days, ec.working_hours, ec.patent, " +
                "ec.patent_date, ec.equipment, y.name FROM hr_employee_contract as ec " +
                "left join year as y on y.id = ec.year_id where ec.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareContractsContainer();
        while (result.next()) {
            String id = result.getString("ec.id");
            Item item = container.addItem(id);
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.addComponent(edv.createButton(myUI.getMessage(Messages.DeleteButton), id,
                    null, FontAwesome.MINUS_SQUARE));
            PopupButton btn = new PopupButton();
            btn.setDescription(myUI.getMessage(Messages.Print));
            btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btn.addStyleName(ValoTheme.BUTTON_TINY);
            btn.setIcon(FontAwesome.PRINT);
            btn.setData(id);
            btn.addClickListener(edv);
            btn.setContent(edv.getContractExtraInfoLay());
            hl.addComponent(btn);
            item.getItemProperty(Settings.button).setValue(hl);
            item.getItemProperty(myUI.getMessage(Messages.AgreementType)).setValue(
                    edv.createCombobox(result.getInt("ec.contract_type_id"),
                            myUI.getMessage(Messages.AgreementType), Settings.dbContractType, true));
            item.getItemProperty(myUI.getMessage(Messages.SalaryAmount)).setValue(
                    edv.createTextField(result.getString("ec.salary"),
                            myUI.getMessage(Messages.SalaryAmount),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                                    1, 250, false), true));
            item.getItemProperty(myUI.getMessage(Messages.Start)).setValue(edv.createDateField(result.getDate("ec.from_date"),
                    myUI.getMessage(Messages.Start), null, true, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(Messages.CreationDate)).setValue(edv.createDateField(result.getDate("ec.creation_date"),
                    myUI.getMessage(Messages.CreationDate), null, true, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(Messages.End)).setValue(edv.createDateField(result.getDate("ec.till_date"),
                    myUI.getMessage(Messages.End), null, true, Settings.datePattern, Resolution.DAY));
            if (result.getInt("ec.probationary_period") != 0) {
                item.getItemProperty(myUI.getMessage(Messages.ProbationaryPeriod)).setValue(
                        result.getInt("ec.probationary_period"));
            }
            if (result.getInt("ec.year_id") != 0) {
                item.getItemProperty(myUI.getMessage(Messages.AcademicYear)).setValue(
                        result.getInt("ec.year_id"));
                item.getItemProperty(myUI.getMessage(Messages.Year)).setValue(
                        result.getString("y.name"));
            }
            if (result.getInt("ec.salary_day") != 0) {
                item.getItemProperty(myUI.getMessage(Messages.SalaryDay)).setValue(
                        result.getInt("ec.salary_day"));
            }
            if (result.getInt("ec.working_days") != 0) {
                item.getItemProperty(myUI.getMessage(Messages.WorkingDays)).setValue(
                        result.getInt("ec.working_days"));
            }
            if (result.getInt("ec.working_hours") != 0) {
                item.getItemProperty(myUI.getMessage(Messages.WorkingHours)).setValue(
                        result.getInt("ec.working_hours"));
            }
            item.getItemProperty(myUI.getMessage(Messages.Patent)).setValue(
                    result.getString("ec.patent"));
            item.getItemProperty(myUI.getMessage(Messages.Equipment)).setValue(
                    result.getString("ec.equipment"));
            item.getItemProperty(myUI.getMessage(Messages.PatentDate)).setValue(
                    result.getDate("ec.patent_date"));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }
}
