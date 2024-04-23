package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbBlock;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbInventoryInvoice;
import kg.alex.indigo.domain.Block;
import kg.alex.indigo.i18n.IndigoMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;

public class BlockDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(BlockDefinitionView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox statusSelect;
    private TextField nameTF;
    private boolean isNew;
    private VerticalLayout settingsLay;

    public BlockDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        String[] NATURAL_COL_ORDER = new String[]{
                myUI.getMessage(IndigoMessages.Title), myUI.getMessage(IndigoMessages.Status)};
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
            DbBlock dbCon = new DbBlock();
            dbCon.connect();
            dataTable.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId()));
            dbCon.close();
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

        this.setSplitPosition(24, Unit.PERCENTAGE);
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
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 50, false));
        settingsLay.addComponent(nameTF);

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
                    DbBlock dbCon = new DbBlock();
                    dbCon.connect();
                    if (isNew) {
                        int id = dbCon.exec_insert(getBlock(0));
                        if (id != 0) {
                            addDataContainerItem(id);
                            Notification.show(myUI.getMessage(IndigoMessages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(IndigoMessages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        boolean isUsed = false;
                        try {
                            DbInventoryInvoice dbii = new DbInventoryInvoice();
                            dbii.connect();
                            isUsed = dbii.isUsed((Integer) dataTable.getContainerProperty(
                                    dataTable.getValue(), Settings.id).getValue(), 0);
                            dbii.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (isUsed) {
                            Notification.show(myUI.getMessage(IndigoMessages.ValueIsUsed),
                                    Notification.Type.WARNING_MESSAGE);
                        } else {
                            int status = 0;
                            try {
                                status = dbCon.exec_update(
                                        getBlock((Integer) dataTable.getContainerProperty(dataTable.getValue(),
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
                    }
                    dbCon.close();
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
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        nameTF.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnBlockDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnBlockDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnBlockDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Title)).getValue().toString());
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        statusSelect.setValue(null);
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(IndigoMessages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(IndigoMessages.Title)).getValue().toString());
    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(IndigoMessages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(
                statusSelect.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Block getBlock(int i) {
        Block block = new Block();
        block.setName(nameTF.getValue());
        block.setStatus_id((Integer) statusSelect.getValue());
        block.setSchool_id(myUI.getUser().getSchool().getId());
        block.setId(i);
        return block;
    }

    private void execDelete() {

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbBlock);
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
            Notification.show(myUI.getMessage(IndigoMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public Component getNewObj() {
        return new BlockDefinitionView(myUI);
    }
}
