/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.reports.hr;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.dao.DbDefinition;
import kg.alex.abl.dao.DbEmployeeLessons;
import kg.alex.abl.dao.DbSchool;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.tableexport.EnhancedFormatExcelExport;
import kg.alex.abl.utils.FormattedTable;
import kg.alex.abl.utils.MyFilterDecorator;
import kg.alex.abl.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class HRLessonHoursReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(HRLessonHoursReport.class);
    private final MyVaadinUI myUI;
    private final HorizontalSplitPanel splitPanel;
    private final String[] NATURAL_COL_ORDER;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final VerticalLayout vl = new VerticalLayout();
    private Button generateBtn, excelBtn, selectAllSchoolsBtn, deselectAllSchoolsBtn,
            selectAllBranchesBtn, deselectAllBranchesBtn, selectAllPositionsBtn, deselectAllPositionsBtn,
            selectAllExtraPositionsBtn, deselectAllExtraPositionsBtn;
    private FilterTable schoolTable, branchTable, positionTable, extraPositionTable;
    private ComboBoxMultiselect workingStatusesMCB;
    private ComboBox yearSelect;
    private EnhancedFormatExcelExport excelReport;

    public HRLessonHoursReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.Title)};
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {

        GridLayout leftGrid = new GridLayout(4, 4);
        leftGrid.setSizeFull();
        leftGrid.setSpacing(true);

        yearSelect = new ComboBox(myUI.getMessage(Messages.LessonsYear));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);

        selectAllSchoolsBtn = new Button(myUI.getMessage(Messages.AllSchools));
        selectAllSchoolsBtn.setWidth(Settings.PERCENTS100);
        selectAllSchoolsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllSchoolsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllSchoolsBtn.addClickListener(this);

        deselectAllSchoolsBtn = new Button(myUI.getMessage(Messages.Clear));
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
            schoolTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        GridLayout schoolsGrid = new GridLayout(2, 2);
        schoolsGrid.setMargin(new MarginInfo(true, false, false, false));
        schoolsGrid.setSizeFull();
        schoolsGrid.setSpacing(true);
        schoolsGrid.addComponent(selectAllSchoolsBtn);
        schoolsGrid.addComponent(deselectAllSchoolsBtn);
        schoolsGrid.addComponent(schoolTable, 0, 1, 1, 1);
        schoolsGrid.setRowExpandRatio(1, 1);

        selectAllBranchesBtn = new Button(myUI.getMessage(Messages.AllBranches));
        selectAllBranchesBtn.setWidth(Settings.PERCENTS100);
        selectAllBranchesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllBranchesBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllBranchesBtn.addClickListener(this);

        deselectAllBranchesBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllBranchesBtn.setWidth(Settings.PERCENTS100);
        deselectAllBranchesBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllBranchesBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllBranchesBtn.addClickListener(this);

        branchTable = new FilterTable();
        branchTable.setFilterDecorator(new MyFilterDecorator(myUI));
        branchTable.setStyleName(ValoTheme.TABLE_SMALL);
        branchTable.setSizeFull();
        branchTable.setNullSelectionAllowed(false);
        branchTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        branchTable.setFilterBarVisible(true);
        branchTable.setFooterVisible(false);
        branchTable.setSelectable(true);
        branchTable.setNullSelectionAllowed(false);
        branchTable.setMultiSelect(true);
        branchTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        branchTable.addValueChangeListener(this);

        GridLayout branchesGrid = new GridLayout(2, 2);
        branchesGrid.setMargin(new MarginInfo(true, false, false, false));
        branchesGrid.setSizeFull();
        branchesGrid.setSpacing(true);
        branchesGrid.addComponent(selectAllBranchesBtn);
        branchesGrid.addComponent(deselectAllBranchesBtn);
        branchesGrid.addComponent(branchTable, 0, 1, 1, 1);
        branchesGrid.setRowExpandRatio(1, 1);

        selectAllPositionsBtn = new Button(myUI.getMessage(Messages.AllPositions));
        selectAllPositionsBtn.setWidth(Settings.PERCENTS100);
        selectAllPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllPositionsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllPositionsBtn.addClickListener(this);

        deselectAllPositionsBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllPositionsBtn.setWidth(Settings.PERCENTS100);
        deselectAllPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllPositionsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllPositionsBtn.addClickListener(this);

        positionTable = new FilterTable();
        positionTable.setFilterDecorator(new MyFilterDecorator(myUI));
        positionTable.setStyleName(ValoTheme.TABLE_SMALL);
        positionTable.setSizeFull();
        positionTable.setNullSelectionAllowed(false);
        positionTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        positionTable.setFilterBarVisible(true);
        positionTable.setFooterVisible(false);
        positionTable.setSelectable(true);
        positionTable.setNullSelectionAllowed(false);
        positionTable.setMultiSelect(true);
        positionTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        positionTable.addValueChangeListener(this);

        GridLayout positionsGrid = new GridLayout(2, 2);
        positionsGrid.setMargin(new MarginInfo(true, false, false, false));
        positionsGrid.setSizeFull();
        positionsGrid.setSpacing(true);
        positionsGrid.addComponent(selectAllPositionsBtn);
        positionsGrid.addComponent(deselectAllPositionsBtn);
        positionsGrid.addComponent(positionTable, 0, 1, 1, 1);
        positionsGrid.setRowExpandRatio(1, 1);

        selectAllExtraPositionsBtn = new Button(myUI.getMessage(Messages.AllExtraPositions));
        selectAllExtraPositionsBtn.setWidth(Settings.PERCENTS100);
        selectAllExtraPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        selectAllExtraPositionsBtn.setIcon(FontAwesome.CHECK_SQUARE);
        selectAllExtraPositionsBtn.addClickListener(this);

        deselectAllExtraPositionsBtn = new Button(myUI.getMessage(Messages.Clear));
        deselectAllExtraPositionsBtn.setWidth(Settings.PERCENTS100);
        deselectAllExtraPositionsBtn.addStyleName(ValoTheme.BUTTON_TINY);
        deselectAllExtraPositionsBtn.setIcon(FontAwesome.MINUS_SQUARE);
        deselectAllExtraPositionsBtn.addClickListener(this);

        extraPositionTable = new FilterTable();
        extraPositionTable.setFilterDecorator(new MyFilterDecorator(myUI));
        extraPositionTable.setStyleName(ValoTheme.TABLE_SMALL);
        extraPositionTable.setSizeFull();
        extraPositionTable.setNullSelectionAllowed(false);
        extraPositionTable.setColumnHeaderMode(CustomTable.ColumnHeaderMode.HIDDEN);
        extraPositionTable.setFilterBarVisible(true);
        extraPositionTable.setFooterVisible(false);
        extraPositionTable.setSelectable(true);
        extraPositionTable.setNullSelectionAllowed(false);
        extraPositionTable.setMultiSelect(true);
        extraPositionTable.setMultiSelectMode(MultiSelectMode.SIMPLE);
        extraPositionTable.addValueChangeListener(this);

        GridLayout extraPositionsGrid = new GridLayout(2, 2);
        extraPositionsGrid.setMargin(new MarginInfo(true, false, false, false));
        extraPositionsGrid.setSizeFull();
        extraPositionsGrid.setSpacing(true);
        extraPositionsGrid.addComponent(selectAllExtraPositionsBtn);
        extraPositionsGrid.addComponent(deselectAllExtraPositionsBtn);
        extraPositionsGrid.addComponent(extraPositionTable, 0, 1, 1, 1);
        extraPositionsGrid.setRowExpandRatio(1, 1);

        workingStatusesMCB = new ComboBoxMultiselect(myUI.getMessage(Messages.WorkingStatus));
        workingStatusesMCB.setRequired(true);
        workingStatusesMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        workingStatusesMCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        workingStatusesMCB.setWidth(Settings.PERCENTS100);
        workingStatusesMCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        workingStatusesMCB.setFilteringMode(FilteringMode.CONTAINS);
        workingStatusesMCB.setClearButtonCaption(myUI.getMessage(Messages.Clear));
        workingStatusesMCB.setShowSelectedOnTop(false);
        workingStatusesMCB.setShowSelectAllButton((filter, page) -> true);
        workingStatusesMCB.setSelectAllButtonCaption(myUI.getMessage(Messages.SelectAll));
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            workingStatusesMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbWorking_status, true));
            branchTable.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbBranchTable, true));
            positionTable.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr)));
            positionTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
            extraPositionTable.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr)));
            extraPositionTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        workingStatusesMCB.setValue(Settings.convertToSet(
                workingStatusesMCB.getContainerDataSource().getItemIds()));
        workingStatusesMCB.addValueChangeListener(this);

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        yearSelect.addValueChangeListener(this);

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

        Accordion acr = new Accordion();
        acr.setSizeFull();
        acr.setTabCaptionsAsHtml(true);
        acr.addTab(schoolsGrid, myUI.getMessage(Messages.Schools) + "<font color='red'> *</font>");
        acr.addTab(branchesGrid, myUI.getMessage(Messages.Lessons) + "<font color='red'> *</font>");
        acr.addTab(positionsGrid, myUI.getMessage(Messages.MainPositions) + "<font color='red'> *</font>");
        acr.addTab(extraPositionsGrid, myUI.getMessage(Messages.ExtraPosition));

        leftGrid.addComponent(yearSelect, 0, 0, 3, 0);
        leftGrid.addComponent(acr, 0, 1, 3, 1);
        leftGrid.addComponent(workingStatusesMCB, 0, 2, 3, 2);
        leftGrid.addComponent(generateBtn, 0, 3, 2, 3);
        leftGrid.addComponent(excelBtn, 3, 3);
        leftGrid.setRowExpandRatio(1, 1);
        ((GridLayout) splitPanel.getFirstComponent()).removeComponent(0, 2);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(leftGrid, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        vl.setWidth(Settings.PERCENTS100);
        vl.setSpacing(true);
        vl.setMargin(true);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            vl.setHeightUndefined();
            vl.removeAllComponents();
            if (yearSelect.isValid() && workingStatusesMCB.isValid()) {
                if (!((Set<?>) schoolTable.getValue()).isEmpty() && !((Set<?>) branchTable.getValue()).isEmpty()
                        && !((Set<?>) positionTable.getValue()).isEmpty()) {
                    IndexedContainer container = null;
                    LinkedList<Integer> branchIds = null;
                    int totalEmployees = 0;
                    int totalAsMainBranch = 0;
                    int totalAsExtraBranch = 0;
                    if (((Set<?>) schoolTable.getValue()).size() > 1) {
                        container = new IndexedContainer();
                        container.addContainerProperty(myUI.getMessage(Messages.Lesson), String.class, null);
                        container.addContainerProperty(myUI.getMessage(Messages.Employee), String.class,
                                myUI.getMessage(Messages.TotalEmployees) + "0");
                        container.addContainerProperty(myUI.getMessage(Messages.Hours), Integer.class, 0);
                        container.addContainerProperty(myUI.getMessage(Messages.ExtraHours), Integer.class, 0);
                        container.addContainerProperty(myUI.getMessage(Messages.MainBranch), String.class, "0");
                        container.addContainerProperty(myUI.getMessage(Messages.ExtraBranches), String.class, "0");
                        container.addItem("t");
                        branchIds = new LinkedList<>((Set<Integer>) branchTable.getValue());
                        Collections.sort(branchIds);
                    }
                    for (Integer next : (Set<Integer>) schoolTable.getValue()) {
                        try {
                            Label schoolNameLbl = new Label();
                            schoolNameLbl.setWidth(Settings.PERCENTS100);
                            schoolNameLbl.setContentMode(ContentMode.HTML);
                            schoolNameLbl.setValue(schoolTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Title)).getValue().toString());
                            schoolNameLbl.setStyleName("tableCpt");
                            vl.addComponent(schoolNameLbl);

                            DbEmployeeLessons dbCon = new DbEmployeeLessons();
                            dbCon.connect();
                            FormattedTable dataTable = new FormattedTable(myUI);
                            dataTable.setFooterVisible(false);
                            dataTable.setWidth(Settings.PERCENTS100);
                            dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
                            dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
                            dataTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
                            dataTable.addStyleName("noWrapHeader");
                            dataTable.setCellStyleGenerator((Table.CellStyleGenerator) (source1, itemId, propertyId) -> {

                                if (propertyId == null) {
                                    // Styling for row
                                    if (itemId.toString().startsWith("s")) {
                                        return "highlight-green";
                                    } else if (itemId.toString().startsWith("t")) {
                                        return "highlight-darkgreen";
                                    } else {
                                        return null;
                                    }
                                } else {
                                    // styling for column propertyId
                                    return null;
                                }
                            });
                            vl.addComponent(dataTable);
                            if (((Set<?>) schoolTable.getValue()).size() == 1) {
                                vl.setHeight("100%");
                                vl.setExpandRatio(dataTable, 1);
                                dataTable.setHeight("100%");
                            } else {
                                dataTable.setPageLength(0);
                            }

                            dataTable.setContainerDataSource(dbCon.execSQLHours(myUI, (Integer) yearSelect.getValue(), next,
                                    Settings.convertCollectionToStr((Set<?>) branchTable.getValue()),
                                    Settings.convertCollectionToStr((Set<?>) positionTable.getValue()),
                                    Settings.convertCollectionToStr((Set<?>) extraPositionTable.getValue()),
                                    Settings.convertCollectionToStr((Set<?>) workingStatusesMCB.getValue())));
                            dataTable.setColumnAlignment(myUI.getMessage(Messages.Hours), Table.Align.RIGHT);
                            dataTable.setColumnAlignment(myUI.getMessage(Messages.ExtraHours), Table.Align.RIGHT);
                            if (((Set<?>) schoolTable.getValue()).size() > 1) {

                                Item item;
                                if (container != null) {
                                    item = container.getItem("t");
                                    item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(
                                            (Integer) item.getItemProperty(myUI.getMessage(Messages.Hours)).getValue()
                                                    + (Integer) dataTable.getContainerProperty("t", myUI.getMessage(Messages.Hours)).getValue());
                                    item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                                            (Integer) item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).getValue()
                                                    + (Integer) dataTable.getContainerProperty("t", myUI.getMessage(Messages.ExtraHours)).getValue());
                                    totalEmployees += Integer.parseInt((dataTable.getContainerProperty("t", myUI.getMessage(Messages.Employee)).getValue()
                                            .toString().replace(myUI.getMessage(Messages.TotalEmployees), "")));
                                    item.getItemProperty(myUI.getMessage(Messages.Employee)).setValue(
                                            myUI.getMessage(Messages.TotalEmployees) + totalEmployees);
                                    totalAsMainBranch += Integer.parseInt((dataTable.getContainerProperty("t", myUI.getMessage(Messages.MainBranch)).getValue()
                                            .toString().replace(myUI.getMessage(Messages.TotalEmployeesAsMainBranch) + ": ", "")));
                                    item.getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(
                                            myUI.getMessage(Messages.TotalEmployeesAsMainBranch) + ": " + totalAsMainBranch);
                                    totalAsExtraBranch += Integer.parseInt((dataTable.getContainerProperty("t", myUI.getMessage(Messages.ExtraBranches)).getValue()
                                            .toString().replace(myUI.getMessage(Messages.TotalEmployeesAsExtraBranch) + ": ", "")));
                                    item.getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(
                                            myUI.getMessage(Messages.TotalEmployeesAsExtraBranch) + ": " + totalAsExtraBranch);
                                    item.getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(
                                            myUI.getMessage(Messages.TotalBranches) + ((Set<?>) branchTable.getValue()).size());
                                }
                                Iterator<Integer> iter2 = null;
                                if (branchIds != null) {
                                    iter2 = branchIds.descendingIterator();
                                }
                                while (iter2 != null && iter2.hasNext()) {
                                    Integer branchId = iter2.next();
                                    item = container.addItem("s" + branchId);
                                    if (item == null) {
                                        item = container.getItem("s" + branchId);
                                    }
                                    item.getItemProperty(myUI.getMessage(Messages.Lesson)).setValue(
                                            branchTable.getContainerProperty(branchId, myUI.getMessage(Messages.Title)).getValue());
                                    item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(
                                            (Integer) item.getItemProperty(myUI.getMessage(Messages.Hours)).getValue()
                                                    + (Integer) dataTable.getContainerProperty("s" + branchId, myUI.getMessage(Messages.Hours)).getValue());
                                    item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                                            (Integer) item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).getValue()
                                                    + (Integer) dataTable.getContainerProperty("s" + branchId, myUI.getMessage(Messages.ExtraHours)).getValue());
                                    item.getItemProperty(myUI.getMessage(Messages.Employee)).setValue(
                                            myUI.getMessage(Messages.TotalEmployees)
                                                    + (Integer.parseInt(item.getItemProperty(myUI.getMessage(Messages.Employee)).getValue().toString()
                                                    .replace(myUI.getMessage(Messages.TotalEmployees), ""))
                                                    + Integer.parseInt(dataTable.getContainerProperty("s" + branchId, myUI.getMessage(Messages.Employee)).getValue().toString()
                                                    .replace(myUI.getMessage(Messages.TotalEmployees), ""))));
                                    item.getItemProperty(myUI.getMessage(Messages.MainBranch)).setValue(
                                            myUI.getMessage(Messages.TotalEmployeesAsMainBranch)
                                                    + branchTable.getContainerProperty(branchId, myUI.getMessage(Messages.Title)).getValue() + ": "
                                                    + (Integer.parseInt(item.getItemProperty(myUI.getMessage(Messages.MainBranch)).getValue().toString()
                                                    .replace(myUI.getMessage(Messages.TotalEmployeesAsMainBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(Messages.Title)).getValue() + ": ", ""))
                                                    + Integer.parseInt(dataTable.getContainerProperty("s" + branchId, myUI.getMessage(Messages.MainBranch)).getValue().toString()
                                                    .replace(myUI.getMessage(Messages.TotalEmployeesAsMainBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(Messages.Title)).getValue() + ": ", ""))));
                                    item.getItemProperty(myUI.getMessage(Messages.ExtraBranches)).setValue(
                                            myUI.getMessage(Messages.TotalEmployeesAsExtraBranch)
                                                    + branchTable.getContainerProperty(branchId, myUI.getMessage(Messages.Title)).getValue() + ": "
                                                    + (Integer.parseInt(item.getItemProperty(myUI.getMessage(Messages.ExtraBranches)).getValue().toString()
                                                    .replace(myUI.getMessage(Messages.TotalEmployeesAsExtraBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(Messages.Title)).getValue() + ": ", ""))
                                                    + Integer.parseInt(dataTable.getContainerProperty("s" + branchId, myUI.getMessage(Messages.ExtraBranches)).getValue().toString()
                                                    .replace(myUI.getMessage(Messages.TotalEmployeesAsExtraBranch)
                                                            + branchTable.getContainerProperty(branchId, myUI.getMessage(Messages.Title)).getValue() + ": ", ""))));
                                }
                            }
                            if (dataTable.getContainerDataSource().size() != 0) {
                                excelBtn.setEnabled(true);
                            }
                            dbCon.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    }
                    if (((Set<?>) schoolTable.getValue()).size() > 1) {
                        Label schoolNameLbl = new Label();
                        schoolNameLbl.setWidth(Settings.PERCENTS100);
                        schoolNameLbl.setContentMode(ContentMode.HTML);
                        schoolNameLbl.setValue(myUI.getMessage(Messages.AllSchools));
                        schoolNameLbl.setStyleName("tableCpt");
                        vl.addComponent(schoolNameLbl);

                        FormattedTable dataTable = new FormattedTable(myUI);
                        dataTable.setFooterVisible(false);
                        dataTable.setWidth(Settings.PERCENTS100);
                        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
                        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
                        dataTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
                        dataTable.addStyleName("noWrapHeader");
                        dataTable.setCellStyleGenerator((Table.CellStyleGenerator) (source12, itemId, propertyId) -> {

                            if (propertyId == null) {
                                // Styling for row
                                if (itemId.toString().startsWith("s")) {
                                    return "highlight-green";
                                } else if (itemId.toString().startsWith("t")) {
                                    return "highlight-darkgreen";
                                } else {
                                    return null;
                                }
                            } else {
                                // styling for column propertyId
                                return null;
                            }
                        });
                        vl.addComponent(dataTable);
                        dataTable.setPageLength(0);

                        dataTable.setContainerDataSource(container);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.Hours), Table.Align.RIGHT);
                        dataTable.setColumnAlignment(myUI.getMessage(Messages.ExtraHours), Table.Align.RIGHT);

                    }
                } else {
                    Notification.show(myUI.getMessage(Messages.RequiredField),
                            Notification.Type.WARNING_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(Messages.RequiredField),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == excelBtn) {
            try {
                for (int i = 0; i < vl.getComponentCount(); i += 2) {
                    if (i == 0) {
                        excelReport = new EnhancedFormatExcelExport((Table) vl.getComponent(i + 1), ((Label) vl.getComponent(i)).getValue());
                        excelReport.setReportTitle(((Label) vl.getComponent(i)).getValue());
                        excelReport.setDisplayTotals(false);

                    } else {
                        excelReport.convertTable();
                        excelReport.setNextTable((Table) vl.getComponent(i + 1), ((Label) vl.getComponent(i)).getValue());
                        excelReport.setReportTitle(((Label) vl.getComponent(i)).getValue());
                        excelReport.setDisplayTotals(false);
                    }
                }
                excelReport.export();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == selectAllSchoolsBtn) {
            schoolTable.setValue(schoolTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllSchoolsBtn) {
            schoolTable.setValue(null);
        } else if (source == selectAllBranchesBtn) {
            branchTable.setValue(branchTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllBranchesBtn) {
            branchTable.setValue(null);
        } else if (source == selectAllPositionsBtn) {
            positionTable.setValue(positionTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllPositionsBtn) {
            positionTable.setValue(null);
        } else if (source == selectAllExtraPositionsBtn) {
            extraPositionTable.setValue(extraPositionTable.getContainerDataSource().getItemIds());
        } else if (source == deselectAllExtraPositionsBtn) {
            extraPositionTable.setValue(null);
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property.getValue() != null) {
            excelBtn.setEnabled(false);
        }
    }
}
