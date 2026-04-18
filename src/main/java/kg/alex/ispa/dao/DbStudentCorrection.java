/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.domain.StudentCorrection;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.ui.StudentDefinitionView;
import kg.alex.ispa.utils.Settings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbStudentCorrection extends BaseDb {
    public DbStudentCorrection() throws Exception {
        super();
    }

    public IndexedContainer execSQLStudentCorrections(MyVaadinUI myUI, int stud_id, int year_id,
                                                      StudentDefinitionView dw) throws SQLException {
        Subject currentUser = SecurityUtils.getSubject();
        String sql = "SELECT t.id, t.correction_type_id, t.amount, t.note, " +
                "CONCAT(e.surname, ' ', e.name) AS employee FROM student_correction AS t " +
                "LEFT JOIN employee AS e ON t.employee_id = e.id WHERE t.student_id = ? and t.year_id = ? " +
                "ORDER BY t.id";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, stud_id);
        stat.setInt(2, year_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = dw.prepareCorrectionsContainer();
        while (result.next()) {
            String id = result.getString("t.id");
            Item item = container.addItem(id);
            Button b = dw.createButton(myUI.getMessage(Messages.DeleteButton), id,
                    Settings.dbStudentCorrection, FontAwesome.MINUS_SQUARE);
            if (!currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.actDelete)) {
                b.setEnabled(false);
            }
            item.getItemProperty(Settings.button).setValue(b);
            ComboBox cb = dw.createComboboxCorr(result.getInt("t.correction_type_id"),
                    myUI.getMessage(Messages.Title), id);
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(cb);
            TextField discTf = dw.createTextFieldDouble(result.getDouble("t.amount"), 2,
                    myUI.getMessage(Messages.Amount), id);
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(discTf);
            TextField tf = dw.createTextField(result.getString("t.note"),
                    myUI.getMessage(Messages.Note), id, new StringLengthValidator(
                            myUI.getMessage(Messages.NotificationWrongValue),
                            null, 250, true), false);
            item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(tf);
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            if (!currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.actModify)) {
                tf.setEnabled(false);
                hl.setEnabled(false);
                cb.setEnabled(false);
                discTf.setEnabled(false);
            }
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public int exec_insert(StudentCorrection cc) throws SQLException {
        String sql = "INSERT INTO student_correction "
                + "(student_id, year_id, correction_type_id, amount, employee_id, modification_date, note, creation_date) "
                + "VALUES(?,?,?,?,?,NOW(),?, NOW())";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cc.getStudent_id());
        stat.setInt(2, cc.getYear_id());
        stat.setInt(3, cc.getCorrection_type_id());
        stat.setDouble(4, cc.getAmount());
        stat.setInt(5, cc.getEmployee_id());
        if (cc.getNote() != null && !cc.getNote().isEmpty()) {
            stat.setString(6, cc.getNote());
        } else {
            stat.setNull(6, Types.VARCHAR);
        }
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(StudentCorrection cc) throws SQLException {
        String sql = "update student_correction set correction_type_id = ?, amount = ?, note = ?, " +
                "modification_date = NOW() WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, cc.getCorrection_type_id());
        stat.setDouble(2, cc.getAmount());
        if (cc.getNote() != null && !cc.getNote().isEmpty()) {
            stat.setString(3, cc.getNote());
        } else {
            stat.setNull(3, Types.VARCHAR);
        }
        stat.setString(4, cc.getId());
        stat.executeUpdate();
        return stat.getUpdateCount();
    }
}
