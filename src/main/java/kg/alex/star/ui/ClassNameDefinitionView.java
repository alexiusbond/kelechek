package kg.alex.star.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.dao.DbClassName;
import kg.alex.star.dao.DbDefinition;
import kg.alex.star.domain.ClassName;
import kg.alex.star.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;

public class ClassNameDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ClassNameDefinitionView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox classNumberSelect, classTypeSelect, statusSelect;
    private TextField nameTF;
    private boolean isNew;
    private VerticalLayout settingsLay;

    public ClassNameDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        String[] NATURAL_COL_ORDER = new String[]{
                myUI.getMessage(Messages.Number), myUI.getMessage(Messages.Title),
                myUI.getMessage(Messages.Type), myUI.getMessage(Messages.School),
                myUI.getMessage(Messages.Status)};
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
            DbClassName dbDef = new DbClassName();
            dbDef.connect();
            dataTable.setContainerDataSource(dbDef.execSQL(myUI, myUI.getUser().getSchool().getId()));
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
        modifyBtn.setDescription(myUI.getMessage(Messages.ModifyButton));
        modifyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        modifyBtn.setIcon(FontAwesome.PENCIL);
        modifyBtn.addClickListener(this);
        buttonsLay.addComponent(modifyBtn);

        createBtn = new Button();
        createBtn.setEnabled(false);
        createBtn.setDescription(myUI.getMessage(Messages.CreateButton));
        createBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        createBtn.setIcon(FontAwesome.FILE_O);
        createBtn.addClickListener(this);
        buttonsLay.addComponent(createBtn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(Messages.DeleteButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        buttonsLay.addComponent(deleteBtn);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(Messages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        buttonsLay.addComponent(saveBtn);

        cancelBtn = new Button();
        cancelBtn.setDescription(myUI.getMessage(Messages.CancelButton));
        cancelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cancelBtn.setIcon(FontAwesome.BAN);
        cancelBtn.addClickListener(this);
        buttonsLay.addComponent(cancelBtn);
        settingsLay.addComponent(buttonsLay);

        nameTF = new TextField(myUI.getMessage(Messages.Title));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 1, 35, false));
        settingsLay.addComponent(nameTF);

        classNumberSelect = new ComboBox(myUI.getMessage(Messages.Number));
        classNumberSelect.setNullSelectionAllowed(false);
        classNumberSelect.setRequired(true);
        classNumberSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        classNumberSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        classNumberSelect.setWidth(Settings.PERCENTS100);
        classNumberSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        classNumberSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(classNumberSelect);

        classTypeSelect = new ComboBox(myUI.getMessage(Messages.Type));
        classTypeSelect.setNullSelectionAllowed(false);
        classTypeSelect.setRequired(true);
        classTypeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        classTypeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        classTypeSelect.setWidth(Settings.PERCENTS100);
        classTypeSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        classTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(classTypeSelect);

        statusSelect = new ComboBox(myUI.getMessage(Messages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setWidth(Settings.PERCENTS100);
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            classTypeSelect.setContainerDataSource(dbDef.exec_for_select(myUI, Settings.dbClassType, false));
            statusSelect.setContainerDataSource(dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
            classNumberSelect.setContainerDataSource(dbDef.exec_for_select(myUI, Settings.classTable, false));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(statusSelect);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && dataTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
            nameTF.focus();
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            nameTF.focus();
            statusSelect.setValue(2);
        } else if (source == deleteBtn && dataTable.getValue() != null) {
            ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                    myUI.getMessage(Messages.ConfirmDeletion),
                    myUI.getMessage(Messages.Yes),
                    myUI.getMessage(Messages.No),
                    (ConfirmDialog.Listener) dialog -> {
                        if (dialog.isConfirmed()) {
                            execDelete();
                        }
                    });
        } else if (source == saveBtn) {
            try {
                if (Settings.validate(settingsLay)) {
                    DbClassName dbcn = new DbClassName();
                    dbcn.connect();
                    if (isNew) {
                        int id = dbcn.exec_insert(getClassName(0));
                        if (id != 0) {
                            addDataContainerItem(id);
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        try {
                            status = dbcn.exec_update(
                                    getClassName((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                            Settings.id).getValue()));
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            updateDataContainer();
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbcn.close();
                    prepareNormalMode();
                } else {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            if (dataTable.getValue() != null) {
                fillFields();
            }
            prepareNormalMode();
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == dataTable) {
            if (dataTable.getItem(dataTable.getValue()) != null) {
                fillFields();
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
        classNumberSelect.setEnabled(true);
        statusSelect.setEnabled(true);
        classTypeSelect.setEnabled(true);
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
        classNumberSelect.setEnabled(false);
        statusSelect.setEnabled(false);
        classTypeSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).getValue().toString());
        classNumberSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.number_id).getValue());
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(), Settings.status_id).getValue());
        classTypeSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(), Settings.class_type_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        classNumberSelect.setValue(null);
        statusSelect.setValue(null);
        classTypeSelect.setValue(null);
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.number_id).setValue(classNumberSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Number)).setValue(classNumberSelect
                .getContainerProperty(classNumberSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.class_type_id).setValue(classTypeSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Type)).setValue(classTypeSelect.
                getContainerProperty(classTypeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.school_id).setValue(myUI.getUser().getSchool().getId());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.School)).setValue(myUI.getUser().getSchool().getName_ru());
    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Number)).setValue(
                classNumberSelect.getContainerProperty(classNumberSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        item.getItemProperty(Settings.number_id).setValue(
                classNumberSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(myUI.getMessage(Messages.Type)).setValue(
                classTypeSelect.getContainerProperty(classTypeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(statusSelect.getValue());
        item.getItemProperty(Settings.class_type_id).setValue(classTypeSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.School)).setValue(
                myUI.getUser().getSchool().getName_ru());
        item.getItemProperty(Settings.school_id).setValue(
                myUI.getUser().getSchool().getId());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private ClassName getClassName(int i) {
        ClassName d = new ClassName();
        d.setName(nameTF.getValue());
        d.setClass_number_id((Integer) classNumberSelect.getValue());
        d.setStatus_id((Integer) statusSelect.getValue());
        d.setClass_type_id((Integer) classTypeSelect.getValue());
        d.setSchool_id(myUI.getUser().getSchool().getId());
        d.setId(i);
        return d;
    }

    private void execDelete() {

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbClass_name);
            if (st != 0) {
                dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                } else {
                    clearFields();
                    dataTable.setValue(null);
                }
            }
            dbDef.close();
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
        return new ClassNameDefinitionView(myUI);
    }
}
