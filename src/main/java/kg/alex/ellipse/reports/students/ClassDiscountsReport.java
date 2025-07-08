/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.dao.DbClassName;
import kg.alex.ellipse.dao.DbDefinition;
import kg.alex.ellipse.dao.DbDiscount;
import kg.alex.ellipse.dao.DbStudentDiscount;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.tableexport.EnhancedFormatExcelExport;
import kg.alex.ellipse.utils.FormattedTable;
import kg.alex.ellipse.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Iterator;
import java.util.Set;

public class ClassDiscountsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ClassDiscountsReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    public FormattedTable dataTable;
    public FilterTable classTable, discountsTable;
    private Button generateBtn, selectAllClassesBtn, deselectAllClassesBtn,
            selectAllDiscountsBtn, deselectAllDiscountsBtn, excelBtn;
    private ComboBox yearSelect;
    private ComboBoxMultiselect educationStatusMCB;

    public ClassDiscountsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 7);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        selectAllClassesBtn = new Button(myUI.getMessage(Messages.AllClasses));
        selectAllClassesBtn.setWidth(Settings.PERCENTS100);
        selectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllClassesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllClassesBtn.addClickListener(this);

        deselectAllClassesBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllClassesBtn.setWidth(Settings.PERCENTS100);
        deselectAllClassesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllClassesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllClassesBtn.addClickListener(this);

        classTable = new FilterTable();
        classTable.setFilterDecorator(new MyFilterDecorator(myUI));
        classTable.setStyleName(ValoTheme.TABLE_SMALL);
        classTable.setSizeFull();
        classTable.setNullSelectionAllowed(false);
        classTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        classTable.setFilterBarVisible(true);
        classTable.setFooterVisible(false);
        classTable.setSelectable(true);
        classTable.setNullSelectionAllowed(false);
        classTable.setMultiSelect(true);
        classTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        classTable.addValueChangeListener(this);
        try {
            DbClassName dbCon = new DbClassName();
            dbCon.connect();
            classTable.setContainerDataSource(dbCon.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(Messages.Title)});

        selectAllDiscountsBtn = new Button(myUI.getMessage(Messages.AllDiscounts));
        selectAllDiscountsBtn.setWidth(Settings.PERCENTS100);
        selectAllDiscountsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllDiscountsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllDiscountsBtn.addClickListener(this);

        deselectAllDiscountsBtn = new Button(myUI.getMessage(Messages.Clear));
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

        generateBtn = new Button(myUI.getMessage(Messages.ShowButton));
        generateBtn.setWidth(Settings.PERCENTS100);
        generateBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.PLUS_SQUARE);
        generateBtn.addClickListener(this);

        excelBtn = new Button();
        excelBtn.setDescription(myUI.getMessage(Messages.ExportToExcel));
        excelBtn.setWidth(Settings.PERCENTS100);
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

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
        educationStatusMCB.setValue(Settings.convertToSet(
                educationStatusMCB.getContainerDataSource().getItemIds()));

        yearSelect.addValueChangeListener(this);
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(educationStatusMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllClassesBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllClassesBtn, 2, 2, 3, 2);
        leftGrid.addComponent(classTable, 0, 3, 3, 3);
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
            if (!((Set<?>) classTable.getValue()).isEmpty() && !((Set<?>) discountsTable.getValue()).isEmpty()) {
                try {
                    DbStudentDiscount dbCon = new DbStudentDiscount();
                    dbCon.connect();
                    dataTable.setContainerDataSource(null);
                    dbCon.execSQL_Discounts_by_classes(myUI, (Integer) yearSelect.getValue(),
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            this);
                    for (Object next : (Set<?>) classTable.getValue()) {
                        dataTable.setColumnAlignment(classTable.getContainerProperty(
                                next, myUI.getMessage(Messages.Title)).getValue() + " "
                                + myUI.getMessage(Messages.Students), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(classTable.getContainerProperty(
                                next, myUI.getMessage(Messages.Title)).getValue() + " "
                                + myUI.getMessage(Messages.DiscountAmount), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(classTable.getContainerProperty(
                                next, myUI.getMessage(Messages.Title)).getValue()
                                + " " + myUI.getMessage(Messages.Average) + "%", Table.Align.RIGHT);
                        try {
                            dataTable.setColumnFooter(classTable.getContainerProperty(
                                            next, myUI.getMessage(Messages.Title)).getValue()
                                            + " " + myUI.getMessage(Messages.Average) + "%",
                                    Settings.dFormat2.format(Double.parseDouble(
                                            dataTable.getColumnFooter(classTable.getContainerProperty(
                                                    next, myUI.getMessage(Messages.Title)).getValue()
                                                    + " " + myUI.getMessage(Messages.Average) + "%"))
                                            / dataTable.getContainerDataSource().size()));
                        } catch (Exception ignored) {
                        }
                    }
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.Students), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.DiscountAmount), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Total) + " "
                            + myUI.getMessage(Messages.Average) + "%", Table.Align.RIGHT);

                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(myUI.getMessage(Messages.SchoolDiscounts));
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    Iterator<?> school_iter = ((Set<?>) classTable.getValue()).iterator();
                    int i = 3;
                    while (school_iter.hasNext()) {
                        Object next = school_iter.next();
                        excelReport.getTotalsRow().getCell(i).setCellFormula(null);
                        excelReport.getTotalsRow().getCell(i).setCellValue(
                                dataTable.getColumnFooter(classTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()
                                        + " " + myUI.getMessage(Messages.Average) + "%"));
                        i += 3;
                    }
                    excelReport.getTotalsRow().getCell(i).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(i).setCellValue(
                            dataTable.getColumnFooter(myUI.getMessage(Messages.Total) + " "
                                    + myUI.getMessage(Messages.Average) + "%"));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
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
            excelBtn.setEnabled(false);
            try {
                DbDiscount dbd = new DbDiscount();
                dbd.connect();
                discountsTable.setContainerDataSource(dbd.exec_disc_select(myUI, (Integer) yearSelect.getValue(),
                        myUI.getUser().getSchool().getCurrency_id()));
                dbd.close();
                discountsTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(Messages.Title)});
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
