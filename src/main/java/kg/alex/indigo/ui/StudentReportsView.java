package kg.alex.indigo.ui;

import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.reports.students.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class StudentReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private final MyVaadinUI myUI;
    private ComboBox repTypeSelect;

    private GridLayout leftGrid, rightGrid;
    private final Subject currentUser = SecurityUtils.getSubject();

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

        repTypeSelect = new ComboBox(myUI.getMessage(IndigoMessages.ReportType));
        repTypeSelect.setNullSelectionAllowed(false);
        repTypeSelect.setRequired(true);
        repTypeSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        repTypeSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        repTypeSelect.setWidth(Settings.PERCENTS100);
        repTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        repTypeSelect.addValueChangeListener(this);
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmPlanPayments)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.PlanPayments));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassPayments)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.ClassPayments));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassInstPlan)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.ClassInstallmentPlan));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDebtReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.DebtReport));
        }
        /*if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmSchoolDiscounts)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.SchoolDiscounts));
        }*/
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassDiscounts)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.ClassDiscounts));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDiscountsReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.DiscountsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmClassList)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.ClassList));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmDebtAndRepayment)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.DebtsAndRepaymentsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmStatusesReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.StatusesReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmYearMonthReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.YearMonthReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmCallsReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.CallsReport));
        }
        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmOutOfReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.OutOfReport));
        }
        leftGrid.addComponent(repTypeSelect, 0, 0);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == repTypeSelect) {
            this.setSecondComponent(null);
            leftGrid.removeComponent(0, 1);
            if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.PlanPayments))) {
                new InstallmentPlanPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.ClassPayments))) {
                new ClassPaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.ClassDiscounts))) {
                new ClassDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.DiscountsReport))) {
                new DiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.SchoolDiscounts))) {
                new SchoolDiscountsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.ClassInstallmentPlan))) {
                new ClassInstPlanReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.DebtReport))) {
                new DebtReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.ClassList))) {
                new ClassListReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.DebtsAndRepaymentsReport))) {
                new DebtsAndRepaymentsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.StatusesReport))) {
                new StatusesReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.YearMonthReport))) {
                new YearMonthReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.CallsReport))) {
                new CallsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.OutOfReport))) {
                new OutOfList(myUI, this);
            }
        }
    }

}
