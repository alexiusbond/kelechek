/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.dao.DbDefinition;
import kg.alex.ispa.dao.DbStudentContract;
import kg.alex.ispa.domain.Month;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.tableexport.EnhancedFormatExcelExport;
import kg.alex.ispa.utils.FormattedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class YearMonthReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(YearMonthReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final String[] NATURAL_COL_ORDER_MONTH;
    private final List<String> year_reports_props = new ArrayList<>();
    public VerticalLayout rightLay;
    public int totalStudents = 0, totalActive = 0;
    public double contracts = 0.0, discounts = 0.0, prevYearDebts = 0.0, prevYearOverpays = 0.0, corrections = 0.0, nets = 0.0,
            paid_amounts = 0.0, debts = 0.0, overpays = 0.0, inst_plans = 0.0;
    private Button generateBtn, excelBtn;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    private EnhancedFormatExcelExport excelReport;
    private OptionGroup type;
    private List<Month> months;

    public YearMonthReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
        year_reports_props.add(myUI.getMessage(Messages.ClassName));
        year_reports_props.add(myUI.getMessage(Messages.Total_Active));
        year_reports_props.add(myUI.getMessage(Messages.Contract));
        year_reports_props.add(myUI.getMessage(Messages.Discount));
        year_reports_props.add(myUI.getMessage(Messages.DiscountPercentage));
        year_reports_props.add(myUI.getMessage(Messages.Correction));
        year_reports_props.add(myUI.getMessage(Messages.PreviousYearDebt));
        year_reports_props.add(myUI.getMessage(Messages.PreviousYearOverpay));
        year_reports_props.add(myUI.getMessage(Messages.Net));
        year_reports_props.add(myUI.getMessage(Messages.Paid));
        year_reports_props.add(Settings.percentage);
        year_reports_props.add(myUI.getMessage(Messages.Debt));
        year_reports_props.add(myUI.getMessage(Messages.OverPay));
        try {
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            months = dbCon.exec_months();
            dbCon.close();
            for (Month month : months) {
                year_reports_props.add(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName());
                year_reports_props.add(myUI.getMessage(Messages.Payments) + " " + month.getName());
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        NATURAL_COL_ORDER_MONTH = new String[]{myUI.getMessage(Messages.Month),
                myUI.getMessage(Messages.InstPlanDebt),
                myUI.getMessage(Messages.Paid),
                Settings.percentage,
                myUI.getMessage(Messages.Debt),
                myUI.getMessage(Messages.OverPay)};
    }

    private void buildLeftPanel() {
        GridLayout leftGrid = new GridLayout(4, 4);
        leftGrid.setSpacing(true);
        leftGrid.setWidth(Settings.PERCENTS100);

        yearSelect = new ComboBox(myUI.getMessage(Messages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

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

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        type = new OptionGroup();
        type.addItem(myUI.getMessage(Messages.Monthly));
        type.addItem(myUI.getMessage(Messages.Yearly));
        type.setValue(myUI.getMessage(Messages.Monthly));
        type.setStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        type.addValueChangeListener(this);
        type.setSizeFull();

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

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftGrid.addComponent(type, 0, 2, 3, 2);
        leftGrid.addComponent(generateBtn, 0, 3, 2, 3);
        leftGrid.addComponent(excelBtn, 3, 3);
        leftGrid.setRowExpandRatio(2, 1);
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
        if (type.getValue().toString().equals(myUI.getMessage(Messages.Yearly))) {
            container.addContainerProperty(myUI.getMessage(Messages.ClassName), String.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.Total_Active), String.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.Contract), Double.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.Discount), Double.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.DiscountPercentage), Double.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.Correction), Double.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.PreviousYearDebt), Double.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.PreviousYearOverpay), Double.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.Net), Double.class, null);
        } else {
            container.addContainerProperty(myUI.getMessage(Messages.Month), String.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.InstPlanDebt), Double.class, null);
        }
        container.addContainerProperty(myUI.getMessage(Messages.Debt), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.OverPay), Double.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Paid), Double.class, null);
        container.addContainerProperty(Settings.percentage, Double.class, 0.0);
        for (Month month : months) {
            container.addContainerProperty(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName(), Double.class, null);
            container.addContainerProperty(myUI.getMessage(Messages.Payments) + " " + month.getName(), Double.class, null);
        }

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
        if (type.getValue().toString().equals(myUI.getMessage(Messages.Yearly))) {
            dataTable.setVisibleColumns(year_reports_props.toArray());
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Total_Active), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Contract), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Discount), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.DiscountPercentage), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Correction), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.PreviousYearDebt), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.PreviousYearOverpay), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Net), Table.Align.RIGHT);
            dataTable.setColumnWidth(myUI.getMessage(Messages.ClassName), 100);
            dataTable.setColumnWidth(myUI.getMessage(Messages.Total_Active), 80);
            dataTable.setColumnWidth(myUI.getMessage(Messages.PreviousYearOverpay), 80);
            for (Month month : months) {
                dataTable.setColumnAlignment(myUI.getMessage(Messages.Payments) + " " + month.getName(), Table.Align.RIGHT);
                dataTable.setColumnAlignment(myUI.getMessage(Messages.InstPlanDebt) + " " + month.getName(), Table.Align.RIGHT);
            }
        } else {
            dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_MONTH);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.InstPlanDebt), Table.Align.RIGHT);
        }
        dataTable.setColumnAlignment(myUI.getMessage(Messages.Debt), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(Messages.OverPay), Table.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(Messages.Paid), Table.Align.RIGHT);
        dataTable.setColumnAlignment(Settings.percentage, Table.Align.RIGHT);
        if (container.size() != 0) {
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
            try {
                for (Month month : months) {
                    month.setTotalPayments(0);
                    month.setTotalInstallments(0);
                }
                DbStudentContract dbsc = new DbStudentContract();
                dbsc.connect();
                String school_ids;
                school_ids = myUI.getUser().getSchool().getId() + "";
                if (type.getValue().toString().equals(myUI.getMessage(Messages.Yearly))) {
                    rightLay.setHeightUndefined();
                    dbsc.execSQL_Yearly_by_classes(myUI, school_ids,
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            (Integer) yearSelect.getValue(), this);
                } else {
                    rightLay.setHeightUndefined();
                    dbsc.execSQL_Monthly_by_classes(myUI, school_ids,
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            (Integer) yearSelect.getValue(), this);
                }
                dbsc.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            if (rightLay.getComponentCount() != 0) {
                excelBtn.setEnabled(true);
            }
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
                            if (type.getValue().toString().equals(myUI.getMessage(Messages.Monthly))) {
                                excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(4).setCellValue(
                                        t.getColumnFooter(Settings.percentage));
                            } else {
                                excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(4).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(10).setCellFormula(null);
                                excelReport.getTotalsRow().getCell(1).setCellValue(
                                        t.getColumnFooter(myUI.getMessage(Messages.Total_Active)));
                                excelReport.getTotalsRow().getCell(4).setCellValue(
                                        t.getColumnFooter(myUI.getMessage(Messages.DiscountPercentage)));
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

    public List<Month> getMonths() {
        return months;
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == yearSelect) {
            excelBtn.setEnabled(false);
            rightLay.removeAllComponents();
        } else if (property == type && type.getValue() != null) {
            excelBtn.setEnabled(false);
            rightLay.removeAllComponents();
        }
    }
}
