package kg.alex.kelechek.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.dao.DbStudentRelative;
import kg.alex.kelechek.domain.StudentInfoPdf;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.utils.Decliner;
import kg.alex.kelechek.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Iterator;

public class ContractPdfRu {

    static final Logger logger = LogManager.getLogger(ContractPdfRu.class);
    private final static String FONT_LOCATION = "/home/kelechek/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/kelechek/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractPdfRu(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
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
                Font font_header = new Font(baseFontBold, 11);

                document.open();
                PdfContentByte punder = writer.getDirectContentUnder();
                Decliner dcl = new Decliner();

                document.add(new Paragraph(10, " "));
                Paragraph spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("между школой «Академия Будущих Лидеров»", font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("и родителями (законными представителями) учащегося", font_header));
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
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Школа «Академия Будущих Лидеров», в лице директора ", ordFont));
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
                paragraph.add(new Phrase("действующего на основании Устава, именуемое в дальнейшем «Школа», и ", ordFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName() + " - " + studentInfo.getMainRelative().getRelativeTitle() + " ", ordBoldFont));
                paragraph.add(new Phrase(" , именуемый в дальнейшем «Родитель» Учащегося ", ordFont));

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
                paragraph.add(new Phrase(
                        " (дата рождения: "
                                + Settings.df.format(studentInfo.getStudent().getBirth_date())
                                + ", класс зачисления: "
                                + studentInfo.getStudent().getClass_name() + ") ",
                        ordFont));
                paragraph.add(new Phrase("с другой стороны, вместе далее по тексту именуемые «Стороны», а по отдельности «Сторона», заключили настоящий Договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. ", ordBoldFont));
                paragraph.add(new Phrase("Стороны настоящего Договора объединяют усилия в обучении, воспитании и развитии Учащегося, обеспечении самоопределения его личности, создании условий для самореализации, формирования человека и гражданина, интегрированного в современное общество и нацеленного на совершенствование этого общества, создании условий для формирования у него компетентности адекватной современному уровню знаний и уровню образовательной программы (ступени обучения).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. ", ordBoldFont));
                paragraph.add(new Phrase("«Школа» и «Родитель» совместно несут полную ответственность за результат своей деятельности в пределах компетенции, разграниченной настоящим договором.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ ШКОЛЫ", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1 Школа имеет право:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. ", ordBoldFont));
                paragraph.add(new Phrase("В случае необходимости использовать персональные данные Учащегося в интернете и печатной продукции (размещение на школьном сайте, образовательных журналах, баннерах, листовках и т. д.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. ", ordBoldFont));
                paragraph.add(new Phrase("По своему усмотрению решать вопросы о приеме, переводе в следующий класс, применение дисциплинарных мер в отношении Учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. ", ordBoldFont));
                paragraph.add(new Phrase("На отчисление Учащегося в случаях:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- регулярного пропуска занятий Учащимся без уважительных причин;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- неоднократного нарушения Учащимся дисциплины в школе, а также в случае нарушения им Правил внутреннего распорядка и иных локальных нормативных актов, регламентирующих образовательную, воспитательную и административную деятельность Школы. В этом случае Учащийся теряет право на повторную регистрацию;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- неудовлетворительных экзаменационных оценках;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- несвоевременной оплате услуг, предоставляемых Школой, до окончания месяца, следующего за соответствующим месяцем, в котором должна быть произведена оплата.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.4. ", ordBoldFont));
                paragraph.add(new Phrase("Изменять методы и способы обучения, а также используемый материал, не противоречащие законодательству Кыргызской Республики, в целях повышения качества образования без согласия Родителей (законных представителей) Учащегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.5. ", ordBoldFont));
                paragraph.add(new Phrase("Не предоставлять Учащемуся табели оценок, дневники и прочую документацию в случае несвоевременной оплаты услуг, предоставляемых Школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.6. ", ordBoldFont));
                paragraph.add(new Phrase("Разрабатывать образовательные программы Школы и учебные планы для их реализации.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.7. ", ordBoldFont));
                paragraph.add(new Phrase("Проводить педагогическую диагностику с целью мониторинга качества обученности учащихся.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.8. ", ordBoldFont));
                paragraph.add(new Phrase("Устанавливать режим работы Школы (расписание занятий, продолжительность учебной недели и т. д.)", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.9. ", ordBoldFont));
                paragraph.add(new Phrase("Формировать профильные классы в старшей школе, основываясь на потребности учащихся и возможности педагогического коллектива.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.10. ", ordBoldFont));
                paragraph.add(new Phrase("Требовать от родителей обучающихся контроля за обучением и поведением ребенка, выполнения ими Правил внутреннего распорядка и иных локальных нормативных актов, регламентирующих образовательную, воспитательную и административную деятельность Школы и настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.11. ", ordBoldFont));
                paragraph.add(new Phrase("Поощрять учащихся или применять меры дисциплинарного взыскания в соответствии с Уставом Школы и Правилами внутреннего распорядка.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.12. ", ordBoldFont));
                paragraph.add(new Phrase("Осуществлять иные права, предоставленные действующим законодательством и Уставом Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2 Школа обязуется:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. ", ordBoldFont));
                paragraph.add(new Phrase("Производить обучение и воспитание Учащегося в классе по программам общего образования в 2026-2027 учебном году в очной форме.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. ", ordBoldFont));
                paragraph.add(new Phrase("Ознакомить Родителя с учредительными документами Школы, лицензией на образовательную деятельность, свидетельством о государственной регистрации, основными и дополнительными образовательными программами, учебным планом, годовым календарным учебным графиком, расписанием занятий, правилами внутреннего распорядка и иными документами, регламентирующими образовательную и воспитательную деятельность Школы, а также не менее чем за 3 рабочих дня информировать Родителей о проведении родительских собраний и иных школьных мероприятий, в которых Родители обязаны или имеют право принимать участие.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. ", ordBoldFont));
                paragraph.add(new Phrase("Ознакомить Родителя с ходом и содержанием учебного процесса, с учебной программой Учащегося на текущий учебный год, включающей в себя указание всех предметов, количества часов в неделю по каждому предмету, а также перечень дополнительных занятий, распорядок дня Учащегося в Школе. Данная учебная программа содержится в утвержденном администрацией Школы учебном плане, который хранится в Школе.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить Учащемуся приобретение знаний, умений и навыков в объеме общего образования, с выдачей, при условии успешной сдачи выпускной итоговой аттестации, аттестата государственного образца.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.5. ", ordBoldFont));
                paragraph.add(new Phrase("Осуществлять обучение по образовательным программам, обеспечивающим усвоение государственного образовательного стандарта школьного образования.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.6. ", ordBoldFont));
                paragraph.add(new Phrase("Предоставить обучающимся и их родителям (лицам их заменяющим) право выбора видов дополнительных (в том числе платных) услуг.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. ", ordBoldFont));
                paragraph.add(new Phrase("Оказывать услуги по присмотру и уходу за Учащимся во время его нахождения в Школе и/либо на проводимых Школой занятиях, осуществлять комплекс мер по организации питания и хозяйственно-бытового обслуживания Учащегося, обеспечению соблюдения им личной гигиены и режима дня.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.8. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить обучение и воспитание Учащегося квалифицированными педагогическими кадрами, предоставить индивидуальные консультации, факультативные и иные дополнительные занятия согласно учебному плану.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.9. ", ordBoldFont));
                paragraph.add(new Phrase("Осуществлять текущий и промежуточный контроль за успеваемостью и поведением обучающегося, в доступной форме информировать о его результатах Родителей и обучающегося.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.10. ", ordBoldFont));
                paragraph.add(new Phrase("Вести личное дело и документы по успеваемости Учащегося, в соответствии с принятыми стандартами школьного делопроизводства, предоставлять указанные документы Родителю на ознакомление по требованию последнего.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.11. ", ordBoldFont));
                paragraph.add(new Phrase("Предоставить Родителю соответствующие документы установленного образца при отчислении Учащегося (за исключением случаев, предусмотренных в п. 2.1.6 настоящего Договора).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.12. ", ordBoldFont));
                paragraph.add(new Phrase("Создать благоприятные условия для интеллектуального, нравственного и физического развития личности обучающегося, всестороннего развития его способностей, также гарантировать защиту прав и свобод личности Учащегося на территории Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.13. ", ordBoldFont));
                paragraph.add(new Phrase("Нести ответственность за жизнь и здоровье обучающегося на территории Школы во время образовательного процесса при условии выполнения ребенком правил внутреннего распорядка Школы. Школа не несет ответственности в случае, когда угроза жизни и здоровью Учащегося произошла по независящим от Школы обстоятельствам. Также школа обязуется соблюдать установленные санитарно-гигиенические нормы, правила и требования.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.14. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить охрану помещений.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.15. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить соблюдение техники безопасности на учебных занятиях и во время нахождения Учащегося в Школе.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.16. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить обучающихся классов трехразовым питанием.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.17. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить организацию охраны здоровья Учащегося (оказание первичной медико-санитарной помощи) в Школе, в том числе при реализации образовательных программ Школа обеспечивает: текущий контроль за состоянием здоровья обучающихся; проведение санитарно-гигиенических, профилактических и оздоровительных мероприятий, обучение и воспитание в сфере охраны здоровья; соблюдение государственных санитарно-эпидемиологических правил и нормативов.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.18. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить неразглашение сведений о личности и состоянии здоровья Учащегося и личных данных его Родителей, ставших известными Школе в соответствии с настоящим договором, за исключением случаев, когда предоставление таких сведений предусмотрено законодательством, кроме случаев, предусмотренных п.2.1.1 настоящего Договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("3. ПРАВА И ОБЯЗАННОСТИ РОДИТЕЛЯ", ordBoldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. Родитель имеет право:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.1. ", ordBoldFont));
                paragraph.add(new Phrase("Знакомиться с личным делом Учащегося, оценками по всем изучаемым предметам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.2. ", ordBoldFont));
                paragraph.add(new Phrase("Обращаться к классному руководителю, администрации Школы, педагогическому совету для разрешения конфликтных ситуаций, связанных с ребенком.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.3. ", ordBoldFont));
                paragraph.add(new Phrase("Вносить предложения по улучшению образовательной деятельности Школы и организации дополнительных образовательных услуг.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.4. ", ordBoldFont));
                paragraph.add(new Phrase("Выбирать дополнительные платные образовательные услуги из перечня, предлагаемого образовательным учреждением, также участвовать в определении набора дополнительных услуг, предусмотренных для учащихся данного класса.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.5. ", ordBoldFont));
                paragraph.add(new Phrase("Осуществлять иные права, предоставленные действующим законодательством и Уставом Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2. Родитель обязуется:", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.1. ", ordBoldFont));
                paragraph.add(new Phrase("Приобрести учебные принадлежности согласно списку, предоставленного Школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.2. ", ordBoldFont));
                paragraph.add(new Phrase("Оплатить стоимость услуги до 5 - числа каждого месяца. В случае оплаты позже указанной даты Школа оставляет за собой право применения штрафных санкций пени в размере 0,1% от общей суммы за обучение за каждый день просрочки).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.3. ", ordBoldFont));
                paragraph.add(new Phrase("В случае перевода либо исключения ребенка из школы по собственной инициативе, Родитель обязуется письменно уведомить администрацию Школы в срок не позднее 14 календарных дней. В противном случае Учащийся будет числиться в Школе, даже если он не посещает занятия, до момента официального письменного уведомления со стороны Родителя в адрес администрации Школы. При данных обстоятельствах Родитель обязуется оплатить услуги Школы в полном объеме.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.4. ", ordBoldFont));
                paragraph.add(new Phrase("Заниматься воспитанием Учащегося в соответствии с концепцией школы, заботиться о его физическом развитии, создать необходимые условия для получения детьми образования.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.5. ", ordBoldFont));
                paragraph.add(new Phrase("Не нарушать морально-этических норм при нахождении на территории Школы, при общении с администрацией Школы и ее сотрудниками, при общении с иными учащимися Школы и их представителями.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.6. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечивать соблюдение Учащимся Устава Школы, правил нахождения в Школе.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.7. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить посещение Учащимся занятий согласно учебному расписанию и иных школьных мероприятий, предусмотренных документами, регламентирующими образовательную и воспитательную деятельность Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.8. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить выполнение Учащимся устных домашних заданий.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.9. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечить Учащегося предметами, необходимыми для участия в образовательном процессе (письменно-канцелярскими принадлежностями и т. П.), в количестве, соответствующем его возрасту и потребностям;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.10. ", ordBoldFont));
                paragraph.add(new Phrase("Обеспечивать своевременную явку Учащегося на занятия, своевременно забирать Учащегося из Школы не позднее 17.15 часов.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.11. ", ordBoldFont));
                paragraph.add(new Phrase("Письменно информировать администрацию и педагогических работников (классного руководителя) о лицах, которым может быть доверен ребенок по окончании учебного дня, о телефонах для связи с Родителем в течение учебного дня.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.12. ", ordBoldFont));
                paragraph.add(new Phrase("Посещать родительские собрания, по приглашению встречаться с администрацией и педагогами Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.13. ", ordBoldFont));
                paragraph.add(new Phrase("Возместить ущерб в кратчайший срок в случае нанесения Учащимся материального ущерба имуществу Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.14. ", ordBoldFont));
                paragraph.add(new Phrase("Не допускать наличия у Учащегося на территории Школы огнеопасных, токсичных, колющих и режущих, а также других опасных для жизни и здоровья предметов, продуктов питания и напитков (сигарет, спичек, зажигалок, ножей, опасных игрушек, лекарственных средств, газированных напитков, чипсов, жевательной резинки и т.д.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.15. ", ordBoldFont));
                paragraph.add(new Phrase("Требовать от Учащегося толерантного отношения к другим Учащимся и педагогам, не допускать употребления нецензурных слов и националистических оскорблений.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.16. ", ordBoldFont));
                paragraph.add(new Phrase("Своевременно ставить в известность Школу о болезни ребёнка или возможном его отсутствии с последующим предоставлением справки с медицинского учреждения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.17. ", ordBoldFont));
                paragraph.add(new Phrase("Не допускать посещение Школы Учащимся в случае обнаружения у него заболеваний, создающих угрозу заражения остальных обучающихся и персонала Школы, также информировать Школу об изменениях в физическом и психическом состоянии Учащегося, препятствующих обучению и воспитанию в Школе.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.18. ", ordBoldFont));
                paragraph.add(new Phrase("Своевременно уведомлять Школу о наличии медицинских показаний по применению медикаментов, ограничении в питании, а также ограничении занятий Учащегося в рамках учебных планов.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. СТОИМОСТЬ ОБРАЗОВАНИЯ И ПОРЯДОК РАСЧЕТОВ", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. ", ordBoldFont));
                paragraph.add(new Phrase("Стоимость услуг, предоставляемых Школой в 2026-2027 учебном году, по настоящему Договору составляет: 455 000 (четыреста пятьдесят пять тысяч) сомов в год.", ordFont
                ));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.2. ", ordBoldFont));
                paragraph.add(new Phrase("Скидка на оплату обучения устанавливается в следующих случаях и размерах:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("_____________________", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- при оплате полной стоимости за год обучения Учащегося предоставляется скидка в размере 10% (пять) процентов от стоимости услуг, предусмотренных п. 4.1. настоящего договора;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- при регистрации более чем одного ребенка в данной Школе, за оплату обучения предоставляется скидка в размере 5% (пять) процентов от стоимости услуг, предусмотренных п. 4.1. настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- скидки, предоставляемые Родителю в рамках настоящего договора, не подлежат суммированию. В случае наличия у Родителя права на получение нескольких скидок одновременно, применяется только одна скидка по выбору Родителя.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.3. ", ordBoldFont));
                paragraph.add(new Phrase("Стоимость услуг, предоставляемых Школой в 2026-2027 – учебном году, по настоящему Договору после применения всех соответствующих скидок составляет ____________________ (_______________________________________________) сом в год.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.4. ", ordBoldFont));
                paragraph.add(new Phrase("В случае невнесения Родителем полной стоимости за год обучения Учащегося в сроки, указанные в п. 4.5 настоящего Договора, Школа вправе отменить скидку, предусмотренную п. 4.2. настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.5. ", ordBoldFont));
                paragraph.add(new Phrase("В случае невнесения Родителем оплаты услуг (в полном размере, по частям), предусмотренных п. 4.3. настоящего договора, в течение 30 (тридцати) календарных дней с момента подписания настоящего Договора обеими сторонами, Школа вправе расторгнуть Договор в одностороннем порядке.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.6. ", ordBoldFont));
                paragraph.add(new Phrase("Стоимость услуг, указанная в п. 4.1. настоящего Договора, может быть изменена в случае существенного изменения условий функционирования Школы (роста уровня заработной платы работников общего образования, изменения размера коммунальных платежей, цены работ и услуг сторонних организаций, которые обеспечивают образовательный процесс по настоящему Договору, роста иных подобных затрат Школы, введения дополнительных обязанностей по уплате налогов и сборов).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.7. ", ordBoldFont));
                paragraph.add(new Phrase("Об изменении стоимости услуг и ее причинах Школа обязана предупредить Родителя в срок не позднее 30 календарных дней до предполагаемого изменения стоимости услуг по Договору. В случае несогласия Родителя с изменением размера стоимости услуг по настоящему Договору он вправе отказаться от исполнения Договора. При этом Родитель обязан оплатить ранее оказанные по Договору услуги.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.8. ", ordBoldFont));
                paragraph.add(new Phrase("Родитель оплачивает стоимость услуг, указанную в п.4.1. настоящего Договора, согласно графику оплаты, утвержденного Школой.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.9. ", ordBoldFont));
                paragraph.add(new Phrase("Отсутствие Учащегося на уроках по какой-либо причине не влияет на стоимость услуг.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.10. ", ordBoldFont));
                paragraph.add(new Phrase("Оплата услуг по Договору производится безналичными или наличными платежами по указанным в Договоре реквизитам.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.11. ", ordBoldFont));
                paragraph.add(new Phrase("В случае досрочного расторжения настоящего договора в течение учебного года по инициативе любой из Сторон, Сторона, инициирующая расторжение, обязана уведомить другую Сторону в письменной форме не позднее чем за 30 (тридцать) календарных дней до предполагаемой даты расторжения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("-В случае расторжения договора по инициативе Родителя, Родитель обязуется уведомить Школу в установленный срок и произвести оплату услуг Школе за фактически оказанный период обучения, включая период уведомления (30 календарных дней), независимо от фактического посещения занятий Учащимся.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("-В случае расторжения договора по инициативе Школы на основании пункта 2.1.3 настоящего договора, Школа вправе в одностороннем порядке потребовать немедленного расторжения договора без соблюдения срока предварительного уведомления. В указанном случае договор считается расторгнутым с момента направления соответствующего уведомления Родителю.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4.13. ", ordBoldFont));
                paragraph.add(new Phrase("Размер увеличения стоимости обучения на следующий учебный период не может превышать 15% (пятнадцать процентов) от стоимости обучения, установленной на текущий учебный период", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. ОТВЕТСТВЕННОСТЬ СТОРОН", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. ", ordBoldFont));
                paragraph.add(new Phrase("Стороны настоящего Договора несут установленную действующим законодательством Кыргызской Республики ответственность за ненадлежащее исполнение принятых на себя обязательств.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. ", ordBoldFont));
                paragraph.add(new Phrase("В случае нарушения сроков оплаты услуг по настоящему Договору Родитель уплачивает Школе неустойку в размере 0,1% от суммы просроченного платежа за каждый день просрочки.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. ", ordBoldFont));
                paragraph.add(new Phrase("В случае отказа Родителя уплатить неустойку Школа вправе не допускать до занятий Учащегося вплоть до ее погашения.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. КОНФИДЕНЦИАЛЬНОСТЬ", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. ", ordBoldFont));
                paragraph.add(new Phrase("Родитель обязан не разглашать в той либо иной форме сведения конфиденциального характера, сведения, составляющих коммерческую тайну по отношению к Школе, а также положения настоящего Договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. ПРЕКРАЩЕНИЕ И ИЗМЕНЕНИЕ ДОГОВОРА", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. ", ordBoldFont));
                paragraph.add(new Phrase("Договор вступает в силу с момента его подписания обеими сторонами.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7.2. ", ordBoldFont));
                paragraph.add(new Phrase("В случае одностороннего расторжения Договора одна из сторон (инициатор расторжения Договора) обязана предупредить другую сторону о досрочном расторжении договора за 14 календарных дней, за исключением случаев, предусмотренных п. 4.5. настоящего Договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. ЗАКЛЮЧИТЕЛЬНЫЕ ПОЛОЖЕНИЯ", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("8.1. ", ordBoldFont));
                paragraph.add(new Phrase("Все дополнения и изменения к настоящему Договору совершаются в письменной форме и подписываются Сторонами. ", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.2. ", ordBoldFont));
                paragraph.add(new Phrase("Настоящий Договор составлен в двух экземплярах, имеющих одинаковую юридическую силу на русском языке, принятыми сторонами рабочим языком, по одному экземпляру для каждой из сторон.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8.4. ", ordBoldFont));
                paragraph.add(new Phrase("Место нахождения, банковские реквизиты, паспортные данные и места жительства сторон:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Школа\t\t\t\t\t\t\tРодитель (законный представитель)", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Перечень лиц кому может быть передан Учащийся", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Экземпляр договора с родителями (лицами, их заменяющими) на руки получил _______________________________________________ _______________________________подпись расшифровка подпись", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Я внимательно прочитал, понял и принял вышеупомянутые статьи настоящего Договора. Я согласен и обязуюсь платить возможные штрафы, оплату за обучение и убытки, которые могут возникнуть у Учащегося. Я принимаю решение, которое школа примет в отношении моего ребенка в результате неспособности связаться со мной. _____________________________подпись расшифровка подписи.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("9. РЕКВИЗИТЫ СТОРОН", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));


                PdfPTable requisitesTable = new PdfPTable(2);
                requisitesTable.setWidthPercentage(85f);
                requisitesTable.setHorizontalAlignment(Element.ALIGN_CENTER);
                requisitesTable.setWidths(new float[]{1f, 1f});
                requisitesTable.setSplitLate(false);
                requisitesTable.setKeepTogether(true);

                String schoolName = "ОсОО «Академия будущих лидеров»";

                String schoolAddress = studentInfo.getSchool().getAddress() == null
                        ? ""
                        : studentInfo.getSchool().getAddress();

                String schoolInn = studentInfo.getSchool().getInn() == null
                        ? ""
                        : studentInfo.getSchool().getInn();

                String schoolBankAccount = studentInfo.getSchool().getBank_account() == null
                        ? ""
                        : studentInfo.getSchool().getBank_account();

                String schoolBik = studentInfo.getSchool().getBik() == null
                        ? ""
                        : studentInfo.getSchool().getBik();

                String schoolBank = studentInfo.getSchool().getBank() == null
                        ? ""
                        : studentInfo.getSchool().getBank();

                String schoolPhone = studentInfo.getSchool().getPhone() == null
                        ? ""
                        : studentInfo.getSchool().getPhone();

                String directorFullName =
                        (studentInfo.getDirector().getSurname() == null
                                ? ""
                                : studentInfo.getDirector().getSurname())
                                + " "
                                + (studentInfo.getDirector().getName() == null
                                ? ""
                                : studentInfo.getDirector().getName())
                                + " "
                                + (studentInfo.getDirector().getMiddle_name() == null
                                ? ""
                                : studentInfo.getDirector().getMiddle_name());

                directorFullName = directorFullName.trim();

                String bankInn = "02712199110068";
                String bankRecipientAddress =
                        "г Бишкек, г Бишкек, ул. Т.Молдо, 54-А";


                String studentFullName = buildFullName(
                        studentInfo.getStudent().getSurname(),
                        studentInfo.getStudent().getName(),
                        studentInfo.getStudent().getMiddle_name()
                );

                String legalRepName = "";
                String legalRepPhone = "";
                String legalRepAddress = "";

                IndexedContainer relativeCont = null;

                try {
                    DbStudentRelative dbr = new DbStudentRelative();
                    dbr.connect();

                    relativeCont = dbr.execSQL(
                            myUI,
                            studentInfo.getStudent().getId()
                    );

                    dbr.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }

                if (relativeCont != null) {
                    Iterator<?> iterator = relativeCont.getItemIds().iterator();

                    while (iterator.hasNext()) {
                        Object itemId = iterator.next();

                        Object isMainValue = null;

                        if (relativeCont.getContainerProperty(itemId, Settings.is_main) != null) {
                            isMainValue = relativeCont
                                    .getContainerProperty(itemId, Settings.is_main)
                                    .getValue();
                        }

                        boolean isMain = false;

                        if (isMainValue instanceof Integer) {
                            isMain = ((Integer) isMainValue) == 1;
                        } else if (isMainValue instanceof Boolean) {
                            isMain = (Boolean) isMainValue;
                        } else if (isMainValue != null) {
                            isMain = "1".equals(isMainValue.toString())
                                    || "true".equalsIgnoreCase(isMainValue.toString());
                        }

                        if (isMain) {
                            legalRepName = getContainerValue(
                                    relativeCont,
                                    itemId,
                                    myUI.getMessage(Messages.FullName)
                            );

                            legalRepPhone = getContainerValue(
                                    relativeCont,
                                    itemId,
                                    myUI.getMessage(Messages.Phone)
                            );

                            legalRepAddress = getContainerValue(
                                    relativeCont,
                                    itemId,
                                    myUI.getMessage(Messages.Address)
                            );
                        }
                    }
                }

                Paragraph schoolRequisites = new Paragraph();
                schoolRequisites.setLeading(14f);

                schoolRequisites.add(new Phrase(
                        schoolName,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

                schoolRequisites.add(new Phrase(
                        schoolAddress,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);
                schoolRequisites.add(Chunk.NEWLINE);

                schoolRequisites.add(new Phrase(
                        "ИНН " + schoolInn,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

                schoolRequisites.add(new Phrase(
                        "Расчетный счет " + schoolBankAccount,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

                schoolRequisites.add(new Phrase(
                        "БИК банка " + schoolBik,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

// В БД такого поля нет
                schoolRequisites.add(new Phrase(
                        "ИНН банка " + bankInn,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

                schoolRequisites.add(new Phrase(
                        "Наименование банка получателя " + schoolBank,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

// В БД такого поля нет
                schoolRequisites.add(new Phrase(
                        "Адрес банка получателя " + bankRecipientAddress,
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

                schoolRequisites.add(new Phrase(
                        "Тел. " + schoolPhone.replace("<br>", "\n"),
                        ordFont
                ));
                schoolRequisites.add(Chunk.NEWLINE);

                schoolRequisites.add(new Phrase(
                        "Генеральный директор " + directorFullName,
                        ordFont
                ));

                PdfPCell schoolCell = new PdfPCell(schoolRequisites);

                schoolCell.setPaddingTop(5f);
                schoolCell.setPaddingBottom(6f);
                schoolCell.setPaddingLeft(8f);
                schoolCell.setPaddingRight(8f);
                schoolCell.setVerticalAlignment(Element.ALIGN_TOP);

                requisitesTable.addCell(schoolCell);

                PdfPTable parentTable = new PdfPTable(1);
                parentTable.setWidthPercentage(100f);

                PdfPCell clientTitleCell = new PdfPCell(
                        new Phrase("Клиент:", ordFont)
                );
                clientTitleCell.setBorder(Rectangle.NO_BORDER);
                clientTitleCell.setPadding(0f);
                clientTitleCell.setPaddingBottom(3f);

                parentTable.addCell(clientTitleCell);

                PdfPTable parentNameLine = createRequisiteValueLine(
                        "ФИО",
                        legalRepName,
                        ordFont,
                        1f,
                        5f
                );

                PdfPCell parentNameContainer = new PdfPCell(parentNameLine);
                parentNameContainer.setBorder(Rectangle.NO_BORDER);
                parentNameContainer.setPadding(0f);
                parentTable.addCell(parentNameContainer);

                PdfPTable addressLine = createRequisiteValueLine(
                        "Адрес:",
                        legalRepAddress,
                        ordFont,
                        1.2f,
                        5f
                );

                PdfPCell addressContainer = new PdfPCell(addressLine);
                addressContainer.setBorder(Rectangle.NO_BORDER);
                addressContainer.setPadding(0f);
                parentTable.addCell(addressContainer);

                PdfPTable phoneLine = createRequisiteValueLine(
                        "Тел.:",
                        legalRepPhone,
                        ordFont,
                        1.2f,
                        5f
                );

                PdfPCell phoneContainer = new PdfPCell(phoneLine);
                phoneContainer.setBorder(Rectangle.NO_BORDER);
                phoneContainer.setPadding(0f);
                parentTable.addCell(phoneContainer);

                PdfPTable studentNameLine = createRequisiteValueLine(
                        "ФИО ребенка",
                        studentFullName,
                        ordFont,
                        1.8f,
                        4.5f
                );

                PdfPCell studentNameContainer = new PdfPCell(studentNameLine);
                studentNameContainer.setBorder(Rectangle.NO_BORDER);
                studentNameContainer.setPadding(0f);
                parentTable.addCell(studentNameContainer);

                PdfPTable signatureLine = createRequisiteValueLine(
                        "Подпись",
                        "",
                        ordFont,
                        1.2f,
                        5f
                );

                PdfPCell signatureContainer = new PdfPCell(signatureLine);
                signatureContainer.setBorder(Rectangle.NO_BORDER);
                signatureContainer.setPaddingLeft(0f);
                signatureContainer.setPaddingRight(0f);
                signatureContainer.setPaddingTop(10f);
                signatureContainer.setPaddingBottom(0f);

                parentTable.addCell(signatureContainer);

                PdfPCell parentCell = new PdfPCell(parentTable);

                parentCell.setPaddingTop(5f);
                parentCell.setPaddingBottom(6f);
                parentCell.setPaddingLeft(8f);
                parentCell.setPaddingRight(8f);
                parentCell.setVerticalAlignment(Element.ALIGN_TOP);

                requisitesTable.addCell(parentCell);

                document.add(requisitesTable);
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

    private String safeValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private String buildFullName(
            Object surname,
            Object name,
            Object middleName
    ) {
        StringBuilder result = new StringBuilder();

        appendNamePart(result, surname);
        appendNamePart(result, name);
        appendNamePart(result, middleName);

        return result.toString().trim();
    }

    private void appendNamePart(
            StringBuilder result,
            Object value
    ) {
        String text = safeValue(value);

        if (!text.isEmpty()) {
            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(text);
        }
    }

    private String getContainerValue(
            IndexedContainer container,
            Object itemId,
            Object propertyId
    ) {
        if (container == null
                || itemId == null
                || propertyId == null
                || container.getContainerProperty(itemId, propertyId) == null
                || container.getContainerProperty(itemId, propertyId).getValue() == null) {

            return "";
        }

        return container
                .getContainerProperty(itemId, propertyId)
                .getValue()
                .toString()
                .trim();
    }

    private PdfPTable createRequisiteValueLine(
            String label,
            String value,
            Font font,
            float labelWidth,
            float valueWidth
    ) throws DocumentException {

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{labelWidth, valueWidth});

        PdfPCell labelCell = new PdfPCell(
                new Phrase(label, font)
        );

        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingLeft(0f);
        labelCell.setPaddingRight(3f);
        labelCell.setPaddingTop(3f);
        labelCell.setPaddingBottom(3f);
        labelCell.setVerticalAlignment(Element.ALIGN_TOP);

        PdfPCell valueCell = new PdfPCell(
                new Phrase(safeValue(value), font)
        );

        // Убираем нижнюю линию
        valueCell.setBorder(Rectangle.NO_BORDER);

        valueCell.setPaddingLeft(3f);
        valueCell.setPaddingRight(0f);
        valueCell.setPaddingTop(3f);
        valueCell.setPaddingBottom(3f);
        valueCell.setVerticalAlignment(Element.ALIGN_TOP);

        table.addCell(labelCell);
        table.addCell(valueCell);

        return table;
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