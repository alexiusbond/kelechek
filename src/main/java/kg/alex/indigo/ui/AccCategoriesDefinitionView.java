package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbAccCategory;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.domain.AccCategory;
import kg.alex.indigo.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;

public class AccCategoriesDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(AccCategoriesDefinitionView.class);
    private final MyVaadinUI myUI;
    private final TreeTable dataTable;
    private final String[] NATURAL_COL_ORDER;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final int movement_type_id;
    private final String permission;
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox parentSelect, statusSelect;
    private TextField nameTF, codeTF;
    private TextArea noteTF;
    private boolean isNew;
    private String parent_code = "";
    private Label parent_code_label;
    private VerticalLayout settingsLay;

    public AccCategoriesDefinitionView(MyVaadinUI myUI, int movement_type_id, String permission) {
        this.myUI = myUI;
        this.movement_type_id = movement_type_id;
        this.permission = permission;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.Code),
                myUI.getMessage(Messages.Title), myUI.getMessage(Messages.Parent),
                myUI.getMessage(Messages.Status), myUI.getMessage(Messages.Note)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new TreeTable();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.addValueChangeListener(this);
        dataTable.setNullSelectionAllowed(false);
        setTableOptions();
        vl.addComponent(dataTable);

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);

        prepareNormalMode();
    }

    private void setTableOptions() {
        try {
            DbAccCategory dbCon = new DbAccCategory();
            dbCon.connect();
            dbCon.execSQL(myUI, movement_type_id, movement_type_id == 5 ? myUI.getUser().getSchool().getId() : 0,
                    movement_type_id == 5, dataTable);
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setWidth(Settings.PERCENTS100);
        GridLayout codeLay = new GridLayout(2, 1);
        codeLay.setMargin(false);
        codeLay.setSpacing(false);
        codeLay.setWidth(Settings.PERCENTS100);

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

        parentSelect = new ComboBox(myUI.getMessage(Messages.Parent));
        parentSelect.setNullSelectionAllowed(true);
        parentSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        parentSelect.setWidth(Settings.PERCENTS100);
        parentSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.FullName));
        parentSelect.setFilteringMode(FilteringMode.CONTAINS);
        if (movement_type_id == 5) {
            parentSelect.setNullSelectionAllowed(false);
            parentSelect.setRequired(true);
            parentSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        }
        setParentCombo();
        parentSelect.addValueChangeListener(this);
        settingsLay.addComponent(parentSelect);

        nameTF = new TextField(myUI.getMessage(Messages.Title));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 1, 350, false));
        settingsLay.addComponent(nameTF);

        parent_code_label = new Label();
        parent_code_label.setImmediate(true);
        parent_code_label.setSizeUndefined();
        codeLay.addComponent(parent_code_label, 0, 0);
        codeLay.setComponentAlignment(parent_code_label, Alignment.BOTTOM_LEFT);

        codeTF = new TextField(myUI.getMessage(Messages.Code));
        codeTF.setRequired(true);
        codeTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        codeTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        codeTF.setWidth(Settings.PERCENTS100);
        codeTF.addValidator(new RegexpValidator("[0-9]{1,20}", true,
                myUI.getMessage(Messages.NotificationWrongValue)));
        codeLay.addComponent(codeTF, 1, 0);
        codeLay.setColumnExpandRatio(1, 1);
        settingsLay.addComponent(codeLay);

        noteTF = new TextArea(myUI.getMessage(Messages.Note));
        noteTF.setWidth(Settings.PERCENTS100);
        noteTF.setImmediate(true);
        noteTF.setRows(3);
        noteTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        noteTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 0, 200, false));
        settingsLay.addComponent(noteTF);

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
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
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
                    DbAccCategory dbac = new DbAccCategory();
                    dbac.connect();
                    if (isNew) {
                        int id = dbac.exec_insert(getAccCategory(0));
                        if (id != 0) {
                            setTableOptions();
                            setParentCombo();
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            prepareNormalMode();
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        AccCategory acc_category = getAccCategory((Integer) dataTable.getValue());
                        try {
                            status = dbac.exec_update(acc_category);
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            dbac.exec_update_all_parent_codes(acc_category.getId(), acc_category.getCode(), true);
                            setTableOptions();
                            setParentCombo();
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            prepareNormalMode();
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbac.close();
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
        } else if (property == parentSelect) {
            if (parentSelect.getValue() != null) {
                parent_code = parentSelect.getContainerProperty(parentSelect.getValue(),
                        myUI.getMessage(Messages.Code)).getValue().toString();
                parent_code_label.setValue(parent_code + ".");

            } else {
                parent_code = "";
                parent_code_label.setValue(parent_code);
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
        noteTF.setEnabled(true);
        codeTF.setEnabled(true);
        parentSelect.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(permission + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(permission + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(permission + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        noteTF.setEnabled(false);
        codeTF.setEnabled(false);
        parentSelect.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        if ((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                Settings.parent_id).getValue() != 0) {
            parentSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.parent_id).getValue());
        } else {
            parentSelect.setValue(null);
        }
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).getValue().toString());
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Note)).getValue() != null) {
            noteTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.Note)).getValue().toString());
        } else {
            noteTF.setValue("");
        }
        if (parentSelect.getValue() != null) {
            parent_code = parentSelect.getContainerProperty(parentSelect.getValue(),
                    myUI.getMessage(Messages.Code)).getValue().toString();
            parent_code_label.setValue(parent_code + ".");
        } else {
            parent_code = "";
            parent_code_label.setValue("");
        }
        codeTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Code)).getValue().toString().replace((parent_code + "."), ""));
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        noteTF.setValue("");
        codeTF.setValue("");
        parent_code = "";
        parent_code_label.setValue(parent_code);
        parentSelect.setValue(null);
        statusSelect.setValue(null);
    }

    private AccCategory getAccCategory(int id) {
        AccCategory ac = new AccCategory();
        ac.setName(nameTF.getValue());
        ac.setNote(noteTF.getValue());
        ac.setCode(codeTF.getValue());
        if (parentSelect.getValue() != null) {
            ac.setParent_id((Integer) parentSelect.getValue());
            ac.setParent_code(parent_code);
        }
        if (movement_type_id == 5) {
            ac.setSchool_id(myUI.getUser().getSchool().getId());
        }
        ac.setStatus_id((Integer) statusSelect.getValue());
        ac.setType_id(movement_type_id);
        ac.setModified_employee_id(myUI.getUser().getId());
        ac.setId(id);
        return ac;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getValue(), Settings.dbAcc_category);
            if (st != 0) {
                setParentCombo();
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

    private void setParentCombo() {
        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            if (movement_type_id == 5) {
                parentSelect.setContainerDataSource(dbac.exec_for_select(myUI, movement_type_id, myUI.getUser().getSchool().getId()));
            } else {
                parentSelect.setContainerDataSource(dbac.exec_for_select(myUI, movement_type_id));
            }
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
