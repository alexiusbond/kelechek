package kg.alex.indigo.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.i18n.IndigoMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ContractCambridgePdf_en {

    static final Logger logger = LogManager.getLogger(ContractCambridgePdf_en.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private Document document = null;
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;


    private final static String FONT_LOCATION = "/home/indigo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/indigo/TimesNewRomanBold.ttf";

    public ContractCambridgePdf_en(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 80, 30);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 11);
                Font ordBoldFont = new Font(baseFontBold, 11);
                Font boldFont = new Font(baseFontBold, 12);
                Font font_header = new Font(baseFontBold, 13);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Student Contract № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("by parental contributions", font_header));
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
                table_date.addCell(new Phrase(" ", ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateEn.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(studentInfo.getSchool().getName_en(), ordBoldFont));
                paragraph.add(new Phrase(", here in after referred to as the “School”, represented by Director ", ordFont));
                String fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name()!=null && !studentInfo.getDirector().getMiddle_name().equals("")) {
                    fullName += " " + studentInfo.getDirector().getMiddle_name();
                }
            paragraph.add(new Phrase(Settings.transliterate(fullName), ordBoldFont));
            paragraph.add(new Phrase(",  acting on the basis of the Charter, on the one hand, and ", ordFont));
            paragraph.add(new Phrase(Settings.transliterate(studentInfo.getRelative().getRelativeTitle()) + ", ", ordBoldFont));
            paragraph.add(new Phrase("being a parent of the “Student” ", ordFont));
            paragraph.add(new Phrase(Settings.transliterate(studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName()), ordBoldFont));
            paragraph.add(new Phrase(", hereinafter referred to as the “Parent” on the other hand, in the interests of the student in accordance with paragraph 1 of article 14 of the Law of the Kyrgyz Republic “On education”, have concluded the present Contract on the following basis:", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.add(new Phrase("1. Subject of the Contract", boldFont));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            String[] temp = studentInfo.getYear().split("-");
            paragraph.add(new Phrase("1.1. Subject of the Contract is the organization of learning process of the Student, his/her education under Cambridge Assessment International Education Standard and programs of appropriate educational level according to the British curriculum, for the period from «25» August " + temp[0] + " to «12» June " + temp[1] + ".", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.add(new Phrase("2. Rights and Obligations of the Parties", boldFont));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.add(new Phrase("2.1. The School is obliged to:", boldFont));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(Chunk.NEWLINE);
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.1.1. Organize and ensure the proper execution of services specified in article 1.1 of the present Contract. Educational services are provided in accordance with Cambridge Assessment International Education Standard and programs of appropriate educational levels according to the British curriculum.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.1.2. In order to assimilate the educational programs by the Student, which are subject of the present Contract, to ensure the Student with methodical and consulting assistance in the order established by the School.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.1.3. Provide the Student with classrooms, computer rooms, science laboratories and a library during the period of study.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.1.4. Provide student meals, including snack, lunch and dinner.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.1.5. Keep the students place in case of absenteeism for valid reasons.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.1.6. Show respect for personality of the Student, to avoid physical and psychological violence, ensure the conditions of the strengthening of moral, physical and mental health, the emotional well-being of the Student, by taking into account his/her individual needs.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.1.7. Be responsible for the life and health of the Student during his tenure at the School subject to strict compliance with the “Internal Regulations School Handbook”.", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.add(new Phrase("2.2 The School is entitled to:", boldFont));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(Chunk.NEWLINE);
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.2.1. Independently, taking into account the state programs, select, develop and use curricula and methodologies in the learning process of Student.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.2.2. Prepare the school menu.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.2.3. Expel the Student from the School without compensation of tuition in the following cases:", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("a) gross, systematic violations of the “Internal Regulations School Handbook”, providing with documents which confirm the violations committed by the Student.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("b) unlawful acts relative to peers and staff of the School.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("c) violations of the legislation of the Kyrgyz Republic.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.2.4. Terminate the present Contract, subject to not learning the educational programs by the Student in the time specified by annual calendar plan (schedule) that are subject of the present Contract.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.2.5. Independently choose which homeroom the student is placed.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.2.6. Terminate the contract in parents consistently violate the conditions laid down in section 2.3.1 of the present contract.", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.add(new Phrase("2.3. The Parents are obliged to:", boldFont));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(Chunk.NEWLINE);
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.3.1. Pay a fee for the Student’s education in the School timely, according to paragraph 1.1. of the present Contract, within the time limits referred to in paragraph 3.1.-3.4. of the present Contract. Provide your child with all the necessary stationery for his/her own use (notebooks, pencils, etc.).", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.3.2. Conduct medical check-up of the Student, provide the School Administration with medical assessment on state of health of the Student before his/her arrival to the School.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.3.3. Fill in the “Application form for the Student” and the “Statement for providing accurate information on the state of health of the Student”».", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.3.4. Familiarize the Student with the “Internal Regulations School Handbook”, to ensure adequate impact on the Student in case if he/she violates the “Internal Regulations School Handbook”.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.3.5. Bear full material responsibility for all the actions, resulting in damage or destruction of the School’s property.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.3.6. Transport children to and from the School if they choose not to use school transportation.", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.add(new Phrase("2.4. The Parents are entitled to:", boldFont));
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(Chunk.NEWLINE);
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.4.1. Demand the School Administration to fulfill the conditions set out in paragraphs 2.1.1. – 2.1.7. of the present Contract.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("2.4.2. Terminate the Contract with reimbursement of tuition costs, minus actual costs incurred during the Student’s tenure at the School in following cases:", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("a) of sudden serious illness of the Student, which makes it impossible for him/her  to remain at the School, if he/she has an official medical assessment by medical experts of the Kyrgyz Republic on the state of health of the Student.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("b) violations by the School of the conditions laid down in p.p.2.1.1.-2.1.7. of the present Contract.", ordFont));
            document.add(paragraph);

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("c) by reason of the transfer to another school.", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(new Phrase("3. Terms of Payment", boldFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.add(new Phrase("3.1. The Parent is obliged to pay total amount for the agreed period, regardless of the Student’s visits of classes, excluding reasons determined in section 2.4.2.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("3.2. The tuition fees are to be paid on time according to the agreed payment schedule which includes in an appendix № 1 page 5. Students whose tuition is not paid on time will not be admitted to the lessons.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("3.3. The cost of the Student’s tuition fee through parental contributions is ", ordFont));
            paragraph.add(new Phrase(studentInfo.getContractInfo().getContract() + " USD.", ordBoldFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("3.4. Course books, transportation and meals are provided by school free of charge.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("If you choose to use school transportation please be aware that buses will depart at 17.40.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("3.5. Uniform is provided once, when the student starts the school. If extra uniform is required parents must pay for each additional piece of clothing.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("3.6. Tuition fee does not include official Cambridge Assessment International Education exams.", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(new Phrase("4. Force Majeure", boldFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("4.1. Neither party shall be responsible for the total or partial non-performance of his/her obligations if there are circumstances which make it impossible to execute all or part of the Contract by one of the parties, namely: fire, natural disaster (earthquake, flood, etc.), war, hostilities, strikes, blockades, changes of the current legislation of the Kyrgyz Republic and other possible circumstances of force majeure beyond the control of the parties, signatories to the present Contract.", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(new Phrase("5. Duration of the Contract, procedure of amending, annexation and termination", boldFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("5.1. The present Contract shall enter into force upon its signature by both parties.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("5.2. The present Contract may be terminated ahead of the schedule, according to paragraph 2.2.3. and 2.4.2.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("5.3. Any additions and changes to the present Contract are valid only if they are made in the written form and signed by both parties.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("5.4. All disputes under the present Contract are settled by the parties by means of negotiations. In case of failure to settle the disputes through negotiations, they are solved in the order established by the legislation of the Kyrgyz Republic.", ordFont));
            document.add(paragraph);
            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.add(new Phrase("5.5. The present Contract is drawn up in two copies and signed by both parties. Both copies are identical and have the same legal power. Each of the parties has one copy of the present Contract", ordFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            paragraph = new Paragraph();
            paragraph.setFirstLineIndent(30);
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.setLeading(15);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add(new Phrase("6. Requisites of the Parties", boldFont));
            document.add(paragraph);
            document.add(new Paragraph(10, " "));

            float[] table_info_colsWidth = {1.5f, 1f};
            PdfPTable table_info = new PdfPTable(2);
            table_info.getDefaultCell().setBorder(0);
            table_info.setWidthPercentage(90f);
            table_info.setWidths(table_info_colsWidth);
            Paragraph text10 = new Paragraph();
            text10.add(new Phrase("School: " + studentInfo.getSchool().getName_en(), ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("Address:  Bishkek city, str. A.Masalieva 26", ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("ID: " + studentInfo.getSchool().getInn(), ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("RNNBO: 30404367", ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("“Optima Bank” JSC", ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("BIC: 109008", ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("R/account: " + studentInfo.getSchool().getBank_account(), ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("Tel.: " + studentInfo.getSchool().getPhone(), ordFont));
            text10.add(Chunk.NEWLINE);
            text10.add(new Phrase("Director of the School: __________________ (Signature)", ordFont));
            table_info.addCell(text10);

            Paragraph text11 = new Paragraph();
            text11.add(new Phrase("Parent: " + Settings.transliterate(studentInfo.getRelative().getFullName()), ordFont));
            text11.add(Chunk.NEWLINE);
            text11.add(new Phrase("Address: " + Settings.transliterate(studentInfo.getRelative().getAddress()), ordFont));
            text11.add(Chunk.NEWLINE);
            text11.add(new Phrase("Passport No: " + studentInfo.getRelative().getPassport(), ordFont));
            text11.add(Chunk.NEWLINE);
            text11.add(new Phrase("Tel.: " + studentInfo.getRelative().getPhone(), ordFont));
            text11.add(Chunk.NEWLINE);
            text11.add(new Phrase("Parent: __________________ (Signature)", ordFont));
            table_info.addCell(text11);

            document.add(table_info);
            document.add(new Paragraph(10, " "));

            Paragraph text15 = new Paragraph();
            text15.setIndentationLeft(30);
            text15.setIndentationRight(30);
            text15.add(new Phrase("Payment Schedule", boldFont));
            text15.add(Chunk.NEWLINE);
            text15.add(new Phrase("Student Fullname: ", ordFont));
            text15.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                    + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordBoldFont));
            text15.add(Chunk.NEWLINE);
            text15.add(new Phrase("Class: ", ordFont));
            text15.add(new Phrase(studentInfo.getStudent().getClass_name(), ordBoldFont));
            text15.add(new Phrase(" Registration Date: ", ordFont));
            text15.add(new Phrase(df.format(new Date()), ordBoldFont));
            text15.add(Chunk.NEWLINE);
            text15.add(new Phrase("TOTAL FEE: ", ordFont));
            text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) + ""), ordBoldFont));
            text15.add(new Phrase(" USD.", ordFont));
            text15.add(Chunk.NEWLINE);
            if (studentInfo.getContractInfo().getDebt() >= 0) {
                text15.add(new Phrase("Debt from previous year: ", ordFont));
            } else {
                text15.add(new Phrase("Overpayment from previous year: ", ordFont));
            }
            text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + ""), ordBoldFont));
            text15.add(new Phrase(" USD.", ordFont));
            text15.add(Chunk.NEWLINE);
            text15.add(new Phrase("Discount: ", ordFont));
            if (studentInfo.getContractInfo().getDiscountStr() != null) {
                text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
            }
            text15.add(Chunk.NEWLINE);
            text15.add(new Phrase("Correction: ", ordFont));
            if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                text15.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordBoldFont));
            }
            text15.add(Chunk.NEWLINE);
            text15.add(new Phrase("Prepayment: ", ordFont));
            text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + "", ordBoldFont));
            text15.add(new Phrase(" USD.", ordFont));
            text15.add(Chunk.NEWLINE);
            text15.add(new Phrase("Remainder: ", ordFont));
            text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + "", ordBoldFont));
            text15.add(new Phrase(" USD.", ordFont));
            document.add(text15);
            document.add(new Paragraph(10, " "));

            Paragraph text16 = new Paragraph();
            text16.setIndentationLeft(30);
            text16.setIndentationRight(30);
            text16.add(new Phrase("Table 1.", ordBoldFont));
            document.add(text16);
            document.add(new Paragraph(10, " "));
            text16.add(Chunk.NEWLINE);

            float[] TContract_colsWidth = {1f, 4f, 4f, 4f, 4f};
            PdfPTable TContract = new PdfPTable(5);
            TContract.setWidthPercentage(90f);
            TContract.setWidths(TContract_colsWidth);
            TContract.addCell(new Phrase("№", ordBoldFont));
            TContract.addCell(new Phrase("Payment Date", ordBoldFont));
            TContract.addCell(new Phrase("Amount", ordBoldFont));
            TContract.addCell(new Phrase("Confirmatory document", ordBoldFont));
            TContract.addCell(new Phrase("Signature ", ordBoldFont));
            int n = 1;
            for (Object obj : instPlanCont.getItemIds()) {
                TContract.addCell(new Phrase(n + "", ordFont));
                TContract.addCell(new Phrase(((DateField) instPlanCont.getContainerProperty(obj,
                        myUI.getMessage(IndigoMessages.Date)).getValue()).getValue().toString(), ordFont));
                TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                        myUI.getMessage(IndigoMessages.Amount)).getValue()).getValue(), ordFont));
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("", ordFont));
                n += 1;
            }
            TContract.addCell(new Phrase("", ordFont));
            TContract.addCell(new Phrase("Total:", ordBoldFont));
            TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + "", ordBoldFont));
            TContract.addCell(new Phrase("", ordFont));
            TContract.addCell(new Phrase("", ordFont));

            document.add(TContract);

            paragraph = new Paragraph();
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.add(Chunk.NEWLINE);
            paragraph.add(new Phrase("Parent Signature: ", ordBoldFont));
            document.add(paragraph);
            document.add(new Paragraph(5, " "));
            paragraph = new Paragraph();
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.add(new Phrase("Director: ", ordBoldFont));
            document.add(paragraph);
            document.add(new Paragraph(5, " "));
            paragraph = new Paragraph();
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.add(new Phrase("Accountant: ", ordBoldFont));
            document.add(paragraph);
            document.add(new Paragraph(5, " "));
            paragraph = new Paragraph();
            paragraph.setIndentationLeft(30);
            paragraph.setIndentationRight(30);
            paragraph.add(new Phrase("Stamp ", ordBoldFont));
            document.add(paragraph);
        } catch(Exception e){
            logger.error(e);
            logger.catching(e);
        } finally{
            if (document != null) {
                document.close();
            }
        }

        b = buffer.toByteArray();
        return new ByteArrayInputStream(b);

    }

    ;

    String nameOf = "Contract";
    StreamResource resource = new StreamResource(source1, nameOf
            + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().

    open(resource, nameOf, false);
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
        try {
            BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                    BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            Font fontGray = new Font(baseFont, 10, Font.NORMAL, BaseColor.RED);
            Font ordFont = new Font(baseFont, 11.5f, Font.NORMAL);
            Font footerFontGray = new Font(baseFont, 6, Font.NORMAL, BaseColor.GRAY);
            Chunk SEPARATOR = new Chunk(
                    new LineSeparator(0.1f, 100, BaseColor.GRAY, Element.ALIGN_CENTER, 15f));

            ColumnText ct = new ColumnText(writer.getDirectContent());
            Rectangle rect = new Rectangle(36, 0, 560, 830);

            float[] col_widths = {3.3f, 0.6f, 2.7f};
            PdfPTable t = new PdfPTable(3);
            t.setWidthPercentage(100f);
            t.setWidths(col_widths);
            t.getDefaultCell().setBorder(0);
            t.getDefaultCell().setBorderColor(BaseColor.GRAY);
            t.getDefaultCell().setBorderWidthBottom(0.1f);
            t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            t.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph p = new Paragraph();
            p.add(new Phrase("Международная школа \n«Юнайтед Ворлд Интернэшнл Скул»", ordFont));
            p.add(Chunk.NEWLINE);
            p.add(new Phrase("(Аккредитованные программы Cambridge Assessment \n и Advanced Placement)", fontGray));
            t.addCell(p);

            Image logo = Image.getInstance(Settings.PATH_TO_UPLOADS + "cambridge_logo.png");
            logo.setAlignment(Image.MIDDLE);
            logo.scaleToFit(50, 50);
            Chunk chunk = new Chunk(logo, 0, -20);
            t.addCell(new Phrase(chunk));
            p = new Paragraph();
            p.add(new Phrase("United World International School", ordFont));
            p.add(Chunk.NEWLINE);
            p.add(new Phrase("(Cambridge Assessment \n and AP Accredited Programs)", fontGray));
            t.addCell(p);

            ct.setSimpleColumn(rect);
            ct.setAlignment(Element.ALIGN_CENTER);
            ct.addElement(t);
            p = new Paragraph(8);
            p.setAlignment(Element.ALIGN_CENTER);
            p.add(new Phrase("Инн: 02705201910356    ОКПО: 30404367     Код ГНИ: 001 Октябрьский     Соц.Фонд: 104100013276     Банк: ОАО “Оптима Банк”    р/с: 1090805952270150     Бик: 109008", footerFontGray));
            p.add(Chunk.NEWLINE);
            p.add(new Phrase("Адрес: г. Бишкек, ул. А.Масалиева 26, 720016    Телефоны: +996 (312) 884129, +996 (551) 932222     www.uwis.edu.kg     info@uwis.edu.kg", footerFontGray));
            ct.addElement(p);
            ct.addElement(SEPARATOR);
            ct.go();

        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

}
}
