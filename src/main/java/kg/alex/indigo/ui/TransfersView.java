package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbAccCategory;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbInvoice;
import kg.alex.indigo.dao.DbTransfers;
import kg.alex.indigo.domain.Invoice;
import kg.alex.indigo.domain.Transfer;
import kg.alex.indigo.i18n.Messages;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.ExistsValidator;
import kg.alex.indigo.utils.FormattedFilterTable;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class TransfersView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(TransfersView.class);
    private final MyVaadinUI myUI;
    private final Button addBtn;
    private final FormattedTable transfersTable;
    private final String[] NATURAL_COL_ORDER;
    private final GridLayout rightLay;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final ArrayList<String> delTransferIds = new ArrayList<>();
    private final String caption;
    private final String viewName;
    private final int acc_category_type_id;
    private final int acc_invoice_type_id;
    private final SimpleDateFormat df;
    private Button createBtn;
    private Button modifyBtn;
    private Button deleteBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private Button excelBtn;
    private Button copyBtn;
    private Button confirmBtn;
    private PopupButton searchBtn;
    private TextField invoiceNumberTF;
    private TextArea noteTF, note2TF;
    private DateField dateDF;
    private boolean isNew;
    private String[] NATURAL_COL_ORDER_TRANSFERS;
    private GridLayout settingsLay;
    private FormattedFilterTable invoicesTable;
    private IndexedContainer transfersCont;
    private int r_table_counter = 1000;
    private int invID;

    public TransfersView(MyVaadinUI myUI, String caption, String viewName,
                         int acc_category_type_id, int acc_invoice_type_id) {
        this.myUI = myUI;
        this.caption = caption;
        this.viewName = viewName;
        this.acc_category_type_id = acc_category_type_id;
        this.acc_invoice_type_id = acc_invoice_type_id;
        if (acc_invoice_type_id != 1) {
            df = Settings.ymdf;
        } else {
            df = Settings.dtmf;
        }
        if (viewName.equals(Settings.cnShortTermDebtsView) || viewName.equals(Settings.cnReturnableAssetsView)) {
            NATURAL_COL_ORDER = new String[]{Settings.button, myUI.getMessage(Messages.InvoiceNumber),
                    myUI.getMessage(Messages.Date), myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.Note), myUI.getMessage(Messages.Note) + " 2"};
        } else {
            NATURAL_COL_ORDER = new String[]{Settings.button, myUI.getMessage(Messages.InvoiceNumber),
                    myUI.getMessage(Messages.Date), myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.Note)};
        }

        rightLay = new GridLayout(2, 2);
        rightLay.setSpacing(true);
        rightLay.setSizeFull();
        rightLay.setMargin(true);

        addBtn = new Button(myUI.getMessage(Messages.AddRecord));
        addBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        addBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        addBtn.setIcon(FontAwesome.PLUS_SQUARE);
        addBtn.addClickListener(this);
        rightLay.addComponent(addBtn, 0, 0);

        transfersTable = new FormattedTable(myUI);
        transfersTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        transfersTable.setStyleName(ValoTheme.TABLE_COMPACT);
        transfersTable.setSizeFull();
        transfersTable.setFooterVisible(true);
        rightLay.addComponent(transfersTable, 0, 1, 1, 1);
        rightLay.setRowExpandRatio(1, 1);
        rightLay.setColumnExpandRatio(1, 1);

        buildSettingsLayout();
        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
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

        copyBtn = new Button();
        copyBtn.setEnabled(false);
        copyBtn.setDescription(myUI.getMessage(Messages.Copy));
        copyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        copyBtn.setIcon(FontAwesome.COPY);
        copyBtn.addClickListener(this);
        buttonsLay.addComponent(copyBtn);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(Messages.Print));
        excelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);
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
        if (acc_invoice_type_id != 1) {
            dateDF.setResolution(Resolution.MONTH);
            dateDF.setDateFormat(Settings.yearMonthPattern);
        } else {
            dateDF.setResolution(Resolution.MINUTE);
            dateDF.setDateFormat(Settings.dateTimeMinPattern);
        }
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
        if (viewName.equals(Settings.cnShortTermDebtsView) || viewName.equals(Settings.cnReturnableAssetsView)) {
            settingsLay.addComponent(note2TF, 0, 4, 1, 4);
        }
        settingsLay.setColumnExpandRatio(0, 1);

        buildSearchLayout();
        searchBtn.setContent(invoicesTable);
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
            invoicesTable.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId(), acc_invoice_type_id, viewName, this));
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
        if (viewName.equals(Settings.cnShortTermDebtsView) || viewName.equals(Settings.cnReturnableAssetsView)) {
            invoicesTable.setColumnWidth(myUI.getMessage(Messages.Note) + " 2", 200);
        }
        invoicesTable.setColumnAlignment(myUI.getMessage(Messages.Amount), CustomTable.Align.RIGHT);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && invoicesTable.getValue() != null) {
            if (currentUser.isPermitted(viewName + ":" + Settings.prmConfirmationControl) ||
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
            if (currentUser.isPermitted(viewName + ":" + Settings.prmConfirmationControl) ||
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
                if (Settings.validate(settingsLay) && Settings.validateTable(myUI, transfersTable)) {
                    DbInvoice dbCon = new DbInvoice();
                    dbCon.connect();
                    if (isNew) {
                        Invoice inv = getInvoice(0);
                        if (acc_invoice_type_id == 1 || !dbCon.isExists(myUI.getUser().getSchool().getId(), acc_invoice_type_id, dateDF.getValue(), 0)) {
                            int id = dbCon.exec_insert(inv);
                            if (id != 0) {
                                insertTransfers(id);
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
                        if (acc_invoice_type_id == 1 || !dbCon.isExists(myUI.getUser().getSchool().getId(), acc_invoice_type_id, dateDF.getValue(), invID)) {
                            int status = 0;
                            Invoice inv = getInvoice(invID);
                            try {
                                status = dbCon.exec_update(inv);
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                            if (status != 0) {
                                insertTransfers(invID);
                                updateDataContainer();
                                setTransfersTable();
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
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == copyBtn && invoicesTable.getValue() != null) {
            try {
                if (Settings.validate(settingsLay) && Settings.validateTable(myUI, transfersTable)) {
                    ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                            myUI.getMessage(Messages.ConfirmCopy),
                            myUI.getMessage(Messages.Yes),
                            myUI.getMessage(Messages.No),
                            (ConfirmDialog.Listener) dialog -> {
                                if (dialog.isConfirmed()) {
                                    try {
                                        double rate = myUI.getDb_currency_rate();
                                        if (rate == 0.0) {
                                            Notification.show(myUI.getMessage(Messages.CantGetFromNBKR), Notification.Type.WARNING_MESSAGE);
                                        } else {
                                            Calendar current = Calendar.getInstance();
                                            current.setTime(dateDF.getValue());
                                            current.add(Calendar.MONTH, 1);

                                            DbInvoice dbCon = new DbInvoice();
                                            dbCon.connect();
                                            if (acc_invoice_type_id == 1 || !dbCon.isExists(myUI.getUser().getSchool().getId(), acc_invoice_type_id, current.getTime(), 0)) {
                                                Invoice inv = getInvoice(0);
                                                inv.setCreation_date(current.getTime());
                                                int id = dbCon.exec_insert(inv);
                                                if (id != 0) {
                                                    DbTransfers dbAcr = new DbTransfers();
                                                    dbAcr.connect();
                                                    if (transfersTable.getContainerDataSource().size() > 0) {
                                                        for (Object next : transfersTable.getItemIds()) {
                                                            Transfer acr = new Transfer();
                                                            acr.setInvoice_id(id);
                                                            acr.setRate(rate);
                                                            acr.setAmount((Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                                                                    myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue());
                                                            acr.setNote(((TextField) transfersTable.getItem(next).getItemProperty(
                                                                    myUI.getMessage(Messages.Note)).getValue()).getValue());
                                                            acr.setAcc_category_id((Integer) ((ComboBox) transfersTable.getItem(next).getItemProperty(
                                                                    myUI.getMessage(Messages.Category)).getValue()).getValue());
                                                            acr.setCurrency_id((Integer) ((ComboBox) transfersTable.getItem(next).getItemProperty(
                                                                    myUI.getMessage(Messages.Currency)).getValue()).getValue());
                                                            dbAcr.exec_insert(acr);
                                                        }
                                                    }
                                                    dbAcr.close();
                                                    addDataContainerItem(id, df.format(inv.getCreation_date()));
                                                    Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                                                } else {
                                                    Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved), Notification.Type.WARNING_MESSAGE);
                                                }
                                                prepareNormalMode();
                                            } else {
                                                Notification.show(myUI.getMessage(Messages.ExistsInvoiceNotification), Notification.Type.WARNING_MESSAGE);
                                            }

                                            dbCon.close();
                                        }
                                    } catch (Exception e) {
                                        logger.error(e);
                                        logger.catching(e);
                                    }
                                }
                            });
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
                setTransfersTable();
            }
            delTransferIds.clear();
            prepareNormalMode();
        } else if (source == addBtn) {
            addTransfersItem();
        } else if (source == confirmBtn) {
            if (currentUser.isPermitted(viewName + ":" + Settings.prmConfirmationControl)) {
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
        } else if (source == excelBtn) {
            if (transfersTable.getContainerDataSource().size() != 0) {
                EnhancedFormatExcelExport excelReport;
                if (caption.length() > 29) {
                    excelReport = new EnhancedFormatExcelExport(transfersTable, caption.substring(0, 29));
                } else {
                    excelReport = new EnhancedFormatExcelExport(transfersTable, caption);
                }
                excelReport.setReportTitle(caption + " (№" + invoiceNumberTF.getValue() + " - " + df.format(dateDF.getValue()) + ")");
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                excelReport.getTotalsRow().getCell(4).setCellValue(transfersTable.getColumnFooter(myUI.getMessage(Messages.Rate)));
                excelReport.getTotalsRow().getCell(5).setCellFormula(null);
                excelReport.getTotalsRow().getCell(5).setCellValue(transfersTable.getColumnFooter(myUI.getMessage(Messages.Amount)));

                for (int i = 0; i < transfersTable.size(); i++) {
                    Row row;
                    if (caption.length() > 29) {
                        row = excelReport.getWorkbook().getSheet(caption.substring(0, 29)).getRow(i + 2);
                    } else {
                        row = excelReport.getWorkbook().getSheet(caption).getRow(i + 2);
                    }
                    for (int k = 0; k < NATURAL_COL_ORDER_TRANSFERS.length; k++) {
                        String propName = NATURAL_COL_ORDER_TRANSFERS[k];
                        Cell cell = row.getCell(k);

                        Object component = transfersTable.getContainerProperty(((IndexedContainer) transfersTable.getContainerDataSource()).getIdByIndex(i), propName).getValue();
                        if (component instanceof ComboBox) {
                            ComboBox cb = (ComboBox) component;
                            cell.setCellValue(cb.getItemCaption(cb.getValue()));
                        } else if (component instanceof TextField) {
                            TextField tf = (TextField) component;
                            if (tf.getPropertyDataSource() != null &&
                                    tf.getPropertyDataSource().getValue() instanceof Number) {
                                cell.setCellValue((Double) tf.getPropertyDataSource().getValue());
                            } else {
                                cell.setCellValue(tf.getValue());
                            }
                        } else {
                            cell.setCellValue((i + 1) + "");
                        }
                    }
                }
                excelReport.sendConverted();
            }
        } else if (source.getId()
                != null && source.getId().equals(Settings.dbTransfers)) {
            delTransferIds.add(source.getData().toString());
            transfersTable.removeItem(event.getButton().getData().toString());
            repaintTransfersFooter();
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == invoicesTable) {
            invoiceNumberTF.removeValueChangeListener(this);
            if (invoicesTable.getItem(invoicesTable.getValue()) != null) {
                invID = (Integer) invoicesTable.getValue();
                setTransfersTable();
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
        } else if (event.getProperty() instanceof ComboBox && ((ComboBox) event.getProperty()).getId() != null) {
            ComboBox catCb = (ComboBox) event.getProperty();
            ((ComboBox) transfersTable.getContainerProperty(catCb.getId(), myUI.getMessage(Messages.Currency)).getValue()).setValue(
                    catCb.getContainerProperty(catCb.getValue(), Settings.acc_currency_id).getValue());
            repaintTransfersFooter();
        } else if (event.getProperty().getType() != null) {
            repaintTransfersFooter();
        }
    }

    private void prepareModificationMode() {
        if (invoicesTable.getValue() != null && invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.button).getValue() != null) {
            confirmBtn.setEnabled(false);
        }
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        copyBtn.setEnabled(false);
        excelBtn.setEnabled(false);
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
        if (currentUser.isPermitted(viewName + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(viewName + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(viewName + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(viewName + ":" + Settings.actCopy)) {
            copyBtn.setEnabled(true);
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
        transfersTable.removeAllItems();
    }

    private void updateDataContainer() {
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Date)).setValue(
                df.format(dateDF.getValue()));
        try {
            invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Amount)).setValue(
                    Settings.dFormat2.parse(transfersTable.getColumnFooter(myUI.getMessage(Messages.Amount))).doubleValue());
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
                    Settings.dFormat2.parse(transfersTable.getColumnFooter(myUI.getMessage(Messages.Amount))).doubleValue());
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
            if (!currentUser.isPermitted(viewName + ":" + Settings.prmConfirmationControl)) {
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
            inv.setInvoice_number(dbCon.execSQL_max_invoice_number(myUI.getUser().getSchool().getId(), acc_invoice_type_id) + 1);
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (noteTF.getValue() != null && !noteTF.getValue().equals("")) {
            inv.setNote(noteTF.getValue());
        }
        if (note2TF.getValue() != null && !note2TF.getValue().equals("")) {
            inv.setNote2(note2TF.getValue());
        }
        inv.setAcc_invoice_type_id(acc_invoice_type_id);
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
                transfersTable.removeAllItems();
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

    public ComboBox createCombobox(int value, String description, String db_table, boolean isRequired, boolean isExistsValidator) {
        ComboBox cb = new ComboBox();
        if (isExistsValidator) {
            cb.addValidator(new ExistsValidator(myUI, transfersCont, cb, description));
        }
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
        }
        try {
            if (db_table != null) {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, db_table, true));
                dbp.close();
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(false);
        cb.setValue(value);
        return cb;
    }

    public Button createButton(String description, String itemId, String tableName) {
        Button btn = new Button();
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(FontAwesome.MINUS_SQUARE);
        btn.setData(itemId);
        btn.setId(tableName);
        btn.addClickListener(this);

        return btn;
    }

    public TextField createTextFieldWithProperty(Object value, String description, Validator validator,
                                                 Property p, Converter conv, boolean isEnabled) {
        TextField tf = new TextField(p);
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
        tf.setNullRepresentation("");
        tf.setConverter(conv);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(validator);
        tf.getPropertyDataSource().setValue(value);
        tf.setEnabled(isEnabled);
        return tf;
    }

    public TextField createTextField(String value, String description, Validator validator, boolean isRequired) {
        TextField tf = new TextField();
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(validator);
        if (isRequired) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
        }
        if (value != null) {
            tf.setValue(value);
        }
        return tf;
    }

    public IndexedContainer prepareTransfersContainer() {
        if (transfersCont == null) {
            transfersCont = new IndexedContainer();
            transfersCont.addContainerProperty(Settings.button, Button.class, null);
            transfersCont.addContainerProperty(myUI.getMessage(Messages.Category), ComboBox.class, null);
            transfersCont.addContainerProperty(myUI.getMessage(Messages.Note), TextField.class, null);
            transfersCont.addContainerProperty(Settings.acc_category_id, Integer.class, 0);
            transfersCont.addContainerProperty(myUI.getMessage(Messages.Currency), ComboBox.class, null);
            transfersCont.addContainerProperty(Settings.acc_currency_id, Integer.class, 0);
            transfersCont.addContainerProperty(myUI.getMessage(Messages.Rate), TextField.class, 0.0);
            transfersCont.addContainerProperty(myUI.getMessage(Messages.Amount), TextField.class, 0.0);
            transfersCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            transfersCont.removeAllItems();
        }
        return transfersCont;
    }

    private void addTransfersItem() {
        NATURAL_COL_ORDER_TRANSFERS = new String[]{Settings.button,
                myUI.getMessage(Messages.Category),
                myUI.getMessage(Messages.Note),
                myUI.getMessage(Messages.Currency),
                myUI.getMessage(Messages.Rate),
                myUI.getMessage(Messages.Amount)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (transfersTable.getContainerDataSource().size() == 0) {
            transfersTable.setContainerDataSource(prepareTransfersContainer());
        }
        Item item;
        item = ((IndexedContainer) transfersTable.getContainerDataSource()).addItemAt(
                transfersTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbTransfers));
        ComboBox cb = createCombobox(0, myUI.getMessage(Messages.Category), null,
                true, acc_invoice_type_id == 1);
        try {
            DbAccCategory dbCon = new DbAccCategory();
            dbCon.connect();
            cb.setContainerDataSource(dbCon.exec_for_select(myUI, getAcc_category_type_id(), myUI.getUser().getSchool().getId(),
                    acc_invoice_type_id != 1));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setItemCaptionPropertyId(myUI.getMessage(Messages.FullName));
        cb.setId(id);
        cb.addValueChangeListener(this);
        item.getItemProperty(myUI.getMessage(Messages.Category)).setValue(cb);
        cb = createCombobox(2, myUI.getMessage(Messages.Currency), Settings.dbAcc_currency, true, false);
        cb.addValueChangeListener(this);
        item.getItemProperty(myUI.getMessage(Messages.Currency)).setValue(cb);
        TextField tf = createTextFieldWithProperty(null, myUI.getMessage(Messages.Amount),
                new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), null, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), true);
        tf.addValueChangeListener(this);
        item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(tf);
        tf = createTextFieldWithProperty(myUI.getDb_currency_rate(), myUI.getMessage(Messages.Rate),
                new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(4),
                currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate));
        tf.addValueChangeListener(this);
        item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(tf);
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(createTextField(
                noteTF.getValue(), id, new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                        null, 250, acc_category_type_id == 1), acc_category_type_id != 1));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Insert));
        transfersTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_TRANSFERS);
        transfersTable.setColumnExpandRatio(myUI.getMessage(Messages.Category), 1);
        transfersTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
        transfersTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);
    }

    private void setTransfersTable() {
        try {
            NATURAL_COL_ORDER_TRANSFERS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Category),
                    myUI.getMessage(Messages.Note),
                    myUI.getMessage(Messages.Currency),
                    myUI.getMessage(Messages.Rate),
                    myUI.getMessage(Messages.Amount)};
            DbTransfers dbCon = new DbTransfers();
            dbCon.connect();
            transfersTable.setContainerDataSource(dbCon.execSQL(myUI, invID, myUI.getUser().getSchool().getId(), acc_invoice_type_id, this));
            dbCon.close();
            transfersTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_TRANSFERS);
            transfersTable.setColumnExpandRatio(myUI.getMessage(Messages.Category), 1);
            transfersTable.setColumnExpandRatio(myUI.getMessage(Messages.Note), 1);
            transfersTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
            transfersTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void repaintTransfersFooter() {
        double totalUsd = 0.0, totalKgs = 0.0;
        if (transfersTable.getContainerDataSource().size() > 0) {
            for (Object next : transfersTable.getItemIds()) {
                if (((TextField) transfersTable.getItem(next).getItemProperty(
                        myUI.getMessage(Messages.Amount)).getValue()).isValid()
                        && ((TextField) transfersTable.getItem(next).getItemProperty(
                        myUI.getMessage(Messages.Rate)).getValue()).isValid()
                        && ((ComboBox) transfersTable.getItem(next).getItemProperty(
                        myUI.getMessage(Messages.Currency)).getValue()).isValid()) {
                    if ((Integer) ((ComboBox) transfersTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Currency)).getValue()).getValue() == 2) {
                        totalUsd += (Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue();
                        totalKgs += (Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue() *
                                (Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(Messages.Rate)).getValue()).getPropertyDataSource().getValue();
                    } else {
                        totalUsd += (Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue()
                                / (Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                                myUI.getMessage(Messages.Rate)).getValue()).getPropertyDataSource().getValue();
                        totalKgs += (Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue();
                    }
                }
            }
        }
        transfersTable.setColumnFooter(myUI.getMessage(Messages.Amount),
                Settings.dFormat2.format(totalUsd) + " " + Settings.USD);
        transfersTable.setColumnFooter(myUI.getMessage(Messages.Rate),
                Settings.dFormat2.format(totalKgs) + " " + Settings.KGS);
    }

    private void insertTransfers(int invoice_id) {
        try {
            DbTransfers dbCon = new DbTransfers();
            dbCon.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delTransferIds.size() > 0) {
                for (int i = 0; i < delTransferIds.size(); i++) {
                    dbd.exec_delete(delTransferIds.get(i), Settings.dbTransfers);
                }
            }
            if (transfersTable.getContainerDataSource().size() > 0) {
                for (Object next : transfersTable.getItemIds()) {
                    Transfer acr = new Transfer();
                    acr.setInvoice_id(invoice_id);
                    acr.setRate((Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Rate)).getValue()).getPropertyDataSource().getValue());
                    acr.setNote(((TextField) transfersTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Note)).getValue()).getValue());
                    acr.setAmount((Double) ((TextField) transfersTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue());
                    acr.setAcc_category_id((Integer) ((ComboBox) transfersTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Category)).getValue()).getValue());
                    acr.setCurrency_id((Integer) ((ComboBox) transfersTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Currency)).getValue()).getValue());
                    if (transfersTable.getContainerProperty(next, Settings.crud_status).getValue().toString().equals(myUI.getMessage(Messages.Update))) {
                        acr.setId(Integer.parseInt(next.toString()));
                        dbCon.exec_update(acr);
                    } else if (transfersTable.getContainerProperty(next, Settings.crud_status).getValue().toString().equals(myUI.getMessage(Messages.Insert))) {
                        dbCon.exec_insert(acr);
                    }
                }
            }
            delTransferIds.clear();
            dbCon.close();
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    public void setTransfersFooter(double amountUSD, double amountKGS) {
        transfersTable.setColumnFooter(myUI.getMessage(Messages.Amount),
                Settings.dFormat2.format(amountUSD) + " " + Settings.USD);
        transfersTable.setColumnFooter(myUI.getMessage(Messages.Rate),
                Settings.dFormat2.format(amountKGS) + " " + Settings.KGS);
    }

    public Component getNewObj() {
        return new TransfersView(myUI, caption, viewName, getAcc_category_type_id(), acc_invoice_type_id);
    }

    /**
     * @return the acc_category_id
     */
    public int getAcc_category_type_id() {
        return acc_category_type_id;
    }
}
