/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.CustomTable;
import com.vaadin.ui.HorizontalLayout;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.EmployeeMessage;
import kg.alex.indigo.domain.OrderMessage;
import kg.alex.indigo.i18n.Messages;
import kg.alex.indigo.ui.SendOrderView;
import org.tepi.filtertable.FilterTable;

import java.sql.*;

public class DbOrderMessage extends BaseDb {

    public DbOrderMessage() throws Exception {
        super();
    }

    public int exec_insert(OrderMessage orderMessage) throws SQLException {
        String sql = "INSERT INTO order_messages (message, order_number, order_title, order_content, student_id, " +
                "creation_date, employee_id, discount, student, year_id, discount_unit_id, currency_rate) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (orderMessage.getMessage() != null) {
            stat.setString(1, orderMessage.getMessage());
        } else {
            stat.setNull(1, Types.VARCHAR);
        }
        stat.setString(2, orderMessage.getOrder_number());
        stat.setString(3, orderMessage.getTitle());
        stat.setString(4, orderMessage.getContent());
        if (orderMessage.getStudent_id() != 0) {
            stat.setInt(5, orderMessage.getStudent_id());
        } else {
            stat.setNull(5, Types.INTEGER);
        }
        stat.setDate(6, new Date(orderMessage.getDate().getTime()));
        stat.setInt(7, orderMessage.getEmployee_id());
        stat.setDouble(8, orderMessage.getDiscount());
        if (orderMessage.getStudent() != null) {
            stat.setString(9, orderMessage.getStudent());
        } else {
            stat.setNull(9, Types.VARCHAR);
        }
        stat.setInt(10, orderMessage.getYear_id());
        stat.setInt(11, orderMessage.getDiscount_unit_id());
        stat.setDouble(12, orderMessage.getCurrencyRate());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public IndexedContainer execSQL(MyVaadinUI myUi, int employee_id, FilterTable t, SendOrderView view) throws SQLException {
        String sql = "SELECT em.id, concat(e.name, ' ', e.surname) as employee, y.name, " +
                "concat(st.name, ' ', st.surname) as student, om.id, om.discount, om.student as student_info, " +
                "om.creation_date, om.order_number, om.message, om.order_content, om.order_title, om.student_id, " +
                "mst.id, mst.name FROM employee_message AS em " +
                "left join employee as e on e.id = em.employee_id " +
                "left join message_status as mst on mst.id = em.message_status_id " +
                "left join order_messages as om on om.id = em.order_messages_id " +
                "left join year as y on y.id = om.year_id " +
                "left join student as st on st.id = om.student_id ";
        if (employee_id != 0) {
            sql += " WHERE om.employee_id = ? ";
        }
        sql += "order by om.year_id DESC, om.creation_date DESC, CAST(substring(order_number, 9, 15) AS SIGNED) DESC";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        if (employee_id != 0) {
            stat.setInt(1, employee_id);
        }
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Employee), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.OrderNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Message), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Student), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Year), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Discount), Integer.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.button, HorizontalLayout.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        int unread = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("em.id"));
            item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.df.format(
                    result.getDate("om.creation_date")));
            item.getItemProperty(myUi.getMessage(Messages.Employee)).setValue(result.getString("employee"));
            if (result.getInt("om.student_id") == 0) {
                item.getItemProperty(myUi.getMessage(Messages.Student)).setValue(result.getString("student_info"));
            } else {
                item.getItemProperty(myUi.getMessage(Messages.Student)).setValue(result.getString("student"));
            }
            item.getItemProperty(myUi.getMessage(Messages.Year)).setValue(result.getString("y.name"));
            item.getItemProperty(myUi.getMessage(Messages.Discount)).setValue(result.getInt("discount"));
            item.getItemProperty(myUi.getMessage(Messages.Message)).setValue(result.getString("om.message"));
            item.getItemProperty(myUi.getMessage(Messages.Title)).setValue(result.getString("om.order_title"));
            item.getItemProperty(myUi.getMessage(Messages.OrderNumber)).setValue(result.getString("om.order_number"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(result.getString("mst.name"));
            item.getItemProperty(Settings.status_id).setValue(result.getInt("mst.id"));
            EmployeeMessage employeeMessage = new EmployeeMessage();
            employeeMessage.setId(result.getInt("em.id"));
            employeeMessage.setOrder_message_id(result.getInt("om.id"));
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.addComponent(view.createButton(myUi.getMessage(Messages.DeleteButton),
                    Settings.actDelete, FontAwesome.BAN, employeeMessage));
            OrderMessage orderMessage = new OrderMessage();
            orderMessage.setTitle(result.getString("om.order_title"));
            orderMessage.setContent(result.getString("om.order_content"));
            orderMessage.setOrder_number(result.getString("om.order_number"));
            orderMessage.setDate(result.getDate("om.creation_date"));
            hl.addComponent(view.createButton(myUi.getMessage(Messages.ViewDocument),
                    Settings.actPdf, FontAwesome.FILE_PDF_O, orderMessage));
            item.getItemProperty(Settings.button).setValue(hl);
            if (result.getInt("mst.id") == 2) {
                unread++;
            }
        }
        if (t != null) {
            t.setContainerDataSource(container);
            t.setColumnAlignment(myUi.getMessage(Messages.Discount), CustomTable.Align.RIGHT);
            t.setColumnFooter(myUi.getMessage(Messages.Title),
                    myUi.getMessage(Messages.UnRead) + ": " + unread);
            t.setColumnFooter(myUi.getMessage(Messages.Status),
                    myUi.getMessage(Messages.Total) + ": " + container.size());
        }
        return container;
    }

    public double execSQL_discountAmount(int year_id, int student_id, String studentFullName,
                                         String discount_unit_ids) throws SQLException {
        String sql = "SELECT (CASE WHEN discount_unit_id = 3 and currency_rate != 0.0 " +
                "THEN discount / currency_rate " +
                "WHEN discount_unit_id in (1,2) THEN discount ELSE 10000.0 END) as discount " +
                "FROM order_messages WHERE id = (SELECT MAX(id) FROM order_messages " +
                "WHERE year_id = ? AND discount_unit_id in (" + discount_unit_ids + ") AND " +
                "(student_id = ? OR student_id IS NULL AND " +
                "(LOWER(SUBSTRING_INDEX(order_content, 'Сапаттын', 1)) LIKE '%" + studentFullName.toLowerCase() + "%' " +
                "OR transliterate_func(LOWER(SUBSTRING_INDEX(order_content, 'Сапаттын', 1))) LIKE '%"
                + studentFullName.toLowerCase().replace(" ", "-") + "%')))";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, year_id);
        stat.setInt(2, student_id);
        ResultSet result = stat.executeQuery();
        double discountAmount = 0.0;
        if (result.next()) {
            discountAmount = result.getDouble("discount");
        }
        return discountAmount;
    }
}
