package kg.alex.lomonosov.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.domain.EmployeeInfoPdf;
import kg.alex.lomonosov.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ContractTechnicalStuffPdf {

    static final Logger logger = LogManager.getLogger(ContractTechnicalStuffPdf.class);
    private final static String FONT_LOCATION = "/home/lomonosov/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/lomonosov/TimesNewRomanBold.ttf";
    private final EmployeeInfoPdf employeeInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractTechnicalStuffPdf(final MyVaadinUI myUi, EmployeeInfoPdf employeeInfo) {
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
                spr.add(new Phrase("ТРУДОВОЙ ДОГОВОР", font_header));
                spr.add(Chunk.NEWLINE);

                spr.add(new Phrase("(с техническими работниками и младшим обслуживающим персоналом)", font_header));
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
                    if (employeeInfo.getDirector().getMiddle_name() != null && !employeeInfo.getDirector().getMiddle_name().equals("")) {
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
                paragraph.add(new Phrase(", действующего на основании   Устава, с одной стороны, и ", ordFont));
                paragraph.add(new Phrase(fullName + ", ", ordBoldFont));
                paragraph.add(new Phrase("именуемый в дальнейшем «Работник», с другой стороны, заключили настоящий трудовой договор о нижеследующем:", ordFont));
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
                paragraph.add(new Phrase("1.1. Работник принимается на должность "
                        + employeeInfo.getEmployeePosition() + ".", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.2. Работнику устанавливается испытательный срок на "
                        + employeeInfo.getContract().getProbationaryPeriod() + " месяца(ев).", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2. Права и обязанности сторон", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.1. Работник обязуется:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.1. Добросовестно выполнять свои трудовые обязанности, приказы и распоряжения Работодателя.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.2. Подчиняться Правилам внутреннего трудового распорядка Учреждения.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.3. Правильно и по назначению использовать переданное ему для работы оборудование, приборы, материалы.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.4. Соблюдать условия требований по конфиденциальности, описанные в разделе 5 настоящего договора;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.5. Соблюдать правила, инструкции по охране труда и распоряжения Работодателя по безопасному ведению работ.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.6. Бережно относиться к имуществу Работодателя, а также к любому чужому имуществу, возмещать причиненный ущерб, причиненный Работодателю, при необходимости заключать договор о полной материальной ответственности.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.7. Быть дружелюбным, не выражаться нецензурной речью.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.8. При заключении Договора Работник должен предоставить  необходимые документы:", ordFont));
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
                paragraph.add(new Phrase("- санитарную книжку, подтверждающую отсутствие у Работника опасных инфекционных и других заболеваний,", ordFont));
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

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.2 Работодатель обязуется:", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.1. Обеспечить Работника работой (материалами, оборудованием, заданием и т.д.) в соответствии с его специальностью и квалификацией;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.2. Создать Работнику здоровые и безопасные условия труда;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.3. Обеспечить прохождение Работником обучения по охране труда и систематически проводить проверку его знаний;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.4. Работодатель имеет право привлекать Работника на сверхурочную работу и работу в выходные и праздничные дни, в связи с производственной необходимостью, только с согласия Работника в каждом конкретном случае, с соблюдением условий оплаты или предоставления компенсаций.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Оплата труда", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("3.1. Работнику устанавливается ежемесячный оклад (заработная плата) в размере "
                        + employeeInfo.getContract().getSalary() + " сом.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2 Заработная плата выплачивается один раз в месяц не позднее "
                        + employeeInfo.getContract().getSalaryDay() + " числа каждого месяца.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Рабочее время и время отдыха", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Работнику устанавливается: "
                        + employeeInfo.getContract().getWorkingDays()
                        + "-дневная " + employeeInfo.getContract().getWorkingHours()
                        + "-часовая рабочая неделя.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.2. Работнику предоставляется ежегодный трудовой отпуск продолжительностью в 28 календарных дней.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.3. Отпуск предоставляется во время каникул в соответствии с графиком отпусков, утвержденном в Учреждении.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.4. Работодатель может отозвать Работника из отпуска в связи с производственной необходимостью только по его желанию и с его письменного согласия по истечении двенадцати дней с начала отпуска, с обязательным предоставлением неиспользованных дней отпуска в течение данного рабочего года.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.5. В случае необходимости Работодатель вправе предоставить Работнику отпуск без сохранения зарплаты.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Конфиденциальность", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Под “конфиденциальной информацией” подразумевается вся информация, касающаяся Работодателя, включая, без ограничения, закрытую  нформацию по деятельности и финансовому положению Работодателя, о клиентах и поставщиках, коммерческую тайну Работодателя, “ноу-хау” технологиях, а также размера вознаграждения, выплачеваемого Работнику во время действия настоящего договора.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.2. Работник не имеет права разглашать, сообщать и/или намеренно позволять любому лицу получить конфиденциальную информацию на протяжении срока действия настоящего договора и 12 (двенадцати) месцев после прекращения действия настоящего договора, если иное не предусмотрено действующим законодательством КР.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.3. Если Работник полагает, но не уверен в том, что определенная информация конфиденциальная, он обязан спросить разрешения у Работадателя, прежде чем разглашать такую информацию.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.4. После прекращения действия настоящего договора Работник обязан сдать Работодателю все материальное и иное имущество, находящееся в его владении и/или хранении. Работник не должен делать копии таких материалов  ни для каких целей.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Интеллектуальная собственность", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("6.1. Работник, заключая настоящий договор, соглашается с тем, что любая интеллектуальная собственность (изобретения, патенты, технологии, “ноу-хау”, коммерческие секреты, авторские права, товарные знаки, фирменные наименования, программное обеспечение, шаблоны, образцы, технические данные, проекты, программы, формулы, гарантии качества, экспериментальные данные, а также любая иная информация, приобретенная в период действия настоящего договора), созданная Работником в процессе оказания услуг Работодателю, является исключительной собственностью Работодателя. Работник передает все права на такую собственность Работодателю.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("7. Ответственность сторон", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.1. При нарушении Работником трудовой дисциплины Работодатель вправе применить к нему нормы трудового законодательства Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.2. За вред, причиненный имуществу Работодателя, Работник несет полную материальную ответственность.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("7.3. В случае наложения соответствующими органами штрафов, по вине Работника, то Работодатель имеет право удержать его с заработной платы Работника.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("8. Особые условия", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("8.1. Ознакомление Работника с Уставом Учреждения, Правилами внутреннего распорядка и техникой безопасности, функциональными обязанностями возлагается на Работодателя.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("8.2. За Работником закрепляется следующее оборудование "
                        + employeeInfo.getContract().getEquipment() + ".", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("9. Изменение и прекращение трудового договора", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.1. Трудовой договор может быть прекращен по соглашению Сторон в любое время, при этом все взаимоотношения, кроме параграфов 5 и 6 прекращаются полностью.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.2. Настоящий договор может быть прекращен по инициативе Работника, с предупреждением об этом Работодателя письменно за две недели.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.3. Договор может быть прекращен по инициативе Работодателя в следующих случаях:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.3.1. несоответствие Работника выполняемой работе вследствие недостаточной квалификации либо состояния здоровья;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.3.2. систематическое нарушение Работником своих трудовых обязанностей;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.3.3. нарушение пункт 5 данного Договора;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.3.4. иных оснований, предусмотренных законодательством Кыргызской Республики", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.4. Трудовые споры, возникшие между Сторонами договора, разрешаются в соответствии с законодательством КР.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.5. В случае прекращения трудового договора Стороны возвращают имущество, переданное ими в пользование друг другу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.6. Настоящий договор вступает в силу с момента его подписания Сторонами.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.7. Договор составлен в 2-х экземплярах, по одному экземпляру каждой стороне.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("9.8. Договор действует в течение с "
                        + Settings.dateRu.format(employeeInfo.getContract().getFromDate()) + " по "
                        + Settings.dateRu.format(employeeInfo.getContract().getTillDate()), ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("10. Реквизиты сторон", boldFont));
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
