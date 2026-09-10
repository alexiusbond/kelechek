/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.domain.EmployeeMessage;
import kg.alex.kelechek.domain.OrderMessage;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.pdf.OrderPdf;
import kg.alex.kelechek.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeMessage extends BaseDb {
    static final Logger logger = LogManager.getLogger(DbEmployeeMessage.class);

    public DbEmployeeMessage() throws Exception {
        super();
    }

    public int exec_insert(EmployeeMessage em) throws SQLException {
        String sql = "INSERT INTO employee_message (order_messages_id, employee_id, " +
                "message_status_id, modification_date) "
                + "VALUES(?,?,?,NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, em.getOrder_message_id());
        stat.setInt(2, em.getEmployee_id());
        stat.setInt(3, em.getMessage_status_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(int order_messages_id, int employee_id, int status_id) throws SQLException {
        String sql = "update employee_message set message_status_id = ?, " +
                "modification_date = NOW() where order_messages_id = ? and employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, status_id);
        stat.setInt(2, order_messages_id);
        stat.setInt(3, employee_id);
        return stat.executeUpdate();
    }

    public void execSQL(MyVaadinUI myUi, int employee_id, int school_id, FilterTable t) throws SQLException {
        String sql = "SELECT om.id, e.id, ifnull(CONCAT(st.name, ' ', st.surname), om.student) AS student, " +
                "om.creation_date, om.order_number, om.message, om.order_content, om.discount, om.order_title, " +
                "mst.id, mst.name, du.name FROM employee_message AS em " +
                "left join employee as e on e.id = em.employee_id " +
                "left join message_status as mst on mst.id = em.message_status_id " +
                "left join order_messages as om on om.id = em.order_messages_id " +
                "left join discount_unit as du on du.id = om.discount_unit_id " +
                "left join student as st on st.id = om.student_id " +
                "WHERE em.employee_id = ? OR em.employee_id IN " +
                "(SELECT eo.employee_id FROM hr_employee_order AS eo " +
                "WHERE eo.school_id = ? AND eo.hr_orders_id = 1 AND " +
                "(eo.to_date IS NULL OR DATE(eo.to_date) >= DATE(om.creation_date) " +
                "OR eo.to_date >= NOW())) group by om.id order by mst.id desc, om.creation_date desc";

        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUi.getMessage(Messages.Date), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.OrderNumber), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Student), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Discount), String.class, null);
        container.addContainerProperty(myUi.getMessage(Messages.Status), String.class, null);
        container.addContainerProperty(Settings.button, Button.class, null);
        container.addContainerProperty(Settings.status_id, Integer.class, 0);
        int unread = 0;
        while (result.next()) {
            Item item = container.addItem(result.getInt("om.id"));
            item.getItemProperty(myUi.getMessage(Messages.Date)).setValue(Settings.df.format(
                    result.getDate("om.creation_date")));
            item.getItemProperty(myUi.getMessage(Messages.Student)).setValue(result.getString("student"));
            item.getItemProperty(myUi.getMessage(Messages.Discount)).setValue(
                    Settings.round(result.getDouble("om.discount"), 2) + " "
                            + result.getString("du.name"));
            item.getItemProperty(myUi.getMessage(Messages.OrderNumber)).setValue(result.getString("om.order_number"));
            item.getItemProperty(myUi.getMessage(Messages.Status)).setValue(result.getString("mst.name"));
            item.getItemProperty(Settings.status_id).setValue(result.getInt("mst.id"));
            OrderMessage orderMessage = new OrderMessage();
            orderMessage.setId(result.getInt("om.id"));
            orderMessage.setEmployee_id(result.getInt("e.id"));
            orderMessage.setTitle(result.getString("om.order_title"));
            orderMessage.setContent(result.getString("om.order_content"));
            orderMessage.setOrder_number(result.getString("om.order_number"));
            orderMessage.setDate(result.getDate("om.creation_date"));
            Button btn = new Button();
            btn.setDescription(myUi.getMessage(Messages.ViewDocument));
            btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
            btn.addStyleName(ValoTheme.BUTTON_TINY);
            btn.setIcon(FontAwesome.FILE_PDF_O);
            btn.setData(orderMessage);
            btn.addClickListener((Button.ClickListener) clickEvent -> {
                try {
                    DbEmployeeMessage dbCon = new DbEmployeeMessage();
                    dbCon.connect();
                    OrderMessage om = (OrderMessage) clickEvent.getButton().getData();
                    dbCon.exec_update(om.getId(), om.getEmployee_id(), 1);
                    dbCon.close();
                    t.getContainerProperty(om.getId(), Settings.status_id).setValue(1);
                    t.getContainerProperty(om.getId(), myUi.getMessage(Messages.Status))
                            .setValue(myUi.getMessage(Messages.UnRead));
                    int unread1 = Integer.parseInt(t.getColumnFooter(Settings.status_id));
                    if (unread1 > 0) {
                        t.setColumnFooter(Settings.status_id, (--unread1) + "");
                        t.setColumnFooter(myUi.getMessage(Messages.Status),
                                myUi.getMessage(Messages.UnRead) + ": " + unread1);
                    }
                    myUi.repaintMessagesButton();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                new OrderPdf(myUi, (OrderMessage) clickEvent.getButton().getData());
            });
            item.getItemProperty(Settings.button).setValue(btn);
            if (result.getInt("mst.id") == 2) {
                unread++;
            }
        }
        t.setContainerDataSource(container);
        t.setColumnFooter(Settings.status_id, unread + "");
        t.setColumnFooter(myUi.getMessage(Messages.Status),
                myUi.getMessage(Messages.UnRead) + ": " + unread);
        t.setColumnFooter(myUi.getMessage(Messages.Student),
                myUi.getMessage(Messages.Total) + ": " + container.size());
    }

    public boolean isUnread(int employee_id, int school_id) throws SQLException {
        String sql = "SELECT count(*) as val FROM employee_message as em " +
                "LEFT JOIN order_messages as om on om.id = em.order_messages_id " +
                "WHERE (em.employee_id = ? OR em.employee_id IN " +
                "(SELECT eo.employee_id FROM hr_employee_order AS eo " +
                "WHERE eo.school_id = ? AND eo.hr_orders_id = 1 AND " +
                "(eo.to_date IS NULL OR DATE(eo.to_date) >= DATE(om.creation_date) " +
                "OR eo.to_date >= NOW()))) and message_status_id = 2";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        stat.setInt(2, school_id);
        ResultSet result = stat.executeQuery();
        if (result.next()) {
            return result.getInt("val") > 0;
        }
        return false;
    }
}
