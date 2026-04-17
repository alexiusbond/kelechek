/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.utils.Settings;
import kg.alex.star.dao.DbAccCategory;
import kg.alex.star.dao.DbInvoice;
import kg.alex.star.dao.DbTransfers;
import kg.alex.star.domain.SchoolAccounting;
import kg.alex.star.i18n.Messages;
import kg.alex.star.utils.FormattedTreeTable;
import kg.alex.star.utils.MyFilterDecorator;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.tepi.filtertable.FilterTreeTable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.Calendar;

public class BalanceReport implements Button.ClickListener,
        Property.ValueChangeListener, Serializable {

    static final Logger logger = LogManager.getLogger(BalanceReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final TabSheet tabSheet;
    private final Calendar fromDate = Calendar.getInstance(), tillDate = Calendar.getInstance();
    public FormattedTreeTable assertsDataTable, debtsDataTable;
    public FilterTreeTable assertsCategoriesTable, debtsCategoriesTable;
    private Button generateBtn, selectAllAssertsBtn, deselectAllAssertsBtn,
            selectAllDebtsBtn, deselectAllDebtsBtn, excelBtn;
    private DateField fromDateDF, tillDateDF;
    private Label assertsTtlLab, debtsTtlLab, ttlLab;
    private HorizontalLayout infoLay;
    private FileDownloader fd;

    public BalanceReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.setStyleName(ValoTheme.TABSHEET_FRAMED);

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        vl.addComponent(tabSheet);

        splitPanel.setSecondComponent(vl);
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 6);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllAssertsBtn = new Button(myUI.getMessage(Messages.AllAsserts));
        selectAllAssertsBtn.setWidth(Settings.PERCENTS100);
        selectAllAssertsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllAssertsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllAssertsBtn.addClickListener(this);

        deselectAllAssertsBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllAssertsBtn.setWidth(Settings.PERCENTS100);
        deselectAllAssertsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllAssertsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllAssertsBtn.addClickListener(this);

        assertsCategoriesTable = new FilterTreeTable();
        assertsCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        assertsCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        assertsCategoriesTable.setSizeFull();
        assertsCategoriesTable.setNullSelectionAllowed(false);
        assertsCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        assertsCategoriesTable.setFilterBarVisible(true);
        assertsCategoriesTable.setFooterVisible(false);
        assertsCategoriesTable.setSelectable(true);
        assertsCategoriesTable.setNullSelectionAllowed(false);
        assertsCategoriesTable.setMultiSelect(true);
        assertsCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        assertsCategoriesTable.addValueChangeListener(this);

        selectAllDebtsBtn = new Button(myUI.getMessage(Messages.AllDebts));
        selectAllDebtsBtn.setWidth(Settings.PERCENTS100);
        selectAllDebtsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllDebtsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllDebtsBtn.addClickListener(this);

        deselectAllDebtsBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllDebtsBtn.setWidth(Settings.PERCENTS100);
        deselectAllDebtsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllDebtsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllDebtsBtn.addClickListener(this);

        debtsCategoriesTable = new FilterTreeTable();
        debtsCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        debtsCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        debtsCategoriesTable.setSizeFull();
        debtsCategoriesTable.setNullSelectionAllowed(false);
        debtsCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        debtsCategoriesTable.setFilterBarVisible(true);
        debtsCategoriesTable.setFooterVisible(false);
        debtsCategoriesTable.setSelectable(true);
        debtsCategoriesTable.setNullSelectionAllowed(false);
        debtsCategoriesTable.setMultiSelect(true);
        debtsCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        debtsCategoriesTable.addValueChangeListener(this);
        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, "3", assertsCategoriesTable, Integer.toString(myUI.getUser().getSchool().getId()), true);
            dbac.execSQL_for_select_as_tree(myUI, "4", debtsCategoriesTable, Integer.toString(myUI.getUser().getSchool().getId()), true);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        assertsCategoriesTable.setVisibleColumns(myUI.getMessage(Messages.Title));
        debtsCategoriesTable.setVisibleColumns(myUI.getMessage(Messages.Title));

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

        fromDateDF = new DateField(myUI.getMessage(Messages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        fromDateDF.setResolution(Resolution.MONTH);
        fromDateDF.setDateFormat(Settings.yearMonthPattern);
        fromDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(Messages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        tillDateDF.setResolution(Resolution.MONTH);
        tillDateDF.setDateFormat(Settings.yearMonthPattern);
        tillDateDF.setValue(DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH));
        tillDateDF.addValueChangeListener(this);

        fromDate.setTime(fromDateDF.getValue());
        fromDate.set(Calendar.DAY_OF_MONTH, 1);
        tillDate.setTime(tillDateDF.getValue());
        tillDate.set(Calendar.DAY_OF_MONTH, tillDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        leftGrid.addComponent(selectAllAssertsBtn, 0, 1, 1, 1);
        leftGrid.addComponent(deselectAllAssertsBtn, 2, 1, 3, 1);
        leftGrid.addComponent(assertsCategoriesTable, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllDebtsBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllDebtsBtn, 2, 3, 3, 3);
        leftGrid.addComponent(debtsCategoriesTable, 0, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(2, 1);
        leftGrid.setRowExpandRatio(4, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private VerticalLayout buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        vl.setSizeFull();

        assertsDataTable = new FormattedTreeTable(myUI);
        assertsDataTable.setCaption(myUI.getMessage(Messages.Asserts));
        assertsDataTable.setSizeFull();
        assertsDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        assertsDataTable.addStyleName("noWrapHeader");

        debtsDataTable = new FormattedTreeTable(myUI);
        debtsDataTable.setCaption(myUI.getMessage(Messages.Debts));
        debtsDataTable.setSizeFull();
        debtsDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        debtsDataTable.addStyleName("noWrapHeader");

        infoLay = new HorizontalLayout();
        infoLay.setSpacing(true);
        infoLay.setWidth(Settings.PERCENTS100);

        assertsTtlLab = new Label();
        assertsTtlLab.setContentMode(ContentMode.HTML);
        assertsTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        assertsTtlLab.setImmediate(true);
        assertsTtlLab.setWidth(Settings.PERCENTS100);
        infoLay.addComponent(assertsTtlLab);

        debtsTtlLab = new Label();
        debtsTtlLab.setContentMode(ContentMode.HTML);
        debtsTtlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        debtsTtlLab.setImmediate(true);
        debtsTtlLab.setWidth(Settings.PERCENTS100);
        infoLay.addComponent(debtsTtlLab);

        ttlLab = new Label();
        ttlLab.setContentMode(ContentMode.HTML);
        ttlLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        ttlLab.setImmediate(true);
        ttlLab.setWidth(Settings.PERCENTS100);
        infoLay.addComponent(ttlLab);
        return vl;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromDateDF.isValid() && tillDateDF.isValid()) {
                if (!((Set<?>) assertsCategoriesTable.getValue()).isEmpty()
                        || !((Set<?>) debtsCategoriesTable.getValue()).isEmpty()) {
                    tabSheet.removeAllComponents();
                    Calendar current = Calendar.getInstance();
                    Calendar end_date = Calendar.getInstance();
                    current.setTime(fromDate.getTime());
                    while (current.before(tillDate)) {
                        end_date.setTime(current.getTime());
                        end_date.set(Calendar.DAY_OF_MONTH, end_date.getActualMaximum(Calendar.DAY_OF_MONTH));
                        VerticalLayout rightLayout = buildRightLayout();
                        tabSheet.addTab(rightLayout, Settings.ymdf.format(current.getTime()));
                        Set<Integer> catIds = new HashSet<>();
                        if (!((Set<?>) assertsCategoriesTable.getValue()).isEmpty()) {
                            catIds.addAll((Set<Integer>) assertsCategoriesTable.getValue());
                            rightLayout.addComponent(assertsDataTable);
                            rightLayout.setExpandRatio(assertsDataTable, 1);
                            try {
                                DbTransfers dbCon = new DbTransfers();
                                dbCon.connect();
                                dbCon.exec_report_by_date(myUI, "3,5", myUI.getUser().getSchool().getId(), current.getTime(), end_date.getTime(),
                                        assertsDataTable, Settings.convertCollectionToStr(catIds));
                                assertsDataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), FormattedTreeTable.Align.RIGHT);
                                assertsDataTable.setColumnAlignment(myUI.getMessage(Messages.Rate), FormattedTreeTable.Align.RIGHT);
                                if (assertsDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }
                                dbCon.close();
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                            try {
                                DbInvoice dbCon = new DbInvoice();
                                dbCon.connect();
                                assertsDataTable.setId(dbCon.execSQL_Note2(myUI.getUser().getSchool().getId(), 3, new java.sql.Date(current.getTime().getTime())));
                                dbCon.close();
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                        }
                        if (!((Set<?>) debtsCategoriesTable.getValue()).isEmpty()) {
                            catIds.addAll((Set<Integer>) debtsCategoriesTable.getValue());
                            rightLayout.addComponent(debtsDataTable);
                            rightLayout.setExpandRatio(debtsDataTable, 1);
                            try {
                                DbTransfers dbsc = new DbTransfers();
                                dbsc.connect();
                                dbsc.exec_report_by_date(myUI, "4,5", myUI.getUser().getSchool().getId(), current.getTime(), end_date.getTime(),
                                        debtsDataTable, Settings.convertCollectionToStr(catIds));
                                debtsDataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), FormattedTreeTable.Align.RIGHT);
                                debtsDataTable.setColumnAlignment(myUI.getMessage(Messages.Rate), FormattedTreeTable.Align.RIGHT);
                                if (debtsDataTable.getContainerDataSource().size() != 0) {
                                    excelBtn.setEnabled(true);
                                }
                                dbsc.close();
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                            try {
                                DbInvoice dbCon = new DbInvoice();
                                dbCon.connect();
                                debtsDataTable.setId(dbCon.execSQL_Note2(myUI.getUser().getSchool().getId(), 4, new java.sql.Date(current.getTime().getTime())));
                                dbCon.close();
                            } catch (Exception e) {
                                logger.error(e);
                                logger.catching(e);
                            }
                        }
                        try {
                            DbTransfers dbtr = new DbTransfers();
                            dbtr.connect();
                            SchoolAccounting schoolAcc = dbtr.exec_get_ttls(myUI.getUser().getSchool().getId(), current.getTime(),
                                    end_date.getTime(), Settings.convertCollectionToStr(catIds));
                            assertsTtlLab.setValue("<b>" + myUI.getMessage(Messages.AssertsTotal) + ": " +
                                    Settings.dFormat2.format(schoolAcc.getTotal_income()) + "$</b>");
                            debtsTtlLab.setValue("<b>" + myUI.getMessage(Messages.DebtsTotal) + ": "
                                    + Settings.dFormat2.format(schoolAcc.getTotal_outcome()) + "$</b>");
                            ttlLab.setValue("<b>" + myUI.getMessage(Messages.Total) + ": " + Settings.dFormat2.format(
                                    (schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome())) + "$</b>");
                            assertsDataTable.setData(schoolAcc.getTotal_income());
                            debtsDataTable.setData(schoolAcc.getTotal_outcome());
                            ttlLab.setData(schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome());
                            ttlLab.setId(myUI.getMessage(Messages.Total));
                            dbtr.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        rightLayout.addComponent(infoLay);
                        current.add(Calendar.MONTH, 1);
                    }
                    createWorkbook();
                }
            }
        } else if (source == selectAllAssertsBtn) {
            assertsCategoriesTable.setValue(assertsCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllAssertsBtn) {
            assertsCategoriesTable.setValue(null);
        } else if (source == selectAllDebtsBtn) {
            debtsCategoriesTable.setValue(debtsCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllDebtsBtn) {
            debtsCategoriesTable.setValue(null);
        }
    }

    private void createWorkbook() {
        if (excelBtn.isEnabled()) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            Font fontBoldWht = workbook.createFont();
            fontBoldWht.setColor(IndexedColors.WHITE.getIndex());
            fontBoldWht.setBold(true);

            Font fontBlk = workbook.createFont();
            fontBlk.setColor(IndexedColors.BLACK.getIndex());

            CellStyle cellStyleGreen = workbook.createCellStyle();
            cellStyleGreen.setFont(fontBoldWht);
            cellStyleGreen.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            cellStyleGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle cellStyleLightGreen = workbook.createCellStyle();
            cellStyleLightGreen.setFont(fontBlk);
            cellStyleLightGreen.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            cellStyleLightGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < tabSheet.getComponentCount(); i++) {
                TabSheet.Tab tab = tabSheet.getTab(i);
                ArrayList<Label> labels = new ArrayList<>();
                ArrayList<FormattedTreeTable> tables = new ArrayList<>();
                for (int j = 0; j < ((VerticalLayout) tab.getComponent()).getComponentCount(); j++) {
                    if (((VerticalLayout) tab.getComponent()).getComponent(j) instanceof FormattedTreeTable) {
                        tables.add((FormattedTreeTable) ((VerticalLayout) tab.getComponent()).getComponent(j));
                    } else if (((VerticalLayout) tab.getComponent()).getComponent(j) instanceof HorizontalLayout) {
                        HorizontalLayout hl = (HorizontalLayout) ((VerticalLayout) tab.getComponent()).getComponent(j);
                        for (int k = 0; k < hl.getComponentCount(); k++) {
                            if (hl.getComponent(k) instanceof Label) {
                                labels.add((Label) hl.getComponent(k));
                            }
                        }
                    }
                }
                HSSFSheet sheet = workbook.createSheet(tab.getCaption());

                int rowNum;
                int colNum;
                Row row;
                Cell cell;

                for (int k = 0; k < tables.size(); k++) {
                    rowNum = 1;
                    colNum = k * 6 + 1;
                    if (sheet.getRow(rowNum) != null) {
                        row = sheet.getRow(rowNum);
                    } else {
                        row = sheet.createRow(rowNum);
                    }
                    Iterator<?> propIter = tables.get(k).getContainerPropertyIds().iterator();
                    while (propIter.hasNext()) {
                        Object nextProp = propIter.next();
                        cell = row.createCell(colNum);
                        cell.setCellStyle(cellStyleGreen);
                        if (nextProp.equals(myUI.getMessage(Messages.Title))) {
                            cell.setCellValue(tables.get(k).getCaption());
                        } else if (nextProp.equals(myUI.getMessage(Messages.Amount))) {
                            cell.setCellValue((Double) tables.get(k).getData());
                        }
                        sheet.autoSizeColumn(colNum);
                        colNum++;
                    }
                    rowNum += 2;
                    colNum = k * 6 + 1;
                    if (sheet.getRow(rowNum) != null) {
                        row = sheet.getRow(rowNum);
                    } else {
                        row = sheet.createRow(rowNum);
                    }
                    rowNum++;
                    propIter = tables.get(k).getContainerPropertyIds().iterator();
                    while (propIter.hasNext()) {
                        Object nextProp = propIter.next();
                        cell = row.createCell(colNum);
                        cell.setCellStyle(cellStyleGreen);
                        cell.setCellValue(nextProp.toString());
                        sheet.autoSizeColumn(colNum);
                        colNum++;
                    }
                    colNum = k * 6 + 1;
                    for (Object nextItem : tables.get(k).getItemIds()) {
                        if (sheet.getRow(rowNum) != null) {
                            row = sheet.getRow(rowNum);
                        } else {
                            row = sheet.createRow(rowNum);
                        }
                        rowNum++;
                        propIter = tables.get(k).getContainerPropertyIds().iterator();
                        while (propIter.hasNext()) {
                            Object nextProp = propIter.next();
                            cell = row.createCell(colNum);
                            if (tables.get(k).getContainerDataSource().getChildren(nextItem) != null) {
                                cell.setCellStyle(cellStyleLightGreen);
                            }
                            if (tables.get(k).getContainerProperty(nextItem, nextProp).getValue() instanceof String) {
                                cell.setCellValue((String) tables.get(k).getContainerProperty(nextItem, nextProp).getValue());
                            } else if (tables.get(k).getContainerProperty(nextItem, nextProp).getValue() instanceof Double) {
                                cell.setCellValue((Double) tables.get(k).getContainerProperty(nextItem, nextProp).getValue());
                            }
                            sheet.autoSizeColumn(colNum);
                            colNum++;
                        }
                        colNum = k * 6 + 1;
                    }

                    if (tables.get(k).getId() != null) {
                        rowNum++;
                        row = sheet.createRow(rowNum);
                        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, colNum, colNum + 4));
                        cell = row.createCell(colNum);
                        cell.setCellValue(tables.get(k).getId());
                        sheet.autoSizeColumn(colNum);
                        colNum = k * 6 + 1;
                        rowNum++;
                    }

                    if (k == tables.size() - 1) {
                        for (int j = 2; j < labels.size(); j++) {
                            rowNum++;
                            if (sheet.getRow(rowNum) != null) {
                                row = sheet.getRow(rowNum);
                            } else {
                                row = sheet.createRow(rowNum);
                            }
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            cell.setCellValue(labels.get(j).getId());
                            sheet.autoSizeColumn(colNum);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            cell.setCellValue((Double) labels.get(j).getData());
                            sheet.autoSizeColumn(colNum);
                            colNum++;
                            cell = row.createCell(colNum);
                            cell.setCellStyle(cellStyleGreen);
                            colNum = k * 6 + 1;
                            rowNum++;
                        }
                    }
                }
            }

            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                // Close the document
                workbook.close();
                if (fd != null && excelBtn.getExtensions().contains(fd)) {
                    excelBtn.removeExtension(fd);
                }
                fd = new FileDownloader(new StreamResource(() -> new ByteArrayInputStream(baos.toByteArray()),
                        myUI.getMessage(Messages.AccountingBalanceReport) + System.currentTimeMillis() + ".xls"));
                fd.extend(excelBtn);
            } catch (IOException e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == assertsCategoriesTable || property == debtsCategoriesTable
                    || property == tillDateDF || property == fromDateDF) {
                excelBtn.setEnabled(false);
                tabSheet.removeAllComponents();
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
