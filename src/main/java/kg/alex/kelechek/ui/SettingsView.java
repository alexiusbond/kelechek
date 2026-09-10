/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.ui;

import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.dao.*;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SettingsView extends GridLayout {

    static final Logger logger = LogManager.getLogger(SettingsView.class);

    public SettingsView(MyVaadinUI myUI) {

        this.setWidth(Settings.PERCENTS100);
        this.setSpacing(true);
        this.setMargin(true);

        IndexedContainer paymentsContainer = null;
        IndexedContainer typesContainer = null;
        IndexedContainer salaryContainer = null;
        IndexedContainer productsContainer = null;
        IndexedContainer positionsContainer = null;
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            List<String> properties = new ArrayList<>();
            properties.add(Settings.hr_position_id);
            positionsContainer = dbDef.exec_for_select(myUI, Settings.positionTable, properties, 0);
            properties = new ArrayList<>();
            properties.add(Settings.acc_category_id);
            productsContainer = dbDef.exec_for_select(myUI, Settings.dpProductCategoryTable, properties, 0);
            typesContainer = dbDef.exec_for_select(myUI, Settings.dbAccType, properties, 5);
            salaryContainer = dbDef.exec_for_select(myUI, Settings.dbSalaryCategory, properties, 0);
            properties.add(Settings.acc_type_id);
            paymentsContainer = dbDef.exec_for_select(myUI, Settings.paymentCategoryTable, properties, 0);
            dbDef.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        this.setRows((positionsContainer.size() + 1) > (paymentsContainer.size() +
                salaryContainer.size() + productsContainer.size() + typesContainer.size()) ? positionsContainer.size() + 1 + 2 :
                paymentsContainer.size() + salaryContainer.size() + productsContainer.size() + typesContainer.size() + 2);
        this.setColumns(4);

        Label captionAccounting = new Label();
        captionAccounting.setSizeFull();
        captionAccounting.setContentMode(ContentMode.HTML);
        captionAccounting.setValue(myUI.getMessage(Messages.AccountingSettings));
        captionAccounting.setStyleName("tableCpt");
        int row = 0;
        addComponent(captionAccounting, 0, row, 1, row);
        row += 1;
        try {
            DbAccCategory dbCon = new DbAccCategory();
            dbCon.connect();
            IndexedContainer expensesContainer = dbCon.exec_for_select(myUI, 2);
            IndexedContainer incomesContainer = dbCon.exec_for_select(myUI, 1);
            IndexedContainer incomesAndExpensesContainer = dbCon.exec_for_select(myUI, 5);
            dbCon.close();
            List<?> itemIds = paymentsContainer.getItemIds();
            for (int i = 0; i < itemIds.size(); i++) {
                Label l = new Label();
                l.setSizeUndefined();
                l.setValue(paymentsContainer.getContainerProperty(itemIds.get(i),
                        myUI.getMessage(Messages.Title)).getValue().toString());
                addComponent(l, 0, (i + row));
                setComponentAlignment(l, Alignment.BOTTOM_LEFT);

                ComboBox cb = new ComboBox();
                cb.setNullSelectionAllowed(false);
                cb.setRequired(true);
                cb.setData(itemIds.get(i));
                cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
                cb.setStyleName(ValoTheme.COMBOBOX_SMALL);
                cb.setWidth(Settings.PERCENTS100);
                cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
                cb.setFilteringMode(FilteringMode.CONTAINS);
                if ((Integer) paymentsContainer.getContainerProperty(itemIds.get(i),
                        Settings.acc_type_id).getValue() == 1) {
                    cb.setContainerDataSource(Settings.copyContainer(incomesContainer));
                } else if ((Integer) paymentsContainer.getContainerProperty(itemIds.get(i),
                        Settings.acc_type_id).getValue() == 2) {
                    cb.setContainerDataSource(Settings.copyContainer(expensesContainer));
                }
                cb.setValue(paymentsContainer.getContainerProperty(itemIds.get(i), Settings.acc_category_id).getValue());
                cb.addValueChangeListener((Property.ValueChangeListener) event -> {
                    try {
                        DbPaymentCategory dbp = new DbPaymentCategory();
                        dbp.connect();
                        if (dbp.exec_update((Integer) cb.getValue(), (Integer) cb.getData()) != 0) {
                            Notification.show(myUI.getMessage(Messages.ValueSaved));
                        }
                        dbp.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                });
                addComponent(cb, 1, (i + row));
            }
            row += paymentsContainer.size();
            itemIds = salaryContainer.getItemIds();
            for (int i = 0; i < itemIds.size(); i++) {
                Label l = new Label();
                l.setSizeUndefined();
                l.setValue(salaryContainer.getContainerProperty(itemIds.get(i),
                        myUI.getMessage(Messages.Title)).getValue().toString());
                addComponent(l, 0, (i + row));
                setComponentAlignment(l, Alignment.BOTTOM_LEFT);

                ComboBox cb = new ComboBox();
                cb.setNullSelectionAllowed(false);
                cb.setRequired(true);
                cb.setData(itemIds.get(i));
                cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
                cb.setStyleName(ValoTheme.COMBOBOX_SMALL);
                cb.setWidth(Settings.PERCENTS100);
                cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
                cb.setFilteringMode(FilteringMode.CONTAINS);
                cb.setContainerDataSource(Settings.copyContainer(expensesContainer));
                cb.setValue(salaryContainer.getContainerProperty(itemIds.get(i), Settings.acc_category_id).getValue());
                cb.addValueChangeListener((Property.ValueChangeListener) event -> {
                    try {
                        DbSalaryCategories dbp = new DbSalaryCategories();
                        dbp.connect();
                        if (dbp.exec_update((Integer) cb.getValue(), (Integer) cb.getData()) != 0) {
                            Notification.show(myUI.getMessage(Messages.ValueSaved));
                        }
                        dbp.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                });
                addComponent(cb, 1, (i + row));
            }
            row += salaryContainer.size();
            itemIds = typesContainer.getItemIds();
            for (int i = 0; i < itemIds.size(); i++) {
                Label l = new Label();
                l.setSizeUndefined();
                l.setValue(typesContainer.getContainerProperty(itemIds.get(i),
                        myUI.getMessage(Messages.Title)).getValue().toString());
                addComponent(l, 0, (i + row));
                setComponentAlignment(l, Alignment.BOTTOM_LEFT);

                ComboBox cb = new ComboBox();
                cb.setNullSelectionAllowed(false);
                cb.setRequired(true);
                cb.setData(itemIds.get(i));
                cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
                cb.setStyleName(ValoTheme.COMBOBOX_SMALL);
                cb.setWidth(Settings.PERCENTS100);
                cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
                cb.setFilteringMode(FilteringMode.CONTAINS);
                cb.setContainerDataSource(Settings.copyContainer(incomesAndExpensesContainer));
                cb.setValue(typesContainer.getContainerProperty(itemIds.get(i), Settings.acc_category_id).getValue());
                cb.addValueChangeListener((Property.ValueChangeListener) event -> {
                    try {
                        DbAccType dbAccType = new DbAccType();
                        dbAccType.connect();
                        if (dbAccType.exec_update((Integer) cb.getValue(), (Integer) cb.getData()) != 0) {
                            Notification.show(myUI.getMessage(Messages.ValueSaved));
                        }
                        dbAccType.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                });
                addComponent(cb, 1, (i + row));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        Label captionStock = new Label();
        captionStock.setSizeFull();
        captionStock.setContentMode(ContentMode.HTML);
        captionStock.setValue(myUI.getMessage(Messages.StockSettings));
        captionStock.setStyleName("tableCpt");
        row += typesContainer.size();
        addComponent(captionStock, 0, row, 1, row);
        row += 1;
        try {
            DbAccCategory dbCon = new DbAccCategory();
            dbCon.connect();
            IndexedContainer container = dbCon.exec_for_select(myUI, 2);
            dbCon.close();
            List<?> itemIds = productsContainer.getItemIds();
            for (int i = 0; i < itemIds.size(); i++) {
                Label l = new Label();
                l.setSizeUndefined();
                l.setValue(productsContainer.getContainerProperty(itemIds.get(i),
                        myUI.getMessage(Messages.Title)).getValue().toString());
                addComponent(l, 0, (i + row));
                setComponentAlignment(l, Alignment.BOTTOM_LEFT);

                ComboBox cb = new ComboBox();
                cb.setNullSelectionAllowed(false);
                cb.setRequired(true);
                cb.setData(itemIds.get(i));
                cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
                cb.setStyleName(ValoTheme.COMBOBOX_SMALL);
                cb.setWidth(Settings.PERCENTS100);
                cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
                cb.setFilteringMode(FilteringMode.CONTAINS);
                cb.setContainerDataSource(Settings.copyContainer(container));
                cb.setValue(productsContainer.getContainerProperty(itemIds.get(i), Settings.acc_category_id).getValue());
                cb.addValueChangeListener((Property.ValueChangeListener) event -> {
                    try {
                        DbProductCategories dbp = new DbProductCategories();
                        dbp.connect();
                        if (dbp.exec_update((Integer) cb.getValue(), (Integer) cb.getData()) != 0) {
                            Notification.show(myUI.getMessage(Messages.ValueSaved));
                        }
                        dbp.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                });
                addComponent(cb, 1, (i + row));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        Label captionPositions = new Label();
        captionPositions.setSizeFull();
        captionPositions.setContentMode(ContentMode.HTML);
        captionPositions.setValue(myUI.getMessage(Messages.PositionsSettings));
        captionPositions.setStyleName("tableCpt");
        row = 0;
        addComponent(captionPositions, 2, row, 3, row);
        row += 1;
        try {
            DbDefinition dbDef = new DbDefinition();
            dbDef.connect();
            IndexedContainer container = dbDef.exec_for_select(myUI, Settings.hr_positionTable, true);
            dbDef.close();
            List<?> itemIds = positionsContainer.getItemIds();
            for (int i = 0; i < itemIds.size(); i++) {
                Label l = new Label();
                l.setSizeUndefined();
                l.setValue(positionsContainer.getContainerProperty(itemIds.get(i),
                        myUI.getMessage(Messages.Title)).getValue().toString());
                addComponent(l, 2, (i + row));
                setComponentAlignment(l, Alignment.BOTTOM_LEFT);

                ComboBox cb = new ComboBox();
                cb.setNullSelectionAllowed(false);
                cb.setRequired(true);
                cb.setData(itemIds.get(i));
                cb.setRequiredError(myUI.getMessage(Messages.RequiredField));
                cb.setStyleName(ValoTheme.COMBOBOX_SMALL);
                cb.setWidth(Settings.PERCENTS100);
                cb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
                cb.setFilteringMode(FilteringMode.CONTAINS);
                cb.setContainerDataSource(Settings.copyContainer(container));
                cb.setValue(positionsContainer.getContainerProperty(itemIds.get(i), Settings.hr_position_id).getValue());
                cb.addValueChangeListener((Property.ValueChangeListener) event -> {
                    try {
                        DbPosition dbp = new DbPosition();
                        dbp.connect();
                        if (dbp.exec_update((Integer) cb.getValue(), (Integer) cb.getData()) != 0) {
                            Notification.show(myUI.getMessage(Messages.ValueSaved));
                        }
                        dbp.close();
                    } catch (Exception e) {
                        logger.error(e);
                        logger.catching(e);
                    }
                });
                addComponent(cb, 3, (i + row));
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        Label captionExams = new Label();
        captionExams.setSizeFull();
        captionExams.setContentMode(ContentMode.HTML);
        captionExams.setValue(myUI.getMessage(Messages.ExamsSettings));
        captionExams.setStyleName("tableCpt");

        Label sebatExamLab = new Label();
        sebatExamLab.setSizeUndefined();
        sebatExamLab.setValue(myUI.getMessage(Messages.KelechekExam));

        ComboBox sebatExamCb = new ComboBox();
        sebatExamCb.setNullSelectionAllowed(false);
        sebatExamCb.setRequired(true);
        sebatExamCb.setData(5);
        sebatExamCb.setRequiredError(myUI.getMessage(Messages.RequiredField));
        sebatExamCb.setStyleName(ValoTheme.COMBOBOX_SMALL);
        sebatExamCb.setWidth(Settings.PERCENTS100);
        sebatExamCb.setItemCaptionPropertyId(myUI.getMessage(Messages.Title));
        sebatExamCb.setFilteringMode(FilteringMode.CONTAINS);

        try {
            DbDefinition dbd = new DbDefinition();
            dbd.connect();
            sebatExamCb.setContainerDataSource(dbd.exec_for_select(myUI, Settings.dbExamTable, true));
            dbd.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        int main_exam = 0;
        try {
            DbExam dbe = new DbExam();
            dbe.connect();
            main_exam = dbe.exec_main_exam();
            dbe.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }

        sebatExamCb.setValue(main_exam);
        row += positionsContainer.size();
        addComponent(captionExams, 2, row, 3, row);
        row++;
        addComponent(sebatExamLab, 2, row);
        addComponent(sebatExamCb, 3, row);
        setComponentAlignment(sebatExamLab, Alignment.BOTTOM_LEFT);
    }
}
