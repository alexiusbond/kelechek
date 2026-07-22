package kg.alex.abl.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.domain.EmployeeInfoPdf;
import kg.alex.abl.utils.Decliner;
import kg.alex.abl.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ServiceAgreementTechnicalStuffPdf {

    static final Logger logger = LogManager.getLogger(ServiceAgreementTechnicalStuffPdf.class);
    private final static String FONT_LOCATION = "/home/abl/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/abl/TimesNewRomanBold.ttf";
    private final EmployeeInfoPdf employeeInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ServiceAgreementTechnicalStuffPdf(final MyVaadinUI myUi, EmployeeInfoPdf employeeInfo) {
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
                spr.add(new Phrase("Договор на оказание услуг", font_header));
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
                        + ", именуемый в дальнейшем «Заказчик», в лице директора ", ordFont));
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
                paragraph.add(new Phrase(", действующего на основании Устава, с одной стороны, и гражданин/ка ", ordFont));
                paragraph.add(new Phrase(fullName + ", ", ordBoldFont));
                paragraph.add(new Phrase("паспорт № " + employeeInfo.getContact().getPassport() +
                        ", выданный " + employeeInfo.getContact().getPassportGiven() +
                        " от " + Settings.df.format(employeeInfo.getContact().getPassportDate()) +
                        ", действующий на основании патента №" + employeeInfo.getContract().getPatent() +
                        " от " + Settings.df.format(employeeInfo.getContract().getPatentDate()) +
                        ", именуемый/ая в дальнейшем “Исполнитель”, с другой стороны, заключили настоящий договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("1. Предмет договора", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Исполнитель обязуется по заданию Заказчика ", ordFont));
                paragraph.add(new Phrase("оказать услуги", ordBoldFont));
                paragraph.add(new Phrase(" в образовательном учреждении "
                        + employeeInfo.getSchool().getName_ru() + ", а Заказчик обязуется принять и оплатить эти услуги.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.2. Услуги считаются оказанными после предъявления планов работы, акта выполненных работ и результатов работ.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.3. Исполнитель получает оплату за оказанные услуги, согласно акта выполненных работ "
                        + employeeInfo.getContract().getSalary() +
                        " сом, с учетом всех налогов Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.4. При наличии замечаний к качеству оказываемых услуг, составляется акт о нарушениях, который прикрепляется к настоящему договору.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2. Срок действия договора", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.1. Настоящий договор заключается на определенный срок и действует с "
                        + Settings.dateRu.format(employeeInfo.getContract().getFromDate()) + " по "
                        + Settings.dateRu.format(employeeInfo.getContract().getTillDate()), ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("3. Обязанности Исполнителя", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Исполнитель обязан:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. добросовестно, своевременно и эффективно исполнять все обязательства по настоящему договору, соблюдать действующее законодательство Кыргызской Республики, Устав Учреждения, правила поведения и техники безопасности, а также противопожарные меры;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2. уважать честь и достоинство обучающихся и других сотрудников Учреждения;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3. соблюдать график и инструкции работы, обусловленные и утвержденные Заказчиком;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.4. бережно относиться к имуществу Заказчика, а также к любому чужому имуществу, возмещать причиненный ущерб, причиненный Заказчику, при необходимости заключать договор о полной материальной ответственности;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.5. выполнять условия настоящего договора;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.6. выполнять обязанности, вытекающие из индивидуального плана работы, своевременно и в полном объеме выполнять распоряжения Заказчика;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.7. выполнять иные обязанности, установленные в надлежащем порядке;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.8. соблюдать условия требований по конфиденциальности, описанные в разделе 4 настоящего договора;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.9. немедленно сообщать Заказчику о возникновении ситуации, представляющей угрозу жизни и здоровью людей, сохранности имущества Заказчика;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.10. своевременно уведомлять Заказчика, если выполнение обязанностей или обязательств Исполнителем становится невозможным;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.11. воздерживаться от прямого или косвенного участия в любой коммерческой или профессиональной деятельности, которая может спровоцировать конфликт интересов в отношении обязанностей Исполнителя по настоящему договору, до окончания срока настоящего договора;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.12. не создавать чаты/группы в социальных мессенджерах с учащимися и их законными представителями, без согласования с Заказчиком;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.13. не давать интервью журналистам СМИ о деятельности Учреждения и МОУ “Сапат”.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.14. в случае, если Исполнитель допускает однократную грубое нарушение своих обязанностей или условий настоящего Договора (совершение “Грубого Нарушения”), Заказчик, согласно статье 6, имеет право прекратить действие настоящего Договора. Исполнитель считается виновным в  совершени “Грубого Нарушения”, если он/она:", ordFont));
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
                paragraph.add(new Phrase("(в) совершает умышленную порчу или хищение имущества Заказчика;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(г) разглашает коммерческую тайну Заказчика или любую конфиденциальную информацию без надлежащих полномочий;", ordFont));
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
                paragraph.add(new Phrase("(е) совершает дейстия, противоречащие миссии, а также наносящие ущерб интересам Заказчика.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(ж) применяет, в том числе однократно, методы воспитания, связанные с физическим и(или) психическим насилием над личностью обущающего, воспитанника;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(35);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("(з) передает третьим лицам контактные данные учащихся и их законных представителей без согласования с Заказчиком.", ordFont));
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
                paragraph.add(new Phrase("3.15. при заключении Договора Исполнитель должен предоставить  необходимые документы:", ordFont));
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
                paragraph.add(new Phrase("- фотографию 3 на 4, 2 шт.,", ordFont));
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
                paragraph.add(new Phrase("4. Конфиденциальность", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("4.1. Под “конфиденциальной информацией” подразумевается вся информация, касающаяся Заказчика, включая, без ограничения, закрытую  нформацию по деятельности и финансовому положению Заказчика, о клиентах и поставщиках, коммерческую тайну Заказчика, “ноу-хау” технологиях, а также размера вознаграждения, выплачеваемого исполнителю во время действия настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.2. Исполнитель не имеет права разглашать, сообщать и/или намеренно позволять любому лицу получить конфиденциальную информацию на протяжении срока действия настоящего договора и двенадцати (12) месцев после прекращения действия настоящего договора, если иное не предусмотрено действующим законодательством КР.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.3 Если исполнитель полагает, но не уверен в том, что определенная информация конфиденциальная, он обязан спросить у Заказчика разрешения, прежде чем разглашать такую информацию.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.4. После прекращения действия настоящего договора Исполнитель обязан сдать Заказчику все книги, отчеты, документы и иное имущество, находящееся в его владении и/или хранении. Исполнитель не должен делать копии таких документов или имущества ни для каких целей.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Интеллектуальная собственность", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Исполнитель, заключая настоящий договор, соглашается с тем, что любая интеллектуальная собственность (изобретения, патенты, технологии, “ноу-хау”, коммерческие секреты, авторские права, товарные знаки, фирменные наименования, программное обеспечение, шаблоны, образцы, технические данные, проекты, программы, формулы, гарантии качества, экспериментальные данные, а также любая иная информация, приобретенная в период действия настоящего договора), созданная Исполнителем в процессе оказания услуг Заказчику, является исключительной собственностью Заказчика. Исполнитель передает все права на такую собственность Заказчику.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Расторжение договора", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. Настоящий договор расторгается по истечении срока его действия, предусмотренного в п.2.1. настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.2. Настоящий договор может быть расторгнут досрочно по обоюдному письменному согласию сторон, а также при ненадлежащем выполнении исполнителем своих обязанностей согласно данного договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(35);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.3. Заказчик вправе расторгнуть настоящий договор ", ordFont));
                paragraph.add(new Phrase("незамедлительно ", ordBoldFont));
                paragraph.add(new Phrase("предварительно  письменно уведомив об этом Исполниетеля  при наступлении “События Грубого Нарушения“;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(35);
                paragraph.setIndentationRight(35);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.4. Заказчик вправе расторгнуть настоящий договор, предварительно письменно уведомив об этом Исполнителя за 2 (две) недели до даты расторжения, в случае, если Исполнитель систематически нарушает свои обязательства и права Заказчика;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.5. Исполнитель вправе расторгнуть настоящий договор досрочно, предварительно письменно уведомив об этом Заказчика за 1 (одну) четверть  учебного года до даты расторжения.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. Заключительные положения", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. Условия настоящего договора носят конфиденциальный характер и разглашению не подлежат.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.2. Стороны не несут ответственности при невозможности или сложности выполнения своих обязательств по настоящему договору при наступлении обстоятельств непреодолимой силы, т,е. форс-мажорных обстоятельств. При введении в одностороннем порядке ограничительных мер на деятельность Заказчика со стороны государственных или других органов, в том числе и стихийных бедствий, объявления властями Кыргызской Республики о закрытии или переводе учащихся на дистанционный вид обучения, Заказчик имеет право пересмотреть условия настоящего договора: вплоть до досрочного приостановления/прекращения действия настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.3. Любые споры или несогласия вытекающие из данного договора, должны быть разрешены путем переговоров.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.4. Условия настоящего договора имеют обязательную юридическую силу для сторон с момента его подписания Сторонами. Все изменения и дополнения к настоящему договору оформляются двухсторонним письменным соглашением.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.5. Настоящий договор составлен в двух экземплярах, имеющих одинаковую юридическую силу, по одному экземпляру для каждой из сторон.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                //document.newPage();
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. Реквизиты и подписи сторон", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));


                float[] table_info_colsWidth = {1f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.getDefaultCell().setPaddingRight(10);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                table_info.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                table_info.addCell(new Phrase("Заказчик:", boldFont));
                table_info.addCell(new Phrase("Исполнитель:", boldFont));
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
                text10.add(new Phrase(this.employeeInfo.getDirector().getSurname() + " "
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
