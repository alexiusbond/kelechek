/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.domain.Definition;
import kg.alex.kelechek.domain.EmployeeWork;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.ui.EmployeeDefinitionView;
import kg.alex.kelechek.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.sql.*;

public class DbEmployeeWork extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeWork.class);

    public DbEmployeeWork() throws Exception {
        super();
    }

    public int exec_insert(EmployeeWork ew) throws SQLException {
        String sql = "INSERT INTO hr_employee_work (employee_id, hr_own_id, hr_work_place_id, position_id, "
                + "start_date, end_date, working_status_id, is_sapat) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ew.getEmployee_id());
        stat.setInt(2, ew.getOwn_id());
        stat.setInt(3, ew.getWork_place_id());
        stat.setInt(4, ew.getMain_position_id());
        stat.setDate(5, new Date(ew.getStart().getTime()));
        if (ew.getEnd() != null) {
            stat.setDate(6, new Date(ew.getEnd().getTime()));
        } else {
            stat.setNull(6, Types.DATE);
        }
        stat.setInt(7, ew.getWorking_status_id());
        stat.setBoolean(8, ew.isSapat());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public void exec_insert_extra_position(int employee_work_id, int position_id) throws SQLException {
        String sql = "INSERT INTO hr_employee_work_extra_positions (hr_employee_work_id, position_id) VALUES(?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_work_id);
        stat.setInt(2, position_id);
        stat.executeUpdate();
    }

    public int exec_update(EmployeeWork ew) throws SQLException {
        String sql = "update hr_employee_work set "
                + "hr_work_place_id = ?, position_id = ?, start_date = ?, end_date = ?, "
                + "working_status_id = ?, is_sapat = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ew.getWork_place_id());
        stat.setInt(2, ew.getMain_position_id());
        stat.setDate(3, new Date(ew.getStart().getTime()));
        if (ew.getEnd() != null) {
            stat.setDate(4, new Date(ew.getEnd().getTime()));
        } else {
            stat.setNull(4, Types.DATE);
        }
        stat.setInt(5, ew.getWorking_status_id());
        stat.setBoolean(6, ew.isSapat());
        stat.setInt(7, ew.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id,
                                    EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ew.id, ew.hr_work_place_id, ew.position_id, " +
                "group_concat(ep.position_id separator ',') as extra_positions, " +
                "ew.start_date, ew.end_date, ew.working_status_id, ew.is_sapat FROM hr_employee_work as ew " +
                "left join hr_employee_work_extra_positions as ep on ep.hr_employee_work_id = ew.id " +
                "where ew.employee_id = ? and ew.hr_own_id = ? group by ew.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, own_id);
        ResultSet result = stat.executeQuery();
        final IndexedContainer container = edv.prepareWorkContainer(own_id);
        while (result.next()) {
            String id = result.getString("ew.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    edv.createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbEmployeeWork, FontAwesome.MINUS_SQUARE));
            ComboBox cb = edv.createCombobox(0, myUI.getMessage(Messages.MainPosition),
                    null, true);
            item.getItemProperty(myUI.getMessage(Messages.MainPosition)).setValue(cb);
            ComboBoxMultiselect cb3 = edv.createComboboxMulti(
                    myUI.getMessage(Messages.ExtraPositions), false);
            try {
                DbDefinition dbDef = new DbDefinition();
                dbDef.connect();
                cb3.setContainerDataSource(
                        dbDef.exec_positions_for_select(myUI, false, true));
                cb.setContainerDataSource(
                        dbDef.exec_positions_for_select(myUI, false, true));
                dbDef.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setValue(result.getInt("ew.position_id"));
            if (result.getString("extra_positions") != null) {
                cb3.setValue(Settings.convertToSet(result.getString("extra_positions")));
            }
            item.getItemProperty(myUI.getMessage(Messages.ExtraPositions)).setValue(cb3);
            cb = edv.createCombobox(0, myUI.getMessage(Messages.WorkingStatus), null, true);
            try {
                DbDefinition dbd = new DbDefinition();
                dbd.connect();
                cb.setContainerDataSource(dbd.exec_for_select_general_working_statuses(myUI));
                dbd.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            cb.setValue(result.getInt("ew.working_status_id"));
            item.getItemProperty(myUI.getMessage(Messages.WorkingStatus)).setValue(cb);
            final ComboBox cb2 = edv.createCombobox(result.getInt("ew.hr_work_place_id"),
                    myUI.getMessage(Messages.WorkPlace), Settings.dbWork_placeTable, true);
            cb2.setNewItemsAllowed(true);
            cb2.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbWork_placeTable, false);
                    dbd.close();
                    if (id1 != 0) {
                        for (Object next : container.getItemIds()) {
                            Item item1 = ((IndexedContainer) ((ComboBox) container.getContainerProperty(next,
                                    myUI.getMessage(Messages.WorkPlace)).getValue()).getContainerDataSource()).addItem(id1);
                            item1.getItemProperty(myUI.getMessage(Messages.Title)).setValue(newItemCaption);
                            cb2.setValue(id1);
                        }
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            });
            item.getItemProperty(myUI.getMessage(Messages.WorkPlace)).setValue(cb2);
            item.getItemProperty(myUI.getMessage(Messages.Start)).setValue(
                    edv.createDateField(result.getDate("ew.start_date"),
                            myUI.getMessage(Messages.Start), null, true, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(Messages.End)).setValue(
                    edv.createDateField(result.getDate("ew.end_date"),
                            myUI.getMessage(Messages.End), null, false, Settings.datePattern, Resolution.DAY));
            item.getItemProperty(myUI.getMessage(Messages.Abl)).setValue(
                    edv.createCheckBox(result.getBoolean("ew.is_sapat"), myUI.getMessage(Messages.Abl)));
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public IndexedContainer execSQL(final MyVaadinUI myUI, int employee_id, int own_id) throws SQLException {

        String sql = "SELECT ew.id, wp.name, p.name, GROUP_CONCAT(p2.name SEPARATOR ',') AS extra_positions, ew.start_date, ew.end_date, " +
                "ws.name, ew.is_sapat FROM hr_employee_work AS ew " +
                "LEFT JOIN hr_employee_work_extra_positions AS ep ON ep.hr_employee_work_id = ew.id " +
                "LEFT JOIN hr_position AS p2 ON ep.position_id = p2.id " +
                "LEFT JOIN hr_work_place AS wp ON ew.hr_work_place_id = wp.id " +
                "LEFT JOIN hr_position AS p ON ew.position_id = p.id " +
                "LEFT JOIN working_status AS ws ON ew.working_status_id = ws.id " +
                "WHERE ew.employee_id = ? AND ew.hr_own_id = ? GROUP BY ew.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, own_id);
        ResultSet result = stat.executeQuery();
        final IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.WorkPlace), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Abl), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.MainPosition), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraPositions), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.WorkingStatus), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Period), String.class, null);

        while (result.next()) {
            String id = result.getString("ew.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.WorkPlace)).setValue(result.getString("wp.name"));
            item.getItemProperty(myUI.getMessage(Messages.Abl)).setValue(result.getInt("ew.is_sapat") == 1 ?
                    myUI.getMessage(Messages.Yes) : myUI.getMessage(Messages.No));
            item.getItemProperty(myUI.getMessage(Messages.MainPosition)).setValue(result.getString("p.name"));
            item.getItemProperty(myUI.getMessage(Messages.ExtraPositions)).setValue(result.getString("extra_positions"));
            item.getItemProperty(myUI.getMessage(Messages.WorkingStatus)).setValue(result.getString("ws.name"));
            item.getItemProperty(myUI.getMessage(Messages.Period)).setValue("с " + Settings.df.format(result.getDate("ew.start_date"))
                    + (result.getDate("ew.end_date") == null ? "" : " по " + Settings.df.format(result.getDate("ew.end_date"))));
        }
        return container;
    }

    public String execSQL_work_experience(int employee_id, int own_id, boolean isSapat) throws SQLException {

        String sql = "SELECT SUM(TIMESTAMPDIFF(DAY, start_date, IFNULL(ew.end_date, NOW()))) / 365 as years " +
                "FROM hr_employee_work AS ew WHERE ew.hr_own_id = ? AND ew.employee_id = ?";
        if (isSapat) {
            sql += " AND ew.is_sapat = ?";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, own_id);
        stat.setInt(2, employee_id);
        if (isSapat) {
            stat.setInt(3, 1);
        }
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            int years = result.getInt("years");
            int months = (int) Math.round((result.getDouble("years") - result.getInt("years")) * 12);
            return (years > 0 ? years + " " + Settings.generateYearPostfix(years) + " " : "")
                    + (months > 0 && months < 12 ? months + " " + Settings.generateMonthPostfix(months) : "");
        }
        return "";
    }
}
