package kg.alex.indigo.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ContractKidsPdf {

    static final Logger logger = LogManager.getLogger(ContractKidsPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;

    private final static String FONT_LOCATION = "/home/logo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/logo/TimesNewRomanBold.ttf";

    public ContractKidsPdf(final MyVaadinUI ui, StudentInfoPdf st_info) {
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
                Font ordFont = new Font(baseFont, 10);
                Font ordBoldFont = new Font(baseFontBold, 10);
                Font ordBoldUnderlinedFont = new Font(baseFontBold, 10, Font.UNDERLINE);
                Font boldFont = new Font(baseFontBold, 11);
                Font boldUnderlinedFont = new Font(baseFontBold, 11, Font.UNDERLINE);
                Font font_header = new Font(baseFontBold, 11);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Договор", font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("Между " + studentInfo.getSchool().getName_ru(), font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("и родителями ребенка, посещающего детский сад.", font_header));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                Decliner dcl = new Decliner();
                String fullName = null;
                try {
                    boolean isFeminine = studentInfo.getDirector().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getDirector().getSurname(), isFeminine)
                            + " " + studentInfo.getDirector().getName().charAt(0) + ".";
                    if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().equals("")) {
                        fullName += " " + studentInfo.getDirector().getMiddle_name().charAt(0) + ".";
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(studentInfo.getSchool().getName_ru(), ordBoldFont));
                paragraph.add(new Phrase(", именуемый в дальнейшем «Детсад» в лице директора " + fullName
                        + " с одной стороны и матерью (отцом; лицом, их замещающим) ", ordFont));
                fullName = studentInfo.getRelative().getFullName();
                try {
                    fullName = dcl.Decline(studentInfo.getRelative().getFullName(), 5, studentInfo.getRelative().getGender_id(), false);
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName.trim() + ", ", ordBoldFont));
                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                try {
                    boolean isFeminine = studentInfo.getStudent().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getStudent().getSurname(), isFeminine) + " "
                            + dcl.DeclineNameGenitive(studentInfo.getStudent().getName(), isFeminine, false);
                    if (!studentInfo.getStudent().getMiddle_name().equals("")) {
                        fullName = fullName + " "
                                + dcl.DeclinePatronymicGenitive(studentInfo.getStudent().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(" именуемым в дальнейшем «Родитель» ребенка " + fullName, ordFont));
                paragraph.add(new Phrase(" с другой стороны заключили настоящий договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("1. Предмет договора:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Детский сад принимает ребенка и предоставляет услуги по содержанию ребенка в Детском саду, питанию, оздоровлению и обучению в соответствии с возрастом и используемой программой, а Родитель оплачивает данные услуги.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2. Обязанности сторон:", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(25);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("«Детсад» обязуется:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Обеспечить охрану жизни и укрепление физического и психического здоровья ребенка, его интеллектуальное, физическое и личностное развитие; развитие его творческих способностей и интересов; осуществлять индивидуальный подход к ребенку, учитывая особенности его развития; заботиться об эмоциональном благополучии ребенка, осуществлять квалифицированную коррекцию.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2. Обучать ребенка по образовательной программе, разработанной самостоятельно с учетом республиканских государственных требований к структуре основной общеобразовательной программы дошкольного образования и условиям ее реализации.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3. Создавать предметно-развивающую среду (помещение, оборудование, учебно-наглядные пособия).", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4. Обеспечить ребенка 4-х разовым сбалансированным питанием, необходимым для его роста и развития.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.5. Оказывать квалифицированную помощь «Родителю» в воспитании и обучении ребенка, в коррекции ребенка.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(25);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("«Родитель» обязуется:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.6. Соблюдать Устав детсада и условия договора", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.7. Лично передавать и забирать ребенка у воспитателя, не передоверяя ребенка лицам, не достигшим 16 лет (или по особым обстоятельствам при наличии письменного заявления от родителей).", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.8. Приводить ребенка в детсад только здоровым, не позднее 9.00 в опрятном виде:чистой одежде, обуви, обеспечить соответствующей одеждой для физкультурных и музыкальных занятий и забирать не позднее 18.15. Выходные: суббота, воскресенье и праздничные дни.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.9. Взаимодействовать с детским садом по всем направлениям воспитания и обучения ребенка, оказывать посильную помощь в создании благоприятных условий для пребывания ребенка в деском саду.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.10. Уважать права педагога, не допускать бестактного поведения по отношению к работникам детского сада, выполнять правила внутреннего трудового распорядка, нормы педагогической этики и общепринятые правила общения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.11. Информировать детсад о предстоящем отсутствии ребенка или его болезни в первый день отсутствия до 13.00.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.12. Обеспечить ребенка всеми учебными принадлежностями, необходимыми для успешного проведения учебно-воспитательного процесса в соответствии с требованиями программы.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.13. До первого (01) числа текущего месяца вносить оплату в размере "
                        + Settings.dFormat2.format(studentInfo.getContractInfo().getContract() / studentInfo.getContractInfo().getDuration())
                        + " USD за содержание ребенка в детском саду.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("3. Права сторон", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(25);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("«Детсад» имеет право:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1 Выбирать, разрабатывать и применять образовательные программы, методики обучения и воспитания, учебные пособия и материалы в пределах, определенных законодательством Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2 Отчислить ребенка из учреждения при наличии медицинского заключения о состоянии здоровья, препятствующего его дальнейшему пребыванию в детсаде.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3 В течение учебного года проводить диагностические методики с целью выявления уровня развития ребенка, степени усвоения программы.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.4 Не принимать ребенка в детский сад при отсутствии платы за текущий месяц.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.5 Не принимать ребенка в детский сад, если при осмотре у него обнаружены признаки заболевания, требовать письменного заключения врача-специалиста.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.6 Расторгнуть настоящий договор досрочно в одностороннем порядке в случае систематического невыполнения Родителем принятых на себя по настоящему договору обязательств. При этом Детсад обязан уведомить Родителя о расторжении договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(25);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("«Родитель» имеет право:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.7 Соблюдать права и интересы своего ребенка.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.8 Родители имеют право знакомиться с содержанием образования, используемыми методами обучения и воспитания, но не имеют права требовать изменений технологического процесса обучения воспитанников, учебного плана или образовательных программ обучения и воспитания.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.9. Оказывать детсаду посильную помощь в реализации уставных задач, в благоустройстве прогулочных площадок, групповых помещений.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.10. Выбирать различные виды дополнительного образования в детском саду за дополнительную плату.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("4. Оплата услуг", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Родитель оплачивает Детсаду за предоставление ребенку присмотра, воспитания, оздоровления и обучения основную оплату в размере " +
                        Settings.dFormat2.format(studentInfo.getContractInfo().getContract() / studentInfo.getContractInfo().getDuration())
                        + " USD ежемесячно. Сумма за содержание оплачивается постоянно, не позднее первого (01) числа текущего месяца, независимо от посещения ребенком Детсада (кроме одного месяца летних каникул). При несвоевременной оплате за каждый день просрочки начисляется штраф в сумме 200 сом.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.2. При расторжении договора оплата за детский сад и взнос родителям не возвращаются.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.3. В случае досрочного расторжения договора по инициативе Родителя по причинам, не зависящим от Детсада, Родитель оплачивает Детсаду фактически оказанные услуги, а также возмещает фактически понесенные Детсадом убытки в соответствии с законодательством.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("5. Ответственность сторон", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Участники договора несут ответственность за соблюдение данного договора в соответствии с законодательством Кыргызской Республики", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("6. Основания для расторжения договора", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. Настоящий договор может быть расторгнут по обоюдному согласию Сторон.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.2. Детский сад вправе отказаться от исполнения настоящего договора, если Родитель систематически нарушает Правила внутреннего распорядка Детсада, нарушает права и законные интересы работников Детсада. Договор считается расторгнутым со дня уведомления Родителя об отказе от исполнения договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("7. Общие положения", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. Все изменения и дополнения к настоящему договору считаются действительными, если они оформлены в письменном виде и подписаны надлежащим образом уполномоченными лицами Сторон.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.2. Настоящий договор подписан в двух экземплярах, имеющих одинаковую силу.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.3. Настоящий договор вступает в силу со дня его подписания Сторонами и действует один год.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.4. По вопросам, не отраженным в настоящем договоре, Стороны руководствуются нормами законодательства КР.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("Стороны, подписывающие договор:", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                table_info.getDefaultCell().setLeading(15, 0);
                Paragraph text10 = new Paragraph();
                text10.setPaddingTop(5);
                text10.add(new Phrase("«Родитель»: ", boldFont));
                table_info.addCell(text10);
                text10 = new Paragraph();
                text10.setPaddingTop(5);
                text10.add(new Phrase("Директор " + studentInfo.getSchool().getName_ru(), boldFont));
                table_info.addCell(text10);
                text10 = new Paragraph();
                text10.setPaddingTop(5);
                text10.add(new Phrase(studentInfo.getRelative().getFullName(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Подпись: __________________________", ordFont));
                table_info.addCell(text10);
                text10 = new Paragraph();
                text10.setPaddingTop(5);
                text10.add(new Phrase(studentInfo.getDirector().getSurname() + " "
                        + studentInfo.getDirector().getName().charAt(0) +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : ". " + studentInfo.getDirector().getMiddle_name().charAt(0)), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Подпись: __________________________", ordFont));
                table_info.addCell(text10);

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
