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
import kg.alex.indigo.reports.accounting.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class AccountingReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private final MyVaadinUI myUI;
    private ComboBox repTypeSelect;
    private GridLayout leftGrid, rightGrid;
    private final Subject currentUser = SecurityUtils.getSubject();

    public AccountingReportsView(MyVaadinUI myUI) {
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
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmMonthReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.MonthReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmByDateReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.ByDateReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmSchoolAccountingReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.SchoolAccountingReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmGeneralReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.GeneralAccountingReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmCurrentAccountStatement)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.CurrentAccountStatementReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmIncomeExpenseAccountStatement)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.IncomeExpenseAccountStatementReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmSalariesReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.SalariesReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmIncomesExpensesReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.IncomesExpensesReport));
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmAccountingBalanceReport)) {
            repTypeSelect.addItem(myUI.getMessage(IndigoMessages.AccountingBalanceReport));
        }
        leftGrid.addComponent(repTypeSelect, 0, 0);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == repTypeSelect) {
            this.setSecondComponent(null);
            leftGrid.removeComponent(0, 1);
            if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.MonthReport))) {
                new MonthReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.SchoolAccountingReport))) {
                new SchoolsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.ByDateReport))) {
                new DateReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.GeneralAccountingReport))) {
                new GeneralReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.CurrentAccountStatementReport))) {
                new CurrentAccountStatementReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.IncomeExpenseAccountStatementReport))) {
                new IncomeExpenseAccountStatementReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.SalariesReport))) {
                new PayoutsReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.IncomesExpensesReport))) {
                new IncomesExpensesReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(IndigoMessages.AccountingBalanceReport))) {
                new BalanceReport(myUI, this);
            }
        }
    }

}
