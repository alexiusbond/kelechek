/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.themes.ValoTheme;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbAccTransactions;
import kg.alex.indigo.dao.DbCurrencyRate;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbEmployee;
import kg.alex.indigo.domain.AccTransaction;
import kg.alex.indigo.domain.CurrencyRate;
import kg.alex.indigo.domain.SchoolAccounting;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.pdf.TransactionInvoicePDF;
import kg.alex.indigo.utils.ValueFromContainerConverter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CashBoxView extends GridLayout implements Button.ClickListener,
        Property.ValueChangeListener, FieldGroup.CommitHandler {
    static final Logger logger = LogManager.getLogger(CashBoxView.class);
    private final MyVaadinUI myUI;
    private Button addButton, saveButton, searchButton;
    private OptionGroup currencySettingsOG;
    private TextField currencyTF;
    private Label incomeTtlLab;
    private Label expenseTtlLab;
    private Label ttlLab;
    private Label prev_balanceLab;
    public SchoolAccounting schoolAcc;
    private DateField fromDateDF, tillDateDF;
    private final Grid expensesGrid, incomesGrid;
    private GeneratedPropertyContainer incomesCont = null, expensesCont = null;
    private int r_table_counter = 1000;
    private final Accordion accordion;
    private final Subject currentUser = SecurityUtils.getSubject();
    private HorizontalLayout currencyHl;
    private ComboBox expensesCategoryCb, incomesCategoryCb, toEmployeesCb;
    private Date today;
    private final Map<String, Container.Filter> filters = new HashMap<>();

    public CashBoxView(MyVaadinUI myUI) {
        this.myUI = myUI;

        setRows(3);
        setColumns(4);
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        buildLabels();
        accordion = new Accordion();
        accordion.setSizeFull();
        incomesGrid = new Grid();
        buildGrid(incomesGrid);
        expensesGrid = new Grid();
        buildGrid(expensesGrid);
        TabSheet.Tab tab = accordion.addTab(incomesGrid, myUI.getMessage(IndigoMessages.Incomes));
        tab.setId("1");
        tab = accordion.addTab(expensesGrid, myUI.getMessage(IndigoMessages.Expenses));
        tab.setId("2");
        accordion.setSelectedTab(expensesGrid);
        accordion.addSelectedTabChangeListener((TabSheet.SelectedTabChangeListener)
                event -> setGridData(Integer.parseInt(accordion.getSelectedTab().getId())));
        addComponent(accordion, 0, 2, 3, 2);
        setRowExpandRatio(2, 1);
        getTotals();
        recount();
    }

    private void buildLabels() {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);
        hl.setSpacing(true);

        if (today == null) {
            today = new Date();
        }
        fromDateDF = new DateField();
        fromDateDF.setDescription(myUI.getMessage(IndigoMessages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(today);

        tillDateDF = new DateField();
        tillDateDF.setDescription(myUI.getMessage(IndigoMessages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(today);

        searchButton = new Button();
        searchButton.addStyleName(ValoTheme.BUTTON_SMALL);
        searchButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        searchButton.setIcon(FontAwesome.SEARCH);
        searchButton.addClickListener(this);

        hl.addComponent(fromDateDF);
        hl.addComponent(tillDateDF);
        hl.addComponent(searchButton);
        hl.setExpandRatio(fromDateDF, 1);
        hl.setExpandRatio(tillDateDF, 1);

        incomeTtlLab = new Label();
        incomeTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        incomeTtlLab.setImmediate(true);
        incomeTtlLab.setWidth(Settings.PERCENTS100);

        expenseTtlLab = new Label();
        expenseTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        expenseTtlLab.setImmediate(true);
        expenseTtlLab.setWidth(Settings.PERCENTS100);

        ttlLab = new Label();
        ttlLab.setContentMode(ContentMode.HTML);
        ttlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        ttlLab.setImmediate(true);
        ttlLab.setWidth(Settings.PERCENTS100);

        prev_balanceLab = new Label();
        prev_balanceLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        prev_balanceLab.setImmediate(true);
        prev_balanceLab.setSizeFull();

        addButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        addButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        addButton.addStyleName(ValoTheme.BUTTON_SMALL);
        addButton.setIcon(FontAwesome.PLUS_SQUARE);
        addButton.setWidth(Settings.PERCENTS100);
        addButton.addClickListener(this);

        currencyHl = new HorizontalLayout();
        currencyHl.setWidth(Settings.PERCENTS100);
        currencyHl.setSpacing(true);
        currencyHl.setEnabled(currentUser.hasRole(Settings.rnAdmin) && myUI.isManualRate());

        Label currencyLab = new Label();
        currencyLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        currencyLab.setImmediate(true);
        currencyLab.setSizeUndefined();
        currencyLab.setValue(myUI.getMessage(IndigoMessages.RateUSD));

        currencyTF = new TextField();
        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        currencyTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        currencyTF.setWidth(Settings.PERCENTS100);
        currencyTF.setNullRepresentation("0.01");
        currencyTF.setNullSettingAllowed(false);
        currencyTF.setConverter(Settings.getStringToDoubleConverter(4));
        currencyTF.addValidator(new DoubleRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1.0, null));
        currencyTF.setPropertyDataSource(property);
        currencyTF.getPropertyDataSource().setValue(myUI.getDb_currency_rate());

        currencyHl.addComponent(currencyLab);
        currencyHl.addComponent(currencyTF);
        currencyHl.setExpandRatio(currencyTF, 1);

        HorizontalLayout currencySettingsHl = new HorizontalLayout();
        currencySettingsHl.setWidth(Settings.PERCENTS100);
        currencySettingsHl.setSpacing(true);
        currencySettingsHl.setEnabled(currentUser.hasRole(Settings.rnAdmin));

        currencySettingsOG = new OptionGroup();
        currencySettingsOG.setWidth(Settings.PERCENTS100);
        currencySettingsOG.setStyleName(ValoTheme.OPTIONGROUP_SMALL);
        currencySettingsOG.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        currencySettingsOG.addItem(myUI.getMessage(IndigoMessages.OptimaCurrency));
        currencySettingsOG.addItem(myUI.getMessage(IndigoMessages.Manual));
        currencySettingsOG.addValueChangeListener(this);
        if (myUI.isManualRate()) {
            currencySettingsOG.select(myUI.getMessage(IndigoMessages.Manual));
        } else {
            currencySettingsOG.select(myUI.getMessage(IndigoMessages.OptimaCurrency));
        }

        saveButton = new Button();
        saveButton.setDescription(myUI.getMessage(IndigoMessages.SaveButton));
        saveButton.addStyleName(ValoTheme.BUTTON_SMALL);
        saveButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveButton.setIcon(FontAwesome.SAVE);
        saveButton.addClickListener(this);

        currencySettingsHl.addComponent(currencySettingsOG);
        currencySettingsHl.addComponent(saveButton);
        currencySettingsHl.setExpandRatio(currencySettingsOG, 1);

        addComponent(hl, 0, 0);
        addComponent(addButton, 0, 1);
        addComponent(prev_balanceLab, 1, 0);
        addComponent(incomeTtlLab, 1, 1);
        addComponent(expenseTtlLab, 2, 1);
        addComponent(ttlLab, 2, 0);
        addComponent(currencyHl, 3, 0);
        addComponent(currencySettingsHl, 3, 1);
        setComponentAlignment(addButton, Alignment.BOTTOM_RIGHT);
        setComponentAlignment(ttlLab, Alignment.MIDDLE_RIGHT);
        setComponentAlignment(prev_balanceLab, Alignment.BOTTOM_LEFT);
        setColumnExpandRatio(0, 2);
        setColumnExpandRatio(1, 4);
        setColumnExpandRatio(2, 4);
        setColumnExpandRatio(3, 2);
    }

    private void buildGrid(Grid grid) {
        int in_out_id = grid == expensesGrid ? 2 : 1;
        grid.setId(in_out_id + "");
        setGridData(in_out_id);
        grid.setSizeFull();
        grid.setEditorEnabled(true);
        grid.setEditorBuffered(true);
        grid.addItemClickListener((ItemClickEvent.ItemClickListener) event -> {
            if (!(Boolean) event.getItem().getItemProperty(Settings.is_disabled).getValue()) {
                grid.setEditorEnabled(true);
                if (!grid.isEditorActive()) {
                    try {
                        grid.editItem(event.getItemId());
                    } catch (Exception ignored) {
                    }
                }

                DateField dateDf = (DateField) grid.getColumn(myUI.getMessage(IndigoMessages.Date)).getEditorField();
                if (!currentUser.isPermitted(Settings.cnTransactionsView + ":"
                        + Settings.prmChangeOldTransactions)) {
                    if (grid.getContainerDataSource().getContainerProperty(event.getItemId(),
                            myUI.getMessage(IndigoMessages.Date)).getValue() != null) {
                        dateDf.setRangeStart((Date) grid.getContainerDataSource().getContainerProperty(event.getItemId(),
                                myUI.getMessage(IndigoMessages.Date)).getValue());
                    } else {
                        if (today == null) {
                            today = new Date();
                        }
                        dateDf.setRangeStart(today);
                    }
                } else {
                    dateDf.setRangeStart(null);
                }
                TextField amountUSDTf = (TextField) grid.getColumn(myUI.getMessage(IndigoMessages.AmountUSD)).getEditorField();
                TextField amountKGSTf = (TextField) grid.getColumn(myUI.getMessage(IndigoMessages.AmountKGS)).getEditorField();
                refreshValidators(amountUSDTf);
                refreshValidators(amountKGSTf);
                if (grid.getContainerDataSource().getContainerProperty(event.getItemId(), myUI.getMessage(IndigoMessages.AmountUSD)).getValue() == null &&
                        grid.getContainerDataSource().getContainerProperty(event.getItemId(), myUI.getMessage(IndigoMessages.AmountKGS)).getValue() == null) {
                    amountUSDTf.setRequired(true);
                    amountKGSTf.setRequired(true);
                } else if (grid.getContainerDataSource().getContainerProperty(event.getItemId(), myUI.getMessage(IndigoMessages.AmountUSD)).getValue() == null) {
                    amountKGSTf.setRequired(true);
                    amountUSDTf.setRequired(false);
                } else if (grid.getContainerDataSource().getContainerProperty(event.getItemId(), myUI.getMessage(IndigoMessages.AmountKGS)).getValue() == null) {
                    amountUSDTf.setRequired(true);
                    amountKGSTf.setRequired(false);
                }
            } else {
                grid.setEditorEnabled(false);
            }
        });
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.getColumn(Settings.from_employee_id).setHidden(true);
        grid.getColumn(Settings.is_disabled).setHidden(true);
        grid.getColumn(Settings.acc_currency_id).setHidden(true);
        if (grid == expensesGrid) {
            if (expensesCategoryCb == null) {
                expensesCategoryCb = createCombobox(null, true, null);
                try {
                    DbAccTransactions dbac = new DbAccTransactions();
                    dbac.connect();
                    expensesCategoryCb.setContainerDataSource(dbac.exec_for_select(myUI, 2, myUI.getUser().getSchool().getId(), 0));
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(IndigoMessages.Category)).setEditorField(expensesCategoryCb);
            if (toEmployeesCb == null) {
                toEmployeesCb = createCombobox(null, false, null);
                try {
                    DbEmployee dbCon = new DbEmployee();
                    dbCon.connect();
                    toEmployeesCb.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId(), 0));
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(IndigoMessages.ToEmployee)).setEditorField(toEmployeesCb);
        } else {
            if (incomesCategoryCb == null) {
                incomesCategoryCb = createCombobox(null, true, null);
                try {
                    DbAccTransactions dbac = new DbAccTransactions();
                    dbac.connect();
                    incomesCategoryCb.setContainerDataSource(dbac.exec_for_select(myUI, 1, myUI.getUser().getSchool().getId(), 0));
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(IndigoMessages.Category)).setEditorField(incomesCategoryCb);
        }
        grid.getColumn(myUI.getMessage(IndigoMessages.Date)).setEditorField(createDateField(this));
        grid.getColumn(myUI.getMessage(IndigoMessages.AmountUSD)).setEditorField(createTextField(
                new DoubleRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 0.01, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), this));
        grid.getColumn(myUI.getMessage(IndigoMessages.AmountKGS)).setEditorField(createTextField(
                new DoubleRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 0.01, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), this));
        grid.getColumn(myUI.getMessage(IndigoMessages.Rate)).setEditorField(createTextField(
                new DoubleRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1.0, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(4), this));
        grid.getColumn(myUI.getMessage(IndigoMessages.Note)).setEditorField(createTextField(
                new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 350, false),
                null, null, null));

        grid.getColumn(Settings.hashTags).setEditable(false);
        grid.getColumn(Settings.acc_currency_id).setEditable(false);
        grid.getColumn(Settings.button).setEditable(false);
        grid.getColumn(Settings.from_employee_id).setEditable(false);
        grid.getColumn(Settings.is_disabled).setEditable(false);
        grid.getColumn(myUI.getMessage(IndigoMessages.Rate)).setEditable(
                currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate));
        grid.setEditorSaveCaption(myUI.getMessage(IndigoMessages.SaveButton));
        grid.setEditorCancelCaption(myUI.getMessage(IndigoMessages.Cancel));

        grid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
            if (cellReference.getProperty().getType() == Double.class || cellReference.getProperty().getType() == Date.class
                    || cellReference.getPropertyId().equals(Settings.hashTags)) {
                return "align-right";
            } else {
                return null;
            }
        });
        grid.setRowStyleGenerator((Grid.RowStyleGenerator) rowReference -> {
            if ((Boolean) rowReference.getItem().getItemProperty(Settings.is_disabled).getValue()) {
                return "yellow";
            }
            if (rowReference.getItemId().toString().contains(Settings.FreshItem)) {
                return "green";
            }
            return "ord";
        });

        grid.getColumn(myUI.getMessage(IndigoMessages.Date)).setRenderer(new DateRenderer(Settings.dtmf));
        grid.getColumn(myUI.getMessage(IndigoMessages.AmountUSD)).setRenderer(
                new NumberRenderer(Settings.getNumberFormat(2)));
        grid.getColumn(myUI.getMessage(IndigoMessages.AmountKGS)).setRenderer(
                new NumberRenderer(Settings.getNumberFormat(2)));
        grid.getColumn(myUI.getMessage(IndigoMessages.Rate)).setRenderer(
                new NumberRenderer(Settings.getNumberFormat(4)));
        if (grid == expensesGrid) {
            grid.getColumn(myUI.getMessage(IndigoMessages.Category)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) expensesCategoryCb.getContainerDataSource(),
                            myUI.getMessage(IndigoMessages.Category), myUI));
            grid.getColumn(myUI.getMessage(IndigoMessages.ToEmployee)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) toEmployeesCb.getContainerDataSource(),
                            myUI.getMessage(IndigoMessages.ToEmployee), myUI));
        } else {
            grid.getColumn(myUI.getMessage(IndigoMessages.Category)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) incomesCategoryCb.getContainerDataSource(),
                            myUI.getMessage(IndigoMessages.Category), myUI));
        }
        grid.getColumn(Settings.button).setRenderer(new ComponentRenderer());

        grid.getColumn(Settings.button).setWidth(52);
        grid.getColumn(Settings.hashTags).setWidth(60);
        grid.getColumn(myUI.getMessage(IndigoMessages.Date)).setWidth(120);
        grid.getColumn(myUI.getMessage(IndigoMessages.Rate)).setWidth(100);
        grid.getColumn(myUI.getMessage(IndigoMessages.AmountUSD)).setWidth(105);
        grid.getColumn(myUI.getMessage(IndigoMessages.AmountKGS)).setWidth(105);
        grid.getColumn(myUI.getMessage(IndigoMessages.Category)).setMinimumWidth(300);
        grid.getColumn(myUI.getMessage(IndigoMessages.Category)).setExpandRatio(1);
        if (grid.getColumn(myUI.getMessage(IndigoMessages.ToEmployee)) != null) {
            grid.getColumn(myUI.getMessage(IndigoMessages.ToEmployee)).setMinimumWidth(230);
            grid.getColumn(myUI.getMessage(IndigoMessages.ToEmployee)).setExpandRatio(1);
        }
        grid.getColumn(myUI.getMessage(IndigoMessages.Note)).setMinimumWidth(300);
        grid.getColumn(myUI.getMessage(IndigoMessages.Note)).setMaximumWidth(400);
        grid.getColumn(myUI.getMessage(IndigoMessages.Note)).setExpandRatio(1);
        grid.getEditorFieldGroup().addCommitHandler(this);

        Grid.HeaderRow filterRow = grid.appendHeaderRow();
        for (Object pid : grid.getContainerDataSource().getContainerPropertyIds()) {
            if (grid.getColumn(pid).getEditorField() instanceof TextField) {
                Grid.HeaderCell cell = filterRow.getCell(pid);

                TextField filterField = new TextField();
                filterField.setStyleName(ValoTheme.TEXTFIELD_TINY);
                filterField.setWidth(Settings.PERCENTS100);
                filterField.setInputPrompt(myUI.getMessage(IndigoMessages.Filter));

                filterField.addTextChangeListener(change -> {
                    ((GeneratedPropertyContainer) grid.getContainerDataSource()).removeContainerFilter(filters.get(pid));
                    if (!change.getText().isEmpty()) {
                        filters.put(pid.toString(), new SimpleStringFilter(pid, change.getText(), true, false));
                        ((GeneratedPropertyContainer) grid.getContainerDataSource()).addContainerFilter(filters.get(pid));
                    }
                });
                cell.setComponent(filterField);
            } else if (grid.getColumn(pid).getEditorField() instanceof ComboBox) {
                Grid.HeaderCell cell = filterRow.getCell(pid);

                ComboBox filterField = createCombobox(null, false, null);
                filterField.setStyleName(ValoTheme.COMBOBOX_TINY);
                filterField.setWidth(Settings.PERCENTS100);
                filterField.setNullSelectionAllowed(true);
                filterField.setInputPrompt(myUI.getMessage(IndigoMessages.Filter));
                try {
                    if (grid == expensesGrid && pid.equals(myUI.getMessage(IndigoMessages.Category))) {
                        DbAccTransactions dbac = new DbAccTransactions();
                        dbac.connect();
                        filterField.setContainerDataSource(dbac.exec_for_select(myUI, 2, myUI.getUser().getSchool().getId(), 0));
                        dbac.close();
                    } else if (grid == incomesGrid && pid.equals(myUI.getMessage(IndigoMessages.Category))) {
                        DbAccTransactions dbac = new DbAccTransactions();
                        dbac.connect();
                        filterField.setContainerDataSource(dbac.exec_for_select(myUI, 1, myUI.getUser().getSchool().getId(), 0));
                        dbac.close();
                    } else if (pid.equals(myUI.getMessage(IndigoMessages.ToEmployee))) {
                        DbEmployee dbCon = new DbEmployee();
                        dbCon.connect();
                        filterField.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId(), 0));
                        dbCon.close();
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }

                filterField.addValueChangeListener(change -> {
                    ((GeneratedPropertyContainer) grid.getContainerDataSource()).removeContainerFilter(filters.get(pid));
                    if (change.getProperty().getValue() != null) {
                        filters.put(pid.toString(), new Compare.Equal(pid, change.getProperty().getValue()));
                        ((GeneratedPropertyContainer) grid.getContainerDataSource()).addContainerFilter(filters.get(pid));
                    }
                });
                cell.setComponent(filterField);
            }
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == searchButton) {
            if (fromDateDF.getValue() != null && tillDateDF.getValue() != null) {
                setGridData(Integer.parseInt(accordion.getSelectedTab().getId()));
                getTotals();
                recount();
            }
        } else if (source == addButton) {
            addGridItem();
        } else if (source == saveButton) {
            if (currencyTF.isValid()) {
                int st = 0;
                CurrencyRate cr = new CurrencyRate();
                try {
                    DbCurrencyRate dbCon = new DbCurrencyRate();
                    dbCon.connect();
                    cr.setEmployee_id(myUI.getUser().getId());
                    cr.setSchool_id(myUI.getUser().getSchool().getId());
                    if (currencySettingsOG.getValue().equals(myUI.getMessage(IndigoMessages.Manual))) {
                        cr.setValue((Double) currencyTF.getPropertyDataSource().getValue());
                        cr.setManual(1);
                    } else {
                        cr.setManual(0);
                    }
                    st = dbCon.exec_insert(cr);
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (st != 0) {
                    if (cr.getManual() == 0) {
                        currencyTF.getPropertyDataSource().setValue(myUI.getCurrencyRateFromOptima());
                    }
                    Notification.show(myUI.getMessage(IndigoMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue), Notification.Type.WARNING_MESSAGE);
            }
        } else if (source.getData().equals(myUI.getMessage(IndigoMessages.Print)) && !source.getId().contains(Settings.FreshItem)) {
            Item item;
            AccTransaction tr;
            ComboBox categoryCb;
            int acc_type_id;
            String orderName;
            if (accordion.getSelectedTab() == incomesGrid) {
                item = incomesGrid.getContainerDataSource().getItem(source.getId());
                acc_type_id = 1;
                tr = getTransaction(item, source.getId(), acc_type_id);
                categoryCb = incomesCategoryCb;
                orderName = myUI.getMessage(IndigoMessages.IncomeOrder);
            } else {
                item = expensesGrid.getContainerDataSource().getItem(source.getId());
                acc_type_id = 2;
                tr = getTransaction(item, source.getId(), acc_type_id);
                categoryCb = expensesCategoryCb;
                orderName = myUI.getMessage(IndigoMessages.ExpenseOrder);
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.ToEmployee)).getValue() != null
                        && (Integer) item.getItemProperty(myUI.getMessage(IndigoMessages.ToEmployee)).getValue() != 0) {
                    tr.setFrom_to_employee(toEmployeesCb.getContainerProperty(
                            item.getItemProperty(myUI.getMessage(IndigoMessages.ToEmployee)).getValue(),
                            myUI.getMessage(IndigoMessages.Title)).getValue().toString());
                }
            }
            tr.setId(source.getId());
            tr.setCategory(categoryCb.getContainerProperty(
                    item.getItemProperty(myUI.getMessage(IndigoMessages.Category)).getValue(),
                    myUI.getMessage(IndigoMessages.Title)).getValue().toString());
            if (tr.getOrder_number() == 0) {
                try {
                    DbAccTransactions dbTr = new DbAccTransactions();
                    dbTr.connect();
                    tr.setOrder_number(dbTr.exec_update_order_number(tr.getId(), tr.getSchool_id(), acc_type_id));
                    dbTr.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            item.getItemProperty(Settings.hashTags).setValue(String.format("%07d", tr.getOrder_number()));
            new TransactionInvoicePDF(myUI, tr, myUI.getUser().getSchool().getName_ru(), myUI.getUser().getSchool().getPhoto(), orderName);
        } else if (source.getData().equals(myUI.getMessage(IndigoMessages.DeleteButton))) {
            if (source.getId().contains(Settings.FreshItem)) {
                ((Grid) accordion.getSelectedTab()).getContainerDataSource().removeItem(source.getId());
            } else {
                ConfirmDialog.show(myUI, myUI.getMessage(IndigoMessages.Question),
                        myUI.getMessage(IndigoMessages.ConfirmDeletion),
                        myUI.getMessage(IndigoMessages.Yes),
                        myUI.getMessage(IndigoMessages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete(source);
                            }
                        });
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property != currencySettingsOG) {
            Grid grid = (Grid) accordion.getSelectedTab();
            TextField rateTf = null;

            if (grid != null && grid.isEditorActive() && grid.getEditedItemId() != null) {
                String itemId = grid.getEditedItemId().toString();
                Item item = grid.getContainerDataSource().getItem(itemId);
                TextField amountUSDTf = (TextField) grid.getEditorFieldGroup().getField(myUI.getMessage(IndigoMessages.AmountUSD));
                TextField amountKGSTf = (TextField) grid.getEditorFieldGroup().getField(myUI.getMessage(IndigoMessages.AmountKGS));
                if (property == amountKGSTf && amountKGSTf.getValue() != null) {
                    item.getItemProperty(Settings.acc_currency_id).setValue(1);
                    amountKGSTf.setRequired(true);
                    amountUSDTf.removeValueChangeListener(this);
                    amountUSDTf.setValue(null);
                    amountUSDTf.setRequired(false);
                    amountUSDTf.addValueChangeListener(this);
                } else if (property == amountUSDTf && amountUSDTf.getValue() != null) {
                    item.getItemProperty(Settings.acc_currency_id).setValue(2);
                    amountUSDTf.setRequired(true);
                    amountKGSTf.removeValueChangeListener(this);
                    amountKGSTf.setValue(null);
                    amountKGSTf.setRequired(false);
                    amountKGSTf.addValueChangeListener(this);
                } else if ((property == amountUSDTf || property == amountKGSTf) && amountUSDTf.getValue() == null
                        && amountKGSTf.getValue() == null) {
                    if ((Integer) item.getItemProperty(Settings.acc_currency_id).getValue() == 1) {
                        amountKGSTf.setRequired(true);
                    } else {
                        amountUSDTf.setRequired(true);
                    }
                }
                if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate)) {
                    rateTf = (TextField) grid.getEditorFieldGroup().getField(myUI.getMessage(IndigoMessages.Rate));
                }
                DateField dateDf = (DateField) grid.getEditorFieldGroup().getField(myUI.getMessage(IndigoMessages.Date));

                if (accordion.getSelectedTab() == expensesGrid) {
                    if ((amountUSDTf.getValue() != null || amountKGSTf.getValue() != null)
                            && (!currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ||
                            rateTf.getValue() != null) && dateDf.getValue() != null) {
                        try {
                            double amount;
                            double rate = currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ?
                                    Settings.dFormat2.parse(rateTf.getValue()).doubleValue() :
                                    (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).getValue();
                            boolean isKGS = (Integer) item.getItemProperty(Settings.acc_currency_id).getValue() == 1;
                            if (isKGS) {
                                amount = Settings.dFormat2.parse(amountKGSTf.getValue()).doubleValue();
                                if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate)) {
                                    amount = Settings.round(amount / rate, 2);
                                } else {
                                    amount = Settings.round(amount / (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).getValue(), 2);
                                }
                            } else {
                                amount = Settings.dFormat2.parse(amountUSDTf.getValue()).doubleValue();
                            }
                            if (amount > 0.0) {
                                double old_amount = 0;
                                if ((Integer) item.getItemProperty(Settings.acc_currency_id).getValue() == 1 &&
                                        item.getItemProperty(myUI.getMessage(IndigoMessages.AmountKGS)).getValue() != null) {
                                    old_amount = (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.AmountKGS)).getValue();
                                    old_amount = Settings.round(old_amount / (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).getValue(), 2);
                                } else if (item.getItemProperty(myUI.getMessage(IndigoMessages.AmountUSD)).getValue() != null) {
                                    old_amount = (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.AmountUSD)).getValue();
                                }
                                DbAccTransactions dbTr = new DbAccTransactions();
                                dbTr.connect();
                                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool().getId(),
                                        dateDf.getValue(), old_amount, amount, 2);
                                amountUSDTf.removeAllValidators();
                                amountKGSTf.removeAllValidators();
                                if (tr != null) {
                                    double limit = tr.getLimit();
                                    if (isKGS) {
                                        limit = limit * rate;
                                        amountKGSTf.addValidator(new DoubleRangeValidator(myUI.getMessage(IndigoMessages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                + " $ (" + Settings.df.format(tr.getDate()) + ")", 0.01, Settings.round(limit, 2)));
                                    } else {
                                        amountUSDTf.addValidator(new DoubleRangeValidator(myUI.getMessage(IndigoMessages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                + " $ (" + Settings.df.format(tr.getDate()) + ")", 0.01, Settings.round(limit, 2)));
                                    }
                                    Notification.show(myUI.getMessage(IndigoMessages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                            + " $ (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                                } else {
                                    refreshValidators(amountUSDTf);
                                    refreshValidators(amountKGSTf);
                                }
                                dbTr.close();
                            } else {
                                refreshValidators(amountUSDTf);
                                refreshValidators(amountKGSTf);
                            }
                        } catch (Exception e) {
                            showMessageIfAnyInvalid(rateTf, amountUSDTf, amountKGSTf, dateDf);
                            refreshValidators(amountUSDTf);
                            refreshValidators(amountKGSTf);
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        showMessageIfAnyInvalid(rateTf, amountUSDTf, amountKGSTf, dateDf);
                        refreshValidators(amountUSDTf);
                        refreshValidators(amountKGSTf);
                    }
                } else if (accordion.getSelectedTab() == incomesGrid && !itemId.contains(Settings.FreshItem)) {
                    if ((amountUSDTf.getValue() != null || amountKGSTf.getValue() != null) &&
                            (!currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ||
                                    rateTf.getValue() != null) && dateDf.getValue() != null) {
                        try {
                            double amount;
                            double rate;
                            rate = currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate) ?
                                    Settings.dFormat2.parse(rateTf.getValue()).doubleValue() :
                                    (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).getValue();
                            boolean isKGS = (Integer) item.getItemProperty(Settings.acc_currency_id).getValue() == 1;
                            if (isKGS) {
                                amount = Settings.dFormat2.parse(amountKGSTf.getValue()).doubleValue();
                                if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate)) {
                                    amount = Settings.round(amount / rate, 2);
                                } else {
                                    amount = Settings.round(amount / (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).getValue(), 2);
                                }
                            } else {
                                amount = Settings.dFormat2.parse(amountUSDTf.getValue()).doubleValue();
                            }
                            if (amount > 0.0) {
                                double old_amount = 0;
                                if ((Integer) item.getItemProperty(Settings.acc_currency_id).getValue() == 1 &&
                                        item.getItemProperty(myUI.getMessage(IndigoMessages.AmountKGS)).getValue() != null) {
                                    old_amount = (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.AmountKGS)).getValue();
                                    old_amount = Settings.round(old_amount / (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).getValue(), 2);
                                } else if (item.getItemProperty(myUI.getMessage(IndigoMessages.AmountUSD)).getValue() != null) {
                                    old_amount = (Double) item.getItemProperty(myUI.getMessage(IndigoMessages.AmountUSD)).getValue();
                                }
                                if (amount <= old_amount || DateUtils.truncate(dateDf.getValue(), Calendar.DAY_OF_MONTH)
                                        .compareTo((Date) item.getItemProperty(myUI.getMessage(IndigoMessages.Date)).getValue()) != 0) {
                                    DbAccTransactions dbTr = new DbAccTransactions();
                                    dbTr.connect();
                                    AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool().getId(),
                                            dateDf.getValue(), old_amount, amount, 1);
                                    amountUSDTf.removeAllValidators();
                                    amountKGSTf.removeAllValidators();
                                    if (tr != null) {
                                        double limit = tr.getLimit();
                                        if (isKGS) {
                                            limit = limit * rate;
                                            amountKGSTf.addValidator(new DoubleRangeValidator(myUI.getMessage(IndigoMessages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                    + " $ (" + Settings.df.format(tr.getDate()) + ")", Settings.round(limit, 2), null));
                                        } else {
                                            amountUSDTf.addValidator(new DoubleRangeValidator(myUI.getMessage(IndigoMessages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                    + " $ (" + Settings.df.format(tr.getDate()) + ")", Settings.round(limit, 2), null));
                                        }
                                        Notification.show(myUI.getMessage(IndigoMessages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                + " $ (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                                    } else {
                                        refreshValidators(amountUSDTf);
                                        refreshValidators(amountKGSTf);
                                    }
                                    dbTr.close();
                                } else {
                                    refreshValidators(amountUSDTf);
                                    refreshValidators(amountKGSTf);
                                }

                            } else {
                                refreshValidators(amountUSDTf);
                                refreshValidators(amountKGSTf);
                            }
                        } catch (Exception e) {
                            showMessageIfAnyInvalid(rateTf, amountUSDTf, amountKGSTf, dateDf);
                            refreshValidators(amountUSDTf);
                            refreshValidators(amountKGSTf);
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        showMessageIfAnyInvalid(rateTf, amountUSDTf, amountKGSTf, dateDf);
                        refreshValidators(amountUSDTf);
                        refreshValidators(amountKGSTf);
                    }
                } else {
                    refreshValidators(amountUSDTf);
                    refreshValidators(amountKGSTf);
                }
            }
        } else {
            currencyHl.setEnabled(currencySettingsOG.getValue().equals(myUI.getMessage(IndigoMessages.Manual)));
        }
    }

    private void showMessageIfAnyInvalid(TextField rateTf, TextField amountUSDTf, TextField amountKGSTf, DateField dateDf) {
        if (amountUSDTf.getValue() != null && !amountUSDTf.isValid()) {
            Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue) + ": " +
                            myUI.getMessage(IndigoMessages.AmountUSD) + " - " + amountUSDTf.getValue(),
                    Notification.Type.ERROR_MESSAGE);
            amountUSDTf.setValue(null);
        }
        if (amountKGSTf.getValue() != null && !amountKGSTf.isValid()) {
            Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue) + ": " +
                            myUI.getMessage(IndigoMessages.AmountKGS) + " - " + amountKGSTf.getValue(),
                    Notification.Type.ERROR_MESSAGE);
            amountKGSTf.setValue(null);
        }
        if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate)
                && rateTf.getValue() != null && !rateTf.isValid()) {
            Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue) + ": " + myUI.getMessage(IndigoMessages.Rate) + " - "
                    + rateTf.getValue(), Notification.Type.ERROR_MESSAGE);
            rateTf.setValue(null);
        }
        if (dateDf.getValue() != null && !dateDf.isValid()) {
            Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue) + ": " +
                            myUI.getMessage(IndigoMessages.Date) + " - " + dateDf.getValue(),
                    Notification.Type.ERROR_MESSAGE);
            dateDf.setValue(null);
        }
    }

    private void refreshValidators(TextField tf) {
        tf.removeAllValidators();
        tf.addValidator(new DoubleRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 0.01, null));
    }

    private void setGridData(int in_out) {
        try {
            DbAccTransactions dbCon = new DbAccTransactions();
            dbCon.connect();
            if (in_out == 1) {
                dbCon.execSQL(myUI, in_out, myUI.getUser().getSchool().getId(), myUI.getUser().getCurrent_year().getId(),
                        incomesGrid, this, fromDateDF.getValue(), tillDateDF.getValue());
            } else {
                dbCon.execSQL(myUI, in_out, myUI.getUser().getSchool().getId(), myUI.getUser().getCurrent_year().getId(),
                        expensesGrid, this, fromDateDF.getValue(), tillDateDF.getValue());
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

    }

    public Button createButton(String description, Object itemId, String data, boolean isDisabled, FontAwesome icon, String style) {
        Button btn = new Button();
        if (isDisabled) {
            btn.setEnabled(false);
        }
        btn.setData(data);
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.setStyleName(ValoTheme.BUTTON_LINK);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        if (style != null) {
            btn.addStyleName(style);
        }
        btn.setIcon(icon);
        btn.setId(itemId.toString());
        btn.addClickListener(this);
        return btn;
    }

    public DateField createDateField(Property.ValueChangeListener valueChangeListener) {
        DateField df = new DateField();
        df.setRangeEnd(new Date());
        df.setRequired(true);
        df.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        df.setStyleName(ValoTheme.DATEFIELD_TINY);
        df.setWidth(Settings.PERCENTS100);
        df.setDateFormat(Settings.dateTimeMinPattern);
        df.setResolution(Resolution.MINUTE);
        df.setRangeStart(myUI.getUser().getTransactions_start_date());
        if (valueChangeListener != null) {
            df.addValueChangeListener(valueChangeListener);
        }
        return df;
    }

    public ComboBox createCombobox(String dbTable, boolean isRequired, Property.ValueChangeListener valueChangeListener) {
        ComboBox cb = new ComboBox();
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        }
        cb.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (dbTable != null) {
            try {
                DbDefinition dbd = new DbDefinition();
                dbd.connect();
                cb.setContainerDataSource(dbd.exec_for_select(myUI, dbTable, true));
                dbd.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        cb.setNullSelectionAllowed(false);
        if (valueChangeListener != null) {
            cb.addValueChangeListener(valueChangeListener);
        }
        return cb;
    }

    public TextField createTextField(Validator validator, ObjectProperty<?> property, Converter<String, ?> converter,
                                     Property.ValueChangeListener valueChangeListener) {
        TextField tf = new TextField();
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        if (validator != null) {
            tf.addValidator(validator);
        }
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        if (property != null) {
            tf.addStyleName(ValoTheme.TEXTFIELD_ALIGN_RIGHT);
            tf.setPropertyDataSource(property);
            tf.setNullSettingAllowed(false);
            tf.setConverter(converter);
        }
        tf.setNullRepresentation("");
        tf.setValue(null);
        if (valueChangeListener != null) {
            tf.addValueChangeListener(valueChangeListener);
        }
        return tf;
    }

    public GeneratedPropertyContainer prepareIncomesContainer() {
        if (incomesCont == null) {
            incomesCont = new GeneratedPropertyContainer(prepareContainerProperties());
        } else {
            incomesCont.removeAllItems();
        }
        return incomesCont;
    }

    public GeneratedPropertyContainer prepareExpensesContainer() {
        if (expensesCont == null) {
            IndexedContainer cont = prepareContainerProperties();
            cont.addContainerProperty(myUI.getMessage(IndigoMessages.ToEmployee), Integer.class, null);
            expensesCont = new GeneratedPropertyContainer(cont);
        } else {
            expensesCont.removeAllItems();
        }
        return expensesCont;
    }

    private IndexedContainer prepareContainerProperties() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Settings.button, HorizontalLayout.class, null);
        container.addContainerProperty(Settings.hashTags, String.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Date), Date.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Category), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.AmountUSD), Double.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.AmountKGS), Double.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Note), String.class, null);
        container.addContainerProperty(Settings.from_employee_id, String.class, null);
        container.addContainerProperty(Settings.acc_currency_id, Integer.class, 0);
        container.addContainerProperty(Settings.is_disabled, Boolean.class, false);
        return container;
    }

    private void addGridItem() {
        String id = Settings.FreshItem + (--r_table_counter);
        if (today == null) {
            today = new Date();
        }
        Item item;
        if (accordion.getSelectedTab() == incomesGrid) {
            item = incomesCont.addItemAt(0, id);
        } else {
            item = expensesCont.addItemAt(0, id);
            item.getItemProperty(Settings.from_employee_id).setValue(myUI.getUser().getFullName());
        }
        item.getItemProperty(myUI.getMessage(IndigoMessages.Date)).setValue(today);
        item.getItemProperty(Settings.acc_currency_id).setValue(1);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).setValue(myUI.getDb_currency_rate());
    }

    private void copyItemAndDelete(String oldItemId, String itemId) {
        Container.Indexed container;
        if (accordion.getSelectedTab() == incomesGrid) {
            container = incomesCont.getWrappedContainer();
        } else {
            container = expensesCont.getWrappedContainer();
        }
        Item item = container.addItemAt(0, itemId);
        for (Object propId : container.getContainerPropertyIds()) {
            item.getItemProperty(propId).setValue(container.getItem(oldItemId).getItemProperty(propId).getValue());
        }
        container.removeItem(oldItemId);
    }

    private void execDelete(Button source) {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            if (accordion.getSelectedTab() == incomesGrid) {
                DbAccTransactions dbTr = new DbAccTransactions();
                dbTr.connect();
                double amount = 0;
                if ((Integer) incomesCont.getContainerProperty(source.getId(), Settings.acc_currency_id).getValue() == 1 &&
                        incomesCont.getContainerProperty(source.getId(), myUI.getMessage(IndigoMessages.AmountKGS)).getValue() != null) {
                    amount = Settings.round((Double) incomesCont.getContainerProperty(source.getId(),
                            myUI.getMessage(IndigoMessages.AmountKGS)).getValue()
                            / (Double) incomesCont.getContainerProperty(source.getId(),
                            myUI.getMessage(IndigoMessages.Rate)).getValue(), 2);
                } else if (incomesCont.getContainerProperty(source.getId(), myUI.getMessage(IndigoMessages.AmountUSD)).getValue() != null) {
                    amount = (Double) incomesCont.getContainerProperty(source.getId(),
                            myUI.getMessage(IndigoMessages.AmountUSD)).getValue();
                }
                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool().getId(),
                        (Date) incomesCont.getContainerProperty(source.getId(),
                                myUI.getMessage(IndigoMessages.Date)).getValue(), amount, 0.0, 1);
                if (tr != null) {
                    Notification.show(myUI.getMessage(IndigoMessages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                            + " $ (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                } else {
                    dbDef.exec_update_emp_id(Integer.parseInt(source.getId()),
                            myUI.getUser().getId(), Settings.dbAcc_transactions);
                    int st = dbDef.exec_delete(Integer.parseInt(source.getId()), Settings.dbAcc_transactions);
                    if (st != 0) {
                        Notification.show(myUI.getMessage(IndigoMessages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
                        incomesCont.removeItem(source.getId());
                        getTotals();
                        recount();
                    }
                }
                dbTr.close();
            } else {
                dbDef.exec_update_emp_id(Integer.parseInt(source.getId()),
                        myUI.getUser().getId(), Settings.dbAcc_transactions);
                int st = dbDef.exec_delete(Integer.parseInt(source.getId()), Settings.dbAcc_transactions);
                if (st != 0) {
                    Notification.show(myUI.getMessage(IndigoMessages.ValueDeleted),
                            Notification.Type.HUMANIZED_MESSAGE);
                    expensesCont.removeItem(source.getId());
                    getTotals();
                    recount();
                }
            }
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(IndigoMessages.CanNotDelete), Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private AccTransaction getTransaction(Item item, String id, int acc_type_id) {
        AccTransaction t = new AccTransaction();
        t.setId(id);
        t.setDate((Date) item.getItemProperty(myUI.getMessage(IndigoMessages.Date)).getValue());
        t.setCategory_id((Integer) item.getItemProperty(myUI.getMessage(IndigoMessages.Category)).getValue());
        t.setAccTypeId(acc_type_id);
        t.setCurrency_id((Integer) item.getItemProperty(Settings.acc_currency_id).getValue());
        t.setCurrency_rate((Double) item.getItemProperty(myUI.getMessage(IndigoMessages.Rate)).getValue());
        if (t.getCurrency_id() == 1) {
            t.setAmount((Double) item.getItemProperty(myUI.getMessage(IndigoMessages.AmountKGS)).getValue());
        } else {
            t.setAmount((Double) item.getItemProperty(myUI.getMessage(IndigoMessages.AmountUSD)).getValue());
        }
        if (item.getItemProperty(myUI.getMessage(IndigoMessages.ToEmployee)) != null &&
                item.getItemProperty(myUI.getMessage(IndigoMessages.ToEmployee)).getValue() != null) {
            t.setFrom_to_employee_id((Integer) item.getItemProperty(myUI.getMessage(IndigoMessages.ToEmployee)).getValue());
        }
        t.setNote(item.getItemProperty(myUI.getMessage(IndigoMessages.Note)).getValue().toString());
        if (item.getItemProperty(Settings.from_employee_id).getValue() != null) {
            t.setEmployee(item.getItemProperty(Settings.from_employee_id).getValue().toString());
        }
        if (item.getItemProperty(Settings.hashTags).getValue() != null) {
            t.setOrder_number(Integer.parseInt(item.getItemProperty(Settings.hashTags).getValue().toString()));
        }
        t.setEmployee_id(myUI.getUser().getId());
        t.setSchool_id(myUI.getUser().getSchool().getId());
        return t;
    }

    private void recount() {
        incomeTtlLab.setValue(myUI.getMessage(IndigoMessages.IncomesTotal) + ": " + Settings.dFormat2.format(schoolAcc.getTotal_income()) + "$");
        expenseTtlLab.setValue(myUI.getMessage(IndigoMessages.ExpensesTotal) + ": " + Settings.dFormat2.format(schoolAcc.getTotal_outcome()) + "$");
        ttlLab.setValue("<b>" + myUI.getMessage(IndigoMessages.CashBox) + ": " + Settings.dFormat2.format(
                (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome())) + "$" + "</b>");
        Calendar c = Calendar.getInstance();
        c.setTime(fromDateDF.getValue());
        c.add(Calendar.DAY_OF_MONTH, -1);
        prev_balanceLab.setValue(myUI.getMessage(IndigoMessages.PreviousBalance) + " (" + Settings.df.format(c.getTime()) + "): "
                + Settings.dFormat2.format(schoolAcc.getPrevious_balance()) + "$");
    }

    private void getTotals() {
        try {
            DbAccTransactions dbAc = new DbAccTransactions();
            dbAc.connect();
            schoolAcc = dbAc.exec_get_totals(myUI.getUser().getSchool().getId(),
                    fromDateDF.getValue(), tillDateDF.getValue(), null);
            dbAc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    @Override
    public void preCommit(FieldGroup.CommitEvent commitEvent) {
    }

    @Override
    public void postCommit(FieldGroup.CommitEvent commitEvent) {
        int status;
        Grid grid = (Grid) accordion.getSelectedTab();

        String itemId = grid.getEditedItemId().toString();
        AccTransaction tr = getTransaction(grid.getContainerDataSource().getItem(itemId),
                itemId, grid == expensesGrid ? 2 : 1);
        try {
            DbAccTransactions dbCon = new DbAccTransactions();
            dbCon.connect();
            if (itemId.contains(Settings.FreshItem)) {
                status = dbCon.exec_insert_new(tr, dbCon.getConnection());
                copyItemAndDelete(itemId, status + "");
                itemId = status + "";
            } else {
                status = dbCon.exec_update_new(tr);
            }

            if (status != 0) {
                Notification.show(myUI.getMessage(IndigoMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                Item item = grid.getContainerDataSource().getItem(itemId);
                item.getItemProperty(Settings.from_employee_id).setValue(myUI.getUser().getFullName());
                schoolAcc = dbCon.exec_get_totals(myUI.getUser().getSchool().getId(),
                        fromDateDF.getValue(), tillDateDF.getValue(), null);
                recount();
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
