/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.domain.Employee;
import kg.alex.ispa.domain.EmployeesCount;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Map;

public class DbEmployee extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployee.class);

    public DbEmployee() throws Exception {
        super();
    }

    public EmployeesCount execSQL(int scl_id) throws SQLException {
        String sql = "SELECT COUNT(IF(p.id > 2, p.id, NULL)) AS others_count, "
                     + "IFNULL(GROUP_CONCAT(CASE WHEN p.id > 2 THEN CONCAT(e.surname, ' ', e.name) "
                     + "ELSE NULL END ORDER BY p.id ASC SEPARATOR ', '), '') AS others, "
                     + "IFNULL(GROUP_CONCAT(CASE WHEN p.id = 1 THEN CONCAT(e.surname, ' ', e.name) "
                     + "ELSE NULL END), '') AS director, "
                     + "IFNULL(GROUP_CONCAT(CASE WHEN p.id = 2 THEN CONCAT(e.surname, ' ', e.name) "
                     + "ELSE NULL END), '') AS accountent "
                     + "FROM school AS scl "
                     + "LEFT JOIN hr_employee_order AS eo ON scl.id = eo.school_id AND eo.to_date IS NULL "
                     + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                     + "LEFT JOIN hr_position AS hrp ON eo.hr_position_id = hrp.id "
                     + "LEFT JOIN position AS p ON hrp.id = p.hr_position_id "
                     + "LEFT JOIN employee AS e ON eo.employee_id = e.id "
                     + "WHERE eo.school_id = ? AND ord.working_status_id = 2";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, scl_id);
        ResultSet result = stat.executeQuery();
        EmployeesCount e = new EmployeesCount();
        while (result.next()) {
            e.setDirector(result.getString("director"));
            e.setAccountant(result.getString("accountent"));
            e.setOthers(result.getString("others"));
            e.setOthers_count(result.getInt("others_count"));
        }
        return e;
    }

    public float execSQL_completeness(int employee_id) throws SQLException {
        String sql = "SELECT ROUND(IF(e.hr_martial_status_id = 2, (IF(e.photo IS NOT NULL, 1, 0) + IF(extra.id IS NOT NULL, 1, 0) + IF(sp.id IS NOT NULL, 1, 0) " +
                     "+ IF(cont.id IS NOT NULL, 1, 0) + IF(sch.id IS NOT NULL, 1, 0) + IF(cmpl.phones IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.branches IS NOT NULL, 1, 0) + IF(cmpl.education IS NOT NULL, 1, 0) + IF(cmpl.work_places IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.exams IS NOT NULL, 1, 0) + IF(cmpl.seminars IS NOT NULL, 1, 0) + IF(cmpl.certificates IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.languages IS NOT NULL, 1, 0) + IF(cmpl.spouse_education IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.spouse_work_places IS NOT NULL, 1, 0) + IF(cmpl.children IS NOT NULL, 1, 0)) / 16, " +
                     "(IF(e.photo IS NOT NULL, 1, 0) + IF(extra.id IS NOT NULL, 1, 0) + IF(cont.id IS NOT NULL, 1, 0) + IF(sch.id IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.phones IS NOT NULL, 1, 0) + IF(cmpl.branches IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.education IS NOT NULL, 1, 0) + IF(cmpl.work_places IS NOT NULL, 1, 0) + IF(cmpl.exams IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.seminars IS NOT NULL, 1, 0) + IF(cmpl.certificates IS NOT NULL, 1, 0) + IF(cmpl.languages IS NOT NULL, 1, 0) " +
                     "+ IF(cmpl.children IS NOT NULL, 1, 0)) / 13), 2) AS points FROM employee AS e " +
                     "LEFT JOIN hr_employee_completeness AS cmpl ON cmpl.employee_id = e.id " +
                     "LEFT JOIN hr_employee_contacts AS cont ON cont.employee_id = e.id " +
                     "LEFT JOIN hr_employee_grad_school AS sch ON sch.employee_id = e.id " +
                     "LEFT JOIN hr_employee_spouse AS sp ON sp.employee_id = e.id " +
                     "LEFT JOIN hr_employee_extra_info AS extra ON extra.employee_id = e.id where e.id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getFloat("points");
        }
        return 0.0f;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id, String working_statuses,
                                    IndexedContainer workingStatCont, boolean withAdmin,
                                    boolean withHr, int branch_id, int employee_id) throws SQLException {

        if (working_statuses.isEmpty()) {
            working_statuses = "-1";
        }
        String sql = "SELECT e.id, e.login, e.name, e.surname, e.middle_name, e.date_of_birth, e.photo, e.can_advisor, "
                     + "g.id, g.name, n.id, n.name, m.id, m.name, ws.name, ws.id, p.name, p.id, cntr.name, cntr.id, ord.visible_hr_orders, "
                     + "group_concat(DISTINCT p2.id ORDER BY eo2.id ASC separator ', ') as extra_position_ids, "
                     + "group_concat(DISTINCT p2.name ORDER BY eo2.id ASC separator ', ') as extra_positions, cat.id, "
                     + "group_concat(DISTINCT if(eb.hr_importance_id = 1, br.name, null) ORDER BY eb.id ASC separator ', ') as main_branch, "
                     + "group_concat(DISTINCT if(eb.hr_importance_id = 2, br.name, null) ORDER BY eb.id ASC separator ', ') as extra_branches, "
                     + "group_concat(DISTINCT up.permissions separator ';') as permissions, ebh.hours, ebh.extra, eo3.id, cat.parent_id "
                     + "FROM employee AS e LEFT JOIN gender AS g ON g.id = e.gender_id "
                     + "LEFT JOIN nationality AS n ON n.id = e.nationality_id "
                     + "LEFT JOIN hr_country AS cntr ON cntr.id = e.hr_country_id "
                     + "LEFT JOIN hr_martial_status AS m ON m.id = e.hr_martial_status_id "
                     + "LEFT JOIN hr_employee_branch AS eb ON eb.employee_id = e.id "
                     + "LEFT JOIN hr_branch AS br ON br.id = eb.hr_branch_id "
                     + "LEFT JOIN "
                     + "(select sum(hours) as hours, sum(extra_hours) as extra, employee_id as e_id, year_id as y_id, school_id as sch_id "
                     + "from hr_employee_branch_hours group by employee_id, year_id, school_id) AS ebh ON ebh.e_id = e.id and ebh.y_id = ? and ebh.sch_id = ? "
                     + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                     + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                     + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                     + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 and (eo2.to_date IS NULL or eo2.to_date>=NOW()) "
                     + "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                     + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                     + "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                     + "LEFT JOIN hr_employee_order AS eo3 ON eo3.employee_id = e.id AND eo3.from_to_school_id = ? AND eo3.hr_orders_id = 8 "
                     + "left join user_permission as up on up.role_name = e.login "
                     + "LEFT JOIN acc_category as cat ON cat.employee_id = e.id and cat.school_id = eo.school_id "
                     + "WHERE eo.school_id = ? ";
        if (!withAdmin) {
            sql += " and (pos.id IS NULL or pos.id != 5) ";
        } else if (!withAdmin && !withHr) {
            sql += " and (pos.id IS NULL or (pos.id != 5 and pos.id !=25) ";
        }
        if (employee_id != 0) {
            sql += " and e.id= " + employee_id + " ";
        } else {
            sql += " AND ord.working_status_id IS NOT NULL and ws.id in (" + working_statuses + ") ";
            if (branch_id != 0) {
                sql += " AND eb.hr_branch_id = ?";
            }
        }
        sql += " group by e.id order by e.id DESC ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, school_id);
        stat.setInt(3, school_id);
        stat.setInt(4, school_id);
        if (employee_id == 0 && branch_id != 0) {
            stat.setInt(5, branch_id);
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MiddleName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.DateOfBirth), Date.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Photo), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MainPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MainBranch), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ExtraPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Hours), Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.ExtraHours), Integer.class, 0);
        container.addContainerProperty(myUi.getMessage(Messages.WorkingStatus), String.class, null);
        container.addContainerProperty(Settings.gender_id, Integer.class, 0);
        container.addContainerProperty(Settings.nationality_id, Integer.class, 0);
        container.addContainerProperty(Settings.citizenship_id, Integer.class, 0);
        container.addContainerProperty(Settings.martial_status_id, Integer.class, 0);
        container.addContainerProperty(Settings.working_status_id, Integer.class, 0);
        container.addContainerProperty(Settings.position_id, Integer.class, 0);
        container.addContainerProperty(Settings.extra_position_ids, String.class, null);
        container.addContainerProperty(Settings.salary_category_id, Integer.class, 0);
        container.addContainerProperty(Settings.acc_category_id, Integer.class, 0);
        container.addContainerProperty(Settings.is_modifiable, Boolean.class, true);
        container.addContainerProperty(Settings.visible_hr_orders, String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Permissions), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.CanBeAdvisor), Boolean.class, null);
        container.addContainerProperty(Settings.id, Integer.class, 0);

        for (Object next : workingStatCont.getItemIds()) {
            workingStatCont.getContainerProperty(next, Settings.count).setValue(0);
        }
        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(Settings.id).setValue(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(Messages.Id)).setValue(
                    result.getString("e.login"));
            item.getItemProperty(myUi.getMessage(Messages.FirstName)).setValue(
                    result.getString("e.name"));
            item.getItemProperty(myUi.getMessage(Messages.LastName)).setValue(
                    result.getString("e.surname"));
            item.getItemProperty(myUi.getMessage(Messages.MiddleName)).setValue(
                    result.getString("e.middle_name"));
            item.getItemProperty(myUi.getMessage(Messages.DateOfBirth)).setValue(
                    result.getDate("e.date_of_birth"));
            item.getItemProperty(myUi.getMessage(Messages.Photo)).setValue(
                    result.getString("e.photo"));
            item.getItemProperty(myUi.getMessage(Messages.MainPosition)).setValue(
                    result.getString("p.name"));
            item.getItemProperty(myUi.getMessage(Messages.MainBranch)).setValue(
                    result.getString("main_branch"));
            item.getItemProperty(myUi.getMessage(Messages.WorkingStatus)).setValue(
                    result.getString("ws.name"));
            item.getItemProperty(myUi.getMessage(Messages.ExtraPosition)).setValue(
                    result.getString("extra_positions"));
            item.getItemProperty(Settings.extra_position_ids).setValue(
                    result.getString("extra_position_ids"));
            item.getItemProperty(myUi.getMessage(Messages.ExtraBranches)).setValue(
                    result.getString("extra_branches"));
            item.getItemProperty(myUi.getMessage(Messages.Hours)).setValue(
                    result.getInt("ebh.hours"));
            item.getItemProperty(myUi.getMessage(Messages.ExtraHours)).setValue(
                    result.getInt("ebh.extra"));
            item.getItemProperty(Settings.gender_id).setValue(
                    result.getInt("g.id"));
            item.getItemProperty(Settings.nationality_id).setValue(
                    result.getInt("n.id"));
            item.getItemProperty(Settings.citizenship_id).setValue(
                    result.getInt("cntr.id"));
            item.getItemProperty(Settings.martial_status_id).setValue(
                    result.getInt("m.id"));
            item.getItemProperty(Settings.working_status_id).setValue(result.getInt("ws.id"));
            if (result.getInt("ws.id") == 3 && result.getInt("eo3.id") != 0) {
                item.getItemProperty(Settings.is_modifiable).setValue(false);
            }
            item.getItemProperty(myUi.getMessage(Messages.CanBeAdvisor)).setValue(
                    result.getBoolean("e.can_advisor"));
            item.getItemProperty(Settings.visible_hr_orders).setValue(result.getString("ord.visible_hr_orders"));
            item.getItemProperty(myUi.getMessage(Messages.Permissions)).setValue(result.getString("permissions"));
            item.getItemProperty(Settings.position_id).setValue(result.getInt("p.id"));
            item.getItemProperty(Settings.salary_category_id).setValue(result.getInt("cat.parent_id"));
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("cat.id"));
            workingStatCont.getContainerProperty(result.getInt("ws.id"), Settings.count).setValue(
                    (Integer) workingStatCont.getContainerProperty(result.getInt("ws.id"), Settings.count).getValue() + 1);
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id,
                                    boolean withAdmin, boolean withHr) throws SQLException {
        String sql = "SELECT e.id, CONCAT(e.surname, ' ', e.name, ' ', IFNULL(e.middle_name, '')) AS fullname, p.name, "
                     + "group_concat(DISTINCT p2.name ORDER BY eo2.id ASC separator ', ') as extra_positions, "
                     + "group_concat(DISTINCT if(eb.hr_importance_id = 1, br.name, null) ORDER BY eb.id ASC separator ', ') as main_branch, "
                     + "group_concat(DISTINCT if(eb.hr_importance_id = 2, br.name, null) ORDER BY eb.id ASC separator ', ') as extra_branches, "
                     + "ebh.hours, ebh.extra "
                     + "FROM employee AS e "
                     + "LEFT JOIN hr_employee_branch AS eb ON eb.employee_id = e.id "
                     + "LEFT JOIN hr_branch AS br ON br.id = eb.hr_branch_id "
                     + "LEFT JOIN "
                     + "(select sum(hours) as hours, sum(extra_hours) as extra, employee_id as e_id, year_id as y_id, school_id as sch_id "
                     + "from hr_employee_branch_hours group by employee_id, year_id, school_id) AS ebh ON ebh.e_id = e.id "
                     + "and ebh.y_id = ? and ebh.sch_id = ? "
                     + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                     + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                     + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                     + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 and (eo2.to_date IS NULL or eo2.to_date>=NOW()) "
                     + "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                     + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                     + "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                     + "WHERE eo.school_id = ? ";
        if (!withAdmin) {
            sql += " and (pos.id IS NULL or pos.id != 5) ";
        } else if (!withAdmin && !withHr) {
            sql += " and (pos.id IS NULL or (pos.id != 5 and pos.id !=25) ";
        }
        sql += " AND ord.working_status_id IS NOT NULL and ws.id in (2,5) group by e.id order by p.hr_position_category_id, p.id DESC ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, school_id);
        stat.setInt(3, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.FullName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MainPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ExtraPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MainBranch), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.TotalHours) + myUi.getUser().getCurrent_year().getName(), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(Messages.FullName)).setValue(
                    result.getString("fullname"));
            item.getItemProperty(myUi.getMessage(Messages.MainPosition)).setValue(
                    result.getString("p.name"));
            item.getItemProperty(myUi.getMessage(Messages.MainBranch)).setValue(
                    result.getString("main_branch"));
            item.getItemProperty(myUi.getMessage(Messages.ExtraPosition)).setValue(
                    result.getString("extra_positions"));
            item.getItemProperty(myUi.getMessage(Messages.ExtraBranches)).setValue(
                    result.getString("extra_branches"));
            item.getItemProperty(myUi.getMessage(Messages.TotalHours) + myUi.getUser().getCurrent_year().getName()).setValue(
                    result.getInt("ebh.hours") + " (" + result.getInt("ebh.extra") + ")");
        }
        return container;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUi, int except_empl_id, int school_id,
                                            boolean withAdmin, boolean withHr) throws SQLException {
        String sql = "SELECT e.id, CONCAT(e.name, ' ', e.surname) AS fullname, p.name, "
                     + "group_concat(DISTINCT p2.name ORDER BY eo2.id ASC separator ', ') as extra_positions "
                     + "FROM employee AS e "
                     + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                     + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                     + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                     + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 and (eo2.to_date IS NULL or eo2.to_date>=NOW()) "
                     + "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                     + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                     + "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                     + "WHERE eo.school_id = ? and e.id != ? ";
        if (!withAdmin) {
            sql += " and (pos.id IS NULL or pos.id != 5) ";
        } else if (!withAdmin && !withHr) {
            sql += " and (pos.id IS NULL or (pos.id != 5 and pos.id !=25) ";
        }
        sql += " AND ord.working_status_id IS NOT NULL and ws.id in (2,5) group by e.id order by e.name, e.surname ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, except_empl_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("fullname") + " - " + result.getString("p.name"));
            if (result.getString("extra_positions") != null) {
                item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                        item.getItemProperty(myUi.getMessage(Messages.Title)).getValue()
                        + ", " + result.getString("extra_positions"));
            }
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id, int position_id) throws SQLException {
        String sql = "SELECT e.id, CONCAT(e.surname, ' ', e.name, ' ', IFNULL(e.middle_name, '')) AS fullname FROM employee AS e "
                     + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                     + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                     + "LEFT JOIN position AS pos ON p.id = pos.hr_position_id "
                     + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 and (eo2.to_date IS NULL or eo2.to_date>=NOW()) "
                     + "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                     + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                     + "WHERE eo.school_id = ? ";
        if (position_id != 0) {
            sql += " and (p2.id = " + position_id + " or pos.id = " + position_id + ") ";
        }
        sql += " AND ord.working_status_id IS NOT NULL and ord.working_status_id in (2,5) group by e.id order by p.hr_position_category_id, p.id DESC ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(
                    result.getString("fullname"));
        }
        return container;
    }

    public String execSQL_by_id(int id) throws SQLException {
        String sql = "SELECT e.id, CONCAT(e.surname, ' ', e.name, ' ', IFNULL(e.middle_name, '')) AS fullname FROM employee AS e WHERE e.id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getString("fullname");
        }
        return null;
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int school_id) throws SQLException {

        String sql = "SELECT e.id, e.login, e.name, e.surname, e.photo, p.id, p.name, eo.from_date, eo.note, ebh.lessons, ebh.hours, ebh.extra, "
                     + "es.fullname, GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 1, br.name, NULL) ORDER BY eb.id ASC SEPARATOR ', ') AS main_branch, "
                     + "GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 2, br.name, NULL) ORDER BY eb.id ASC SEPARATOR ', ') AS extra_branches, "
                     + "GROUP_CONCAT(DISTINCT p2.name ORDER BY eo2.id ASC SEPARATOR ', ') AS extra_positions, "
                     + "GROUP_CONCAT(DISTINCT CONCAT(wp.name, ' (', p3.name, ')') ORDER BY ew.id ASC SEPARATOR ', ') AS spouse_work, "
                     + "GROUP_CONCAT(DISTINCT CONCAT(ech.fullname, ' ', (YEAR(NOW()) - YEAR(ech.date_of_birth)), ' y/o', IF(ech.hr_education_status_id = 1, "
                     + "CONCAT(' (', ech.institution, ')'), '')) ORDER BY ech.id ASC SEPARATOR ', ') AS children, cat.id "
                     + "FROM employee AS e "
                     + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                     + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                     + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 "
                     + "AND (eo2.to_date IS NULL OR eo2.to_date >= NOW()) LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                     + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                     + "LEFT JOIN hr_employee_branch AS eb ON e.id = eb.employee_id LEFT JOIN hr_branch AS br ON br.id = eb.hr_branch_id "
                     + "LEFT JOIN (SELECT t1.e_id AS e_id, t1.y_id AS y_id, t1.sch_id AS sch_id, SUM(t1.hours) AS hours, SUM(t1.extra) AS extra,"
                     + " GROUP_CONCAT(DISTINCT t1.lessons SEPARATOR ', ') AS lessons FROM (SELECT CONCAT(br.name, ' - ', "
                     + "SUM(el.hours), ' (', SUM(el.extra_hours), ')') AS lessons, el.employee_id AS e_id, el.year_id AS y_id, el.school_id "
                     + "AS sch_id, SUM(el.hours) AS hours, SUM(el.extra_hours) AS extra FROM hr_employee_branch_hours AS el "
                     + "LEFT JOIN hr_branch AS br ON br.id = el.hr_branch_id GROUP BY el.employee_id, el.hr_branch_id, el.year_id, el.school_id) AS t1 "
                     + "GROUP BY t1.e_id, t1.y_id, t1.sch_id) AS ebh ON ebh.e_id = e.id AND ebh.y_id = ? AND ebh.sch_id = ? "
                     + "LEFT JOIN hr_employee_spouse AS es ON es.employee_id = e.id "
                     + "LEFT JOIN hr_employee_work AS ew ON ew.employee_id = e.id AND ew.hr_own_id = 2 AND ew.working_status_id = 2 "
                     + "LEFT JOIN hr_work_place AS wp ON ew.hr_work_place_id = wp.id "
                     + "LEFT JOIN hr_position AS p3 ON ew.position_id = p3.id "
                     + "LEFT JOIN hr_employee_children AS ech ON ech.employee_id = e.id "
                     + "LEFT JOIN acc_category as cat ON cat.employee_id = e.id and cat.school_id = eo.school_id "
                     + "WHERE eo.school_id = ? "
                     + "AND ord.working_status_id IS NOT NULL AND ws.id = 2 GROUP BY e.id ORDER BY eo.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUi.getUser().getCurrent_year().getId());
        stat.setInt(2, school_id);
        stat.setInt(3, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Id), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MainPosition), ComboBox.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.FromDate), DateField.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Note), TextField.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Position), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ExtraPosition), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.MainBranch), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Lessons), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.SpouseInfo), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Children), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Photo), String.class, null);
        container.addContainerProperty(Settings.crud_status, String.class, null);
        container.addContainerProperty(Settings.school_id, Integer.class, school_id);
        container.addContainerProperty(Settings.position_id, Integer.class, 0);
        container.addContainerProperty(Settings.acc_category_id, Integer.class, 0);

        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(myUi.getMessage(Messages.Id)).setValue(
                    result.getString("e.login"));
            item.getItemProperty(myUi.getMessage(Messages.FirstName)).setValue(
                    result.getString("e.name"));
            item.getItemProperty(myUi.getMessage(Messages.LastName)).setValue(
                    result.getString("e.surname"));
            item.getItemProperty(myUi.getMessage(Messages.Position)).setValue(
                    result.getString("p.name"));
            item.getItemProperty(myUi.getMessage(Messages.ExtraPosition)).setValue(
                    result.getString("extra_positions"));
            item.getItemProperty(myUi.getMessage(Messages.MainBranch)).setValue(
                    result.getString("main_branch"));
            item.getItemProperty(myUi.getMessage(Messages.ExtraBranches)).setValue(
                    result.getString("extra_branches"));
            if (result.getString("ebh.lessons") != null) {
                item.getItemProperty(myUi.getMessage(Messages.Lessons)).setValue(
                        result.getString("ebh.lessons") + "; " + myUi.getMessage(Messages.Total) + " - "
                        + result.getInt("ebh.hours") + "(" + result.getInt("ebh.extra") + ")");
            }
            item.getItemProperty(myUi.getMessage(Messages.SpouseInfo)).setValue(
                    result.getString("es.fullname"));
            if (result.getString("es.fullname") != null) {
                item.getItemProperty(myUi.getMessage(Messages.SpouseInfo)).setValue(
                        result.getString("es.fullname") + " - " + result.getString("spouse_work"));
            }
            item.getItemProperty(myUi.getMessage(Messages.Children)).setValue(
                    result.getString("children"));
            item.getItemProperty(myUi.getMessage(Messages.Photo)).setValue(
                    result.getString("e.photo"));
            ComboBox cb = new ComboBox();
            cb.setDescription(myUi.getMessage(Messages.MainPosition));
            cb.setStyleName(ValoTheme.COMBOBOX_TINY);
            cb.setWidth(Settings.PERCENTS100);
            cb.setItemCaptionPropertyId(myUi.getMessage(Messages.Title));
            cb.setFilteringMode(FilteringMode.CONTAINS);
            cb.setRequired(true);
            cb.setRequiredError(myUi.getMessage(Messages.RequiredField));
            try {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUi, Settings.hr_positionTable, true));
                dbp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setNullSelectionAllowed(false);
            cb.setValue(result.getInt("p.id"));
            cb.setEnabled(false);
            item.getItemProperty(myUi.getMessage(Messages.MainPosition)).setValue(cb);

            DateField df = new DateField();
            df.setWidth(Settings.PERCENTS100);
            df.setStyleName(ValoTheme.DATEFIELD_TINY);
            df.setRequired(true);
            df.setRequiredError(myUi.getMessage(Messages.RequiredField));
            df.setDateFormat(Settings.datePattern);
            df.setDescription(myUi.getMessage(Messages.FromDate));
            df.setValue(result.getDate("eo.from_date"));
            df.setRangeEnd(new Date());
            df.setEnabled(false);
            item.getItemProperty(myUi.getMessage(Messages.FromDate)).setValue(df);

            TextField tf = new TextField();
            tf.setDescription(myUi.getMessage(Messages.Note));
            tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
            tf.setWidth(Settings.PERCENTS100);
            tf.addValidator(new StringLengthValidator(myUi.getMessage(Messages.NotificationWrongValue), null, 300, true));
            if (result.getString("eo.note") != null) {
                tf.setValue(result.getString("eo.note"));
            }
            tf.setEnabled(false);
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(tf);
            item.getItemProperty(Settings.position_id).setValue(result.getInt("p.id"));
            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("cat.id"));
        }
        return container;
    }

    public int exec_insert(Employee employee) throws SQLException {
        String sql = "INSERT ignore INTO employee (login, password, name, "
                     + "surname, middle_name, date_of_birth, photo, gender_id, "
                     + "hr_martial_status_id,nationality_id,employee_id,modification_date,hr_country_id) "
                     + "VALUES(?,?,?,?,?,?,?,?,?,?,?,NOW(),?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, employee.getLogin());
        stat.setString(2, employee.getPassword());
        stat.setString(3, employee.getName().trim());
        stat.setString(4, employee.getSurname().trim());
        if (employee.getMiddle_name() != null) {
            stat.setString(5, employee.getMiddle_name().trim());
        } else {
            stat.setNull(5, Types.VARCHAR);
        }
        stat.setDate(6, new java.sql.Date(employee.getBirth_date().getTime()));
        stat.setString(7, employee.getPhoto());
        stat.setInt(8, employee.getGender_id());
        stat.setInt(9, employee.getMartial_status_id());
        stat.setInt(10, employee.getNationality_id());
        stat.setInt(11, employee.getModified_by_id());
        stat.setInt(12, employee.getCitizenship_id());

        int st = stat.executeUpdate();
        if (st != 0) {
            int employee_id = getLastInsertedId();
            try {
                DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
                dbCon.connect();
                dbCon.exec_insert(employee_id);
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            return employee_id;
        } else {
            return 0;
        }
    }

    public int exec_update(Employee e, String pass) throws SQLException {
        String sql = "UPDATE employee SET login = ?, name = ?, surname = ?, middle_name = ?, ";
        if (pass != null && !pass.isEmpty()) {
            sql += ("password='" + pass + "',");
        }
        sql += "date_of_birth = ?, nationality_id = ?, gender_id = ?, hr_martial_status_id = ?, employee_id = ?, " +
               "photo = ?, modification_date = NOW(), hr_country_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, e.getLogin());
        stat.setString(2, e.getName().trim());
        stat.setString(3, e.getSurname().trim());
        if (e.getMiddle_name() != null) {
            stat.setString(4, e.getMiddle_name().trim());
        } else {
            stat.setNull(4, Types.VARCHAR);
        }
        stat.setDate(5, new java.sql.Date(e.getBirth_date().getTime()));
        stat.setInt(6, e.getNationality_id());
        stat.setInt(7, e.getGender_id());
        stat.setInt(8, e.getMartial_status_id());
        stat.setInt(9, e.getModified_by_id());
        stat.setString(10, e.getPhoto());
        stat.setInt(11, e.getCitizenship_id());
        stat.setInt(12, e.getId());
        return stat.executeUpdate();
    }

    public int exec_update(int id, boolean canBeAdvisor) throws SQLException {
        String sql = "UPDATE employee SET can_advisor = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setBoolean(1, canBeAdvisor);
        stat.setInt(2, id);
        return stat.executeUpdate();
    }

    public int exec_delete_perm(String role_name) throws SQLException {
        String sql = "DELETE FROM user_permission WHERE role_name = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, role_name);
        return stat.executeUpdate();
    }

    public int exec_delete_role(String login) throws SQLException {
        String sql = "DELETE FROM user_roles WHERE login = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        return stat.executeUpdate();
    }

    public int exec_insert_perm(String login, String perm) throws SQLException {
        String sql = "INSERT IGNORE INTO user_permission (role_name,permissions) "
                     + "VALUES(?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, login);
        stat.setString(2, perm);
        return stat.executeUpdate();
    }

    public int exec_insert_role(String loginRName, String roleName) throws SQLException {
        String sql = "INSERT IGNORE INTO user_roles (login,role_name) "
                     + "VALUES(?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, loginRName);
        stat.setString(2, roleName);
        return stat.executeUpdate();
    }

    public int exec_update_role(String oldLogin, String newLogin, String roleName)
            throws SQLException {
        String sql = "UPDATE user_roles SET login = ?, role_name = ? WHERE login = ? ";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, newLogin);
        stat.setString(2, roleName);
        stat.setString(3, oldLogin);
        return stat.executeUpdate();
    }

    public int exec_update_perm(String oldroleName, String newroleName)
            throws SQLException {
        String sql = "UPDATE user_permission SET role_name = ? WHERE role_name = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, newroleName);
        stat.setString(2, oldroleName);
        return stat.executeUpdate();
    }

    public int execUpdateYear(int year_id, int id) throws SQLException {
        String sql = "UPDATE employee SET year_id = ? where id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, id);
        int st = stat.executeUpdate();
        return st;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int year_id, Map<String, String> params) throws SQLException {

        String sql = "SELECT e.id, e.login, e.photo, e.name, e.surname, e.middle_name, e.date_of_birth, " +
                     "year(NOW()) - year(e.date_of_birth) as age, e.can_advisor, g.name, n.name, " +
                     "m.name, ws.name, p.name, GROUP_CONCAT(DISTINCT p2.name ORDER BY eo2.id ASC SEPARATOR ', ') AS extra_positions, " +
                     "GROUP_CONCAT(DISTINCT ph.number ORDER BY ph.id ASC SEPARATOR ', ') AS phones, mbr.name  AS main_branch, " +
                     "GROUP_CONCAT(DISTINCT ebr.name ORDER BY eeb.id ASC SEPARATOR ', ') AS extra_branches, " +
                     "GROUP_CONCAT(DISTINCT concat(ex.name, ' - ', ee.score) ORDER BY ex.id ASC SEPARATOR ', ') AS exams, " +
                     "GROUP_CONCAT(DISTINCT concat(uni.name, ' - ', el.name) ORDER BY edu.id ASC SEPARATOR ', ') AS education, " +
                     "GROUP_CONCAT(DISTINCT concat(wp.name, ' - ', wpos.name) ORDER BY ew.id ASC SEPARATOR ', ') AS work, " +
                     "(SELECT GROUP_CONCAT(DISTINCT sem.name ORDER BY sem.id ASC SEPARATOR ', ') FROM hr_employee_seminar AS sem " +
                     "WHERE sem.employee_id = e.id) AS sems, " +
                     "GROUP_CONCAT(DISTINCT cert.name ORDER BY ec.id ASC SEPARATOR ', ') AS certs, " +
                     "GROUP_CONCAT(DISTINCT concat(lang.name, ' - ', lev.name) ORDER BY elan.id ASC SEPARATOR ', ') AS langs, " +
                     "ebh.hours, ebh.extra, concat(sch.code, ' - ', sch.name_ru) as school, sch2.name_ru as grad_school, " +
                     "sal.name, cntr.name, hs.name FROM employee AS e " +
                     "LEFT JOIN gender AS g ON g.id = e.gender_id " +
                     "LEFT JOIN nationality AS n ON n.id = e.nationality_id " +
                     "LEFT JOIN hr_country AS cntr ON cntr.id = e.hr_country_id " +
                     "LEFT JOIN hr_martial_status AS m ON m.id = e.hr_martial_status_id " +
                     "LEFT JOIN hr_employee_branch AS meb ON meb.employee_id = e.id AND meb.hr_importance_id = 1 " +
                     "LEFT JOIN hr_branch AS mbr ON mbr.id = meb.hr_branch_id " +
                     "LEFT JOIN hr_employee_branch AS eeb ON eeb.employee_id = e.id AND eeb.hr_importance_id = 2 " +
                     "LEFT JOIN hr_branch AS ebr ON ebr.id = eeb.hr_branch_id " +
                     "LEFT JOIN hr_employee_education AS edu ON edu.employee_id = e.id AND edu.hr_own_id = 1 " +
                     "LEFT JOIN hr_university AS uni ON uni.id = edu.hr_university_id " +
                     "LEFT JOIN hr_education_level AS el ON el.id = edu.education_level_id " +
                     "LEFT JOIN hr_employee_work AS ew ON ew.employee_id = e.id AND ew.hr_own_id = 1 " +
                     "LEFT JOIN hr_work_place AS wp ON wp.id = ew.hr_work_place_id " +
                     "LEFT JOIN hr_position AS wpos ON wpos.id = ew.position_id " +
                     "LEFT JOIN hr_employee_order AS eo ON eo.id = (SELECT MAX(eo.id) FROM hr_employee_order as eo " +
                     "LEFT JOIN hr_orders as o on o.id = eo.hr_orders_id WHERE eo.employee_id = e.id " +
                     "AND eo.to_date IS NULL AND o.working_status_id IS NOT NULL ";
        if (params.get(myUI.getMessage(Messages.Schools)) != null) {
            sql += "AND school_id IN (" + params.get(myUI.getMessage(Messages.Schools)) + ")";
        }
        sql += ") " +
               "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id " +
               "LEFT JOIN position AS pos ON p.id = pos.hr_position_id " +
               "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 " +
               "AND (eo2.to_date IS NULL OR eo2.to_date >= NOW()) " +
               "LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id " +
               "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id " +
               "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id " +
               "LEFT JOIN (SELECT SUM(hours) AS hours, SUM(extra_hours) AS extra, employee_id AS e_id, year_id AS y_id, " +
               "school_id AS sch_id FROM hr_employee_branch_hours GROUP BY employee_id, year_id, school_id) AS ebh " +
               "ON ebh.e_id = e.id AND ebh.y_id = ? AND ebh.sch_id = eo.school_id " +
               "LEFT JOIN school AS sch ON sch.id = eo.school_id " +
               "LEFT JOIN acc_category AS cat ON e.id = cat.employee_id and cat.school_id = eo.school_id " +
               "LEFT JOIN acc_category AS cat2 ON cat.parent_id = cat2.id " +
               "LEFT JOIN hr_salary_category AS sal ON cat2.parent_id = sal.acc_category_id " +
               "LEFT JOIN hr_employee_grad_school AS gr_sh ON gr_sh.employee_id = e.id " +
               "LEFT JOIN school AS sch2 ON gr_sh.school_id = sch2.id " +
               "LEFT JOIN hr_employee_extra_info AS ei ON ei.employee_id = e.id " +
               "LEFT JOIN hr_health_status AS hs ON ei.hr_health_status_id = hs.id " +
               "LEFT JOIN hr_employee_exam AS ee ON ee.employee_id = e.id " +
               "LEFT JOIN hr_exam AS ex ON ee.hr_exam_id = ex.id " +
               "LEFT JOIN hr_employee_certificate AS ec ON ec.employee_id = e.id " +
               "LEFT JOIN hr_certificate AS cert ON ec.certificate_id = cert.id " +
               "LEFT JOIN hr_employee_language AS elan ON elan.employee_id = e.id " +
               "LEFT JOIN hr_language AS lang ON elan.hr_language_id = lang.id " +
               "LEFT JOIN hr_language_level AS lev ON elan.hr_language_level_id = lev.id " +
               "LEFT JOIN hr_employee_phone_number AS ph ON ph.employee_id = e.id " +
               "WHERE 1 ";
        if (params.get(myUI.getMessage(Messages.Schools)) != null) {
            sql += "AND eo.school_id IN (" + params.get(myUI.getMessage(Messages.Schools)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.WorkingStatuses)) != null) {
            sql +=
                    "AND ws.id IN (" + params.get(myUI.getMessage(Messages.WorkingStatuses)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Positions)) != null) {
            sql += "AND p.id IN (" + params.get(myUI.getMessage(Messages.Positions)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Genders)) != null) {
            sql += "AND g.id IN (" + params.get(myUI.getMessage(Messages.Genders)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Nationalities)) != null) {
            sql += "AND n.id IN (" + params.get(myUI.getMessage(Messages.Nationalities)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.MartialStatuses)) != null) {
            sql += "AND m.id IN (" + params.get(myUI.getMessage(Messages.MartialStatuses)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.CanBeAdvisors)) != null) {
            sql += "AND e.can_advisor IN (" + params.get(myUI.getMessage(Messages.CanBeAdvisors)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.ContractTypes)) != null) {
            sql += "AND sal.id IN (" + params.get(myUI.getMessage(Messages.ContractTypes)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Citizenships)) != null) {
            sql += "AND cntr.id IN (" + params.get(myUI.getMessage(Messages.Citizenships)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.ExtraPositions)) != null) {
            sql += "AND p2.id IN (" + params.get(myUI.getMessage(Messages.ExtraPositions)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Education)) != null) {
            sql += "AND uni.id IN (" + params.get(myUI.getMessage(Messages.Education)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.WorkPlaces)) != null) {
            sql += "AND wp.id IN (" + params.get(myUI.getMessage(Messages.WorkPlaces)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Certificates)) != null) {
            sql += "AND cert.id IN (" + params.get(myUI.getMessage(Messages.Certificates)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Languages)) != null) {
            sql += "AND lang.id IN (" + params.get(myUI.getMessage(Messages.Languages)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.GraduationSchools)) != null) {
            if (params.get(myUI.getMessage(Messages.GraduationSchools)).contains("-1")) {
                sql += "AND (sch2.id IN (" + params.get(myUI.getMessage(Messages.GraduationSchools)) + ") OR sch2.id IS NULL) ";
            } else {
                sql += "AND sch2.id IN (" + params.get(myUI.getMessage(Messages.GraduationSchools)) + ") ";
            }
        }
        if (params.get(myUI.getMessage(Messages.HealthStatuses)) != null) {
            sql += "AND hs.id IN (" + params.get(myUI.getMessage(Messages.HealthStatuses)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.Exams)) != null) {
            sql += "AND ex.id IN (" + params.get(myUI.getMessage(Messages.Exams)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.MainBranches)) != null) {
            sql += "AND mbr.id IN (" + params.get(myUI.getMessage(Messages.MainBranches)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.ExtraBranches)) != null) {
            sql += "AND ebr.id IN (" + params.get(myUI.getMessage(Messages.ExtraBranches)) + ") ";
        }
        if (params.get(myUI.getMessage(Messages.FirstName)) != null
            && !params.get(myUI.getMessage(Messages.FirstName)).isEmpty()) {
            sql += "AND e.name LIKE  '%" + params.get(myUI.getMessage(Messages.FirstName)) + "%' ";
        }
        if (params.get(myUI.getMessage(Messages.LastName)) != null
            && !params.get(myUI.getMessage(Messages.LastName)).isEmpty()) {
            sql += "AND e.surname LIKE  '%" + params.get(myUI.getMessage(Messages.LastName)) + "%' ";
        }
        if (params.get(myUI.getMessage(Messages.FromAge)) != null
            && !params.get(myUI.getMessage(Messages.FromAge)).equals("0")) {
            sql += "AND year(NOW()) - year(e.date_of_birth) >= " + params.get(myUI.getMessage(Messages.FromAge)) + " ";
        }
        if (params.get(myUI.getMessage(Messages.ToAge)) != null
            && !params.get(myUI.getMessage(Messages.ToAge)).equals("0")) {
            sql += "AND year(NOW()) - year(e.date_of_birth) <= " + params.get(myUI.getMessage(Messages.ToAge)) + " ";
        }
        sql += "GROUP BY e.id ORDER BY sch.id, e.surname, e.name";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(" ", Integer.class, 0);
        container.addContainerProperty(Settings.button, String.class, "CV");
        container.addContainerProperty(myUI.getMessage(Messages.School), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.LastName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Id), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Photo), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.FirstName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.MiddleName), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.DateOfBirth), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Age), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.WorkingStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ContractType), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.MainPosition), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraPositions), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Gender), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Nationality), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Citizenship), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.MartialStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ContactInfo), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.HealthStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.GraduationSchools),
                String.class, myUI.getMessage(Messages.OtherSchool));
        container.addContainerProperty(myUI.getMessage(Messages.Education), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.WorkPlaces), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Hours), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraHours), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.MainBranch), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.CanBeAdvisor), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Exams), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Seminars), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Certificates), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Languages), String.class, null);
        int i = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("e.id"));
            item.getItemProperty(" ").setValue(++i);
            item.getItemProperty(myUI.getMessage(Messages.School)).setValue(result.getString("school"));
            item.getItemProperty(myUI.getMessage(Messages.Id)).setValue(result.getString("e.login"));
            item.getItemProperty(myUI.getMessage(Messages.Photo)).setValue(result.getString("e.photo"));
            if (result.getString("grad_school") != null) {
                item.getItemProperty(myUI.getMessage(Messages.GraduationSchools)).setValue(result.getString("grad_school"));
            }
            item.getItemProperty(myUI.getMessage(Messages.FirstName)).setValue(result.getString("e.name"));
            item.getItemProperty(myUI.getMessage(Messages.LastName)).setValue(result.getString("e.surname"));
            item.getItemProperty(myUI.getMessage(Messages.MiddleName)).setValue(result.getString("e.middle_name"));
            item.getItemProperty(myUI.getMessage(Messages.Age)).setValue(result.getInt("age"));
            item.getItemProperty(myUI.getMessage(Messages.DateOfBirth)).setValue(Settings.df.format(
                    result.getDate("e.date_of_birth")));
            item.getItemProperty(myUI.getMessage(Messages.ContractType)).setValue(result.getString("sal.name"));
            item.getItemProperty(myUI.getMessage(Messages.MainPosition)).setValue(result.getString("p.name"));
            item.getItemProperty(myUI.getMessage(Messages.ContactInfo)).setValue(result.getString("phones"));
            item.getItemProperty(myUI.getMessage(Messages.ExtraPositions)).setValue(result.getString("extra_positions"));
            item.getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(result.getString("main_branch"));
            item.getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(result.getString("extra_branches"));
            item.getItemProperty(myUI.getMessage(Messages.Gender)).setValue(result.getString("g.name"));
            item.getItemProperty(myUI.getMessage(Messages.Nationality)).setValue(result.getString("n.name"));
            item.getItemProperty(myUI.getMessage(Messages.Citizenship)).setValue(result.getString("cntr.name"));
            item.getItemProperty(myUI.getMessage(Messages.MartialStatus)).setValue(result.getString("m.name"));
            item.getItemProperty(myUI.getMessage(Messages.WorkingStatus)).setValue(result.getString("ws.name"));
            item.getItemProperty(myUI.getMessage(Messages.HealthStatus)).setValue(result.getString("hs.name"));
            item.getItemProperty(myUI.getMessage(Messages.Education)).setValue(result.getString("education"));
            item.getItemProperty(myUI.getMessage(Messages.WorkPlaces)).setValue(result.getString("work"));
            item.getItemProperty(myUI.getMessage(Messages.CanBeAdvisor)).setValue(result.getInt("e.can_advisor") == 1 ?
                    myUI.getMessage(Messages.Yes) : myUI.getMessage(Messages.No));
            item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(result.getInt("ebh.hours"));
            item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(result.getInt("ebh.extra"));
            item.getItemProperty(myUI.getMessage(Messages.Exams)).setValue(result.getString("exams"));
            item.getItemProperty(myUI.getMessage(Messages.Seminars)).setValue(result.getString("sems"));
            item.getItemProperty(myUI.getMessage(Messages.Certificates)).setValue(result.getString("certs"));
            item.getItemProperty(myUI.getMessage(Messages.Languages)).setValue(result.getString("langs"));
        }
        return container;
    }

    public int execSQL_id(int school_id, String login) throws SQLException {

        String sql = "SELECT e.id FROM employee AS e " +
                     "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL " +
                     "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id " +
                     "LEFT JOIN position AS pos ON p.id = pos.hr_position_id " +
                     "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id " +
                     "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id " +
                     "WHERE eo.school_id = ? AND e.login = ? AND ord.working_status_id IS NOT NULL AND ws.id = 2";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setString(2, login);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("e.id");
        }
        return 0;
    }

    public Employee exec_by_position_id(int position_id, int school_id) throws SQLException {
        Employee employee = null;
        String sql = "SELECT e.id, e.name, e.surname, e.middle_name, e.gender_id " +
                     "FROM hr_employee_order AS eo " +
                     "LEFT JOIN employee AS e ON eo.employee_id = e.id " +
                     "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id " +
                     "LEFT JOIN position AS pos ON p.id = pos.hr_position_id " +
                     "WHERE eo.hr_orders_id IN (1, 2) AND " +
                     "(eo.to_date IS NULL OR eo.to_date >= NOW()) AND pos.id = ? and eo.school_id = ? LIMIT 1";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, position_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            employee = new Employee();
            employee.setId(result.getInt("e.id"));
            employee.setGender_id(result.getInt("e.gender_id"));
            employee.setSurname(result.getString("e.surname"));
            employee.setName(result.getString("e.name"));
            employee.setMiddle_name(result.getString("e.middle_name"));
        }
        return employee;
    }
}
