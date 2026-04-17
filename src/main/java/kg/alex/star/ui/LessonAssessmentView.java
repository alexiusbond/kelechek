package kg.alex.star.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.utils.Settings;
import kg.alex.star.dao.DbDefinition;
import kg.alex.star.dao.DbEmployee;
import kg.alex.star.dao.DbEmployeeLessons;
import kg.alex.star.domain.EmployeeLessons;
import kg.alex.star.i18n.Messages;
import kg.alex.star.utils.MyFilterDecorator;
import kg.alex.star.utils.ResetableFilterGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;

public class LessonAssessmentView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(LessonAssessmentView.class);
    private final MyVaadinUI myUI;
    private final Button saveBtn;
    private final ComboBox classNumberSelect;
    private final Table lessonsTable;
    private final FilterTable employeesTable;

    public LessonAssessmentView(MyVaadinUI myUI) {
        this.myUI = myUI;

        employeesTable = new FilterTable();
        employeesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        employeesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        employeesTable.addStyleName("noWrapHeader");
        employeesTable.setSizeFull();
        employeesTable.setNullSelectionAllowed(false);
        employeesTable.setFilterBarVisible(true);
        employeesTable.setSelectable(true);
        employeesTable.addValueChangeListener(this);
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            Subject currentUser = SecurityUtils.getSubject();
            employeesTable.setContainerDataSource(
                    dbe.execSQL(myUI, myUI.getUser().getSchool().getId(), currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr)));
            dbe.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        employeesTable.setColumnWidth(myUI.getMessage(Messages.TotalHours), 10);
        employeesTable.setFilterGenerator(new ResetableFilterGenerator(employeesTable));

        VerticalLayout vl2 = new VerticalLayout();
        vl2.setMargin(true);
        vl2.setSpacing(true);
        vl2.setSizeFull();
        vl2.addComponent(employeesTable);

        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(new MarginInfo(true, true, true, false));
        vl.setSpacing(true);
        vl.setSizeFull();

        classNumberSelect = new ComboBox(myUI.getMessage(Messages.ClassNumber));
        classNumberSelect.setNullSelectionAllowed(false);
        classNumberSelect.setRequired(true);
        classNumberSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        classNumberSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        classNumberSelect.setWidth(Settings.PERCENTS100);
        classNumberSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        classNumberSelect.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            classNumberSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.classTable, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classNumberSelect.addValueChangeListener(this);

        lessonsTable = new Table();
        lessonsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        lessonsTable.setSizeFull();
        lessonsTable.setSelectable(false);

        saveBtn = new Button(myUI.getMessage(Messages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_HUGE);
        saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);

        vl.addComponent(classNumberSelect);
        vl.addComponent(lessonsTable);
        vl.addComponent(saveBtn);
        vl.setComponentAlignment(saveBtn, Alignment.MIDDLE_RIGHT);
        vl.setExpandRatio(lessonsTable, 1);

        this.setSplitPosition(68, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(vl2);
        this.setSecondComponent(vl);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == saveBtn) {
            try {
                if (Settings.validateTable(myUI, lessonsTable)) {
                    DbEmployeeLessons dbel = new DbEmployeeLessons();
                    dbel.connect();
                    for (Object next : lessonsTable.getItemIds()) {
                        CheckBox cb = (CheckBox) lessonsTable.getContainerProperty(next, Settings.button).getValue();
                        if (cb.getValue()) {
                            EmployeeLessons el = new EmployeeLessons();
                            el.setBranch_id((Integer) cb.getData());
                            el.setYear_id(myUI.getUser().getCurrent_year().getId());
                            el.setSchool_id(myUI.getUser().getSchool().getId());
                            el.setClass_number_id((Integer) classNumberSelect.getValue());
                            el.setEmployee_id((Integer) employeesTable.getValue());
                            el.setHours((Integer) ((TextField) lessonsTable.getContainerProperty(next, myUI.getMessage(Messages.Hours))
                                    .getValue()).getPropertyDataSource().getValue());
                            el.setExtra_hours((Integer) ((TextField) lessonsTable.getContainerProperty(next, myUI.getMessage(Messages.ExtraHours))
                                    .getValue()).getPropertyDataSource().getValue());
                            int id = dbel.exec_insert(el);
                            if (id == 0) {
                                el.setId((Integer) ((TextField) lessonsTable.getContainerProperty(next, myUI.getMessage(Messages.Hours))
                                        .getValue()).getData());
                                dbel.exec_update(el);
                            }
                        }
                    }
                    employeesTable.getContainerProperty(employeesTable.getValue(), myUI.getMessage(Messages.TotalHours)
                            + myUI.getUser().getCurrent_year().getName()).setValue(dbel.execSQLTotalHours(myUI,
                            (Integer) employeesTable.getValue(), myUI.getUser().getSchool().getId()));
                    Notification.show(myUI.getMessage(Messages.ValueSaved),
                            Notification.Type.HUMANIZED_MESSAGE);
                    dbel.close();

                } else {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == employeesTable || property == classNumberSelect) {
            if (employeesTable.getValue() != null && classNumberSelect.getValue() != null) {
                try {
                    DbEmployeeLessons dbel = new DbEmployeeLessons();
                    dbel.connect();
                    lessonsTable.setContainerDataSource(dbel.execSQLHours(myUI, (Integer) employeesTable.getValue(), myUI.getUser().getSchool().getId(),
                            (Integer) classNumberSelect.getValue(), this));
                    dbel.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                lessonsTable.setContainerDataSource(null);
            }
        } else {
            final Integer branch_id = (Integer) ((CheckBox) property).getData();
            TextField tfHours = (TextField) lessonsTable.getContainerProperty(branch_id, myUI.getMessage(Messages.Hours)).getValue();
            TextField tfExtra = (TextField) lessonsTable.getContainerProperty(branch_id, myUI.getMessage(Messages.ExtraHours)).getValue();
            if (((CheckBox) property).getValue()) {
                tfHours.setEnabled(true);
                tfHours.setRequired(true);
                tfHours.setRequiredError(myUI.getMessage(Messages.RequiredField));
                tfExtra.setEnabled(true);
                tfExtra.setRequired(true);
                tfExtra.setRequiredError(myUI.getMessage(Messages.RequiredField));
            } else {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmDeletion),
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete(branch_id);
                            }
                        });

            }
        }
    }

    private void execDelete(Integer branch_id) {
        TextField tfHours = (TextField) lessonsTable.getContainerProperty(branch_id, myUI.getMessage(Messages.Hours)).getValue();
        TextField tfExtra = (TextField) lessonsTable.getContainerProperty(branch_id, myUI.getMessage(Messages.ExtraHours)).getValue();
        tfHours.setEnabled(false);
        tfHours.setRequired(false);
        tfHours.setValue("");
        tfExtra.setEnabled(false);
        tfExtra.setRequired(false);
        tfExtra.setValue("");
        try {
            DbEmployeeLessons dbel = new DbEmployeeLessons();
            dbel.connect();
            int st = dbel.exec_delete((Integer) employeesTable.getValue(), branch_id, myUI.getUser().getSchool().getId(),
                    (Integer) classNumberSelect.getValue(), myUI.getUser().getCurrent_year().getId());
            if (st != 0) {
                Notification.show(myUI.getMessage(Messages.ValueDeleted),
                        Notification.Type.HUMANIZED_MESSAGE);
                employeesTable.getContainerProperty(employeesTable.getValue(), myUI.getMessage(Messages.TotalHours)
                        + myUI.getUser().getCurrent_year().getName()).setValue(dbel.execSQLTotalHours(myUI,
                        (Integer) employeesTable.getValue(), myUI.getUser().getSchool().getId()));
            }
            dbel.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(Messages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public Component getNewObj() {
        return new LessonAssessmentView(myUI);
    }
}
