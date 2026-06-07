/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.dao.*;
import kg.alex.ispa.domain.StudentInfoPdf;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.pdf.ClassPaymentsPdf;
import kg.alex.ispa.tableexport.EnhancedFormatExcelExport;
import kg.alex.ispa.utils.FormattedTable;
import kg.alex.ispa.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Date;
import java.util.Set;

public class ClassPaymentsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ClassPaymentsReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final String[] NATURAL_COL_ORDER;
    public double totalKGS, totalUSD;
    private Button generateBtn, makePdfBtn, selectAllBtn, deselectAllBtn, excelBtn;
    private FilterTable classTable;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB, cashboxMCB;
    private DateField tillDateDF, fromDateDF;
    private FormattedTable dataTable;
    private IndexedContainer paymentsCont;
    private final String currency;

    public ClassPaymentsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.currency = Settings.KGS;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        NATURAL_COL_ORDER = new String[]{
                myUI.getMessage(Messages.FirstName), myUI.getMessage(Messages.LastName),
                myUI.getMessage(Messages.ClassName), myUI.getMessage(Messages.PaymentCategoryType),
                myUI.getMessage(Messages.WhoPaid), myUI.getMessage(Messages.Date),
                myUI.getMessage(Messages.Rate), myUI.getMessage(Messages.Amount),
                myUI.getMessage(Messages.CashBox)
        };
    }

    private void buildLeftPanel() {
        GridLayout leftGrid = new GridLayout(4, 7);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelect = new ComboBox(myUI.getMessage(Messages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        cashboxMCB = new ComboBoxMultiselect(myUI.getMessage(Messages.CashBox));
        cashboxMCB.setRequired(true);
        cashboxMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        cashboxMCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        cashboxMCB.setWidth(Settings.PERCENTS100);
        cashboxMCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        cashboxMCB.setFilteringMode(FilteringMode.CONTAINS);
        cashboxMCB.setClearButtonCaption(myUI.getMessage(Messages.Clear));
        cashboxMCB.setShowSelectAllButton((filter, page) -> true);
        cashboxMCB.setSelectAllButtonCaption(myUI.getMessage(Messages.SelectAll));
        try {
            DbCashbox dbd = new DbCashbox();
            dbd.connect();
            cashboxMCB.setContainerDataSource(dbd.execSQL(myUI));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cashboxMCB.addValueChangeListener(this);

        educationStatusMCB = new ComboBoxMultiselect(myUI.getMessage(Messages.EducationStatus));
        educationStatusMCB.setRequired(true);
        educationStatusMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        educationStatusMCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        educationStatusMCB.setWidth(Settings.PERCENTS100);
        educationStatusMCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        educationStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        educationStatusMCB.setClearButtonCaption(myUI.getMessage(Messages.Clear));
        educationStatusMCB.setShowSelectAllButton((filter, page) -> true);
        educationStatusMCB.setSelectAllButtonCaption(myUI.getMessage(Messages.SelectAll));
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            educationStatusMCB.setContainerDataSource(
                    dbd.exec_for_select(myUI, Settings.dbEducationStatus, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        educationStatusMCB.setValue(Settings.convertToSet(
                educationStatusMCB.getContainerDataSource().getItemIds()));
        educationStatusMCB.addValueChangeListener(this);

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        selectAllBtn = new Button(myUI.getMessage(Messages.AllClasses));
        selectAllBtn.setWidth(Settings.PERCENTS100);
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllBtn.setWidth(Settings.PERCENTS100);
        deselectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBtn.addClickListener(this);

        classTable = new FilterTable();
        classTable.setFilterDecorator(new MyFilterDecorator(myUI));
        classTable.setStyleName(ValoTheme.TABLE_SMALL);
        classTable.setSizeFull();
        classTable.setNullSelectionAllowed(false);
        classTable.setMultiSelect(true);
        classTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        classTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        classTable.setFilterBarVisible(true);
        classTable.setFooterVisible(false);
        classTable.setSelectable(true);
        classTable.addValueChangeListener(this);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            classTable.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(Messages.Title)});

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

        generateBtn = new Button(myUI.getMessage(Messages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(Messages.ExportToPdf));
        makePdfBtn.setWidth(Settings.PERCENTS100);
        makePdfBtn.setEnabled(false);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(Messages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(cashboxMCB, 0, 1, 3, 1);
        leftGrid.addComponent(educationStatusMCB, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllBtn, 2, 3, 3, 3);
        leftGrid.addComponent(classTable, 0, 4, 3, 4);
        leftGrid.addComponent(fromDateDF, 0, 5, 1, 5);
        leftGrid.addComponent(tillDateDF, 2, 5, 3, 5);
        leftGrid.addComponent(generateBtn, 0, 6, 1, 6);
        leftGrid.addComponent(makePdfBtn, 2, 6);
        leftGrid.addComponent(excelBtn, 3, 6);
        leftGrid.setRowExpandRatio(4, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            buildRightPanel();
            makePdfBtn.setEnabled(true);
            excelBtn.setEnabled(true);
        } else if (source == makePdfBtn) {
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
                        Date fromDate = fromDateDF.getValue();
                        Date tillDate = tillDateDF.getValue();
                        new ClassPaymentsPdf(myUI, paymentsCont,
                                yearSelect.getContainerProperty(yearSelect.getValue(),
                                        myUI.getMessage(Messages.Title)).getValue().toString(),
                                fromDate, tillDate, studentInfo, totalKGS, totalUSD);
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
        } else if (source == selectAllBtn) {
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            classTable.setValue(null);
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable, "sheet1");
                    excelReport.setReportTitle(myUI.getMessage(Messages.ClassPayments));
                    excelReport.setDisplayTotals(true);
                    excelReport.export();
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
        if (property == classTable || property == yearSelect || educationStatusMCB == property ||
                cashboxMCB == property || fromDateDF == property || tillDateDF == property) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
        }
    }

    private void buildRightPanel() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new FormattedTable(myUI);
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(FormattedTable.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        try {
            DbStudentPayment dbsp = new DbStudentPayment();
            dbsp.connect();
            dataTable.clear();
            totalKGS = 0;
            totalUSD = 0;
            paymentsCont = dbsp.execSQL_PaymentsByClass(myUI,
                    fromDateDF.getValue(), tillDateDF.getValue(),
                    (Integer) yearSelect.getValue(),
                    Settings.convertCollectionToStr((Set<?>) classTable.getValue()),
                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                    Settings.convertCollectionToStr((Set<?>) cashboxMCB.getValue()),
                    this);
            dataTable.setContainerDataSource(paymentsCont);
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setColumnAlignment(myUI.getMessage(Messages.Amount), Table.Align.RIGHT);
        dataTable.setColumnFooter(myUI.getMessage(Messages.Amount),
                  Settings.dFormat2.format(totalKGS) + " " + currency);
        dataTable.setColumnFooter(myUI.getMessage(Messages.CashBox),
                Settings.dFormat2.format(totalUSD) + " " + Settings.USD);
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }
}
