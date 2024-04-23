package kg.alex.indigo.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbRelative;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ContractKidsPdf {

    static final Logger logger = LogManager.getLogger(ContractKidsPdf.class);
    private final static String FONT_LOCATION = "/home/indigo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/indigo/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractKidsPdf(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
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
                spr.add(new Phrase("ДОГОВОР № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);

                spr.add(new Phrase("Об оказании платных образовательных услуг", font_header));
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
                    fullName = dcl.Decline(studentInfo.getRelative().getFullName().trim(),
                            5, studentInfo.getRelative().getGender_id(), false);
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
                paragraph.add(new Phrase(" с другой стороны, заключили настоящий договор о нижеследующем:", ordFont));
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
                paragraph.add(new Phrase("2.8. Приводить ребенка в детсад только здоровым, не позднее 9.00 в опрятном виде: чистой одежде, обуви, обеспечить соответствующей одеждой для физкультурных и музыкальных занятий и забирать не позднее 18.15. Выходные: суббота, воскресенье и праздничные дни.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.9. Взаимодействовать с детским садом по всем направлениям воспитания и обучения ребенка, оказывать посильную помощь в создании благоприятных условий для пребывания ребенка в детском саду.", ordFont));
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
                paragraph.add(new Phrase("2.12. Обеспечить ребенка всеми учебными принадлежностями, необходимыми для успешного проведения учебно- воспитательного процесса в соответствии с требованиями программы.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.13. Оплачивать один раз в год 5000 (пять тысяч) сом на приобретение канцтоваров, необходимые для каждого ребенка.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(20);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.14. До первого (01) числа текущего месяца вносить оплату в размере "
                        + Settings.dFormat2.format(studentInfo.getContractInfo().getContract() / studentInfo.getContractInfo().getDuration())
                        + " " + studentInfo.getContractInfo().getCurrency() + " за содержание ребенка в детском саду.", ordFont));
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
                paragraph.add(new Phrase("4.1. Родитель оплачивает Детсаду за предоставление ребенку присмотра, воспитания, оздоровления и обучения основную оплату в размере  " +
                        Settings.dFormat2.format(studentInfo.getContractInfo().getContract() / studentInfo.getContractInfo().getDuration())
                        + " " + studentInfo.getContractInfo().getCurrency()
                        + " ежемесячно. Сумма за содержание оплачивается постоянно, не позднее первого (01) числа текущего месяца, независимо от посещения ребенком Детсада (кроме одного месяца летних каникул). При несвоевременной оплате за каждый день просрочки начисляется штраф в сумме 200 сом.", ordFont));
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
                paragraph.add(new Phrase("5.1. Участники договора несут ответственность за соблюдение данного договора в соответствии с законодательством Кыргызской Республики.", ordFont));
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

                document.newPage();
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(25);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. Реквизиты сторон", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.2f, 1f};
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
                String f_work_place = "";
                String m_name = "";
                String m_work_place = "";
                String passport = "";
                while (iter != null && iter.hasNext()) {
                    Object obj = iter.next();
                    if ((Integer) obj == 1) {
                        f_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.FullName)).getValue().toString();
                        f_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.WorkPlace)).getValue().toString();
                    }
                    if ((Integer) obj == 2) {
                        m_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.FullName)).getValue().toString();
                        m_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.WorkPlace)).getValue().toString();

                    }
                    if ((Integer) relativeCont.getContainerProperty(obj,
                            Settings.is_main).getValue() == 1) {
                        text18.add(new Phrase("Контактный тел: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.Phone)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Адрес места жительства: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.Address)).getValue().toString(), ordFont));
                        passport = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.Passport)).getValue().toString();
                    }
                }
                text11.add(new Phrase("Ф.И.О. отца: " + f_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Ф.И.О. матери: " + m_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Данные паспорта: ", ordFont));
                text11.add(new Phrase(passport, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место работы отца: " + f_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место работы матери: " + m_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
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
                text15.add(new Phrase("График оплаты", boldFont));
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
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Долг с предыдущего года: ", ordFont));
                } else {
                    text15.add(new Phrase("Переплата с предыдущего года: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + ""), ordBoldFont));
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
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
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Остаток: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + "", ordBoldFont));
                text15.add(new Phrase(" " + studentInfo.getContractInfo().getCurrency() + ".", ordFont));
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
                            myUI.getMessage(IndigoMessages.Date)).getValue()).getValue()), ordFont));
                    TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(IndigoMessages.Amount)).getValue()).getValue(), ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    n += 1;
                }
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("Итого:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet())
                        + " " + studentInfo.getContractInfo().getCurrency(), ordBoldFont));
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
                paragraph.add(new Phrase("Гл. бухгалтер: ", ordBoldFont));
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
