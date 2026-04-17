/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.utils.Settings;
import kg.alex.star.dao.*;
import kg.alex.star.domain.SchoolAccounting;
import kg.alex.star.domain.StudentInfoPdf;
import kg.alex.star.i18n.Messages;
import kg.alex.star.pdf.AccountingByDatesPdf;
import kg.alex.star.tableexport.EnhancedFormatExcelExport;
import kg.alex.star.utils.FormattedTable;
import kg.alex.star.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.tepi.filtertable.FilterTreeTable;

import java.util.*;
import java.util.Calendar;

public class DateReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DateReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    public FormattedTable incomesDataTable, outcomesDataTable;
    public FilterTreeTable incomeCategoriesTable, outcomeCategoriesTable;
    private Button generateBtn, selectAllIncomesBtn, deselectAllIncomesBtn,
            selectAllOutcomesBtn, deselectAllOutcomesBtn, excelBtn, pdfBtn;
    private DateField fromDateDF, tillDateDF;
    private ComboBox cashBoxSelect;
    private EnhancedFormatExcelExport excelReport;
    private VerticalLayout rightLayout;
    private Label incomeTtlLab, expenseTtlLab, ttlLab, prev_balanceLab;
    private HorizontalLayout infoLay;
    private SchoolAccounting schoolAcc;

    public DateReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 7);
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
            dbac.execSQL_for_select_as_tree(myUI, "2, 5", outcomeCategoriesTable, Integer.toString(myUI.getUser().getSchool().getId()), false);
            dbac.execSQL_for_select_as_tree(myUI, "1, 5", incomeCategoriesTable, Integer.toString(myUI.getUser().getSchool().getId()), false);
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

        pdfBtn = new Button();
        pdfBtn.setDescription(myUI.getMessage(Messages.ExportToPdf));
        pdfBtn.setWidth(Settings.PERCENTS100);
        pdfBtn.setEnabled(false);
        pdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        pdfBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addClickListener(this);

        fromDateDF = new DateField(myUI.getMessage(Messages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(new Date());
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(Messages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(new Date());
        tillDateDF.addValueChangeListener(this);

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
        leftGrid.addComponent(generateBtn, 0, 6, 1, 6);
        leftGrid.addComponent(pdfBtn, 2, 6);
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

        incomesDataTable = new FormattedTable(myUI);
        incomesDataTable.setCaption(myUI.getMessage(Messages.Incomes));
        incomesDataTable.setSizeFull();
        incomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        incomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        incomesDataTable.addStyleName("noWrapHeader");

        outcomesDataTable = new FormattedTable(myUI);
        outcomesDataTable.setCaption(myUI.getMessage(Messages.Expenses));
        outcomesDataTable.setSizeFull();
        outcomesDataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        outcomesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        outcomesDataTable.addStyleName("noWrapHeader");

        infoLay = new HorizontalLayout();
        infoLay.setSpacing(true);
        infoLay.setWidth(Settings.PERCENTS100);

        incomeTtlLab = new Label();
        incomeTtlLab.setContentMode(ContentMode.HTML);
        incomeTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        incomeTtlLab.setImmediate(true);
        incomeTtlLab.setWidth(Settings.PERCENTS100);
        infoLay.addComponent(incomeTtlLab);
        infoLay.setExpandRatio(incomeTtlLab, 25);

        expenseTtlLab = new Label();
        expenseTtlLab.setContentMode(ContentMode.HTML);
        expenseTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        expenseTtlLab.setImmediate(true);
        expenseTtlLab.setWidth(Settings.PERCENTS100);
        infoLay.addComponent(expenseTtlLab);
        infoLay.setExpandRatio(expenseTtlLab, 25);

        prev_balanceLab = new Label();
        prev_balanceLab.setContentMode(ContentMode.HTML);
        prev_balanceLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        prev_balanceLab.setImmediate(true);
        prev_balanceLab.setSizeFull();
        infoLay.addComponent(prev_balanceLab);
        infoLay.setExpandRatio(prev_balanceLab, 30);

        ttlLab = new Label();
        ttlLab.setContentMode(ContentMode.HTML);
        ttlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        ttlLab.setImmediate(true);
        ttlLab.setWidth(Settings.PERCENTS100);
        infoLay.addComponent(ttlLab);
        infoLay.setExpandRatio(ttlLab, 20);

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
                    Set<Integer> catIds = new HashSet<>();
                    if (!((Set<?>) incomeCategoriesTable.getValue()).isEmpty()) {
                        catIds.addAll((Set<Integer>) incomeCategoriesTable.getValue());
                        for (Object next : (Set<Integer>) incomeCategoriesTable.getValue()) {
                            if (incomeCategoriesTable.getChildren(next) != null) {
                                catIds.addAll(new HashSet<>(
                                        (Collection<Integer>) incomeCategoriesTable.getChildren(next)));
                            }
                        }
                        rightLayout.addComponent(incomesDataTable);
                        rightLayout.setExpandRatio(incomesDataTable, 1);
                        try {
                            DbAccTransactions dbsc = new DbAccTransactions();
                            dbsc.connect();
                            incomesDataTable.setContainerDataSource(dbsc.exec_report_by_date(myUI, 1,
                                    myUI.getUser().getSchool().getId(), (Integer) cashBoxSelect.getValue(), fromDateDF.getValue(), tillDateDF.getValue(),
                                    incomeCategoriesTable));
                            incomesDataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
                            incomesDataTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);
                            if (incomesDataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                                pdfBtn.setEnabled(true);
                            }
                            dbsc.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        incomesDataTable.setContainerDataSource(null);
                    }
                    if (!((Set<?>) outcomeCategoriesTable.getValue()).isEmpty()) {
                        catIds.addAll((Set<Integer>) outcomeCategoriesTable.getValue());
                        for (Object next : (Set<Integer>) outcomeCategoriesTable.getValue()) {
                            if (outcomeCategoriesTable.getChildren(next) != null) {
                                catIds.addAll(new HashSet<>(
                                        (Collection<Integer>) outcomeCategoriesTable.getChildren(next)));
                            }
                        }
                        rightLayout.addComponent(outcomesDataTable);
                        rightLayout.setExpandRatio(outcomesDataTable, 1);
                        try {
                            DbAccTransactions dbsc = new DbAccTransactions();
                            dbsc.connect();
                            outcomesDataTable.setContainerDataSource(dbsc.exec_report_by_date(myUI, 2,
                                    myUI.getUser().getSchool().getId(), (Integer) cashBoxSelect.getValue(),
                                    fromDateDF.getValue(), tillDateDF.getValue(), outcomeCategoriesTable));
                            outcomesDataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
                            outcomesDataTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);
                            if (outcomesDataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                                pdfBtn.setEnabled(true);
                            }
                            dbsc.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    } else {
                        outcomesDataTable.setContainerDataSource(null);
                    }
                    try {
                        DbAccTransactions dbtr = new DbAccTransactions();
                        dbtr.connect();
                        schoolAcc = dbtr.exec_get_totals(myUI.getUser().getSchool().getId(),
                                (Integer) cashBoxSelect.getValue(),
                                fromDateDF.getValue(), tillDateDF.getValue(), Settings.convertCollectionToStr(catIds));

                        incomeTtlLab.setValue("<b>" + myUI.getMessage(Messages.IncomesTotal) + ": " + Settings.dFormat2.format(
                                schoolAcc.getTotal_income()) + getCurrency() + "</b>");
                        expenseTtlLab.setValue("<b>" + myUI.getMessage(Messages.ExpensesTotal) + ": " + Settings.dFormat2.format(
                                schoolAcc.getTotal_outcome()) + getCurrency() + "</b>");
                        ttlLab.setValue("<b>" + myUI.getMessage(Messages.CashBox) + ": " + Settings.dFormat2.format(
                                (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome())) + getCurrency() + "</b>");
                        Calendar c = Calendar.getInstance();
                        c.setTime(fromDateDF.getValue());
                        c.add(Calendar.DAY_OF_MONTH, -1);
                        prev_balanceLab.setValue("<b>" + myUI.getMessage(Messages.Saldo) + " (" + Settings.df.format(c.getTime()) + "): " + Settings.dFormat2.format(
                                schoolAcc.getPrevious_balance()) + getCurrency() + "</b>");
                        dbtr.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    rightLayout.addComponent(infoLay);
                }
            }
        } else if (source == excelBtn) {
            StudentInfoPdf studentInfo = new StudentInfoPdf();
            try {
                Calendar c = Calendar.getInstance();
                c.setTime(fromDateDF.getValue());
                c.add(Calendar.DAY_OF_MONTH, -1);
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
                        if (rightLayout.getComponentCount() != 0) {
                            int table_count = rightLayout.getComponentCount();
                            for (int i = 0; i < table_count && rightLayout.getComponent(i) instanceof Table; i++) {
                                Table t = ((Table) rightLayout.getComponent(i));
                                if (t.getContainerDataSource().size() != 0) {
                                    String sheet = t.getCaption();
                                    if (excelReport == null) {
                                        excelReport = new EnhancedFormatExcelExport(t, sheet);
                                    } else {
                                        excelReport.setNextTable(t, sheet);
                                    }
                                    excelReport.setReportTitle(t.getCaption() + " (" + Settings.df.format(fromDateDF.getValue()) + " / "
                                            + Settings.df.format(tillDateDF.getValue()) + ")");
                                    excelReport.setDisplayTotals(true);
                                    excelReport.convertTable();
                                    CellStyle style = excelReport.getWorkbook().createCellStyle();

                                    Font font = excelReport.getWorkbook().createFont();
                                    font.setBold(true);
                                    style.setFont(font);

                                    int rowNum = excelReport.getTotalsRow().getRowNum();
                                    Row row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);
                                    row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);
                                    row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);

                                    Cell cell = row.createCell(1);
                                    cell.setCellValue(myUI.getMessage(Messages.Accountant));
                                    cell.setCellStyle(style);
                                    cell = row.createCell(5);
                                    cell.setCellValue(myUI.getMessage(Messages.Director));
                                    cell.setCellStyle(style);
                                    row = excelReport.getWorkbook().getSheet(sheet).createRow(++rowNum);
                                    row.createCell(1).setCellValue(studentInfo.getAccountant().getSurname() + " "
                                            + studentInfo.getAccountant().getName() + " " +
                                            (studentInfo.getAccountant().getMiddle_name() == null ?
                                                    "" : studentInfo.getAccountant().getMiddle_name()));
                                    row.createCell(5).setCellValue(studentInfo.getDirector().getSurname() + " "
                                            + studentInfo.getDirector().getName() + " " +
                                            (studentInfo.getDirector().getMiddle_name() == null ?
                                                    "" : studentInfo.getDirector().getMiddle_name()));

                                    excelReport.getWorkbook().getSheet(sheet).addMergedRegion(
                                            new CellRangeAddress(excelReport.getTotalsRow().getRowNum(), excelReport.getTotalsRow().getRowNum(),
                                                    excelReport.getTotalsRow().getFirstCellNum(), excelReport.getTotalsRow().getLastCellNum() - 1));
                                    excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getFirstCellNum()).setCellValue(
                                            myUI.getMessage(Messages.IncomesTotal) + ": " + Settings.dFormat2.format(schoolAcc.getTotal_income()) + getCurrency() + "\t "
                                                    + myUI.getMessage(Messages.ExpensesTotal) + ": " + Settings.dFormat2.format(
                                                    schoolAcc.getTotal_outcome()) + getCurrency() + "\t "
                                                    + myUI.getMessage(Messages.Saldo) + " (" + Settings.df.format(c.getTime())
                                                    + "): " + Settings.dFormat2.format(schoolAcc.getPrevious_balance()) + getCurrency() + "\t "
                                                    + myUI.getMessage(Messages.CashBox) + ": " + Settings.dFormat2.format(
                                                    (schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome())) + getCurrency() + "\t ");
                                }
                            }
                        }
                    } else {
                        Notification.show(myUI.getMessage(Messages.FillSchoolInfo),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(Messages.NoAccountant),
                            Notification.Type.WARNING_MESSAGE);
                }
                if (excelReport != null) {
                    excelReport.sendConverted();
                    excelReport = null;
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == pdfBtn) {
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
                        new AccountingByDatesPdf(myUI, studentInfo, (IndexedContainer) incomesDataTable.getContainerDataSource(),
                                (IndexedContainer) outcomesDataTable.getContainerDataSource(),
                                myUI.getMessage(Messages.From) + " " + Settings.df.format(fromDateDF.getValue())
                                        + " " + myUI.getMessage(Messages.To) + " " + Settings.df.format(tillDateDF.getValue()));
                    } else {
                        Notification.show(myUI.getMessage(Messages.FillSchoolInfo),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(Messages.NoAccountant),
                            Notification.Type.WARNING_MESSAGE);
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
                    || property == tillDateDF || property == fromDateDF || property == cashBoxSelect) {
                excelBtn.setEnabled(false);
                pdfBtn.setEnabled(false);
                rightLayout.removeAllComponents();
            }
        }
    }

    private String getCurrency() {
        Object currency = cashBoxSelect.getContainerProperty(cashBoxSelect.getValue(),
                myUI.getMessage(Messages.Currency)).getValue();
        return currency == null ? " " + Settings.USD : " " + currency;
    }
}
