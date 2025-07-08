/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.domain.EmployeeLessons;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.ui.EmployeeDefinitionView;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.TreeSet;

public class DbEmployeeLessons extends BaseDb {

    public DbEmployeeLessons() throws Exception {
        super();
    }

    public int exec_insert(EmployeeLessons el) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_branch_hours "
                + "(employee_id,hr_branch_id,year_id,hours,extra_hours,school_id,class_number_id) "
                + "VALUES(?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, el.getEmployee_id());
        stat.setInt(2, el.getBranch_id());
        stat.setInt(3, el.getYear_id());
        stat.setInt(4, el.getHours());
        stat.setInt(5, el.getExtra_hours());
        stat.setInt(6, el.getSchool_id());
        stat.setInt(7, el.getClass_number_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeLessons el) throws SQLException {
        String sql = "update hr_employee_branch_hours set "
                + "hr_branch_id = ?, year_id = ?, hours = ?, extra_hours = ?, class_number_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, el.getBranch_id());
        stat.setInt(2, el.getYear_id());
        stat.setInt(3, el.getHours());
        stat.setInt(4, el.getExtra_hours());
        stat.setInt(5, el.getClass_number_id());
        stat.setInt(6, el.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id, int school_id,
                                    EmployeeDefinitionView edv) throws SQLException {
        String sql = "SELECT ex.id, ex.hr_branch_id, ex.year_id, ex.hours, ex.extra_hours, ex.class_number_id FROM hr_employee_branch_hours as ex "
                + "where ex.employee_id = ? and school_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareLessonsContainer();
        while (result.next()) {
            String id = result.getString("ex.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeBranchHours, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(
                    edv.createCombobox(result.getInt("ex.hr_branch_id"),
                            myUI.getMessage(Messages.Lesson), Settings.dbBranchTable, true));
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                    edv.createCombobox(result.getInt("ex.class_number_id"),
                            myUI.getMessage(Messages.ClassName), Settings.classTable, true));
            item.getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(
                    edv.createCombobox(result.getInt("ex.hr_branch_id"),
                            myUI.getMessage(Messages.Lesson), Settings.dbBranchTable, true));
            item.getItemProperty(myUI.getMessage(Messages.AcademicYear)).setValue(
                    edv.createCombobox(result.getInt("ex.year_id"),
                            myUI.getMessage(Messages.AcademicYear), Settings.dbYear, true));
            item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(
                    edv.createTextFieldWithProperty(result.getInt("ex.hours"), myUI.getMessage(Messages.Hours),
                            new IntegerRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 1, 999),
                            new ObjectProperty<Integer>(0), Settings.getStringToIntegerConverter()));
            item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                    edv.createTextFieldWithProperty(result.getInt("ex.extra_hours"), myUI.getMessage(Messages.ExtraHours),
                            new IntegerRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0, 999),
                            new ObjectProperty<Integer>(0), Settings.getStringToIntegerConverter()));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }


    public IndexedContainer execSQL(MyVaadinUI myUI, int year_id, int school_id) throws SQLException {
        String sql = "SELECT bh.id, CONCAT('[', e.login, '] ', e.surname, ' ', e.name) AS empl, " +
                "CONCAT(b.code, ' - ', b.name) AS branch, cn.name, bh.hours, bh.extra_hours " +
                "FROM hr_employee_branch_hours AS bh " +
                "LEFT JOIN employee AS e ON e.id = bh.employee_id " +
                "LEFT JOIN hr_branch AS b ON b.id = bh.hr_branch_id " +
                "LEFT JOIN class_number AS cn ON cn.id = bh.class_number_id " +
                "WHERE bh.school_id = ? AND bh.year_id = ? order by e.surname, e.name";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Lecturer), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Lesson), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.Hours), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraHours), Integer.class, 0);
        while (result.next()) {
            int id = result.getInt("bh.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.Lecturer)).setValue(result.getString("empl"));
            item.getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(result.getString("branch"));
            item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(result.getInt("cn.name"));
            item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(result.getInt("bh.hours"));
            item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(result.getInt("bh.extra_hours"));
        }
        return container;
    }

    public String execSQLTotalHours(MyVaadinUI myUI, int employee_id, int school_id) throws SQLException {
        String sql = "select sum(hours) as hours, sum(extra_hours) as extra, employee_id as e_id, year_id as y_id, school_id as sch_id "
                + "from hr_employee_branch_hours where employee_id = ? and school_id = ? and year_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, school_id);
        stat.setInt(3, myUI.getUser().getCurrent_year().getId());
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            return result.getString("hours") + " (" + result.getString("extra") + ")";
        }
        return null;
    }

    public IndexedContainer execSQLHours(MyVaadinUI myUI, int employee_id, int school_id, int cl_num_id,
                                         Property.ValueChangeListener vll) throws SQLException {

        String sql = "SELECT br.id, br.name, ebr.hours, ebr.extra_hours, ebr.id FROM hr_branch AS br "
                + "LEFT JOIN hr_employee_branch_hours AS ebr ON ebr.hr_branch_id = br.id AND ebr.year_id = ? AND ebr.employee_id = ? "
                + "AND ebr.class_number_id = ? and ebr.school_id = ? WHERE br.activity_status_id = 2 order by br.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, myUI.getUser().getCurrent_year().getId());
        stat.setInt(2, employee_id);
        stat.setInt(3, cl_num_id);
        stat.setInt(4, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.button, CheckBox.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Lesson), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Hours), TextField.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraHours), TextField.class, null);

        while (result.next()) {
            Item item = container.addItem(result.getInt("br.id"));
            CheckBox ch = new CheckBox();
            ch.setValue(result.getInt("ebr.id") != 0);
            ch.addValueChangeListener(vll);
            ch.setData(result.getInt("br.id"));
            item.getItemProperty(Settings.button).setValue(ch);
            item.getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(result.getString("br.name"));
            Integer hours = null;
            if (result.getInt("ebr.id") != 0) {
                hours = result.getInt("ebr.hours");
            }
            TextField tf = createTextfieldWithProperty(myUI, hours, myUI.getMessage(Messages.Hours),
                    new IntegerRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 1, 999),
                    new ObjectProperty<Integer>(0), Settings.getStringToIntegerConverter());
            tf.setData(result.getInt("ebr.id"));
            item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(tf);
            Integer extra_hours = null;
            if (result.getInt("ebr.id") != 0) {
                extra_hours = result.getInt("ebr.extra_hours");
            }
            item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                    createTextfieldWithProperty(myUI, extra_hours, myUI.getMessage(Messages.ExtraHours),
                            new IntegerRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0, 999),
                            new ObjectProperty<Integer>(0), Settings.getStringToIntegerConverter()));
        }
        return container;
    }

    private TextField createTextfieldWithProperty(MyVaadinUI myUI, Integer value, String description,
                                                  Validator validator, Property p, Converter conv) {
        TextField tf = new TextField(p);
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        if (value != null) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
        } else {
            tf.setEnabled(false);
        }
        tf.setNullRepresentation("");
        tf.setConverter(conv);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(validator);
        tf.getPropertyDataSource().setValue(value);
        return tf;
    }

    public IndexedContainer execSQLHours(MyVaadinUI myUI, int year_id, int school_id,
                                         String branchIds, String positionIds, String extraPositionIds, String workingStatusIds) throws SQLException {

        String sql = "SELECT br.name, br.id, empl_t.br_id, empl_t.emp_id, empl_t.fullname, empl_t.work_status, empl_t.main_branch, empl_t.main_branch_id, empl_t.extra_branch, "
                + "empl_t.extra_branch_ids, empl_t.position, empl_t.extra_positions, empl_t.hours, empl_t.extra_hours FROM hr_branch AS br "
                + "LEFT JOIN (SELECT e.id as emp_id, brh.hr_branch_id AS br_id, "
                + "CONCAT(e.surname, ' ', e.name, ' ', IFNULL(e.middle_name, '')) AS fullname, "
                + "GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 1, br.name, NULL) ORDER BY eb.id ASC SEPARATOR ', ') AS main_branch, "
                + "GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 1, br.id, 0) ORDER BY eb.id ASC SEPARATOR ', ') AS main_branch_id, "
                + "GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 2, br.name, NULL) ORDER BY eb.id ASC SEPARATOR ', ') AS extra_branch, "
                + "GROUP_CONCAT(DISTINCT IF(eb.hr_importance_id = 2, br.id, NULL) ORDER BY eb.id ASC SEPARATOR ', ') AS extra_branch_ids, "
                + "SUM(brh.hours) AS hours, SUM(brh.extra_hours) AS extra_hours, ws.name AS work_status, p.name AS position, "
                + "GROUP_CONCAT(DISTINCT p2.name ORDER BY eo2.id ASC SEPARATOR ', ') AS extra_positions "
                + "FROM hr_employee_branch_hours AS brh "
                + "LEFT JOIN employee AS e ON e.id = brh.employee_id "
                + "LEFT JOIN hr_employee_branch AS eb ON eb.employee_id = e.id "
                + "LEFT JOIN hr_branch AS br ON br.id = eb.hr_branch_id "
                + "LEFT JOIN hr_employee_order AS eo ON eo.employee_id = e.id AND eo.to_date IS NULL "
                + "LEFT JOIN hr_position AS p ON p.id = eo.hr_position_id "
                + "LEFT JOIN hr_orders AS ord ON ord.id = eo.hr_orders_id "
                + "LEFT JOIN working_status AS ws ON ord.working_status_id = ws.id "
                + "LEFT JOIN hr_employee_order AS eo2 ON eo2.employee_id = e.id AND eo2.hr_orders_id = 2 AND "
                + "(eo2.to_date IS NULL OR eo2.to_date >= NOW()) LEFT JOIN hr_position AS p2 ON p2.id = eo2.hr_position_id "
                + "WHERE brh.hr_branch_id IN (" + branchIds + ") AND brh.year_id = ? AND ws.id IN (" + workingStatusIds + ") "
                + "AND eo.school_id = ? AND p.id IN (" + positionIds + ") ";
        if (extraPositionIds != null) {
            sql += "AND p2.id IN (" + extraPositionIds + ") ";
        }
        sql += "GROUP BY brh.hr_branch_id, e.id) AS empl_t ON empl_t.br_id = br.id "
                + "WHERE br.id IN (" + branchIds + ") order by br.id desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Lesson), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Employee), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Hours), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraHours), Integer.class, 0);
        container.addContainerProperty(myUI.getMessage(Messages.WorkingStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.MainBranch), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraBranches), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Position), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraPosition), String.class, null);
        int i = 0;
        int branch_id = 0;
        int totalLessons = 0;
        int totalEmpl = 0;
        int totalAsMainBranch = 0;
        int totalAsExtraBranch = 0;
        int grandTotalAsMainBranch = 0;
        int grandTotalAsExtraBranch = 0;
        TreeSet<Integer> empCount = new TreeSet<>();
        container.addItem("t");
        container.getItem("t").getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(
                myUI.getMessage(Messages.TotalEmployeesAsExtraBranch) + ": 0");
        container.getItem("t").getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(
                myUI.getMessage(Messages.TotalEmployeesAsMainBranch) + ": 0");
        while (result.next()) {
            if (branch_id != result.getInt("br.id")) {
                Item item = container.addItem("s" + result.getInt("br.id"));
                item.getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(result.getString("br.name"));
                item.getItemProperty(myUI.getMessage(Messages.Employee)).setValue(myUI.getMessage(Messages.TotalEmployees) + "0");
                item.getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(myUI.getMessage(Messages.TotalEmployeesAsMainBranch)
                        + result.getString("br.name") + ": 0");
                item.getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(myUI.getMessage(Messages.TotalEmployeesAsExtraBranch)
                        + result.getString("br.name") + ": 0");
                totalEmpl = 0;
                totalAsMainBranch = 0;
                totalAsExtraBranch = 0;
                container.getItem("t").getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(
                        myUI.getMessage(Messages.TotalBranches) + (++totalLessons));
            }
            branch_id = result.getInt("br.id");
            if (result.getInt("empl_t.br_id") != 0) {
                Item item = container.addItem("" + (++i));
                item.getItemProperty(myUI.getMessage(Messages.Employee)).setValue(
                        result.getString("empl_t.fullname"));
                empCount.add(result.getInt("empl_t.emp_id"));
                item.getItemProperty(myUI.getMessage(Messages.WorkingStatus)).setValue(
                        result.getString("empl_t.work_status"));
                item.getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(
                        result.getString("empl_t.main_branch"));
                item.getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(
                        result.getString("empl_t.extra_branch"));
                item.getItemProperty(myUI.getMessage(Messages.Position)).setValue(
                        result.getString("empl_t.position"));
                item.getItemProperty(myUI.getMessage(Messages.ExtraPosition)).setValue(
                        result.getString("empl_t.extra_positions"));
                item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(
                        result.getInt("empl_t.hours"));
                item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                        result.getInt("empl_t.extra_hours"));

                container.getItem("s" + result.getInt("br.id")).getItemProperty(myUI.getMessage(Messages.Employee)).setValue(
                        myUI.getMessage(Messages.TotalEmployees) + (++totalEmpl));

                container.getItem("s" + result.getInt("br.id")).getItemProperty(myUI.getMessage(Messages.Hours)).setValue(
                        ((Integer) container.getItem("s" + result.getInt("br.id")).getItemProperty(myUI.getMessage(Messages.Hours)).getValue())
                                + result.getInt("empl_t.hours"));
                container.getItem("s" + result.getInt("br.id")).getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                        ((Integer) container.getItem("s" + result.getInt("br.id")).getItemProperty(myUI.getMessage(Messages.ExtraHours)).getValue())
                                + result.getInt("empl_t.extra_hours"));
                container.getItem("t").getItemProperty(myUI.getMessage(Messages.Hours)).setValue(
                        ((Integer) container.getItem("t").getItemProperty(myUI.getMessage(Messages.Hours)).getValue())
                                + result.getInt("empl_t.hours"));
                container.getItem("t").getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                        ((Integer) container.getItem("t").getItemProperty(myUI.getMessage(Messages.ExtraHours)).getValue())
                                + result.getInt("empl_t.extra_hours"));
                if (result.getInt("br.id") == result.getInt("empl_t.main_branch_id")) {
                    container.getItem("s" + result.getInt("br.id")).getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(
                            myUI.getMessage(Messages.TotalEmployeesAsMainBranch) + result.getString("br.name") + ": " + (++totalAsMainBranch));
                    container.getItem("t").getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(
                            myUI.getMessage(Messages.TotalEmployeesAsMainBranch) + ": " + (++grandTotalAsMainBranch));
                }
                if (result.getString("empl_t.extra_branch_ids") != null
                        && Arrays.asList(result.getString("empl_t.extra_branch_ids").split(", ")).contains(result.getString("br.id"))) {
                    container.getItem("s" + result.getInt("br.id")).getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(
                            myUI.getMessage(Messages.TotalEmployeesAsExtraBranch) + result.getString("br.name") + ": " + (++totalAsExtraBranch));
                    container.getItem("t").getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(
                            myUI.getMessage(Messages.TotalEmployeesAsExtraBranch) + ": " + (++grandTotalAsExtraBranch));
                }
            }

            container.getItem("t").getItemProperty(myUI.getMessage(Messages.Employee)).setValue(
                    myUI.getMessage(Messages.TotalEmployees) + empCount.size());
        }
        return container;
    }

    public int exec_delete(int employee_id, int branch_id, int school_id, int class_number_id, int year_id) throws SQLException {
        String sql = "DELETE FROM hr_employee_branch_hours WHERE employee_id = ? and hr_branch_id = ? and year_id = ? and school_id = ? and class_number_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, branch_id);
        stat.setInt(3, year_id);
        stat.setInt(4, school_id);
        stat.setInt(5, class_number_id);
        return stat.executeUpdate();
    }

    public int exec_delete(int school_id, int year_id) throws SQLException {
        String sql = "DELETE FROM hr_employee_branch_hours WHERE year_id = ? and school_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, school_id);
        return stat.executeUpdate();
    }
}
