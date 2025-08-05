package kg.alex.ellipse.ui;

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
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.dao.DbAccTransactions;
import kg.alex.ellipse.dao.DbCurrencyRate;
import kg.alex.ellipse.dao.DbDefinition;
import kg.alex.ellipse.dao.DbEmployee;
import kg.alex.ellipse.domain.AccTransaction;
import kg.alex.ellipse.domain.CurrencyRate;
import kg.alex.ellipse.domain.SchoolAccounting;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.pdf.TransactionInvoicePDF;
import kg.alex.ellipse.utils.ValueFromContainerConverter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CashBoxView extends GridLayout implements Button.ClickListener,
        Property.ValueChangeListener, FieldGroup.CommitHandler {
    static final Logger logger = LogManager.getLogger(CashBoxView.class);
    private final MyVaadinUI myUI;
    private final Grid expensesGrid, incomesGrid;
    private final Accordion accordion;
    private final Subject currentUser = SecurityUtils.getSubject();
    public SchoolAccounting schoolAcc;
    private Button addButton, saveButton, searchButton;
    private OptionGroup currencySettingsOG, cashBoxesOG;
    private TextField currencyTF;
    private Label incomeTtlLab, expenseTtlLab, ttlLab, prev_balanceLab;
    private DateField fromDateDF, tillDateDF;
    private GeneratedPropertyContainer incomesCont = null, expensesCont = null;
    private int r_table_counter = 1000;
    private HorizontalLayout currencyHl;
    private ComboBox expensesCategoryCb, incomesCategoryCb, toEmployeesCb;
    private final Date today = new Date();
    private Item editedItem;
    private final Map<String, Container.Filter> filters = new HashMap<>();

    public CashBoxView(MyVaadinUI myUI) {
        this.myUI = myUI;

        setRows(3);
        setColumns(5);
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
        TabSheet.Tab tab = accordion.addTab(incomesGrid, myUI.getMessage(Messages.Incomes));
        tab.setId("1");
        tab = accordion.addTab(expensesGrid, myUI.getMessage(Messages.Expenses));
        tab.setId("2");
        accordion.setSelectedTab(expensesGrid);
        accordion.addSelectedTabChangeListener((TabSheet.SelectedTabChangeListener)
                event -> setGridData(Integer.parseInt(accordion.getSelectedTab().getId())));
        addComponent(accordion, 0, 2, 4, 2);
        setRowExpandRatio(2, 1);
        getTotals();
        recount();
    }

    private void buildLabels() {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);
        hl.setSpacing(true);

        fromDateDF = new DateField();
        fromDateDF.setDescription(myUI.getMessage(Messages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(today);
        fromDateDF.setEnabled(currentUser.isPermitted(
                Settings.cnTransactionsView + ":" + Settings.prmSearchByDates));

        tillDateDF = new DateField();
        tillDateDF.setDescription(myUI.getMessage(Messages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(today);
        tillDateDF.setEnabled(currentUser.isPermitted(
                Settings.cnTransactionsView + ":" + Settings.prmSearchByDates));

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

        addButton = new Button(myUI.getMessage(Messages.AddRecord));
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
        currencyLab.setValue(myUI.getMessage(Messages.RateUSD));

        currencyTF = new TextField();
        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        currencyTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        currencyTF.setWidth(Settings.PERCENTS100);
        currencyTF.setNullRepresentation("0.01");
        currencyTF.setNullSettingAllowed(false);
        currencyTF.setConverter(Settings.getStringToDoubleConverter(4));
        currencyTF.addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 1.0, null));
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
        currencySettingsOG.addItem(myUI.getMessage(Messages.NbkrCurrency));
        currencySettingsOG.addItem(myUI.getMessage(Messages.Manual));
        currencySettingsOG.addValueChangeListener(this);
        if (myUI.isManualRate()) {
            currencySettingsOG.select(myUI.getMessage(Messages.Manual));
        } else {
            currencySettingsOG.select(myUI.getMessage(Messages.NbkrCurrency));
        }

        saveButton = new Button();
        saveButton.setDescription(myUI.getMessage(Messages.SaveButton));
        saveButton.addStyleName(ValoTheme.BUTTON_SMALL);
        saveButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveButton.setIcon(FontAwesome.SAVE);
        saveButton.addClickListener(this);

        currencySettingsHl.addComponent(currencySettingsOG);
        currencySettingsHl.addComponent(saveButton);
        currencySettingsHl.setExpandRatio(currencySettingsOG, 1);

        cashBoxesOG = new OptionGroup();
        cashBoxesOG.setWidth(Settings.PERCENTS100);
        cashBoxesOG.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            cashBoxesOG.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbAccCashBox, false));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cashBoxesOG.setValue(1);
        cashBoxesOG.addValueChangeListener((Property.ValueChangeListener) event -> {
            if (fromDateDF.getValue() != null && tillDateDF.getValue() != null) {
                setGridData(Integer.parseInt(accordion.getSelectedTab().getId()));
                getTotals();
                recount();
            }
        });

        addComponent(hl, 0, 0);
        addComponent(addButton, 0, 1);
        addComponent(cashBoxesOG, 1, 0, 1, 1);
        addComponent(prev_balanceLab, 2, 0);
        addComponent(incomeTtlLab, 2, 1);
        addComponent(expenseTtlLab, 3, 1);
        addComponent(ttlLab, 3, 0);
        addComponent(currencyHl, 4, 0);
        addComponent(currencySettingsHl, 4, 1);
        setComponentAlignment(cashBoxesOG, Alignment.MIDDLE_CENTER);
        setComponentAlignment(addButton, Alignment.BOTTOM_RIGHT);
        setComponentAlignment(ttlLab, Alignment.MIDDLE_RIGHT);
        setComponentAlignment(prev_balanceLab, Alignment.BOTTOM_LEFT);
        setColumnExpandRatio(0, 2.2f);
        setColumnExpandRatio(1, 0.4f);
        setColumnExpandRatio(2, 4);
        setColumnExpandRatio(3, 3.8f);
        setColumnExpandRatio(4, 2);
    }

    private void buildGrid(Grid grid) {
        int in_out_id = grid == expensesGrid ? 2 : 1;
        grid.setId(in_out_id + "");
        setGridData(in_out_id);
        grid.setSizeFull();
        grid.setEditorEnabled(false);
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

                DateField dateDf = (DateField) grid.getColumn(myUI.getMessage(Messages.Date)).getEditorField();
                if (!currentUser.isPermitted(Settings.cnTransactionsView + ":"
                                             + Settings.prmChangeOldTransactions)) {
                    if (grid.getContainerDataSource().getContainerProperty(event.getItemId(),
                            myUI.getMessage(Messages.Date)).getValue() != null) {
                        dateDf.setRangeStart((Date) grid.getContainerDataSource().getContainerProperty(event.getItemId(),
                                myUI.getMessage(Messages.Date)).getValue());
                    } else {
                        dateDf.setRangeStart(today);
                    }
                } else {
                    dateDf.setRangeStart(null);
                }
                TextField amountTf = (TextField) grid.getColumn(myUI.getMessage(Messages.Amount)).getEditorField();
                refreshValidators(amountTf);
                amountTf.setRequired(true);
            } else {
                grid.setEditorEnabled(false);
            }
        });
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.getColumn(Settings.from_employee_id).setHidden(true);
        grid.getColumn(Settings.is_disabled).setHidden(true);
        if (grid == expensesGrid) {
            if (expensesCategoryCb == null) {
                expensesCategoryCb = createCombobox(null, true, null);
                try {
                    DbAccTransactions dbac = new DbAccTransactions();
                    dbac.connect();
                    expensesCategoryCb.setContainerDataSource(dbac.exec_for_select(myUI, 2, myUI.getUser().getSchool().getId()));
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(Messages.Category)).setEditorField(expensesCategoryCb);
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
            grid.getColumn(myUI.getMessage(Messages.ToEmployee)).setEditorField(toEmployeesCb);
        } else {
            if (incomesCategoryCb == null) {
                incomesCategoryCb = createCombobox(null, true, null);
                try {
                    DbAccTransactions dbac = new DbAccTransactions();
                    dbac.connect();
                    incomesCategoryCb.setContainerDataSource(dbac.exec_for_select(myUI, 1, myUI.getUser().getSchool().getId()));
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
            grid.getColumn(myUI.getMessage(Messages.Category)).setEditorField(incomesCategoryCb);
        }
        grid.getColumn(myUI.getMessage(Messages.Date)).setEditorField(createDateField(this));
        grid.getColumn(myUI.getMessage(Messages.Amount)).setEditorField(createTextField(
                new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(2), this));
        grid.getColumn(myUI.getMessage(Messages.Rate)).setEditorField(createTextField(
                new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 1.0, null),
                new ObjectProperty<>(0.0), Settings.getStringToDoubleConverter(4), this));
        grid.getColumn(myUI.getMessage(Messages.Note)).setEditorField(createTextField(
                new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue), 1, 350, false),
                null, null, null));

        grid.getColumn(Settings.hashTags).setEditable(false);
        grid.getColumn(Settings.button).setEditable(false);
        grid.getColumn(Settings.from_employee_id).setEditable(false);
        grid.getColumn(Settings.is_disabled).setEditable(false);
        grid.getColumn(myUI.getMessage(Messages.Rate)).setEditable(
                currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmChangeCurrencyRate));
        grid.setEditorSaveCaption(myUI.getMessage(Messages.SaveButton));
        grid.setEditorCancelCaption(myUI.getMessage(Messages.Cancel));

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

        grid.getColumn(myUI.getMessage(Messages.Date)).setRenderer(new DateRenderer(Settings.dtmf));
        grid.getColumn(myUI.getMessage(Messages.Amount)).setRenderer(
                new NumberRenderer(Settings.getNumberFormat(2)));
        grid.getColumn(myUI.getMessage(Messages.Rate)).setRenderer(
                new NumberRenderer(Settings.getNumberFormat(4)));
        if (grid == expensesGrid) {
            grid.getColumn(myUI.getMessage(Messages.Category)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) expensesCategoryCb.getContainerDataSource(),
                            myUI.getMessage(Messages.Category), myUI));
            grid.getColumn(myUI.getMessage(Messages.ToEmployee)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) toEmployeesCb.getContainerDataSource(),
                            myUI.getMessage(Messages.ToEmployee), myUI));
        } else {
            grid.getColumn(myUI.getMessage(Messages.Category)).setRenderer(
                    new HtmlRenderer(), new ValueFromContainerConverter((IndexedContainer) incomesCategoryCb.getContainerDataSource(),
                            myUI.getMessage(Messages.Category), myUI));
        }
        grid.getColumn(Settings.button).setRenderer(new ComponentRenderer());

        grid.getColumn(Settings.button).setWidth(52);
        grid.getColumn(Settings.hashTags).setWidth(60);
        grid.getColumn(myUI.getMessage(Messages.Date)).setWidth(133);
        grid.getColumn(myUI.getMessage(Messages.Rate)).setWidth(100);
        grid.getColumn(myUI.getMessage(Messages.Amount)).setWidth(105);
        grid.getColumn(myUI.getMessage(Messages.Category)).setMinimumWidth(300);
        grid.getColumn(myUI.getMessage(Messages.Category)).setExpandRatio(1);
        if (grid.getColumn(myUI.getMessage(Messages.ToEmployee)) != null) {
            grid.getColumn(myUI.getMessage(Messages.ToEmployee)).setMinimumWidth(230);
            grid.getColumn(myUI.getMessage(Messages.ToEmployee)).setExpandRatio(1);
        }
        grid.getColumn(myUI.getMessage(Messages.Note)).setMinimumWidth(300);
        grid.getColumn(myUI.getMessage(Messages.Note)).setMaximumWidth(400);
        grid.getColumn(myUI.getMessage(Messages.Note)).setExpandRatio(1);
        grid.getEditorFieldGroup().addCommitHandler(this);

        Grid.HeaderRow filterRow = grid.appendHeaderRow();
        for (Object pid : grid.getContainerDataSource().getContainerPropertyIds()) {
            if (grid.getColumn(pid).getEditorField() instanceof TextField) {
                Grid.HeaderCell cell = filterRow.getCell(pid);

                TextField filterField = new TextField();
                filterField.setStyleName(ValoTheme.TEXTFIELD_TINY);
                filterField.setWidth(Settings.PERCENTS100);
                filterField.setInputPrompt(myUI.getMessage(Messages.Filter));

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
                filterField.setInputPrompt(myUI.getMessage(Messages.Filter));
                try {
                    if (grid == expensesGrid && pid.equals(myUI.getMessage(Messages.Category))) {
                        DbAccTransactions dbac = new DbAccTransactions();
                        dbac.connect();
                        filterField.setContainerDataSource(dbac.exec_for_select(myUI, 2, myUI.getUser().getSchool().getId()));
                        dbac.close();
                    } else if (grid == incomesGrid && pid.equals(myUI.getMessage(Messages.Category))) {
                        DbAccTransactions dbac = new DbAccTransactions();
                        dbac.connect();
                        filterField.setContainerDataSource(dbac.exec_for_select(myUI, 1, myUI.getUser().getSchool().getId()));
                        dbac.close();
                    } else if (pid.equals(myUI.getMessage(Messages.ToEmployee))) {
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
                    if (currencySettingsOG.getValue().equals(myUI.getMessage(Messages.Manual))) {
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
                    Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(Messages.NotificationWrongValue), Notification.Type.WARNING_MESSAGE);
            }
        } else if (source.getData().equals(myUI.getMessage(Messages.Print)) && !source.getId().contains(Settings.FreshItem)) {
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
                orderName = myUI.getMessage(Messages.IncomeOrder);
            } else {
                item = expensesGrid.getContainerDataSource().getItem(source.getId());
                acc_type_id = 2;
                tr = getTransaction(item, source.getId(), acc_type_id);
                categoryCb = expensesCategoryCb;
                orderName = myUI.getMessage(Messages.ExpenseOrder);
                if (item.getItemProperty(myUI.getMessage(Messages.ToEmployee)).getValue() != null
                    && (Integer) item.getItemProperty(myUI.getMessage(Messages.ToEmployee)).getValue() != 0) {
                    tr.setFrom_to_employee(toEmployeesCb.getContainerProperty(
                            item.getItemProperty(myUI.getMessage(Messages.ToEmployee)).getValue(),
                            myUI.getMessage(Messages.Title)).getValue().toString());
                }
            }
            tr.setId(source.getId());
            tr.setCategory(categoryCb.getContainerProperty(
                    item.getItemProperty(myUI.getMessage(Messages.Category)).getValue(),
                    myUI.getMessage(Messages.Title)).getValue().toString());
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
        } else if (source.getData().equals(myUI.getMessage(Messages.DeleteButton))) {
            if (source.getId().contains(Settings.FreshItem)) {
                ((Grid) accordion.getSelectedTab()).getContainerDataSource().removeItem(source.getId());
            } else {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmDeletion),
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
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
            if (grid != null && grid.isEditorActive() && grid.getEditedItemId() != null) {
                String itemId = grid.getEditedItemId().toString();
                Item item = grid.getContainerDataSource().getItem(itemId);
                TextField amountTf = (TextField) grid.getEditorFieldGroup().getField(myUI.getMessage(Messages.Amount));
                DateField dateDf = (DateField) grid.getEditorFieldGroup().getField(myUI.getMessage(Messages.Date));
                if (accordion.getSelectedTab() == expensesGrid) {
                    if (amountTf.getValue() != null && dateDf.getValue() != null) {
                        try {
                            double amount = Settings.dFormat2.parse(amountTf.getValue()).doubleValue();
                            if (amount > 0.0) {
                                double old_amount = 0;
                                if (item.getItemProperty(myUI.getMessage(Messages.Amount)).getValue() != null) {
                                    old_amount = (Double) item.getItemProperty(myUI.getMessage(Messages.Amount)).getValue();
                                }
                                DbAccTransactions dbTr = new DbAccTransactions();
                                dbTr.connect();
                                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool().getId(), (Integer) cashBoxesOG.getValue(),
                                        dateDf.getValue(), old_amount, amount, 2);
                                amountTf.removeAllValidators();
                                if (tr != null) {
                                    double limit = tr.getLimit();
                                    amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                                                   + " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue())
                                                                                   + " (" + Settings.df.format(tr.getDate()) + ")", 0.01, Settings.round(limit, 2)));
                                    Notification.show(myUI.getMessage(Messages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                      + " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue())
                                                      + " (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                                } else {
                                    refreshValidators(amountTf);
                                }
                                dbTr.close();
                            } else {
                                refreshValidators(amountTf);
                            }
                        } catch (Exception e) {
                            showMessageIfAnyInvalid(amountTf, dateDf);
                            refreshValidators(amountTf);
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        showMessageIfAnyInvalid(amountTf, dateDf);
                        refreshValidators(amountTf);
                    }
                } else if (accordion.getSelectedTab() == incomesGrid && !itemId.contains(Settings.FreshItem)) {
                    if (amountTf.getValue() != null && dateDf.getValue() != null) {
                        try {
                            double amount = Settings.dFormat2.parse(amountTf.getValue()).doubleValue();
                            if (amount > 0.0) {
                                double old_amount = (Double) item.getItemProperty(myUI.getMessage(Messages.Amount)).getValue();
                                if (amount <= old_amount || DateUtils.truncate(dateDf.getValue(), Calendar.DAY_OF_MONTH)
                                                                    .compareTo((Date) item.getItemProperty(myUI.getMessage(Messages.Date)).getValue()) != 0) {
                                    DbAccTransactions dbTr = new DbAccTransactions();
                                    dbTr.connect();
                                    AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool().getId(),
                                            (Integer) cashBoxesOG.getValue(), dateDf.getValue(), old_amount, amount, 1);
                                    amountTf.removeAllValidators();
                                    if (tr != null) {
                                        double limit = tr.getLimit();
                                        amountTf.addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                                                       + " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue()) +
                                                                                       " (" + Settings.df.format(tr.getDate()) + ")", Settings.round(limit, 2), null));
                                        Notification.show(myUI.getMessage(Messages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                          + " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue()) +
                                                          " (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                                    } else {
                                        refreshValidators(amountTf);
                                    }
                                    dbTr.close();
                                } else {
                                    refreshValidators(amountTf);
                                }

                            } else {
                                refreshValidators(amountTf);
                            }
                        } catch (Exception e) {
                            showMessageIfAnyInvalid(amountTf, dateDf);
                            refreshValidators(amountTf);
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        showMessageIfAnyInvalid(amountTf, dateDf);
                        refreshValidators(amountTf);
                    }
                } else {
                    refreshValidators(amountTf);
                }
            }
        } else {
            currencyHl.setEnabled(currencySettingsOG.getValue().equals(myUI.getMessage(Messages.Manual)));
        }
    }

    private void showMessageIfAnyInvalid(TextField amountTf, DateField dateDf) {
        if (amountTf.getValue() != null && !amountTf.isValid()) {
            Notification.show(myUI.getMessage(Messages.NotificationWrongValue) + ": " +
                              myUI.getMessage(Messages.Amount) + " - " + amountTf.getValue(),
                    Notification.Type.ERROR_MESSAGE);
            amountTf.setValue(null);
        }
        if (dateDf.getValue() != null && !dateDf.isValid()) {
            Notification.show(myUI.getMessage(Messages.NotificationWrongValue) + ": " +
                              myUI.getMessage(Messages.Date) + " - " + dateDf.getValue(),
                    Notification.Type.ERROR_MESSAGE);
            dateDf.setValue(null);
        }
    }

    private void refreshValidators(TextField tf) {
        tf.removeAllValidators();
        tf.addValidator(new DoubleRangeValidator(myUI.getMessage(Messages.NotificationWrongValue), 0.01, null));
    }

    private void setGridData(int in_out) {
        try {
            DbAccTransactions dbCon = new DbAccTransactions();
            dbCon.connect();
            if (in_out == 1) {
                dbCon.execSQL(myUI, in_out, myUI.getUser().getSchool().getId(), (Integer) cashBoxesOG.getValue(),
                        myUI.getUser().getCurrent_year().getId(),
                        incomesGrid, this, fromDateDF.getValue(), tillDateDF.getValue());
            } else {
                dbCon.execSQL(myUI, in_out, myUI.getUser().getSchool().getId(), (Integer) cashBoxesOG.getValue(),
                        myUI.getUser().getCurrent_year().getId(),
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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.with(LocalTime.of(23, 59));
        Date date = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
        df.setRangeEnd(date);
        df.setRequired(true);
        df.setRequiredError(myUI.getMessage(Messages.RequiredField));
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
            cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
        }
        cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
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
        tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
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
            cont.addContainerProperty(myUI.getMessage(Messages.ToEmployee), Integer.class, null);
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
        container.addContainerProperty(myUI.getMessage(Messages.Date), Date.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Category), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Rate), Double.class, 0.0);
        container.addContainerProperty(myUI.getMessage(Messages.Amount), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Note), String.class, null);
        container.addContainerProperty(Settings.from_employee_id, String.class, null);
        container.addContainerProperty(Settings.is_disabled, Boolean.class, false);
        return container;
    }

    private void addGridItem() {
        String id = Settings.FreshItem + (--r_table_counter);
        Item item;
        if (accordion.getSelectedTab() == incomesGrid) {
            item = incomesCont.addItemAt(0, id);
        } else {
            item = expensesCont.addItemAt(0, id);
            item.getItemProperty(Settings.from_employee_id).setValue(myUI.getUser().getFullName());
        }
        item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(today);
        item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(myUI.getDb_currency_rate());
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
                double amount = (Double) incomesCont.getContainerProperty(source.getId(),
                        myUI.getMessage(Messages.Amount)).getValue();
                AccTransaction tr = dbTr.exec_low_balance(dbTr.getConnection(), myUI.getUser().getSchool().getId(),
                        (Integer) cashBoxesOG.getValue(), (Date) incomesCont.getContainerProperty(source.getId(),
                                myUI.getMessage(Messages.Date)).getValue(), amount, 0.0, 1);
                if (tr != null) {
                    Notification.show(myUI.getMessage(Messages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                      + " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue()) +
                                      " (" + Settings.df.format(tr.getDate()) + ")", Notification.Type.ERROR_MESSAGE);
                } else {
                    dbDef.exec_update_emp_id(Integer.parseInt(source.getId()),
                            myUI.getUser().getId(), Settings.dbAcc_transactions);
                    int st = dbDef.exec_delete(Integer.parseInt(source.getId()), Settings.dbAcc_transactions);
                    if (st != 0) {
                        Notification.show(myUI.getMessage(Messages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
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
                    Notification.show(myUI.getMessage(Messages.ValueDeleted),
                            Notification.Type.HUMANIZED_MESSAGE);
                    expensesCont.removeItem(source.getId());
                    getTotals();
                    recount();
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

    private AccTransaction getTransaction(Item item, String id, int acc_type_id) {
        AccTransaction t = new AccTransaction();
        t.setId(id);
        t.setDate((Date) item.getItemProperty(myUI.getMessage(Messages.Date)).getValue());
        t.setCategory_id((Integer) item.getItemProperty(myUI.getMessage(Messages.Category)).getValue());
        t.setAccTypeId(acc_type_id);
        t.setCashbox_id((Integer) cashBoxesOG.getValue());
        t.setCurrency_rate((Double) item.getItemProperty(myUI.getMessage(Messages.Rate)).getValue());
        t.setAmount((Double) item.getItemProperty(myUI.getMessage(Messages.Amount)).getValue());
        if (item.getItemProperty(myUI.getMessage(Messages.ToEmployee)) != null &&
            item.getItemProperty(myUI.getMessage(Messages.ToEmployee)).getValue() != null) {
            t.setFrom_to_employee_id((Integer) item.getItemProperty(myUI.getMessage(Messages.ToEmployee)).getValue());
        }
        t.setNote(item.getItemProperty(myUI.getMessage(Messages.Note)).getValue().toString());
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
        incomeTtlLab.setValue(myUI.getMessage(Messages.IncomesTotal) + ": "
                              + Settings.dFormat2.format(schoolAcc.getTotal_income()) +
                              " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue()));
        expenseTtlLab.setValue(myUI.getMessage(Messages.ExpensesTotal) + ": "
                               + Settings.dFormat2.format(schoolAcc.getTotal_outcome()) +
                               " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue()));
        ttlLab.setValue("<b>" + myUI.getMessage(Messages.CashBox) + ": " + Settings.dFormat2.format(
                (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome())) +
                        " " + cashBoxesOG.getItemCaption(cashBoxesOG.getValue()) + "</b>");
        Calendar c = Calendar.getInstance();
        c.setTime(fromDateDF.getValue());
        c.add(Calendar.DAY_OF_MONTH, -1);
        prev_balanceLab.setValue(myUI.getMessage(Messages.PreviousBalance) + " (" + Settings.df.format(c.getTime()) + "): "
                                 + Settings.dFormat2.format(schoolAcc.getPrevious_balance()) + " "
                                 + cashBoxesOG.getItemCaption(cashBoxesOG.getValue()));
    }

    private void getTotals() {
        try {
            DbAccTransactions dbAc = new DbAccTransactions();
            dbAc.connect();
            schoolAcc = dbAc.exec_get_totals(myUI.getUser().getSchool().getId(),
                    (Integer) cashBoxesOG.getValue(),
                    fromDateDF.getValue(), tillDateDF.getValue(), null);
            dbAc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    @Override
    public void preCommit(FieldGroup.CommitEvent commitEvent) {
        Grid grid = (Grid) accordion.getSelectedTab();
        editedItem = grid.getContainerDataSource().getItem(grid.getEditedItemId().toString());
    }

    @Override
    public void postCommit(FieldGroup.CommitEvent commitEvent) {
        int status;
        Grid grid = (Grid) accordion.getSelectedTab();

        String itemId = grid.getEditedItemId().toString();
        AccTransaction tr = getTransaction(editedItem, itemId, grid == expensesGrid ? 2 : 1);
        try {
            DbAccTransactions dbCon = new DbAccTransactions();
            dbCon.connect();
            if (itemId.contains(Settings.FreshItem)) {
                status = dbCon.exec_insert(tr, dbCon.getConnection());
                copyItemAndDelete(itemId, status + "");
                itemId = status + "";
                editedItem = grid.getContainerDataSource().getItem(itemId);
            } else {
                status = dbCon.exec_update(tr);
            }

            if (status != 0) {
                Notification.show(myUI.getMessage(Messages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                editedItem.getItemProperty(Settings.from_employee_id).setValue(myUI.getUser().getFullName());
                schoolAcc = dbCon.exec_get_totals(myUI.getUser().getSchool().getId(),
                        (Integer) cashBoxesOG.getValue(),
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
