/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.reports.students;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.dao.DbClassName;
import kg.alex.ellipse.dao.DbDefinition;
import kg.alex.ellipse.dao.DbLeavingReasons;
import kg.alex.ellipse.dao.DbStudentOrder;
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

public class OutOfList implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(OutOfList.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final String[] NATURAL_COL_ORDER;
    public double nets, paid_amounts;
    private Button generateBtn, selectAllBtn, deselectAllBtn, excelBtn;
    private ComboBoxMultiselect yearSelectMCB, reasonsMCB;
    private FormattedTable dataTable;
    private FilterTable fromClassTable;

    public OutOfList(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
        NATURAL_COL_ORDER = new String[]{
                myUI.getMessage(Messages.Id),
                myUI.getMessage(Messages.FirstName),
                myUI.getMessage(Messages.LastName),
                myUI.getMessage(Messages.Year),
                myUI.getMessage(Messages.Reasons),
                myUI.getMessage(Messages.FromClass),
                myUI.getMessage(Messages.ToClass),
                myUI.getMessage(Messages.Date),
                myUI.getMessage(Messages.Net),
                myUI.getMessage(Messages.Paid),
                myUI.getMessage(Messages.Left)};
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 5);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelectMCB = new ComboBoxMultiselect(myUI.getMessage(Messages.Year));
        yearSelectMCB.setRequired(true);
        yearSelectMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelectMCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        yearSelectMCB.setWidth(Settings.PERCENTS100);
        yearSelectMCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        yearSelectMCB.setFilteringMode(FilteringMode.CONTAINS);
        yearSelectMCB.setClearButtonCaption(myUI.getMessage(Messages.Clear));
        yearSelectMCB.setShowSelectAllButton((filter, page) -> true);
        yearSelectMCB.setSelectAllButtonCaption(myUI.getMessage(Messages.SelectAll));

        reasonsMCB = new ComboBoxMultiselect(myUI.getMessage(Messages.Reasons));
        reasonsMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        reasonsMCB.setWidth(Settings.PERCENTS100);
        reasonsMCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        reasonsMCB.setFilteringMode(FilteringMode.CONTAINS);
        reasonsMCB.setClearButtonCaption(myUI.getMessage(Messages.Clear));
        reasonsMCB.setShowSelectAllButton((filter, page) -> true);
        reasonsMCB.setSelectAllButtonCaption(myUI.getMessage(Messages.SelectAll));

        try {
            DbDefinition dbd = new DbDefinition();
            DbLeavingReasons dblr = new DbLeavingReasons();
            dbd.connect();
            dblr.connect();
            yearSelectMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            reasonsMCB.setContainerDataSource(
                    dblr.exec_for_select(myUI, true));
            dblr.close();
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        yearSelectMCB.addValueChangeListener(this);

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

        fromClassTable = new FilterTable();
        fromClassTable.setFilterDecorator(new MyFilterDecorator(myUI));
        fromClassTable.setStyleName(ValoTheme.TABLE_SMALL);
        fromClassTable.setSizeFull();
        fromClassTable.setNullSelectionAllowed(false);
        fromClassTable.setMultiSelect(true);
        fromClassTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        fromClassTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        fromClassTable.setFilterBarVisible(true);
        fromClassTable.setFooterVisible(false);
        fromClassTable.setSelectable(true);
        fromClassTable.addValueChangeListener(this);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            fromClassTable.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fromClassTable.setVisibleColumns((Object[]) new String[]{myUI.getMessage(Messages.Title)});

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

        leftGrid.addComponent(yearSelectMCB, 0, 0, 3, 0);
        leftGrid.addComponent(reasonsMCB, 0, 1, 3, 1);
        leftGrid.addComponent(selectAllBtn, 0, 2, 1, 2);
        leftGrid.addComponent(deselectAllBtn, 2, 2, 3, 2);
        leftGrid.addComponent(fromClassTable, 0, 3, 3, 3);
        leftGrid.addComponent(generateBtn, 0, 4, 2, 4);
        leftGrid.addComponent(excelBtn, 3, 4);
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
        vl.addComponent(dataTable);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (fromClassTable.getValue() != null) {
                try {
                    DbStudentOrder dbor = new DbStudentOrder();
                    dbor.connect();
                    IndexedContainer dataCont = dbor.execSQL_outOf(myUI,
                            Settings.convertCollectionToStr((Set<?>) yearSelectMCB.getValue()),
                            Settings.convertCollectionToStr((Set<?>) fromClassTable.getValue()),
                            getMultiComboCaptions((Set<?>) reasonsMCB.getValue()),
                            myUI.getUser().getSchool().getId(), this);
                    dataTable.setContainerDataSource(dataCont);
                    dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Net), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Paid), Table.Align.RIGHT);
                    dataTable.setColumnAlignment(myUI.getMessage(Messages.Left), Table.Align.RIGHT);

                    dataTable.setColumnFooter(myUI.getMessage(Messages.Id),
                            myUI.getMessage(Messages.Students) + ": " + dataCont.size());
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Net),
                            Settings.dFormat2.format(nets));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Paid),
                            Settings.dFormat2.format(paid_amounts));
                    dataTable.setColumnFooter(myUI.getMessage(Messages.Left),
                            Settings.dFormat2.format(nets - paid_amounts));
                    if (dataCont.size() != 0) {
                        excelBtn.setEnabled(true);
                    }
                    dbor.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(myUI.getMessage(Messages.OutOfReport));
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(0).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.Id)));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllBtn) {
            fromClassTable.setValue(fromClassTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBtn) {
            fromClassTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == fromClassTable && fromClassTable.getValue() != null) {
            excelBtn.setEnabled(false);
        } else if (property == yearSelectMCB) {
            excelBtn.setEnabled(false);
        }
    }

    private String getMultiComboCaptions(Set<?> set) {
        if (!set.isEmpty()) {
            Iterator<?> iter = set.iterator();
            boolean isFirst = true;
            StringBuilder reasons = new StringBuilder();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (!isFirst) {
                    reasons.append(", ");
                }
                reasons.append(reasonsMCB.getContainerProperty(next,
                        myUI.getMessage(Messages.Title)).getValue());
                isFirst = false;
            }
            return reasons.toString();
        } else {
            return null;
        }
    }
}
