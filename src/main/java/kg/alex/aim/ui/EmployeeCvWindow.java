package kg.alex.aim.ui;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.dao.*;
import kg.alex.aim.domain.Employee;
import kg.alex.aim.domain.EmployeeExtraInfo;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.pdf.CVPdf;
import kg.alex.aim.utils.FormattedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EmployeeCvWindow extends Window {
    static final Logger logger = LogManager.getLogger(EmployeeCvWindow.class);
    private final MyVaadinUI myUI;
    public Subject currentUser = SecurityUtils.getSubject();
    private FormattedTable educationTable, workPlacesTable, examsTable, seminarsTable, certificatesTable,
            spouseEducationTable, spouseWorkPlacesTable, childrenTable;

    public EmployeeCvWindow(MyVaadinUI myUI, Employee employee, EmployeeExtraInfo employeeExtraInfo, String year) {
        this.myUI = myUI;
        this.setWidth("90%");
        this.setHeight("90%");
        this.setModal(true);

        VerticalLayout mainLay = new VerticalLayout();
        mainLay.setWidth(Settings.PERCENTS100);
        mainLay.setSpacing(true);
        mainLay.setMargin(true);
        this.setContent(mainLay);

        GridLayout gl = new GridLayout(4, 16);
        gl.setMargin(new MarginInfo(true, true));
        gl.setWidth(Settings.PERCENTS100);
        gl.setSpacing(true);
        gl.setColumnExpandRatio(0, 1.5f);
        gl.setColumnExpandRatio(1, 1.1f);
        gl.setColumnExpandRatio(2, 1.1f);
        gl.setColumnExpandRatio(3, 1.5f);
        mainLay.addComponent(gl);

        int rowNum = 0;
        GridLayout rightGl = new GridLayout(2, 19);
        rightGl.setColumnExpandRatio(0, 2);
        rightGl.setColumnExpandRatio(1, 1);
        rightGl.setWidth(Settings.PERCENTS100);
        gl.addComponent(rightGl, 0, 0, 0, 4);

        rightGl.addComponent(createLabel(employee.getSurname() + " " + employee.getName(),
                new String[]{ValoTheme.LABEL_HUGE, ValoTheme.LABEL_BOLD}), 0, rowNum, 1, rowNum);

        java.util.Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        java.util.Calendar bd = Calendar.getInstance();
        bd.setTime(employee.getBirth_date());
        Label l = createLabel((now.get(Calendar.YEAR) - bd.get(Calendar.YEAR))
                        + " " + Settings.generateYearPostfix(now.get(Calendar.YEAR) - bd.get(Calendar.YEAR)),
                new String[]{ValoTheme.LABEL_SUCCESS});
        rowNum++;
        rightGl.addComponent(l, 1, rowNum);
        rightGl.setComponentAlignment(l, Alignment.MIDDLE_RIGHT);

        rowNum++;
        rightGl.addComponent(createLabel("<br>", null), 0, rowNum, 1, rowNum);

        Embedded photoEmb = new Embedded();
        if (employee.getPhoto() != null) {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS_HR
                    + employee.getPhoto())));
        } else {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS_HR + "no_photo.jpg")));
        }
        photoEmb.setImmediate(true);
        photoEmb.setHeight("200px");
        photoEmb.setId(myUI.getMessage(Messages.Photo));
        photoEmb.setData(Settings.PATH_TO_UPLOADS_HR + employee.getPhoto());
        rowNum++;
        rightGl.addComponent(photoEmb, 0, rowNum, 1, rowNum);
        rightGl.setComponentAlignment(photoEmb, Alignment.MIDDLE_CENTER);

        rowNum++;
        rightGl.addComponent(createLabel("<br>", null), 0, rowNum, 1, rowNum);

        rowNum++;
        rightGl.addComponent(createLabel("<b>" + myUI.getMessage(Messages.ModificationDate) + ": </b>"
                + (employeeExtraInfo.getModificationDate() == null ? "" :
                Settings.df.format(employeeExtraInfo.getModificationDate())), null), 0, rowNum, 1, rowNum);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setWidth(Settings.PERCENTS100);
        rowNum++;
        rightGl.addComponent(progressBar, 0, rowNum, 1, rowNum);
        try {
            DbEmployee dbCon = new DbEmployee();
            dbCon.connect();
            progressBar.setValue(dbCon.execSQL_completeness(employee.getId()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        progressBar.setDescription(progressBar.getValue() * 100 + "%");

        rowNum++;
        rightGl.addComponent(createLabel("<hr>", null), 0, rowNum, 1, rowNum);

        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                        myUI.getMessage(Messages.WorkingStatus) + ": </b>" + employeeExtraInfo.getWorkingStatus(), null),
                0, rowNum, 1, rowNum);
        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                        myUI.getMessage(Messages.MainPosition) + ": </b>" + employeeExtraInfo.getMainPosition(), null),
                0, rowNum, 1, rowNum);
        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                        myUI.getMessage(Messages.ExtraPositions) + ": </b>" + employeeExtraInfo.getExtraPositions(), null),
                0, rowNum, 1, rowNum);
        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                        myUI.getMessage(Messages.TotalWorkExperience) + ": </b>" + employeeExtraInfo.getWorkExperience(), null),
                0, rowNum, 1, rowNum);
        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                                         myUI.getMessage(Messages.AimWorkExperience) + ": </b>" + employeeExtraInfo.getWorkExperienceSapat(), null),
                0, rowNum, 1, rowNum);
        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                        myUI.getMessage(Messages.MainBranch) + ": </b>" + employeeExtraInfo.getMainBranch(), null),
                0, rowNum, 1, rowNum);
        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                        myUI.getMessage(Messages.ExtraBranches) + ": </b>" + employeeExtraInfo.getExtraBranches(), null),
                0, rowNum, 1, rowNum);
        rowNum++;
        rightGl.addComponent(createLabel("<b>" + myUI.getMessage(Messages.TotalHours) + year + ": </b>"
                        + employeeExtraInfo.getHours() + ", <b>" + myUI.getMessage(Messages.ExtraHours) + ": </b>"
                        + employeeExtraInfo.getExtraHours(), null),
                0, rowNum, 1, rowNum);

        if (currentUser.isPermitted(Settings.cnCV_Window + ":" + Settings.contract_info)) {
            rowNum++;
            rightGl.addComponent(createLabel("<b>" +
                            myUI.getMessage(Messages.CanBeAdvisor) + ": </b>" + employeeExtraInfo.getCanBeAdvisor(), null),
                    0, rowNum, 1, rowNum);
            rowNum++;
            rightGl.addComponent(createLabel("<b>" +
                            myUI.getMessage(Messages.ContractType) + ": </b>" + employeeExtraInfo.getSalaryCategory(), null),
                    0, rowNum, 1, rowNum);
        }
        rowNum++;
        rightGl.addComponent(createLabel("<b>" +
                        myUI.getMessage(Messages.Languages) + ": </b>" + employeeExtraInfo.getLanguages(), null),
                0, rowNum, 1, rowNum);
        l = createLabel(myUI.getMessage(Messages.MainInfo),
                new String[]{ValoTheme.LABEL_BOLD, ValoTheme.LABEL_LARGE});
        l.setWidthUndefined();
        gl.addComponent(l, 1, 0, 2, 0);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        Table infoTable = new Table();
        infoTable.setWidth(Settings.PERCENTS100);
        infoTable.setSortEnabled(false);
        infoTable.setStyleName(ValoTheme.TABLE_NO_STRIPES);
        infoTable.addStyleName(ValoTheme.TABLE_NO_HEADER);
        infoTable.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        infoTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        infoTable.addStyleName(ValoTheme.TABLE_COMPACT);
        infoTable.setColumnAlignment(myUI.getMessage(Messages.ClassCaption), Table.Align.RIGHT);
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.ClassCaption), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Content), String.class, null);
        addInfoItem(container, myUI.getMessage(Messages.Id), employee.getLogin());
        addInfoItem(container, myUI.getMessage(Messages.LastName), employee.getSurname());
        addInfoItem(container, myUI.getMessage(Messages.FirstName), employee.getName());
        addInfoItem(container, myUI.getMessage(Messages.MiddleName), employee.getMiddle_name());
        addInfoItem(container, myUI.getMessage(Messages.Gender), employeeExtraInfo.getGender());
        addInfoItem(container, myUI.getMessage(Messages.DateAndPlaceOfBirth),
                Settings.df.format(employee.getBirth_date()) + (employeeExtraInfo.getBirth_place() == null ?
                        "" : ", " + employeeExtraInfo.getBirth_place()));
        addInfoItem(container, myUI.getMessage(Messages.Nationality), employeeExtraInfo.getNationality());
        addInfoItem(container, myUI.getMessage(Messages.Citizenship), employeeExtraInfo.getCitizenship());
        addInfoItem(container, myUI.getMessage(Messages.MartialStatus), employeeExtraInfo.getMartialStatus());
        addInfoItem(container, myUI.getMessage(Messages.HealthStatus), employeeExtraInfo.getHealth_notes());
        infoTable.setContainerDataSource(container);
        infoTable.setPageLength(container.size());
        gl.addComponent(infoTable, 1, 1, 2, 1);
        gl.setRowExpandRatio(1, 1);

        VerticalLayout contactsVl = new VerticalLayout();
        contactsVl.setWidth(Settings.PERCENTS100);
        contactsVl.setSpacing(true);

        contactsVl.addComponent(createLabel("<i class=\"fa fa-university\"></i><b> "
                + myUI.getMessage(Messages.School) + ": </b>" + employeeExtraInfo.getSchool(), null));
        contactsVl.addComponent(createLabel("<i class=\"fa fa-envelope\"></i><b> Email: </b>"
                + employeeExtraInfo.getEmail(), null));
        contactsVl.addComponent(createLabel("<i class=\"fa fa-phone-square\"></i><b> "
                + myUI.getMessage(Messages.PhoneNumbers) + ": </b>"
                + employeeExtraInfo.getPhones(), null));
        contactsVl.addComponent(createLabel("<i class=\"fa fa-map-marker\"></i><b> "
                + myUI.getMessage(Messages.Address) + ": </b>"
                + employeeExtraInfo.getAddress(), null));
        contactsVl.addComponent(createLabel("<i class=\"fa fa-users\"></i><b> "
                + myUI.getMessage(Messages.SpouseInfo) + ": </b>"
                + employeeExtraInfo.getFamilyInfo(), null));
        contactsVl.addComponent(createLabel("<i class=\"fa fa-child\"></i><b> "
                + myUI.getMessage(Messages.Children) + ": </b>"
                + employeeExtraInfo.getChildren(), null));
        Button pdfBtn = new Button(myUI.getMessage(Messages.ExportToPdf));
        pdfBtn.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        pdfBtn.addClickListener((Button.ClickListener) event -> {
            List<Table> tableList = new ArrayList<>();
            educationTable.setData(myUI.getMessage(Messages.Education));
            tableList.add(educationTable);
            workPlacesTable.setData(myUI.getMessage(Messages.WorkPlaces));
            tableList.add(workPlacesTable);
            examsTable.setData(myUI.getMessage(Messages.Exams));
            tableList.add(examsTable);
            seminarsTable.setData(myUI.getMessage(Messages.Seminars));
            tableList.add(seminarsTable);
            certificatesTable.setData(myUI.getMessage(Messages.Certificates));
            tableList.add(certificatesTable);
            spouseEducationTable.setData(myUI.getMessage(Messages.SpouseEducation));
            tableList.add(spouseEducationTable);
            spouseWorkPlacesTable.setData(myUI.getMessage(Messages.SpouseWorkPlaces));
            tableList.add(spouseWorkPlacesTable);
            childrenTable.setData(myUI.getMessage(Messages.Children));
            tableList.add(childrenTable);
            new CVPdf(myUI, employee, employeeExtraInfo, year, tableList);
        });
        contactsVl.addComponent(pdfBtn);
        contactsVl.setComponentAlignment(pdfBtn, Alignment.BOTTOM_RIGHT);
        gl.addComponent(contactsVl, 3, 1);

        l = createLabel(myUI.getMessage(Messages.Education),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 1, 2, 3, 2);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        educationTable = new FormattedTable(myUI);
        educationTable.setWidth(Settings.PERCENTS100);
        educationTable.setColumnReorderingAllowed(false);
        educationTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        educationTable.setStyleName(ValoTheme.TABLE_COMPACT);
        educationTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        educationTable.addStyleName("noWrap");
        try {
            DbEmployeeEducation dbCon = new DbEmployeeEducation();
            dbCon.connect();
            educationTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId(), 1));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        educationTable.setPageLength(educationTable.size());
        gl.addComponent(educationTable, 1, 3, 3, 3);
        gl.setRowExpandRatio(3, 1);

        l = createLabel(myUI.getMessage(Messages.WorkPlaces),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 1, 4, 3, 4);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        workPlacesTable = new FormattedTable(myUI);
        workPlacesTable.setWidth(Settings.PERCENTS100);
        workPlacesTable.setColumnReorderingAllowed(false);
        workPlacesTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        workPlacesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        workPlacesTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        workPlacesTable.addStyleName("noWrap");
        workPlacesTable.setColumnWidth(myUI.getMessage(Messages.ExtraPositions), 250);
        try {
            DbEmployeeWork dbCon = new DbEmployeeWork();
            dbCon.connect();
            workPlacesTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId(), 1));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        workPlacesTable.setPageLength(workPlacesTable.size());
        gl.addComponent(workPlacesTable, 0, 5, 3, 5);

        l = createLabel(myUI.getMessage(Messages.Exams),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 0, 6, 1, 6);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        examsTable = new FormattedTable(myUI);
        examsTable.setWidth(Settings.PERCENTS100);
        examsTable.setColumnReorderingAllowed(false);
        examsTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        examsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        examsTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        examsTable.addStyleName("noWrap");
        try {
            DbEmployeeExam dbCon = new DbEmployeeExam();
            dbCon.connect();
            examsTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        examsTable.setPageLength(examsTable.size());
        gl.addComponent(examsTable, 0, 7, 1, 7);

        l = createLabel(myUI.getMessage(Messages.Seminars),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 2, 6, 3, 6);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        seminarsTable = new FormattedTable(myUI);
        seminarsTable.setWidth(Settings.PERCENTS100);
        seminarsTable.setColumnReorderingAllowed(false);
        seminarsTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        seminarsTable.setStyleName(ValoTheme.TABLE_COMPACT);
        seminarsTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        seminarsTable.addStyleName("noWrap");
        seminarsTable.setColumnWidth(myUI.getMessage(Messages.Title), 200);
        try {
            DbEmployeeSeminar dbCon = new DbEmployeeSeminar();
            dbCon.connect();
            seminarsTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        seminarsTable.setPageLength(seminarsTable.size());
        gl.addComponent(seminarsTable, 2, 7, 3, 7);

        l = createLabel(myUI.getMessage(Messages.Certificates),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 0, 8, 1, 8);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        certificatesTable = new FormattedTable(myUI);
        certificatesTable.setWidth(Settings.PERCENTS100);
        certificatesTable.setColumnReorderingAllowed(false);
        certificatesTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        certificatesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        certificatesTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        certificatesTable.addStyleName("noWrap");
        try {
            DbEmployeeCertificate dbCon = new DbEmployeeCertificate();
            dbCon.connect();
            certificatesTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        certificatesTable.setPageLength(certificatesTable.size());
        gl.addComponent(certificatesTable, 0, 9, 1, 9);

        l = createLabel(myUI.getMessage(Messages.SpouseEducation),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 0, 10, 3, 10);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        spouseEducationTable = new FormattedTable(myUI);
        spouseEducationTable.setWidth(Settings.PERCENTS100);
        spouseEducationTable.setColumnReorderingAllowed(false);
        spouseEducationTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        spouseEducationTable.setStyleName(ValoTheme.TABLE_COMPACT);
        spouseEducationTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        spouseEducationTable.addStyleName("noWrap");
        try {
            DbEmployeeEducation dbCon = new DbEmployeeEducation();
            dbCon.connect();
            spouseEducationTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId(), 2));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        spouseEducationTable.setPageLength(spouseEducationTable.size());
        gl.addComponent(spouseEducationTable, 0, 11, 3, 11);

        l = createLabel(myUI.getMessage(Messages.SpouseWorkPlaces),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 0, 12, 3, 12);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        spouseWorkPlacesTable = new FormattedTable(myUI);
        spouseWorkPlacesTable.setWidth(Settings.PERCENTS100);
        spouseWorkPlacesTable.setColumnReorderingAllowed(false);
        spouseWorkPlacesTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        spouseWorkPlacesTable.setStyleName(ValoTheme.TABLE_COMPACT);
        spouseWorkPlacesTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        spouseWorkPlacesTable.addStyleName("noWrap");
        try {
            DbEmployeeWork dbCon = new DbEmployeeWork();
            dbCon.connect();
            spouseWorkPlacesTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId(), 2));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        spouseWorkPlacesTable.setPageLength(spouseWorkPlacesTable.size());
        gl.addComponent(spouseWorkPlacesTable, 0, 13, 3, 13);

        l = createLabel(myUI.getMessage(Messages.Children),
                new String[]{ValoTheme.LABEL_LARGE, ValoTheme.LABEL_BOLD});
        l.setWidthUndefined();
        gl.addComponent(l, 0, 14, 3, 14);
        gl.setComponentAlignment(l, Alignment.MIDDLE_CENTER);

        childrenTable = new FormattedTable(myUI);
        childrenTable.setWidth(Settings.PERCENTS100);
        childrenTable.setColumnReorderingAllowed(false);
        childrenTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        childrenTable.setStyleName(ValoTheme.TABLE_COMPACT);
        childrenTable.addStyleName(ValoTheme.TABLE_NO_STRIPES);
        childrenTable.addStyleName("noWrap");
        try {
            DbEmployeeChildren dbCon = new DbEmployeeChildren();
            dbCon.connect();
            childrenTable.setContainerDataSource(dbCon.execSQL(myUI, employee.getId()));
            dbCon.close();
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        childrenTable.setPageLength(childrenTable.size());
        gl.addComponent(childrenTable, 0, 15, 3, 15);

    }

    private void addInfoItem(IndexedContainer container, String caption, String content) {
        Item item = container.addItem(container.size() + 1);
        item.getItemProperty(myUI.getMessage(Messages.ClassCaption)).setValue(caption);
        item.getItemProperty(myUI.getMessage(Messages.Content)).setValue(content);
    }

    private Label createLabel(String value, String[] styles) {
        Label l = new Label();
        l.setContentMode(ContentMode.HTML);
        l.setValue(value);
        if (styles != null) {
            for (int i = 0; i < styles.length; i++) {
                if (i == 0) {
                    l.setStyleName(styles[i]);
                } else {
                    l.addStyleName(styles[i]);
                }
            }
        }
        return l;
    }
}
