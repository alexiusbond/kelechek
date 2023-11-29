/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbDefinition;
import kg.alex.indigo.dao.DbDiscount;
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.dao.DbStudentDiscount;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Iterator;
import java.util.Set;

public class SchoolDiscountsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(SchoolDiscountsReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, selectAllSchoolsBtn, deselectAllSchoolsBtn,
            selectAllDiscountsBtn, deselectAllDiscountsBtn, excelBtn;
    private final HorizontalSplitPanel splitPanel;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;
    public FormattedTable dataTable;
    public FilterTable schoolTable, discountsTable;

    public SchoolDiscountsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 7);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllSchoolsBtn = new Button(myUI.getMessage(IndigoMessages.AllSchools));
        selectAllSchoolsBtn.setWidth(Settings.PERCENTS100);
        selectAllSchoolsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllSchoolsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllSchoolsBtn.addClickListener(this);

        deselectAllSchoolsBtn = new Button(myUI.getMessage(IndigoMessages.Clear));
        deselectAllSchoolsBtn.setWidth(Settings.PERCENTS100);
        deselectAllSchoolsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllSchoolsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllSchoolsBtn.addClickListener(this);

        schoolTable = new FilterTable();
        schoolTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolTable.setSizeFull();
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolTable.setFilterBarVisible(true);
        schoolTable.setFooterVisible(false);
        schoolTable.setSelectable(true);
        schoolTable.setNullSelectionAllowed(false);
        schoolTable.setMultiSelect(true);
        schoolTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolTable.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolTable.setContainerDataSource(dbs.execSchoolSel(myUI, 0));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

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
        discountsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        discountsTable.setFilterBarVisible(true);
        discountsTable.setFooterVisible(false);
        discountsTable.setSelectable(true);
        discountsTable.setNullSelectionAllowed(false);
        discountsTable.setMultiSelect(true);
        discountsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        discountsTable.addValueChangeListener(this);

        generateBtn = new Button(myUI.getMessage(IndigoMessages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(IndigoMessages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        yearSelect = new ComboBox(myUI.getMessage(IndigoMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        educationStatusMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.EducationStatus));
        educationStatusMCB.setRequired(true);
        educationStatusMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
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

        yearSelect.addValueChangeListener(this);
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllSchoolsBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllSchoolsBtn, 2, 2, 3, 2);
        leftGrid.addComponent(schoolTable, 0, 3, 3, 3);
        leftGrid.addComponent(selectAllDiscountsBtn, 0, 4, 1, 4);
        leftGrid.addComponent(deselectAllDiscountsBtn, 2, 4, 3, 4);
        leftGrid.addComponent(discountsTable, 0, 5, 3, 5);
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
        dataTable.addStyleName("noWrapHeader");
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (!((Set<?>) schoolTable.getValue()).isEmpty()
                    && !((Set<?>) discountsTable.getValue()).isEmpty()) {
                try {
                    DbStudentDiscount dbsc = new DbStudentDiscount();
                    dbsc.connect();
                    dataTable.setContainerDataSource(null);
                    dbsc.execSQL_Discounts_by_schools(myUI, (Integer) yearSelect.getValue(),
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            this);
                    for (Object next : (Set<?>) schoolTable.getValue()) {
                        dataTable.setColumnAlignment(schoolTable.getContainerProperty(
                                next, myUI.getMessage(IndigoMessages.Title)).getValue() + " "
                                + myUI.getMessage(IndigoMessages.Students), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(schoolTable.getContainerProperty(
                                next, myUI.getMessage(IndigoMessages.Title)).getValue() + " "
                                + myUI.getMessage(IndigoMessages.DiscountAmount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(schoolTable.getContainerProperty(
                                next, myUI.getMessage(IndigoMessages.Title)).getValue()
                                + " " + myUI.getMessage(IndigoMessages.Average) + "%", Table.Align.RIGHT);
                        try {
                            dataTable.setColumnFooter(schoolTable.getContainerProperty(
                                            next, myUI.getMessage(IndigoMessages.Title)).getValue()
                                            + " " + myUI.getMessage(IndigoMessages.Average) + "%",
                                    Settings.dFormat2.format(Double.parseDouble(dataTable.getColumnFooter(schoolTable.getContainerProperty(
                                            next, myUI.getMessage(IndigoMessages.Title)).getValue()
                                            + " " + myUI.getMessage(IndigoMessages.Average) + "%"))
                                            / dataTable.getContainerDataSource().size()));
                        } catch (Exception ignored) {
                        }
                    }
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Total) + " "
                            + myUI.getMessage(IndigoMessages.Students), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Total) + " "
                            + myUI.getMessage(IndigoMessages.DiscountAmount), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Total) + " "
                            + myUI.getMessage(IndigoMessages.Average) + "%", Table.Align.RIGHT);
                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbsc.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(myUI.getMessage(IndigoMessages.SchoolDiscounts));
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    Iterator<?> school_iter = ((Set<?>) schoolTable.getValue()).iterator();
                    int i = 3;
                    while (school_iter.hasNext()) {
                        Object next = school_iter.next();
                        excelReport.getTotalsRow().getCell(i).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(i).setCellValue(
                                dataTable.getColumnFooter(schoolTable.getContainerProperty(next, myUI.getMessage(IndigoMessages.Title)).getValue()
                                        + " " + myUI.getMessage(IndigoMessages.Average) + "%"));
                        i += 3;
                    }
                    excelReport.getTotalsRow().getCell(i).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(i).setCellValue(
                            dataTable.getColumnFooter(myUI.getMessage(IndigoMessages.Total) + " "
                                    + myUI.getMessage(IndigoMessages.Average) + "%"));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllSchoolsBtn) {
            schoolTable.setValue(schoolTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllSchoolsBtn) {
            schoolTable.setValue(null);
        } else if (source == selectAllDiscountsBtn) {
            discountsTable.setValue(discountsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllDiscountsBtn) {
            discountsTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == schoolTable && schoolTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == discountsTable && discountsTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == yearSelect) {
            excelBtn.setEnabled(false);
            try {
                DbDiscount dbd = new DbDiscount();
                dbd.connect();
                discountsTable.setContainerDataSource(
                        dbd.exec_disc_select(myUI, (Integer) yearSelect.getValue(), 0));
                dbd.close();
                discountsTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
