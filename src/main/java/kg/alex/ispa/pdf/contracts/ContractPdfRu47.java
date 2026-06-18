package kg.alex.ispa.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.dao.DbStudentRelative;
import kg.alex.ispa.domain.StudentInfoPdf;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.utils.Decliner;
import kg.alex.ispa.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Iterator;

public class ContractPdfRu47 {

    static final Logger logger = LogManager.getLogger(ContractPdfRu47.class);
    private final static String FONT_LOCATION = "/home/ispa/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/ispa/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractPdfRu47(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 25, 45);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10);
                Font ordBoldFont = new Font(baseFontBold, 10);
                Font boldFont = new Font(baseFontBold, 11);
                Font font_header = new Font(baseFontBold, 11);

                document.open();
                PdfContentByte punder = writer.getDirectContentUnder();
                Decliner dcl = new Decliner();

                document.add(new Paragraph(10, " "));
                Paragraph spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);

                spr.add(new Phrase("об оказании платных образовательных услуг", font_header));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                float[] table_date_colsWidth = {4.5f, 1.5f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase("г. " + studentInfo.getSchool().getCity().toUpperCase(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(new Date()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(studentInfo.getSchool().getName_ru().replace(
                        "ОсОО", "Общество с ограниченной ответственностью "), ordBoldFont));
                paragraph.add(new Phrase(", именуемое в дальнейшем “Школа”, в лице директора ", ordFont));
                String fullName = null;
                try {
                    boolean isFeminine = studentInfo.getDirector().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getDirector().getSurname(), isFeminine)
                            + " " + dcl.DeclineNameGenitive(studentInfo.getDirector().getName(), isFeminine, false);
                    if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                        fullName += " " + dcl.DeclinePatronymicGenitive(studentInfo.getDirector().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName + ", ", ordBoldFont));
                paragraph.add(new Phrase("действующего на основании Устава и Закона КР  “Об образовании”  с одной стороны и родитель (законный представитель) ", ordFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName() + ", ", ordBoldFont));
                paragraph.add(new Phrase(" именуемый(ая) в дальнейшем Родитель, действующий в интересах учащегося ", ordFont));
                paragraph.add(new Phrase(studentInfo.getStudent().getClass_name() + " класса ", ordFont));

                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                try {
                    boolean isFeminine = studentInfo.getStudent().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getStudent().getSurname(), isFeminine) + " "
                            + dcl.DeclineNameGenitive(studentInfo.getStudent().getName(), isFeminine, false);
                    if (!studentInfo.getStudent().getMiddle_name().isEmpty()) {
                        fullName = fullName + " "
                                + dcl.DeclinePatronymicGenitive(studentInfo.getStudent().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" с другой стороны заключили настоящий договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("1. Предмет договора", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. ", ordBoldFont));
                paragraph.add(new Phrase("Предметом договора является организация процесса обучения, воспитания учащегося, получение им образования в рамках государственного образовательного стандарта и программ общеобразовательной средней школы и программ Кембридж (с 1 по 12 класс) на период ", ordFont));
                paragraph.add(new Phrase(studentInfo.getYear().getPeriod(), ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий договор определяет и регулирует отношения между школой и родителями в период обучения учащегося в школе. Целью договора является установление ответственности сторон, юридическое закрепление сторон обучения учащегося на ступенях начального общего и основного общего образования, обеспечение взаимодействия между сторонами.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("2. Права и обязанности сторон", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1 Школа обязуется:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. ", ordBoldFont));
                paragraph.add(new Phrase("Организовать и обеспечить надлежащее исполнение услуг, предусмотренных в статье 1.1. настоящего договора. Образовательные услуги оказываются в соответствии с государственным образовательным стандартом и программой соответствующего уровня образования, утверждённого Министерством просвещения Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. ", ordBoldFont));
                paragraph.add(new Phrase("В целях усвоения Учащимся образовательных программ обеспечить учащегося методической и консультационной помощью, оказываемой в порядке, установленном школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. ", ordBoldFont));
                paragraph.add(new Phrase("При первоначальном зачислении в школу родитель получает школьную форму для обучающегося бесплатно. В дальнейшем обновление школьной формы осуществляется за счёт родителя по мере необходимости.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.4. ", ordBoldFont));
                paragraph.add(new Phrase("Сохранить место за учащимся в случае пропуска занятий по уважительной причине.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.5. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить учащегося трёхразовым питанием (завтрак, обед, полдник).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.6. ", ordBoldFont));
                paragraph.add(new Phrase("Вести медицинское наблюдение за учащимися на консультативно-рекомендательном уровне (оказание первой медицинской помощи).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.7. ", ordBoldFont));
                paragraph.add(new Phrase("Осуществлять в установленном школой порядке полугодовую и годовую аттестацию Учащегося, соответствующую базисному учебному плану Министерства образования и науки Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.8. ", ordBoldFont));
                paragraph.add(new Phrase("Переводить учащегося в следующий класс в установленном порядке по решению педагогического совета школы на основании результатов переводной и итоговой аттестации (за курс основной школы).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.9. ", ordBoldFont));
                paragraph.add(new Phrase("При успешном окончании учащимся 2-й ступени образования выдать свидетельство, 3-й ступени – аттестат установленного образца.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.10. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить безопасность учащегося во взаимодействии со структурными подразделениями школы, осуществляющими организацию внутриобъектного и пропускного режимов.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.11. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить необходимыми учебными материалами учащегося (учебники будут выдаваться из школьной библиотеки).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.12. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить контроль за учащимся в период его пребывания в школе при условии соблюдения им порядка «Режим дня» и «Правил внутреннего распорядка».", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.13. ", ordBoldFont));
                paragraph.add(new Phrase("Предоставлять информацию родителю (законному представителю) об успеваемости и поведении учащегося по итогам каждой учебной четверти.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.14. ", ordBoldFont));
                paragraph.add(new Phrase("Проявлять уважение к личности учащегося, не допускать физического и психологического насилия. Обеспечить условия для укрепления и развития нравственного, физического и психологического здоровья, эмоционального благополучия учащегося с учётом его индивидуальных особенностей.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.15. ", ordBoldFont));
                paragraph.add(new Phrase("Предоставлять учащимся кружки и дополнительные занятия, проводимые школой, на безвозмездной основе в качестве бонуса; при этом перечень и условия их проведения определяются школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2 Школа имеет право:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. ", ordBoldFont));
                paragraph.add(new Phrase("Самостоятельно составлять меню блюд и производить их замену.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. ", ordBoldFont));
                paragraph.add(new Phrase("Отчислить учащегося без возмещения стоимости обучения в следующих случаях:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("а) ", ordBoldFont));
                paragraph.add(new Phrase("совершения учащимся грубого, систематического нарушения «Правил внутреннего распорядка» с предоставлением документов (фактов), подтверждающих нарушение;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) ", ordBoldFont));
                paragraph.add(new Phrase("совершения противоправных действий по отношению к другим учащимся и персоналу школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. ", ordBoldFont));
                paragraph.add(new Phrase("Не выдавать учащемуся документ государственного образца о соответствующем образовании в случае невыполнения им «Положения о проведении государственной итоговой аттестации выпускников и порядке перевода учащихся в последующий класс в государственных и негосударственных общеобразовательных учреждениях Кыргызской Республики», а также при имеющейся финансовой задолженности за обучение.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4. ", ordBoldFont));
                paragraph.add(new Phrase("Не продлевать договор на обучение с родителями учащихся, систематически нарушающих положения настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.5. ", ordBoldFont));
                paragraph.add(new Phrase("При заключении договора и оформлении учащегося в школу родитель или законный представитель оплачивает за услугу бронирования места, регистрацию и тестирование в размере 7 000 (семь тысяч) сомов (безвозвратно).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.6. ", ordBoldFont));
                paragraph.add(new Phrase("Школа не будет ежегодно изменять процентные ставки льготникам, однако льгота будет аннулирована в случае наличия у обучающегося дисциплинарного взыскания. Также в случае нарушения графика оплаты родителем 2 раза ранее предоставленные скидки каждый раз снижаются на 5%. Оплата за обучение для учащихся, продлевающих договор, будет повышаться один раз в два года с учётом инфляции.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. ", ordBoldFont));
                paragraph.add(new Phrase("Расторгнуть договор в одностороннем порядке в случае отсутствия оплаты за обучение в течение трёх полных календарных месяцев при условии предварительного письменного уведомления родителя (законного представителя) не менее чем за 10 (десять) календарных дней до даты расторжения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.8. ", ordBoldFont));
                paragraph.add(new Phrase("Не выдавать документы учащегося при переводе в другую школу при имеющейся финансовой задолженности за обучение.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.9. ", ordBoldFont));
                paragraph.add(new Phrase("Требовать оплату за обучение ежемесячно до 5-го (пятого) числа каждого месяца. В случае просрочки начисляется пеня в размере 0,5% за каждый день задержки.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.10. ", ordBoldFont));
                paragraph.add(new Phrase("В целях компетентного подхода к трудовому воспитанию учащегося администрация школы имеет право привлекать учащихся к уборке своего рабочего места в учебных классах.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.11. ", ordBoldFont));
                paragraph.add(new Phrase("Организовывать проведение подготовительных курсов, подготовку к TOEFL, IELTS и международным экзаменам, а также работу кружков по интересам учащихся.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.12. ", ordBoldFont));
                paragraph.add(new Phrase("Подготовка к ОРТ и SAT проводится бесплатно, однако за комплект пособий для подготовки к ОРТ взимается отдельная плата.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.13. ", ordBoldFont));
                paragraph.add(new Phrase("Запретить использование сотовых телефонов, планшетов и ноутбуков на территории школы. Администрация школы не несёт ответственности за их хранение и утерю.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.14. ", ordBoldFont));
                paragraph.add(new Phrase("Школа не предоставляет особых условий обучения для учащихся, состоящих на диспансерном учёте по хроническим заболеваниям (эпилепсия, астма, порок сердца, энурез и другие заболевания).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.15. ", ordBoldFont));
                paragraph.add(new Phrase("При несвоевременной оплате родителем взноса школа вправе, с извещением родителя, ограничить доступ обучающегося ко всем формам учебных и неучебных занятий, а также к использованию материально-технических условий школы (занятий, библиотеки, столовой, кружков, этюдов и т.д.), не допускать к экзаменам и не выставлять оценки в электронном журнале.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.16. ", ordBoldFont));
                paragraph.add(new Phrase("Удержать/требовать сумму родительского взноса за один месяц со дня расторжения настоящего договора по инициативе родителя, при этом ранее предусмотренные скидки не учитываются, начиная с сентября.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.17. ", ordBoldFont));
                paragraph.add(new Phrase("При расторжении настоящего договора по непредвиденным обстоятельствам, с учётом всех понесённых расходов школы, сумма, подлежащая возврату, возвращается по мере возможности школы, но не позднее мая текущего года.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.18. ", ordBoldFont));
                paragraph.add(new Phrase("В целях освещения образовательного процесса и деятельности школы школа вправе без уведомления учащегося и родителей размещать фото- и видеоматериалы на своих интернет-страницах и в СМИ.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3. Родители (законные представители) обязуются:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.1. ", ordBoldFont));
                paragraph.add(new Phrase("Родитель обязуется своевременно и в полном объеме оплачивать образовательные услуги и иные предусмотренные настоящим договором платежи.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.2. ", ordBoldFont));
                paragraph.add(new Phrase("Для сохранения за учащимся места на следующий учебный год Родитель обязуется ежегодно, в период с 1 апреля по 15 мая, заключить договор на следующий учебный год и внести невозвратный платеж в размере 7 000 (семь тысяч) сомов в качестве бронирования места либо произвести предоплату в размере одного месяца обучения. В случае невыполнения указанных условий образовательная Школа вправе предоставить учебное место другому учащемуся.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.3. ", ordBoldFont));
                paragraph.add(new Phrase("Перед прибытием учащегося в школу провести его полную медицинскую диспансеризацию и предоставить администрации школы медицинское заключение о состоянии здоровья учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.4. ", ordBoldFont));
                paragraph.add(new Phrase("В случае болезни и отсутствия учащегося на занятиях в течение длительного времени вносить оплату контракта в полном объёме, учитывая оплату праздничных и каникулярных дней.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.5. ", ordBoldFont));
                paragraph.add(new Phrase("Содействовать выполнению учащимся порядка «Режим дня» и «Правил внутреннего распорядка» школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.6. ", ordBoldFont));
                paragraph.add(new Phrase("Нести полную материальную ответственность за все действия учащегося, повлёкшие за собой порчу или уничтожение имущества школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.7. ", ordBoldFont));
                paragraph.add(new Phrase("Возместить школе стоимость нанесённого ущерба в течение 7 (семи) календарных дней со дня получения официального счёта от администрации школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.8. ", ordBoldFont));
                paragraph.add(new Phrase("Постоянно взаимодействовать со школой по всем направлениям воспитания и обучения учащегося. При изменении номера телефона, адреса проживания и иных документов в течение 7 (семи) календарных дней известить администрацию школы ", ordFont));
                paragraph.add(new Phrase("(бухгалтерию, классного руководителя).", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.9. ", ordBoldFont));
                paragraph.add(new Phrase("Своевременно и лично приводить и забирать учащегося из школы в случае отказа от трансфера. В случае если учащегося будут приводить и забирать из школы другие совершеннолетние родственники, родитель обязан заранее предупредить администрацию школы и написать соответствующее заявление.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.10. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить обучающегося всеми необходимыми канцелярскими принадлежностями для собственного использования (тетради, альбомы, ручки, цветные карандаши, точилки и т.д.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.11. ", ordBoldFont));
                paragraph.add(new Phrase("Родители несут ответственность за сохранность учебников, выданных школой. В случае порчи или утери учебников родители возмещают их стоимость в полном объёме.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.12. ", ordBoldFont));
                paragraph.add(new Phrase("Вне территории школы ответственность за жизнь и безопасность учащегося школа не несёт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4. Родители (законные представители) имеют право:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.1. ", ordBoldFont));
                paragraph.add(new Phrase("Требовать от администрации школы выполнения условий, изложенных в п. 2.1.1–2.1.12 настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.2. ", ordBoldFont));
                paragraph.add(new Phrase("Досрочно расторгнуть договор с возмещением стоимости обучения за период пребывания учащегося в школе в соответствии с п. 2.2.8 и 2.2.9 настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.3. ", ordBoldFont));
                paragraph.add(new Phrase("Требовать защиты законных прав и интересов детей.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.4. ", ordBoldFont));
                paragraph.add(new Phrase("Избираться в состав школьного или классного родительского комитета.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Условия платы", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. ", ordBoldFont));
                paragraph.add(new Phrase("Администрацией школы установлены следующие сроки и правила оплаты за обучение учащегося за счёт родительских взносов.", ordFont
                ));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.1 ", ordBoldFont));
                paragraph.add(new Phrase("В случае отсутствия оплаты за обучение родителями (законными представителями), учащийся не допускается к переводным и государственным экзаменам, а также не переводится в следующий класс.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.2. ", ordBoldFont));
                paragraph.add(new Phrase("Стоимость оплаты за обучение составляет ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract())
                        + " " + studentInfo.getContractInfo().getCurrency() + " (кыргызских сом) за период, указанный в пункте 1.1. Настоящего Договора.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- при полной оплате годовой суммы до 30 июня предусмотрена скидка в размере 5%. Расчёт в инвалюте производится по курсу Национального Банка Кыргызской Республики на дату оплаты.", ordFont));
                document.add(paragraph);
                paragraph.clear();
                paragraph.add(new Phrase("3.1.3. ", ordBoldFont));
                paragraph.add(new Phrase("При оформлении учащегося школа обеспечивает учащегося 1 (одним) комплектом школьной формы. В последующие годы обновление школьной формы осуществляется за счёт родителя по мере необходимости.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.4. ", ordBoldFont));
                paragraph.add(new Phrase("Ежегодно в период с 1 апреля по 15 мая родители обязаны заключить договор на образовательные услуги на следующий учебный год с обязательным внесением 7 000 (семь тысяч) сомов (безвозвратно) за бронь места или предоплаты родительского взноса за один месяц. После заключения договора и внесения оплаты, в случае отказа от учёбы, внесённая сумма ", ordFont));
                paragraph.add(new Phrase("не возвращается.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("В случае не заключения договора в указанные сроки место обучающегося будет предоставлено другим желающим.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.5. ", ordBoldFont));
                paragraph.add(new Phrase("В случае отказа от обучения в школе или перевода ученика в другую образовательную организацию родители (законные представители) обязуются уведомить школу в письменной форме не позднее чем за 1 (один) месяц до предполагаемой даты прекращения обучения. В случае прекращения обучения без соблюдения указанного срока уведомления школа вправе удержать/требовать сумму оплаты за 1 (один) месяц обучения в качестве компенсации. При этом родители (законные представители) обязуются оплатить сумму договора за 1 (один) месяц обучения в случае отсутствия своевременного уведомления.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.6. ", ordBoldFont));
                if (st_info.getContractInfo().getDiscount() != null && st_info.getContractInfo().getDiscount() != 0.0) {
                    paragraph.add(new Phrase("Скидка на обучение в размере " + Settings.dFormat2.format(st_info.getContractInfo().getDiscount())
                            + " " + studentInfo.getContractInfo().getCurrency()
                            + " сохраняется до конца обучения при отличной академической успеваемости, отсутствии нарушений со стороны учащегося и пропусков без предупреждения.", ordFont));
                } else {
                    paragraph.add(new Phrase("Скидка на обучение сохраняется до конца обучения при отличной академической успеваемости, отсутствии нарушений со стороны учащегося и пропусков без предупреждения.", ordFont));
                }
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.7. ", ordBoldFont));
                paragraph.add(new Phrase("Своевременно вносить оплату за образовательные услуги обучающегося в школе согласно настоящему договору и индивидуальному графику оплаты, согласованному между школой и родителями.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.8. ", ordBoldFont));
                paragraph.add(new Phrase("Родительская плата производится согласно графику, подписанному обеими сторонами и являющемуся неотъемлемой частью настоящего договора. При этом последний взнос должен быть внесён не позднее 15 мая следующего года. Размер родительского взноса не изменяется даже при условии перехода школы на дистанционную форму обучения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.9. ", ordBoldFont));
                paragraph.add(new Phrase("Родительская плата производится в сомах на банковский счёт школы не позднее 3 (трёх) календарных дней с даты, указанной в официальном счёте/invoice.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Форс-мажорные обстоятельства", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. ", ordBoldFont));
                paragraph.add(new Phrase("Ни одна из сторон не несёт ответственности за полное или частичное неисполнение своих    обязательств при возникновении обстоятельств, которые делают невозможным выполнение договора сторонами, а именно: пожар, стихийное природное бедствие (землетрясение, наводнение и др.), война, военные действия всех видов, забастовка, блокада, изменение текущего законодательства Кыргызской Республики и другие обстоятельства непреодолимой силы, независящие от воли сторон.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Срок действия Договора, порядок изменения, дополнения и расторжения", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий договор вступает в силу с момента его подписания обеими сторонами", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий договор может быть расторгнут досрочно согласно п. 2.2.2 и 2.4.2 настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. ", ordBoldFont));
                paragraph.add(new Phrase("Любые дополнения и изменения к настоящему договору действительны лишь при условии, что они совершены в письменной форме и подписаны обеими сторонами.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. ", ordBoldFont));
                paragraph.add(new Phrase("Все разногласия по настоящему договору решаются сторонами в порядке переговоров. В случае невозможности разрешения спора путём переговоров спор подлежит разрешению в порядке, установленном законодательством Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.5. ", ordBoldFont));
                paragraph.add(new Phrase("Стороны договорились, что деловая корреспонденция и иные документы, касающиеся настоящего договора, отправленные и полученные посредством факсимильной, электронной связи или иным способом, позволяющим достоверно установить, что документ исходит от стороны по договору, признаются имеющими юридическую силу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.6. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий договор составлен в двух экземплярах на русском языке и подписан обеими сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из сторон находится один экземпляр настоящего договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Реквизиты сторон", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.5f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.getDefaultCell().setPaddingRight(10f);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase(studentInfo.getSchool().getName_ru(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + (studentInfo.getSchool().getInn() == null ? "" :
                        studentInfo.getSchool().getInn()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Банк: " + (studentInfo.getSchool().getBank() == null ? "" :
                        studentInfo.getSchool().getBank()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("БИК: " + (studentInfo.getSchool().getBik() == null ? "" :
                        studentInfo.getSchool().getBik()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Р/С: " + (studentInfo.getSchool().getBank_account() == null ? "" :
                        studentInfo.getSchool().getBank_account()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ОКПО: " + (studentInfo.getSchool().getOkpo() == null ? "" :
                        studentInfo.getSchool().getOkpo()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Юр. Адрес: " + (studentInfo.getSchool().getAddress() == null ? "" :
                        studentInfo.getSchool().getAddress()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Тел.: " + (studentInfo.getSchool().getPhone() == null ? "" :
                        studentInfo.getSchool().getPhone()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор Школы: " + studentInfo.getDirector().getSurname() + " "
                        + studentInfo.getDirector().getName() + " " +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);

                IndexedContainer relativeCont = null;
                table_info.addCell(text10);
                try {
                    DbStudentRelative dbr = new DbStudentRelative();
                    dbr.connect();
                    relativeCont = dbr.execSQL(myUI, studentInfo.getStudent().getId());
                    dbr.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                Paragraph text11 = new Paragraph();
                Paragraph text18 = new Paragraph();
                Iterator<?> iter = null;
                if (relativeCont != null) {
                    iter = relativeCont.getItemIds().iterator();
                }
                String f_name = "";
                String f_work_place = "";
                String m_name = "";
                String m_work_place = "";
                String passport = "";
                while (iter != null && iter.hasNext()) {
                    Object obj = iter.next();
                    if ((Integer) obj == 1) {
                        f_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.FullName)).getValue().toString();
                        if (relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.WorkPlace)).getValue() != null) {
                            f_work_place = relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(Messages.WorkPlace)).getValue().toString();
                        }
                    }
                    if ((Integer) obj == 2) {
                        m_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.FullName)).getValue().toString();
                        if (relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.WorkPlace)).getValue() != null) {
                            m_work_place = relativeCont.getContainerProperty(obj,
                                    myUI.getMessage(Messages.WorkPlace)).getValue().toString();
                        }
                    }
                    if ((Integer) relativeCont.getContainerProperty(obj,
                            Settings.is_main).getValue() == 1) {
                        text18.add(new Phrase("\nКонтактный тел: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Phone)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("\nАдрес места жительства: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Address)).getValue().toString(), ordFont));
                        passport = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Passport)).getValue().toString();
                    }
                }
                text11.add(new Phrase("Ф.И.О. отца: " + f_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("\nФ.И.О. матери: " + m_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("\nДанные паспорта: ", ordFont));
                text11.add(new Phrase(passport, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("\nМесто работы отца: " + f_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("\nМесто работы матери: " + m_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(text18);
                table_info.addCell(text11);
                table_info.addCell(new Phrase("\n (М.П)", ordFont));
                table_info.addCell(new Phrase("\nПодпись: ________________", ordFont));

                document.add(table_info);
                document.add(new Paragraph(10, " "));

                document.newPage();
                Paragraph text15 = new Paragraph();
                text15.setIndentationLeft(25);
                text15.setIndentationRight(25);
                text15.add(new Phrase("График оплаты за обучение", boldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Оплата стоимости обучения осуществляется одним из следующих способов по выбору Родителя:", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("1. Ежемесячная оплата", ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Стоимость обучения оплачивается равными платежами в течение 9 (девяти) учебных месяцев — с сентября по май включительно. Оплата производится ежемесячно не позднее 5 числа текущего месяца.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("2. Ежеквартальная оплата", ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Стоимость обучения оплачивается частями один раз в квартал в соответствии с графиком, утвержденным Школой.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("3. Единовременная оплата", ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Родитель вправе произвести полную оплату стоимости обучения за весь учебный год единовременно до начала учебного года либо в иной срок, установленный настоящим договором.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("4. Порядок выбора формы оплаты", ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Выбранная форма оплаты указывается в настоящем договоре и может быть изменена по соглашению сторон путем подачи письменного заявления и заключения дополнительного соглашения.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("ID ученика: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getLogin(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Ф.И.О. Ученика: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                        + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Кл.: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getClass_name(), ordBoldFont));
                text15.add(new Phrase(". Дата регистрации: ", ordFont));
                text15.add(new Phrase(Settings.df.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Сумма контракта: ", ordFont));
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract())), ordBoldFont));
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Долг с предыдущего года: ", ordFont));
                } else {
                    text15.add(new Phrase("Переплата с предыдущего года: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt())), ordBoldFont));
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));

                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Скидка: ", ordFont));
                    text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
                    text15.add(new Phrase(" (вид скидки прописью, %)", ordFont));
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Сумма после скидки: ", ordFont));
                    text15.add(new Phrase(Settings.dFormat2.format(
                            studentInfo.getContractInfo().getContractWithDiscount()), ordBoldFont));
                    text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
                }
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    text15.add(Chunk.NEWLINE);
                    text15.add(new Phrase("Корректировка: ", ordFont));
                    text15.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Предоплата: ", ordFont));
                text15.add(new Phrase(studentInfo.getContractInfo().getInitialPayment() == null ? "0.00" :
                        Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()), ordBoldFont));
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Остаток: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()), ordBoldFont));
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
                document.add(text15);
                document.add(new Paragraph(10, " "));

                Paragraph text16 = new Paragraph();
                text16.setIndentationLeft(25);
                text16.setIndentationRight(25);
                text16.add(new Phrase("Таблица 1.", ordBoldFont));
                document.add(text16);
                document.add(new Paragraph(10, " "));
                text16.add(Chunk.NEWLINE);

                float[] TContract_colsWidth = {1f, 4f, 4f, 4f, 4f};
                PdfPTable TContract = new PdfPTable(5);
                TContract.setWidthPercentage(90f);
                TContract.setWidths(TContract_colsWidth);
                TContract.addCell(new Phrase("№", ordBoldFont));
                TContract.addCell(new Phrase("Дата оплаты", ordBoldFont));
                TContract.addCell(new Phrase("Сумма", ordBoldFont));
                TContract.addCell(new Phrase("Подтверждающий документ", ordBoldFont));
                TContract.addCell(new Phrase("Подпись ", ordBoldFont));
                int n = 1;
                for (Object obj : instPlanCont.getItemIds()) {
                    TContract.addCell(new Phrase(n + "", ordFont));
                    TContract.addCell(new Phrase(Settings.df.format(((DateField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Date)).getValue()).getValue()), ordFont));
                    TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Amount)).getValue()).getValue(), ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    n += 1;
                }
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("Итого:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()), ordBoldFont));
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("", ordFont));

                document.add(TContract);
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("\nРодитель: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName(), ordFont));
                paragraph.add(new Phrase(" ___________________________", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(new Phrase("\n\nДиректор Школы: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getDirector().getSurname() + " "
                        + studentInfo.getDirector().getName() + " " +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                paragraph.add(new Phrase(" ___________________________", ordFont));
                document.add(paragraph);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            } finally {
                if (document != null) {
                    document.close();
                }
            }

            b = buffer.toByteArray();
            return new ByteArrayInputStream(b);

        };

        String nameOf = "Contract";
        StreamResource resource = new StreamResource(source1, nameOf
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, nameOf, false);
    }

    private static class myPageEvent extends PdfPageEventHelper {

        Font f_font = new Font(Font.FontFamily.UNDEFINED, 10, Font.NORMAL);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();

                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_RIGHT,
                        new Phrase(String.valueOf(writer.getPageNumber()), f_font),
                        560f,
                        22f,
                        0
                );

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}