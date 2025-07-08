package kg.alex.lomonosov.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.dao.*;
import kg.alex.lomonosov.domain.School;
import kg.alex.lomonosov.i18n.Messages;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SchoolModificationView extends GridLayout implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(SchoolModificationView.class);
    private final MyVaadinUI myUI;
    private final Button createBtn;
    private final Button modifyBtn;
    private final Button deleteBtn;
    private final Button saveBtn;
    private final Button cancelBtn;
    private final ComboBox statusSelect, typeSelect;
    private final TextField nameKgTF, nameEnTF, codeTF, nameRuTF, addressTF, innTF, bankTF,
            bankAccountTF, phoneTF, cityTF;

    private final int school_id;
    private final Embedded photoEmb;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Upload photoUpl;
    private File myFile;
    private Window statusWindow;
    private Button cancelButton;
    private ProgressBar uploadProgressBar;
    private String photoName, mimeType;
    private School school = new School();

    public SchoolModificationView(MyVaadinUI myUI, int scl_id) {
        this.myUI = myUI;
        this.school_id = scl_id;

        this.setRows(8);
        this.setColumns(3);
        this.setWidth("60%");
        this.setSpacing(true);
        this.setMargin(true);

        HorizontalLayout buttonsLay = new HorizontalLayout();
        buttonsLay.setSpacing(true);

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

        codeTF = new TextField(myUI.getMessage(Messages.Code));
        codeTF.setRequired(true);
        codeTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        codeTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        codeTF.setWidth(Settings.PERCENTS100);

        nameRuTF = new TextField(myUI.getMessage(Messages.TitleRu));
        nameRuTF.setRequiredError(myUI.getMessage(Messages.RequiredField));
        nameRuTF.setRequired(true);
        nameRuTF.setNullRepresentation("");
        nameRuTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameRuTF.setWidth(Settings.PERCENTS100);

        nameKgTF = new TextField(myUI.getMessage(Messages.TitleKg));
        nameKgTF.setNullRepresentation("");
        nameKgTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameKgTF.setWidth(Settings.PERCENTS100);

        nameEnTF = new TextField(myUI.getMessage(Messages.TitleEn));
        nameEnTF.setNullRepresentation("");
        nameEnTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        nameEnTF.setWidth(Settings.PERCENTS100);

        cityTF = new TextField(myUI.getMessage(Messages.City) + " (Ru/Kg)");
        cityTF.setRequired(false);
        cityTF.setNullRepresentation("");
        cityTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        cityTF.setWidth(Settings.PERCENTS100);

        addressTF = new TextField(myUI.getMessage(Messages.Address) + " (Ru/Kg)");
        addressTF.setRequired(false);
        addressTF.setNullRepresentation("");
        addressTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        addressTF.setWidth(Settings.PERCENTS100);

        innTF = new TextField(myUI.getMessage(Messages.INN));
        innTF.setRequired(false);
        innTF.setNullRepresentation("");
        innTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        innTF.setWidth(Settings.PERCENTS100);

        bankTF = new TextField(myUI.getMessage(Messages.Bank) + " (Ru/Kg)");
        bankTF.setRequired(false);
        bankTF.setNullRepresentation("");
        bankTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankTF.setWidth(Settings.PERCENTS100);

        bankAccountTF = new TextField(myUI.getMessage(Messages.BankAccount));
        bankAccountTF.setRequired(false);
        bankAccountTF.setNullRepresentation("");
        bankAccountTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        bankAccountTF.setWidth(Settings.PERCENTS100);

        phoneTF = new TextField(myUI.getMessage(Messages.Phone));
        phoneTF.setRequired(false);
        phoneTF.setNullRepresentation("");
        phoneTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        phoneTF.setWidth(Settings.PERCENTS100);

        typeSelect = new ComboBox(myUI.getMessage(Messages.SchoolType));
        typeSelect.setNullSelectionAllowed(false);
        typeSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        typeSelect.setRequired(true);
        typeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        typeSelect.setWidth(Settings.PERCENTS100);
        typeSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        typeSelect.setFilteringMode(FilteringMode.CONTAINS);

        statusSelect = new ComboBox(myUI.getMessage(Messages.Status));
        statusSelect.setNullSelectionAllowed(false);
        statusSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        statusSelect.setRequired(true);
        statusSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        statusSelect.setWidth(Settings.PERCENTS100);
        statusSelect.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        statusSelect.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            typeSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbSchoolType, false));
            statusSelect.setContainerDataSource(
                    dbDef.exec_for_select(myUI, Settings.dbActivity_status, true));
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        photoEmb = new Embedded();
        photoEmb.setHeight("100%");
        photoEmb.setWidth("95%");
        photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS + "no_photo.jpg")));
        photoEmb.setImmediate(true);
        buildUpload();

        this.addComponent(buttonsLay, 0, 0, 1, 0);
        this.addComponent(codeTF, 0, 1);
        this.addComponent(nameRuTF, 1, 1);
        this.addComponent(nameKgTF, 0, 2);
        this.addComponent(nameEnTF, 1, 2);
        this.addComponent(cityTF, 0, 3);
        this.addComponent(phoneTF, 1, 3);
        this.addComponent(addressTF, 0, 4, 1, 4);
        this.addComponent(bankTF, 0, 5);
        this.addComponent(bankAccountTF, 1, 5);
        this.addComponent(innTF, 0, 6);
        this.addComponent(typeSelect, 1, 6);
        this.addComponent(statusSelect, 0, 7);
        this.addComponent(photoEmb, 2, 1, 2, 3);
        this.addComponent(photoUpl, 2, 4);

        fillFields(school_id);
        prepareNormalMode();
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source.getId() != null && source.getId().equals(Settings.cancel_upload_button)) {
            if (photoUpl != null) {
                photoUpl.interruptUpload();
            }
        } else if (source == modifyBtn) {
            prepareModificationMode();
        } else if (source == saveBtn) {
            try {
                if (Settings.validate(this)) {
                    DbSchool dbScl = new DbSchool();
                    dbScl.connect();
                    School sch = getSchool(school_id);
                    int status = 0;
                    try {
                        status = dbScl.exec_update(sch);
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                    if (status != 0) {
                        if (!sch.getCode().equals(school.getCode()) || !sch.getName_ru().equals(school.getName_ru())) {
                            DbSalaryCategories dbsc = new DbSalaryCategories();
                            dbsc.connect();
                            IndexedContainer salCont = dbsc.execSQL(myUI);
                            dbsc.close();
                            DbAccType dbAccType = new DbAccType();
                            dbAccType.connect();
                            IndexedContainer typesContainer = dbAccType.execSQL(myUI);
                            dbAccType.close();
                            DbAccCategory dba = new DbAccCategory();
                            dba.connect();
                            for (Object next : salCont.getItemIds()) {
                                int id = dba.exec_id((Integer) next, sch.getId());
                                dba.exec_update_code(id, sch.getCode(), salCont.getContainerProperty(next,
                                        myUI.getMessage(Messages.Title)).getValue() + " - " + sch.getName_ru());
                                dba.exec_update_all_parent_codes(id, salCont.getContainerProperty(next,
                                        myUI.getMessage(Messages.Code)).getValue() + "." + sch.getCode(), false);
                            }
                            for (Object next : typesContainer.getItemIds()) {
                                int id = dba.exec_id((Integer) next, sch.getId());
                                dba.exec_update_code(id, sch.getCode(), typesContainer.getContainerProperty(next,
                                        myUI.getMessage(Messages.Title)).getValue() + " - " + sch.getName_ru());
                                dba.exec_update_all_parent_codes(id, typesContainer.getContainerProperty(next,
                                        myUI.getMessage(Messages.Code)).getValue() + "." + sch.getCode(), false);
                            }
                            dba.close();
                        }
                        Notification.show(myUI.getMessage(Messages.ValueSaved),
                                Notification.Type.HUMANIZED_MESSAGE);
                        myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                                myUI.getMessage(Messages.Title)).setValue(codeTF.getValue() + " - " + nameRuTF.getValue());
                        myUI.getSchoolCont().getContainerProperty(myUI.getUser().getSchool().getId(),
                                myUI.getMessage(Messages.Logo)).setValue(sch.getPhoto());
                    } else {
                        Notification.show(myUI.getMessage(Messages.ValueCanNotBeSaved),
                                Notification.Type.WARNING_MESSAGE);
                    }
                    dbScl.close();
                    prepareNormalMode();
                } else {
                    Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == cancelBtn) {
            prepareNormalMode();
        }
    }

    private void prepareModificationMode() {
        modifyBtn.setEnabled(false);
        createBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        saveBtn.setEnabled(true);
        cancelBtn.setEnabled(true);
        nameKgTF.setEnabled(true);
        nameEnTF.setEnabled(true);
        statusSelect.setEnabled(false);
        typeSelect.setEnabled(true);
        codeTF.setEnabled(false);
        nameRuTF.setEnabled(true);
        cityTF.setEnabled(true);
        innTF.setEnabled(true);
        addressTF.setEnabled(true);
        bankTF.setEnabled(true);
        bankAccountTF.setEnabled(true);
        phoneTF.setEnabled(true);
        photoUpl.setEnabled(true);
    }

    private void prepareNormalMode() {
        if (currentUser.isPermitted(Settings.cnSchoolModificationView + ":" + Settings.actModify)) {
            modifyBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnSchoolModificationView + ":" + Settings.actAdd)) {
            createBtn.setEnabled(true);
        }
        if (currentUser.isPermitted(Settings.cnSchoolModificationView + ":" + Settings.actDelete)) {
            deleteBtn.setEnabled(true);
        }
        saveBtn.setEnabled(false);
        cancelBtn.setEnabled(false);
        nameKgTF.setEnabled(false);
        nameEnTF.setEnabled(false);
        statusSelect.setEnabled(false);
        typeSelect.setEnabled(false);
        codeTF.setEnabled(false);
        nameRuTF.setEnabled(false);
        cityTF.setEnabled(false);
        innTF.setEnabled(false);
        addressTF.setEnabled(false);
        bankTF.setEnabled(false);
        bankAccountTF.setEnabled(false);
        phoneTF.setEnabled(false);
        photoUpl.setEnabled(false);

    }

    private void fillFields(int school_id) {

        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            school = dbs.execSchool(school_id);
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        codeTF.setValue(school.getCode());
        nameKgTF.setValue(school.getName_kg());
        nameEnTF.setValue(school.getName_en());
        nameRuTF.setValue(school.getName_ru());
        cityTF.setValue(school.getCity());
        addressTF.setValue(school.getAddress());
        innTF.setValue(school.getInn());
        bankTF.setValue(school.getBank());
        bankAccountTF.setValue(school.getBank_account());
        phoneTF.setValue(school.getPhone());
        statusSelect.setValue(school.getStatus_id());
        typeSelect.setValue(school.getSchool_type_id());
        if (school.getPhoto() != null && !school.getPhoto().isEmpty()) {
            photoEmb.setSource(new FileResource(new File(Settings.PATH_TO_UPLOADS
                    + school.getPhoto())));
            photoName = school.getPhoto();
        } else {
            photoName = null;
        }
    }

    private School getSchool(int i) {
        School s = new School();
        s.setId(i);
        s.setCode(codeTF.getValue());
        s.setName_kg(nameKgTF.getValue());
        s.setName_en(nameEnTF.getValue());
        s.setName_ru(nameRuTF.getValue());
        s.setCity(cityTF.getValue());
        s.setAddress(addressTF.getValue());
        s.setInn(innTF.getValue());
        s.setBank(bankTF.getValue());
        s.setBank_account(bankAccountTF.getValue());
        s.setPhone(phoneTF.getValue());
        s.setStatus_id((Integer) statusSelect.getValue());
        s.setSchool_type_id((Integer) typeSelect.getValue());
        s.setSchool_type_id(1);
        s.setPhoto(photoName);
        return s;
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

    private void buildUpload() {
        MyReceiver receiver = new MyReceiver();
        photoUpl = new Upload(null, receiver);
        photoUpl.setImmediate(true);
        photoUpl.setStyleName(ValoTheme.BUTTON_SMALL);
        photoUpl.setButtonCaption(myUI.getMessage(Messages.Upload));
        photoUpl.setWidth(Settings.PERCENTS100);

        photoUpl.addStartedListener((Upload.StartedListener) event -> {

            buildUploadWindow();
            myUI.addWindow(statusWindow);
            statusWindow.setClosable(false);

            uploadProgressBar.setValue(0f);
            uploadProgressBar.setVisible(true);
            UI.getCurrent().setPollInterval(500);

            cancelButton.setVisible(true);
        });

        photoUpl.addProgressListener((Upload.ProgressListener) (readBytes, contentLength) -> {
            // This method gets called several times during the update
            if (!mimeType.equals("image/jpeg")) {
                photoUpl.interruptUpload();
                photoName = null;
                Notification.show(myUI.getMessage(Messages.OnlyJpg),
                        Notification.Type.WARNING_MESSAGE);
            } else if (contentLength >= 5000000) {
                photoUpl.interruptUpload();
                photoName = null;
                Notification.show(myUI.getMessage(Messages.Maxsize),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                uploadProgressBar.setValue(readBytes / (float) contentLength);
            }
        });

        photoUpl.addSucceededListener((Upload.SucceededListener) event -> {
            // This method gets called when the upload finished successfully
            try {
                Thumbnails.of(myFile).size(300, 300).toFile(myFile);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
            photoEmb.setSource(new FileResource(myFile));
        });

        photoUpl.addFailedListener((Upload.FailedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
            try {
                myFile.delete();
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
            }
        });

        photoUpl.addFinishedListener((Upload.FinishedListener) event -> {
            if (statusWindow != null) {
                statusWindow.close();
            }
        });
    }

    public class MyReceiver implements Upload.Receiver {

        @Override
        public OutputStream receiveUpload(String filename, String mimetype) {
            mimeType = mimetype;
            FileOutputStream fos; // Output stream to write to
            photoName = codeTF.getValue() + ".jpg";
            try {
                myFile = new File(Settings.PATH_TO_UPLOADS + photoName);

                // Open the file for writing.
                fos = new FileOutputStream(myFile);
            } catch (Exception e) {
                // Error while opening the file. Not reported here.
                logger.error(e);
                logger.catching(e);
                return null;
            }
            return fos; // Return the output stream to write tou
        }
    }
}
