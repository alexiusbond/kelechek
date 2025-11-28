/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.dao.DbClassName;
import kg.alex.aim.dao.DbDefinition;
import kg.alex.aim.dao.DbStudentContract;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.tableexport.EnhancedFormatExcelExport;
import kg.alex.aim.utils.FormattedTable;
import kg.alex.aim.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Set;

public class DebtsAndRepaymentsReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(DebtsAndRepaymentsReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private Button generateBtn, selectAllBtn, deselectAllBtn, excelBtn;
    private ComboBoxMultiselect educationStatusMCB;
    private ComboBox yearSelect;
    private FormattedTable dataTable;
    private FilterTable classTable;
    private PopupDateField fromDateDF, tillDateDF;

    public DebtsAndRepaymentsReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
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
        leftGrid.addComponent(generateBtn, 0, 5, 2, 5);
        leftGrid.addComponent(excelBtn, 3, 5);
        leftGrid.setRowExpandRatio(4, 1);
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
        dataTable.addStyleName("noWrap");
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (classTable.getValue() != null && ((Set<?>) classTable.getValue()).size() > 0) {
                try {
                    DbStudentContract dbCon = new DbStudentContract();
                    dbCon.connect();
                    dbCon.execDebtsAndRepayments(myUI, (Integer) yearSelect.getValue(),
                            Settings.convertCollectionToStr((Set<?>) classTable.getValue()),
                            Settings.convertCollectionToStr((Set<?>) educationStatusMCB.getValue()),
                            fromDateDF.getValue(), tillDateDF.getValue(), dataTable);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Debt), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Repayment), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Balance), Table.Align.RIGHT);
                    dataTable.setColumnWidth(myUI.getMessage(Messages.ClassName), 80);
                    dataTable.setColumnWidth(myUI.getMessage(Messages.Note), 200);
                    if (dataTable.size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                Notification.show(myUI.getMessage(Messages.NotificationNothingIsSelected),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == excelBtn) {
            if (dataTable.getContainerDataSource().size() != 0) {
                EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                excelReport.setReportTitle(myUI.getMessage(Messages.DebtsAndRepaymentsReport));
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.getTotalsRow().getCell(9).setCellFormula(null);
                excelReport.getTotalsRow().getCell(9).setCellValue(
                        dataTable.getColumnFooter(myUI.getMessage(Messages.Balance)));
                excelReport.sendConverted();
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
            excelBtn.setEnabled(false);
            dataTable.setContainerDataSource(null);
        } else if (property == yearSelect || property == educationStatusMCB
                || property == fromDateDF || property == tillDateDF) {
            excelBtn.setEnabled(false);
            dataTable.setContainerDataSource(null);
        }
    }
}
