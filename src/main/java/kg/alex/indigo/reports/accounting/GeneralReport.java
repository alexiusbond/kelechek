/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.reports.accounting;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.util.SVGGenerator;
import com.vaadin.data.Property;
import com.vaadin.data.util.ListSet;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbAccTransactions;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.dao.DbStudentContract;
import kg.alex.indigo.domain.ContractInfo;
import kg.alex.indigo.domain.SchoolAccounting;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.pdf.AccountingGeneralReportPdf;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Calendar;
import java.util.*;

public class GeneralReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(GeneralReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final Calendar prevDayCal;
    private Button generateBtn, pdfBtn;
    private GridLayout totalsGrid;
    private VerticalLayout rightLay;
    private FormattedTable transactionsTable, paymentsTable;
    private FilterTable schoolsTable;
    private Chart chartDisc, chartPaid, chartPayments;
    private Configuration confDisc, confPaid, confPayments;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    private String[] NATURAL_COL_ORDER_TRANSACTIONS;
    private String[] NATURAL_COL_ORDER_PAYMENTS;
    private Label outcomeLastDateLbl, outcomeTotalLbl, incTotalLbl, incLastDateLbl, prevBalanceLbl, totalLbl;
    private SchoolAccounting schoolAcc;
    private ContractInfo contractTtl;

    public GeneralReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        prevDayCal = Calendar.getInstance();
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 4);
        leftGrid.setWidth(Settings.PERCENTS100);
        leftGrid.setSpacing(true);

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
            yearSelect.setContainerDataSource(dbd.exec_years_with_dates(myUI));
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

        schoolsTable = new FilterTable();
        schoolsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolsTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolsTable.setSizeFull();
        schoolsTable.setNullSelectionAllowed(false);
        schoolsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolsTable.setFilterBarVisible(true);
        schoolsTable.setFooterVisible(false);
        schoolsTable.setSelectable(true);
        schoolsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolsTable.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolsTable.setContainerDataSource(dbs.execSchoolSel(myUI, 0));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        schoolsTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});

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

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        if (currentUser.hasRole(Settings.rnAdmin)) {
            leftGrid.setSizeFull();
            leftGrid.addComponent(schoolsTable, 0, 2, 3, 2);
            leftGrid.setRowExpandRatio(2, 1);
        } else {
            schoolsTable.setValue(myUI.getUser().getSchool().getId());
        }
        leftGrid.addComponent(generateBtn, 0, 3, 2, 3);
        leftGrid.addComponent(pdfBtn, 3, 3);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);

    }

    private void buildRightLayout() {
        // building grid layout and all tables and charts
        rightLay = new VerticalLayout();
        rightLay.setMargin(true);
        rightLay.setWidth(Settings.PERCENTS100);
        rightLay.setSpacing(true);

        buildSchoolAccounting();
        buildTransactionsTable();
        buildTotalContract();
        buildPaymentsLayout();

        splitPanel.setSecondComponent(rightLay);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (schoolsTable.getValue() != null) {
                try {
                    DbAccTransactions dbacc = new DbAccTransactions();
                    dbacc.connect();
                    schoolAcc = dbacc.exec_get_totals((Integer) schoolsTable.getValue(), 2,
                            ((Date) yearSelect.getContainerProperty(yearSelect.getValue(), myUI.getMessage(IndigoMessages.StartDate)).getValue()),
                            ((Date) yearSelect.getContainerProperty(yearSelect.getValue(), myUI.getMessage(IndigoMessages.TillDate)).getValue()), null);
                    dbacc.close();
                    setSchoolAccounting(schoolAcc);
                    DbStudentContract dbsc = new DbStudentContract();
                    dbsc.connect();
                    contractTtl = dbsc.execSQL_totalsByScl(myUI, (Integer) yearSelect.getValue(),
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            (Integer) schoolsTable.getValue());
                    dbsc.close();
                    setTotalContract(contractTtl);
                    setTransactionTableOptions();
                    setPaymentsTableOptions();
                    setPaymentsChartOptions();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == pdfBtn) {
            try {
                String svgPayments = SVGGenerator.getInstance().generate(confPayments);
                String svgDiscounts = SVGGenerator.getInstance().withWidth(355).withHeight(100).generate(confDisc);
                String svgPaid = SVGGenerator.getInstance().withWidth(400).withHeight(100).generate(confPaid);
                new AccountingGeneralReportPdf(myUI, svgPayments, svgPaid, svgDiscounts, schoolAcc,
                        transactionsTable, contractTtl, paymentsTable, (Integer) schoolsTable.getValue(),
                        (String) yearSelect.getContainerProperty(yearSelect.getValue(), myUI.getMessage(IndigoMessages.Title)).getValue(),
                        Settings.df.format(prevDayCal.getTime()));
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        if (pdfBtn.isEnabled()) {
            setSchoolAccounting(null);
            transactionsTable.setContainerDataSource(null);
            paymentsTable.setContainerDataSource(null);
            setTotalContract(null);
            Configuration c = new Configuration();
            c.getTitle().setText(null);
            chartDisc.drawChart(c);
            chartPaid.drawChart(c);
            chartPayments.getConfiguration().setSeries(new ListSet<>());
            chartPayments.drawChart(c);
            pdfBtn.setEnabled(false);
        }
        if (event.getProperty() == yearSelect) {
            prevDayCal.setTime((Date) yearSelect.getContainerProperty(yearSelect.getValue(), myUI.getMessage(IndigoMessages.StartDate)).getValue());
            prevDayCal.add(Calendar.DAY_OF_MONTH, -1);
            prevBalanceLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.PreviousBalance) + " (" + Settings.df.format(prevDayCal.getTime()) + "): </b>");
        }
    }

    private void buildTotalContract() {
        //labels
        totalsGrid = new GridLayout(4, 9);
        totalsGrid.setWidth(Settings.PERCENTS100);

        Label caption = new Label();
        caption.setWidth(Settings.PERCENTS100);
        caption.setContentMode(ContentMode.HTML);
        caption.setStyleName("tableCpt");
        caption.setValue(myUI.getMessage(IndigoMessages.Total));

        Label discountsCaption = new Label();
        discountsCaption.setWidth(Settings.PERCENTS100);
        discountsCaption.setContentMode(ContentMode.HTML);
        discountsCaption.setStyleName("tableCpt");
        discountsCaption.setValue(myUI.getMessage(IndigoMessages.Discounts));

        Label paymentsCaption = new Label();
        paymentsCaption.setWidth(Settings.PERCENTS100);
        paymentsCaption.setContentMode(ContentMode.HTML);
        paymentsCaption.setStyleName("tableCpt");
        paymentsCaption.setValue(myUI.getMessage(IndigoMessages.Payments));

        Label ttlStudentsLab = new Label();
        ttlStudentsLab.setWidth(Settings.PERCENTS100);
        ttlStudentsLab.setContentMode(ContentMode.HTML);
        ttlStudentsLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlStudentsLab.setValue("<b>" + myUI.getMessage(IndigoMessages.Students) + ":" + "</b>");

        Label ttlContractLab = new Label();
        ttlContractLab.setWidth(Settings.PERCENTS100);
        ttlContractLab.setContentMode(ContentMode.HTML);
        ttlContractLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlContractLab.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalContract) + "</b>");

        Label ttlDebtLab = new Label();
        ttlDebtLab.setWidth(Settings.PERCENTS100);
        ttlDebtLab.setContentMode(ContentMode.HTML);
        ttlDebtLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlDebtLab.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalDebt) + "</b>");

        Label ttlDiscLab = new Label();
        ttlDiscLab.setWidth(Settings.PERCENTS100);
        ttlDiscLab.setContentMode(ContentMode.HTML);
        ttlDiscLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlDiscLab.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalDiscount) + "</b>");

        Label ttlCorrectionLab = new Label();
        ttlCorrectionLab.setWidth(Settings.PERCENTS100);
        ttlCorrectionLab.setContentMode(ContentMode.HTML);
        ttlCorrectionLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlCorrectionLab.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalCorrection) + "</b>");

        Label ttlNetLab = new Label();
        ttlNetLab.setWidth(Settings.PERCENTS100);
        ttlNetLab.setContentMode(ContentMode.HTML);
        ttlNetLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlNetLab.setValue("<b>" + myUI.getMessage(IndigoMessages.Net) + ":" + "</b>");

        Label ttlPaymentLab = new Label();
        ttlPaymentLab.setWidth(Settings.PERCENTS100);
        ttlPaymentLab.setContentMode(ContentMode.HTML);
        ttlPaymentLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlPaymentLab.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalPayment) + "</b>");

        Label ttlLeftLab = new Label();
        ttlLeftLab.setWidth(Settings.PERCENTS100);
        ttlLeftLab.setContentMode(ContentMode.HTML);
        ttlLeftLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlLeftLab.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalLeft) + "</b>");

        totalsGrid.addComponent(caption, 0, 0, 1, 0);
        totalsGrid.addComponent(ttlStudentsLab, 0, 1);
        totalsGrid.addComponent(ttlContractLab, 0, 2);
        totalsGrid.addComponent(ttlDebtLab, 0, 3);
        totalsGrid.addComponent(ttlDiscLab, 0, 4);
        totalsGrid.addComponent(ttlCorrectionLab, 0, 5);
        totalsGrid.addComponent(ttlNetLab, 0, 6);
        totalsGrid.addComponent(ttlPaymentLab, 0, 7);
        totalsGrid.addComponent(ttlLeftLab, 0, 8);
        totalsGrid.addComponent(discountsCaption, 2, 0);
        totalsGrid.addComponent(paymentsCaption, 3, 0);

        //charts 
        //chart Discount
        chartDisc = new Chart(ChartType.PIE);
        chartDisc.setSizeFull();

        confDisc = chartDisc.getConfiguration();
        confDisc.getTitle().setText(null);
        confDisc.getChart().setBackgroundColor(new SolidColor(255, 255, 255, 0));
        confDisc.getTooltip().setEnabled(false);

        PlotOptionsPie plotOptionsDisc = new PlotOptionsPie();
        plotOptionsDisc.setSize("100%");
        DataLabels dataLabelsDisc = new DataLabels();
        dataLabelsDisc.setFormatter("'<b>'+ this.point.name +'</b>'+ this.percentage.toFixed(2) +'%'");
        plotOptionsDisc.setDataLabels(dataLabelsDisc);
        confDisc.setPlotOptions(plotOptionsDisc);

        //chart Payment
        chartPaid = new Chart(ChartType.PIE);
        chartPaid.setSizeFull();

        confPaid = chartPaid.getConfiguration();
        confPaid.getTitle().setText(null);
        confPaid.getChart().setBackgroundColor(new SolidColor(255, 255, 255, 0));
        confPaid.getTooltip().setEnabled(false);

        PlotOptionsPie plotOptionsPay = new PlotOptionsPie();
        plotOptionsPay.setSize("100%");
        DataLabels dataLabelsPay = new DataLabels();
        dataLabelsPay.setFormatter("'<b>'+ this.point.name +'</b>'+ this.percentage.toFixed(2) +'%'");
        plotOptionsPay.setDataLabels(dataLabelsPay);
        confPaid.setPlotOptions(plotOptionsPay);

        totalsGrid.addComponent(chartDisc, 2, 1, 2, 7);
        totalsGrid.addComponent(chartPaid, 3, 1, 3, 7);
        totalsGrid.setColumnExpandRatio(0, 1);
        totalsGrid.setColumnExpandRatio(1, 1);
        totalsGrid.setColumnExpandRatio(2, 3);
        totalsGrid.setColumnExpandRatio(3, 3);

        rightLay.addComponent(totalsGrid);
    }

    private void buildSchoolAccounting() {

        Label caption = new Label();
        caption.setWidth(Settings.PERCENTS100);
        caption.setContentMode(ContentMode.HTML);
        caption.setStyleName("tableCpt");
        caption.setValue(myUI.getMessage(IndigoMessages.AccountingInformationCaption));

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);

        incTotalLbl = new Label();
        incTotalLbl.setContentMode(ContentMode.HTML);
        incTotalLbl.setStyleName(ValoTheme.LABEL_SMALL);
        incTotalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.IncomesTotal) + ": </b>");
        hl.addComponent(incTotalLbl);

        incLastDateLbl = new Label();
        incLastDateLbl.setContentMode(ContentMode.HTML);
        incLastDateLbl.setStyleName(ValoTheme.LABEL_SMALL);
        incLastDateLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.LastIncomeDate) + ": </b>");
        hl.addComponent(incLastDateLbl);

        outcomeTotalLbl = new Label();
        outcomeTotalLbl.setContentMode(ContentMode.HTML);
        outcomeTotalLbl.setStyleName(ValoTheme.LABEL_SMALL);
        outcomeTotalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.ExpensesTotal) + ": </b>");
        hl.addComponent(outcomeTotalLbl);

        outcomeLastDateLbl = new Label();
        outcomeLastDateLbl.setContentMode(ContentMode.HTML);
        outcomeLastDateLbl.setStyleName(ValoTheme.LABEL_SMALL);
        outcomeLastDateLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.LastExpenseDate) + ": </b>");
        hl.addComponent(outcomeLastDateLbl);

        prevBalanceLbl = new Label();
        prevBalanceLbl.setContentMode(ContentMode.HTML);
        prevBalanceLbl.setStyleName(ValoTheme.LABEL_SMALL);
        prevBalanceLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.PreviousBalance) + " (" + Settings.df.format(prevDayCal.getTime())
                + "): </b>");
        hl.addComponent(prevBalanceLbl);

        totalLbl = new Label();
        totalLbl.setContentMode(ContentMode.HTML);
        totalLbl.setStyleName(ValoTheme.LABEL_SMALL);
        totalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.Total) + ": </b>");
        hl.addComponent(totalLbl);

        rightLay.addComponent(caption);
        rightLay.addComponent(hl);
    }

    private void buildTransactionsTable() {

        NATURAL_COL_ORDER_TRANSACTIONS = new String[]{myUI.getMessage(IndigoMessages.Month),
                myUI.getMessage(IndigoMessages.InstallmentPlan), myUI.getMessage(IndigoMessages.Payments),
                myUI.getMessage(IndigoMessages.Incomes),
                myUI.getMessage(IndigoMessages.Expenses), myUI.getMessage(IndigoMessages.Difference)};

        Label caption = new Label();
        caption.setWidth(Settings.PERCENTS100);
        caption.setContentMode(ContentMode.HTML);
        caption.setStyleName("tableCpt");
        caption.setValue(myUI.getMessage(IndigoMessages.IncomeOutcomeMonthlyCaption));

        transactionsTable = new FormattedTable(myUI);
        transactionsTable.setFooterVisible(true);
        transactionsTable.setSizeFull();
        transactionsTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        transactionsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        transactionsTable.addStyleName("noWrapHeader");

        rightLay.addComponent(caption);
        rightLay.addComponent(transactionsTable);
    }

    private void buildPaymentsLayout() {

        NATURAL_COL_ORDER_PAYMENTS = new String[]{myUI.getMessage(IndigoMessages.Month),
                myUI.getMessage(IndigoMessages.InstallmentPlan), myUI.getMessage(IndigoMessages.Payments),
                myUI.getMessage(IndigoMessages.Debt)};

        Label caption = new Label();
        caption.setWidth(Settings.PERCENTS100);
        caption.setContentMode(ContentMode.HTML);
        caption.setStyleName("tableCpt");
        caption.setValue(myUI.getMessage(IndigoMessages.PaymentsMonthlyCaption));
        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);

        paymentsTable = new FormattedTable(myUI);
        paymentsTable.setFooterVisible(true);
        paymentsTable.setSizeFull();
        paymentsTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        paymentsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        paymentsTable.addStyleName("noWrapHeader");

        chartPayments = new Chart();
        chartPayments.setSizeFull();

        confPayments = chartPayments.getConfiguration();
        confPayments.getChart().setType(ChartType.LINE);
        confPayments.getTitle().setText(null);
        confPayments.getChart().setBackgroundColor(new SolidColor(255, 255, 255, 0));
        confPayments.getTooltip().setEnabled(false);

        YAxis yAxis = confPayments.getyAxis();
        yAxis.setMin(0);
        yAxis.setTitle(new AxisTitle(null));

        PlotOptionsLine plotOptions = new PlotOptionsLine();
        plotOptions.getDataLabels().setEnabled(true);
        confPayments.setPlotOptions(plotOptions);

        Legend legend = confPayments.getLegend();
        legend.setLayout(LayoutDirection.HORIZONTAL);
        legend.setAlign(HorizontalAlign.RIGHT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setBorderWidth(0);

        hl.addComponent(paymentsTable);
        hl.addComponent(chartPayments);
        hl.setExpandRatio(paymentsTable, 1.2F);
        hl.setExpandRatio(chartPayments, 1.8F);

        rightLay.addComponent(caption);
        rightLay.addComponent(hl);
    }

    private void setSchoolAccounting(SchoolAccounting schoolAcc) {
        if (schoolAcc != null) {
            incTotalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.IncomesTotal)
                    + ": </b>" + Settings.dFormat2.format(schoolAcc.getTotal_income()) + "$");
            incLastDateLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.LastIncomeDate)
                    + ": </b>" + schoolAcc.getLast_income_date());
            outcomeTotalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.ExpensesTotal)
                    + ": </b>" + Settings.dFormat2.format(schoolAcc.getTotal_outcome()) + "$");
            outcomeLastDateLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.LastExpenseDate)
                    + ": </b>" + schoolAcc.getLast_outcome_date());
            prevBalanceLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.PreviousBalance)
                    + " (" + Settings.df.format(prevDayCal.getTime())
                    + "): </b>" + schoolAcc.getPrevious_balance() + "$");
            totalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.CashBox)
                    + ": </b>" + Settings.dFormat2.format(schoolAcc.getPrevious_balance() + schoolAcc.getTotal_income() - schoolAcc.getTotal_outcome()) + "$");
        } else {
            incTotalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.IncomesTotal) + ": </b>");
            incLastDateLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.LastIncomeDate) + ": </b>");
            outcomeTotalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.ExpensesTotal) + ": </b>");
            outcomeLastDateLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.LastExpenseDate) + ": </b>");
            prevBalanceLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.PreviousBalance) + " (" + Settings.df.format(prevDayCal.getTime()) + "): </b>");
            totalLbl.setValue("<b>" + myUI.getMessage(IndigoMessages.CashBox) + ": </b>");
        }
    }

    private void setTotalContract(ContractInfo contractTtl) {
        if (totalsGrid.getComponent(1, 1) != null) {
            totalsGrid.removeComponent(1, 1);
            totalsGrid.removeComponent(1, 2);
            totalsGrid.removeComponent(1, 3);
            totalsGrid.removeComponent(1, 4);
            totalsGrid.removeComponent(1, 5);
            totalsGrid.removeComponent(1, 6);
            totalsGrid.removeComponent(1, 7);
            totalsGrid.removeComponent(1, 8);
        }
        if (contractTtl != null) {
            totalsGrid.addComponent(new Label(contractTtl.getStudents() + ""), 1, 1);
            totalsGrid.addComponent(new Label(Settings.dFormat2.format(contractTtl.getContract()) + "$"), 1, 2);
            totalsGrid.addComponent(new Label(Settings.dFormat2.format(contractTtl.getDebt()) + "$"), 1, 3);
            totalsGrid.addComponent(new Label(Settings.dFormat2.format(contractTtl.getDiscount()) + "$"), 1, 4);
            totalsGrid.addComponent(new Label(Settings.dFormat2.format(contractTtl.getCorrection()) + "$"), 1, 5);
            totalsGrid.addComponent(new Label(Settings.dFormat2.format(contractTtl.getNet()) + "$"), 1, 6);
            totalsGrid.addComponent(new Label(Settings.dFormat2.format(contractTtl.getPaid()) + "$"), 1, 7);
            totalsGrid.addComponent(new Label(Settings.dFormat2.format(contractTtl.getLeft()) + "$"), 1, 8);
            //draw discounts chart
            double totalDisc = contractTtl.getNet() + contractTtl.getDiscount();
            if (totalDisc != 0.0) {
                final DataSeries series = new DataSeries();
                DataSeriesItem discounts = new DataSeriesItem(myUI.getMessage(IndigoMessages.TotalDiscount), Settings.round(contractTtl.getDiscount() * 100 / totalDisc, 2));
                discounts.setSliced(true);
                series.add(discounts);
                series.add(new DataSeriesItem(myUI.getMessage(IndigoMessages.Net) + ": ", Settings.round(contractTtl.getNet() * 100 / totalDisc, 2)));
                confDisc.setSeries(series);
            }

            chartDisc.drawChart(confDisc);
            //draw Payments chart
            double totalPay = contractTtl.getLeft() + contractTtl.getPaid();
            if (totalPay != 0.0) {
                final DataSeries series = new DataSeries();
                DataSeriesItem discounts = new DataSeriesItem(myUI.getMessage(IndigoMessages.TotalLeft), Settings.round(contractTtl.getLeft() * 100 / totalPay, 2));
                discounts.setSliced(true);
                series.add(discounts);
                series.add(new DataSeriesItem(myUI.getMessage(IndigoMessages.TotalPayment), Settings.round(contractTtl.getPaid() * 100 / totalPay, 2)));
                confPaid.setSeries(series);
            }
            chartPaid.drawChart(confPaid);
        }
    }

    private void setTransactionTableOptions() {
        try {
            DbAccTransactions dbsc = new DbAccTransactions();
            dbsc.connect();
            dbsc.execSQL_Plan_Payments(myUI, (Integer) yearSelect.getValue(),
                    Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                    (Integer) schoolsTable.getValue(),
                    new Date(((Date) yearSelect.getContainerProperty(yearSelect.getValue(), myUI.getMessage(IndigoMessages.StartDate)).getValue()).getTime()),
                    new Date(((Date) yearSelect.getContainerProperty(yearSelect.getValue(), myUI.getMessage(IndigoMessages.TillDate)).getValue()).getTime()),
                    transactionsTable);
            transactionsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_TRANSACTIONS);
            transactionsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.InstallmentPlan), Table.Align.RIGHT);
            transactionsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Payments), Table.Align.RIGHT);
            transactionsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Incomes), Table.Align.RIGHT);
            transactionsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Expenses), Table.Align.RIGHT);
            transactionsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Difference), Table.Align.RIGHT);
            transactionsTable.setPageLength(transactionsTable.size());
            if (transactionsTable.getContainerDataSource().size() != 0) {
                pdfBtn.setEnabled(true);
            }
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setPaymentsTableOptions() {
        paymentsTable.setContainerDataSource(transactionsTable.getContainerDataSource());
        paymentsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PAYMENTS);
        paymentsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.InstallmentPlan), Table.Align.RIGHT);
        paymentsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Payments), Table.Align.RIGHT);
        paymentsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Debt), Table.Align.RIGHT);
        paymentsTable.setPageLength(paymentsTable.size());
        paymentsTable.setColumnFooter(myUI.getMessage(IndigoMessages.InstallmentPlan),
                transactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.InstallmentPlan)));
        paymentsTable.setColumnFooter(myUI.getMessage(IndigoMessages.Payments),
                transactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Payments)));
        paymentsTable.setColumnFooter(myUI.getMessage(IndigoMessages.Debt),
                transactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Debt)));
        if (paymentsTable.getContainerDataSource().size() != 0) {
            pdfBtn.setEnabled(true);
        }
    }

    private void setPaymentsChartOptions() {
        List<String> months = new ArrayList<>();
        List<Number> instList = new ArrayList<>();
        List<Number> paymentsList = new ArrayList<>();
        for (Object next : paymentsTable.getItemIds()) {
            months.add(paymentsTable.getContainerProperty(next, myUI.getMessage(IndigoMessages.Month)).getValue().toString());
            instList.add((Number) paymentsTable.getContainerProperty(next, myUI.getMessage(IndigoMessages.InstallmentPlan)).getValue());
            paymentsList.add((Number) paymentsTable.getContainerProperty(next, myUI.getMessage(IndigoMessages.Payments)).getValue());
        }
        confPayments.getxAxis().setCategories(months.toArray(new String[0]));
        ListSeries ls = new ListSeries();
        ls.setName(myUI.getMessage(IndigoMessages.InstallmentPlan));
        ls.setData(instList);
        confPayments.addSeries(ls);
        ls = new ListSeries();
        ls.setName(myUI.getMessage(IndigoMessages.Payments));
        ls.setData(paymentsList);
        confPayments.addSeries(ls);

        chartPayments.drawChart(confPayments);
    }
}
