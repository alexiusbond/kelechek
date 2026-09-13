package kg.alex.kelechek.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.domain.StudentInfoPdf;
import kg.alex.kelechek.domain.StudentRelative;
import kg.alex.kelechek.utils.Decliner;
import kg.alex.kelechek.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

public class ContractPdfRu {

    static final Logger logger = LogManager.getLogger(ContractPdfRu.class);
    private final static String FONT_LOCATION = "/home/kelechek/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/kelechek/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractPdfRu(final MyVaadinUI ui, StudentInfoPdf st_info) {
        this.myUI = ui;
        this.studentInfo = st_info;
        String[] years = studentInfo.getYear().getName().split("-");

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 25, 45);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10);
                Font ordBoldFont = new Font(baseFontBold, 10);
                Font ordItalicFont = new Font(baseFont, 10, Font.ITALIC);
                Font headerBoldFont = new Font(baseFontBold, 11);
                Font headerItalicFont = new Font(baseFont, 11, Font.ITALIC);
                Font fontFooter = new Font(
                        baseFont, 10, Font.NORMAL, new BaseColor(120, 120, 120));

                writer.setPageEvent(new myPageEvent(fontFooter));

                document.open();

                document.add(new Paragraph(10, " "));
                Paragraph spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР", headerBoldFont));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("об оказании образовательных услуг", headerBoldFont));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("Лицензия МОиН КР № B2023-0001", headerItalicFont));
                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("Зачисление производится на основании подписанного договора, результатов тестирования, медицинской формы №63, №26, ксерокопии паспорта родителей, внесения денежной суммы в качестве предоплаты и единовременного взноса.", ordItalicFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_date_colsWidth = {1f, 1f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase("г. Бишкек", ordFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(new Date()), ordFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                StudentRelative mainRelative = studentInfo.getMainRelative();
                String parentFullName = mainRelative == null || mainRelative.getFullName() == null
                        ? "__________________________" : mainRelative.getFullName();
                String relativeTitle = mainRelative == null || mainRelative.getRelativeTitle() == null
                        ? "____________" : mainRelative.getRelativeTitle();
                Decliner dcl = new Decliner();
                String fullName = null;
                try {
                    boolean isFeminine = studentInfo.getStudent().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getStudent().getSurname(), isFeminine)
                            + " " + dcl.DeclineNameGenitive(studentInfo.getStudent().getName(), isFeminine, false);
                    if (studentInfo.getStudent().getMiddle_name() != null && !studentInfo.getStudent().getMiddle_name().isEmpty()) {
                        fullName += " " + dcl.DeclinePatronymicGenitive(studentInfo.getStudent().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }

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
                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Учреждение " + studentInfo.getSchool().getName_ru() +
                        ", зарегистрированное в соответствии с законодательством Кыргызской Республики, именуемое в дальнейшем «Школа», в лице Директора " + directorFullName + ", действующего на основании Устава, с одной стороны и ", ordFont));
                paragraph.add(new Phrase(parentFullName + " - " + relativeTitle, ordBoldFont));
                paragraph.add(new Phrase(", именуем(ый)ая в дальнейшем ", ordFont));
                paragraph.add(new Phrase("«Родитель»", ordBoldFont));
                paragraph.add(new Phrase(", представляющ(ий)ая интересы ", ordFont));
                paragraph.add(new Phrase(fullName + ", "
                        + (studentInfo.getStudent().getBirth_date() == null ? "____________"
                        : Settings.df.format(studentInfo.getStudent().getBirth_date())), ordBoldFont));
                paragraph.add(new Phrase(" класс ", ordFont));
                paragraph.add(new Phrase(studentInfo.getStudent().getClass_name() == null
                        ? "____________" : studentInfo.getStudent().getClass_name(), ordBoldFont));
                paragraph.add(new Phrase(", именуемо(го)й в дальнейшем ", ordFont));
                paragraph.add(new Phrase("«Учащийся»", ordBoldFont));
                paragraph.add(new Phrase(", с другой стороны, совместно именуемые «Стороны», заключили настоящий Договор об оказании образовательных услуг в соответствии с общеобразовательными учебными программами дошкольного, начального, основного общего и среднего общего образования (далее – «Договор») о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. ", ordBoldFont));
                paragraph.add(new Phrase("По настоящему Договору Школа обязуется оказывать Учащемуся комплекс образовательных услуг (далее – «услуги») в период ", ordFont));
                paragraph.add(new Phrase(studentInfo.getYear().getPeriod(), ordBoldFont));
                paragraph.add(new Phrase(" в зависимости от класса согласно п.2.3. (далее – «учебный год»), а Родитель обязуется оплачивать указанные услуги в порядке и сроки, предусмотренные настоящим Договором.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. ", ordBoldFont));
                paragraph.add(new Phrase("Под комплексом образовательных услуг в рамках настоящего Договора понимается совокупность следующих услуг:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2.1. ", ordBoldFont));
                paragraph.add(new Phrase("организация и реализация образовательного процесса, соответствующего образовательной программе начального общего, основного общего и среднего общего образования в соответствии с требованиями Государственного образовательного стандарта, законодательства об образовании, Уставом Школы, внутренними актами Школы и условиями настоящего Договора;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2.2. ", ordBoldFont));
                paragraph.add(new Phrase("услуги, связанные с организацией трехразового горячего питания Учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.3. ", ordBoldFont));
                paragraph.add(new Phrase("Комплекс услуг, предусмотренный п.п. 1.2.1-1.2.2. настоящего Договора, предоставляется целиком без возможности выделения отдельных компонентов. Исключение составляют случаи введения в установленном порядке органами государственной власти и/или органами местного самоуправления режима Чрезвычайного положения и/или Чрезвычайной ситуации и/или карантина и/или иных общеобязательных ограничительных мер, препятствующих оказанию всего комплекса образовательных услуг. В таких случаях комплекс образовательных услуг оказывается частично, то есть в части услуг, предусмотренных п.1.2.1 настоящего Договора, с использованием дистанционных образовательных технологий (далее – «дистанционное обучение»). Порядок оказания и стоимость дистанционного обучения определяется разделом 5 настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.4. ", ordBoldFont));
                paragraph.add(new Phrase("Школа вправе оказывать другие дополнительные образовательные услуги, порядок, сроки и стоимость которых устанавливаются отдельными договорами.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("2. СТОИМОСТЬ УСЛУГ И ПОРЯДОК РАСЧЕТОВ", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. ", ordBoldFont));
                paragraph.add(new Phrase("Стоимость услуг по настоящему Договору составляет:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                // Стоимость оставлена как в шаблоне договора.
                paragraph.add(new Phrase("49500 (сорок девять тысяч пятьсот) сом 00 т. сомов в месяц,", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("445500 (четыреста сорок пять тысяч пятьсот) сом 00 т. сомов за один учебный год.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2. ", ordBoldFont));
                paragraph.add(new Phrase("Оплата, предусмотренная пунктом 2.1. настоящего Договора, производится путем перечисления/пополнения денежных средств на расчётный счет, либо наличными в кассу Школы в национальной валюте в следующем порядке:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. ", ordBoldFont));
                paragraph.add(new Phrase("предоплата в размере двухмесячной оплаты, которая засчитывается в счет оплаты за сентябрь и октябрь " + years[0] + " г., производится в день заключения настоящего Договора;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. ", ordBoldFont));
                paragraph.add(new Phrase("с ноября " + years[0] + " г. по май " + years[1] + " г. на ежемесячной основе, в виде ", ordFont));
                paragraph.add(new Phrase("предоплаты до 5 числа каждого месяца.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3. ", ordBoldFont));
                paragraph.add(new Phrase("В случае отсутствия предоплаты, указанной в п.2.2 настоящего Договора, а равно несвоевременной оплаты Школа вправе прекратить оказание комплексных образовательных услуг Учащемуся либо ограничить выполнение своих обязательств по настоящему Договору в соответствии с п.2.12, а также Школа оставляет за собой право применить штрафные санкции, указанные в пункте 2.11. При систематическом нарушении Родителем сроков оплаты в течение учебного года, Школа вправе отказаться от подписания договора об оказании образовательных услуг Учащемуся на новый учебный год.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4. ", ordBoldFont));
                paragraph.add(new Phrase("Длительность учебного года для Учащихся с 1 по 8 и 10 классов составляет 9 (девять) месяцев, а для 9 и 11 классов – 9,5 месяцев.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.5. ", ordBoldFont));
                paragraph.add(new Phrase("В случае предоплаты стоимости услуг за весь учебный год единовременным платежом ", ordFont));
                paragraph.add(new Phrase("в срок до 31 мая " + years[0] + " года", ordBoldFont));
                paragraph.add(new Phrase(", Родителю предоставляется скидка в размере ", ordFont));
                paragraph.add(new Phrase("5% (пять)", ordBoldFont));
                paragraph.add(new Phrase(" от годовой стоимости услуг.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.6. ", ordBoldFont));
                paragraph.add(new Phrase("Учащемуся, впервые поступающему в Школу, предоставляется испытательный срок, который длится 1 (один) месяц с момента зачисления Учащегося в Школу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.7. ", ordBoldFont));
                paragraph.add(new Phrase("При заключении настоящего Договора для впервые поступающих в Школу Родителем также оплачивается единовременный вступительный взнос в размере 19 500 (девятнадцать тысяч пятьсот) сомов с учетом налогов. В случае ", ordFont));
                paragraph.add(new Phrase("отказа ", ordBoldFont));
                paragraph.add(new Phrase("Учащегося от учебы до начала учебного года, вступительный взнос не возвращается, и предоплата за 1 учебный месяц удерживается. В случае ухода Учащегося во время учебного года ", ordFont));
                paragraph.add(new Phrase("вступительный взнос не возвращается и учебный месяц на момент ухода считается полным. Все выданные учебные пособия согласно п.п 4.2.16 настоящего Договора должны быть возвращены.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Назначение вступительного взноса: улучшение инфраструктуры Школы (строительные и ремонтные работы, подготовка к новому учебному году), расходы на инновационное и техническое оборудование, прочие расходы Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.8. ", ordBoldFont));
                paragraph.add(new Phrase("Моментом оплаты (моментом исполнения обязательства по оплате на соответствующую сумму) считается дата зачисления денежных средств на расчетный счет либо в кассу Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.9. ", ordBoldFont));
                paragraph.add(new Phrase("Стоимость услуг, предусмотренная п.2.1 настоящего Договора, не включает в себя банковские расходы (удержания, комиссии за перечисление оплаты, администрирование, ведение банковского счета и/или электронного кошелька Родителя, включая аналогичные расходы банков-корреспондентов), связанные с платежами по настоящему Договору, которые являются расходами самого Родителя и оплачиваются им самостоятельно.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.10. ", ordBoldFont));
                paragraph.add(new Phrase("Стоимость услуг, предусмотренная пунктом 2.1. настоящего Договора, является ", ordFont));
                paragraph.add(new Phrase("фиксированной и не подлежит изменению в течение учебного года. ", ordBoldFont));
                paragraph.add(new Phrase("Если иное не будет установлено дополнительным соглашением Сторон, то перерасчет стоимости услуг (или отдельных компонентов услуг) во время пропусков и каникул не производится и не зависит от фактического количества дней посещения Учащимся занятий, пропуска дней по уважительной причине, а также количества дней в каждом конкретном календарном месяце. Исключения составляют, случаи вынужденного перевода на дистанционное обучение, стоимость которого определяется в соответствии с разделом 5 настоящего Договора. Перерасчет за питание производится в случае отсутствия Учащегося на ", ordFont));
                paragraph.add(new Phrase("15 дней и более, при предоставлении соответствующих справок.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.11. ", ordBoldFont));
                paragraph.add(new Phrase("За нарушение сроков исполнения обязательств по оплате, в сроки, предусмотренные в пункте 2.2 настоящего Договора, Родитель обязуется оплачивать неустойку в размере ", ordFont));
                paragraph.add(new Phrase("3000 (трех тысяч) сом ", ordBoldFont));
                paragraph.add(new Phrase(" при просрочке более ", ordFont));
                paragraph.add(new Phrase("5 (пяти) календарных дней. ", ordBoldFont));
                paragraph.add(new Phrase("При просрочке оплаты более 30 (тридцати) календарных дней Родитель обязуется оплатить неустойку в размере ", ordFont));
                paragraph.add(new Phrase("9000 (девяти тысяч) сом. ", ordBoldFont));
                paragraph.add(new Phrase("В случае задолженности по оплате ", ordFont));
                paragraph.add(new Phrase("за 2 месяца ", ordBoldFont));
                paragraph.add(new Phrase("Школа вправе прекратить оказание комплексных образовательных услуг по настоящему договору путем его расторжения. При этом договор будет считаться автоматически расторгнутым в одностороннем порядке без подписания соглашения о расторжении. Случаи форс-мажоров рассматриваются индивидуально, на основе заявлений от Родителей.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.12. ", ordBoldFont));
                paragraph.add(new Phrase("Школа оставляет за собой право ограничить выполнение своих обязательств по настоящему Договору в случае неисполнения или частичного неисполнения Родителем обязанности по оплате, предусмотренный пунктом 2.2 настоящего Договора. Ограниченное оказание услуг подразумевает ", ordFont));
                paragraph.add(new Phrase("недопуск к кружкам и спортивным секциям, ограничение доступа к учетной записи в электронном дневнике Эдупейдж, недопуск к четвертным экзаменам, недопуск к урокам или другие ограничения, которые могут применяться по собственному усмотрению Школы.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.13. ", ordBoldFont));
                paragraph.add(new Phrase("Книги по английскому языку, рабочие тетради входят в стоимость услуг. Все дополнительные услуги, такие как транспортные услуги, не предусмотренные настоящим Договором, оформляются отдельным соглашением и оплачиваются отдельно.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. ПРАВА И ОБЯЗАННОСТИ ШКОЛЫ", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. ", ordBoldFont));
                paragraph.add(new Phrase("Школа вправе:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.1. ", ordBoldFont));
                paragraph.add(new Phrase("свободно и самостоятельно разрабатывать, выбирать и использовать учебные материалы, программы, технологии, вносить изменения в учебный план и расписание занятий, определять сроки каникул, продолжительность учебного дня и занятий, вносить корректировки в расписание учебных занятий, питание;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.2. ", ordBoldFont));
                paragraph.add(new Phrase("свободно и самостоятельно формировать состав Учащихся в классе в начале учебного года, переводить Учащегося в другой класс;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.3. ", ordBoldFont));
                paragraph.add(new Phrase("переводить Учащегося в другую группу (подгруппу) или на индивидуальное обучение по всем или отдельным занятиям, предварительно согласовав с Родителем;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.4. ", ordBoldFont));
                paragraph.add(new Phrase("размещать изображение, видеозаписи выступлений, работ и иных достижений Учащегося в печатных изданиях, на стендах, выставках, корпоративном сайте или социальных сетях Школы, иных информационных сетях и средствах. В случае несогласия Родителя Родитель обязан предоставить соответствующее заявление на имя директора Школы в начале учебного года;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.5. ", ordBoldFont));
                paragraph.add(new Phrase("требовать своевременной оплаты оказываемых услуг, а также оплаты неустойки в установленных настоящим Договором и действующим законодательством случаях и порядке;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.6. ", ordBoldFont));
                paragraph.add(new Phrase("по своему усмотрению осуществлять подбор специалистов и педагогов, в том числе привлекать третьих лиц для оказания услуг по настоящему Договору (транспортные услуги, репетиторы, кружковые занятия, питание и пр.);", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.7. ", ordBoldFont));
                paragraph.add(new Phrase("приостановить или прекратить оказание услуг согласно условиям Договора;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.8. ", ordBoldFont));
                paragraph.add(new Phrase("на основании решения педагогического совета, в связи с неуспеваемостью учащегося, предварительно согласовав с Родителем, оставить учащегося на второй год обучения в том же классе или отчислить учащегося из Школы путем одностороннего расторжения настоящего Договора;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.9. ", ordBoldFont));
                paragraph.add(new Phrase("на основании решения педагогического совета, в связи с успеваемостью учащегося перевести учащегося в следующий класс и, предварительно согласовав с Родителем, применить льготы по стоимости обучения путем подписания дополнительного соглашения к настоящему Договору;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.10. ", ordBoldFont));
                paragraph.add(new Phrase("пользоваться иными правами, предусмотренными настоящим Договором и действующим законодательством.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2. ", ordBoldFont));
                paragraph.add(new Phrase("Школа обязана:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.1. ", ordBoldFont));
                paragraph.add(new Phrase("оказывать образовательные услуги в объеме и в сроки, предусмотренные настоящим Договором, Государственным образовательным стандартом и действующим законодательством;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.2. ", ordBoldFont));
                paragraph.add(new Phrase("ознакомить Родителя с правилами внутреннего распорядка Школы, а также при необходимости с другими документами, регламентирующими организацию и осуществление образовательной деятельности, права и обязанности Учащегося;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.3. ", ordBoldFont));
                paragraph.add(new Phrase("по запросу Родителя ознакомить с ходом и содержанием учебного процесса, а также с перечнем дополнительных занятий, распорядком дня Учащегося в Школе.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.4. ", ordBoldFont));
                paragraph.add(new Phrase("обеспечить пятидневную учебную неделю в режиме ", ordFont));
                paragraph.add(new Phrase("с 08:00 до 17:00 часов для ", ordBoldFont));
                paragraph.add(new Phrase("Учащихся 1-4-х классов и ", ordFont));
                paragraph.add(new Phrase("с 08:00 до 16:00 часов для", ordBoldFont));
                paragraph.add(new Phrase(" Учащихся 5-11-х классов с понедельника по пятницу (за исключением официальных праздничных и нерабочих дней). В случае, если Родитель по своей инициативе сокращает время нахождения Учащегося в Школе, и данное обстоятельство привело к неполному освоению им образовательной программы, то Школа не несет ответственности за полноту оказанных услуг.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Ответственность за последствия нахождения Учащегося на территории Школы после окончания учебного процесса (с 08:00 до 17:00 часов для Учащихся 1-4-х классов и с 08:00 до 16:00 часов для Учащихся 5-11-х классов) ", ordFont));
                paragraph.add(new Phrase("администрация Школы не несет.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.5. ", ordBoldFont));
                paragraph.add(new Phrase("во время нахождения Учащегося в Школе обеспечить условия для подготовки домашних заданий и иной самоподготовки, организовать досуг Учащегося, в том числе дополнительно согласуемые с Родителем экскурсии, занятия в различных кружках и секциях при Школе;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.6. ", ordBoldFont));
                paragraph.add(new Phrase("обеспечить получение образовательных услуг Учащимся в классе с наполняемостью до 22-24 обучающихся;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.7. ", ordBoldFont));
                paragraph.add(new Phrase("осуществлять текущий контроль успеваемости и промежуточную аттестацию Учащегося в соответствии с учебным планом и требованиями законодательства;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.8. ", ordBoldFont));
                paragraph.add(new Phrase("вести личное дело и документы по успеваемости Учащегося в соответствии с принятыми стандартами школьного делопроизводства, предоставлять указанные документы для ознакомления по запросу Родителя;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.9. ", ordBoldFont));
                paragraph.add(new Phrase("при отчислении Учащегося из Школы предоставить Родителю соответствующие документы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.10. ", ordBoldFont));
                paragraph.add(new Phrase("обеспечить сбалансированное Зх-разовое питание Учащегося во время его нахождения в Школе.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. ПРАВА И ОБЯЗАННОСТИ РОДИТЕЛЯ", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. ", ordBoldFont));
                paragraph.add(new Phrase("Родитель вправе:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1.1. ", ordBoldFont));
                paragraph.add(new Phrase("знакомиться с документами и сведениями, связанными с организацией образовательного процесса в Школе, в том числе со сведениями об академической успеваемости Учащегося, информацией о планируемых школьных мероприятиях и дополнительных образовательных услугах Школы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1.2. ", ordBoldFont));
                paragraph.add(new Phrase("участвовать в культурных, спортивно-оздоровительных мероприятиях, а также открытых уроках, выставках, балах, экскурсиях и иных мероприятиях, проводимых в Школе (если участие родителей допускается для такого рода мероприятий);", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1.3. ", ordBoldFont));
                paragraph.add(new Phrase("принимать участие в работе общественных (родительских) объединений, комитетов и попечительских советов, вносить замечания и предложения по организации образовательных и других услуг путем направления письменных обращений на имя Директора Школы, в том числе посредством e-mail;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1.4. ", ordBoldFont));
                paragraph.add(new Phrase("оказывать различную, в том числе благотворительную помощь (пожертвование) Школе для достижения уставных целей Школы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1.5. ", ordBoldFont));
                paragraph.add(new Phrase("обращаться к классному руководителю и заместителю Директора, а в приемные часы к Директору Школы, по вопросам, связанным с оказанием услуг, вносить свои предложения и замечания;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1.6. ", ordBoldFont));
                paragraph.add(new Phrase("требовать соблюдения условий настоящего Договора, а также представлять и защищать права и законные интересы Учащегося в установленном порядке;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.1.7. ", ordBoldFont));
                paragraph.add(new Phrase("пользоваться иными правами, предусмотренными настоящим Договором, действующим законодательством и внутренними актами Школы,", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2. ", ordBoldFont));
                paragraph.add(new Phrase("Родитель обязан:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.1. ", ordBoldFont));
                paragraph.add(new Phrase("своевременно предоставлять по запросу Школы всю необходимую информацию, документы и данные, необходимые для оказания услуг, в том числе, но, не ограничиваясь копией свидетельства о рождении Учащегося, медицинские справки о состоянии здоровья Учащегося в установленной форме;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.2. ", ordBoldFont));
                paragraph.add(new Phrase("в письменной форме информировать администрацию Школы и педагогических работников (классного руководителя) о лицах, которым может быть доверен Учащийся по окончании учебного процесса, о 2 (двух) телефонных номерах для связи с Родителем и дополнительным представителем Учащегося в течение учебного процесса;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.3. ", ordBoldFont));
                paragraph.add(new Phrase("своевременно сообщать Школе обо всех изменениях данных, предусмотренных п.п.4.2.1-4.2.2 настоящего Договора, нести ответственность за достоверность и полноту указанных сведений и документов;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.4. ", ordBoldFont));
                paragraph.add(new Phrase("по окончании учебного процесса забрать ребенка из школы и/или давать Учащемуся разъяснение о том, что сразу после окончания учебного процесса Учащийся обязан покинуть территорию Школы (не гулять на территории Школы, не ходить на спортивную площадку, не собираться с другими учащимися и т.п.). Ответственность за последствия нахождения Учащегося после окончания учебного процесса указанного в ", ordFont));
                paragraph.add(new Phrase("п.п. 3.2.4 ", ordBoldFont));
                paragraph.add(new Phrase("Школа не несет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.5. ", ordBoldFont));
                paragraph.add(new Phrase("своевременно и в полном объеме производить оплату стоимости услуг по настоящему Договору, а при просрочке оплаты – оплатить неустойку в порядке, предусмотренном пунктом 2.11 настоящего Договора и действующим законодательством;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.6. ", ordBoldFont));
                paragraph.add(new Phrase("обеспечить своевременную явку и посещение Учащимся занятий в Школе в соответствии с расписанием, его активное участие в учебном процессе. В случае необходимости пропуска занятий Учащимся, независимо от причин пропуска, уведомить классного руководителя или директора Школы о причинах пропуска и его предстоящей длительности в первый же день пропуска, не позднее 08:30 часов утра;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.7. ", ordBoldFont));
                paragraph.add(new Phrase("не допускать посещения Школы Учащимся в случае наличия подозрений или непосредственного обнаружения у Учащегося любых инфекционных заболеваний, а также иных видов заболеваний, препятствующих обучению в Школе или создающих угрозу заражения других Учащихся и/или персонала Школы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.8. ", ordBoldFont));
                paragraph.add(new Phrase("при отсутствии учащегося более 3 (трех) дней на занятиях, представить справку о состоянии здоровья от участкового врача;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.9. ", ordBoldFont));
                paragraph.add(new Phrase("уведомлять Школу о наличии медицинских показаний для ограничения занятий Учащегося в рамках учебных планов Школы, также уведомлять Школу о наличии каких-либо ограничений в питании Учащегося и противопоказаний по применению медикаментов;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.10. ", ordBoldFont));
                paragraph.add(new Phrase("выполнять требования Устава Школы, приказы и распоряжения администрации Школы, соблюдать расписание занятий, медицинские предписания и санитарные нормы, строить свои отношения со Школой на основе взаимного уважения и такта, проявлять уважение к другим участникам образовательного процесса;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.11. ", ordBoldFont));
                paragraph.add(new Phrase("контролировать учебу Учащегося, выполнение им домашнего задания, поведение и посещаемость школы Учащимся;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.12. ", ordBoldFont));
                paragraph.add(new Phrase("по рекомендации Школы обеспечивать Учащегося сменной обувью, одеждой (формой), канцелярскими товарами, приобретать необходимые средства для его успешного обучения и развития;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.13. ", ordBoldFont));
                paragraph.add(new Phrase("присутствовать на запланированных встречах и собраниях с администрацией Школы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.14. ", ordBoldFont));
                paragraph.add(new Phrase("возмещать ущерб, причинённый Учащимся при небрежном его отношении к имуществу Школы (поломка мебели, роспись покрытия столов или парт, царапины на мебели, порчи полового покрытия, боя стекла, посуды, поломки зеленых насаждений и др. имущества, находящегося в собственности школы), а также имуществу любых третьих лиц, выраженный в оплате размера причиненного ущерба имуществу, либо приобрести новое;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.15. ", ordBoldFont));
                paragraph.add(new Phrase("не допускать наличия у Учащегося огнеопасных, токсичных, колющих и режущих, а также других опасных для жизни и здоровья предметов;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.16. ", ordBoldFont));
                paragraph.add(new Phrase("При получении учебника(ов) в библиотеке школы:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("\u00D8 ", new Font(Font.FontFamily.ZAPFDINGBATS, 10)));
                paragraph.add(new Phrase("до 10 июня учащийся обязан сдать учебник(и);", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("\u00D8 ", new Font(Font.FontFamily.ZAPFDINGBATS, 10)));
                paragraph.add(new Phrase("если учебник(и) сдается в неудовлетворительном состоянии, учебник(и) не принимается;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("\u00D8 ", new Font(Font.FontFamily.ZAPFDINGBATS, 10)));
                paragraph.add(new Phrase("при утере или порче книг или учебного или художественного фонда родители должны восстановить их полную рыночную стоимость либо приобрести новый экземпляр книги взамен утерянной;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2.17. ", ordBoldFont));
                paragraph.add(new Phrase("исполнять другие обязанности, предусмотренные настоящим Договором, действующим законодательством и внутренними актами Школы.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. ДИСТАНЦИОННОЕ ОБУЧЕНИЕ", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. ", ordBoldFont));
                paragraph.add(new Phrase("В случаях введения режима Чрезвычайного положения и/или Чрезвычайной ситуации и/или карантина и/или иных общеобязательных ограничительных мер, препятствующих оказанию всего комплекса образовательных услуг, Школа вправе в одностороннем порядке перевести Учащегося на режим дистанционного обучения (как частично, так и полностью).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. ", ordBoldFont));
                paragraph.add(new Phrase("Под дистанционным обучением, в рамках настоящего Договора, понимается обучение с использованием образовательных технологий, реализуемых с применением средств информационных и телекоммуникационных технологий при опосредованном или неполностью опосредованном взаимодействии обучающегося и педагогического работника Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. ", ordBoldFont));
                paragraph.add(new Phrase("Средства информационных и телекоммуникационных технологий, в том числе онлайн (дистанционная) платформа и/или программа, а также форма, порядок, расписание и методика образовательного процесса в период дистанционного обучения определяется Школой самостоятельно и на свое усмотрение.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. ", ordBoldFont));
                paragraph.add(new Phrase("В период дистанционного обучения Учащийся не посещает Школу, при этом Родитель обязан обеспечить Учащегося интернет связью, необходимым оборудованием (компьютер/ноутбук/планшет), а также доступом к определенной Школой онлайн (дистанционной) платформе (скачать и установить необходимую программу или приложение) по месту жительства Учащегося самостоятельно и за свой счет, а также самостоятельно обеспечить участие Учащегося в дистанционном обучение, в установленное расписанием время.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.5. ", ordBoldFont));
                paragraph.add(new Phrase("В период дистанционного обучения вычитывается только стоимость питания. При этом за первый месяц дистанционного обучения осуществляется перерасчет фактически внесенной ранее предоплаты, исходя из фактического количества рабочих дней в соответствующем месяце, в котором часть обучения осуществлялась в обычном режиме, а другая часть – в режиме дистанционного обучения.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. ОТВЕТСТВЕННОСТЬ", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. ", ordBoldFont));
                paragraph.add(new Phrase("За неисполнение или ненадлежащее исполнение своих обязательств Стороны несут ответственность, предусмотренную настоящим Договором и действующим законодательством Кыргызской Республики.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. ОСОБЫЕ УСЛОВИЯ", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. ", ordBoldFont));
                paragraph.add(new Phrase("Учащийся, получивший неудовлетворительную итоговую оценку в четверти (по одному или нескольким предметам), рассматривается дисциплинарной комиссией, которая принимает соответствующее решение. Комиссия также вправе принять решение об исключении Учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.2. ", ordBoldFont));
                paragraph.add(new Phrase("В случае намерения смены места жительства или перевода Учащегося в другое учебное заведение в течение года, Родитель должен поставить в известность об этом администрацию Школы не менее чем за 2 (два) месяца до этого.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.3. ", ordBoldFont));
                paragraph.add(new Phrase("При неуважительном отношении Родителей или Учащегося к работникам школы, проявлении агрессии Учащегося к другим ученикам, а также в случаях кражи, драки, актов вандализма со стороны Учащегося, инцидент рассматривается дисциплинарной комиссией, которая принимает соответствующее решение согласно нормам внутренних положений Школы. Комиссия также может принять решение об исключении Учащегося. В случае исключения Учащегося из Школы настоящий договор автоматически прекращается без дополнительных уведомлений и соглашения о расторжении.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.4. ", ordBoldFont));
                paragraph.add(new Phrase("При расторжении настоящего Договора по любым основаниям, все взаиморасчеты должны быть произведены сторонами в течение 5 (пяти) рабочих дней до даты расторжения настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.5. ", ordBoldFont));
                paragraph.add(new Phrase("Все вопросы по поводу воспитания и образования решаются с педагогами ", ordFont));
                paragraph.add(new Phrase("в отсутствии Учащегося и других учащихся.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.6. ", ordBoldFont));
                paragraph.add(new Phrase("В случае нахождения Учащегося свыше оговоренного в договоре времени Школа вправе пересмотреть условия Договора и повысить оплату обучения Учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.7. ", ordBoldFont));
                paragraph.add(new Phrase("Запрещено использование Учащимся сотового телефона и других средств интерактивной связи во время уроков.", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. ЗАКЛЮЧИТЕЛЬНЫЕ ПОЛОЖЕНИЯ", headerBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("8.1. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий Договор действует с даты его подписания, указанной в начале настоящего Договора до окончания учебного года.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.2. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий Договор может быть расторгнут по основаниям и в порядке, предусмотренным настоящим Договором и действующим законодательством.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.3. ", ordBoldFont));
                paragraph.add(new Phrase("Стороны договорились о том, что обмен информацией по официальной электронной почте по адресам, указанным в разделе 9 настоящего Договора, имеют юридическую силу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.4. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий Договор составлен на русском языке в двух идентичных экземплярах, имеющих равную юридическую силу, по одному экземпляру для каждой из Сторон", ordFont));
                document.add(paragraph);

                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setFirstLineIndent(0);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("9. АДРЕСА И РЕКВИЗИТЫ СТОРОН", headerBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));
                directorFullName = studentInfo.getDirector().getSurname();

                if (studentInfo.getDirector().getName() != null
                        && !studentInfo.getDirector().getName().isEmpty()) {
                    directorFullName += " " + studentInfo.getDirector().getName().charAt(0) + ".";
                }

                if (studentInfo.getDirector().getMiddle_name() != null
                        && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                    directorFullName += studentInfo.getDirector().getMiddle_name().charAt(0) + ".";
                }

                float[] table_info_colsWidth = {1f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.getDefaultCell().setPaddingRight(10f);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);

                // Реквизиты школы оставлены как в шаблоне договора.
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase("«Школа»:", ordBoldFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Учреждение " + studentInfo.getSchool().getName_ru(), ordBoldFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase(studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("тел: " + studentInfo.getSchool().getPhone().replace("<br>", "\n"), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор", ordBoldFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase(directorFullName + " ___________________", ordBoldFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("__________________________________", ordFont));
                table_info.addCell(text10);

                Paragraph text11 = new Paragraph();
                text11.add(new Phrase("«Родитель»:", ordBoldFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("(Ф.И.О.): " + parentFullName, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("(паспорт, ID карта): ", ordFont));
                text11.add(new Phrase(mainRelative == null || mainRelative.getPassport() == null
                        ? "__________________________" : mainRelative.getPassport(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("(выдан, дата): ", ordFont));
                text11.add(new Phrase(mainRelative == null || mainRelative.getPassport_issue_place() == null
                        ? "__________________" : mainRelative.getPassport_issue_place(), ordFont));
                text11.add(new Phrase(", ", ordFont));
                text11.add(new Phrase(mainRelative == null || mainRelative.getPassport_issue_date() == null
                        ? "____________" : Settings.df.format(mainRelative.getPassport_issue_date()), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("(место работы): ", ordFont));
                text11.add(new Phrase(mainRelative == null || mainRelative.getWork_place() == null
                        ? "__________________________" : mainRelative.getWork_place(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("(домашний адрес, телефон, e-mail): ", ordFont));
                text11.add(new Phrase(mainRelative == null || mainRelative.getAddress() == null
                        ? "__________________________" : mainRelative.getAddress(), ordFont));
                text11.add(new Phrase(", ", ordFont));
                text11.add(new Phrase(mainRelative == null || mainRelative.getPhone() == null
                        ? "__________________" : mainRelative.getPhone(), ordFont));
                text11.add(new Phrase(", __________________", ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("(телефон иного контактного лица): __________________", ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("(подпись): __________________", ordFont));
                table_info.addCell(text11);

                document.add(table_info);
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

        Font f_font;

        myPageEvent(Font f_font) {
            this.f_font = f_font;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();

                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_LEFT,
                        new Phrase("Школа _________________", f_font),
                        70f,
                        22f,
                        0
                );

                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_RIGHT,
                        new Phrase("Родитель ______________", f_font),
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
