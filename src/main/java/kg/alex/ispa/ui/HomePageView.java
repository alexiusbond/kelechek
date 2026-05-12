/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.dao.*;
import kg.alex.ispa.domain.ContractInfo;
import kg.alex.ispa.domain.EducationStatus;
import kg.alex.ispa.domain.EmployeesCount;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.tableexport.EnhancedFormatExcelExport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class HomePageView extends GridLayout implements Button.ClickListener, Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(HomePageView.class);
    private final MyVaadinUI myUI;

    private final Subject currentUser = SecurityUtils.getSubject();
    private Button weekLogBtn, monthLogBtn, allLogBtn, excelBtn;
    private Table logTable;
    private Label logTableCaption;
    private ComboBox logsTypeSelect;
    private ContractInfo tc;

    public HomePageView(MyVaadinUI myUI) {
        this.myUI = myUI;

        this.setColumns(3);
        this.setRows(3);
        this.setMargin(true);
        this.setSizeFull();
        this.setSpacing(true);
        this.setRowExpandRatio(2, 1);
        this.setColumnExpandRatio(0, 4);
        this.setColumnExpandRatio(1, 3);
        this.setColumnExpandRatio(2, 5);

        if (currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmGeneralInfo)) {
            this.addComponent(buildEmpInfo(), 0, 0);
        }
        if (currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmStudentsInfo)) {
            this.addComponent(buildStudEduCount(), 0, 1);
        }
        if (currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmAccountingInfo)) {
            VerticalLayout accLayout = new VerticalLayout();
            accLayout.addComponent(buildWeekPlan());
            accLayout.addComponent(buildMonthPlan());
            accLayout.addComponent(buildTotalContract());
            this.addComponent(accLayout, 1, 0, 1, 1);
            this.addComponent(createChart(), 2, 0, 2, 1);
        }
        if (currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmLogsInfo)
                || currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmAccountingLogsSelect)) {
            this.addComponent(buildLogLayout(), 0, 2, 2, 2);
        }
    }

    public Chart createChart() {
        Chart chart = new Chart(ChartType.PIE);
        chart.setSizeFull();

        Configuration conf = chart.getConfiguration();
        conf.getTitle().setText(null);
        conf.getChart().setBackgroundColor(new SolidColor(255, 255, 255, 0));
        conf.getTooltip().setEnabled(false);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setSize("80%");
        DataLabels dataLabels = new DataLabels();
        dataLabels.setFormatter("'<b>'+ this.point.name +'</b>'+ this.percentage.toFixed(2) +'%'");
        plotOptions.setDataLabels(dataLabels);
        conf.setPlotOptions(plotOptions);
        double total = tc.getContract() + tc.getDebt();

        if (total != 0.0) {
            final DataSeries series = new DataSeries();
            DataSeriesItem payments = new DataSeriesItem(myUI.getMessage(Messages.TotalPayment), Settings.round(tc.getPaid() * 100 / total, 2));
            payments.setSliced(true);
            series.add(payments);
            series.add(new DataSeriesItem(myUI.getMessage(Messages.TotalLeft), Settings.round(tc.getLeft() * 100 / total, 2)));
            series.add(new DataSeriesItem(myUI.getMessage(Messages.TotalDiscount), Settings.round(tc.getDiscount() * 100 / total, 2)));
            conf.setSeries(series);
        }

        chart.drawChart(conf);

        return chart;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == weekLogBtn) {
            setLogTable(6);
            logTableCaption.setValue(myUI.getMessage(Messages.Last7DaysLogs));
        } else if (source == monthLogBtn) {
            setLogTable(30);
            logTableCaption.setValue(myUI.getMessage(Messages.Last30DaysLogs));
        } else if (source == allLogBtn) {
            setLogTable(0);
            logTableCaption.setValue(myUI.getMessage(Messages.ForAllTimeLogs));
        } else if (source == excelBtn) {
            if (logTable.size() > 0) {
                try {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(logTable);
                    excelReport.setReportTitle(logsTypeSelect.getItemCaption(logsTypeSelect.getValue() + " - "
                            + logTableCaption.getValue()));
                    excelReport.setDisplayTotals(false);
                    excelReport.convertTable();
                    excelReport.sendConverted();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        }
    }

    private GridLayout buildEmpInfo() {
        GridLayout layout = new GridLayout(2, 5);
        layout.setWidth(Settings.PERCENTS100);

        Label caption = new Label();
        caption.setContentMode(ContentMode.HTML);
        caption.setValue(myUI.getMessage(Messages.SchoolInformation));
        caption.setStyleName("tableCpt");
        layout.addComponent(caption, 0, 0, 1, 0);

        Label schoolLabCpt = new Label();
        schoolLabCpt.setContentMode(ContentMode.HTML);
        schoolLabCpt.setStyleName(ValoTheme.LABEL_SMALL);
        schoolLabCpt.setValue("<b>" + myUI.getMessage(Messages.School) + ":</b> ");
        layout.addComponent(schoolLabCpt);

        Label schoolLab = new Label();
        schoolLab.setContentMode(ContentMode.HTML);
        schoolLab.setStyleName(ValoTheme.LABEL_SMALL);
        schoolLab.setValue(myUI.getUser().getSchool().getName_ru());
        layout.addComponent(schoolLab);

        EmployeesCount emp = new EmployeesCount();
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            emp = dbe.execSQL(myUI.getUser().getSchool().getId());
            dbe.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        Label directorLabCpt = new Label();
        directorLabCpt.setContentMode(ContentMode.HTML);
        directorLabCpt.setStyleName(ValoTheme.LABEL_SMALL);
        directorLabCpt.setValue("<b>" + myUI.getMessage(Messages.Director) + ":</b> ");
        layout.addComponent(directorLabCpt);

        Label directorLab = new Label();
        directorLab.setContentMode(ContentMode.HTML);
        directorLab.setStyleName(ValoTheme.LABEL_SMALL);
        directorLab.setValue(emp.getDirector());
        layout.addComponent(directorLab);

        Label accountantLabCpt = new Label();
        accountantLabCpt.setContentMode(ContentMode.HTML);
        accountantLabCpt.setStyleName(ValoTheme.LABEL_SMALL);
        accountantLabCpt.setValue("<b>" + myUI.getMessage(Messages.Accountant) + ":</b> ");
        layout.addComponent(accountantLabCpt);

        Label accountantLab = new Label();
        accountantLab.setContentMode(ContentMode.HTML);
        accountantLab.setStyleName(ValoTheme.LABEL_SMALL);
        accountantLab.setValue(emp.getAccountant());
        layout.addComponent(accountantLab);

        Label othersLabCpt = new Label();
        othersLabCpt.setContentMode(ContentMode.HTML);
        othersLabCpt.setStyleName(ValoTheme.LABEL_SMALL);
        othersLabCpt.setValue("<b>" + myUI.getMessage(Messages.OtherUsers) + " (" + emp.getOthers_count()
                + "):</b> ");
        layout.addComponent(othersLabCpt);

        Label othersLab = new Label();
        othersLab.setContentMode(ContentMode.HTML);
        othersLab.setStyleName(ValoTheme.LABEL_SMALL);
        if (emp.getOthers() != null && emp.getOthers().length() > 85) {
            othersLab.setValue(emp.getOthers().substring(0, 80) + "...");
        } else {
            othersLab.setValue(emp.getOthers());
        }
        layout.addComponent(othersLab);
        layout.setColumnExpandRatio(0, 2);
        layout.setColumnExpandRatio(1, 3);
        return layout;
    }

    private GridLayout buildWeekPlan() {
        GridLayout layout = new GridLayout(2, 3);
        layout.setWidth(Settings.PERCENTS100);

        Label caption = new Label();
        caption.setContentMode(ContentMode.HTML);
        caption.setValue(myUI.getMessage(Messages.ForThisWeek));
        caption.setStyleName("tableCpt");
        layout.addComponent(caption, 0, 0, 1, 0);

        String week_plan = null;
        String week_paid = null;
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbip.connect();
            dbsp.connect();
            week_plan = dbip.execGetWeeklyPlan(myUI.getMessage(Messages.Students),
                    myUI.getUser().getSchool().getId(),
                    myUI.getUser().getCurrent_year().getId());
            week_paid = dbsp.execGetWeeklyPaid(myUI.getMessage(Messages.Students),
                    myUI.getUser().getSchool().getId(),
                    myUI.getUser().getCurrent_year().getId());
            dbip.close();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        Label instPlanWeekLab = new Label();
        instPlanWeekLab.setContentMode(ContentMode.HTML);
        instPlanWeekLab.setStyleName(ValoTheme.LABEL_SMALL);
        instPlanWeekLab.setValue("<b>" + myUI.getMessage(Messages.InstallmentPlan) + ":</b>");

        Label paymentsWeekLab = new Label();
        paymentsWeekLab.setContentMode(ContentMode.HTML);
        paymentsWeekLab.setStyleName(ValoTheme.LABEL_SMALL);
        paymentsWeekLab.setValue("<b>" + myUI.getMessage(Messages.Payments) + ":</b>");

        layout.addComponent(instPlanWeekLab);
        layout.addComponent(new Label(week_plan + " " + Settings.USD));
        layout.addComponent(paymentsWeekLab);
        layout.addComponent(new Label(week_paid + " " + Settings.USD));
        return layout;
    }

    private GridLayout buildMonthPlan() {
        GridLayout layout = new GridLayout(2, 3);
        layout.setWidth(Settings.PERCENTS100);

        Label caption = new Label();
        caption.setContentMode(ContentMode.HTML);
        caption.setValue(myUI.getMessage(Messages.ForThisMonth));
        caption.setStyleName("tableCpt");
        layout.addComponent(caption, 0, 0, 1, 0);

        String month_plan = null;
        String month_paid = null;
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbip.connect();
            dbsp.connect();
            month_plan = dbip.execGetMonthlyPlan(myUI.getMessage(Messages.Students),
                    myUI.getUser().getSchool().getId(),
                    myUI.getUser().getCurrent_year().getId());
            month_paid = dbsp.execGetMonthlyPaid(myUI.getMessage(Messages.Students),
                    myUI.getUser().getSchool().getId(),
                    myUI.getUser().getCurrent_year().getId());
            dbip.close();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        Label instPlanMonthLab = new Label();
        instPlanMonthLab.setContentMode(ContentMode.HTML);
        instPlanMonthLab.setStyleName(ValoTheme.LABEL_SMALL);
        instPlanMonthLab.setValue("<b>" + myUI.getMessage(Messages.InstallmentPlan) + ":</b>");

        Label paymentsMonthLab = new Label();
        paymentsMonthLab.setContentMode(ContentMode.HTML);
        paymentsMonthLab.setStyleName(ValoTheme.LABEL_SMALL);
        paymentsMonthLab.setValue("<b>" + myUI.getMessage(Messages.Payments) + ":</b>");

        layout.addComponent(instPlanMonthLab);
        layout.addComponent(new Label(month_plan + " " + Settings.USD));
        layout.addComponent(paymentsMonthLab);
        layout.addComponent(new Label(month_paid + " " + Settings.USD));
        return layout;
    }

    private GridLayout buildStudEduCount() {
        GridLayout layout = new GridLayout(2, 7);
        layout.setWidth(Settings.PERCENTS100);

        Label caption = new Label();
        caption.setContentMode(ContentMode.HTML);
        caption.setStyleName("tableCpt");
        caption.setValue(myUI.getMessage(Messages.StudentsInformation));

        Label ttlStudLab = new Label();
        ttlStudLab.setContentMode(ContentMode.HTML);
        ttlStudLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlStudLab.setValue("<b>" + myUI.getMessage(Messages.TotalStudents) + "</b>");

        Label activeLab = new Label();
        activeLab.setContentMode(ContentMode.HTML);
        activeLab.setStyleName(ValoTheme.LABEL_SMALL);
        activeLab.setValue("<b>" + myUI.getMessage(Messages.Active) + "</b>");

        Label preRegLab = new Label();
        preRegLab.setContentMode(ContentMode.HTML);
        preRegLab.setStyleName(ValoTheme.LABEL_SMALL);
        preRegLab.setValue("<b>" + myUI.getMessage(Messages.PreRegistered) + "</b>");

        Label notConfLab = new Label();
        notConfLab.setContentMode(ContentMode.HTML);
        notConfLab.setStyleName(ValoTheme.LABEL_SMALL);
        notConfLab.setValue("<b>" + myUI.getMessage(Messages.NotConfirmed) + "</b>");

        Label outOfLab = new Label();
        outOfLab.setContentMode(ContentMode.HTML);
        outOfLab.setStyleName(ValoTheme.LABEL_SMALL);
        outOfLab.setValue("<b>" + myUI.getMessage(Messages.OutOf) + "</b>");

        Label graduatedLab = new Label();
        graduatedLab.setContentMode(ContentMode.HTML);
        graduatedLab.setStyleName(ValoTheme.LABEL_SMALL);
        graduatedLab.setValue("<b>" + myUI.getMessage(Messages.Graduated) + "</b>");

        EducationStatus ed = new EducationStatus();
        try {
            DbStudent dbst = new DbStudent();
            dbst.connect();
            ed = dbst.execEduCount(myUI.getUser().getSchool().getId(), myUI.getUser().getCurrent_year().getId());
            dbst.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        layout.addComponent(caption, 0, 0, 1, 0);
        layout.addComponent(ttlStudLab);
        layout.addComponent(new Label(ed.getTotal()));
        layout.addComponent(activeLab);
        layout.addComponent(new Label(ed.getActive()));
        layout.addComponent(preRegLab);
        layout.addComponent(new Label(ed.getPre_registered()));
        layout.addComponent(notConfLab);
        layout.addComponent(new Label(ed.getNot_confirmed()));
        layout.addComponent(outOfLab);
        layout.addComponent(new Label(ed.getOutOf()));
        layout.addComponent(graduatedLab);
        layout.addComponent(new Label(ed.getGraduated()));
        layout.setColumnExpandRatio(0, 2);
        layout.setColumnExpandRatio(1, 3);
        return layout;
    }

    private GridLayout buildTotalContract() {
        GridLayout layout = new GridLayout(2, 7);
        layout.setWidth(Settings.PERCENTS100);

        Label caption = new Label();
        caption.setContentMode(ContentMode.HTML);
        caption.setStyleName("tableCpt");
        caption.setValue(myUI.getMessage(Messages.TotalContractStudents));

        Label ttlContractLab = new Label();
        ttlContractLab.setContentMode(ContentMode.HTML);
        ttlContractLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlContractLab.setValue("<b>" + myUI.getMessage(Messages.TotalContract) + "</b>");

        Label ttlDiscLab = new Label();
        ttlDiscLab.setContentMode(ContentMode.HTML);
        ttlDiscLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlDiscLab.setValue("<b>" + myUI.getMessage(Messages.TotalDiscount) + "</b>");

        Label ttlCorrectionLab = new Label();
        ttlCorrectionLab.setContentMode(ContentMode.HTML);
        ttlCorrectionLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlCorrectionLab.setValue("<b>" + myUI.getMessage(Messages.TotalCorrection) + "</b>");

        Label ttlNetLab = new Label();
        ttlNetLab.setContentMode(ContentMode.HTML);
        ttlNetLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlNetLab.setValue("<b>" + myUI.getMessage(Messages.Net) + "</b>");

        Label ttlPaymentLab = new Label();
        ttlPaymentLab.setContentMode(ContentMode.HTML);
        ttlPaymentLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlPaymentLab.setValue("<b>" + myUI.getMessage(Messages.TotalPayment) + "</b>");

        Label ttlLeftLab = new Label();
        ttlLeftLab.setContentMode(ContentMode.HTML);
        ttlLeftLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlLeftLab.setValue("<b>" + myUI.getMessage(Messages.TotalLeft) + "</b>");

        Label ttlDebtLab = new Label();
        ttlDebtLab.setContentMode(ContentMode.HTML);
        ttlDebtLab.setStyleName(ValoTheme.LABEL_SMALL);
        ttlDebtLab.setValue("<b>" + myUI.getMessage(Messages.TotalDebt) + "</b>");

        try {
            DbStudentContract dbsc = new DbStudentContract();
            dbsc.connect();
            tc = dbsc.execSQLTotals(myUI.getUser().getSchool().getId(),
                    myUI.getUser().getCurrent_year().getId());
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        layout.addComponent(caption, 0, 0, 1, 0);
        layout.addComponent(ttlContractLab);
        layout.addComponent(new Label(Settings.dFormat2.format(tc.getContract()) + " " + Settings.USD));
        layout.addComponent(ttlDiscLab);
        layout.addComponent(new Label(Settings.dFormat2.format(tc.getDiscount()) + " " + Settings.USD));
        layout.addComponent(ttlCorrectionLab);
        layout.addComponent(new Label(Settings.dFormat2.format(tc.getCorrection()) + " " + Settings.USD));
        layout.addComponent(ttlNetLab);
        layout.addComponent(new Label(Settings.dFormat2.format(tc.getNet()) + " " + Settings.USD));
        layout.addComponent(ttlPaymentLab);
        layout.addComponent(new Label(Settings.dFormat2.format(tc.getPaid()) + " " + Settings.USD));
        layout.addComponent(ttlLeftLab);
        layout.addComponent(new Label(Settings.dFormat2.format(tc.getLeft()) + " " + Settings.USD));
        layout.addComponent(ttlDebtLab);
        layout.addComponent(new Label(Settings.dFormat2.format(tc.getDebt()) + " " + Settings.USD));
        return layout;
    }

    private VerticalLayout buildLogLayout() {

        VerticalLayout layout = new VerticalLayout();
        layout.setWidth(Settings.PERCENTS100);
        layout.setHeight("100%");
        layout.setSpacing(true);

        logTableCaption = new Label();
        logTableCaption.setContentMode(ContentMode.HTML);
        logTableCaption.setStyleName("tableCpt");
        logTableCaption.setValue(myUI.getMessage(Messages.Last7DaysLogs));

        logTable = new Table();
        logTable.setStyleName(ValoTheme.TABLE_SMALL);
        logTable.setStyleName(ValoTheme.TABLE_SMALL);
        logTable.addStyleName("noWrap");
        logTable.setSizeFull();
        logTable.setImmediate(true);
        logTable.setSelectable(false);

        HorizontalLayout logButtonsLayout = new HorizontalLayout();
        logButtonsLayout.setWidth(Settings.PERCENTS100);
        logButtonsLayout.setSpacing(true);

        logsTypeSelect = new ComboBox();
        logsTypeSelect.setNullSelectionAllowed(false);
        logsTypeSelect.setWidth(Settings.PERCENTS100);
        logsTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        logsTypeSelect.addItem(myUI.getMessage(Messages.SystemLogs));
        if (currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmAccountingLogsSelect)) {
            logsTypeSelect.addItem(myUI.getMessage(Messages.AccountingLogs));
        }
        logsTypeSelect.setValue(myUI.getMessage(Messages.SystemLogs));
        logsTypeSelect.addValueChangeListener(this);

        weekLogBtn = new Button(myUI.getMessage(Messages.WeekLogs));
        weekLogBtn.setWidth(Settings.PERCENTS100);
        weekLogBtn.addClickListener(this);

        monthLogBtn = new Button(myUI.getMessage(Messages.MonthLogs));
        monthLogBtn.setWidth(Settings.PERCENTS100);
        monthLogBtn.addClickListener(this);

        allLogBtn = new Button(myUI.getMessage(Messages.AllLogs));
        allLogBtn.setWidth(Settings.PERCENTS100);
        allLogBtn.addClickListener(this);

        excelBtn = new Button(myUI.getMessage(Messages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        setLogTable(6);

        logButtonsLayout.addComponent(logsTypeSelect);
        logButtonsLayout.addComponent(weekLogBtn);
        logButtonsLayout.addComponent(monthLogBtn);
        logButtonsLayout.addComponent(allLogBtn);
        logButtonsLayout.addComponent(excelBtn);

        logTable.setColumnExpandRatio(myUI.getMessage(Messages.Action), 1);
        layout.addComponent(logTableCaption);
        layout.addComponent(logTable);
        layout.addComponent(logButtonsLayout);
        layout.setExpandRatio(logTable, 1);
        return layout;
    }

    private void setLogTable(int days_int) {
        try {
            DbLog dblo = new DbLog();
            dblo.connect();
            logTable.setContainerDataSource(dblo.execSQL(myUI, myUI.getUser().getSchool().getId(), days_int,
                    logsTypeSelect.getValue().toString()));
            dblo.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == logsTypeSelect) {
            try {
                DbLog dblo = new DbLog();
                dblo.connect();
                logTable.setContainerDataSource(dblo.execSQL(myUI, myUI.getUser().getSchool().getId(), 6,
                        logsTypeSelect.getValue().toString()));
                logTableCaption.setValue(myUI.getMessage(Messages.Last7DaysLogs));
                dblo.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
