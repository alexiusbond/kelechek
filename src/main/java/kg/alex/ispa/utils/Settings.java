/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.utils;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.ui.*;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.i18n.Messages;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author alex
 */
public class Settings implements Serializable {

    public static final int MAIN_OFFICE_ID = 0;
    public static final String PATH_TO_UPLOADS = "/home/ispa/";
    public static final String PATH_TO_UPLOADS_HR = "/home/ispa/hr/";
    public static final DecimalFormat dFormat2 = new DecimalFormat("#,##0.00");
    public static final DecimalFormat dFormat4 = new DecimalFormat("#,##0.0000");
    public static final String datePattern = "dd-MM-yyyy";
    public static final String yearMonthPattern = "MM-yyyy";
    public static final String yearPattern = "yyyy";
    public static final SimpleDateFormat df = new SimpleDateFormat(datePattern);
    public static final SimpleDateFormat mysql_only_year = new SimpleDateFormat("yyyy-01-01");
    public static final String dateTimeMinPattern = "dd-MM-yyyy HH:mm";
    public static final SimpleDateFormat dtmf = new SimpleDateFormat(dateTimeMinPattern);
    public static final SimpleDateFormat ymdf = new SimpleDateFormat(yearMonthPattern);
    public static final SimpleDateFormat ydf = new SimpleDateFormat(yearPattern);
    public static final String id = "id";
    public static final String count = "count";
    public static final String email = "E-mail";
    public static final String resigned = " [уволен]";
    public static final String transferred = " [переведен]";
    public static final String number_id = "number_id";
    public static final String status_id = "status_id";
    public static final String is_order_discount = "is_order_discount";
    public static final String block_id = "block_id";
    public static final String room_id = "room_id";
    public static final String floor_id = "floor_id";
    public static final String acc_category_id = "acc_category_id";
    public static final String parent_id = "parent_id";
    public static final String school_type_id = "school_type_id";
    public static final String category_id = "category_id";
    public static final String payment_category_id = "payment_category_id";
    public static final String paymentCategoryTable = "payment_category";
    public static final String is_main = "is_main";
    public static final String is_last = "is_last";
    public static final String titleShort = "TitleShort";
    public static final String installmentDateLimit = "installmentDateLimit";
    public static final String year_id = "year_id";
    public static final String school_id = "school_id";
    public static final String discount_type_id = "discount_type_id";
    public static final String correction_type_id = "correction_type_id";
    public static final String position_id = "position_id";
    public static final String hr_position_id = "hr_position_id";
    public static final String dpProductCategoryTable = "dp_product_category";
    public static final String extra_position_ids = "extra_position_ids";
    public static final String salary_category_id = "salary_category_id";
    public static final String gender_id = "gender_id";
    public static final String nationality_id = "nationality_id";
    public static final String citizenship_id = "hr_country_id";
    public static final String martial_status_id = "martial_status_id";
    public static final String activity_status_id = "activity_status_id";
    public static final String working_status_id = "working_status_id";
    public static final String education_status_id = "education_status_id";
    public static final String class_type_id = "class_type_id";
    public static final String class_name_id = "class_name_id";
    public static final String class_id = "class_id";
    public static final String visible_hr_orders = "visible_hr_orders";
    public static final String is_modifiable = "is_modifiable";
    public static final String is_disabled = "is_disabled";
    public static final String columnPhones = "phones";
    public static final String columnExams = "exams";
    public static final String columnSeminars = "seminars";
    public static final String columnCertificates = "certificates";
    public static final String columnChildren = "children";
    public static final String columnLanguages = "languages";
    public static final String PERCENTS100 = "100%";
    public static final String columnBranches = "branches";
    public static final String columnEducation = "education";
    public static final String columnSpouseEducation = "spouse_education";
    public static final String columnSpouseWork_places = "spouse_work_places";
    public static final String columnWork_places = "work_places";
    public static final String order_id = "order_id";
    public static final String max = "max";
    public static final String min = "min";
    public static final String class_order_number = "class_order_number";
    public static final String from_education_status_id = "from_education_status_id";
    public static final String from_class_id = "from_class_id";
    public static final String from_employee_id = "from_employee_id";
    public static final String to_employee_id = "to_employee_id";
    public static final String to_class_id = "to_class_id";
    public static final String student_id = "student_id";
    public static final String stock_id = "stock_id";
    public static final String quantity_id = "remain_id";
    public static final String measurement_id = "measurement_id";
    public static final String crud_status = "crud_status";
    public static final String effected_by_id = "effected_by_id";
    public static final String classTable = "class_number";
    public static final String orderMessagesTable = "order_messages";
    public static final String dbStudentCorrection = "student_correction";
    public static final String dbLanguageTable = "hr_language";
    public static final String dbLanguageLevelTable = "hr_language_level";
    public static final String dbExamTable = "hr_exam";
    public static final String positionCategoryTable = "hr_position_category";
    public static final String dbBranchTable = "hr_branch";
    public static final String dbColumnCode = "code";
    public static final String dbInventoryCategoryTable = "dm_inventory_category";
    public static final String dbInventoryBrandTable = "dm_brand";
    public static final String dbInventoryTitleTable = "dm_title";
    public static final String dbUniversityTable = "hr_university";
    public static final String dbWork_placeTable = "hr_work_place";
    public static final String dbEmployeeWorkExtraPosition = "hr_employee_work_extra_positions";
    public static final String dbEmployeeSpouse = "hr_employee_spouse";
    public static final String dbCertificateTable = "hr_certificate";
    public static final String dbEmployeeMessageTable = "employee_message";
    public static final String dbQuestion = "hr_question";
    public static final String dbActivity_status = "activity_status";
    public static final String dbSchoolType = "school_type";
    public static final String dbClassType = "class_type";
    public static final String dbFloor = "dm_floor";
    public static final String dbAccessoriesCategory = "accessories_category";
    public static final String dbGender = "gender";
    public static final String dbCountry = "hr_country";
    public static final String employee_work_id = "hr_employee_work_id";
    public static final String dbEduLevel = "hr_education_level";
    public static final String dbHealthStatus = "hr_health_status";
    public static final String dbAcc_currency = "acc_currency";
    public static final String acc_currency_id = "acc_currency_id";
    public static final String acc_cashbox_id = "acc_cashbox_id";
    public static final String dbAcc_transactions = "acc_transactions";
    public static final String dbPaymentType = "payment_type";
    public static final String payment_type_id = "payment_type_id";
    public static final String dbWorking_status = "working_status";
    public static final String dbClass_name = "class_name";
    public static final String dbColumnName = "name";
    public static final String dbBlock = "dm_block";
    public static final String dbRoom = "dm_room";
    public static final String db_dp_invoice = "dp_invoice";
    public static final String db_dm_invoice = "dm_invoice";
    public static final String dbAccInvoice = "acc_invoice";
    public static final String db_acc_invoice_id = "acc_invoice_id";
    public static final String acc_type_id = "acc_type_id";
    public static final String invoice_id = "invoice_id";
    public static final String dbStock = "dp_stock";
    public static final String dbOperation = "dp_service_type";
    public static final String dbAcc_category = "acc_category";
    public static final String dbAccType = "acc_type";
    public static final String dbRelatives = "relatives";
    public static final String dbStudentRelatives = "student_relatives";
    public static final String dbStudentAccessories = "student_accessories";
    public static final String dbStudentPayments = "student_payments";
    public static final String dbStudentCalls = "student_calls";
    public static final String dbStudentInstallment = "student_installement_plan";
    public static final String dbMartialStatus = "hr_martial_status";
    public static final String dbSalaryCategory = "hr_salary_category";
    public static final String dbMeasurement = "dp_measurement";
    public static final String dbStockMovement = "dp_stock_movements";
    public static final String dbInventoryOrganization = "dm_inventory_organization";
    public static final String dbInventoryLiquidation = "dm_inventory_liquidation";
    public static final String dbTransfers = "acc_transfers";
    public static final String dbNationality = "nationality";
    public static final String dbEducationStatus = "education_status";
    public static final String dbDiscountType = "discount_type";
    public static final String hr_positionTable = "hr_position";
    public static final String positionTable = "position";
    public static final String dbStudent = "student";
    public static final String dbStudentDiscount = "student_discount";
    public static final String dbDiscount = "discount";
    public static final String dbEmployee = "employee";
    public static final String dbEmployeePhoneNumber = "hr_employee_phone_number";
    public static final String dbEmployeeContract = "hr_employee_contract";
    public static final String dbEmployeeChildren = "hr_employee_children";
    public static final String dbEmployeeEducation = "hr_employee_education";
    public static final String dbColumnUniversityId = "hr_university_id";
    public static final String dbColumnCertificateId = "certificate_id";
    public static final String dbEmployeeLanguage = "hr_employee_language";
    public static final String dbEmployeeExams = "hr_employee_exam";
    public static final String dbEmployeeBranch = "hr_employee_branch";
    public static final String dbEmployeeBranchHours = "hr_employee_branch_hours";
    public static final String dbEmployeeOrder = "hr_employee_order";
    public static final String dbEmployeeSeminar = "hr_employee_seminar";
    public static final String dbEmployeeCertificate = "hr_employee_certificate";
    public static final String dbEmployeeWork = "hr_employee_work";
    public static final String dbColumnEmployeeWorkId = "hr_work_place_id";
    public static final String dbEmployeeQuestion = "hr_employee_question";
    public static final String dbPhoneType = "hr_phone_type";
    public static final String dbContractType = "hr_contract_type";
    public static final String dbHrEducationStatus = "hr_education_status";
    public static final String employee_id = "employee_id";
    public static final String dbAccessories = "accessories";
    public static final String dbContract = "contract";
    public static final String dbOrderReason = "order_reason";
    public static final String dbSchool = "school";
    public static final String dbYear = "year";
    public static final String dbDiscountUnit = "discount_unit";
    public static final String dbStudentOrders = "student_orders";
    public static final String rnAdmin = "admin";
    public static final String rnBank = "bank";
    public static final String rnHr = "hr";
    public static final String rnSapatSecretary = "sapat_secretary";
    public static final String cnAccessoriesDefinitionView = "AccessoriesDefinitionView";
    public static final String cnIssueOrderView = "IssueOrderView";
    public static final String cnSendOrderView = "SendOrderView";
    public static final String cnClassNameDefinitionView = "ClassNameDefinitionView";
    public static final String cnBlockDefinitionView = "BlockDefinitionView";
    public static final String cnContractDefinitionView = "ContractDefinitionView";
    public static final String cnLeavingReasonsDefinitionView = "LeavingReasonsDefinitionView";
    public static final String cnDefinitionView = "DefinitionView";
    public static final String cnHRDefinitionView = "HRDefinitionView";
    public static final String cnInventoryDefinitionView = "InventoryDefinitionView";
    public static final String cnHomePageView = "HomePageView";
    public static final String cnDiscountDefinitionView = "DiscountDefinitionView";
    public static final String cnExpensesDefinitionView = "ExpensesDefinitionView";
    public static final String cnIncomesExpensesDefinitionView = "IncomesExpensesDefinitionView";
    public static final String cnShortTermDebtsDefinitionView = "ShortTermDebtsDefinitionView";
    public static final String cnReturnableAssetsDefinitionView = "ReturnableAssetsDefinitionView";
    public static final String cnIncomesDefinitionView = "IncomesDefinitionView";
    public static final String cnStockDefinitionView = "StockDefinitionView";
    public static final String cnStockIncomeView = "StockIncomeView";
    public static final String cnStockOutcomeView = "StockOutcomeView";
    public static final String cnInventoryOrganizationView = "InventoryOrganizationView";
    public static final String cnInventoryLiquidationView = "InventoryLiquidationView";
    public static final String cnTransactionsView = "TransactionsView";
    public static final String cnMessagesView = "MessagesView";
    public static final String cnAccrualsView = "AccrualsView";
    public static final String cnShortTermDebtsView = "ShortTermDebtsView";
    public static final String cnReturnableAssetsView = "ReturnableAssetsView";
    public static final String cnBalanceAccountsView = "BalanceAccountsView";
    public static final String cnPayoutsView = "PayoutsView";
    public static final String cnSchoolDefinitionView = "SchoolDefinitionView";
    public static final String cnEmployeeDefinitionView = "EmployeeDefinitionView";
    public static final String cnEmployeeTransferView = "EmployeeTransferView";
    public static final String cnCV_Window = "CV_Window";
    public static final String cv = "CV";
    public static final String cnLessonAssessmentView = "LessonAssessmentView";
    public static final String cnImportBranchesFromExcelView = "ImportBranchesFromExcelView";
    public static final String cnSchoolModificationView = "SchoolModificationView";
    public static final String cnStudentDefinitionView = "StudentDefinitionView";
    public static final String cnReportsView = "ReportsView";
    public static final String discountsTable = "DiscountsTable";
    public static final String correctionsTable = "CorrectionsTable";
    public static final String paymentsTab = "PaymentsTab";
    public static final String callsTab = "CallsTab";
    public static final String takeAccessoriesTab = "TakeAccessoriesTab";
    public static final String giveAccessoriesTab = "GiveAccessoriesTab";
    public static final String contractTab = "ContractTab";
    public static final String familyTab = "FamilyTab";
    public static final String actPrint = "распечатка";
    public static final String cnAccountingReportsView = "AccountingReportsView";
    public static final String cnStockReportsView = "StockReportsView";
    public static final String cnInventoryReportsView = "InventoryReportsView";
    public static final String cnHRReportsView = "HRReportsView";
    public static final String actAdd = "добавление";
    public static final String contract_info = "информация о контракте";
    public static final String actReadMessages = "просмотр всех сообщений школы";
    public static final String actModify = "изменение";
    public static final String prmChangeOldTransactions = "изменение старых записей";
    public static final String prmPaymentsByDates = "оплаты по датам";
    public static final String prmChangeCurrencyRate = "изменение курса доллара";
    public static final String prmGeneralInfo = "общая информация";
    public static final String prmStudentsInfo = "информация об учениках";
    public static final String prmAccountingInfo = "информация по бухгалтерии";
    public static final String prmLogsInfo = "логи системы";
    public static final String prmAccountingLogsSelect = "логи по бухгалтерии";
    public static final String actDelete = "удаление";
    public static final String actImport = "импорт";
    public static final String actPdf = "pdf";
    public static final String prmContractInfo = "информация о контракте";
    public static final String prmFinancialHistoryInfo = "информация о финансовой истории";
    public static final String prmContractInfoLeftDebt = "информация о задолженностях";
    public static final String prmMenu = "показ в меню";
    public static final String prmSearchByDates = "поиск по датам";
    public static final String prmIncomesExpensesDefinition = "определение двусторонних категорий";
    public static final String prmContractVisible = "видимость контракта";
    public static final String prmOrganizeLessons = "организация уроков";
    public static final String prmOrganizeSupervision = "назначение кураторства";
    public static final String prmViewAllEmployees = "видимость всех сотрудников";
    public static final String actCopy = "копирование";
    public static final String prmPlanPayments = "план оплат и оплаты";
    public static final String prmClassPayments = "оплаты по классам";
    public static final String prmClassDiscounts = "скидки по классам";
    public static final String prmDiscountsReport = "отчет по скидкам";
    public static final String prmSchoolDiscounts = "скидки по школам";
    public static final String prmDebtReport = "отчет по долгам";
    public static final String prmClassInstPlan = "план оплат по классам";
    public static final String prmClassList = "список класса";
    public static final String prmDebtAndRepayment = "отчет по долгам и погашениям";
    public static final String prmStatusesReport = "отчет по статусам";
    public static final String prmYearMonthReport = "годовой и месячный отчет";
    public static final String prmMonthReport = "отчет по месяцам";
    public static final String prmAccountingBalanceReport = "балансовый отчет";
    public static final String prmAccountingBankReport = "отчет по оплатам через банк";
    public static final String prmByDateReport = "отчет по датам";
    public static final String prmSchoolAccountingReport = "school_accounting_report";
    public static final String prmGeneralReport = "общий отчет по бухгалтерии";
    public static final String prmCurrentAccountStatement = "выписка по текущему счету";
    public static final String prmIncomeExpenseAccountStatement = "выписка по двустороннему счету";
    public static final String prmIncomesExpensesReport = "отчет по двусторонним счетам";
    public static final String prmSalariesReport = "отчет по выплатам";
    public static final String prmLessonHoursReport = "отчет по количеству преподаваемых часов";
    public static final String prmHrGeneralReport = "общий отчет по кадрам";
    public static final String prmCallsReport = "отчет о вызовах";
    public static final String prmOutOfReport = "отчет о выбывших";
    public static final String prmTabPermissions = "вкладка разрешений";
    public static final String prmTabEduActivities = "вкладка образовательной деятельности";
    public static final String prmTabOrders = "вкладка приказов";
    public static final String prmTabSearch = "вкладка поиска";
    public static final String prmTabContacts = "вкладка контактной информации";
    public static final String prmTabContracts = "вкладка договоров";
    public static final String prmTabProfInfo = "вкладка профессиональной информации";
    public static final String prmTabAchievements = "вкладка достижений";
    public static final String prmTabFamilyInfo = "вкладка информации о семье";
    public static final String prmTabExtraInfo = "вкладка доп. информации";
    public static final String prmTabDocuments = "вкладка документов";
    public static final String prmChangeYear = "ChangeYear";
    public static final String prmShowAllSchools = "ShowAllSchools";
    public static final String prmProductMovementsReport = "отчет по передвижениям товара";
    public static final String prmStockGeneralReport = "общий отчет по складам";
    public static final String prmConfirmationControl = "контроль подтверждений";
    public static final String button = "  ";
    public static final String hashTags = "#";
    public static final String FreshItem = "fresh";
    public static final String cnBackupView = "BackupView";
    public static final String cnTemplatesView = "TemplatesView";
    public static final String cnCallsView = "CallsView";
    public static final String cnSettingsView = "SettingsView";
    public static final String percentage = "%";
    public static final String activeStatus = "active";
    public static final String entering_year_id = "entering_year_id";
    public static final String hr_position_category_id = "hr_position_category_id";
    public static final String dbColumnStudent_payments_id = "student_payments_id";
    public static final String download_button = "download_button";
    public static final String cancel_upload_button = "cancel_upload_button";
    public static final String KGS = "KGS";
    public static final String USD = "USD";
    public static final SimpleDateFormat dateRu = new SimpleDateFormat(
            "«dd» MMMMM yyyy г.", new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    });
    public static final SimpleDateFormat dateKg = new SimpleDateFormat(
            "dd-MMMMM yyyy-ж.", new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"январь", "февраль", "март", "апрель", "май", "июнь",
                    "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};
        }
    });

    public static NumberFormat getNumberFormat(int digits) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(true);
        format.setMaximumFractionDigits(digits);
        format.setMinimumFractionDigits(digits);
        return format;
    }

    public static StringToDoubleConverter getStringToDoubleConverter(int digits) {
        return new StringToDoubleConverter() {
            @Override
            protected java.text.NumberFormat getFormat(Locale locale) {
                return getNumberFormat(digits);
            }
        };
    }

    public static StringToIntegerConverter getStringToIntegerConverter() {
        return new StringToIntegerConverter() {
            @Override
            protected java.text.NumberFormat getFormat(Locale locale) {
                NumberFormat format = super.getFormat(Locale.ENGLISH);
                format.setGroupingUsed(true);
                format.setMaximumFractionDigits(0);
                return format;
            }
        };
    }

    public static Set<?> convertToSet(Collection<?> coll) {
        Iterator<?> iter = coll.iterator();
        HashSet<Integer> hs = new HashSet<>();
        while (iter.hasNext()) {
            Object next = iter.next();
            hs.add((Integer) next);
        }
        return hs;
    }

    public static Set<?> convertToSet(String str) {
        String[] arr = str.split(",");
        HashSet<Integer> hs = new HashSet<>();
        for (String curVal : arr) {
            Integer next = Integer.parseInt(curVal);
            hs.add(next);
        }
        return hs;
    }

    public static String convertCollectionToStr(Collection<?> set) {
        if (!set.isEmpty()) {
            return StringUtils.join(set, ",");
        } else {
            return null;
        }
    }

    public static String convertCollectionToStr(Collection<?> set, IndexedContainer container, Object property) {
        Set ids = new HashSet();
        if (!set.isEmpty()) {
            for (Object o : set) {
                ids.add(container.getContainerProperty(o, property).getValue());
            }
            return StringUtils.join(ids, ",");
        } else {
            return null;
        }
    }

    public static Set<Integer> getChild_ids(HierarchicalContainer container, Set<?> selectedIds) {
        Set<Integer> set = new HashSet<>();
        for (Object selectedId : selectedIds) {
            Integer nextId = (Integer) selectedId;
            set.add(nextId);
            if (container.hasChildren(nextId)) {
                for (Object o : container.getChildren(nextId)) {
                    Set<Integer> setChild = new HashSet<>();
                    setChild.add((Integer) o);
                    set.addAll(getChild_ids(container, setChild));
                }
            }
        }
        return set;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String generateYearPostfix(int age) {
        int lastChar = Integer.parseInt(("" + age).substring(("" + age).length() - 1));
        String str;
        if (age == 11) str = "лет";
        else if (("" + age).endsWith("1")) str = "год";
        else if (age > 11 && age < 15) str = "лет";
        else if (lastChar > 1 && lastChar < 5) str = "года";
        else str = "лет";
        return str;
    }

    public static String generateMonthPostfix(int age) {
        int lastChar = Integer.parseInt(("" + age).substring(("" + age).length() - 1));
        String str;
        if (age == 11) str = "месяцев";
        else if (("" + age).endsWith("1")) str = "месяц";
        else if (age > 11 && age < 15) str = "месяцев";
        else if (lastChar > 1 && lastChar < 5) str = "месяца";
        else str = "месяцев";
        return str;
    }

    public static String transliterate(String message) {
        char[] abcCyr = {'-', ',', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', 'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        String[] abcLat = {"-", ",", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", " ", "a", "b", "v", "g", "d", "e", "e", "zh", "z", "i", "y", "k", "l", "m", "n", "o", "p", "r", "s", "t", "u", "f", "h", "ts", "ch", "sh", "sch", "", "i", "", "e", "ju", "ja", "A", "B", "V", "G", "D", "E", "E", "Zh", "Z", "I", "Y", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "F", "H", "Ts", "Ch", "Sh", "Sch", "", "I", "", "E", "Ju", "Ja", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            for (int x = 0; x < abcCyr.length; x++) {
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                    break;
                }
            }
        }
        return builder.toString();
    }

    public static IndexedContainer copyContainer(Container source) {
        IndexedContainer cont = new IndexedContainer();

        for (Object prop : source.getContainerPropertyIds()) {
            cont.addContainerProperty(prop, source.getType(prop), null);
        }
        for (Object id : source.getItemIds()) {
            Item sourceItem = source.getItem(id);
            Item destItem = cont.addItem(id);
            for (Object prop : source.getContainerPropertyIds()) {
                Object value = sourceItem.getItemProperty(prop).getValue();
                destItem.getItemProperty(prop).setValue(value);
            }
        }
        return cont;
    }

    public static boolean validate(ComponentContainer layout) {
        boolean result = true;
        for (Component c : layout) {
            if (c instanceof AbstractField) {
                try {
                    ((AbstractField<?>) c).validate();
                } catch (Exception e) {
                    //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                    result = false;
                }
            } else if (c instanceof AbstractComponentContainer) {
                if (!validate((AbstractComponentContainer) c)) {
                    result = false;
                }
            }
        }
        return result;
    }

    public static boolean validateTable(MyVaadinUI myUI, Table t) {
        if (t.size() == 0) {
            Notification.show(myUI.getMessage(Messages.NotificationWrongValue),
                    Notification.Type.WARNING_MESSAGE);
            return false;
        } else {
            for (Object next : ((IndexedContainer) t
                    .getContainerDataSource()).getItemIds()) {
                for (Object next1 : t
                        .getContainerDataSource().getContainerPropertyIds()) {
                    Object c = t.getItem(next).getItemProperty(
                            next1).getValue();
                    if (c instanceof AbstractField) {
                        try {
                            ((AbstractField<?>) c).validate();
                        } catch (Exception e) {
                            //((AbstractComponent) c).setComponentError(new UserError(e.getMessage()));
                            return false;
                        }
                    } else if (c instanceof AbstractComponentContainer) {
                        if (!validate((AbstractComponentContainer) c)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
