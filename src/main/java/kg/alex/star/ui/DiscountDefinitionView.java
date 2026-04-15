package kg.alex.star.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.dao.DbDefinition;
import kg.alex.star.dao.DbDiscount;
import kg.alex.star.dao.DbStudentDiscount;
import kg.alex.star.domain.Discount;
import kg.alex.star.i18n.Messages;
import kg.alex.star.utils.DefinitionsFilterGenerator;
import kg.alex.star.utils.FormattedFilterTable;
import kg.alex.star.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.SQLIntegrityConstraintViolationException;

public class DiscountDefinitionView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DiscountDefinitionView.class);
    private final MyVaadinUI myUI;
    private final FormattedFilterTable dataTable;
    private final String[] NATURAL_COL_ORDER;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn;
    private ComboBox discTypeSelect, statusSelect, yearSelect;
    private TextField nameTF, valueTF;
    private PopupButton copyButton;
    private boolean isNew;
    private VerticalLayout settingsLay;

    public DiscountDefinitionView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{
                myUI.getMessage(Messages.DiscountType),
                myUI.getMessage(Messages.Title),
                myUI.getMessage(Messages.Value),
                myUI.getMessage(Messages.Year),
                myUI.getMessage(Messages.Status)};
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
        setTable_options();
        dataTable.setNullSelectionAllowed(false);
        dataTable.setFilterFieldValue(myUI.getMessage(Messages.Year),
                myUI.getUser().getCurrent_year().getName());
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

        discTypeSelect = new ComboBox(myUI.getMessage(Messages.DiscountType));
        discTypeSelect.setNullSelectionAllowed(false);
        discTypeSelect.setRequired(true);
        discTypeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        discTypeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        discTypeSelect.setWidth(Settings.PERCENTS100);
        discTypeSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        discTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        discTypeSelect.addValueChangeListener(this);
        settingsLay.addComponent(discTypeSelect);

        nameTF = new TextField(myUI.getMessage(Messages.Title));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 1, 250, false));
        settingsLay.addComponent(nameTF);

        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        valueTF = new TextField(myUI.getMessage(Messages.Value), property);
        valueTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        valueTF.setRequired(true);
        valueTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        valueTF.setNullRepresentation("");
        valueTF.setConverter(Settings.getStringToDoubleConverter(2));
        valueTF.setWidth(Settings.PERCENTS100);
        valueTF.addValidator(new DoubleRangeValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 0.01, null));
        settingsLay.addComponent(valueTF);

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
            discTypeSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbDiscountType, true));
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
            prepareModificationMode();
            fillFields();
            int i = 0;
            try {
                DbStudentDiscount dbsd = new DbStudentDiscount();
                dbsd.connect();
                i = dbsd.exec_disc_count((Integer) dataTable.getValue());
                dbsd.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            if (i != 0) {
                discTypeSelect.setEnabled(false);
                valueTF.setEnabled(false);
            } else {
                discTypeSelect.setEnabled(true);
                valueTF.setEnabled(true);
            }
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
            discTypeSelect.setEnabled(true);
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
                    DbDiscount dbDis = new DbDiscount();
                    dbDis.connect();
                    if (isNew) {
                        int id = dbDis.exec_insert(getDiscount(0));
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
                                    getDiscount((Integer) dataTable.getContainerProperty(dataTable.getValue(),
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
                DbDiscount dbDis = new DbDiscount();
                dbDis.connect();
                yearSelect.setContainerDataSource(
                        dbDis.execSQL_for_year_sel(myUI, myUI.getUser().getCurrent_year().getId()));
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
        } else if (property == discTypeSelect && discTypeSelect.getValue() != null) {
            if ((Integer) discTypeSelect.getValue() == 1) {
                valueTF.setCaption(myUI.getMessage(Messages.Value));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(Messages.NotificationWrongPercentValue), 0.01, 100.0));
            } else if ((Integer) discTypeSelect.getValue() == 3) {
                valueTF.setCaption(myUI.getMessage(Messages.MaxValue));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(Messages.NotificationWrongPercentValue), 0.01, 100.0));
            } else if ((Integer) discTypeSelect.getValue() == 2) {
                valueTF.setCaption(myUI.getMessage(Messages.Value));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), 0.01, null));
            } else if ((Integer) discTypeSelect.getValue() == 4) {
                valueTF.setCaption(myUI.getMessage(Messages.MaxValue));
                valueTF.removeAllValidators();
                valueTF.addValidator(new DoubleRangeValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), 0.01, null));
            }
        } else if (property == yearSelect && yearSelect.getValue() != null
                   && dataTable.getValue() != null) {
            try {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmDiscountCopy)
                        + yearSelect.getContainerProperty(yearSelect.getValue(),
                                        myUI.getMessage(Messages.Title))
                                .getValue().toString() + " года?",
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                copyDiscounts();
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
        copyButton.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        dataTable.setEnabled(false);
        nameTF.setEnabled(true);
        valueTF.setEnabled(true);
        statusSelect.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnDiscountDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnDiscountDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnDiscountDefinitionView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnDiscountDefinitionView + ":" + Settings.actCopy)) {
            copyButton.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        dataTable.setEnabled(true);
        nameTF.setEnabled(false);
        valueTF.setEnabled(false);
        discTypeSelect.setEnabled(false);
        statusSelect.setEnabled(false);
    }

    private void fillFields() {
        nameTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).getValue().toString());
        valueTF.setValue(dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Value)).getValue().toString());
        discTypeSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(), Settings.discount_type_id).getValue());
        statusSelect.setValue(dataTable.getContainerProperty(dataTable.getValue(), Settings.status_id).getValue());
    }

    private void clearFields() {
        nameTF.setValue("");
        valueTF.setValue(null);
        valueTF.removeAllValidators();
        discTypeSelect.setValue(null);
        statusSelect.setValue(null);
    }

    private void updateDataContainer() {
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Title)).setValue(nameTF.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Value)).setValue(
                valueTF.getPropertyDataSource().getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.DiscountType)).setValue(discTypeSelect
                .getContainerProperty(discTypeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.discount_type_id).setValue(discTypeSelect.getValue());
        dataTable.getContainerProperty(dataTable.getValue(),
                myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        dataTable.getContainerProperty(dataTable.getValue(),
                Settings.status_id).setValue(statusSelect.getValue());
    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                nameTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Value)).setValue(
                valueTF.getPropertyDataSource().getValue());
        item.getItemProperty(myUI.getMessage(Messages.DiscountType)).setValue(
                discTypeSelect.getContainerProperty(discTypeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.discount_type_id).setValue(discTypeSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Status)).setValue(
                statusSelect.getContainerProperty(statusSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.status_id).setValue(statusSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Year)).setValue(
                myUI.getUser().getCurrent_year().getName());
        item.getItemProperty(Settings.year_id).setValue(
                myUI.getUser().getCurrent_year().getId());
        item.getItemProperty(Settings.id).setValue(id);
        dataTable.setValue(id);
    }

    private Discount getDiscount(int i) {
        Discount d = new Discount();
        d.setDisc_type_id((Integer) discTypeSelect.getValue());
        d.setName(nameTF.getValue());
        d.setAmount((Double) valueTF.getPropertyDataSource().getValue());
        d.setYear_id(myUI.getUser().getCurrent_year().getId());
        d.setStatus_id((Integer) statusSelect.getValue());
        d.setId(i);
        return d;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete((Integer) dataTable.getContainerProperty(dataTable.getValue(),
                    Settings.id).getValue(), Settings.dbDiscount);
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

    private void setTable_options() {
        try {
            DbDiscount dbd = new DbDiscount();
            dbd.connect();
            dataTable.setContainerDataSource(dbd.execSQL(myUI));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        dataTable.setColumnAlignment(myUI.getMessage(Messages.Value), CustomTable.Align.RIGHT);
        if (dataTable.getContainerDataSource().size() != 0) {
            dataTable.setValue(((IndexedContainer) dataTable.getContainerDataSource()).firstItemId());
        }
    }

    private void copyDiscounts() {
        int status = 0;
        try {
            DbDiscount dbDis = new DbDiscount();
            dbDis.connect();
            status = dbDis.exec_copy(myUI.getUser().getCurrent_year().getId(),
                    (Integer) yearSelect.getValue());
            dbDis.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (status != 0) {
            setTable_options();
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
