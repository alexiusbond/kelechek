/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbAccCategory;
import kg.alex.indigo.dao.DbAccTransactions;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.domain.School;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.pdf.CurrentAccountStatementPdf;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTreeTable;

import java.util.Date;

public class CurrentAccountStatementReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(CurrentAccountStatementReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    public FormattedTable dataTable;
    public FilterTreeTable employeeCategoriesTable;
    private Button generateBtn, pdfBtn, excelBtn;
    private ComboBox currencySelect;
    private DateField fromDateDF, tillDateDF;

    public CurrentAccountStatementReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 4);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        employeeCategoriesTable = new FilterTreeTable();
        employeeCategoriesTable.setFilterDecorator(new MyFilterDecorator(myUI));
        employeeCategoriesTable.setStyleName(ValoTheme.TABLE_SMALL);
        employeeCategoriesTable.setSizeFull();
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        employeeCategoriesTable.setFilterBarVisible(true);
        employeeCategoriesTable.setFooterVisible(false);
        employeeCategoriesTable.setSelectable(true);
        employeeCategoriesTable.setNullSelectionAllowed(false);
        employeeCategoriesTable.addValueChangeListener(this);

        try {
            DbAccCategory dbac = new DbAccCategory();
            dbac.connect();
            dbac.execSQL_for_select_as_tree(myUI, "2", employeeCategoriesTable,
                    Integer.toString(myUI.getUser().getSchool().getId()), false);
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

        pdfBtn = new Button();
        pdfBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToPdf));
        pdfBtn.setWidth(Settings.PERCENTS100);
        pdfBtn.setEnabled(false);
        pdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        pdfBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        pdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        pdfBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        fromDateDF = new DateField(myUI.getMessage(IndigoMessages.FromDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setRequired(true);
        fromDateDF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setValue(new Date());
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new DateField(myUI.getMessage(IndigoMessages.TillDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setRequired(true);
        tillDateDF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setValue(new Date());
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

        leftGrid.addComponent(fromDateDF, 0, 0, 1, 0);
        leftGrid.addComponent(tillDateDF, 2, 0, 3, 0);
        leftGrid.addComponent(currencySelect, 0, 1, 3, 1);
        leftGrid.addComponent(employeeCategoriesTable, 0, 2, 3, 2);
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
            if (employeeCategoriesTable.getValue() != null && currencySelect.isValid() && fromDateDF.isValid() && tillDateDF.isValid()) {
                try {
                    DbAccTransactions dbat = new DbAccTransactions();
                    dbat.connect();
                    dbat.exec_current_account_statement(myUI, (Integer) employeeCategoriesTable.getValue(), fromDateDF.getValue(),
                            tillDateDF.getValue(), dataTable, (Integer) currencySelect.getValue(), myUI.getUser().getSchool().getId());

                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Rate), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Accrual), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Payout), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Balance), Table.Align.RIGHT);
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
                    new CurrentAccountStatementPdf(myUI, dataTable,
                            employeeCategoriesTable.getContainerProperty(employeeCategoriesTable.getValue(),
                                    myUI.getMessage(IndigoMessages.Title)).getValue().toString(),
                            currencySelect.getItemCaption(currencySelect.getValue()),
                            fromDateDF.getValue(), tillDateDF.getValue(), school);
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.FillSchoolInfo),
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
                    excelReport.setReportTitle(employeeCategoriesTable.getContainerProperty(employeeCategoriesTable.getValue(),
                            myUI.getMessage(IndigoMessages.Title)).getValue()
                            + "( " + currencySelect.getItemCaption(currencySelect.getValue()) + ") "
                            + myUI.getMessage(IndigoMessages.From) + " " + Settings.df.format(fromDateDF.getValue()) + " "
                            + myUI.getMessage(IndigoMessages.To) + " " + Settings.df.format(tillDateDF.getValue()));
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(6).setCellValue(dataTable.getColumnFooter(myUI.getMessage(IndigoMessages.Balance)));
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
            if (property == employeeCategoriesTable || property == tillDateDF || property == fromDateDF || property == currencySelect) {
                pdfBtn.setEnabled(false);
                excelBtn.setEnabled(false);
                dataTable.setContainerDataSource(null);
            }
        }
    }
}
