package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.AuthenticatedScreen;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbYear;
import kg.alex.indigo.domain.Year;
import kg.alex.indigo.i18n.IndigoMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.inputmask.InputMask;

import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YearDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(YearDefinitionView.class);
    private final MyVaadinUI myUI;
    private final AuthenticatedScreen as;
    private final Table dataTable;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private TextField nameTF;
    private TextField period, periodKg;
    private DateField start_date, end_date;
    private boolean isNew;
    private VerticalLayout settingsLay;

    public YearDefinitionView(MyVaadinUI myUI, AuthenticatedScreen as) {
        this.myUI = myUI;
        this.as = as;
        String[] NATURAL_COL_ORDER = new String[]{myUI.getMessage(IndigoMessages.Title), myUI.getMessage(IndigoMessages.Period),
                myUI.getMessage(IndigoMessages.PeriodKg), myUI.getMessage(IndigoMessages.StartDate), myUI.getMessage(IndigoMessages.EndDate)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new Table();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.addValueChangeListener(this);
        try {
            DbYear dbDef = new DbYear();
            dbDef.connect();
            dataTable.setContainerDataSource(dbDef.execSQL(myUI));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
        dataTable.setNullSelectionAllowed(false);
        vl.addComponent(dataTable);

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);

        prepareNormalMode();
    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setWidth(Settings.PERCENTS100);

        HorizontalLayout buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        modifyBtn = new Button();
        modifyBtn.setEnabled(false);
        modifyBtn.setDescription(myUI.getMessage(IndigoMessages.ModifyButton));
        modifyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        modifyBtn.setIcon(FontAwesome.PENCIL);
        modifyBtn.addClickListener(this);
        buttonsLay.addComponent(modifyBtn);

        createBtn = new Button();
        createBtn.setEnabled(false);
        createBtn.setDescription(myUI.getMessage(IndigoMessages.CreateButton));
        createBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        createBtn.setIcon(FontAwesome.FILE_O);
        createBtn.addClickListener(this);
        buttonsLay.addComponent(createBtn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(IndigoMessages.DeleteButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        buttonsLay.addComponent(deleteBtn);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(IndigoMessages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        buttonsLay.addComponent(saveBtn);

        cancelBtn = new Button();
        cancelBtn.setDescription(myUI.getMessage(IndigoMessages.CancelButton));
        cancelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cancelBtn.setIcon(FontAwesome.BAN);
        cancelBtn.addClickListener(this);
        buttonsLay.addComponent(cancelBtn);
        settingsLay.addComponent(buttonsLay);

        nameTF = new TextField(myUI.getMessage(IndigoMessages.Title));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 9, 9, false));
        InputMask im = new InputMask("2099-2099");
        im.extend(nameTF);
        im.setClearIncomplete(true);
        settingsLay.addComponent(nameTF);

        period = new TextField(myUI.getMessage(IndigoMessages.Period));
        period.setRequired(true);
        period.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        period.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        period.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(period);

        periodKg = new TextField(myUI.getMessage(IndigoMessages.PeriodKg));
        periodKg.setRequired(true);
        periodKg.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        periodKg.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        periodKg.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(periodKg);

        start_date = new DateField(myUI.getMessage(IndigoMessages.StartDate));
        start_date.setStyleName(ValoTheme.DATEFIELD_SMALL);
        start_date.setRequired(true);
        start_date.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        start_date.setWidth(Settings.PERCENTS100);
        start_date.setValue(new Date());
        start_date.setDateFormat(Settings.datePattern);
        settingsLay.addComponent(start_date);

        end_date = new DateField(myUI.getMessage(IndigoMessages.EndDate));
        end_date.setStyleName(ValoTheme.DATEFIELD_SMALL);
        end_date.setRequired(true);
        end_date.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        end_date.setWidth(Settings.PERCENTS100);
        end_date.setValue(new Date());
        end_date.setDateFormat(Settings.datePattern);
        settingsLay.addComponent(end_date);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && dataTable.getValue() != null) {
            isNew = false;
            try {
                fillFields();
            } catch (ParseException ex) {
                Logger.getLogger(YearDefinitionView.class.getName()).log(Level.SEVERE, null, ex);
            }
            prepareModificationMode();
            nameTF.focus();
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            nameTF.focus();
        } else if (source == deleteBtn && dataTable.getValue() != null) {
            ConfirmDialog.show(myUI, myUI.getMessage(IndigoMessages.Question),
                    myUI.getMessage(IndigoMessages.ConfirmDeletion),
                    myUI.getMessage(IndigoMessages.Yes),
                    myUI.getMessage(IndigoMessages.No),
                    (ConfirmDialog.Listener) dialog -> {
                        if (dialog.isConfirmed()) {
                            execDelete();
                        }
                    });
        } else if (source == saveBtn) {
            try {
                if (Settings.validate(settingsLay)) {
                    DbYear dby = new DbYear();
                    dby.connect();
                    if (isNew) {
                        int id = dby.exec_insert(getYearDefinition(0));
                        if (id != 0) {
                            addDataContainerItem(id);
                            try {
                                DbDefinition dbd = new DbDefinition();
                                dbd.connect();
                                as.yearSelect.removeValueChangeListener(as);
                                as.yearSelect.setContainerDataSource(dbd.exec_years_for_select(myUI, myUI.getUser().getCurrent_year().getId()));
                                as.yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
                                as.yearSelect.addValueChangeListener(as);
                                dbd.close();
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                            Notification.show(myUI.getMessage(IndigoMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(IndigoMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        try {
                            status = dby.exec_update(
                                    getYearDefinition((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                            Settings.id).getValue()));
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            updateDataContainer();
                            Notification.show(myUI.getMessage(IndigoMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(IndigoMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    try {
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        as.yearSelect.removeValueChangeListener(as);
                        as.yearSelect.setContainerDataSource(dbd.exec_years_for_select(myUI, myUI.getUser().getCurrent_year().getId()));
                        as.yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
                        as.yearSelect.addValueChangeListener(as);
                        dbd.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    dby.close();
                    prepareNormalMode();
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            if (dataTable.getValue() != null) {
                try {
                    fillFields();
                } catch (ParseException ex) {
                    Logger.getLogger(YearDefinitionView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            prepareNormalMode();
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == dataTable) {
            if (dataTable.getItem(dataTable.getValue()) != null) {
                try {
                    fillFields();
                } catch (ParseException ex) {
                    Logger.getLogger(YearDefinitionView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        nameTF.setEnabled(true);
        period.setEnabled(true);
        periodKg.setEnabled(true);
        start_date.setEnabled(true);
        end_date.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnClassNameDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnClassNameDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnClassNameDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        period.setEnabled(false);
        periodKg.setEnabled(false);
        start_date.setEnabled(false);
        end_date.setEnabled(false);
    }

    private void fillFields() throws ParseException {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Title)).getValue().toString());
        period.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Period)).getValue().toString());
        periodKg.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.PeriodKg)).getValue().toString());
        start_date.setValue(Settings.df.parse(dataTable.getContainerProperty(
                        dataTable.getValue(), myUI.getMessage(IndigoMessages.StartDate))
                .getValue().toString()));
        end_date.setValue(Settings.df.parse(dataTable.getContainerProperty(
                        dataTable.getValue(), myUI.getMessage(IndigoMessages.EndDate))
                .getValue().toString()));

    }

    private void clearFields() {
        nameTF.setValue("");
        period.setValue("");
        periodKg.setValue("");
        start_date.setValue(null);
        end_date.setValue(null);
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Period)).setValue(period.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.PeriodKg)).setValue(periodKg.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.StartDate)).setValue(
                Settings.df.format(start_date.getValue()));
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.EndDate)).setValue(
                Settings.df.format(end_date.getValue()));

    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(
                nameTF.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Period)).setValue(
                period.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.PeriodKg)).setValue(
                periodKg.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.StartDate)).setValue(
                Settings.df.format(start_date.getValue()));
        item.getItemProperty(myUI.getMessage(IndigoMessages.EndDate)).setValue(
                Settings.df.format(end_date.getValue()));

        dataTable.setValue(id);
    }

    private Year getYearDefinition(int i) {
        Year y = new Year();
        y.setName(nameTF.getValue());
        y.setPeriod(period.getValue());
        y.setPeriod_kg(periodKg.getValue());
        y.setStart_date(start_date.getValue());
        y.setEnd_date(end_date.getValue());
        y.setId(i);
        return y;
    }

    private void execDelete() {
        if (((Integer) ((IndexedContainer) dataTable.getContainerDataSource()).getIdByIndex(0)).intValue() == ((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                Settings.id).getValue()).intValue()) {
            try {
                DbDefinition dbDef = new DbDefinition();
                dbDef.connect();
                int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                        Settings.id).getValue(), Settings.dbYear);
                if (st != 0) {
                    dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                    if (dataTable.getContainerDataSource().size() != 0) {
                        dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                    } else {
                        clearFields();
                        dataTable.setValue(null);
                    }
                    try {
                        as.yearSelect.removeValueChangeListener(as);
                        as.yearSelect.setContainerDataSource(dbDef.exec_years_for_select(myUI, myUI.getUser().getCurrent_year().getId()));
                        as.yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
                        as.yearSelect.addValueChangeListener(as);
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
                dbDef.close();
            } catch (SQLIntegrityConstraintViolationException e) {
                Notification.show(myUI.getMessage(IndigoMessages.CanNotDelete),
                        Notification.Type.WARNING_MESSAGE);
                logger.error(e);
                logger.catching(e);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else {
            Notification.show(myUI.getMessage(IndigoMessages.CanDeleteOnlyLastItem),
                    Notification.Type.WARNING_MESSAGE);
        }
    }


    public Component getNewObj() {
        return new YearDefinitionView(myUI, as);
    }
}
