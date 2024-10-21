package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbContract;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbStudentContract;
import kg.alex.indigo.domain.Contract;
import kg.alex.indigo.i18n.Messages;
import kg.alex.indigo.utils.DefinitionsFilterGenerator;
import kg.alex.indigo.utils.FormattedFilterTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.SQLIntegrityConstraintViolationException;

public class ContractDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ContractDefinitionView.class);
    private final MyVaadinUI myUI;
    private final FormattedFilterTable dataTable;
    private final String[] NATURAL_COL_ORDER;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox statusSelect, yearSelect;
    private PopupButton copyButton;
    private TextField nameTF, valueTF, durationTF;
    private boolean isNew;
    private VerticalLayout settingsLay;
    private String amountTitle;

    public ContractDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;
        amountTitle = myUI.getUser().getSchool().getCurrency_id() == 1 ?
                myUI.getMessage(Messages.AmountKGS) : myUI.getMessage(Messages.AmountUSD);

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.Title),
                amountTitle, myUI.getMessage(Messages.School),
                myUI.getMessage(Messages.Year), myUI.getMessage(Messages.DurationInMonths)
                , myUI.getMessage(Messages.Status)};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new FormattedFilterTable(myUI);
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(true);
        dataTable.setFilterBarVisible(true);
        dataTable.addValueChangeListener(this);
        setTableOptions();
        dataTable.setFilterFieldValue(myUI.getMessage(Messages.Year),
                myUI.getUser().getCurrent_year().getName());
        dataTable.setNullSelectionAllowed(false);
        dataTable.setFilterGenerator(new DefinitionsFilterGenerator(dataTable));
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
        buttonsLay.setWidth(Settings.PERCENTS100);

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

        yearSelect = new ComboBox(myUI.getMessage(Messages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.addValueChangeListener(this);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        copyButton = new PopupButton(myUI.getMessage(Messages.Copy));
        copyButton.setWidth(Settings.PERCENTS100);
        copyButton.setDescription(myUI.getMessage(Messages.Copy));
        copyButton.setIcon(FontAwesome.COPY);
        copyButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        copyButton.setImmediate(true);
        copyButton.setEnabled(false);
        copyButton.addClickListener(this);
        copyButton.setContent(yearSelect);
        buttonsLay.addComponent(copyButton);
        buttonsLay.setExpandRatio(copyButton, 1);

        nameTF = new TextField(myUI.getMessage(Messages.Title));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 1, 250, false));
        settingsLay.addComponent(nameTF);

        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        valueTF = new TextField(amountTitle, property);
        valueTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        valueTF.setRequired(true);
        valueTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        valueTF.setNullRepresentation("");
        valueTF.setConverter(Settings.getStringToDoubleConverter(2));
        valueTF.setWidth(Settings.PERCENTS100);
        valueTF.addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, null));
        settingsLay.addComponent(valueTF);

        ObjectProperty<Integer> propertyDuration = new ObjectProperty<>(1);
        durationTF = new TextField(myUI.getMessage(Messages.DurationInMonths), propertyDuration);
        durationTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        durationTF.setRequired(true);
        durationTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        durationTF.setNullRepresentation("");
        durationTF.setConverter(Settings.getStringToIntegerConverter());
        durationTF.setWidth(Settings.PERCENTS100);
        durationTF.addValidator(new IntegerRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 1, 12));
        settingsLay.addComponent(durationTF);

        statusSelect = new ComboBox(myUI.getMessage(Messages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setRequired(true);
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
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
            int i = 0;
            try {
                DbStudentContract dbsd = new DbStudentContract();
                dbsd.connect();
                i = dbsd.exec_contr_count((Integer) dataTable.getValue());
                dbsd.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            valueTF.setEnabled(i == 0);
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
                    DbContract dbDis = new DbContract();
                    dbDis.connect();
                    if (isNew) {
                        Contract c = getContract(0);
                        int id = dbDis.exec_insert(c);
                        if (id != 0) {
                            removeTableFilters();
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
                            status = dbDis.exec_update(
                                    getContract((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                                            Settings.id).getValue()));
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            removeTableFilters();
                            updateDataContainer();
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    }
                    dbDis.close();
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
        } else if (source == copyButton) {
            try {
                DbContract dbDis = new DbContract();
                dbDis.connect();
                yearSelect.setContainerDataSource(
                        dbDis.execSQL_for_year_sel(myUI, myUI.getUser().getCurrent_year().getId(),
                                myUI.getUser().getSchool().getId()));
                dbDis.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == dataTable) {
            if (dataTable.getItem(dataTable.getValue()) != null) {
                fillFields();
            } else if (dataTable.getItem(dataTable.getValue()) == null) {
                clearFields();
            }
        } else if (property == yearSelect && yearSelect.getValue() != null
                && dataTable.getValue() != null) {
            try {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmContractCopy)
                                + yearSelect.getContainerProperty(yearSelect.getValue(),
                                        myUI.getMessage(Messages.Title))
                                .getValue().toString() + " года?",
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                copyContracts();
                            }
                        });
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }

        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        copyButton.setEnabled(false);
        dataTable.setEnabled(false);
        nameTF.setEnabled(true);
        valueTF.setEnabled(true);
        durationTF.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnContractDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnContractDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnContractDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnContractDefinitionView + ":" + Settings.actCopy)) {
            copyButton.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        valueTF.setEnabled(false);
        durationTF.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).getValue().toString());
        valueTF.getPropertyDataSource().setValue(dataTable.getContainerProperty(dataTable.getValue(),
                amountTitle).getValue());
        durationTF.getPropertyDataSource().setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.DurationInMonths)).getValue());
        statusSelect.setValue(Integer.parseInt(dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).getValue().toString()));
    }

    private void clearFields() {
        nameTF.setValue("");
        valueTF.setValue(null);
        durationTF.setValue(null);
        statusSelect.setValue(null);
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(), amountTitle).setValue(
                valueTF.getPropertyDataSource().getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.DurationInMonths)).setValue(
                durationTF.getPropertyDataSource().getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
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
        item.getItemProperty(amountTitle).setValue(
                valueTF.getPropertyDataSource().getValue());
        item.getItemProperty(myUI.getMessage(Messages.DurationInMonths)).setValue(
                durationTF.getPropertyDataSource().getValue());
        item.getItemProperty(myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Year)).setValue(
                myUI.getUser().getCurrent_year().getName());
        item.getItemProperty(Settings.year_id).setValue(
                myUI.getUser().getCurrent_year().getId());
        item.getItemProperty(myUI.getMessage(Messages.School)).setValue(
                myUI.getUser().getSchool().getName_ru());
        item.getItemProperty(Settings.school_id).setValue(
                myUI.getUser().getSchool().getId());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Contract getContract(int i) {
        Contract c = new Contract();
        c.setName(nameTF.getValue());
        c.setValue((Double) valueTF.getPropertyDataSource().getValue());
        c.setDuration((Integer) durationTF.getPropertyDataSource().getValue());
        c.setYear_id(myUI.getUser().getCurrent_year().getId());
        c.setSchool_id(myUI.getUser().getSchool().getId());
        c.setStatus_id((Integer) statusSelect.getValue());
        c.setId(i);
        c.setEmployee_id(myUI.getUser().getId());
        return c;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            dbDef.exec_update_emp_id((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), myUI.getUser().getId(), Settings.dbContract);
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbContract);
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

    private void setTableOptions() {
        try {
            DbContract dbd = new DbContract();
            dbd.connect();
            dataTable.setContainerDataSource(dbd.execSQL(myUI, myUI.getUser().getSchool().getId(), amountTitle));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        dataTable.setColumnAlignment(amountTitle, CustomTable.Align.RIGHT);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
    }

    private void copyContracts() {
        int status = 0;
        try {
            DbContract dbCon = new DbContract();
            dbCon.connect();
            status = dbCon.exec_copy((Integer) yearSelect.getValue(), myUI);
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (status != 0) {
            setTableOptions();
            Notification.show(myUI.getMessage(Messages.ValuesCopied),
                    Notification.Type.HUMANIZED_MESSAGE);
        } else {
            Notification.show(myUI.getMessage(Messages.ValuesCanNotBeCopied),
                    Notification.Type.WARNING_MESSAGE);
        }
    }

    private void removeTableFilters() {
        for (Object next : dataTable.getContainerPropertyIds()) {
            dataTable.setFilterFieldValue(next, null);
        }
    }
}
