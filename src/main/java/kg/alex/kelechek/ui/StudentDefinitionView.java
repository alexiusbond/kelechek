package kg.alex.kelechek.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.data.validator.DoubleRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.dao.*;
import kg.alex.kelechek.domain.*;
import kg.alex.kelechek.enums.InstallmentPlanTypeCode;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.pdf.Invoice2023PDF;
import kg.alex.kelechek.pdf.contracts.ContractPdfRu;
import kg.alex.kelechek.tableexport.ExcelExport;
import kg.alex.kelechek.utils.*;
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
import java.util.Calendar;

public class StudentDefinitionView extends VerticalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {
    static final Logger logger = LogManager.getLogger(StudentDefinitionView.class);
    private final MyVaadinUI myUI;
    private final FilterTable studDataTable;
    private final OptionGroup statusesOG;
    private final int receive = 2;
    private final int give = 1;
    private final PopupButton copyRelPopupButton;
    private final Button plusRelButton;
    private final Button plusMatGiveButton;
    private final Button plusInstButton;
    private final Button plusPayButton;
    private final Button plusMatReceiveButton;
    private final Button plusDiscButton;
    private final Button plusCorrectionButton;
    private final Button plusCallButton;
    private final FormattedTable relativesTable;
    private final FormattedTable acsGiveTable;
    private final FormattedTable paymentsTable;
    private final FormattedTable acsReceiveTable;
    private final FormattedTable callsTable;
    private final TabSheet tabs;
    private final ArrayList<String> delPayIds = new ArrayList<>();
    private final ArrayList<String> delCallIds = new ArrayList<>();
    private final ArrayList<String> delCorrectionIds = new ArrayList<>();
    private final ArrayList<String> delDiscIds = new ArrayList<>();
    private final ArrayList<String> delRelIds = new ArrayList<>();
    private final Label eduStatTtlLab;
    private final String[] NATURAL_COL_ORDER;
    private final VerticalLayout famTableLay;
    private final VerticalLayout acsGiveTableLay;
    private final VerticalLayout payTableLay;
    private final VerticalLayout acsReceiveTableLay;
    private final VerticalLayout callsTableLay;
    private final GridLayout studSearchLay;
    private final HorizontalSplitPanel horSplitPanel;
    private final Subject currentUser = SecurityUtils.getSubject();
    private final String currency;
    public IndexedContainer eduStatCont;
    StringBuilder discountsStr = new StringBuilder();
    private Button createBtn;
    private Button modifyBtn;
    private Button deleteBtn;
    private Button saveBtn;
    private Button cancelBtn;
    private Button divideBtn;
    private final Button copyRelBtn;
    private TextField nameTF, loginTF, surnameTF, middleNameTF;
    private DateField birthDateDF, currDate;
    private ComboBox genderCB;
    private ComboBox classCB;
    private ComboBox statusCB;
    private ComboBox contractCB;
    private final ComboBox studentsCB;
    private ComboBox instTypeCB;
    private FormLayout fieldsLay1, fieldsLay2;
    private int r_table_counter = 1000;
    private int discCounter;
    private int contr_id;
    private Double instCtrAmount;
    private Double netContrAmount;
    private Double contract_amount;
    private Double instPlanContSum;
    private Double contr_with_disc;
    private Double ttl_left;
    private Double ttl_payment;
    private Double discountAmount;
    private Double debt;
    private Double toPay;
    private Double contractWithDiscount;
    private FormattedTable installmentTable;
    private FormattedTable discountsTable;
    private FormattedTable correctionsTable;
    private Button printButton;
    private Button excelButton;
    private Button financialHistoryButton;
    private IndexedContainer relativesContainer = null,
            acsGivContainer = null, acsRecContainer = null, instPlanCont = null,
            paymentCont = null, discountCont = null, correctionCont = null, callsCont = null;
    private boolean isNew;
    private Label debtLab;
    private Label contractLab;
    private Label discountLab;
    private Label correctionLab;
    private Label netLab;
    private Label paidLab;
    private Label leftLab;
    private Label instPlanTtlLab;
    private Label netIPlanTtlLab;
    private Label planDebt;
    private Label instPlanDifLab;
    private Label tabContractLab;
    private Label tabContractNetLab;
    private String[] NATURAL_COL_ORDER_PAYMENTS;
    private String[] NATURAL_COL_ORDER_CALLS;
    private String[] NATURAL_COL_ORDER_INST_PLAN;
    private String[] NATURAL_COL_ORDER_DISCOUNTS;
    private String[] NATURAL_COL_ORDER_CORRECTIONS;
    private String[] NATURAL_COL_ORDER_RELATIVES;
    private VerticalLayout contractLay;
    private GridLayout gridStudLay;
    private GridLayout contractTabLay;
    private HorizontalLayout buttonsLay;
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private ProgressBar uploadProgressBar;
    private String photoName, fileName, mimeType;
    private Embedded photoEmb;
    private SimpleFileDownloader downloader = null;

    public StudentDefinitionView(final MyVaadinUI myUI) {
        this.myUI = myUI;
        this.currency = Settings.KGS;

        buildButtonsLayout();
        buildStudGridLayout();
        try {
            DbDefinition dbed = new DbDefinition();
            dbed.connect();
            eduStatCont = dbed.execSQL_statuses_with_count(myUI, Settings.dbEducationStatus, true);
            dbed.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        horSplitPanel = new HorizontalSplitPanel();
        horSplitPanel.setSplitPosition(76, Unit.PERCENTAGE);
        horSplitPanel.setSizeFull();
        horSplitPanel.setLocked(true);
        horSplitPanel.setFirstComponent(gridStudLay);
        horSplitPanel.setSecondComponent(contractLay);

        NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.StudentId),
                myUI.getMessage(Messages.FirstName), myUI.getMessage(Messages.LastName),
                myUI.getMessage(Messages.ClassName), myUI.getMessage(Messages.EducationStatus),
                myUI.getMessage(Messages.EnteringYear),
                myUI.getMessage(Messages.Relative), myUI.getMessage(Messages.Phone)};

        Label eduStatusLab = new Label();
        eduStatusLab.setSizeUndefined();
        eduStatusLab.setContentMode(ContentMode.HTML);
        eduStatusLab.setValue(myUI.getMessage(Messages.ShowByEducationStatuses) + ": ");

