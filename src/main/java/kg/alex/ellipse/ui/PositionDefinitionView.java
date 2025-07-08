package kg.alex.ellipse.ui;

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
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.dao.DbDefinition;
import kg.alex.ellipse.dao.DbPosition;
import kg.alex.ellipse.domain.Position;
import kg.alex.ellipse.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class PositionDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(PositionDefinitionView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;
    private final String[] PERMISSION_NATURAL_COL_ORDER;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox categorySelect, statusSelect;
    private Table permissionTable;
    private IndexedContainer permissionCont;
    private TextField nameTF;
    private boolean isNew;
    private VerticalLayout settingsLay;

    public PositionDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        PERMISSION_NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.ClassCaption),
                myUI.getMessage(Messages.Functions)};
        String[] NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.Title),
                myUI.getMessage(Messages.Category),
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
            DbPosition dbp = new DbPosition();
            dbp.connect();
            dataTable.setContainerDataSource(dbp.execSQL(myUI));
            dbp.close();
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
                myUI.getMessage(Messages.NotificationWrongValue), 1, 100, false));
        settingsLay.addComponent(nameTF);

        categorySelect = new ComboBox(myUI.getMessage(Messages.Category));
        categorySelect.setNullSelectionAllowed(false);
        categorySelect.setRequired(true);
        categorySelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        categorySelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        categorySelect.setWidth(Settings.PERCENTS100);
        categorySelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        categorySelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(categorySelect);

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
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
            categorySelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.positionCategoryTable, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(statusSelect);

        permissionTable = new Table();
        permissionTable.setCaption(myUI.getMessage(Messages.Permissions));
        permissionTable.setStyleName(ValoTheme.TABLE_SMALL);
        permissionTable.setSizeFull();
        permissionTable.setSelectable(false);
        settingsLay.addComponent(permissionTable);
        setPermContainer();

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
                    DbPosition dbp = new DbPosition();
                    dbp.connect();
                    if (isNew) {
                        int id = dbp.exec_insert(getPosition(0));
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
                            status = dbp.exec_update(
                                    getPosition((Integer) dataTable.getContainerProperty(dataTable.getValue(),
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
                    dbp.close();
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
                permissionTable.setVisible((Integer) dataTable.getContainerProperty(dataTable.getValue(), Settings.position_id).getValue() != 5
                        && (Integer) dataTable.getContainerProperty(dataTable.getValue(), Settings.position_id).getValue() != 25);
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
        categorySelect.setEnabled(true);
        statusSelect.setEnabled(true);
        permissionTable.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnHRDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnHRDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnHRDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        categorySelect.setEnabled(false);
        statusSelect.setEnabled(false);
        permissionTable.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).getValue().toString());
        categorySelect.setValue(
                dataTable.getContainerProperty(dataTable.getValue(),
                        Settings.hr_position_category_id).getValue());
        statusSelect.setValue(
                dataTable.getContainerProperty(dataTable.getValue(),
                        Settings.activity_status_id).getValue());
        clearPermissionTable();
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Permissions)).getValue() != null) {
            setPermTable_options(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.Permissions)).getValue().toString());
        }
    }

    private void clearFields() {
        nameTF.setValue("");
        categorySelect.setValue(null);
        statusSelect.setValue(null);
        clearPermissionTable();
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.hr_position_category_id).setValue(categorySelect
                .getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Category)).setValue(categorySelect
                .getContainerProperty(categorySelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Permissions)).setValue(permJoinSingleStr());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.activity_status_id).setValue(statusSelect
                .getValue());
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
        item.getItemProperty(myUI.getMessage(Messages.Category)).setValue(
                categorySelect.getContainerProperty(categorySelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        item.getItemProperty(myUI.getMessage(Messages.Permissions)).setValue(permJoinSingleStr());
        item.getItemProperty(Settings.hr_position_category_id).setValue(
                categorySelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.activity_status_id).setValue(
                statusSelect.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Position getPosition(int i) {
        Position p = new Position();
        p.setName(nameTF.getValue());
        p.setPosition_category_id((Integer) categorySelect.getValue());
        p.setActivity_status_id((Integer) statusSelect.getValue());
        p.setPermissions(permJoinSingleStr());
        p.setId(i);
        return p;
    }

    private void execDelete() {

        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            int st = dbd.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.hr_positionTable);
            if (st != 0) {
                dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                } else {
                    clearFields();
                    dataTable.setValue(null);
                }
            }
            dbd.close();
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
        return new PositionDefinitionView(myUI);
    }

    private void setPermContainer() {
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            permissionCont = dbd.execPermissionSQL(myUI);
            dbd.close();
            for (Object next : permissionCont.getItemIds()) {
                ComboBoxMultiselect permMCB = new ComboBoxMultiselect();
                permMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
                permMCB.setWidth(Settings.PERCENTS100);
                permMCB.setShowSelectAllButton((filter, page) -> true);
                permMCB.addItems(convertStrToSet(permissionCont.getContainerProperty(next,
                        myUI.getMessage(Messages.Value)).getValue().toString()));
                permissionCont.getContainerProperty(next,
                        myUI.getMessage(Messages.Functions)).setValue(permMCB);
            }
            permissionTable.setContainerDataSource(permissionCont);
            permissionTable.setVisibleColumns((Object[]) PERMISSION_NATURAL_COL_ORDER);
            permissionTable.setPageLength(7);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private Set<?> convertStrToSet(String str) {
        String[] strArr = str.split(",");
        HashSet<String> hs = new HashSet<>(strArr.length);
        hs.addAll(Arrays.asList(strArr));
        return hs;
    }

    private String permJoinSingleStr() {
        Iterator<?> iter = ((IndexedContainer) permissionTable
                .getContainerDataSource()).getItemIds().iterator();
        StringBuilder permissions = new StringBuilder();
        while (iter.hasNext()) {
            Object next = iter.next();
            if (Settings.convertCollectionToStr((Set) ((ComboBoxMultiselect) (permissionTable
                    .getContainerProperty(next, myUI.getMessage(Messages.Functions))
                    .getValue())).getValue()) != null) {
                permissions.append(next).append(":").append(Settings.convertCollectionToStr((Set) ((ComboBoxMultiselect) (permissionTable
                        .getContainerProperty(next, myUI.getMessage(Messages.Functions))
                        .getValue())).getValue())).append(";");
            }
        }
        if (permissions.length() > 1) {
            permissions = new StringBuilder(permissions.substring(0, permissions.length() - 1));
        }
        if (permissions.toString().isEmpty()) {
            return null;
        } else {
            return permissions.toString();
        }
    }

    private void clearPermissionTable() {
        if (permissionCont != null) {
            for (int i = 0; i < permissionCont.size(); i++) {
                ((ComboBoxMultiselect) permissionCont.getContainerProperty(permissionCont.getIdByIndex(i),
                        myUI.getMessage(Messages.Functions)).getValue()).setValue(null);
            }
        }
    }

    private void setPermTable_options(String oneStringAll) {
        try {
            String[] dividedStringAll = oneStringAll.split(";");
            int z = permissionCont.size();
            for (int i = 0; i < z; i++) {
                if (i < dividedStringAll.length && dividedStringAll[i] != null) {
                    String[] byClassName = dividedStringAll[i].split(":");
                    Item itm = permissionCont.getItem(byClassName[0]);
                    if (itm != null) {
                        ((ComboBoxMultiselect) itm.getItemProperty(
                                myUI.getMessage(Messages.Functions)).getValue())
                                .setValue(convertStrToSet(byClassName[1]));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
