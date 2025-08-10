/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.*;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.*;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.domain.AccTransaction;
import kg.alex.ellipse.domain.CashBox;
import kg.alex.ellipse.domain.SchoolAccounting;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.reports.accounting.SchoolsReport;
import kg.alex.ellipse.ui.CashBoxView;
import kg.alex.ellipse.ui.PayoutsView;
import kg.alex.ellipse.utils.FormattedTreeTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTreeTable;

import java.sql.*;
import java.util.*;
import java.util.Calendar;
import java.util.Date;

public class DbAccTransactions extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbAccTransactions.class);

    public DbAccTransactions() throws Exception {
        super();
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int invoice_id, int school_id, PayoutsView pav)
            throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();

        String sql = "SELECT t.id, t.amount, t.acc_category_id, t.acc_cashbox_id, " +
                "t.currency_rate, t.note, c.acc_currency_id " +
                "FROM acc_transactions as t " +
                "LEFT JOIN acc_cashbox as c on c.id = t.acc_cashbox_id " +
                "where t.acc_invoice_id = ? order by t.id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, invoice_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = pav.preparePayoutsContainer();
        double total = 0.0, usd = 0.0, kgs = 0.0;
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(
                    pav.createButton(myUi.getMessage(Messages.DeleteButton), id, Settings.dbAcc_transactions));
            ComboBox categoryCb = pav.createCombobox(0, myUi.getMessage(Messages.Category), null, true, true);
            ComboBox cashboxCb = pav.createCombobox(0, myUi.getMessage(Messages.CashBox),
                    null, true, false);
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                categoryCb.setContainerDataSource(dbCon.exec_for_select(myUi, 2, school_id, false));
                dbCon.close();
                DbCashbox dbc = new DbCashbox();
                dbc.connect();
                cashboxCb.setContainerDataSource(dbc.execSQL(myUi));
                dbc.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            categoryCb.setItemCaptionPropertyId(myUi.getMessage(Messages.FullName));
            categoryCb.setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUi.getMessage(Messages.Category)).setValue(categoryCb);

            cashboxCb.setValue(result.getInt("t.acc_cashbox_id"));
            cashboxCb.addValueChangeListener(pav);
            item.getItemProperty(myUi.getMessage(Messages.CashBox)).setValue(cashboxCb);

            item.getItemProperty(Settings.acc_category_id).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(Settings.acc_cashbox_id).setValue(result.getInt("t.acc_cashbox_id"));
            TextField tf = pav.createTextFieldWithProperty(
                    result.getDouble("t.amount"), myUi.getMessage(Messages.Amount),
                    new DoubleRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), 0.01, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), true);
            tf.addValueChangeListener(pav);
            item.getItemProperty(myUi.getMessage(Messages.Amount)).setValue(tf);
            tf = pav.createTextFieldWithProperty(
                    result.getDouble("t.currency_rate"), myUi.getMessage(Messages.Rate),
                    new DoubleRangeValidator(myUi.getMessage(Messages.NotificationWrongValue), 0.01, null),
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(4),
                    currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate));
            tf.addValueChangeListener(pav);
            item.getItemProperty(myUi.getMessage(Messages.Rate)).setValue(tf);
            item.getItemProperty(myUi.getMessage(Messages.Note)).setValue(pav.createTextField(
                    result.getString("t.note"), id, new StringLengthValidator(myUi.getMessage(Messages.NotificationWrongValue), null, 250, true), true));
            item.getItemProperty(Settings.crud_status).setValue(myUi.getMessage(Messages.Update));
            if (result.getInt("c.acc_currency_id") == 1) {
                total += result.getDouble("t.amount");
                kgs += result.getDouble("t.amount");
            } else {
                total += result.getDouble("t.amount") * result.getDouble("t.currency_rate");
                usd += result.getDouble("t.amount");
            }
        }
        pav.setPayoutsFooter(usd, kgs, total);
        return container;
    }

    public void execSQL(MyVaadinUI myUI, int incOrOut, int school_id, int cashbox_id, int year_id,
                        Grid grid, CashBoxView cbv, Date from, Date till) throws SQLException {

        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT t.id, t.date_time, t.acc_category_id, t.acc_cashbox_id, c.acc_currency_id, t.order_number, t.currency_rate, " +
                "t.amount, IF(t.student_payments_id IS NULL, t.note, CONCAT(IFNULL(vcs.class_name, vlcs.class_name), ' ', " +
                "st.login, ' ', st.name, ' ', st.surname)) AS note, " +
                "IF(t.student_payments_id IS NOT NULL OR t.dp_invoice_id IS NOT NULL " +
                "OR t.acc_invoice_id IS NOT NULL, TRUE, FALSE) AS isDisabled, " +
                "IF(t.date_time > concat(date(now()), ' 19:00:00'), TRUE, FALSE) AS isNotOld, t.from_to_employee_id, " +
                "CONCAT(e.surname, ' ', e.name) AS fullname " +
                "FROM acc_transactions AS t " +
                "LEFT JOIN employee AS e ON t.employee_id = e.id " +
                "LEFT JOIN acc_cashbox AS c ON t.acc_cashbox_id = c.id " +
                "LEFT JOIN student_payments AS sp ON t.student_payments_id = sp.id " +
                "LEFT JOIN student AS st ON sp.student_id = st.id " +
                "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? " +
                "LEFT JOIN view_student_last_class_status AS vlcs ON vlcs.student_id = st.id " +
                "where (t.acc_type_id = ? OR t.acc_type_id = 5) AND t.school_id = ? " +
                "AND DATE(t.date_time) >= ? AND DATE(t.date_time) <= ? and t.acc_cashbox_id = ? " +
                "order by t.date_time desc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, incOrOut);
        stat.setInt(3, school_id);
        stat.setDate(4, new java.sql.Date(from.getTime()));
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, cashbox_id);
        ResultSet result = stat.executeQuery();
        GeneratedPropertyContainer container;
        if (incOrOut == 1) {
            container = cbv.prepareIncomesContainer();
        } else {
            container = cbv.prepareExpensesContainer();
        }

        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(result.getTimestamp("t.date_time"));
            item.getItemProperty(myUI.getMessage(Messages.Category)).setValue(result.getInt("t.acc_category_id"));
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(result.getDouble("t.amount"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("note"));
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(result.getDouble("t.currency_rate"));
            if (incOrOut == 2) {
                item.getItemProperty(myUI.getMessage(Messages.ToEmployee)).setValue(result.getInt("t.from_to_employee_id"));
            }
            boolean isDisabled = result.getBoolean("isDisabled");
            if (!isDisabled) {
                isDisabled = !(currentUser.isPermitted(Settings.cnTransactionsView + ":"
                        + Settings.prmChangeOldTransactions) || result.getBoolean("isNotOld"));
            }
            item.getItemProperty(Settings.is_disabled).setValue(isDisabled);
            item.getItemProperty(Settings.from_employee_id).setValue(result.getString("fullname"));
            if (result.getInt("t.order_number") != 0) {
                item.getItemProperty(Settings.hashTags).setValue(
                        String.format("%07d", result.getInt("t.order_number")));
            }
        }
        container.addGeneratedProperty(Settings.button, new PropertyValueGenerator<Component>() {
            @Override
            public Component getValue(Item item, Object itemId, Object propertyId) {
                HorizontalLayout hl = new HorizontalLayout();
                hl.setWidth("52px");
                hl.addComponent(cbv.createButton(myUI.getMessage(Messages.DeleteButton), itemId,
                        myUI.getMessage(Messages.DeleteButton),
                        (Boolean) item.getItemProperty(Settings.is_disabled).getValue(), FontAwesome.MINUS_SQUARE, "min-padding"));
                hl.addComponent(cbv.createButton(myUI.getMessage(Messages.Print), itemId,
                        myUI.getMessage(Messages.Print), false, FontAwesome.FILE_PDF_O, "min-padding"));
                hl.addLayoutClickListener((LayoutEvents.LayoutClickListener) layoutClickEvent -> grid.setEditorEnabled(false));
                return hl;
            }

            @Override
            public Class<Component> getType() {
                return Component.class;
            }
        });
        grid.setContainerDataSource(container);
    }

    public int exec_insert(AccTransaction t, Connection conn) throws SQLException {
        String sql = "INSERT INTO acc_transactions (date_time, amount, acc_cashbox_id, currency_rate, note, "
                + "acc_category_id, employee_id, school_id, modification_date, dp_invoice_id, student_payments_id, "
                + "from_to_employee_id, acc_invoice_id, acc_type_id) VALUES(?,?,?,?,?,?,?,?,NOW(),?,?,?,?,?)";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setTimestamp(1, new java.sql.Timestamp(t.getDate().getTime()));
        stat.setDouble(2, t.getAmount());
        stat.setInt(3, t.getCashbox().getId());
        stat.setDouble(4, t.getCurrency_rate());
        stat.setString(5, t.getNote());
        stat.setInt(6, t.getCategory_id());
        stat.setInt(7, t.getEmployee_id());
        stat.setInt(8, t.getSchool_id());
        if (t.getDp_invoice_id() != 0) {
            stat.setInt(9, t.getDp_invoice_id());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        if (t.getStudent_payments_id() != 0) {
            stat.setInt(10, t.getStudent_payments_id());
        } else {
            stat.setNull(10, Types.INTEGER);
        }
        if (t.getFrom_to_employee_id() != 0) {
            stat.setInt(11, t.getFrom_to_employee_id());
        } else {
            stat.setNull(11, Types.INTEGER);
        }
        if (t.getAcc_invoice_id() != 0) {
            stat.setInt(12, t.getAcc_invoice_id());
        } else {
            stat.setNull(12, Types.INTEGER);
        }
        stat.setInt(13, t.getAccTypeId());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId(conn);
        } else {
            return 0;
        }
    }

    public int exec_update(AccTransaction t) throws SQLException {
        String sql = "UPDATE acc_transactions set date_time = ?, amount = ?, acc_cashbox_id = ?, " +
                "currency_rate = ?, note = ?, acc_category_id = ?, employee_id = ?, school_id = ?, " +
                "modification_date = NOW(), from_to_employee_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setTimestamp(1, new java.sql.Timestamp(t.getDate().getTime()));
        stat.setDouble(2, t.getAmount());
        stat.setInt(3, t.getCashbox().getId());
        stat.setDouble(4, t.getCurrency_rate());
        stat.setString(5, t.getNote());
        stat.setInt(6, t.getCategory_id());
        stat.setInt(7, t.getEmployee_id());
        stat.setInt(8, t.getSchool_id());
        if (t.getFrom_to_employee_id() != 0) {
            stat.setInt(9, t.getFrom_to_employee_id());
        } else {
            stat.setNull(9, Types.INTEGER);
        }
        stat.setString(10, t.getId());
        return stat.executeUpdate();
    }

    public int exec_update(AccTransaction t, String by_column_name, int by_column_value, Connection conn) throws SQLException {

        String sql = "update acc_transactions set date_time = ?, amount = ?, acc_cashbox_id = ?, currency_rate = ?, " +
                "note = ?, acc_category_id = ?, acc_type_id = ?, modification_date = NOW() " +
                "WHERE " + by_column_name + " = ?";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(t.getDate().getTime()));
        stat.setDouble(2, t.getAmount());
        stat.setInt(3, t.getCashbox().getId());
        stat.setDouble(4, t.getCurrency_rate());
        stat.setString(5, t.getNote());
        stat.setInt(6, t.getCategory_id());
        stat.setInt(7, t.getAccTypeId());
        stat.setInt(8, by_column_value);
        return stat.executeUpdate();
    }

    public int exec_delete(String by_column_name, String by_column_value, Connection conn) throws SQLException {
        String sql = "DELETE FROM acc_transactions WHERE " + by_column_name + " = ?";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setString(1, by_column_value);
        return stat.executeUpdate();
    }

    public int exec_delete_by_st_id(int st_id, int year_id, Connection conn) throws SQLException {
        String sql = "delete act from acc_transactions as act left join student_payments as sp " +
                "on sp.id = act.student_payments_id where sp.student_id = ? ";
        if (year_id != 0) {
            sql += "and sp.year_id = ? ";
        }
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setInt(1, st_id);
        if (year_id != 0) {
            stat.setInt(2, year_id);
        }
        return stat.executeUpdate();
    }

    public AccTransaction exec_allow_delete_by_st_id(int st_id, int school_id, int cashbox_id) throws SQLException {
        String sql = "select tr.date_time, tr.amount, sp.payment_category_id from acc_transactions as tr "
                + "left join student_payments as sp on sp.id = tr.student_payments_id "
                + "where sp.student_id = ? and tr.acc_cashbox_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, st_id);
        stat.setInt(2, cashbox_id);
        ResultSet result = stat.executeQuery();
        while (result.next()) {
            if (result.getInt("sp.payment_category_id") != 3) {
                AccTransaction accTr = exec_low_balance(dbCon, school_id, cashbox_id, result.getDate("tr.date_time"),
                        result.getDouble("tr.amount"), 0.0, 1);
                if (accTr != null) {
                    return accTr;
                }
            }
        }
        return null;
    }

    public IndexedContainer exec_for_select(MyVaadinUI myUI, int type_id, int school_id) throws SQLException {
        String sql = "select ac.id, concat(ifnull(concat(ac.parent_code,'.',ac.code), ac.code), ' - ', ac.name) as name "
                + "from acc_category as ac where (ac.acc_type_id = ? or ac.acc_type_id = 5) "
                + "and ac.parent_id is not null and (ac.school_id is null or ac.school_id = ?) "
                + "and ac.parent_id not in (select acc_category_id from dp_product_category) "
                + "order by ifnull(concat(ac.parent_code,'.',ac.code), ac.code) asc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, type_id);
        //stat.setInt(2, id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("ac.id"));
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                    result.getString("name"));
        }
        return container;
    }

    public void execSQL_by_months(MyVaadinUI myUI, int type_id, int cashbox_id, int school_id,
                                  FilterTreeTable categoriesTable, Calendar from, Calendar till, FormattedTreeTable t)
            throws SQLException {
        Set<Integer> selectedIds = Settings.getChild_ids((HierarchicalContainer) categoriesTable.getContainerDataSource(),
                (Set<?>) categoriesTable.getValue());
        String sql = "SELECT cat.id, cat.parent_id, "
                + "sum(CASE WHEN ? = 0 AND c.acc_currency_id != 1 THEN tr.amount * tr.currency_rate ELSE tr.amount END) as amount, "
                + "DATE(tr.date_time) AS dt  FROM acc_category AS cat "
                + "LEFT JOIN acc_transactions AS tr ON tr.acc_category_id = cat.id "
                + "LEFT JOIN acc_cashbox AS c ON tr.acc_cashbox_id = c.id "
                + "WHERE cat.id IN ("
                + Settings.convertCollectionToStr(selectedIds)
                + ") AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ? AND cat.acc_type_id = ? "
                + "AND tr.school_id = ? ";
        if (cashbox_id != 0) {
            sql += "and tr.acc_cashbox_id = ? ";
        }
        sql += "GROUP BY cat.id, YEAR(tr.date_time), MONTH(tr.date_time) ORDER BY ifnull(concat(cat.parent_code,'.',cat.code), cat.code)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cashbox_id);
        stat.setDate(2, new java.sql.Date(from.getTime().getTime()));
        stat.setDate(3, new java.sql.Date(till.getTime().getTime()));
        stat.setInt(4, type_id);
        stat.setInt(5, school_id);
        if (cashbox_id != 0) {
            stat.setInt(6, cashbox_id);
        }
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Category), String.class, null);
        Calendar current = Calendar.getInstance();
        current.setTime(from.getTime());
        while (current.before(till)) {
            container.addContainerProperty(Settings.ymdf.format(current.getTime()), Double.class, 0.0);
            current.add(Calendar.MONTH, 1);
        }
        container.addContainerProperty(myUI.getMessage(Messages.Total), Double.class, 0.0);
        t.setContainerDataSource(container);
        current.setTime(from.getTime());
        while (current.before(till)) {
            t.setColumnFooter(Settings.ymdf.format(current.getTime()), "0.00");
            current.add(Calendar.MONTH, 1);
        }
        t.setColumnFooter(myUI.getMessage(Messages.Total), "0.00");
        for (Object catNext : categoriesTable.getContainerDataSource().getItemIds()) {
            if (selectedIds.contains(catNext)) {
                Item item = container.addItem(catNext);
                item.getItemProperty(myUI.getMessage(Messages.Code))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Code)).getValue().toString());
                item.getItemProperty(myUI.getMessage(Messages.Category))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Category)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = (categoriesTable.getContainerDataSource()).getParent(catNext);
                if (parent != null) {
                    container.setParent(catNext, parent);
                }
                if ((categoriesTable.getContainerDataSource()).getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            String month = Settings.ymdf.format(result.getDate("dt"));
            item.getItemProperty(month).setValue(result.getDouble("amount"));
            try {
                t.setColumnFooter(myUI.getMessage(Messages.Total), Settings.dFormat2.format(
                        Settings.dFormat2.parse(t.getColumnFooter(myUI.getMessage(Messages.Total))).doubleValue()
                                + result.getDouble("amount")));
                t.setColumnFooter(month, Settings.dFormat2.format(Settings.dFormat2.parse(t.getColumnFooter(month)).doubleValue()
                        + result.getDouble("amount")));
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            item.getItemProperty(myUI.getMessage(Messages.Total)).setValue((Double) item.getItemProperty(myUI.getMessage(Messages.Total)).getValue()
                    + result.getDouble("amount"));
            Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
            while (parent_id != null) {
                item = container.getItem(parent_id);
                item.getItemProperty(month).setValue(
                        (Double) item.getItemProperty(month).getValue()
                                + result.getDouble("amount"));
                item.getItemProperty(myUI.getMessage(Messages.Total)).setValue((Double) item.getItemProperty(myUI.getMessage(Messages.Total)).getValue()
                        + result.getDouble("amount"));
                parent_id = (Integer) container.getParent(parent_id);
            }
        }
    }

    public SchoolAccounting exec_get_totals(int scl_id, int cashbox_id, Date from, Date till, String cat_ids)
            throws SQLException {
        String sql = "SELECT " +
                "SUM(IF(tr.acc_type_id = 1 AND DATE(tr.date_time) >= ? " +
                "AND DATE(tr.date_time) <= ?, CASE WHEN ? = 0 AND c.acc_currency_id != 1 THEN tr.amount * tr.currency_rate ELSE tr.amount END, 0.0)) AS incTtl, " +
                "SUM(IF(tr.acc_type_id = 2 AND DATE(tr.date_time) >= ? " +
                "AND DATE(tr.date_time) <= ?, CASE WHEN ? = 0 AND c.acc_currency_id != 1 THEN tr.amount * tr.currency_rate ELSE tr.amount END, 0.0)) AS expTtl, " +
                "SUM(IF(DATE(tr.date_time) < ?, IF(tr.acc_type_id = 1, CASE WHEN ? = 0 AND c.acc_currency_id != 1 THEN tr.amount * tr.currency_rate ELSE tr.amount END, " +
                "-(CASE WHEN ? = 0 AND c.acc_currency_id != 1 THEN tr.amount * tr.currency_rate ELSE tr.amount END)), 0.0)) AS prev_balance " +
                "FROM acc_transactions AS tr " +
                "LEFT JOIN acc_cashbox as c on tr.acc_cashbox_id = c.id " +
                "WHERE tr.school_id = ? ";
        if (cashbox_id != 0) {
            sql += "and tr.acc_cashbox_id = ? ";
        }
        if (cat_ids != null) {
            sql += "and tr.acc_category_id in (" + cat_ids + ")";
        }
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from.getTime()));
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setInt(3, cashbox_id);
        stat.setDate(4, new java.sql.Date(from.getTime()));
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, cashbox_id);
        stat.setDate(7, new java.sql.Date(from.getTime()));
        stat.setInt(8, cashbox_id);
        stat.setInt(9, cashbox_id);
        stat.setInt(10, scl_id);
        if (cashbox_id != 0) {
            stat.setInt(11, cashbox_id);
        }
        ResultSet result = stat.executeQuery();
        SchoolAccounting acc = new SchoolAccounting();
        while (result.next()) {
            acc.setTotal_income(result.getDouble("incTtl"));
            acc.setTotal_outcome(result.getDouble("expTtl"));
            acc.setPrevious_balance(result.getDouble("prev_balance"));
        }
        return acc;
    }

    public void exec_schools_accounting(MyVaadinUI myUI, String school_ids, Date from_date, Date till_date,
                                        SchoolsReport sar) throws SQLException {

        String sql = "SELECT sch.id, sch.name_ru, "
                + "MAX(IF(tr.acc_type_id = 2, DATE(tr.date_time), null)) as max_exp, "
                + "MAX(IF(tr.acc_type_id = 1, DATE(tr.date_time), null)) as max_inc, "
                + "SUM(IF(tr.acc_type_id = 1 AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ?, "
                + "if(c.acc_currency_id = 1, tr.amount, ROUND(tr.amount*tr.currency_rate,2)), 0.0)) AS incTtl, "
                + "SUM(IF(tr.acc_type_id = 2 AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ?, "
                + "if(c.acc_currency_id = 1, tr.amount, ROUND(tr.amount*tr.currency_rate,2)), 0.0)) AS expTtl, "
                + "SUM(IF(DATE(tr.date_time) < ?, IF(tr.acc_type_id = 1, if(c.acc_currency_id = 1, tr.amount, ROUND(tr.amount*tr.currency_rate,2)), "
                + "-(if(c.acc_currency_id = 1, tr.amount, ROUND(tr.amount*tr.currency_rate,2)))), 0.0)) AS prev_balance "
                + "FROM acc_transactions AS tr "
                + "LEFT JOIN acc_cashbox AS c ON c.id = tr.acc_cashbox_id "
                + "LEFT JOIN school AS sch ON sch.id = tr.school_id "
                + "WHERE sch.id IN (" + school_ids + ") GROUP BY tr.school_id";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setDate(1, new java.sql.Date(from_date.getTime()));
        stat.setDate(2, new java.sql.Date(till_date.getTime()));
        stat.setDate(3, new java.sql.Date(from_date.getTime()));
        stat.setDate(4, new java.sql.Date(till_date.getTime()));
        stat.setDate(5, new java.sql.Date(from_date.getTime()));
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        Calendar c = Calendar.getInstance();
        c.setTime(from_date);
        c.add(Calendar.DAY_OF_MONTH, -1);
        container.addContainerProperty(myUI.getMessage(Messages.School), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.IncomesTotal), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.LastIncomeDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExpensesTotal), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.LastExpenseDate), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.PreviousBalance) + " (" + Settings.df.format(c.getTime()) + ")", Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.CashBox), Double.class, 0.0);
        double ttlInc = 0;
        double ttlExp = 0;
        double ttlPrev = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("sch.id"));
            item.getItemProperty(myUI.getMessage(Messages.School)).setValue(
                    result.getString("sch.name_ru"));
            item.getItemProperty(myUI.getMessage(Messages.IncomesTotal)).setValue(
                    result.getDouble("incTtl"));
            ttlInc += result.getDouble("incTtl");
            item.getItemProperty(myUI.getMessage(Messages.ExpensesTotal)).setValue(
                    result.getDouble("expTtl"));
            ttlExp += result.getDouble("expTtl");
            if (result.getDate("max_inc") != null) {
                item.getItemProperty(myUI.getMessage(Messages.LastIncomeDate)).setValue(
                        Settings.df.format(result.getDate("max_inc")));
            }
            if (result.getDate("max_exp") != null) {
                item.getItemProperty(myUI.getMessage(Messages.LastExpenseDate)).setValue(
                        Settings.df.format(result.getDate("max_exp")));
            }
            item.getItemProperty(myUI.getMessage(Messages.CashBox)).setValue(
                    result.getDouble("incTtl") + result.getDouble("prev_balance") - result.getDouble("expTtl"));
            item.getItemProperty(myUI.getMessage(Messages.PreviousBalance) + " (" + Settings.df.format(c.getTime()) + ")").setValue(
                    result.getDouble("prev_balance"));
            ttlPrev += result.getDouble("prev_balance");
        }
        sar.dataTable.setContainerDataSource(container);

        sar.dataTable.setColumnFooter(myUI.getMessage(Messages.School),
                myUI.getMessage(Messages.Total));
        sar.dataTable.setColumnFooter(myUI.getMessage(Messages.IncomesTotal),
                Settings.dFormat2.format(ttlInc));
        sar.dataTable.setColumnFooter(myUI.getMessage(Messages.ExpensesTotal),
                Settings.dFormat2.format(ttlExp));
        sar.dataTable.setColumnFooter(myUI.getMessage(Messages.PreviousBalance) + " (" + Settings.df.format(c.getTime()) + ")",
                Settings.dFormat2.format(ttlPrev));
        sar.dataTable.setColumnFooter(myUI.getMessage(Messages.CashBox),
                Settings.dFormat2.format(ttlInc - ttlExp));
    }

    public double exec_income_expense_balance(int school_id, int acc_category_id, Date till) throws SQLException {

        String sql = "SELECT " +
                "IFNULL(SUM(IF(tr.acc_type_id = 1, IF(c.acc_currency_id != 1, tr.amount * tr.currency_rate, tr.amount), " +
                "-IF(c.acc_currency_id != 1, tr.amount * tr.currency_rate, tr.amount))), 0.0) AS balance " +
                "FROM acc_transactions AS tr " +
                "LEFT JOIN acc_cashbox AS c ON c.id = tr.acc_cashbox_id " +
                "WHERE tr.acc_category_id = ? AND date(tr.date_time) < ? and tr.school_id = ?";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setInt(3, school_id);
        ResultSet result = stat.executeQuery();

        while (result.next()) {
            return result.getDouble("balance");
        }
        return 0.0;
    }

    public double exec_salary_balance(int school_id, int acc_category_id, int currency_id, Date till) throws SQLException {

        String sql = "SELECT ";
        if (currency_id == 2) {
            sql += "(SELECT IFNULL(SUM(IF(acr.acc_currency_id != 2, acr.amount / acr.currency_rate, acr.amount)), 0.0) AS amount ";
        } else {
            sql += "(SELECT IFNULL(SUM(IF(acr.acc_currency_id != 1, acr.amount * acr.currency_rate, acr.amount)), 0.0) AS amount ";
        }
        sql += "FROM acc_transfers AS acr "
                + "LEFT JOIN acc_invoice AS inv ON acr.invoice_id = inv.id "
                + "WHERE acr.acc_category_id = ? AND date(inv.creation_date) < ? AND inv.acc_invoice_type_id = 1 "
                + "and inv.school_id = ?) - (SELECT ";
        if (currency_id == 2) {
            sql += "IFNULL(SUM(IF(tr.acc_currency_id != 2, tr.amount / tr.currency_rate, tr.amount)), 0.0) AS amount ";
        } else {
            sql += "IFNULL(SUM(IF(tr.acc_currency_id != 1, tr.amount * tr.currency_rate, tr.amount)), 0.0) AS amount ";
        }
        sql += "FROM acc_transactions AS tr WHERE tr.acc_category_id = ? AND date(tr.date_time) < ? and tr.school_id = ?) "
                + "as balance";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, acc_category_id);
        stat.setDate(2, new java.sql.Date(till.getTime()));
        stat.setInt(3, school_id);
        stat.setInt(4, acc_category_id);
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, school_id);
        ResultSet result = stat.executeQuery();

        while (result.next()) {
            return result.getDouble("balance");
        }
        return 0.0;
    }

    public void exec_current_account_statement(MyVaadinUI myUI, int acc_category_id, Date from, Date till, Table t,
                                               int currency_id, int school_id) throws SQLException {
        String sign = "/";
        if (currency_id == 1) {
            sign = "*";
        }
        String sql = "SELECT t.creation_date, t.amount, t.rate, t.note, t.type FROM "
                + "(SELECT inv.creation_date AS creation_date, "
                + "IF(acr.acc_currency_id != ?, acr.amount " + sign + " acr.currency_rate, acr.amount) AS amount, "
                + "acr.currency_rate as rate, acr.note as note, ? as type FROM acc_transfers AS acr "
                + "LEFT JOIN acc_invoice AS inv ON acr.invoice_id = inv.id "
                + "WHERE acr.acc_category_id = ? AND (date(inv.creation_date) BETWEEN ? AND ?) "
                + "AND inv.school_id = ? AND inv.acc_invoice_type_id = 1 "
                + "UNION ALL "
                + "SELECT tr.date_time AS creation_date, "
                + "IF(tr.acc_currency_id != ?, tr.amount " + sign + " tr.currency_rate, tr.amount) AS amount, "
                + "tr.currency_rate as rate, tr.note as note, ? as type FROM acc_transactions AS tr "
                + "WHERE tr.acc_category_id = ? AND (date(tr.date_time) BETWEEN ? AND ?) AND tr.school_id = ? ) t "
                + "ORDER BY creation_date";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, currency_id);
        stat.setString(2, myUI.getMessage(Messages.Accrual));
        stat.setInt(3, acc_category_id);
        stat.setDate(4, new java.sql.Date(from.getTime()));
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, school_id);
        stat.setInt(7, currency_id);
        stat.setString(8, myUI.getMessage(Messages.Payout));
        stat.setInt(9, acc_category_id);
        stat.setDate(10, new java.sql.Date(from.getTime()));
        stat.setDate(11, new java.sql.Date(till.getTime()));
        stat.setInt(12, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Type), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Accrual), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Payout), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Balance), String.class, "0.00");
        int i = 0;
        double currentBalance = exec_salary_balance(school_id, acc_category_id, currency_id, from), totalAccruals = 0.0;
        double prevBalance = currentBalance;
        Item item;

        while (result.next()) {
            item = container.addItem(++i);
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format(result.getDate("t.creation_date")));
            item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(result.getString("t.type"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("t.note"));
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(result.getDouble("t.rate"));
            if (result.getString("t.type").equals(myUI.getMessage(Messages.Payout))) {
                item.getItemProperty(myUI.getMessage(Messages.Payout)).setValue(result.getDouble("t.amount"));
                currentBalance -= result.getDouble("t.amount");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Accrual)).setValue(result.getDouble("t.amount"));
                currentBalance += result.getDouble("t.amount");
                totalAccruals += result.getDouble("t.amount");
            }
            if (currentBalance < 0) {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue((Settings.dFormat2.format(currentBalance * -1))
                        + " (" + myUI.getMessage(Messages.Payout).charAt(0) + ")");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(Settings.dFormat2.format(currentBalance)
                        + " (" + myUI.getMessage(Messages.Accrual).charAt(0) + ")");
            }
            t.setColumnFooter(myUI.getMessage(Messages.Balance),
                    item.getItemProperty(myUI.getMessage(Messages.Balance)).getValue().toString());
        }
        if (container.size() > 0) {
            item = container.addItemAt(0, 0);

            String type = myUI.getMessage(Messages.Accrual);
            if (prevBalance < 0) {
                type = myUI.getMessage(Messages.Payout);
                item.getItemProperty(myUI.getMessage(Messages.Payout)).setValue(prevBalance * -1);
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue((Settings.dFormat2.format(prevBalance * -1))
                        + " (" + myUI.getMessage(Messages.Payout).charAt(0) + ")");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Accrual)).setValue(prevBalance);
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(Settings.dFormat2.format(prevBalance)
                        + " (" + myUI.getMessage(Messages.Accrual).charAt(0) + ")");
                totalAccruals += prevBalance;
            }
            item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(type);
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(myUI.getMessage(Messages.PreviousBalance));
        }
        t.setColumnFooter(myUI.getMessage(Messages.Accrual), Settings.dFormat2.format(totalAccruals));
        t.setColumnFooter(myUI.getMessage(Messages.Payout), Settings.dFormat2.format(totalAccruals - currentBalance));
        t.setContainerDataSource(container);
    }

    public void exec_income_expense_account_statement(MyVaadinUI myUI, int acc_category_id, Date from,
                                                      Date till, Table t, int school_id) throws SQLException {
        String sql = "SELECT t.date_time AS creation_date, "
                + "IF(c.acc_currency_id != 1, t.amount * t.currency_rate, t.amount) AS amount, "
                + "t.currency_rate as rate, t.note as note, if(t.acc_type_id = 1, ?, ?) as type "
                + "FROM acc_transactions AS t "
                + "LEFT JOIN acc_cashbox as c on c.id = t.acc_cashbox_id "
                + "WHERE t.acc_category_id = ? AND (date(t.date_time) BETWEEN ? AND ?) AND t.school_id = ? "
                + "ORDER BY t.date_time";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, myUI.getMessage(Messages.Income));
        stat.setString(2, myUI.getMessage(Messages.Expense));
        stat.setInt(3, acc_category_id);
        stat.setDate(4, new java.sql.Date(from.getTime()));
        stat.setDate(5, new java.sql.Date(till.getTime()));
        stat.setInt(6, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Type), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Income), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Expense), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Balance), String.class, "0.00");
        int i = 0;
        double currentBalance = exec_income_expense_balance(school_id, acc_category_id, from), totalIncomes = 0.0;
        double prevBalance = currentBalance;
        Item item;

        while (result.next()) {
            item = container.addItem(++i);
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format(result.getDate("creation_date")));
            item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(result.getString("type"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(result.getString("note"));
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(result.getDouble("rate"));
            if (result.getString("type").equals(myUI.getMessage(Messages.Expense))) {
                item.getItemProperty(myUI.getMessage(Messages.Expense)).setValue(result.getDouble("amount"));
                currentBalance -= result.getDouble("amount");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Income)).setValue(result.getDouble("amount"));
                currentBalance += result.getDouble("amount");
                totalIncomes += result.getDouble("amount");
            }
            if (currentBalance < 0) {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue((Settings.dFormat2.format(currentBalance * -1))
                        + " (" + myUI.getMessage(Messages.Expense).charAt(0) + ")");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(Settings.dFormat2.format(currentBalance)
                        + " (" + myUI.getMessage(Messages.Income).charAt(0) + ")");
            }
            t.setColumnFooter(myUI.getMessage(Messages.Balance),
                    item.getItemProperty(myUI.getMessage(Messages.Balance)).getValue().toString());
        }
        if (container.size() > 0) {
            item = container.addItemAt(0, 0);

            String type = myUI.getMessage(Messages.Income);
            if (prevBalance < 0) {
                type = myUI.getMessage(Messages.Expense);
                item.getItemProperty(myUI.getMessage(Messages.Expense)).setValue(prevBalance * -1);
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue((Settings.dFormat2.format(prevBalance * -1))
                        + " (" + myUI.getMessage(Messages.Expense).charAt(0) + ")");
            } else {
                item.getItemProperty(myUI.getMessage(Messages.Income)).setValue(prevBalance);
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(Settings.dFormat2.format(prevBalance)
                        + " (" + myUI.getMessage(Messages.Income).charAt(0) + ")");
                totalIncomes += prevBalance;
            }
            item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(type);
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(myUI.getMessage(Messages.PreviousBalance));
        }
        t.setColumnFooter(myUI.getMessage(Messages.Income), Settings.dFormat2.format(totalIncomes));
        t.setColumnFooter(myUI.getMessage(Messages.Expense), Settings.dFormat2.format(totalIncomes - currentBalance));
        t.setContainerDataSource(container);
    }

    public void exec_incomes_expenses(MyVaadinUI myUI, FilterTreeTable categoriesTable, Date from, Date till,
                                      FormattedTreeTable t, int school_id) throws SQLException {
        Set<Integer> selectedCategoryIds = Settings.getChild_ids(
                (HierarchicalContainer) categoriesTable.getContainerDataSource(),
                (Set<?>) categoriesTable.getValue());
        String sql = "SELECT cat.id, IFNULL(CONCAT(cat.parent_code, '.', cat.code), cat.code) AS code, cat.name AS name, " +
                "SUM(IF(t.acc_type_id = 1, IF(c.acc_currency_id != 1, t.amount * t.currency_rate, t.amount),0.0)) AS incomes, " +
                "SUM(IF(t.acc_type_id = 2, IF(c.acc_currency_id != 1, t.amount * t.currency_rate, t.amount),0.0)) AS expenses " +
                "FROM acc_transactions AS t " +
                "left join acc_category as cat on cat.id = t.acc_category_id " +
                "left join acc_cashbox as c on c.id = t.acc_cashbox_id " +
                "WHERE t.school_id = ? AND t.acc_category_id IN (" + Settings.convertCollectionToStr(selectedCategoryIds) +
                ") ";
        if (from != null && till != null) {
            sql += "AND date(t.date_time) >= ? AND date(t.date_time) <= ? ";
        } else if (from != null) {
            sql += "AND date(t.date_time) >= ? ";
        } else if (till != null) {
            sql += "AND date(t.date_time) <= ? ";
        }
        sql += " GROUP BY cat.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        int counter = 0;
        stat.setInt(++counter, school_id);
        if (from != null && till != null) {
            stat.setDate(++counter, new java.sql.Date(from.getTime()));
            stat.setDate(++counter, new java.sql.Date(till.getTime()));
        } else if (from != null) {
            stat.setDate(++counter, new java.sql.Date(from.getTime()));
        } else if (till != null) {
            stat.setDate(++counter, new java.sql.Date(till.getTime()));
        }
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Category), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Incomes), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Expenses), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Balance), Double.class, 0.0);
        t.setContainerDataSource(container);
        double total_incomes = 0.0, total_expenses = 0.0;
        for (Object catNext : categoriesTable.getContainerDataSource().getItemIds()) {
            if (selectedCategoryIds.contains(catNext)) {
                Item item = container.addItem(catNext);
                item.getItemProperty(myUI.getMessage(Messages.Code))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Code)).getValue().toString());
                item.getItemProperty(myUI.getMessage(Messages.Category))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Category)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = categoriesTable.getContainerDataSource().getParent(catNext);
                if (parent != null) {
                    if (container.getItem(parent) == null) {
                        item = container.addItem(parent);
                        item.getItemProperty(myUI.getMessage(Messages.Code))
                                .setValue(categoriesTable.getContainerProperty(parent, myUI.getMessage(Messages.Code)).getValue().toString());
                        item.getItemProperty(myUI.getMessage(Messages.Category))
                                .setValue(categoriesTable.getContainerProperty(parent, myUI.getMessage(Messages.Category)).getValue().toString());
                    }
                    container.setParent(catNext, parent);
                }
                if (categoriesTable.getContainerDataSource().getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
                if (categoriesTable.getContainerDataSource().getChildren(parent) != null) {
                    container.setChildrenAllowed(parent, true);
                    t.setCollapsed(parent, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            if (item != null) {
                item.getItemProperty(myUI.getMessage(Messages.Incomes)).setValue(result.getDouble("incomes"));
                item.getItemProperty(myUI.getMessage(Messages.Expenses)).setValue(result.getDouble("expenses"));
                item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(
                        result.getDouble("incomes") - result.getDouble("expenses"));
                total_incomes += result.getDouble("incomes");
                total_expenses += result.getDouble("expenses");
                Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
                while (parent_id != null) {
                    item = container.getItem(parent_id);
                    item.getItemProperty(myUI.getMessage(Messages.Incomes)).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(Messages.Incomes)).getValue()
                                    + result.getDouble("incomes"));
                    item.getItemProperty(myUI.getMessage(Messages.Expenses)).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(Messages.Expenses)).getValue()
                                    + result.getDouble("expenses"));
                    item.getItemProperty(myUI.getMessage(Messages.Balance)).setValue(
                            (Double) item.getItemProperty(myUI.getMessage(Messages.Balance)).getValue()
                                    + result.getDouble("incomes") - result.getDouble("expenses"));
                    parent_id = (Integer) container.getParent(parent_id);
                }
            }
        }
        t.setColumnFooter(myUI.getMessage(Messages.Incomes), Settings.dFormat2.format(total_incomes));
        t.setColumnFooter(myUI.getMessage(Messages.Expenses), Settings.dFormat2.format(total_expenses));
        t.setColumnFooter(myUI.getMessage(Messages.Balance), Settings.dFormat2.format(total_incomes - total_expenses));
    }


    public void exec_account_remains(MyVaadinUI myUI, FilterTreeTable categoriesTable, int currency_id, int school_id,
                                     Date from, Date till, FormattedTreeTable t) throws SQLException {

        Set<Integer> selectedCategoryIds = Settings.getChild_ids(
                (HierarchicalContainer) categoriesTable.getContainerDataSource(),
                (Set<?>) categoriesTable.getValue());
        String sql = "SELECT cat.id, IFNULL(CONCAT(cat.parent_code, '.', cat.code), cat.code) AS code, cat.name AS name, " +
                "IFNULL(acr.amount_som, 0.0) - IFNULL(tr.amount_som, 0.0) AS remain_som, " +
                "IFNULL(acr.amount_usd, 0.0) - IFNULL(tr.amount_usd, 0.0) AS remain_usd, " +
                "IFNULL(IF((aacr.acc_currency_id <> 2), (aacr.amount / aacr.currency_rate), aacr.amount), 0.0) AS salary_usd, " +
                "IFNULL(IF((aacr.acc_currency_id <> 1), (aacr.amount * aacr.currency_rate), aacr.amount), 0.0) AS salary_som " +
                "FROM acc_category AS cat " +
                "LEFT JOIN " +
                "(SELECT inv.school_id AS school_id, acr.acc_category_id AS acc_category_id, " +
                "IFNULL(SUM(IF((acr.acc_currency_id <> 2), (acr.amount / acr.currency_rate), acr.amount)), 0.0) AS amount_usd, " +
                "IFNULL(SUM(IF((acr.acc_currency_id <> 1), (acr.amount * acr.currency_rate), acr.amount)), 0.0) AS amount_som, " +
                "MAX(inv.id) AS invoice_id FROM acc_transfers acr " +
                "LEFT JOIN acc_invoice inv ON inv.id = acr.invoice_id AND inv.acc_invoice_type_id = 1 AND " +
                "inv.school_id = ? ";
        if (from != null && till != null) {
            sql += "AND inv.creation_date >= ? AND inv.creation_date <= ? ";
        } else if (from != null) {
            sql += "AND inv.creation_date >= ? ";
        } else if (till != null) {
            sql += "AND inv.creation_date <= ? ";
        }
        sql += "GROUP BY acr.acc_category_id) AS acr ON acr.acc_category_id = cat.id " +
                "LEFT JOIN " +
                "(SELECT tr.school_id AS school_id, tr.acc_category_id AS acc_category_id, IFNULL(SUM(IF((tr.acc_currency_id <> 2), " +
                "(tr.amount / tr.currency_rate), tr.amount)), 0.0) AS amount_usd, IFNULL(SUM(IF((tr.acc_currency_id <> 1), " +
                "(tr.amount * tr.currency_rate), tr.amount)), 0.0) AS amount_som FROM acc_transactions tr " +
                "WHERE tr.school_id = ? ";
        if (from != null && till != null) {
            sql += "AND tr.date_time >= ? AND tr.date_time <= ? ";
        } else if (from != null) {
            sql += "AND tr.date_time >= ? ";
        } else if (till != null) {
            sql += "AND tr.date_time <= ? ";
        }
        sql += "GROUP BY tr.acc_category_id) AS tr ON tr.acc_category_id = cat.id " +
                "LEFT JOIN acc_transfers AS aacr ON aacr.invoice_id = acr.invoice_id AND aacr.acc_category_id = cat.id " +
                "WHERE cat.id IN (" + Settings.convertCollectionToStr(selectedCategoryIds) + ")";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        int counter = 0;
        stat.setInt(++counter, school_id);
        if (from != null && till != null) {
            stat.setDate(++counter, new java.sql.Date(from.getTime()));
            stat.setDate(++counter, new java.sql.Date(till.getTime()));
        } else if (from != null) {
            stat.setDate(++counter, new java.sql.Date(from.getTime()));
        } else if (till != null) {
            stat.setDate(++counter, new java.sql.Date(till.getTime()));
        }
        stat.setInt(++counter, school_id);
        if (from != null && till != null) {
            stat.setDate(++counter, new java.sql.Date(from.getTime()));
            stat.setDate(++counter, new java.sql.Date(till.getTime()));
        } else if (from != null) {
            stat.setDate(++counter, new java.sql.Date(from.getTime()));
        } else if (till != null) {
            stat.setDate(++counter, new java.sql.Date(till.getTime()));
        }
        ResultSet result = stat.executeQuery();
        HierarchicalContainer container = new HierarchicalContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Category), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Remain), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Salary), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Ratio), Double.class, null);
        t.setContainerDataSource(container);
        double total_remains = 0.0, total_salaries = 0.0;
        for (Object catNext : categoriesTable.getContainerDataSource().getItemIds()) {
            if (selectedCategoryIds.contains(catNext)) {
                Item item = container.addItem(catNext);
                item.getItemProperty(myUI.getMessage(Messages.Code))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Code)).getValue().toString());
                item.getItemProperty(myUI.getMessage(Messages.Category))
                        .setValue(categoriesTable.getContainerProperty(catNext, myUI.getMessage(Messages.Category)).getValue().toString());
                container.setChildrenAllowed(catNext, false);
                Object parent = categoriesTable.getContainerDataSource().getParent(catNext);
                if (parent != null) {
                    if (container.getItem(parent) == null) {
                        item = container.addItem(parent);
                        item.getItemProperty(myUI.getMessage(Messages.Code))
                                .setValue(categoriesTable.getContainerProperty(parent, myUI.getMessage(Messages.Code)).getValue().toString());
                        item.getItemProperty(myUI.getMessage(Messages.Category))
                                .setValue(categoriesTable.getContainerProperty(parent, myUI.getMessage(Messages.Category)).getValue().toString());
                    }
                    container.setParent(catNext, parent);
                }
                if (categoriesTable.getContainerDataSource().getChildren(catNext) != null) {
                    container.setChildrenAllowed(catNext, true);
                    t.setCollapsed(catNext, false);
                }
                if (categoriesTable.getContainerDataSource().getChildren(parent) != null) {
                    container.setChildrenAllowed(parent, true);
                    t.setCollapsed(parent, false);
                }
            }
        }
        while (result.next()) {
            Item item = container.getItem(result.getInt("cat.id"));
            if (item != null) {
                if (currency_id == 1) {
                    item.getItemProperty(myUI.getMessage(Messages.Remain)).setValue(result.getDouble("remain_som"));
                    item.getItemProperty(myUI.getMessage(Messages.Salary)).setValue(result.getDouble("salary_som"));
                    total_remains += result.getDouble("remain_som");
                    total_salaries += result.getDouble("salary_som");
                    if (result.getDouble("salary_som") != 0.0) {
                        item.getItemProperty(myUI.getMessage(Messages.Ratio)).setValue(result.getDouble("remain_som") / result.getDouble("salary_som"));
                    }
                    Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
                    while (parent_id != null) {
                        item = container.getItem(parent_id);
                        item.getItemProperty(myUI.getMessage(Messages.Remain)).setValue(
                                (Double) item.getItemProperty(myUI.getMessage(Messages.Remain)).getValue() + result.getDouble("remain_som"));
                        item.getItemProperty(myUI.getMessage(Messages.Salary)).setValue(
                                (Double) item.getItemProperty(myUI.getMessage(Messages.Salary)).getValue() + result.getDouble("salary_som"));
                        if ((Double) item.getItemProperty(myUI.getMessage(Messages.Salary)).getValue() != 0.0) {
                            item.getItemProperty(myUI.getMessage(Messages.Ratio)).setValue(
                                    (Double) item.getItemProperty(myUI.getMessage(Messages.Remain)).getValue()
                                            / (Double) item.getItemProperty(myUI.getMessage(Messages.Salary)).getValue());
                        }
                        parent_id = (Integer) container.getParent(parent_id);
                    }
                } else {
                    item.getItemProperty(myUI.getMessage(Messages.Remain)).setValue(result.getDouble("remain_usd"));
                    item.getItemProperty(myUI.getMessage(Messages.Salary)).setValue(result.getDouble("salary_usd"));
                    total_remains += result.getDouble("remain_usd");
                    total_salaries += result.getDouble("salary_usd");
                    if (result.getDouble("salary_usd") != 0.0) {
                        item.getItemProperty(myUI.getMessage(Messages.Ratio)).setValue(
                                result.getDouble("remain_usd") / result.getDouble("salary_usd"));
                    }
                    Integer parent_id = (Integer) container.getParent(result.getInt("cat.id"));
                    while (parent_id != null) {
                        item = container.getItem(parent_id);
                        item.getItemProperty(myUI.getMessage(Messages.Remain)).setValue(
                                (Double) item.getItemProperty(myUI.getMessage(Messages.Remain)).getValue() + result.getDouble("remain_usd"));
                        item.getItemProperty(myUI.getMessage(Messages.Salary)).setValue(
                                (Double) item.getItemProperty(myUI.getMessage(Messages.Salary)).getValue() + result.getDouble("salary_usd"));
                        if ((Double) item.getItemProperty(myUI.getMessage(Messages.Salary)).getValue() != 0.0) {
                            item.getItemProperty(myUI.getMessage(Messages.Ratio)).setValue(
                                    (Double) item.getItemProperty(myUI.getMessage(Messages.Remain)).getValue()
                                            / (Double) item.getItemProperty(myUI.getMessage(Messages.Salary)).getValue());
                        }
                        parent_id = (Integer) container.getParent(parent_id);
                    }
                }
            }
        }
        t.setColumnFooter(myUI.getMessage(Messages.Remain), Settings.dFormat2.format(total_remains));
        t.setColumnFooter(myUI.getMessage(Messages.Salary), Settings.dFormat2.format(total_salaries));
        if (total_salaries != 0.0) {
            t.setColumnFooter(myUI.getMessage(Messages.Ratio), Settings.dFormat2.format(total_remains / total_salaries));
        }
    }

    public IndexedContainer exec_report_by_date(MyVaadinUI myUI, int type_id, int school_id, int cashbox_id, Date from_date, Date till_date,
                                                FilterTreeTable categoriesTable) throws SQLException {
        Set<Integer> selectedIds = new HashSet<>((Set<Integer>) categoriesTable.getValue());
        for (Object next : (Set<Integer>) categoriesTable.getValue()) {
            if (categoriesTable.getChildren(next) != null) {
                selectedIds.addAll(new HashSet<>(
                        (Collection<Integer>) categoriesTable.getChildren(next)));
            }
        }
        String sql = "SELECT t.id, date(t.date_time), ifnull(concat(ac.parent_code,'.',ac.code), ac.code) as code, ac.name as category, "
                + "cur.name, t.currency_rate, t.amount, t.note, concat(e.name, ' ', e.surname) as fullname "
                + "FROM acc_transactions as t "
                + "left join acc_category as ac on ac.id = t.acc_category_id "
                + "left join acc_cashbox as c on c.id = t.acc_cashbox_id "
                + "left join acc_currency as cur on cur.id = c.acc_currency_id "
                + "left join employee as e on e.id = t.employee_id "
                + "where t.school_id = ? and date(t.date_time) >= ? and date(t.date_time) <= ? "
                + "and t.acc_type_id = ? ";
        if (cashbox_id != 0) {
            sql += "and t.acc_cashbox_id = ? ";
        }
        sql += "and t.acc_category_id in (" + Settings.convertCollectionToStr(selectedIds) + ") "
                + "order by t.date_time asc";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setDate(2, new java.sql.Date(from_date.getTime()));
        stat.setDate(3, new java.sql.Date(till_date.getTime()));
        stat.setInt(4, type_id);
        if (cashbox_id != 0) {
            stat.setInt(5, cashbox_id);
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Code), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Category), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Currency), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Accountant), String.class, null);
        while (result.next()) {
            Item item = container.addItem(result.getInt("t.id"));
            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format(
                    result.getDate("date(t.date_time)")));
            item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(
                    result.getString("code"));
            item.getItemProperty(myUI.getMessage(Messages.Category)).setValue(
                    result.getString("category"));
            item.getItemProperty(myUI.getMessage(Messages.Currency)).setValue(
                    result.getString("cur.name"));
            item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(
                    result.getDouble("t.currency_rate"));
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                    result.getDouble("t.amount"));
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                    result.getString("t.note"));
            item.getItemProperty(myUI.getMessage(Messages.Accountant)).setValue(
                    result.getString("fullname"));
        }
        return container;
    }

    public void execSQL_Plan_Payments(MyVaadinUI myUI, int year_id, String edu_statuses_ids, int school_id,
                                      Date academic_year_start_date, Date academic_year_end_date, Table t) throws SQLException {

        String sql = "SELECT months.name AS month_name, months.id, i_temp.amn AS inst, p_temp.amn AS payments, "
                + "in_temp.amn AS income, out_temp.amn AS outcome "
                + "FROM months "
                + "LEFT JOIN (SELECT SUM(inst.amount) AS amn, MONTH(inst.date_of_payment) AS mnth "
                + "FROM student_installement_plan AS inst "
                + "LEFT JOIN student AS st ON st.id = inst.student_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "WHERE inst.year_id = ? AND st.school_id = ? AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "GROUP BY MONTH(inst.date_of_payment)) AS i_temp ON i_temp.mnth = months.id "
                + "LEFT JOIN (SELECT SUM(IF(pay.payment_category_id = 3, - pay.amount, pay.amount)) AS amn, "
                + "MONTH(pay.modification_date) AS mnth FROM student_payments AS pay "
                + "LEFT JOIN student AS st ON st.id = pay.student_id "
                + "LEFT JOIN view_student_class_status as vcs on vcs.student_id = st.id and vcs.year_id = ? "
                + "WHERE pay.year_id = ? AND st.school_id = ? AND pay.payment_category_id IN (1, 2, 3) "
                + "AND vcs.education_status_id IN (" + edu_statuses_ids + ") "
                + "GROUP BY MONTH(pay.modification_date)) AS p_temp ON p_temp.mnth = months.id "
                + "LEFT JOIN (SELECT SUM(if(c.acc_currency_id = 1, tr.amount, ROUND(tr.amount*tr.currency_rate,2))) AS amn, "
                + "MONTH(tr.date_time) AS mnth FROM acc_transactions AS tr "
                + "LEFT JOIN acc_cashbox as c ON c.id = tr.acc_cashbox_id "
                + "WHERE tr.school_id = ? AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ? "
                + "AND tr.acc_category_id IN (SELECT acc_category_id FROM payment_category WHERE id IN (1, 2)) "
                + "GROUP BY MONTH(date_time)) AS in_temp ON in_temp.mnth = months.id LEFT JOIN "
                + "(SELECT SUM(if(c.acc_currency_id = 1, tr.amount, ROUND(tr.amount*tr.currency_rate,2))) AS amn, "
                + "MONTH(tr.date_time) AS mnth FROM acc_transactions AS tr "
                + "LEFT JOIN acc_cashbox as c ON c.id = tr.acc_cashbox_id "
                + "WHERE tr.school_id = ? AND DATE(tr.date_time) >= ? AND DATE(tr.date_time) <= ? "
                + "AND tr.acc_type_id = 2 GROUP BY MONTH(date_time)) AS out_temp ON out_temp.mnth = months.id "
                + "ORDER BY months.order_num";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, year_id);
        stat.setInt(3, school_id);
        stat.setInt(4, year_id);
        stat.setInt(5, year_id);
        stat.setInt(6, school_id);
        stat.setInt(7, school_id);
        stat.setDate(8, new java.sql.Date(academic_year_start_date.getTime()));
        stat.setDate(9, new java.sql.Date(academic_year_end_date.getTime()));
        stat.setInt(10, school_id);
        stat.setDate(11, new java.sql.Date(academic_year_start_date.getTime()));
        stat.setDate(12, new java.sql.Date(academic_year_end_date.getTime()));
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Month), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.InstallmentPlan), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Payments), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Debt), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Incomes), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Expenses), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Difference), Double.class, 0.0);
        t.setContainerDataSource(container);
        double totalInst = 0.0, totalPayments = 0.0, totalDebt = 0.0, totalIncomes = 0.0, totalOutcomes = 0.0, totalTransactions = 0.0;

        while (result.next()) {
            Item item = container.addItem(result.getInt("months.id"));
            item.getItemProperty(myUI.getMessage(Messages.Month)).setValue(
                    result.getString("month_name"));
            item.getItemProperty(myUI.getMessage(Messages.InstallmentPlan)).setValue(
                    result.getDouble("inst"));
            totalInst += result.getDouble("inst");
            item.getItemProperty(myUI.getMessage(Messages.Payments)).setValue(
                    result.getDouble("payments"));
            totalPayments += result.getDouble("payments");
            item.getItemProperty(myUI.getMessage(Messages.Debt)).setValue(
                    result.getDouble("inst") - result.getDouble("payments"));
            totalDebt += (result.getDouble("inst") - result.getDouble("payments"));
            item.getItemProperty(myUI.getMessage(Messages.Incomes)).setValue(
                    result.getDouble("income"));
            totalIncomes += result.getDouble("income");
            item.getItemProperty(myUI.getMessage(Messages.Expenses)).setValue(
                    result.getDouble("outcome"));
            totalOutcomes += result.getDouble("outcome");
            item.getItemProperty(myUI.getMessage(Messages.Difference)).setValue(
                    result.getDouble("income") - result.getDouble("outcome"));
            totalTransactions += (result.getDouble("income") - result.getDouble("outcome"));
        }
        t.setColumnFooter(myUI.getMessage(Messages.InstallmentPlan), Settings.dFormat2.format(totalInst));
        t.setColumnFooter(myUI.getMessage(Messages.Payments), Settings.dFormat2.format(totalPayments));
        t.setColumnFooter(myUI.getMessage(Messages.Debt), Settings.dFormat2.format(totalDebt));
        t.setColumnFooter(myUI.getMessage(Messages.Incomes), Settings.dFormat2.format(totalIncomes));
        t.setColumnFooter(myUI.getMessage(Messages.Expenses), Settings.dFormat2.format(totalOutcomes));
        t.setColumnFooter(myUI.getMessage(Messages.Difference), Settings.dFormat2.format(totalTransactions));
    }

    public AccTransaction exec_low_balance(Connection conn, int school_id, int cashbox_id, Date date, double old_amount,
                                           double new_amount, int inOut) throws SQLException {

        Date d = exec_nearestDate(conn, school_id, cashbox_id, date);
        if (d == null) {
            return null;
        }
        String sql = "SELECT balances_table.date_time, round(balances_table.amount,2) as amount, "
                + "round(balances_table.balance,2) as balance FROM "
                + "(SELECT gr_transactions.date_time AS date_time, gr_transactions.amount AS amount, "
                + "(@runtot:=gr_transactions.amount + @runtot) AS balance FROM "
                + "(SELECT DATE(tr.date_time) AS date_time, SUM(IF(tr.acc_type_id = 1, tr.amount, -tr.amount)) AS amount FROM "
                + "acc_transactions AS tr where tr.school_id = ? and tr.acc_cashbox_id = ? "
                + "GROUP BY DATE(tr.date_time) ORDER BY DATE(tr.date_time)) AS gr_transactions, "
                + "(SELECT @runtot:=0) c) AS balances_table WHERE balances_table.date_time >= ? "
                + "and round(balances_table.balance,2) + ? + ? < 0";
        PreparedStatement stat = conn.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, cashbox_id);
        stat.setDate(3, new java.sql.Date(d.getTime()));
        if (inOut == 1) {
            stat.setDouble(4, -old_amount);
            stat.setDouble(5, new_amount);
        } else {
            stat.setDouble(4, old_amount);
            stat.setDouble(5, -new_amount);
        }
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            AccTransaction tr = new AccTransaction();
            tr.setDate(result.getDate("balances_table.date_time"));
            if (inOut == 1) {
                tr.setLimit(old_amount - result.getDouble("balance"));
                tr.setOverLimit((result.getDouble("balance") - old_amount + new_amount) * -1);
            } else {
                tr.setLimit(result.getDouble("balance") + old_amount);
                tr.setOverLimit((result.getDouble("balance") + old_amount - new_amount) * -1);
            }
            try {
                DbCashbox dbc = new DbCashbox();
                dbc.connect();
                tr.setCashbox(dbc.getCashboxById(cashbox_id));
                dbc.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            return tr;
        }
        return null;
    }

    public Date exec_nearestDate(Connection conn, int school_id, int cashbox_id, Date date) throws SQLException {

        String sql = "SELECT balances_table.date_time, balances_table.amount, balances_table.balance FROM "
                + "(SELECT gr_transactions.date_time AS date_time, gr_transactions.amount AS amount, "
                + "(@runtot:=gr_transactions.amount + @runtot) AS balance FROM "
                + "(SELECT DATE(tr.date_time) AS date_time, SUM(IF(tr.acc_type_id = 1, tr.amount, -tr.amount)) AS amount FROM "
                + "acc_transactions AS tr where tr.school_id = ? and tr.acc_cashbox_id = ? "
                + "GROUP BY DATE(tr.date_time) ORDER BY DATE(tr.date_time)) AS gr_transactions, (SELECT @runtot:=0) c) AS balances_table "
                + "WHERE balances_table.date_time <= ?";
        PreparedStatement stat = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        stat.setInt(1, school_id);
        stat.setInt(2, cashbox_id);
        stat.setDate(3, new java.sql.Date(date.getTime()));
        ResultSet result = stat.executeQuery();
        while (result.last()) {
            return result.getDate("balances_table.date_time");
        }
        return null;
    }

    public int getMaxOrderNum(int school_id, int acc_type_id) throws SQLException {
        int maxValue;
        String sql = "select (ifnull(max(tr.order_number), 0) + 1) as max_plus1 from acc_transactions as tr " +
                "where tr.school_id = ? and tr.acc_type_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, school_id);
        stat.setInt(2, acc_type_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            maxValue = (result.getInt("max_plus1"));
        } else {
            maxValue = 1;
        }
        return maxValue;
    }

    public int exec_update_order_number(String id, int school_id, int acc_type_id) throws SQLException {
        int order_number = getMaxOrderNum(school_id, acc_type_id);
        String sql = "update acc_transactions set order_number = ? WHERE id = ? and order_number IS NULL";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, order_number);
        stat.setString(2, id);
        if (stat.executeUpdate() != 0) {
            return order_number;
        } else {
            return 0;
        }
    }
}
