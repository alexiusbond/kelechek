/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.dao.*;
import kg.alex.star.domain.StudentInfoPdf;
import kg.alex.star.i18n.Messages;
import kg.alex.star.pdf.ClassListPdf;
import kg.alex.star.tableexport.EnhancedFormatExcelExport;
import kg.alex.star.utils.DefinitionsFilterGenerator;
import kg.alex.star.utils.FormattedFilterTable;
import kg.alex.star.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Set;

public class ClassListReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ClassListReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    public int activeStudents, discountedStudents, males;
    public double contracts, discounts, corrections, prevYearDebts, prevYearOverpays, nets, paid_amounts, debts, overPays;
    private Button generateBtn, makePdfBtn, selectAllBtn, deselectAllBtn, excelBtn;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    private FormattedFilterTable dataTable;
    private FilterTable classTable;
    private IndexedContainer dataCont;
    private PopupDateField fromDateDF, tillDateDF;

    public ClassListReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 6);
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
        educationStatusMCB.setValue(Settings.convertToSet(educationStatusMCB.getContainerDataSource().getItemIds()));
        educationStatusMCB.addValueChangeListener(this);

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

        fromDateDF = new PopupDateField(myUI.getMessage(Messages.FromDate));
        fromDateDF.setInputPrompt(myUI.getMessage(Messages.AnyDate));
        fromDateDF.setWidth(Settings.PERCENTS100);
        fromDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        fromDateDF.setDateFormat(Settings.datePattern);
        fromDateDF.setResolution(Resolution.DAY);
        fromDateDF.addValueChangeListener(this);

        tillDateDF = new PopupDateField(myUI.getMessage(Messages.TillDate));
        tillDateDF.setInputPrompt(myUI.getMessage(Messages.AnyDate));
        tillDateDF.setWidth(Settings.PERCENTS100);
        tillDateDF.setStyleName(ValoTheme.DATEFIELD_TINY);
        tillDateDF.setDateFormat(Settings.datePattern);
        tillDateDF.setResolution(Resolution.DAY);
        tillDateDF.addValueChangeListener(this);

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
        leftGrid.addComponent(fromDateDF, 0, 1, 1, 1);
        leftGrid.addComponent(tillDateDF, 2, 1, 3, 1);
        leftGrid.addComponent(educationStatusMCB, 0, 2, 3, 2);
        leftGrid.addComponent(selectAllBtn, 0, 3, 1, 3);
        leftGrid.addComponent(deselectAllBtn, 2, 3, 3, 3);
        leftGrid.addComponent(classTable, 0, 4, 3, 4);
        leftGrid.addComponent(generateBtn, 0, 5, 1, 5);
        leftGrid.addComponent(makePdfBtn, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(4, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);

    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new FormattedFilterTable(myUI);
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setFilterBarVisible(true);
        dataTable.setFilterGenerator(new DefinitionsFilterGenerator(dataTable));
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(CustomTable.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (classTable.getValue() != null && ((Set<?>) classTable.getValue()).size() > 0) {
                try {
                    DbStudentContract dbsc = new DbStudentContract();
                    dbsc.connect();
                    dataCont = dbsc.execSQL_ClassList(myUI, Settings.convertCollectionToStr((Set<?>) classTable.getValue()),
                            (Integer) yearSelect.getValue(), fromDateDF.getValue(), tillDateDF.getValue(),
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()), this);
                    dataTable.setContainerDataSource(dataCont);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Contract), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Discount), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Correction), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.PreviousYearDebt), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.PreviousYearOverpay), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Net), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Paid), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Debt), CustomTable.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.OverPay), CustomTable.Align.RIGHT);
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Id),
                            myUI.getMessage(Messages.Students) + ": " + dataCont.size());
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Gender),
                            "М: " + males + "; Ж: " + (dataTable.getContainerDataSource().size() - males));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.EducationStatus),
                            myUI.getMessage(Messages.Active) + activeStudents);
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Contract),
                            Settings.dFormat2.format(contracts));
                    if (contracts != 0) {
                        dataTable.setColumnFooter(myUI.getMessage(Messages.Discount),
                                Settings.dFormat2.format(discounts) + " (" +
                                        Settings.dFormat2.format(discounts * 100 / contracts) + "%)");
                    }
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Correction),
                            Settings.dFormat2.format(corrections));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.PreviousYearDebt),
                            Settings.dFormat2.format(prevYearDebts));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.PreviousYearOverpay),
                            Settings.dFormat2.format(prevYearOverpays));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Net),
                            Settings.dFormat2.format(nets));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Paid),
                            Settings.dFormat2.format(paid_amounts));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Debt),
                            Settings.dFormat2.format(debts));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.OverPay),
                            Settings.dFormat2.format(overPays));
                    if (dataCont.size() != 0) {
                        dataTable.setColumnFooter(myUI.getMessage(Messages.DiscountType), myUI.getMessage(Messages.Discounted)
                                + discountedStudents + " " + myUI.getMessage(Messages.StudentsPossessive) + " ("
                                + discountedStudents * 100 / dataCont.size() + "%)");
                        makePdfBtn.setEnabled(true);
                        excelBtn.setEnabled(true);
                    }
                    dbsc.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                Notification.show(myUI.getMessage(Messages.NotificationNothingIsSelected),
                        Notification.Type.WARNING_MESSAGE);
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
                        new ClassListPdf(myUI, dataCont, studentInfo, this);
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
        } else if (source == excelBtn) {
            if (dataTable.getContainerDataSource().size() != 0) {
                Table t = new Table();
                t.setContainerDataSource(dataTable.getContainerDataSource());

                Window w = new Window();
                w.setModal(true);
                myUI.addWindow(w);
                w.setContent(t);

                EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(t);
                excelReport.setReportTitle(myUI.getMessage(Messages.ClassList));
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                excelReport.getTotalsRow().getCell(8).setCellFormula(null);
                excelReport.getTotalsRow().getCell(0).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.Id)));
                excelReport.getTotalsRow().getCell(3).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.EducationStatus)));
                excelReport.getTotalsRow().getCell(8).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.DiscountType)));
                excelReport.sendConverted();
                w.close();
            }
        } else if (source == selectAllBtn) {
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            classTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == classTable && classTable.getValue() != null) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
            dataTable.setContainerDataSource(null);
        } else if (property == yearSelect || property == educationStatusMCB
                || property == fromDateDF || property == tillDateDF) {
            makePdfBtn.setEnabled(false);
            excelBtn.setEnabled(false);
            dataTable.setContainerDataSource(null);
        }
    }
}
