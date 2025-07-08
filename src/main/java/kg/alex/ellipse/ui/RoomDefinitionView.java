package kg.alex.ellipse.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.dao.DbDefinition;
import kg.alex.ellipse.dao.DbInventoryInvoice;
import kg.alex.ellipse.dao.DbRoom;
import kg.alex.ellipse.domain.Room;
import kg.alex.ellipse.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;

public class RoomDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(RoomDefinitionView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox blockSelect, floorSelect, statusSelect;
    private TextField nameTF, descriptionTF;
    private boolean isNew;
    private VerticalLayout settingsLay;

    public RoomDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        String[] NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.Block),
                myUI.getMessage(Messages.Floor), myUI.getMessage(Messages.Title),
                myUI.getMessage(Messages.Description), myUI.getMessage(Messages.Status)};
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
            DbRoom dbCon = new DbRoom();
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
                myUI.getMessage(Messages.NotificationWrongValue), 1, 50, false));
        settingsLay.addComponent(nameTF);

        descriptionTF = new TextField(myUI.getMessage(Messages.Description));
        descriptionTF.setRequired(true);
        descriptionTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        descriptionTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        descriptionTF.setWidth(Settings.PERCENTS100);
        descriptionTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 1, 150, false));
        settingsLay.addComponent(descriptionTF);

        blockSelect = new ComboBox(myUI.getMessage(Messages.Block));
        blockSelect.setNullSelectionAllowed(false);
        blockSelect.setRequired(true);
        blockSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        blockSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        blockSelect.setWidth(Settings.PERCENTS100);
        blockSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        blockSelect.setFilteringMode(FilteringMode.CONTAINS);

        floorSelect = new ComboBox(myUI.getMessage(Messages.Floor));
        floorSelect.setNullSelectionAllowed(false);
        floorSelect.setRequired(true);
        floorSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        floorSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        floorSelect.setWidth(Settings.PERCENTS100);
        floorSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        floorSelect.setFilteringMode(FilteringMode.CONTAINS);

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
            blockSelect.setContainerDataSource(dbDef.exec_for_select(myUI,
                    Settings.dbBlock, myUI.getUser().getSchool().getId(), false));
            floorSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbFloor, false));
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(blockSelect);
        settingsLay.addComponent(floorSelect);
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
                    DbRoom dbCon = new DbRoom();
                    dbCon.connect();
                    if (isNew) {
                        int id = dbCon.exec_insert(getRoom(0));
                        if (id != 0) {
                            addDataContainerItem(id);
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        boolean isUsed = false;
                        try {
                            DbInventoryInvoice dbii = new DbInventoryInvoice();
                            dbii.connect();
                            isUsed = dbii.isUsed(0, (Integer) dataTable.getContainerProperty(
                                    dataTable.getValue(), Settings.id).getValue());
                            dbii.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (isUsed) {
                            Notification.show(myUI.getMessage(Messages.ValueIsUsed),
                                    Notification.Type.WARNING_MESSAGE);
                        } else {
                            int status = 0;
                            try {
                                status = dbCon.exec_update(
                                        getRoom((Integer) dataTable.getContainerProperty(dataTable.getValue(),
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
                    }
                    dbCon.close();
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
        descriptionTF.setEnabled(true);
        blockSelect.setEnabled(true);
        floorSelect.setEnabled(true);
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
        descriptionTF.setEnabled(false);
        blockSelect.setEnabled(false);
        floorSelect.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).getValue().toString());
        descriptionTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Description)).getValue().toString());
        blockSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.block_id).getValue());
        floorSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.floor_id).getValue());
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        descriptionTF.setValue("");
        blockSelect.setValue(null);
        floorSelect.setValue(null);
        statusSelect.setValue(null);
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Description)).setValue(descriptionTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.block_id).setValue(blockSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Block)).setValue(blockSelect.
                getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.floor_id).setValue(floorSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Floor)).setValue(floorSelect.
                getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Description)).setValue(
                descriptionTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(
                statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Block)).setValue(
                blockSelect.getContainerProperty(blockSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.block_id).setValue(
                blockSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Floor)).setValue(
                floorSelect.getContainerProperty(floorSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.floor_id).setValue(
                floorSelect.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Room getRoom(int i) {
        Room room = new Room();
        room.setName(nameTF.getValue());
        room.setDescription(descriptionTF.getValue());
        room.setBlock_id((Integer) blockSelect.getValue());
        room.setFloor_id((Integer) floorSelect.getValue());
        room.setActivity_status_id((Integer) statusSelect.getValue());
        room.setId(i);
        return room;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbRoom);
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
        return new RoomDefinitionView(myUI);
    }
}
