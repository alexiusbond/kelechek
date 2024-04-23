package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.*;
import kg.alex.indigo.domain.EmployeeMessage;
import kg.alex.indigo.domain.OrderMessage;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.pdf.OrderPdf;
import kg.alex.indigo.tableexport.EnhancedFormatExcelExport;
import kg.alex.indigo.utils.FormattedFilterTable;
import kg.alex.indigo.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.vaadin.addons.comboboxmultiselect.ComboBoxMultiselect;
import org.vaadin.dialogs.ConfirmDialog;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class SendOrderView extends HorizontalSplitPanel implements Button.ClickListener,
        Property.ValueChangeListener {

    static final Logger logger = LogManager.getLogger(SendOrderView.class);
    private final MyVaadinUI myUI;
    private final Button excelBtn;
    private final FormattedFilterTable dataTable;
    private final Table tableForExport;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Button sendBtn;
    private ComboBox schoolSelect, studentSelect, yearSelect, unitSelect;
    private ComboBoxMultiselect employeeMCB;
    private TextField orderNumberTF, discountTF, studentTF;
    private DateField dateDF;
    private TextArea contentRTA, messageTA, headlineTA;
    private GridLayout settingsLay;

    public SendOrderView(MyVaadinUI myUI) {
        this.myUI = myUI;

        String[] NATURAL_COL_ORDER = new String[]{myUI.getMessage(IndigoMessages.Date),
                myUI.getMessage(IndigoMessages.Employee), myUI.getMessage(IndigoMessages.OrderNumber),
                myUI.getMessage(IndigoMessages.Student), myUI.getMessage(IndigoMessages.Year), myUI.getMessage(IndigoMessages.Discount),
                myUI.getMessage(IndigoMessages.Title), myUI.getMessage(IndigoMessages.Message), myUI.getMessage(IndigoMessages.Status),
                Settings.button};
        buildSettingsLayout();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setMargin(true);
        vl.setSpacing(true);

        tableForExport = new Table();
        tableForExport.setHeight("1%");
        tableForExport.setVisible(false);
        vl.addComponent(tableForExport);

        dataTable = new FormattedFilterTable(myUI);
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setStyleName(ValoTheme.TABLE_SMALL);
        dataTable.addStyleName("noWrap");
        dataTable.setSizeFull();
        dataTable.setFilterBarVisible(true);
        dataTable.setFooterVisible(true);
        try {
            DbOrderMessage dbCon = new DbOrderMessage();
            dbCon.connect();
            if (currentUser.hasRole(Settings.rnAdmin)) {
                dbCon.execSQL(myUI, 0, dataTable, this);
            } else {
                dbCon.execSQL(myUI, myUI.getUser().getId(), dataTable, this);
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        dataTable.setColumnExpandRatio(myUI.getMessage(IndigoMessages.Message), 1);
        dataTable.setColumnWidth(myUI.getMessage(IndigoMessages.Date), 80);
        dataTable.setColumnWidth(myUI.getMessage(IndigoMessages.Title), 240);
        dataTable.setColumnWidth(Settings.button, 60);
        dataTable.setCellStyleGenerator((CustomTable.CellStyleGenerator) (source, itemId, propertyId) -> {

            if (propertyId == null) {
                // Styling for row
                if ((Integer) source.getContainerProperty(itemId,
                        Settings.status_id).getValue() == 2) {
                    return "highlight-red";
                } else {
                    return null;
                }
            } else {
                // styling for column propertyId
                return null;
            }
        });
        vl.addComponent(dataTable);

        excelBtn = new Button();
        excelBtn.setCaption(myUI.getMessage(IndigoMessages.ExportToExcel));
        excelBtn.setStyleName(ValoTheme.BUTTON_PRIMARY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.SHARE_SQUARE_O);
        excelBtn.addClickListener(this);
        vl.addComponent(excelBtn);
        vl.setComponentAlignment(excelBtn, Alignment.BOTTOM_RIGHT);
        vl.setExpandRatio(dataTable, 1);

        this.setSplitPosition(31, Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(settingsLay);
        this.setSecondComponent(vl);
    }

    private void buildSettingsLayout() {

        settingsLay = new GridLayout(4, 7);
        settingsLay.setMargin(new MarginInfo(true, false, true, true));
        settingsLay.setSpacing(true);
        settingsLay.setSizeFull();

        schoolSelect = new ComboBox(myUI.getMessage(IndigoMessages.School));
        schoolSelect.setNullSelectionAllowed(false);
        schoolSelect.setRequired(true);
        schoolSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        schoolSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        schoolSelect.setWidth(Settings.PERCENTS100);
        schoolSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        schoolSelect.setFilteringMode(FilteringMode.CONTAINS);
        schoolSelect.addValueChangeListener(this);
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            schoolSelect.setContainerDataSource(dbs.execSchoolSel(myUI, 1));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        settingsLay.addComponent(schoolSelect, 0, 0, 3, 0);

        employeeMCB = new ComboBoxMultiselect(myUI.getMessage(IndigoMessages.ToEmployees));
        employeeMCB.setRequired(true);
        employeeMCB.setStyleName(ValoTheme.COMBOBOX_SMALL);
        employeeMCB.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        employeeMCB.setWidth(Settings.PERCENTS100);
        employeeMCB.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        employeeMCB.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(employeeMCB, 0, 1, 3, 1);

        studentSelect = new ComboBox(myUI.getMessage(IndigoMessages.Student));
        studentSelect.setNullSelectionAllowed(false);
        studentSelect.setRequired(true);
        studentSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        studentSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        studentSelect.setWidth(Settings.PERCENTS100);
        studentSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        studentSelect.setFilteringMode(FilteringMode.CONTAINS);
        studentSelect.addValueChangeListener(this);
        settingsLay.addComponent(studentSelect, 0, 2, 1, 2);

        studentTF = new TextField(myUI.getMessage(IndigoMessages.FullName));
        studentTF.setEnabled(false);
        studentTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        studentTF.setWidth(Settings.PERCENTS100);
        studentTF.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), null, 200, true));
        studentTF.addValueChangeListener(this);
        settingsLay.addComponent(studentTF, 2, 2, 3, 2);

        dateDF = new DateField(myUI.getMessage(IndigoMessages.Date));
        dateDF.setResolution(Resolution.MINUTE);
        dateDF.setWidth(Settings.PERCENTS100);
        dateDF.setStyleName(ValoTheme.DATEFIELD_SMALL);
        dateDF.setRequired(true);
        dateDF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        dateDF.setDateFormat(Settings.datePattern);
        dateDF.setValue(new Date());
        settingsLay.addComponent(dateDF, 0, 3);

        orderNumberTF = new TextField(myUI.getMessage(IndigoMessages.OrderNumber));
        orderNumberTF.setRequired(true);
        orderNumberTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        orderNumberTF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        orderNumberTF.setWidth(Settings.PERCENTS100);
        orderNumberTF.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 25, false));
        orderNumberTF.setValue("01-31/2  ");
        settingsLay.addComponent(orderNumberTF, 1, 3);

        yearSelect = new ComboBox(myUI.getMessage(IndigoMessages.Year));
        yearSelect.setNullSelectionAllowed(false);
        yearSelect.setRequired(true);
        yearSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        yearSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        yearSelect.setWidth(Settings.PERCENTS100);
        yearSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        yearSelect.setFilteringMode(FilteringMode.CONTAINS);
        settingsLay.addComponent(yearSelect, 2, 3);

        unitSelect = new ComboBox(myUI.getMessage(IndigoMessages.Unit));
        unitSelect.setNullSelectionAllowed(false);
        unitSelect.setRequired(true);
        unitSelect.setStyleName(ValoTheme.COMBOBOX_SMALL);
        unitSelect.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        unitSelect.setWidth(Settings.PERCENTS100);
        unitSelect.setItemCaptionPropertyId(myUI.getMessage(IndigoMessages.Title));
        unitSelect.setFilteringMode(FilteringMode.CONTAINS);
        unitSelect.addValueChangeListener(this);

        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            yearSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbYear, true));
            unitSelect.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbDiscountUnit, false));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        unitSelect.setValue(3);

        ObjectProperty<Integer> property = new ObjectProperty<>(0);
        discountTF = new TextField(myUI.getMessage(IndigoMessages.Discount), property);
        discountTF.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        discountTF.setRequired(true);
        discountTF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        discountTF.setWidth(Settings.PERCENTS100);
        discountTF.setNullRepresentation("");
        discountTF.setConverter(Settings.getStringToIntegerConverter());
        discountTF.addValidator(new IntegerRangeValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, null));
        discountTF.addValueChangeListener(this);

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);
        hl.addComponent(discountTF);
        hl.addComponent(unitSelect);
        settingsLay.addComponent(hl, 3, 3);

        headlineTA = new TextArea(myUI.getMessage(IndigoMessages.Headline));
        headlineTA.setRows(2);
        headlineTA.setRequired(true);
        headlineTA.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        headlineTA.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        headlineTA.setWidth(Settings.PERCENTS100);
        headlineTA.addValidator(new StringLengthValidator(
                myUI.getMessage(IndigoMessages.NotificationWrongValue), 1, 300, false));
        settingsLay.addComponent(headlineTA, 0, 4, 3, 4);

        contentRTA = new TextArea(myUI.getMessage(IndigoMessages.Content));
        contentRTA.setRows(2);
        contentRTA.setRequired(true);
        contentRTA.setStyleName(ValoTheme.TEXTAREA_SMALL);
        contentRTA.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        contentRTA.setSizeFull();
        settingsLay.addComponent(contentRTA, 0, 5, 3, 5);

        messageTA = new TextArea(myUI.getMessage(IndigoMessages.Message));
        messageTA.setRows(2);
        messageTA.setRequired(true);
        messageTA.setStyleName(ValoTheme.TEXTAREA_SMALL);
        messageTA.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
        messageTA.setWidth(Settings.PERCENTS100);
        settingsLay.addComponent(messageTA, 0, 6, 2, 6);

        sendBtn = new Button();
        sendBtn.setWidth(Settings.PERCENTS100);
        sendBtn.setCaption(myUI.getMessage(IndigoMessages.Send));
        sendBtn.addClickListener(this);
        settingsLay.addComponent(sendBtn, 3, 6);
        settingsLay.setComponentAlignment(sendBtn, Alignment.BOTTOM_RIGHT);
        settingsLay.setRowExpandRatio(5, 1);
        settingsLay.setColumnExpandRatio(0, 0.9f);
        settingsLay.setColumnExpandRatio(1, 0.8f);
        settingsLay.setColumnExpandRatio(2, 0.85f);
        settingsLay.setColumnExpandRatio(3, 1.15f);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == sendBtn) {
            try {
                if (Settings.validate(settingsLay)) {
                    DbOrderMessage dbcn = new DbOrderMessage();
                    dbcn.connect();
                    OrderMessage orderMessage = getOrderMessage();
                    int id = dbcn.exec_insert(orderMessage);
                    if (id != 0) {
                        Iterator<?> iter = ((Set<?>) employeeMCB.getValue()).iterator();
                        DbEmployeeMessage dbem = new DbEmployeeMessage();
                        dbem.connect();
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            EmployeeMessage employeeMessage = new EmployeeMessage();
                            employeeMessage.setEmployee_id((Integer) next);
                            employeeMessage.setOrder_message_id(id);
                            employeeMessage.setMessage_status_id(2);
                            int em_id = dbem.exec_insert(employeeMessage);
                            employeeMessage.setId(em_id);
                            employeeMessage.setEmployee(employeeMCB.getContainerProperty(
                                    next, myUI.getMessage(IndigoMessages.Title)).getValue().toString());
                            addDataContainerItem(employeeMessage, orderMessage);
                        }
                        dbem.close();
                        Notification.show(myUI.getMessage(IndigoMessages.Sent),
                                Notification.Type.HUMANIZED_MESSAGE);
                    } else {
                        Notification.show(myUI.getMessage(IndigoMessages.ValueCanNotBeSaved),
                                Notification.Type.WARNING_MESSAGE);
                    }
                    dbcn.close();
                    clearFields();
                } else {
                    Notification.show(myUI.getMessage(IndigoMessages.NotificationWrongValue),
                            Notification.Type.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    DbOrderMessage dbCon = new DbOrderMessage();
                    dbCon.connect();
                    if (currentUser.hasRole(Settings.rnAdmin)) {
                        tableForExport.setContainerDataSource(dbCon.execSQL(myUI, 0, null, this));
                    } else {
                        tableForExport.setContainerDataSource(
                                dbCon.execSQL(myUI, myUI.getUser().getId(), null, this));
                    }
                    dbCon.close();
                    tableForExport.setColumnCollapsingAllowed(true);
                    tableForExport.setColumnCollapsed(Settings.button, true);
                    tableForExport.setColumnCollapsed(Settings.status_id, true);
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(tableForExport, "sheet1");
                    excelReport.excludeCollapsedColumns();
                    excelReport.setReportTitle(myUI.getMessage(IndigoMessages.SendOrders));
                    excelReport.setDisplayTotals(true);
                    excelReport.export();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (source.getId() != null && source.getId().equals(Settings.actDelete)) {
            EmployeeMessage employeeMessage = (EmployeeMessage) source.getData();
            if ((Integer) dataTable.getContainerProperty(employeeMessage.getId(),
                    Settings.status_id).getValue() == 2) {
                ConfirmDialog.show(myUI, myUI.getMessage(IndigoMessages.Question),
                        myUI.getMessage(IndigoMessages.ConfirmDeletion),
                        myUI.getMessage(IndigoMessages.Yes),
                        myUI.getMessage(IndigoMessages.No),
                        (ConfirmDialog.Listener) dialog -> {
                            if (dialog.isConfirmed()) {
                                execDelete(employeeMessage);
                            }
                        });
            } else {
                Notification.show(myUI.getMessage(IndigoMessages.CanNotDelete),
                        Notification.Type.WARNING_MESSAGE);
            }
        } else {
            new OrderPdf(myUI, (OrderMessage) source.getData());
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == schoolSelect && schoolSelect.getValue() != null) {
            try {
                headlineTA.setValue(schoolSelect.getContainerProperty(schoolSelect.getValue(),
                        myUI.getMessage(IndigoMessages.TitleKg)).getValue().toString().toUpperCase()
                        + "НИН ДИРЕКТОРУНА");
                DbStudent dbSt = new DbStudent();
                dbSt.connect();
                studentSelect.setContainerDataSource(dbSt.exec_for_select(myUI,
                        (Integer) schoolSelect.getValue(), myUI.getUser().getCurrent_year().getId(), "1,2,3"));
                Item item = ((IndexedContainer) studentSelect.getContainerDataSource()).addItemAt(0, 0);
                item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(
                        myUI.getMessage(IndigoMessages.Other));
                dbSt.close();
                DbEmployee dbe = new DbEmployee();
                dbe.connect();
                employeeMCB.setContainerDataSource(dbe.exec_for_select(myUI, (Integer) schoolSelect.getValue(),
                        myUI.getUser().getId(), false, false));
                dbe.close();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if ((property == studentSelect || property == yearSelect || property == unitSelect
                || property == discountTF || property == studentTF)
                && discountTF != null && studentTF != null && studentSelect != null
                && studentSelect.getValue() != null && yearSelect != null
                && yearSelect.getValue() != null && unitSelect.getValue() != null && discountTF.getValue() != null
                && studentTF.getValue() != null) {
            String student, class_name = "", discount = "", school = "Лицейдин ";
            if ((Integer) studentSelect.getValue() == 0) {
                student = studentTF.getValue();
            } else {
                class_name = studentSelect.getContainerProperty(studentSelect.getValue(),
                        myUI.getMessage(IndigoMessages.ClassNumber)).getValue().toString();
                student = studentSelect.getContainerProperty(studentSelect.getValue(),
                        myUI.getMessage(IndigoMessages.FullName)).getValue().toString();
            }
            if ((Integer) unitSelect.getValue() == 1) {
                discount = discountTF.getPropertyDataSource().getValue() + "% жеңилдик берилсин.";
            } else if ((Integer) unitSelect.getValue() == 2) {
                discount = "УБ КР курсу менен " + discountTF.getPropertyDataSource().getValue()
                        + " АКШ доллар жеңилдик берилсин.";
                school = "Эл аралык мектептин ";
            } else {
                discount = discountTF.getPropertyDataSource().getValue() + " сом жеңилдик берилсин.";
            }
            contentRTA.setValue(school + class_name + "-классынын окуучусу " + student
                    + "га “Сапаттын” акылуу билим берүү кызмат көрсөтүүдөгү жеңилдиктер жөнүндөгү " +
                    "Жобосунун 3-пунктунун негизинде "
                    + yearSelect.getItemCaption(yearSelect.getValue())
                    + "-окуу жылынын окуу төлөмүндө " + discount);
        }
        if (property == studentSelect) {
            if (studentSelect.getValue() != null && (Integer) studentSelect.getValue() == 0) {
                studentTF.setEnabled(true);
                studentTF.setRequired(true);
                studentTF.setRequiredError(myUI.getMessage(IndigoMessages.RequiredField));
            } else {
                studentTF.setEnabled(false);
                studentTF.setRequired(false);
                studentTF.setValue("");
            }
        }
    }

    private void clearFields() {
        headlineTA.setValue("");
        orderNumberTF.setValue("01-31/2  ");
        discountTF.setValue(null);
        contentRTA.setValue("");
        messageTA.setValue("");
        schoolSelect.setValue(null);
        dateDF.setValue(new Date());
        studentSelect.setValue(null);
        yearSelect.setValue(null);
        unitSelect.setValue(null);
        employeeMCB.setValue(null);
        studentTF.setValue("");
    }

    private void addDataContainerItem(EmployeeMessage employeeMessage, OrderMessage orderMessage) {
        Item item = ((IndexedContainer) dataTable.getContainerDataSource())
                .addItemAt(0, employeeMessage.getId());
        item.getItemProperty(myUI.getMessage(IndigoMessages.Date)).setValue(
                Settings.df.format(dateDF.getValue()));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Employee)).setValue(
                employeeMessage.getEmployee());
        item.getItemProperty(myUI.getMessage(IndigoMessages.Message)).setValue(messageTA.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(headlineTA.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.OrderNumber)).setValue(orderNumberTF.getValue());
        item.getItemProperty(myUI.getMessage(IndigoMessages.Discount)).setValue(discountTF.getPropertyDataSource().getValue());
        String student;
        if ((Integer) studentSelect.getValue() == 0) {
            student = studentTF.getValue();
        } else {
            student = studentSelect.getContainerProperty(studentSelect.getValue(),
                    myUI.getMessage(IndigoMessages.FullName)).getValue().toString();
        }
        item.getItemProperty(myUI.getMessage(IndigoMessages.Year)).setValue(yearSelect.getItemCaption(yearSelect.getValue()));
        item.getItemProperty(myUI.getMessage(IndigoMessages.Student)).setValue(student);
        item.getItemProperty(Settings.status_id).setValue(2);
        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.addComponent(createButton(myUI.getMessage(IndigoMessages.DeleteButton),
                Settings.actDelete, FontAwesome.BAN, employeeMessage));
        hl.addComponent(createButton(myUI.getMessage(IndigoMessages.ViewDocument),
                Settings.actPdf, FontAwesome.FILE_PDF_O, orderMessage));
        item.getItemProperty(Settings.button).setValue(hl);
        item.getItemProperty(myUI.getMessage(IndigoMessages.Status)).setValue(
                myUI.getMessage(IndigoMessages.UnRead));
    }

    public Button createButton(String description, String button_id, Resource icon, Object data) {
        Button btn = new Button();
        btn.setDescription(description);
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.addStyleName(ValoTheme.BUTTON_TINY);
        btn.setIcon(icon);
        btn.setData(data);
        btn.setId(button_id);
        btn.addClickListener(this);
        return btn;
    }

    private OrderMessage getOrderMessage() {
        OrderMessage om = new OrderMessage();
        om.setMessage(messageTA.getValue());
        om.setContent(contentRTA.getValue());
        om.setStudent_id((Integer) studentSelect.getValue());
        om.setYear_id((Integer) yearSelect.getValue());
        om.setDiscount_unit_id((Integer) unitSelect.getValue());
        om.setCurrencyRate(myUI.getDb_currency_rate());
        if (om.getStudent_id() == 0) {
            om.setStudent(studentTF.getValue());
        }
        om.setDate(dateDF.getValue());
        om.setOrder_number(orderNumberTF.getValue());
        om.setDiscount((Integer) discountTF.getPropertyDataSource().getValue());
        om.setTitle(headlineTA.getValue());
        om.setEmployee_id(myUI.getUser().getId());
        om.setId(0);
        return om;
    }

    private void execDelete(EmployeeMessage employeeMessage) {
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            int st = dbDef.exec_delete(employeeMessage.getId(),
                    Settings.dbEmployeeMessageTable);
            if (st != 0) {
                dataTable.getContainerDataSource().removeItem(employeeMessage.getId());
                try {
                    dbDef.exec_delete(employeeMessage.getOrder_message_id(),
                            Settings.orderMessagesTable);
                } catch (Exception ignored) {
                }
            }
            dbDef.close();
        } catch (SQLIntegrityConstraintViolationException e) {
            Notification.show(myUI.getMessage(IndigoMessages.CanNotDeleteRead),
                    Notification.Type.WARNING_MESSAGE);
            logger.error(e);
            logger.catching(e);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
