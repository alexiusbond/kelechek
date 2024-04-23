/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbEmployee;
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.dao.DbStudentContract;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.pdf.MonthsReportPdf;
import kg.alex.indigo.pdf.SummaryReportPdf;
import kg.alex.indigo.pdf.YearReportPdf;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Set;

public class YearMonthReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(YearMonthReport.class);
    private final MyVaadinUI myUI;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final HorizontalSplitPanel splitPanel;
    private final String[] NATURAL_COL_ORDER_YEAR;
    private final String[] NATURAL_COL_ORDER_MONTH;
    private final String[] NATURAL_COL_ORDER_SUMMARY;
    public VerticalLayout rightLay;
    public int totalStudents = 0, totalActive = 0;
    public double contracts = 0.0, discounts = 0.0, prevYearDebts = 0.0, prevYearOverpays = 0.0, corrections = 0.0, nets = 0.0,
            paid_amounts = 0.0, debts = 0.0, overpays = 0.0, inst_plans = 0.0;
    private Button generateBtn, makePdfBtn, selectAllBtn, deselectAllBtn, excelBtn;
    private FilterTable schoolTable;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    private EnhancedFormatExcelExport excelReport;
    private PopupDateField fromDateDF, tillDateDF;
    private OptionGroup type;

    public YearMonthReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
        NATURAL_COL_ORDER_YEAR = new String[]{myUI.getMessage(IndigoMessages.ClassName),
                myUI.getMessage(IndigoMessages.Total_Active),
                myUI.getMessage(IndigoMessages.Contract),
                myUI.getMessage(IndigoMessages.Discount),
                myUI.getMessage(IndigoMessages.DiscountPercentage),
                myUI.getMessage(IndigoMessages.Correction),
                myUI.getMessage(IndigoMessages.PreviousYearDebt),
                myUI.getMessage(IndigoMessages.PreviousYearOverpay),
                myUI.getMessage(IndigoMessages.Net),
                myUI.getMessage(IndigoMessages.Paid),
                Settings.percentage,
                myUI.getMessage(IndigoMessages.Debt),
                myUI.getMessage(IndigoMessages.OverPay)};

        NATURAL_COL_ORDER_SUMMARY = new String[]{myUI.getMessage(IndigoMessages.School),
                myUI.getMessage(IndigoMessages.Total_Active),
                myUI.getMessage(IndigoMessages.Contract),
                myUI.getMessage(IndigoMessages.Discount),
                myUI.getMessage(IndigoMessages.DiscountPercentage),
                myUI.getMessage(IndigoMessages.Correction),
                myUI.getMessage(IndigoMessages.PreviousYearDebt),
                myUI.getMessage(IndigoMessages.PreviousYearOverpay),
                myUI.getMessage(IndigoMessages.Net),
                myUI.getMessage(IndigoMessages.Paid),
                Settings.percentage,
                myUI.getMessage(IndigoMessages.Debt),
                myUI.getMessage(IndigoMessages.OverPay)};

        NATURAL_COL_ORDER_MONTH = new String[]{myUI.getMessage(IndigoMessages.Month),
                myUI.getMessage(IndigoMessages.InstPlanDebt),
                myUI.getMessage(IndigoMessages.Paid),
                Settings.percentage,
                myUI.getMessage(IndigoMessages.Debt),
                myUI.getMessage(IndigoMessages.OverPay)};
    }

    private void buildLeftPanel() {
        GridLayout leftGrid = new GridLayout(4, 7);
        leftGrid.setSpacing(true);
        leftGrid.setWidth(Settings.PERCENTS100);

        yearSelect = new ComboBox(myUI.getMessage(IndigoMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        educationStatusMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.EducationStatus));
        educationStatusMCB.setRequired(true);
        educationStatusMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        educationStatusMCB.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        educationStatusMCB.setWidth(Settings.PERCENTS100);
        educationStatusMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        educationStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        educationStatusMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        educationStatusMCB.setShowSelectAllButton((filter, page) -> true);
        educationStatusMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
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

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        fromDateDF = new PopupDateField(myUI.getMessage(IndigoMessages.FromDate));
        fromDateDF.setInputPrompt(myUI.getMessage(IndigoMessages.AnyDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setResolution(Resolution.DAY);
        fromDateDF.setVisible(false);
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new PopupDateField(myUI.getMessage(IndigoMessages.TillDate));
        tillDateDF.setInputPrompt(myUI.getMessage(IndigoMessages.AnyDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setResolution(Resolution.DAY);
        tillDateDF.setVisible(false);
        tillDateDF.addValueChangeListener(this);

        selectAllBtn = new Button(myUI.getMessage(IndigoMessages.AllSchools));
        selectAllBtn.setWidth(Settings.PERCENTS100);
        selectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBtn.addClickListener(this);

        deselectAllBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
        deselectAllBtn.setWidth(Settings.PERCENTS100);
        deselectAllBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBtn.addClickListener(this);

        schoolTable = new FilterTable();
        schoolTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolTable.setSizeFull();
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setMultiSelect(true);
        schoolTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolTable.setFilterBarVisible(true);
        schoolTable.setFooterVisible(false);
        schoolTable.setSelectable(true);

        schoolTable.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolTable.setContainerDataSource(dbs.execSchoolSel(myUI, 0));
            schoolTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        type = new OptionGroup();
        type.setWidth(Settings.PERCENTS100);
        type.addItem(myUI.getMessage(IndigoMessages.Monthly));
        type.addItem(myUI.getMessage(IndigoMessages.Yearly));
        if (currentUser.hasRole(Settings.rnAdmin)) {
            type.addItem(myUI.getMessage(IndigoMessages.Summary));
        }
        type.setValue(myUI.getMessage(IndigoMessages.Monthly));
        type.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        type.addValueChangeListener(this);

        generateBtn = new Button(myUI.getMessage(IndigoMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToPdf));
        makePdfBtn.setWidth(Settings.PERCENTS100);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);
        makePdfBtn.setEnabled(false);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(fromDateDF, 0, 1, 1, 1);
        leftGrid.addComponent(tillDateDF, 2, 1, 3, 1);
        leftGrid.addComponent(educationStatusMCB, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllBtn, 2, 3, 3, 3);
        if (currentUser.hasRole(Settings.rnAdmin)) {
            leftGrid.setSizeFull();
            leftGrid.addComponent(schoolTable, 0, 4, 3, 4);
            leftGrid.setRowExpandRatio(4, 1);
        }
        leftGrid.addComponent(type, 0, 5, 3, 5);
        leftGrid.addComponent(generateBtn, 0, 6, 1, 6);
        leftGrid.addComponent(makePdfBtn, 2, 6);
        leftGrid.addComponent(excelBtn, 3, 6);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        rightLay = new VerticalLayout();
        rightLay.setMargin(true);
        rightLay.setSpacing(true);
        rightLay.setWidth(Settings.PERCENTS100);
        rightLay.setImmediate(true);
        splitPanel.setSecondComponent(rightLay);
    }

    public FormattedTable createTable(String caption) {
        IndexedContainer container = new IndexedContainer();
        if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Yearly))) {
            container.addContainerProperty(myUI.getMessage(IndigoMessages.ClassName), String.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Total_Active), String.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Contract), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Discount), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.DiscountPercentage), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Correction), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.PreviousYearDebt), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.PreviousYearOverpay), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Net), Double.class, null);
        } else if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Summary))) {
            container.addContainerProperty(myUI.getMessage(IndigoMessages.School), String.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Total_Active), String.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Contract), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Discount), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.DiscountPercentage), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Correction), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.PreviousYearDebt), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.PreviousYearOverpay), Double.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Net), Double.class, null);
        } else {
            container.addContainerProperty(myUI.getMessage(IndigoMessages.Month), String.class, null);
            container.addContainerProperty(myUI.getMessage(IndigoMessages.InstPlanDebt), Double.class, null);
        }
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Debt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.OverPay), Double.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Paid), Double.class, null);
        container.addContainerProperty(Settings.percentage, Double.class, 0.0);

        FormattedTable dataTable = new FormattedTable(myUI);
        dataTable.setCaption(caption);
        dataTable.setFooterVisible(true);
        dataTable.setWidth(Settings.PERCENTS100);
        dataTable.setPageLength(container.size());
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.addStyleName("noWrap");
        dataTable.addStyleName("noWrapHeader");
        dataTable.setContainerDataSource(container);
        if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Yearly))
                || type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Summary))) {
            if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Yearly))) {
                dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_YEAR);
            } else {
                dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_SUMMARY);
            }
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Total_Active), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Contract), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Discount), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.DiscountPercentage), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Correction), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.PreviousYearDebt), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.PreviousYearOverpay), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Net), Table.Align.RIGHT);
            dataTable.setColumnWidth(myUI.getMessage(IndigoMessages.ClassName), 100);
            dataTable.setColumnWidth(myUI.getMessage(IndigoMessages.Total_Active), 80);
            dataTable.setColumnWidth(myUI.getMessage(IndigoMessages.PreviousYearOverpay), 80);
        } else {
            dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_MONTH);
            dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.InstPlanDebt), Table.Align.RIGHT);
        }
        dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Debt), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.OverPay), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Paid), Table.Align.RIGHT);
        dataTable.setColumnAlignment(Settings.percentage, Table.Align.RIGHT);
        if (container.size() != 0) {
            makePdfBtn.setEnabled(true);
            excelBtn.setEnabled(true);
        }
        rightLay.addComponent(dataTable);
        return dataTable;
    }

    public void clearLayout() {
        rightLay.removeAllComponents();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (schoolTable.getValue() != null) {
                try {
                    DbStudentContract dbsc = new DbStudentContract();
                    dbsc.connect();
                    String school_ids;
                    if (currentUser.hasRole(Settings.rnAdmin)) {
                        school_ids = Settings.convertCollectionToStr((Set<?>) schoolTable.getValue());
                    } else {
                        school_ids = myUI.getUser().getSchool().getId() + "";
                    }
                    if (school_ids != null) {
                        if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Yearly))) {
                            rightLay.setHeightUndefined();
                            dbsc.execSQL_Yearly_by_classes(myUI, school_ids,
                                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                                    (Integer) yearSelect.getValue(), fromDateDF.getValue(), tillDateDF.getValue(), this);
                        } else if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Summary))) {
                            dbsc.execSQL_Summary_report(myUI, school_ids,
                                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                                    (Integer) yearSelect.getValue(), fromDateDF.getValue(), tillDateDF.getValue(), this);
                            rightLay.setHeight("100%");
                            rightLay.getComponent(0).setHeight("100%");
                        } else {
                            rightLay.setHeightUndefined();
                            dbsc.execSQL_Monthly_by_classes(myUI, school_ids,
                                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                                    (Integer) yearSelect.getValue(), this);
                        }
                    } else {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationNothingIsSelected),
                                Notification.Type.WARNING_MESSAGE);
                    }
                    dbsc.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (rightLay.getComponentCount() != 0) {
                    makePdfBtn.setEnabled(true);
                    excelBtn.setEnabled(true);
                }
            }
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
                        if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Yearly))) {
                            new YearReportPdf(myUI, rightLay, studentInfo);
                        } else if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Monthly))) {
                            new MonthsReportPdf(myUI, rightLay, studentInfo);
                        } else {
                            new SummaryReportPdf(myUI, rightLay, studentInfo);
                        }
                    } else {
                        Notification.show(myUI.getMessage(IndigoMessages.FillSchoolInfo),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.NoAccountant),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            schoolTable.setValue(schoolTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            schoolTable.setValue(null);
        } else if (source == excelBtn) {
            try {
                if (rightLay.getComponentCount() != 0) {
                    int table_count = rightLay.getComponentCount();
                    for (int i = 0; i < table_count; i++) {
                        Table t = ((Table) rightLay.getComponent(i));
                        if (t.getContainerDataSource().size() != 0) {
                            if (excelReport == null) {
                                excelReport = new EnhancedFormatExcelExport(t, t.getCaption());
                            } else {
                                excelReport.setNextTable(t, t.getCaption());
                            }
                            excelReport.setReportTitle(t.getCaption());
                            excelReport.setDisplayTotals(true);
                            excelReport.convertTable();
                            if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Monthly))) {
                                excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(4).setCellValue(
                                        t.getColumnFooter(Settings.percentage));
                            } else {
                                excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(10).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(1).setCellValue(
                                        t.getColumnFooter(myUI.getMessage(IndigoMessages.Total_Active)));
                                excelReport.getTotalsRow().getCell(4).setCellValue(
                                        t.getColumnFooter(myUI.getMessage(IndigoMessages.DiscountPercentage)));
                                excelReport.getTotalsRow().getCell(10).setCellValue(t.getColumnFooter(Settings.percentage));
                            }
                        }
                    }
                    if (excelReport != null) {
                        excelReport.sendConverted();
                        excelReport = null;
                    }
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
        if (property == schoolTable && schoolTable.getValue() != null) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
            rightLay.removeAllComponents();
        } else if (property == yearSelect || property == fromDateDF || property == tillDateDF) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
            rightLay.removeAllComponents();
        } else if (property == type && type.getValue() != null) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
            rightLay.removeAllComponents();
            if (type.getValue().toString().equals(myUI.getMessage(IndigoMessages.Monthly))) {
                fromDateDF.setVisible(false);
                tillDateDF.setVisible(false);
            } else {
                fromDateDF.setVisible(true);
                tillDateDF.setVisible(true);
            }
        }
    }
}
