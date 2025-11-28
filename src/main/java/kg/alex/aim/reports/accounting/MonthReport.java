package kg.alex.aim.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.dao.DbAccCategory;
import kg.alex.aim.dao.DbAccTransactions;
import kg.alex.aim.dao.DbCashbox;
import kg.alex.aim.domain.SchoolAccounting;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.tableexport.EnhancedFormatExcelExport;
import kg.alex.aim.utils.FormattedTreeTable;
import kg.alex.aim.utils.MyFilterDecorator;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

import java.util.*;
import java.util.Calendar;

public class MonthReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(MonthReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final Calendar fromDate = Calendar.getInstance(), tillDate = Calendar.getInstance();
    public FormattedTreeTable incomesDataTable, outcomesDataTable;
    public FilterTreeTable incomeCategoriesTable, outcomeCategoriesTable;
    private Button generateBtn, selectAllIncomesBtn, deselectAllIncomesBtn,
            selectAllOutcomesBtn, deselectAllOutcomesBtn, excelBtn;
    private DateField fromDateDF, tillDateDF;
    private ComboBox cashBoxSelect;
    private VerticalLayout rightLayout;

    public MonthReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 8);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllIncomesBtn = new Button(myUI.getMessage(Messages.AllIncomes));
        selectAllIncomesBtn.setWidth(Settings.PERCENTS100);
        selectAllIncomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllIncomesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllIncomesBtn.addClickListener(this);

        deselectAllIncomesBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllIncomesBtn.setWidth(Settings.PERCENTS100);
        deselectAllIncomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllIncomesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllIncomesBtn.addClickListener(this);

        incomeCategoriesTable = new FilterTreeTable();
        incomeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        incomeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        incomeCategoriesTable.setSizeFull();
        incomeCategoriesTable.setNullSelectionAllowed(false);
        incomeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        incomeCategoriesTable.setFilterBarVisible(true);
        incomeCategoriesTable.setFooterVisible(false);
        incomeCategoriesTable.setSelectable(true);
        incomeCategoriesTable.setNullSelectionAllowed(false);
        incomeCategoriesTable.setMultiSelect(true);
        incomeCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        incomeCategoriesTable.addValueChangeListener(this);

        selectAllOutcomesBtn = new Button(myUI.getMessage(Messages.AllOutcomes));
        selectAllOutcomesBtn.setWidth(Settings.PERCENTS100);
        selectAllOutcomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllOutcomesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllOutcomesBtn.addClickListener(this);

        deselectAllOutcomesBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllOutcomesBtn.setWidth(Settings.PERCENTS100);
        deselectAllOutcomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllOutcomesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllOutcomesBtn.addClickListener(this);

        outcomeCategoriesTable = new FilterTreeTable();
        outcomeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        outcomeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        outcomeCategoriesTable.setSizeFull();
        outcomeCategoriesTable.setNullSelectionAllowed(false);
        outcomeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        outcomeCategoriesTable.setFilterBarVisible(true);
        outcomeCategoriesTable.setFooterVisible(false);
        outcomeCategoriesTable.setSelectable(true);
        outcomeCategoriesTable.setNullSelectionAllowed(false);
        outcomeCategoriesTable.setMultiSelect(true);
        outcomeCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        outcomeCategoriesTable.addValueChangeListener(this);
        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, "2", outcomeCategoriesTable, Integer.toString(myUI.getUser().getSchool().getId()), false);
            dbac.execSQL_for_select_as_tree(myUI, "1", incomeCategoriesTable, Integer.toString(myUI.getUser().getSchool().getId()), false);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        outcomeCategoriesTable.setVisibleColumns(myUI.getMessage(Messages.Title));
        incomeCategoriesTable.setVisibleColumns(myUI.getMessage(Messages.Title));

        generateBtn = new Button(myUI.getMessage(Messages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(Messages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        fromDateDF = new DateField(myUI.getMessage(Messages.FromDate));
        fromDateDF.setRequired(true);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setResolution(Resolution.MONTH);
        fromDateDF.setDateFormat(Settings.yearMonthPattern);
        fromDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(Messages.TillDate));
        tillDateDF.setRequired(true);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setResolution(Resolution.MONTH);
        tillDateDF.setDateFormat(Settings.yearMonthPattern);
        tillDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        tillDateDF.addValueChangeListener(this);

        fromDate.setTime(fromDateDF.getValue());
        fromDate.set(Calendar.DAY_OF_MONTH, 1);
        tillDate.setTime(tillDateDF.getValue());
        tillDate.set(Calendar.DAY_OF_MONTH, tillDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        cashBoxSelect = new ComboBox(myUI.getMessage(Messages.CashBox));
        cashBoxSelect.setNullSelectionAllowed(false);
        cashBoxSelect.setRequired(true);
        cashBoxSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        cashBoxSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        cashBoxSelect.setWidth(Settings.PERCENTS100);
        cashBoxSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        cashBoxSelect.setFilteringMode(FilteringMode.CONTAINS);
        cashBoxSelect.addValueChangeListener(this);
        try {
            DbCashbox dbd = new DbCashbox();
            dbd.connect();
            cashBoxSelect.setContainerDataSource(dbd.execSQL(myUI));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        Item item = cashBoxSelect.getContainerDataSource().addItem(0);
        item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(myUI.getMessage(Messages.All));

        leftGrid.addComponent(cashBoxSelect, 0, 0, 3, 0);
        leftGrid.addComponent(fromDateDF, 0, 1, 1, 1);
        leftGrid.addComponent(tillDateDF, 2, 1, 3, 1);
        leftGrid.addComponent(selectAllIncomesBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllIncomesBtn, 2, 2, 3, 2);
        leftGrid.addComponent(incomeCategoriesTable, 0, 3, 3, 3);
        leftGrid.addComponent(selectAllOutcomesBtn, 0, 4, 1, 4);
        leftGrid.addComponent(deselectAllOutcomesBtn, 2, 4, 3, 4);
        leftGrid.addComponent(outcomeCategoriesTable, 0, 5, 3, 5);
        leftGrid.addComponent(generateBtn, 0, 6, 2, 6);
        leftGrid.addComponent(excelBtn, 3, 6);
        leftGrid.setRowExpandRatio(3, 1);
        leftGrid.setRowExpandRatio(5, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        rightLayout = new VerticalLayout();
        rightLayout.setMargin(true);
        rightLayout.setSpacing(true);
        rightLayout.setSizeFull();
        rightLayout.setImmediate(true);

        incomesDataTable = new FormattedTreeTable(myUI);
        incomesDataTable.setCaption(myUI.getMessage(Messages.Incomes));
        incomesDataTable.setFooterVisible(true);
        incomesDataTable.setSizeFull();
        incomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        incomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        incomesDataTable.addStyleName("noWrapHeader");
        incomesDataTable.addStyleName("noWrapFooter");

        outcomesDataTable = new FormattedTreeTable(myUI);
        outcomesDataTable.setCaption(myUI.getMessage(Messages.Expenses));
        outcomesDataTable.setFooterVisible(true);
        outcomesDataTable.setSizeFull();
        outcomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        outcomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        outcomesDataTable.addStyleName("noWrapHeader");
        outcomesDataTable.addStyleName("noWrapFooter");

        splitPanel.setSecondComponent(rightLayout);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromDateDF.isValid() && tillDateDF.isValid() && cashBoxSelect.isValid()) {
                if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()
                        || !((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                    rightLayout.removeAllComponents();
                    try {
                        DbAccTransactions dbTr = new DbAccTransactions();
                        dbTr.connect();
                        if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                            rightLayout.addComponent(incomesDataTable);
                            rightLayout.setExpandRatio(incomesDataTable, 1);
                            dbTr.execSQL_by_months(myUI, 1,
                                    (Integer) cashBoxSelect.getValue(),
                                    myUI.getUser().getSchool().getId(),
                                    incomeCategoriesTable, fromDate, tillDate, incomesDataTable);
                            if (incomesDataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                            }
                        }
                        if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                            rightLayout.addComponent(outcomesDataTable);
                            rightLayout.setExpandRatio(outcomesDataTable, 1);
                            dbTr.execSQL_by_months(myUI, 2,
                                    (Integer) cashBoxSelect.getValue(),
                                    myUI.getUser().getSchool().getId(),
                                    outcomeCategoriesTable, fromDate, tillDate, outcomesDataTable);
                            if (outcomesDataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                            }
                        }
                        Calendar current = Calendar.getInstance();
                        Calendar prev_date = Calendar.getInstance();
                        current.setTime(fromDate.getTime());
                        Set<Integer> catIds = new HashSet<>();
                        while (current.before(tillDate)) {
                            prev_date.setTime(current.getTime());
                            prev_date.add(Calendar.DATE, -1);
                            SchoolAccounting scAcc = dbTr.exec_get_totals(myUI.getUser().getSchool().getId(),
                                    (Integer) cashBoxSelect.getValue(), current.getTime(),
                                    tillDate.getTime(), Settings.convertCollectionToStr(catIds));
                            incomesDataTable.setColumnFooter(
                                    Settings.ymdf.format(current.getTime()), myUI.getMessage(Messages.Saldo) + " ("
                                            + Settings.df.format(prev_date.getTime()) + "): " + Settings.dFormat2.format(scAcc.getPrevious_balance())
                                            + getCurrency() + "; " + myUI.getMessage(Messages.Total)
                                            + ": " + incomesDataTable.getColumnFooter(Settings.ymdf.format(current.getTime())) + getCurrency());
                            outcomesDataTable.setColumnFooter(Settings.ymdf.format(current.getTime()),
                                    outcomesDataTable.getColumnFooter(Settings.ymdf.format(current.getTime())) + getCurrency());
                            incomesDataTable.setColumnAlignment(Settings.ymdf.format(current.getTime()), Table.Align.RIGHT);
                            outcomesDataTable.setColumnAlignment(Settings.ymdf.format(current.getTime()), Table.Align.RIGHT);
                            current.add(Calendar.MONTH, 1);
                        }
                        incomesDataTable.setColumnFooter(myUI.getMessage(Messages.Total),
                                incomesDataTable.getColumnFooter(myUI.getMessage(Messages.Total)) + getCurrency());
                        outcomesDataTable.setColumnFooter(myUI.getMessage(Messages.Total),
                                outcomesDataTable.getColumnFooter(myUI.getMessage(Messages.Total)) + getCurrency());
                        incomesDataTable.setColumnAlignment(myUI.getMessage(Messages.Total), Table.Align.RIGHT);
                        outcomesDataTable.setColumnAlignment(myUI.getMessage(Messages.Total), Table.Align.RIGHT);
                        dbTr.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
            }
        } else if (source == excelBtn) {
            try {
                EnhancedFormatExcelExport excelReport;
                if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()
                        && !((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                    if (incomesDataTable.getContainerDataSource().size() != 0
                            && outcomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(Messages.Incomes));
                        excelReport.setReportTitle(incomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator<?> iter = incomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(incomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.setNextTable(outcomesDataTable, myUI.getMessage(Messages.Expenses));
                        excelReport.setReportTitle(outcomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        i = 1;
                        iter = outcomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(outcomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    } else if (incomesDataTable.getContainerDataSource().size() != 0
                            && outcomesDataTable.getContainerDataSource().size() == 0) {
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(Messages.Incomes));
                        excelReport.setReportTitle(incomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator<?> iter = incomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(incomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    } else if (incomesDataTable.getContainerDataSource().size() == 0
                            && outcomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(outcomesDataTable, myUI.getMessage(Messages.Expenses));
                        excelReport.setReportTitle(outcomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator<?> iter = outcomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(outcomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    }
                } else if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                    if (outcomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(outcomesDataTable, myUI.getMessage(Messages.Expenses));
                        excelReport.setReportTitle(outcomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator<?> iter = outcomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(outcomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    }
                } else if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                    if (incomesDataTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(Messages.Incomes));
                        excelReport.setReportTitle(incomesDataTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        int i = 1;
                        Iterator<?> iter = incomesDataTable.getContainerPropertyIds().iterator();
                        if (iter.hasNext()) {
                            iter.next();
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            excelReport.getTotalsRow().getCell(i).setCellValue(incomesDataTable.getColumnFooter(next));
                            i++;
                        }
                        excelReport.sendConverted();
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllIncomesBtn) {
            incomeCategoriesTable.setValue(incomeCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllIncomesBtn) {
            incomeCategoriesTable.setValue(null);
        } else if (source == selectAllOutcomesBtn) {
            outcomeCategoriesTable.setValue(outcomeCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllOutcomesBtn) {
            outcomeCategoriesTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == incomeCategoriesTable || property == outcomeCategoriesTable
                    || property == fromDateDF || property == tillDateDF || property == cashBoxSelect) {
                excelBtn.setEnabled(false);
                rightLayout.removeAllComponents();
            }
        }
        if ((property == fromDateDF || property == tillDateDF) && fromDateDF.isValid() && tillDateDF.isValid()) {
            fromDate.setTime(fromDateDF.getValue());
            fromDate.set(Calendar.DAY_OF_MONTH, 1);
            tillDate.setTime(tillDateDF.getValue());
            tillDate.set(Calendar.DAY_OF_MONTH, tillDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
    }

    private String getCurrency() {
        Object currency = cashBoxSelect.getContainerProperty(cashBoxSelect.getValue(),
                myUI.getMessage(Messages.Currency)).getValue();
        return currency == null ? " " + Settings.KGS : " " + currency;
    }
}
