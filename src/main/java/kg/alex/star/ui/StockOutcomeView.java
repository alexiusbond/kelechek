package kg.alex.star.ui;

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
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.dao.*;
import kg.alex.star.domain.StockInvoice;
import kg.alex.star.domain.StockMovement;
import kg.alex.star.domain.StudentInfoPdf;
import kg.alex.star.i18n.Messages;
import kg.alex.star.pdf.StockMovementsPdf;
import kg.alex.star.utils.FormattedFilterTable;
import kg.alex.star.utils.FormattedTable;
import kg.alex.star.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class StockOutcomeView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(StockIncomeView.class);
    private final MyVaadinUI myUI;
    private final Button addBtn;
    private final FormattedTable movementsTable;
    private final String[] NATURAL_COL_ORDER;
    private final GridLayout rightLay;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button createBtn;
    private Button modifyBtn;
    private Button deleteBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private Button printBtn;
    private PopupButton searchBtn;
    private ComboBox stockSelect, fromEmployeeSelect, toEmployeeSelect, productCategorySelect;
    private TextField invoiceNumberTF;
    private TextArea noteTF;
    private DateField dateDF;
    private boolean isNew;
    private String[] NATURAL_COL_ORDER_MOVEMENTS;
    private GridLayout settingsLay;
    private FormattedFilterTable invoicesTable;
    private IndexedContainer movementsCont;
    private int r_table_counter = 1000;
    private int invID;
    private IndexedContainer originalCont;

    public StockOutcomeView(MyVaadinUI myUI) {
        this.myUI = myUI;

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.InvoiceNumber),
                myUI.getMessage(Messages.Stock),
                myUI.getMessage(Messages.Date), myUI.getMessage(Messages.Amount),
                myUI.getMessage(Messages.FromEmployee), myUI.getMessage(Messages.ToEmployee),
                myUI.getMessage(Messages.Note)};

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

        movementsTable = new FormattedTable(myUI);
        movementsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        movementsTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        movementsTable.setSizeFull();
        movementsTable.setFooterVisible(true);
        rightLay.addComponent(movementsTable, 0, 1, 1, 1);
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

        settingsLay = new GridLayout(2, 8);
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

        printBtn = new Button();
        printBtn.setDescription(myUI.getMessage(Messages.Print));
        printBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        printBtn.setIcon(FontAwesome.FILE_PDF_O);
        printBtn.addClickListener(this);
        buttonsLay.addComponent(printBtn);
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
        dateDF.setResolution(Resolution.MINUTE);
        dateDF.setWidth(Settings.PERCENTS100);
        dateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        dateDF.setRequired(true);
        dateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        dateDF.setDateFormat(Settings.dateTimeMinPattern);
        dateDF.setValue(new Date());
        dateDF.addValueChangeListener(this);
        settingsLay.addComponent(dateDF, 0, 2, 1, 2);

        stockSelect = new ComboBox(myUI.getMessage(Messages.Stock));
        stockSelect.setNullSelectionAllowed(false);
        stockSelect.setRequired(true);
        stockSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        stockSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        stockSelect.setWidth(Settings.PERCENTS100);
        stockSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        stockSelect.setFilteringMode(FilteringMode.CONTAINS);
        stockSelect.addValueChangeListener(this);
        settingsLay.addComponent(stockSelect, 0, 3, 1, 3);

        productCategorySelect = new ComboBox(myUI.getMessage(Messages.ProductCategory));
        productCategorySelect.setNullSelectionAllowed(false);
        productCategorySelect.setRequired(true);
        productCategorySelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        productCategorySelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        productCategorySelect.setWidth(Settings.PERCENTS100);
        productCategorySelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        productCategorySelect.setFilteringMode(FilteringMode.CONTAINS);
        productCategorySelect.addValueChangeListener(this);
        settingsLay.addComponent(productCategorySelect, 0, 4, 1, 4);

        fromEmployeeSelect = new ComboBox(myUI.getMessage(Messages.FromEmployee));
        fromEmployeeSelect.setNullSelectionAllowed(false);
        fromEmployeeSelect.setRequired(true);
        fromEmployeeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        fromEmployeeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        fromEmployeeSelect.setWidth(Settings.PERCENTS100);
        fromEmployeeSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        fromEmployeeSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(fromEmployeeSelect, 0, 5, 1, 5);

        toEmployeeSelect = new ComboBox(myUI.getMessage(Messages.ToEmployee));
        toEmployeeSelect.setNullSelectionAllowed(false);
        toEmployeeSelect.setRequired(true);
        toEmployeeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        toEmployeeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        toEmployeeSelect.setWidth(Settings.PERCENTS100);
        toEmployeeSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        toEmployeeSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(toEmployeeSelect, 0, 6, 1, 6);

        try {
            DbEmployee dbCon = new DbEmployee();
            dbCon.connect();
            fromEmployeeSelect.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId(), 100));
            if (fromEmployeeSelect.getContainerDataSource() != null) {
                fromEmployeeSelect.setValue(((IndexedContainer) fromEmployeeSelect.getContainerDataSource()).firstItemId());
            }
            toEmployeeSelect.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId(), 17));
            if (toEmployeeSelect.getContainerDataSource() != null) {
                toEmployeeSelect.setValue(((IndexedContainer) toEmployeeSelect.getContainerDataSource()).firstItemId());
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        noteTF = new TextArea(myUI.getMessage(Messages.Note));
        noteTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        noteTF.setWidth(Settings.PERCENTS100);
        noteTF.setRows(3);
        settingsLay.addComponent(noteTF, 0, 7, 1, 7);
        settingsLay.setColumnExpandRatio(0, 1);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            stockSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbStock, myUI.getUser().getSchool().getId(), true));
            dbDef.close();
            DbProductCategories dbpc = new DbProductCategories();
            dbpc.connect();
            productCategorySelect.setContainerDataSource(dbpc.execSQL_for_select(myUI));
            dbpc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
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
            DbStockInvoice dbCon = new DbStockInvoice();
            dbCon.connect();
            invoicesTable.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId(), 2));
            dbCon.close();
            if (invoicesTable.getContainerDataSource().size() != 0) {
                invoicesTable.setValue(((IndexedContainer) invoicesTable.getContainerDataSource()).firstItemId());
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        invoicesTable.setPageLength(5);
        invoicesTable.setColumnAlignment(myUI.getMessage(Messages.Amount), CustomTable.Align.RIGHT);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == modifyBtn && invoicesTable.getValue() != null) {
            isNew = false;
            fillFields();
            prepareModificationMode();
            invoiceNumberTF.focus();
            stockSelect.setEnabled(false);
            productCategorySelect.setEnabled(false);
            dateDF.setEnabled(false);
        } else if (source == printBtn) {
            StockInvoice stInv = getInvoice(invID);
            stInv.setStock(stockSelect.getItemCaption(stockSelect.getValue()));
            stInv.setAcc_category(productCategorySelect.getItemCaption(productCategorySelect.getValue()));
            stInv.setFrom_employee(fromEmployeeSelect.getItemCaption(fromEmployeeSelect.getValue()));
            stInv.setTo_employee(toEmployeeSelect.getItemCaption(toEmployeeSelect.getValue()));
            StudentInfoPdf studentInfo = new StudentInfoPdf();
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                studentInfo.setSchool(dbsc.execSchool(myUI.getUser().getSchool().getId()));
                dbsc.close();
                DbEmployee dbEmployee = new DbEmployee();
                dbEmployee.connect();
                studentInfo.setDirector(dbEmployee.exec_by_position_id(1, myUI.getUser().getSchool().getId()));
                studentInfo.setAccountant(dbEmployee.exec_by_position_id(2, myUI.getUser().getSchool().getId()));
                dbEmployee.close();
                if (studentInfo.getAccountant() != null) {
                    if (studentInfo.getSchool().getAddress() != null) {
                        new StockMovementsPdf(myUI, myUI.getMessage(Messages.StockOutcome), stInv,
                                movementsCont, studentInfo,
                                movementsTable.getColumnFooter(myUI.getMessage(Messages.Amount)));
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == createBtn) {
            isNew = true;
            clearFields();
            prepareModificationMode();
        } else if (source == deleteBtn && invoicesTable.getValue() != null) {
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
                if (Settings.validate(settingsLay) && Settings.validateTable(myUI, movementsTable)) {
                    DbStockInvoice dbCon = new DbStockInvoice();
                    dbCon.connect();
                    if (isNew) {
                        StockInvoice inv = getInvoice(0);
                        int id = dbCon.exec_insert(inv);
                        if (id != 0) {
                            insertMovements(id);
                            addDataContainerItem(id);
                            invoicesTable.setValue(id);
                            Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved), Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        int status = 0;
                        StockInvoice inv = getInvoice(invID);
                        try {
                            status = dbCon.exec_update(inv);
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (status != 0) {
                            insertMovements(invID);
                            updateDataContainer();
                            setMovementsTable();
                            Notification.show(myUI.getMessage(Messages.ValueSaved),
                                    Notification.Type.HUMANIZED_MESSAGE);
                        } else {
                            Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                    Notification.Type.WARNING_MESSAGE);
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
            if (invoicesTable.getValue() != null) {
                fillFields();
                setMovementsTable();
            }
            prepareNormalMode();
        } else if (source == addBtn) {
            addMovementsItem();
            stockSelect.setEnabled(false);
            productCategorySelect.setEnabled(false);
            dateDF.setEnabled(false);
        } else if (source.getId() != null && source.getId().equals(Settings.dbStockMovement)) {
            if (((ComboBox) movementsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.Product)).getValue()).getValue() != null
                    && ((ComboBox) movementsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                double quantity = (Double) ((TextField) movementsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue();
                int acc_category_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(Messages.Product)).getValue()).getValue();
                int measurement_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(Messages.Measurement)).getValue()).getValue();
                recalculateRemaindersAfterDelete(source.getData(), acc_category_id, measurement_id, quantity);
            }
            movementsTable.removeItem(event.getButton().getData().toString());
            if (movementsTable.size() == 0) {
                stockSelect.setEnabled(true);
                productCategorySelect.setEnabled(true);
                dateDF.setEnabled(true);
            }
            repaintMovementsFooter();
            if (!disableFields(movementsTable)) {
                enableFields(movementsTable);
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
                fillFields();
                setMovementsTable();
            }
            invoiceNumberTF.addValueChangeListener(this);
        } else if (property == stockSelect || property == dateDF || property == productCategorySelect) {
            if (stockSelect.getValue() != null && dateDF.getValue() != null && productCategorySelect.getValue() != null
                    && !rightLay.isEnabled() && stockSelect.isEnabled()) {
                rightLay.setEnabled(true);
            } else if (dateDF.getValue() == null && rightLay.isEnabled()) {
                rightLay.setEnabled(false);
            } else if (productCategorySelect.getValue() == null && rightLay.isEnabled()) {
                rightLay.setEnabled(false);
            }
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
        } else {
            Object changedItemId = ((AbstractField<?>) property).getId();

            if (((ComboBox) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Product)).getValue()).getValue() != null
                    && ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                int acc_category_id = (Integer) movementsTable.getContainerProperty(changedItemId, Settings.acc_category_id).getValue();
                int measurement_id = (Integer) movementsTable.getContainerProperty(changedItemId, Settings.measurement_id).getValue();
                if ((int) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(Messages.Product)).getValue()).getValue()
                        == (int) movementsTable.getContainerProperty(changedItemId, Settings.acc_category_id).getValue()
                        && (int) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(Messages.Measurement)).getValue()).getValue()
                        == (int) movementsTable.getContainerProperty(changedItemId, Settings.measurement_id).getValue()) {
                    double quantity = (Double) movementsTable.getContainerProperty(changedItemId, Settings.quantity_id).getValue();
                    recalculateRemaindersAfterDelete(changedItemId, acc_category_id, measurement_id, quantity);
                } else if ((Integer) movementsTable.getContainerProperty(changedItemId, Settings.acc_category_id).getValue() != 0) {
                    double quantity = (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                            myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue();
                    recalculateRemaindersAfterDelete(changedItemId, acc_category_id, measurement_id, quantity);
                }
                recalculateRemaindersAfterInsert(changedItemId);
                updateOldValues(changedItemId);
                try {
                    int cat_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                            myUI.getMessage(Messages.Product)).getValue()).getValue();
                    int msr_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                            myUI.getMessage(Messages.Measurement)).getValue()).getValue();
                    int inv_id = 0;
                    if (!isNew) {
                        inv_id = invID;
                    }
                    DbStockMovements dbCon = new DbStockMovements();
                    dbCon.connect();
                    dbCon.getConnection().setAutoCommit(false);
                    ArrayList<StockMovement> delMovementIds = dbCon.execSQL(inv_id, cat_id, msr_id);
                    if (delMovementIds.size() > 0) {
                        for (StockMovement delMovementId : delMovementIds) {
                            dbCon.exec_delete(delMovementId);
                        }
                    }
                    if (movementsTable.getContainerDataSource().size() > 0) {
                        Iterator<?> iter = movementsTable.getItemIds().iterator();
                        ArrayList<StockMovement> list = dbCon.execSQL_remains(cat_id, msr_id, (Integer) stockSelect.getValue());
                        int i = 0;
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            if (((ComboBox) movementsTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Product)).getValue()).getValue() != null
                                    && ((ComboBox) movementsTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Measurement)).getValue()).getValue() != null
                                    && ((TextField) movementsTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                                if ((int) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                                        myUI.getMessage(Messages.Product)).getValue()).getValue()
                                        == (int) ((ComboBox) movementsTable.getContainerProperty(next,
                                        myUI.getMessage(Messages.Product)).getValue()).getValue()
                                        && (int) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                                        myUI.getMessage(Messages.Measurement)).getValue()).getValue()
                                        == (int) ((ComboBox) movementsTable.getContainerProperty(next,
                                        myUI.getMessage(Messages.Measurement)).getValue()).getValue()) {
                                    double quantity = (Double) ((TextField) movementsTable.getItem(next).getItemProperty(
                                            myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue();
                                    try {
                                        double avg_price = 0.0, avg_rate = 0.0, q = 0.0, amount = 0.0;
                                        while (i < list.size() && quantity > 0.0) {
                                            if (quantity > list.get(i).getRemain()) {
                                                q += list.get(i).getRemain();
                                                avg_rate += list.get(i).getRate() * list.get(i).getRemain();
                                                avg_price += list.get(i).getPrice() * list.get(i).getRemain();
                                                amount += list.get(i).getPrice() * list.get(i).getRemain();
                                                quantity -= list.get(i).getRemain();
                                            } else {
                                                q += quantity;
                                                avg_rate += list.get(i).getRate() * quantity;
                                                avg_price += list.get(i).getPrice() * quantity;
                                                list.get(i).setRemain(list.get(i).getRemain() - quantity);
                                                amount += list.get(i).getPrice() * quantity;
                                                quantity = 0.0;
                                                i--;
                                            }
                                            i++;
                                        }
                                        if (q != 0) {
                                            movementsTable.getItem(next).getItemProperty(myUI.getMessage(Messages.Rate)).setValue(avg_rate / q);
                                            movementsTable.getItem(next).getItemProperty(myUI.getMessage(Messages.Price)).setValue(avg_price / q);
                                            movementsTable.getItem(next).getItemProperty(myUI.getMessage(Messages.Amount)).setValue(amount);
                                        }
                                    } catch (Exception e) {
                                        logger.error(e);
                                        logger.catching(e);
                                    }
                                }
                            }
                        }
                    }

                    dbCon.getConnection().rollback();
                    dbCon.getConnection().setAutoCommit(true);
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            if (((AbstractField<?>) property).getData() != null && ((AbstractField<?>) property).getData().equals(myUI.getMessage(Messages.Quantity))) {
                TextField quantityTF = (TextField) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(Messages.Quantity)).getValue();
                if (movementsTable.getContainerProperty(changedItemId, myUI.getMessage(Messages.Price)).getValue() != null
                        && quantityTF != null && quantityTF.getPropertyDataSource().getValue() != null) {
                    movementsTable.getContainerProperty(changedItemId, myUI.getMessage(Messages.Amount)).setValue(
                            (Double) quantityTF.getPropertyDataSource().getValue()
                                    * (Double) movementsTable.getContainerProperty(changedItemId,
                                    myUI.getMessage(Messages.Price)).getValue());
                    repaintMovementsFooter();
                }
            }
        }
    }

    private void updateOldValues(Object changedItemId) {
        int acc_category_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(Messages.Product)).getValue()).getValue();
        int measurement_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(Messages.Measurement)).getValue()).getValue();
        double quantity = (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue();

        movementsTable.getContainerProperty(changedItemId, Settings.acc_category_id).setValue(acc_category_id);
        movementsTable.getContainerProperty(changedItemId, Settings.measurement_id).setValue(measurement_id);
        movementsTable.getContainerProperty(changedItemId, Settings.quantity_id).setValue(quantity);
    }

    private void recalculateRemaindersAfterDelete(Object changedItemId, int acc_category_id, int measurement_id, double quantity) {
        double remain = 0;
        for (Object next : movementsTable.getItemIds()) {
            if (((ComboBox) movementsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Product)).getValue()).getValue() != null
                    && ((ComboBox) movementsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                if (acc_category_id == (Integer) ((ComboBox) movementsTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Product)).getValue()).getValue()
                        && measurement_id == (Integer) ((ComboBox) movementsTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Measurement)).getValue()).getValue()) {
                    remain = 0;
                    if (movementsTable.getContainerProperty(next, myUI.getMessage(Messages.Remain)).getValue() != null) {
                        remain = (Double) movementsTable.getContainerProperty(next, myUI.getMessage(Messages.Remain)).getValue();
                    }
                    movementsTable.getContainerProperty(next, myUI.getMessage(Messages.Remain)).setValue(remain + quantity);
                }
            }
        }
        if (remain > 0) {
            ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Quantity)).getValue()).removeAllValidators();
            ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Quantity)).getValue())
                    .addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, remain));
        }
        if (!disableFields(movementsTable)) {
            enableFields(movementsTable);
        }
    }

    private void recalculateRemaindersAfterInsert(Object changedItemId) {
        int acc_category_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(Messages.Product)).getValue()).getValue();
        int measurement_id = (Integer) ((ComboBox) movementsTable.getContainerProperty(changedItemId,
                myUI.getMessage(Messages.Measurement)).getValue()).getValue();
        double curr_remainder = 0.0, old_remainder = 0.0, value_from_db = 0.0;
        boolean isExistsSame = false;
        for (Object next : movementsTable.getItemIds()) {
            if (((ComboBox) movementsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Product)).getValue()).getValue() != null
                    && ((ComboBox) movementsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Measurement)).getValue()).getValue() != null
                    && ((TextField) movementsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                if (!next.equals(changedItemId)
                        && acc_category_id == (Integer) ((ComboBox) movementsTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Product)).getValue()).getValue()
                        && measurement_id == (Integer) ((ComboBox) movementsTable.getContainerProperty(
                        next, myUI.getMessage(Messages.Measurement)).getValue()).getValue()) {
                    old_remainder = (Double) movementsTable.getContainerProperty(next, myUI.getMessage(Messages.Remain)).getValue();
                    curr_remainder = (Double) movementsTable.getContainerProperty(next, myUI.getMessage(Messages.Remain)).getValue()
                            - (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                            myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue();
                    isExistsSame = true;
                    break;
                }
            }
        }
        if (!isExistsSame) {
            double old_value = 0.0;
            if (getOriginalCont() != null) {
                for (Object next : getOriginalCont().getItemIds()) {
                    if (acc_category_id == (Integer) getOriginalCont().getContainerProperty(next, Settings.acc_category_id).getValue()
                            && measurement_id == (Integer) getOriginalCont().getContainerProperty(next, Settings.measurement_id).getValue()) {
                        old_value -= (Double) getOriginalCont().getContainerProperty(next, Settings.quantity_id).getValue();
                    }
                }
            }
            try {
                DbStockMovements dbCon = new DbStockMovements();
                dbCon.connect();
                value_from_db = dbCon.execSQL_remain(acc_category_id, measurement_id, (Integer) stockSelect.getValue(), dateDF.getValue());
                old_remainder = value_from_db - old_value;
                curr_remainder = old_remainder - (Double) ((TextField) movementsTable.getContainerProperty(changedItemId,
                        myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue();
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        if (isExistsSame) {
            for (Object next : movementsTable.getItemIds()) {
                if (((ComboBox) movementsTable.getContainerProperty(next,
                        myUI.getMessage(Messages.Product)).getValue()).getValue() != null
                        && ((ComboBox) movementsTable.getContainerProperty(next,
                        myUI.getMessage(Messages.Measurement)).getValue()).getValue() != null
                        && ((TextField) movementsTable.getContainerProperty(next,
                        myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue() != null) {
                    if (acc_category_id == (Integer) ((ComboBox) movementsTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Product)).getValue()).getValue()
                            && measurement_id == (Integer) ((ComboBox) movementsTable.getContainerProperty(
                            next, myUI.getMessage(Messages.Measurement)).getValue()).getValue()) {
                        movementsTable.getContainerProperty(next, myUI.getMessage(Messages.Remain)).setValue(curr_remainder);
                    }
                }
            }
        } else {
            movementsTable.getContainerProperty(changedItemId, myUI.getMessage(Messages.Remain)).setValue(curr_remainder);
        }
        if (value_from_db == 0 && !isExistsSame) {
            ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Quantity)).getValue()).removeAllValidators();
            ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Quantity)).getValue())
                    .addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, 0.0));
        } else if (old_remainder > 0) {
            ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Quantity)).getValue()).removeAllValidators();
            ((TextField) movementsTable.getContainerProperty(changedItemId,
                    myUI.getMessage(Messages.Quantity)).getValue())
                    .addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, old_remainder));
        }
        if (!disableFields(movementsTable)) {
            enableFields(movementsTable);
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        searchBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(false);
        stockSelect.setEnabled(true);
        productCategorySelect.setEnabled(true);
        fromEmployeeSelect.setEnabled(true);
        dateDF.setEnabled(true);
        toEmployeeSelect.setEnabled(true);
        noteTF.setEnabled(true);
        if (stockSelect.getValue() != null && dateDF.getValue() != null && productCategorySelect.getValue() != null) {
            rightLay.setEnabled(true);
        }
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnStockIncomeView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnStockIncomeView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnStockIncomeView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        invoiceNumberTF.setEnabled(true);
        searchBtn.setEnabled(true);
        stockSelect.setEnabled(false);
        productCategorySelect.setEnabled(false);
        fromEmployeeSelect.setEnabled(false);
        dateDF.setEnabled(false);
        toEmployeeSelect.setEnabled(false);
        noteTF.setEnabled(false);
        rightLay.setEnabled(false);
    }

    private void fillFields() {
        invoiceNumberTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(Messages.InvoiceNumber)).getValue().toString());
        try {
            dateDF.setValue(Settings.dtmf.parse(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(Messages.Date)).getValue().toString()));
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        stockSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.stock_id).getValue());
        productCategorySelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.acc_category_id).getValue());
        fromEmployeeSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.from_employee_id).getValue());
        toEmployeeSelect.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                Settings.to_employee_id).getValue());
        if (invoicesTable.getContainerProperty(invoicesTable.getValue(),
                myUI.getMessage(Messages.Note)).getValue() != null) {
            noteTF.setValue(invoicesTable.getContainerProperty(invoicesTable.getValue(),
                    myUI.getMessage(Messages.Note)).getValue().toString());
        } else {
            noteTF.setValue("");
        }
    }

    private void clearFields() {
        invoiceNumberTF.removeValueChangeListener(this);
        invoiceNumberTF.setValue("");
        invoiceNumberTF.addValueChangeListener(this);
        dateDF.setValue(new Date());
        stockSelect.setValue(null);
        productCategorySelect.setValue(null);
        fromEmployeeSelect.setValue(null);
        toEmployeeSelect.setValue(null);
        noteTF.setValue("");
        movementsTable.removeAllItems();
        originalCont = null;
        movementsTable.setColumnFooter(myUI.getMessage(Messages.Amount), null);
    }

    private void updateDataContainer() {
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Date)).setValue(
                Settings.dtmf.format(dateDF.getValue()));
        try {
            invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Amount)).setValue(
                    Settings.dFormat2.parse(movementsTable.getColumnFooter(myUI.getMessage(Messages.Amount))).doubleValue());
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Note)).setValue(
                noteTF.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.Stock)).setValue(
                stockSelect.getContainerProperty(stockSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.stock_id).setValue(
                stockSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.FromEmployee)).setValue(
                fromEmployeeSelect.getContainerProperty(fromEmployeeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.from_employee_id).setValue(
                fromEmployeeSelect.getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), myUI.getMessage(Messages.ToEmployee)).setValue(
                toEmployeeSelect.getContainerProperty(toEmployeeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        invoicesTable.getContainerProperty(invoicesTable.getValue(), Settings.to_employee_id).setValue(
                toEmployeeSelect.getValue());
    }

    private void addDataContainerItem(int id) {
        Item item = ((IndexedContainer) invoicesTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(
                Settings.dtmf.format(dateDF.getValue()));
        try {
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                    Settings.dFormat2.parse(movementsTable.getColumnFooter(myUI.getMessage(Messages.Amount))).doubleValue());
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                noteTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Stock)).setValue(
                stockSelect.getContainerProperty(stockSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        item.getItemProperty(Settings.stock_id).setValue(
                stockSelect.getValue());
        item.getItemProperty(Settings.acc_category_id).setValue(productCategorySelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.FromEmployee)).setValue(
                fromEmployeeSelect.getContainerProperty(fromEmployeeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        item.getItemProperty(Settings.from_employee_id).setValue(
                fromEmployeeSelect.getValue());
        item.getItemProperty(myUI.getMessage(Messages.ToEmployee)).setValue(
                toEmployeeSelect.getContainerProperty(toEmployeeSelect.getValue(),
                        myUI.getMessage(Messages.Title)).getValue());
        item.getItemProperty(Settings.to_employee_id).setValue(
                toEmployeeSelect.getValue());
        try {
            DbStockInvoice dbCon = new DbStockInvoice();
            dbCon.connect();
            item.getItemProperty(myUI.getMessage(Messages.InvoiceNumber)).setValue(dbCon.execSQL_invoice_number(id));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        movementsTable.setValue(id);
    }

    private StockInvoice getInvoice(int inv_id) {
        StockInvoice inv = new StockInvoice();
        if (inv_id == 0) {
            try {
                DbStockInvoice dbCon = new DbStockInvoice();
                dbCon.connect();
                inv.setInvoice_number(dbCon.execSQL_max_invoice_number(myUI.getUser().getSchool().getId(), 2) + 1);
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else {
            inv.setInvoiceNumberStr(invoicesTable.getContainerProperty(inv_id, myUI.getMessage(Messages.InvoiceNumber)).getValue().toString());
        }
        if (noteTF.getValue() != null && !noteTF.getValue().isEmpty()) {
            inv.setNote(noteTF.getValue());
        }
        inv.setCreation_date(dateDF.getValue());
        inv.setStock_id((Integer) stockSelect.getValue());
        inv.setTo_employee_id((Integer) toEmployeeSelect.getValue());
        inv.setFrom_employee_id((Integer) fromEmployeeSelect.getValue());
        inv.setAcc_category_id((Integer) productCategorySelect.getValue());
        inv.setService_type_id(2);
        inv.setSchool_id(myUI.getUser().getSchool().getId());
        inv.setEmployee_id(myUI.getUser().getId());
        inv.setId(inv_id);
        return inv;
    }

    private void execDelete() {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            DbStockMovements dbCon = new DbStockMovements();
            dbCon.connect();
            int st = 0;
            ArrayList<StockMovement> delMovementIds = dbCon.execSQL((Integer) invoicesTable.getValue(), null, null);
            if (delMovementIds.size() > 0) {
                for (StockMovement delMovementId : delMovementIds) {
                    st += dbCon.exec_delete(delMovementId);
                }
            }
            if (st != 0) {
                movementsTable.removeAllItems();
                st = dbDef.exec_delete((Integer) invoicesTable.getValue(), Settings.db_dp_invoice);
                if (st != 0) {
                    invoicesTable.getContainerDataSource().removeItem(invoicesTable.getValue());
                    Notification.show(myUI.getMessage(Messages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
                    if (invoicesTable.getContainerDataSource().size() != 0) {
                        invoicesTable.setValue(((IndexedContainer) invoicesTable.getContainerDataSource()).firstItemId());
                    } else {
                        clearFields();
                        invoicesTable.setValue(null);
                    }
                }
            }
            dbCon.close();
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

    public ComboBox createCombobox(int value, String description, String db_table, boolean isRequired) {
        ComboBox cb = new ComboBox();
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

    public TextField createTextFieldWithProperty(Object value, String description, Validator validator, Property p, Converter conv) {
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

    public IndexedContainer prepareMovementsContainer() {
        if (movementsCont == null) {
            movementsCont = new IndexedContainer();
            movementsCont.addContainerProperty(Settings.button, Button.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Product), ComboBox.class, null);
            movementsCont.addContainerProperty(Settings.acc_category_id, Integer.class, 0);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Note), TextField.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Quantity), TextField.class, null);
            movementsCont.addContainerProperty(Settings.quantity_id, Double.class, 0.0);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Remain), Double.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Measurement), ComboBox.class, null);
            movementsCont.addContainerProperty(Settings.measurement_id, Integer.class, 0);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Price), Double.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, null);
            movementsCont.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, 0.0);
            movementsCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            movementsCont.removeAllItems();
        }
        return movementsCont;
    }

    public void prepareOriginalContainer() {
        if (getOriginalCont() == null) {
            originalCont = new IndexedContainer();
            getOriginalCont().addContainerProperty(Settings.acc_category_id, Integer.class, 0);
            getOriginalCont().addContainerProperty(Settings.quantity_id, Double.class, 0.0);
            getOriginalCont().addContainerProperty(Settings.measurement_id, Integer.class, 0);
            getOriginalCont().addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, 0.0);
        } else {
            getOriginalCont().removeAllItems();
        }
    }

    private void addMovementsItem() {
        NATURAL_COL_ORDER_MOVEMENTS = new String[]{Settings.button,
                myUI.getMessage(Messages.Remain),
                myUI.getMessage(Messages.Product),
                myUI.getMessage(Messages.Measurement),
                myUI.getMessage(Messages.Note),
                myUI.getMessage(Messages.Quantity),
                myUI.getMessage(Messages.Price),
                myUI.getMessage(Messages.Rate),
                myUI.getMessage(Messages.Amount)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (movementsTable.getContainerDataSource().size() == 0) {
            movementsTable.setContainerDataSource(prepareMovementsContainer());
        }
        Item item;
        item = ((IndexedContainer) movementsTable.getContainerDataSource()).addItemAt(
                movementsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(Messages.DeleteButton), id, Settings.dbStockMovement));
        ComboBox cb = createCombobox(0, myUI.getMessage(Messages.Product), null, true);
        try {
            DbAccCategory dbCon = new DbAccCategory();
            dbCon.connect();
            cb.setContainerDataSource(dbCon.exec_for_select(myUI, productCategorySelect.getValue() + ""));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setItemCaptionPropertyId(myUI.getMessage(Messages.FullName));
        cb.addValueChangeListener(this);
        cb.setId(id);
        item.getItemProperty(myUI.getMessage(Messages.Product)).setValue(cb);
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(createTextField(
                null, myUI.getMessage(Messages.Note),
                new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), null, 250, false), true));
        cb = createCombobox(0, myUI.getMessage(Messages.Measurement), Settings.dbMeasurement, true);
        cb.addValueChangeListener(this);
        cb.setId(id);
        item.getItemProperty(myUI.getMessage(Messages.Measurement)).setValue(cb);
        TextField tf = createTextFieldWithProperty(null, myUI.getMessage(Messages.Quantity),
                new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2));
        tf.addValueChangeListener(this);
        tf.setId(id);
        tf.setData(myUI.getMessage(Messages.Quantity));
        item.getItemProperty(myUI.getMessage(Messages.Quantity)).setValue(tf);

        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Insert));
        movementsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_MOVEMENTS);
        movementsTable.setColumnExpandRatio(myUI.getMessage(Messages.Product), 1);
        movementsTable.setColumnExpandRatio(myUI.getMessage(Messages.Note), 1);
        movementsTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(Messages.Price), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(Messages.Remain), Table.Align.RIGHT);
        movementsTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);

    }

    private void setMovementsTable() {
        try {
            NATURAL_COL_ORDER_MOVEMENTS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Remain),
                    myUI.getMessage(Messages.Product),
                    myUI.getMessage(Messages.Measurement),
                    myUI.getMessage(Messages.Note),
                    myUI.getMessage(Messages.Quantity),
                    myUI.getMessage(Messages.Price),
                    myUI.getMessage(Messages.Rate),
                    myUI.getMessage(Messages.Amount)};
            DbStockMovements dbepn = new DbStockMovements();
            dbepn.connect();
            movementsTable.setContainerDataSource(dbepn.execSQL(myUI, invID, this));
            dbepn.close();
            movementsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_MOVEMENTS);
            movementsTable.setColumnExpandRatio(myUI.getMessage(Messages.Product), 1);
            movementsTable.setColumnExpandRatio(myUI.getMessage(Messages.Note), 1);
            movementsTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(Messages.Price), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(Messages.Remain), Table.Align.RIGHT);
            movementsTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void repaintMovementsFooter() {
        double totPrice = 0.0;
        if (movementsTable.getContainerDataSource().size() > 0) {
            for (Object next : movementsTable.getItemIds()) {
                totPrice += (Double) movementsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue();
            }
        }
        movementsTable.setColumnFooter(myUI.getMessage(Messages.Amount), Settings.dFormat2.format(totPrice));
    }

    private void insertMovements(int invoice_id) {
        try {
            DbStockMovements dbCon = new DbStockMovements();
            dbCon.connect();
            ArrayList<StockMovement> delMovementIds = dbCon.execSQL(invoice_id, null, null);
            if (delMovementIds.size() > 0) {
                for (StockMovement delMovementId : delMovementIds) {
                    dbCon.exec_delete(delMovementId);
                }
            }
            if (movementsTable.getContainerDataSource().size() > 0) {
                Iterator<?> iter = movementsTable.getItemIds().iterator();
                int order_num = 1;
                while (iter.hasNext()) {
                    Object next = iter.next();
                    StockMovement smv = new StockMovement();
                    smv.setInvoice_id(invoice_id);
                    smv.setOrder_number(order_num++);
                    smv.setNote(((TextField) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Note)).getValue()).getValue());
                    smv.setAcc_category_id((Integer) ((ComboBox) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Product)).getValue()).getValue());
                    smv.setMeasurement_id((Integer) ((ComboBox) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Measurement)).getValue()).getValue());

                    double quantity = (Double) ((TextField) movementsTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Quantity)).getValue()).getPropertyDataSource().getValue();
                    try {
                        ArrayList<StockMovement> list = dbCon.execSQL_remains(smv.getAcc_category_id(), smv.getMeasurement_id(),
                                (Integer) stockSelect.getValue());
                        for (int i = 0; i < list.size() && quantity > 0.0; i++) {
                            smv.setQuantity(quantity);
                            if (quantity > list.get(i).getRemain()) {
                                smv.setQuantity(list.get(i).getRemain());
                                quantity -= list.get(i).getRemain();
                            } else {
                                quantity = 0.0;
                            }
                            smv.setPrice(list.get(i).getPrice());
                            smv.setStock_movement_id(list.get(i).getId());
                            smv.setRate(list.get(i).getRate());
                            dbCon.exec_insert(smv);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void enableFields(Table t) {
        for (Object next : ((IndexedContainer) t
                .getContainerDataSource()).getItemIds()) {
            for (Object next1 : (t.getContainerDataSource()).getContainerPropertyIds()) {
                Object c = t.getItem(next).getItemProperty(next1).getValue();
                if (c instanceof AbstractField) {
                    ((AbstractField<?>) c).setEnabled(true);
                } else if (c instanceof AbstractComponent) {
                    ((AbstractComponent) c).setEnabled(true);
                }
            }
        }
    }

    private boolean disableFields(Table t) {
        Object idToEnable = null;
        for (Object next : ((IndexedContainer) t
                .getContainerDataSource()).getItemIds()) {
            for (Object next1 : t.getContainerDataSource().getContainerPropertyIds()) {
                Object c = t.getItem(next).getItemProperty(next1).getValue();
                if (c instanceof AbstractField) {
                    if (!((AbstractField<?>) c).isValid()) {
                        idToEnable = next;
                    } else {
                        ((AbstractField<?>) c).setEnabled(false);
                    }
                } else if (c instanceof AbstractComponent) {
                    ((AbstractComponent) c).setEnabled(false);
                }
            }
        }
        if (idToEnable == null) {
            return false;
        } else {
            enableRow(t, idToEnable);
            return true;
        }
    }

    public void enableRow(Table t, Object itemId) {
        for (Object next1 : t.getContainerDataSource().getContainerPropertyIds()) {
            Object c = t.getItem(itemId).getItemProperty(next1).getValue();
            if (c instanceof AbstractField) {
                ((AbstractField<?>) c).setEnabled(true);
            } else if (c instanceof AbstractComponent) {
                ((AbstractComponent) c).setEnabled(true);
            }
        }
    }

    public void setMovementsFooter(double amount) {
        movementsTable.setColumnFooter(myUI.getMessage(Messages.Amount), Settings.dFormat2.format(amount));
    }

    public ComboBox getProductCategorySelect() {
        return productCategorySelect;
    }

    /**
     * @return the originalCont
     */
    public IndexedContainer getOriginalCont() {
        return originalCont;
    }

    public Component getNewObj() {
        return new StockOutcomeView(myUI);
    }
}
