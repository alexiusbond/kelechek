/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.*;
import kg.alex.indigo.domain.StudentContract;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.domain.StudentPayment;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.pdf.InstallmentPlanPaymentsPdf;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;

import java.io.File;
import java.util.Iterator;

public class InstallmentPlanPaymentsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(InstallmentPlanPaymentsReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, makePdfBtn, excelBtn;
    private final HorizontalSplitPanel splitPanel;
    private GridLayout rightGrid;
    private double debt, amount, plan_debt, ttl_payment, to_pay, ttl_left;
    private StringBuilder discounts;
    private String corrections;
    private StudentInfoPdf studInfo;
    private FormattedTable installmentTable, paymentsTable;
    private FilterTable studentsTable, classTable;
    private ComboBox yearSelect;
    private IndexedContainer installmentCont, paymentsCont;
    private CheckBox paymentsCkb, instPlanCkb;
    private Label debtLab;
    private Label contractLab;
    private Label discountLab;
    private Label correctionLab;
    private Label planDebt;
    private Label netLab;
    private Label paidLab;
    private Label leftLab;

    private final String[] NATURAL_COL_ORDER;
    public double total_inst, total_pay;

    public InstallmentPlanPaymentsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(IndigoMessages.Date),
                myUI.getMessage(IndigoMessages.Amount),
                myUI.getMessage(IndigoMessages.WhoPaid),
                myUI.getMessage(IndigoMessages.PaymentCategoryType)};
    }

    private void buildLeftPanel() {
        GridLayout leftGrid = new GridLayout(4, 5);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelect = new ComboBox(myUI.getMessage(IndigoMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        classTable = new FilterTable();
        classTable.setFilterDecorator(new MyFilterDecorator(myUI));
        classTable.setStyleName(ValoTheme.TABLE_SMALL);
        classTable.setCaption(myUI.getMessage(IndigoMessages.AllClasses));
        classTable.setSizeFull();
        classTable.setNullSelectionAllowed(false);
        classTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
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
        classTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});

        studentsTable = new FilterTable();
        studentsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        studentsTable.setStyleName(ValoTheme.TABLE_SMALL);
        studentsTable.setCaption(myUI.getMessage(IndigoMessages.AllStudents));
        studentsTable.setSizeFull();
        studentsTable.setNullSelectionAllowed(false);
        studentsTable.setMultiSelect(false);
        studentsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        studentsTable.setFilterBarVisible(true);
        studentsTable.setFooterVisible(false);
        studentsTable.setSelectable(true);
        studentsTable.addValueChangeListener(this);

        instPlanCkb = new CheckBox(myUI.getMessage(IndigoMessages.InstallmentPlan));
        instPlanCkb.setWidth(Settings.PERCENTS100);
        instPlanCkb.setValue(true);
        instPlanCkb.addValueChangeListener(this);

        paymentsCkb = new CheckBox(myUI.getMessage(IndigoMessages.Payments));
        paymentsCkb.setWidth(Settings.PERCENTS100);
        paymentsCkb.setValue(false);
        paymentsCkb.addValueChangeListener(this);

        generateBtn = new Button(myUI.getMessage(IndigoMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        makePdfBtn = new Button();
        makePdfBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToPdf));
        makePdfBtn.setWidth(Settings.PERCENTS100);
        makePdfBtn.setEnabled(false);
        makePdfBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        makePdfBtn.setIcon(FontAwesome.FILE_PDF_O);
        makePdfBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(classTable, 0, 1, 3, 1);
        leftGrid.addComponent(studentsTable, 0, 2, 3, 2);
        leftGrid.addComponent(instPlanCkb, 0, 3, 1, 3);
        leftGrid.addComponent(paymentsCkb, 2, 3, 3, 3);
        leftGrid.addComponent(generateBtn, 0, 4, 1, 4);
        leftGrid.addComponent(makePdfBtn, 2, 4);
        leftGrid.addComponent(excelBtn, 3, 4);
        leftGrid.setRowExpandRatio(1, 1);
        leftGrid.setRowExpandRatio(2, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (studentsTable.getValue() != null) {
                try {
                    DbStudentInfoPdf dbs = new DbStudentInfoPdf();
                    dbs.connect();
                    studInfo = dbs.execSQL((Integer) yearSelect.getValue(), (Integer) studentsTable.getValue());
                    dbs.close();
                    DbEmployee dbEmployee = new DbEmployee();
                    dbEmployee.connect();
                    studInfo.setDirector(dbEmployee.exec_by_position_id(1, myUI.getUser().getSchool().getId()));
                    studInfo.setAccountant(dbEmployee.exec_by_position_id(2, myUI.getUser().getSchool().getId()));
                    dbEmployee.close();
                    DbSchool dbSchool = new DbSchool();
                    dbSchool.connect();
                    studInfo.setSchool(dbSchool.execSchool(myUI.getUser().getSchool().getId()));
                    dbSchool.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                if (studInfo.getAccountant() != null) {
                    buildRightPanel();
                    studInfo.getContractInfo().setContract(amount);
                    studInfo.getContractInfo().setDebt(debt);
                    studInfo.getContractInfo().setInstallmentPlanDebt(plan_debt - ttl_payment);
                    studInfo.getContractInfo().setDiscountStr(discounts.toString());
                    studInfo.getContractInfo().setCorrectionStr(corrections);
                    studInfo.getContractInfo().setNet(to_pay);
                    studInfo.getContractInfo().setLeft(ttl_left);
                    studInfo.getContractInfo().setPaid(ttl_payment);
                    studInfo.setYear(yearSelect.getContainerProperty(yearSelect.getValue(),
                            myUI.getMessage(IndigoMessages.Title)).getValue().toString());
                    makePdfBtn.setEnabled(true);
                    excelBtn.setEnabled(true);
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.NoAccountant),
                            Notification.Type.WARNING_MESSAGE);
                }
            }
        } else if (source == makePdfBtn) {
            if (studInfo.getAccountant() != null) {
                if (studInfo.getSchool() != null && studInfo.getSchool().getAddress() != null) {
                    new InstallmentPlanPaymentsPdf(myUI, studInfo, installmentCont, paymentsCont, total_inst,
                            total_pay);
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.FillSchoolInfo),
                            Notification.Type.WARNING_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(IndigoMessages.NoAccountant),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == excelBtn) {
            try {
                EnhancedFormatExcelExport excelReport;
                if (paymentsCkb.getValue() && !instPlanCkb.getValue()) {
                    if (paymentsTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(paymentsTable, "sheet1");
                        excelReport.setReportTitle(paymentsTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.export();
                    }
                } else if (!paymentsCkb.getValue() && instPlanCkb.getValue()) {
                    if (installmentTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(installmentTable, "sheet1");
                        excelReport.setReportTitle(installmentTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.export();
                    }
                } else if (paymentsCkb.getValue() && instPlanCkb.getValue()) {
                    if (installmentTable.getContainerDataSource().size() != 0
                            && paymentsTable.getContainerDataSource().size() != 0) {
                        excelReport = new EnhancedFormatExcelExport(paymentsTable, "sheet1");
                        excelReport.setReportTitle(paymentsTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.convertTable();
                        excelReport.setNextTable(installmentTable, "sheet2");
                        excelReport.setReportTitle(installmentTable.getCaption());
                        excelReport.setDisplayTotals(true);
                        excelReport.export();
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
        makePdfBtn.setEnabled(false);
        if ((property == classTable || property == yearSelect)
                && classTable.getValue() != null && yearSelect.getValue() != null) {
            try {
                DbStudent dbst = new DbStudent();
                dbst.connect();
                studentsTable.setContainerDataSource(
                        dbst.execStud_sel(myUI, (Integer) classTable.getValue(),
                                (Integer) yearSelect.getValue()));
                dbst.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (property == studentsTable || property == instPlanCkb
                || property == paymentsCkb || property == yearSelect) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
        }
    }

    private void buildRightPanel() {
        rightGrid = new GridLayout(7, 5);
        rightGrid.setSizeFull();
        rightGrid.setMargin(true);
        buildStudInfo();
        buildContractInfo();
        recount();
        installmentCont = null;
        paymentsCont = null;
        if (instPlanCkb.getValue()) {
            installmentTable = new FormattedTable(myUI);
            installmentTable.setCaption(myUI.getMessage(IndigoMessages.InstallmentPlan));
            installmentTable.setFooterVisible(true);
            installmentTable.setSizeFull();
            installmentTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
            installmentTable.setStyleName(ValoTheme.TABLE_COMPACT);
            try {
                DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
                dbip.connect();
                total_inst = 0;
                installmentCont = dbip.execSQL_InstPLan(myUI,
                        (Integer) studentsTable.getValue(), (Integer) yearSelect.getValue(), this);
                installmentTable.setContainerDataSource(installmentCont);
                dbip.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            installmentTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Amount), Table.Align.RIGHT);
            installmentTable.setColumnFooter(myUI.getMessage(IndigoMessages.Amount), "Total "
                    + Settings.dFormat2.format(total_inst));
            if (!paymentsCkb.getValue()) {
                rightGrid.addComponent(installmentTable, 0, 4, 6, 4);
            } else {
                rightGrid.addComponent(installmentTable, 0, 4, 2, 4);
            }
            rightGrid.setRowExpandRatio(4, 1);
        }
        if (paymentsCkb.getValue()) {
            paymentsTable = new FormattedTable(myUI);
            paymentsTable.setCaption(myUI.getMessage(IndigoMessages.Payments));
            paymentsTable.setFooterVisible(true);
            paymentsTable.setSizeFull();
            paymentsTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
            paymentsTable.setStyleName(ValoTheme.TABLE_COMPACT);
            try {
                DbStudentPayment dbsp = new DbStudentPayment();
                dbsp.connect();
                total_pay = 0;
                paymentsCont = dbsp.execSQL_Payment(myUI,
                        (Integer) studentsTable.getValue(),
                        (Integer) yearSelect.getValue(), this);
                paymentsTable.setContainerDataSource(paymentsCont);
                dbsp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            paymentsTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Amount), Table.Align.RIGHT);
            paymentsTable.setColumnFooter(myUI.getMessage(IndigoMessages.Amount), "Total "
                    + Settings.dFormat2.format(total_pay));

            if (!instPlanCkb.getValue()) {
                rightGrid.addComponent(paymentsTable, 0, 4, 6, 4);
            } else {
                rightGrid.addComponent(paymentsTable, 3, 4, 6, 4);
            }
            paymentsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
            rightGrid.setRowExpandRatio(4, 1);
        }
        splitPanel.setSecondComponent(rightGrid);
    }

    private void buildContractInfo() {

        contractLab = new Label();
        contractLab.setSizeFull();
        contractLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        contractLab.setValue(myUI.getMessage(IndigoMessages.Contract) + ":");
        rightGrid.addComponent(contractLab, 3, 0, 4, 0);

        discountLab = new Label();
        discountLab.setSizeFull();
        discountLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        discountLab.setValue(myUI.getMessage(IndigoMessages.Discount) + ":");
        rightGrid.addComponent(discountLab, 3, 1, 4, 1);

        correctionLab = new Label();
        correctionLab.setSizeFull();
        correctionLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        correctionLab.setValue(myUI.getMessage(IndigoMessages.Discount) + ":");
        rightGrid.addComponent(correctionLab, 3, 2, 4, 2);

        debtLab = new Label();
        debtLab.setSizeFull();
        debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        debtLab.setValue(myUI.getMessage(IndigoMessages.PreviousYearDebt) + ":");
        rightGrid.addComponent(debtLab, 3, 3, 4, 3);

        netLab = new Label();
        netLab.setSizeFull();
        netLab.setContentMode(ContentMode.HTML);
        netLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        netLab.setValue(myUI.getMessage(IndigoMessages.Net) + ":");
        rightGrid.addComponent(netLab, 5, 0, 6, 0);

        paidLab = new Label();
        paidLab.setSizeFull();
        paidLab.setContentMode(ContentMode.HTML);
        paidLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        paidLab.setValue(myUI.getMessage(IndigoMessages.Paid) + ":");
        rightGrid.addComponent(paidLab, 5, 1, 6, 1);

        leftLab = new Label();
        leftLab.setSizeFull();
        leftLab.setContentMode(ContentMode.HTML);
        leftLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        leftLab.setValue(myUI.getMessage(IndigoMessages.Left) + ":");
        rightGrid.addComponent(leftLab, 5, 2, 6, 2);

        planDebt = new Label();
        planDebt.setSizeFull();
        planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
        planDebt.setValue(myUI.getMessage(IndigoMessages.InstPlanDebt) + ":");
        rightGrid.addComponent(planDebt, 5, 3, 6, 3);
    }

    private void buildStudInfo() {
        if (studentsTable.getValue() != null) {
            Embedded photoEmb = new Embedded();
            if (studInfo.getStudent().getPhoto() != null && !studInfo.getStudent().getPhoto().equals("")) {
                photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + studInfo.getStudent().getPhoto())));
            } else {
                photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
            }
            photoEmb.setImmediate(true);
            photoEmb.setHeight("110px");
            rightGrid.addComponent(photoEmb, 0, 0, 0, 3);

            Label loginLab = new Label();
            loginLab.setSizeFull();
            loginLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            loginLab.setValue(myUI.getMessage(IndigoMessages.Id) + ": " + studInfo.getStudent().getLogin());
            rightGrid.addComponent(loginLab, 1, 0, 2, 0);

            Label nameLab = new Label();
            nameLab.setSizeFull();
            nameLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            nameLab.setValue(myUI.getMessage(IndigoMessages.FirstName) + ": " + studInfo.getStudent().getName());
            rightGrid.addComponent(nameLab, 1, 1, 2, 1);

            Label surnameLab = new Label();
            surnameLab.setSizeFull();
            surnameLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            surnameLab.setValue(myUI.getMessage(IndigoMessages.LastName) + ": " + studInfo.getStudent().getSurname());
            rightGrid.addComponent(surnameLab, 1, 2, 2, 2);

            Label classLab = new Label();
            classLab.setSizeFull();
            classLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            classLab.setValue(myUI.getMessage(IndigoMessages.ClassName) + ": " + studInfo.getStudent().getClass_name());
            rightGrid.addComponent(classLab, 1, 3, 2, 3);
        }
    }

    private void recount() {
        StudentContract c = new StudentContract();
        StudentPayment sp;
        IndexedContainer discCont = new IndexedContainer();
        try {
            DbStudentContract dbsc = new DbStudentContract();
            DbStudentDiscount dbsd = new DbStudentDiscount();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbsc.connect();
            dbsd.connect();
            dbsp.connect();
            c = dbsc.exec_recount_contract((Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue());
            discCont = dbsd.exec_disc_strCont(myUI, (Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue());
            sp = dbsp.exec_recount_payment((Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue(), myUI.getUser().getSchool().getCurrency_id());
            amount = c.getAmount();
            debt = dbsc.exec_get_debt((Integer) studentsTable.getValue(),
                    (Integer) yearSelect.getValue());
            ttl_payment = sp.getTtl_pay();
            plan_debt = c.getPlan_debt() - total_pay;
            to_pay = c.getContr_with_disc() + debt + c.getCorrection();
            ttl_left = (c.getContr_with_disc() + debt) - ttl_payment + c.getCorrection();
            dbsc.close();
            dbsd.close();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        Iterator<?> iter = discCont.getItemIds().iterator();
        discounts = new StringBuilder();
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 1) {
                discounts.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(IndigoMessages.Amount)).getValue())).append("%");
                if (iter.hasNext()) {
                    discounts.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 2) {
                discounts.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(IndigoMessages.Amount)).getValue())).append("$");
                if (iter.hasNext()) {
                    discounts.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 3) {
                discounts.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(IndigoMessages.FreeAmount)).getValue())).append("%");
                if (iter.hasNext()) {
                    discounts.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 4) {
                discounts.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(IndigoMessages.FreeAmount)).getValue())).append("$");
                if (iter.hasNext()) {
                    discounts.append(", ");
                }
            }
        }
        corrections = c.getCorrectionDetails() == null ? "0.00$" : c.getCorrectionDetails();
        contractLab.setValue(myUI.getMessage(IndigoMessages.Contract) + ": " + Settings.dFormat2.format(c.getAmount()) + "$");
        discountLab.setValue(myUI.getMessage(IndigoMessages.Discount) + ": " + discounts.toString());
        correctionLab.setValue(myUI.getMessage(IndigoMessages.Correction) + ": " + corrections);
        if (debt > 0) {
            debtLab.setStyleName(ValoTheme.LABEL_FAILURE);
        } else {
            debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        }
        debtLab.setValue(myUI.getMessage(IndigoMessages.PreviousYearDebt) + ": " + Settings.dFormat2.format(debt) + "$");
        netLab.setValue(myUI.getMessage(IndigoMessages.Net) + ": " + Settings.dFormat2.format(c.getContr_with_disc() + debt + c.getCorrection()) + "$");
        paidLab.setValue(myUI.getMessage(IndigoMessages.Paid) + ": " + Settings.dFormat2.format(ttl_payment) + "$");
        leftLab.setValue(myUI.getMessage(IndigoMessages.Left) + ": " + Settings.dFormat2.format(
                (c.getContr_with_disc() + debt) - ttl_payment + c.getCorrection()) + "$");
        if ((c.getPlan_debt() - ttl_payment) > 0) {
            planDebt.setStyleName(ValoTheme.LABEL_FAILURE);
            planDebt.setValue(myUI.getMessage(IndigoMessages.InstPlanDebt) + ": " + Settings.dFormat2.format(
                    c.getPlan_debt() - ttl_payment + c.getCorrection()) + "$");
        } else {
            planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
            planDebt.setValue(myUI.getMessage(IndigoMessages.InstPlanDebt) + ": " + Settings.dFormat2.format(0.0) + "$");
        }
    }
}
