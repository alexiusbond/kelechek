package kg.alex.star.reports.stock;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.dao.*;
import kg.alex.star.i18n.Messages;
import kg.alex.star.tableexport.EnhancedFormatExcelExport;
import kg.alex.star.utils.FormattedTreeTable;
import kg.alex.star.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Date;
import java.util.Set;

public class StockGeneralReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(StockGeneralReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private Button generateBtn, excelBtn, selectAllBtn, deselectAllBtn;
    private ComboBox schoolSelect;
    private ComboBoxMultiselect stocksMSB;
    private OptionGroup operationOG;
    private DateField fromDateDF, tillDateDF;
    private FormattedTreeTable dataTable;
    private FilterTreeTable productsTable;

    public StockGeneralReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 7);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        productsTable = new FilterTreeTable();
        productsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        productsTable.setStyleName(ValoTheme.TABLE_SMALL);
        productsTable.setSizeFull();
        productsTable.setNullSelectionAllowed(false);
        productsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        productsTable.setFilterBarVisible(true);
        productsTable.setFooterVisible(false);
        productsTable.setMultiSelect(true);
        productsTable.setSelectable(true);
        productsTable.setNullSelectionAllowed(false);
        productsTable.addValueChangeListener(this);

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

        operationOG = new OptionGroup();
        operationOG.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        operationOG.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        operationOG.setNullSelectionAllowed(false);
        operationOG.setWidth(Settings.PERCENTS100);
        operationOG.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        operationOG.addValueChangeListener(this);

        stocksMSB = new ComboBoxMultiselect(myUI.getMessage(Messages.Stocks));
        stocksMSB.setRequired(true);
        stocksMSB.setStyleName(ValoTheme.COMBOBOX_TINY);
        stocksMSB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        stocksMSB.setWidth(Settings.PERCENTS100);
        stocksMSB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        stocksMSB.setFilteringMode(FilteringMode.CONTAINS);
        stocksMSB.setClearButtonCaption(myUI.getMessage(Messages.Clear));
        stocksMSB.setShowSelectAllButton((filter, page) -> true);
        stocksMSB.setSelectAllButtonCaption(myUI.getMessage(Messages.SelectAll));
        stocksMSB.addValueChangeListener(this);

        schoolSelect = new ComboBox(myUI.getMessage(Messages.School));
        schoolSelect.setNullSelectionAllowed(false);
        schoolSelect.setRequired(true);
        schoolSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        schoolSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        schoolSelect.setWidth(Settings.PERCENTS100);
        schoolSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        schoolSelect.setFilteringMode(FilteringMode.CONTAINS);
        schoolSelect.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolSelect.setContainerDataSource(
                    dbs.execSchoolSel(myUI, Settings.MAIN_OFFICE_ID));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        schoolSelect.setValue(myUI.getUser().getSchool().getId());

        selectAllBtn = new Button(myUI.getMessage(Messages.AllCategories));
        selectAllBtn.setWidth(Settings.PERCENTS100);
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllBtn.setWidth(Settings.PERCENTS100);
        deselectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBtn.addClickListener(this);

        try {
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            DbAccCategory dbAc = new DbAccCategory();
            dbAc.connect();
            DbProductCategories dbPC = new DbProductCategories();
            dbPC.connect();
            operationOG.setContainerDataSource(dbCon.exec_for_select(myUI, Settings.dbOperation, true));
            Item item = operationOG.getContainerDataSource().addItem(0);
            item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(myUI.getMessage(Messages.General));
            if (operationOG.getContainerDataSource() != null) {
                operationOG.setValue(((IndexedContainer) operationOG.getContainerDataSource()).firstItemId());
            }
            dbAc.execSQL_for_select_as_tree(myUI, productsTable,
                    Settings.convertCollectionToStr(dbPC.execSQL_for_select(myUI).getItemIds()));
            dbAc.close();
            dbCon.close();
            dbPC.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        stocksMSB.setValue(Settings.convertToSet(stocksMSB.getContainerDataSource().getItemIds()));

        leftGrid.addComponent(operationOG, 0, 0, 3, 0);
        leftGrid.addComponent(schoolSelect, 0, 1, 3, 1);
        leftGrid.addComponent(fromDateDF, 0, 2, 1, 2);
        leftGrid.addComponent(tillDateDF, 2, 2, 3, 2);
        leftGrid.addComponent(stocksMSB, 0, 3, 3, 3);
        leftGrid.addComponent(selectAllBtn, 0, 4, 1, 4);
        leftGrid.addComponent(deselectAllBtn, 2, 4, 3, 4);
        leftGrid.addComponent(productsTable, 0, 5, 3, 5);
        leftGrid.addComponent(generateBtn, 0, 6, 2, 6);
        leftGrid.addComponent(excelBtn, 3, 6);
        leftGrid.setRowExpandRatio(5, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataTable = new FormattedTreeTable(myUI);
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.addStyleName("noWrapHeader");
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (productsTable.getValue() != null && stocksMSB.isValid() && fromDateDF.isValid() && tillDateDF.isValid()) {
                try {
                    DbStockMovements dbCon = new DbStockMovements();
                    dbCon.connect();
                    if ((Integer) operationOG.getValue() == 0) {
                        dbCon.exec_stock_balance(myUI, productsTable, fromDateDF.getValue(), tillDateDF.getValue(),
                                Settings.convertCollectionToStr((Set<?>) stocksMSB.getValue()), dataTable);

                        dataTable.setColumnAlignment(myUI.getMessage(Messages.StockIncome) + " - " + myUI.getMessage(Messages.Quantity), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.StockOutcome) + " - " + myUI.getMessage(Messages.Quantity), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.StockIncome) + " - " + myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.StockOutcome) + " - " + myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.PreviousBalance) + " " +
                                myUI.getMessage(Messages.ToThe).toLowerCase() + " " + Settings.df.format(tillDateDF.getValue()), Table.Align.RIGHT);
                    } else {
                        dbCon.exec_stock_operations(myUI, productsTable, fromDateDF.getValue(), tillDateDF.getValue(), (Integer) operationOG.getValue(),
                                Settings.convertCollectionToStr((Set<?>) stocksMSB.getValue()), dataTable);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.Quantity), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.AveragePrice), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.AverageRate), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
                    }


                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(myUI.getMessage(Messages.StockGeneralReport) + " ("
                            + operationOG.getContainerProperty(operationOG.getValue(),
                            myUI.getMessage(Messages.Title)).getValue() + ") - [" + myUI.getMessage(Messages.From).toLowerCase() + " "
                            + Settings.df.format(fromDateDF.getValue())
                            + " " + myUI.getMessage(Messages.To).toLowerCase() + " " + Settings.df.format(tillDateDF.getValue()) + "]");
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    if ((Integer) operationOG.getValue() == 0) {
                        excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(2).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(2).setCellValue(
                                dataTable.getColumnFooter(myUI.getMessage(Messages.StockIncome)
                                        + " - " + myUI.getMessage(Messages.Amount)));
                        excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(4).setCellValue(
                                dataTable.getColumnFooter(myUI.getMessage(Messages.StockOutcome)
                                        + " - " + myUI.getMessage(Messages.Amount)));
                        excelReport.getTotalsRow().getCell(5).setCellFormula(null);
                    } else {
                        excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(5).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(5).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.Amount)));
                    }
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            productsTable.setValue(productsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            productsTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == productsTable || property == schoolSelect ||
                    property == tillDateDF || property == fromDateDF || property == stocksMSB || property == operationOG) {
                excelBtn.setEnabled(false);
                dataTable.getContainerDataSource().removeAllItems();
            }
        }
        if (property == schoolSelect) {
            try {
                DbDefinition dbCon = new DbDefinition();
                dbCon.connect();
                stocksMSB.setContainerDataSource(dbCon.exec_for_select(myUI, Settings.dbStock,
                        (Integer) schoolSelect.getValue(), true));
                stocksMSB.setValue(Settings.convertToSet(stocksMSB.getContainerDataSource().getItemIds()));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
