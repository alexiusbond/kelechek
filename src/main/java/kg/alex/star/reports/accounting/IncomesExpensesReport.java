/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.dao.DbAccCategory;
import kg.alex.star.dao.DbAccTransactions;
import kg.alex.star.dao.DbDefinition;
import kg.alex.star.i18n.Messages;
import kg.alex.star.tableexport.EnhancedFormatExcelExport;
import kg.alex.star.utils.FormattedTreeTable;
import kg.alex.star.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

public class IncomesExpensesReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(IncomesExpensesReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    public FormattedTreeTable dataTable;
    public FilterTreeTable employeeCategoriesTable;
    private Button generateBtn, excelBtn, selectAllBtn, deselectAllBtn;
    private ComboBox currencySelect;
    private PopupDateField fromDateDF, tillDateDF;

    public IncomesExpensesReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 5);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        employeeCategoriesTable = new FilterTreeTable();
        employeeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        employeeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        employeeCategoriesTable.setSizeFull();
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.setMultiSelect(true);
        employeeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        employeeCategoriesTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        employeeCategoriesTable.setFilterBarVisible(true);
        employeeCategoriesTable.setFooterVisible(false);
        employeeCategoriesTable.setSelectable(true);
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.addValueChangeListener(this);

        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, "5", employeeCategoriesTable,
                    Integer.toString(myUI.getUser().getSchool().getId()), true);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        employeeCategoriesTable.setVisibleColumns(myUI.getMessage(Messages.Title));

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

        fromDateDF = new PopupDateField(myUI.getMessage(Messages.FromDate));
        fromDateDF.setInputPrompt(myUI.getMessage(Messages.AnyDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setResolution(Resolution.DAY);
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new PopupDateField(myUI.getMessage(Messages.TillDate));
        tillDateDF.setInputPrompt(myUI.getMessage(Messages.AnyDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setResolution(Resolution.DAY);
        tillDateDF.addValueChangeListener(this);

        currencySelect = new ComboBox(myUI.getMessage(Messages.Currency));
        currencySelect.setNullSelectionAllowed(false);
        currencySelect.setRequired(true);
        currencySelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        currencySelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        currencySelect.setWidth(Settings.PERCENTS100);
        currencySelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        currencySelect.setFilteringMode(FilteringMode.CONTAINS);
        currencySelect.addValueChangeListener(this);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            currencySelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbAcc_currency, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        currencySelect.setValue(2);

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

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        leftGrid.addComponent(currencySelect, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllBtn, 2, 2, 3, 2);
        leftGrid.addComponent(employeeCategoriesTable, 0, 3, 3, 3);
        leftGrid.addComponent(generateBtn, 0, 4, 2, 4);
        leftGrid.addComponent(excelBtn, 3, 4);
        leftGrid.setRowExpandRatio(3, 1);
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
            if (employeeCategoriesTable.getValue() != null && currencySelect.isValid() && fromDateDF.isValid() && tillDateDF.isValid()) {
                try {
                    DbAccTransactions dbat = new DbAccTransactions();
                    dbat.connect();
                    dbat.exec_incomes_expenses(myUI, employeeCategoriesTable, fromDateDF.getValue(), tillDateDF.getValue(),
                            dataTable, (Integer) currencySelect.getValue(), myUI.getUser().getSchool().getId());
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Incomes), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Expenses), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Balance), Table.Align.RIGHT);

                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbat.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable, myUI.getMessage(Messages.IncomesExpensesReport) + " ("
                        + currencySelect.getItemCaption(currencySelect.getValue()) + ")");
                excelReport.setReportTitle(myUI.getMessage(Messages.IncomesExpensesReport) + " ("
                        + currencySelect.getItemCaption(currencySelect.getValue()) + ") ");
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getLastCellNum() - 1).setCellFormula(null);
                excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getLastCellNum() - 1).setCellValue(
                        dataTable.getColumnFooter(myUI.getMessage(Messages.Ratio)));
                excelReport.sendConverted();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            employeeCategoriesTable.setValue(employeeCategoriesTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            employeeCategoriesTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (excelBtn.isEnabled()) {
            if (property == employeeCategoriesTable || property == currencySelect || property == fromDateDF) {
                excelBtn.setEnabled(false);
                dataTable.getContainerDataSource().removeAllItems();
            }
        }
    }
}
