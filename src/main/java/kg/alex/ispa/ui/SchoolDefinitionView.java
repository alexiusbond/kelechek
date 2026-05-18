package kg.alex.ispa.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.dao.*;
import kg.alex.ispa.domain.AccCategory;
import kg.alex.ispa.domain.School;
import kg.alex.ispa.i18n.Messages;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.SQLIntegrityConstraintViolationException;

public class SchoolDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(SchoolDefinitionView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox statusSelect, typeSelect;
    private TextField nameKgTF, nameEnTF, codeTF, nameRuTF, addressTF, innTF, bankTF,
            bankAccountTF, phoneTF, cityTF;
    private boolean isNew;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private ProgressBar uploadProgressBar;
    private String photoName, mimeType;
    private Embedded photoEmb;
    private GridLayout settingsLay;

    public SchoolDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        String[] NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.Code),
                myUI.getMessage(Messages.TitleRu), myUI.getMessage(Messages.TitleKg),
                myUI.getMessage(Messages.TitleEn),
                myUI.getMessage(Messages.SchoolType), myUI.getMessage(Messages.Status),
                myUI.getMessage(Messages.Bank), myUI.getMessage(Messages.BankAccount),
                myUI.getMessage(Messages.Address), myUI.getMessage(Messages.Phone)};
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
            DbSchool dbscl = new DbSchool();
            dbscl.connect();
            dataTable.setContainerDataSource(dbscl.execSQL(myUI));
            dbscl.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setColumnCollapsingAllowed(true);
        dataTable.setColumnCollapsed(myUI.getMessage(Messages.TitleKg), true);
        dataTable.setColumnCollapsed(myUI.getMessage(Messages.TitleEn), true);
        dataTable.setColumnCollapsed(myUI.getMessage(Messages.Bank), true);
        dataTable.setColumnCollapsed(myUI.getMessage(Messages.BankAccount), true);
        dataTable.setColumnCollapsed(myUI.getMessage(Messages.Address), true);
        dataTable.setColumnCollapsed(myUI.getMessage(Messages.Phone), true);
        dataTable.setColumnCollapsed(myUI.getMessage(Messages.Logo), true);
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
        dataTable.setNullSelectionAllowed(false);
        vl.addComponent(dataTable);

        this.setSplitPosition(40, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);

        prepareNormalMode();
    }

    private void buildSettingsLayout() {

        settingsLay = new GridLayout(2, 7);
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setSizeFull();
        settingsLay.setRowExpandRatio(6, 1);

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
        settingsLay.addComponent(buttonsLay, 0, 0, 1, 0);

        codeTF = new TextField(myUI.getMessage(Messages.Code));
        codeTF.setRequired(true);
        codeTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        codeTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        codeTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(codeTF);

        innTF = new TextField(myUI.getMessage(Messages.INN));
        innTF.setRequired(false);
        innTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        innTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(innTF);

        nameRuTF = new TextField(myUI.getMessage(Messages.TitleRu));
        nameRuTF.setRequired(true);
        nameRuTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        nameRuTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameRuTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(nameRuTF);

        nameKgTF = new TextField(myUI.getMessage(Messages.TitleKg));
        nameKgTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameKgTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(nameKgTF);

        nameEnTF = new TextField(myUI.getMessage(Messages.TitleEn));
        nameEnTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameEnTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(nameEnTF);

        cityTF = new TextField(myUI.getMessage(Messages.City) + " (Ru/Kg)");
        cityTF.setRequired(false);
        cityTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        cityTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(cityTF);

        addressTF = new TextField(myUI.getMessage(Messages.Address) + " (Ru/Kg)");
        addressTF.setRequired(false);
        addressTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        addressTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(addressTF);

        phoneTF = new TextField(myUI.getMessage(Messages.Phone));
        phoneTF.setRequired(false);
        phoneTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        phoneTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(phoneTF);

        bankTF = new TextField(myUI.getMessage(Messages.Bank) + " (Ru/Kg)");
        bankTF.setRequired(false);
        bankTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(bankTF);

        bankAccountTF = new TextField(myUI.getMessage(Messages.BankAccount));
        bankAccountTF.setRequired(false);
        bankAccountTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankAccountTF.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(bankAccountTF);

        typeSelect = new ComboBox(myUI.getMessage(Messages.SchoolType));
        typeSelect.setNullSelectionAllowed(false);
        typeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        typeSelect.setRequired(true);
        typeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        typeSelect.setWidth(Settings.PERCENTS100);
        typeSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        typeSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(typeSelect);

        photoEmb = new Embedded();
        photoEmb.setHeight("100%");
        photoEmb.setWidth("70%");
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        settingsLay.addComponent(photoEmb);
        settingsLay.setComponentAlignment(photoEmb, Alignment.MIDDLE_CENTER);

        buildUpload();
        settingsLay.addComponent(new Label());
        settingsLay.addComponent(photoUpl);

        statusSelect = new ComboBox(myUI.getMessage(Messages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        statusSelect.setWidth(Settings.PERCENTS100);
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
            typeSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbSchoolType, false));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(statusSelect);
        settingsLay.setComponentAlignment(statusSelect, Alignment.TOP_CENTER);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source.getId() != null && source.getId().equals(Settings.cancel_upload_button)) {
            if (photoUpl != null) {
                photoUpl.interruptUpload();
            }
        } else if (source == modifyBtn && dataTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            codeTF.focus();
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
                    DbSchool dbScl = new DbSchool();
                    dbScl.connect();
                    if (isNew) {
                        School sch = getSchool(0);
                        int id = dbScl.exec_insert(sch);
                        if (id != 0) {
                            DbSalaryCategories dbsc = new DbSalaryCategories();
                            dbsc.connect();
                            IndexedContainer salCont = dbsc.execSQL(myUI);
                            dbsc.close();
                            DbAccType dbAccType = new DbAccType();
                            dbAccType.connect();
                            IndexedContainer typesContainer = dbAccType.execSQL(myUI);
                            dbAccType.close();
                            DbAccCategory dba = new DbAccCategory();
                            dba.connect();
                            for (Object next : salCont.getItemIds()) {
                                AccCategory ac = new AccCategory();
                                ac.setCode(sch.getCode());
                                ac.setName(salCont.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue() + " - " + sch.getName_ru());
                                ac.setParent_code(salCont.getContainerProperty(next, myUI.getMessage(Messages.Code)).getValue().toString());
                                ac.setParent_id((Integer) next);
                                ac.setStatus_id(2);
                                ac.setType_id(2);
                                ac.setSchool_id(id);
                                ac.setModified_employee_id(myUI.getUser().getId());
                                dba.exec_insert(ac);
                            }
                            for (Object next : typesContainer.getItemIds()) {
                                AccCategory ac = new AccCategory();
                                ac.setCode(sch.getCode());
                                ac.setName(typesContainer.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue() + " - " + sch.getName_ru());
                                ac.setParent_code(typesContainer.getContainerProperty(next, myUI.getMessage(Messages.Code)).getValue().toString());
                                ac.setParent_id((Integer) next);
                                ac.setStatus_id(2);
                                ac.setType_id(5);
                                ac.setSchool_id(id);
                                ac.setModified_employee_id(myUI.getUser().getId());
                                dba.exec_insert(ac);
                            }
                            dba.close();
                            addDataContainerItem(id);
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            Item item = (myUI.getSchoolCont()).addItem(id);
                            item.getItemProperty(myUI.getMessage(Messages.Logo)).setValue(sch.getPhoto());
                            item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(codeTF.getValue());
                            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                                    codeTF.getValue() + " - " + nameRuTF.getValue());
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        School school = getSchool((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                Settings.id).getValue());
                        try {
                            status = dbScl.exec_update(school);
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            if (!school.getCode().equals(dataTable.getContainerProperty(dataTable.getContainerProperty(dataTable.getValue(),
                                    Settings.id).getValue(), myUI.getMessage(Messages.Code)).getValue())
                                    || !school.getName_kg().equals(dataTable.getContainerProperty(dataTable.getContainerProperty(dataTable.getValue(),
                                    Settings.id).getValue(), myUI.getMessage(Messages.TitleRu)).getValue())) {
                                DbSalaryCategories dbsc = new DbSalaryCategories();
                                dbsc.connect();
                                IndexedContainer salCont = dbsc.execSQL(myUI);
                                dbsc.close();
                                DbAccType dbAccType = new DbAccType();
                                dbAccType.connect();
                                IndexedContainer typesContainer = dbAccType.execSQL(myUI);
                                dbAccType.close();
                                DbAccCategory dba = new DbAccCategory();
                                dba.connect();
                                for (Object next : salCont.getItemIds()) {
                                    int id = dba.exec_id((Integer) next, school.getId());
                                    dba.exec_update_code(id, school.getCode(), salCont.getContainerProperty(next,
                                            myUI.getMessage(Messages.Title)).getValue() + " - " + school.getName_ru());
                                    dba.exec_update_all_parent_codes(id, salCont.getContainerProperty(next,
                                            myUI.getMessage(Messages.Code)).getValue() + "." + school.getCode(), false);
                                }
                                for (Object next : typesContainer.getItemIds()) {
                                    int id = dba.exec_id((Integer) next, school.getId());
                                    dba.exec_update_code(id, school.getCode(), typesContainer.getContainerProperty(next,
                                            myUI.getMessage(Messages.Title)).getValue() + " - " + school.getName_ru());
                                    dba.exec_update_all_parent_codes(id, typesContainer.getContainerProperty(next,
                                            myUI.getMessage(Messages.Code)).getValue() + "." + school.getCode(), false);
                                }
                                dba.close();
                            }
                            updateDataContainer();
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                            myUI.getSchoolCont().getContainerProperty(dataTable.getValue(),
                                    myUI.getMessage(Messages.Title)).setValue(codeTF.getValue() + " - " + nameRuTF.getValue());
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbScl.close();
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
                clearFields();
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
        nameKgTF.setEnabled(true);
        statusSelect.setEnabled(true);
        typeSelect.setEnabled(true);
        codeTF.setEnabled(true);
        nameRuTF.setEnabled(true);
        nameEnTF.setEnabled(true);
        cityTF.setEnabled(true);
        innTF.setEnabled(true);
        addressTF.setEnabled(true);
        bankTF.setEnabled(true);
        bankAccountTF.setEnabled(true);
        phoneTF.setEnabled(true);
        photoUpl.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnSchoolDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnSchoolDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnSchoolDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameKgTF.setEnabled(false);
        statusSelect.setEnabled(false);
        typeSelect.setEnabled(false);
        codeTF.setEnabled(false);
        nameRuTF.setEnabled(false);
        nameEnTF.setEnabled(false);
        cityTF.setEnabled(false);
        innTF.setEnabled(false);
        addressTF.setEnabled(false);
        bankTF.setEnabled(false);
        bankAccountTF.setEnabled(false);
        phoneTF.setEnabled(false);
        photoUpl.setEnabled(false);

    }

    private void fillFields() {
        codeTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Code)).getValue().toString());
        nameRuTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.TitleRu)).getValue().toString());
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue());
        typeSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.school_type_id).getValue());
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.TitleKg)).getValue() != null) {
            nameKgTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.TitleKg)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.TitleEn)).getValue() != null) {
            nameEnTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.TitleEn)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.City)).getValue() != null) {
            cityTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.City)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Address)).getValue() != null) {
            addressTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.Address)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.INN)).getValue() != null) {
            innTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.INN)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Bank)).getValue() != null) {
            bankTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.Bank)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.BankAccount)).getValue() != null) {
            bankAccountTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.BankAccount)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Phone)).getValue() != null) {
            phoneTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.Phone)).getValue().toString());
        }
        if (dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Logo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS
                    + dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.Logo)).getValue().toString())));
            photoName = dataTable.getContainerProperty(dataTable.getValue(),
                    myUI.getMessage(Messages.Logo)).getValue().toString();
        } else {
            photoName = null;
        }

    }

    private void clearFields() {
        codeTF.setValue("");
        nameKgTF.setValue("");
        nameRuTF.setValue("");
        nameEnTF.setValue("");
        cityTF.setValue("");
        addressTF.setValue("");
        innTF.setValue("");
        bankTF.setValue("");
        bankAccountTF.setValue("");
        phoneTF.setValue("");
        statusSelect.setValue(2);
        typeSelect.setValue(null);
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoName = null;
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Code)).setValue(codeTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.TitleKg)).setValue(nameKgTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.TitleRu)).setValue(nameRuTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.TitleEn)).setValue(nameEnTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.City)).setValue(cityTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Address)).setValue(addressTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.INN)).setValue(innTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Address)).setValue(addressTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Bank)).setValue(bankTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.BankAccount)).setValue(bankAccountTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Phone)).setValue(phoneTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Logo)).setValue(photoName);
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Status)).setValue(statusSelect.
                getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.school_type_id).setValue(typeSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.SchoolType)).setValue(typeSelect.
                getContainerProperty(typeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(Messages.Code)).setValue(codeTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.TitleKg)).setValue(nameKgTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.TitleRu)).setValue(nameRuTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.TitleEn)).setValue(nameEnTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.City)).setValue(cityTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Address)).setValue(addressTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.INN)).setValue(innTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Bank)).setValue(bankTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.BankAccount)).setValue(bankAccountTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Phone)).setValue(phoneTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Logo)).setValue(photoName);
        item.getItemProperty(myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.SchoolType)).setValue(
                typeSelect.getContainerProperty(typeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.school_type_id).setValue(typeSelect.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private School getSchool(int i) {
        School school = new School();
        school.setId(i);
        school.setCode(codeTF.getValue());
        school.setName_kg(nameKgTF.getValue());
        school.setName_ru(nameRuTF.getValue());
        school.setName_en(nameEnTF.getValue());
        school.setCity(cityTF.getValue());
        school.setAddress(addressTF.getValue());
        school.setInn(innTF.getValue());
        school.setBank(bankTF.getValue());
        school.setBank_account(bankAccountTF.getValue());
        school.setPhone(phoneTF.getValue());
        school.setStatus_id((Integer) statusSelect.getValue());
        school.setSchool_type_id((Integer) typeSelect.getValue());
        school.setSchool_type_id(1);
        school.setPhoto(photoName);
        return school;
    }

    private void execDelete() {
        DbDefinition dbDef = null;
        try {
            dbDef = new DbDefinition();
            dbDef.connect();
            dbDef.getConnection().setAutoCommit(false);
            dbDef.exec_delete(dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue().toString(), Settings.dbAcc_category, Settings.school_id);
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbSchool);
            if (st != 0) {
                myUI.getSchoolCont().removeItem(dataTable.getContainerProperty(
                        dataTable.getValue(), Settings.id).getValue());
                dataTable.getContainerDataSource().removeItem(dataTable.getValue());
                if (dataTable.getContainerDataSource().size() != 0) {
                    dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
                } else {
                    clearFields();
                    dataTable.setValue(null);
                }
            }
            dbDef.getConnection().commit();
            dbDef.getConnection().setAutoCommit(true);
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(Messages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            logger.error(e);
            logger.catching(e);
        }
    }

    private void buildUploadWindow() {
        uploadProgressBar = new ProgressBar();
        uploadProgressBar.setWidth("90%");

        statusWindow = new Window(myUI.getMessage(Messages.UploadStatus));
        statusWindow.setResizable(false);
        statusWindow.setDraggable(false);
        statusWindow.setModal(true);
        statusWindow.setWidth("20%");
        statusWindow.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);

        final HorizontalLayout l = new HorizontalLayout();
        l.setSpacing(true);
        l.setWidth(Settings.PERCENTS100);
        l.setMargin(true);
        statusWindow.setContent(l);

        cancelButton = new Button();
        cancelButton.addClickListener(this);
        cancelButton.setId(Settings.cancel_upload_button);
        cancelButton.setVisible(false);
        cancelButton.setIcon(FontAwesome.CLOSE);
        cancelButton.setStyleName(ValoTheme.BUTTON_TINY);
        cancelButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        l.addComponent(cancelButton);
        l.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);

        uploadProgressBar.setCaption(myUI.getMessage(Messages.Progress));
        uploadProgressBar.setVisible(false);
        l.addComponent(uploadProgressBar);
        l.setExpandRatio(uploadProgressBar, 1);
    }

    private void buildUpload() {
        MyReceiver receiver = new MyReceiver();
        photoUpl = new Upload(null, receiver);
        photoUpl.setImmediate(true);
        photoUpl.setStyleName(ValoTheme.BUTTON_SMALL);
        photoUpl.setButtonCaption(myUI.getMessage(Messages.Upload));
        photoUpl.setWidth(Settings.PERCENTS100);

        photoUpl.addStartedListener((Upload.StartedListener) event -> {

            buildUploadWindow();
            myUI.addWindow(statusWindow);
            statusWindow.setClosable(false);

            uploadProgressBar.setValue(0f);
            uploadProgressBar.setVisible(true);
            UI.getCurrent().setPollInterval(500);

            cancelButton.setVisible(true);
        });

        photoUpl.addProgressListener((Upload.ProgressListener) (readBytes, contentLength) -> {
            // This method gets called several times during the update
            if (!mimeType.equals("image/jpeg")) {
                photoUpl.interruptUpload();
                photoName = null;
                Notification.show(myUI.getMessage(Messages.OnlyJpg),
                        Notification.Type.WARNING_MESSAGE);
            } else if (contentLength >= 5000000) {
                photoUpl.interruptUpload();
                photoName = null;
                Notification.show(myUI.getMessage(Messages.Maxsize),
                        Notification.Type.WARNING_MESSAGE);
            } else {

                uploadProgressBar.setValue(readBytes / (float) contentLength);
            }
        });

        photoUpl.addSucceededListener((Upload.SucceededListener) event -> {
            // This method gets called when the upload finished successfully
            try {
                Thumbnails.of(myFile).size(300, 300).toFile(myFile);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            photoEmb.setSource(new FileResource(myFile));
        });

        photoUpl.addFailedListener((Upload.FailedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
            try {
                myFile.delete();
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        });

        photoUpl.addFinishedListener((Upload.FinishedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
        });
    }

    public class MyReceiver implements Upload.Receiver {

        @Override
        public OutputStream receiveUpload(String filename, String mimetype) {
            mimeType = mimetype;
            FileOutputStream fos; // Output stream to write to
            photoName = codeTF.getValue() + ".jpg";
            try {
                myFile = new File(Settings.PATH_TO_UPLOADS + photoName);

                // Open the file for writing.
                fos = new FileOutputStream(myFile);
            } catch (Exception e) {
                // Error while opening the file. Not reported here.
                logger.error(e);
                logger.catching(e);
                return null;
            }
            return fos; // Return the output stream to write tou
        }
    }
}
