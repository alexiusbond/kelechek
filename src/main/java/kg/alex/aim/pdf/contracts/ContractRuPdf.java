package kg.alex.aim.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.domain.StudentInfoPdf;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ContractRuPdf {

    static final Logger logger = LogManager.getLogger(ContractRuPdf.class);
    private final static String FONT_LOCATION = "/home/muras/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/muras/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractRuPdf(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 30, 35);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10.5f);
                Font ordItalicFont = new Font(baseFont, 10.5f, Font.ITALIC);
                Font ordBoldFont = new Font(baseFontBold, 10.5f);
                Font ordBolditalicFont = new Font(baseFontBold, 10.5f, Font.ITALIC);
                Font boldUnderlinedFont = new Font(baseFontBold, 10.5f, Font.UNDERLINE);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();
                // все абзацы, которые должны быть в двух колонках
                List<Element> contractBody = new ArrayList<>();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР ОБ ОБУЧЕНИИ ЗА СЧЕТ ", ordBoldFont));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("РОДИТЕЛЬСКОЙ ОПЛАТЫ", ordBoldFont));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("№ " + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), ordBoldFont));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                contractBody.add(spr);
                contractBody.add(new Paragraph(10, " "));

                float[] table_date_colsWidth = {1.1f, 1f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase("г. " + studentInfo.getSchool().getCity(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                contractBody.add(table_date);
                contractBody.add(new Paragraph(10, " "));


                Paragraph paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Образовательное учреждение «" + studentInfo.getSchool().getName_ru() + "», в лице директора ", ordFont));
                Decliner dcl = new Decliner();
                String directorFullName = null;
                try {
                    boolean isFeminine = studentInfo.getDirector().getGender_id() == 2;
                    directorFullName = dcl.DeclineSurnameGenitive(studentInfo.getDirector().getSurname(), isFeminine)
                            + " " + dcl.DeclineNameGenitive(studentInfo.getDirector().getName(), isFeminine, false);
                    if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                        directorFullName += " " + dcl.DeclinePatronymicGenitive(studentInfo.getDirector().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(directorFullName, ordBoldFont));
                paragraph.add(new Phrase(", действующее на основании Устава, именуемое далее «Школа», с одной стороны, и родитель (законный представитель) ", ordFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName(), ordBoldFont));
                paragraph.add(new Phrase(" именуемый в дальнейшем «Родитель» Учащегося ", ordFont));
                String studentFullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                try {
                    boolean isFeminine = studentInfo.getStudent().getGender_id() == 2;
                    studentFullName = dcl.DeclineSurnameGenitive(studentInfo.getStudent().getSurname(), isFeminine) + " "
                            + dcl.DeclineNameGenitive(studentInfo.getStudent().getName(), isFeminine, false);
                    if (!studentInfo.getStudent().getMiddle_name().isEmpty()) {
                        studentFullName = studentFullName + " "
                                + dcl.DeclinePatronymicGenitive(studentInfo.getStudent().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(studentFullName, ordBoldFont));
                paragraph.add(new Phrase(" с другой стороны, заключили настоящий договор (далее — «Договор») в интересах Учащегося в соответствии с п. 14 ст. 50 Закона Кыргызской Республики «Об образовании» о нижеследующем:", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Предметом настоящего Договора является обучение Учащегося в " + studentInfo.getYear().getName() + " учебном году в период " +
                        studentInfo.getYear().getPeriod() + " по образовательным программам соответствующего уровня образования Кыргызской Республики, " +
                        "обеспечение питанием и общежитием, а также внесение платы за предоставляемые услуги (далее – плата за обучение).", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ СТОРОН", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Школа обязуется:", boldUnderlinedFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.1. Обеспечить надлежащее выполнение услуг, предусмотренных п. 1.1 Договора.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.2. Оказывать методическую и консультационную помощь Учащегося в полном освоении образовательной программы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.3. Обеспечить Учащегося в период обучения учебниками, библиотекой, лабораторией, компьютерными классами и общежитием.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.4. Обеспечить трехразовое питание (завтрак, обед, ужин).", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.5. Сохранять место Учащегося при наличии уважительных причин пропусков.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.6. Обеспечить уважительное отношение к личности Учащегося, исключить любые формы физического и психологического насилия.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.7. Обеспечивать безопасность жизни и здоровья Учащегося в период его пребывания в Школе.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.8. Проводить переводные и итоговые аттестации, обеспечивать переход Учащегося в следующий класс.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.9. Выдать по окончании уровня образования установленный образец свидетельства или аттестата.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.10. Обеспечить медицинский контроль состояния здоровья Учащегося.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2. Школа имеет право:", boldUnderlinedFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.1. Самостоятельно выбирать программы и методики обучения.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.2. Самостоятельно формировать меню питания.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.3. Самостоятельно изменять размер платы за обучение в соответствии со сметой годовых расходов.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.4. Устанавливать и изменять размеры скидок.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.5. Исключить Учащегося без возврата платы в случаях грубых нарушений правил:", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("а) грубое или систематическое нарушение правил внутреннего распорядка;", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("б) совершение противоправных действий в отношении других учащихся или сотрудников;", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("в) употребление алкоголя, сигарет, электронных сигарет, насвая, наркотических средств;", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("г) нарушение законодательства Кыргызской Республики.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.6. Расторгнуть Договор в одностороннем порядке, если Учащийся не освоил программу в календарные сроки.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.7. Перевести Учащегося в другой класс.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.8. Расторгнуть Договор при систематическом нарушении п. 3.1. Договора", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.9. Размещать фото и видеоматериалы Учащегося без предварительного уведомления и согласия.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.10. Исключить Учащегося при отсутствии посещений 10 (десяти) дней (70 часов) без уважительной причины без возврата платы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.11. Требовать возмещения ущерба, причиненного имуществу Школы.", ordFont));
                contractBody.add(paragraph);


                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3. Родитель обязуется:", boldUnderlinedFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.1. Своевременно оплачивать плату за обучение.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.2. Обеспечить Учащегося необходимыми канцелярскими принадлежностями.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.3. Предоставить медицинские документы о состоянии здоровья Учащегося.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.4. Заполнить анкету Учащегося и предоставить достоверные сведения.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.5. Ознакомить Учащегося с правилами внутреннего распорядка.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.6. Возместить ущерб, причиненный Учащимся имуществу Школы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.7. Сопровождать Учащегося до и из Школы или предоставить письменное разрешение на его самостоятельное передвижение.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.8. Участвовать в мероприятиях, проводимых Школой.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4. Родитель имеет право:", boldUnderlinedFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.1. Требовать от администрации исполнения обязательств Школы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.2. Расторгнуть Договор при условии компенсации фактических затрат, если:", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("а) состояние здоровья Учащегося не позволяет продолжать обучение (при наличии официального медицинского заключения);", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("б) Школа нарушает условия Договора.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.3. Участвовать в учебном процессе и получать информацию об Учащемся.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.4. Участвовать в публичных мероприятиях Школы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.5. Сотрудничать со Школой для развития способностей Учащегося.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.6. Входить и избираться в органы Школы, созданные в интересах Учащихся.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. ПЛАТА ЗА ОБУЧЕНИЕ И ПОРЯДОК\nРАСЧЕТОВ", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. Плата за обучение производится частями согласно Графику платежей (Приложение). Первый (первоначальный) платеж производится при заключении Договора и составляет не менее 1000 (одной тысячи) долларов США. Последний платеж должен быть внесен не позднее 31 марта следующего года.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2. Оплата производится в сомах по курсу Национального банка КР на день платежа.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3. Форма Школы выдается один раз при поступлении. Дополнительная форма оплачивается отдельно.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.4. Плата за обучение не включает расходы на официальные экзамены, экскурсии, пикники и иные мероприятия.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.5. Скидки предоставляются по решению педсовета и с учетом партнерских соглашений.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.6. При досрочном отчислении по уважительной причине возвращается 50% оставшейся суммы после удержания фактических расходов.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. ФОРС-МАЖОР", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Ни одна из сторон не несет ответственности за неисполнение обязательств, вызванных форс-мажорными обстоятельствами: пожар, наводнение, землетрясение, война, военное положение, забастовки, массовые беспорядки и иные обстоятельства непреодолимой силы.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. ПРОЧИЕ УСЛОВИЯ", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Договор вступает в силу с момента подписания сторонами и действует до конца учебного года.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.2. Изменения и дополнения действительны только в письменной форме и при подписании обеими сторонами.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.3. При нарушении графика платежей 3 раза подряд Школа вправе расторгнуть Договор без уведомления.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.4. При расторжении после 30 июня удерживается 5% от суммы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.4 (повтор). В следующий класс переводятся учащиеся без академической задолженности и с годовым баллом не ниже 40 из 100. При неудовлетворительной сдаче осеннего пересдачи Учащийся не переводится и повторяет год без компенсации ранее внесенной платы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.5. Споры решаются путем переговоров, при недостижении согласия — в судебном порядке.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.6. Для заключения Договора предоставляются:", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("– документ, удостоверяющий личность родителя/законного представителя;", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("– свидетельство о рождении ребенка;", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("– иные документы по запросу администрации Школы.", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.7. Договор составлен в двух экземплярах на государственном языке, оба экземпляра имеют одинаковую юридическую силу.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(20, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Подтверждаю, что ознакомлен(а) с условиями договора и согласен(на) с ними. ", ordBolditalicFont));
                paragraph.add(new Phrase("____________________________________________ ", ordFont));
                paragraph.add(new Phrase("\n(подпись Родителя)", ordFont));
                contractBody.add(paragraph);
                addTwoColumnText(document, writer, contractBody);
                contractBody.clear();

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. РЕКВИЗИТЫ СТОРОН", ordBoldFont));
                contractBody.add(paragraph);
                directorFullName = studentInfo.getDirector().getSurname()
                        + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                    directorFullName += " " + studentInfo.getDirector().getMiddle_name();
                }
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("\nОбразовательное учреждение «" + studentInfo.getSchool().getName_ru() + "»\n" +
                        "Адрес: " + studentInfo.getSchool().getAddress() + ".\n" +
                        "ИНН: " + studentInfo.getSchool().getInn() + "\n" +
                        "ОКПО: " + studentInfo.getSchool().getOkpo() + "\n" +
                        "Банк: " + studentInfo.getSchool().getBank() + "\n" +
                        "Р/с (мультивалютный): " + studentInfo.getSchool().getBank_account() + "\n" +
                        "БИК: " + studentInfo.getSchool().getBik() + "\n" +
                        "Тел. " + studentInfo.getSchool().getPhone() + "\n" +
                        "\n", ordFont));
                paragraph.add(new Phrase(
                        "Директор:\n", ordBoldFont));
                paragraph.add(new Phrase(directorFullName + "\n_______________________\n", ordFont));
                paragraph.add(new Phrase("(М.П.)", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("\n\nДАННЫЕ РОДИТЕЛЯ: ", ordFont));
                paragraph.add(new Phrase("\nФИО: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName() + "\n", ordFont));
                paragraph.add(new Phrase("Адрес проживания: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getAddress() + "\n", ordFont));
                paragraph.add(new Phrase("Паспорт / ИНН: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getPassport() + "   " +
                        studentInfo.getMainRelative().getInn() + "\n", ordFont));
                paragraph.add(new Phrase("Телефон: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getPhone() + "\n\n", ordFont));
                paragraph.add(new Phrase("Подпись:   __________________", ordBoldFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(20, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("ГРАФИК ПЛАТЕЖЕЙ:", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Общая сумма: ", ordBolditalicFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) + " долларов США\n", ordFont));
                paragraph.add(new Phrase("Скидка: ", ordBolditalicFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    paragraph.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordFont));
                }
                paragraph.add(new Phrase("\nКорректировка: ", ordBolditalicFont));
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    paragraph.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordFont));
                }
                paragraph.add(new Phrase("\n* При нарушении графика скидка аннулируется.", ordItalicFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                float[] TContract_colsWidth = {2f, 1f};
                PdfPTable TContract = new PdfPTable(2);
                TContract.setWidthPercentage(90f);
                TContract.setWidths(TContract_colsWidth);
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                TContract.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                TContract.getDefaultCell().setPaddingTop(5f);
                TContract.getDefaultCell().setPaddingBottom(5f);
                TContract.getDefaultCell().setPaddingRight(5f);
                TContract.addCell(new Phrase("Первоначальный взнос\n(платёж при заключении договора)", ordFont));
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + " " + Settings.USD, ordFont));
                for (Object obj : instPlanCont.getItemIds()) {
                    if ((Integer) instPlanCont.getContainerProperty(obj, Settings.status_id).getValue() == 1) {
                        TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                        TContract.addCell(new Phrase(((DateField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Date)).getValue()).getValue().toString(), ordFont));
                        TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        TContract.addCell(new Phrase(Settings.dFormat2.format(((TextField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue()) + " " + Settings.USD, ordFont));
                    }
                }
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                TContract.addCell(new Phrase("Общая сумма:", ordBoldFont));
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + " " + Settings.USD, ordBoldFont));
                contractBody.add(TContract);

                addToSecondColumn(document, writer, contractBody);
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

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            try {
                Phrase ft = new Phrase(String.format(" %d",
                        writer.getPageNumber()), f_font);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, ft,
                        (document.right() - 30),
                        document.bottom() - 15, 0);

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }

    }

    private static void addTwoColumnText(Document document,
                                         PdfWriter writer,
                                         List<Element> elements) throws DocumentException {

        ColumnText ct = new ColumnText(writer.getDirectContent());

        float gutter = 5f; // расстояние между колонками

        float left = document.left();
        float right = document.right();
        float top = document.top(); // немного ниже верхней границы
        float bottom = document.bottom();

        float fullWidth = right - left;
        float columnWidth = (fullWidth - gutter) / 2f;

        float[][] columns = new float[][]{
                {left, bottom, left + columnWidth, top},
                {left + columnWidth + gutter, bottom, right, top}
        };

        for (Element e : elements) {
            ct.addElement(e);
        }

        int column = 0;

        while (true) {
            ct.setSimpleColumn(
                    columns[column][0],
                    columns[column][1],
                    columns[column][2],
                    columns[column][3]
            );

            int status = ct.go();

            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                break; // текста больше нет
            }

            column++;
            if (column >= columns.length) {
                column = 0;
                document.newPage();
            }
        }
    }

    private static void addToSecondColumn(Document document,
                                          PdfWriter writer,
                                          List<Element> elements) throws DocumentException {

        PdfContentByte cb = writer.getDirectContent();

        float gutter = 5f; // расстояние между колонками

        float left = document.left();
        float right = document.right();
        float top = document.top(); // чуть ниже верхней границы
        float bottom = document.bottom();

        float fullWidth = right - left;
        float columnWidth = (fullWidth - gutter) / 2f;

        // левая/правая границы второй колонки
        float colLeft = left + columnWidth + gutter;
        float colRight = right;

        ColumnText ct = new ColumnText(cb);

        // изначальный прямоугольник второй колонки на текущей странице
        ct.setSimpleColumn(colLeft, bottom, colRight, top);

        for (Element e : elements) {
            ct.addElement(e);
        }

        while (true) {
            int status = ct.go();

            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                break; // всё отрисовано
            }

            // текст не влез — новая страница и снова рисуем во второй колонке
            document.newPage();
            ct.setSimpleColumn(colLeft, bottom, colRight, top);
        }
    }


}
