package kg.alex.indigo.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.interfaces.IAccessibleElement;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ContractAsylkechPdf_2024_ru {

    static final Logger logger = LogManager.getLogger(ContractAsylkechPdf_2024_ru.class);
    private final static String FONT_LOCATION = "/home/indigo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/indigo/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractAsylkechPdf_2024_ru(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
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

                PdfContentByte punder = writer.getDirectContentUnder();

                float[] mainTableWidth = {1f, 1f};
                PdfPTable mainTable = new PdfPTable(2);
                mainTable.setWidthPercentage(97f);
                mainTable.setWidths(mainTableWidth);
                mainTable.getDefaultCell().setBorder(0);
                mainTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("Об оказании платных образовательных услуг", font_header));
                spr.add(Chunk.NEWLINE);

                mainTable.addCell(createCell(spr, Element.ALIGN_CENTER, 5));

                spr = new Paragraph();
                spr.add(new Phrase("CONTRACT № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("for the provision of paid educational services", font_header));
                spr.add(Chunk.NEWLINE);

                mainTable.addCell(createCell(spr, Element.ALIGN_CENTER, 5));

                float[] table_date_colsWidth = {3.5f, 2.5f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase("г. " + studentInfo.getSchool().getCity(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));

                mainTable.addCell(createCell(table_date, Element.ALIGN_CENTER, 5));

                table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase("Bishkek", ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateEn.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));

                mainTable.addCell(createCell(table_date, Element.ALIGN_CENTER, 5));

                Decliner dcl = new Decliner();

                Paragraph paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("Учреждение " + studentInfo.getSchool().getName_ru(), ordBoldFont));
                paragraph.add(new Phrase(", именуемая в дальнейшем «Школа», в лице директора ", ordFont));
                String fullName = null;
                try {
                    fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                    if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().equals("")) {
                        fullName += " " + studentInfo.getDirector().getMiddle_name();
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName + ", действующего на основании Устава, с одной стороны, и ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName() + ", ", ordBoldFont));
                paragraph.add(new Phrase(" и являющаяся (-щийся) " + studentInfo.getRelative().getRelativeDeclarative()
                        + " «Учащегося» ", ordFont));

                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                try {
                    boolean isFeminine = studentInfo.getStudent().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getStudent().getSurname(), isFeminine) + " "
                            + dcl.DeclineNameGenitive(studentInfo.getStudent().getName(), isFeminine, false);
                    if (!studentInfo.getStudent().getMiddle_name().equals("")) {
                        fullName += " " + dcl.DeclinePatronymicGenitive(studentInfo.getStudent().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(", именуемый(ая) в дальнейшем «" + studentInfo.getRelative().getRelativeTitle() + "» с другой стороны, в интересах обучающегося, в соответствии с пунктом 1 статьи 14 Закона Кыргызской Республики «Об образовании», заключили настоящий Договор о нижеследующем:", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 7));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("Institution named \"" + studentInfo.getSchool().getName_en() + "\"", ordBoldFont));
                paragraph.add(new Phrase(", hereinafter referred to as the \"School\", represented by Director ", ordFont));
                fullName = null;
                try {
                    fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                    if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().equals("")) {
                        fullName += " " + studentInfo.getDirector().getMiddle_name();
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(Settings.transliterate(fullName) + ", acting on the basis of the Order, on the one hand, and ", ordFont));
                paragraph.add(new Phrase(Settings.transliterate(studentInfo.getRelative().getFullName()), ordBoldFont));
                paragraph.add(new Phrase(" being the parent of a \"Student\", ", ordFont));
                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                paragraph.add(new Phrase(Settings.transliterate(fullName), ordBoldFont));
                paragraph.add(new Phrase(" hereinafter referred to as the \"Parent\", on the other hand, in the interests of a student, in accordance with Clause 1 of Article 14 of the Law of the Kyrgyz Republic\" On Education \", have made this Contract as follows:", ordFont));

                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 7));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("1. SUBJECT OF THE CONTRACT", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("1.1. Предметом Договора является организация процесса обучения Учащегося, получение им образования по стандартам «MARSHALL CAVENDISH EDUCATION» (Сингапур) и программ соответствующего уровня образования по кембриджскому учебному плану, на период ", ordFont));
                String[] temp = studentInfo.getYear().split("-");
                paragraph.add(new Phrase("с «29» августа " + temp[0] + " года по «9» июня " + temp[1] + " года.", ordBoldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("1.1. The subject of the Agreement is the organization of the student's learning process, his / her education according to the standards of \"Cambridge Assessment International Education\" and programs of the corresponding level of education according to the British curriculum, for the period ", ordFont));
                paragraph.add(new Phrase("from «29» August " + temp[0] + " to «9» June " + temp[1] + ".", ordBoldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ СТОРОН", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("2. RIGHTS AND OBLIGATIONS OF THE PARTIES", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1. Школа обязана:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1. The School is obliged to:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение услуг, предусмотренных в статье 1.1. настоящего Договора.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.1. Organize and ensure the proper performance of the services provided for in Article 1.1. of this Contract.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.2. В целях усвоения Учащимся образовательных программ, являющихся предметом настоящего Договора, обеспечить Учащегося методической и консультационной помощью, оказываемой в порядке, установленной Школой.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.2. In order to assimilate the students of the educational programs that are the subject of this Contract, to provide the Student with methodological and consulting assistance rendered in the manner prescribed by the School.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.3. Предоставить Учащемуся на время обучения учебные кабинеты, компьютерный класс, библиотеку.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.3. Provide the Student with study rooms, a computer class and the library for the period of study.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.4. Обеспечить Учащегося трехразовым питанием (завтрак, обед и полдник).", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.4. Provide the Student meals, including (breakfast, lunch and snacks).", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.5. Сохранить место за Учащимся в случае пропуска занятий по уважительным причинам.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.5. Hold a seat for the Student in case of missing classes for valid reasons.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.6. Проявлять уважение к личности Учащегося, не допускать физического и психологического насилия, обеспечить условия укрепления нравственного, физического и психологического здоровья, эмоционального благополучия Учащегося с учетом его индивидуальных особенностей.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.6. Show respect for personality of the Student, prevent physical and psychological violence, provide conditions for strengthening the moral, physical and psychological health, emotional well-being of the Student, while considering his/her individual characteristics.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.7. Отвечать за жизнь и здоровье Учащегося в период его пребывания в Школе, при условии неукоснительного соблюдения им «Правил Внутреннего Распорядка».", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.1.7. Have responsibility for the life and health of the Student during his/her stay at the School, subject to his/her strict observance of \"Internal Regulations\".", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2. Школа имеет право:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2. The School has the right to:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.1. Самостоятельно, с учетом государственных программ, выбирать, разрабатывать и применять учебные программы и методики в процессе обучения Учащегося.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.1. Independently, while considering state programs, choose, develop and apply educational programs and methods in the learning process of the Student.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.2. Самостоятельно составлять меню, производить замены блюд.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.2. Make up a menu on his/her own, make substitutions of dishes.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.3. Школа имеет право ежегодно изменять размер предоставленной скидки в оплате, при этом предоставленные скидки действуют только в течении данного учебного года.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.3. The school has the right to change the amount of the   discount provided annually, while the discounts provided are valid only during this academic year.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.4. Отчислить Учащегося из Школы без возмещения стоимости обучения в следующих случаях:", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.4. Expel a Student from School without refund of tuition fees in cases as follows:", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("а) грубого, систематического нарушения «Правил Внутреннего Распорядка» с предоставлением документов, подтверждающих нарушения, совершенные Учащимся,", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("a) gross, repeated violation of \"Internal Regulations\" with the provision of documents confirming violations committed by the Student,", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("б) противозаконных действий по отношению к сверстникам и персоналу Школы,", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("b) illegal actions in relation to peers and School staff,", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("в) нарушения действующего законодательства Кыргызской Республики.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("c) violation of the current legislation of the Kyrgyz Republic.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.5. Расторгнуть настоящий Договор при условии не освоения Учащимся в установленный годовым календарным планом (графиком) срок образовательных программ, являющихся предметом настоящего Договора.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.5. Terminate this Contract, provided that the Student failed to master the educational programs that are the subject of this Contract within the period established by the annual calendar plan (schedule).", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.6. Расторгать в одностороннем порядке договор с родителями, систематически нарушающих п.3.1-3.4 настоящего Договора.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.6. Unilaterally terminate the Contract with parents who repeatedly violate Clauses 3.1-3.4 of this Contract.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.7. В случае расторжения контракта с родителем ученика после 30-го июня, удержанию подлежит 5% от суммы контракта.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.7. In case of termination of the contract with the student's parent after the 30th day of June, 5% of the contract amount is non-refundable.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.8. В целях оповещения образовательного процесса и деятельности Школы, Школа без уведомления учащегося и родителей имеет право размещать фото и видеоматериалы в своих интернет страницах и СМИ.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.2.8. In order to notify the educational process and activities of the School, the School without notifying the student and parents has the right to post photos and videos on its Internet pages and social media.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3. Родители обязаны:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3. Parents are obliged to:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.1. Своевременно вносить взнос за обучение Учащегося в Школе, согласно п. 1.1. настоящего Договора, в сроки, оговоренные в пункте 3.1. -3.4. настоящего Договора. Обеспечить своего ребенка всеми необходимыми канцелярскими принадлежностями для собственного использования (тетради, цветные карандаши, точилки и т.д.).", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.1. Pay the student's tuition fee at the School in due time, in accordance with Clause 1.1. of this Contract, within the time frame specified in Clauses 3.1. – 3.4. of this Contract. Provide own child with all the necessary stationeries for his/her own use (notebooks, colored pencils, sharpeners, etc.).", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.2. Провести медицинскую диспансеризацию Учащегося, предоставить Администрации Школы заключение медицинских специалистов о состоянии здоровья Учащегося перед прибытием его в Школу.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.2. Conduct a medical examination of the Student, provide the School Administration with the conclusion of medical specialists on the student's health condition before his/her arrival at the School.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.3. Заполнить «Анкету учащегося» и «Заявление о предоставлении правдивой информации о состоянии здоровья учащегося».", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.3. Fill out the “Student Questionnaire” and “Application for the provision of reliable information about the student's health status”.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.4. Ознакомить Учащегося с «Правилами внутреннего распорядка». Обеспечить адекватное воздействие на Учащегося в случаях нарушения им «Правил внутреннего распорядка» согласно «Школьному справочнику».", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.4. Introduce the Student to the Internal Regulations. Ensure adequate impact on the Student in cases of violation of the “Internal Regulations” based on school handbook.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.5. Нести полную материальную ответственность за все действия Учащегося, повлекшие за собой порчу или уничтожение имущества Школы.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.3.5. Be fully financially responsible for all actions of the Student that entail damage or destruction of the School property.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.4. Родители имеют право:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.4. Parents have the right to:", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_LEFT, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.4.1. Требовать от Администрации Школы выполнения условий, изложенных в пунктах 2.1.1. – 2.1.7. настоящего Договора.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.4.1. Require the School Administration to fulfill conditions set forth in Clauses 2.1.1. – 2.1.7. of this Contract.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.4.2. Досрочно расторгнуть Договор с возмещением стоимости обучения (за вычетом фактически понесенных расходов в период пребывания Учащегося в Школе) в следующих случаях:", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("2.4.2. Early terminate the Contract with reimbursement of tuition fees (less the actual expenses incurred during the period of Student's stay at the School) in cases as follows:", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("а) внезапной тяжелой болезни Учащегося, делающей невозможным его дальнейшее пребывание в Школе, при наличии официального заключения о состоянии здоровья Учащегося медицинскими специалистами Кыргызской Республики,", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("a) a sudden serious illness of the Student, making it impossible for his/her further stay at the School, subject to provision of an official conclusion on the state of health of the Student by medical specialists of the Kyrgyz Republic,", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("б) нарушения Школой условий, изложенных в п.п. 2.1.1. – 2.1.7 настоящего Договора.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("b) violation by the School of the terms and conditions set forth in Clauses 2.1.1. – 2.1.7 of this Contract.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("3. УСЛОВИЯ ОПЛАТЫ", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("3. TERMS OF PAYMENT", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.1. В Школе установлены следующие сроки оплаты родительского взноса:", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.1. The School Administration has established the following deadlines for paying parental contributions:", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.2. Оплата производится в 4 взноса. Первый взнос производится при заключении настоящего договора, остальная часть в первую неделю сентября, января и марта. При этом последний взнос должен быть внесен не позднее 31 Апреля, следующего года. Ученики, не оплатившие сумму договора в указанное время, не будут допущены к занятиям.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.2. The Contract shall be paid in 4 installments. The first installment is due at the conclusion of the contract; the rest is due during the first week of September, January and March respectively. In addition, the last installment shall be made no later than the 31st day of April of the next year. Students whose tuition is not paid on time will not be admitted to the lessons.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.3. Первоначальный взнос для вновь поступивших учеников составляет 30% от общей стоимости контракта, остальная часть вносится в первую неделю сентября, ноября и января.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.3. The first installment for new students is 30% of the total fee; the rest is due during the first week of September, November and January respectively.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.4. Оплата родителями или лицами их заменяющими, родительского взноса ", ordFont));
                paragraph.add(new Phrase("производится в сомах ", ordBoldFont));
                paragraph.add(new Phrase(", не позднее 3 календарных дней с даты, указанной в официальном счете/ invoice.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.4. Payment by parents, or persons replacing them, of the parental contribution ", ordFont));
                paragraph.add(new Phrase("shall be made in KG soms ", ordBoldFont));
                paragraph.add(new Phrase("no later than 3 calendar days from the date specified in the official invoice.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.5. Общая стоимость родительских взносов составляет ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract()), ordBoldFont));
                paragraph.add(new Phrase(" долл. США, которая производится строго в сомах на основе сегодняшнего курса валюты.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.5. The total amount of parental contributions is ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract()), ordBoldFont));
                paragraph.add(new Phrase(" US dollars, which is made strictly in KG soms based on today’s exchange rate.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.6. Учебники и питание предоставляется Школой бесплатно.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.6. Textbooks and meals are provided by the school on a free of charge basis.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.7. Школьная форма предоставляется ", ordFont));
                paragraph.add(new Phrase("1 раз при поступлении ", ordBoldFont));
                paragraph.add(new Phrase("в Школу. В случае необходимости дополнительная школьная форма предоставляется за отдельную оплату за каждую единицу формы.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.7. School uniforms are provided ", ordFont));
                paragraph.add(new Phrase("once upon admission ", ordBoldFont));
                paragraph.add(new Phrase("to school. If necessary, an additional school uniform is provided for a separate payment for each unit of the uniform.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.8. Оплата за обучение не включает в себя оплату за официальный экзамен «Cambridge Assessment International Education».", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("3.8. Tuition fees do not include fees for the official Cambridge Assessment International Education exam.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("4. ФОРС-МАЖОР", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("4. FORCE MAJEURE", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("4.1. Ни одна из сторон не несет ответственности за полное или частичное неисполнение своих обязательств при возникновении обстоятельств, которые делают полностью или частично невозможным выполнением Договора одной из сторон, а именно: пожар, стихийное природное бедствие (землетрясение, наводнение и др.), война, военные действия всех видов, забастовка, блокада, эпидемия, изменение текущего Законодательства Кыргызской Республики и другие возможные обстоятельства непреодолимой силы, не зависящие от сторон, подписавших Договор.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("4.1. Neither party is responsible for full or partial failure to fulfill its obligations in the event of circumstances that make it completely or partially impossible for one of the parties to fulfill the Contract, namely: fire, natural disaster (earthquake, flood, etc.), war, military actions of all kinds, strike, blockade, epidemic, changes in the current legislation of the Kyrgyz Republic and other possible force majeure circumstances beyond the reasonable control of the parties who signed the Contract.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("5. СРОК ДЕЙСТВИЯ ДОГОВОРА, ПОРЯДОК ИЗМЕНЕНИЯ, ДОПОЛНЕНИЯ И РАСТОРЖЕНИЯ", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("5. CONTRACT VALIDITY PERIOD, PROCEDURE FOR CHANGES, ADDITIONS AND TERMINATIONS", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.1. Настоящий Договор вступает в силу с момента его подписания обеими сторонами. Договор считается продленным на следующий академический год на тех же условиях, если ни одна из сторон не заявит о его расторжении не позднее чем за 30 дней до расчётной даты прекращения действия договора.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.1. This Contract comes into force from the moment of its signing by both parties. The contract is considered extended for the next academic year on the same terms, if neither party declares its termination no later than 30 days before the mentioned date of termination of the contract.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.2. Настоящий Договор может быть расторгнут досрочно, согласно п.2.2.3. и 2.4.2.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.2. This Contract can be terminated early in accordance with Clauses 2.2.3. and 2.4.2.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.3. Любые дополнения и изменения к настоящему Договору действительны лишь при условии, что они совершены в письменной форме и подписаны обеими сторонами.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.3. Any additions and changes to this Contract are valid provided that they are made in writing and signed by both parties only.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.4. Все разногласия по данному Договору решаются сторонами в порядке переговоров. В случае невозможности разрешения спора путем переговоров, они решаются в порядке, установленном законодательством Кыргызской Республики.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.4. All disputes under this Contract shall be resolved by the parties through negotiations. If it is impossible to resolve the dispute through negotiations, they shall be resolved in the manner prescribed by the legislation of the Kyrgyz Republic.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.5. Родителям при заключении настоящего договора необходимо предоставить следующие документы: оригинал документа, удостоверяющий личность Родителей либо законного представителя, свидетельство о рождении ребенка оригинал, и другие документы по запросу специалиста.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.5. Parents must provide the following documents to conclude a contract: the original identity document of the Parents or legal representative, the child's original birth certificate, and other documents which admission requests.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.6. Настоящий Договор составлен в двух экземплярах на русском и английском языках и подписан обеими сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из сторон находится один экземпляр настоящего Договора.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.setTabSettings(new TabSettings(15f));
                paragraph.add(Chunk.TABBING);
                paragraph.add(new Phrase("5.6. This Contract is made in two original copies in Russian and in English languages and signed by both parties. Both original copies are identical and have the same legal force. Each of the parties has one original copy of this Contract.", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("6. РОДИТЕЛЬ И УЧЕНИК ДОЛЖНЫ ОЗНАКОМИТЬСЯ С УСЛОВИЯМИ КОНТРАКТА И ПОДПИСАТЬ", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("6. PARENT AND STUDENT SHALL READ THE TERMS AND CONDITIONS OF THE CONTRACT AND SIGN IT", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("Я ознакомлен(а) с условиями настоящего договора и согласен(на)", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("I have read the terms and conditions of this Contract and accept them", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                PdfTemplate template = writer.getDirectContent().createTemplate(30, 20);
                template.setColorFill(BaseColor.WHITE);
                template.rectangle(0, 0, 30, 20);
                template.fill();
                writer.releaseTemplate(template);
                Image image = Image.getInstance(template);
                image.setBorderColor(BaseColor.BLACK);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(1);
                mainTable.addCell(createCell(image, Element.ALIGN_CENTER, 5));

                template = writer.getDirectContent().createTemplate(30, 20);
                template.setColorFill(BaseColor.WHITE);
                template.rectangle(0, 0, 30, 20);
                template.fill();
                writer.releaseTemplate(template);
                image = Image.getInstance(template);
                image.setBorderColor(BaseColor.BLACK);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(1);
                mainTable.addCell(createCell(image, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("7. РЕКВИЗИТЫ СТОРОН", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("7. DETAILS OF THE PARTIES", boldFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 5));

                String[] banks = studentInfo.getSchool().getBank().split("<br>");
                String[] bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");

                paragraph = new Paragraph();
                paragraph.add(new Phrase("Школа: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getName_ru(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Адрес: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getAddress(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("ИНН: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getInn(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("ОКПО: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getOkpo(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                for (int i = 0; i < banks.length; i++) {
                    paragraph.add(new Phrase("Банк: ", ordBoldFont));
                    paragraph.add(new Phrase(banks[i], ordFont));
                    paragraph.add(Chunk.NEWLINE);
                    paragraph.add(new Phrase("Р/счет (мультив.счёт): ", ordBoldFont));
                    paragraph.add(new Phrase(bankAccounts[i], ordFont));
                    paragraph.add(Chunk.NEWLINE);
                }
                paragraph.add(new Phrase("БИК: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getBik(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("SWIFT: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getSwift(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Тел.: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getPhone(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Директор Школы: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getDirector().getSurname() + " "
                        + studentInfo.getDirector().getName() + " " +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                paragraph.add(Chunk.NEWLINE);
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("School: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getName_en(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Address: ", ordBoldFont));
                paragraph.add(new Phrase(Settings.transliterate(studentInfo.getSchool().getAddress()), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("INN: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getInn(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("OKPO: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getOkpo(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                for (int i = 0; i < banks.length; i++) {
                    paragraph.add(new Phrase("Bank: ", ordBoldFont));
                    paragraph.add(new Phrase(Settings.transliterate(banks[i]), ordFont));
                    paragraph.add(Chunk.NEWLINE);
                    paragraph.add(new Phrase("A/c: ", ordBoldFont));
                    paragraph.add(new Phrase(bankAccounts[i], ordFont));
                    paragraph.add(Chunk.NEWLINE);
                }
                paragraph.add(new Phrase("BIC: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getBik(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("SWIFT: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getSwift(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Tel.: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getPhone(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Director: ", ordBoldFont));
                paragraph.add(new Phrase(Settings.transliterate(studentInfo.getDirector().getSurname()) + " "
                        + Settings.transliterate(studentInfo.getDirector().getName()) + " " +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : Settings.transliterate(studentInfo.getDirector().getMiddle_name())), ordFont));
                paragraph.add(Chunk.NEWLINE);
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("(М.П.)", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 10));

                paragraph = new Paragraph();
                paragraph.add(new Phrase("(Seal)", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_CENTER, 10));

                paragraph = new Paragraph();
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("ФИО Родителя: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Адрес проживания: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getAddress(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Данные паспорта: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getPassport(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Тел: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getPhone(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Подпись: ", ordBoldFont));
                paragraph.add(new Phrase("-----------------------", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));

                paragraph = new Paragraph();
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Parent’s full name: ", ordBoldFont));
                paragraph.add(new Phrase(Settings.transliterate(studentInfo.getRelative().getFullName()), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Residence address: ", ordBoldFont));
                paragraph.add(new Phrase(Settings.transliterate(studentInfo.getRelative().getAddress()), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Passport Details: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getPassport(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Tel: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getPhone(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Signature: ", ordBoldFont));
                paragraph.add(new Phrase("-----------------------", ordFont));
                mainTable.addCell(createCell(paragraph, Element.ALIGN_JUSTIFIED, 0));
                document.add(mainTable);


                document.newPage();
                PdfPTable planTable = new PdfPTable(2);
                planTable.setWidthPercentage(97f);
                planTable.setWidths(mainTableWidth);
                planTable.getDefaultCell().setBorder(0);
                planTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                Paragraph text15 = new Paragraph();
                text15.setIndentationLeft(30);
                text15.setIndentationRight(30);
                text15.add(new Phrase("График оплаты", boldFont));
                text15.add(Chunk.NEWLINE);
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
                planTable.addCell(text15);

                text15 = new Paragraph();
                text15.setIndentationLeft(30);
                text15.setIndentationRight(30);
                text15.add(new Phrase("Payment Schedule", boldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Student's ID: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getLogin(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Student's Fullname: ", ordFont));
                text15.add(new Phrase(Settings.transliterate(studentInfo.getStudent().getSurname()) + " "
                        + Settings.transliterate(studentInfo.getStudent().getName()) + " "
                        + Settings.transliterate(studentInfo.getStudent().getMiddle_name() != null ?
                        studentInfo.getStudent().getMiddle_name() : " "), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Class.: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getClass_name(), ordBoldFont));
                text15.add(new Phrase(". Registration Date: ", ordFont));
                text15.add(new Phrase(Settings.df.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("TOTAL Fee: ", ordFont));
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) + ""), ordBoldFont));
                text15.add(new Phrase(" US dollars.", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Debt from previous year: ", ordFont));
                } else {
                    text15.add(new Phrase("Overpayment from previous year: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + ""), ordBoldFont));
                text15.add(new Phrase(" US dollars.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Discount: ", ordFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
                    text15.add(new Phrase(" (Discount type in words, %)", ordFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Correction: ", ordFont));
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Prepayment: ", ordFont));
                text15.add(new Phrase(studentInfo.getContractInfo().getInitialPayment() == null ? "0.00" :
                        Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + "", ordBoldFont));
                text15.add(new Phrase(" US dollars.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Remain: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + "", ordBoldFont));
                text15.add(new Phrase(" US dollars.", ordFont));
                planTable.addCell(text15);
                document.add(planTable);
                document.add(new Paragraph(10, " "));

                Paragraph text16 = new Paragraph();
                text16.setIndentationLeft(30);
                text16.setIndentationRight(30);
                text16.add(new Phrase("Таблица 1. / Table 1.", ordBoldFont));
                document.add(text16);
                document.add(new Paragraph(10, " "));
                text16.add(Chunk.NEWLINE);

                float[] TContract_colsWidth = {0.15f, 0.5f, 0.6f, 1f, 0.8f};
                PdfPTable TContract = new PdfPTable(5);
                TContract.setWidthPercentage(90f);
                TContract.setWidths(TContract_colsWidth);
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                TContract.addCell(new Phrase("№", ordBoldFont));
                TContract.addCell(new Phrase("Дата оплаты \nPayment Date", ordBoldFont));
                TContract.addCell(new Phrase("Сумма \nAmount", ordBoldFont));
                TContract.addCell(new Phrase("Потверждающий документ \nConfirmation Document", ordBoldFont));
                TContract.addCell(new Phrase("Подпись \nSignature", ordBoldFont));
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
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
                TContract.addCell(new Phrase("Итого / Total:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + "", ordBoldFont));
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("", ordFont));

                document.add(TContract);

                PdfPTable signaturesTable = new PdfPTable(2);
                signaturesTable.setWidthPercentage(97f);
                signaturesTable.setWidths(mainTableWidth);
                signaturesTable.getDefaultCell().setBorder(0);
                signaturesTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Подпись Родителя: ", ordBoldFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Директор: ", ordBoldFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Гл. бухгалтер: ", ordBoldFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Печать ", ordBoldFont));
                signaturesTable.addCell(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Parent's signature: ", ordBoldFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Director: ", ordBoldFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Head Accountant: ", ordBoldFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Stamp", ordBoldFont));
                signaturesTable.addCell(paragraph);
                document.add(signaturesTable);
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

    private PdfPCell createCell(IAccessibleElement e, int alignment, int topPadding) {

        PdfPCell cell = null;
        if (e instanceof Paragraph) {
            cell = new PdfPCell((Paragraph) e);
        } else if (e instanceof PdfPTable) {
            cell = new PdfPCell((PdfPTable) e);
        } else if (e instanceof Image) {
            cell = new PdfPCell((Image) e);
        }
        cell.setBorder(0);
        cell.setHorizontalAlignment(alignment);
        cell.setLeading(14f, 0);
        cell.setPaddingLeft(8);
        cell.setPaddingRight(8);
        if (topPadding != 0) {
            cell.setPaddingTop(topPadding);
        }
        return cell;
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
