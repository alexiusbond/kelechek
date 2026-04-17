/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.dao;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.TextField;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.domain.EmployeeQuestioning;
import kg.alex.star.i18n.Messages;
import kg.alex.star.ui.EmployeeDefinitionView;
import kg.alex.star.utils.Settings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeQuestion extends BaseDb {

    public DbEmployeeQuestion() throws Exception {
        super();
    }

    public int exec_insert(EmployeeQuestioning eq) throws SQLException {
        String sql = "INSERT IGNORE INTO hr_employee_question (employee_id,question_id,answer) "
                + "VALUES(?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, eq.getEmployee_id());
        stat.setInt(2, eq.getQuestion_id());
        stat.setString(3, eq.getAnswer());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeQuestioning eq) throws SQLException {
        String sql = "update hr_employee_question set "
                + " answer = ? WHERE question_id = ? and employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setString(1, eq.getAnswer());
        stat.setInt(2, eq.getQuestion_id());
        stat.setInt(3, eq.getEmployee_id());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id,
                                    EmployeeDefinitionView edv) throws SQLException {


        String sql = "SELECT q.id, eq.id, q.name, eq.answer FROM hr_question as q "
                + "left join hr_employee_question as eq on eq.question_id = q.id and eq.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Question), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Answer), TextField.class, null);
        container.addContainerProperty(Settings.id, Integer.class, 0);
        while (result.next()) {
            String id = result.getString("q.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.Question)).setValue(result.getString("q.name"));
            item.getItemProperty(Settings.id).setValue(result.getInt("eq.id"));
            item.getItemProperty(myUI.getMessage(Messages.Answer)).setValue(
                    edv.createTextField(result.getString("eq.answer"),
                            myUI.getMessage(Messages.Answer),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 350, true), false));
        }
        return container;
    }
}
