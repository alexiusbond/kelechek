/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbAccCategory;
import kg.alex.indigo.dao.DbAccTransactions;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.domain.SchoolAccounting;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTreeTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.tepi.filtertable.FilterTreeTable;

import java.util.Calendar;
import java.util.*;

public class MonthReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(MonthReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, selectAllIncomesBtn, deselectAllIncomesBtn, selectAllSchoolsBtn, deselectAllSchoolsBtn,
            selectAllOutcomesBtn, deselectAllOutcomesBtn, excelBtn;
    private final HorizontalSplitPanel splitPanel;
    private DateField fromDateDF, tillDateDF;
    private ComboBox cashBoxSelect;
    public FormattedTreeTable incomesDataTable, outcomesDataTable;
    public FilterTreeTable incomeCategoriesTable, outcomeCategoriesTable;
    public FilterTable schoolsTable;

    private VerticalLayout rightLayout;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final Calendar fromDate = Calendar.getInstance(), tillDate = Calendar.getInstance();

    public MonthReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 9);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllIncomesBtn = new Button(myUI.getMessage(IndigoMessages.AllIncomes));
        selectAllIncomesBtn.setWidth(Settings.PERCENTS100);
        selectAllIncomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllIncomesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllIncomesBtn.addClickListener(this);

        deselectAllIncomesBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
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

        selectAllSchoolsBtn = new Button(myUI.getMessage(IndigoMessages.AllSchools));
        selectAllSchoolsBtn.setWidth(Settings.PERCENTS100);
        selectAllSchoolsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllSchoolsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllSchoolsBtn.addClickListener(this);

        deselectAllSchoolsBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
        deselectAllSchoolsBtn.setWidth(Settings.PERCENTS100);
        deselectAllSchoolsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllSchoolsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllSchoolsBtn.addClickListener(this);

        schoolsTable = new FilterTable();
        schoolsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolsTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolsTable.setSizeFull();
        schoolsTable.setNullSelectionAllowed(false);
        schoolsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolsTable.setFilterBarVisible(true);
        schoolsTable.setFooterVisible(false);
        schoolsTable.setSelectable(true);
        schoolsTable.setNullSelectionAllowed(false);
        schoolsTable.setMultiSelect(true);
        schoolsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolsTable.addValueChangeListener(this);

        selectAllOutcomesBtn = new Button(myUI.getMessage(IndigoMessages.AllOutcomes));
        selectAllOutcomesBtn.setWidth(Settings.PERCENTS100);
        selectAllOutcomesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllOutcomesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllOutcomesBtn.addClickListener(this);

        deselectAllOutcomesBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
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
        if (!currentUser.hasRole(Settings.rnAdmin)) {
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
            outcomeCategoriesTable.setVisibleColumns(myUI.getMessage(IndigoMessages.Title));
            incomeCategoriesTable.setVisibleColumns(myUI.getMessage(IndigoMessages.Title));
        }
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolsTable.setContainerDataSource(dbs.execSchoolSel(myUI, 0));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        schoolsTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});

        generateBtn = new Button(myUI.getMessage(IndigoMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        fromDateDF = new DateField(myUI.getMessage(IndigoMessages.FromDate));
        fromDateDF.setRequired(true);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setResolution(Resolution.MONTH);
        fromDateDF.setDateFormat(Settings.yearMonthPattern);
        fromDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(IndigoMessages.TillDate));
        tillDateDF.setRequired(true);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setResolution(Resolution.MONTH);
        tillDateDF.setDateFormat(Settings.yearMonthPattern);
        tillDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        tillDateDF.addValueChangeListener(this);

        fromDate.setTime(fromDateDF.getValue());
        fromDate.set(Calendar.DAY_OF_MONTH, 1);
        tillDate.setTime(tillDateDF.getValue());
        tillDate.set(Calendar.DAY_OF_MONTH, tillDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        cashBoxSelect = new ComboBox(myUI.getMessage(IndigoMessages.CashBox));
        cashBoxSelect.setNullSelectionAllowed(false);
        cashBoxSelect.setRequired(true);
        cashBoxSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        cashBoxSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        cashBoxSelect.setWidth(Settings.PERCENTS100);
        cashBoxSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        cashBoxSelect.setFilteringMode(FilteringMode.CONTAINS);
        cashBoxSelect.addValueChangeListener(this);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            cashBoxSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbAcc_currency, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (!currentUser.hasRole(Settings.rnAdmin) && myUI.getUser().getSchool().getCurrency_id() == 1) {
            cashBoxSelect.setEnabled(false);
            cashBoxSelect.setValue(1);
        }

        leftGrid.addComponent(cashBoxSelect, 0, 0, 3, 0);
        leftGrid.addComponent(fromDateDF, 0, 1, 1, 1);
        leftGrid.addComponent(tillDateDF, 2, 1, 3, 1);
        if (currentUser.hasRole(Settings.rnAdmin)) {
            leftGrid.addComponent(selectAllSchoolsBtn, 0, 2, 1, 2);
            leftGrid.addComponent(deselectAllSchoolsBtn, 2, 2, 3, 2);
            leftGrid.addComponent(schoolsTable, 0, 3, 3, 3);
            leftGrid.setRowExpandRatio(3, 1);
        }
        leftGrid.addComponent(selectAllIncomesBtn, 0, 4, 1, 4);
        leftGrid.addComponent(deselectAllIncomesBtn, 2, 4, 3, 4);
        leftGrid.addComponent(incomeCategoriesTable, 0, 5, 3, 5);
        leftGrid.addComponent(selectAllOutcomesBtn, 0, 6, 1, 6);
        leftGrid.addComponent(deselectAllOutcomesBtn, 2, 6, 3, 6);
        leftGrid.addComponent(outcomeCategoriesTable, 0, 7, 3, 7);
        leftGrid.addComponent(generateBtn, 0, 8, 2, 8);
        leftGrid.addComponent(excelBtn, 3, 8);
        leftGrid.setRowExpandRatio(5, 1);
        leftGrid.setRowExpandRatio(7, 1);
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
        incomesDataTable.setCaption(myUI.getMessage(IndigoMessages.Incomes));
        incomesDataTable.setFooterVisible(true);
        incomesDataTable.setSizeFull();
        incomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        incomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        incomesDataTable.addStyleName("noWrapHeader");
        incomesDataTable.addStyleName("noWrapFooter");

        outcomesDataTable = new FormattedTreeTable(myUI);
        outcomesDataTable.setCaption(myUI.getMessage(IndigoMessages.Expenses));
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
            if (fromDateDF.isValid() && tillDateDF.isValid()) {
                if (!currentUser.hasRole(Settings.rnAdmin) || !((Set<?>) schoolsTable.getValue()).isEmpty()) {
                    if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()
                            || !((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                        rightLayout.removeAllComponents();
                        try {
                            DbAccTransactions dbTr = new DbAccTransactions();
                            dbTr.connect();
                            if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                                rightLayout.addComponent(incomesDataTable);
                                rightLayout.setExpandRatio(incomesDataTable, 1);
                                if (currentUser.hasRole(Settings.rnAdmin)) {
                                    dbTr.execSQL_by_months(myUI, 1, (Integer) cashBoxSelect.getValue(),
                                            schoolsTable, incomeCategoriesTable, fromDate, tillDate, incomesDataTable);
                                } else {
                                    dbTr.execSQL_by_months(myUI, 1,
                                            myUI.getUser().getSchool().getId(), (Integer) cashBoxSelect.getValue(),
                                            incomeCategoriesTable, fromDate, tillDate, incomesDataTable);
                                }
                                if (incomesDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }

                            }
                            if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                                rightLayout.addComponent(outcomesDataTable);
                                rightLayout.setExpandRatio(outcomesDataTable, 1);
                                if (currentUser.hasRole(Settings.rnAdmin)) {
                                    dbTr.execSQL_by_months(myUI, 2, (Integer) cashBoxSelect.getValue(), schoolsTable,
                                            outcomeCategoriesTable, fromDate, tillDate, outcomesDataTable);
                                } else {
                                    dbTr.execSQL_by_months(myUI, 2, myUI.getUser().getSchool().getId(), (Integer) cashBoxSelect.getValue(),
                                            outcomeCategoriesTable, fromDate, tillDate, outcomesDataTable);
                                }
                                if (outcomesDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }
                            }
                            Iterator school_iter;
                            Calendar current = Calendar.getInstance();
                            Calendar prev_date = Calendar.getInstance();
                            current.setTime(fromDate.getTime());
                            Set<Integer> catIds = new HashSet<>();
                            while (current.before(tillDate)) {
                                school_iter = ((Set<?>) schoolsTable.getValue()).iterator();
                                prev_date.setTime(current.getTime());
                                prev_date.add(Calendar.DATE, -1);
                                while (school_iter.hasNext()) {
                                    Object nextSchool = school_iter.next();
                                    if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                                        catIds.addAll((Set<Integer>) outcomeCategoriesTable.getValue());
                                        outcomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                        nextSchool, myUI.getMessage(IndigoMessages.Title)).getValue()
                                                        + " - " + Settings.ymdf.format(current.getTime()),
                                                Table.Align.RIGHT);
                                        outcomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                        nextSchool, myUI.getMessage(IndigoMessages.Title)).getValue()
                                                        + " - " + myUI.getMessage(IndigoMessages.Total),
                                                Table.Align.RIGHT);
                                    }
                                    if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                                        catIds.addAll((Set<Integer>) incomeCategoriesTable.getValue());
                                        incomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                        nextSchool, myUI.getMessage(IndigoMessages.Title)).getValue()
                                                        + " - " + Settings.ymdf.format(current.getTime()),
                                                Table.Align.RIGHT);
                                        incomesDataTable.setColumnAlignment(schoolsTable.getContainerProperty(
                                                        nextSchool, myUI.getMessage(IndigoMessages.Title)).getValue()
                                                        + " - " + myUI.getMessage(IndigoMessages.Total),
                                                Table.Align.RIGHT);
                                        SchoolAccounting scAcc = dbTr.exec_get_totals((Integer) nextSchool, 2, current.getTime(),
                                                tillDate.getTime(), Settings.convertCollectionToStr(catIds));
                                        incomesDataTable.setColumnFooter(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(IndigoMessages.Title)).getValue() + " - "
                                                + Settings.ymdf.format(current.getTime()), myUI.getMessage(IndigoMessages.PreviousBalance) + " ("
                                                + Settings.df.format(prev_date.getTime()) + "): " + Settings.dFormat2.format(scAcc.getPrevious_balance())
                                                + "; " + myUI.getMessage(IndigoMessages.Total) + ": " + incomesDataTable.getColumnFooter(schoolsTable.getContainerProperty(
                                                nextSchool, myUI.getMessage(IndigoMessages.Title)).getValue() + " - "
                                                + Settings.ymdf.format(current.getTime())));
                                    }
                                }
                                if (!currentUser.hasRole(Settings.rnAdmin)) {
                                    SchoolAccounting scAcc = dbTr.exec_get_totals(myUI.getUser().getSchool().getId(), 2, current.getTime(),
                                            tillDate.getTime(), Settings.convertCollectionToStr(catIds));
                                    incomesDataTable.setColumnFooter(
                                            Settings.ymdf.format(current.getTime()), myUI.getMessage(IndigoMessages.PreviousBalance) + " ("
                                                    + Settings.df.format(prev_date.getTime()) + ") - " + Settings.dFormat2.format(scAcc.getPrevious_balance())
                                                    + "; " + myUI.getMessage(IndigoMessages.Total) + " - " + incomesDataTable.getColumnFooter(Settings.ymdf.format(current.getTime())));
                                    incomesDataTable.setColumnAlignment(Settings.ymdf.format(current.getTime()), Table.Align.RIGHT);
                                    outcomesDataTable.setColumnAlignment(Settings.ymdf.format(current.getTime()), Table.Align.RIGHT);
                                }
                                current.add(Calendar.MONTH, 1);
                            }
                            incomesDataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Total) + "", Table.Align.RIGHT);
                            outcomesDataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Total) + "", Table.Align.RIGHT);
                            dbTr.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
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
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(IndigoMessages.Incomes));
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
                        excelReport.setNextTable(outcomesDataTable, myUI.getMessage(IndigoMessages.Expenses));
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
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(IndigoMessages.Incomes));
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
                        excelReport = new EnhancedFormatExcelExport(outcomesDataTable, myUI.getMessage(IndigoMessages.Expenses));
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
                        excelReport = new EnhancedFormatExcelExport(outcomesDataTable, myUI.getMessage(IndigoMessages.Expenses));
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
                        excelReport = new EnhancedFormatExcelExport(incomesDataTable, myUI.getMessage(IndigoMessages.Incomes));
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
        } else if (source == selectAllSchoolsBtn) {
            schoolsTable.setValue(schoolsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllSchoolsBtn) {
            schoolsTable.setValue(null);
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
        if (property == schoolsTable) {
            if (schoolsTable.getValue() != null && !((Set<?>) schoolsTable.getValue()).isEmpty()) {
                try {
                    DbAccCategory dbac = new DbAccCategory();
                    dbac.connect();
                    dbac.execSQL_for_select_as_tree(myUI, "2", outcomeCategoriesTable, Settings.convertCollectionToStr((Set<?>) schoolsTable.getValue()), false);
                    dbac.execSQL_for_select_as_tree(myUI, "1", incomeCategoriesTable, Settings.convertCollectionToStr((Set<?>) schoolsTable.getValue()), false);
                    dbac.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                outcomeCategoriesTable.setVisibleColumns(myUI.getMessage(IndigoMessages.Title));
                incomeCategoriesTable.setVisibleColumns(myUI.getMessage(IndigoMessages.Title));
            } else {
                outcomeCategoriesTable.getContainerDataSource().removeAllItems();
                incomeCategoriesTable.getContainerDataSource().removeAllItems();
            }
        }
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
}
