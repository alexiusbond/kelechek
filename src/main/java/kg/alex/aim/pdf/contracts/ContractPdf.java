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
import kg.alex.aim.domain.StudentRelative;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;

public class ContractPdf {

    static final Logger logger = LogManager.getLogger(ContractPdf.class);
    private final static String FONT_LOCATION = "/home/aim/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/aim/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    public static final SimpleDateFormat dateRu = new SimpleDateFormat(
            "MMMMM yyyyг.", new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"январь", "февраль", "март", "апрель", "май", "июнь",
                    "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};
        }
    });

    public ContractPdf(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 20, 30);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10.5f);
                Font headerBoldFont = new Font(baseFont, 8.5f);
                Font tinyFont = new Font(baseFont, 5f);
                Font captionBoldFont = new Font(baseFontBold, 11);
                Font ordBoldFont = new Font(baseFontBold, 10.5f);
                Font ordBoldUnderlineFont = new Font(baseFontBold, 10.5f, Font.UNDERLINE);
                Font ordItalicFont = new Font(baseFont, 10.5f, Font.ITALIC);
                Font ordBoldItalicFont = new Font(baseFontBold, 10.5f, Font.ITALIC);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                float[] tHeader_cols = {1f, 1f, 1f};
                PdfPTable tableHeader = new PdfPTable(3);
                tableHeader.setWidthPercentage(90f);
                tableHeader.setWidths(tHeader_cols);
                tableHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                tableHeader.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
                tableHeader.getDefaultCell().setBorder(0);

                Paragraph p = new Paragraph();
                p.add(new Phrase(studentInfo.getSchool().getName_kg(), captionBoldFont));
                p.add(new Phrase("\n\nДареги: ш. Бишкек, к. Курулуш шаарчасы 9,\n" +
                        "720016\n" +
                        "Тел: +996 (558) 250 250, +996 (508) 250 250\n" +
                        "Эл. почтасы: aim.bishkek@gmail.com", headerBoldFont));
                tableHeader.addCell(p);

                try {
                    Image logo = Image.getInstance("/home/aim/" + studentInfo.getSchool().getPhoto());
                    logo.scaleToFit(95f, 95f);

                    PdfPCell logoCell = new PdfPCell(logo);
                    logoCell.setBorder(0);
                    logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    logoCell.setVerticalAlignment(Element.ALIGN_TOP);
                    logoCell.setPaddingTop(5f);
                    tableHeader.addCell(logoCell);
                } catch (Exception e) {
                    logger.error("Fail to load logo", e);
                    tableHeader.addCell(" ");
                }

                p = new Paragraph();
                p.add(new Phrase(studentInfo.getSchool().getName_ru(), captionBoldFont));
                p.add(new Phrase("\n\nАдрес: г. Бишкек, ул. Городок Строителей 9,\n" +
                        "720016\n" +
                        "Тел: +996 (558) 250 250, +996 (508) 250 250\n" +
                        "Эл. почта: aim.bishkek@gmail.com", headerBoldFont));
                tableHeader.addCell(p);

                document.add(tableHeader);

                PdfPTable lineTable = new PdfPTable(1);
                lineTable.setWidthPercentage(85f);

                PdfPCell lineCell = new PdfPCell();
                lineCell.setBorderWidthBottom(1f);
                lineCell.setBorderWidthTop(0);
                lineCell.setBorderWidthLeft(0);
                lineCell.setBorderWidthRight(0);
                lineCell.setFixedHeight(5f);
                lineTable.addCell(lineCell);

                document.add(lineTable);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(0);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), captionBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()) + " на " + studentInfo.getYear().getName()
                        + " учебный год \nоб оказании платных образовательных услуг", captionBoldFont));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                Decliner dcl = new Decliner();
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

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(studentInfo.getSchool().getName_ru().replace("ОсОО", "Общество с ограниченной ответственностью"), ordBoldItalicFont));
                paragraph.add(new Phrase(", именуемая в дальнейшем «Школа», в лице директора " + fullName + ", действующего на основании  Устава, с одной стороны, и "
                        + studentInfo.getMainRelative().getFullName() + ", являющаяся(щийся) родителем или законным представителем «Учащегося» "
                        + studentFullName + ", именуемый в дальнейшем «Родитель» с другой стороны, в интересах обучающегося, в соответствии с пунктом 1 статьи 14 Закона Кыргызской Республики «Об образовании», заключили настоящий Договор о нижеследующем: ", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Предметом Договора является организация процесса обучения Учащегося, получение им образования соответствующего государственным стандартам учебных программ Министерства образования Кыргызской Республики, или получение им образования по стандартам учебных программ «Cambridge Assessment International Education» на период " + studentInfo.getYear().getPeriod() + ".", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. При освоении Учащимся образовательной программы и успешного прохождения государственной итоговой аттестации выдается соответствующий документ.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.3. ФИО родителя или законного представителя, указанного в настоящем Договоре, выступает ответственным лицом за взаимодействие со Школой, а также несет обязанности за исполнения обязательств, предусмотренных настоящим Договором.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ СТОРОН", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Школа обязана:", ordBoldUnderlineFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение услуг, предусмотренных в пункте 1.1. настоящего Договора. Образовательные услуги оказываются в соответствии с государственными стандартами учебных программ Министерства образования Кыргызской Республики или учебных программ «Cambridge Assessment International Education».", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. В целях усвоения Учащимся образовательных программ, являющихся предметом настоящего Договора, обеспечить Учащегося методической и консультационной помощью, оказываемой в порядке, установленным Школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. Предоставить Учащемуся на время обучения учебные кабинеты, компьютерный класс, доступ к библиотечным и информационным ресурсам Школы в рамках реализуемых образовательных программ.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.4. Обеспечить Учащегося трехразовым питанием (завтрак, обед и полдник).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.5. Сохранить место за Учащимся в случае пропуска занятий по уважительным причинам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.6. Проявлять уважение к личности Учащегося и создать благоприятные условия.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.7. Отвечать за здоровье Учащегося в период его пребывания в Школе, при условии неукоснительного соблюдения им «Правил Внутреннего Распорядка».", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.8. Быть уведомленной письменно (предоставить соответствующие медицинские справки) об индивидуальных аллергических особенностях Учащегося на те или иные продукты.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2. Школа имеет право:", ordBoldUnderlineFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. Самостоятельно, с учетом государственных программ, выбирать, разрабатывать и применять учебные программы и методики в процессе обучения Учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. Устанавливать размер родительской платы за оказание дополнительных платных образовательных услуг.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. Требовать с родителей (законных представителей) контроль за обучением и поведением Учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4. Самостоятельно составлять меню, производить замены блюд.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.5. Отчислить Учащегося из Школы с возмещением стоимости обучения и наложением штрафа в размере 10000 сомов в следующих случаях:", ordFont));
                document.add(paragraph);

                paragraph.setFirstLineIndent(40);
                paragraph.clear();
                paragraph.add(new Phrase("а) грубого, систематического нарушения «Правил Внутреннего Распорядка» с предоставлением документов, подтверждающих нарушения, совершенные Учащимся,", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) противозаконных действий по отношению к сверстникам и персоналу Школы,", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("в) нарушения законодательства Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph.setFirstLineIndent(20);
                paragraph.clear();
                paragraph.add(new Phrase("2.2.6. Расторгнуть настоящий Договор при условии не освоения Учащимся в установленный годовым календарным планом (графиком) срок образовательных программ, являющихся предметом настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. Самостоятельно перевести Учащегося в параллельную группу (класс).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.8. Расторгать в одностороннем порядке договор с родителями, нарушающих п.3.1., 3.2. и 3.3. настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.9. Не предоставлять образовательные услуги ученику, если родитель нарушает график оплаты более чем на 10 банковских дней.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.10. В случае расторжения контракта с родителем ученика после 30-го июня, 3% суммы контракта не подлежит возврату; в случае расторжения контракта в течение учебного года, оплаченная сумма за обучение возвращается по мере возможности Школы, но не позднее мая текущего учебного года.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.11. Школа имеет право повышать стоимость оплаты за обучение, исходя из инфляционных процессов в Кыргызстане.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3. Родители обязаны:", ordBoldUnderlineFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.1. При составлении договора родители должны оплатить ", ordFont));
                paragraph.add(new Phrase("30% ", ordBoldFont));
                paragraph.add(new Phrase("от общей стоимости контракта. Своевременно оплатить взнос за обучение Учащегося в Школе, согласно графику оплаты, в сроки, оговоренные в пункте 3.1-3.4. настоящего Договора. Обеспечить своего ребенка всеми необходимыми канцелярскими принадлежностями для собственного использования (тетради, цветные карандаши, точилки и т.д.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.2. Предоставить администрации школы медицинские справки 026, 063 о состоянии здоровья Учащегося перед прибытием его в Школу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.3. Заполнить «Анкету учащегося» и «Заявление о предоставлении достоверной информации о состоянии здоровья учащегося».", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.4. Ознакомить Учащегося с «Правилами внутреннего распорядка». Обеспечить адекватное воздействие на Учащегося в случаях нарушения им «Правил внутреннего распорядка».", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.5. Нести полную материальную ответственность за все действия Учащегося, повлекшие за собой, порчу или уничтожение имущества Школы в течение 7 рабочих дней.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.6. Обеспечить подвоз ученика в школу и обратно.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.7. Уведомить об аллергии ребенка и переносимости продукта.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4. Родители имеют право:", ordBoldUnderlineFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.1. Требовать от Администрации выполнения условий, изложенных в пунктах 2.1.1. – 2.1.7. настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.2. Досрочно расторгнуть Договор с возмещением стоимости обучения (за вычетом фактически понесенных расходов в период пребывания Учащегося в Школе, при этом каникулярное время считается как время учебы) в следующих случаях:", ordFont));
                document.add(paragraph);

                paragraph.setFirstLineIndent(40);
                paragraph.clear();
                paragraph.add(new Phrase("а) внезапной тяжелой болезни Учащегося, делающей невозможным его дальнейшее пребывание в Школе, при наличии официального заключения о состоянии здоровья Учащегося медицинскими специалистами Кыргызской Республики,", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) нарушения Школой условий, изложенных в пункте 2.1.1. – 2.1.7 настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("в) в связи с переводом в другую школу.", ordFont));
                document.add(paragraph);

                paragraph.setFirstLineIndent(20);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("3. УСЛОВИЯ ОПЛАТЫ", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. АДМИНИСТРАЦИЕЙ ШКОЛЫ УСТАНОВЛЕНЫ СЛЕДУЮЩИЕ УСЛОВИЯ ОПЛАТЫ РОДИТЕЛЬСКОГО ВЗНОСА:", ordItalicFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2. Оплата родительского взноса производится согласно ", ordFont));
                paragraph.add(new Phrase("Графику оплаты", ordBoldFont));
                paragraph.add(new Phrase(", подписанному обеими сторонами, являющегося неотъемлемой частью настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.3. Оплата за обучение производится родителями или лицами, их заменяющими, на банковский счет Школы (документ, подтверждающий оплату, родитель обязан сохранить и предоставить по требованию Школы).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.4. Питание и учебники включены в стоимость контракта. В конце каждого учебного года учебники сдаются в школьную библиотеку. Книги являются собственностью Школы, и в случае их потери родитель обязан возместить их стоимость.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.5. Для новых обучающихся 1 (один) комплект школьной формы предоставляется один раз бесплатно. При необходимости школьная форма и дополнительные учебники приобретаются за отдельную оплату.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.6. Оплата за обучение не включает в себя оплату за официальный экзамен «Cambridge Assessment International Education».", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.7. При возврате денег сроком ухода считается конец месяца, в котором уходит ученик, и за данный месяц взимается полная оплата.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("4. ФОРС-МАЖОР", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Стороны освобождаются от ответственности за полное или частичное невыполнение обязательств по настоящему Договору, если оно явилось последствием непреодолимой силы, а именно: пожар в здании Школы, землетрясение, наводнение, война, военные действия всех видов, забастовки, эпидемии, пандемии и карантины, а также Решение Правительства Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2. В случае карантина, введённого соответствующим образом на территории Кыргызской Республики, или болезни Учащегося сроком от двух и более недель (при наличии справки о болезни), 20% оплаты за дни карантина или болезни подлежат вычету.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("5. СРОК ДЕЙСТВИЯ ДОГОВОРА, ПОРЯДОК ИЗМЕНЕНИЯ,\n" +
                        "ДОПОЛНЕНИЯ И РАСТОРЖЕНИЯ", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Настоящий Договор вступает в силу с момента его подписания обеими Сторонами и внесения соответствующей оплаты согласно графику погашения. Договор действует до 31 мая 2026 года.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Настоящий Договор может быть расторгнут досрочно в случаях, предусмотренных п. 2.2.5. и п. 2.4.2.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. Любые изменения к настоящему Договору действительны только в случае, если они оформлены в письменной форме и подписаны обеими Сторонами.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. Настоящий Договор составлен в двух экземплярах на русском языке и подписан обеими Сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из Сторон находится один экземпляр настоящего Договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("6. РЕКВИЗИТЫ СТОРОН", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);
                document.add(new Paragraph(5, " "));

                float[] tDetails_cols = {1f, 1f};
                PdfPTable tableDetails = new PdfPTable(2);
                tableDetails.setWidthPercentage(90f);
                tableDetails.setWidths(tDetails_cols);
                tableDetails.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                tableDetails.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDetails.getDefaultCell().setPadding(8f);
                tableDetails.addCell(new Phrase(studentInfo.getSchool().getName_ru(), ordBoldFont));
                p = new Paragraph();
                p.add(new Phrase("ФИО родителя: ", ordBoldFont));
                p.add(new Phrase(studentInfo.getMainRelative().getFullName(), ordFont));
                tableDetails.addCell(p);

                p = new Paragraph();
                p.add(new Phrase("Адрес: ", ordBoldFont));
                p.add(new Phrase(studentInfo.getSchool().getAddress(), ordFont));
                tableDetails.addCell(p);
                p = new Paragraph();
                p.add(new Phrase("Адрес: ", ordBoldFont));
                p.add(new Phrase(studentInfo.getMainRelative().getAddress(), ordFont));
                tableDetails.addCell(p);

                p = new Paragraph();
                if (studentInfo.getSchool().getPhone() != null) {
                    String[] str = studentInfo.getSchool().getPhone().split(", ");
                    p.add(new Phrase("Тел: ", ordBoldFont));
                    for (int i = 0; i < str.length; i++) {
                        if (i == 0) {
                            p.add(new Phrase(str[i], ordFont));
                        } else {
                            p.add(new Phrase("        " + str[i], ordFont));
                        }
                        p.add(new Phrase(" \n\n", tinyFont));
                    }
                } else {
                    p.add(new Phrase("Тел: ", ordBoldFont));
                }
                tableDetails.addCell(p);
                p = new Paragraph();
                p.add(new Phrase("Тел:", ordBoldFont));
                for (int i = 0; i < studentInfo.getRelatives().size(); i++) {
                    if (i == 0) {
                        p.add(new Phrase(studentInfo.getRelatives().get(i).getPhone() +
                                " (" + studentInfo.getRelatives().get(i).getRelativeTitle() + ")", ordFont));
                    } else {
                        p.add(new Phrase("         "
                                + studentInfo.getRelatives().get(i).getPhone() +
                                " (" + studentInfo.getRelatives().get(i).getRelativeTitle() + ")", ordFont));
                    }
                    p.add(new Phrase(" \n\n", tinyFont));
                }
                tableDetails.addCell(p);

                p = new Paragraph();
                p.add(new Phrase("Банковские реквизиты: ", ordBoldFont));
                p.add(new Phrase(" \n\n", tinyFont));
                p.add(new Phrase(studentInfo.getSchool().getBank(), ordBoldFont));
                p.add(new Phrase(" \n\n", tinyFont));
                p.add(new Phrase("Банковский счет: " + studentInfo.getSchool().getBank_account(), ordBoldFont));
                p.add(new Phrase(" \n\n", tinyFont));
                p.add(new Phrase("БИК филиала: " + studentInfo.getSchool().getBik(), ordBoldFont));
                p.add(new Phrase(" \n\n", tinyFont));
                p.add(new Phrase("SWIFT: " + studentInfo.getSchool().getSwift(), ordBoldFont));
                tableDetails.addCell(p);
                p = new Paragraph();
                p.add(new Phrase("Паспорт: ", ordBoldFont));
                p.add(new Phrase(studentInfo.getMainRelative().getPassport(), ordFont));
                p.add(new Phrase(" \n\n", tinyFont));
                p.add(new Phrase("Выдан: ", ordBoldFont));
                p.add(new Phrase(studentInfo.getMainRelative().getGivenBy(), ordFont));
                p.add(new Phrase(" \n\n", tinyFont));
                p.add(new Phrase("Дата выдачи: ", ordBoldFont));
                p.add(new Phrase(Settings.df.format(studentInfo.getMainRelative().getIssueDate()), ordFont));
                p.add(new Phrase(" \n\n", tinyFont));
                p.add(new Phrase("(приложите ксерокопию паспорта)", ordBoldFont));
                tableDetails.addCell(p);

                p = new Paragraph();
                p.add(new Phrase("Директор: " + studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName(), ordBoldFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Подпись: ____________________ МП", ordBoldFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                tableDetails.addCell(p);
                p = new Paragraph();
                p.add(new Phrase("Родитель: " + studentInfo.getMainRelative().getFullName(), ordBoldFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Подпись: ____________________", ordBoldFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                tableDetails.addCell(p);

                document.add(tableDetails);


                headerBoldFont = new Font(baseFontBold, 14f);
                Font headerFont = new Font(baseFont, 14f);
                Font headerUnderlineFont = new Font(baseFontBold, 14f, Font.UNDERLINE);
                ordBoldFont = new Font(baseFontBold, 12f);
                ordFont = new Font(baseFont, 12f);
                ordBoldUnderlineFont = new Font(baseFontBold, 12f, Font.UNDERLINE);

                fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName().charAt(0) + ".";
                if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                    fullName += studentInfo.getDirector().getMiddle_name().charAt(0) + ".";
                }
                studentFullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                if (!studentInfo.getStudent().getMiddle_name().isEmpty()) {
                    studentFullName = studentFullName + " " + studentInfo.getStudent().getMiddle_name();
                }
                document.newPage();
                document.add(tableHeader);
                document.add(lineTable);
                document.add(new Paragraph(10, " "));

                spr = new Paragraph();
                spr.setAlignment(Element.ALIGN_CENTER);
                spr.add(new Phrase("График оплаты к договору", headerBoldFont));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("на " + studentInfo.getYear().getName() + " учебный год", ordBoldFont));
                document.add(spr);
                document.add(new Paragraph(10, " "));

                p = new Paragraph();
                p.setIndentationLeft(30);
                p.setIndentationRight(30);
                for (StudentRelative studentRelative : studentInfo.getRelatives()) {
                    p.add(new Phrase("Родитель: " + studentRelative.getFullName()
                            + " (" + studentRelative.getRelativeTitle() + ")", ordFont));
                    p.add(Chunk.NEWLINE);
                }
                p.add(new Phrase("Ученик: " + studentFullName + " " + studentInfo.getStudent().getClass_name() + " класс.", ordFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Стоимость обучения:", ordBoldUnderlineFont));
                p.add(Chunk.NEWLINE);
                p.add(new Phrase(studentInfo.getContractInfo().getContractTitle()
                        + ": " + Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) + " сом.", ordFont));
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Особые условия:", ordBoldUnderlineFont));
                p.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    p.add(new Phrase("Долг с предыдущего года: " + Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + " сом.", ordFont));
                } else {
                    p.add(new Phrase("Переплата с предыдущего года: " + Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + " сом.", ordFont));
                }
                p.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    p.add(new Phrase("Скидка: " + studentInfo.getContractInfo().getDiscountStr(), ordFont));
                    p.add(Chunk.NEWLINE);
                }
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    p.add(new Phrase("Корректировка: " + studentInfo.getContractInfo().getCorrectionStr(), ordFont));
                    p.add(Chunk.NEWLINE);
                }
                p.add(new Phrase("Окончательная сумма на " + studentInfo.getYear().getName() + " учебный год: " +
                        Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + " сом.", ordBoldFont));
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Предоплата 30%: " + Settings.dFormat2.format(studentInfo.getContractInfo().getPaid() == null ?
                        0 : studentInfo.getContractInfo().getPaid()) + " сом.", ordBoldFont));
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Остаток долга: " + Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + " сом.", ordBoldFont));
                p.add(Chunk.NEWLINE);
                document.add(p);

                spr = new Paragraph();
                spr.setAlignment(Element.ALIGN_CENTER);
                spr.add(new Phrase("График погашения остатка долга", ordBoldUnderlineFont));
                document.add(spr);
                document.add(new Paragraph(10, " "));

                tableDetails = new PdfPTable(2);
                tableDetails.setWidthPercentage(90f);
                tableDetails.setWidths(tDetails_cols);
                tableDetails.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                tableDetails.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDetails.getDefaultCell().setPaddingLeft(5f);
                tableDetails.getDefaultCell().setPaddingBottom(5f);
                List<Integer> list = (List<Integer>) instPlanCont.getItemIds();
                int counter = 0;
                for (int i = 0; i < instPlanCont.getItemIds().size(); i++) {
                    if ((Integer) instPlanCont.getContainerProperty(list.get(i), Settings.status_id).getValue() == 1) {
                        tableDetails.addCell(new Phrase(dateRu.format(((DateField) instPlanCont.getContainerProperty(list.get(i),
                                myUI.getMessage(Messages.Date)).getValue()).getValue()) + " - " + ((TextField) instPlanCont.getContainerProperty(list.get(i),
                                myUI.getMessage(Messages.Amount)).getValue()).getValue() + " сом.", ordFont));
                        counter++;
                    }
                }
                if (counter % 2 != 0) {
                    tableDetails.addCell(new Phrase(" ", ordFont));
                }
                tableDetails.addCell(p);
                document.add(tableDetails);

                p = new Paragraph();
                p.setIndentationLeft(30);
                p.setIndentationRight(30);
                p.add(new Phrase("Внимание: ", headerBoldFont));
                p.add(new Phrase("Ежемесячная оплата производится до 10 числа текущего месяца.", headerUnderlineFont));
                p.add(new Phrase("Школа аннулирует в автоматическом режиме особые условия, указанные в настоящем графике если: 1) родитель нарушает график оплаты, 2) ученик уходит со школы, не отучившись до конца учебного года.", headerFont));
                document.add(p);
                document.add(new Paragraph(10, " "));

                tableDetails = new PdfPTable(2);
                tableDetails.setWidthPercentage(90f);
                tableDetails.setWidths(tDetails_cols);
                tableDetails.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                tableDetails.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDetails.getDefaultCell().setPadding(8f);
                p = new Paragraph();
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Директор: " + fullName, ordFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("_________________", ordFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("           М.П.", ordFont));
                tableDetails.addCell(p);
                p = new Paragraph();
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("ФИО Родителя: " + studentInfo.getMainRelative().getFullName(), ordFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Подпись: _____________________", ordFont));
                p.add(Chunk.NEWLINE);
                p.add(Chunk.NEWLINE);
                p.add(new Phrase("Обязуюсь оплатить стоимость обучения в соответствии с настоящим графиком", ordFont));
                tableDetails.addCell(p);
                document.add(tableDetails);

                spr = new Paragraph();
                spr.setAlignment(Element.ALIGN_CENTER);
                spr.add(new Phrase("(Пункты, не вошедшие в договор вычеркнуты ручкой)", ordFont));
                document.add(spr);

                document.newPage();
                document.add(tableHeader);
                document.add(lineTable);
                document.add(new Paragraph(10, " "));

                headerBoldFont = new Font(baseFont, 12f);
                ordFont = new Font(baseFont, 11f);
                captionBoldFont = new Font(baseFontBold, 14);
                ordBoldFont = new Font(baseFontBold, 12f);

                float[] tHeader_cols2 = {1.4f, 1f};
                tableHeader = new PdfPTable(2);
                tableHeader.setWidthPercentage(90f);
                tableHeader.setWidths(tHeader_cols2);
                tableHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                tableHeader.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
                tableHeader.getDefaultCell().setBorder(0);

                tableHeader.addCell(new Phrase(" "));
                p = new Paragraph();
                p.add(new Phrase("Утверждаю директор\n" +
                        studentInfo.getSchool().getName_ru() + "\n" +
                        fullName + "  ______________", headerBoldFont));
                tableHeader.addCell(p);
                document.add(tableHeader);
                document.add(new Paragraph(20, " "));

                paragraph.setIndentationLeft(50);
                paragraph.setFirstLineIndent(-20);
                paragraph.clear();
                paragraph.add(new Phrase("Правила внутреннего распорядка Школы", captionBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);
                document.add(new Paragraph(5, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1. Школьная линейка и физзарядка", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.1. Торжественная линейка в честь начала учебной недели с исполнением Государственного гимна проводится в понедельник в 8.40. Общешкольная линейка с исполнением государственного гимна проводится в пятницу в 16:20. Линейка проводится с целью подготовки учащихся к учебной неделе, подведения итогов учебно-воспитательного процесса, получения информации и объявлений, привития дисциплинарных норм поведения. В остальные дни, в 08.30, проводится физзарядка (15 мин) и чтение книг (15 мин).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. Линейка и физзарядка проводятся на спортивной площадке, при неподходящих погодных условиях – в помещении школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.3. Линейку и физзарядку организуют классные руководители и ассистенты. Дежурные учителя контролируют организацию проведения линейки. Физзарядка проводится учителем физкультуры.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("2. Уроки", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1. После звонка на урок ученик должен находиться в классе. Необходимые для данного урока учебные принадлежности (учебник, тетрадь, ручку и др.) ученик должен подготовить на перемене.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2. Для приветствия входящих в класс учителей, родителей, гостей – ученики должны встать.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3. Во время урока учащийся должен находиться на своем учебном месте, слушать учителя, не мешать одноклассникам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4. Если ученик нарушает дисциплину на уроке: срывает урок, не желает выполнять задания, громко разговаривает, грубо относится к учителю и ассистенту, оскорбляет одноклассников и др. – его поведение будет рассматриваться дисциплинарной комиссией.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.5. Запрещено выходить с урока без разрешения учителя и есть еду на уроках.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("3. Режим школы", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1. Учащиеся приходят в школу в 08:30, находятся в школе до 17:00.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2. На линейку, уроки, этюды, дополнительные занятия, кружки, мероприятия учащийся должен приходить без опоздания.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.3. При опоздании ученик может зайти на урок только с разрешения (специальный талон) заместителя директора по УВР, который ведет учет опозданий.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("4. Разрешения", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1. Все разрешения выдаются заместителем директора по УВР.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2. Разрешения досрочно забрать ребёнка с уроков по каким-либо причинам выдаются только родителям.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.3. При ухудшении состояния здоровья учащегося, его могут забрать домой родители или близкие родственники. Родители обязаны предупредить учителя или ассистента о том, кто заберёт ребёнка.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.4. При самовольном уходе с уроков или из школы, поведение учащегося рассматривается на заседании дисциплинарной комиссии.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("5. Внешний вид учащегося", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.1. Учащийся обязан находиться в школе в школьной форме, на уроках физкультуры – в спортивной форме и обуви.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Учащийся должен быть опрятен и аккуратен: приходить в чистой выглаженной школьной форме, волосы должны быть причесаны, у мальчиков волосы коротко острижены, ногти — чистые и коротко остриженные.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("6. Мероприятия ", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.1. Участие ребёнка в мероприятиях вне школы проводится только с разрешения родителей.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.2. Согласно воспитательным планам школы, после уроков могут проводиться субботники с участием учащихся.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("7. Система поощрений и дисциплинарных мер", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.1 В нашей школе, по рекомендации опытных педагогов, действует Система \"Зелёная карта\", направленная на активное участие учеников в учебном процессе. За хорошие поступки и активное участие в уроках ученики набирают баллы через платформу EduPage. В конце каждого четверти они обменивают набранные баллы на билеты для участия в лотерее \"Green Card\" и получают подарки. ", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.2 В то же время за нежелательное поведение во время уроков и внутри школы начисляются штрафные баллы. Если количество таких баллов превышает установленный лимит, родители уведомляются, и могут быть применены определённые дисциплинарные меры. ", ordFont));
                document.add(paragraph);
                paragraph.clear();

                paragraph.add(new Phrase("7.3 Эта система направлена на развитие у учеников ответственности и поддержку их академической успеваемости. Мы просим родителей оказать содействие в адаптации учеников к данной системе.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("8. Этюды", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.1. Этюд – ежедневное выполнение домашних заданий. В течение 1 часа учащийся выполняет домашнюю работу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.2. Во время этюдов в классе находится классный руководитель и ассистент учителя.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.3. Все дисциплинарные требования, предъявляемые к урокам, сохраняются на этюдах.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("9. Общие правила", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.1. Для связи с родителями до или после уроков ученику разрешается пользоваться мобильным телефоном. Во время учебной деятельности (09:00–17:00) пользоваться мобильным телефоном запрещено. Утром телефон сдается классному руководителю или ассистенту, после уроков ассистенты отдают учащемуся телефон. Приносить умные часы запрещается.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.2. В случае утери телефона, не сданного классному руководителю или ассистенту, школа ответственности не несёт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.3. Нельзя приносить в школу планшеты, игрушки, а также не относящиеся к учёбе книги, журналы, брошюры, сладости, напитки и т.д. Любой вид торговли между учащимися запрещён.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.4. В случае обнаружения у учащегося запрещённых вещей, они изымаются администрацией, и данный факт рассматривается на заседании дисциплинарной комиссии.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.5. Учащийся должен говорить правду, не искажать факты, не наговаривать на одноклассников.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.6. Учащийся должен относить родителям табель успеваемости за полугодие, документы, информационные письма и т.д.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.7. Учащийся должен бережно относиться к школьному имуществу: ученикам, книгам, партам, шкафам, интерактивной доске, компьютеру. В случае поломки учащимся школьного имущества, родители должны восстановить нанесённый ущерб, по отношению к ученику применяются дисциплинарные меры дисциплинарной комиссии.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.8. Учащийся должен соблюдать нормы санитарной гигиены и беречь окружающую среду, не бросать мусор в помещениях и вокруг школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.9. Учащиеся не должны брать чужие вещи, учебные принадлежности и др. В случае совершения кражи или оказания помощи в совершении кражи, учащийся может быть исключён из школы. Случаи кражи рассматриваются на дисциплинарной комиссии.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.10. Учащийся должен соблюдать нормы этики в общении с другими учащимися школы: не кричать, не оскорблять, не говорить бранные слова. Неоднократные случаи грубого отношения к другим учащимся рассматриваются на дисциплинарной комиссии.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9.11. В случае нарушения правил внутреннего распорядка и снижения академической успеваемости, школа имеет право лишить учащегося предоставленной скидки.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("10. Родители", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("10.1. При посещении школы в учебное время (с 08:30 до 17:00) родители не должны подниматься на 2 этаж здания и вызывать учащихся во время уроков.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("10.2. При необходимости встречи с классным руководителем, предметным учителем, ассистентом учителя или представителем администрации, родители должны заранее назначить встречу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("10.3. Посещать уроки можно только при согласовании с администрацией.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("10.4. Запрещается приносить на дни рождения детей кондитерские изделия, подарки и др.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("10.5. Запрещается дополнительно оплачивать учителям индивидуальные занятия.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("10.6. Самостоятельный отъезд детей после уроков (в 17:00) без сопровождения взрослых осуществляется при наличии заявления родителей с принятием ответственности за здоровье и жизнь ребёнка. Ученики должны покинуть школу не позднее 17:00.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(20, " "));

                tableHeader = new PdfPTable(2);
                tableHeader.setWidthPercentage(90f);
                tableHeader.setWidths(tHeader_cols2);
                tableHeader.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                tableHeader.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
                tableHeader.getDefaultCell().setBorder(0);

                tableHeader.addCell(new Phrase("ФИО: " + studentInfo.getMainRelative().getFullName() +
                        "\nЯ, ознакомился с правилами и условиями", ordBoldFont));
                tableHeader.addCell(new Phrase("_____________________" +
                        "\n          (подпись)", ordBoldFont));
                document.add(tableHeader);

                captionBoldFont = new Font(baseFontBold, 16f);
                ordBoldFont = new Font(baseFontBold, 14f);

                document.newPage();
                document.add(new Paragraph(20, " "));

                p = new Paragraph();
                p.setIndentationLeft(30);
                p.setIndentationRight(30);
                p.add(new Phrase("В НАЗНАЧЕНИИ  ПЛАТЕЖА ОБЯЗАТЕЛЬНО УКАЗЫВАТЬ № ДОГОВОРА, Ф.И. РЕБЕНКА", ordBoldFont));
                document.add(p);
                document.add(new Paragraph(20, " "));

                p.clear();
                p.add(new Phrase("ОАО \"Оптима Банк\"", ordBoldFont));
                document.add(p);
                document.add(new Paragraph(20, " "));

                float[] tHeader_cols3 = {2f, 2f, 1f, 0.9f, 1.3f};
                tableDetails = new PdfPTable(5);
                tableDetails.setWidthPercentage(90f);
                tableDetails.setWidths(tHeader_cols3);
                tableDetails.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                tableDetails.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                tableDetails.getDefaultCell().setPadding(5f);

                tableDetails.addCell(new Phrase("ПОЛУЧАТЕЛЬ", ordBoldFont));
                tableDetails.addCell(new Phrase("Банковский счет", ordBoldFont));
                tableDetails.addCell(new Phrase("БИК\nфилиала", ordBoldFont));
                tableDetails.addCell(new Phrase("Сумма", ordBoldFont));
                tableDetails.addCell(new Phrase("Ежемесячно", ordBoldFont));
                tableDetails.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                tableDetails.addCell(new Phrase(studentInfo.getSchool().getName_ru(), ordBoldFont));
                tableDetails.addCell(new Phrase(studentInfo.getSchool().getBank_account(), captionBoldFont));
                tableDetails.addCell(new Phrase(studentInfo.getSchool().getBik(), captionBoldFont));
                tableDetails.addCell(new Phrase(" ", ordBoldFont));
                tableDetails.addCell(new Phrase(" ", ordBoldFont));
                document.add(tableDetails);
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

        Font f_font = new Font(Font.FontFamily.UNDEFINED, 9, Font.NORMAL);

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            try {
                Phrase ft = new Phrase(String.format(" %d",
                        writer.getPageNumber()), f_font);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, ft,
                        (document.right() - 30),
                        document.bottom() - 10, 0);

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }

    }
}
