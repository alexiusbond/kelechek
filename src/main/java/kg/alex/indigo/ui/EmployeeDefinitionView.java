/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.*;
import kg.alex.indigo.domain.*;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.pdf.contracts.*;
import kg.alex.indigo.utils.FormattedTable;
import kg.alex.indigo.utils.GenerateRandomString;
import kg.alex.indigo.utils.MyFilterDecorator;
import kg.alex.indigo.utils.MyFilterGenerator;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.hene.popupbutton.PopupButton;
import org.vaadin.simplefiledownloader.SimpleFileDownloader;

import java.io.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

/**
 * @author alex
 */
public class EmployeeDefinitionView extends HorizontalSplitPanel
        implements Button.ClickListener, Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(EmployeeDefinitionView.class);
    private final MyVaadinUI myUI;
    private final FilterTable employeesDataTable;
    private final OptionGroup optionGroup;
    private final TabSheet tabs;
    private final Label workingStatTtlLb;
    private final String[] NATURAL_COL_ORDER;
    private final GridLayout empSearchLay;
    private final VerticalLayout contractExtraInfoLay = new VerticalLayout();
    private final Subject currentUser = SecurityUtils.getSubject();
    private final ArrayList<String> delPhoneIds = new ArrayList<>();
    private final ArrayList<String> delContractIds = new ArrayList<>();
    private final ArrayList<String> delChildIds = new ArrayList<>();
    private final ArrayList<EmployeeEducation> delSpouseEducationIds = new ArrayList<>();
    private final ArrayList<String> delSpouseWorkIds = new ArrayList<>();
    private final ArrayList<String> delLanguagesIds = new ArrayList<>();
    private final ArrayList<String> delSeminarsIds = new ArrayList<>();
    private final ArrayList<EmployeeCertificate> delCertificatesIds = new ArrayList<>();
    private final ArrayList<EmployeeExam> delExamsIds = new ArrayList<>();
    private final ArrayList<EmployeeEducation> delEducationIds = new ArrayList<>();
    private final ArrayList<String> delWorkPlacesIds = new ArrayList<>();
    private final ArrayList<String> delBranchesIds = new ArrayList<>();
    private final ArrayList<String> delLessonIds = new ArrayList<>();
    private final ArrayList<String> delSupervisionIds = new ArrayList<>();
    private final ArrayList<EmployeeOrder> delOrderIds = new ArrayList<>();
    private final boolean isMyProfile;
    private final Date today = new Date();
    private Button createBtn, modifyBtn, deleteBtn, saveBtn, cancelBtn, generateBtn, cvBtn;
    private Table documentsDataTable;
    private int employeeID;
    private CheckBox canBeAdvisorCkb, noPhonesCkb, noBranchesCkb, noEducationCkb, noWorkPlacesCkb, noExamsCkb,
            noSeminarsCkb, noCertificatesCkb, noLanguagesCkb, noSpouseEducationCkb, noSpouseWorkPlacesCkb, noChildrenCkb;
    private TextField nameTF, loginTF, passwordTF, surnameTF, middleNameTF,
            birthPlaceTF, emailTF, spouseFullNameTF, spousePhoneTF,
            spouseHealthNotesTF, hobbiesTF, fobbiesTF, passportTF, innTF, passportGivenTf;
    private TextArea addressTA, healthNotesTA, shortNotesTA;
    private DateField birthDateDF, gradSchoolStartDF, gradSchoolEndDF, passportDateDF;
    private ComboBox genderCB, nationalityCB, martialStatusCB, mainPositionCB, citizenshipCB,
            spouseHealthCB, healthCB, contractCategoryCB, gradSchoolCB;
    private FormLayout formLay;
    private FormLayout fieldsLayFamily;
    private boolean isNew;
    private Label workingStatusLb;
    private Label mainPositionLb;
    private Label extraPositionsLb;
    private Label mainBranchLb;
    private Label extraBranchesLb;
    private Label totalHoursLb;
    private Label captionSpouseInfo;
    private String[] NATURAL_COL_ORDER_PHONES;
    private String[] NATURAL_COL_ORDER_CONTRACTS;
    private String[] NATURAL_COL_ORDER_CHILDREN;
    private String[] NATURAL_COL_ORDER_EDU;
    private String[] NATURAL_COL_ORDER_WORK;
    private String[] NATURAL_COL_ORDER_EXAMS;
    private String[] NATURAL_COL_ORDER_SEMINARS;
    private String[] NATURAL_COL_ORDER_LANGUAGES;
    private String[] NATURAL_COL_ORDER_CERTIFICATES;
    private String[] NATURAL_COL_ORDER_BRANCHES;
    private String[] NATURAL_COL_ORDER_LESSONS;
    private String[] NATURAL_COL_ORDER_SUPERVISION;
    private String[] NATURAL_COL_ORDER_ORDERS;
    private VerticalLayout infoLay, documentsLay;
    private GridLayout contactInfoLay;
    private VerticalLayout contractInfoLay;
    private Button printContractBtn;
    private GridLayout familyInfoLay;
    private GridLayout extraInfoLay;
    private GridLayout achievementsInfoLay;
    private GridLayout profInfoLay;
    private GridLayout ordersInfoLay;
    private VerticalLayout schoolInfoLay, permissionsLay, leftLay;
    private HorizontalLayout buttonsLay;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private ProgressBar uploadProgressBar;
    private String photoName, fileName, mimeType;
    private Embedded photoEmb;
    private IndexedContainer workingStatCont;
    private FormattedTable phonesTable, contractsTable, childrenTable, spouseEducationTable, spouseWorkPlacesTable, questioningTable,
            examsTable, seminarsTable, certificatesTable, languagesTable, educationTable, workPlacesTable, branchesTable, lessonsTable,
            supervisionTable, permissionTable, ordersTable;
    private Button plusPhonesButton, plusContractButton, plusChildButton, plusSpouseEducationButton, plusSpouseWorkPlacesButton,
            plusExamButton, plusSeminarButton, plusCertificateButton, plusLanguageButton,
            plusEducationButton, plusWorkPlaceButton, plusBranchButton, plusLessonsButton, plusSupervisionButton, plusOrdersButton;
    private int r_table_counter = 1000;
    private IndexedContainer phonesCont, contractsCont, childrenCont, spouseEducationCont, spouseWorkCont,
            educationCont, workPlacesCont, examsCont, languagesCont, seminarsCont,
            certificatesCont, branchesCont, lessonsCont, supervisionCont, permissionCont, ordersCont;
    private SimpleFileDownloader downloader = null;

    public EmployeeDefinitionView(final MyVaadinUI myUI, boolean isMyProfile) {
        this.myUI = myUI;
        this.isMyProfile = isMyProfile;

        if (isMyProfile) {
            employeeID = myUI.getUser().getId();
        }
        buildButtonsLayout();
        buildLeftLayout();
        try {
            DbDefinition dbed = new DbDefinition();
            dbed.connect();
            workingStatCont = dbed.execSQL_statuses_with_count(myUI, Settings.dbWorking_status, false);
            dbed.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(IndigoMessages.Id), myUI.getMessage(IndigoMessages.LastName),
                myUI.getMessage(IndigoMessages.FirstName),
                myUI.getMessage(IndigoMessages.WorkingStatus),
                myUI.getMessage(IndigoMessages.MainPosition),
                myUI.getMessage(IndigoMessages.MainBranch)};

        Label eduStatusLab = new Label();
        eduStatusLab.setSizeUndefined();
        eduStatusLab.setContentMode(ContentMode.HTML);
        eduStatusLab.setValue(myUI.getMessage(IndigoMessages.ShowByWorkingStatuses) + ": ");

        optionGroup = new OptionGroup();
        optionGroup.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        optionGroup.setMultiSelect(true);
        optionGroup.setContainerDataSource(workingStatCont);
        optionGroup.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        optionGroup.select(2);
        optionGroup.addValueChangeListener(this);

        employeesDataTable = new FilterTable();
        employeesDataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        employeesDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        employeesDataTable.setSizeFull();
        employeesDataTable.setNullSelectionAllowed(false);
        employeesDataTable.setFilterBarVisible(true);
        employeesDataTable.setSelectable(true);
        employeesDataTable.addValueChangeListener(this);

        setEmployeesDataTable(optionGroup.getValue().toString());

        HorizontalLayout searchLayFooter = new HorizontalLayout();
        searchLayFooter.setWidth(Settings.PERCENTS100);
        workingStatTtlLb = new Label();
        workingStatTtlLb.setSizeUndefined();
        workingStatTtlLb.setImmediate(true);
        workingStatTtlLb.setContentMode(ContentMode.HTML);
        Label filteredLab = new Label();
        filteredLab.setSizeUndefined();
        filteredLab.setImmediate(true);
        filteredLab.setContentMode(ContentMode.HTML);
        filteredLab.setValue(myUI.getMessage(IndigoMessages.Filtered) + ": 0");
        repaint();

        employeesDataTable.setFilterGenerator(new MyFilterGenerator(
                filteredLab, myUI.getMessage(IndigoMessages.Filtered), employeesDataTable));

        empSearchLay = new GridLayout(2, 3);
        empSearchLay.setSizeFull();
        empSearchLay.setMargin(true);
        searchLayFooter.addComponent(filteredLab);
        searchLayFooter.addComponent(workingStatTtlLb);
        searchLayFooter.setComponentAlignment(filteredLab, Alignment.BOTTOM_LEFT);
        searchLayFooter.setComponentAlignment(workingStatTtlLb, Alignment.BOTTOM_RIGHT);
        searchLayFooter.setExpandRatio(filteredLab, 1);
        searchLayFooter.setExpandRatio(workingStatTtlLb, 2);

        empSearchLay.addComponent(eduStatusLab, 0, 0);
        empSearchLay.addComponent(optionGroup, 1, 0);
        empSearchLay.addComponent(employeesDataTable, 0, 1, 1, 1);
        empSearchLay.addComponent(searchLayFooter, 0, 2, 1, 2);
        empSearchLay.setRowExpandRatio(1, 1);
        empSearchLay.setColumnExpandRatio(0, 1);
        empSearchLay.setComponentAlignment(optionGroup, Alignment.MIDDLE_RIGHT);
        empSearchLay.setComponentAlignment(eduStatusLab, Alignment.MIDDLE_RIGHT);

        this.setSplitPosition(25, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(leftLay);

        buildContactsLayout();
        buildContractsLayout();
        buildFamilyLayout();
        buildExtraLayout();
        buildAchievementsLayout();
        buildProfLayout();
        buildSchoolInfoLayout();
        buildPermissionsLayout();
        buildOrdersLayout();
        buildDocumentsLayout();

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabs.addTab(empSearchLay).setCaption(myUI.getMessage(IndigoMessages.Search));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabSearch) || isMyProfile) {
            tabs.getTab(empSearchLay).setVisible(false);
        }
        tabs.addTab(contactInfoLay).setCaption(myUI.getMessage(IndigoMessages.ContactInfo));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabContacts) && !isMyProfile) {
            tabs.getTab(contactInfoLay).setVisible(false);
        }
        tabs.addTab(contractInfoLay).setCaption(myUI.getMessage(IndigoMessages.Contracts));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabContracts)) {
            tabs.getTab(contractInfoLay).setVisible(false);
        }
        tabs.addTab(profInfoLay).setCaption(myUI.getMessage(IndigoMessages.ProfInfo));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabProfInfo) && !isMyProfile) {
            tabs.getTab(profInfoLay).setVisible(false);
        }
        tabs.addTab(achievementsInfoLay).setCaption(myUI.getMessage(IndigoMessages.Achievements));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabAchievements) && !isMyProfile) {
            tabs.getTab(achievementsInfoLay).setVisible(false);
        }
        tabs.addTab(familyInfoLay).setCaption(myUI.getMessage(IndigoMessages.FamilyInfo));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabFamilyInfo) && !isMyProfile) {
            tabs.getTab(familyInfoLay).setVisible(false);
        }
        tabs.addTab(extraInfoLay).setCaption(myUI.getMessage(IndigoMessages.ExtraInfo));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabExtraInfo) && !isMyProfile) {
            tabs.getTab(extraInfoLay).setVisible(false);
        }
        tabs.addTab(schoolInfoLay).setCaption(myUI.getMessage(IndigoMessages.EduActivitiesInfo));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabEduActivities) ||
                (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeSupervision) &&
                        !currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeLessons))) {
            tabs.getTab(schoolInfoLay).setVisible(false);
        }
        tabs.addTab(permissionsLay).setCaption(myUI.getMessage(IndigoMessages.Permissions));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabPermissions)) {
            tabs.getTab(permissionsLay).setVisible(false);
        }
        tabs.addTab(ordersInfoLay).setCaption(myUI.getMessage(IndigoMessages.OrdersHistory));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabOrders)) {
            tabs.getTab(ordersInfoLay).setVisible(false);
        }
        tabs.addTab(documentsLay).setCaption(myUI.getMessage(IndigoMessages.Documents));

        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmTabDocuments)) {
            tabs.getTab(documentsLay).setVisible(false);
        }
        tabs.addSelectedTabChangeListener(
                (TabSheet.SelectedTabChangeListener) event -> {
                    prepareNormalMode();
                    if (event.getTabSheet().getSelectedTab() == contactInfoLay && employeeID != 0) {
                        setPhonesTable();
                        setContactFields();
                    } else if (event.getTabSheet().getSelectedTab() == contractInfoLay && employeeID != 0) {
                        setContractsTable();
                    } else if (event.getTabSheet().getSelectedTab() == familyInfoLay && employeeID != 0) {
                        setChildrenTable();
                        setEducationTable(spouseEducationTable, 2);
                        setWorkTable(spouseWorkPlacesTable, 2);
                        setSpouseFields();
                    } else if (event.getTabSheet().getSelectedTab() == extraInfoLay && employeeID != 0) {
                        setQuestioningTable();
                        setExtraInfoFields();
                    } else if (event.getTabSheet().getSelectedTab() == achievementsInfoLay && employeeID != 0) {
                        setLanguagesTable();
                        setCertificatesTable();
                        setSeminarsTable();
                        setExamsTable();
                    } else if (event.getTabSheet().getSelectedTab() == profInfoLay && employeeID != 0) {
                        setEducationTable(educationTable, 1);
                        setWorkTable(workPlacesTable, 1);
                        setBranchesTable();
                        setGradSchoolFields();
                    } else if (event.getTabSheet().getSelectedTab() == schoolInfoLay && employeeID != 0) {
                        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeSupervision)) {
                            canBeAdvisorCkb.setValue((Boolean) employeesDataTable.getContainerProperty(
                                    employeeID, myUI.getMessage(IndigoMessages.CanBeAdvisor)).getValue());
                            setSupervisionTable();
                        }
                        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeLessons)) {
                            setLessonsTable();
                        }
                    } else if (event.getTabSheet().getSelectedTab() == permissionsLay && employeeID != 0) {
                        if (employeesDataTable.getContainerDataSource().
                                getContainerProperty(employeeID, myUI.getMessage(
                                        IndigoMessages.Permissions)).getValue() != null) {
                            setPermTable_options(employeesDataTable.getContainerDataSource().
                                    getContainerProperty(employeeID, myUI.getMessage(
                                            IndigoMessages.Permissions)).getValue().toString());
                        } else {
                            clearPermissionsTable();
                        }
                    } else if (event.getTabSheet().getSelectedTab() == ordersInfoLay && employeeID != 0) {
                        setOrdersTable();
                    } else if (event.getTabSheet().getSelectedTab() == documentsLay && employeeID != 0) {
                        setDocumentsTable();
                    }
                });
        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(new MarginInfo(true, false, false, false));
        vl.addComponent(tabs);
        this.setSecondComponent(vl);
        prepareNormalMode();
        updateInfoLayout();
        employeesDataTable.setValue(employeeID);
    }

    private void buildContractsLayout() {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);

        Label caption = new Label();
        caption.setSizeFull();
        caption.setContentMode(ContentMode.HTML);
        caption.setValue(myUI.getMessage(IndigoMessages.Contracts));
        caption.setStyleName("tableCpt");

        plusContractButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusContractButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusContractButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusContractButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusContractButton.addClickListener(this);

        hl.addComponent(caption);
        hl.setSpacing(true);
        hl.addComponent(plusContractButton);
        hl.setExpandRatio(caption, 1);

        contractsTable = new FormattedTable(myUI);
        contractsTable.setSizeFull();
        contractsTable.setStyleName(ValoTheme.TABLE_SMALL);
        contractInfoLay = new VerticalLayout();
        contractInfoLay.setSizeFull();
        contractInfoLay.setSpacing(true);
        contractInfoLay.setMargin(true);
        contractInfoLay.addComponent(hl);
        contractInfoLay.addComponent(contractsTable);
        contractInfoLay.setExpandRatio(contractsTable, 1);
    }

    private void buildContactsLayout() {

        FormLayout fieldsLayContacts = new FormLayout();
        fieldsLayContacts.setWidth(Settings.PERCENTS100);
        fieldsLayContacts.setSpacing(false);
        fieldsLayContacts.setMargin(false);

        birthPlaceTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 250, false), true);
        birthPlaceTF.setCaption(myUI.getMessage(IndigoMessages.BirthPlace));
        fieldsLayContacts.addComponent(birthPlaceTF);

        addressTA = new TextArea(myUI.getMessage(IndigoMessages.Address));
        addressTA.setRequired(true);
        addressTA.setStyleName(ValoTheme.TEXTFIELD_TINY);
        addressTA.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        addressTA.setWidth(Settings.PERCENTS100);
        addressTA.setRows(7);
        addressTA.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 400, false));
        fieldsLayContacts.addComponent(addressTA);

        emailTF = createTextField(null, null, new EmailValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue)), false);
        emailTF.setNullRepresentation("");
        emailTF.setCaption(Settings.email);
        fieldsLayContacts.addComponent(emailTF);

        passportTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 20, true), false);
        passportTF.setNullRepresentation("");
        passportTF.setCaption(myUI.getMessage(IndigoMessages.Passport));
        fieldsLayContacts.addComponent(passportTF);

        passportGivenTf = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 45, true), false);
        passportGivenTf.setNullRepresentation("");
        passportGivenTf.setCaption(myUI.getMessage(IndigoMessages.PassportGiven));
        fieldsLayContacts.addComponent(passportGivenTf);

        passportDateDF = createDateField(null, null, myUI.getMessage(IndigoMessages.PassportDate),
                false, Settings.datePattern, Resolution.DAY);
        passportDateDF.setRangeEnd(today);
        fieldsLayContacts.addComponent(passportDateDF);

        innTF = createTextField(null, null, new RegexpValidator("[0-9]*",
                myUI.getMessage(IndigoMessages.NotificationWrongValue)), false);
        innTF.setNullRepresentation("");
        innTF.setCaption(myUI.getMessage(IndigoMessages.INN));
        fieldsLayContacts.addComponent(innTF);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);

        Label captionPhones = new Label();
        captionPhones.setSizeFull();
        captionPhones.setContentMode(ContentMode.HTML);
        captionPhones.setValue(myUI.getMessage(IndigoMessages.PhoneNumbers));
        captionPhones.setStyleName("tableCpt");

        Label captionContactsInfo = new Label();
        captionContactsInfo.setSizeFull();
        captionContactsInfo.setContentMode(ContentMode.HTML);
        captionContactsInfo.setValue(myUI.getMessage(IndigoMessages.ContactInfo));
        captionContactsInfo.setStyleName("tableCpt");

        noPhonesCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noPhonesCkb.addValueChangeListener(this);

        plusPhonesButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusPhonesButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusPhonesButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusPhonesButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusPhonesButton.addClickListener(this);

        hl.addComponent(captionPhones);
        hl.setSpacing(true);
        hl.addComponent(plusPhonesButton);
        hl.addComponent(noPhonesCkb);
        hl.setComponentAlignment(noPhonesCkb, Alignment.BOTTOM_RIGHT);
        hl.setExpandRatio(captionPhones, 1);

        phonesTable = new FormattedTable(myUI);
        phonesTable.setSizeFull();
        phonesTable.setStyleName(ValoTheme.TABLE_SMALL);
        contactInfoLay = new GridLayout(2, 2);
        contactInfoLay.setSizeFull();
        contactInfoLay.setSpacing(true);
        contactInfoLay.setMargin(true);
        contactInfoLay.addComponent(captionContactsInfo, 0, 0);
        contactInfoLay.addComponent(hl, 1, 0);
        contactInfoLay.addComponent(fieldsLayContacts, 0, 1);
        contactInfoLay.addComponent(phonesTable, 1, 1);
        contactInfoLay.setRowExpandRatio(1, 1);
    }

    private void buildFamilyLayout() {

        familyInfoLay = new GridLayout(3, 8);
        familyInfoLay.setWidth(Settings.PERCENTS100);
        familyInfoLay.setSpacing(true);
        familyInfoLay.setMargin(true);
        familyInfoLay.setColumnExpandRatio(0, 1);

        captionSpouseInfo = new Label();
        captionSpouseInfo.setWidth(Settings.PERCENTS100);
        captionSpouseInfo.setContentMode(ContentMode.HTML);
        captionSpouseInfo.setValue(myUI.getMessage(IndigoMessages.SpouseInfo));
        captionSpouseInfo.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionSpouseInfo, 0, 0, 2, 0);

        fieldsLayFamily = new FormLayout();
        fieldsLayFamily.setWidth(Settings.PERCENTS100);
        fieldsLayFamily.setSpacing(false);
        fieldsLayFamily.setMargin(false);
        familyInfoLay.addComponent(fieldsLayFamily, 0, 1, 2, 1);

        spouseFullNameTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 200, true), true);
        spouseFullNameTF.setCaption(myUI.getMessage(IndigoMessages.FullName));
        spouseFullNameTF.setWidth("50%");
        fieldsLayFamily.addComponent(spouseFullNameTF);

        spousePhoneTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 100, true), false);
        spousePhoneTF.setCaption(myUI.getMessage(IndigoMessages.Phone));
        spousePhoneTF.setWidth("50%");
        fieldsLayFamily.addComponent(spousePhoneTF);

        spouseHealthCB = createCombobox(0, null, Settings.dbHealthStatus, true);
        spouseHealthCB.setCaption(myUI.getMessage(IndigoMessages.HealthStatus));
        spouseHealthCB.setWidth("50%");
        fieldsLayFamily.addComponent(spouseHealthCB);

        spouseHealthNotesTF = new TextField(myUI.getMessage(IndigoMessages.HealthNotes));
        spouseHealthNotesTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        spouseHealthNotesTF.setWidth("50%");
        spouseHealthNotesTF.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 350, true));
        fieldsLayFamily.addComponent(spouseHealthNotesTF);

        Label captionSpouseEducation = new Label();
        captionSpouseEducation.setSizeFull();
        captionSpouseEducation.setContentMode(ContentMode.HTML);
        captionSpouseEducation.setValue(myUI.getMessage(IndigoMessages.SpouseEducation));
        captionSpouseEducation.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionSpouseEducation, 0, 2);

        noSpouseEducationCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noSpouseEducationCkb.addValueChangeListener(this);
        familyInfoLay.addComponent(noSpouseEducationCkb, 2, 2);
        familyInfoLay.setComponentAlignment(noSpouseEducationCkb, Alignment.BOTTOM_RIGHT);

        plusSpouseEducationButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusSpouseEducationButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusSpouseEducationButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusSpouseEducationButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusSpouseEducationButton.addClickListener(this);
        familyInfoLay.addComponent(plusSpouseEducationButton, 1, 2);

        spouseEducationTable = new FormattedTable(myUI);
        spouseEducationTable.setSizeFull();
        spouseEducationTable.setStyleName(ValoTheme.TABLE_SMALL);
        familyInfoLay.addComponent(spouseEducationTable, 0, 3, 2, 3);

        Label captionSpouseWorkPlaces = new Label();
        captionSpouseWorkPlaces.setSizeFull();
        captionSpouseWorkPlaces.setContentMode(ContentMode.HTML);
        captionSpouseWorkPlaces.setValue(myUI.getMessage(IndigoMessages.SpouseWorkPlaces));
        captionSpouseWorkPlaces.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionSpouseWorkPlaces, 0, 4);

        noSpouseWorkPlacesCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noSpouseWorkPlacesCkb.addValueChangeListener(this);
        familyInfoLay.addComponent(noSpouseWorkPlacesCkb, 2, 4);
        familyInfoLay.setComponentAlignment(noSpouseWorkPlacesCkb, Alignment.BOTTOM_RIGHT);

        plusSpouseWorkPlacesButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusSpouseWorkPlacesButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusSpouseWorkPlacesButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusSpouseWorkPlacesButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusSpouseWorkPlacesButton.addClickListener(this);
        familyInfoLay.addComponent(plusSpouseWorkPlacesButton, 1, 4);

        spouseWorkPlacesTable = new FormattedTable(myUI);
        spouseWorkPlacesTable.setSizeFull();
        spouseWorkPlacesTable.setStyleName(ValoTheme.TABLE_SMALL);
        familyInfoLay.addComponent(spouseWorkPlacesTable, 0, 5, 2, 5);


        Label captionChildren = new Label();
        captionChildren.setSizeFull();
        captionChildren.setContentMode(ContentMode.HTML);
        captionChildren.setValue(myUI.getMessage(IndigoMessages.Children));
        captionChildren.setStyleName("tableCpt");
        familyInfoLay.addComponent(captionChildren, 0, 6);

        noChildrenCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noChildrenCkb.addValueChangeListener(this);
        familyInfoLay.addComponent(noChildrenCkb, 2, 6);
        familyInfoLay.setComponentAlignment(noChildrenCkb, Alignment.BOTTOM_RIGHT);

        plusChildButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusChildButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusChildButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusChildButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusChildButton.addClickListener(this);
        familyInfoLay.addComponent(plusChildButton, 1, 6);

        childrenTable = new FormattedTable(myUI);
        childrenTable.setSizeFull();
        childrenTable.setStyleName(ValoTheme.TABLE_SMALL);
        familyInfoLay.addComponent(childrenTable, 0, 7, 2, 7);
    }

    private void buildDocumentsLayout() {
        Label captionDocuments = new Label();
        captionDocuments.setWidth(Settings.PERCENTS100);
        captionDocuments.setContentMode(ContentMode.HTML);
        captionDocuments.setValue(myUI.getMessage(IndigoMessages.ListOfDocuments));
        captionDocuments.setStyleName("tableCpt");

        documentsDataTable = new Table();
        documentsDataTable.setSizeFull();
        documentsDataTable.setStyleName(ValoTheme.TABLE_SMALL);

        documentsLay = new VerticalLayout();
        documentsLay.setSizeFull();
        documentsLay.setSpacing(true);
        documentsLay.setMargin(true);
        documentsLay.addComponent(captionDocuments);
        documentsLay.addComponent(documentsDataTable);
        documentsLay.setExpandRatio(documentsDataTable, 1);
    }

    private void buildExtraLayout() {

        FormLayout fieldsLayExtra = new FormLayout();
        fieldsLayExtra.setSizeFull();
        fieldsLayExtra.setSpacing(false);
        fieldsLayExtra.setMargin(false);

        healthCB = createCombobox(0, null, Settings.dbHealthStatus, true);
        healthCB.setCaption(myUI.getMessage(IndigoMessages.HealthStatus));
        fieldsLayExtra.addComponent(healthCB);

        healthNotesTA = new TextArea(myUI.getMessage(IndigoMessages.HealthNotes));
        healthNotesTA.setStyleName(ValoTheme.TEXTFIELD_TINY);
        healthNotesTA.setWidth(Settings.PERCENTS100);
        healthNotesTA.setRows(5);
        healthNotesTA.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 350, true));
        fieldsLayExtra.addComponent(healthNotesTA);

        hobbiesTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 300, true), false);
        hobbiesTF.setCaption(myUI.getMessage(IndigoMessages.Hobbies));
        fieldsLayExtra.addComponent(hobbiesTF);

        fobbiesTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 300, true), false);
        fobbiesTF.setCaption(myUI.getMessage(IndigoMessages.Phobias));
        fieldsLayExtra.addComponent(fobbiesTF);

        shortNotesTA = new TextArea(myUI.getMessage(IndigoMessages.ShortNote));
        shortNotesTA.setStyleName(ValoTheme.TEXTFIELD_TINY);
        shortNotesTA.setWidth(Settings.PERCENTS100);
        shortNotesTA.setRows(3);
        shortNotesTA.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 300, true));
        fieldsLayExtra.addComponent(shortNotesTA);

        Label captionQuestioning = new Label();
        captionQuestioning.setWidth(Settings.PERCENTS100);
        captionQuestioning.setContentMode(ContentMode.HTML);
        captionQuestioning.setValue(myUI.getMessage(IndigoMessages.Questioning));
        captionQuestioning.setStyleName("tableCpt");

        questioningTable = new FormattedTable(myUI);
        questioningTable.setSizeFull();
        questioningTable.setStyleName(ValoTheme.TABLE_SMALL);

        Label captionExtraInfo = new Label();
        captionExtraInfo.setWidth(Settings.PERCENTS100);
        captionExtraInfo.setContentMode(ContentMode.HTML);
        captionExtraInfo.setValue(myUI.getMessage(IndigoMessages.ExtraInfo));
        captionExtraInfo.setStyleName("tableCpt");

        extraInfoLay = new GridLayout(2, 2);
        extraInfoLay.setSizeFull();
        extraInfoLay.setSpacing(true);
        extraInfoLay.setMargin(true);
        extraInfoLay.addComponent(captionExtraInfo, 0, 0);
        extraInfoLay.addComponent(fieldsLayExtra, 0, 1);
        extraInfoLay.addComponent(captionQuestioning, 1, 0);
        extraInfoLay.addComponent(questioningTable, 1, 1);
        extraInfoLay.setRowExpandRatio(1, 1);
        extraInfoLay.setColumnExpandRatio(0, 1.2f);
        extraInfoLay.setColumnExpandRatio(1, 1.8f);
    }

    private void buildAchievementsLayout() {

        achievementsInfoLay = new GridLayout(3, 8);
        achievementsInfoLay.setWidth(Settings.PERCENTS100);
        achievementsInfoLay.setSpacing(true);
        achievementsInfoLay.setMargin(true);
        achievementsInfoLay.setColumnExpandRatio(0, 1);

        Label captionExams = new Label();
        captionExams.setSizeFull();
        captionExams.setContentMode(ContentMode.HTML);
        captionExams.setValue(myUI.getMessage(IndigoMessages.Exams));
        captionExams.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionExams, 0, 0);

        noExamsCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noExamsCkb.addValueChangeListener(this);
        achievementsInfoLay.addComponent(noExamsCkb, 2, 0);
        achievementsInfoLay.setComponentAlignment(noExamsCkb, Alignment.BOTTOM_RIGHT);

        plusExamButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusExamButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusExamButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusExamButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusExamButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusExamButton, 1, 0);

        examsTable = new FormattedTable(myUI);
        examsTable.setSizeFull();
        examsTable.setStyleName(ValoTheme.TABLE_SMALL);
        achievementsInfoLay.addComponent(examsTable, 0, 1, 2, 1);

        Label captionSeminars = new Label();
        captionSeminars.setSizeFull();
        captionSeminars.setContentMode(ContentMode.HTML);
        captionSeminars.setValue(myUI.getMessage(IndigoMessages.Seminars));
        captionSeminars.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionSeminars, 0, 2);

        noSeminarsCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noSeminarsCkb.addValueChangeListener(this);
        achievementsInfoLay.addComponent(noSeminarsCkb, 2, 2);
        achievementsInfoLay.setComponentAlignment(noSeminarsCkb, Alignment.BOTTOM_RIGHT);

        plusSeminarButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusSeminarButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusSeminarButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusSeminarButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusSeminarButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusSeminarButton, 1, 2);

        seminarsTable = new FormattedTable(myUI);
        seminarsTable.setSizeFull();
        seminarsTable.setStyleName(ValoTheme.TABLE_SMALL);
        achievementsInfoLay.addComponent(seminarsTable, 0, 3, 2, 3);

        Label captionCertificates = new Label();
        captionCertificates.setSizeFull();
        captionCertificates.setContentMode(ContentMode.HTML);
        captionCertificates.setValue(myUI.getMessage(IndigoMessages.Certificates));
        captionCertificates.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionCertificates, 0, 4);

        noCertificatesCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noCertificatesCkb.addValueChangeListener(this);
        achievementsInfoLay.addComponent(noCertificatesCkb, 2, 4);
        achievementsInfoLay.setComponentAlignment(noCertificatesCkb, Alignment.BOTTOM_RIGHT);

        plusCertificateButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusCertificateButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusCertificateButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusCertificateButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusCertificateButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusCertificateButton, 1, 4);

        certificatesTable = new FormattedTable(myUI);
        certificatesTable.setSizeFull();
        certificatesTable.setStyleName(ValoTheme.TABLE_SMALL);
        achievementsInfoLay.addComponent(certificatesTable, 0, 5, 2, 5);

        Label captionLanguages = new Label();
        captionLanguages.setSizeFull();
        captionLanguages.setContentMode(ContentMode.HTML);
        captionLanguages.setValue(myUI.getMessage(IndigoMessages.Languages));
        captionLanguages.setStyleName("tableCpt");
        achievementsInfoLay.addComponent(captionLanguages, 0, 6);

        noLanguagesCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noLanguagesCkb.addValueChangeListener(this);
        achievementsInfoLay.addComponent(noLanguagesCkb, 2, 6);
        achievementsInfoLay.setComponentAlignment(noLanguagesCkb, Alignment.BOTTOM_RIGHT);

        plusLanguageButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusLanguageButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusLanguageButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusLanguageButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusLanguageButton.addClickListener(this);
        achievementsInfoLay.addComponent(plusLanguageButton, 1, 6);

        languagesTable = new FormattedTable(myUI);
        languagesTable.setSizeFull();
        languagesTable.setStyleName(ValoTheme.TABLE_SMALL);
        achievementsInfoLay.addComponent(languagesTable, 0, 7, 2, 7);
    }

    private void buildProfLayout() {

        profInfoLay = new GridLayout(3, 8);
        profInfoLay.setSpacing(true);
        profInfoLay.setMargin(true);
        profInfoLay.setWidth(Settings.PERCENTS100);
        profInfoLay.setColumnExpandRatio(0, 1);

        Label captionGradSchool = new Label();
        captionGradSchool.setWidth(Settings.PERCENTS100);
        captionGradSchool.setContentMode(ContentMode.HTML);
        captionGradSchool.setValue(myUI.getMessage(IndigoMessages.GraduationSchool));
        captionGradSchool.setStyleName("tableCpt");
        profInfoLay.addComponent(captionGradSchool, 0, 0, 2, 0);

        gradSchoolCB = createCombobox(0, null, null, true);
        gradSchoolCB.setCaption(myUI.getMessage(IndigoMessages.GraduationSchool));
        try {
            DbSchool dbCon = new DbSchool();
            dbCon.connect();
            gradSchoolCB.setContainerDataSource(dbCon.execSchoolSel(myUI, "1,3,4"));
            Item item = gradSchoolCB.getContainerDataSource().addItem(0);
            item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(myUI.getMessage(IndigoMessages.OtherSchool));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        gradSchoolCB.setValue(0);
        profInfoLay.addComponent(gradSchoolCB, 0, 1);

        gradSchoolStartDF = createDateField(null, null, myUI.getMessage(IndigoMessages.Start),
                true, Settings.yearPattern, Resolution.YEAR);
        gradSchoolStartDF.setWidth(Settings.PERCENTS100);
        gradSchoolStartDF.setResolution(Resolution.YEAR);
        profInfoLay.addComponent(gradSchoolStartDF, 1, 1);

        gradSchoolEndDF = createDateField(null, null, myUI.getMessage(IndigoMessages.End),
                true, Settings.yearPattern, Resolution.YEAR);
        gradSchoolEndDF.setWidth(Settings.PERCENTS100);
        gradSchoolEndDF.setResolution(Resolution.YEAR);
        profInfoLay.addComponent(gradSchoolEndDF, 2, 1);

        Label captionBranches = new Label();
        captionBranches.setSizeFull();
        captionBranches.setContentMode(ContentMode.HTML);
        captionBranches.setValue(myUI.getMessage(IndigoMessages.Branches));
        captionBranches.setStyleName("tableCpt");
        profInfoLay.addComponent(captionBranches, 0, 2);

        noBranchesCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noBranchesCkb.addValueChangeListener(this);
        profInfoLay.addComponent(noBranchesCkb, 2, 2);
        profInfoLay.setComponentAlignment(noBranchesCkb, Alignment.BOTTOM_RIGHT);

        plusBranchButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusBranchButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusBranchButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusBranchButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusBranchButton.addClickListener(this);
        profInfoLay.addComponent(plusBranchButton, 1, 2);

        branchesTable = new FormattedTable(myUI);
        branchesTable.setSizeFull();
        branchesTable.setStyleName(ValoTheme.TABLE_SMALL);
        profInfoLay.addComponent(branchesTable, 0, 3, 2, 3);

        Label captionEducation = new Label();
        captionEducation.setSizeFull();
        captionEducation.setContentMode(ContentMode.HTML);
        captionEducation.setValue(myUI.getMessage(IndigoMessages.Education));
        captionEducation.setStyleName("tableCpt");
        profInfoLay.addComponent(captionEducation, 0, 4);

        noEducationCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noEducationCkb.addValueChangeListener(this);
        profInfoLay.addComponent(noEducationCkb, 2, 4);
        profInfoLay.setComponentAlignment(noEducationCkb, Alignment.BOTTOM_RIGHT);

        plusEducationButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusEducationButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusEducationButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusEducationButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusEducationButton.addClickListener(this);
        profInfoLay.addComponent(plusEducationButton, 1, 4);

        educationTable = new FormattedTable(myUI);
        educationTable.setSizeFull();
        educationTable.setStyleName(ValoTheme.TABLE_SMALL);
        profInfoLay.addComponent(educationTable, 0, 5, 2, 5);

        Label captionWorkPlaces = new Label();
        captionWorkPlaces.setSizeFull();
        captionWorkPlaces.setContentMode(ContentMode.HTML);
        captionWorkPlaces.setValue(myUI.getMessage(IndigoMessages.WorkPlaces));
        captionWorkPlaces.setStyleName("tableCpt");
        profInfoLay.addComponent(captionWorkPlaces, 0, 6);

        noWorkPlacesCkb = new CheckBox(myUI.getMessage(IndigoMessages.DontHave));
        noWorkPlacesCkb.addValueChangeListener(this);
        profInfoLay.addComponent(noWorkPlacesCkb, 2, 6);
        profInfoLay.setComponentAlignment(noWorkPlacesCkb, Alignment.BOTTOM_RIGHT);

        plusWorkPlaceButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusWorkPlaceButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusWorkPlaceButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusWorkPlaceButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusWorkPlaceButton.addClickListener(this);
        profInfoLay.addComponent(plusWorkPlaceButton, 1, 6);

        workPlacesTable = new FormattedTable(myUI);
        workPlacesTable.setSizeFull();
        workPlacesTable.setStyleName(ValoTheme.TABLE_SMALL);
        profInfoLay.addComponent(workPlacesTable, 0, 7, 2, 7);
    }

    private void buildOrdersLayout() {

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);

        Label captionOrders = new Label();
        captionOrders.setSizeFull();
        captionOrders.setContentMode(ContentMode.HTML);
        captionOrders.setValue(myUI.getMessage(IndigoMessages.OrdersHistory));
        captionOrders.setStyleName("tableCpt");

        plusOrdersButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
        plusOrdersButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusOrdersButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusOrdersButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusOrdersButton.addClickListener(this);

        hl.addComponent(captionOrders);
        hl.setSpacing(true);
        hl.addComponent(plusOrdersButton);
        hl.setExpandRatio(captionOrders, 1);

        ordersTable = new FormattedTable(myUI);
        ordersTable.setImmediate(true);
        ordersTable.setSizeFull();
        ordersTable.setStyleName(ValoTheme.TABLE_SMALL);

        ordersInfoLay = new GridLayout(1, 2);
        ordersInfoLay.setSizeFull();
        ordersInfoLay.setSpacing(true);
        ordersInfoLay.setMargin(true);
        ordersInfoLay.addComponent(hl, 0, 0);
        ordersInfoLay.addComponent(ordersTable, 0, 1);
        ordersInfoLay.setRowExpandRatio(1, 1);
    }

    private void buildSchoolInfoLayout() {

        HorizontalLayout hlSupervision = null;
        HorizontalLayout hlLessons = null;

        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeSupervision)) {
            hlSupervision = new HorizontalLayout();
            hlSupervision.setWidth(Settings.PERCENTS100);

            Label captionSupervision = new Label();
            captionSupervision.setSizeFull();
            captionSupervision.setContentMode(ContentMode.HTML);
            captionSupervision.setValue(myUI.getMessage(IndigoMessages.Supervision));
            captionSupervision.setStyleName("tableCpt");

            canBeAdvisorCkb = new CheckBox(myUI.getMessage(IndigoMessages.CanBeAdvisor));

            plusSupervisionButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
            plusSupervisionButton.setStyleName(ValoTheme.BUTTON_SMALL);
            plusSupervisionButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
            plusSupervisionButton.setIcon(FontAwesome.PLUS_SQUARE);
            plusSupervisionButton.addClickListener(this);

            hlSupervision.setSpacing(true);
            hlSupervision.addComponent(captionSupervision);
            hlSupervision.addComponent(plusSupervisionButton);
            hlSupervision.addComponent(canBeAdvisorCkb);
            hlSupervision.setComponentAlignment(canBeAdvisorCkb, Alignment.BOTTOM_RIGHT);
            hlSupervision.setExpandRatio(captionSupervision, 1);

            supervisionTable = new FormattedTable(myUI);
            supervisionTable.setSizeFull();
            supervisionTable.setStyleName(ValoTheme.TABLE_SMALL);
        }

        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeLessons)) {
            hlLessons = new HorizontalLayout();
            hlLessons.setWidth(Settings.PERCENTS100);

            Label captionLessons = new Label();
            captionLessons.setSizeFull();
            captionLessons.setContentMode(ContentMode.HTML);
            captionLessons.setValue(myUI.getMessage(IndigoMessages.Lessons));
            captionLessons.setStyleName("tableCpt");

            plusLessonsButton = new Button(myUI.getMessage(IndigoMessages.AddRecord));
            plusLessonsButton.setStyleName(ValoTheme.BUTTON_SMALL);
            plusLessonsButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
            plusLessonsButton.setIcon(FontAwesome.PLUS_SQUARE);
            plusLessonsButton.addClickListener(this);

            hlLessons.setSpacing(true);
            hlLessons.addComponent(captionLessons);
            hlLessons.addComponent(plusLessonsButton);
            hlLessons.setExpandRatio(captionLessons, 1);

            lessonsTable = new FormattedTable(myUI);
            lessonsTable.setSizeFull();
            lessonsTable.setStyleName(ValoTheme.TABLE_SMALL);
        }
        schoolInfoLay = new VerticalLayout();
        schoolInfoLay.setWidth(Settings.PERCENTS100);
        schoolInfoLay.setSpacing(true);
        schoolInfoLay.setMargin(true);
        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeSupervision)) {
            schoolInfoLay.addComponent(hlSupervision);
            schoolInfoLay.addComponent(supervisionTable);
            schoolInfoLay.setExpandRatio(supervisionTable, 1);
        }
        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeLessons)) {
            schoolInfoLay.addComponent(hlLessons);
            schoolInfoLay.addComponent(lessonsTable);
            schoolInfoLay.setExpandRatio(lessonsTable, 1);
        }
    }

    private void buildPermissionsLayout() {

        Label captionPermissions = new Label();
        captionPermissions.setWidth(Settings.PERCENTS100);
        captionPermissions.setHeight("27px");
        captionPermissions.setContentMode(ContentMode.HTML);
        captionPermissions.setValue(myUI.getMessage(IndigoMessages.Permissions));
        captionPermissions.setStyleName("tableCpt");

        String[] NATURAL_COL_ORDER_PERMISSIONS = new String[]{
                myUI.getMessage(IndigoMessages.Functions), myUI.getMessage(IndigoMessages.ClassCaption)};
        permissionTable = new FormattedTable(myUI);
        permissionTable.setSizeFull();
        permissionTable.setStyleName(ValoTheme.TABLE_SMALL);
        try {
            DbDefinition dbe = new DbDefinition();
            dbe.connect();
            permissionCont = dbe.execPermissionSQL(myUI);
            dbe.close();
            for (Object next : permissionCont.getItemIds()) {
                ComboBoxMultiselect permMCB = new ComboBoxMultiselect();
                permMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
                permMCB.setWidth(Settings.PERCENTS100);
                permMCB.setPageLength(20);
                permMCB.setShowSelectAllButton((filter, page) -> true);
                permMCB.addItems(convertStrToSet(permissionCont.getContainerProperty(next,
                        myUI.getMessage(IndigoMessages.Value)).getValue().toString()));
                permissionCont.getContainerProperty(next,
                        myUI.getMessage(IndigoMessages.Functions)).setValue(permMCB);
            }
            permissionTable.setContainerDataSource(permissionCont);
            permissionTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PERMISSIONS);
            permissionTable.setColumnWidth(myUI.getMessage(IndigoMessages.ClassCaption), 235);
            permissionTable.setPageLength(0);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }

        permissionsLay = new VerticalLayout();
        permissionsLay.setSizeFull();
        permissionsLay.setSpacing(true);
        permissionsLay.setMargin(true);
        permissionsLay.addComponent(captionPermissions);
        permissionsLay.addComponent(permissionTable);
        permissionsLay.setExpandRatio(permissionTable, 1);
    }

    private Set<?> convertStrToSet(String str) {
        String[] strArr = str.split(",");
        HashSet<String> hs = new HashSet<>(strArr.length);
        Collections.addAll(hs, strArr);
        return hs;
    }

    private void setPermTable_options(String oneStringAll) {
        try {
            String[] dividedStringAll = oneStringAll.split(";");
            int z = permissionCont.size();
            for (int i = 0; i < z; i++) {
                if (i < dividedStringAll.length && dividedStringAll[i] != null) {
                    String[] byClassName = dividedStringAll[i].split(":");
                    Item itm = permissionCont.getItem(byClassName[0]);
                    if (itm != null) {
                        ((ComboBoxMultiselect) itm.getItemProperty(
                                myUI.getMessage(IndigoMessages.Functions)).getValue())
                                .setValue(convertStrToSet(byClassName[1]));
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setContractsTable() {
        contractsTable.setEnabled(true);
        plusContractButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_CONTRACTS = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.AgreementType),
                    myUI.getMessage(IndigoMessages.SalaryAmount),
                    myUI.getMessage(IndigoMessages.CreationDate),
                    myUI.getMessage(IndigoMessages.Start),
                    myUI.getMessage(IndigoMessages.End)};
            DbEmployeeContract dbCon = new DbEmployeeContract();
            dbCon.connect();
            contractsTable.setContainerDataSource(dbCon.execSQL(myUI, employeeID, this));
            dbCon.close();
            contractsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CONTRACTS);
            contractsTable.setColumnWidth(Settings.button, 60);
            contractsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.AgreementType), 1);
            contractsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.SalaryAmount), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setPhonesTable() {
        phonesTable.setEnabled(true);
        plusPhonesButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_PHONES = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.Type),
                    myUI.getMessage(IndigoMessages.Number)};
            DbEmployeePhoneNumber dbepn = new DbEmployeePhoneNumber();
            dbepn.connect();
            phonesTable.setContainerDataSource(dbepn.execSQL(myUI, employeeID, this));
            dbepn.close();
            phonesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PHONES);
            phonesTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Number), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, Settings.columnPhones);
            if (isFilled != null) {
                noPhonesCkb.setEnabled(!isFilled);
                noPhonesCkb.setValue(!isFilled);
                phonesTable.setEnabled(isFilled);
                plusPhonesButton.setEnabled(isFilled);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setChildrenTable() {
        childrenTable.setEnabled(true);
        plusChildButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_CHILDREN = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.FullName),
                    myUI.getMessage(IndigoMessages.DateOfBirth),
                    myUI.getMessage(IndigoMessages.Institution),
                    myUI.getMessage(IndigoMessages.EducationStatus),
                    myUI.getMessage(IndigoMessages.HealthStatus)};
            DbEmployeeChildren dbech = new DbEmployeeChildren();
            dbech.connect();
            childrenTable.setContainerDataSource(dbech.execSQL(myUI, employeeID, this));
            dbech.close();
            childrenTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CHILDREN);
            childrenTable.setPageLength(childrenTable.size() > 0 ? childrenTable.size() : 1);
            childrenTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.FullName), 1);
            childrenTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Institution), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, Settings.columnChildren);
            if (isFilled != null) {
                noChildrenCkb.setEnabled(!isFilled);
                noChildrenCkb.setValue(!isFilled);
                childrenTable.setEnabled(isFilled);
                plusChildButton.setEnabled(isFilled);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setEducationTable(Table t, int own_id) {
        t.setEnabled(true);
        try {
            if (own_id == 1) {
                plusEducationButton.setEnabled(true);
                NATURAL_COL_ORDER_EDU = new String[]{Settings.button,
                        myUI.getMessage(IndigoMessages.University),
                        myUI.getMessage(IndigoMessages.Country),
                        myUI.getMessage(IndigoMessages.Department),
                        myUI.getMessage(IndigoMessages.EduLevel),
                        myUI.getMessage(IndigoMessages.Start),
                        myUI.getMessage(IndigoMessages.End),
                        myUI.getMessage(IndigoMessages.Document)};
            } else {
                plusSpouseEducationButton.setEnabled(true);
                NATURAL_COL_ORDER_EDU = new String[]{Settings.button,
                        myUI.getMessage(IndigoMessages.University),
                        myUI.getMessage(IndigoMessages.Country),
                        myUI.getMessage(IndigoMessages.Department),
                        myUI.getMessage(IndigoMessages.EduLevel),
                        myUI.getMessage(IndigoMessages.Start),
                        myUI.getMessage(IndigoMessages.End)};
            }
            DbEmployeeEducation dbed = new DbEmployeeEducation();
            dbed.connect();
            t.setContainerDataSource(dbed.execSQL(myUI, employeeID, own_id, this));
            dbed.close();
            t.setVisibleColumns((Object[]) NATURAL_COL_ORDER_EDU);
            t.setPageLength(t.size());
            t.setColumnExpandRatio(myUI.getMessage(IndigoMessages.University), 1);
            t.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Department), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, own_id == 1 ?
                    Settings.columnEducation : Settings.columnSpouseEducation);
            if (isFilled != null) {
                t.setEnabled(isFilled);
                if (own_id == 1) {
                    noEducationCkb.setEnabled(!isFilled);
                    noEducationCkb.setValue(!isFilled);
                    plusEducationButton.setEnabled(isFilled);
                } else {
                    noSpouseEducationCkb.setEnabled(!isFilled);
                    noSpouseEducationCkb.setValue(!isFilled);
                    plusSpouseEducationButton.setEnabled(isFilled);
                }
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setWorkTable(Table t, int own_id) {
        t.setEnabled(true);
        if (own_id == 1) {
            plusSpouseWorkPlacesButton.setEnabled(true);
        } else {
            plusSpouseWorkPlacesButton.setEnabled(true);
        }
        try {
            NATURAL_COL_ORDER_WORK = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.WorkPlace),
                    myUI.getMessage(IndigoMessages.Sapat),
                    myUI.getMessage(IndigoMessages.MainPosition),
                    myUI.getMessage(IndigoMessages.ExtraPositions),
                    myUI.getMessage(IndigoMessages.WorkingStatus),
                    myUI.getMessage(IndigoMessages.Start),
                    myUI.getMessage(IndigoMessages.End)};
            DbEmployeeWork dbew = new DbEmployeeWork();
            dbew.connect();
            t.setContainerDataSource(dbew.execSQL(myUI, employeeID, own_id, this));
            dbew.close();
            t.setVisibleColumns((Object[]) NATURAL_COL_ORDER_WORK);
            t.setPageLength(t.size() > 0 ? t.size() : 1);
            t.setColumnExpandRatio(myUI.getMessage(IndigoMessages.WorkPlace), 1);
            t.setColumnExpandRatio(myUI.getMessage(IndigoMessages.MainPosition), 1);
            t.setColumnExpandRatio(myUI.getMessage(IndigoMessages.ExtraPositions), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, own_id == 1 ?
                    Settings.columnWork_places : Settings.columnSpouseWork_places);
            if (isFilled != null) {
                t.setEnabled(isFilled);
                if (own_id == 1) {
                    noWorkPlacesCkb.setEnabled(!isFilled);
                    noWorkPlacesCkb.setValue(!isFilled);
                    plusWorkPlaceButton.setEnabled(isFilled);
                } else {
                    noSpouseWorkPlacesCkb.setEnabled(!isFilled);
                    noSpouseWorkPlacesCkb.setValue(!isFilled);
                    plusSpouseWorkPlacesButton.setEnabled(isFilled);
                }
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setQuestioningTable() {
        try {
            String[] NATURAL_COL_ORDER_QUESTIONING = new String[]{
                    myUI.getMessage(IndigoMessages.Question),
                    myUI.getMessage(IndigoMessages.Answer)};
            DbEmployeeQuestion dbq = new DbEmployeeQuestion();
            dbq.connect();
            questioningTable.setContainerDataSource(dbq.execSQL(myUI, employeeID, this));
            dbq.close();
            questioningTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_QUESTIONING);
            questioningTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Question), 1);
            questioningTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Answer), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setLanguagesTable() {
        languagesTable.setEnabled(true);
        plusLanguageButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_LANGUAGES = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.Language),
                    myUI.getMessage(IndigoMessages.Level)};
            DbEmployeeLanguage dbel = new DbEmployeeLanguage();
            dbel.connect();
            languagesTable.setContainerDataSource(
                    dbel.execSQL(myUI, employeeID, this));
            dbel.close();
            languagesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_LANGUAGES);
            languagesTable.setPageLength(languagesTable.size() > 0 ? languagesTable.size() : 1);
            languagesTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Language), 1);
            languagesTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Level), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, Settings.columnLanguages);
            if (isFilled != null) {
                noLanguagesCkb.setEnabled(!isFilled);
                noLanguagesCkb.setValue(!isFilled);
                languagesTable.setEnabled(isFilled);
                plusLanguageButton.setEnabled(isFilled);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setCertificatesTable() {
        certificatesTable.setEnabled(true);
        plusCertificateButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_CERTIFICATES = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.Certificate),
                    myUI.getMessage(IndigoMessages.GivenBy),
                    myUI.getMessage(IndigoMessages.IssueDate),
                    myUI.getMessage(IndigoMessages.Note),
                    myUI.getMessage(IndigoMessages.Document)};
            DbEmployeeCertificate dbec = new DbEmployeeCertificate();
            dbec.connect();
            certificatesTable.setContainerDataSource(
                    dbec.execSQL(myUI, employeeID, this));
            dbec.close();
            certificatesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CERTIFICATES);
            certificatesTable.setPageLength(certificatesTable.size() > 0 ? certificatesTable.size() : 1);
            certificatesTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Title), 1);
            certificatesTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.GivenBy), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, Settings.columnCertificates);
            if (isFilled != null) {
                noCertificatesCkb.setEnabled(!isFilled);
                noCertificatesCkb.setValue(!isFilled);
                certificatesTable.setEnabled(isFilled);
                plusCertificateButton.setEnabled(isFilled);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setSeminarsTable() {
        seminarsTable.setEnabled(true);
        plusSeminarButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_SEMINARS = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.Title),
                    myUI.getMessage(IndigoMessages.Subject),
                    myUI.getMessage(IndigoMessages.Note),
                    myUI.getMessage(IndigoMessages.IssueDate)};
            DbEmployeeSeminar dbes = new DbEmployeeSeminar();
            dbes.connect();
            seminarsTable.setContainerDataSource(
                    dbes.execSQL(myUI, employeeID, this));
            dbes.close();
            seminarsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_SEMINARS);
            seminarsTable.setPageLength(seminarsTable.size() > 0 ? seminarsTable.size() : 1);
            seminarsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Title), 1);
            seminarsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Subject), 1);
            seminarsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Note), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, Settings.columnSeminars);
            if (isFilled != null) {
                noSeminarsCkb.setEnabled(!isFilled);
                noSeminarsCkb.setValue(!isFilled);
                seminarsTable.setEnabled(isFilled);
                plusSeminarButton.setEnabled(isFilled);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setExamsTable() {
        examsTable.setEnabled(true);
        plusExamButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_EXAMS = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.Exam),
                    myUI.getMessage(IndigoMessages.Score),
                    myUI.getMessage(IndigoMessages.IssueDate),
                    myUI.getMessage(IndigoMessages.Document)};
            DbEmployeeExam dbex = new DbEmployeeExam();
            dbex.connect();
            examsTable.setContainerDataSource(dbex.execSQL(myUI, employeeID, this));
            dbex.close();
            examsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_EXAMS);
            examsTable.setPageLength(examsTable.size() > 0 ? examsTable.size() : 1);
            examsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Exam), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, Settings.columnExams);
            if (isFilled != null) {
                noExamsCkb.setEnabled(!isFilled);
                noExamsCkb.setValue(!isFilled);
                examsTable.setEnabled(isFilled);
                plusExamButton.setEnabled(isFilled);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setBranchesTable() {
        branchesTable.setEnabled(true);
        plusBranchButton.setEnabled(true);
        try {
            NATURAL_COL_ORDER_BRANCHES = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.Branch),
                    myUI.getMessage(IndigoMessages.Main)};
            DbEmployeeBranch dbeb = new DbEmployeeBranch();
            dbeb.connect();
            branchesTable.setContainerDataSource(
                    dbeb.execSQL(myUI, employeeID, this));
            dbeb.close();
            branchesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_BRANCHES);
            branchesTable.setPageLength(branchesTable.size() > 0 ? branchesTable.size() : 1);
            branchesTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Branch), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            Boolean isFilled = dbCon.execSQL(employeeID, Settings.columnBranches);
            if (isFilled != null) {
                noBranchesCkb.setEnabled(!isFilled);
                noBranchesCkb.setValue(!isFilled);
                branchesTable.setEnabled(isFilled);
                plusBranchButton.setEnabled(isFilled);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setLessonsTable() {
        try {
            NATURAL_COL_ORDER_LESSONS = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.Lesson),
                    myUI.getMessage(IndigoMessages.ClassName),
                    myUI.getMessage(IndigoMessages.AcademicYear),
                    myUI.getMessage(IndigoMessages.Hours),
                    myUI.getMessage(IndigoMessages.ExtraHours)};
            DbEmployeeLessons dbel = new DbEmployeeLessons();
            dbel.connect();
            lessonsTable.setContainerDataSource(
                    dbel.execSQL(myUI, employeeID, myUI.getUser().getSchool().getId(), this));
            dbel.close();
            lessonsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_LESSONS);
            lessonsTable.setPageLength(lessonsCont.size() > 0 ? lessonsTable.size() : 1);
            lessonsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Lessons), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setSupervisionTable() {
        try {
            NATURAL_COL_ORDER_SUPERVISION = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.ClassName),
                    myUI.getMessage(IndigoMessages.FromDate),
                    myUI.getMessage(IndigoMessages.TillDate),
                    myUI.getMessage(IndigoMessages.Note)};
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            supervisionTable.setContainerDataSource(dbeo.execSQL(myUI, employeeID, myUI.getUser().getSchool().getId(), this));
            dbeo.close();
            supervisionTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_SUPERVISION);
            supervisionTable.setPageLength(supervisionTable.size() > 0 ? supervisionTable.size() : 1);
            supervisionTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.ClassName), 1);
            supervisionTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Note), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setOrdersTable() {
        try {
            NATURAL_COL_ORDER_ORDERS = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.OrderType),
                    myUI.getMessage(IndigoMessages.Details),
                    myUI.getMessage(IndigoMessages.FromDate),
                    myUI.getMessage(IndigoMessages.TillDate),
                    myUI.getMessage(IndigoMessages.Note)};
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            ordersTable.setContainerDataSource(
                    dbeo.execSQL(myUI, employeeID, myUI.getUser().getSchool().getId(), currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr), this));
            dbeo.close();
            ordersTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_ORDERS);
            ordersTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.OrderType), 1);
            ordersTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Note), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setDocumentsTable() {
        try {
            DbAttachment dbCon = new DbAttachment();
            dbCon.connect();
            documentsDataTable.setContainerDataSource(dbCon.execSQL(myUI, employeeID, this));
            dbCon.close();
            documentsDataTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Title), 1);
            documentsDataTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Details), 1);
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setContactFields() {
        try {
            DbEmployeeContact dbec = new DbEmployeeContact();
            dbec.connect();
            EmployeeContact ec = dbec.execSQL(employeeID);
            dbec.close();
            if (ec != null) {
                addressTA.setValue(ec.getAddress());
                birthPlaceTF.setValue(ec.getBirth_place());
                passportTF.setValue(ec.getPassport());
                innTF.setValue(ec.getInn());
                passportGivenTf.setValue(ec.getPassportGiven());
                passportDateDF.setValue(ec.getPassportDate());
                emailTF.setValue(ec.getEmail());
            } else {
                clearContactFields();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setSpouseFields() {
        try {
            DbEmployeeSpouse dbes = new DbEmployeeSpouse();
            dbes.connect();
            EmployeeSpouse es = dbes.execSQL(employeeID);
            dbes.close();
            if (es != null) {
                spouseFullNameTF.setValue(es.getFullName());
                if (es.getPhone() != null) {
                    spousePhoneTF.setValue(es.getPhone());
                }
                if (es.getHealth_notes() != null) {
                    spouseHealthNotesTF.setValue(es.getHealth_notes());
                }
                spouseHealthCB.setValue(es.getHealth_status_id());
            } else {
                spouseHealthCB.setValue(null);
                spouseFullNameTF.setValue("");
                spousePhoneTF.setValue("");
                spouseHealthNotesTF.setValue("");
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setExtraInfoFields() {
        try {
            DbEmployeeExtraInfo dbei = new DbEmployeeExtraInfo();
            dbei.connect();
            EmployeeExtraInfo eei = dbei.execSQL(employeeID);
            dbei.close();
            if (eei != null) {
                if (eei.getHobbies() != null) {
                    hobbiesTF.setValue(eei.getHobbies());
                } else {
                    hobbiesTF.setValue("");
                }
                if (eei.getPhobias() != null) {
                    fobbiesTF.setValue(eei.getPhobias());
                } else {
                    fobbiesTF.setValue("");
                }
                if (eei.getHealth_notes() != null) {
                    healthNotesTA.setValue(eei.getHealth_notes());
                } else {
                    healthNotesTA.setValue("");
                }
                if (eei.getShort_notes() != null) {
                    shortNotesTA.setValue(eei.getShort_notes());
                } else {
                    shortNotesTA.setValue("");
                }
                healthCB.setValue(eei.getHealth_status_id());
            } else {
                clearExtraInfoFields();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void setGradSchoolFields() {
        try {
            DbEmployeeGraduationSchool dbCon = new DbEmployeeGraduationSchool();
            dbCon.connect();
            EmployeeGraduationSchool egs = dbCon.execSQL(employeeID);
            dbCon.close();
            if (egs != null) {
                gradSchoolCB.setValue(egs.getSchool_id());
                gradSchoolStartDF.setValue(egs.getStart());
                gradSchoolEndDF.setValue(egs.getEnd());
            } else {
                clearGradSchoolFields();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        cvBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        optionGroup.setEnabled(false);
        employeesDataTable.setEnabled(false);
        contactInfoLay.setEnabled(true);
        contractInfoLay.setEnabled(true);
        familyInfoLay.setEnabled(true);
        extraInfoLay.setEnabled(true);
        formLay.setEnabled(true);
        achievementsInfoLay.setEnabled(true);
        enableUploads(certificatesTable);
        enableUploads(examsTable);
        enableUploads(educationTable);
        profInfoLay.setEnabled(true);
        schoolInfoLay.setEnabled(true);
        permissionsLay.setEnabled(true);
        ordersInfoLay.setEnabled(true);
        photoUpl.setEnabled(true);

        for (Component component : tabs) {
            TabSheet.Tab tab = tabs.getTab(component);
            tab.setEnabled(tab == tabs.getTab(tabs.getSelectedTab()));
        }
    }

    private void prepareNormalMode() {
        if (employeeID == myUI.getUser().getId()) {
            modifyBtn.setEnabled(true);
        } else if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.actModify) && employeeID != 0
                && (Boolean) employeesDataTable.getContainerProperty(employeeID, Settings.is_modifiable).getValue()) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (tabs.getSelectedTab() == tabs.getTab(empSearchLay).getComponent()) {
            if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.actDelete) && employeeID != 0
                    && (Boolean) employeesDataTable.getContainerProperty(employeeID, Settings.is_modifiable).getValue()) {
                deleteBtn.setEnabled(true);
            }
        } else {
            deleteBtn.setEnabled(false);
        }
        if (employeeID != 0) {
            cvBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        employeesDataTable.setEnabled(true);
        optionGroup.setEnabled(true);
        contactInfoLay.setEnabled(false);
        contractInfoLay.setEnabled(false);
        familyInfoLay.setEnabled(false);
        extraInfoLay.setEnabled(false);
        formLay.setEnabled(false);
        achievementsInfoLay.setEnabled(false);
        schoolInfoLay.setEnabled(false);
        permissionsLay.setEnabled(false);
        profInfoLay.setEnabled(false);
        ordersInfoLay.setEnabled(false);
        photoUpl.setEnabled(false);

        for (Component component : tabs) {
            TabSheet.Tab tab = tabs.getTab(component);
            if (tab.getComponent() == empSearchLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == contactInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == contractInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == familyInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == extraInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == achievementsInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == profInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == schoolInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == permissionsLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == ordersInfoLay) {
                tab.setEnabled(true);
            } else if (tab.getComponent() == documentsLay) {
                tab.setEnabled(true);
            }
        }
    }

    private void repaint() {
        StringBuilder str = new StringBuilder();
        Iterator<Integer> iter = (Iterator<Integer>) workingStatCont.getItemIds().iterator();
        int total = 0;
        while (iter.hasNext()) {
            Integer next = iter.next();
            str.append("&emsp;").append(workingStatCont.getContainerProperty(next, myUI.getMessage(IndigoMessages.Title)).getValue()).append(": ").append(workingStatCont.getContainerProperty(next, Settings.count).getValue());
            total += (Integer) workingStatCont.getContainerProperty(next, Settings.count).getValue();
        }
        str.append("&emsp;").append(myUI.getMessage(IndigoMessages.Total)).append(": ").append(total);
        workingStatTtlLb.setValue(str.toString());
    }

    private void setEmployeesDataTable(String edu_st_ids) {
        edu_st_ids = edu_st_ids.substring(1, edu_st_ids.length() - 1);
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            employeesDataTable.setContainerDataSource(
                    dbe.execSQL(myUI, myUI.getUser().getSchool().getId(), edu_st_ids, workingStatCont,
                            currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr),
                            (currentUser.isPermitted(Settings.cnEmployeeDefinitionView
                                    + ":" + Settings.prmViewAllEmployees) ? 0 : myUI.getUser().getBranch_id()), (isMyProfile ? employeeID : 0)));
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        employeesDataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
    }

    private void buildLeftLayout() {
        leftLay = new VerticalLayout();
        leftLay.setSpacing(true);
        leftLay.setMargin(true);
        leftLay.setWidth(Settings.PERCENTS100);

        buildPhotoLayout();
        buildFormLayout();
        buildInfoLayout();

        Label lb = new Label(myUI.getMessage(IndigoMessages.MainInfo));
        lb.setStyleName(ValoTheme.LABEL_LARGE);
        lb.setSizeUndefined();

        leftLay.addComponent(buttonsLay);
        leftLay.addComponent(lb);
        leftLay.setComponentAlignment(lb, Alignment.MIDDLE_CENTER);
        leftLay.addComponent(infoLay);
        leftLay.addComponent(photoEmb);
        leftLay.addComponent(photoUpl);
        leftLay.addComponent(formLay);
    }

    private void buildInfoLayout() {

        infoLay = new VerticalLayout();

        workingStatusLb = new Label();
        workingStatusLb.setContentMode(ContentMode.HTML);
        workingStatusLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        mainPositionLb = new Label();
        mainPositionLb.setContentMode(ContentMode.HTML);
        mainPositionLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        extraPositionsLb = new Label();
        extraPositionsLb.setContentMode(ContentMode.HTML);
        extraPositionsLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        mainBranchLb = new Label();
        mainBranchLb.setContentMode(ContentMode.HTML);
        mainBranchLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        extraBranchesLb = new Label();
        extraBranchesLb.setContentMode(ContentMode.HTML);
        extraBranchesLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        totalHoursLb = new Label();
        totalHoursLb.setContentMode(ContentMode.HTML);
        totalHoursLb.setStyleName(ValoTheme.LABEL_SUCCESS);

        infoLay.addComponent(workingStatusLb);
        infoLay.setComponentAlignment(workingStatusLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(mainPositionLb);
        infoLay.setComponentAlignment(mainPositionLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(extraPositionsLb);
        infoLay.setComponentAlignment(extraPositionsLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(mainBranchLb);
        infoLay.setComponentAlignment(mainBranchLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(extraBranchesLb);
        infoLay.setComponentAlignment(extraBranchesLb, Alignment.BOTTOM_LEFT);
        infoLay.addComponent(totalHoursLb);
        infoLay.setComponentAlignment(totalHoursLb, Alignment.BOTTOM_LEFT);
    }

    private void updateInfoLayout() {
        if (employeesDataTable.getValue() != null) {
            workingStatusLb.setValue("<b>" + myUI.getMessage(IndigoMessages.WorkingStatus) + ": </b>"
                    + employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.WorkingStatus)).getValue());
            mainPositionLb.setValue("<b>" + myUI.getMessage(IndigoMessages.MainPosition) + ": </b>"
                    + employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.MainPosition)).getValue());
            if (employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.ExtraPosition)).getValue() != null) {
                extraPositionsLb.setValue("<b>" + myUI.getMessage(IndigoMessages.ExtraPosition) + ": </b>"
                        + employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.ExtraPosition)).getValue());
            } else {
                extraPositionsLb.setValue("<b>" + myUI.getMessage(IndigoMessages.ExtraPosition) + ": </b>");
            }
            if (employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.MainBranch)).getValue() != null) {
                mainBranchLb.setValue("<b>" + myUI.getMessage(IndigoMessages.MainBranch) + ": </b>"
                        + employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.MainBranch)).getValue());
            } else {
                mainBranchLb.setValue("<b>" + myUI.getMessage(IndigoMessages.MainBranch) + ": </b>");
            }
            if (employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.ExtraBranches)).getValue() != null) {
                extraBranchesLb.setValue("<b>" + myUI.getMessage(IndigoMessages.ExtraBranches) + ": </b>"
                        + employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.ExtraBranches)).getValue());
            } else {
                extraBranchesLb.setValue("<b>" + myUI.getMessage(IndigoMessages.ExtraBranches) + ": </b>");
            }
            totalHoursLb.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalHours) + myUI.getUser().getCurrent_year().getName() + ": </b>"
                    + employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.Hours)).getValue()
                    + ", <b>" + myUI.getMessage(IndigoMessages.ExtraHours) + ": </b>"
                    + employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.ExtraHours)).getValue());
            mainPositionCB.setValue(employeesDataTable.getContainerProperty(employeeID, Settings.position_id).getValue());
            contractCategoryCB.setValue(employeesDataTable.getContainerProperty(employeeID, Settings.salary_category_id).getValue());
        } else {
            workingStatusLb.setValue("<b>" + myUI.getMessage(IndigoMessages.WorkingStatus) + ": </b>");
            mainPositionLb.setValue("<b>" + myUI.getMessage(IndigoMessages.MainPosition) + ": </b>");
            extraPositionsLb.setValue("<b>" + myUI.getMessage(IndigoMessages.ExtraPosition) + ": </b>");
            mainBranchLb.setValue("<b>" + myUI.getMessage(IndigoMessages.MainBranch) + ": </b>");
            extraBranchesLb.setValue("<b>" + myUI.getMessage(IndigoMessages.ExtraBranches) + ": </b>");
            totalHoursLb.setValue("<b>" + myUI.getMessage(IndigoMessages.TotalHours) + myUI.getUser().getCurrent_year().getName() + ": </b>");
            mainPositionCB.setValue(null);
            contractCategoryCB.setValue(((IndexedContainer) contractCategoryCB.getContainerDataSource()).lastItemId());
        }
    }

    private void buildFormLayout() {
        formLay = new FormLayout();
        formLay.setSpacing(false);
        formLay.setMargin(false);

        loginTF = createTextField(null, null, new RegexpValidator("^[1-9][0-9][0-9][0-9][0-9][0-9]$", true,
                myUI.getMessage(IndigoMessages.NotificationWrongValue)), true);
        loginTF.setCaption(myUI.getMessage(IndigoMessages.Id));
        loginTF.addValueChangeListener(this);
        formLay.addComponent(loginTF);

        HorizontalLayout passwordLay = new HorizontalLayout();
        passwordLay.setCaption(myUI.getMessage(IndigoMessages.Password));
        passwordLay.setSpacing(true);
        passwordLay.setWidth(Settings.PERCENTS100);

        passwordTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 20, true), false);
        passwordTF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        passwordLay.addComponent(passwordTF);
        passwordLay.setExpandRatio(passwordTF, 1);

        generateBtn = new Button();
        generateBtn.setDescription(myUI.getMessage(IndigoMessages.GenerateButton));
        generateBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        generateBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        generateBtn.setIcon(FontAwesome.REFRESH);
        generateBtn.addClickListener(this);
        passwordLay.addComponent(generateBtn);
        passwordLay.setComponentAlignment(generateBtn, Alignment.BOTTOM_RIGHT);
        formLay.addComponent(passwordLay);

        surnameTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 100, false), true);
        surnameTF.setCaption(myUI.getMessage(IndigoMessages.LastName));
        formLay.addComponent(surnameTF);

        nameTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 100, false), true);
        nameTF.setCaption(myUI.getMessage(IndigoMessages.FirstName));
        formLay.addComponent(nameTF);

        middleNameTF = createTextField(null, null, new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 100, true), false);
        middleNameTF.setCaption(myUI.getMessage(IndigoMessages.MiddleName));
        formLay.addComponent(middleNameTF);

        birthDateDF = createDateField(today, null, myUI.getMessage(IndigoMessages.DateOfBirth),
                true, Settings.datePattern, Resolution.DAY);
        formLay.addComponent(birthDateDF);

        genderCB = createCombobox(0, null, Settings.dbGender, true);
        genderCB.setCaption(myUI.getMessage(IndigoMessages.Gender));
        formLay.addComponent(genderCB);

        nationalityCB = createCombobox(0, null, Settings.dbNationality, true);
        nationalityCB.setCaption(myUI.getMessage(IndigoMessages.Nationality));
        formLay.addComponent(nationalityCB);

        citizenshipCB = createCombobox(0, null, Settings.dbCountry, true);
        citizenshipCB.setCaption(myUI.getMessage(IndigoMessages.Citizenship));
        formLay.addComponent(citizenshipCB);

        martialStatusCB = createCombobox(0, null, Settings.dbMartialStatus, true);
        martialStatusCB.setCaption(myUI.getMessage(IndigoMessages.MartialStatus));
        martialStatusCB.addValueChangeListener(this);
        formLay.addComponent(martialStatusCB);

        mainPositionCB = new ComboBox(myUI.getMessage(IndigoMessages.MainPosition));
        mainPositionCB.setNullSelectionAllowed(false);
        mainPositionCB.setRequired(true);
        mainPositionCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        mainPositionCB.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        mainPositionCB.setWidth(Settings.PERCENTS100);
        mainPositionCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        mainPositionCB.setFilteringMode(FilteringMode.CONTAINS);
        formLay.addComponent(mainPositionCB);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            mainPositionCB.setContainerDataSource(
                    dbDef.exec_positions_for_select(myUI, currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr)));
            dbDef.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }

        contractCategoryCB = new ComboBox(myUI.getMessage(IndigoMessages.SalaryCategory));
        contractCategoryCB.setNullSelectionAllowed(false);
        contractCategoryCB.setRequired(true);
        contractCategoryCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        contractCategoryCB.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        contractCategoryCB.setWidth(Settings.PERCENTS100);
        contractCategoryCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        contractCategoryCB.setFilteringMode(FilteringMode.CONTAINS);
        contractCategoryCB.setEnabled(currentUser.hasRole(Settings.rnAdmin) || currentUser.hasRole(Settings.rnHr));
        if (!currentUser.isPermitted(Settings.cnEmployeeDefinitionView
                + ":" + Settings.prmContractVisible)) {
            contractCategoryCB.setVisible(false);
        }
        formLay.addComponent(contractCategoryCB);

        try {
            DbSalaryCategories dbCon = new DbSalaryCategories();
            dbCon.connect();
            contractCategoryCB.setContainerDataSource(dbCon.execSQL(myUI, myUI.getUser().getSchool().getId()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        contractCategoryCB.setValue(((IndexedContainer) contractCategoryCB.getContainerDataSource()).lastItemId());
    }


    private void buildPhotoLayout() {
        photoEmb = new Embedded();
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        photoEmb.setHeight("140px");

        photoUpl = createUpload(myUI.getMessage(IndigoMessages.Upload), true);
    }

    private void buildButtonsLayout() {

        buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        modifyBtn = new Button();
        modifyBtn.setEnabled(false);
        modifyBtn.setDescription(myUI.getMessage(IndigoMessages.ModifyButton));
        modifyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        modifyBtn.setIcon(FontAwesome.PENCIL);
        modifyBtn.addClickListener(this);
        buttonsLay.addComponent(modifyBtn);

        createBtn = new Button();
        createBtn.setEnabled(false);
        createBtn.setDescription(myUI.getMessage(IndigoMessages.CreateButton));
        createBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        createBtn.setIcon(FontAwesome.FILE_O);
        createBtn.addClickListener(this);
        buttonsLay.addComponent(createBtn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(IndigoMessages.DeleteButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        buttonsLay.addComponent(deleteBtn);
        if (isMyProfile) {
            createBtn.setVisible(false);
            deleteBtn.setVisible(false);
        }

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(IndigoMessages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        buttonsLay.addComponent(saveBtn);

        cancelBtn = new Button();
        cancelBtn.setDescription(myUI.getMessage(IndigoMessages.CancelButton));
        cancelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cancelBtn.setIcon(FontAwesome.BAN);
        cancelBtn.addClickListener(this);
        buttonsLay.addComponent(cancelBtn);

        cvBtn = new Button();
        cvBtn.setEnabled(false);
        cvBtn.setDescription(myUI.getMessage(IndigoMessages.CvButton));
        cvBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cvBtn.setIcon(FontAwesome.INFO);
        cvBtn.addClickListener(this);
        buttonsLay.addComponent(cvBtn);

    }

    private boolean validate(ComponentContainer layout, boolean isDocumentsRequired) {
        boolean result = true;
        for (Component c : layout) {
            if (isDocumentsRequired && c instanceof Button) {
                if (((Button) c).getData() == null) {
                    return false;
                }
            } else if (c instanceof AbstractField) {
                try {
                    ((AbstractField<?>) c).validate();
                } catch (Exception ex) {
                    //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                    result = false;
                }
            } else if (c instanceof AbstractComponentContainer) {
                if (!validate((AbstractComponentContainer) c, isDocumentsRequired)) {
                    result = false;
                }
            }
        }
        return result;
    }

    private void enableUploads(Table t) {
        if (t.size() > 0) {
            for (Object next : ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds()) {
                (((HorizontalLayout) t.getContainerProperty(next,
                        myUI.getMessage(IndigoMessages.Document)).getValue()).getComponent(1)).setEnabled(true);
            }
        }
    }

    private boolean validateTable(Table t, boolean isEmptyAllowed, boolean isDocumentsRequired) {
        if (t.size() == 0 && !isEmptyAllowed) {
            Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                    Notification.Type.WARNING_MESSAGE);
            return false;
        } else {
            for (Object next : ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds()) {
                for (Object next1 : (t.getContainerDataSource()).getContainerPropertyIds()) {
                    Object c = t.getItem(next).getItemProperty(next1).getValue();
                    if (c instanceof AbstractField) {
                        try {
                            ((AbstractField<?>) c).validate();
                        } catch (Exception ex) {
                            //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                            return false;
                        }
                    } else if (c instanceof AbstractComponentContainer) {
                        if (!validate((AbstractComponentContainer) c, isDocumentsRequired)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean validateMainBranches() {
        int counter = 0;
        for (Object next : ((IndexedContainer) branchesTable
                .getContainerDataSource()).getItemIds()) {
            if (((CheckBox) branchesTable.getItem(next).getItemProperty(
                    myUI.getMessage(IndigoMessages.Main)).getValue()).getValue()) {
                counter++;
            }
            if (counter > 1) {
                return false;
            }
        }
        return true;
    }

    private boolean validateOrdersTable() {
        List<?> list = ((IndexedContainer) ordersTable.getContainerDataSource()).getItemIds();
        ListIterator<?> iter2 = list.listIterator(list.size());
        while (iter2.hasPrevious()) {
            Object next2 = iter2.previous();
            Item lastItem = ordersTable.getItem(next2);
            int hr_order_id = (Integer) ((ComboBox) lastItem.getItemProperty(myUI.getMessage(IndigoMessages.OrderType)).getValue()).getValue();
            Iterator<?> iter = ((IndexedContainer) ordersTable.getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                if (next.equals(next2)) {
                    break;
                }
                if (iter.hasNext()) {
                    if (hr_order_id == 3 || hr_order_id == 1 || hr_order_id == 2 || hr_order_id == 7) {
                        if (hr_order_id != 3 || (Integer) ((ComboBox) ordersCont.getContainerProperty(next,
                                myUI.getMessage(IndigoMessages.OrderType)).getValue()).getValue() == hr_order_id) {
                            if (ordersCont.getContainerProperty(next, myUI.getMessage(IndigoMessages.Details)).getValue() != null
                                    && lastItem.getItemProperty(myUI.getMessage(IndigoMessages.Details)).getValue() != null
                                    && ((ComboBox) ordersCont.getContainerProperty(next, myUI.getMessage(IndigoMessages.Details)).getValue()).getValue().equals(((ComboBox) lastItem.getItemProperty(myUI.getMessage(IndigoMessages.Details)).getValue()).getValue())) {
                                if (((DateField) ordersCont.getContainerProperty(next, myUI.getMessage(IndigoMessages.TillDate)).getValue()).getValue() == null
                                        || ((DateField) lastItem.getItemProperty(myUI.getMessage(IndigoMessages.FromDate)).getValue()).getValue().before(
                                        ((DateField) ordersCont.getContainerProperty(next, myUI.getMessage(IndigoMessages.TillDate)).getValue()).getValue())) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void buildUploadWindow() {
        uploadProgressBar = new ProgressBar();
        uploadProgressBar.setWidth("90%");

        statusWindow = new Window(myUI.getMessage(IndigoMessages.UploadStatus));
        statusWindow.setResizable(false);
        statusWindow.setDraggable(false);
        statusWindow.setModal(true);
        statusWindow.setWidth("20%");
        statusWindow.addStyleName(ValoTheme.WINDOW_BOTTOM_TOOLBAR);

        final HorizontalLayout l = new HorizontalLayout();
        l.setSpacing(true);
        l.setWidth(Settings.PERCENTS100);
        l.setMargin(true);
        statusWindow.setContent(l);

        cancelButton = new Button();
        cancelButton.setImmediate(true);
        cancelButton.addClickListener(this);
        cancelButton.setId(Settings.cancel_upload_button);
        cancelButton.setVisible(false);
        cancelButton.setIcon(FontAwesome.CLOSE);
        cancelButton.setStyleName(ValoTheme.BUTTON_TINY);
        cancelButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        l.addComponent(cancelButton);
        l.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);

        uploadProgressBar.setCaption(myUI.getMessage(IndigoMessages.Progress));
        uploadProgressBar.setVisible(false);
        l.addComponent(uploadProgressBar);
        l.setExpandRatio(uploadProgressBar, 1);
    }

    public Upload createUpload(String caption, boolean isPhoto) {
        Upload upl = new Upload(null, new MyReceiver(isPhoto));
        upl.setImmediate(true);
        if (!isPhoto) {
            upl.setStyleName("with-icon");
        }
        upl.addStyleName(ValoTheme.BUTTON_TINY);
        upl.setButtonCaption(caption);
        upl.addStartedListener((Upload.StartedListener) event -> {

            buildUploadWindow();
            myUI.addWindow(statusWindow);
            statusWindow.setClosable(false);

            uploadProgressBar.setValue(0f);
            uploadProgressBar.setVisible(true);
            UI.getCurrent().setPollInterval(500);
            cancelButton.setVisible(true);
            cancelButton.addClickListener((Button.ClickListener) clickEvent -> {
                try {
                    upl.interruptUpload();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            });
        });

        upl.addProgressListener((Upload.ProgressListener) (readBytes, contentLength) -> {
            // This method gets called several times during the update
            if ((isPhoto && !mimeType.equals("image/jpeg")) || (!mimeType.equals("image/jpeg") && !mimeType.equals("application/pdf"))) {
                try {
                    upl.interruptUpload();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
                if (isPhoto) {
                    photoName = null;
                    Notification.show(myUI.getMessage(IndigoMessages.OnlyJpg), Notification.Type.WARNING_MESSAGE);
                } else {
                    fileName = null;
                    Button b = (Button) upl.getData();
                    b.setEnabled(false);
                    b.setData(null);
                    Notification.show(myUI.getMessage(IndigoMessages.OnlyJpgOrPdf), Notification.Type.WARNING_MESSAGE);
                }
            } else if (contentLength >= 15000000) {
                try {
                    upl.interruptUpload();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
                photoName = null;
                fileName = null;
                Button b = (Button) upl.getData();
                b.setEnabled(false);
                b.setData(null);
                Notification.show(myUI.getMessage(IndigoMessages.Maxsize), Notification.Type.WARNING_MESSAGE);
            } else {
                uploadProgressBar.setValue(readBytes / (float) contentLength);
            }
        });

        upl.addSucceededListener((Upload.SucceededListener) event -> {
            // This method gets called when the upload finished successfully
            if (isPhoto) {
                try {
                    Thumbnails.of(myFile).size(200, 200).toFile(myFile);
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
                photoEmb.setSource(new FileResource(myFile));
            } else {
                Button b = (Button) upl.getData();
                Attachment attachment = null;
                try {
                    attachment = new Attachment();
                    attachment.setUnique_name(myFile.getName());
                    attachment.setExtension(mimeType);
                    attachment.setName(fileName);
                    DbAttachment dbCon = new DbAttachment();
                    dbCon.connect();
                    int id = dbCon.exec_insert(attachment);
                    if (id != 0) {
                        attachment.setId(id);
                    }
                    dbCon.close();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
                b.setData(attachment);
            }
            Notification.show(myUI.getMessage(IndigoMessages.UploadedSuccessfully),
                    Notification.Type.TRAY_NOTIFICATION);
        });

        upl.addFailedListener((Upload.FailedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
            Notification.show(myUI.getMessage(IndigoMessages.UploadFailed), Notification.Type.ERROR_MESSAGE);
            try {
                myFile.delete();
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        });

        upl.addFinishedListener((Upload.FinishedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
        });
        return upl;
    }

    private void clearEmployeeFields(boolean generateNewLogin) {
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (generateNewLogin) {
                loginTF.setValue(dbd.execSQL_login() + "");
            } else {
                loginTF.setValue("");
            }
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        passwordTF.setValue("");
        nameTF.setValue("");
        surnameTF.setValue("");
        middleNameTF.setValue("");
        genderCB.setValue(null);
        birthDateDF.setValue(null);
        nationalityCB.setValue(null);
        citizenshipCB.setValue(null);
        martialStatusCB.setValue(null);
        mainPositionCB.setValue(null);
        contractCategoryCB.setValue(((IndexedContainer) contractCategoryCB.getContainerDataSource()).lastItemId());
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
        photoName = null;
    }

    private void clearContactFields() {
        phonesTable.removeAllItems();
        noPhonesCkb.setEnabled(true);
        addressTA.setValue("");
        emailTF.setValue("");
        birthPlaceTF.setValue("");
        passportTF.setValue("");
        innTF.setValue("");
        passportGivenTf.setValue("");
        passportDateDF.setValue(null);
    }

    private void clearSpouseFields() {
        childrenTable.removeAllItems();
        noChildrenCkb.setEnabled(true);
        spouseEducationTable.removeAllItems();
        noSpouseEducationCkb.setEnabled(true);
        spouseWorkPlacesTable.removeAllItems();
        noSpouseWorkPlacesCkb.setEnabled(true);
        spouseHealthCB.setValue(null);
        spouseFullNameTF.setValue("");
        spousePhoneTF.setValue("");
        spouseHealthNotesTF.setValue("");
    }

    private void clearExtraInfoFields() {
        healthCB.setValue(null);
        hobbiesTF.setValue("");
        fobbiesTF.setValue("");
        healthNotesTA.setValue("");
        shortNotesTA.setValue("");
    }

    private void clearGradSchoolFields() {
        gradSchoolStartDF.setValue(null);
        gradSchoolEndDF.setValue(null);
        gradSchoolCB.setValue(null);
    }

    private void clearAchievementsFields() {
        languagesTable.removeAllItems();
        noLanguagesCkb.setEnabled(true);
        seminarsTable.removeAllItems();
        noSeminarsCkb.setEnabled(true);
        certificatesTable.removeAllItems();
        noCertificatesCkb.setEnabled(true);
        examsTable.removeAllItems();
        noExamsCkb.setEnabled(true);
    }

    private void clearSchoolFields() {
        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeLessons)) {
            lessonsTable.removeAllItems();
        }
        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeSupervision)) {
            supervisionTable.removeAllItems();
        }
    }

    private void clearProfFields() {
        noBranchesCkb.setEnabled(true);
        educationTable.removeAllItems();
        noEducationCkb.setEnabled(true);
        workPlacesTable.removeAllItems();
        noWorkPlacesCkb.setEnabled(true);
        branchesTable.removeAllItems();
    }

    private void clearPermissionsTable() {
        if (permissionCont != null) {
            for (int i = 0; i < permissionCont.size(); i++) {
                ((ComboBoxMultiselect) permissionCont.getContainerProperty(permissionCont.getIdByIndex(i),
                        myUI.getMessage(IndigoMessages.Functions)).getValue()).setValue(null);
            }
        }
    }

    private void fillFields() {
        loginTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, myUI.getMessage(IndigoMessages.Id)).getValue().toString());
        nameTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, myUI.getMessage(IndigoMessages.FirstName)).getValue().toString());
        surnameTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, myUI.getMessage(IndigoMessages.LastName)).getValue().toString());
        if (employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, myUI.getMessage(IndigoMessages.MiddleName)).getValue() != null) {
            middleNameTF.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                    employeeID, myUI.getMessage(IndigoMessages.MiddleName)).getValue().toString());
        } else {
            middleNameTF.setValue("");
        }
        genderCB.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, Settings.gender_id).getValue());
        birthDateDF.setValue((Date) employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, myUI.getMessage(IndigoMessages.DateOfBirth)).getValue());
        nationalityCB.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, Settings.nationality_id).getValue());
        citizenshipCB.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, Settings.citizenship_id).getValue());
        martialStatusCB.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, Settings.martial_status_id).getValue());
        mainPositionCB.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, Settings.position_id).getValue());
        contractCategoryCB.setValue(employeesDataTable.getContainerDataSource().getContainerProperty(
                employeeID, Settings.salary_category_id).getValue());
        if (employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.Photo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS_HR
                    + employeesDataTable.getContainerProperty(employeeID,
                    myUI.getMessage(IndigoMessages.Photo)).getValue().toString())));
            photoName = employeesDataTable.getContainerProperty(employeeID,
                    myUI.getMessage(IndigoMessages.Photo)).getValue().toString();
        } else {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
            photoName = null;
        }
        if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()) {
            setPhonesTable();
            setContactFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(contractInfoLay).getComponent()) {
            setContractsTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent()) {
            setChildrenTable();
            setEducationTable(spouseEducationTable, 2);
            setWorkTable(spouseWorkPlacesTable, 2);
            setSpouseFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(extraInfoLay).getComponent()) {
            setQuestioningTable();
            setExtraInfoFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent()) {
            setLanguagesTable();
            setCertificatesTable();
            setSeminarsTable();
            setExamsTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()) {
            setEducationTable(educationTable, 1);
            setWorkTable(workPlacesTable, 1);
            setBranchesTable();
            setGradSchoolFields();
        } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent()) {
            if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeSupervision)) {
                canBeAdvisorCkb.setValue((Boolean) employeesDataTable.getContainerProperty(
                        employeeID, myUI.getMessage(IndigoMessages.CanBeAdvisor)).getValue());
                setSupervisionTable();
            }
            if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeLessons)) {
                setLessonsTable();
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(permissionsLay).getComponent()) {
            if (employeesDataTable.getContainerDataSource().
                    getContainerProperty(employeeID, myUI.getMessage(
                            IndigoMessages.Permissions)).getValue() != null) {
                setPermTable_options(employeesDataTable.getContainerDataSource().
                        getContainerProperty(employeeID, myUI.getMessage(
                                IndigoMessages.Permissions)).getValue().toString());
            } else {
                clearPermissionsTable();
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent()) {
            setOrdersTable();
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == createBtn) {
            isNew = true;
            tabs.setSelectedTab(empSearchLay);
            clearEmployeeFields(true);
            clearContactFields();
            clearSpouseFields();
            clearExtraInfoFields();
            clearGradSchoolFields();
            questioningTable.removeAllItems();
            clearAchievementsFields();
            clearProfFields();
            clearSchoolFields();
            ordersTable.removeAllItems();
            prepareModificationMode();
            passwordTF.setRequired(true);
            mainPositionCB.setEnabled(true);
            loginTF.focus();
        } else if (source == modifyBtn) {
            if (employeeID != 0) {
                isNew = false;
                photoUpl.setEnabled(true);
                prepareModificationMode();
                mainPositionCB.setEnabled(false);
                passwordTF.setRequired(false);
            }
        } else if (source == cvBtn) {
            if (employeeID != 0) {
                Employee employee = new Employee();
                Item item = employeesDataTable.getItem(employeeID);
                employee.setId(employeeID);
                employee.setLogin(item.getItemProperty(myUI.getMessage(IndigoMessages.Id)).getValue().toString());
                employee.setName(item.getItemProperty(myUI.getMessage(IndigoMessages.FirstName)).getValue().toString());
                employee.setSurname(item.getItemProperty(myUI.getMessage(IndigoMessages.LastName)).getValue().toString());
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.MiddleName)).getValue() != null) {
                    employee.setMiddle_name(item.getItemProperty(myUI.getMessage(IndigoMessages.MiddleName)).getValue().toString());
                }
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.Photo)).getValue() != null) {
                    employee.setPhoto(item.getItemProperty(myUI.getMessage(IndigoMessages.Photo)).getValue().toString());
                }
                employee.setBirth_date((Date) item.getItemProperty(myUI.getMessage(IndigoMessages.DateOfBirth)).getValue());

                EmployeeExtraInfo employeeExtraInfo = null;
                try {
                    DbEmployeeExtraInfo dbCon = new DbEmployeeExtraInfo();
                    dbCon.connect();
                    employeeExtraInfo = dbCon.execSQL_for_cv(employeeID);
                    dbCon.close();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
                try {
                    DbEmployeeWork dbCon = new DbEmployeeWork();
                    dbCon.connect();
                    employeeExtraInfo.setWorkExperience(dbCon.execSQL_work_experience(employeeID, 1, false));
                    employeeExtraInfo.setWorkExperienceSapat(dbCon.execSQL_work_experience(employeeID, 1, true));
                    dbCon.close();
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }

                employeeExtraInfo.setMainPosition(item.getItemProperty(myUI.getMessage(IndigoMessages.MainPosition)).getValue().toString());
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraPosition)).getValue() != null) {
                    employeeExtraInfo.setExtraPositions(item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraPosition)).getValue().toString());
                }
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.MainBranch)).getValue() != null) {
                    employeeExtraInfo.setMainBranch(item.getItemProperty(myUI.getMessage(IndigoMessages.MainBranch)).getValue().toString());
                }
                if (item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraBranches)).getValue() != null) {
                    employeeExtraInfo.setExtraBranches(item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraBranches)).getValue().toString());
                }
                employeeExtraInfo.setSchool(myUI.getUser().getSchool().getName_ru());
                employeeExtraInfo.setWorkingStatus(item.getItemProperty(myUI.getMessage(IndigoMessages.WorkingStatus)).getValue().toString());
                employeeExtraInfo.setHours((Integer) item.getItemProperty(myUI.getMessage(IndigoMessages.Hours)).getValue());
                employeeExtraInfo.setExtraHours((Integer) item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraHours)).getValue());
                employeeExtraInfo.setCanBeAdvisor((Boolean) item.getItemProperty(myUI.getMessage(IndigoMessages.CanBeAdvisor)).getValue() ?
                        myUI.getMessage(IndigoMessages.Yes) : myUI.getMessage(IndigoMessages.No));
                myUI.addWindow(new EmployeeCvWindow(myUI, employee, employeeExtraInfo, myUI.getUser().getCurrent_year().getName()));
            }
        } else if (source == cancelBtn) {
            clearEmployeeFields(false);
            clearContactFields();
            clearSpouseFields();
            clearExtraInfoFields();
            clearGradSchoolFields();
            questioningTable.removeAllItems();
            clearAchievementsFields();
            clearProfFields();
            clearSchoolFields();
            ordersTable.removeAllItems();
            if (employeeID != 0) {
                fillFields();
                passwordTF.setRequired(false);
            }
            prepareNormalMode();
            delPhoneIds.clear();
            delContractIds.clear();
            delChildIds.clear();
            delSpouseEducationIds.clear();
            delSpouseWorkIds.clear();
            delLanguagesIds.clear();
            delSeminarsIds.clear();
            delCertificatesIds.clear();
            delExamsIds.clear();
            delEducationIds.clear();
            delWorkPlacesIds.clear();
            delBranchesIds.clear();
            delLessonIds.clear();
            delSupervisionIds.clear();
            delOrderIds.clear();
            fileName = null;
        } else if (source == saveBtn) {
            try {
                if (validate(leftLay, false)) {
                    if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()
                            && (!validateTable(phonesTable, noPhonesCkb.getValue(), false) || !validate(contactInfoLay, false))) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(contractInfoLay).getComponent()
                            && (!validateTable(contractsTable, true, false))) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent()
                            && (!validateTable(childrenTable, noChildrenCkb.getValue(), false)
                            || !validateTable(spouseEducationTable, noSpouseEducationCkb.getValue(), false)
                            || !validateTable(spouseWorkPlacesTable, noSpouseWorkPlacesCkb.getValue(), false)
                            || !validate(familyInfoLay, false))) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(extraInfoLay).getComponent()
                            && (!validateTable(questioningTable, true, false) || !validate(extraInfoLay, false))) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent()
                            && (!validateTable(languagesTable, noLanguagesCkb.getValue(), false)
                            || !validateTable(examsTable, noExamsCkb.getValue(), true)
                            || !validateTable(certificatesTable, noCertificatesCkb.getValue(), true)
                            || !validateTable(seminarsTable, noSeminarsCkb.getValue(), false))) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()
                            && !validateMainBranches()) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationOnlyOneMain),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()
                            && (!validateTable(educationTable, noEducationCkb.getValue(), true)
                            || !validateTable(workPlacesTable, noWorkPlacesCkb.getValue(), false)
                            || !validateTable(branchesTable, noBranchesCkb.getValue(), false)
                            || !validate(profInfoLay, false))) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent()
                            && currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":"
                            + Settings.prmOrganizeLessons) && !validateTable(lessonsTable, !canBeAdvisorCkb.getValue(), false)) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent()
                            && (!validateTable(ordersTable, true, false) || !validateOrdersTable())) {
                        Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                    } else {
                        DbEmployee dbe = new DbEmployee();
                        dbe.connect();
                        if (isNew) {
                            Employee employee = getEmployee(0);
                            int id = dbe.exec_insert(employee);
                            if (id != 0) {
                                EmployeeOrder eo = new EmployeeOrder();
                                eo.setOrder_id(1);
                                eo.setEmployee_id(id);
                                eo.setSchool_id(myUI.getUser().getSchool().getId());
                                eo.setPosition_id((Integer) mainPositionCB.getValue());
                                eo.setM_employee_id(myUI.getUser().getId());
                                eo.setFrom_date(today);
                                eo.setCan_not_delete(1);
                                insertEmployeeOrder(eo);
                                String roleName = loginTF.getValue();
                                List<String> extra_position_ids = null;
                                if (employeesDataTable.getContainerProperty(employeeID, Settings.extra_position_ids) != null
                                        && employeesDataTable.getContainerProperty(employeeID, Settings.extra_position_ids).getValue() != null) {
                                    extra_position_ids = Arrays.asList(employeesDataTable.getContainerProperty(employeeID,
                                            Settings.extra_position_ids).getValue().toString().split(", "));
                                }
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() == 5
                                        || (extra_position_ids != null && extra_position_ids.contains("5"))) {
                                    roleName = Settings.rnAdmin;
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() == 25
                                        || (extra_position_ids != null && extra_position_ids.contains("25"))) {
                                    roleName = Settings.rnHr;
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() == 115
                                        || (extra_position_ids != null && extra_position_ids.contains("115"))) {
                                    roleName = Settings.rnSapatSecretary;
                                }
                                insertLoginRoleName(loginTF.getValue(), roleName);
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 5
                                        && (extra_position_ids == null || !extra_position_ids.contains("5"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 25
                                        && (extra_position_ids == null || !extra_position_ids.contains("25"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 115
                                        && (extra_position_ids == null || !extra_position_ids.contains("115"))
                                        && mainPositionCB.getContainerProperty(mainPositionCB.getValue(),
                                        myUI.getMessage(IndigoMessages.Permissions)).getValue() != null) {
                                    insertPermissions(loginTF.getValue(),
                                            mainPositionCB.getContainerProperty(mainPositionCB.getValue(),
                                                    myUI.getMessage(IndigoMessages.Permissions)).getValue().toString());
                                }
                                DbAccCategory dba = new DbAccCategory();
                                dba.connect();
                                AccCategory ac = new AccCategory();
                                ac.setEmployee_id(id);
                                ac.setName(employee.getSurname() + " " + employee.getName());
                                ac.setStatus_id(2);
                                ac.setType_id(2);
                                ac.setSchool_id(myUI.getUser().getSchool().getId());
                                ac.setCode(employee.getLogin());
                                ac.setParent_code(contractCategoryCB.getContainerProperty(contractCategoryCB.getValue(),
                                        myUI.getMessage(IndigoMessages.Code)).getValue().toString());
                                ac.setParent_id((Integer) contractCategoryCB.getValue());
                                ac.setModified_employee_id(myUI.getUser().getId());
                                int acc_id = dba.exec_insert(ac);
                                dba.close();
                                addDataContainerItem(id, acc_id);
                                Notification.show(myUI.getMessage(IndigoMessages.ValueSaved), Notification.Type.HUMANIZED_MESSAGE);
                                prepareNormalMode();
                                passwordTF.setValue("");
                                workingStatCont.getContainerProperty(2, Settings.count)
                                        .setValue(((Integer) workingStatCont.getContainerProperty(2, Settings.count).getValue()) + 1);
                                repaint();
                            } else {
                                Notification.show(myUI.getMessage(IndigoMessages.CanNotSaveIdNumber), Notification.Type.WARNING_MESSAGE);
                                prepareModificationMode();
                            }
                        } else {
                            String oldLogin = employeesDataTable.getContainerDataSource()
                                    .getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.Id)).getValue().toString();
                            int status = 0;
                            Employee e = getEmployee((Integer) employeesDataTable.getContainerProperty(employeeID, Settings.id).getValue());
                            try {
                                status = dbe.exec_update(e, checkPassword());
                            } catch (Exception ex) {
                                logger.error(ex);
                                ex.printStackTrace();
                            }
                            DbAccCategory dba = new DbAccCategory();
                            dba.connect();
                            AccCategory ac = new AccCategory();
                            ac.setId((Integer) employeesDataTable.getContainerProperty(employeeID, Settings.acc_category_id).getValue());
                            ac.setName(e.getSurname() + " " + e.getName());
                            ac.setStatus_id(2);
                            ac.setCode(e.getLogin());
                            ac.setParent_code(contractCategoryCB.getContainerProperty(contractCategoryCB.getValue(),
                                    myUI.getMessage(IndigoMessages.Code)).getValue().toString());
                            ac.setParent_id((Integer) contractCategoryCB.getValue());
                            ac.setModified_employee_id(myUI.getUser().getId());
                            dba.exec_update(ac);
                            dba.close();
                            if (status != 0) {
                                updateDataContainer();
                                if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()) {
                                    insertPhones(employeeID);
                                    insertEmployeeContact(getEmployeeContact(employeeID));
                                    setPhonesTable();
                                    setContactFields();
                                } else if (tabs.getSelectedTab() == tabs.getTab(contractInfoLay).getComponent()) {
                                    insertContracts(employeeID);
                                    setContractsTable();
                                } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent()) {
                                    insertChildren(employeeID);
                                    insertEducation(employeeID, 2, spouseEducationTable, delSpouseEducationIds);
                                    insertWorkPlaces(employeeID, 2, spouseWorkPlacesTable, delSpouseWorkIds);
                                    insertEmployeeSpouse(getEmployeeSpouse(employeeID));
                                    setChildrenTable();
                                    setEducationTable(spouseEducationTable, 2);
                                    setWorkTable(spouseWorkPlacesTable, 2);
                                    setSpouseFields();
                                } else if (tabs.getSelectedTab() == tabs.getTab(extraInfoLay).getComponent()) {
                                    insertQuestioning(employeeID);
                                    insertEmployeeExtraInfo(getEmployeeExtraInfo(employeeID));
                                    setQuestioningTable();
                                    setExtraInfoFields();
                                } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent()) {
                                    insertLanguages(employeeID);
                                    insertCertificates(employeeID);
                                    insertSeminars(employeeID);
                                    insertExams(employeeID);
                                    setLanguagesTable();
                                    setCertificatesTable();
                                    setSeminarsTable();
                                    setExamsTable();
                                    fileName = null;
                                } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent()) {
                                    insertEducation(employeeID, 1, educationTable, delEducationIds);
                                    insertWorkPlaces(employeeID, 1, workPlacesTable, delWorkPlacesIds);
                                    insertBranches(employeeID);
                                    insertEmployeeGraduationSchool(getEmployeeGradSchool(employeeID));
                                    setEducationTable(educationTable, 1);
                                    setWorkTable(workPlacesTable, 1);
                                    setBranchesTable();
                                    setGradSchoolFields();
                                    updateInfoLayout();
                                    fileName = null;
                                } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent()) {
                                    if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeSupervision)) {
                                        dbe.exec_update(employeeID, canBeAdvisorCkb.getValue());
                                        employeesDataTable.getContainerProperty(employeeID,
                                                myUI.getMessage(IndigoMessages.CanBeAdvisor)).setValue(canBeAdvisorCkb.getValue());
                                        insertSupervision(employeeID);
                                        setSupervisionTable();
                                    }
                                    if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.prmOrganizeLessons)) {
                                        insertLessons(employeeID);
                                        updateInfoLayout();
                                        setLessonsTable();
                                    }
                                } else if (tabs.getSelectedTab() == tabs.getTab(permissionsLay).getComponent()) {
                                    dbe.exec_delete_perm(oldLogin);
                                    List<String> extra_position_ids = null;
                                    if (employeesDataTable.getContainerProperty(employeeID, Settings.extra_position_ids).getValue() != null) {
                                        extra_position_ids = Arrays.asList(employeesDataTable.getContainerProperty(employeeID,
                                                Settings.extra_position_ids).getValue().toString().split(", "));
                                    }
                                    if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 5
                                            && (extra_position_ids == null || !extra_position_ids.contains("5"))
                                            && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 25
                                            && (extra_position_ids == null || !extra_position_ids.contains("25"))
                                            && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 115
                                            && (extra_position_ids == null || !extra_position_ids.contains("115"))) {
                                        insertPermissions(loginTF.getValue());
                                    }
                                    if (employeesDataTable.getContainerDataSource().
                                            getContainerProperty(employeeID, myUI.getMessage(
                                                    IndigoMessages.Permissions)).getValue() != null) {
                                        setPermTable_options(employeesDataTable.getContainerDataSource().
                                                getContainerProperty(employeeID, myUI.getMessage(
                                                        IndigoMessages.Permissions)).getValue().toString());
                                    } else {
                                        clearPermissionsTable();
                                    }
                                } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent()) {
                                    insertOrders(employeeID);
                                    setOrdersTable();
                                    updateInfoLayout();
                                }
                                String roleName = loginTF.getValue();
                                List<String> extra_position_ids = null;
                                if (employeesDataTable.getContainerProperty(employeeID, Settings.extra_position_ids).getValue() != null) {
                                    extra_position_ids = Arrays.asList(employeesDataTable.getContainerProperty(employeeID,
                                            Settings.extra_position_ids).getValue().toString().split(", "));
                                }
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() == 5
                                        || (extra_position_ids != null && extra_position_ids.contains("5"))) {
                                    roleName = Settings.rnAdmin;
                                    dbe.exec_delete_perm(oldLogin);
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() == 25
                                        || (extra_position_ids != null && extra_position_ids.contains("25"))) {
                                    roleName = Settings.rnHr;
                                    dbe.exec_delete_perm(oldLogin);
                                } else if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() == 115
                                        || (extra_position_ids != null && extra_position_ids.contains("115"))) {
                                    roleName = Settings.rnSapatSecretary;
                                    dbe.exec_delete_perm(oldLogin);
                                }
                                dbe.exec_update_role(oldLogin, loginTF.getValue(), roleName);
                                if ((Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 5
                                        && (extra_position_ids == null || !extra_position_ids.contains("5"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 25
                                        && (extra_position_ids == null || !extra_position_ids.contains("25"))
                                        && (Integer) mainPositionCB.getContainerProperty(mainPositionCB.getValue(), Settings.position_id).getValue() != 115
                                        && (extra_position_ids == null || !extra_position_ids.contains("115"))
                                        && !oldLogin.equals(loginTF.getValue())) {
                                    dbe.exec_update_perm(oldLogin, loginTF.getValue());
                                }
                                passwordTF.setValue("");
                                prepareNormalMode();
                                try {
                                    DbDefinition dbCon = new DbDefinition();
                                    dbCon.connect();
                                    // dbCon.exec_delete_not_referenced(Settings.dbAttachmentsTable);
                                    dbCon.close();
                                    if (isMyProfile || myUI.getUser().getId() == employeeID) {
                                        DbEmployeeCompleteness dbc = new DbEmployeeCompleteness();
                                        dbc.connect();
                                        dbc.exec_update_modification_date(employeeID);
                                        dbc.close();
                                    }
                                } catch (Exception ex) {
                                    logger.error(ex);
                                    logger.catching(ex);
                                }
                                Notification.show(myUI.getMessage(IndigoMessages.ValueSaved),
                                        Notification.Type.HUMANIZED_MESSAGE);
                            } else {
                                Notification.show(myUI.getMessage(IndigoMessages.ValueCanNotBeSaved),
                                        Notification.Type.WARNING_MESSAGE);
                            }
                        }
                        dbe.close();
                    }
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                logger.error(ex);
                logger.catching(ex);
            }
        } else if (source == deleteBtn) {
            if (employeeID != 0) {
                ConfirmDialog.show(myUI, myUI.getMessage(IndigoMessages.Question),
                        myUI.getMessage(IndigoMessages.ConfirmStudentDeletion)
                                + " " + employeesDataTable.getContainerProperty(employeeID,
                                myUI.getMessage(IndigoMessages.FirstName)).getValue().toString()
                                + " " + employeesDataTable.getContainerProperty(employeeID,
                                myUI.getMessage(IndigoMessages.LastName)).getValue().toString()
                                + "?",
                        myUI.getMessage(IndigoMessages.Yes),
                        myUI.getMessage(IndigoMessages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete();
                            }
                        });
            }
        } else if (source == generateBtn) {
            GenerateRandomString gns = new GenerateRandomString();
            String newPass = gns.getAlphaNumeric(8);
            passwordTF.setValue(newPass);
        } else if (source == plusPhonesButton) {
            addPhonesItem();
        } else if (source == plusContractButton) {
            addContractItem();
        } else if (source == plusChildButton) {
            addChildItem();
        } else if (source == plusSpouseEducationButton) {
            addEducationItem(spouseEducationTable, 2);
        } else if (source == plusSpouseWorkPlacesButton) {
            addWorkItem(spouseWorkPlacesTable, 2);
        } else if (source == plusEducationButton) {
            addEducationItem(educationTable, 1);
        } else if (source == plusWorkPlaceButton) {
            addWorkItem(workPlacesTable, 1);
        } else if (source == plusLanguageButton) {
            addLanguageItem();
        } else if (source == plusCertificateButton) {
            addCertificateItem();
        } else if (source == plusSeminarButton) {
            addSeminarItem();
        } else if (source == plusExamButton) {
            addExamItem();
        } else if (source == plusBranchButton) {
            addBranchItem();
        } else if (source == plusLessonsButton) {
            addLessonItem();
        } else if (source == plusSupervisionButton) {
            addSupervisionItem();
        } else if (source == printContractBtn) {
            EmployeeInfoPdf employeeInfo = new EmployeeInfoPdf();
            try {
                DbEmployeeContact dbec = new DbEmployeeContact();
                dbec.connect();
                employeeInfo.setContact(dbec.execSQL(employeeID));
                dbec.close();
            } catch (Exception ex) {
                logger.error(ex);
                logger.catching(ex);
            }
            int type_id = (Integer) ((ComboBox) contractsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(IndigoMessages.AgreementType)).getValue()).getValue();
            if (employeeInfo.getContact() == null || employeeInfo.getContact().getPassport() == null ||
                    employeeInfo.getContact().getPassportGiven() == null ||
                    employeeInfo.getContact().getPassportDate() == null) {
                Notification.show(myUI.getMessage(IndigoMessages.NotificationNoPassportInfo),
                        Notification.Type.WARNING_MESSAGE);
            } else if (!validate(contractExtraInfoLay, false)) {
                Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
            } else if (type_id == 3 && employeesDataTable.getContainerProperty(employeeID,
                    myUI.getMessage(IndigoMessages.MainBranch)).getValue() == null) {
                Notification.show(myUI.getMessage(IndigoMessages.NotificationNoBranchInfo),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                employeeInfo.setContract(saveContractExtraInfo(source.getData(), new EmployeeContract()));
                employeeInfo.getContract().setCreationDate(((DateField) contractsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(IndigoMessages.CreationDate)).getValue()).getValue());
                employeeInfo.getContract().setFromDate(((DateField) contractsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(IndigoMessages.Start)).getValue()).getValue());
                employeeInfo.getContract().setTillDate(((DateField) contractsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(IndigoMessages.End)).getValue()).getValue());
                employeeInfo.getContract().setSalary((((TextField) contractsTable.getItem(source.getData()).getItemProperty(
                        myUI.getMessage(IndigoMessages.SalaryAmount)).getValue()).getValue()));
                employeeInfo.setEmployeeName(nameTF.getValue());
                employeeInfo.setEmployeeSurname(surnameTF.getValue());
                employeeInfo.setEmployeeMiddleName(middleNameTF.getValue());
                employeeInfo.setEmployeePosition(employeesDataTable.getContainerProperty(employeeID,
                        myUI.getMessage(IndigoMessages.MainPosition)).getValue().toString());
                if (type_id == 3) {
                    employeeInfo.setEmployeeBranch(employeesDataTable.getContainerProperty(employeeID,
                            myUI.getMessage(IndigoMessages.MainBranch)).getValue().toString());
                }
                try {
                    DbEmployee dbEmployee = new DbEmployee();
                    dbEmployee.connect();
                    employeeInfo.setDirector(dbEmployee.exec_by_position_id(1, myUI.getUser().getSchool().getId()));
                    dbEmployee.close();
                    DbSchool dbSchool = new DbSchool();
                    dbSchool.connect();
                    employeeInfo.setSchool(dbSchool.execSchool(myUI.getUser().getSchool().getId()));
                    dbSchool.close();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
                switch (type_id) {
                    case 1:
                        new ContractTechnicalStuffPdf(myUI, employeeInfo);
                        break;
                    case 2:
                        new ContractAdministrativeStuffPdf(myUI, employeeInfo);
                        break;
                    case 3:
                        new ContractAcademicStuffPdf(myUI, employeeInfo);
                        break;
                    case 4:
                        new ServiceAgreementAcademicStuffPdf(myUI, employeeInfo);
                        break;
                    case 5:
                        new ServiceAgreementTechnicalStuffPdf(myUI, employeeInfo);
                        break;
                }
            }
        } else if (source == plusOrdersButton) {
            Object last_id = ((IndexedContainer) ordersTable.getContainerDataSource()).lastItemId();
            if (!(ordersTable.getContainerDataSource()).getItem(last_id).getItemProperty(
                    Settings.crud_status).getValue().equals(myUI.getMessage(IndigoMessages.Insert))
                    && (Integer) ((ComboBox) (ordersTable.getContainerDataSource()).getItem(last_id).getItemProperty(
                    myUI.getMessage(IndigoMessages.OrderType)).getValue()).getValue() != 5) {
                addOrderItem();
            } else {
                Notification.show(myUI.getMessage(IndigoMessages.CannotInsertOrder),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(contactInfoLay).getComponent()) {
            delPhoneIds.add(source.getData().toString());
            phonesTable.removeItem(event.getButton().getData().toString());
            if (phonesTable.size() == 0) {
                noPhonesCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(contractInfoLay).getComponent()) {
            if (source.getDescription().equals(myUI.getMessage(IndigoMessages.DeleteButton))) {
                delContractIds.add(source.getData().toString());
                contractsTable.removeItem(event.getButton().getData().toString());
            } else if (source.getDescription().equals(myUI.getMessage(IndigoMessages.Print)) &&
                    (printContractBtn == null || !Objects.equals(source.getData(), printContractBtn.getData()))) {
                int type_id = (Integer) ((ComboBox) contractsTable.getContainerProperty(source.getData(),
                        myUI.getMessage(IndigoMessages.AgreementType)).getValue()).getValue();
                buildContractExtraInfoLayout(source.getData(), type_id);
                printContractBtn.setData(source.getData());
                if (type_id == 2 || type_id == 3) {
                    printContractBtn.click();
                    ((PopupButton) source).setPopupVisible(false);
                }
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeChildren)) {
            delChildIds.add(source.getData().toString());
            childrenTable.removeItem(event.getButton().getData().toString());
            if (childrenTable.size() == 0) {
                noChildrenCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeEducation)) {
            EmployeeEducation employeeEducation = new EmployeeEducation();
            employeeEducation.setIdStr(source.getData().toString());
            //delEducationIds.add(employeeEducation);
            delSpouseEducationIds.add(employeeEducation);
            spouseEducationTable.removeItem(event.getButton().getData().toString());
            if (spouseEducationTable.size() == 0) {
                noSpouseEducationCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(familyInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeWork)) {
            delSpouseWorkIds.add(source.getData().toString());
            spouseWorkPlacesTable.removeItem(event.getButton().getData().toString());
            if (spouseWorkPlacesTable.size() == 0) {
                noSpouseWorkPlacesCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeExams)) {
            EmployeeExam employeeExam = new EmployeeExam();
            employeeExam.setIdStr(source.getData().toString());
            Button b = (Button) ((HorizontalLayout) examsTable.getContainerProperty(employeeExam.getIdStr(),
                    myUI.getMessage(IndigoMessages.Document)).getValue()).getComponent(0);
            if (b.getData() != null) {
                employeeExam.setAttachmentUniqueName(((Attachment) b.getData()).getUnique_name());
            }
            delExamsIds.add(employeeExam);
            examsTable.removeItem(event.getButton().getData().toString());
            if (examsTable.size() == 0) {
                noExamsCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeLanguage)) {
            delLanguagesIds.add(source.getData().toString());
            languagesTable.removeItem(event.getButton().getData().toString());
            if (languagesTable.size() == 0) {
                noLanguagesCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeSeminar)) {
            delSeminarsIds.add(source.getData().toString());
            seminarsTable.removeItem(event.getButton().getData().toString());
            if (seminarsTable.size() == 0) {
                noSeminarsCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(achievementsInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeCertificate)) {
            EmployeeCertificate employeeCertificate = new EmployeeCertificate();
            employeeCertificate.setIdStr(source.getData().toString());
            Button b = (Button) ((HorizontalLayout) certificatesTable.getContainerProperty(employeeCertificate.getIdStr(),
                    myUI.getMessage(IndigoMessages.Document)).getValue()).getComponent(0);
            if (b.getData() != null) {
                employeeCertificate.setAttachmentUniqueName(((Attachment) b.getData()).getUnique_name());
            }
            delCertificatesIds.add(employeeCertificate);
            certificatesTable.removeItem(event.getButton().getData().toString());
            if (certificatesTable.size() == 0) {
                noCertificatesCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeWork)) {
            delWorkPlacesIds.add(source.getData().toString());
            workPlacesTable.removeItem(event.getButton().getData().toString());
            if (workPlacesTable.size() == 0) {
                noWorkPlacesCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeEducation)) {
            EmployeeEducation employeeEducation = new EmployeeEducation();
            employeeEducation.setIdStr(source.getData().toString());
            Button b = (Button) ((HorizontalLayout) educationTable.getContainerProperty(employeeEducation.getIdStr(),
                    myUI.getMessage(IndigoMessages.Document)).getValue()).getComponent(0);
            if (b.getData() != null) {
                employeeEducation.setAttachmentUniqueName(((Attachment) b.getData()).getUnique_name());
            }
            delEducationIds.add(employeeEducation);
            educationTable.removeItem(event.getButton().getData().toString());
            if (educationTable.size() == 0) {
                noEducationCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(profInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeBranch)) {
            delBranchesIds.add(source.getData().toString());
            branchesTable.removeItem(event.getButton().getData().toString());
            if (branchesTable.size() == 0) {
                noBranchesCkb.setEnabled(true);
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeBranchHours)) {
            delLessonIds.add(source.getData().toString());
            lessonsTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(schoolInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeOrder)) {
            delSupervisionIds.add(source.getData().toString());
            supervisionTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(ordersInfoLay).getComponent() && source.getId().equals(Settings.dbEmployeeOrder)) {
            EmployeeOrder eo = new EmployeeOrder();
            eo.setIdStr(source.getData().toString());
            delOrderIds.add(eo);
            if (eo.getIdStr().startsWith("_")) {
                eo.setOrder_id((Integer) ((ComboBox) ordersTable.getItem(event.getButton().getData().toString().replace("_", "")).getItemProperty(
                        myUI.getMessage(IndigoMessages.OrderType)).getValue()).getValue());
                ordersTable.removeItem(event.getButton().getData().toString().replace("_", ""));
            } else {
                if (((ComboBox) ordersTable.getItem(event.getButton().getData().toString()).getItemProperty(
                        myUI.getMessage(IndigoMessages.OrderType)).getValue()).getValue() != null) {
                    eo.setOrder_id((Integer) ((ComboBox) ordersTable.getItem(event.getButton().getData().toString()).getItemProperty(
                            myUI.getMessage(IndigoMessages.OrderType)).getValue()).getValue());
                }
                ordersTable.removeItem(event.getButton().getData().toString());
            }
        } else if (source.getId().equals(Settings.download_button)) {
            if (downloader == null) {
                downloader = new SimpleFileDownloader();
                addExtension(downloader);
            }
            downloader.setFileDownloadResource(getFileStream(new File(Settings.PATH_TO_UPLOADS_HR
                    + ((Attachment) source.getData()).getUnique_name())));
            downloader.download();
        }
    }

    private StreamResource getFileStream(File inputFile) {

        StreamResource.StreamSource source = () -> {

            InputStream input = null;
            try {
                input = new FileInputStream(inputFile);
            } catch (FileNotFoundException ex) {
                logger.error(ex);
                logger.catching(ex);
            }
            return input;

        };
        return new StreamResource(source, inputFile.getName());
    }

    private void insertContracts(int employee_id) {
        try {
            DbEmployeeContract dbCon = new DbEmployeeContract();
            dbCon.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delContractIds.size() > 0) {
                for (String contractId : delContractIds) {
                    dbd.exec_delete(contractId, Settings.dbEmployeeContract);
                }
            }
            if (contractsTable.getContainerDataSource().size() > 0) {
                for (Object next : contractsTable.getItemIds()) {
                    EmployeeContract ec = new EmployeeContract();
                    ec.setEmployee_id(employee_id);
                    ec.setSalary(((TextField) contractsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.SalaryAmount)).getValue()).getValue());
                    ec.setFromDate(((DateField) contractsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Start)).getValue()).getValue());
                    ec.setTillDate(((DateField) contractsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.End)).getValue()).getValue());
                    ec.setCreationDate(((DateField) contractsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.CreationDate)).getValue()).getValue());
                    ec.setContract_type_id(((Integer) ((ComboBox) contractsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.AgreementType)).getValue()).getValue()));
                    if (contractsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        ec.setId(Integer.parseInt(next.toString()));
                        dbCon.exec_update(ec);
                    } else if (contractsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbCon.exec_insert(ec);
                    }
                }
            }
            delContractIds.clear();
            dbCon.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertPhones(int employee_id) {
        try {
            DbEmployeePhoneNumber dbepn = new DbEmployeePhoneNumber();
            dbepn.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            dbCon.exec_update(employee_id, Settings.columnPhones, !noPhonesCkb.getValue());
            dbCon.close();
            if (delPhoneIds.size() > 0) {
                for (String delPhoneId : delPhoneIds) {
                    dbd.exec_delete(delPhoneId, Settings.dbEmployeePhoneNumber);
                }
            }
            if (phonesTable.getContainerDataSource().size() > 0) {
                for (Object next : phonesTable.getItemIds()) {
                    EmployeePhoneNumber epn = new EmployeePhoneNumber();
                    epn.setEmployee_id(employee_id);
                    epn.setNumber(((TextField) phonesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Number)).getValue()).getValue());
                    epn.setPhone_type_id((Integer) ((ComboBox) phonesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Type)).getValue()).getValue());
                    if (phonesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        epn.setId(Integer.parseInt(next.toString()));
                        dbepn.exec_update(epn);
                    } else if (phonesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbepn.exec_insert(epn);
                    }
                }
            }
            delPhoneIds.clear();
            dbepn.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertChildren(int employee_id) {
        try {
            DbEmployeeChildren dbech = new DbEmployeeChildren();
            dbech.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            dbCon.exec_update(employee_id, Settings.columnChildren, !noChildrenCkb.getValue());
            dbCon.close();
            if (delChildIds.size() > 0) {
                for (String delChildId : delChildIds) {
                    dbd.exec_delete(delChildId, Settings.dbEmployeeChildren);
                }
            }
            if (childrenTable.getContainerDataSource().size() > 0) {
                for (Object next : childrenTable.getItemIds()) {
                    EmployeeChildren ec = new EmployeeChildren();
                    ec.setEmployee_id(employee_id);
                    ec.setFullName(((TextField) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.FullName)).getValue()).getValue());
                    ec.setInstitution(((TextField) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Institution)).getValue()).getValue());
                    ec.setDate_of_birth(((DateField) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.DateOfBirth)).getValue()).getValue());
                    if (((ComboBox) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.EducationStatus)).getValue()).getValue() != null) {
                        ec.setEducation_status_id((Integer) ((ComboBox) childrenTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.EducationStatus)).getValue()).getValue());
                    }
                    ec.setHealth_status_id((Integer) ((ComboBox) childrenTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.HealthStatus)).getValue()).getValue());
                    if (childrenTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        ec.setId(Integer.parseInt(next.toString()));
                        dbech.exec_update(ec);
                    } else if (childrenTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbech.exec_insert(ec);
                    }
                }
            }
            delChildIds.clear();
            dbech.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEducation(int employee_id, int own_id, Table t, ArrayList<?> list) {
        EmployeeEducation ed = null;
        try {
            DbEmployeeEducation dbed = new DbEmployeeEducation();
            dbed.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            if (own_id == 1) {
                dbCon.exec_update(employee_id, Settings.columnEducation, !noEducationCkb.getValue());
            } else {
                dbCon.exec_update(employee_id, Settings.columnSpouseEducation, !noSpouseEducationCkb.getValue());
            }
            dbCon.close();
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    try {
                        if (delEducationIds.get(i).getAttachmentUniqueName() != null) {
                            File f = new File(Settings.PATH_TO_UPLOADS_HR + delEducationIds.get(i).getAttachmentUniqueName());
                            f.delete();
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    dbd.exec_delete(((EmployeeEducation) list.get(i)).getIdStr(), Settings.dbEmployeeEducation);
                }
            }
            if (t.getContainerDataSource().size() > 0) {
                for (Object next : t.getItemIds()) {
                    ed = new EmployeeEducation();
                    ed.setEmployee_id(employee_id);
                    ed.setOwn_id(own_id);
                    ed.setDepartment(((TextField) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Department)).getValue()).getValue());
                    ed.setStart(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Start)).getValue()).getValue());
                    ed.setEnd(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.End)).getValue()).getValue());
                    ed.setCountry_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Country)).getValue()).getValue());
                    ed.setEducation_level_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.EduLevel)).getValue()).getValue());
                    ed.setUniversity_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.University)).getValue()).getValue());
                    if (t.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Document)).getValue() != null) {
                        Button b = (Button) ((HorizontalLayout) t.getContainerProperty(next,
                                myUI.getMessage(IndigoMessages.Document)).getValue()).getComponent(0);
                        if (b.getData() != null) {
                            ed.setAttachment_id(((Attachment) b.getData()).getId());
                        }
                    }
                    if (t.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        ed.setId(Integer.parseInt(next.toString()));
                        dbed.exec_update(ed);
                    } else if (t.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbed.exec_insert(ed);
                    }
                }
            }
            list.clear();
            dbed.close();
            dbd.close();
        } catch (Exception ex) {
            logger.info(ed.getId() + " " + ed.getAttachment_id());
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertWorkPlaces(int employee_id, int own_id, Table t, ArrayList<String> list) {
        try {
            DbEmployeeWork dbew = new DbEmployeeWork();
            dbew.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbc = new DbEmployeeCompleteness();
            dbc.connect();
            if (own_id == 1) {
                dbc.exec_update(employee_id, Settings.columnWork_places, !noWorkPlacesCkb.getValue());
            } else {
                dbc.exec_update(employee_id, Settings.columnSpouseWork_places, !noSpouseWorkPlacesCkb.getValue());
            }
            dbc.close();
            if (list.size() > 0) {
                for (String s : list) {
                    dbd.exec_delete(s, Settings.dbEmployeeWork);
                }
            }
            if (t.getContainerDataSource().size() > 0) {
                for (Object next : t.getItemIds()) {
                    EmployeeWork ew = new EmployeeWork();
                    ew.setEmployee_id(employee_id);
                    ew.setOwn_id(own_id);
                    ew.setWork_place_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.WorkPlace)).getValue()).getValue());
                    ew.setMain_position_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.MainPosition)).getValue()).getValue());
                    if (((ComboBoxMultiselect) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.ExtraPositions)).getValue()).getValue() != null
                            && !((Set<?>) ((ComboBoxMultiselect) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.ExtraPositions)).getValue()).getValue()).isEmpty()) {
                        ew.setExtra_position_ids((Set<?>) ((ComboBoxMultiselect) t.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.ExtraPositions)).getValue()).getValue());
                    } else {
                        ew.setExtra_position_ids(null);
                    }
                    ew.setWorking_status_id((Integer) ((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.WorkingStatus)).getValue()).getValue());
                    ew.setStart(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Start)).getValue()).getValue());
                    ew.setEnd(((DateField) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.End)).getValue()).getValue());
                    ew.setSapat(((CheckBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Sapat)).getValue()).getValue());
                    if (t.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        ew.setId(Integer.parseInt(next.toString()));
                        dbew.exec_update(ew);
                    } else if (t.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        ew.setId(dbew.exec_insert(ew));
                    }
                    DbDefinition dbCon = new DbDefinition();
                    dbCon.connect();
                    dbCon.exec_delete(ew.getId() + "", Settings.dbEmployeeWorkExtraPosition, Settings.employee_work_id);
                    if (ew.getExtra_position_ids() != null && !ew.getExtra_position_ids().isEmpty()) {
                        for (Object o : ew.getExtra_position_ids()) {
                            int position_id = (Integer) o;
                            dbew.exec_insert_extra_position(ew.getId(), position_id);
                        }
                    }
                    dbCon.close();
                }
            }
            list.clear();
            dbew.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertQuestioning(int employee_id) {
        try {
            DbEmployeeQuestion dbeq = new DbEmployeeQuestion();
            dbeq.connect();
            if (questioningTable.getContainerDataSource().size() > 0) {
                for (Object next : questioningTable.getItemIds()) {
                    if (((TextField) questioningTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Answer)).getValue()).getValue() != null
                            && !((TextField) questioningTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Answer)).getValue()).getValue().equals("")) {
                        EmployeeQuestioning eq = new EmployeeQuestioning();
                        eq.setEmployee_id(employee_id);
                        eq.setQuestion_id(Integer.parseInt(next.toString()));
                        eq.setAnswer(((TextField) questioningTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.Answer)).getValue()).getValue());
                        int st = dbeq.exec_insert(eq);
                        if (st == 0) {
                            dbeq.exec_update(eq);
                        }
                    } else {
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        dbd.exec_delete((Integer) questioningTable.getContainerProperty(next, Settings.id).getValue(), Settings.dbEmployeeQuestion);
                        dbd.close();
                    }
                }
            }
            dbeq.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertLanguages(int employee_id) {
        try {
            DbEmployeeLanguage dbel = new DbEmployeeLanguage();
            dbel.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            dbCon.exec_update(employee_id, Settings.columnLanguages, !noLanguagesCkb.getValue());
            dbCon.close();
            if (delLanguagesIds.size() > 0) {
                for (String delLanguagesId : delLanguagesIds) {
                    dbd.exec_delete(delLanguagesId, Settings.dbEmployeeLanguage);
                }
            }
            if (languagesTable.getContainerDataSource().size() > 0) {
                for (Object next : languagesTable.getItemIds()) {
                    EmployeeLanguage el = new EmployeeLanguage();
                    el.setEmployee_id(employee_id);
                    el.setLanguage_id((Integer) ((ComboBox) languagesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Language)).getValue()).getValue());
                    el.setLevel_id((Integer) ((ComboBox) languagesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Level)).getValue()).getValue());
                    if (languagesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        el.setId(Integer.parseInt(next.toString()));
                        dbel.exec_update(el);
                    } else if (languagesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbel.exec_insert(el);
                    }
                }
            }
            delLanguagesIds.clear();
            dbel.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertCertificates(int employee_id) {
        EmployeeCertificate ec = null;
        try {
            DbEmployeeCertificate dbec = new DbEmployeeCertificate();
            dbec.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            dbCon.exec_update(employee_id, Settings.columnCertificates, !noCertificatesCkb.getValue());
            dbCon.close();
            if (delCertificatesIds.size() > 0) {
                for (int i = 0; i < delCertificatesIds.size(); i++) {
                    try {
                        if (delCertificatesIds.get(i).getAttachmentUniqueName() != null) {
                            File f = new File(Settings.PATH_TO_UPLOADS_HR + delCertificatesIds.get(i).getAttachmentUniqueName());
                            f.delete();
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    dbd.exec_delete(delCertificatesIds.get(i).getIdStr(), Settings.dbEmployeeCertificate);
                }
            }
            if (certificatesTable.getContainerDataSource().size() > 0) {
                for (Object next : certificatesTable.getItemIds()) {
                    ec = new EmployeeCertificate();
                    ec.setEmployee_id(employee_id);
                    ec.setNote(((TextField) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Note)).getValue()).getValue());
                    ec.setCertificate_id((Integer) ((ComboBox) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Certificate)).getValue()).getValue());
                    ec.setGiven_by(((TextField) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.GivenBy)).getValue()).getValue());
                    ec.setDate_of_issue(((DateField) certificatesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.IssueDate)).getValue()).getValue());
                    Button b = (Button) ((HorizontalLayout) certificatesTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Document)).getValue()).getComponent(0);
                    if (b.getData() != null) {
                        ec.setAttachment_id(((Attachment) b.getData()).getId());
                    }
                    if (certificatesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        ec.setId(Integer.parseInt(next.toString()));
                        dbec.exec_update(ec);
                    } else if (certificatesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbec.exec_insert(ec);
                    }
                }
            }
            delCertificatesIds.clear();
            dbec.close();
            dbd.close();
        } catch (Exception ex) {
            logger.info(ec.getId() + " " + ec.getAttachment_id());
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertSeminars(int employee_id) {
        try {
            DbEmployeeSeminar dbes = new DbEmployeeSeminar();
            dbes.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            dbCon.exec_update(employee_id, Settings.columnSeminars, !noSeminarsCkb.getValue());
            dbCon.close();
            if (delSeminarsIds.size() > 0) {
                for (String delSeminarsId : delSeminarsIds) {
                    dbd.exec_delete(delSeminarsId, Settings.dbEmployeeSeminar);
                }
            }
            if (seminarsTable.getContainerDataSource().size() > 0) {
                for (Object next : seminarsTable.getItemIds()) {
                    EmployeeSeminar es = new EmployeeSeminar();
                    es.setEmployee_id(employee_id);
                    es.setName(((TextField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Title)).getValue()).getValue());
                    es.setSubject(((TextField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Subject)).getValue()).getValue());
                    es.setNote(((TextField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Note)).getValue()).getValue());
                    es.setDate_of_issue(((DateField) seminarsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.IssueDate)).getValue()).getValue());
                    if (seminarsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        es.setId(Integer.parseInt(next.toString()));
                        dbes.exec_update(es);
                    } else if (seminarsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbes.exec_insert(es);
                    }
                }
            }
            delSeminarsIds.clear();
            dbes.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertExams(int employee_id) {
        EmployeeExam exam = null;
        try {
            DbEmployeeExam dbex = new DbEmployeeExam();
            dbex.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            dbCon.exec_update(employee_id, Settings.columnExams, !noExamsCkb.getValue());
            dbCon.close();
            if (delExamsIds.size() > 0) {
                for (EmployeeExam delExamsId : delExamsIds) {
                    try {
                        if (delExamsId.getAttachmentUniqueName() != null) {
                            File f = new File(Settings.PATH_TO_UPLOADS_HR + delExamsId.getAttachmentUniqueName());
                            f.delete();
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                        logger.catching(ex);
                    }
                    dbd.exec_delete(delExamsId.getIdStr(), Settings.dbEmployeeExams);
                }
            }
            if (examsTable.getContainerDataSource().size() > 0) {
                for (Object next : examsTable.getItemIds()) {
                    exam = new EmployeeExam();
                    exam.setEmployee_id(employee_id);
                    exam.setExam_id((Integer) ((ComboBox) examsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Exam)).getValue()).getValue());
                    exam.setScore(((TextField) examsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Score)).getValue()).getValue());
                    exam.setDate_of_issue(((DateField) examsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.IssueDate)).getValue()).getValue());
                    Button b = (Button) ((HorizontalLayout) examsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Document)).getValue()).getComponent(0);
                    if (b.getData() != null) {
                        exam.setAttachment_id(((Attachment) b.getData()).getId());
                    }
                    if (examsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        exam.setId(Integer.parseInt(next.toString()));
                        dbex.exec_update(exam);
                    } else if (examsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbex.exec_insert(exam);
                    }
                }
            }
            delExamsIds.clear();
            dbex.close();
            dbd.close();
        } catch (Exception ex) {
            logger.info(exam.getId() + " " + exam.getAttachment_id());
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertBranches(int employee_id) {
        try {
            DbEmployeeBranch dbeb = new DbEmployeeBranch();
            dbeb.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            DbEmployeeCompleteness dbCon = new DbEmployeeCompleteness();
            dbCon.connect();
            dbCon.exec_update(employee_id, Settings.columnBranches, !noBranchesCkb.getValue());
            dbCon.close();
            if (delBranchesIds.size() > 0) {
                for (int i = 0; i < delBranchesIds.size(); i++) {
                    dbd.exec_delete(delBranchesIds.get(i), Settings.dbEmployeeBranch);
                }
            }
            if (branchesTable.getContainerDataSource().size() > 0) {
                employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraBranches)).setValue(null);
                for (Object next : branchesTable.getItemIds()) {
                    EmployeeBranch eb = new EmployeeBranch();
                    eb.setEmployee_id(employee_id);
                    eb.setBranch_id((Integer) ((ComboBox) branchesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Branch)).getValue()).getValue());
                    eb.setMain(((CheckBox) branchesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Main)).getValue()).getValue());
                    String str = ((ComboBox) branchesTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Branch)).getValue()).getItemCaption(eb.getBranch_id());
                    if (eb.isMain()) {
                        employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.MainBranch)).setValue(
                                ((ComboBox) branchesTable.getItem(next).getItemProperty(
                                        myUI.getMessage(IndigoMessages.Branch)).getValue()).getItemCaption(eb.getBranch_id()));
                    } else {
                        if (employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraBranches)).getValue() == null) {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraBranches)).setValue(str);
                        } else {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraBranches)).setValue(
                                    employeesDataTable.getContainerProperty(employee_id,
                                            myUI.getMessage(IndigoMessages.ExtraBranches)).getValue().toString() + ", " + str);
                        }
                    }
                    if (branchesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        eb.setId(Integer.parseInt(next.toString()));
                        dbeb.exec_update(eb);
                    } else if (branchesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbeb.exec_insert(eb);
                    }
                }
            }
            delBranchesIds.clear();
            dbeb.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertLessons(int employee_id) {
        try {
            DbEmployeeLessons dbel = new DbEmployeeLessons();
            dbel.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delLessonIds.size() > 0) {
                for (String delLessonId : delLessonIds) {
                    dbd.exec_delete(delLessonId, Settings.dbEmployeeBranchHours);
                }
            }
            if (lessonsTable.getContainerDataSource().size() > 0) {
                Iterator<?> iter = lessonsTable.getItemIds().iterator();
                int hours = 0, extra = 0;
                while (iter.hasNext()) {
                    Object next = iter.next();
                    EmployeeLessons el = new EmployeeLessons();
                    el.setEmployee_id(employee_id);
                    el.setSchool_id(myUI.getUser().getSchool().getId());
                    el.setBranch_id((Integer) ((ComboBox) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Lesson)).getValue()).getValue());
                    el.setClass_number_id((Integer) ((ComboBox) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.ClassName)).getValue()).getValue());
                    el.setYear_id((Integer) ((ComboBox) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.AcademicYear)).getValue()).getValue());
                    el.setHours((Integer) ((TextField) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Hours)).getValue()).getPropertyDataSource().getValue());
                    el.setExtra_hours((Integer) ((TextField) lessonsTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.ExtraHours)).getValue()).getPropertyDataSource().getValue());
                    if (el.getYear_id() == myUI.getUser().getCurrent_year().getId()) {
                        hours += el.getHours();
                        extra += el.getExtra_hours();
                    }
                    if (lessonsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        el.setId(Integer.parseInt(next.toString()));
                        dbel.exec_update(el);
                    } else if (lessonsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbel.exec_insert(el);
                    }
                }
                employeesDataTable.getContainerProperty(employeesDataTable.getValue(), myUI.getMessage(IndigoMessages.Hours)).setValue(hours);
                employeesDataTable.getContainerProperty(employeesDataTable.getValue(), myUI.getMessage(IndigoMessages.ExtraHours)).setValue(extra);
            }
            delLessonIds.clear();
            dbel.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertSupervision(int employee_id) {
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delSupervisionIds.size() > 0) {
                for (String delSupervisionId : delSupervisionIds) {
                    dbd.exec_delete(delSupervisionId, Settings.dbEmployeeOrder);
                }
            }
            if (supervisionTable.getContainerDataSource().size() > 0) {
                for (Object next : supervisionTable.getItemIds()) {
                    EmployeeOrder eo = new EmployeeOrder();
                    eo.setEmployee_id(employee_id);
                    eo.setOrder_id(3);
                    eo.setSchool_id(myUI.getUser().getSchool().getId());
                    eo.setPosition_id((Integer) employeesDataTable.getContainerProperty(employee_id, Settings.position_id).getValue());
                    eo.setClass_name_id((Integer) ((ComboBox) supervisionTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.ClassName)).getValue()).getValue());
                    eo.setFrom_date(((DateField) supervisionTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.FromDate)).getValue()).getValue());
                    if (((DateField) supervisionTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.TillDate)).getValue()).getValue() != null) {
                        eo.setTo_date(((DateField) supervisionTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.TillDate)).getValue()).getValue());
                    }
                    if (((TextField) supervisionTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Note)).getValue()).getValue() != null
                            && !((TextField) supervisionTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Note)).getValue()).getValue().equals("")) {
                        eo.setNote(((TextField) supervisionTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.Note)).getValue()).getValue());
                    }
                    eo.setM_employee_id(myUI.getUser().getId());
                    if (supervisionTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        eo.setId(Integer.parseInt(next.toString()));
                        dbeo.exec_update(eo);
                    } else if (supervisionTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbeo.exec_insert(eo);
                    }
                }
            }
            delSupervisionIds.clear();
            dbeo.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertOrders(int employee_id) {
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            if (delOrderIds.size() > 0) {
                for (EmployeeOrder delOrderId : delOrderIds) {
                    if (delOrderId.getIdStr().startsWith("_")) {
                        employeesDataTable.getContainerProperty(employee_id, Settings.is_modifiable).setValue(false);
                        delOrderId.setIdStr(delOrderId.getIdStr().replace("_", ""));
                    }
                    dbeo.exec_update_before_delete(delOrderId.getIdStr());
                    dbd.exec_delete(delOrderId.getIdStr(), Settings.dbEmployeeOrder);
                    if (delOrderId.getOrder_id() == 6 || delOrderId.getOrder_id() == 5) {
                        DbAccCategory dbAc = new DbAccCategory();
                        dbAc.connect();
                        String namePostfix = Settings.transferred;
                        if (delOrderId.getOrder_id() == 6) {
                            namePostfix = Settings.resigned;
                        }
                        dbAc.exec_update_activity_status((Integer) employeesDataTable.getContainerProperty(
                                employee_id, Settings.acc_category_id).getValue(), 2, namePostfix);
                        dbAc.close();
                    }
                    if (delOrderId.getOrder_id() == 8) {
                        DbDefinition dbCon = new DbDefinition();
                        dbCon.connect();
                        dbCon.exec_delete((Integer) employeesDataTable.getContainerProperty(employee_id,
                                Settings.acc_category_id).getValue(), Settings.dbAcc_category);
                        dbCon.close();
                    }
                }
            }
            if (ordersTable.getContainerDataSource().size() > 0) {
                employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraPosition)).setValue(null);
                employeesDataTable.getContainerProperty(employee_id, Settings.extra_position_ids).setValue(null);
                for (Object next : ordersTable.getItemIds()) {
                    EmployeeOrder eo = new EmployeeOrder();
                    eo.setEmployee_id(employee_id);
                    eo.setOrder_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.OrderType)).getValue()).getValue());
                    eo.setSchool_id(myUI.getUser().getSchool().getId());
                    eo.setPosition_id((Integer) employeesDataTable.getContainerProperty(employee_id, Settings.position_id).getValue());
                    if (eo.getOrder_id() == 5) {
                        eo.setFrom_to_school_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.Details)).getValue()).getValue());
                    } else if (eo.getOrder_id() == 3) {
                        eo.setClass_name_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.Details)).getValue()).getValue());
                    } else if (eo.getOrder_id() == 1 || eo.getOrder_id() == 2 || eo.getOrder_id() == 7) {
                        eo.setPosition_id((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.Details)).getValue()).getValue());
                    }
                    eo.setFrom_date(((DateField) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.FromDate)).getValue()).getValue());
                    boolean isContain = false;
                    for (EmployeeOrder temp : delOrderIds) {
                        if (ordersTable.getItem(next).getItemProperty(
                                Settings.effected_by_id).getValue() != null && ordersTable.getItem(next).getItemProperty(
                                Settings.effected_by_id).getValue().toString().equals(temp.getIdStr())) {
                            isContain = true;
                        }
                    }
                    if (ordersTable.getItem(next).getItemProperty(
                            Settings.effected_by_id).getValue() == null || !isContain) {
                        eo.setTo_date(((DateField) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.TillDate)).getValue()).getValue());
                    }
                    if (((TextField) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Note)).getValue()).getValue() != null
                            && !((TextField) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.Note)).getValue()).getValue().equals("")) {
                        eo.setNote(((TextField) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.Note)).getValue()).getValue());
                    }
                    eo.setM_employee_id(myUI.getUser().getId());
                    if (ordersTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Update))) {
                        eo.setId(Integer.parseInt(next.toString()));
                        dbeo.exec_update(eo);
                    } else if (ordersTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(IndigoMessages.Insert))) {
                        dbeo.exec_insert(eo);
                        if (eo.getOrder_id() == 6) {
                            DbAccCategory dbAc = new DbAccCategory();
                            dbAc.connect();
                            dbAc.exec_update_activity_status((Integer) employeesDataTable.getContainerProperty(employee_id,
                                    Settings.acc_category_id).getValue(), 1, Settings.resigned);
                            dbAc.close();
                        }
                        if (eo.getOrder_id() == 5) {
                            eo.setSchool_id(eo.getFrom_to_school_id());
                            eo.setFrom_to_school_id(myUI.getUser().getSchool().getId());
                            eo.setOrder_id(8);
                            dbeo.exec_insert(eo);
                            eo.setOrder_id(5);
                            DbAccCategory dbAc = new DbAccCategory();
                            dbAc.connect();
                            AccCategory ac = dbAc.exec_sql(employee_id, eo.getFrom_to_school_id(), eo.getSchool_id());
                            ac.setModified_employee_id(myUI.getUser().getId());
                            dbAc.exec_insert(ac);
                            dbAc.exec_update_activity_status((Integer) employeesDataTable.getContainerProperty(employee_id,
                                    Settings.acc_category_id).getValue(), 1, Settings.transferred);
                            dbAc.close();
                        }
                    }
                    if (eo.getOrder_id() == 1) {
                        employeesDataTable.getContainerProperty(employee_id, Settings.position_id).setValue(eo.getPosition_id());
                        employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.MainPosition)).setValue(
                                ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(IndigoMessages.Details)).getValue()).getItemCaption(eo.getPosition_id()));
                    } else if (eo.getOrder_id() == 2) {
                        String str = ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                myUI.getMessage(IndigoMessages.Details)).getValue()).getItemCaption(eo.getPosition_id());
                        String str_ids = eo.getPosition_id() + "";
                        if (employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraPosition)).getValue() == null) {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraPosition)).setValue(str);
                            employeesDataTable.getContainerProperty(employee_id, Settings.extra_position_ids).setValue(str_ids);
                        } else {
                            employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.ExtraPosition)).setValue(
                                    employeesDataTable.getContainerProperty(employee_id,
                                            myUI.getMessage(IndigoMessages.ExtraPosition)).getValue().toString() + ", " + str);
                            employeesDataTable.getContainerProperty(employee_id, Settings.extra_position_ids).setValue(
                                    employeesDataTable.getContainerProperty(employee_id, Settings.extra_position_ids).getValue().toString() + ", " + str_ids);
                        }
                    } else if (eo.getOrder_id() == 5) {
                        employeesDataTable.getContainerProperty(employee_id, Settings.is_modifiable).setValue(false);
                    }
                    if ((Integer) ((ComboBox) ordersTable.getItem(next).getItemProperty(
                            myUI.getMessage(IndigoMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                            Settings.working_status_id).getValue() != 0) {
                        employeesDataTable.getContainerProperty(employee_id, Settings.working_status_id).setValue(
                                ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(IndigoMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                                        Settings.working_status_id).getValue());
                        employeesDataTable.getContainerProperty(employee_id, Settings.visible_hr_orders).setValue(
                                ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(IndigoMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                                        Settings.visible_hr_orders).getValue().toString());
                        employeesDataTable.getContainerProperty(employee_id, myUI.getMessage(IndigoMessages.WorkingStatus)).setValue(
                                ((ComboBox) ordersTable.getItem(next).getItemProperty(
                                        myUI.getMessage(IndigoMessages.OrderType)).getValue()).getContainerProperty(eo.getOrder_id(),
                                        myUI.getMessage(IndigoMessages.WorkingStatus)).getValue().toString());
                    }
                }
            }
            delOrderIds.clear();
            dbeo.close();
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void addContractItem() {
        NATURAL_COL_ORDER_CONTRACTS = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.AgreementType),
                myUI.getMessage(IndigoMessages.SalaryAmount),
                myUI.getMessage(IndigoMessages.CreationDate),
                myUI.getMessage(IndigoMessages.Start),
                myUI.getMessage(IndigoMessages.End)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (contractsTable.getContainerDataSource().size() == 0) {
            contractsTable.setContainerDataSource(prepareContractsContainer());
        }
        Item item;
        item = ((IndexedContainer) contractsTable.getContainerDataSource()).addItemAt(
                contractsTable.getContainerDataSource().size(), id);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, null, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(Settings.button).setValue(hl);
        item.getItemProperty(myUI.getMessage(IndigoMessages.AgreementType)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.AgreementType), Settings.dbContractType, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.SalaryAmount)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.SalaryAmount),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                1, 250, false), true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.CreationDate)).setValue(
                createDateField(today, myUI.getMessage(IndigoMessages.CreationDate), null, true,
                        Settings.datePattern, Resolution.DAY));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Start)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.Start), null, true,
                        Settings.datePattern, Resolution.DAY));
        item.getItemProperty(myUI.getMessage(IndigoMessages.End)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.End), null, true,
                        Settings.datePattern, Resolution.DAY));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        contractsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.AgreementType), 1);
        contractsTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.SalaryAmount), 1);
        contractsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CONTRACTS);
    }

    private void addPhonesItem() {
        noPhonesCkb.setEnabled(false);
        NATURAL_COL_ORDER_PHONES = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.Type),
                myUI.getMessage(IndigoMessages.Number)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (phonesTable.getContainerDataSource().size() == 0) {
            phonesTable.setContainerDataSource(preparePhonesContainer());
        }
        Item item;
        item = ((IndexedContainer) phonesTable.getContainerDataSource()).addItemAt(
                phonesTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, null, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Type)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.Type), Settings.dbPhoneType, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Number)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Number),
                        new RegexpValidator("^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s0-9]*$", true,
                                myUI.getMessage(IndigoMessages.NotificationWrongValue)), true));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        phonesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PHONES);

    }

    private void addChildItem() {
        noChildrenCkb.setEnabled(false);
        NATURAL_COL_ORDER_CHILDREN = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.FullName),
                myUI.getMessage(IndigoMessages.DateOfBirth),
                myUI.getMessage(IndigoMessages.Institution),
                myUI.getMessage(IndigoMessages.EducationStatus),
                myUI.getMessage(IndigoMessages.HealthStatus)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (childrenTable.getContainerDataSource().size() == 0) {
            childrenTable.setContainerDataSource(prepareChildrenContainer());
        }
        Item item;
        item = ((IndexedContainer) childrenTable.getContainerDataSource()).addItemAt(
                childrenTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeChildren, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(IndigoMessages.EducationStatus)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.EducationStatus), Settings.dbHrEducationStatus, false));
        item.getItemProperty(myUI.getMessage(IndigoMessages.HealthStatus)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.HealthStatus), Settings.dbHealthStatus, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.FullName)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.FullName),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Institution)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Institution),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 300, true), false));
        item.getItemProperty(myUI.getMessage(IndigoMessages.DateOfBirth)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.DateOfBirth),
                        null, true, Settings.datePattern, Resolution.DAY));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        childrenTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CHILDREN);
        childrenTable.setPageLength(childrenTable.size());
    }

    private void addEducationItem(final Table t, int own_id) {
        if (own_id == 1) {
            noEducationCkb.setEnabled(false);
            NATURAL_COL_ORDER_EDU = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.University),
                    myUI.getMessage(IndigoMessages.Country),
                    myUI.getMessage(IndigoMessages.Department),
                    myUI.getMessage(IndigoMessages.EduLevel),
                    myUI.getMessage(IndigoMessages.Start),
                    myUI.getMessage(IndigoMessages.End),
                    myUI.getMessage(IndigoMessages.Document)};
        } else {
            noSpouseEducationCkb.setEnabled(false);
            NATURAL_COL_ORDER_EDU = new String[]{Settings.button,
                    myUI.getMessage(IndigoMessages.University),
                    myUI.getMessage(IndigoMessages.Country),
                    myUI.getMessage(IndigoMessages.Department),
                    myUI.getMessage(IndigoMessages.EduLevel),
                    myUI.getMessage(IndigoMessages.Start),
                    myUI.getMessage(IndigoMessages.End)};
        }
        String id = Settings.FreshItem + (--r_table_counter);
        if (t.getContainerDataSource().size() == 0) {
            t.setContainerDataSource(prepareEducationContainer(own_id));
        }
        Item item;
        item = ((IndexedContainer) t.getContainerDataSource()).addItemAt(
                t.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeEducation, FontAwesome.MINUS_SQUARE));
        final ComboBox cb = createCombobox(0, myUI.getMessage(IndigoMessages.University), Settings.dbUniversityTable, true);
        cb.setNewItemsAllowed(true);
        cb.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
            try {
                DbDefinition dbd = new DbDefinition();
                dbd.connect();
                int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbUniversityTable, false);
                dbd.close();
                if (id1 != 0) {
                    for (Object next : t.getItemIds()) {
                        Item item1 = ((IndexedContainer) ((ComboBox) t.getContainerProperty(next,
                                myUI.getMessage(IndigoMessages.University)).getValue()).getContainerDataSource()).addItem(id1);
                        item1.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(newItemCaption);
                        cb.setValue(id1);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        });
        item.getItemProperty(myUI.getMessage(IndigoMessages.University)).setValue(cb);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Department)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Department),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 250, true), true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Start)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.Start),
                        null, true, Settings.yearPattern, Resolution.YEAR));
        item.getItemProperty(myUI.getMessage(IndigoMessages.End)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.End),
                        null, true, Settings.yearPattern, Resolution.YEAR));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Country)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.Country), Settings.dbCountry, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.EduLevel)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.EduLevel), Settings.dbEduLevel, true));
        if (own_id == 1) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);

            Button b = createButton(myUI.getMessage(IndigoMessages.DownLoad), null, Settings.download_button, FontAwesome.DOWNLOAD);
            b.setStyleName(ValoTheme.BUTTON_SMALL);
            b.setEnabled(false);
            hl.addComponent(b);

            Upload u = createUpload("", false);
            u.setId(id);
            u.setData(b);
            hl.addComponent(u);
            item.getItemProperty(myUI.getMessage(IndigoMessages.Document)).setValue(hl);
        }
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        t.setVisibleColumns((Object[]) NATURAL_COL_ORDER_EDU);
        t.setPageLength(t.size() > 0 ? t.size() : 1);
        t.setColumnExpandRatio(myUI.getMessage(IndigoMessages.University), 1);
        t.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Department), 1);
    }

    private void addWorkItem(final Table t, int own_id) {
        if (own_id == 1) {
            noWorkPlacesCkb.setEnabled(false);
        } else {
            noSpouseWorkPlacesCkb.setEnabled(false);
        }
        NATURAL_COL_ORDER_WORK = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.WorkPlace),
                myUI.getMessage(IndigoMessages.Sapat),
                myUI.getMessage(IndigoMessages.MainPosition),
                myUI.getMessage(IndigoMessages.ExtraPositions),
                myUI.getMessage(IndigoMessages.WorkingStatus),
                myUI.getMessage(IndigoMessages.Start),
                myUI.getMessage(IndigoMessages.End)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (t.getContainerDataSource().size() == 0) {
            t.setContainerDataSource(prepareWorkContainer(own_id));
        }
        Item item;
        item = ((IndexedContainer) t.getContainerDataSource()).addItemAt(
                t.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeWork, FontAwesome.MINUS_SQUARE));
        ComboBox cb = createCombobox(0, myUI.getMessage(IndigoMessages.MainPosition), null, true);
        item.getItemProperty(myUI.getMessage(IndigoMessages.MainPosition)).setValue(cb);
        ComboBoxMultiselect cb3 = createComboboxMulti(myUI.getMessage(IndigoMessages.ExtraPosition), false);
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            cb3.setContainerDataSource(
                    dbDef.exec_positions_for_select(myUI, false, true));
            cb.setContainerDataSource(
                    dbDef.exec_positions_for_select(myUI, false, true));
            dbDef.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraPositions)).setValue(cb3);
        cb = createCombobox(0, myUI.getMessage(IndigoMessages.WorkingStatus), null, true);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            cb.setContainerDataSource(dbd.exec_for_select_general_working_statuses(myUI));
            dbd.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        item.getItemProperty(myUI.getMessage(IndigoMessages.WorkingStatus)).setValue(cb);
        final ComboBox cb2 = createCombobox(0, myUI.getMessage(IndigoMessages.WorkPlace), Settings.dbWork_placeTable, true);
        cb2.setNewItemsAllowed(true);
        cb2.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
            try {
                DbDefinition dbd = new DbDefinition();
                dbd.connect();
                int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbWork_placeTable, false);
                dbd.close();
                if (id1 != 0) {
                    for (Object next : t.getContainerDataSource().getItemIds()) {
                        Item item1 = ((IndexedContainer) ((ComboBox) t.getContainerProperty(next,
                                myUI.getMessage(IndigoMessages.WorkPlace)).getValue()).getContainerDataSource()).addItem(id1);
                        item1.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(newItemCaption);
                        cb2.setValue(id1);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        });
        item.getItemProperty(myUI.getMessage(IndigoMessages.WorkPlace)).setValue(cb2);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Start)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.Start), null,
                        true, Settings.datePattern, Resolution.DAY));
        item.getItemProperty(myUI.getMessage(IndigoMessages.End)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.End), null,
                        false, Settings.datePattern, Resolution.DAY));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Sapat)).setValue(
                createCheckBox(false, myUI.getMessage(IndigoMessages.Sapat)));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        t.setVisibleColumns((Object[]) NATURAL_COL_ORDER_WORK);
        t.setPageLength(t.size());
    }

    private void addLanguageItem() {
        noLanguagesCkb.setEnabled(false);
        NATURAL_COL_ORDER_LANGUAGES = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.Language),
                myUI.getMessage(IndigoMessages.Level)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (languagesTable.getContainerDataSource().size() == 0) {
            languagesTable.setContainerDataSource(prepareLanguageContainer());
        }
        Item item;
        item = ((IndexedContainer) languagesTable.getContainerDataSource()).addItemAt(
                languagesTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeLanguage, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Language)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.Language), Settings.dbLanguageTable, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Level)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.Level), Settings.dbLanguageLevelTable, true));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        languagesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_LANGUAGES);
        languagesTable.setPageLength(languagesTable.size());
    }

    private void addCertificateItem() {
        noCertificatesCkb.setEnabled(false);
        NATURAL_COL_ORDER_CERTIFICATES = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.Certificate),
                myUI.getMessage(IndigoMessages.GivenBy),
                myUI.getMessage(IndigoMessages.IssueDate),
                myUI.getMessage(IndigoMessages.Note),
                myUI.getMessage(IndigoMessages.Document)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (certificatesTable.getContainerDataSource().size() == 0) {
            certificatesTable.setContainerDataSource(prepareCertificateContainer());
        }
        Item item;
        item = ((IndexedContainer) certificatesTable.getContainerDataSource()).addItemAt(
                certificatesTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeCertificate, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Note)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Note),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                                null, 250, true), false));
        item.getItemProperty(myUI.getMessage(IndigoMessages.GivenBy)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.GivenBy),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.IssueDate)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.IssueDate),
                        null, true, Settings.datePattern, Resolution.DAY));
        final ComboBox cb = createCombobox(0, myUI.getMessage(IndigoMessages.Certificate), Settings.dbCertificateTable, true);
        cb.setNewItemsAllowed(true);
        cb.setNewItemHandler((AbstractSelect.NewItemHandler) newItemCaption -> {
            try {
                DbDefinition dbd = new DbDefinition();
                dbd.connect();
                int id1 = dbd.exec_insert(new Definition(0, newItemCaption), Settings.dbCertificateTable, false);
                dbd.close();
                if (id1 != 0) {
                    for (Object next : certificatesTable.getContainerDataSource().getItemIds()) {
                        Item item1 = ((IndexedContainer) ((ComboBox) certificatesTable.getContainerDataSource().getContainerProperty(next,
                                myUI.getMessage(IndigoMessages.Certificate)).getValue()).getContainerDataSource()).addItem(id1);
                        item1.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(newItemCaption);
                        cb.setValue(id1);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        });
        item.getItemProperty(myUI.getMessage(IndigoMessages.Certificate)).setValue(cb);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        Button b = createButton(myUI.getMessage(IndigoMessages.DownLoad), null, Settings.download_button, FontAwesome.DOWNLOAD);
        b.setStyleName(ValoTheme.BUTTON_SMALL);
        b.setEnabled(false);
        hl.addComponent(b);

        Upload u = createUpload("", false);
        u.setId(id);
        u.setData(b);
        hl.addComponent(u);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Document)).setValue(hl);

        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        certificatesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CERTIFICATES);
        certificatesTable.setPageLength(certificatesTable.size());
    }

    private void addSeminarItem() {
        noSeminarsCkb.setEnabled(false);
        NATURAL_COL_ORDER_SEMINARS = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.Title),
                myUI.getMessage(IndigoMessages.Subject),
                myUI.getMessage(IndigoMessages.Note),
                myUI.getMessage(IndigoMessages.IssueDate)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (seminarsTable.getContainerDataSource().size() == 0) {
            seminarsTable.setContainerDataSource(prepareSeminarContainer());
        }
        Item item;
        item = ((IndexedContainer) seminarsTable.getContainerDataSource()).addItemAt(
                seminarsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeSeminar, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Title),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Subject)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Subject),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 200, true), true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Note)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Note),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 200, true), false));
        item.getItemProperty(myUI.getMessage(IndigoMessages.IssueDate)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.IssueDate),
                        null, true, Settings.datePattern, Resolution.DAY));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        seminarsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_SEMINARS);
        seminarsTable.setPageLength(seminarsTable.size());
    }

    private void addExamItem() {
        noExamsCkb.setEnabled(false);
        NATURAL_COL_ORDER_EXAMS = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.Exam),
                myUI.getMessage(IndigoMessages.Score),
                myUI.getMessage(IndigoMessages.IssueDate),
                myUI.getMessage(IndigoMessages.Document)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (examsTable.getContainerDataSource().size() == 0) {
            examsTable.setContainerDataSource(prepareExamContainer());
        }
        Item item;
        item = ((IndexedContainer) examsTable.getContainerDataSource()).addItemAt(
                examsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeExams, FontAwesome.MINUS_SQUARE));
        ComboBox cb = createCombobox(0, myUI.getMessage(IndigoMessages.Exam), null, true);
        try {
            DbExam dbe = new DbExam();
            dbe.connect();
            cb.setContainerDataSource(dbe.exec_for_select(myUI, 0));
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        item.getItemProperty(myUI.getMessage(IndigoMessages.Exam)).setValue(cb);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Score)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Score),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 10, false), true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.IssueDate)).setValue(
                createDateField(null, myUI.getMessage(IndigoMessages.IssueDate),
                        null, true, Settings.datePattern, Resolution.DAY));

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);

        Button b = createButton(myUI.getMessage(IndigoMessages.DownLoad), null, Settings.download_button, FontAwesome.DOWNLOAD);
        b.setStyleName(ValoTheme.BUTTON_SMALL);
        b.setEnabled(false);
        hl.addComponent(b);

        Upload u = createUpload("", false);
        u.setId(id);
        u.setData(b);
        hl.addComponent(u);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Document)).setValue(hl);
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        examsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_EXAMS);
        examsTable.setPageLength(examsTable.size());
    }

    private void addBranchItem() {
        noBranchesCkb.setEnabled(false);
        NATURAL_COL_ORDER_BRANCHES = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.Branch),
                myUI.getMessage(IndigoMessages.Main)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (branchesTable.getContainerDataSource().size() == 0) {
            branchesTable.setContainerDataSource(prepareBranchContainer());
        }
        Item item;
        item = ((IndexedContainer) branchesTable.getContainerDataSource()).addItemAt(
                branchesTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeBranch, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Branch)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.Branch), Settings.dbBranchTable, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Main)).setValue(
                createCheckBox(false, myUI.getMessage(IndigoMessages.Main)));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        branchesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_BRANCHES);
        branchesTable.setPageLength(branchesTable.size());
    }

    private void addLessonItem() {
        NATURAL_COL_ORDER_LESSONS = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.Lesson),
                myUI.getMessage(IndigoMessages.ClassName),
                myUI.getMessage(IndigoMessages.AcademicYear),
                myUI.getMessage(IndigoMessages.Hours),
                myUI.getMessage(IndigoMessages.ExtraHours)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (lessonsTable.getContainerDataSource().size() == 0) {
            lessonsTable.setContainerDataSource(prepareLessonsContainer());
        }
        Item item;
        item = ((IndexedContainer) lessonsTable.getContainerDataSource()).addItemAt(
                lessonsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeBranchHours, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Lesson)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.Lesson), Settings.dbBranchTable, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.ClassName)).setValue(
                createCombobox(0, myUI.getMessage(IndigoMessages.ClassName), Settings.classTable, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.AcademicYear)).setValue(
                createCombobox(myUI.getUser().getCurrent_year().getId(), myUI.getMessage(IndigoMessages.AcademicYear), Settings.dbYear, true));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Hours)).setValue(
                createTextFieldWithProperty(null, myUI.getMessage(IndigoMessages.Hours),
                        new IntegerRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 999),
                        new ObjectProperty<>(0), Settings.getStringToIntegerConverter()));
        item.getItemProperty(myUI.getMessage(IndigoMessages.ExtraHours)).setValue(
                createTextFieldWithProperty(null, myUI.getMessage(IndigoMessages.ExtraHours),
                        new IntegerRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 0, 999),
                        new ObjectProperty<>(0), Settings.getStringToIntegerConverter()));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        lessonsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_LESSONS);
        lessonsTable.setPageLength(lessonsTable.size());
    }

    private void addSupervisionItem() {
        NATURAL_COL_ORDER_SUPERVISION = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.ClassName),
                myUI.getMessage(IndigoMessages.FromDate),
                myUI.getMessage(IndigoMessages.TillDate),
                myUI.getMessage(IndigoMessages.Note)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (supervisionTable.getContainerDataSource().size() == 0) {
            supervisionTable.setContainerDataSource(prepareSupervisionContainer());
        }
        Item item = ((IndexedContainer) supervisionTable.getContainerDataSource()).addItemAt(
                supervisionTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(createButton(myUI.getMessage(IndigoMessages.DeleteButton), id,
                Settings.dbEmployeeOrder, FontAwesome.MINUS_SQUARE));
        ComboBox cb = createCombobox(0, myUI.getMessage(IndigoMessages.ClassName), null, true);
        item.getItemProperty(myUI.getMessage(IndigoMessages.ClassName)).setValue(cb);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            cb.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
            dbcn.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        DateField df = createDateField(today, myUI.getMessage(IndigoMessages.FromDate), null, true,
                Settings.datePattern, Resolution.DAY);
        df.setRangeEnd(today);
        item.getItemProperty(myUI.getMessage(IndigoMessages.FromDate)).setValue(df);
        df = createDateField(null, myUI.getMessage(IndigoMessages.TillDate), null,
                false, Settings.datePattern, Resolution.DAY);
        item.getItemProperty(myUI.getMessage(IndigoMessages.TillDate)).setValue(df);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Note)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Note),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 300, true), false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        supervisionTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_SUPERVISION);
        supervisionTable.setPageLength(supervisionTable.size());
    }

    private void addOrderItem() {
        NATURAL_COL_ORDER_ORDERS = new String[]{Settings.button,
                myUI.getMessage(IndigoMessages.OrderType),
                myUI.getMessage(IndigoMessages.Details),
                myUI.getMessage(IndigoMessages.FromDate),
                myUI.getMessage(IndigoMessages.TillDate),
                myUI.getMessage(IndigoMessages.Note)};
        String id = Settings.FreshItem + (--r_table_counter);
        if (ordersTable.getContainerDataSource().size() == 0) {
            ordersTable.setContainerDataSource(prepareOrdersContainer());
        }
        Item item;
        item = ((IndexedContainer) ordersTable.getContainerDataSource()).addItemAt(
                ordersTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(createButton(myUI.getMessage(IndigoMessages.DeleteButton), id, Settings.dbEmployeeOrder, FontAwesome.MINUS_SQUARE));
        ComboBox cb = createCombobox(0, myUI.getMessage(IndigoMessages.OrderType), null, true);
        cb.setData(id);
        cb.addValueChangeListener(this);
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            cb.setContainerDataSource(dbeo.execOrderTypesSel(myUI,
                    employeesDataTable.getContainerProperty(employeeID, Settings.visible_hr_orders).getValue().toString()));
            if (!currentUser.isPermitted(Settings.cnEmployeeTransferView + ":" + Settings.actAdd)) {
                cb.removeItem(5);
            }
            dbeo.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        item.getItemProperty(myUI.getMessage(IndigoMessages.OrderType)).setValue(cb);
        cb = createCombobox(0, null, null, false);
        cb.setVisible(false);
        cb.setRequired(false);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Details)).setValue(cb);
        DateField df = createDateField(today, myUI.getMessage(IndigoMessages.FromDate), null, true,
                Settings.datePattern, Resolution.DAY);
        df.setRangeEnd(today);
        item.getItemProperty(myUI.getMessage(IndigoMessages.FromDate)).setValue(df);
        df = createDateField(null, myUI.getMessage(IndigoMessages.TillDate), null,
                false, Settings.datePattern, Resolution.DAY);
        df.setEnabled(false);
        item.getItemProperty(myUI.getMessage(IndigoMessages.TillDate)).setValue(df);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Note)).setValue(
                createTextField(null, myUI.getMessage(IndigoMessages.Note),
                        new StringLengthValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 300, true), false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(IndigoMessages.Insert));
        ordersTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_ORDERS);

    }

    public TextField createTextField(String value, String description, Validator validator, boolean isRequired) {
        TextField tf = new TextField();
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(validator);
        if (isRequired) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        }
        if (value != null) {
            tf.setValue(value);
        }
        return tf;
    }

    public TextField createTextFieldWithProperty(Object value, String description, Validator validator, Property p, Converter conv) {
        TextField tf = new TextField(p);
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        tf.setNullRepresentation("");
        tf.setConverter(conv);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(validator);
        tf.getPropertyDataSource().setValue(value);
        return tf;
    }

    public DateField createDateField(Date value, String description, String caption,
                                     boolean isRequired, String date_format, Resolution resolution) {
        DateField df = new DateField();
        df.setCaption(caption);
        df.setWidth(Settings.PERCENTS100);
        df.setStyleName(ValoTheme.DATEFIELD_TINY);
        if (isRequired) {
            df.setRequired(true);
            df.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        }
        df.setDateFormat(date_format);
        df.setResolution(resolution);
        df.setDescription(description);
        if (value != null) {
            df.setValue(value);
        }
        return df;
    }

    public CheckBox createCheckBox(boolean value, String description) {
        CheckBox ckb = new CheckBox();
        ckb.setDescription(description);
        ckb.setValue(value);
        return ckb;
    }

    public ComboBoxMultiselect createComboboxMulti(String description, boolean isRequired) {
        ComboBoxMultiselect cb = new ComboBoxMultiselect();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        }
        cb.setClearButtonCaption(null);
        cb.setSelectAllButtonCaption(null);
        return cb;
    }

    public ComboBox createCombobox(int value, String description, String dbTable, boolean isRequired) {
        ComboBox cb = new ComboBox();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (isRequired) {
            cb.setRequired(true);
            cb.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        }
        try {
            if (dbTable != null) {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, dbTable, true));
                dbp.close();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        cb.setNullSelectionAllowed(false);
        cb.setValue(value);
        return cb;
    }

    public Button createButton(String description, String itemId, String table_name, Resource icon) {

        Button btn = new Button();
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(icon);
        btn.setData(itemId);
        btn.setId(table_name);
        btn.addClickListener(this);

        return btn;
    }

    public IndexedContainer prepareContractsContainer() {
        if (contractsCont == null) {
            contractsCont = new IndexedContainer();
            contractsCont.addContainerProperty(Settings.button, HorizontalLayout.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.AgreementType), ComboBox.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.SalaryAmount), TextField.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.CreationDate), DateField.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.Start), DateField.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.End), DateField.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.AcademicYear), Integer.class, 0);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.Year), String.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.ProbationaryPeriod), Integer.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.WorkingDays), Integer.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.SalaryDay), Integer.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.WorkingHours), Integer.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.Patent), String.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.Equipment), String.class, null);
            contractsCont.addContainerProperty(myUI.getMessage(IndigoMessages.PatentDate), Date.class, null);
            contractsCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            contractsCont.removeAllItems();
        }
        return contractsCont;
    }

    public IndexedContainer preparePhonesContainer() {
        if (phonesCont == null) {
            phonesCont = new IndexedContainer();
            phonesCont.addContainerProperty(Settings.button, Button.class, null);
            phonesCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Type), ComboBox.class, null);
            phonesCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Number), TextField.class, null);
            phonesCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            phonesCont.removeAllItems();
        }
        return phonesCont;
    }

    public IndexedContainer prepareChildrenContainer() {
        if (childrenCont == null) {
            childrenCont = new IndexedContainer();
            childrenCont.addContainerProperty(Settings.button, Button.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.FullName), TextField.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.DateOfBirth), DateField.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Institution), TextField.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.EducationStatus), ComboBox.class, null);
            childrenCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.HealthStatus), ComboBox.class, null);
            childrenCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            childrenCont.removeAllItems();
        }
        return childrenCont;
    }

    public IndexedContainer prepareEducationContainer(int own_id) {
        IndexedContainer c = own_id == 1 ? educationCont : spouseEducationCont;
        if (c == null) {
            c = new IndexedContainer();
            c.addContainerProperty(Settings.button, Button.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.University), ComboBox.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.Department), TextField.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.Start), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.End), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.Country), ComboBox.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.EduLevel), ComboBox.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.Document), HorizontalLayout.class, null);
            c.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            c.removeAllItems();
        }
        return c;
    }

    public IndexedContainer prepareWorkContainer(int own_id) {
        IndexedContainer c = own_id == 1 ? workPlacesCont : spouseWorkCont;
        if (c == null) {
            c = new IndexedContainer();
            c.addContainerProperty(Settings.button, Button.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.WorkPlace), ComboBox.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.MainPosition), ComboBox.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.ExtraPositions), ComboBoxMultiselect.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.WorkingStatus), ComboBox.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.Start), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.End), DateField.class, null);
            c.addContainerProperty(myUI.getMessage(IndigoMessages.Sapat), CheckBox.class, null);
            c.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            c.removeAllItems();
        }
        return c;
    }

    public IndexedContainer prepareLanguageContainer() {
        if (languagesCont == null) {
            languagesCont = new IndexedContainer();
            languagesCont.addContainerProperty(Settings.button, Button.class, null);
            languagesCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Language), ComboBox.class, null);
            languagesCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Level), ComboBox.class, null);
            languagesCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            languagesCont.removeAllItems();
        }
        return languagesCont;
    }

    public IndexedContainer prepareCertificateContainer() {
        if (certificatesCont == null) {
            certificatesCont = new IndexedContainer();
            certificatesCont.addContainerProperty(Settings.button, Button.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(IndigoMessages.Note), TextField.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(IndigoMessages.GivenBy), TextField.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(IndigoMessages.IssueDate), DateField.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(IndigoMessages.Certificate), ComboBox.class, null);
            certificatesCont.addContainerProperty(myUI.getMessage(IndigoMessages.Document), HorizontalLayout.class, null);
            certificatesCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            certificatesCont.removeAllItems();
        }
        return certificatesCont;
    }

    public IndexedContainer prepareSeminarContainer() {
        if (seminarsCont == null) {
            seminarsCont = new IndexedContainer();
            seminarsCont.addContainerProperty(Settings.button, Button.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Title), TextField.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Subject), TextField.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Note), TextField.class, null);
            seminarsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.IssueDate), DateField.class, null);
            seminarsCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            seminarsCont.removeAllItems();
        }
        return seminarsCont;
    }

    public IndexedContainer prepareExamContainer() {
        if (examsCont == null) {
            examsCont = new IndexedContainer();
            examsCont.addContainerProperty(Settings.button, Button.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Exam), ComboBox.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Score), TextField.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.IssueDate), DateField.class, null);
            examsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Document), HorizontalLayout.class, null);
            examsCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            examsCont.removeAllItems();
        }
        return examsCont;
    }

    public IndexedContainer prepareBranchContainer() {
        if (branchesCont == null) {
            branchesCont = new IndexedContainer();
            branchesCont.addContainerProperty(Settings.button, Button.class, null);
            branchesCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Branch), ComboBox.class, null);
            branchesCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Main), CheckBox.class, null);
            branchesCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            branchesCont.removeAllItems();
        }
        return branchesCont;
    }

    public IndexedContainer prepareLessonsContainer() {
        if (lessonsCont == null) {
            lessonsCont = new IndexedContainer();
            lessonsCont.addContainerProperty(Settings.button, Button.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Lesson), ComboBox.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.ClassName), ComboBox.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.AcademicYear), ComboBox.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Hours), TextField.class, null);
            lessonsCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.ExtraHours), TextField.class, null);
            lessonsCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            lessonsCont.removeAllItems();
        }
        return lessonsCont;
    }

    public IndexedContainer prepareSupervisionContainer() {
        if (supervisionCont == null) {
            supervisionCont = new IndexedContainer();
            supervisionCont.addContainerProperty(Settings.button, Button.class, null);
            supervisionCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.ClassName), ComboBox.class, null);
            supervisionCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.FromDate), DateField.class, null);
            supervisionCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.TillDate), DateField.class, null);
            supervisionCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Note), TextField.class, null);
            supervisionCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            supervisionCont.removeAllItems();
        }
        return supervisionCont;
    }

    public IndexedContainer prepareOrdersContainer() {
        if (ordersCont == null) {
            ordersCont = new IndexedContainer();
            ordersCont.addContainerProperty(Settings.button, Button.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.OrderType), ComboBox.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Details), ComboBox.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.FromDate), DateField.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.TillDate), DateField.class, null);
            ordersCont.addContainerProperty(
                    myUI.getMessage(IndigoMessages.Note), TextField.class, null);
            ordersCont.addContainerProperty(Settings.effected_by_id, String.class, null);
            ordersCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            ordersCont.removeAllItems();
        }
        return ordersCont;
    }

    private void insertEmployeeContact(EmployeeContact ec) {
        try {
            DbEmployeeContact dbec = new DbEmployeeContact();
            dbec.connect();
            int st = dbec.exec_insert(ec);
            if (st == 0) {
                dbec.exec_update(ec);
            }
            dbec.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmployeeSpouse(EmployeeSpouse es) {
        try {
            if (spouseHealthCB.getValue() != null &&
                    spouseFullNameTF.getValue() != null && !spouseFullNameTF.getValue().equals("")) {
                DbEmployeeSpouse dbes = new DbEmployeeSpouse();
                dbes.connect();
                int st = dbes.exec_insert(es);
                if (st == 0) {
                    dbes.exec_update(es);
                }
                dbes.close();
            } else {
                DbDefinition dbCon = new DbDefinition();
                dbCon.connect();
                dbCon.exec_delete(es.getEmployee_id() + "",
                        Settings.dbEmployeeSpouse, Settings.employee_id);
                dbCon.close();
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmployeeExtraInfo(EmployeeExtraInfo eei) {
        try {
            DbEmployeeExtraInfo dbei = new DbEmployeeExtraInfo();
            dbei.connect();
            int st = dbei.exec_insert(eei);
            if (st == 0) {
                dbei.exec_update(eei);
            }
            dbei.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmployeeGraduationSchool(EmployeeGraduationSchool egs) {
        try {
            DbEmployeeGraduationSchool dbCon = new DbEmployeeGraduationSchool();
            dbCon.connect();
            int st = dbCon.exec_insert(egs);
            if (st == 0) {
                dbCon.exec_update(egs);
            }
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertEmployeeOrder(EmployeeOrder eo) {
        try {
            DbEmployeeOrder dbeo = new DbEmployeeOrder();
            dbeo.connect();
            dbeo.exec_insert(eo);
            dbeo.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertLoginRoleName(String loginRName, String roleName) {
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            dbe.exec_insert_role(loginRName, roleName);
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertPermissions(String login, String permissions) {
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            String[] parsedArr = permissions.split(";");
            for (String string : parsedArr) {
                dbe.exec_insert_perm(login, string);
            }
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private void insertPermissions(String login) {
        StringBuilder permOneStr = new StringBuilder();
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            Iterator<?> iter = ((IndexedContainer) permissionTable
                    .getContainerDataSource()).getItemIds().iterator();
            while (iter.hasNext()) {
                Object next = iter.next();
                String permission;
                if (Settings.convertCollectionToStr((Set) ((ComboBoxMultiselect) (permissionTable
                        .getContainerProperty(next, myUI.getMessage(IndigoMessages.Functions))
                        .getValue())).getValue()) != null) {
                    permission = next + ":" + (Settings.convertCollectionToStr((Set) ((ComboBoxMultiselect) (permissionTable
                            .getContainerProperty(next, myUI.getMessage(IndigoMessages.Functions))
                            .getValue())).getValue()));
                    dbe.exec_insert_perm(login, permission);
                    permOneStr.append(permission);
                    if (iter.hasNext()) {
                        permOneStr.append(";");
                    }
                }
            }
            employeesDataTable.getContainerProperty(employeeID, myUI.getMessage(IndigoMessages.Permissions)).setValue(permOneStr.toString());
            dbe.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
    }

    private String checkPassword() {
        String password = "";
        if (passwordTF.getValue() != null
                && !passwordTF.getValue().equals("")) {
            password = (new Sha256Hash(passwordTF.getValue()).toString());
        }
        return password;
    }

    private Employee getEmployee(int id) {
        Employee e = new Employee();
        e.setPhoto(photoName);
        e.setLogin(loginTF.getValue());
        e.setPassword(new Sha256Hash(passwordTF.getValue()).toString());
        e.setName(nameTF.getValue());
        e.setSurname(surnameTF.getValue());
        if (middleNameTF.getValue() != null && !middleNameTF.getValue().equals("")) {
            e.setMiddle_name(middleNameTF.getValue());
        }
        e.setGender_id((Integer) genderCB.getValue());
        e.setBirth_date(birthDateDF.getValue());
        e.setNationality_id((Integer) nationalityCB.getValue());
        e.setCitizenship_id((Integer) citizenshipCB.getValue());
        e.setMartial_status_id((Integer) martialStatusCB.getValue());
        e.setModified_by_id(myUI.getUser().getId());
        e.setId(id);
        return e;
    }

    private EmployeeContact getEmployeeContact(int employee_id) {
        EmployeeContact ec = new EmployeeContact();
        ec.setEmployee_id(employee_id);
        ec.setEmail(emailTF.getValue());
        ec.setAddress(addressTA.getValue());
        ec.setBirth_place(birthPlaceTF.getValue());
        ec.setPassport(passportTF.getValue());
        ec.setInn(innTF.getValue());
        ec.setPassportGiven(passportGivenTf.getValue());
        ec.setPassportDate(passportDateDF.getValue());
        return ec;
    }

    private EmployeeSpouse getEmployeeSpouse(int employee_id) {
        EmployeeSpouse es = new EmployeeSpouse();
        es.setEmployee_id(employee_id);
        es.setFullName(spouseFullNameTF.getValue());
        es.setPhone(spousePhoneTF.getValue());
        es.setHealth_notes(spouseHealthNotesTF.getValue());
        if (spouseHealthCB.getValue() != null) {
            es.setHealth_status_id((Integer) spouseHealthCB.getValue());
        }
        return es;
    }

    private EmployeeExtraInfo getEmployeeExtraInfo(int employee_id) {
        EmployeeExtraInfo es = new EmployeeExtraInfo();
        es.setEmployee_id(employee_id);
        es.setHobbies(hobbiesTF.getValue());
        es.setPhobias(fobbiesTF.getValue());
        es.setHealth_notes(healthNotesTA.getValue());
        es.setShort_notes(shortNotesTA.getValue());
        es.setHealth_status_id((Integer) healthCB.getValue());
        return es;
    }

    private EmployeeGraduationSchool getEmployeeGradSchool(int employee_id) {
        EmployeeGraduationSchool es = new EmployeeGraduationSchool();
        es.setEmployee_id(employee_id);
        es.setStart(gradSchoolStartDF.getValue());
        es.setEnd(gradSchoolEndDF.getValue());
        es.setSchool_id((Integer) gradSchoolCB.getValue());
        return es;
    }

    private void addDataContainerItem(int id, int acc_category_id) {
        Item item = ((IndexedContainer) employeesDataTable.getContainerDataSource())
                .addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Id)).setValue(loginTF.getValue());
        item.getItemProperty(Settings.id).setValue(id);
        item.getItemProperty(myUI.getMessage(IndigoMessages.FirstName)).setValue(nameTF.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.LastName)).setValue(surnameTF.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.MiddleName)).setValue(middleNameTF.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.DateOfBirth)).setValue(birthDateDF.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.Photo)).setValue(photoName);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Permissions)).setValue(
                mainPositionCB.getContainerProperty(mainPositionCB.getValue(),
                        myUI.getMessage(IndigoMessages.Permissions)).getValue());
        item.getItemProperty(Settings.visible_hr_orders).setValue("1,2,3,4,5,6");
        item.getItemProperty(Settings.gender_id).setValue(genderCB.getValue());
        item.getItemProperty(Settings.nationality_id).setValue(nationalityCB.getValue());
        item.getItemProperty(Settings.citizenship_id).setValue(citizenshipCB.getValue());
        item.getItemProperty(Settings.martial_status_id).setValue(martialStatusCB.getValue());
        item.getItemProperty(Settings.position_id).setValue(mainPositionCB.getValue());
        item.getItemProperty(Settings.salary_category_id).setValue(contractCategoryCB.getValue());
        item.getItemProperty(Settings.working_status_id).setValue(2);
        item.getItemProperty(Settings.acc_category_id).setValue(acc_category_id);
        item.getItemProperty(myUI.getMessage(IndigoMessages.MainPosition)).setValue(
                mainPositionCB.getContainerDataSource().getContainerProperty(mainPositionCB.getValue(),
                        myUI.getMessage(IndigoMessages.Title)).getValue().toString());
        item.getItemProperty(myUI.getMessage(IndigoMessages.WorkingStatus)).setValue(
                workingStatCont.getContainerProperty(2, myUI.getMessage(IndigoMessages.Title)).getValue().toString());
        employeesDataTable.clearFilters();
        employeesDataTable.setValue(id);
    }

    private void updateDataContainer() {
        employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.Id)).setValue(loginTF.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.FirstName)).setValue(nameTF.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.LastName)).setValue(surnameTF.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.MiddleName)).setValue(middleNameTF.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.Photo)).setValue(photoName);
        employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.DateOfBirth)).setValue(birthDateDF.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                Settings.gender_id).setValue(genderCB.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                Settings.nationality_id).setValue(nationalityCB.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                Settings.citizenship_id).setValue(citizenshipCB.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                Settings.martial_status_id).setValue(martialStatusCB.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                Settings.salary_category_id).setValue(contractCategoryCB.getValue());
        employeesDataTable.getContainerProperty(employeeID,
                myUI.getMessage(IndigoMessages.MainPosition)).setValue(mainPositionCB
                .getContainerDataSource().getContainerProperty(mainPositionCB.getValue(),
                        myUI.getMessage(IndigoMessages.Title)).getValue().toString());
    }

    private void execDelete() {
        DbDefinition dbDef = null;
        int st = 0;
        try {
            dbDef = new DbDefinition();
            dbDef.connect();
            dbDef.getConnection().setAutoCommit(false);
            st = dbDef.exec_delete((Integer) employeesDataTable.getContainerProperty(employeeID, Settings.acc_category_id).getValue(), Settings.dbAcc_category);
            if (st != 0) {
                dbDef.exec_delete((Integer) employeesDataTable.getContainerProperty(employeeID,
                        Settings.id).getValue(), Settings.dbEmployee);
            }
            dbDef.getConnection().commit();
            dbDef.getConnection().setAutoCommit(true);
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException ex) {
            Notification.show(myUI.getMessage(IndigoMessages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception exx) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            logger.error(ex);
            logger.catching(ex);
        } catch (Exception ex) {
            if (dbDef != null) {
                try {
                    dbDef.getConnection().rollback();
                } catch (Exception exx) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            logger.error(ex);
            logger.catching(ex);
        }
        try {
            DbEmployee dbe = new DbEmployee();
            dbe.connect();
            if (st != 0) {
                dbe.exec_delete_perm(employeesDataTable.getContainerDataSource().getContainerProperty(employeeID,
                        myUI.getMessage(IndigoMessages.Id)).getValue().toString());
                dbe.exec_delete_role(employeesDataTable.getContainerDataSource().getContainerProperty(employeeID,
                        myUI.getMessage(IndigoMessages.Id)).getValue().toString());
                clearEmployeeFields(false);
                clearContactFields();
                clearSpouseFields();
                clearExtraInfoFields();
                clearGradSchoolFields();
                canBeAdvisorCkb.setValue(false);
                questioningTable.removeAllItems();
                clearAchievementsFields();
                clearProfFields();
                clearSchoolFields();
                ordersTable.removeAllItems();
                workingStatCont.getContainerProperty(employeesDataTable
                                .getContainerProperty(employeeID,
                                        Settings.working_status_id).getValue(), Settings.count)
                        .setValue(((Integer) workingStatCont.getContainerProperty(employeesDataTable
                                        .getContainerProperty(employeeID,
                                                Settings.working_status_id).getValue(),
                                Settings.count).getValue()) - 1);
                repaint();
                employeesDataTable.getContainerDataSource().removeItem(employeeID);
                employeesDataTable.setValue(null);
                Notification.show(myUI.getMessage(IndigoMessages.ValueDeleted), Notification.Type.HUMANIZED_MESSAGE);
            }
            dbe.close();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == loginTF) {
            photoUpl.setEnabled(isNew && loginTF.getValue() != null && !loginTF.getValue().equals("")
                    && loginTF.isEnabled());
        } else if (property == noPhonesCkb) {
            phonesTable.setEnabled(!noPhonesCkb.getValue());
            plusPhonesButton.setEnabled(!noPhonesCkb.getValue());
        } else if (property == noExamsCkb) {
            examsTable.setEnabled(!noExamsCkb.getValue());
            plusExamButton.setEnabled(!noExamsCkb.getValue());
        } else if (property == noLanguagesCkb) {
            languagesTable.setEnabled(!noLanguagesCkb.getValue());
            plusLanguageButton.setEnabled(!noLanguagesCkb.getValue());
        } else if (property == noChildrenCkb) {
            childrenTable.setEnabled(!noChildrenCkb.getValue());
            plusChildButton.setEnabled(!noChildrenCkb.getValue());
        } else if (property == noCertificatesCkb) {
            certificatesTable.setEnabled(!noCertificatesCkb.getValue());
            plusCertificateButton.setEnabled(!noCertificatesCkb.getValue());
        } else if (property == noSeminarsCkb) {
            seminarsTable.setEnabled(!noSeminarsCkb.getValue());
            plusSeminarButton.setEnabled(!noSeminarsCkb.getValue());
        } else if (property == noBranchesCkb) {
            branchesTable.setEnabled(!noBranchesCkb.getValue());
            plusBranchButton.setEnabled(!noBranchesCkb.getValue());
        } else if (property == noEducationCkb) {
            educationTable.setEnabled(!noEducationCkb.getValue());
            plusEducationButton.setEnabled(!noEducationCkb.getValue());
        } else if (property == noSpouseEducationCkb) {
            spouseEducationTable.setEnabled(!noSpouseEducationCkb.getValue());
            plusSpouseEducationButton.setEnabled(!noSpouseEducationCkb.getValue());
        } else if (property == noWorkPlacesCkb) {
            workPlacesTable.setEnabled(!noWorkPlacesCkb.getValue());
            plusWorkPlaceButton.setEnabled(!noWorkPlacesCkb.getValue());
        } else if (property == noSpouseWorkPlacesCkb) {
            spouseWorkPlacesTable.setEnabled(!noSpouseWorkPlacesCkb.getValue());
            plusSpouseWorkPlacesButton.setEnabled(!noSpouseWorkPlacesCkb.getValue());
        } else if (property == optionGroup) {
            setEmployeesDataTable(property.getValue().toString());
            repaint();
        } else if (property == martialStatusCB) {
            if (martialStatusCB.getValue() != null && (Integer) martialStatusCB.getValue() == 2) {
                spouseHealthCB.setRequired(true);
                spouseFullNameTF.setRequired(true);
                fieldsLayFamily.setVisible(true);
                captionSpouseInfo.setVisible(true);
            } else {
                spouseHealthCB.setRequired(false);
                spouseFullNameTF.setRequired(false);
                fieldsLayFamily.setVisible(false);
                captionSpouseInfo.setVisible(false);
            }
        } else if (property == employeesDataTable) {
            if (employeesDataTable.getItem(employeesDataTable.getValue()) != null) {
                employeeID = (Integer) employeesDataTable.getValue();
                fillFields();
                if (isMyProfile) {
                    modifyBtn.setEnabled(true);
                    cvBtn.setEnabled(true);
                } else if (!(Boolean) employeesDataTable.getContainerProperty(
                        employeeID, Settings.is_modifiable).getValue()) {
                    modifyBtn.setEnabled(false);
                    deleteBtn.setEnabled(false);
                } else {
                    cvBtn.setEnabled(true);
                    if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.actModify)) {
                        modifyBtn.setEnabled(true);
                    }
                    if (tabs.getSelectedTab() == tabs.getTab(empSearchLay).getComponent()) {
                        if (currentUser.isPermitted(Settings.cnEmployeeDefinitionView + ":" + Settings.actDelete)) {
                            deleteBtn.setEnabled(true);
                        }
                    }
                }
            } else {
                employeeID = 0;
            }
            updateInfoLayout();
        } else {
            ComboBox ordersCB = (ComboBox) property;
            if (ordersCB.getValue() != null) {
                ComboBox orders_extraCB = ((ComboBox) ordersTable.getContainerProperty(ordersCB.getData(), myUI.getMessage(IndigoMessages.Details)).getValue());
                DateField orderToDateDF = ((DateField) ordersTable.getContainerProperty(ordersCB.getData(), myUI.getMessage(IndigoMessages.TillDate)).getValue());

                try {
                    if ((Integer) ordersCB.getValue() == 3) {
                        orderToDateDF.setEnabled(true);
                        orders_extraCB.setVisible(true);
                        orders_extraCB.setDescription(myUI.getMessage(IndigoMessages.ClassName));
                        orders_extraCB.setRequired(true);
                        orders_extraCB.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
                        DbClassName dbcn = new DbClassName();
                        dbcn.connect();
                        orders_extraCB.setContainerDataSource(
                                dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
                        dbcn.close();
                    } else if ((Integer) ordersCB.getValue() == 5) {
                        orders_extraCB.setVisible(true);
                        orders_extraCB.setDescription(myUI.getMessage(IndigoMessages.School));
                        orders_extraCB.setRequired(true);
                        orders_extraCB.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
                        DbSchool dbs = new DbSchool();
                        dbs.connect();
                        orders_extraCB.setContainerDataSource(
                                dbs.execSchoolSel(myUI, myUI.getUser().getSchool().getId()));
                        dbs.close();
                    } else if ((Integer) ordersCB.getValue() == 1 || (Integer) ordersCB.getValue() == 2
                            || (Integer) ordersCB.getValue() == 7) {
                        if ((Integer) ordersCB.getValue() == 2) {
                            orderToDateDF.setEnabled(true);
                        } else {
                            orderToDateDF.setValue(null);
                            orderToDateDF.setEnabled(false);
                        }
                        orders_extraCB.setVisible(true);
                        orders_extraCB.setDescription(myUI.getMessage(IndigoMessages.Position));
                        orders_extraCB.setRequired(true);
                        orders_extraCB.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
                        DbDefinition dbd = new DbDefinition();
                        dbd.connect();
                        orders_extraCB.setContainerDataSource(
                                dbd.exec_positions_for_select(myUI, currentUser.hasRole(Settings.rnAdmin), currentUser.hasRole(Settings.rnHr)));
                        dbd.close();
                    } else {
                        orders_extraCB.setValue(null);
                        orders_extraCB.setVisible(false);
                        orders_extraCB.setRequired(false);
                        orderToDateDF.setValue(null);
                        orderToDateDF.setEnabled(false);
                    }
                    ordersTable.refreshRowCache();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
        }
    }

    private void buildContractExtraInfoLayout(Object contract_id, int type_id) {
        contractExtraInfoLay.removeAllComponents();
        contractExtraInfoLay.setSpacing(true);
        contractExtraInfoLay.setMargin(true);

        TextField tf;
        if (type_id == 1) {
            tf = createTextFieldWithProperty(contractsTable.getContainerProperty(
                            contract_id, myUI.getMessage(IndigoMessages.ProbationaryPeriod)).getValue(), null,
                    new IntegerRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 12),
                    new ObjectProperty(0), Settings.getStringToIntegerConverter());
            tf.setCaption(myUI.getMessage(IndigoMessages.ProbationaryPeriod));
            contractExtraInfoLay.addComponent(tf);
            tf = createTextFieldWithProperty(contractsTable.getContainerProperty(
                            contract_id, myUI.getMessage(IndigoMessages.SalaryDay)).getValue(), null,
                    new IntegerRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 31),
                    new ObjectProperty(0), Settings.getStringToIntegerConverter());
            tf.setCaption(myUI.getMessage(IndigoMessages.SalaryDay));
            contractExtraInfoLay.addComponent(tf);
            tf = createTextFieldWithProperty(contractsTable.getContainerProperty(
                            contract_id, myUI.getMessage(IndigoMessages.WorkingDays)).getValue(), null,
                    new IntegerRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 7),
                    new ObjectProperty(0), Settings.getStringToIntegerConverter());
            tf.setCaption(myUI.getMessage(IndigoMessages.WorkingDays));
            contractExtraInfoLay.addComponent(tf);
            tf = createTextFieldWithProperty(contractsTable.getContainerProperty(
                            contract_id, myUI.getMessage(IndigoMessages.WorkingHours)).getValue(), null,
                    new IntegerRangeValidator(myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 24),
                    new ObjectProperty(0), Settings.getStringToIntegerConverter());
            tf.setCaption(myUI.getMessage(IndigoMessages.WorkingHours));
            contractExtraInfoLay.addComponent(tf);
            if (type_id == 1) {
                tf = createTextField(contractsTable.getContainerProperty(
                        contract_id, myUI.getMessage(IndigoMessages.Equipment)).getValue() == null ? "" :
                        contractsTable.getContainerProperty(contract_id, myUI.getMessage(IndigoMessages.Equipment))
                                .getValue().toString(), null, new StringLengthValidator(
                        myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 350, false), true);
                tf.setCaption(myUI.getMessage(IndigoMessages.Equipment));
                contractExtraInfoLay.addComponent(tf);
            }
        } else {
            if (type_id == 4) {
                ComboBox cb = createCombobox(0, null, null, true);
                cb.setCaption(myUI.getMessage(IndigoMessages.AcademicYear));
                cb.setData(myUI.getMessage(IndigoMessages.Year));
                try {
                    DbDefinition dbd = new DbDefinition();
                    dbd.connect();
                    cb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
                    dbd.close();
                } catch (Exception ex) {
                    logger.error(ex);
                    logger.catching(ex);
                }
                cb.setValue(contractsTable.getContainerProperty(contract_id,
                        myUI.getMessage(IndigoMessages.AcademicYear)).getValue());
                contractExtraInfoLay.addComponent(cb);
            }
            if (type_id != 3 && type_id != 2) {
                tf = createTextField(contractsTable.getContainerProperty(
                        contract_id, myUI.getMessage(IndigoMessages.Patent)).getValue() == null ? "" :
                        contractsTable.getContainerProperty(contract_id, myUI.getMessage(IndigoMessages.Patent))
                                .getValue().toString(), null, new StringLengthValidator(
                        myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 200, false), true);
                tf.setCaption(myUI.getMessage(IndigoMessages.Patent));
                contractExtraInfoLay.addComponent(tf);
                contractExtraInfoLay.addComponent(createDateField((Date) contractsTable.getContainerProperty(
                                contract_id, myUI.getMessage(IndigoMessages.PatentDate)).getValue(), null,
                        myUI.getMessage(IndigoMessages.PatentDate), true, Settings.datePattern, Resolution.DAY));
            }
        }
        if (printContractBtn == null) {
            printContractBtn = new Button(myUI.getMessage(IndigoMessages.Print));
            printContractBtn.setStyleName(ValoTheme.BUTTON_SMALL);
            printContractBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
            printContractBtn.setIcon(FontAwesome.PRINT);
            printContractBtn.addClickListener(this);
        }
        contractExtraInfoLay.addComponent(printContractBtn);
        contractExtraInfoLay.setComponentAlignment(printContractBtn, Alignment.BOTTOM_RIGHT);
    }

    private EmployeeContract saveContractExtraInfo(Object id, EmployeeContract employeeContract) {
        for (int i = 0; i < contractExtraInfoLay.getComponentCount(); i++) {
            Component component = contractExtraInfoLay.getComponent(i);
            if (component instanceof TextField) {
                contractsTable.getContainerProperty(id, component.getCaption()).setValue(
                        ((TextField) component).getConvertedValue());
            } else if (component instanceof DateField) {
                contractsTable.getContainerProperty(id, component.getCaption()).setValue(((DateField) component).getValue());
            } else if (component instanceof ComboBox) {
                ComboBox cb = (ComboBox) component;
                contractsTable.getContainerProperty(id, cb.getCaption()).setValue(cb.getValue());
                contractsTable.getContainerProperty(id, cb.getData()).setValue(cb.getItemCaption(cb.getValue()));
            }
        }
        if ((Integer) contractsTable.getContainerProperty(id, myUI.getMessage(IndigoMessages.AcademicYear)).getValue() != 0) {
            employeeContract.setYearId((Integer) contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.AcademicYear)).getValue());
            employeeContract.setYear(contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.Year)).getValue().toString());
        }
        if (contractsTable.getContainerProperty(id,
                myUI.getMessage(IndigoMessages.ProbationaryPeriod)).getValue() != null) {
            employeeContract.setProbationaryPeriod((Integer) contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.ProbationaryPeriod)).getValue());
        }
        if (contractsTable.getContainerProperty(id,
                myUI.getMessage(IndigoMessages.SalaryDay)).getValue() != null) {
            employeeContract.setSalaryDay((Integer) contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.SalaryDay)).getValue());
        }
        if (contractsTable.getContainerProperty(id,
                myUI.getMessage(IndigoMessages.WorkingDays)).getValue() != null) {
            employeeContract.setWorkingDays((Integer) contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.WorkingDays)).getValue());
        }
        if (contractsTable.getContainerProperty(id,
                myUI.getMessage(IndigoMessages.WorkingHours)).getValue() != null) {
            employeeContract.setWorkingHours((Integer) contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.WorkingHours)).getValue());
        }
        if (contractsTable.getContainerProperty(id,
                myUI.getMessage(IndigoMessages.Patent)).getValue() != null) {
            employeeContract.setPatent(contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.Patent)).getValue().toString());
        }
        if (contractsTable.getContainerProperty(id,
                myUI.getMessage(IndigoMessages.Equipment)).getValue() != null) {
            employeeContract.setEquipment(contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.Equipment)).getValue().toString());
        }
        if (contractsTable.getContainerProperty(id,
                myUI.getMessage(IndigoMessages.PatentDate)).getValue() != null) {
            employeeContract.setPatentDate((Date) contractsTable.getContainerProperty(id,
                    myUI.getMessage(IndigoMessages.PatentDate)).getValue());
        }
        try {
            DbEmployeeContract dbCon = new DbEmployeeContract();
            dbCon.connect();
            dbCon.exec_update(employeeContract, Integer.parseInt(id.toString()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        return employeeContract;
    }

    public VerticalLayout getContractExtraInfoLay() {
        return contractExtraInfoLay;
    }

    public class MyReceiver implements Upload.Receiver {
        boolean isPhoto;

        public MyReceiver(boolean isPhoto) {
            this.isPhoto = isPhoto;
        }

        @Override
        public OutputStream receiveUpload(String filename, String mimetype) {
            fileName = filename;
            mimeType = mimetype;
            FileOutputStream fos; // Output stream to write to
            if (isPhoto) {
                photoName = loginTF.getValue() + ".jpg";
            }
            try {
                if (isPhoto) {
                    myFile = new File(Settings.PATH_TO_UPLOADS_HR + photoName);
                } else {
                    myFile = new File(Settings.PATH_TO_UPLOADS_HR + System.currentTimeMillis() + "_" + filename);
                }

                fos = new FileOutputStream(myFile);
            } catch (Exception ex) {
                // Error while opening the file. Not reported here.
                logger.error(ex);
                logger.catching(ex);
                return null;
            }
            return fos; // Return the output stream to write tou
        }
    }
}
