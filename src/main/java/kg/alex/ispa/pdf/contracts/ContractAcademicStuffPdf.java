package kg.alex.ispa.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.domain.EmployeeInfoPdf;
import kg.alex.ispa.utils.Decliner;
import kg.alex.ispa.utils.money.WritableSummRu;
import kg.alex.ispa.utils.money.WritableSummRuSOM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ContractAcademicStuffPdf {

    static final Logger logger = LogManager.getLogger(ContractAcademicStuffPdf.class);
    private final static String FONT_LOCATION = "/home/ispa/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/ispa/TimesNewRomanBold.ttf";
    private final EmployeeInfoPdf employeeInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractAcademicStuffPdf(final MyVaadinUI myUi, EmployeeInfoPdf employeeInfo) {
        this.employeeInfo = employeeInfo;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 30, 30);
                document.setMargins(10, 10, 30, 30);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 9.5f);
                Font ordBoldFont = new Font(baseFontBold, 9.5f);
                Font boldFont = new Font(baseFontBold, 10);
                Font font_header = new Font(baseFontBold, 10.5f);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Трудовой договор с педагогическим работником", font_header));
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
                table_date.addCell(new Phrase("г. " + this.employeeInfo.getSchool().getCity(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(this.employeeInfo.getContract().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Decliner dcl = new Decliner();

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(this.employeeInfo.getSchool().getName_ru()
                        + ", именуемый в дальнейшем «Работодатель», в лице директора ", ordFont));
                String fullName = null;
                try {
                    boolean isFeminine = employeeInfo.getDirector().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(employeeInfo.getDirector().getSurname(), isFeminine)
                            + " " + dcl.DeclineNameGenitive(employeeInfo.getDirector().getName(), isFeminine, false);
                    if (employeeInfo.getDirector().getMiddle_name() != null && !employeeInfo.getDirector().getMiddle_name().isEmpty()) {
                        fullName += " " + dcl.DeclinePatronymicGenitive(employeeInfo.getDirector().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                fullName = employeeInfo.getEmployeeSurname() + " " + employeeInfo.getEmployeeName();
                if (employeeInfo.getEmployeeMiddleName() != null) {
                    fullName += " " + employeeInfo.getEmployeeMiddleName();
                }
                paragraph.add(new Phrase(", действующего на основании Устава, с одной стороны, и ", ordFont));
                paragraph.add(new Phrase(fullName + ", ", ordBoldFont));
                paragraph.add(new Phrase("именуемый в дальнейшем «Работник», с другой стороны, заключили настоящий трудовой договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("1. Общие положения", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. По настоящему трудовому договору Работник обязуется выполнять обязанности в должности преподавания: " +
                        dcl.DeclineNameGenitive(employeeInfo.getEmployeeBranch(), true, false)
                        + ", а Работодатель обязуется обеспечивать ему необходимые условия труда.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.2. Местом работы по данному трудовому договору является Учреждение " + employeeInfo.getSchool().getName_ru() + ".", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.3. Дата начала работы по настоящему трудовому договору: "
                        + Settings.dateRu.format(employeeInfo.getContract().getFromDate()), ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.4. Срок действия настоящего трудового договора истекает "
                        + Settings.dateRu.format(employeeInfo.getContract().getTillDate()), ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2. Права и обязанности Работника", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.1. Работник имеет право:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.1. выбирать и использовать методики обучения и воспитания, учебные пособия и материалы, учебники, методы оценки знаний учащихся в соответствии с государственными образовательными стандартами;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.2. на участие в разработке новых авторских программ, учебников и учебных пособий;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.3. на создание и использование новаторских методов и приемов, проведение научно-педагогического эксперимента, внедрение передового педагогического опыта в практику;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.4. повышать квалификацию в системе переподготовки и повышения квалификации;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.5. на защиту своего достоинства и профессиональной чести;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.6. на отдых и праздники, утвержденные Правительством Кыргызской Республики и согласованные Работодателем;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.7. обращаться к администрации Учреждения с заявлениями, которые подлежат к рассмотрению.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.2 Работник обязан:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.1. соблюдать действующее законодательство Кыргызской Республики, Устав Учреждения, правила внутреннего трудового распорядка (Приложение №1) и иные локальные нормативные акты Учреждения, в том числе нормы и правила по охране труда и технике безопасности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.2. проводить обучение на высоком профессиональном уровне, обеспечивать усвоение учащимися учебной программы не ниже уровня государственных образовательных стандартов;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.3. проводить учебно-воспитательную работу в духе уважения к родителям, старшим, к культурно-историческим ценностям Кыргызстана, его государственному устройству, воспитывать бережное отношение к окружающей среде;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.4. утверждать уважение к принципам общечеловеческой морали: доброте, трудолюбию, гуманизму, патриотизму, правде, справедливости и толерантности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.5. постоянно совершенствовать свои педагогические знания и мастерство;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.6. уважать честь и достоинство учащихся;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.7. готовить учащихся к сознательной жизни в духе взаимопонимания, мира, согласия между народами;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.8. содействовать развитию индивидуальных творческих способностей учащихся;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.9. соблюдать трудовую дисциплину;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.10. бережно относиться к имуществу Работодателя, а также к любому чужому имуществу, возмещать материальный ущерб, причиненный Работодателю, при необходимости заключить с Работодателем Договор о полной материальной ответственности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.11. выполнять условия настоящего договора, должностную инструкцию;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.12. выполнять обязанности, вытекающие из индивидуального плана работы, расписания учебных занятий, своевременно и в полном объеме выполнять распоряжения администрации Учреждения;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.13. выполнять иные обязанности, установленные в надлежащем порядке;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.14. немедленно сообщать Работодателю о возникновении ситуации, представляющей угрозу жизни и здоровью людей, сохранности имущества Работодателя.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.15. воздерживаться от прямого или косвенного участия в любой коммерческой или профессиональной деятельности, которая может спровоцировать конфликт интересов в отношении обязанностей Работника по настоящему договору, до окончания срока настоящего договора;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.16. не создавать чаты/группы в социальных мессенджерах с учащимися и их законными представителями, без согласования с Работодателем;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.17. письменно информировать Работодателя о жалобах со стороны родителей и учащихся;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.18. не давать интервью журналистам СМИ о деятельности Учреждения и МОУ “Сапат”.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.19. не навязывать свои личные политические и религиозные взгляды учащимся и иному персоналу Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.20. в случае, если Работник допускает однократное грубое нарушение своих обязанностей или условий настоящего договора (совершение “Грубого Нарушения”), Работодатель, согласно статье 6, имеет право прекратить действие настоящего договора. Работник считается виновным в  совершении “ Грубого Нарушения”, если он/она:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(а) совершает действия, ущемляющие честь и достоинство учащихся и их законных представителей;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(б) при выполнении своих обязанностей и обязательств находится в состоянии алкогольного, наркотического или химического опьянения;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(в) совершает умышленную порчу или хищение имущества Работодателя;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(г) раглашает коммерческую тайну Работодателя или любую конфиденциальную информацию без надлежащих полномочий;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(д) допускает нарушение правил безопасности, повлекшее тяжкие последствия, включая травмы и аварии;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(е) совершает дейстия, противоречащие миссии Учреждения, а также наносящие ущерб интересам Работодателя.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(ж) применяет, в том числе однократно, методы воспитания, связанные с физическим и (или) психическим насилием над личностью обущающегося;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(з) передает третьим лицам контактные данные учащихся и их законных представителей без согласования с Работодателем.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(и) допускает получение со стороны родителей, либо лиц их заменяющих подарки, денежное вознаграждение и.т.д.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.21. В своей работе Работник подчиняется Работодателю, своевременно предоставляет все отчеты и необходимую информацию, запрашиваемую администрацией Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.22. При заключении трудового договора Работник должен предоставить все необходимые документы:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- трудовую книжку,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- копию паспорта,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- нотариально заверенную копию диплома,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- заявление о приеме на работу,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- фотографию 3на 4, 2 шт.,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- характеристику с прежнего места работы (по необходимости)", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- санитарную книжку, подтверждающий отсутствие у Работника опасных инфекционных и других заболеваний,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- справку с центра психиатрии и наркологии,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- справку о неимении судимости.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Права и обязанности Работодателя", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("3.1. Работодатель имеет права, предусмотренные законами и иными нормативными актами Кыргызской Республики о труде и образовании, Уставом Учреждения и другими локальными нормативными актами, настоящим трудовым договором.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2. Работодатель имеет право требовать от Работника добросовестного выполнения своих договорных обязательств.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3. Работодатель обязан:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3.1. обеспечить условия для безопасного и эффективного труда Работника;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3.2. оборудовать рабочее место Работника в соответствии с правилами охраны труда и техники безопасности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3.3. своевременно выплачивать Работнику обусловленную настоящим договором заработную плату.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Режим труда и отдыха", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Работнику устанавливается режим труда и отдыха в соответствии с законодательством Кыргызской Республики, правилами внутреннего трудового распорядка Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.2. Работнику предоставляется в соответствии с графиком отпусков или по личному заявлению ежегодный отпуск во время каникул, продолжительность которого определяется Правительством Кыргызской Республики", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.3. В случае неявки на работу или опоздания по уважительной причине Работник обязан минимум за 2 (два) часа до начала работ сообщить директору или заместителю директора по учебной части, для избежания срывов учебного процесса.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Условия оплаты труда", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Работнику выплачиваются:", ordFont));
                document.add(paragraph);

                WritableSummRu convertToLetters = new WritableSummRuSOM();
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("а) месячный должностной оклад " + employeeInfo.getContract().getSalary() + ".", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.2. Работнику могут быть установлены иные выплаты, в том числе стимулирующие. Размеры, сроки и источники указанных выплат определяются приказом директора Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.3. Прекращение, изменение размера и сроков выплат, установленных приказом директора Учреждения и не предусмотренных п. 5.1. настоящего договора и правовыми нормативными актами Кыргызской Республики как обязательные, производится приказом директора Учреждения без согласования с Работником.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Прекращение трудового договора", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. Настоящий договор расторгается по истечении срока его действия, предусмотренного в п.1.4. настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.2. Работодатель вправе расторгнуть настоящий договор, предварительно письменно уведомив об этом Работника, за 2 (две) недели до расторжения данного договора, в случае;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(35);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- при совершении “Грубого Нарушения”,", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(35);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- если Работник систематически нарушает свои обязательства и права Работодателя.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.3. Настоящий договор может быть расторгнут досрочно по обоюдному письменному согласию Сторон, а также при ненадлежащем выполнении Работником своих обязанностей, согласно данного договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.4. Работник вправе расторгнуть настоящий договор досрочно, предварительно письменно уведомив об этом Работодателя за  1 (одну) четверть учебного года до даты расторжения.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. Конфиденциальность", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. Под “конфиденциальной информацией” подразумевается вся информация, касающаяся Работодателя, включая, без ограничения, закрытую информацию по деятельности и финансовому положению Работодателя, о клиентах и поставщиках, коммерческую тайну Работодателя, “ноу-хау” технологиях, а также размера вознаграждения, выплачиваемого Работнику во время действия настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.2. Работник не имеет права разглашать, сообщать и/или намеренно позволять любому лицу получить конфиденциальную информацию на протяжении срока действия настоящего договора и 12 (двенадцати) месяцев после прекращения действия настоящего договора, если иное не предусмотрено действующим законодательством КР.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.3. Если Работник полагает, но не уверен в том, что определенная информация конфиденциальная, он обязан спросить у Работодателя разрешения, прежде чем разглашать такую информацию.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.4. После прекращения действия настоящего договора Работник обязан сдать Работодателю все книги, отчеты, документы и иное имущество, находящееся в его владении и/или хранении. Работник не должен делать копии таких документов или имущества ни для каких целей.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. Интеллектуальная собственность", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("8.1. Работник, заключая настоящий договор, соглашается с тем, что любая интеллектуальная собственность (изобретения, патенты, технологии, “ноу-хау”, коммерческие секреты, авторские права, товарные знаки, фирменные наименования, программное обеспечение, шаблоны, образцы, технические данные, проекты, программы, формулы, гарантии качества, экспериментальные данные, а также любая иная информация, приобретенная в период действия настоящего договора), созданная Работником в процессе работы оказания услуг Работодателю, является исключительной собственностью Работодателя.  Работник передает все права на такую собственность Работодателю.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("9. Заключительные положения", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.1. Трудовые споры из настоящего трудового договора разрешаются в порядке, установленном законодательством Кыргызской Республики о труде.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.2. Настоящий трудовой договор составлен в двух экземплярах, имеющих одинаковую юридическую силу. Один экземпляр хранится у Работодателя, второй - у Работника.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.3. Условия настоящего трудового договора могут быть изменены только по соглашению Сторон в письменной форме.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                document.newPage();
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("10. Реквизиты и подписи сторон", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));


                float[] table_info_colsWidth = {1f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.getDefaultCell().setPaddingRight(10);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                table_info.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                table_info.addCell(new Phrase("Работодатель:", boldFont));
                table_info.addCell(new Phrase("Работник:", boldFont));
                table_info.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase(this.employeeInfo.getSchool().getName_ru(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Адрес: ", ordBoldFont));
                text10.add(new Phrase(this.employeeInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор: ", ordBoldFont));
                text10.add(new Phrase(employeeInfo.getDirector().getSurname() + " "
                        + employeeInfo.getDirector().getName() + " " +
                        (employeeInfo.getDirector().getMiddle_name() == null ?
                                "" : employeeInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(Chunk.NEWLINE);
                table_info.addCell(text10);

                Paragraph text11 = new Paragraph();
                text11.add(new Phrase("Ф.И.О. ", ordBoldFont));
                text11.add(new Phrase(fullName, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Паспорт: № ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getPassport(), ordFont));
                text11.add(new Phrase(" выдан ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getPassportGiven(), ordFont));
                text11.add(new Phrase(" от ", ordBoldFont));
                text11.add(new Phrase(Settings.df.format(employeeInfo.getContact().getPassportDate()), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место жительства: ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getAddress(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("ИНН: ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getInn(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Тел.: ", ordBoldFont));
                text11.add(new Phrase(employeeInfo.getContact().getPhoneNumbers(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(Chunk.NEWLINE);
                table_info.addCell(text11);

                table_info.addCell(new Phrase("Подпись.: ________________________", ordBoldFont));
                table_info.addCell(new Phrase("Подпись.: ________________________", ordBoldFont));
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

        myUi.getPage().open(resource, nameOf, false);
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
