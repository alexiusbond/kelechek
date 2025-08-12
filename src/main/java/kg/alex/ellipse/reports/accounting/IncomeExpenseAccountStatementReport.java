/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.dao.DbAccCategory;
import kg.alex.ellipse.dao.DbAccTransactions;
import kg.alex.ellipse.dao.DbDefinition;
import kg.alex.ellipse.dao.DbSchool;
import kg.alex.ellipse.domain.School;
import kg.alex.ellipse.domain.StudentInfoPdf;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.pdf.IncomeExpenseAccountStatementPdf;
import kg.alex.ellipse.tableexport.EnhancedFormatExcelExport;
import kg.alex.ellipse.utils.FormattedTable;
import kg.alex.ellipse.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

import java.util.Date;

public class IncomeExpenseAccountStatementReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(IncomeExpenseAccountStatementReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    public FormattedTable dataTable;
    public FilterTreeTable categoriesTable;
    private Button generateBtn, pdfBtn, excelBtn;
    private ComboBox currencySelect;
    private DateField fromDateDF, tillDateDF;

    public IncomeExpenseAccountStatementReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 4);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        categoriesTable = new FilterTreeTable();
        categoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        categoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        categoriesTable.setSizeFull();
        categoriesTable.setNullSelectionAllowed(false);
        categoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        categoriesTable.setFilterBarVisible(true);
        categoriesTable.setFooterVisible(false);
        categoriesTable.setSelectable(true);
        categoriesTable.setNullSelectionAllowed(false);
        categoriesTable.addValueChangeListener(this);

        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, "5", categoriesTable,
                    Integer.toString(myUI.getUser().getSchool().getId()), false);
            dbac.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        categoriesTable.setVisibleColumns(myUI.getMessage(Messages.Title));

        generateBtn = new Button(myUI.getMessage(Messages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        pdfBtn = new Button();
        pdfBtn.setDescription(myUI.getMessage(Messages.ExportToPdf));
        pdfBtn.setWidth(Settings.PERCENTS100);
        pdfBtn.setEnabled(false);
        pdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        pdfBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addClickListener(this);

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

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        leftGrid.addComponent(currencySelect, 0, 1, 3, 1);
        leftGrid.addComponent(categoriesTable, 0, 2, 3, 2);
        leftGrid.addComponent(generateBtn, 0, 3, 1, 3);
        leftGrid.addComponent(pdfBtn, 2, 3);
        leftGrid.addComponent(excelBtn, 3, 3);
        leftGrid.setRowExpandRatio(2, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataTable = new FormattedTable(myUI);
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
            if (categoriesTable.getValue() != null && currencySelect.isValid() && fromDateDF.isValid() && tillDateDF.isValid()) {
                try {
                    DbAccTransactions dbat = new DbAccTransactions();
                    dbat.connect();
                    dbat.exec_income_expense_account_statement(myUI, (Integer) categoriesTable.getValue(),
                            fromDateDF.getValue(), tillDateDF.getValue(), dataTable, (Integer) currencySelect.getValue(),
                            myUI.getUser().getSchool().getId());

                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Rate), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Income), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Expense), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Balance), Table.Align.RIGHT);
                    if (dataTable.getContainerDataSource().size() != 0) {
                        pdfBtn.setEnabled(true);
                        excelBtn.setEnabled(true);
                    }
                    dbat.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == pdfBtn) {
            StudentInfoPdf st;
            try {
                DbSchool dbsc = new DbSchool();
                dbsc.connect();
                School school = dbsc.execSchool(myUI.getUser().getSchool().getId());
                dbsc.close();
                if (school != null && school.getAddress() != null) {
                    new IncomeExpenseAccountStatementPdf(myUI, dataTable,
                            categoriesTable.getContainerProperty(categoriesTable.getValue(),
                                    myUI.getMessage(Messages.Title)).getValue().toString(),
                            currencySelect.getItemCaption(currencySelect.getValue()),
                            fromDateDF.getValue(), tillDateDF.getValue(), school);
                } else {
                    Notification.show(myUI.getMessage(Messages.FillSchoolInfo),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(categoriesTable.getContainerProperty(categoriesTable.getValue(),
                            myUI.getMessage(Messages.Title)).getValue()
                                               + "( " + currencySelect.getItemCaption(currencySelect.getValue()) + ") "
                                               + myUI.getMessage(Messages.From) + " " + Settings.df.format(fromDateDF.getValue()) + " "
                                               + myUI.getMessage(Messages.To) + " " + Settings.df.format(tillDateDF.getValue()));
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(6).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.Balance)));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (pdfBtn.isEnabled()) {
            if (property == categoriesTable || property == tillDateDF || property == fromDateDF || property == currencySelect) {
                pdfBtn.setEnabled(false);
                excelBtn.setEnabled(false);
                dataTable.setContainerDataSource(null);
            }
        }
    }
}
