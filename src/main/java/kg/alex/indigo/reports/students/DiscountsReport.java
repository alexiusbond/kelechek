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
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbClassName;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbDiscount;
import kg.alex.indigo.dao.DbStudentContract;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Set;

public class DiscountsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DiscountsReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final String[] NATURAL_COL_ORDER;
    public int activeStudents, discountedStudents;
    public double contracts, discounts, corrections, debts, nets, paid_amounts, lefts;
    private Button generateBtn, selectAllClassesBtn, deselectAllClassesBtn,
            selectAllDiscountsBtn, deselectAllDiscountsBtn, excelBtn;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    private FormattedTable dataTable;
    private FilterTable classTable, discountsTable;

    public DiscountsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(IndigoMessages.Id),
                myUI.getMessage(IndigoMessages.EducationStatus),
                myUI.getMessage(IndigoMessages.FirstName),
                myUI.getMessage(IndigoMessages.LastName),
                myUI.getMessage(IndigoMessages.ClassName),
                myUI.getMessage(IndigoMessages.Contract),
                myUI.getMessage(IndigoMessages.DiscountType),
                myUI.getMessage(IndigoMessages.Discount),
                myUI.getMessage(IndigoMessages.CorrectionType),
                myUI.getMessage(IndigoMessages.Correction),
                myUI.getMessage(IndigoMessages.PreviousYearDebt),
                myUI.getMessage(IndigoMessages.Net),
                myUI.getMessage(IndigoMessages.Paid),
                myUI.getMessage(IndigoMessages.Left)};
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 7);
        leftGrid.setSizeFull();
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

        selectAllDiscountsBtn = new Button(myUI.getMessage(IndigoMessages.AllDiscounts));
        selectAllDiscountsBtn.setWidth(Settings.PERCENTS100);
        selectAllDiscountsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllDiscountsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllDiscountsBtn.addClickListener(this);

        deselectAllDiscountsBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
        deselectAllDiscountsBtn.setWidth(Settings.PERCENTS100);
        deselectAllDiscountsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllDiscountsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllDiscountsBtn.addClickListener(this);

        discountsTable = new FilterTable();
        discountsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        discountsTable.setStyleName(ValoTheme.TABLE_SMALL);
        discountsTable.setSizeFull();
        discountsTable.setNullSelectionAllowed(false);
        discountsTable.setMultiSelect(true);
        discountsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        discountsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        discountsTable.setFilterBarVisible(true);
        discountsTable.setFooterVisible(false);
        discountsTable.setSelectable(true);
        discountsTable.addValueChangeListener(this);

        selectAllClassesBtn = new Button(myUI.getMessage(IndigoMessages.AllClasses));
        selectAllClassesBtn.setWidth(Settings.PERCENTS100);
        selectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllClassesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllClassesBtn.addClickListener(this);

        deselectAllClassesBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
        deselectAllClassesBtn.setWidth(Settings.PERCENTS100);
        deselectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllClassesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllClassesBtn.addClickListener(this);

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
        classTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});

        generateBtn = new Button(myUI.getMessage(IndigoMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        yearSelect.addValueChangeListener(this);
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllDiscountsBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllDiscountsBtn, 2, 2, 3, 2);
        leftGrid.addComponent(discountsTable, 0, 3, 3, 3);
        leftGrid.addComponent(selectAllClassesBtn, 0, 4, 1, 4);
        leftGrid.addComponent(deselectAllClassesBtn, 2, 4, 3, 4);
        leftGrid.addComponent(classTable, 0, 5, 3, 5);
        leftGrid.addComponent(generateBtn, 0, 6, 2, 6);
        leftGrid.addComponent(excelBtn, 3, 6);
        leftGrid.setRowExpandRatio(3, 1);
        leftGrid.setRowExpandRatio(5, 1);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);

    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        dataTable = new FormattedTable(myUI);
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (!((Set<?>) classTable.getValue()).isEmpty()
                    && !((Set<?>) discountsTable.getValue()).isEmpty()) {
                try {
                    DbStudentContract dbsc = new DbStudentContract();
                    dbsc.connect();
                    IndexedContainer dataCont = dbsc.execSQL_Discounts(myUI,
                            Settings.convertCollectionToStr((Set<?>) classTable.getValue()),
                            Settings.convertCollectionToStr((Set<?>) discountsTable.getValue()),
                            (Integer) yearSelect.getValue(),
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            this);
                    dataTable.setContainerDataSource(dataCont);
                    dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Contract), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Discount), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Correction), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.PreviousYearDebt), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Net), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Paid), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Left), Table.Align.RIGHT);
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.Id),
                            myUI.getMessage(IndigoMessages.Students) + ": " + dataCont.size());
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.EducationStatus),
                            myUI.getMessage(IndigoMessages.Active) + activeStudents);
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.Contract),
                            Settings.dFormat2.format(contracts));
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.Discount),
                            Settings.dFormat2.format(discounts));
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.Correction),
                            Settings.dFormat2.format(corrections));
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.PreviousYearDebt),
                            Settings.dFormat2.format(debts));
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.Net),
                            Settings.dFormat2.format(nets));
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.Paid),
                            Settings.dFormat2.format(paid_amounts));
                    dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.Left),
                            Settings.dFormat2.format(lefts));
                    if (dataCont.size() != 0) {
                        dataTable.setColumnFooter(myUI.getMessage(IndigoMessages.DiscountType), myUI.getMessage(IndigoMessages.Discounted)
                                + discountedStudents + " (" + discountedStudents * 100 / dataCont.size() + "%)");
                        excelBtn.setEnabled(true);
                    }
                    dbsc.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            if (dataTable.getContainerDataSource().size() != 0) {
                EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                excelReport.setReportTitle(myUI.getMessage(IndigoMessages.DiscountsReport));
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                excelReport.getTotalsRow().getCell(1).setCellFormula(null);
                excelReport.getTotalsRow().getCell(6).setCellFormula(null);
                excelReport.getTotalsRow().getCell(0).setCellValue(dataTable.getColumnFooter(myUI.getMessage(IndigoMessages.Id)));
                excelReport.getTotalsRow().getCell(1).setCellValue(dataTable.getColumnFooter(myUI.getMessage(IndigoMessages.EducationStatus)));
                excelReport.getTotalsRow().getCell(6).setCellValue(dataTable.getColumnFooter(myUI.getMessage(IndigoMessages.DiscountType)));
                excelReport.sendConverted();
            }
        } else if (source == selectAllClassesBtn) {
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllClassesBtn) {
            classTable.setValue(null);
        } else if (source == selectAllDiscountsBtn) {
            discountsTable.setValue(discountsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllDiscountsBtn) {
            discountsTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == classTable && classTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == discountsTable && discountsTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == yearSelect) {
            try {
                DbDiscount dbd = new DbDiscount();
                dbd.connect();
                discountsTable.setContainerDataSource(
                        dbd.exec_disc_select(myUI, (Integer) yearSelect.getValue(), myUI.getUser().getSchool().getCurrency_id()));
                dbd.close();
                discountsTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            excelBtn.setEnabled(false);
        }
    }
}
