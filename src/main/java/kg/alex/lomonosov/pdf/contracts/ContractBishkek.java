package kg.alex.lomonosov.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.dao.DbRelative;
import kg.alex.lomonosov.domain.StudentInfoPdf;
import kg.alex.lomonosov.i18n.Messages;
import kg.alex.lomonosov.utils.Decliner;
import kg.alex.lomonosov.utils.money.WritableSummRu;
import kg.alex.lomonosov.utils.money.WritableSummRuSOM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ContractBishkek {

    static final Logger logger = LogManager.getLogger(ContractBishkek.class);
    private final static String FONT_LOCATION = "/home/lomonosov/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/lomonosov/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractBishkek(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 30, 30);
                document.setMargins(10, 10, 30, 30);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 11);
                Font ordBoldFont = new Font(baseFontBold, 11);
                Font ordBoldUnderlinedFont = new Font(baseFontBold, 11, Font.UNDERLINE);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Договор № "
                                   + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), ordBoldFont));
                spr.add(Chunk.NEWLINE);

                spr.add(new Phrase("на оказание образовательных услуг", ordBoldFont));
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
                table_date.addCell(new Phrase("г. " + studentInfo.getSchool().getCity(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Decliner dcl = new Decliner();

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Образовательное Учреждение Научно-образовательный комплекс «Школа Ломоносова» в лице Президента образовательного учреждения ", ordFont));
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

                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(", действующей на основании Устава, далее по тексту «Школа», с одной стороны, и ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName() + ", ", ordBoldFont));
                paragraph.add(new Phrase("далее по тексту «Родитель», с другой стороны, который является законным представителем ", ordFont));
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
                paragraph.add(new Phrase(" зачисленного (ой) в ", ordFont));
                paragraph.add(new Phrase(studentInfo.getStudent().getClass_name() + " класс", ordBoldFont));
                paragraph.add(new Phrase(", именуемые в дальнейшем «Стороны», заключили настоящий Договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.setFirstLineIndent(0);
                paragraph.clear();
                paragraph.add(new Phrase("1. Предмет Договора", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Предметом договора является организация обучения по образовательному процессу, соответствующая основной общеобразовательной программе начального общего, основного общего, среднего общего (нужное подчеркнуть) образования.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. Полная информация об обучающемся:\nдата рождения: "
                                         + Settings.dateRu.format(studentInfo.getStudent().getBirth_date())
                                         + "\nадрес места жительства: "
                                         + studentInfo.getStudent().getAddress(), ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.3. Школа и Родители в целях создания необходимых условий для обучения и разностороннего развития личности ребёнка и творческой деятельности учителя, признавая необходимость сотрудничества, согласия, уважения, обязуются:\n" +
                                         "- сотрудничать, соблюдая законодательство Кыргызской Республики, Устав Школы, внутренние локальные акты школы и настоящий договор;\n" +
                                         "- поддерживать инициативу по совершенствованию обучения и воспитания путём взаимодействия;\n" +
                                         "- принимать необходимые меры, ограждающие педагогических работников и администрацию школы от необоснованного вмешательства в их профессиональную и должностную деятельность;\n" +
                                         "- уважительно вести себя по отношению к участникам образовательного процесса, а также вспомогательному и обслуживающему персоналу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.4. Школа обязуется создать благоприятные условия для интеллектуального, нравственного, эмоционального и физического развития личности обучающегося, всестороннего развития его способностей, гарантирует защиту прав и свобод личности обучающихся.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("2. Обязанности Сторон", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Школа обязана:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. Зачислить Обучающегося в соответствии с Правилами приёма в школу для вновь поступающих или в соответствии с Правилами перевода в следующий класс для продолжающих обучение в школе и предоставить услуги согласно п. 1.1. настоящего Договора ", ordFont));
                paragraph.add(new Phrase(studentInfo.getYear().getPeriod(), ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. Организовать и обеспечить надлежащее предоставление образовательных услуг в соответствии с Учебным планом, разработанным и утверждённым Школой на основе Базисного учебного плана, Государственного стандарта общего образования, в соответствии с Конституцией КР, Законом КР «Об образовании», Государственными стандартами начального общего, основного общего и среднего общего образования, утверждёнными Приказами МОиН КР, иными государственными, региональными и ведомственными законодательными и нормативными актами, Уставом Учреждения НОК «Школа Ломоносова» и Конвенцией о правах ребёнка, принятой Генеральной Ассамблеей ООН.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. Предоставить для проведения образовательного процесса помещения, соответствующие действующим санитарным и гигиеническим требованиям, а также оборудование, соответствующее нормам и правилам, предъявляемым к образовательному процессу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.4. Обеспечивать Обучающегося набором учебников, входящих в перечень учебников, рекомендованных к использованию при реализации образовательных программ, утверждённого приказом МОиН КР и используемых Исполнителем, в количестве достаточным для освоения Обучающимся образовательной программы, указанной в п. 1.1.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.5. Обеспечивать в установленном законодательством КР порядке безопасность жизни и здоровья Обучающегося во время образовательного процесса.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.6. Создать условия для медицинского обслуживания Обучающегося в объёме лечебно-профилактических мероприятий, предусмотренных действующими санитарно-гигиеническими нормами и правилами СанПин и Методическим рекомендациям Минздрава КР.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.7. Обеспечить Обучающегося с понедельника по пятницу 3-х разовым питанием, согласно Распорядка дня и действующим Санитарным правилам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.8. Сохранить в течение текущего учебного года место за Обучающимся в случае его пропуска занятий по уважительным причинам при условии своевременной оплаты этого периода согласно настоящему Договору.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.9. Через классного руководителя своевременно, не реже одного раза в четверть, информировать Родителя об успехах и проблемах Обучающегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.10. Информировать Родителя об изменениях в образовательном процессе через классного руководителя, с помощью электронной рассылки и иными способами, принятыми в школе.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.11. (данный пункт применяется только для 9 и 11 классов) Обучающемуся, завершившему полный курс обучения и успешно прошедшему государственную итоговую аттестацию, выдать документ государственного образца.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.12. По желанию Родителя или по рекомендации педагогического совета школы перевести Обучающегося на иную форму обучения при наличии возможности, заключив Дополнительное соглашение, которое после подписания Сторонами является неотъемлемой частью настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.13. По поручению и желанию Родителя при наличии возможности обеспечить участие Обучающегося в образовательных, социально-культурных и оздоровительных мероприятиях, организованных сторонними учреждениями.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.14. Письменно известить Родителя об изменении своего адреса, наименования, контактных телефонов, банковских реквизитов школы в течение десяти рабочих дней с момента такого изменения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2 Родитель обязан:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. При заключении Договора предоставить: заявление о приёме в школу, свидетельство о рождении (и/или паспорт) Обучающегося, ИНН Обучающегося, копии паспортов одного из родителей, законных представителей (стороны по договору), личное дело Обучающегося с предыдущего места обучения (для 2-го и следующих классов), медицинские карты по форме № 026 и форме № 63 c полными и достоверными сведениями о состоянии здоровья Обучающегося в течение 5 рабочих дней с даты начала действия настоящего Договора, а также информировать Исполнителя об имеющихся особенностях здоровья Обучающегося, которые могут повлиять на образовательный процесс. В случае непредоставления сведений (или предоставления недостоверных сведений) о здоровье Обучающегося ответственность за последствия несёт Родитель.\nНастоящий пункт является существенным условием договора и его невыполнение со стороны Родителя даёт Школе право приостановить оказание образовательных услуг Обучающемуся до полного выполнения настоящего пункта Родителем.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. Выполнять условия настоящего Договора и локальных нормативных актов школы, а также обеспечивать соблюдение Обучающимся Устава Учреждения Научно-образовательный комплекс «Школа Ломоносова», Правил поведения, Распорядка дня и других локальных нормативных актов школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. Нести ответственность за воспитание Обучающегося и его обучение вне школы. Выполнять все рекомендации Школы, касающиеся учебно-воспитательного процесса (рекомендации педсовета, психолога и пр.), предоставлять по запросу Школы отчёт о выполнении вышеуказанных рекомендаций. В случае неисполнения Родителем вышеуказанных рекомендаций, Школа не несёт ответственность за результат учебно-воспитательного процесса.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4. Обеспечить выполнение Обучающимся программы второй половины дня по расписанию, устанавливаемому на год и включающему модульные пред профильные курсы, проектную работу, самостоятельное выполнение домашних заданий, отработки и т. п.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.5. Создать необходимые условия для полноценного отдыха и занятий Обучающегося дома, в том числе, для выполнения домашних заданий различного вида.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.6. Ограничивать воздействие на Обучающегося тех социальных факторов, которые могут вредить психическому и физическому здоровью Обучающегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. Контролировать доступ Обучающегося в Интернете и его общение в социальных сетях. По рекомендации Школы ограничивать доступ Обучающегося в Интернете и его общение в социальных сетях.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.8. Своевременно (не позднее 08.00) доставлять и забирать (не позднее 17.00) из школы Обучающегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.9. Обеспечить посещение Обучающимся занятий в соответствии с Распорядком дня и Расписанием занятий. Обучающийся находится в школе с 08-00 до 17-00 часов. Режим работы школы: 5-дневная учебная неделя.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.10. В случае пропуска Обучающимся занятий по вине или инициативе Родителя, последний обязан:\n- заранее уведомить Школу о предстоящих пропусках занятий (с указанием причины и объёма пропусков) не позднее чем за 7 календарных дней;\n- в случае принятия Школой решения о проведении дополнительных занятий дистанционно/онлайн, обеспечить для Обучающегося условия для проведения таких занятий.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.11. Обеспечить участие Обучающегося в образовательных, развивающих, социально-культурных, оздоровительных и воспитательных мероприятиях, организуемых Школой и включённых в План работы Школы, с которым можно ознакомиться, обратившись к Школе.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.12. Ежедневно знакомиться с содержанием электронного журнала и Дневника как с основным источником информации для родителей об отметках и домашнем задании ученика. Информация, оставленная Школой в электронном журнале и Дневнике, считается полученной Родителем на следующий день после её размещения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.13. Своевременно знакомиться с содержанием дневника, поддерживать постоянную связь с классным руководителем и Администрацией школы и, по их обоснованной просьбе, в течение двух рабочих дней являться в школу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.14. В случае длительного (более 2-х недель) отсутствия Родителя в г. Бишкек последний обязан назначить доверенное лицо по исполнению настоящего Договора и заблаговременно, но не позднее чем за день до отъезда, письменно уведомить Школу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.15. В случае неявки Обучающегося в школу, своевременно (до 8.00 часов) извещать классного руководителя или дежурного администратора о болезни или других причинах отсутствия Обучающегося на занятиях.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.16. Телефонное общение с педагогами осуществлять в рабочие дни до 20-00. Звонить педагогу в выходные дни и после 20-00 на его личный мобильный телефон только в случае крайней необходимости.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.17. В первый день посещения Обучающимся школы после болезни предоставить справку из медицинского учреждения, разрешающую посещение школы. В случае непредоставления справки, разрешающей посещение Обучающимся школы, Школа вправе приостановить оказание образовательных услуг Обучающемуся и не допускать его на территорию школы либо не допускать в класс, при этом у Обучающегося фиксируется пропуск занятий по вине Родителя.\nВ этом случае Школа обязана незамедлительно уведомить о ситуации законных представителей Обучающегося и организовать его содержание на территории школы до приезда его законных представителей, прибытие которых Родитель обязуется обеспечить в течение 2-х часов.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.18. Производить оплату в порядке и в сроки, установленные настоящим Договором.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.19. В случае невозможности своевременного внесения оплаты, не менее чем за три дня до установленного срока оплаты письменно обратиться в Администрацию школы с просьбой о предоставлении отсрочки платежа не более чем на один месяц.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.20. Возмещать, в соответствии с законодательством КР, ущерб, причинённый Обучающимся во время образовательного процесса имуществу Школы или третьих лиц. Возмещение ущерба осуществляется в размере и в срок, устанавливаемые по договорённости Сторон.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.21. Осуществить оплату за учебно-методическую литературу по ", ordFont));
                paragraph.add(new Phrase("4000 (четыре тысячи) ", ordBoldFont));
                paragraph.add(new Phrase("сом в три платежа за 1-4, 5-8 и 9-11 классы. Указанная сумма составляет 30% от стоимости годового комплекта учебников, 70% финансируется школой. А также данная сумма покрывает амортизационные расходы на поддержание в должном состоянии учебников.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.22. Возмещать Школе стоимость утерянных Обучающимся учебных пособий и других книг из библиотечного фонда, выданных ему Школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.23. Не допускать в адрес Школы (сотрудников Школы) не соответствующие действительности высказывания, направленные на ознакомление с ними неопределённого круга лиц (в том числе, в печати и иных средствах массовой информации, на собраниях и в различных выступлениях), порочащие их честь, достоинство или деловую репутацию, в том числе содержащие утверждение о нарушении Школой (или её сотрудниками) действующего законодательства, совершении нечестного поступка, неправильном, неэтичном поведении в личной, общественной или политической жизни, недобросовестности при осуществлении ведущейся деятельности, нарушении деловой этики или обычаев делового оборота.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.24. Не проносить, без разрешения Школы, на территорию Школы кремовые кондитерские изделия (пирожные, торты и пр.) и прочие продукты и блюда, реализация которых в образовательных учреждениях не допускается в соответствии с СанПиН.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.25. Получить документы Обучающегося у Школы в течение 30 дней со дня истечения срока действия настоящего Договора или его досрочного расторжения, предоставив Школе справку о зачислении Обучающегося в другое учебное заведение.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.26. Письменно известить Школу об изменении своего контактного телефона и/или места жительства в течение пяти рабочих дней с момента изменения. Письменно уведомить Школу о досрочном расторжении договора не менее чем за 30 календарных дней до даты расторжения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.27. Принимать участие в опросах и анкетировании, проводимых Школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.28. Лично присутствовать на родительских собраниях, встречах с администрацией и получать информацию от сотрудников школы (для нянь, водителей, родственников информация в полном объёме не предоставляется).", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Права Школы и Родителя", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. Школа вправе:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.1. При наличии письменного заявления Родителя об отсрочке оплаты за обучение продолжить получение Обучающимся образовательных услуг на срок, согласованный с директором школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.2. При неисполнении или нарушении условий настоящего Договора Родителем расторгнуть настоящий Договор в одностороннем порядке, уведомив Родителя за 2 недели до даты предполагаемого расторжения и при необходимости обратиться в суд о взыскании задолженности.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.3. Самостоятельно выбирать, разрабатывать и внедрять в образовательный процесс новые программы, в том числе учебные, психологические и логопедические, способствующие повышению эффективности обучения, а также разрабатывать, утверждать и корректировать в течение учебного года Расписание занятий и Распорядок дня, периодичность проведения контролирующих учебных мероприятий.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.4. Самостоятельно определять:\nа) сроки каникул, дни занятий, продолжительность учебного дня и уроков, в соответствии с рекомендациями МОиН КР;\nб) форму и периодичность проведения родительских собраний, но не реже 2-х раз в течение учебного года;\nв) состав педагогического коллектива, а также самостоятельно назначать классного руководителя;\nг) персональный состав обучающихся в классах, количество классов в параллелях и перевод обучающегося из класса в класс.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.5. Организовывать факультативные занятия по основным предметам для успевающих Обучающихся.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.6. Рекомендовать организацию дополнительных занятий (в том числе, во время каникул) для неуспевающих Обучающихся на условиях, указанных во внутренних нормативных актах, распространяющих своё действие на данные отношения и доведённых до сведения Родителя.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.7. В процессе оказания образовательных услуг использовать систему видеонаблюдения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.8. По решению Педагогического Совета школы за совершение противоправных действий, грубые и неоднократные нарушения Устава Учреждения НОК «Школа Ломоносова» и Правил поведения, обучающихся в школе, в соответствии с Законом КР «Об образовании», применять, как крайнюю меру педагогического воздействия, исключение Обучающегося, достигшего возраста 15 лет, из школы. В случае, если Обучающийся не достиг 15-летнего возраста, школа имеет право его отчислить в другое учебное заведение с предоставлением справки со школы. В случаях рекомендации психолога об отстранении Обучающегося от посещения школы отправить на онлайн обучение.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.9. Требовать обязательное ношение Обучающимся школьной формы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.10. Проводить опросы и анкетирование Родителя с целью улучшения качества предоставляемых услуг.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.11. Проводить замену уроков. На уроках допускается проведение независимой диагностики и административных контрольных работ по любым предметам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2. Родитель вправе:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.1. Защищать законные права и интересы Обучающегося, требовать своего личного присутствия при рассмотрении всех возникающих проблем Обучающегося.\nПри возникновении во время образовательного процесса конфликтной ситуации между его ребёнком и другим обучающимся присутствовать при рассмотрении такой ситуации сотрудниками школы. Разрешать создавшуюся конфликтную ситуацию вправе исключительно педагоги Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.2. Получать от Школы:\nа) информацию о содержании образовательного процесса;\nб) сведения об успеваемости, поведении, а также отношении Обучающегося к учёбе в целом и по отдельным предметам Учебного плана.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.3. Вносить предложения по улучшению учебного процесса, работы школы и мероприятий, включённых в образовательный процесс. Присутствовать на учебных занятиях по предварительному согласованию с Администрацией школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.4. Вносить предложения по улучшению работы школы и мероприятий, включённых в образовательный процесс (медицинские услуги, питание, оздоровительные мероприятия и пр.).", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Срок действия Договора", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Договор вступает в силу и становится обязательным для Сторон с момента его подписания и действует по ", ordFont));
                paragraph.add(new Phrase(Settings.dateRu.format(studentInfo.getYear().getEnd_date()), ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2. После истечения срока действия настоящего Договора или при его досрочном расторжении Школа не несёт ответственности за жизнь, здоровье и возможные противоправные действия Обучающегося на территории школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.3. (данный пункт не применяется для 11 класса) В случае неисполнения или ненадлежащего исполнения Родителем условий оплаты по настоящему Договору, Родитель лишается преимущественного права на заключение Договора на следующий учебный год.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.4. В случае, если в течение учебного года Администрацией школы будет вынесено решение о невозможности продолжения обучения Обучающегося в Школе на общих основаниях в классе, то Школа вправе предложить Родителю о переводе Обучающегося на онлайн обучение.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Условия оплаты", ordBoldFont));
                document.add(paragraph);

                WritableSummRu convertToLetters = new WritableSummRuSOM();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Сумма оплаты по Договору (стоимость образовательных услуг) за " +
                                         studentInfo.getYear().getName() +
                                         " учебный год составляет ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) +
                                         " (" +
                                         convertToLetters.numberToString(studentInfo.getContractInfo().getContract()).trim() +
                                         ").", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Оплата образовательных услуг производится на расчётный счёт школы в соответствии с графиком платежей, указанном в Приложении №1 к настоящему договору.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. В случае, если платёж № 1 не произведён Родителем в полном объёме в течение 20 календарных дней с момента заключения настоящего Договора, данный договор считается не заключённым, и у Сторон не возникают обязательства по нему.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. Если день платежа приходится на нерабочий день, то сроком исполнения обязательства является первый рабочий день, следующий за днём платежа.\nВ случае просрочки установленных настоящим Договором срока платежей или частичной (неполной) оплаты от установленной Графиком платежа суммы, Родитель уплачивает Школе пеню в размере 0,1% от суммы задолженности за каждый календарный день просрочки, но не более размера общей суммы задолженности.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.5. В случае просрочки, денежные средства, поступившие Школе от Родителя, независимо от назначения платежа, засчитываются Школе в счёт уплаты:", ordFont));
                paragraph.add(new Phrase("\nв первую очередь ", ordBoldFont));
                paragraph.add(new Phrase("– задолженности по договору (договорам) на обучение ребёнка (детей) Родителя;", ordFont));
                paragraph.add(new Phrase("\nво вторую очередь ", ordBoldFont));
                paragraph.add(new Phrase("– начисленных пени;", ordFont));
                paragraph.add(new Phrase("\nв третью очередь ", ordBoldFont));
                paragraph.add(new Phrase("– текущих платежей.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.6. Школа оставляет за собой право изменять в течение срока действия Договора размер годовой оплаты по настоящему Договору с учётом уровня инфляции, но не более 15% от суммы годовой оплаты.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.7. Школа уведомляет Родителя о планируемых изменениях размера годовой оплаты по Договору за 1 месяц до очередного платежа.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.8. В случае досрочного расторжения настоящего Договора по инициативе Родителя, Школа оставляет за собой право удержать сумму родительского взноса за текущую учебную четверть, независимо от даты подачи заявления о расторжении. При этом скидки, предоставленные на основании условий настоящего Договора, не учитываются при расчёте суммы возврата. Возврат оставшейся суммы родительского взноса, за вычетом оплаты за текущую четверть, производится в течение 60 (шестидесяти) календарных дней со дня подписания соглашения о расторжении или получения заявления от Родителя, при условии отсутствия задолженности перед Школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.9. Школа может предоставить скидки на данный учебный год по следующим основаниям:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.9.1. В случае обучения в Школе в текущем учебном году 2-х или 3-х и более несовершеннолетних детей, законным представителем которых является Родитель.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.9.2. В случае наличия у обучающихся в годовых оценках предыдущего учебного года по всем предметам Учебного Плана оценки «отлично» - «5» и отсутствия письменных дисциплинарных взысканий.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.9.3. При предоплате, сделанной Родителями за полгода вперёд.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.9.4. В случае внесения оплаты за весь учебный год.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.9.5. В рамках социального пакета.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.10. На каждый последующий учебный год заключается дополнительное соглашение к настоящему Договору с установлением суммы оплаты.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Условия расторжения Договора", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. Досрочное расторжение настоящего Договора возможно в следующих случаях:\n1) по взаимному соглашению Сторон;\n2) по заявлению Родителя;\n3) по инициативе одной из Сторон по основаниям, предусмотренным действующим законодательством КР.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.2. Помимо этого, Школа вправе отказаться от исполнения настоящего Договора после соответствующего предупреждения в случае:\n1) если Родитель нарушил срок оплаты услуг по Договору более чем на 2 месяца;\n2) отчисления Обучающегося, достигшего возраста пятнадцати лет из школы по решению Школы, возможно за:\n- неоднократное совершение дисциплинарных проступков (неисполнение или нарушение Устава Школы, правил внутреннего распорядка Обучающихся и иных локальных нормативных актов по вопросам организации и осуществления образовательной деятельности),\n- если у Обучающегося обнаружены наркотические либо психотропные средства, спиртосодержащая продукция,\n- при фиксации факта распространения (передачи другому) Обучающимся наркотических либо психотропных средств, спиртосодержащей продукции,\n- если зафиксирован факт нахождения Обучающегося в состоянии алкогольного или наркотического опьянения на территории Школы.\nОтчисление обучающегося применяется, если иные меры дисциплинарного взыскания и меры педагогического воздействия не дали результата и дальнейшее его пребывание в школе оказывает отрицательное влияние на других обучающихся, нарушает их права и права работников Школы, а также нормальное функционирование Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6.3. Договор на оказание образовательных услуг считается расторгнутым по истечении 14 календарных дней со дня письменного уведомления администрацией школы Родителя об отказе от исполнения настоящего Договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. Особые условия", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. Настоящий Договор составлен в 2-х экземплярах, имеющих равную юридическую силу, по одному для каждой из Сторон.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.2. Все приложения к настоящему Договору являются его неотъемлемыми частями.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.3. Изменения и дополнения к настоящему Договору совершаются в письменной форме и подписываются обеими сторонами.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.4. Подписывая настоящий Договор, Родитель даёт согласие школе на обработку (включая получение от него и/или от любых третьих лиц, с учётом требований действующего законодательства КР) его персональных данных, а также персональных данных Обучающегося.\nСогласие на обработку персональных данных предоставляется на осуществление любых действий в отношении персональных данных Родителя и Обучающегося, которые необходимы или желаемы для нормальной работы Школы.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. Адреса, банковские реквизиты и подписи Сторон", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.5f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase("Школа: " + studentInfo.getSchool().getName_ru(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Адрес: " + studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                text10.add(Chunk.NEWLINE);
                String[] banks = studentInfo.getSchool().getBank().split("<br>");
                String[] bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");
                for (int i = 0; i < banks.length; i++) {
                    text10.add(new Phrase("Банк: " + banks[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Р/счет: " + bankAccounts[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                }
                text10.add(new Phrase("Тел.: " + studentInfo.getSchool().getPhone(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор Школы: " + studentInfo.getDirector().getSurname() + " "
                                      + studentInfo.getDirector().getName() + " " +
                                      (studentInfo.getDirector().getMiddle_name() == null ?
                                              "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);

                IndexedContainer relativeCont = null;
                table_info.addCell(text10);
                try {
                    DbRelative dbr = new DbRelative();
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
                String m_name = "";
                String passport = "";
                while (iter != null && iter.hasNext()) {
                    Object obj = iter.next();
                    if ((Integer) obj == 1) {
                        f_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.FullName)).getValue().toString();
                    }
                    if ((Integer) obj == 2) {
                        m_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.FullName)).getValue().toString();

                    }
                    if ((Integer) relativeCont.getContainerProperty(obj,
                            Settings.is_main).getValue() == 1) {
                        text18.add(new Phrase("Контактный тел: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Phone)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Адрес места жительства: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Address)).getValue().toString(), ordFont));
                        passport = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Passport)).getValue().toString();
                    }
                }
                text11.add(new Phrase("Ф.И.О. отца: " + f_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Ф.И.О. матери: " + m_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Данные паспорта: ", ordFont));
                text11.add(new Phrase(passport, ordFont));
                text11.add(text18);
                table_info.addCell(text11);
                table_info.addCell(new Phrase(" (М.П)", ordFont));
                table_info.addCell(new Phrase("Подпись отца: ________________", ordFont));
                table_info.addCell(new Phrase(" ", ordFont));
                table_info.addCell(new Phrase("Подпись матери: ________________", ordFont));

                document.add(table_info);
                document.add(new Paragraph(10, " "));

                Paragraph text15 = new Paragraph();
                text15.setIndentationLeft(30);
                text15.setIndentationRight(30);
                text15.add(new Phrase("График оплаты", ordBoldFont));
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
                text15.add(new Phrase("ИТОГО взноса: ", ordFont));
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) + ""), ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Долг с предыдущего года: ", ordFont));
                } else {
                    text15.add(new Phrase("Переплата с предыдущего года: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + ""), ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Скидка: ", ordFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
                    text15.add(new Phrase(" (вид скидки прописью, %)", ordFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Корректировка: ", ordFont));
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Предоплата: ", ordFont));
                text15.add(new Phrase(studentInfo.getContractInfo().getInitialPayment() == null ? "0.00" :
                        Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + "", ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Остаток: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + "", ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                document.add(text15);
                document.add(new Paragraph(10, " "));

                Paragraph text16 = new Paragraph();
                text16.setIndentationLeft(30);
                text16.setIndentationRight(30);
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
                TContract.addCell(new Phrase("Потверждающий документ", ordBoldFont));
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
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + "", ordBoldFont));
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("", ordFont));

                document.add(TContract);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Подпись Родителя: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(new Phrase("Директор: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(new Phrase("Печать ", ordBoldFont));
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
