package kg.alex.indigo;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.dao.*;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.reports.students.BankPaymentsByDateReport;
import kg.alex.indigo.ui.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

public class AuthenticatedScreen extends VerticalLayout implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(AuthenticatedScreen.class);
    private final MyVaadinUI myUI;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final VerticalSplitPanel verticalPanel;
    private final Button changePassBtn;
    public ComboBox yearSelect, schoolSelect;
    private final Label header = new Label();
    private Label infoLabel;

    public AuthenticatedScreen(MyVaadinUI myUi) {

        super();
        this.myUI = myUi;

        verticalPanel = new VerticalSplitPanel();
        verticalPanel.setSplitPosition(97, Sizeable.Unit.PIXELS);
        verticalPanel.setSizeFull();
        verticalPanel.setLocked(true);

        setSizeFull();
        setSpacing(true);
        if (!currentUser.hasRole(Settings.rnBank)) {
            try {
                myUI.workingDetails(currentUser);
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
            if (currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmMenu)) {
                verticalPanel.setSecondComponent(new HomePageView(myUI));
            }
        } else {
            verticalPanel.setSecondComponent(new BankPaymentsByDateReport(myUI));
        }

        header.setSizeUndefined();
        header.setStyleName("mylabel");
        header.setImmediate(true);
        header.setValue((myUI.getMessage(IndigoMessages.Welcome)).toUpperCase());

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        myUI.setMessagesBtn(new Button(myUI.getMessage(IndigoMessages.Messages)));
        myUI.getMessagesBtn().setImmediate(true);
        myUI.getMessagesBtn().addClickListener(this);

        changePassBtn = new Button(myUI.getMessage(IndigoMessages.ChangePasswordButton));
        changePassBtn.setStyleName(ValoTheme.BUTTON_LINK);
        changePassBtn.setIcon(FontAwesome.KEY);
        changePassBtn.addClickListener(this);
        if (!currentUser.hasRole(Settings.rnBank)) {
            hl.addComponent(myUI.getMessagesBtn());
            myUI.repaintMessagesButton();
            hl.addComponent(changePassBtn);
        }

        Button logout = new Button(myUi.getMessage(IndigoMessages.LogoutButton));
        logout.addClickListener(new MyVaadinUI.LogoutListener(this.myUI));
        logout.setStyleName(ValoTheme.BUTTON_PRIMARY);
        logout.addStyleName(ValoTheme.BUTTON_SMALL);
        logout.setIcon(FontAwesome.SIGN_OUT);

        GridLayout upperLay = new GridLayout(3, 3);
        upperLay.setSizeFull();
        upperLay.setSpacing(false);
        if (!currentUser.hasRole(Settings.rnBank)) {
            upperLay.addComponent(buildInfoLay(), 0, 0, 2, 0);
        }
        upperLay.addComponent(hl, 0, 1);
        upperLay.setComponentAlignment(hl, Alignment.MIDDLE_LEFT);
        upperLay.addComponent(header, 1, 1);
        upperLay.setComponentAlignment(header, Alignment.MIDDLE_CENTER);
        upperLay.addComponent(logout, 2, 1);
        upperLay.setComponentAlignment(logout, Alignment.MIDDLE_RIGHT);
        upperLay.addComponent(buildMenu(), 0, 2, 2, 2);
        upperLay.setColumnExpandRatio(0, 2.5f);
        upperLay.setColumnExpandRatio(1, 7.5f);
        upperLay.setColumnExpandRatio(2, 1);
        upperLay.setRowExpandRatio(2, 1);

        Label warning;
        if (myUi.getUser() != null && myUi.getUser().getWorking_status_id() == 1) {
            warning = new Label(myUI.getMessage(IndigoMessages.SystemClosedNotification));
            warning.setSizeUndefined();
            warning.setStyleName("mylabel");
            changePassBtn.setEnabled(false);
            upperLay.removeComponent(header);
            upperLay.addComponent(warning, 1, 1);
        }

        verticalPanel.setFirstComponent(upperLay);
        this.addComponent(verticalPanel);
    }

    private HorizontalLayout buildInfoLay() {

        infoLabel = new Label();
        infoLabel.setSizeUndefined();
        infoLabel.setContentMode(ContentMode.HTML);
        infoLabel.setStyleName("labelInfo");
        updateInfo();

        Label schoolLabel = new Label();
        schoolLabel.setSizeUndefined();
        schoolLabel.setContentMode(ContentMode.HTML);
        schoolLabel.setStyleName("labelInfo");
        schoolLabel.setValue("<i class=\"fa fa-university fa-inverse\"></i><b> "
                + myUI.getMessage(IndigoMessages.School) + ": </b>");

        schoolSelect = new ComboBox();
        schoolSelect.setWidth(Settings.PERCENTS100);
        schoolSelect.setImmediate(true);
        schoolSelect.setEnabled(currentUser.isPermitted(Settings.prmChangeSchool + ":" + Settings.actModify) ||
                myUI.getUser().getPosition_id() == 116);
        schoolSelect.setNullSelectionAllowed(false);
        schoolSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        schoolSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        schoolSelect.setContainerDataSource(myUI.getSchoolCont());
        schoolSelect.setValue(myUI.getUser().getSchool().getId());
        schoolSelect.setFilteringMode(FilteringMode.CONTAINS);
        schoolSelect.addValueChangeListener(this);

        HorizontalLayout schHl = new HorizontalLayout();
        schHl.setWidth(Settings.PERCENTS100);
        schHl.setSpacing(true);
        schHl.addComponent(schoolLabel);
        schHl.addComponent(schoolSelect);
        schHl.setExpandRatio(schoolSelect, 1);

        Label yearLabel = new Label();
        yearLabel.setSizeUndefined();
        yearLabel.setContentMode(ContentMode.HTML);
        yearLabel.setStyleName("labelInfo");
        yearLabel.setValue("<i class=\"fa fa-calendar fa-inverse\"></i><b> "
                + myUI.getMessage(IndigoMessages.Year) + ": </b>");

        yearSelect = new ComboBox();
        yearSelect.setWidth("65%");
        yearSelect.setEnabled(currentUser.isPermitted(Settings.prmChangeYear + ":" + Settings.actModify));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        setYearSel(myUI.getUser().getCurrent_year().getId());

        HorizontalLayout yearHl = new HorizontalLayout();
        yearHl.setSpacing(true);
        yearHl.setWidth(Settings.PERCENTS100);
        yearHl.addComponent(yearLabel);
        yearHl.addComponent(yearSelect);
        yearHl.setExpandRatio(yearSelect, 1);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setWidth(Settings.PERCENTS100);
        hl.setMargin(new MarginInfo(false, true, false, true));
        hl.setStyleName("loginLayout");
        hl.addComponent(infoLabel);
        hl.addComponent(yearHl);
        hl.addComponent(schHl);
        return hl;
    }

    public void updateInfo() {
        infoLabel.setValue("<i class=\"fa fa-user fa-inverse\"></i><b> "
                + myUI.getMessage(IndigoMessages.LogInAsLabel) + ": </b>"
                + myUI.getUser().getFullName());
    }

    private MenuBar buildMenu() {
        final MenuBar menubar = new MenuBar();
        menubar.setSizeFull();
        menubar.setHeight("35px");
        menubar.setAutoOpen(true);

        MenuBar.MenuItem mi;
        if (currentUser.isPermitted(Settings.cnHomePageView + ":" + Settings.prmMenu)) {
            menubar.addItem(myUI.getMessage(IndigoMessages.HomePage), menuCommand);
        }
        if (!currentUser.hasRole(Settings.rnBank)) {
            menubar.addItem(myUI.getMessage(IndigoMessages.MyInfo), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmMenu)) {
            menubar.addItem(myUI.getMessage(IndigoMessages.EmployeeDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmMenu)) {
            menubar.addItem(myUI.getMessage(IndigoMessages.StudentDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnTransactionsView + ":" + Settings.prmMenu)) {
            menubar.addItem(myUI.getMessage(IndigoMessages.CashBox), menuCommand);
        }

        if (currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmMenu) ||
                currentUser.isPermitted(Settings.cnReportsView + ":" + Settings.prmPaymentsByDates)) {
            menubar.addItem(myUI.getMessage(IndigoMessages.Reports), menuCommand);
        }

        mi = menubar.addItem(myUI.getMessage(IndigoMessages.Salaries), null);
        if (currentUser.isPermitted(Settings.cnAccrualsView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.Accruals), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnPayoutsView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.Payouts), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(IndigoMessages.Definitions), null);
        if (currentUser.isPermitted(Settings.cnDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.YearDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.ClassNumberDefinition), menuCommand);

        }
        if (currentUser.isPermitted(Settings.cnClassNameDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.ClassNameDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnDiscountDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.DiscountDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnContractDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.ContractDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnSchoolDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.SchoolDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnAccessoriesDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.AccessoriesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnLeavingReasonsDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.LeavingReasonsDefinition), menuCommand);
        }

        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(IndigoMessages.OtherFunctions), null);
        if (currentUser.isPermitted(Settings.cnIssueOrderView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.IssueStudentOrder), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnCallsView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.Calls), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnSchoolModificationView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.SchoolModification), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnTemplatesView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.Templates), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnBackupView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.Backup), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnSettingsView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.Settings), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(IndigoMessages.Accounting), null);
        if (currentUser.isPermitted(Settings.cnIncomesDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.IncomesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnExpensesDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.ExpensesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnIncomesExpensesDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.IncomesExpensesDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnReturnableAssetsDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.ReturnableAssetsDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnShortTermDebtsDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.ShortTermDebtsDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnShortTermDebtsView + ":" + Settings.prmMenu)
                || currentUser.isPermitted(Settings.cnReturnableAssetsView + ":" + Settings.prmMenu)
                || currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.prmMenu)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
            if (currentUser.isPermitted(Settings.cnReturnableAssetsView + ":" + Settings.prmMenu)) {
                mi.addItem(myUI.getMessage(IndigoMessages.ReturnableAssets), menuCommand);
            }
            if (currentUser.isPermitted(Settings.cnShortTermDebtsView + ":" + Settings.prmMenu)) {
                mi.addItem(myUI.getMessage(IndigoMessages.ShortTermDebts), menuCommand);
            }
            if (currentUser.isPermitted(Settings.cnBalanceAccountsView + ":" + Settings.prmMenu)) {
                mi.addItem(myUI.getMessage(IndigoMessages.BalanceAccounts), menuCommand);
            }
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmMenu)
                || currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmAccountingBankReport)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.AccountingReports), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnAccountingReportsView + ":" + Settings.prmAccountingBankReport)) {
            mi.addItem(myUI.getMessage(IndigoMessages.AccountingBankReport), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(IndigoMessages.Stock), null);
        if (currentUser.isPermitted(Settings.cnStockDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.StocksDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnStockIncomeView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.StockIncome), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnStockOutcomeView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.StockOutcome), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnStockReportsView + ":" + Settings.prmMenu)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
            mi.addItem(myUI.getMessage(IndigoMessages.StockReports), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(IndigoMessages.Inventory), null);
        if (currentUser.isPermitted(Settings.cnInventoryDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.InventoryCategoryDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.BlockDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.RoomDefinition), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnInventoryOrganizationView + ":" + Settings.prmMenu)
                || currentUser.isPermitted(Settings.cnInventoryLiquidationView + ":" + Settings.prmMenu)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
            if (currentUser.isPermitted(Settings.cnInventoryOrganizationView + ":" + Settings.prmMenu)) {
                mi.addItem(myUI.getMessage(IndigoMessages.InventoryOrganization), menuCommand);
            }
            if (currentUser.isPermitted(Settings.cnInventoryLiquidationView + ":" + Settings.prmMenu)) {
                mi.addItem(myUI.getMessage(IndigoMessages.InventoryLiquidation), menuCommand);
            }
        }
        if (currentUser.isPermitted(Settings.cnInventoryReportsView + ":" + Settings.prmMenu)) {
            if (mi.getChildren() != null && !mi.getChildren().isEmpty()) {
                mi.addSeparator();
            }
            mi.addItem(myUI.getMessage(IndigoMessages.InventoryReports), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        mi = menubar.addItem(myUI.getMessage(IndigoMessages.HR), null);
        if (currentUser.isPermitted(Settings.cnHRDefinitionView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.PositionDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.BranchDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.LanguageDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.ExamDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.CertificateDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.UniversityDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.WorkPlacesDefinition), menuCommand);
            mi.addItem(myUI.getMessage(IndigoMessages.QuestionDefinition), menuCommand);
        }
        if (mi.getChildren() != null && !mi.getChildren().isEmpty() && (currentUser.isPermitted(Settings.cnEmployeeTransferView + ":" + Settings.prmMenu)
                || currentUser.isPermitted(Settings.cnHRReportsView + ":" + Settings.prmMenu)
                || currentUser.isPermitted(Settings.cnLessonAssessmentView + ":" + Settings.prmMenu))) {
            mi.addSeparator();
        }
        if (currentUser.isPermitted(Settings.cnEmployeeTransferView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.EmployeeTransfer), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnLessonAssessmentView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.LessonAssessment), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnImportBranchesFromExcelView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.ImportBranchesFromExcel), menuCommand);
        }
        if (currentUser.isPermitted(Settings.cnHRReportsView + ":" + Settings.prmMenu)) {
            mi.addItem(myUI.getMessage(IndigoMessages.HRReports), menuCommand);
        }
        if (mi.getChildren() == null || mi.getChildren().isEmpty()) {
            menubar.removeItem(mi);
        }

        if (currentUser.isPermitted(Settings.cnSendOrderView + ":" + Settings.prmMenu)) {
            menubar.addItem(myUI.getMessage(IndigoMessages.SendOrders), menuCommand);
        }
        return menubar;

    }

    private final Command menuCommand = new Command() {

        @Override
        public void menuSelected(MenuItem selectedItem) {
            if (selectedItem != null) {
                if (!currentUser.hasRole(Settings.rnBank)) {
                    myUI.repaintMessagesButton();
                }
                String eventPressed = selectedItem.getText();
                if (eventPressed.equals(myUI.getMessage(IndigoMessages.ClassNumberDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, Settings.classTable, null, null, false, Settings.cnDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.YearDefinition))) {
                    verticalPanel.setSecondComponent(new YearDefinitionView(myUI, AuthenticatedScreen.this));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.LanguageDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, Settings.dbLanguageTable, null, null, false, Settings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ExamDefinition))) {
                    verticalPanel.setSecondComponent(new ExamDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.UniversityDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, Settings.dbUniversityTable, Settings.dbEmployeeEducation, Settings.dbColumnUniversityId,
                            false, Settings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.CertificateDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, Settings.dbCertificateTable, Settings.dbEmployeeCertificate, Settings.dbColumnCertificateId,
                            false, Settings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.WorkPlacesDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, Settings.dbWork_placeTable, Settings.dbEmployeeWork, Settings.dbColumnEmployeeWorkId, false, Settings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.QuestionDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, Settings.dbQuestion, null, null, true, Settings.cnHRDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.EmployeeTransfer))) {
                    verticalPanel.setSecondComponent(new EmployeeTransferView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.LessonAssessment))) {
                    verticalPanel.setSecondComponent(new LessonAssessmentView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.BranchDefinition))) {
                    verticalPanel.setSecondComponent(new BranchDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.InventoryCategoryDefinition))) {
                    verticalPanel.setSecondComponent(new DefinitionView(
                            myUI, Settings.dbInventoryCategoryTable, null, null, false, Settings.cnInventoryDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ClassNameDefinition))) {
                    verticalPanel.setSecondComponent(new ClassNameDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.BlockDefinition))) {
                    verticalPanel.setSecondComponent(new BlockDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.RoomDefinition))) {
                    verticalPanel.setSecondComponent(new RoomDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.PositionDefinition))) {
                    verticalPanel.setSecondComponent(new PositionDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.DiscountDefinition))) {
                    verticalPanel.setSecondComponent(new DiscountDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.AccessoriesDefinition))) {
                    verticalPanel.setSecondComponent(new AccessoriesDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.LeavingReasonsDefinition))) {
                    verticalPanel.setSecondComponent(new LeavingReasonsDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ContractDefinition))) {
                    verticalPanel.setSecondComponent(new ContractDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.SchoolDefinition))) {
                    verticalPanel.setSecondComponent(new SchoolDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.EmployeeDefinition))) {
                    verticalPanel.setSecondComponent(new EmployeeDefinitionView(myUI, false));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.MyInfo))) {
                    verticalPanel.setSecondComponent(new EmployeeDefinitionView(myUI, true));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.SchoolModification))) {
                    verticalPanel.setSecondComponent(new SchoolModificationView(
                            myUI, myUI.getUser().getSchool().getId()));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.StudentDefinition))) {
                    verticalPanel.setSecondComponent(new StudentDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.IssueStudentOrder))) {
                    verticalPanel.setSecondComponent(new IssueOrderView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ImportBranchesFromExcel))) {
                    verticalPanel.setSecondComponent(new ImportBranchesFromExcelView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.SendOrders))) {
                    verticalPanel.setSecondComponent(new SendOrderView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.Reports))) {
                    if (currentUser.hasRole(Settings.rnBank)) {
                        verticalPanel.setSecondComponent(new BankPaymentsByDateReport(myUI));
                    } else {
                        verticalPanel.setSecondComponent(new StudentReportsView(myUI));
                    }
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.AccountingReports))) {
                    verticalPanel.setSecondComponent(new AccountingReportsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.AccountingBankReport))) {
                    verticalPanel.setSecondComponent(new BankPaymentsByDateReport(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.StockReports))) {
                    verticalPanel.setSecondComponent(new StockReportsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.HRReports))) {
                    verticalPanel.setSecondComponent(new HRReportsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.Templates))) {
                    verticalPanel.setSecondComponent(new TemplatesView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.Backup))) {
                    verticalPanel.setSecondComponent(new BackupView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.Calls))) {
                    verticalPanel.setSecondComponent(new CallsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.HomePage))) {
                    verticalPanel.setSecondComponent(new HomePageView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.CashBox))) {
                    verticalPanel.setSecondComponent(new CashBoxView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.Accruals))) {
                    verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(IndigoMessages.Accruals),
                            Settings.cnAccrualsView, 2, 1));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ShortTermDebts))) {
                    verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(IndigoMessages.ShortTermDebts),
                            Settings.cnShortTermDebtsView, 4, 4));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.BalanceAccounts))) {
                    verticalPanel.setSecondComponent(new BalanceAccountsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ReturnableAssets))) {
                    verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(IndigoMessages.ReturnableAssets),
                            Settings.cnReturnableAssetsView, 3, 3));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.Payouts))) {
                    verticalPanel.setSecondComponent(new PayoutsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.StocksDefinition))) {
                    verticalPanel.setSecondComponent(new StockDefinitionView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.StockIncome))) {
                    verticalPanel.setSecondComponent(new StockIncomeView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.InventoryOrganization))) {
                    verticalPanel.setSecondComponent(new InventoryOrganizationView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.InventoryLiquidation))) {
                    verticalPanel.setSecondComponent(new InventoryLiquidationView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.StockOutcome))) {
                    verticalPanel.setSecondComponent(new StockOutcomeView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.Settings))) {
                    verticalPanel.setSecondComponent(new SettingsView(myUI));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.IncomesDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI,
                            1, Settings.cnIncomesDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ExpensesDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI,
                            2, Settings.cnExpensesDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.IncomesExpensesDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI,
                            5, Settings.cnIncomesExpensesDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ReturnableAssetsDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI,
                            3, Settings.cnReturnableAssetsDefinitionView));
                } else if (eventPressed.equals(myUI.getMessage(IndigoMessages.ShortTermDebtsDefinition))) {
                    verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI,
                            4, Settings.cnShortTermDebtsDefinitionView));
                }

                header.setValue(eventPressed.toUpperCase());
            }
        }
    };

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == changePassBtn) {
            this.verticalPanel.setSecondComponent(new ChangeUserData(myUI));
            header.setValue(myUI.getMessage(IndigoMessages.ChangeUserDataHeader)
                    .toUpperCase());
        } else if (source == myUI.getMessagesBtn()) {
            myUI.repaintMessagesButton();
            this.verticalPanel.setSecondComponent(new MessagesView(myUI));
            header.setValue(myUI.getMessage(IndigoMessages.Messages).toUpperCase());
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == yearSelect) {
            if (yearSelect.getValue() != null) {
                ConfirmDialog.show(myUI, myUI.getMessage(IndigoMessages.Question),
                        myUI.getMessage(IndigoMessages.ConfirmChangeYear),
                        myUI.getMessage(IndigoMessages.Yes),
                        myUI.getMessage(IndigoMessages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                try {
                                    DbEmployee dbCon = new DbEmployee();
                                    dbCon.connect();
                                    dbCon.execUpdateYear((Integer) yearSelect.getValue(), myUI.getUser().getId());
                                    dbCon.close();
                                } catch (Exception e) {
                                    logger.error(e);
                                    logger.catching(e);
                                }
                                myUI.getUser().getCurrent_year().setId((Integer) yearSelect.getValue());
                                myUI.getUser().getCurrent_year().setName(yearSelect.getContainerProperty(
                                        yearSelect.getValue(), Settings.titleShort).getValue().toString());
                                myUI.getUser().getCurrent_year().setLast((Boolean) yearSelect.getContainerProperty(
                                        yearSelect.getValue(), Settings.is_last).getValue());
                                myUI.getUser().getCurrent_year().setInstallment_date_limit((Long) yearSelect.getContainerProperty(
                                        yearSelect.getValue(), Settings.installmentDateLimit).getValue());
                                Notification.show(myUI.getMessage(IndigoMessages.ValueSaved),
                                        Notification.Type.HUMANIZED_MESSAGE);
                                insertPre_regOrders((Integer) yearSelect.getValue(), (Integer) schoolSelect.getValue(),
                                        myUI.getUser().getId());
                                yearSelect.removeValueChangeListener(AuthenticatedScreen.this);
                                setYearSel(myUI.getUser().getCurrent_year().getId());
                                updatePage();
                            } else {
                                yearSelect.removeValueChangeListener(AuthenticatedScreen.this);
                                yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
                                yearSelect.addValueChangeListener(AuthenticatedScreen.this);
                            }
                        });
            }
        } else if (property == schoolSelect) {
            if (schoolSelect.getValue() != null) {
                int curSchoolId = myUI.getUser().getSchool().getId();
                String curSchoolName = myUI.getUser().getSchool().getName_ru();
                if (curSchoolId != (Integer) schoolSelect.getValue()
                        && !curSchoolName.equals(schoolSelect.getItemCaption(schoolSelect.getValue()))) {
                    myUI.getUser().getSchool().setId((Integer) schoolSelect.getValue());
                    myUI.getUser().getSchool().setName_ru(schoolSelect.getItemCaption(schoolSelect.getValue()));
                    if (schoolSelect.getContainerProperty(schoolSelect.getValue(),
                            myUI.getMessage(IndigoMessages.Logo)).getValue() != null) {
                        myUI.getUser().getSchool().setPhoto(schoolSelect.getContainerProperty(schoolSelect.getValue(),
                                myUI.getMessage(IndigoMessages.Logo)).getValue().toString());
                    }
                    insertPre_regOrders((Integer) yearSelect.getValue(), (Integer) schoolSelect.getValue(),
                            myUI.getUser().getId());
                    updatePage();
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.ValueCanNotBeSaved),
                            Notification.Type.WARNING_MESSAGE);
                }
            }
        }
        myUI.repaintMessagesButton();
    }

    private void updatePage() {
        if (header.getValue() != null) {
            if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.ClassNumberDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new DefinitionView(
                        myUI, Settings.classTable, null, null, false, Settings.cnDefinitionView));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.YearDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new YearDefinitionView(myUI, AuthenticatedScreen.this));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.BranchDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new DefinitionView(
                        myUI, Settings.dbBranchTable, null, null, true, Settings.cnHRDefinitionView));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.ClassNameDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new ClassNameDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Messages)).toUpperCase())) {
                myUI.repaintMessagesButton();
                verticalPanel.setSecondComponent(new MessagesView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.BlockDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new BlockDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.RoomDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new RoomDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.DiscountDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new DiscountDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.ContractDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new ContractDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.AccessoriesDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new AccessoriesDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.LeavingReasonsDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new LeavingReasonsDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.SchoolDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new SchoolDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.EmployeeDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new EmployeeDefinitionView(myUI, false));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.MyInfo)).toUpperCase())) {
                verticalPanel.setSecondComponent(new EmployeeDefinitionView(myUI, true));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.SchoolModification)).toUpperCase())) {
                verticalPanel.setSecondComponent(new SchoolModificationView(myUI, myUI.getUser().getSchool().getId()));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.StudentDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StudentDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Reports)).toUpperCase())) {
                if (currentUser.hasRole(Settings.rnBank)) {
                    verticalPanel.setSecondComponent(new BankPaymentsByDateReport(myUI));
                } else {
                    verticalPanel.setSecondComponent(new StudentReportsView(myUI));
                }
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.AccountingReports)).toUpperCase())) {
                verticalPanel.setSecondComponent(new AccountingReportsView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.AccountingBankReport)).toUpperCase())) {
                verticalPanel.setSecondComponent(new BankPaymentsByDateReport(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.StockReports)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockReportsView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.LessonAssessment)).toUpperCase())) {
                verticalPanel.setSecondComponent(new LessonAssessmentView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.HRReports)).toUpperCase())) {
                verticalPanel.setSecondComponent(new HRReportsView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.ImportBranchesFromExcel)).toUpperCase())) {
                verticalPanel.setSecondComponent(new ImportBranchesFromExcelView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.IssueStudentOrder)).toUpperCase())) {
                verticalPanel.setSecondComponent(new IssueOrderView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Templates)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TemplatesView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Backup)).toUpperCase())) {
                verticalPanel.setSecondComponent(new BackupView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Calls)).toUpperCase())) {
                verticalPanel.setSecondComponent(new CallsView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.HomePage)).toUpperCase())) {
                verticalPanel.setSecondComponent(new HomePageView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Welcome)).toUpperCase())) {
                verticalPanel.setSecondComponent(new HomePageView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.CashBox)).toUpperCase())) {
                verticalPanel.setSecondComponent(new CashBoxView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Accruals)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(IndigoMessages.Accruals),
                        Settings.cnAccrualsView, 2, 1));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.ShortTermDebts)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(IndigoMessages.ShortTermDebts),
                        Settings.cnShortTermDebtsView, 4, 4));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.ReturnableAssets)).toUpperCase())) {
                verticalPanel.setSecondComponent(new TransfersView(myUI, myUI.getMessage(IndigoMessages.ReturnableAssets),
                        Settings.cnReturnableAssetsView, 3, 3));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.BalanceAccounts)).toUpperCase())) {
                verticalPanel.setSecondComponent(new BalanceAccountsView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Payouts)).toUpperCase())) {
                verticalPanel.setSecondComponent(new PayoutsView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.IncomesExpensesDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new AccCategoriesDefinitionView(myUI,
                        5, Settings.cnIncomesExpensesDefinitionView));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.StocksDefinition)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockDefinitionView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.StockIncome)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockIncomeView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.InventoryOrganization)).toUpperCase())) {
                verticalPanel.setSecondComponent(new InventoryOrganizationView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.InventoryLiquidation)).toUpperCase())) {
                verticalPanel.setSecondComponent(new InventoryLiquidationView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(
                    IndigoMessages.StockOutcome)).toUpperCase())) {
                verticalPanel.setSecondComponent(new StockOutcomeView(myUI));
            } else if (header.getValue().equals((myUI.getMessage(IndigoMessages.Settings)).toUpperCase())) {
                verticalPanel.setSecondComponent(new SettingsView(myUI));
            }

        }
    }

    private void setYearSel(int year_id) {
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_years_for_select(myUI, year_id));
            yearSelect.setValue(year_id);
            yearSelect.addValueChangeListener(this);
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertPre_regOrders(int year_id, int school_id, int emp_id) {
        try {
            DbStudentOrder dbso = new DbStudentOrder();
            dbso.connect();
            dbso.insertPre_regChange_year(year_id, school_id, emp_id);
            dbso.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
