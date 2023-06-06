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
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.dao.DbStudent;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StatusesReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(StatusesReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, selectAllClassesBtn, deselectAllClassesBtn,
            selectAllSchoolsBtn, deselectAllSchoolsBtn, excelBtn;
    private final HorizontalSplitPanel splitPanel;
    private ComboBox yearSelect;
    public ComboBoxMultiselect statusMS;
    public FormattedTable dataTable;
    public FilterTable classTable, schoolsTable;

    private final Subject currentUser = SecurityUtils.getSubject();

    public StatusesReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 7);
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

        statusMS = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Status));
        statusMS.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusMS.setWidth(Settings.PERCENTS100);
        statusMS.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        statusMS.setFilteringMode(FilteringMode.CONTAINS);
        statusMS.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        statusMS.setShowSelectAllButton((filter, page) -> true);
        statusMS.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        statusMS.addValueChangeListener(this);

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
        classTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        classTable.setFilterBarVisible(true);
        classTable.setFooterVisible(false);
        classTable.setSelectable(true);
        classTable.setNullSelectionAllowed(false);
        classTable.setMultiSelect(true);
        classTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        classTable.addValueChangeListener(this);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            statusMS.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbEducationStatus, true));
            classTable.setContainerDataSource(dbd.exec_for_select(myUI, Settings.classTable, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});

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

        schoolsTable = new FilterTable();
        schoolsTable.setFilterDecorator(new MyFilterDecorator(myUI));
        schoolsTable.setStyleName(ValoTheme.TABLE_SMALL);
        schoolsTable.setSizeFull();
        schoolsTable.setNullSelectionAllowed(false);
        schoolsTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        schoolsTable.setFilterBarVisible(true);
        schoolsTable.setFooterVisible(false);
        schoolsTable.setSelectable(true);
        schoolsTable.setNullSelectionAllowed(false);
        schoolsTable.setMultiSelect(true);
        schoolsTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        schoolsTable.addValueChangeListener(this);
        try {
            DbSchool dbsc = new DbSchool();
            dbsc.connect();
            schoolsTable.setContainerDataSource(dbsc.execSchoolSel(myUI, 0));
            dbsc.close();
            schoolsTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(IndigoMessages.Title)});
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

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

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(statusMS, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllClassesBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllClassesBtn, 2, 2, 3, 2);
        leftGrid.addComponent(classTable, 0, 3, 3, 3);
        if (currentUser.hasRole(Settings.rnAdmin)) {
            leftGrid.addComponent(selectAllSchoolsBtn, 0, 4, 1, 4);
            leftGrid.addComponent(deselectAllSchoolsBtn, 2, 4, 3, 4);
            leftGrid.addComponent(schoolsTable, 0, 5, 3, 5);
            leftGrid.setRowExpandRatio(5, 1);
        } else {
            schoolsTable.setValue(convertToSet(myUI.getUser().getSchool().getId()));
        }
        leftGrid.addComponent(generateBtn, 0, 6, 2, 6);
        leftGrid.addComponent(excelBtn, 3, 6);
        leftGrid.setRowExpandRatio(3, 1);
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
        splitPanel.setSecondComponent(dataTable);
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (!((Set<?>) schoolsTable.getValue()).isEmpty()
                    && !((Set<?>) schoolsTable.getValue()).isEmpty()
                    && !((Set<?>) statusMS.getValue()).isEmpty()) {
                try {
                    DbStudent dbs = new DbStudent();
                    dbs.connect();
                    dataTable.setContainerDataSource(null);
                    dbs.execSQL_Statuses_by_classes(myUI,
                            (Integer) yearSelect.getValue(), this);
                    Iterator<?> class_iter = ((Set<?>) classTable.getValue()).iterator();
                    Iterator<?> status_iter;
                    while (class_iter.hasNext()) {
                        Object nextClass = class_iter.next();
                        status_iter = ((Set<?>) statusMS.getValue()).iterator();
                        while (status_iter.hasNext()) {
                            Object nextStatus = status_iter.next();
                            dataTable.setColumnAlignment(classTable.getContainerProperty(
                                    nextClass, myUI.getMessage(IndigoMessages.Title)).getValue() + " "
                                    + myUI.getMessage(IndigoMessages.ClassName) + " "
                                    + statusMS.getContainerProperty(
                                            nextStatus, myUI.getMessage(IndigoMessages.Title)).getValue(),
                                    Table.Align.RIGHT);
                        }
                    }
                    dataTable.setColumnAlignment(myUI.getMessage(IndigoMessages.Total),
                            Table.Align.RIGHT);
                    if (dataTable.getContainerDataSource().size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbs.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable, "sheet1");
                    excelReport.setReportTitle(myUI.getMessage(IndigoMessages.StatusesReport));
                    excelReport.setDisplayTotals(true);
                    excelReport.export();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllClassesBtn) {
            classTable.setValue(classTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllClassesBtn) {
            classTable.setValue(null);
        } else if (source == selectAllSchoolsBtn) {
            schoolsTable.setValue(schoolsTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllSchoolsBtn) {
            schoolsTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == classTable && classTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == schoolsTable && schoolsTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == yearSelect) {
            excelBtn.setEnabled(false);
        } else if (property == statusMS) {
            excelBtn.setEnabled(false);
        }
    }

    private Set<?> convertToSet(int val) {
        HashSet<Integer> hs = new HashSet<>(1);
        hs.add(val);
        return hs;
    }
}
