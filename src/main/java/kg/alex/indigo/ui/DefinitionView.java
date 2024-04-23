package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.domain.Definition;
import kg.alex.indigo.i18n.IndigoMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.SQLIntegrityConstraintViolationException;

public class DefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DefinitionView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;
    private final boolean withActivityStatus;
    private final String dbTableName, dbTableReplaceIn, dbReplaceColumn;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final String permissionView;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, replaceSaveBtn;
    private PopupButton replaceBtn;
    private TextField nameTF;
    private ComboBox statusSelect, replaceItemSelect;
    private boolean isNew;
    private VerticalLayout settingsLay;

    public DefinitionView(MyVaadinUI myUI, String dbTable, String dbTableReplaceIn, String dbReplaceColumn,
                          boolean withActivityStatus, String permissionView) {
        this.dbTableName = dbTable;
        this.dbTableReplaceIn = dbTableReplaceIn;
        this.dbReplaceColumn = dbReplaceColumn;
        this.myUI = myUI;
        this.permissionView = permissionView;
        this.withActivityStatus = withActivityStatus;
        String[] NATURAL_COL_ORDER;
        if (this.withActivityStatus) {
            NATURAL_COL_ORDER = new String[]{myUI.getMessage(IndigoMessages.Title),
                    myUI.getMessage(IndigoMessages.Status)};
        } else {
            NATURAL_COL_ORDER = new String[]{myUI.getMessage(IndigoMessages.Title)};
        }
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
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            dataTable.setContainerDataSource(
                    dbDef.execSQL(myUI, dbTableName, this.withActivityStatus, false));
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

        VerticalLayout replaceLay = new VerticalLayout();
        replaceLay.setMargin(true);
        replaceLay.setSpacing(true);

        replaceItemSelect = new ComboBox(myUI.getMessage(IndigoMessages.ElementForReplacement));
        replaceItemSelect.setNullSelectionAllowed(false);
        replaceItemSelect.setRequired(true);
        replaceItemSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        replaceItemSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        replaceItemSelect.setWidth(Settings.PERCENTS100);
        replaceItemSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        replaceItemSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            replaceItemSelect.setContainerDataSource(dbDef.exec_for_select(myUI, dbTableName, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        replaceLay.addComponent(replaceItemSelect);

        replaceSaveBtn = new Button();
        replaceSaveBtn.setDescription(myUI.getMessage(IndigoMessages.SaveButton));
        replaceSaveBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        replaceSaveBtn.setIcon(FontAwesome.FLOPPY_O);
        replaceSaveBtn.addClickListener(this);
        replaceLay.addComponent(replaceSaveBtn);
        replaceLay.setComponentAlignment(replaceSaveBtn, Alignment.MIDDLE_RIGHT);

        replaceBtn = new PopupButton();
        replaceBtn.setEnabled(false);
        replaceBtn.setDescription(myUI.getMessage(IndigoMessages.Replace));
        replaceBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        replaceBtn.setIcon(FontAwesome.EXCHANGE);
        replaceBtn.addClickListener(this);
        replaceBtn.setContent(replaceLay);
        buttonsLay.addComponent(replaceBtn);

        if (dbTableReplaceIn == null) {
            replaceBtn.setVisible(false);
        }
        settingsLay.addComponent(buttonsLay);

        nameTF = new TextField(myUI.getMessage(IndigoMessages.Title));
        if (dbTableName.equals(Settings.classTable)) {
            nameTF.setPropertyDataSource(new ObjectProperty<>(0));
            nameTF.setConverter(Settings.getStringToIntegerConverter());
            nameTF.addValidator(new IntegerRangeValidator(
                    myUI.getMessage(IndigoMessages.NotificationWrongValue), 0, null));
        } else {
            nameTF.setPropertyDataSource(new ObjectProperty<>(""));
        }
        nameTF.setNullRepresentation("");
        nameTF.setNullSettingAllowed(false);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequired(true);
        nameTF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(nameTF);

        if (withActivityStatus) {
            statusSelect = new ComboBox(myUI.getMessage(IndigoMessages.Status));
            statusSelect.setNullSelectionAllowed(false);
            statusSelect.setRequired(true);
            statusSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
            statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
            statusSelect.setWidth(Settings.PERCENTS100);
            statusSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
            statusSelect.setFilteringMode(FilteringMode.CONTAINS);

            try {
                DbDefinition dbDef = new DbDefinition();
                dbDef.connect();
                statusSelect.setContainerDataSource(
                        dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
                dbDef.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            settingsLay.addComponent(statusSelect);
        }

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
            nameTF.setEnabled(true);
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
        } else if (source == replaceBtn) {
        } else if (source == replaceSaveBtn) {
            if (dataTable.getValue() != null && replaceItemSelect.getValue() != null
                    && (int) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue() != (int) replaceItemSelect.getValue()) {
                try {
                    DbDefinition dbCon = new DbDefinition();
                    dbCon.connect();
                    dbCon.exec_update(dbTableReplaceIn, dbReplaceColumn, (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                            Settings.id).getValue(), (Integer) replaceItemSelect.getValue());
                    int st = dbCon.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                            Settings.id).getValue(), dbTableName);
                    if (st != 0) {
                        replaceItemSelect.getContainerDataSource().removeItem(dataTable.getContainerProperty(
                                dataTable.getValue(), Settings.id).getValue());
                        dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                        if (dataTable.getContainerDataSource().size() != 0) {
                            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                        } else {
                            clearFields();
                            dataTable.setValue(null);
                        }
                    }
                    dbCon.close();
                    Notification.show(myUI.getMessage(IndigoMessages.ValueReplaced),
                            Notification.Type.HUMANIZED_MESSAGE);
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == saveBtn) {
            try {
                if (Settings.validate(settingsLay)) {
                    DbDefinition dbDef = new DbDefinition();
                    dbDef.connect();
                    if (isNew) {
                        int id = dbDef.exec_insert(getDefinition(0), dbTableName, withActivityStatus);
                        if (id != 0) {
                            addDataContainerItem(id);
                            Notification.show(myUI.getMessage(IndigoMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(IndigoMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        try {
                            status = dbDef.exec_update(
                                    getDefinition(
                                            (Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                                    Settings.id).getValue()), dbTableName, withActivityStatus);
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
                    dbDef.close();
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
        replaceBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        nameTF.setEnabled(true);
        if (withActivityStatus) {
            statusSelect.setEnabled(true);
        }
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(permissionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(permissionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(permissionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        replaceBtn.setEnabled(true);
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        if (withActivityStatus) {
            statusSelect.setEnabled(false);
        }
    }

    private void fillFields() {

        if (dbTableName.equals(Settings.classTable)) {
            nameTF.getPropertyDataSource().setValue(Integer.parseInt(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(IndigoMessages.Title)).getValue().toString()));
        } else {
            nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(IndigoMessages.Title)).getValue().toString());
        }
        if (withActivityStatus) {
            statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.activity_status_id).getValue());
        }
    }

    private void clearFields() {
        nameTF.setValue("");
        if (withActivityStatus) {
            statusSelect.setValue(2);
        }
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Title)).setValue(nameTF.getValue());
        if (withActivityStatus) {
            dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(IndigoMessages.Status)).setValue(statusSelect.getItemCaption(statusSelect.getValue()));
            dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.activity_status_id).setValue(statusSelect.getValue());
        }
    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(
                nameTF.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        if (withActivityStatus) {
            item.getItemProperty(myUI.getMessage(IndigoMessages.Status)).setValue(
                    statusSelect.getItemCaption(statusSelect.getValue()));
            item.getItemProperty(Settings.activity_status_id).setValue(statusSelect.getValue());
        }
        dataTable.setValue(id);
    }

    private Definition getDefinition(int id) {
        Definition d = new Definition();
        d.setName(nameTF.getPropertyDataSource().getValue().toString());
        d.setId(id);
        if (withActivityStatus) {
            d.setActivity_status_id((Integer) statusSelect.getValue());
        }
        return d;
    }

    private void execDelete() {

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), dbTableName);
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
            Notification.show(myUI.getMessage(IndigoMessages.ValueDeleted),
                    Notification.Type.HUMANIZED_MESSAGE);
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(IndigoMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
