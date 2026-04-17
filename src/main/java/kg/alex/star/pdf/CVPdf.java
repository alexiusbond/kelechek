/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Table;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.utils.Settings;
import kg.alex.star.domain.Employee;
import kg.alex.star.domain.EmployeeExtraInfo;
import kg.alex.star.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CVPdf {

    static final Logger logger = LogManager.getLogger(CVPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private final Subject currentUser = SecurityUtils.getSubject();
    private Font fontBold;
    private Font captionFont;
    private Font ordFont;
    private Font ordFontBold;

    public CVPdf(final MyVaadinUI myUI, Employee employee, EmployeeExtraInfo employeeExtraInfo, String year,
                 List<Table> tableList) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/star/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/star/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 20, 20, 20, 20);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    fontBold = new Font(baseFontBold, 15);
                    captionFont = new Font(baseFontBold, 12);
                    ordFont = new Font(baseFont, 9);
                    ordFontBold = new Font(baseFontBold, 9);

                    document.open();

                    float[] table_data_colsWidth = {1.5f, 1.0f, 0.9f, 1.2f};
                    PdfPTable dataTable = new PdfPTable(4);
                    dataTable.setWidthPercentage(100f);
                    dataTable.setWidths(table_data_colsWidth);
                    dataTable.getDefaultCell().setBorder(0);
                    dataTable.getDefaultCell().setPaddingLeft(5);
                    dataTable.getDefaultCell().setPaddingRight(5);

                    float[] table_right_colsWidth = {1.0f};
                    PdfPTable rightTable = new PdfPTable(1);
                    rightTable.setWidthPercentage(95f);
                    rightTable.setWidths(table_right_colsWidth);
                    rightTable.getDefaultCell().setBorder(0);

                    Paragraph paragraph;
                    paragraph = new Paragraph();
                    paragraph.setAlignment(Element.ALIGN_LEFT);
                    paragraph.add(new Phrase(employee.getSurname().toUpperCase()
                            + " " + employee.getName().toUpperCase(), fontBold));
                    rightTable.addCell(paragraph);

                    java.util.Calendar now = java.util.Calendar.getInstance();
                    now.setTime(new Date());
                    java.util.Calendar bd = java.util.Calendar.getInstance();
                    bd.setTime(employee.getBirth_date());

                    paragraph = new Paragraph();
                    paragraph.setAlignment(Element.ALIGN_RIGHT);
                    paragraph.add(new Phrase((now.get(java.util.Calendar.YEAR) - bd.get(java.util.Calendar.YEAR))
                            + " " + Settings.generateYearPostfix(now.get(java.util.Calendar.YEAR) - bd.get(Calendar.YEAR)), captionFont));
                    rightTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    rightTable.addCell(paragraph);
                    rightTable.addCell(new Phrase(" "));

                    rightTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                    Image logo = Image.getInstance(Settings.PATH_TO_UPLOADS_HR + employee.getPhoto());
                    logo.setAlignment(Image.MIDDLE);
                    logo.scaleAbsolute(80, 80);
                    PdfPCell cell = new PdfPCell(logo);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBorder(0);
                    rightTable.addCell(cell);
                    rightTable.addCell(new Phrase(" "));
                    dataTable.addCell(rightTable);

                    float[] table_main_info_colsWidth = {1.0f, 1.0f};
                    PdfPTable mainInfoTable = new PdfPTable(2);
                    mainInfoTable.setWidthPercentage(80f);
                    mainInfoTable.setWidths(table_main_info_colsWidth);
                    mainInfoTable.getDefaultCell().setBorder(0);
                    mainInfoTable.getDefaultCell().setBorderWidthBottom(0.3f);
                    cell = new PdfPCell(new Phrase(myUI.getMessage(Messages.MainInfo) + ": ", captionFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setColspan(2);
                    cell.setBorder(0);
                    cell.setBorderWidthBottom(0.3f);
                    mainInfoTable.addCell(cell);
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.Id), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employee.getLogin(), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.LastName), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employee.getSurname(), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.FirstName), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employee.getName(), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.MiddleName), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employee.getMiddle_name() != null ? employee.getMiddle_name() : "", ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.Gender), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employeeExtraInfo.getGender(), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.DateAndPlaceOfBirth), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(Settings.df.format(employee.getBirth_date()) +
                            (employeeExtraInfo.getBirth_place() == null ?
                                    "" : ", " + employeeExtraInfo.getBirth_place()), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.Nationality), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employeeExtraInfo.getNationality(), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.Citizenship), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employeeExtraInfo.getCitizenship(), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.MartialStatus), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employeeExtraInfo.getMartialStatus(), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    mainInfoTable.addCell(new Phrase(myUI.getMessage(Messages.HealthStatus), ordFont));
                    mainInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    mainInfoTable.addCell(new Phrase(employeeExtraInfo.getHealth_notes(), ordFont));

                    cell = new PdfPCell(mainInfoTable);
                    cell.setPaddingRight(10);
                    cell.setPaddingLeft(10);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    cell.setBorder(0);
                    cell.setColspan(2);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    paragraph = new Paragraph();
                    paragraph.setAlignment(Element.ALIGN_LEFT);
                    paragraph.add(new Phrase(myUI.getMessage(Messages.School) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getSchool() + "\n", ordFont));
                    paragraph.add(new Phrase("Email: ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getEmail() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.PhoneNumbers) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getPhones() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.Address) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getAddress() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.SpouseInfo) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getFamilyInfo() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.Children) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getChildren() + "", ordFont));
                    cell = new PdfPCell(paragraph);
                    cell.setBorder(0);
                    cell.setLeading(0, 1.4f);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    paragraph = new Paragraph();
                    paragraph.setAlignment(Element.ALIGN_LEFT);
                    paragraph.add(new Phrase(myUI.getMessage(Messages.WorkingStatus) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getWorkingStatus() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.MainPosition) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getMainPosition() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.ExtraPositions) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getExtraPositions() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.TotalWorkExperience) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getWorkExperience() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.StarWorkExperience) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getWorkExperienceSapat() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.MainBranch) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getMainBranch() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.ExtraBranches) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getExtraBranches() + "\n", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.TotalHours) + year + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getHours() + " ", ordFont));
                    paragraph.add(new Phrase(myUI.getMessage(Messages.ExtraHours) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getExtraHours() + "\n", ordFont));
                    if (currentUser.isPermitted(Settings.cnCV_Window + ":" + Settings.contract_info)) {
                        paragraph.add(new Phrase(myUI.getMessage(Messages.CanBeAdvisor) + ": ", ordFontBold));
                        paragraph.add(new Phrase(employeeExtraInfo.getCanBeAdvisor() + "\n", ordFont));
                        paragraph.add(new Phrase(myUI.getMessage(Messages.ContractType) + ": ", ordFontBold));
                        paragraph.add(new Phrase(employeeExtraInfo.getSalaryCategory() + "\n", ordFont));
                    }
                    paragraph.add(new Phrase(myUI.getMessage(Messages.Languages) + ": ", ordFontBold));
                    paragraph.add(new Phrase(employeeExtraInfo.getLanguages(), ordFont));
                    cell = new PdfPCell(paragraph);
                    cell.setBorder(0);
                    cell.setLeading(0, 1.4f);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(0), new float[]{0.2f, 2.0f, 2.0f, 1.0f, 1.0f, 1.0f}));
                    cell.setBorder(0);
                    cell.setColspan(3);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(1), new float[]{0.2f, 2.6f, 0.4f, 1.0f, 2.0f, 1.0f, 1.0f}));
                    cell.setBorder(0);
                    cell.setColspan(4);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(2), new float[]{0.2f, 2.6f, 1.0f, 1.0f}));
                    cell.setBorder(0);
                    cell.setColspan(4);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(3), new float[]{0.2f, 2.0f, 2.0f, 1.0f, 0.6f}));
                    cell.setBorder(0);
                    cell.setColspan(4);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(4), new float[]{0.2f, 2.0f, 2.0f, 1.0f, 0.6f}));
                    cell.setBorder(0);
                    cell.setColspan(4);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(5), new float[]{0.2f, 1.0f, 2.6f, 2.0f, 1.0f, 1.0f}));
                    cell.setBorder(0);
                    cell.setColspan(4);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(6), new float[]{0.2f, 2.6f, 1.0f, 2.0f, 2.0f, 1.0f, 1.0f}));
                    cell.setBorder(0);
                    cell.setColspan(4);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    cell = new PdfPCell(createTable(tableList.get(7), new float[]{0.2f, 2.0f, 1.0f, 2.0f, 1.0f, 1.0f}));
                    cell.setBorder(0);
                    cell.setColspan(4);
                    cell.setPaddingRight(5);
                    cell.setPaddingLeft(5);
                    dataTable.addCell(cell);

                    document.add(dataTable);

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
            }
        };

        StreamResource resource = new StreamResource(source1, "CV"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "CV", false);
    }

    private PdfPTable createTable(Table table, float[] colsWidth) throws DocumentException {
        PdfPTable pdfTable = new PdfPTable(colsWidth.length);
        pdfTable.setWidthPercentage(80f);
        pdfTable.setWidths(colsWidth);

        PdfPCell cell = new PdfPCell(new Phrase(table.getData().toString(), captionFont));
        cell.setPaddingTop(15);
        cell.setPaddingBottom(10);
        cell.setBorder(0);
        cell.setBorderWidthBottom(0.3f);
        cell.setColspan(colsWidth.length);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfTable.addCell(cell);
        pdfTable.getDefaultCell().setBorderWidthTop(0.3f);
        pdfTable.getDefaultCell().setBorderWidthLeft(0.3f);
        pdfTable.getDefaultCell().setBorderWidthRight(0.3f);
        pdfTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
        pdfTable.addCell(new Phrase("#", ordFontBold));
        for (Object p : table.getContainerPropertyIds()) {
            pdfTable.addCell(new Phrase(p.toString(), ordFontBold));
        }
        int i = 0;
        for (Object next : table.getItemIds()) {
            pdfTable.addCell(new Phrase((++i) + "", ordFont));
            for (Object p : table.getContainerPropertyIds()) {
                pdfTable.addCell(new Phrase(table.getContainerProperty(
                        next, p).getValue() != null ? table.getContainerProperty(
                        next, p).getValue().toString() : "", ordFont));
            }
        }
        return pdfTable;
    }
}
