package kg.alex.ellipse.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.domain.StudentInfoPdf;
import kg.alex.ellipse.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ContractOnTemplatePdf {
    static final Logger logger = LogManager.getLogger(ContractOnTemplatePdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private final MyVaadinUI myUI;

    public ContractOnTemplatePdf(final MyVaadinUI ui, StudentInfoPdf studentInfo, final IndexedContainer instPlanCont) {
        myUI = ui;
        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/ellipse/TimesNewRomanRegular.ttf";
            private final static String FONT_LOCATION2 = "/home/ellipse/TimesNewRomanBold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();
                PdfStamper pdfStamper = null;

                try {

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    Font ordFont = new Font(baseFont, 10.5f);
                    Font captionBoldFont = new Font(baseFontBold, 11);
                    Font ordBoldFont = new Font(baseFontBold, 10.5f);
                    Font ordBoldUnderlineFont = new Font(baseFontBold, 10.5f, Font.UNDERLINE);
                    Font ordBoldItalicFont = new Font(baseFontBold, 10.5f, Font.ITALIC);

                    PdfReader pdfReader = new PdfReader("/home/ellipse/contract_template.pdf");

                    pdfStamper = new PdfStamper(pdfReader, buffer);

                    PdfContentByte pageContentByte = pdfStamper.getOverContent(1);
                    ColumnText ct = new ColumnText(pageContentByte);
                    ct.setSimpleColumn(38, 720, 580, 50);

                    Paragraph paragraph = new Paragraph();
                    paragraph.setFirstLineIndent(0);
                    paragraph.setIndentationLeft(30);
                    paragraph.setIndentationRight(30);
                    paragraph.setLeading(15);
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), captionBoldFont));
                    ct.addElement(paragraph);
                    ct.addElement(new Paragraph(10, " "));

                    Paragraph spr = new Paragraph();
                    spr.add(new Phrase("ДОГОВОР № "
                                       + String.format("%07d", studentInfo.getContractInfo().getContractNumber()) + " на " + studentInfo.getYear().getName()
                                       + " учебный год \nоб оказании платных образовательных услуг", captionBoldFont));
                    spr.add(Chunk.NEWLINE);

                    spr.setAlignment(Element.ALIGN_CENTER);
                    ct.addElement(spr);
                    ct.addElement(new Paragraph(10, " "));

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
                                             + studentInfo.getRelative().getFullName() + ", являющаяся(щийся) родителем или законным представителем «Учащегося» "
                                             + studentFullName + ", именуемый в дальнейшем «Родитель» с другой стороны, в интересах обучающегося, в соответствии с пунктом 1 статьи 14 Закона Кыргызской Республики «Об образовании», заключили настоящий Договор о нижеследующем: ", ordFont));
                    ct.addElement(paragraph);
                    ct.addElement(new Paragraph(10, " "));

                    paragraph.clear();
                    paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", ordBoldFont));
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("1.1. Предметом Договора является организация процесса обучения Учащегося, получение им образования соответствующего государственным стандартам учебных программ Министерства образования Кыргызской Республики, или получение им образования по стандартам учебных программ «Cambridge Assessment International Education» на период " + studentInfo.getYear().getPeriod() + ".", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("1.2. При освоении Учащимся образовательной программы и успешного прохождения государственной итоговой аттестации выдается соответствующий документ.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("1.3. ФИО родителя или законного представителя, указанного в настоящем Договоре, выступает ответственным лицом за взаимодействие со Школой, а также несет обязанности за исполнения обязательств, предусмотренных настоящим Договором.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ СТОРОН", ordBoldFont));
                    paragraph.setAlignment(Element.ALIGN_CENTER);
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    paragraph.add(new Phrase("2.1. Школа обязана:", ordBoldUnderlineFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение услуг, предусмотренных в пункте 1.1. настоящего Договора. Образовательные услуги оказываются в соответствии с государственными стандартами учебных программ Министерства образования Кыргызской Республики или учебных программ «Cambridge Assessment International Education».", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.2. В целях усвоения Учащимся образовательных программ, являющихся предметом настоящего Договора, обеспечить Учащегося методической и консультационной помощью, оказываемой в порядке, установленным Школой.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.3. Предоставить Учащемуся на время обучения учебные кабинеты, компьютерный класс, доступ к библиотечным и информационным ресурсам Школы в рамках реализуемых образовательных программ.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.4. Обеспечить Учащегося трехразовым питанием (завтрак, обед и полдник).", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.5. Сохранить место за Учащимся в случае пропуска занятий по уважительным причинам.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.6. Проявлять уважение к личности Учащегося и создать благоприятные условия.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.7. Отвечать за здоровье Учащегося в период его пребывания в Школе, при условии неукоснительного соблюдения им «Правил Внутреннего Распорядка».", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.1.8. Быть уведомлённой письменно (предоставить соответствующие медицинские справки) об индивидуальных аллергических особенностях Учащегося на те или иные продукты.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2. Школа имеет право:", ordBoldUnderlineFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.1. Самостоятельно, с учетом государственных программ, выбирать, разрабатывать и применять учебные программы и методики в процессе обучения Учащегося.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.2. Устанавливать размер родительской платы за оказание дополнительных платных образовательных услуг.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.3. Требовать с родителей (законных представителей) контроль за обучением и поведением Учащегося.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.4. Самостоятельно составлять меню, производить замены блюд.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.5. Отчислить Учащегося из Школы с возмещением стоимости обучения и наложением штрафа в размере 10000 сомов в следующих случаях:", ordFont));
                    ct.addElement(paragraph);

                    paragraph.setFirstLineIndent(40);
                    paragraph.clear();
                    paragraph.add(new Phrase("а) грубого, систематического нарушения «Правил Внутреннего Распорядка» с предоставлением документов, подтверждающих нарушения, совершенные Учащимся,", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("б) противозаконных действий по отношению к сверстникам и персоналу Школы,", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("в) нарушения законодательства Кыргызской Республики.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.setFirstLineIndent(20);
                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.6. Расторгнуть настоящий Договор при условии не освоения Учащимся в установленный годовым календарным планом (графиком) срок образовательных программ, являющихся предметом настоящего Договора.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.7. Самостоятельно перевести Учащегося в параллельную группу (класс).", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.8. Расторгать в одностороннем порядке договор с родителями, нарушающих п.3.1., 3.2. и 3.3. настоящего Договора.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.9. Не предоставлять образовательные услуги ученику, если родитель нарушает график оплаты более чем на 10 банковских дней.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.10. В случае расторжения контракта с родителем ученика после 30-го июня, 3% суммы контракта не подлежит возврату; в случае расторжения контракта в течение учебного года, оплаченная сумма за обучение возвращается по мере возможности Школы, но не позднее мая текущего учебного года.", ordFont));
                    ct.addElement(paragraph);

                    paragraph.clear();
                    paragraph.add(new Phrase("2.2.11. Школа имеет право повышать стоимость оплаты за обучение, исходя из инфляционных процессов в Кыргызстане.", ordFont));
                    ct.addElement(paragraph);


                    ct.go();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                } finally {
                    if (pdfStamper != null) {
                        try {
                            pdfStamper.close();
                        } catch (Exception e) {
                            logger.error(e);
                            logger.catching(e);
                        }
                    }
                }
                b = buffer.toByteArray();
                return new ByteArrayInputStream(b);
            }
        };

        StreamResource resource = new StreamResource(source1, "Contract"
                                                              + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "Contract", false);
    }
}
