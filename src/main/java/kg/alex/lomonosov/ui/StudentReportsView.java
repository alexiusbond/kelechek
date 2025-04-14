package kg.alex.lomonosov.ui;

import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.i18n.Messages;
import kg.alex.lomonosov.reports.students.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class StudentReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private final MyVaadinUI myUI;
    private final Subject currentUser = SecurityUtils.getSubject();
    private ComboBox repTypeSelect;
    private GridLayout leftGrid, rightGrid;

    public StudentReportsView(MyVaadinUI myUI) {
        this.myUI = myUI;
        buildGridLayouts();
        this.setSplitPosition(23, Sizeable.Unit.PERCENTAGE);
        this.setStyleName(ValoTheme.SPLITPANEL_LARGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(leftGrid);
        this.setSecondComponent(rightGrid);
    }

    private void buildGridLayouts() {

        leftGrid = new GridLayout(1, 2);
        leftGrid.setMargin(new MarginInfo(true, false, true, true));
        leftGrid.setSpacing(true);
        leftGrid.setSizeFull();

        rightGrid = new GridLayout(2, 2);
        rightGrid.setMargin(true);
        rightGrid.setSpacing(true);
        rightGrid.setWidth(Settings.PERCENTS100);

        repTypeSelect = new ComboBox(myUI.getMessage(Messages.ReportType));
        repTypeSelect.setNullSelectionAllowed(false);
        repTypeSelect.setRequired(true);
        repTypeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        repTypeSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        repTypeSelect.setWidth(Settings.PERCENTS100);
        repTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        repTypeSelect.addValueChangeListener(this);
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmPlanPayments)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.PlanPayments));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassPayments)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.ClassPayments));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassInstPlan)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.ClassInstallmentPlan));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDebtReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.DebtReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassDiscounts)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.ClassDiscounts));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDiscountsReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.DiscountsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassList)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.ClassList));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDebtAndRepayment)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.DebtsAndRepaymentsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmStatusesReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.StatusesReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmYearMonthReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.YearMonthReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmCallsReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.CallsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmOutOfReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.OutOfReport));
        }
        leftGrid.addComponent(repTypeSelect, 0, 0);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == repTypeSelect) {
            this.setSecondComponent(null);
            leftGrid.removeComponent(0, 1);
            if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.PlanPayments))) {
                new InstallmentPlanPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.ClassPayments))) {
                new ClassPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.ClassDiscounts))) {
                new ClassDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.DiscountsReport))) {
                new DiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.SchoolDiscounts))) {
                new SchoolDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.ClassInstallmentPlan))) {
                new ClassInstPlanReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.DebtReport))) {
                new DebtReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.ClassList))) {
                new ClassListReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.DebtsAndRepaymentsReport))) {
                new DebtsAndRepaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.StatusesReport))) {
                new StatusesReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.YearMonthReport))) {
                new YearMonthReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.CallsReport))) {
                new CallsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.OutOfReport))) {
                new OutOfList(myUI, this);
            }
        }
    }

}
