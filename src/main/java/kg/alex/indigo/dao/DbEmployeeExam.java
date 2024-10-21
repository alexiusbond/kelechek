/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.dao;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Upload;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.Attachment;
import kg.alex.indigo.domain.EmployeeExam;
import kg.alex.indigo.i18n.Messages;
import kg.alex.indigo.ui.EmployeeDefinitionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbEmployeeExam extends BaseDb {

    static final Logger logger = LogManager.getLogger(DbEmployeeExam.class);

    public DbEmployeeExam() throws Exception {
        super();
    }

    public int exec_insert(EmployeeExam ex) throws SQLException {
        String sql = "INSERT INTO hr_employee_exam (employee_id,hr_exam_id,score,date_of_issue,attachment_id) "
                + "VALUES(?,?,?,?,?)";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ex.getEmployee_id());
        stat.setInt(2, ex.getExam_id());
        stat.setString(3, ex.getScore());
        stat.setDate(4, new java.sql.Date(ex.getDate_of_issue().getTime()));
        stat.setInt(5, ex.getAttachment_id());
        int st = stat.executeUpdate();
        if (st != 0) {
            return getLastInsertedId();
        } else {
            return 0;
        }
    }

    public int exec_update(EmployeeExam ex) throws SQLException {
        String sql = "update hr_employee_exam set "
                + "hr_exam_id = ?, score = ?, date_of_issue = ?, attachment_id = ? WHERE id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, ex.getExam_id());
        stat.setString(2, ex.getScore());
        stat.setDate(3, new java.sql.Date(ex.getDate_of_issue().getTime()));
        stat.setInt(4, ex.getAttachment_id());
        stat.setInt(5, ex.getId());
        return stat.executeUpdate();
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id, EmployeeDefinitionView edv) throws SQLException {

        String sql = "SELECT ex.id, ex.hr_exam_id, ex.score, ex.date_of_issue, ex.attachment_id, " +
                "a.id, a.name, a.extension, a.unique_name " +
                "FROM hr_employee_exam as ex " +
                "left join attachments as a on a.id = ex.attachment_id where ex.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = edv.prepareExamContainer();
        while (result.next()) {
            String id = result.getString("ex.id");
            Item item = container.addItem(id);
            item.getItemProperty(Settings.button).setValue(edv.createButton(myUI.getMessage(Messages.DeleteButton),
                    id, Settings.dbEmployeeExams, FontAwesome.MINUS_SQUARE));
            ComboBox cb = edv.createCombobox(0, myUI.getMessage(Messages.Exam), null, true);
            try {
                DbExam dbe = new DbExam();
                dbe.connect();
                cb.setContainerDataSource(dbe.exec_for_select(myUI, result.getInt("ex.hr_exam_id")));
                dbe.close();
            } catch (Exception ex) {
                logger.error(ex);
                logger.catching(ex);
            }
            cb.setValue(result.getInt("ex.hr_exam_id"));
            item.getItemProperty(myUI.getMessage(Messages.Exam)).setValue(cb);
            item.getItemProperty(myUI.getMessage(Messages.Score)).setValue(
                    edv.createTextField(result.getString("ex.score"), myUI.getMessage(Messages.Score),
                            new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), 1, 10, false), true));
            item.getItemProperty(myUI.getMessage(Messages.IssueDate)).setValue(edv.createDateField(result.getDate("ex.date_of_issue"),
                    myUI.getMessage(Messages.IssueDate), null, true, Settings.datePattern, Resolution.DAY));

            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);

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
            item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Update));
        }
        return container;
    }

    public IndexedContainer execSQL(MyVaadinUI myUI, int employee_id) throws SQLException {

        String sql = "SELECT ex.id, e.name, ex.score, ex.date_of_issue FROM hr_employee_exam AS ex " +
                "LEFT JOIN hr_exam AS e ON e.id = ex.hr_exam_id WHERE ex.employee_id = ?";
        PreparedStatement stat = dbCon.prepareStatement(sql);
        stat.setInt(1, employee_id);
        ResultSet result = stat.executeQuery();
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Exam), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Score), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.IssueDate), String.class, null);
        while (result.next()) {
            String id = result.getString("ex.id");
            Item item = container.addItem(id);
            item.getItemProperty(myUI.getMessage(Messages.Exam)).setValue(result.getString("e.name"));
            item.getItemProperty(myUI.getMessage(Messages.Score)).setValue(result.getString("ex.score"));
            item.getItemProperty(myUI.getMessage(Messages.IssueDate)).setValue(Settings.df.format(result.getDate("ex.date_of_issue")));
        }
        return container;
    }
}
