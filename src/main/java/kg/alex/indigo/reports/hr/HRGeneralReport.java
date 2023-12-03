/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.reports.hr;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import de.datenhahn.vaadin.componentrenderer.ComponentRenderer;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.*;
import kg.alex.indigo.domain.Employee;
import kg.alex.indigo.domain.EmployeeExtraInfo;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.ui.EmployeeCvWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HRGeneralReport implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(HRGeneralReport.class);
    private final MyVaadinUI myUI;
    private Button generateBtn, excelBtn;
    private final HorizontalSplitPanel splitPanel;
    private ComboBoxMultiselect schoolsMCB;
    private ComboBoxMultiselect positionsMCB;
    private ComboBoxMultiselect extraPositionsMCB;
    private ComboBoxMultiselect workingStatusesMCB;
    private ComboBoxMultiselect genderMCB;
    private ComboBoxMultiselect nationalityMCB;
    private ComboBoxMultiselect citizenshipMCB;
    private ComboBoxMultiselect martialStatusMCB;
    private ComboBoxMultiselect canBeAdvisorMCB;
    private ComboBoxMultiselect contractTypeMCB;
    private ComboBoxMultiselect gradSchoolMCB;
    private ComboBoxMultiselect healthStatusMCB;
    private ComboBoxMultiselect examMCB;
    private ComboBoxMultiselect mainBranchMCB;
    private ComboBoxMultiselect universityMCB;
    private ComboBoxMultiselect workPlaceMCB;
    private ComboBoxMultiselect certificateMCB;
    private ComboBoxMultiselect languageMCB;
    private ComboBox yearSelect;
    private Grid.FooterRow footer;
    private TextField nameTF, surnameTF, fromAge, toAge;

    private final Subject currentUser = SecurityUtils.getSubject();
    public Grid dataGrid;

    public HRGeneralReport(final MyVaadinUI ui, final HorizontalSplitPanel splitPanel) {
        this.myUI = ui;
        this.splitPanel = splitPanel;
        buildLeftPanel();
        buildRightLayout();
    }

    private void buildLeftPanel() {
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
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY); excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);
        hl.setSpacing(true);
        hl.addComponent(generateBtn);
        hl.addComponent(excelBtn);
        hl.setExpandRatio(generateBtn, 75);
        hl.setExpandRatio(excelBtn, 25);

        ((GridLayout) splitPanel.getFirstComponent()).addComponent(hl, 0, 2);

        Panel p = new Panel();
        p.setSizeFull();
        p.setStyleName(ValoTheme.PANEL_BORDERLESS);

        VerticalLayout leftLay = new VerticalLayout();
        leftLay.setWidth(Settings.PERCENTS100);
        leftLay.setSpacing(true);

        schoolsMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Schools));
        schoolsMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        schoolsMCB.addValueChangeListener(this);
        schoolsMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        schoolsMCB.setWidth(Settings.PERCENTS100);
        schoolsMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        schoolsMCB.setFilteringMode(FilteringMode.CONTAINS);
        schoolsMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        schoolsMCB.setShowSelectAllButton((filter, page) -> true);
        schoolsMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(schoolsMCB);

        nameTF = new TextField(myUI.getMessage(IndigoMessages.FirstName));
        nameTF.setInputPrompt(myUI.getMessage(IndigoMessages.Any));
        nameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        nameTF.setWidth(Settings.PERCENTS100);
        leftLay.addComponent(nameTF);

        surnameTF = new TextField(myUI.getMessage(IndigoMessages.LastName));
        surnameTF.setInputPrompt(myUI.getMessage(IndigoMessages.Any));
        surnameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        surnameTF.setWidth(Settings.PERCENTS100);
        leftLay.addComponent(surnameTF);

        fromAge = new TextField(myUI.getMessage(IndigoMessages.FromAge), new ObjectProperty<>(0));
        fromAge.setInputPrompt(myUI.getMessage(IndigoMessages.Any));
        fromAge.setStyleName(ValoTheme.TEXTFIELD_TINY);
        fromAge.setNullRepresentation("");
        fromAge.setConverter(Settings.getStringToIntegerConverter());
        fromAge.setWidth(Settings.PERCENTS100);
        fromAge.addValidator(new IntegerRangeValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 0, null));
        fromAge.setValue(null);

        toAge = new TextField(myUI.getMessage(IndigoMessages.ToAge), new ObjectProperty<>(0));
        toAge.setInputPrompt(myUI.getMessage(IndigoMessages.Any));
        toAge.setStyleName(ValoTheme.TEXTFIELD_TINY);
        toAge.setNullRepresentation("");
        toAge.setConverter(Settings.getStringToIntegerConverter());
        toAge.setWidth(Settings.PERCENTS100);
        toAge.addValidator(new IntegerRangeValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 0, null));
        toAge.setValue(null);

        hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setWidth(Settings.PERCENTS100);
        hl.addComponent(fromAge);
        hl.addComponent(toAge);
        leftLay.addComponent(hl);

        positionsMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Positions));
        positionsMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        positionsMCB.addValueChangeListener(this);
        positionsMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        positionsMCB.setWidth(Settings.PERCENTS100);
        positionsMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        positionsMCB.setFilteringMode(FilteringMode.CONTAINS);
        positionsMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        positionsMCB.setShowSelectAllButton((filter, page) -> true);
        positionsMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(positionsMCB);

        extraPositionsMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.ExtraPositions));
        extraPositionsMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        extraPositionsMCB.addValueChangeListener(this);
        extraPositionsMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        extraPositionsMCB.setWidth(Settings.PERCENTS100);
        extraPositionsMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        extraPositionsMCB.setFilteringMode(FilteringMode.CONTAINS);
        extraPositionsMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        extraPositionsMCB.setShowSelectedOnTop(false);
        extraPositionsMCB.setShowSelectAllButton((filter, page) -> true);
        extraPositionsMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(extraPositionsMCB);

        workingStatusesMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.WorkingStatuses));
        workingStatusesMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        workingStatusesMCB.addValueChangeListener(this);
        workingStatusesMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        workingStatusesMCB.setWidth(Settings.PERCENTS100);
        workingStatusesMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        workingStatusesMCB.setFilteringMode(FilteringMode.CONTAINS);
        workingStatusesMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        workingStatusesMCB.setShowSelectAllButton((filter, page) -> true);
        workingStatusesMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(workingStatusesMCB);

        contractTypeMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.ContractTypes));
        contractTypeMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        contractTypeMCB.addValueChangeListener(this);
        contractTypeMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        contractTypeMCB.setWidth(Settings.PERCENTS100);
        contractTypeMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        contractTypeMCB.setFilteringMode(FilteringMode.CONTAINS);
        contractTypeMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        contractTypeMCB.setShowSelectAllButton((filter, page) -> true);
        contractTypeMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(contractTypeMCB);

        genderMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Genders));
        genderMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        genderMCB.addValueChangeListener(this);
        genderMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        genderMCB.setWidth(Settings.PERCENTS100);
        genderMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        genderMCB.setFilteringMode(FilteringMode.CONTAINS);
        genderMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        genderMCB.setShowSelectAllButton((filter, page) -> true);
        genderMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(genderMCB);

        nationalityMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Nationalities));
        nationalityMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        nationalityMCB.addValueChangeListener(this);
        nationalityMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        nationalityMCB.setWidth(Settings.PERCENTS100);
        nationalityMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        nationalityMCB.setFilteringMode(FilteringMode.CONTAINS);
        nationalityMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        nationalityMCB.setShowSelectAllButton((filter, page) -> true);
        nationalityMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(nationalityMCB);

        citizenshipMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Citizenships));
        citizenshipMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        citizenshipMCB.addValueChangeListener(this);
        citizenshipMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        citizenshipMCB.setWidth(Settings.PERCENTS100);
        citizenshipMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        citizenshipMCB.setFilteringMode(FilteringMode.CONTAINS);
        citizenshipMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        citizenshipMCB.setShowSelectAllButton((filter, page) -> true);
        citizenshipMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(citizenshipMCB);

        martialStatusMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.MartialStatuses));
        martialStatusMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        martialStatusMCB.addValueChangeListener(this);
        martialStatusMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        martialStatusMCB.setWidth(Settings.PERCENTS100);
        martialStatusMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        martialStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        martialStatusMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        martialStatusMCB.setShowSelectAllButton((filter, page) -> true);
        martialStatusMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(martialStatusMCB);

        healthStatusMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.HealthStatuses));
        healthStatusMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        healthStatusMCB.addValueChangeListener(this);
        healthStatusMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        healthStatusMCB.setWidth(Settings.PERCENTS100);
        healthStatusMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        healthStatusMCB.setFilteringMode(FilteringMode.CONTAINS);
        healthStatusMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        healthStatusMCB.setShowSelectAllButton((filter, page) -> true);
        healthStatusMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(healthStatusMCB);

        gradSchoolMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.GraduationSchools));
        gradSchoolMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        gradSchoolMCB.addValueChangeListener(this);
        gradSchoolMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        gradSchoolMCB.setWidth(Settings.PERCENTS100);
        gradSchoolMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        gradSchoolMCB.setFilteringMode(FilteringMode.CONTAINS);
        gradSchoolMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        gradSchoolMCB.setShowSelectAllButton((filter, page) -> true);
        gradSchoolMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(gradSchoolMCB);

        universityMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Education));
        universityMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        universityMCB.addValueChangeListener(this);
        universityMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        universityMCB.setWidth(Settings.PERCENTS100);
        universityMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        universityMCB.setFilteringMode(FilteringMode.CONTAINS);
        universityMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        universityMCB.setShowSelectAllButton((filter, page) -> true);
        universityMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(universityMCB);

        workPlaceMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.WorkPlaces));
        workPlaceMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        workPlaceMCB.addValueChangeListener(this);
        workPlaceMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        workPlaceMCB.setWidth(Settings.PERCENTS100);
        workPlaceMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        workPlaceMCB.setFilteringMode(FilteringMode.CONTAINS);
        workPlaceMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        workPlaceMCB.setShowSelectAllButton((filter, page) -> true);
        workPlaceMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(workPlaceMCB);

        languageMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Languages));
        languageMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        languageMCB.addValueChangeListener(this);
        languageMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        languageMCB.setWidth(Settings.PERCENTS100);
        languageMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        languageMCB.setFilteringMode(FilteringMode.CONTAINS);
        languageMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        languageMCB.setShowSelectAllButton((filter, page) -> true);
        languageMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(languageMCB);

        examMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Exams));
        examMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        examMCB.addValueChangeListener(this);
        examMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        examMCB.setWidth(Settings.PERCENTS100);
        examMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        examMCB.setFilteringMode(FilteringMode.CONTAINS);
        examMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        examMCB.setShowSelectAllButton((filter, page) -> true);
        examMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(examMCB);

        certificateMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.Certificates));
        certificateMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        certificateMCB.addValueChangeListener(this);
        certificateMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        certificateMCB.setWidth(Settings.PERCENTS100);
        certificateMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        certificateMCB.setFilteringMode(FilteringMode.CONTAINS);
        certificateMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        certificateMCB.setShowSelectAllButton((filter, page) -> true);
        certificateMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(certificateMCB);

        mainBranchMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.MainBranches));
        mainBranchMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        mainBranchMCB.addValueChangeListener(this);
        mainBranchMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        mainBranchMCB.setWidth(Settings.PERCENTS100);
        mainBranchMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        mainBranchMCB.setFilteringMode(FilteringMode.CONTAINS);
        mainBranchMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        mainBranchMCB.setShowSelectAllButton((filter, page) -> true);
        mainBranchMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(mainBranchMCB);

        ComboBoxMultiselect extraBranchMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.ExtraBranches));
        extraBranchMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        extraBranchMCB.addValueChangeListener(this);
        extraBranchMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        extraBranchMCB.setWidth(Settings.PERCENTS100);
        extraBranchMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        extraBranchMCB.setFilteringMode(FilteringMode.CONTAINS);
        extraBranchMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        extraBranchMCB.setShowSelectAllButton((filter, page) -> true);
        extraBranchMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(extraBranchMCB);

        yearSelect = new ComboBox(myUI.getMessage(IndigoMessages.LessonsYear));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.addValueChangeListener(this);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        yearSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);
        leftLay.addComponent(yearSelect);

        canBeAdvisorMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.CanBeAdvisors));
        canBeAdvisorMCB.setInputPrompt(myUI.getMessage(IndigoMessages.All));
        canBeAdvisorMCB.addValueChangeListener(this);
        canBeAdvisorMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        canBeAdvisorMCB.setWidth(Settings.PERCENTS100);
        canBeAdvisorMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        canBeAdvisorMCB.setFilteringMode(FilteringMode.CONTAINS);
        canBeAdvisorMCB.setClearButtonCaption(myUI.getMessage(IndigoMessages.Clear));
        canBeAdvisorMCB.setShowSelectAllButton((filter, page) -> true);
        canBeAdvisorMCB.setSelectAllButtonCaption(myUI.getMessage(IndigoMessages.SelectAll));
        leftLay.addComponent(canBeAdvisorMCB);

        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            workingStatusesMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbWorking_status, false));
            genderMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbGender, false));
            nationalityMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbNationality, false));
            citizenshipMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbCountry, false));
            martialStatusMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbMartialStatus, false));
            contractTypeMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbSalaryCategory, false));
            healthStatusMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbHealthStatus, false));
            examMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbExamTable, false));
            mainBranchMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbBranchTable, false));
            extraBranchMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbBranchTable, false));
            universityMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbUniversityTable, false));
            workPlaceMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbWork_placeTable, false));
            certificateMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbCertificateTable, false));
            languageMCB.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbLanguageTable, false));
            canBeAdvisorMCB.setContainerDataSource(dbd.execSQL_yes_no(myUI));
            positionsMCB.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr)));
            extraPositionsMCB.setContainerDataSource(dbd.exec_positions_for_select(myUI,
                    currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr)));
            dbd.close();
            DbSchool dbCon = new DbSchool();
            dbCon.connect();
            schoolsMCB.setContainerDataSource(dbCon.execSchoolSel(myUI, "1,3,4,5,6"));
            gradSchoolMCB.setContainerDataSource(dbCon.execSchoolSel(myUI, "1,3,4"));
            Item item = gradSchoolMCB.getContainerDataSource().addItem(-1);
            item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(myUI.getMessage(IndigoMessages.OtherSchool));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());

        p.setContent(leftLay);
        ((GridLayout) splitPanel.getFirstComponent()).addComponent(p, 0, 1);
        ((GridLayout) splitPanel.getFirstComponent()).setRowExpandRatio(1, 1);
    }

    private void buildRightLayout() {
        VerticalLayout vl = new VerticalLayout();
        vl.setMargin(true);
        vl.setSizeFull();
        dataGrid = new Grid();
        dataGrid.setFooterVisible(true);
        dataGrid.setSizeFull();
        dataGrid.setSelectionMode(Grid.SelectionMode.NONE);
        dataGrid.setStyleName(ValoTheme.TABLE_COMPACT);
        dataGrid.addStyleName("noWrapHeader");
        dataGrid.addStyleName("noWrap");
        vl.addComponent(dataGrid);
        splitPanel.setSecondComponent(vl);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == generateBtn) {
            if (yearSelect.isValid()) {
                try {
                    Map<String, String> params = new HashMap<>();
                    insertParameter(params, myUI.getMessage(IndigoMessages.Schools), schoolsMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Positions), positionsMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.ExtraPositions), extraPositionsMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.WorkingStatuses), workingStatusesMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.ContractTypes), contractTypeMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Genders), genderMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Nationalities), nationalityMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Citizenships), citizenshipMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.MartialStatuses), martialStatusMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.HealthStatuses), healthStatusMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.GraduationSchools), gradSchoolMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Education), universityMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.WorkPlaces), workPlaceMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.CanBeAdvisors), canBeAdvisorMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Exams), examMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Certificates), certificateMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.Languages), languageMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.MainBranches), mainBranchMCB);
                    insertParameter(params, myUI.getMessage(IndigoMessages.ExtraPositions), extraPositionsMCB);
                    params.put(myUI.getMessage(IndigoMessages.FirstName), nameTF.getValue());
                    params.put(myUI.getMessage(IndigoMessages.LastName), surnameTF.getValue());
                    params.put(myUI.getMessage(IndigoMessages.FromAge), fromAge.getValue());
                    params.put(myUI.getMessage(IndigoMessages.ToAge), toAge.getValue());

                    DbEmployee dbEmployee = new DbEmployee();
                    dbEmployee.connect();
                    IndexedContainer container = dbEmployee.execSQL(myUI, (Integer) yearSelect.getValue(), params);
                    GeneratedPropertyContainer gpc = new GeneratedPropertyContainer(container);
                    gpc.addGeneratedProperty(Settings.button, new PropertyValueGenerator<Component>() {
                        @Override
                        public Component getValue(Item item, Object itemId, Object propertyId) {
                            Button button = new Button(Settings.cv);
                            button.setStyleName(ValoTheme.BUTTON_LINK);
                            button.addStyleName(ValoTheme.BUTTON_TINY);
                            button.addStyleName("cv");
                            button.setId(itemId + "");
                            button.addClickListener(HRGeneralReport.this);
                            return button;
                        }

                        @Override
                        public Class<Component> getType() {
                            return Component.class;
                        }
                    });
                    dataGrid.setContainerDataSource(gpc);
                    dataGrid.getColumn(Settings.button).setRenderer(new ComponentRenderer());
                    dbEmployee.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                dataGrid.setCellStyleGenerator((Grid.CellReference cellReference) -> {
                    if (cellReference.getProperty().getType() == Double.class
                            || cellReference.getProperty().getType() == Integer.class) {
                        return "align-right";
                    } else {
                        return null;
                    }
                });
                if (footer == null) {
                    footer = dataGrid.appendFooterRow();
                }
                footer.getCell(myUI.getMessage(IndigoMessages.School)).setText(myUI.getMessage(IndigoMessages.Total));
                footer.getCell(myUI.getMessage(IndigoMessages.LastName)).setText(dataGrid.getContainerDataSource().size() + "");
                dataGrid.setFrozenColumnCount(5);
                dataGrid.setSizeFull();
                dataGrid.getColumn(myUI.getMessage(IndigoMessages.Id)).setHidden(true);
                dataGrid.getColumn(myUI.getMessage(IndigoMessages.Photo)).setHidden(true);
                if (dataGrid.getContainerDataSource().size() != 0) {
                    excelBtn.setEnabled(true);
                }
            } else {
                Notification.show(myUI.getMessage(IndigoMessages.RequiredField),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == excelBtn) {
            try {
                Table t = new Table();
                t.setContainerDataSource(dataGrid.getContainerDataSource());

                Window w = new Window();
                w.setModal(true);
                myUI.addWindow(w);
                w.setContent(t);

                EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(t, myUI.getMessage(IndigoMessages.HRGeneralReport));
                excelReport.setReportTitle(myUI.getMessage(IndigoMessages.HRGeneralReport));
                excelReport.setDisplayTotals(true);
                excelReport.convertTable();
                excelReport.sendConverted();
                w.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else {
            Employee employee = new Employee();
            int emp_id = Integer.parseInt(event.getButton().getId());
            Item item = dataGrid.getContainerDataSource().getItem(emp_id);
            employee.setId(emp_id);
            employee.setLogin(item.getItemProperty(myUI.getMessage(IndigoMessages.Id)).getValue().toString());
            employee.setName(item.getItemProperty(myUI.getMessage(IndigoMessages.FirstName)).getValue().toString());
            employee.setSurname(item.getItemProperty(myUI.getMessage(IndigoMessages.LastName)).getValue().toString());
            if (item.getItemProperty(myUI.getMessage(IndigoMessages.MiddleName)).getValue() != null) {
                employee.setMiddle_name(item.getItemProperty(myUI.getMessage(IndigoMessages.MiddleName)).getValue().toString());
            }
            if (item.getItemProperty(myUI.getMessage(IndigoMessages.Photo)).getValue() != null) {
                employee.setPhoto(item.getItemProperty(myUI.getMessage(IndigoMessages.Photo)).getValue().toString());
            }
            try {
                employee.setBirth_date(Settings.df.parse(item.getItemProperty(myUI.getMessage(IndigoMessages.DateOfBirth)).getValue().toString()));
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }

            EmployeeExtraInfo employeeExtraInfo = null;
            try {
                DbEmployeeExtraInfo dbCon = new DbEmployeeExtraInfo();
                dbCon.connect();
                employeeExtraInfo = dbCon.execSQL_for_cv(emp_id);
                dbCon.close();
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
            try {
                DbEmployeeWork dbCon = new DbEmployeeWork();
                dbCon.connect();
                if (employeeExtraInfo != null) {
                    employeeExtraInfo.setWorkExperience(dbCon.execSQL_work_experience(emp_id, 1, false));
                    employeeExtraInfo.setWorkExperienceSapat(dbCon.execSQL_work_experience(emp_id, 1, true));
                }
                dbCon.close();
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }

            if (employeeExtraInfo != null) {
                employeeExtraInfo.setMainPosition(item.getItemProperty(myUI.getMessage(IndigoMessages.MainPosition)).getValue().toString());
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraPositions)).getValue() != null) {
                    employeeExtraInfo.setExtraPositions(item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraPositions)).getValue().toString());
                }
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.MainBranch)).getValue() != null) {
                    employeeExtraInfo.setMainBranch(item.getItemProperty(myUI.getMessage(IndigoMessages.MainBranch)).getValue().toString());
                }
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraBranches)).getValue() != null) {
                    employeeExtraInfo.setExtraBranches(item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraBranches)).getValue().toString());
                }
            }

            if (employeeExtraInfo != null) {
                employeeExtraInfo.setSchool(item.getItemProperty(myUI.getMessage(IndigoMessages.School)).getValue().toString());
                employeeExtraInfo.setWorkingStatus(item.getItemProperty(myUI.getMessage(IndigoMessages.WorkingStatus)).getValue().toString());
                employeeExtraInfo.setHours((Integer) item.getItemProperty(myUI.getMessage(IndigoMessages.Hours)).getValue());
                employeeExtraInfo.setExtraHours((Integer) item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraHours)).getValue());
                employeeExtraInfo.setCanBeAdvisor(item.getItemProperty(myUI.getMessage(IndigoMessages.CanBeAdvisor)).getValue().toString());
            }
            myUI.addWindow(new EmployeeCvWindow(myUI, employee, employeeExtraInfo, yearSelect.getItemCaption(yearSelect.getValue())));
        }
    }

    private void insertParameter(Map<String, String> params, String key, ComboBoxMultiselect cb) {
        if (cb.getContainerDataSource().size() != ((Set<?>) cb.getValue()).size()) {
            params.put(key, Settings.convertCollectionToStr((Set<?>) cb.getValue()));
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
