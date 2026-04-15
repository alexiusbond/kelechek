package kg.alex.star.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.dao.DbBranch;
import kg.alex.star.dao.DbDefinition;
import kg.alex.star.dao.DbEmployee;
import kg.alex.star.dao.DbEmployeeLessons;
import kg.alex.star.domain.EmployeeBranchesExcel;
import kg.alex.star.domain.EmployeeLessons;
import kg.alex.star.excel.ExcelUploader;
import kg.alex.star.i18n.Messages;
import kg.alex.star.pdf.BranchCodesPdf;
import kg.alex.star.utils.FormattedFilterTable;
import kg.alex.star.utils.FormattedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.dialogs.ConfirmDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportBranchesFromExcelView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(ImportBranchesFromExcelView.class);
    private final MyVaadinUI myUI;
    private final FormattedFilterTable dataTable;
    private final FormattedTable importTable;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button deleteBtn, saveBtn, templateBtn, branchesPDF_Btn;
    private ComboBox yearSelect;
    private VerticalLayout settingsLay;

    public ImportBranchesFromExcelView(MyVaadinUI myUI) {
        this.myUI = myUI;

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);
        vl.setMargin(true);

        Label captionFromFile = new Label();
        captionFromFile.setWidth(Settings.PERCENTS100);
        captionFromFile.setContentMode(ContentMode.HTML);
        captionFromFile.setValue(myUI.getMessage(Messages.FileData));
        captionFromFile.setStyleName("tableCpt");
        vl.addComponent(captionFromFile);

        importTable = new FormattedTable(myUI);
        importTable.setStyleName(ValoTheme.TABLE_COMPACT);
        importTable.setSizeFull();
        vl.addComponent(importTable);
        vl.setExpandRatio(importTable, 1);

        Label captionFromDb = new Label();
        captionFromDb.setWidth(Settings.PERCENTS100);
        captionFromDb.setContentMode(ContentMode.HTML);
        captionFromDb.setValue(myUI.getMessage(Messages.DbData));
        captionFromDb.setStyleName("tableCpt");
        vl.addComponent(captionFromDb);

        dataTable = new FormattedFilterTable(myUI);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        vl.addComponent(dataTable);
        vl.setExpandRatio(dataTable, 1);

        buildSettingsLayout();

        this.setSplitPosition(24, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);
    }

    private void setTableData() {
        try {
            DbEmployeeLessons dbCon = new DbEmployeeLessons();
            dbCon.connect();
            dataTable.setContainerDataSource(dbCon.execSQL(myUI, (Integer) yearSelect.getValue(), myUI.getUser().getSchool().getId()));
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns((Object[]) new String[]{
                myUI.getMessage(Messages.Lecturer), myUI.getMessage(Messages.Lesson),
                myUI.getMessage(Messages.ClassName),
                myUI.getMessage(Messages.Hours),
                myUI.getMessage(Messages.ExtraHours)});
        dataTable.setColumnAlignment(myUI.getMessage(Messages.ClassName), CustomTable.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(Messages.Hours), CustomTable.Align.RIGHT);
        dataTable.setColumnAlignment(myUI.getMessage(Messages.ExtraHours), CustomTable.Align.RIGHT);
    }

    private void buildSettingsLayout() {

        settingsLay = new VerticalLayout();
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setWidth(Settings.PERCENTS100);

        HorizontalLayout buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

        saveBtn = new Button();
        saveBtn.setDescription(myUI.getMessage(Messages.SaveButton));
        saveBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        saveBtn.setIcon(FontAwesome.FLOPPY_O);
        saveBtn.addClickListener(this);
        saveBtn.setEnabled(currentUser.isPermitted(Settings.cnImportBranchesFromExcelView + ":" + Settings.actImport));
        buttonsLay.addComponent(saveBtn);

        templateBtn = new Button();
        templateBtn.setDescription(myUI.getMessage(Messages.DownloadTemplateBtn));
        templateBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        templateBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        templateBtn.addClickListener(this);
        buttonsLay.addComponent(templateBtn);

        branchesPDF_Btn = new Button();
        branchesPDF_Btn.setDescription(myUI.getMessage(Messages.DownloadBranchesCode));
        branchesPDF_Btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        branchesPDF_Btn.setIcon(FontAwesome.FILE_PDF_O);
        branchesPDF_Btn.addClickListener(this);
        buttonsLay.addComponent(branchesPDF_Btn);

        deleteBtn = new Button();
        deleteBtn.setEnabled(false);
        deleteBtn.setDescription(myUI.getMessage(Messages.DeleteAllButton));
        deleteBtn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        deleteBtn.setIcon(FontAwesome.TRASH_O);
        deleteBtn.addClickListener(this);
        deleteBtn.setEnabled(currentUser.isPermitted(Settings.cnImportBranchesFromExcelView + ":" + Settings.actDelete));
        buttonsLay.addComponent(deleteBtn);

        ExcelUploader<?> excelUploader = new ExcelUploader<>(EmployeeBranchesExcel.class);
        excelUploader.addSucceededListener((event, items) -> {
            importTable.setContainerDataSource(buildContainer(items));
            importTable.setColumnAlignment(myUI.getMessage(Messages.Hours), Table.Align.RIGHT);
            importTable.setColumnAlignment(myUI.getMessage(Messages.ExtraHours), Table.Align.RIGHT);
            importTable.setColumnAlignment(myUI.getMessage(Messages.ClassName), Table.Align.RIGHT);
        });

        Upload fileUpl = new Upload(null, excelUploader);
        fileUpl.setImmediate(true);
        fileUpl.setStyleName("large");
        fileUpl.setButtonCaption(myUI.getMessage(Messages.UploadExcel));
        fileUpl.setDescription(myUI.getMessage(Messages.UploadExcel));
        fileUpl.addSucceededListener(excelUploader);
        buttonsLay.addComponent(fileUpl);

        yearSelect = new ComboBox(myUI.getMessage(Messages.LessonsYear));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);
        yearSelect.addValueChangeListener(this);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            yearSelect.setContainerDataSource(dbDef.exec_for_select(myUI, Settings.dbYear, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        yearSelect.setValue(myUI.getUser().getCurrent_year().getId());
        settingsLay.addComponent(buttonsLay);
        settingsLay.addComponent(yearSelect);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == deleteBtn && yearSelect.getValue() != null) {
            ConfirmDialog.show(myUI, myUI.getMessage(Messages.Question),
                    myUI.getMessage(Messages.ConfirmDeletionAll) + " " + yearSelect.getItemCaption(yearSelect.getValue()) + " "
                            + myUI.getMessage(Messages.Year) + "?",
                    myUI.getMessage(Messages.Yes),
                    myUI.getMessage(Messages.No),
                    (ConfirmDialog.Listener) dialog -> {
                        if (dialog.isConfirmed()) {
                            execDelete();
                            setTableData();
                        }
                    });
        } else if (source == saveBtn) {
            List itemsToDel = new ArrayList();
            int counter = 0;
            try {
                DbEmployee dbEmp = new DbEmployee();
                dbEmp.connect();
                DbEmployeeLessons dbEl = new DbEmployeeLessons();
                dbEl.connect();
                DbDefinition dbDef = new DbDefinition();
                dbDef.connect();
                for (Object item_id : importTable.getItemIds()) {
                    EmployeeLessons el = new EmployeeLessons();
                    el.setSchool_id(myUI.getUser().getSchool().getId());
                    el.setYear_id((Integer) yearSelect.getValue());
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(Messages.Hours)).getValue() != null) {
                        el.setHours((Integer) importTable.getContainerProperty(item_id,
                                myUI.getMessage(Messages.Hours)).getValue());
                    } else {
                        continue;
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(Messages.ExtraHours)).getValue() != null) {
                        el.setExtra_hours((Integer) importTable.getContainerProperty(item_id,
                                myUI.getMessage(Messages.ExtraHours)).getValue());
                    } else {
                        el.setExtra_hours(0);
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(Messages.LecturerID)).getValue() != null) {
                        int id = 0;
                        try {
                            id = dbEmp.execSQL_id(myUI.getUser().getSchool().getId(), importTable.getContainerProperty(item_id,
                                    myUI.getMessage(Messages.LecturerID)).getValue().toString());
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (id != 0) {
                            el.setEmployee_id(id);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(Messages.CourseCode)).getValue() != null) {
                        int id = 0;
                        try {
                            id = dbDef.search_id(Settings.dbBranchTable, Settings.dbColumnCode, importTable.getContainerProperty(item_id,
                                    myUI.getMessage(Messages.CourseCode)).getValue().toString());
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (id != 0) {
                            el.setBranch_id(id);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    if (importTable.getContainerProperty(item_id, myUI.getMessage(Messages.ClassName)).getValue() != null) {
                        int id = 0;
                        try {
                            id = dbDef.search_id(Settings.classTable, Settings.dbColumnName, importTable.getContainerProperty(item_id,
                                    myUI.getMessage(Messages.ClassName)).getValue().toString());
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                        if (id != 0) {
                            el.setClass_number_id(id);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                    try {
                        if (dbEl.exec_insert(el) != 0) {
                            counter++;
                            itemsToDel.add(item_id);
                        }
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                }
                dbEl.close();
                dbEmp.close();
                dbDef.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            for (Object id : itemsToDel) {
                importTable.removeItem(id);
            }
            if (importTable.size() != 0) {
                Notification.show(counter + " " + myUI.getMessage(Messages.InsertedAmount)
                                + " " + myUI.getMessage(Messages.NotInserted),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                Notification.show(counter + " " + myUI.getMessage(Messages.InsertedAmount),
                        Notification.Type.HUMANIZED_MESSAGE);
            }
            if (counter > 0) {
                setTableData();
            }
        } else if (source == branchesPDF_Btn) {
            try {
                DbBranch dbCon = new DbBranch();
                dbCon.connect();
                new BranchCodesPdf(myUI, dbCon.execSQL(myUI));
                dbCon.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == templateBtn) {
            try {
                myUI.getPage().open(new FileResource(
                                new File(Settings.PATH_TO_UPLOADS + "star_hr_branches.xlsx")),
                        "_blank", false);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == yearSelect) {
            if (yearSelect.getValue() != null) {
                setTableData();
            }
        }
    }

    private void execDelete() {
        try {
            DbEmployeeLessons dbCon = new DbEmployeeLessons();
            dbCon.connect();
            dbCon.exec_delete(myUI.getUser().getSchool().getId(), (Integer) yearSelect.getValue());
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private IndexedContainer buildContainer(List<EmployeeBranchesExcel> list) {

        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.LecturerID), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.CourseCode), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ClassName), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Hours), Integer.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.ExtraHours), Integer.class, null);
        for (int i = 0; i < list.size(); i++) {
            Item item = container.addItem(i);
            if (item != null) {
                item.getItemProperty(myUI.getMessage(Messages.LecturerID)).setValue(list.get(i).getLogin());
                item.getItemProperty(myUI.getMessage(Messages.CourseCode)).setValue(
                        list.get(i).getCourse_code());
                try {
                    item.getItemProperty(myUI.getMessage(Messages.Hours)).setValue(
                            Integer.parseInt(list.get(i).getHours()));
                } catch (Exception ignored) {
                }
                try {
                    item.getItemProperty(myUI.getMessage(Messages.ExtraHours)).setValue(
                            Integer.parseInt(list.get(i).getExtra_hours()));
                } catch (Exception ignored) {
                }
                try {
                    item.getItemProperty(myUI.getMessage(Messages.ClassName)).setValue(
                            Integer.parseInt(list.get(i).getClass_number()));
                } catch (Exception ignored) {
                }
            }
        }
        return container;
    }

    public Component getNewObj() {
        return new ImportBranchesFromExcelView(myUI);
    }
}