        IndexedContainer eduContainer = new IndexedContainer();
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            eduContainer = dbd.exec_for_select(myUI, Settings.dbEducationStatus, true);
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        statusesOG = new OptionGroup();
        statusesOG.addStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);
        statusesOG.setMultiSelect(true);
        statusesOG.addItems(eduContainer.getItemIds());
        for (Object o : eduContainer.getItemIds()) {
            Integer next = (Integer) o;
            statusesOG.setItemCaption(next, eduContainer.getContainerProperty(next,
                    myUI.getMessage(Messages.Title)).getValue().toString());
            if (next <= 3) {
                statusesOG.select(next);
            }
        }
        statusesOG.addValueChangeListener(this);

        studDataTable = new FilterTable();
        studDataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        studDataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        studDataTable.setSizeFull();
        studDataTable.setNullSelectionAllowed(false);
        studDataTable.setFilterBarVisible(true);
        studDataTable.setSelectable(true);
        studDataTable.addValueChangeListener(this);

        setStudDataTable(statusesOG.getValue().toString());

        HorizontalLayout studSearchLayFooter = new HorizontalLayout();
        studSearchLayFooter.setWidth(Settings.PERCENTS100);
        eduStatTtlLab = new Label();
        eduStatTtlLab.setSizeUndefined();
        eduStatTtlLab.setImmediate(true);
        eduStatTtlLab.setContentMode(ContentMode.HTML);
        Label filteredLab = new Label();
        filteredLab.setSizeUndefined();
        filteredLab.setImmediate(true);
        filteredLab.setContentMode(ContentMode.HTML);
        filteredLab.setValue(myUI.getMessage(Messages.Filtered) + ": 0");
        repaint();

        studDataTable.setFilterGenerator(new MyFilterGenerator(
                filteredLab, myUI.getMessage(Messages.Filtered), studDataTable));

        studSearchLay = new GridLayout(2, 3);
        studSearchLay.setSizeFull();
        studSearchLay.setMargin(true);
        studSearchLayFooter.addComponent(filteredLab);
        studSearchLayFooter.addComponent(eduStatTtlLab);
        studSearchLayFooter.setComponentAlignment(filteredLab, Alignment.BOTTOM_LEFT);
        studSearchLayFooter.setComponentAlignment(eduStatTtlLab, Alignment.BOTTOM_RIGHT);
        studSearchLayFooter.setExpandRatio(filteredLab, 1);
        studSearchLayFooter.setExpandRatio(eduStatTtlLab, 2);

        studSearchLay.addComponent(eduStatusLab, 0, 0);
        studSearchLay.addComponent(statusesOG, 1, 0);
        studSearchLay.addComponent(studDataTable, 0, 1, 1, 1);
        studSearchLay.addComponent(studSearchLayFooter, 0, 2, 1, 2);
        studSearchLay.setRowExpandRatio(1, 1);
        studSearchLay.setColumnExpandRatio(0, 1);
        studSearchLay.setComponentAlignment(statusesOG, Alignment.MIDDLE_RIGHT);
        studSearchLay.setComponentAlignment(eduStatusLab, Alignment.MIDDLE_RIGHT);

        this.setSplitPosition(40, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(horSplitPanel);

        plusRelButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusRelButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusRelButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusRelButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusRelButton.addClickListener(this);

        copyRelPopupButton = new PopupButton(myUI.getMessage(Messages.CopyRelatives));
        copyRelPopupButton.setImmediate(true);
        copyRelPopupButton.setStyleName(ValoTheme.BUTTON_SMALL);
        copyRelPopupButton.addClickListener(this);

        studentsCB = new ComboBox(myUI.getMessage(Messages.SelectStudent));
        studentsCB.setNullSelectionAllowed(false);
        studentsCB.setRequired(true);
        studentsCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        studentsCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        studentsCB.setWidth(Settings.PERCENTS100);
        studentsCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        studentsCB.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbStudent dbDef = new DbStudent();
            dbDef.connect();
            studentsCB.setContainerDataSource(
                    dbDef.exec_for_select(myUI, myUI.getUser().getSchool().getId(),
                            myUI.getUser().getCurrent_year().getId(), "1,2,3,4,5"));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        copyRelBtn = new Button(myUI.getMessage(Messages.Copy));
        copyRelBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        copyRelBtn.setIcon(FontAwesome.COPY);
        copyRelBtn.addClickListener(this);

        VerticalLayout cpRelVl = new VerticalLayout();
        cpRelVl.setWidth("350px");
        cpRelVl.setSpacing(true);
        cpRelVl.setMargin(true);
        cpRelVl.addComponent(studentsCB);
        cpRelVl.addComponent(copyRelBtn);
        cpRelVl.setComponentAlignment(copyRelBtn, Alignment.BOTTOM_RIGHT);
        copyRelPopupButton.setContent(cpRelVl);

        plusMatGiveButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusMatGiveButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusMatGiveButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusMatGiveButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusMatGiveButton.addClickListener(this);

        plusMatReceiveButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusMatReceiveButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusMatReceiveButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusMatReceiveButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusMatReceiveButton.addClickListener(this);

        plusDiscButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusDiscButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusDiscButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusDiscButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusDiscButton.addClickListener(this);

        plusCorrectionButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusCorrectionButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusCorrectionButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusCorrectionButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusCorrectionButton.addClickListener(this);

        plusInstButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusInstButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusInstButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusInstButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusInstButton.addClickListener(this);

        plusPayButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusPayButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusPayButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusPayButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusPayButton.addClickListener(this);

        plusCallButton = new Button(myUI.getMessage(Messages.AddRecord));
        plusCallButton.setStyleName(ValoTheme.BUTTON_SMALL);
        plusCallButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        plusCallButton.setIcon(FontAwesome.PLUS_SQUARE);
        plusCallButton.addClickListener(this);

        relativesTable = new FormattedTable(myUI);
        relativesTable.setSizeFull();
        relativesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        relativesTable.addStyleName("noWrapHeader");
        relativesTable.setNullSelectionAllowed(false);

        HorizontalLayout btnLay = new HorizontalLayout();
        btnLay.setWidth(Settings.PERCENTS100);
        btnLay.setSpacing(true);
        btnLay.addComponent(plusRelButton);
        btnLay.addComponent(copyRelPopupButton);
        btnLay.setComponentAlignment(copyRelPopupButton, Alignment.MIDDLE_RIGHT);

        famTableLay = new VerticalLayout();
        famTableLay.setSizeFull();
        famTableLay.setSpacing(true);
        famTableLay.setMargin(true);
        famTableLay.addComponent(btnLay);
        famTableLay.setComponentAlignment(btnLay, Alignment.BOTTOM_LEFT);
        famTableLay.addComponent(relativesTable);
        famTableLay.setExpandRatio(relativesTable, 1);

        acsGiveTable = new FormattedTable(myUI);
        acsGiveTable.setSizeFull();
        acsGiveTable.setStyleName(ValoTheme.TABLE_SMALL);
        acsGiveTable.setNullSelectionAllowed(false);
        acsGiveTableLay = new VerticalLayout();
        acsGiveTableLay.setSizeFull();
        acsGiveTableLay.setSpacing(true);
        acsGiveTableLay.setMargin(true);
        acsGiveTableLay.addComponent(plusMatGiveButton);
        acsGiveTableLay.setComponentAlignment(plusMatGiveButton, Alignment.BOTTOM_LEFT);
        acsGiveTableLay.addComponent(acsGiveTable);
        acsGiveTableLay.setExpandRatio(acsGiveTable, 1);

        acsReceiveTable = new FormattedTable(myUI);
        acsReceiveTable.setSizeFull();
        acsReceiveTable.setStyleName(ValoTheme.TABLE_SMALL);
        acsReceiveTable.setNullSelectionAllowed(false);
        acsReceiveTableLay = new VerticalLayout();
        acsReceiveTableLay.setSizeFull();
        acsReceiveTableLay.setSpacing(true);
        acsReceiveTableLay.setMargin(true);
        acsReceiveTableLay.addComponent(plusMatReceiveButton);
        acsReceiveTableLay.setComponentAlignment(plusMatReceiveButton, Alignment.BOTTOM_LEFT);
        acsReceiveTableLay.addComponent(acsReceiveTable);
        acsReceiveTableLay.setExpandRatio(acsReceiveTable, 1);

        buildContractTab();

        paymentsTable = new FormattedTable(myUI);
        paymentsTable.setSizeFull();
        paymentsTable.setStyleName(ValoTheme.TABLE_SMALL);
        paymentsTable.setNullSelectionAllowed(false);
        payTableLay = new VerticalLayout();
        payTableLay.setSizeFull();
        payTableLay.setSpacing(true);
        payTableLay.setMargin(true);
        payTableLay.addComponent(plusPayButton);
        payTableLay.setComponentAlignment(plusPayButton, Alignment.BOTTOM_LEFT);
        payTableLay.addComponent(paymentsTable);
        payTableLay.setExpandRatio(paymentsTable, 1);

        callsTable = new FormattedTable(myUI);
        callsTable.setSizeFull();
        callsTable.setStyleName(ValoTheme.TABLE_SMALL);
        callsTable.setNullSelectionAllowed(false);
        callsTableLay = new VerticalLayout();
        callsTableLay.setSizeFull();
        callsTableLay.setSpacing(true);
        callsTableLay.setMargin(true);
        callsTableLay.addComponent(plusCallButton);
        callsTableLay.setComponentAlignment(plusCallButton, Alignment.BOTTOM_LEFT);
        callsTableLay.addComponent(callsTable);
        callsTableLay.setExpandRatio(callsTable, 1);

        tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabs.addTab(studSearchLay).setCaption(myUI.getMessage(Messages.Search));
        tabs.addTab(famTableLay).setCaption(myUI.getMessage(Messages.FamilyInfo));
        tabs.addTab(contractTabLay).setCaption(myUI.getMessage(Messages.Contract));
        tabs.addTab(payTableLay).setCaption(myUI.getMessage(Messages.Payments));
        tabs.addTab(acsGiveTableLay).setCaption(myUI.getMessage(Messages.MaterialsGive));
        tabs.addTab(acsReceiveTableLay).setCaption(myUI.getMessage(Messages.MaterialsReceive));
        tabs.addTab(callsTableLay).setCaption(myUI.getMessage(Messages.Calls));
        tabs.addSelectedTabChangeListener(
                (TabSheet.SelectedTabChangeListener) event -> {
                    if (event.getTabSheet().getSelectedTab() == famTableLay
                            && studDataTable.getValue() != null) {
                        setRelativesTable();
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == acsGiveTableLay
                            && studDataTable.getValue() != null) {
                        setMaterialsTable(give);
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == acsReceiveTableLay
                            && studDataTable.getValue() != null) {
                        setMaterialsTable(receive);
                        prepareNormalMode();
                    }
                    if (event.getTabSheet().getSelectedTab() == contractTabLay
                            && studDataTable.getValue() != null) {
                        setInstPlanTable();
                        setDiscountsTable();
                        setCorrectionsTable();
                        setContractTab((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId());
                        recountInstPlanLabel();
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == payTableLay
                            && studDataTable.getValue() != null) {
                        setPaymentsTable();
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == callsTableLay
                            && studDataTable.getValue() != null) {
                        setCallsTable();
                        prepareNormalMode();
                    } else if (event.getTabSheet().getSelectedTab() == studSearchLay) {
                        prepareNormalMode();
                    }
                });
        this.setSecondComponent(tabs);
        prepareNormalMode();
    }

    private void buildButtonsLayout() {

        buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);
        buttonsLay.setWidth(Settings.PERCENTS100);

        modifyBtn = new Button();
        modifyBtn.setEnabled(false);
        modifyBtn.setDescription(myUI.getMessage(Messages.ModifyButton));
        modifyBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        modifyBtn.setIcon(FontAwesome.PENCIL);
        modifyBtn.addClickListener(this);
        buttonsLay.addComponent(modifyBtn);

        createBtn = new Button();
        createBtn.setEnabled(false);
        createBtn.setDescription(myUI.getMessage(Messages.CreateButton));
        createBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        createBtn.setIcon(FontAwesome.FILE_O);
        createBtn.addClickListener(this);
        buttonsLay.addComponent(createBtn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(Messages.DeleteButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        buttonsLay.addComponent(deleteBtn);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(Messages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        buttonsLay.addComponent(saveBtn);

        cancelBtn = new Button();
        cancelBtn.setDescription(myUI.getMessage(Messages.CancelButton));
        cancelBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        cancelBtn.setIcon(FontAwesome.BAN);
        cancelBtn.addClickListener(this);
        buttonsLay.addComponent(cancelBtn);

        financialHistoryButton = new Button();
        financialHistoryButton.setDescription(myUI.getMessage(Messages.FinancialHistory));
        financialHistoryButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        financialHistoryButton.setIcon(FontAwesome.DOLLAR);
        financialHistoryButton.setEnabled(false);
        financialHistoryButton.addClickListener(this);
        buttonsLay.addComponent(financialHistoryButton);

        printButton = new PopupButton(myUI.getMessage(Messages.Print));
        printButton.setDescription(myUI.getMessage(Messages.Print));
        printButton.setIcon(FontAwesome.PRINT);
        printButton.setImmediate(true);
        printButton.addClickListener(this);
        printButton.setEnabled(false);
        buttonsLay.addComponent(printButton);

        excelButton = new Button(myUI.getMessage(Messages.ExportToExcel));
        excelButton.setDescription(myUI.getMessage(Messages.ExportToExcel));
        excelButton.setIcon(FontAwesome.FILE_EXCEL_O);
        excelButton.addClickListener(this);
        excelButton.setEnabled(true);
        buttonsLay.addComponent(excelButton);
        buttonsLay.setExpandRatio(excelButton, 1);
    }

    private void buildStudGridLayout() {
        gridStudLay = new GridLayout(3, 3);
        gridStudLay.setSpacing(true);
        gridStudLay.setMargin(true);
        gridStudLay.setWidth("95%");

        buildPhotoLayout();
        buildFieldsLayout1();
        buildFieldsLayout2();
        buildContractLayout();

        gridStudLay.addComponent(buttonsLay);
        gridStudLay.addComponent(photoEmb, 0, 1);
        gridStudLay.addComponent(photoUpl, 0, 2);
        gridStudLay.addComponent(fieldsLay1, 1, 1, 1, 2);
        gridStudLay.addComponent(fieldsLay2, 2, 1, 2, 2);
        gridStudLay.setRowExpandRatio(1, 1);
        gridStudLay.setColumnExpandRatio(1, 1);
        gridStudLay.setColumnExpandRatio(2, 1);
    }

    private void buildPhotoLayout() {
        photoEmb = new Embedded();
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        photoEmb.setHeight("100px");

        photoUpl = createUpload(myUI.getMessage(Messages.Upload), true);
    }

    private void buildFieldsLayout1() {
        fieldsLay1 = new FormLayout();
        fieldsLay1.setSpacing(false);

        loginTF = new TextField(myUI.getMessage(Messages.Id));
        loginTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        loginTF.setWidth(Settings.PERCENTS100);
        loginTF.setEnabled(false);
        fieldsLay1.addComponent(loginTF);

        nameTF = new TextField(myUI.getMessage(Messages.FirstName));
        nameTF.setRequired(true);
        nameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        nameTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        nameTF.setWidth(Settings.PERCENTS100);
        nameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 1, 100, false));
        fieldsLay1.addComponent(nameTF);

        surnameTF = new TextField(myUI.getMessage(Messages.LastName));
        surnameTF.setRequired(true);
        surnameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        surnameTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        surnameTF.setWidth(Settings.PERCENTS100);
        surnameTF.addValidator(new StringLengthValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 1, 100, false));
        fieldsLay1.addComponent(surnameTF);

        middleNameTF = new TextField(myUI.getMessage(Messages.MiddleName));
        middleNameTF.setStyleName(ValoTheme.TEXTFIELD_TINY);
        middleNameTF.setWidth(Settings.PERCENTS100);
        fieldsLay1.addComponent(middleNameTF);
    }

    private void buildFieldsLayout2() {
        fieldsLay2 = new FormLayout();
        fieldsLay2.setSpacing(false);

        genderCB = new ComboBox(myUI.getMessage(Messages.Gender));
        genderCB.setNullSelectionAllowed(false);
        genderCB.setRequired(true);
        genderCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        genderCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        genderCB.setWidth(Settings.PERCENTS100);
        genderCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        genderCB.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            genderCB.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbGender, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fieldsLay2.addComponent(genderCB);

        birthDateDF = createDateField(new Date(), null, null, false, true,
                Settings.datePattern, Resolution.DAY);
        birthDateDF.setCaption(myUI.getMessage(Messages.DateOfBirth));
        fieldsLay2.addComponent(birthDateDF);

        classCB = new ComboBox(myUI.getMessage(Messages.ClassName));
        classCB.setNullSelectionAllowed(false);
        classCB.setRequired(true);
        classCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        classCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        classCB.setWidth(Settings.PERCENTS100);
        classCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        classCB.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbClassName dbcn = new DbClassName();
            dbcn.connect();
            classCB.setContainerDataSource(dbcn.execClass_sel(myUI, myUI.getUser().getSchool().getId()));
            dbcn.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        classCB.addValueChangeListener(this);
        fieldsLay2.addComponent(classCB);

        statusCB = new ComboBox(myUI.getMessage(Messages.EducationStatus));
        statusCB.setNullSelectionAllowed(false);
        statusCB.setEnabled(false);
        statusCB.setRequired(true);
        statusCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        statusCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        statusCB.setWidth(Settings.PERCENTS100);
        statusCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        statusCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            statusCB.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbEducationStatus, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        fieldsLay2.addComponent(statusCB);
    }

    private void buildContractLayout() {
        contractLay = new VerticalLayout();
        contractLay.setMargin(true);

        contractLab = new Label();
        contractLab.setContentMode(ContentMode.HTML);
        contractLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        contractLab.setValue(myUI.getMessage(Messages.Contract) + ":");

        discountLab = new Label();
        discountLab.setContentMode(ContentMode.HTML);
        discountLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        discountLab.setValue(myUI.getMessage(Messages.Discounts) + ":");

        correctionLab = new Label();
        correctionLab.setContentMode(ContentMode.HTML);
        correctionLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        correctionLab.setValue(myUI.getMessage(Messages.Corrections) + ":");

        debtLab = new Label();
        debtLab.setContentMode(ContentMode.HTML);
        debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        debtLab.setValue(myUI.getMessage(Messages.PreviousYearDebt) + ":");

        netLab = new Label();
        netLab.setContentMode(ContentMode.HTML);
        netLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        netLab.setValue(myUI.getMessage(Messages.Net) + ":");

        paidLab = new Label();
        paidLab.setContentMode(ContentMode.HTML);
        paidLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        paidLab.setValue(myUI.getMessage(Messages.Paid) + ":");

        leftLab = new Label();
        leftLab.setContentMode(ContentMode.HTML);
        leftLab.setStyleName(ValoTheme.LABEL_SUCCESS);
        leftLab.setValue(myUI.getMessage(Messages.Left) + ":");

        planDebt = new Label();
        planDebt.setContentMode(ContentMode.HTML);
        planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
        planDebt.setValue(myUI.getMessage(Messages.InstPlanDebt) + ":");

        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmContractInfo)) {
            contractLay.addComponent(contractLab);
            contractLay.setComponentAlignment(contractLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(discountLab);
            contractLay.setComponentAlignment(discountLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(correctionLab);
            contractLay.setComponentAlignment(correctionLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(debtLab);
            contractLay.setComponentAlignment(debtLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(netLab);
            contractLay.setComponentAlignment(netLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(paidLab);
            contractLay.setComponentAlignment(paidLab, Alignment.BOTTOM_LEFT);
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":"
                + Settings.prmContractInfoLeftDebt)) {
            contractLay.addComponent(leftLab);
            contractLay.setComponentAlignment(leftLab, Alignment.BOTTOM_LEFT);
            contractLay.addComponent(planDebt);
            contractLay.setComponentAlignment(planDebt, Alignment.BOTTOM_LEFT);
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source.getId() != null && source.getId().equals(Settings.cancel_upload_button)) {
            if (photoUpl != null) {
                photoUpl.interruptUpload();
            }
        } else if (source == modifyBtn) {
            if (studDataTable.getValue() != null) {
                if ((tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                        || tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent())
                        && debt > 0.01 && contract_amount == 0.0 && !currentUser.hasRole(Settings.rnAdmin)) {
                    Notification.show(myUI.getMessage(Messages.OperationNotAllowedDueToDebt),
                            Notification.Type.WARNING_MESSAGE);
                } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent() && !myUI.getUser().getCurrent_year().isLast()) {
                    ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                            myUI.getMessage(Messages.ConfirmActionNotInLastYear)
                                    + " (" + myUI.getUser().getCurrent_year().getName()
                                    + ") " + myUI.getMessage(Messages.NotCurrentYear) + ".",
                            myUI.getMessage(Messages.Yes),
                            myUI.getMessage(Messages.No),
                            (ConfirmDialog.Listener) dialog -> {
                                if (dialog.isConfirmed()) {
                                    modifyBtnAction();
                                }
                            });
                } else {
                    modifyBtnAction();
                }
            }
        } else if (source == plusRelButton) {
            addRelativeItem();
        } else if (source == copyRelPopupButton) {
        } else if (source == plusMatGiveButton) {
            addAccessoriesItem(give);
        } else if (source == plusMatReceiveButton) {
            addAccessoriesItem(receive);
        } else if (source == plusPayButton) {
            addPaymentsItem();
        } else if (source == plusCallButton) {
            addCallsItem();
        } else if (source == divideBtn) {
            if (instTypeCB.getValue() != null) {
                recount();
                addInstallmentPlanItem(true);
                recountInstPlanLabel();
            }
        } else if (source == copyRelBtn) {
            if (studentsCB.getValue() != null) {
                relativesTable.getContainerDataSource().removeAllItems();
                try {
                    DbStudentRelative dbCon = new DbStudentRelative();
                    dbCon.connect();
                    addRelativesToTable(dbCon.getStudentRelatives((Integer) studentsCB.getValue()));
                    dbCon.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            } else {
                Notification.show(myUI.getMessage(Messages.NotificationNothingIsSelected),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == plusInstButton) {
            addInstallmentPlanItem(false);
        } else if (source == plusDiscButton) {
            if (discCounter < 4) {
                addDiscountsItem();
            } else {
                Notification.show(myUI.getMessage(Messages.OnlyThreeDiscountsAllowed),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == plusCorrectionButton) {
            addCorrectionsItem();
        } else if (source == createBtn) {
            if (!myUI.getUser().getCurrent_year().isLast()) {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmActionNotInLastYear)
                                + " (" + myUI.getUser().getCurrent_year().getName()
                                + ") " + myUI.getMessage(Messages.NotCurrentYear) + ".",
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                createBtnAction();
                            }
                        });
            } else {
                createBtnAction();
            }
        } else if (source == deleteBtn) {
            if (studDataTable.getValue() != null) {
                ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                        myUI.getMessage(Messages.ConfirmStudentDeletion)
                                + " " + studDataTable.getContainerProperty(studDataTable.getValue(),
                                myUI.getMessage(Messages.FirstName)).getValue().toString()
                                + " " + studDataTable.getContainerProperty(studDataTable.getValue(),
                                myUI.getMessage(Messages.LastName)).getValue().toString()
                                + "?",
                        myUI.getMessage(Messages.Yes),
                        myUI.getMessage(Messages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete();
                            }
                        });
            }
        } else if (source.getId() != null && source.getId().equals(Settings.download_button)) {
            if (downloader == null) {
                downloader = new SimpleFileDownloader();
                addExtension(downloader);
            }
            downloader.setFileDownloadResource(getFileStream(new File(Settings.PATH_TO_UPLOADS
                    + ((Attachment) source.getData()).getUnique_name())));
            downloader.download();
        } else if (source.getId() != null && source.getId().equals(myUI.getMessage(Messages.Invoice))) {
            InvoiceInfoPdf iip = new InvoiceInfoPdf();
            iip.setStudent_id((Integer) studDataTable.getValue());
            iip.setLeft(ttl_left);
            iip.setPayments(ttl_payment);
            iip.setLogin(loginTF.getValue());
            iip.setClass_name(classCB.getContainerProperty(classCB.getValue(),
                    myUI.getMessage(Messages.Title)).getValue().toString());
            iip.setStudentFullName(nameTF.getValue() + " " + surnameTF.getValue()
                    + " " + middleNameTF.getValue());
            iip.setWhoPaidFullName(((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.WhoPaid)).getValue()).getValue());
            iip.setPayment_date(((DateField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.Date)).getValue()).getValue());
            iip.setAmount((Double) (((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue()));
            iip.setKurs((Double) (((TextField) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.Rate)).getValue()).getPropertyDataSource().getValue()));
            iip.setSchool_name(myUI.getUser().getSchool().getName_ru());
            iip.setPaymentCategoryId((Integer) ((ComboBox) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getValue());
            ComboBox cashBoxCB = (ComboBox) paymentsTable.getContainerProperty(source.getData(),
                    myUI.getMessage(Messages.CashBox)).getValue();
            iip.setCurrency_id((Integer) cashBoxCB.getContainerProperty(cashBoxCB.getValue(), Settings.acc_currency_id).getValue());
            iip.setPayment_type(cashBoxCB.getContainerProperty(cashBoxCB.getValue(), myUI.getMessage(Messages.PaymentType)).getValue().toString());
            try {
                DbStudentPayment dbsp = new DbStudentPayment();
                dbsp.connect();
                iip.setOrder_number(dbsp.getOrderNum((String) source.getData()));
                dbsp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            try {
                DbSchool dbs = new DbSchool();
                dbs.connect();
                iip.setScl_logo(dbs.execGet_logo(loginTF.getValue()));
                dbs.close();
            } catch (Exception ignored) {
            }
            if (iip.getScl_logo() != null) {
                new Invoice2023PDF(myUI, iip);
            } else {
                Notification.show(myUI.getMessage(Messages.NoSchoolLogo),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == saveBtn) {
            try {
                if (Settings.validate(horSplitPanel)) {
                    if (validateRelativesTable(relativesTable)) {
                        if (validateAcsGiveTable(acsGiveTable)) {
                            if (validateAcsReceiveTable(acsReceiveTable)) {
                                if (validateContractsTab(contractTabLay) && validateDiscountsTable() &&
                                        validateCorrectionsTable() && validateInstallmentTable()) {
                                    if (validatePaymentsTable(paymentsTable)) {
                                        DbStudent dbst = new DbStudent();
                                        dbst.connect();
                                        if (isNew) {
                                            loginTF.setValue(generateStudId(
                                                    myUI.getUser().getCurrent_year().getId(),
                                                    myUI.getUser().getCurrent_year().getName()));
                                            Student student = getStudent(0);
                                            int id = dbst.exec_insert(student);
                                            if (id != 0) {
                                                insertNewStOrder(id);
                                                Item relativeItem = null;
                                                if (relativesTable.isEnabled()) {
                                                    relativeItem = insertRelatives(id);
                                                }
                                                addDataContainerItem(id, relativeItem);
                                                Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                                prepareNormalMode();
                                                eduStatCont.getContainerProperty(1, Settings.count)
                                                        .setValue(((Integer) eduStatCont.getContainerProperty(1,
                                                                Settings.count).getValue()) + 1);
                                                eduStatCont.getContainerProperty(6, Settings.count)
                                                        .setValue(((Integer) eduStatCont.getContainerProperty(6, Settings.count)
                                                                .getValue()) + 1);
                                                repaint();
                                            } else {
                                                Notification.show(myUI.getMessage(Messages.CanNotSaveIdNumber),
                                                        Notification.Type.WARNING_MESSAGE);
                                                prepareModificationMode();
                                            }
                                        } else {
                                            if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
                                                int status = 0;
                                                try {
                                                    status = dbst.exec_update(
                                                            getStudent((Integer) studDataTable.getValue()));
                                                } catch (Exception e) {
                                                    logger.error(e);
                                                    logger.catching(e);
                                                }
                                                if (status != 0) {
                                                    Item relativeItem = insertRelatives((Integer) studDataTable.getValue());
                                                    updateDataContainer(relativeItem);
                                                    setRelativesTable();
                                                    prepareNormalMode();
                                                    Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                            Notification.Type.HUMANIZED_MESSAGE);
                                                } else {
                                                    Notification.show(myUI.getMessage(Messages.CanNotSaveIdNumber),
                                                            Notification.Type.WARNING_MESSAGE);
                                                }

                                            } else if (tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
                                                int status = 0;
                                                try {
                                                    status = dbst.exec_update(getStudent((Integer) studDataTable.getValue()));
                                                    Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                            Notification.Type.HUMANIZED_MESSAGE);
                                                } catch (Exception e) {
                                                    logger.error(e);
                                                    logger.catching(e);
                                                }
                                                if (status != 0) {
                                                    updateDataContainer(null);
                                                    prepareNormalMode();
                                                    Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                            Notification.Type.HUMANIZED_MESSAGE);
                                                } else {
                                                    Notification.show(myUI.getMessage(Messages.CanNotSaveIdNumber),
                                                            Notification.Type.WARNING_MESSAGE);
                                                }
                                            } else if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
                                                execDeleteAccessoriesFromDb((Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId(), give);
                                                insertAccessoriesToDb((Integer) studDataTable.getValue(), give);
                                                setMaterialsTable(give);
                                                prepareNormalMode();
                                                Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
                                                execDeleteAccessoriesFromDb((Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId(), receive);
                                                insertAccessoriesToDb((Integer) studDataTable.getValue(), receive);
                                                setMaterialsTable(receive);
                                                prepareNormalMode();
                                                Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } //pressed save button on contract tab
                                            else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                                                insertDiscounts();
                                                insertContractToDb();
                                                execDeleteInstPlanFromDb((Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId());
                                                insertInstPlanToDb((Integer) studDataTable.getValue());
                                                insertCorrections();
                                                setInstPlanTable();
                                                recount();
                                                updateNetPaymentDb(ttl_payment, (Integer) studDataTable.getValue(),
                                                        myUI.getUser().getCurrent_year().getId());
                                                prepareNormalMode();
                                                insertNewStCtrOrder();
                                                updateStudEduStatus();
                                                fileName = null;
                                                Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            } //pressed save button on payments tab
                                            else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()) {
                                                AccTransaction tr = insertTestPayments(new Date());
                                                if (tr != null) {
                                                    Notification.show(myUI.getMessage(Messages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                                                    + " " + tr.getCashbox().getCurrency() + " (" + Settings.df.format(tr.getDate()) + ")",
                                                            Notification.Type.ERROR_MESSAGE);
                                                } else {
                                                    insertPayments((Integer) studDataTable.getValue());
                                                    setPaymentsTable();
                                                    recount();
                                                    updateNetPaymentDb(ttl_payment, (Integer) studDataTable.getValue(),
                                                            myUI.getUser().getCurrent_year().getId());
                                                    prepareNormalMode();
                                                    Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                            Notification.Type.HUMANIZED_MESSAGE);
                                                }
                                            } else if (tabs.getSelectedTab() == tabs.getTab(callsTableLay).getComponent()) {
                                                //save calls
                                                insertCalls((Integer) studDataTable.getValue());
                                                setCallsTable();
                                                prepareNormalMode();
                                                Notification.show(myUI.getMessage(Messages.ValueSaved),
                                                        Notification.Type.HUMANIZED_MESSAGE);
                                            }
                                        }
                                        dbst.close();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            isNew = false;
            clearFields();
            delPayIds.clear();
            delCallIds.clear();
            delDiscIds.clear();
            delCorrectionIds.clear();
            delRelIds.clear();
            if (studDataTable.getValue() != null) {
                fillFields();
            }
            prepareNormalMode();
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
                recountInstPlanLabel();
            }
            fileName = null;
        } else if (source == excelButton) {
            try {
                if (studDataTable.getContainerDataSource().size() != 0) {
                    Table t = new Table();
                    t.setContainerDataSource(studDataTable.getContainerDataSource());
                    t.setVisibleColumns(NATURAL_COL_ORDER);
                    t.setWidth("1px");
                    t.setHeight("1px");
                    t.setVisible(false);
                    buttonsLay.addComponent(t);
                    ExcelExport excelReport = new ExcelExport(t, "sheet1");
                    excelReport.excludeCollapsedColumns();
                    excelReport.setReportTitle(myUI.getMessage(Messages.Students));
                    excelReport.setDisplayTotals(true);
                    excelReport.export();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == printButton) {
            tabs.setSelectedTab(contractTabLay);
            if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                    && studDataTable.getValue() != null) {
                if (contractCB.getValue() != null) {
                    StudentInfoPdf studInfo = new StudentInfoPdf();
                    try {
                        DbStudentInfoPdf dbs = new DbStudentInfoPdf();
                        dbs.connect();
                        studInfo = dbs.execSQL(myUI.getUser().getCurrent_year().getId(),
                                (Integer) studDataTable.getValue());
                        dbs.close();
                        DbEmployee dbEmployee = new DbEmployee();
                        dbEmployee.connect();
                        studInfo.setDirector(dbEmployee.exec_president(1));
                        studInfo.setAccountant(dbEmployee.exec_by_position_id(2, myUI.getUser().getSchool().getId()));
                        dbEmployee.close();
                        DbSchool dbSchool = new DbSchool();
                        dbSchool.connect();
                        studInfo.setSchool(dbSchool.execSchool(myUI.getUser().getSchool().getId()));
                        dbSchool.close();
                        DbStudentRelative dbRel = new DbStudentRelative();
                        dbRel.connect();
                        studInfo.setRelatives(dbRel.allRelativesByStudentId(
                                (Integer) studDataTable.getValue()));
                        dbRel.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    if (instTypeCB.getValue() != null) {
                        studInfo.getContractInfo().setInstallmentPlanType(instTypeCB.getContainerProperty(instTypeCB.getValue(),
                                myUI.getMessage(Messages.Title)).getValue().toString());
                    }
                    if (contractCB.getValue() != null) {
                        studInfo.getContractInfo().setContract((Double) (contractCB.getContainerProperty(contractCB.getValue(),
                                myUI.getMessage(Messages.Amount)).getValue()));
                        studInfo.getContractInfo().setContractTitle(contractCB.getContainerProperty(contractCB.getValue(),
                                myUI.getMessage(Messages.ShortTitle)).getValue().toString());
                    }
                    studInfo.getContractInfo().setDebt(debt);
                    if (discountsTable.size() > 0) {
                        Iterator<?> iter = discountsTable.getItemIds().iterator();
                        StringBuilder allDisc = new StringBuilder();
                        String dis;
                        double count_amount = (Double) (contractCB.getContainerProperty(contractCB.getValue(),
                                myUI.getMessage(Messages.Amount)).getValue());
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            dis = ((((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                            myUI.getMessage(Messages.Title)).getValue().toString()));
                            dis = dis.substring(0, dis.indexOf(" - "));
                            allDisc.append(dis);

                            if (((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                            myUI.getMessage(Messages.DiscountType)).getValue() == 1)
                                    || ((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                            myUI.getMessage(Messages.DiscountType)).getValue() == 3)) {
                                allDisc.append(" - ").append(((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue())
                                        .getPropertyDataSource().getValue().toString()).append("% (").append(Settings.dFormat2.format(count_amount
                                        * ((Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue())
                                        .getPropertyDataSource().getValue()) / 100)).append(" ").append(currency).append(")");
                                count_amount -= count_amount
                                        * ((Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue())
                                        .getPropertyDataSource().getValue()) / 100;
                            } else if (((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                            myUI.getMessage(Messages.DiscountType)).getValue() == 2)
                                    || ((Integer) ((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) discountsTable
                                                    .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                            myUI.getMessage(Messages.DiscountType)).getValue() == 4)) {
                                allDisc.append(" (").append(Settings.dFormat2.format(((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue())
                                        .getPropertyDataSource().getValue())).append(" ").append(currency).append(")");
                                count_amount -= (Double) ((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue())
                                        .getPropertyDataSource().getValue();
                            }
                            if (iter.hasNext()) {
                                allDisc.append(", ");
                            }
                        }
                        studInfo.getContractInfo().setDiscountStr(allDisc.toString());
                    }
                    if (correctionsTable.size() > 0) {
                        Iterator<?> iter = correctionsTable.getItemIds().iterator();
                        StringBuilder allCorrections = new StringBuilder();
                        String dis;
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            dis = ((((ComboBox) correctionsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                    .getContainerProperty(((ComboBox) correctionsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                            myUI.getMessage(Messages.Title)).getValue().toString()));
                            allCorrections.append(dis);
                            allCorrections.append(" (").append(Settings.dFormat2.format(((TextField) correctionsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue())
                                    .getPropertyDataSource().getValue())).append(" ").append(currency).append(")");
                            if (iter.hasNext()) {
                                allCorrections.append(", ");
                            }
                        }
                        studInfo.getContractInfo().setCorrectionStr(allCorrections.toString());
                    }
                    studInfo.getContractInfo().setNet(toPay);
                    studInfo.getContractInfo().setLeft(ttl_left);
                    studInfo.getContractInfo().setContractWithDiscount(contractWithDiscount);
                    studInfo.getContractInfo().setPaid(ttl_payment);
                    studInfo.getContractInfo().setCurrency(currency);
                    if (studInfo.getMainRelative() != null && studInfo.getMainRelative().getFullName() != null) {
                        if (studInfo.getSchool() != null && studInfo.getSchool().getAddress() != null) {
                            if (studInfo.getDirector() != null) {
                                new ContractPdfRu(myUI, studInfo, instPlanCont);
                            } else {
                                Notification.show(myUI.getMessage(Messages.NoDirectorAssigned),
                                        Notification.Type.WARNING_MESSAGE);
                            }
                        } else {
                            Notification.show(myUI.getMessage(Messages.FillSchoolInfo),
                                    Notification.Type.WARNING_MESSAGE);
                        }
                    } else {
                        Notification.show(myUI.getMessage(Messages.FillRelativeInfo),
                                Notification.Type.WARNING_MESSAGE);
                    }
                } else {
                    Notification.show(myUI.getMessage(Messages.SelectContract),
                            Notification.Type.WARNING_MESSAGE);
                }
            } else {
                Notification.show(myUI.getMessage(Messages.SelectContractTab),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else if (source == financialHistoryButton) {
            if (studDataTable.getValue() != null) {
                int st_id = (Integer) studDataTable.getValue();
                myUI.addWindow(new StudentFinancialHistoryWindow(myUI, myUI.getMessage(Messages.FinancialHistory) + " - " +
                        studDataTable.getContainerProperty(st_id, myUI.getMessage(Messages.FirstName)).getValue() + " " +
                        studDataTable.getContainerProperty(st_id, myUI.getMessage(Messages.LastName)).getValue() + "; " +
                        studDataTable.getContainerProperty(st_id, myUI.getMessage(Messages.ClassName)).getValue(), st_id));
            }
        } else if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
            delRelIds.add((String) source.getData());
            relativesTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(callsTableLay).getComponent()) {
            delCallIds.add((String) source.getData());
            callsTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
            acsGiveTable.removeItem(event.getButton().getData().toString());
        } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
            acsReceiveTable.removeItem(event.getButton().getData().toString());
        } else if (source.getId() != null && source.getId().equals(Settings.dbStudentInstallment)) {
            installmentTable.removeItem(event.getButton().getData().toString());
            recountInstPlanLabel();
        } else if (source.getId() != null && source.getId().equals(Settings.dbStudentDiscount)) {
            discCounter--;
            delDiscIds.add(source.getData().toString());
            discountsTable.removeItem(event.getButton().getData().toString());
            recountInstPlanLabel();
        } else if (source.getId() != null && source.getId().equals(Settings.dbStudentCorrection)) {
            delCorrectionIds.add(source.getData().toString());
            correctionsTable.removeItem(event.getButton().getData().toString());
            recountInstPlanLabel();
        } else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent() && source.getCaption() == null) {
            delPayIds.add(source.getData().toString());
            paymentsTable.removeItem(event.getButton().getData().toString());
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

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == studDataTable) {
            if (studDataTable.getItem(studDataTable.getValue()) != null) {
                netContrAmount = 0.0;
                clearFields();
                fillFields();
                recount();
                printButton.setEnabled(true);
                if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmFinancialHistoryInfo)) {
                    financialHistoryButton.setEnabled(true);
                }
                setContractCb(contr_id);
            }
        } else if (property == classCB) {
            if (classCB.getValue() != null) {
                photoUpl.setEnabled(true);
                if (isNew) {
                    loginTF.setValue(generateStudId(myUI.getUser().getCurrent_year().getId(),
                            myUI.getUser().getCurrent_year().getName()));
                }
            } else {
                photoUpl.setEnabled(false);
            }
        } else if (property == contractCB) {
            recountInstPlanLabel();
        } else if (property == instTypeCB) {
            if (instTypeCB.isValid()) {
                changeCurrDateByInstallmentPlanType();
            }
        } else if (property == statusesOG) {
            setStudDataTable(property.getValue().toString());
            repaint();
        } else if (property instanceof TextField && tabs.getSelectedTab() == tabs.getTab(contractTabLay).

                getComponent()
                && ((TextField) property).

                getDescription().

                equals(myUI.getMessage(Messages.Amount))) {
            recountInstPlanLabel();
        } else if (property instanceof CheckBox) {
            if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
                familyTableCheck((Boolean) property.getValue(), ((CheckBox) property).getData().toString());
            }
        } else if (property instanceof ComboBox && ((ComboBox) property).

                getId() != null
                && ((ComboBox) property).

                getId().

                equals(Settings.discount_type_id)) {
            checkDiscountsTable(property);
        } else if (property instanceof ComboBox && ((ComboBox) property).

                getId() != null
                && ((ComboBox) property).

                getId().

                equals(Settings.correction_type_id)) {
            recountInstPlanLabel();
        }
    }

    private void changeCurrDateByInstallmentPlanType() {
        if (instTypeCB.getValue() == null) {
            currDate.setValue(new Date());
            return;
        }

        Item item = instTypeCB.getContainerDataSource().getItem(instTypeCB.getValue());

        if (item == null) {
            currDate.setValue(new Date());
            return;
        }

        String code = (String) item.getItemProperty(
                Settings.installment_plan_type_code
        ).getValue();

        InstallmentPlanTypeCode planType =
                InstallmentPlanTypeCode.fromString(code);

        if (planType.isAutoStartDate()) {

            Integer startMonth = (Integer) item.getItemProperty(
                    Settings.installment_start_month
            ).getValue();

            Integer dueDay = (Integer) item.getItemProperty(
                    Settings.installment_due_day
            ).getValue();

            currDate.setValue(
                    getInstallmentStartDate(startMonth, dueDay)
            );

        } else {
            currDate.setValue(new Date());
        }
    }

    private Date getInstallmentStartDate(Integer startMonth, Integer dueDay) {
        Calendar dateLimit = Calendar.getInstance();
        dateLimit.setTimeInMillis(myUI.getUser().getCurrent_year().getInstallment_date_limit());

        Calendar cal = Calendar.getInstance();

        if (startMonth == null || startMonth <= 0) {
            return new Date();
        }

        int limitYear = dateLimit.get(Calendar.YEAR);
        int limitMonth = dateLimit.get(Calendar.MONTH) + 1;

        int startYear;

        if (startMonth > limitMonth) {
            startYear = limitYear - 1;
        } else {
            startYear = limitYear;
        }

        cal.set(Calendar.YEAR, startYear);
        cal.set(Calendar.MONTH, startMonth - 1);

        if (dueDay != null && dueDay > 0) {
            int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            cal.set(Calendar.DAY_OF_MONTH, Math.min(dueDay, maxDay));
        } else {
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private void prepareModificationMode() {
        if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()
                || tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
            nameTF.setEnabled(true);
            surnameTF.setEnabled(true);
            middleNameTF.setEnabled(true);
            genderCB.setEnabled(true);
            birthDateDF.setEnabled(true);
            classCB.setEnabled(true);
        }
        contractCB.setEnabled(true);
        discountsTable.setEnabled(true);
        correctionsTable.setEnabled(true);
        if (currentUser.isPermitted(Settings.discountsTable + ":" + Settings.actAdd)) {
            plusDiscButton.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.actAdd)) {
            plusCorrectionButton.setEnabled(true);
        }
        divideBtn.setEnabled(true);
        copyRelBtn.setEnabled(true);
        instTypeCB.setEnabled(true);
        plusInstButton.setEnabled(true);
        installmentTable.setEnabled(true);
        currDate.setEnabled(true);
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        printButton.setEnabled(false);
        financialHistoryButton.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        studDataTable.setEnabled(false);
        plusRelButton.setEnabled(true);
        copyRelPopupButton.setEnabled(true);
        plusMatGiveButton.setEnabled(true);
        plusMatReceiveButton.setEnabled(true);
        if (currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.actAdd)) {
            plusPayButton.setEnabled(true);
        }
        plusCallButton.setEnabled(true);
        relativesTable.setEnabled(true);
        paymentsTable.setEnabled(true);
        callsTable.setEnabled(true);
        acsGiveTable.setEnabled(true);
        acsReceiveTable.setEnabled(true);

        for (Object next : ((IndexedContainer) installmentTable
                .getContainerDataSource()).getItemIds()) {
            if ((Integer) installmentTable.getContainerProperty(next, Settings.status_id).getValue() == 0) {
                ((Button) installmentTable.getContainerProperty(next, Settings.button).getValue()).setEnabled(false);
                ((DateField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(Messages.Date)).getValue()).setEnabled(false);
                ((TextField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(Messages.Amount)).getValue()).setEnabled(false);
            } else {
                ((Button) installmentTable.getContainerProperty(next, Settings.button)
                        .getValue()).setEnabled(true);
                ((DateField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(Messages.Date)).getValue()).setEnabled(true);
                ((TextField) installmentTable.getContainerProperty(next,
                        myUI.getMessage(Messages.Amount)).getValue()).setEnabled(true);
            }

        }
        for (Component component : tabs) {
            TabSheet.Tab tab = tabs.getTab(component);
            tab.setEnabled(tab == tabs.getTab(tabs.getSelectedTab()));
        }
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (tabs.getSelectedTab() == tabs.getTab(studSearchLay).getComponent()) {
            if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.actDelete)) {
                deleteBtn.setEnabled(true);
            }
        } else {
            deleteBtn.setEnabled(false);
        }
        if (studDataTable.getValue() != null) {
            printButton.setEnabled(true);
            if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmFinancialHistoryInfo)) {
                financialHistoryButton.setEnabled(true);
            }
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        studDataTable.setEnabled(true);
        nameTF.setEnabled(false);
        surnameTF.setEnabled(false);
        middleNameTF.setEnabled(false);
        genderCB.setEnabled(false);
        birthDateDF.setEnabled(false);
        classCB.setEnabled(false);
        photoUpl.setEnabled(false);
        relativesTable.setEnabled(false);
        plusRelButton.setEnabled(false);
        copyRelPopupButton.setEnabled(false);
        plusPayButton.setEnabled(false);
        plusCallButton.setEnabled(false);
        plusMatGiveButton.setEnabled(false);
        plusMatReceiveButton.setEnabled(false);
        acsGiveTable.setEnabled(false);
        callsTable.setEnabled(false);
        acsReceiveTable.setEnabled(false);
        for (Object next : ((IndexedContainer) paymentsTable
                .getContainerDataSource()).getItemIds()) {
            ((Button) paymentsTable.getContainerProperty(next, Settings.button).getValue()).setEnabled(false);
            ((ComboBox) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.PaymentCategoryType)).getValue()).setEnabled(false);
            ((ComboBox) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.CashBox)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Amount)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Rate)).getValue()).setEnabled(false);
            ((DateField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Date)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.WhoPaid)).getValue()).setEnabled(false);
            ((TextField) paymentsTable.getContainerProperty(next,
                    myUI.getMessage(Messages.Note)).getValue()).setEnabled(false);

        }
        contractCB.setEnabled(false);
        discountsTable.setEnabled(false);
        correctionsTable.setEnabled(false);
        currDate.setEnabled(false);
        instTypeCB.setEnabled(false);
        plusInstButton.setEnabled(false);
        plusDiscButton.setEnabled(false);
        plusCorrectionButton.setEnabled(false);
        installmentTable.setEnabled(false);
        divideBtn.setEnabled(false);
        copyRelBtn.setEnabled(false);
        for (Component component : tabs) {
            TabSheet.Tab tab = tabs.getTab(component);
            if (tab.getComponent() == contractTabLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.contractTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == payTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == callsTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.callsTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == famTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.familyTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == acsGiveTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.giveAccessoriesTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == acsReceiveTableLay) {
                tab.setEnabled(currentUser.isPermitted(Settings.takeAccessoriesTab + ":" + Settings.prmMenu));
            } else if (tab.getComponent() == studSearchLay) {
                tab.setEnabled(true);
            }
        }
    }

    private void fillFields() {
        loginTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(Messages.StudentId)).getValue().toString());
        nameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(Messages.FirstName)).getValue().toString());
        surnameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(Messages.LastName)).getValue().toString());
        if (studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(Messages.MiddleName)).getValue() != null) {
            middleNameTF.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                    studDataTable.getValue(), myUI.getMessage(Messages.MiddleName)).getValue().toString());
        }
        genderCB.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), Settings.gender_id).getValue());
        birthDateDF.setValue((Date) studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), myUI.getMessage(Messages.DateOfBirth)).getValue());
        classCB.removeValueChangeListener(this);
        classCB.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), Settings.class_name_id).getValue());
        statusCB.setValue(studDataTable.getContainerDataSource().getContainerProperty(
                studDataTable.getValue(), Settings.education_status_id).getValue());
        if (studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.Photo)).getValue() != null) {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS
                    + studDataTable.getContainerProperty(studDataTable.getValue(),
                    myUI.getMessage(Messages.Photo)).getValue().toString())));
            photoName = studDataTable.getContainerProperty(studDataTable.getValue(),
                    myUI.getMessage(Messages.Photo)).getValue().toString();
        } else {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
            photoName = null;
        }
        if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
            setRelativesTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
            setMaterialsTable(give);
        } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
            setMaterialsTable(receive);
        } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
            setInstPlanTable();
            setDiscountsTable();
            setCorrectionsTable();
            setContractTab((Integer) studDataTable.getValue(), myUI.getUser().getCurrent_year().getId());
        } else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()) {
            setPaymentsTable();
        } else if (tabs.getSelectedTab() == tabs.getTab(callsTableLay).getComponent()) {
            setCallsTable();
        }
    }

    private void clearFields() {
        loginTF.setValue("");
        nameTF.setValue("");
        surnameTF.setValue("");
        middleNameTF.setValue("");
        genderCB.setValue(null);
        birthDateDF.setValue(null);
        classCB.setValue(null);
        statusCB.setValue(null);
        contractCB.setValue(null);
        instTypeCB.setValue(1);
        currDate.setValue(null);
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoName = null;
        relativesTable.removeAllItems();
        acsGiveTable.removeAllItems();
        acsReceiveTable.removeAllItems();
        installmentTable.removeAllItems();
        paymentsTable.removeAllItems();
        callsTable.removeAllItems();
        discountsTable.removeAllItems();
    }

    private void updateDataContainer(Item relativeItem) {
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.StudentId)).setValue(loginTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.FirstName)).setValue(nameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.LastName)).setValue(surnameTF.getValue());
        if (relativeItem != null) {
            studDataTable.getContainerProperty(studDataTable.getValue(), myUI.getMessage(Messages.Relative)).setValue(
                    ((TextField) relativeItem.getItemProperty(myUI.getMessage(Messages.FullName)).getValue()).getValue());
            studDataTable.getContainerProperty(studDataTable.getValue(), myUI.getMessage(Messages.Phone)).setValue(
                    ((TextField) relativeItem.getItemProperty(myUI.getMessage(Messages.Phone)).getValue()).getValue());
        }
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.MiddleName)).setValue(middleNameTF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.Photo)).setValue(photoName);
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.DateOfBirth)).setValue(birthDateDF.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.class_name_id).setValue(classCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.ClassName)).setValue(classCB
                .getContainerDataSource().getContainerProperty(classCB.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.education_status_id).setValue(statusCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.gender_id).setValue(genderCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.EducationStatus)).setValue(statusCB
                .getContainerDataSource().getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());

    }

    private void addDataContainerItem(int id, Item relativeItem) {
        Item item = ((IndexedContainer) studDataTable.getContainerDataSource()).addItemAt(0, id);
        item.getItemProperty(myUI.getMessage(Messages.StudentId)).setValue(loginTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.FirstName)).setValue(nameTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.LastName)).setValue(surnameTF.getValue());
        if (relativeItem != null) {
            item.getItemProperty(myUI.getMessage(Messages.Relative)).setValue(
                    ((TextField) relativeItem.getItemProperty(myUI.getMessage(Messages.FullName)).getValue()).getValue());
            item.getItemProperty(myUI.getMessage(Messages.Phone)).setValue(
                    ((TextField) relativeItem.getItemProperty(myUI.getMessage(Messages.Phone)).getValue()).getValue());
        }
        item.getItemProperty(myUI.getMessage(Messages.MiddleName)).setValue(middleNameTF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.DateOfBirth)).setValue(birthDateDF.getValue());
        item.getItemProperty(myUI.getMessage(Messages.Photo)).setValue(photoName);
        item.getItemProperty(Settings.gender_id).setValue(genderCB.getValue());
        item.getItemProperty(Settings.class_name_id).setValue(classCB.getValue());
        item.getItemProperty(Settings.entering_year_id).setValue(myUI.getUser().getCurrent_year().getId());
        item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                classCB.getContainerDataSource().getContainerProperty(classCB.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(Settings.education_status_id).setValue(statusCB.getValue());
        item.getItemProperty(myUI.getMessage(Messages.EducationStatus)).setValue(
                statusCB.getContainerDataSource().getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
        item.getItemProperty(myUI.getMessage(Messages.EnteringYear)).setValue(
                myUI.getUser().getCurrent_year().getName());
        studDataTable.clearFilters();
        studDataTable.setValue(id);
    }

    private Student getStudent(int id) {
        Student s = new Student();
        s.setPhoto(photoName);
        s.setLogin(loginTF.getValue());
        s.setPassword(new Sha256Hash(loginTF.getValue()).toString());
        s.setName(nameTF.getValue());
        s.setSurname(surnameTF.getValue());
        s.setMiddle_name(middleNameTF.getValue());
        s.setGender_id((Integer) genderCB.getValue());
        s.setBirth_date(birthDateDF.getValue());
        s.setClass_name_id((Integer) classCB.getValue());
        s.setEdu_status_id((Integer) statusCB.getValue());
        s.setEntering_year_id(myUI.getUser().getCurrent_year().getId());
        s.setSchool_id(myUI.getUser().getSchool().getId());
        s.setEmployee_id(myUI.getUser().getId());
        s.setId(id);
        return s;
    }

    private void execDelete() {
        try {
            DbAccTransactions dbt = new DbAccTransactions();
            dbt.connect();
            DbCashbox dbc = new DbCashbox();
            dbc.connect();
            AccTransaction tr = null;
            for (Object o : dbc.execSQL(myUI).getItemIds()) {
                Integer cashboxId = (Integer) o;
                tr = dbt.exec_allow_delete_by_st_id((Integer) studDataTable.getValue(), myUI.getUser().getSchool().getId(), cashboxId);
                if (tr != null) {
                    break;
                }
            }
            dbc.close();
            if (tr != null) {
                Notification.show(myUI.getMessage(Messages.LowBalance) + Settings.dFormat2.format(tr.getOverLimit())
                                + " " + tr.getCashbox().getCurrency() + " (" + Settings.df.format(tr.getDate()) + ")",
                        Notification.Type.ERROR_MESSAGE);
            } else {
                DbStudent dbst = new DbStudent();
                DbDefinition dbdef = new DbDefinition();
                dbst.connect();
                dbdef.connect();
                dbt.exec_delete_by_st_id((Integer) studDataTable.getValue(), 0, dbt.getConnection());
                dbst.exec_delete((Integer) studDataTable.getValue());
                int st = dbdef.exec_delete((Integer) studDataTable.getValue(), Settings.dbStudent);
                if (st != 0) {
                    clearFields();
                    eduStatCont.getContainerProperty(studDataTable
                                    .getContainerProperty(studDataTable.getValue(),
                                            Settings.education_status_id).getValue(), Settings.count)
                            .setValue(((Integer) eduStatCont.getContainerProperty(studDataTable
                                            .getContainerProperty(studDataTable.getValue(),
                                                    Settings.education_status_id).getValue(),
                                    Settings.count).getValue()) - 1);
                    eduStatCont.getContainerProperty(6, Settings.count)
                            .setValue(((Integer) eduStatCont.getContainerProperty(6, Settings.count)
                                    .getValue()) - 1);
                    repaint();
                    studDataTable.removeItem(studDataTable.getValue());
                    studDataTable.setValue(null);
                    Notification.show(myUI.getMessage(Messages.StudentDeletedSuccessfully),
                            Notification.Type.HUMANIZED_MESSAGE);
                    tabs.setSelectedTab(studDataTable);
                    clearContractInfo();
                }
                dbst.close();
                dbdef.close();
            }
            dbt.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(Messages.CanNotDelete),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void execDeleteAccessoriesFromDb(int stud_id, int year_id, int cat_id) {
        try {
            DbStudentAccessories dbsa = new DbStudentAccessories();
            dbsa.connect();
            dbsa.exec_delete(stud_id, year_id, cat_id);
            dbsa.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void execDeleteInstPlanFromDb(int stud_id, int year_id) {
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            dbip.connect();
            dbip.exec_delete(stud_id, year_id);
            dbip.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void buildUploadWindow() {
        uploadProgressBar = new ProgressBar();
        uploadProgressBar.setWidth("90%");

        statusWindow = new Window(myUI.getMessage(Messages.UploadStatus));
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
        cancelButton.addClickListener(this);
        cancelButton.setId(Settings.cancel_upload_button);
        cancelButton.setVisible(false);
        cancelButton.setIcon(FontAwesome.CLOSE);
        cancelButton.setStyleName(ValoTheme.BUTTON_TINY);
        cancelButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        l.addComponent(cancelButton);
        l.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);

        uploadProgressBar.setCaption(myUI.getMessage(Messages.Progress));
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
                    Notification.show(myUI.getMessage(Messages.OnlyJpg), Notification.Type.WARNING_MESSAGE);
                } else {
                    fileName = null;
                    Button b = (Button) upl.getData();
                    b.setEnabled(false);
                    b.setData(null);
                    Notification.show(myUI.getMessage(Messages.OnlyJpgOrPdf), Notification.Type.WARNING_MESSAGE);
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
                Notification.show(myUI.getMessage(Messages.Maxsize), Notification.Type.WARNING_MESSAGE);
            } else if (myFile.getName().length() > 255) {
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
                Notification.show(myUI.getMessage(Messages.MaxFileName), Notification.Type.WARNING_MESSAGE);
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
                b.setData(null);
                b.setStyleName(ValoTheme.BUTTON_FRIENDLY);
                b.addStyleName(ValoTheme.BUTTON_SMALL);
                Attachment attachment;
                try {
                    attachment = new Attachment();
                    attachment.setUnique_name(myFile.getName());
                    attachment.setExtension(mimeType);
                    attachment.setName(fileName);
                    DbAttachment dbCon = new DbAttachment();
                    dbCon.connect();
                    logger.info(">>> BEFORE INSERT " + attachment);
                    int id = dbCon.exec_insert(attachment);
                    logger.info(">>> ATTACHMENT ID " + id);
                    if (id != 0) {
                        attachment.setId(id);
                        b.setData(attachment);
                    }
                    logger.info(">>> AFTER INSERT " + attachment);
                    dbCon.close();
                } catch (Exception ex) {
                    logger.error(">>> ATTACHMENT");
                    logger.error(ex);
                    logger.catching(ex);
                }
            }
            Notification.show(myUI.getMessage(Messages.UploadedSuccessfully),
                    Notification.Type.TRAY_NOTIFICATION);
        });

        upl.addFailedListener((Upload.FailedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
            Notification.show(myUI.getMessage(Messages.UploadFailed), Notification.Type.ERROR_MESSAGE);
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

    private void setRelativesTable() {
        if (NATURAL_COL_ORDER_RELATIVES == null) {
            NATURAL_COL_ORDER_RELATIVES = new String[]{
                    Settings.button,
                    myUI.getMessage(Messages.RelativeType),
                    myUI.getMessage(Messages.FullName),
                    myUI.getMessage(Messages.Address),
                    myUI.getMessage(Messages.Phone),
                    myUI.getMessage(Messages.Responsible)};
        }
        try {
            DbStudentRelative dbr = new DbStudentRelative();
            dbr.connect();
            relativesTable.setContainerDataSource(
                    dbr.execSQL_St_Rel(myUI, (Integer) studDataTable.getValue(), this));
            dbr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        relativesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_RELATIVES);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.FullName), 1);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.Address), 1);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.RelativeType), 0.5f);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.Responsible), 0.2f);
    }

    public IndexedContainer prepareRelativesContainer() {
        if (relativesContainer == null) {
            relativesContainer = new IndexedContainer();
            relativesContainer.addContainerProperty(Settings.button, Button.class, null);
            relativesContainer.addContainerProperty(
                    myUI.getMessage(Messages.RelativeType), ComboBox.class, null);
            relativesContainer.addContainerProperty(
                    myUI.getMessage(Messages.FullName), TextField.class, null);
            relativesContainer.addContainerProperty(
                    myUI.getMessage(Messages.Phone), TextField.class, null);
            relativesContainer.addContainerProperty(
                    myUI.getMessage(Messages.Address), TextField.class, null);
            relativesContainer.addContainerProperty(
                    myUI.getMessage(Messages.Responsible), CheckBox.class, false);
            relativesContainer.addContainerProperty(
                    Settings.crud_status, String.class, null);
        } else {
            relativesContainer.removeAllItems();
        }
        return relativesContainer;
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

    public TextField createTextField(String value, String description, String itemId, Validator validator, boolean isRequired) {
        TextField tf = new TextField();
        tf.setDescription(description);
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.setData(itemId);

        if (value != null) {
            tf.setValue(value);
        }

        if (isRequired) {
            tf.setRequired(true);
            tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
        }
        if (validator != null) {
            tf.addValidator(validator);
        }
        return tf;
    }

    public TextField createTextFieldDouble(Double value, int digits, String description, String itemId) {
        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        TextField tf = new TextField(property);
        tf.setDescription(description);
        tf.setRequired(true);
        tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.addValidator(new DoubleRangeValidator(
                myUI.getMessage(Messages.NotificationWrongValue), 0.01, null));
        tf.setConverter(Settings.getStringToDoubleConverter(digits));
        tf.setNullRepresentation("0.0");
        tf.setNullSettingAllowed(false);
        tf.setData(itemId);
        tf.getPropertyDataSource().setValue(value);
        tf.addValueChangeListener(this);
        return tf;
    }

    public TextField createTextFieldDisc(Double value, Double maxValue, String description, String itemId,
                                         boolean isDisabled) {
        ObjectProperty<Double> property = new ObjectProperty<>(0.0);
        TextField tf = new TextField(property);
        tf.setDescription(description);
        tf.setRequired(true);
        if (isDisabled) {
            tf.setEnabled(false);
        } else {
            tf.addValidator(new DoubleRangeValidator(
                    myUI.getMessage(Messages.NotificationWrongValue), 0.01, maxValue));
        }
        tf.setRequiredError(myUI.getMessage(Messages.RequiredField));
        tf.setStyleName(ValoTheme.TEXTFIELD_TINY);
        tf.setWidth(Settings.PERCENTS100);
        tf.getPropertyDataSource().setValue(value);
        tf.setNullRepresentation("0.0");
        tf.setNullSettingAllowed(false);
        tf.setConverter(Settings.getStringToDoubleConverter(2));
        tf.setData(itemId);
        tf.addValueChangeListener(this);
        tf.setImmediate(true);
        return tf;
    }

    public DateField createDateField(Date value, String description, String itemId, boolean isFutureAvailable, boolean isRequired,
                                     String format, Resolution resolution) {
        DateField df = new DateField();
        df.setDescription(description);
        df.setStyleName(ValoTheme.DATEFIELD_TINY);
        df.setWidth(Settings.PERCENTS100);
        df.setData(itemId);
        if (isRequired) {
            df.setRequired(true);
            df.setRequiredError(myUI.getMessage(Messages.RequiredField));
        }
        if (!isFutureAvailable) {
            df.setRangeEnd(new Date());
        }
        df.setDateFormat(format);
        df.setResolution(resolution);
        df.setValue(value);
        return df;
    }

    public CheckBox createCheckBox(boolean value, String description, String itemId) {
        CheckBox ckb = new CheckBox();
        ckb.setDescription(description);
        ckb.setValue(value);
        ckb.setData(itemId);
        ckb.addValueChangeListener(this);
        return ckb;
    }

    public ComboBox createCombobox(int value, String description, String itemId,
                                   String db_table, boolean is_disabled) {
        ComboBox cb = new ComboBox();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        if (db_table != null) {
            try {
                DbDefinition dbp = new DbDefinition();
                dbp.connect();
                cb.setContainerDataSource(dbp.exec_for_select(myUI, db_table, true));
                dbp.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        if (is_disabled) {
            cb.setEnabled(false);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        cb.setValue(value);
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBox createComboboxDisc(int value, String description, String itemId) {
        ComboBox cb = new ComboBox();
        cb.addValidator(new ExistsValidator(myUI, discountsTable.getContainerDataSource(),
                cb, myUI.getMessage(Messages.Title)));
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDiscount dbd = new DbDiscount();
            dbd.connect();
            cb.setContainerDataSource(dbd.exec_for_select(
                    myUI, myUI.getUser().getCurrent_year().getId(), value,
                    (Integer) studDataTable.getValue(),
                    studDataTable.getContainerProperty(studDataTable.getValue(),
                            myUI.getMessage(Messages.LastName)).getValue().toString().trim() + " "
                            + studDataTable.getContainerProperty(studDataTable.getValue(),
                            myUI.getMessage(Messages.FirstName)).getValue().toString().trim()));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        cb.setId(Settings.discount_type_id);
        cb.setValue(value);
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBox createComboboxCorr(int value, String description, String itemId) {
        ComboBox cb = new ComboBox();
        cb.setDescription(description);
        cb.setStyleName(ValoTheme.COMBOBOX_TINY);
        cb.setWidth(Settings.PERCENTS100);
        cb.setRequired(true);
        cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
        cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        cb.setFilteringMode(FilteringMode.CONTAINS);
        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            cb.setContainerDataSource(dbd.exec_correction_types(myUI));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setNullSelectionAllowed(false);
        cb.setData(itemId);
        cb.setId(Settings.correction_type_id);
        cb.setValue(value);
        cb.addValueChangeListener(this);
        return cb;
    }

    public ComboBoxMultiselect createComboboxMultiAcs(String value, int cat_id) {
        ComboBoxMultiselect comboMCB = new ComboBoxMultiselect(myUI.getMessage(Messages.Materials));
        comboMCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        comboMCB.setWidth(Settings.PERCENTS100);
        comboMCB.setRequired(true);
        comboMCB.setImmediate(true);
        comboMCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        comboMCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        comboMCB.setShowSelectAllButton((filter, page) -> true);
        try {
            DbAccessories dba = new DbAccessories();
            dba.connect();
            comboMCB.setContainerDataSource(dba.exec_for_select(myUI, cat_id));
            dba.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (value != null) {
            comboMCB.setValue(convertStrToSet(value));
        }
        return comboMCB;
    }

    private Boolean validateContractsTab(ComponentContainer layout) {
        if (tabs.getSelectedTab() == tabs.getTab(layout).getComponent()) {
            return Settings.validate(layout);
        }
        return true;
    }

    private boolean validateRelativesTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(famTableLay).getComponent()) {
            if (t.size() == 0) {
                Notification.show(myUI.getMessage(Messages.NotificationEmptyTable),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                Iterator<?> iter = ((IndexedContainer) relativesTable
                        .getContainerDataSource()).getItemIds().iterator();
                int counter = 0;
                while (iter.hasNext()) {
                    Object next = iter.next();
                    if (((CheckBox) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Responsible)).getValue()).getValue()) {
                        counter++;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.FullName)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Phone)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((TextField) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Address)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBox) relativesTable.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.RelativeType)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
                if (counter != 1) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValueCounter),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
            }
            return true;
        }
        return true;
    }


    private boolean validateAcsGiveTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()) {
            if (t.size() == 0) {
                Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                for (Object next : ((IndexedContainer) t
                        .getContainerDataSource()).getItemIds()) {
                    if (!((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Year)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBoxMultiselect) t.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Materials)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    private boolean validateAcsReceiveTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()) {
            if (t.size() == 0) {
                Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            } else {
                for (Object next : ((IndexedContainer) t
                        .getContainerDataSource()).getItemIds()) {
                    if (!((ComboBox) t.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Year)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                    if (!((ComboBoxMultiselect) t.getItem(next).getItemProperty(
                            myUI.getMessage(Messages.Materials)).getValue()).isValid()) {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    private boolean validatePaymentsTable(Table t) {
        if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()) {
            for (Object next : ((IndexedContainer) t.getContainerDataSource()).getItemIds()) {
                if (!((ComboBox) t.getItem(next).getItemProperty(
                        myUI.getMessage(Messages.CashBox)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((TextField) t.getItem(next).getItemProperty(myUI.getMessage(Messages.Amount)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((TextField) t.getItem(next).getItemProperty(
                        myUI.getMessage(Messages.WhoPaid)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((DateField) t.getItem(next).getItemProperty(
                        myUI.getMessage(Messages.Date)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private void addRelativeItem() {
        if (NATURAL_COL_ORDER_RELATIVES == null) {
            NATURAL_COL_ORDER_RELATIVES = new String[]{
                    Settings.button,
                    myUI.getMessage(Messages.RelativeType),
                    myUI.getMessage(Messages.FullName),
                    myUI.getMessage(Messages.Address),
                    myUI.getMessage(Messages.Phone),
                    myUI.getMessage(Messages.Responsible)};
        }
        String id = Settings.FreshItem + (--r_table_counter);
        if (relativesTable.getContainerDataSource().size() == 0) {
            relativesTable.setContainerDataSource(prepareRelativesContainer());
        }
        Item item;
        item = ((IndexedContainer) relativesTable.getContainerDataSource()).addItemAt(
                relativesTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(Messages.DeleteButton), id,
                        Settings.dbStudentRelatives, FontAwesome.MINUS_SQUARE));
        CheckBox cb = createCheckBox(false, myUI.getMessage(Messages.Responsible), id);
        item.getItemProperty(myUI.getMessage(Messages.Responsible)).setValue(cb);
        item.getItemProperty(myUI.getMessage(Messages.FullName)).setValue(
                createTextField(null, myUI.getMessage(Messages.FullName), id,
                        new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                                1, 250, false), true));
        item.getItemProperty(myUI.getMessage(Messages.Phone)).setValue(
                createTextField(null, myUI.getMessage(Messages.Phone), id,
                        new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                                null, 100, true), false));
        item.getItemProperty(myUI.getMessage(Messages.Address)).setValue(
                createTextField(null, myUI.getMessage(Messages.Address), id,
                        new StringLengthValidator(myUI.getMessage(Messages.NotificationWrongValue),
                                null, 300, true), false));
        item.getItemProperty(myUI.getMessage(Messages.RelativeType)).setValue(
                createCombobox(0, myUI.getMessage(Messages.RelativeType), id,
                        Settings.dbRelatives, false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Insert));

        relativesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_RELATIVES);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.FullName), 1);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.Address), 1);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.RelativeType), 0.5f);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.Responsible), 0.2f);
    }

    private void insertInstPlanToDb(int student_id) {
        double diff;
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbip.connect();
            dbsp.connect();
            for (Object next : ((IndexedContainer) installmentTable
                    .getContainerDataSource()).getItemIds()) {

                StudentInstallmentPlan ip = getInstPlan(student_id, installmentTable.getItem(next));
                if ((Integer) installmentTable.getContainerProperty(next,
                        Settings.status_id).getValue() != 0) {
                    dbip.exec_insert(ip);
                }
            }
            diff = dbsp.exec_get_difference(student_id, myUI.getUser().getCurrent_year().getId());
            if (diff != 0) {
                dbip.exec_insert_notVisible(student_id, myUI.getUser().getCurrent_year().getId(), diff);
            }
            dbsp.close();
            dbip.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertAccessoriesToDb(int student_id, int cat_id) {
        if (cat_id == give) {
            try {
                DbStudentAccessories dbsa = new DbStudentAccessories();
                dbsa.connect();
                for (Object next : ((IndexedContainer) acsGiveTable
                        .getContainerDataSource()).getItemIds()) {
                    String[] acs_ids = Objects.requireNonNull(Settings.convertCollectionToStr((Set<?>) ((ComboBoxMultiselect) (acsGiveTable
                            .getContainerDataSource().getContainerProperty(next,
                                    myUI.getMessage(Messages.Materials))
                            .getValue())).getValue())).split(",");
                    for (String s : acs_ids) {
                        int acs_id = Integer.parseInt(s);
                        StudentAccessories acs = getAccessories(student_id, acs_id, acsGiveTable.getItem(next));
                        dbsa.exec_insert(acs);
                    }
                }
                dbsa.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (cat_id == receive) {
            try {
                DbStudentAccessories dbsa = new DbStudentAccessories();
                dbsa.connect();
                for (Object next : ((IndexedContainer) acsReceiveTable
                        .getContainerDataSource()).getItemIds()) {
                    String[] acs_ids = Objects.requireNonNull(Settings.convertCollectionToStr((Set<?>) ((ComboBoxMultiselect) (acsReceiveTable
                            .getContainerDataSource().getContainerProperty(next,
                                    myUI.getMessage(Messages.Materials))
                            .getValue())).getValue())).split(",");
                    for (String s : acs_ids) {
                        int acs_id = Integer.parseInt(s);
                        StudentAccessories acs = getAccessories(student_id, acs_id, acsReceiveTable.getItem(next));
                        dbsa.exec_insert(acs);
                    }
                }
                dbsa.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    private StudentRelative getRelative(String id, int student_id, Item item) {
        StudentRelative rel = new StudentRelative();
        rel.setStudent_id(student_id);
        rel.setFullName(((TextField) item.getItemProperty(
                myUI.getMessage(Messages.FullName)).getValue()).getValue());
        rel.setPhone(((TextField) item.getItemProperty(
                myUI.getMessage(Messages.Phone)).getValue()).getValue());
        rel.setAddress(((TextField) item.getItemProperty(
                myUI.getMessage(Messages.Address)).getValue()).getValue());
        if (((CheckBox) item.getItemProperty(
                myUI.getMessage(Messages.Responsible)).getValue()).getValue()) {
            rel.setIs_main(1);
        } else {
            rel.setIs_main(0);
        }
        rel.setRelative_id((Integer) ((ComboBox) item.getItemProperty(
                myUI.getMessage(Messages.RelativeType)).getValue()).getValue());

        rel.setId(id);
        return rel;
    }

    private StudentPayment getPayment(int id, int student_id, Item item) {
        StudentPayment sp = new StudentPayment();
        sp.setStudent_id(student_id);
        sp.setYear_id(myUI.getUser().getCurrent_year().getId());
        sp.setPayment_cat_type_id((Integer) ((ComboBox) item.getItemProperty(
                myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getValue());
        ComboBox cashBoxCB = (ComboBox) item.getItemProperty(myUI.getMessage(Messages.CashBox)).getValue();
        sp.setPayment_type_id((Integer) cashBoxCB.getContainerProperty(cashBoxCB.getValue(), Settings.payment_type_id).getValue());
        sp.setCurrency_id((Integer) cashBoxCB.getContainerProperty(cashBoxCB.getValue(), Settings.acc_currency_id).getValue());
        sp.setCashBox_id((Integer) cashBoxCB.getValue());
        sp.setAmount((Double) ((TextField) item.getItemProperty(
                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue());
        sp.setRate((Double) ((TextField) item.getItemProperty(
                myUI.getMessage(Messages.Rate)).getValue()).getPropertyDataSource().getValue());
        sp.setWho_paid(((TextField) item.getItemProperty(
                myUI.getMessage(Messages.WhoPaid)).getValue()).getValue());
        sp.setNote(((TextField) item.getItemProperty(
                myUI.getMessage(Messages.Note)).getValue()).getValue());
        sp.setNoteForCashBox(studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.ClassName)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.StudentId)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.FirstName)).getValue().toString() + " "
                + studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.LastName)).getValue().toString());
        sp.setEmployee_id(myUI.getUser().getId());
        sp.setSchool_id(myUI.getUser().getSchool().getId());
        sp.setModification_date(((DateField) item.getItemProperty(
                myUI.getMessage(Messages.Date)).getValue()).getValue());
        sp.setId(id);
        return sp;
    }

    private StudentInstallmentPlan getInstPlan(int student_id, Item item) {
        StudentInstallmentPlan ip = new StudentInstallmentPlan();
        ip.setStudent_id(student_id);
        ip.setYear_id(myUI.getUser().getCurrent_year().getId());
        ip.setDate_of_payment(((DateField) item.getItemProperty(
                myUI.getMessage(Messages.Date)).getValue()).getValue());
        ip.setAmount((Double) (((TextField) item.getItemProperty(
                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue()));
        ip.setId(0);
        return ip;
    }

    private StudentAccessories getAccessories(int student_id, int acs_id, Item item) {
        StudentAccessories acc = new StudentAccessories();
        acc.setStudent_id(student_id);
        acc.setYear_id((Integer) ((ComboBox) item.getItemProperty(
                myUI.getMessage(Messages.Year)).getValue()).getValue());
        acc.setAccessories_id(acs_id);
        acc.setEmployee_id(myUI.getUser().getId());
        acc.setId(0);
        return acc;
    }

    private void setMaterialsTable(int cat_id) {
        try {
            DbStudentAccessories dbsa = new DbStudentAccessories();
            dbsa.connect();
            if (cat_id == give) {
                acsGiveTable.setContainerDataSource(
                        dbsa.execSQL_St_Acs(myUI, (Integer) studDataTable.getValue(), this, cat_id));
            } else if (cat_id == receive) {
                acsReceiveTable.setContainerDataSource(
                        dbsa.execSQL_St_Acs(myUI, (Integer) studDataTable.getValue(), this, cat_id));
            }
            dbsa.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setPaymentsTable() {
        if (NATURAL_COL_ORDER_PAYMENTS == null) {
            NATURAL_COL_ORDER_PAYMENTS = new String[]{Settings.button,
                    myUI.getMessage(Messages.PaymentCategoryType),
                    myUI.getMessage(Messages.CashBox),
                    myUI.getMessage(Messages.Rate),
                    myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.WhoPaid), myUI.getMessage(Messages.Date),
                    myUI.getMessage(Messages.Note), myUI.getMessage(Messages.Print)};
        }
        try {
            DbStudentPayment dbsa = new DbStudentPayment();
            dbsa.connect();
            paymentsTable.setContainerDataSource(
                    dbsa.execSQL_St_Payments(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsa.close();
            paymentsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PAYMENTS);
            paymentsTable.setColumnWidth(myUI.getMessage(Messages.Rate), 100);
            paymentsTable.setColumnExpandRatio(myUI.getMessage(Messages.WhoPaid), 1);
            paymentsTable.setColumnExpandRatio(myUI.getMessage(Messages.Note), 1);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setCallsTable() {
        if (NATURAL_COL_ORDER_CALLS == null) {
            NATURAL_COL_ORDER_CALLS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Date),
                    myUI.getMessage(Messages.WhoCalled), myUI.getMessage(Messages.Note)};
        }
        try {
            DbStudentCalls dbsc = new DbStudentCalls();
            dbsc.connect();
            callsTable.setContainerDataSource(
                    dbsc.execSQL_St_Calls(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsc.close();
            callsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CALLS);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setInstPlanTable() {
        NATURAL_COL_ORDER_INST_PLAN = new String[]{Settings.button,
                myUI.getMessage(Messages.Date),
                myUI.getMessage(Messages.Amount)};
        try {
            DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
            dbip.connect();
            installmentTable.setContainerDataSource(
                    dbip.execSQL_St_InstPLan(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbip.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        currDate.setValue(new Date());
        installmentTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_INST_PLAN);
    }

    private void setDiscountsTable() {
        if (NATURAL_COL_ORDER_DISCOUNTS == null) {
            NATURAL_COL_ORDER_DISCOUNTS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Title), myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.Note)};
        }
        try {
            DbStudentDiscount dbsd = new DbStudentDiscount();
            dbsd.connect();
            discountsTable.setContainerDataSource(
                    dbsd.execSQL_St_Discounts(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsd.close();

        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        currDate.setValue(new Date());
        discCounter = 0;
        for (int i = 0; i < discountsTable.size(); i++) {
            discCounter++;
        }
        discountsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_DISCOUNTS);
    }

    private void setCorrectionsTable() {
        if (NATURAL_COL_ORDER_CORRECTIONS == null) {
            NATURAL_COL_ORDER_CORRECTIONS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Title), myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.Note)};
        }
        try {
            DbStudentCorrection dbsd = new DbStudentCorrection();
            dbsd.connect();
            correctionsTable.setContainerDataSource(
                    dbsd.execSQLStudentCorrections(myUI, (Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), this));
            dbsd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        correctionsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CORRECTIONS);
    }

    public IndexedContainer prepareMaterialsGivContainer() {
        if (acsGivContainer == null) {
            acsGivContainer = new IndexedContainer();
            acsGivContainer.addContainerProperty(Settings.button, Button.class, null);
            acsGivContainer.addContainerProperty(
                    myUI.getMessage(Messages.Year), ComboBox.class, null);
            acsGivContainer.addContainerProperty(
                    myUI.getMessage(Messages.Materials), ComboBoxMultiselect.class, null);

        } else {
            acsGivContainer.removeAllItems();
        }
        return acsGivContainer;
    }

    public IndexedContainer prepareMaterialsRecContainer() {
        if (acsRecContainer == null) {
            acsRecContainer = new IndexedContainer();
            acsRecContainer.addContainerProperty(Settings.button, Button.class, null);
            acsRecContainer.addContainerProperty(
                    myUI.getMessage(Messages.Year), ComboBox.class, null);
            acsRecContainer.addContainerProperty(
                    myUI.getMessage(Messages.Materials), ComboBoxMultiselect.class, null);

        } else {
            acsRecContainer.removeAllItems();
        }
        return acsRecContainer;
    }

    public IndexedContainer preparePaymentsContainer() {
        if (paymentCont == null) {
            paymentCont = new IndexedContainer();
            paymentCont.addContainerProperty(Settings.button, Button.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.PaymentCategoryType), ComboBox.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.CashBox), ComboBox.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.Rate), TextField.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.Amount), TextField.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.WhoPaid), TextField.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.Date), DateField.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.Note), TextField.class, null);
            paymentCont.addContainerProperty(myUI.getMessage(Messages.Print), Button.class, null);
            paymentCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            paymentCont.removeAllItems();
        }
        return paymentCont;
    }

    public IndexedContainer prepareCallsContainer() {
        if (callsCont == null) {
            callsCont = new IndexedContainer();
            callsCont.addContainerProperty(Settings.button, Button.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(Messages.Date), String.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(Messages.WhoCalled), String.class, null);
            callsCont.addContainerProperty(
                    myUI.getMessage(Messages.Note), TextField.class, null);
            callsCont.addContainerProperty(Settings.crud_status, String.class, null);
        } else {
            callsCont.removeAllItems();
        }
        return callsCont;
    }

    public IndexedContainer prepareDiscountsContainer() {
        if (discountCont == null) {
            discountCont = new IndexedContainer();
            discountCont.addContainerProperty(Settings.button, Button.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(Messages.Title), ComboBox.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(Messages.Amount), TextField.class, null);
            discountCont.addContainerProperty(
                    myUI.getMessage(Messages.Note), TextField.class, null);
            discountCont.addContainerProperty(
                    Settings.crud_status, String.class, null);
        } else {
            discountCont.removeAllItems();
        }
        return discountCont;
    }

    public IndexedContainer prepareCorrectionsContainer() {
        if (correctionCont == null) {
            correctionCont = new IndexedContainer();
            correctionCont.addContainerProperty(Settings.button, Button.class, null);
            correctionCont.addContainerProperty(
                    myUI.getMessage(Messages.Title), ComboBox.class, null);
            correctionCont.addContainerProperty(
                    myUI.getMessage(Messages.Amount), TextField.class, null);
            correctionCont.addContainerProperty(
                    myUI.getMessage(Messages.Note), TextField.class, null);
            correctionCont.addContainerProperty(
                    Settings.crud_status, String.class, null);
        } else {
            correctionCont.removeAllItems();
        }
        return correctionCont;
    }

    public IndexedContainer prepareInstPlanContainer() {
        if (instPlanCont == null) {
            instPlanCont = new IndexedContainer();
            instPlanCont.addContainerProperty(Settings.button, Button.class, null);
            instPlanCont.addContainerProperty(
                    myUI.getMessage(Messages.Date), DateField.class, null);
            instPlanCont.addContainerProperty(
                    myUI.getMessage(Messages.Amount), TextField.class, null);
            instPlanCont.addContainerProperty(Settings.status_id, Integer.class, 0);

        } else {
            instPlanCont.removeAllItems();
        }
        return instPlanCont;
    }

    private void addAccessoriesItem(int cat_id) {
        if (cat_id == give) {
            String id = Settings.FreshItem + (--r_table_counter);
            if (acsGiveTable.getContainerDataSource().size() == 0) {
                acsGiveTable.setContainerDataSource(prepareMaterialsGivContainer());
            }
            Item item;
            item = ((IndexedContainer) acsGiveTable.getContainerDataSource()).addItemAt(
                    acsGiveTable.getContainerDataSource().size(), id);
            item.getItemProperty(Settings.button).setValue(
                    createButton(myUI.getMessage(Messages.DeleteButton), id,
                            Settings.dbStudentAccessories, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Year)).setValue(
                    createCombobox(myUI.getUser().getCurrent_year().getId(), myUI.getMessage(Messages.Year), id,
                            Settings.dbYear, false));
            item.getItemProperty(myUI.getMessage(Messages.Materials)).setValue(
                    createComboboxMultiAcs(null, cat_id));
        } else if (cat_id == receive) {
            String id = Settings.FreshItem + (--r_table_counter);
            if (acsReceiveTable.getContainerDataSource().size() == 0) {
                acsReceiveTable.setContainerDataSource(prepareMaterialsRecContainer());
            }
            Item item;
            item = ((IndexedContainer) acsReceiveTable.getContainerDataSource()).addItemAt(
                    acsReceiveTable.getContainerDataSource().size(), id);
            item.getItemProperty(Settings.button).setValue(
                    createButton(myUI.getMessage(Messages.DeleteButton), id,
                            Settings.dbStudentAccessories, FontAwesome.MINUS_SQUARE));
            item.getItemProperty(myUI.getMessage(Messages.Year)).setValue(
                    createCombobox(myUI.getUser().getCurrent_year().getId(), myUI.getMessage(Messages.Year), id,
                            Settings.dbYear, false));
            item.getItemProperty(myUI.getMessage(Messages.Materials)).setValue(
                    createComboboxMultiAcs(null, cat_id));
        }
    }

    private void addPaymentsItem() {
        if (NATURAL_COL_ORDER_PAYMENTS == null) {
            NATURAL_COL_ORDER_PAYMENTS = new String[]{Settings.button,
                    myUI.getMessage(Messages.PaymentCategoryType),
                    myUI.getMessage(Messages.CashBox),
                    myUI.getMessage(Messages.Rate),
                    myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.WhoPaid), myUI.getMessage(Messages.Date),
                    myUI.getMessage(Messages.Note), myUI.getMessage(Messages.Print)};
        }
        String id = Settings.FreshItem + (--r_table_counter);
        if (paymentsTable.getContainerDataSource().size() == 0) {
            paymentsTable.setContainerDataSource(preparePaymentsContainer());
        }
        Item item;
        item = ((IndexedContainer) paymentsTable.getContainerDataSource()).addItemAt(
                paymentsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(Messages.DeleteButton), id,
                        Settings.dbStudentPayments, FontAwesome.MINUS_SQUARE));
        ComboBox cb = createCombobox(0, myUI.getMessage(Messages.PaymentCategoryType), null, null, false);
        try {
            DbPaymentCategory dbp = new DbPaymentCategory();
            dbp.connect();
            cb.setContainerDataSource(dbp.exec_for_select(myUI, true));
            dbp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        cb.setValue(2);
        cb.setId(myUI.getMessage(Messages.Payments));
        item.getItemProperty(myUI.getMessage(Messages.PaymentCategoryType)).setValue(cb);
        cb = createCombobox(0, myUI.getMessage(Messages.CashBox), id, null, false);
        item.getItemProperty(myUI.getMessage(Messages.CashBox)).setValue(cb);
        TextField tf = createTextFieldDouble(null, 2, myUI.getMessage(Messages.Amount), id);
        tf.setId(myUI.getMessage(Messages.Payments));
        item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(tf);
        tf = createTextFieldDouble(myUI.getDb_currency_rate(), 4, myUI.getMessage(Messages.Rate), id);
        tf.setEnabled(currentUser.hasRole(Settings.rnAdmin));
        item.getItemProperty(myUI.getMessage(Messages.Rate)).setValue(tf);
        String wh_paid = null;
        try {
            DbStudentRelative dbsr = new DbStudentRelative();
            dbsr.connect();
            wh_paid = dbsr.exec_get_who_paid((Integer) studDataTable.getValue());
            dbsr.close();
            DbCashbox dbc = new DbCashbox();
            dbc.connect();
            cb.setContainerDataSource(dbc.execSQLForStudentPayments(myUI));
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        item.getItemProperty(myUI.getMessage(Messages.WhoPaid)).setValue(
                createTextField(wh_paid, myUI.getMessage(Messages.WhoPaid), id, new StringLengthValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), 1, 120, false), true));
        DateField df = createDateField(null, myUI.getMessage(Messages.Date), id, false, true,
                Settings.dateTimeMinPattern, Resolution.MINUTE);
        df.setId(myUI.getMessage(Messages.Payments));
        if (!currentUser.isPermitted(Settings.paymentsTab + ":" + Settings.prmChangeOldTransactions)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -1441);
            df.setRangeStart(calendar.getTime());
            df.addValidator(new DateRangeValidator(myUI.getMessage(Messages.NotificationWrongValue),
                    df.getRangeStart(), df.getRangeEnd(), Resolution.MINUTE));
        }
        item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(df);
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                createTextField(null, myUI.getMessage(Messages.Note), id, new StringLengthValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), null, 100, true), false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Insert));
        paymentsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_PAYMENTS);
        paymentsTable.setColumnExpandRatio(myUI.getMessage(Messages.WhoPaid), 1);
        paymentsTable.setColumnExpandRatio(myUI.getMessage(Messages.Note), 1);
    }

    private void addCallsItem() {
        if (NATURAL_COL_ORDER_CALLS == null) {
            NATURAL_COL_ORDER_CALLS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Date),
                    myUI.getMessage(Messages.WhoCalled), myUI.getMessage(Messages.Note)};
        }
        String id = Settings.FreshItem + (--r_table_counter);
        if (callsTable.getContainerDataSource().size() == 0) {
            callsTable.setContainerDataSource(prepareCallsContainer());
        }
        Item item;
        item = ((IndexedContainer) callsTable.getContainerDataSource()).addItemAt(
                callsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(
                createButton(myUI.getMessage(Messages.DeleteButton), id,
                        Settings.dbStudentCalls, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(Settings.df.format(new Date()));
        item.getItemProperty(myUI.getMessage(Messages.WhoCalled)).setValue(myUI.getUser().getFullName());
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                createTextField(null, myUI.getMessage(Messages.Note), id, new StringLengthValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), null, 100, true), false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Insert));
        callsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CALLS);

    }

    private void addDiscountsItem() {
        if (NATURAL_COL_ORDER_DISCOUNTS == null) {
            NATURAL_COL_ORDER_DISCOUNTS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Title), myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.Note)};
        }
        discCounter++;
        String id = Settings.FreshItem + (--r_table_counter);
        if (discountsTable.getContainerDataSource().size() == 0) {
            discountsTable.setContainerDataSource(prepareDiscountsContainer());
        }
        Item item;
        item = ((IndexedContainer) discountsTable.getContainerDataSource()).addItemAt(
                discountsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(createButton(myUI.getMessage(Messages.DeleteButton), id,
                Settings.dbStudentDiscount, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                createComboboxDisc(0, myUI.getMessage(Messages.Title), id));
        item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                createTextFieldDisc(null, null, myUI.getMessage(Messages.Amount), id, true));
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                createTextField(null, myUI.getMessage(Messages.Note), id, new StringLengthValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), null, 150, true), false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Insert));
        discountsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_DISCOUNTS);
    }

    private void addCorrectionsItem() {
        if (NATURAL_COL_ORDER_CORRECTIONS == null) {
            NATURAL_COL_ORDER_CORRECTIONS = new String[]{Settings.button,
                    myUI.getMessage(Messages.Title), myUI.getMessage(Messages.Amount),
                    myUI.getMessage(Messages.Note)};
        }
        String id = Settings.FreshItem + (--r_table_counter);
        if (correctionsTable.getContainerDataSource().size() == 0) {
            correctionsTable.setContainerDataSource(prepareCorrectionsContainer());
        }
        Item item;
        item = ((IndexedContainer) correctionsTable.getContainerDataSource()).addItemAt(
                correctionsTable.getContainerDataSource().size(), id);
        item.getItemProperty(Settings.button).setValue(createButton(myUI.getMessage(Messages.DeleteButton), id,
                Settings.dbStudentCorrection, FontAwesome.MINUS_SQUARE));
        item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                createComboboxCorr(0, myUI.getMessage(Messages.Title), id));
        item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                createTextFieldDouble(null, 2, myUI.getMessage(Messages.Amount), id));
        item.getItemProperty(myUI.getMessage(Messages.Note)).setValue(
                createTextField(null, myUI.getMessage(Messages.Note), id, new StringLengthValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), null, 250, false), false));
        item.getItemProperty(Settings.crud_status).setValue(myUI.getMessage(Messages.Insert));
        correctionsTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_CORRECTIONS);
    }

    private void addInstallmentPlanItem(Boolean autoFill) {
        NATURAL_COL_ORDER_INST_PLAN = new String[]{Settings.button,
                myUI.getMessage(Messages.Date),
                myUI.getMessage(Messages.Amount)};

        if (!autoFill) {
            String id = Settings.FreshItem + (--r_table_counter);

            if (installmentTable.getContainerDataSource().size() == 0) {
                installmentTable.setContainerDataSource(prepareInstPlanContainer());
            }

            Item item = ((IndexedContainer) installmentTable.getContainerDataSource()).addItemAt(
                    installmentTable.getContainerDataSource().size(), id);

            item.getItemProperty(Settings.button).setValue(
                    createButton(myUI.getMessage(Messages.DeleteButton), id,
                            Settings.dbStudentInstallment, FontAwesome.MINUS_SQUARE));

            DateField df = createDateField(currDate.getValue(), myUI.getMessage(Messages.Date),
                    id, true, true, Settings.datePattern, Resolution.DAY);

            if (myUI.getUser().getSchool().getSchool_type_id() == 6) {
                df.setRangeEnd(new Date(myUI.getUser().getCurrent_year().getInstallment_date_limit()));
            }

            item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(df);
            item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                    createTextFieldDouble(null, 2, myUI.getMessage(Messages.Amount), id));
            item.getItemProperty(Settings.status_id).setValue(1);

        } else if (instCtrAmount != null) {

            Item planTypeItem = getSelectedInstallmentPlanTypeItem();

            if (planTypeItem == null) {
                return;
            }

            Boolean isCustom = (Boolean) planTypeItem.getItemProperty(
                    Settings.installment_is_custom).getValue();

            if (isCustom != null && isCustom) {
                return;
            }

            Integer paymentCount = (Integer) planTypeItem.getItemProperty(
                    Settings.installment_payment_count).getValue();

            Integer intervalMonths = (Integer) planTypeItem.getItemProperty(
                    Settings.installment_payment_interval_months).getValue();

            Integer dueDay = (Integer) planTypeItem.getItemProperty(
                    Settings.installment_due_day).getValue();

            if (paymentCount == null || paymentCount <= 0) {
                return;
            }

            if (intervalMonths == null || intervalMonths < 0) {
                intervalMonths = 1;
            }

            installmentTable.removeAllItems();

            Iterator<?> iter = instPlanCont.getItemIds().iterator();
            Double s = 0.0;
            Double left = instCtrAmount;

            while (iter.hasNext()) {
                Object next = iter.next();

                if (((TextField) instPlanCont.getContainerProperty(next,
                        myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                        && !((TextField) instPlanCont.getContainerProperty(next,
                        myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue().equals("")) {

                    s += (Double) ((TextField) instPlanCont.getContainerProperty(next,
                            myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue();
                }
            }

            left -= s;

            if (left <= 0) {
                return;
            }

            double divSum = Settings.round(left / paymentCount, 2);
            double addedSum = 0.0;

            for (int i = 0; i < paymentCount; i++) {

                double currentAmount;

                if (i == paymentCount - 1) {
                    currentAmount = Settings.round(left - addedSum, 2);
                } else {
                    currentAmount = divSum;
                }

                Calendar dateLimit = Calendar.getInstance();
                dateLimit.setTimeInMillis(myUI.getUser().getCurrent_year().getInstallment_date_limit());

                Calendar cal = Calendar.getInstance();
                cal.setTime(currDate.getValue());

                cal.add(Calendar.MONTH, i * intervalMonths);

                if (dueDay != null && dueDay > 0) {
                    int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    cal.set(Calendar.DAY_OF_MONTH, Math.min(dueDay, maxDay));
                }

                if (installmentTable.getContainerDataSource().size() == 0) {
                    installmentTable.setContainerDataSource(prepareInstPlanContainer());
                }

                if (dateLimit.after(cal) || myUI.getUser().getSchool().getSchool_type_id() != 6) {
                    String id = Settings.FreshItem + (--r_table_counter);

                    Item item = ((IndexedContainer) installmentTable.getContainerDataSource()).addItemAt(
                            installmentTable.getContainerDataSource().size(), id);

                    item.getItemProperty(Settings.button).setValue(
                            createButton(myUI.getMessage(Messages.DeleteButton), id,
                                    Settings.dbStudentInstallment, FontAwesome.MINUS_SQUARE));

                    DateField df = createDateField(cal.getTime(), myUI.getMessage(Messages.Date), id,
                            true, true, Settings.datePattern, Resolution.DAY);

                    if (myUI.getUser().getSchool().getSchool_type_id() == 6) {
                        df.setRangeEnd(new Date(myUI.getUser().getCurrent_year().getInstallment_date_limit()));
                    }

                    item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(df);
                    item.getItemProperty(myUI.getMessage(Messages.Amount)).setValue(
                            createTextFieldDouble(currentAmount, 2, myUI.getMessage(Messages.Amount), id));
                    item.getItemProperty(Settings.status_id).setValue(1);

                    addedSum += currentAmount;
                } else {
                    double restAmount = Settings.round(left - addedSum, 2);
                    addAmountToLastInstallmentPlanItem(restAmount);
                    break;
                }
            }
        }

        installmentTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_INST_PLAN);
    }

    private Item getSelectedInstallmentPlanTypeItem() {
        if (instTypeCB.getValue() == null) {
            return null;
        }

        return instTypeCB.getContainerDataSource().getItem(
                instTypeCB.getValue());
    }

    private void addAmountToLastInstallmentPlanItem(Double amount) {
        if (amount == null || amount <= 0) {
            return;
        }

        IndexedContainer container = (IndexedContainer) installmentTable.getContainerDataSource();

        if (container == null || container.size() == 0) {
            return;
        }

        Object lastItemId = null;

        Iterator<?> iter = container.getItemIds().iterator();
        while (iter.hasNext()) {
            lastItemId = iter.next();
        }

        if (lastItemId == null) {
            return;
        }

        Item item = container.getItem(lastItemId);

        if (item == null) {
            return;
        }

        TextField tf = (TextField) item.getItemProperty(
                myUI.getMessage(Messages.Amount)).getValue();

        Double oldAmount = 0.0;

        if (tf.getPropertyDataSource().getValue() != null
                && !tf.getPropertyDataSource().getValue().equals("")) {
            oldAmount = (Double) tf.getPropertyDataSource().getValue();
        }

        tf.getPropertyDataSource().setValue(
                Settings.round(oldAmount + amount, 2));
    }

    private Set<?> convertStrToSet(String str) {
        String[] strArr = str.split(",");
        HashSet<String> hs = new HashSet<>(strArr.length);
        Collections.addAll(hs, strArr);
        return hs;
    }

    private void buildContractTab() {
        contractTabLay = new GridLayout(2, 1);
        contractTabLay.setMargin(true);
        contractTabLay.setSpacing(true);
        contractTabLay.setSizeFull();

        installmentTable = new FormattedTable(myUI);
        installmentTable.setSizeFull();
        installmentTable.setSelectable(false);
        installmentTable.setStyleName(ValoTheme.TABLE_SMALL);

        discountsTable = new FormattedTable(myUI);
        discountsTable.setSizeFull();
        discountsTable.setSelectable(false);
        discountsTable.setStyleName(ValoTheme.TABLE_SMALL);

        correctionsTable = new FormattedTable(myUI);
        correctionsTable.setSizeFull();
        correctionsTable.setSelectable(false);
        correctionsTable.setStyleName(ValoTheme.TABLE_SMALL);

        contractCB = new ComboBox(myUI.getMessage(Messages.Contract));
        contractCB.setWidth(Settings.PERCENTS100);
        contractCB.setNullSelectionAllowed(false);
        contractCB.setRequired(true);
        contractCB.addValueChangeListener(this);
        contractCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        contractCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        contractCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        contractCB.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbContract dbc = new DbContract();
            dbc.connect();
            contractCB.setContainerDataSource(
                    dbc.exec_contr_select(myUI, myUI.getUser().getCurrent_year().getId(),
                            myUI.getUser().getSchool().getId(), contr_id));
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        VerticalLayout leftVL = new VerticalLayout();
        leftVL.setSpacing(true);
        leftVL.setSizeFull();
        leftVL.addComponent(contractCB);

        VerticalLayout rightVL = new VerticalLayout();
        rightVL.setSpacing(true);
        rightVL.setSizeFull();

        contractTabLay.addComponent(leftVL, 0, 0);
        contractTabLay.addComponent(rightVL, 1, 0);

        currDate = new DateField(myUI.getMessage(Messages.StartDate));
        currDate.setStyleName(ValoTheme.DATEFIELD_TINY);
        currDate.setDateFormat(Settings.datePattern);

        instTypeCB = new ComboBox(myUI.getMessage(Messages.InstallmentPlanType));
        instTypeCB.setWidth(Settings.PERCENTS100);
        instTypeCB.setNullSelectionAllowed(false);
        instTypeCB.setRequired(true);
        instTypeCB.setStyleName(ValoTheme.COMBOBOX_TINY);
        instTypeCB.setRequiredError(myUI.getMessage(Messages.RequiredField));
        instTypeCB.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        instTypeCB.setFilteringMode(FilteringMode.CONTAINS);
        instTypeCB.addValueChangeListener(this);
        try {
            DbInstallmentPlanType dbc = new DbInstallmentPlanType();
            dbc.connect();
            instTypeCB.setContainerDataSource(dbc.exec_for_select(myUI));
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        divideBtn = new Button(myUI.getMessage(Messages.DivideButton));
        divideBtn.setStyleName(ValoTheme.BUTTON_TINY);
        divideBtn.setIcon(FontAwesome.CHECK);
        divideBtn.addClickListener(this);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setWidth(Settings.PERCENTS100);
        hl.addComponent(instTypeCB);
        hl.addComponent(currDate);
        hl.addComponent(divideBtn);
        hl.setComponentAlignment(divideBtn, Alignment.BOTTOM_LEFT);
        hl.setExpandRatio(instTypeCB, 1);
        rightVL.addComponent(hl);

        Label captionInst = new Label();
        captionInst.setSizeFull();
        captionInst.setContentMode(ContentMode.HTML);
        captionInst.setValue(myUI.getMessage(Messages.InstallmentPlan));
        captionInst.setStyleName("tableCpt");

        hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);
        hl.setSpacing(true);
        hl.addComponent(plusInstButton);
        hl.addComponent(captionInst);
        hl.setExpandRatio(captionInst, 1);

        rightVL.addComponent(hl);
        rightVL.addComponent(installmentTable);
        rightVL.addComponent(buildInstPlanInfoLayout());
        rightVL.setExpandRatio(installmentTable, 1);
        if (currentUser.isPermitted(Settings.discountsTable + ":" + Settings.prmMenu) ||
                currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.prmMenu)) {

            VerticalLayout discountsLay = new VerticalLayout();
            discountsLay.setMargin(new MarginInfo(true, false, false, false));
            discountsLay.setSpacing(true);
            discountsLay.setSizeFull();
            discountsLay.addComponent(plusDiscButton);
            discountsLay.setComponentAlignment(plusDiscButton, Alignment.BOTTOM_LEFT);
            discountsLay.addComponent(discountsTable);
            discountsLay.setExpandRatio(discountsTable, 1);

            VerticalLayout correctionsLay = new VerticalLayout();
            correctionsLay.setMargin(new MarginInfo(true, false, false, false));
            correctionsLay.setSpacing(true);
            correctionsLay.setSizeFull();
            correctionsLay.addComponent(plusCorrectionButton);
            correctionsLay.setComponentAlignment(plusCorrectionButton, Alignment.BOTTOM_LEFT);
            correctionsLay.addComponent(correctionsTable);
            correctionsLay.setExpandRatio(correctionsTable, 1);

            TabSheet discountsCorrectionsTab = new TabSheet();
            discountsCorrectionsTab.setSizeFull();
            if (currentUser.isPermitted(Settings.discountsTable + ":" + Settings.prmMenu)) {
                discountsCorrectionsTab.addTab(discountsLay, myUI.getMessage(Messages.Discounts));
            }
            if (currentUser.isPermitted(Settings.correctionsTable + ":" + Settings.prmMenu)) {
                discountsCorrectionsTab.addTab(correctionsLay, myUI.getMessage(Messages.Corrections));
            }
            discountsCorrectionsTab.setSelectedTab(discountsLay);
            leftVL.addComponent(discountsCorrectionsTab);
            leftVL.setExpandRatio(discountsCorrectionsTab, 1);
        }
    }

    private Boolean validateDiscountsTable() {
        for (Object obj : discountsTable.getItemIds()) {
            if (!((TextField) discountsTable.getItem(obj).getItemProperty(
                    myUI.getMessage(Messages.Amount)).getValue()).isValid()) {
                Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }
            if (!((ComboBox) discountsTable.getItem(obj).getItemProperty(
                    myUI.getMessage(Messages.Title)).getValue()).isValid()) {
                Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private Boolean validateCorrectionsTable() {
        if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
            for (Object obj : correctionCont.getItemIds()) {
                if (!((TextField) correctionCont.getItem(obj).getItemProperty(
                        myUI.getMessage(Messages.Amount)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((ComboBox) correctionCont.getItem(obj).getItemProperty(
                        myUI.getMessage(Messages.Title)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    private Boolean validateInstallmentTable() {
        if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()) {
            ArrayList<String> dates = new ArrayList<>();
            Iterator<?> iter = instPlanCont.getItemIds().iterator();
            Double amount = 0.0;
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (!((TextField) installmentTable.getItem(obj).getItemProperty(
                        myUI.getMessage(Messages.Amount)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (!((DateField) installmentTable.getItem(obj).getItemProperty(
                        myUI.getMessage(Messages.Date)).getValue()).isValid()) {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                    return false;
                }
                if (((DateField) installmentTable.getItem(obj).getItemProperty(
                        myUI.getMessage(Messages.Date)).getValue()).isValid()) {
                    if (dates.contains(Settings.df.format(((DateField) installmentTable.getItem(obj).getItemProperty(
                            myUI.getMessage(Messages.Date)).getValue()).getValue()))) {
                        Notification.show(myUI.getMessage(Messages.NotificationSameDatesAreNotAllowed),
                                Notification.Type.WARNING_MESSAGE);
                        return false;
                    } else if ((Integer) installmentTable.getItem(obj).getItemProperty(Settings.status_id).getValue() == 1) {
                        dates.add(Settings.df.format(((DateField) installmentTable.getItem(obj).getItemProperty(
                                myUI.getMessage(Messages.Date)).getValue()).getValue()));
                    }
                }
                if ((Integer) installmentTable.getItem(obj).getItemProperty(
                        Settings.status_id).getValue() != 0) {
                    amount += (Double) (((TextField) installmentTable.getItem(obj).getItemProperty(
                            myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue());
                }
            }
            recount();
            if (Settings.round(instCtrAmount, 2) != Settings.round(amount, 2)) {
                Notification.show(myUI.getMessage(Messages.NotificationWrongSumInstSum),
                        Notification.Type.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void insertContractToDb() {
        try {
            DbStudentContract dbc = new DbStudentContract();
            dbc.connect();
            int status = dbc.exec_insert_st_contract(myUI, getStudentContract((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId()));
            if (status == 0) {
                dbc.exec_update_st_contract(getStudentContract((Integer) studDataTable.getValue(),
                        myUI.getUser().getCurrent_year().getId()));
            }
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private StudentContract getStudentContract(int st_id, int year_id) {
        double debt = 0.0;
        try {
            DbStudentContract dbsc = new DbStudentContract();
            dbsc.connect();
            debt = dbsc.exec_get_debt((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        StudentContract c = new StudentContract();
        c.setStudent_id(st_id);
        c.setYear_id(year_id);
        c.setContract_id((Integer) contractCB.getValue());
        c.setInstallmentPlanTypeId((Integer) instTypeCB.getValue());
        c.setDebt(debt);
        c.setEmployee_id(myUI.getUser().getId());
        c.setStatus_id(2);
        c.setContr_with_disc(contr_with_disc);

        return c;
    }

    private void setContractTab(int st_id, int year_id) {
        StudentContract sc = null;
        try {
            DbStudentContract dbsc = new DbStudentContract();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbsc.connect();
            dbsp.connect();
            sc = dbsc.execSQL(st_id, year_id);
            dbsc.close();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (sc != null) {
            contractCB.removeValueChangeListener(this);
            contractCB.setValue(sc.getContract_id());
            contractCB.addValueChangeListener(this);
            instTypeCB.removeValueChangeListener(this);
            instTypeCB.setValue(sc.getInstallmentPlanTypeId());
            instTypeCB.addValueChangeListener(this);
        }
    }

    private StudentDiscount getStudentDiscount(int st_id, int year_id, String disc_id) {
        StudentDiscount d = new StudentDiscount();
        if ((((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue())
                .getContainerProperty(((ComboBox) discountsTable
                                .getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                        myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("3"))
                || (((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue())
                .getContainerProperty(((ComboBox) discountsTable
                                .getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                        myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("4"))) {
            d.setFree_entry_amount((Double) (((TextField) discountsTable.getContainerProperty(disc_id,
                    myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue()));
        }
        d.setDiscount_id(Integer.parseInt(((ComboBox) discountsTable
                .getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue()).getValue().toString()));
        d.setStudent_id(st_id);
        d.setEmployee_id(myUI.getUser().getId());
        d.setYear_id(year_id);
        d.setId(disc_id);
        d.setNote(((TextField) discountsTable
                .getContainerProperty(disc_id, myUI.getMessage(Messages.Note)).getValue()).getValue());
        if (contractCB.getValue() != null) {
            discountAmount = (Double) (((TextField) discountsTable.getContainerProperty(disc_id,
                    myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue());
            if ((((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("1"))
                    || (((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("3"))) {
                d.setDiscount_value(Settings.round((contr_with_disc * discountAmount / 100), 2));
                contr_with_disc -= contr_with_disc * discountAmount / 100;
            } else if ((((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("2"))
                    || (((ComboBox) discountsTable.getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(((ComboBox) discountsTable
                                    .getContainerProperty(disc_id, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("4"))) {
                d.setDiscount_value(Settings.round((discountAmount), 2));
                contr_with_disc = contr_with_disc - discountAmount;
            }
        }
        return d;
    }

    private StudentCorrection getStudentCorrection(int st_id, int year_id, String corr_id) {
        StudentCorrection c = new StudentCorrection();
        c.setAmount((Double) (((TextField) correctionsTable.getContainerProperty(corr_id,
                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue()));
        c.setCorrection_type_id(Integer.parseInt(((ComboBox) correctionsTable
                .getContainerProperty(corr_id, myUI.getMessage(Messages.Title)).getValue()).getValue().toString()));
        c.setStudent_id(st_id);
        c.setEmployee_id(myUI.getUser().getId());
        c.setYear_id(year_id);
        c.setId(corr_id);
        c.setNote(((TextField) correctionsTable
                .getContainerProperty(corr_id, myUI.getMessage(Messages.Note)).getValue()).getValue());
        return c;
    }

    private void recount() {
        StudentContract studentContract = new StudentContract();
        StudentPayment sp;
        IndexedContainer discCont = new IndexedContainer();
        try {
            DbStudentContract dbsc = new DbStudentContract();
            DbStudentDiscount dbsd = new DbStudentDiscount();
            DbStudentPayment dbsp = new DbStudentPayment();
            dbsc.connect();
            dbsd.connect();
            dbsp.connect();
            studentContract = dbsc.exec_recount_contract((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            discCont = dbsd.exec_disc_strCont(myUI, (Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            debt = dbsc.exec_get_debt((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            sp = dbsp.exec_recount_payment((Integer) studDataTable.getValue(),
                    myUI.getUser().getCurrent_year().getId());
            ttl_payment = sp.getTtl_pay();
            contract_amount = studentContract.getAmount();
            toPay = studentContract.getContr_with_disc() + studentContract.getCorrection() + debt;
            contractWithDiscount = studentContract.getContr_with_disc();
            ttl_left = (studentContract.getContr_with_disc() + studentContract.getCorrection() + debt) - ttl_payment;
            dbsc.close();
            dbsd.close();
            dbsp.close();
            contr_id = studentContract.getContract_id();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        discountsStr = new StringBuilder();
        Iterator<?> iter = discCont.getItemIds().iterator();
        while (iter.hasNext()) {
            Object next = iter.next();
            if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 1) {
                discountsStr.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(Messages.Amount)).getValue())).append("%");
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 2) {
                discountsStr.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(Messages.Amount)).getValue())).append(currency);
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 3) {
                discountsStr.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(Messages.FreeAmount)).getValue())).append("%");
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            } else if ((Integer) discCont.getContainerProperty(next,
                    Settings.discount_type_id).getValue() == 4) {
                discountsStr.append(Settings.dFormat2.format(discCont.getContainerProperty(next,
                        myUI.getMessage(Messages.FreeAmount)).getValue())).append(" ").append(currency);
                if (iter.hasNext()) {
                    discountsStr.append(", ");
                }
            }
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmContractInfo)) {
            String value = myUI.getMessage(Messages.Contract) + ": " +
                    Settings.dFormat2.format(studentContract.getAmount()) + " " + currency;
            if (studentContract.getCreationDate() != null) {
                value += " (" + Settings.df.format(studentContract.getCreationDate()) + ")";
            }
            contractLab.setValue(value);
            discountLab.setValue(myUI.getMessage(Messages.Discounts) + ": " + discountsStr);
            correctionLab.setValue(myUI.getMessage(Messages.Corrections) + ": " + (studentContract.getCorrectionDetails() == null ? "0.00 " + currency : studentContract.getCorrectionDetails()));
            if (debt > 0) {
                debtLab.setStyleName(ValoTheme.LABEL_FAILURE);
            } else {
                debtLab.setStyleName(ValoTheme.LABEL_SUCCESS);
            }
            debtLab.setValue(myUI.getMessage(Messages.PreviousYearDebt) + ": " + Settings.dFormat2.format(debt) + " " + currency);
            netLab.setValue(myUI.getMessage(Messages.Net) + ": " + Settings.dFormat2.format(studentContract.getContr_with_disc() + studentContract.getCorrection() + debt) + " " + currency);
            double paidPercentage = 0.0;
            if (studentContract.getContr_with_disc() +
                    studentContract.getCorrection() + debt != 0) {
                paidPercentage = ttl_payment * 100 / (studentContract.getContr_with_disc() + studentContract.getCorrection() + debt);
            }
            paidLab.setValue(myUI.getMessage(Messages.Paid) + ": " + Settings.dFormat2.format(ttl_payment) + " " + currency
                    + " (" + Settings.dFormat2.format(paidPercentage) + "%)");
        }
        if (currentUser.isPermitted(Settings.cnStudentDefinitionView + ":" + Settings.prmContractInfoLeftDebt)) {
            leftLab.setValue(myUI.getMessage(Messages.Left) + ": " + Settings.dFormat2.format((studentContract.getContr_with_disc() + studentContract.getCorrection() + debt) - ttl_payment) + " " + currency);
            if ((studentContract.getPlan_debt() - ttl_payment) > 0) {
                planDebt.setStyleName(ValoTheme.LABEL_FAILURE);
                planDebt.setValue(myUI.getMessage(Messages.InstPlanDebt) + ": " + Settings.dFormat2.format(studentContract.getPlan_debt() - ttl_payment) + " " + currency);
            } else {
                planDebt.setStyleName(ValoTheme.LABEL_SUCCESS);
                planDebt.setValue(myUI.getMessage(Messages.InstPlanDebt) + ": " + Settings.dFormat2.format(0.0) + " " + currency);
            }
        }
    }

    private void updateStudEduStatus() {
        int eduStatusActive = 2;
        statusCB.setValue(eduStatusActive);
        eduStatCont.getContainerProperty(studDataTable
                        .getContainerProperty(studDataTable.getValue(),
                                Settings.education_status_id).getValue(), Settings.count)
                .setValue(((Integer) eduStatCont.getContainerProperty(studDataTable
                                .getContainerProperty(studDataTable.getValue(),
                                        Settings.education_status_id).getValue(),
                        Settings.count).getValue()) - 1);
        eduStatCont.getContainerProperty(2, Settings.count)
                .setValue(((Integer) eduStatCont.getContainerProperty(2,
                        Settings.count).getValue()) + 1);
        repaint();
        studDataTable.getContainerProperty(studDataTable.getValue(),
                Settings.education_status_id).setValue(statusCB.getValue());
        studDataTable.getContainerProperty(studDataTable.getValue(),
                myUI.getMessage(Messages.EducationStatus)).setValue(statusCB
                .getContainerProperty(statusCB.getValue(),
                        myUI.getMessage(Messages.Title)).getValue().toString());
    }

    private void addRowIfTableEmpty() {
        if (tabs.getSelectedTab() == tabs.getTab(acsGiveTableLay).getComponent()
                && acsGiveTable.size() == 0) {
            plusMatGiveButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(acsReceiveTableLay).getComponent()
                && acsReceiveTable.size() == 0) {
            plusMatReceiveButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(contractTabLay).getComponent()
                && installmentTable.size() == 0) {
            plusInstButton.click();
        } else if (tabs.getSelectedTab() == tabs.getTab(payTableLay).getComponent()
                && paymentsTable.size() == 0) {
            plusPayButton.click();
        }
    }

    private void recountInstPlanLabel() {
        if (contractCB.getValue() != null) {
            instCtrAmount = 0.0;
            netContrAmount = 0.0;
            instPlanContSum = 0.0;
            instCtrAmount = Double.parseDouble(contractCB.getContainerProperty(contractCB.getValue(),
                    myUI.getMessage(Messages.Amount)).getValue().toString());

            if (discountsTable.size() > 0) {
                for (Object next : discountsTable.getItemIds()) {
                    if (((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                            && (!((TextField) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue().equals(""))) {
                        discountAmount = (Double) (((TextField) discountsTable.getContainerProperty(next,
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue());
                        if ((((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                        myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("1"))
                                || (((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                        myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("3"))) {
                            instCtrAmount -= instCtrAmount * discountAmount / 100;
                        } else if ((((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                        myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("2"))
                                || (((ComboBox) discountsTable.getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue())
                                .getContainerProperty(((ComboBox) discountsTable
                                                .getContainerProperty(next, myUI.getMessage(Messages.Title)).getValue()).getValue(),
                                        myUI.getMessage(Messages.DiscountType)).getValue().toString().equals("4"))) {
                            instCtrAmount = instCtrAmount - discountAmount;
                        }
                    }
                }
            }

            if (correctionsTable.size() > 0) {
                for (Object next : correctionsTable.getItemIds()) {
                    if (((TextField) correctionsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue() != null
                            && (!((TextField) correctionsTable.getContainerProperty(next, myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue().equals(""))) {
                        ComboBox cb = (ComboBox) correctionsTable.getContainerProperty(next,
                                myUI.getMessage(Messages.Title)).getValue();
                        instCtrAmount = instCtrAmount + (Double) (((TextField) correctionsTable.getContainerProperty(next,
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue())
                                * (Double) (cb.getContainerProperty(cb.getValue(), Settings.correction_type_id).getValue());
                    }
                }
            }
            if (installmentTable.size() > 0) {
                for (Object obj : installmentTable.getItemIds()) {
                    if (((TextField) installmentTable.getItem(obj).getItemProperty(
                            myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue() != null) {
                        if ((Integer) installmentTable.getItem(obj).getItemProperty(Settings.status_id).getValue() != 0) {
                            instPlanContSum += (Double) ((TextField) installmentTable.getItem(obj).getItemProperty(
                                    myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue();
                        }
                    }
                }
            }
            instCtrAmount += debt;
            netContrAmount = instCtrAmount;
            if (ttl_payment != null && ttl_payment != 0.0) {
                instCtrAmount -= ttl_payment;
            }
            netIPlanTtlLab.setValue(myUI.getMessage(Messages.ToPlan) + ": " + Settings.dFormat2.format(Settings.round(instCtrAmount, 2)) + " " + currency);
            instPlanTtlLab.setValue(myUI.getMessage(Messages.InstallmentPlanTotal) + ": " + Settings.dFormat2.format(instPlanContSum) + " " + currency);
            if (instPlanCont != null) {
                instPlanDifLab.setValue(myUI.getMessage(Messages.Difference) + ": " + Settings.dFormat2.format(Settings.round(instCtrAmount, 2)
                        - Settings.round(instPlanContSum, 2)) + " " + currency);
            }
            if (contractCB.getValue() != null) {
                tabContractLab.setValue(myUI.getMessage(Messages.Contract) + ": "
                        + Settings.dFormat2.format(contractCB.getContainerProperty(contractCB.getValue(),
                        myUI.getMessage(Messages.Amount)).getValue()) + " " + currency);
                tabContractNetLab.setValue(myUI.getMessage(Messages.Net) + ": "
                        + Settings.dFormat2.format(netContrAmount) + " " + currency);
            }
        } else {
            clearInstPlanInfo();
        }
    }

    private GridLayout buildInstPlanInfoLayout() {

        GridLayout gl = new GridLayout(3, 2);
        gl.setWidth(Settings.PERCENTS100);

        tabContractLab = new Label();
        tabContractLab.setSizeUndefined();
        tabContractLab.setStyleName(ValoTheme.LABEL_SMALL);
        if (contractCB.getValue() != null) {
            tabContractLab.setValue(myUI.getMessage(Messages.Contract) + ": "
                    + Settings.dFormat2.format(contractCB.getContainerProperty(contractCB.getValue(),
                    myUI.getMessage(Messages.Amount)).getValue()) + " " + currency);
        }

        tabContractNetLab = new Label();
        tabContractNetLab.setSizeUndefined();
        tabContractNetLab.setStyleName(ValoTheme.LABEL_SMALL);
        if (contractCB.getValue() != null) {
            tabContractNetLab.setValue(myUI.getMessage(Messages.Net) + ": "
                    + Settings.dFormat2.format(netContrAmount + " " + currency));
        }

        netIPlanTtlLab = new Label();
        netIPlanTtlLab.setSizeUndefined();
        netIPlanTtlLab.setStyleName(ValoTheme.LABEL_SMALL);
        if (instCtrAmount != null) {
            netIPlanTtlLab.setValue(myUI.getMessage(Messages.ToPlan) + ": " + Settings.dFormat2.format(instCtrAmount) + " " + currency);
        }

        instPlanTtlLab = new Label();
        instPlanTtlLab.setSizeUndefined();
        instPlanTtlLab.setStyleName(ValoTheme.LABEL_SMALL);
        if (instPlanCont != null) {
            instPlanTtlLab.setValue(myUI.getMessage(Messages.InstallmentPlanTotal) + ": " + Settings.dFormat2.format(instPlanContSum) + " " + currency);
        }

        instPlanDifLab = new Label();
        instPlanDifLab.setSizeUndefined();
        instPlanDifLab.setStyleName(ValoTheme.LABEL_BOLD);
        if (instPlanCont != null) {
            instPlanDifLab.setValue(myUI.getMessage(Messages.Difference) + ": " + (instCtrAmount - instPlanContSum) + " " + currency);
        }

        gl.addComponent(tabContractLab, 0, 0);
        gl.addComponent(tabContractNetLab, 0, 1);
        gl.addComponent(netIPlanTtlLab, 1, 0);
        gl.addComponent(instPlanTtlLab, 1, 1);
        gl.addComponent(instPlanDifLab, 2, 0, 2, 1);
        gl.setComponentAlignment(instPlanDifLab, Alignment.MIDDLE_LEFT);
        return gl;
    }

    private void familyTableCheck(boolean isMain, String itemId) {
        if (isMain) {
            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Address)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Address)).getValue()).setRequiredError(myUI.getMessage(Messages.NotificationWrongValue));
            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Address)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(Messages.NotificationWrongValue), 1, 300, false));

            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Phone)).getValue()).setRequired(true);
            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Phone)).getValue()).setRequiredError(myUI.getMessage(Messages.NotificationWrongValue));
            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Phone)).getValue()).addValidator(new StringLengthValidator(
                    myUI.getMessage(Messages.NotificationWrongValue), 1, 100, false));
        } else {

            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Address)).getValue()).setRequired(false);
            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Address)).getValue()).removeAllValidators();

            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Phone)).getValue()).setRequired(false);
            ((TextField) relativesTable.getContainerProperty(itemId,
                    myUI.getMessage(Messages.Phone)).getValue()).removeAllValidators();
        }
    }

    private void checkDiscountsTable(Property property) {
        if (discountsTable.size() > 0) {
            if ((Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue() == 1
                    || (Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue() == 2) {
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).setEnabled(false);
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).removeAllValidators();
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().setValue(
                        ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                                myUI.getMessage(Messages.Title)).getValue())
                                .getContainerProperty(property.getValue(),
                                        myUI.getMessage(Messages.Amount)).getValue());
            } else if ((Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue() == 3
                    || (Integer) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                    myUI.getMessage(Messages.Title)).getValue())
                    .getContainerProperty(property.getValue(),
                            myUI.getMessage(Messages.DiscountType)).getValue() == 4) {
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).removeAllValidators();
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).addValidator(new DoubleRangeValidator(
                        myUI.getMessage(Messages.NotificationWrongValue), 0.01,
                        (Double) ((ComboBox) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                                myUI.getMessage(Messages.Title)).getValue())
                                .getContainerProperty(property.getValue(),
                                        myUI.getMessage(Messages.Amount)).getValue()));
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).setEnabled(true);
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().setValue(null);
                ((TextField) discountsTable.getContainerProperty(((ComboBox) property).getData(),
                        myUI.getMessage(Messages.Amount)).getValue()).setRequired(true);
            }
        }
    }

    private void insertDiscounts() {
        try {
            DbStudentDiscount dbsd = new DbStudentDiscount();
            dbsd.connect();
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            if (delDiscIds.size() > 0) {
                for (String delDiscId : delDiscIds) {
                    dbsd.exec_update_emp_id(myUI.getUser().getId(), delDiscId);
                    dbCon.exec_delete(delDiscId, Settings.dbStudentDiscount);
                }
            }
            contr_with_disc = (Double) contractCB.getContainerProperty(contractCB.getValue(), myUI.getMessage(Messages.Amount)).getValue();

            if (discountsTable.getContainerDataSource().size() > 0) {
                for (Object next : discountsTable.getItemIds()) {
                    StudentDiscount studentDiscount = getStudentDiscount((Integer) studDataTable.getValue(),
                            myUI.getUser().getCurrent_year().getId(), ((String) next));
                    if (discountsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Update))) {
                        dbsd.exec_update(studentDiscount);
                    } else if (discountsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Insert))) {
                        dbsd.exec_insert_st_discount(studentDiscount);
                    }
                }
            }
            delDiscIds.clear();
            dbsd.close();
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertCorrections() {
        try {
            DbStudentCorrection dbsd = new DbStudentCorrection();
            dbsd.connect();
            DbDefinition dbCon = new DbDefinition();
            dbCon.connect();
            if (delCorrectionIds.size() > 0) {
                for (String delCorrectionId : delCorrectionIds) {
                    dbCon.exec_delete(delCorrectionId, Settings.dbStudentCorrection);
                }
            }
            if (correctionsTable.getContainerDataSource().size() > 0) {
                for (Object next : correctionsTable.getItemIds()) {
                    if (correctionsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Update))) {
                        dbsd.exec_update(getStudentCorrection((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    } else if (correctionsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Insert))) {
                        dbsd.exec_insert(getStudentCorrection((Integer) studDataTable.getValue(),
                                myUI.getUser().getCurrent_year().getId(), ((String) next)));
                    }
                }
            }
            delCorrectionIds.clear();
            dbCon.close();
            dbsd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private Item insertRelatives(int student_id) {
        Item mainRelativeItem = null;
        try {
            if (delRelIds.size() > 0) {
                DbDefinition dbCon = new DbDefinition();
                dbCon.connect();
                for (String delRelId : delRelIds) {
                    dbCon.exec_delete(delRelId, Settings.dbStudentRelatives);
                }
                dbCon.close();
            }
            DbStudentRelative dbsr = new DbStudentRelative();
            dbsr.connect();
            if (relativesTable.getContainerDataSource().size() > 0) {
                for (Object next : relativesTable.getItemIds()) {
                    StudentRelative relative = null;
                    if (relativesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Update))) {
                        relative = getRelative(next.toString(), student_id, relativesTable.getItem(next));
                        dbsr.exec_update(relative);
                    } else if (relativesTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Insert))) {
                        relative = getRelative("0", student_id, relativesTable.getItem(next));
                        dbsr.exec_insert(relative);
                    }
                    if (relative != null && relative.getIs_main() == 1) {
                        mainRelativeItem = relativesTable.getItem(next);
                    }
                }
            }
            delRelIds.clear();
            dbsr.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        return mainRelativeItem;
    }

    private void insertPayments(int student_id) {
        try {
            DbStudentPayment dbsp = new DbStudentPayment();
            DbAccTransactions dbat = new DbAccTransactions();
            dbsp.connect();
            for (String delPayId : delPayIds) {
                dbat.exec_delete(Settings.dbColumnStudent_payments_id, delPayId, dbsp.getConnection());//delete transaction
                dbsp.exec_update_emp_id(myUI.getUser().getId(), delPayId);
                dbsp.exec_delete(delPayId);
            }
            if (paymentsTable.getContainerDataSource().size() > 0) {
                for (Object next : paymentsTable.getItemIds()) {
                    if (paymentsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Update))) {
                        StudentPayment sp = getPayment(Integer.parseInt(next.toString()), student_id, paymentsTable.getItem(next));
                        AccTransaction tr = new AccTransaction();
                        tr.setAmount(sp.getAmount());
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                Settings.acc_category_id).getValue());
                        tr.setAccTypeId((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                Settings.acc_type_id).getValue());
                        tr.setCashbox(new CashBox(sp.getCashBox_id(), sp.getCurrency_id()));
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForCashBox());
                        tr.setEmployee_id(sp.getEmployee_id());
                        tr.setSchool_id(sp.getSchool_id());
                        tr.setStudent_payments_id(sp.getId());
                        dbsp.exec_update(sp);
                        int update_status = dbat.exec_update(tr, Settings.dbColumnStudent_payments_id,
                                tr.getStudent_payments_id(), dbsp.getConnection());
                        if (update_status == 0) {
                            dbat.exec_insert(tr, dbsp.getConnection());
                        }
                    } else if (paymentsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Insert))) {
                        int order_num = dbsp.getMaxOrderNum((Integer) studDataTable.getValue());
                        StudentPayment sp = getPayment(0, student_id, paymentsTable.getItem(next));
                        int payment_id = dbsp.exec_insert(sp, order_num);
                        sp.setId(payment_id);//payment Id
                        AccTransaction tr = new AccTransaction();
                        tr.setAmount(sp.getAmount());
                        tr.setDate(sp.getModification_date());
                        tr.setCategory_id((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                Settings.acc_category_id).getValue());
                        tr.setAccTypeId((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                                myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                                Settings.acc_type_id).getValue());
                        tr.setCashbox(new CashBox(sp.getCashBox_id(), sp.getCurrency_id()));
                        tr.setCurrency_rate(sp.getRate());
                        tr.setNote(sp.getNoteForCashBox());
                        tr.setEmployee_id(sp.getEmployee_id());
                        tr.setSchool_id(sp.getSchool_id());
                        tr.setStudent_payments_id(sp.getId());
                        dbat.exec_insert(tr, dbsp.getConnection());
                    }
                }
            }
            delPayIds.clear();
            dbsp.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private AccTransaction insertTestPayments(Date date) {
        AccTransaction lowBalance = null;
        try {
            DbCashbox dbc = new DbCashbox();
            dbc.connect();
            DbAccTransactions dbat = new DbAccTransactions();
            dbat.connect();
            dbat.getConnection().setAutoCommit(false);
            try {
                dbat.exec_delete_by_st_id((Integer) studDataTable.getValue(),
                        myUI.getUser().getCurrent_year().getId(), dbat.getConnection());
                AccTransaction tr;
                for (Object next : paymentsTable.getItemIds()) {
                    StudentPayment sp = getPayment(0, 0, paymentsTable.getItem(next));
                    tr = new AccTransaction();
                    tr.setCashbox(dbc.getCashboxByCurrencyAndType(sp.getCurrency_id(), sp.getPayment_type_id()));
                    tr.setAmount(sp.getAmount());
                    tr.setDate(sp.getModification_date());
                    tr.setCategory_id((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                            myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                            Settings.acc_category_id).getValue());
                    tr.setAccTypeId((Integer) ((ComboBox) paymentsTable.getContainerProperty(next,
                            myUI.getMessage(Messages.PaymentCategoryType)).getValue()).getContainerProperty(sp.getPayment_cat_type_id(),
                            Settings.acc_type_id).getValue());
                    tr.setCurrency_rate(sp.getRate());
                    tr.setNote(sp.getNoteForCashBox());
                    tr.setEmployee_id(sp.getEmployee_id());
                    tr.setSchool_id(sp.getSchool_id());
                    dbat.exec_insert(tr, dbat.getConnection());
                }
                Iterator iter = dbc.execSQL(myUI).getItemIds().iterator();
                while (iter.hasNext()) {
                    Integer cashboxId = (Integer) iter.next();
                    lowBalance = dbat.exec_low_balance(dbat.getConnection(), myUI.getUser().getSchool().getId(),
                            cashboxId, date, 0.0, 0.0, 2);
                    if (lowBalance != null) {
                        break;
                    }
                }
                dbat.getConnection().rollback();
            } catch (Exception e) {
                dbat.getConnection().rollback();
                logger.error(e);
                logger.catching(e);
            }
            dbat.getConnection().setAutoCommit(true);
            dbat.close();
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        return lowBalance;
    }

    private void insertCalls(int student_id) {
        try {
            DbStudentCalls dbsc = new DbStudentCalls();
            dbsc.connect();
            if (delCallIds.size() > 0) {
                for (String delCallId : delCallIds) {
                    dbsc.exec_delete(delCallId);
                }
            }
            if (callsTable.getContainerDataSource().size() > 0) {
                for (Object next : callsTable.getItemIds()) {
                    if (callsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Update))) {
                        dbsc.exec_update(((TextField) callsTable.getItem(next).getItemProperty(
                                myUI.getMessage(Messages.Note)).getValue()).getValue(), Integer.parseInt(next.toString()));
                    } else if (callsTable.getContainerProperty(next, Settings.crud_status).getValue().toString()
                            .equals(myUI.getMessage(Messages.Insert))) {
                        dbsc.exec_insert(student_id, myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId(),
                                ((TextField) callsTable.getItem(next).getItemProperty(
                                        myUI.getMessage(Messages.Note)).getValue()).getValue());
                    }
                }

            }
            delCallIds.clear();
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void repaint() {
        eduStatTtlLab.setValue(eduStatCont.getContainerProperty(
                2, myUI.getMessage(Messages.Title)).getValue() +
                ": " + eduStatCont.getContainerProperty(2, Settings.count).getValue().toString()
                + "&emsp;" + eduStatCont.getContainerProperty(
                1, myUI.getMessage(Messages.Title)).getValue() +
                ": " + eduStatCont.getContainerProperty(1, Settings.count).getValue().toString()
                + "&emsp;" + eduStatCont.getContainerProperty(
                3, myUI.getMessage(Messages.Title)).getValue() +
                ": " + eduStatCont.getContainerProperty(3, Settings.count).getValue().toString()
                + "&emsp;" + eduStatCont.getContainerProperty(
                5, myUI.getMessage(Messages.Title)).getValue() +
                ": " + eduStatCont.getContainerProperty(5, Settings.count).getValue().toString()
                + "&emsp;" + eduStatCont.getContainerProperty(
                4, myUI.getMessage(Messages.Title)).getValue() +
                ": " + eduStatCont.getContainerProperty(4, Settings.count).getValue().toString()
                + "&emsp;" + eduStatCont.getContainerProperty(
                6, myUI.getMessage(Messages.Title)).getValue() +
                ": " + eduStatCont.getContainerProperty(6, Settings.count).getValue().toString());
    }

    private void clearContractInfo() {
        contractLab.setValue(myUI.getMessage(Messages.Contract) + ":");
        discountLab.setValue(myUI.getMessage(Messages.Discounts) + ":");
        correctionLab.setValue(myUI.getMessage(Messages.Corrections) + ":");
        debtLab.setValue(myUI.getMessage(Messages.PreviousYearDebt) + ":");
        netLab.setValue(myUI.getMessage(Messages.Net) + ":");
        paidLab.setValue(myUI.getMessage(Messages.Paid) + ":");
        leftLab.setValue(myUI.getMessage(Messages.Left) + ":");
        planDebt.setValue(myUI.getMessage(Messages.InstPlanDebt) + ":");
    }

    private void clearInstPlanInfo() {
        netIPlanTtlLab.setValue(myUI.getMessage(Messages.ToPlan) + ": 0.00 " + currency);
        instPlanTtlLab.setValue(myUI.getMessage(Messages.InstallmentPlanTotal) + ": 0.00 " + currency);
        instPlanDifLab.setValue(myUI.getMessage(Messages.Difference) + ": 0.00 " + currency);
        tabContractLab.setValue(myUI.getMessage(Messages.Contract) + ": 0.00 " + currency);
        tabContractNetLab.setValue(myUI.getMessage(Messages.Net) + ": 0.00 " + currency);

    }

    private void updateNetPaymentDb(double ttl_pay, int stud_id, int year_id) {
        try {
            DbStudentContract dbsc = new DbStudentContract();
            dbsc.connect();
            dbsc.execUpdateNetPayments(ttl_pay, stud_id, year_id);
            dbsc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setContractCb(int contract_id) {
        try {
            DbContract dbc = new DbContract();
            dbc.connect();
            contractCB.setContainerDataSource(
                    dbc.exec_contr_select(myUI, myUI.getUser().getCurrent_year().getId(),
                            myUI.getUser().getSchool().getId(), contract_id));
            dbc.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertNewStOrder(int id) {
        try {
            DbStudentOrder dbso = new DbStudentOrder();
            dbso.connect();
            dbso.insertNewStOrder(id, (Integer) classCB.getValue(),
                    myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId());
            dbso.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void insertNewStCtrOrder() {
        try {
            DbStudentOrder dbso = new DbStudentOrder();
            dbso.connect();
            dbso.insertNewStCtrOrder((Integer) studDataTable.getValue(), (Integer) classCB.getValue(),
                    ((Integer) statusCB.getValue()), myUI.getUser().getCurrent_year().getId(), myUI.getUser().getId());
            dbso.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private void setStudDataTable(String edu_st_ids) {
        edu_st_ids = edu_st_ids.substring(1, edu_st_ids.length() - 1);
        try {
            DbStudent dbs = new DbStudent();
            dbs.connect();
            studDataTable.setContainerDataSource(dbs.execSQL(myUI,
                    myUI.getUser().getSchool().getId(), myUI.getUser().getCurrent_year().getId(), this, edu_st_ids));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        studDataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
    }

    private String generateStudId(int yearId, String yearName) {
        String generated_id = null;
        try {
            DbStudent dbCon = new DbStudent();
            dbCon.connect();
            String school_code = (Integer) classCB.getContainerProperty(
                    classCB.getValue(), myUI.getMessage(Messages.ClassNumber)).getValue() < 7 ?
                    myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                            myUI.getMessage(Messages.PrimaryCode)).getValue().toString() :
                    myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                            myUI.getMessage(Messages.SecondaryCode)).getValue().toString();
            String school_level = null;
            if ((Integer) classCB.getContainerProperty(
                    classCB.getValue(), myUI.getMessage(Messages.ClassNumber)).getValue() < 7 &&
                    !myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                            myUI.getMessage(Messages.PrimaryCode)).getValue().equals(
                            myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                                    myUI.getMessage(Messages.SecondaryCode)).getValue())) {
                school_level = myUI.getMessage(Messages.PrimaryCode);
            } else if ((Integer) classCB.getContainerProperty(
                    classCB.getValue(), myUI.getMessage(Messages.ClassNumber)).getValue() >= 7 &&
                    !myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                            myUI.getMessage(Messages.PrimaryCode)).getValue().equals(
                            myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                                    myUI.getMessage(Messages.SecondaryCode)).getValue())) {
                myUI.getMessage(Messages.SecondaryCode);
            }
            int year_ord = Integer.parseInt(yearName.substring(2, 4));
            String class_num = Integer.toString(year_ord - (Integer) classCB.getContainerProperty(
                    classCB.getValue(), Settings.class_order_number).getValue());
            char cl = (Integer) classCB.getContainerProperty(classCB.getValue(), Settings.class_type_id).getValue() >= 4
                    ? '0' : class_num.charAt(class_num.length() - 1);
            int order_number = 1;
            do {
                generated_id = school_code + year_ord + cl + String.format("%03d", dbCon.execSQL_login(myUI,
                        yearId, myUI.getUser().getSchool().getId(), (Integer) classCB.getContainerProperty(
                                classCB.getValue(), Settings.class_type_id).getValue(),
                        order_number, (Integer) classCB.getContainerProperty(
                                classCB.getValue(), Settings.min).getValue(),
                        (Integer) classCB.getContainerProperty(
                                classCB.getValue(), Settings.max).getValue(), school_level));
                order_number++;
                if (order_number == 100) {
                    break;
                }
            } while (dbCon.isLoginExists(generated_id));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            logger.catching(ex);
        }
        return generated_id;
    }

    private void createBtnAction() {
        isNew = true;
        tabs.getTab(famTableLay).setEnabled(true);
        tabs.setSelectedTab(famTableLay);
        clearFields();
        prepareModificationMode();
        statusCB.setValue(1);
        loginTF.focus();
        delRelIds.clear();
        plusRelButton.click();
        clearContractInfo();
    }

    private void modifyBtnAction() {
        isNew = false;
        photoUpl.setEnabled(true);
        fillFields();
        prepareModificationMode();
        classCB.setEnabled(false);
        addRowIfTableEmpty();
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
                    myFile = new File(Settings.PATH_TO_UPLOADS + photoName);
                } else {
                    myFile = new File(Settings.PATH_TO_UPLOADS + System.currentTimeMillis() + "_" + filename);
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

    private void addRelativesToTable(List<StudentRelative> relatives) {

        if (relatives == null || relatives.isEmpty()) {
            return;
        }

        if (NATURAL_COL_ORDER_RELATIVES == null) {
            NATURAL_COL_ORDER_RELATIVES = new String[]{
                    Settings.button,
                    myUI.getMessage(Messages.RelativeType),
                    myUI.getMessage(Messages.FullName),
                    myUI.getMessage(Messages.Address),
                    myUI.getMessage(Messages.Phone),
                    myUI.getMessage(Messages.Responsible)
            };
        }

        if (relativesTable.getContainerDataSource().size() == 0) {
            relativesTable.setContainerDataSource(prepareRelativesContainer());
        }

        IndexedContainer container = (IndexedContainer) relativesTable.getContainerDataSource();

        for (StudentRelative relative : relatives) {
            String id = Settings.FreshItem + (--r_table_counter);
            Item item = container.addItemAt(container.size(), id);

            item.getItemProperty(Settings.button).setValue(
                    createButton(
                            myUI.getMessage(Messages.DeleteButton),
                            id,
                            Settings.dbStudentRelatives,
                            FontAwesome.MINUS_SQUARE
                    )
            );

            CheckBox cb = createCheckBox(
                    relative.getIs_main() == 1,
                    myUI.getMessage(Messages.Responsible),
                    id
            );

            item.getItemProperty(myUI.getMessage(Messages.Responsible)).setValue(cb);

            item.getItemProperty(myUI.getMessage(Messages.FullName)).setValue(
                    createTextField(
                            relative.getFullName(),
                            myUI.getMessage(Messages.FullName),
                            id,
                            new StringLengthValidator(
                                    myUI.getMessage(Messages.NotificationWrongValue),
                                    1,
                                    250,
                                    false
                            ),
                            true
                    )
            );

            item.getItemProperty(myUI.getMessage(Messages.Phone)).setValue(
                    createTextField(
                            relative.getPhone(),
                            myUI.getMessage(Messages.Phone),
                            id,
                            new StringLengthValidator(
                                    myUI.getMessage(Messages.NotificationWrongValue),
                                    null,
                                    100,
                                    true
                            ),
                            false
                    )
            );

            item.getItemProperty(myUI.getMessage(Messages.Address)).setValue(
                    createTextField(
                            relative.getAddress(),
                            myUI.getMessage(Messages.Address),
                            id,
                            new StringLengthValidator(
                                    myUI.getMessage(Messages.NotificationWrongValue),
                                    null,
                                    300,
                                    true
                            ),
                            false
                    )
            );

            item.getItemProperty(myUI.getMessage(Messages.RelativeType)).setValue(
                    createCombobox(
                            relative.getRelative_id(),
                            myUI.getMessage(Messages.RelativeType),
                            id,
                            Settings.dbRelatives,
                            false
                    )
            );

            item.getItemProperty(Settings.crud_status).setValue(
                    myUI.getMessage(Messages.Insert)
            );
        }

        relativesTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER_RELATIVES);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.FullName), 1);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.Address), 1);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.RelativeType), 0.5f);
        relativesTable.setColumnExpandRatio(myUI.getMessage(Messages.Responsible), 0.2f);
    }
}
