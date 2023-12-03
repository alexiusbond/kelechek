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
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTreeTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

public class PayoutsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(PayoutsReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, excelBtn, selectAllBtn, deselectAllBtn;
    private final HorizontalSplitPanel splitPanel;
    private ComboBox currencySelect;
    private PopupDateField fromDateDF, tillDateDF;
    public FormattedTreeTable dataTable;
    public FilterTreeTable employeeCategoriesTable;

    public PayoutsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
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
            dbac.execSQL_for_select_as_tree(myUI, "2", employeeCategoriesTable,
                    Integer.toString(myUI.getUser().getSchool().getId()), true);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        employeeCategoriesTable.setVisibleColumns(myUI.getMessage(IndigoMessages.Title));

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
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY); excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        fromDateDF = new PopupDateField(myUI.getMessage(IndigoMessages.FromDate));
        fromDateDF.setInputPrompt(myUI.getMessage(IndigoMessages.AnyDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setResolution(Resolution.DAY);
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new PopupDateField(myUI.getMessage(IndigoMessages.TillDate));
        tillDateDF.setInputPrompt(myUI.getMessage(IndigoMessages.AnyDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setResolution(Resolution.DAY);
        tillDateDF.addValueChangeListener(this);

        currencySelect = new ComboBox(myUI.getMessage(IndigoMessages.Currency));
        currencySelect.setNullSelectionAllowed(false);
        currencySelect.setRequired(true);
        currencySelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        currencySelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        currencySelect.setWidth(Settings.PERCENTS100);
        currencySelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
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

        selectAllBtn = new Button(myUI.getMessage(IndigoMessages.AllCategories));
        selectAllBtn.setWidth(Settings.PERCENTS100);
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
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
                    dbat.exec_account_remains(myUI, employeeCategoriesTable, (Integer) currencySelect.getValue(),
                            myUI.getUser().getSchool().getId(), fromDateDF.getValue(), tillDateDF.getValue(), dataTable);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Remain), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Salary), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Ratio), Table.Align.RIGHT);

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
                EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable, myUI.getMessage(IndigoMessages.SalariesReport) + " ("
                        + currencySelect.getItemCaption(currencySelect.getValue()) + ")");
                excelReport.setReportTitle(myUI.getMessage(IndigoMessages.SalariesReport) + " ("
                        + currencySelect.getItemCaption(currencySelect.getValue()) + ") ");
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getLastCellNum() - 1).setCellFormula(null);
                excelReport.getTotalsRow().getCell(excelReport.getTotalsRow().getLastCellNum() - 1).setCellValue(
                        dataTable.getColumnFooter(myUI.getMessage(IndigoMessages.Ratio)));
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
            if (property == employeeCategoriesTable || property == currencySelect || property == fromDateDF || property == currencySelect) {
                excelBtn.setEnabled(false);
                dataTable.getContainerDataSource().removeAllItems();
            }
        }
    }
}
