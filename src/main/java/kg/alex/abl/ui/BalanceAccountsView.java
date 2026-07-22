package kg.alex.abl.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FileDownloader;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.dao.DbDefinition;
import kg.alex.abl.dao.DbInvoice;
import kg.alex.abl.dao.DbTransfers;
import kg.alex.abl.domain.AccBalanceSettings;
import kg.alex.abl.domain.Invoice;
import kg.alex.abl.domain.Transfer;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.utils.FormattedFilterTable;
import kg.alex.abl.utils.MyFilterDecorator;
import kg.alex.abl.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BalanceAccountsView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(BalanceAccountsView.class);
    private final MyVaadinUI myUI;
    private final String[] NATURAL_COL_ORDER;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final SimpleDateFormat df;
    private Button createBtn;
    private Button modifyBtn;
    private Button deleteBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private Button confirmBtn;
    private Button excelBtn;
    private PopupButton searchBtn;
    private TextField invoiceNumberTF;
    private TextArea noteTF, note2TF;
    private DateField dateDF;
    private boolean isNew;
    private GridLayout settingsLay;
    private GridLayout rightLay;
    private FormattedFilterTable invoicesTable;
    private int invID;
    private FileDownloader fdText;

    public BalanceAccountsView(MyVaadinUI myUI) {
        this.myUI = myUI;
        df = Settings.ymdf;
        NATURAL_COL_ORDER = new String[]{Settings.button, myUI.getMessage(Messages.InvoiceNumber),
                myUI.getMessage(Messages.Date), myUI.getMessage(Messages.Amount),
                myUI.getMessage(Messages.Note), myUI.getMessage(Messages.Note) + " 2"};

        buildRightLayout();
        buildSettingsLayout();
        this.setSplitPosition(20, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(rightLay);

        prepareNormalMode();
    }

    private void buildSettingsLayout() {

        settingsLay = new GridLayout(2, 5);
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

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(Messages.ExportToExcel));
        excelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.setEnabled(false);
        buttonsLay.addComponent(excelBtn);

        confirmBtn = new Button();
        confirmBtn.setDescription(myUI.getMessage(Messages.Confirm));
        confirmBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        confirmBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
        confirmBtn.setIcon(FontAwesome.CHECK);
        confirmBtn.setEnabled(false);
        confirmBtn.addClickListener(this);
        buttonsLay.addComponent(confirmBtn);
        settingsLay.addComponent(buttonsLay, 0, 0, 1, 0);

        invoiceNumberTF = new TextField(myUI.getMessage(Messages.InvoiceNumber));
        invoiceNumberTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        invoiceNumberTF.setWidth(Settings.PERCENTS100);
        invoiceNumberTF.addValueChangeListener(this);
        settingsLay.addComponent(invoiceNumberTF);

        searchBtn = new PopupButton();
        searchBtn.setDescription(myUI.getMessage(Messages.Search));
        searchBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        searchBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        searchBtn.setIcon(FontAwesome.BINOCULARS);
        searchBtn.addClickListener(this);
        settingsLay.addComponent(searchBtn);
        settingsLay.setComponentAlignment(searchBtn, Alignment.BOTTOM_RIGHT);

        dateDF = new DateField(myUI.getMessage(Messages.Date));
        dateDF.setWidth(Settings.PERCENTS100);
        dateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        dateDF.setRequired(true);
        dateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        dateDF.setResolution(Resolution.MONTH);
        dateDF.setDateFormat(Settings.yearMonthPattern);
        dateDF.setValue(new Date());
        settingsLay.addComponent(dateDF, 0, 2, 1, 2);

        noteTF = new TextArea(myUI.getMessage(Messages.Note));
        noteTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        noteTF.setWidth(Settings.PERCENTS100);
        noteTF.setRows(3);
        settingsLay.addComponent(noteTF, 0, 3, 1, 3);

        note2TF = new TextArea(myUI.getMessage(Messages.Note) + " 2");
        note2TF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        note2TF.setWidth(Settings.PERCENTS100);
        note2TF.setRows(3);
        settingsLay.addComponent(note2TF, 0, 4, 1, 4);
        settingsLay.setColumnExpandRatio(0, 1);

        buildSearchLayout();
        searchBtn.setContent(invoicesTable);
    }

    private void buildRightLayout() {
        int assetsCounter, debtsCounter, acc_category_id = 0;
        try {
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            List<AccBalanceSettings> assetsList = dbCon.exec_balance_settings(3);
            assetsCounter = assetsList.size();
            List<AccBalanceSettings> debtsList = dbCon.exec_balance_settings(4);
            debtsCounter = debtsList.size();
            dbCon.close();
            for (AccBalanceSettings balanceSettings : assetsList) {
                if (acc_category_id != balanceSettings.getAccCategoryId()) {
                    assetsCounter++;
                }
                acc_category_id = balanceSettings.getAccCategoryId();
            }
            acc_category_id = 0;
            for (AccBalanceSettings balanceSettings : debtsList) {
                if (acc_category_id != balanceSettings.getAccCategoryId()) {
                    debtsCounter++;
                }
                acc_category_id = balanceSettings.getAccCategoryId();
            }

            rightLay = new GridLayout(5, (assetsCounter > debtsCounter ? assetsCounter + 4 : debtsCounter + 4));
            rightLay.setSpacing(true);
            rightLay.setWidth(Settings.PERCENTS100);
            rightLay.setMargin(true);
            rightLay.setColumnExpandRatio(0, 4f);
            rightLay.setColumnExpandRatio(1, 1f);
            rightLay.setColumnExpandRatio(2, 0.1f);
            rightLay.setColumnExpandRatio(3, 4f);
            rightLay.setColumnExpandRatio(4, 1f);

            Label l = createLabel(myUI.getMessage(Messages.ReturnableAssetsUSD), ValoTheme.LABEL_H3);
            l.addStyleName("border");
            rightLay.addComponent(l, 0, 0);
            l = createLabel(Settings.dFormat2.format(0), ValoTheme.LABEL_H3);
            l.addStyleName("border");
            rightLay.addComponent(l, 1, 0);
            rightLay.setComponentAlignment(l, Alignment.MIDDLE_RIGHT);

            l = createLabel(myUI.getMessage(Messages.ShortTermDebtsUSD), ValoTheme.LABEL_H3);
            l.addStyleName("border");
            rightLay.addComponent(l, 3, 0);
            l = createLabel(Settings.dFormat2.format(0), ValoTheme.LABEL_H3);
            l.addStyleName("border");
            rightLay.addComponent(l, 4, 0);
            rightLay.setComponentAlignment(l, Alignment.MIDDLE_RIGHT);
            int assetsRow = buildRows(assetsList, 0);
            int debtsRow = buildRows(debtsList, 3);

            int row = assetsRow > debtsRow ? assetsRow + 1 : debtsRow + 1;
            l = createLabel(myUI.getMessage(Messages.ProfitOfPreviousPeriods), ValoTheme.LABEL_LARGE);
            l.addStyleName("border");
            rightLay.addComponent(l, 3, row);
            l = createLabel(Settings.dFormat2.format(0), ValoTheme.LABEL_LARGE);
            l.addStyleName("border");
            rightLay.addComponent(l, 4, row);
            rightLay.setComponentAlignment(l, Alignment.MIDDLE_RIGHT);
            row++;
            l = createLabel(myUI.getMessage(Messages.ProfitForPeriod), ValoTheme.LABEL_LARGE);
            l.addStyleName("border");
            rightLay.addComponent(l, 3, row);
            l = createLabel(Settings.dFormat2.format(0), ValoTheme.LABEL_LARGE);
            l.addStyleName("border");
            rightLay.addComponent(l, 4, row);
            rightLay.setComponentAlignment(l, Alignment.MIDDLE_RIGHT);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private int buildRows(List<AccBalanceSettings> list, int column) {
        int acc_category_id = 0;
        int row = 1;
        for (AccBalanceSettings balanceSettings : list) {
            if (acc_category_id != balanceSettings.getAccCategoryId()) {
                Label l = createLabel(balanceSettings.getAccCategory(), ValoTheme.LABEL_LARGE);
                l.addStyleName("tableCpt");
                rightLay.addComponent(l, column, row);
                l = createLabel(Settings.dFormat2.format(0), ValoTheme.LABEL_LARGE);
                l.addStyleName("tableCpt");
                l.setId(myUI.getMessage(Messages.Parent));
                rightLay.addComponent(l, column + 1, row);
                rightLay.setComponentAlignment(l, Alignment.MIDDLE_RIGHT);
                row++;
            }
            if (balanceSettings.isWithTextField()) {
                HorizontalLayout hl = new HorizontalLayout();
                hl.setSpacing(true);
                hl.setWidth(Settings.PERCENTS100);
                hl.addComponent(createLabel(balanceSettings.getPrefix(), "lblBalance"));
                TextField tf = createTextField(new StringLengthValidator(
                                myUI.getMessage(Messages.NotificationWrongValue),
                                null, 100, true), false, null,
                        myUI.getMessage(Messages.Note), null, null, null);
                hl.addComponent(tf);
                hl.setExpandRatio(tf, 1);
                if (balanceSettings.getPostfix() != null) {
                    hl.addComponent(createLabel(balanceSettings.getPostfix(), "lblBalance"));
                }
                rightLay.addComponent(hl, column, row);
            } else {
                rightLay.addComponent(createLabel(balanceSettings.getPrefix(), "lblBalance"), column, row);
            }
            balanceSettings.setRow(row);
            balanceSettings.setColumn(column);
            rightLay.addComponent(createTextField(new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue),
                            0.0, null), false, null, balanceSettings, this,
                    new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2)), column + 1, row);
            row++;
            acc_category_id = balanceSettings.getAccCategoryId();
        }
        return row;
    }

    private Label createLabel(String value, String style) {
        Label l = new Label(value);
        l.setSizeUndefined();
        if (style != null) {
            l.setStyleName(style);
        }
        return l;
    }

    public TextField createTextField(Validator validator, boolean isRequired, String itemId, Object data,
                                     Property.ValueChangeListener listener,
                                     ObjectProperty<?> property, Converter<String, ?> converter) {
        TextField tf = new TextField();
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        if (validator != null) {
            tf.addValidator(validator);
        }
        if (isRequired) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
        }
        if (property != null) {
            tf.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
            property.setValue(null);
            tf.setPropertyDataSource(property);
            tf.setNullRepresentation("");
            tf.setNullSettingAllowed(true);
            tf.setConverter(converter);
        } else {
            tf.setNullRepresentation("");
        }
        tf.setId(itemId);
        tf.setData(data);
        if (listener != null) {
            tf.addValueChangeListener(listener);
        }
        return tf;
    }

    private void buildSearchLayout() {

        invoicesTable = new FormattedFilterTable(myUI);
        invoicesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        invoicesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        invoicesTable.setSelectable(true);
        invoicesTable.setFilterBarVisible(true);
        invoicesTable.setNullSelectionAllowed(false);
        invoicesTable.addValueChangeListener(this);
        try {
            DbInvoice dbCon = new DbInvoice();
            dbCon.connect();
            invoicesTable.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId(), this));
            dbCon.close();
            if (invoicesTable.getContainerDataSource().size() != 0) {
                invoicesTable.setValue(((IndexedContainer) invoicesTable.getContainerDataSource()).firstItemId());
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.setPageLength(5);
        invoicesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        invoicesTable.setColumnWidth(myUI.getMessage(Messages.Note), 200);
        invoicesTable.setColumnWidth(myUI.getMessage(Messages.Note) + " 2", 200);
        invoicesTable.setColumnAlignment(myUI.getMessage(Messages.Amount), CustomTable.Align.RIGHT);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && invoicesTable.getValue() != null) {
            if (currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.prmConfirmationControl) ||
                    invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue() != null &&
                            !((CheckBox) invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue()).getValue()) {
                isNew = false;
                fillFields();
                prepareModificationMode();
                invoiceNumberTF.focus();
            } else {
                Notification.show(myUI.getMessage(Messages.YouCanNotModifyOrDeleteConfirmed), Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
        } else if (source == deleteBtn && invoicesTable.getValue() != null) {
            if (currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.prmConfirmationControl) ||
                    invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue() != null &&
                            !((CheckBox) invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue()).getValue()) {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmDeletion),
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete();
                            }
                        });
            } else {
                Notification.show(myUI.getMessage(Messages.YouCanNotModifyOrDeleteConfirmed), Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == saveBtn) {
            try {
                if (Settings.validate(settingsLay) && Settings.validate(rightLay)) {
                    double assetsAmount = Settings.dFormat2.parse((
                            (Label) rightLay.getComponent(1, 0)).getValue()).doubleValue();
                    double debtsAmount = Settings.dFormat2.parse((
                            (Label) rightLay.getComponent(4, 0)).getValue()).doubleValue();
                    if (assetsAmount > 0.0 || debtsAmount > 0.0) {
                        DbInvoice dbCon = new DbInvoice();
                        dbCon.connect();
                        if (isNew) {
                            if (!dbCon.isExists(myUI.getUser().getSchool().getId(), 5, dateDF.getValue(), 0)) {
                                Invoice inv = getInvoice(0);
                                int id = dbCon.exec_insert(inv);
                                if (id != 0) {
                                    insertTransfers(id, 3);
                                    insertTransfers(id, 4);
                                    addDataContainerItem(id, df.format(dateDF.getValue()));
                                    Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                                    prepareNormalMode();
                                } else {
                                    Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved), Notification.Type.WARNING_MESSAGE);
                                }
                            } else {
                                Notification.show(myUI.getMessage(Messages.ExistsInvoiceNotification), Notification.Type.WARNING_MESSAGE);
                            }
                        } else {
                            if (!dbCon.isExists(myUI.getUser().getSchool().getId(), 5, dateDF.getValue(), invID)) {
                                int status = 0;
                                Invoice inv = getInvoice(invID);
                                try {
                                    status = dbCon.exec_update(inv);
                                } catch (Exception e) {
                                    logger.error(e);
                                    logger.catching(e);
                                }
                                if (status != 0) {
                                    insertTransfers(invID, 3);
                                    insertTransfers(invID, 4);
                                    updateDataContainer();
                                    setTransfers();
                                    Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                                    prepareNormalMode();
                                } else {
                                    Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved), Notification.Type.WARNING_MESSAGE);
                                }
                            } else {
                                Notification.show(myUI.getMessage(Messages.ExistsInvoiceNotification), Notification.Type.WARNING_MESSAGE);
                            }
                        }
                        dbCon.close();
                    } else {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue), Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue), Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            if (invoicesTable.getValue() != null) {
                fillFields();
                setTransfers();
            }
            prepareNormalMode();
        } else if (source == confirmBtn) {
            if (currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.prmConfirmationControl)) {
                CheckBox ckb = (CheckBox) invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue();
                ckb.setValue(true);
                confirmBtn.setEnabled(false);
            } else {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmConfirmation),
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                CheckBox ckb = (CheckBox) invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue();
                                ckb.setValue(true);
                                try {
                                    DbInvoice dbInvoice = new DbInvoice();
                                    dbInvoice.connect();
                                    int status = dbInvoice.exec_update(invID, 1);
                                    dbInvoice.close();
                                    if (status == 1) {
                                        Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                                        confirmBtn.setEnabled(false);
                                    }
                                } catch (Exception e) {
                                    logger.error(e);
                                    logger.catching(e);
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == invoicesTable) {
            invoiceNumberTF.removeValueChangeListener(this);
            if (invoicesTable.getItem(invoicesTable.getValue()) != null) {
                invID = (Integer) invoicesTable.getValue();
                setTransfers();
                fillFields();
            }
            invoiceNumberTF.addValueChangeListener(this);
        } else if (property == invoiceNumberTF) {
            invoiceNumberTF.removeValueChangeListener(this);
            Iterator<?> iter = invoicesTable.getItemIds().iterator();
            boolean isFound = false;
            Object next;
            while (iter.hasNext()) {
                next = iter.next();
                if (invoicesTable.getContainerProperty(next, myUI.getMessage(Messages.InvoiceNumber)).getValue().equals(property.getValue())) {
                    invoicesTable.setValue(next);
                    isFound = true;
                    break;
                }
            }
            if (!isFound) {
                invoiceNumberTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                        myUI.getMessage(Messages.InvoiceNumber)).getValue().toString());
            }
            invoiceNumberTF.addValueChangeListener(this);
        } else if (event.getProperty() instanceof CheckBox) {
            try {
                int value = 1;
                if (event.getProperty().getValue() == Boolean.FALSE) {
                    value = 0;
                }
                DbInvoice dbInvoice = new DbInvoice();
                dbInvoice.connect();
                int status = dbInvoice.exec_update((Integer) ((CheckBox) event.getProperty()).getData(), value);
                dbInvoice.close();
                if (status == 1) {
                    Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                    if ((int) ((CheckBox) event.getProperty()).getData() == (int) invoicesTable.getValue()) {
                        confirmBtn.setEnabled(value != 1);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else {
            TextField amountTf = ((TextField) event.getProperty());
            AccBalanceSettings balanceSettings = (AccBalanceSettings) amountTf.getData();
            if (balanceSettings.isWithTextField()) {
                HorizontalLayout hl = (HorizontalLayout) rightLay.getComponent(
                        balanceSettings.getColumn(), balanceSettings.getRow());
                TextField tf = (TextField) hl.getComponent(1);
                if (amountTf.isValid() && amountTf.getConvertedValue() != null && (Double) amountTf.getConvertedValue() != 0.0) {
                    tf.setRequired(true);
                    tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
                    tf.setNullSettingAllowed(false);
                    tf.removeAllValidators();
                    tf.addValidator(new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                            1, 100, false));
                } else {
                    tf.setRequired(false);
                    tf.setRequiredError(null);
                    tf.setNullSettingAllowed(true);
                    tf.removeAllValidators();
                    tf.addValidator(new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                            null, 100, true));
                }
            }
            recalculateTotals(balanceSettings.getColumn() + 1);
        }
    }

    private void prepareModificationMode() {
        if (invoicesTable.getValue() != null && invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue() != null) {
            confirmBtn.setEnabled(false);
        }
        excelBtn.setEnabled(false);
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        searchBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(false);
        dateDF.setEnabled(true);
        noteTF.setEnabled(true);
        note2TF.setEnabled(true);
        rightLay.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        excelBtn.setEnabled(true);
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(true);
        searchBtn.setEnabled(true);
        dateDF.setEnabled(false);
        noteTF.setEnabled(false);
        note2TF.setEnabled(false);
        rightLay.setEnabled(false);

        if (invoicesTable.getValue() != null && invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue() != null) {
            confirmBtn.setEnabled(!((CheckBox) invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    Settings.button).getValue()).getValue());
        }
    }

    private void fillFields() {
        if (invoicesTable.getValue() != null && invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.button).getValue() != null) {
            confirmBtn.setEnabled(!((CheckBox) invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    Settings.button).getValue()).getValue());
        }
        invoiceNumberTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(Messages.InvoiceNumber)).getValue().toString());
        try {
            dateDF.setValue(df.parse(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(Messages.Date)).getValue().toString()));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(Messages.Note)).getValue() != null) {
            noteTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(Messages.Note)).getValue().toString());
        } else {
            noteTF.setValue("");
        }
        if (invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(Messages.Note) + " 2").getValue() != null) {
            note2TF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(Messages.Note) + " 2").getValue().toString());
        } else {
            note2TF.setValue("");
        }
    }

    private void clearFields() {
        invoiceNumberTF.removeValueChangeListener(this);
        invoiceNumberTF.setValue("");
        invoiceNumberTF.addValueChangeListener(this);
        dateDF.setValue(new Date());
        note2TF.setValue("");
        noteTF.setValue("");
        clearAll(rightLay);
    }

    public void clearAll(ComponentContainer layout) {
        for (Component c : layout) {
            if (c instanceof AbstractField) {
                ((AbstractField<?>) c).setValue(null);
                c.setId(null);
                if (((AbstractField<?>) c).getData() != null &&
                        ((AbstractField<?>) c).getData().equals(myUI.getMessage(Messages.Note))) {
                    ((TextField) c).setRequired(false);
                    ((TextField) c).setRequiredError(null);
                    ((TextField) c).setNullSettingAllowed(true);
                    ((TextField) c).removeAllValidators();
                    ((TextField) c).addValidator(new StringLengthValidator(
                            myUI.getMessage(Messages.NotificationWrongValue),
                            null, 100, true));
                }
            } else if (c instanceof AbstractComponentContainer) {
                clearAll((AbstractComponentContainer) c);
            }
        }
        if (excelBtn != null) {
            excelBtn.setEnabled(false);
        }
    }

    private void setTransfers() {
        clearAll(rightLay);
        try {
            DbTransfers dbCon = new DbTransfers();
            dbCon.connect();
            Map<Integer, Transfer> transfersMap = dbCon.execSQL(invID);
            dbCon.close();
            for (int i = 0; i < rightLay.getRows(); i++) {
                int column = 1;
                setTransfers(transfersMap, i, column);
                column = 4;
                setTransfers(transfersMap, i, column);
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        recalculateTotals(1);
        recalculateTotals(4);
        prepareTextExcelDownload();
    }

    private void setTransfers(Map<Integer, Transfer> transfersMap, int i, int column) {
        if (rightLay.getComponent(column, i) instanceof TextField) {
            TextField tf = (TextField) rightLay.getComponent(column, i);
            AccBalanceSettings balanceSettings = (AccBalanceSettings) tf.getData();
            Transfer tr = transfersMap.get(balanceSettings.getId());
            if (tr != null) {
                tf.removeValueChangeListener(this);
                tf.getPropertyDataSource().setValue(tr.getAmount());
                tf.addValueChangeListener(this);
                tf.setId(Integer.toString(tr.getId()));
                if (balanceSettings.isWithTextField()) {
                    Component component = rightLay.getComponent(column - 1, i);
                    if (component instanceof HorizontalLayout) {
                        String value = tr.getNote().replace(balanceSettings.getPrefix(), "");
                        if (balanceSettings.getPostfix() != null) {
                            value = value.replace(balanceSettings.getPostfix(), "");
                        }
                        for (int j = 0; j < ((HorizontalLayout) component).getComponentCount(); j++) {
                            Component c = ((HorizontalLayout) component).getComponent(j);
                            if (c instanceof TextField) {
                                ((TextField) c).setValue(value.trim());
                                ((TextField) c).setRequired(true);
                                ((TextField) c).setRequiredError(myUI.getMessage(Messages.RequiredField));
                                ((TextField) c).setNullSettingAllowed(false);
                                ((TextField) c).removeAllValidators();
                                ((TextField) c).addValidator(new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                                        1, 100, false));
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateDataContainer() {
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Date)).setValue(
                df.format(dateDF.getValue()));
        try {
            invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Amount)).setValue(
                    Settings.dFormat2.parse(((Label) rightLay.getComponent(4, rightLay.getRows() - 1))
                            .getValue()).doubleValue());
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Note)).setValue(
                noteTF.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Note) + " 2").setValue(
                note2TF.getValue());
    }

    private void addDataContainerItem(int id, String date) {
        Item item = ((IndexedContainer) invoicesTable.getContainerDataSource()).addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(date);
        try {
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                    Settings.dFormat2.parse(((Label) rightLay.getComponent(4, rightLay.getRows() - 1))
                            .getValue()).doubleValue());
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(noteTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Note) + " 2").setValue(note2TF.getValue());
        try {
            DbInvoice dbCon = new DbInvoice();
            dbCon.connect();
            item.getItemProperty(myUI.getMessage(Messages.InvoiceNumber)).setValue(dbCon.execSQL_invoice_number(id));
            dbCon.close();
            CheckBox cb = new CheckBox();
            cb.setData(id);
            cb.setStyleName(ValoTheme.CHECKBOX_SMALL);
            if (!currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.prmConfirmationControl)) {
                cb.setEnabled(false);
            } else {
                cb.addValueChangeListener(this);
            }
            item.getItemProperty(Settings.button).setValue(cb);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.setValue(id);
    }

    private Invoice getInvoice(int i) {
        Invoice inv = new Invoice();
        try {
            DbInvoice dbCon = new DbInvoice();
            dbCon.connect();
            inv.setInvoice_number(dbCon.execSQL_max_invoice_number(
                    myUI.getUser().getSchool().getId(), 5) + 1);
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (noteTF.getValue() != null && !noteTF.getValue().isEmpty()) {
            inv.setNote(noteTF.getValue());
        }
        if (note2TF.getValue() != null && !note2TF.getValue().isEmpty()) {
            inv.setNote2(note2TF.getValue());
        }
        inv.setAcc_invoice_type_id(5);
        inv.setCreation_date(dateDF.getValue());
        inv.setSchool_id(myUI.getUser().getSchool().getId());
        inv.setEmployee_id(myUI.getUser().getId());
        inv.setId(i);
        return inv;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete(invoicesTable.getValue().toString(), Settings.dbTransfers, Settings.invoice_id);
            if (st != 0) {
                st = dbDef.exec_delete((Integer) invoicesTable.getValue(), Settings.dbAccInvoice);
                if (st != 0) {
                    invoicesTable.getContainerDataSource().removeItem(invoicesTable.getValue());
                    if (invoicesTable.getContainerDataSource().size() != 0) {
                        invoicesTable.setValue(((IndexedContainer) invoicesTable.getContainerDataSource()).firstItemId());
                    } else {
                        clearFields();
                        invoicesTable.setValue(null);
                    }
                }
            }
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(Messages.CanNotDelete), Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertTransfers(int invoice_id, int invoice_type_id) {
        int column = invoice_type_id == 3 ? 1 : 4;
        try {
            DbTransfers dbCon = new DbTransfers();
            dbCon.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            for (int i = 0; i < rightLay.getRows(); i++) {
                if (rightLay.getComponent(column, i) instanceof TextField) {
                    TextField tf = (TextField) rightLay.getComponent(column, i);
                    Component component = rightLay.getComponent(column - 1, i);
                    if (tf.isValid() && tf.getConvertedValue() != null && (Double) tf.getConvertedValue() != 0.0) {
                        AccBalanceSettings balanceSettings = (AccBalanceSettings) tf.getData();
                        Transfer tr = new Transfer();
                        tr.setAcc_balance_settings_id(balanceSettings.getId());
                        tr.setAcc_category_id(balanceSettings.getAccCategoryId());
                        tr.setAmount((Double) tf.getConvertedValue());
                        tr.setCurrency_id(2);
                        tr.setRate(myUI.getDb_currency_rate());
                        tr.setInvoice_id(invoice_id);
                        if (component instanceof Label) {
                            tr.setNote(((Label) component).getValue());
                        } else if (component instanceof HorizontalLayout) {
                            StringBuilder value = new StringBuilder();
                            for (int j = 0; j < ((HorizontalLayout) component).getComponentCount(); j++) {
                                Component c = ((HorizontalLayout) component).getComponent(j);
                                if (c instanceof Label) {
                                    value.append(" ").append(((Label) c).getValue());
                                } else if (c instanceof TextField) {
                                    value.append(" ").append(((TextField) c).getValue());
                                }
                            }
                            tr.setNote(value.toString().trim());
                        }
                        if (tf.getId() != null) {
                            tr.setId(Integer.parseInt(tf.getId()));
                            dbCon.exec_update(tr);
                        } else {
                            dbCon.exec_insert(tr);
                        }
                    } else if (tf.getId() != null) {
                        dbd.exec_delete(tf.getId(), Settings.dbTransfers);
                    }
                }
            }
            dbCon.close();
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public void recalculateTotals(int column) {
        double totalAmount = 0.0, parentTotal = 0.0;
        for (int i = rightLay.getRows() - 1; i >= 0; i--) {
            if (rightLay.getComponent(column, i) instanceof TextField) {
                TextField tf = (TextField) rightLay.getComponent(column, i);
                if (tf.isValid() && tf.getConvertedValue() != null && (Double) tf.getConvertedValue() != 0.0) {
                    totalAmount += (Double) tf.getConvertedValue();
                    parentTotal += (Double) tf.getConvertedValue();
                }
            } else if (rightLay.getComponent(column, i) instanceof Label && rightLay.getComponent(column, i).getId() != null
                    && rightLay.getComponent(column, i).getId().equals(myUI.getMessage(Messages.Parent))) {
                ((Label) rightLay.getComponent(column, i)).setValue(Settings.dFormat2.format(parentTotal));
                parentTotal = 0.0;
            }
        }
        ((Label) rightLay.getComponent(column, 0)).setValue(Settings.dFormat2.format(totalAmount));
        try {
            double net = Settings.dFormat2.parse(((Label) rightLay.getComponent(1, 0)).getValue()).doubleValue()
                    - Settings.dFormat2.parse(((Label) rightLay.getComponent(4, 0)).getValue()).doubleValue();
            ((Label) rightLay.getComponent(4, rightLay.getRows() - 1)).setValue(Settings.dFormat2.format(net));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public Component getNewObj() {
        return new BalanceAccountsView(myUI);
    }

    // Генерим XLS в памяти и «вешаем» FileDownloader на кнопку
    private void prepareTextExcelDownload() {
        try (HSSFWorkbook wb = buildTextWorkbook()) {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            wb.write(baos);

            // снять старый даунлоадер, если он уже висит
            if (fdText != null && excelBtn.getExtensions().contains(fdText)) {
                excelBtn.removeExtension(fdText);
            }

            com.vaadin.server.StreamResource res = new com.vaadin.server.StreamResource(
                    () -> new java.io.ByteArrayInputStream(baos.toByteArray()),
                    "balance_text_" + System.currentTimeMillis() + ".xls"
            );
            res.setCacheTime(0);
            res.setMIMEType("application/vnd.ms-excel");

            fdText = new com.vaadin.server.FileDownloader(res);
            fdText.extend(excelBtn);
            excelBtn.setEnabled(true);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved), Notification.Type.ERROR_MESSAGE);
            if (excelBtn != null) {
                excelBtn.setEnabled(false);
            }
        }
    }

    private HSSFWorkbook buildTextWorkbook() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh = wb.createSheet(myUI.getMessage(Messages.BalanceAccounts));

        DataFormat dfmt = wb.createDataFormat();

        // базовые текстовые стили
        CellStyle textLeft = baseTextStyle(wb, dfmt, HorizontalAlignment.LEFT, false, IndexedColors.WHITE.getIndex());
        CellStyle textRight = baseTextStyle(wb, dfmt, HorizontalAlignment.RIGHT, false, IndexedColors.WHITE.getIndex());

        // основной тёмный зелёный для шапок блоков и «прибыль»-блока (#003300)
        short MAIN_BG = ensureCustomColor(wb, "#003300",
                HSSFColor.HSSFColorPredefined.DARK_GREEN.getIndex());
        CellStyle titleLeft = baseTextStyle(wb, dfmt, HorizontalAlignment.LEFT, true, MAIN_BG);
        CellStyle titleRight = baseTextStyle(wb, dfmt, HorizontalAlignment.RIGHT, true, MAIN_BG);
        setWhiteBoldFont(wb, titleLeft);
        setWhiteBoldFont(wb, titleRight);

        // ЗАГОЛОВКИ КОЛОНОК: #339966 + белый жирный
        short COLHEAD_BG = ensureCustomColor(wb, "#339966",
                HSSFColor.HSSFColorPredefined.SEA_GREEN.getIndex());
        CellStyle colHeadLeft = baseTextStyle(wb, dfmt, HorizontalAlignment.LEFT, true, COLHEAD_BG);
        CellStyle colHeadRight = baseTextStyle(wb, dfmt, HorizontalAlignment.RIGHT, true, COLHEAD_BG);
        setWhiteBoldFont(wb, colHeadLeft);
        setWhiteBoldFont(wb, colHeadRight);

        // КАТЕГОРИИ (то, что было «голубым»): #CCFFCC
        short CAT_BG = ensureCustomColor(wb, "#CCFFCC",
                HSSFColor.HSSFColorPredefined.LIGHT_GREEN.getIndex());
        CellStyle catLeft = baseTextStyle(wb, dfmt, HorizontalAlignment.LEFT, true, CAT_BG);
        CellStyle catRight = baseTextStyle(wb, dfmt, HorizontalAlignment.RIGHT, true, CAT_BG);
        // тут оставляем чёрный шрифт (читабельно на светло-зелёном)

        int r = 0;

        // верхняя шапка
        r = writeRow(sh, r, textLeft, "Дата", formatDate(dateDF.getValue()));
        r = writeRow(sh, r, textLeft,
                myUI.getMessage(Messages.InvoiceNumber),
                safeStr(invoicesTable != null && invoicesTable.getValue() != null
                        ? invoicesTable.getContainerProperty(invoicesTable.getValue(),
                        myUI.getMessage(Messages.InvoiceNumber)).getValue()
                        : ""));
        r = writeRow(sh, r, textLeft, myUI.getMessage(Messages.Note), safeStr(noteTF.getValue()));
        r = writeRow(sh, r, textLeft, myUI.getMessage(Messages.Note) + " 2", safeStr(note2TF.getValue()));
        r++;

        // плашки блоков
        String assetsHdr = myUI.getMessage(Messages.ReturnableAssetsUSD);
        String debtsHdr = myUI.getMessage(Messages.ShortTermDebtsUSD);
        String assetsSum = getValueText(rightLay.getComponent(1, 0));
        String debtsSum = getValueText(rightLay.getComponent(4, 0));

        Row row = sh.createRow(r++);
        row.setHeightInPoints(18f);
        put(row, 0, assetsHdr, titleLeft);
        put(row, 1, assetsSum, titleRight);
        put(row, 2, "", textLeft);
        put(row, 3, debtsHdr, titleLeft);
        put(row, 4, debtsSum, titleRight);

        // заголовки колонок (теперь зелёные)
        row = sh.createRow(r++);
        put(row, 0, myUI.getMessage(Messages.Category), colHeadLeft);
        put(row, 1, myUI.getMessage(Messages.Amount), colHeadRight);
        put(row, 2, "", textLeft);
        put(row, 3, myUI.getMessage(Messages.Category), colHeadLeft);
        put(row, 4, myUI.getMessage(Messages.Amount), colHeadRight);

        final int freezeAt = r;

        // подготовим «прибыль» — вытащим отдельно
        String profitPrevTxt = myUI.getMessage(Messages.ProfitOfPreviousPeriods);
        String profitPeriodTxt = myUI.getMessage(Messages.ProfitForPeriod);
        String profitPrevAmt = null, profitPeriodAmt = null;

        // данные
        for (int i = 0; i < rightLay.getRows(); i++) {
            String aTitle = getText(rightLay.getComponent(0, i));
            String aAmt = getValueText(rightLay.getComponent(1, i));
            String dTitle = getText(rightLay.getComponent(3, i));
            String dAmt = getValueText(rightLay.getComponent(4, i));
            if (isBlank(aTitle) && isBlank(aAmt) && isBlank(dTitle) && isBlank(dAmt)) continue;

            if (profitPrevTxt.equals(dTitle)) {
                profitPrevAmt = dAmt;
                continue;
            }
            if (profitPeriodTxt.equals(dTitle)) {
                profitPeriodAmt = dAmt;
                continue;
            }

            boolean aIsCat = isParentRow(1, i);
            boolean dIsCat = isParentRow(4, i);

            row = sh.createRow(r++);
            put(row, 0, aTitle, aIsCat ? catLeft : textLeft);
            put(row, 1, aAmt, aIsCat ? catRight : textRight);
            put(row, 2, "", textLeft);
            put(row, 3, dTitle, dIsCat ? catLeft : textLeft);
            put(row, 4, dAmt, dIsCat ? catRight : textRight);
        }

        // отступ
        r++;

        // блок «прибыль» — тем же #003300
        if (profitPrevAmt != null || profitPeriodAmt != null) {
            row = sh.createRow(r++);
            row.setHeightInPoints(18f);
            put(row, 0, "", textLeft);
            put(row, 1, "", textLeft);
            put(row, 2, "", textLeft);
            put(row, 3, profitPrevTxt, titleLeft);
            put(row, 4, safeStr(profitPrevAmt), titleRight);

            row = sh.createRow(r++);
            row.setHeightInPoints(18f);
            put(row, 0, "", textLeft);
            put(row, 1, "", textLeft);
            put(row, 2, "", textLeft);
            put(row, 3, profitPeriodTxt, titleLeft);
            put(row, 4, safeStr(profitPeriodAmt), titleRight);
        }

        for (int c = 0; c <= 4; c++) sh.autoSizeColumn(c);
        sh.createFreezePane(2, freezeAt);

        return wb;
    }

    // Базовый текстовый стиль c заданным фоном, выравниванием и рамками
    private CellStyle baseTextStyle(HSSFWorkbook wb, DataFormat dfmt,
                                    HorizontalAlignment align, boolean bold, short bgIndex) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(dfmt.getFormat("@"));              // всё текстом
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(false);
        if (bgIndex != IndexedColors.WHITE.getIndex()) {
            s.setFillForegroundColor(bgIndex);
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        Font f = wb.createFont();
        f.setBold(bold);
        s.setFont(f);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    // Перекрасить палитру HSSF под наш HEX и вернуть индекс
    private short ensureCustomColor(HSSFWorkbook wb, String hex, short reuseIndex) {
        int[] rgb = hexToRgb(hex);
        HSSFPalette pal = wb.getCustomPalette();
        HSSFColor c = pal.findColor((byte) rgb[0], (byte) rgb[1], (byte) rgb[2]);
        if (c == null) {
            pal.setColorAtIndex(reuseIndex, (byte) rgb[0], (byte) rgb[1], (byte) rgb[2]);
            return reuseIndex;
        }
        return c.getIndex();
    }

    private void setWhiteBoldFont(HSSFWorkbook wb, CellStyle s) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
    }

    private int[] hexToRgb(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        int r = Integer.parseInt(h.substring(0, 2), 16);
        int g = Integer.parseInt(h.substring(2, 4), 16);
        int b = Integer.parseInt(h.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    private CellStyle baseTextStyle(HSSFWorkbook wb, DataFormat dfmt,
                                    HorizontalAlignment align, boolean bold, IndexedColors bg) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(dfmt.getFormat("@"));
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(false);
        if (bg != null && bg != IndexedColors.WHITE) {
            s.setFillForegroundColor(bg.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        Font f = wb.createFont();
        f.setBold(bold);
        s.setFont(f);
        addThinBorders(s);
        return s;
    }

    private void setWhiteFont(CellStyle s, HSSFWorkbook wb) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
    }

    private void addThinBorders(CellStyle s) {
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    private boolean isParentRow(int amountCol, int row) {
        Component comp = rightLay.getComponent(amountCol, row);
        return comp instanceof Label
                && myUI.getMessage(Messages.Parent).equals(comp.getId());
    }

    private int writeRow(HSSFSheet sh, int r, CellStyle style, String... vals) {
        Row row = sh.createRow(r++);
        for (int i = 0; i < vals.length; i++) put(row, i, vals[i], style);
        return r;
    }

    private void put(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellStyle(style);
        c.setCellValue(val == null ? "" : val);
    }

    private String safeStr(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private String formatDate(Date d) {
        try {
            return d == null ? "" : Settings.ymdf.format(d);
        } catch (Exception e) {
            return d.toString();
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String getText(Component comp) {
        if (comp == null) return "";
        if (comp instanceof Label) {
            return ((Label) comp).getValue();
        }
        if (comp instanceof HorizontalLayout) {
            StringBuilder sb = new StringBuilder();
            HorizontalLayout hl = (HorizontalLayout) comp;
            for (int i = 0; i < hl.getComponentCount(); i++) {
                Component c = hl.getComponent(i);
                if (c instanceof Label) {
                    String v = ((Label) c).getValue();
                    if (!isBlank(v)) {
                        if (sb.length() > 0) sb.append(' ');
                        sb.append(v);
                    }
                } else if (c instanceof TextField) {
                    String v = ((TextField) c).getValue();
                    if (!isBlank(v)) {
                        if (sb.length() > 0) sb.append(' ');
                        sb.append(v);
                    }
                }
            }
            return sb.toString().trim();
        }
        return "";
    }

    /**
     * Значение суммы (Label с итого, либо TextField с конвертированным Double). Всегда строкой.
     */
    private String getValueText(Component comp) {
        if (comp == null) return "";
        try {
            if (comp instanceof Label) {
                return ((Label) comp).getValue();
            }
            if (comp instanceof TextField) {
                TextField tf = (TextField) comp;
                Object cv = tf.getConvertedValue();
                if (cv instanceof Number) {
                    return Settings.dFormat2.format(cv); // формат как в UI
                }
                String v = tf.getValue();
                return v == null ? "" : v;
            }
        } catch (Exception ignore) {
        }
        return "";
    }
}
