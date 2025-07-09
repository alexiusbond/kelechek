/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.domain.StudentInfoPdf;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.reports.students.ClassListReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class ClassListPdf {

    static final Logger logger = LogManager.getLogger(ClassListPdf.class);
    private final Date aDate = new Date(System.currentTimeMillis());
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ClassListPdf(final MyVaadinUI myUI, final IndexedContainer dataCont,
                        final StudentInfoPdf studentInfo, final ClassListReport clr) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/ellipse/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/ellipse/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4.rotate(), 10, 10, 70, 40);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterLandscape event = new HeaderFooterLandscape(myUI, studentInfo.getSchool().getName_ru(),
                            studentInfo.getSchool().getAddress(), studentInfo.getSchool().getPhone());
                    writer.setPageEvent(event);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 13);
                    Font ordFont = new Font(baseFont, 10);
                    Font ordFontBold = new Font(baseFontBold, 10);
                    Font tableFont = new Font(baseFont, 9);
                    Font tableFontBold = new Font(baseFontBold, 9);

                    document.open();

                    float[] table_date_colsWidth = {3.5f, 1f};
                    PdfPTable table_date = new PdfPTable(2);
                    table_date.setWidthPercentage(90f);
                    table_date.setWidths(table_date_colsWidth);
                    table_date.getDefaultCell().setBorder(0);
                    table_date.addCell(new Phrase(" ", ordFont));
                    table_date.addCell(new Phrase("Дата: " + Settings.df.format(aDate), tableFont));
                    document.add(table_date);

                    Paragraph spr = new Paragraph(myUI.getMessage(Messages.ClassList), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(9, " "));
                    document.add(spr);
                    document.add(new Paragraph(20, " "));

                    //installment plan table
                    float[] table_plan_colsWidth = {
                            0.1f, 0.24f, 0.23f, 0.28f, 0.35f, 0.15f, 0.25f, 0.43f, 0.25f,
                            0.21f, 0.21f, 0.21f, 0.25f, 0.25f, 0.25f, 0.25f,};
                    PdfPTable dataTable = new PdfPTable(16);
                    dataTable.setWidthPercentage(90f);
                    dataTable.setWidths(table_plan_colsWidth);
                    dataTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                    dataTable.addCell(new Phrase(" №", ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Id), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.EducationStatus), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.FirstName), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.LastName), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.ClassName), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Contract), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.DiscountType), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Discount), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Correction), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.PreviousYearDebt), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.PreviousYearOverpay), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Net), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Paid), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Debt), ordFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.OverPay), ordFontBold));

                    Iterator<?> iter = dataCont.getItemIds().iterator();
                    int i = 0;
                    if (dataCont.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        dataTable.addCell(new Phrase(i + "", tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Id)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.EducationStatus)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.FirstName)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.LastName)).getValue().toString(), tableFont));
                        dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.ClassName)).getValue().toString(), tableFont));
                        dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        if (dataCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Contract)).getValue() != null) {
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.Contract)).getValue()), tableFont));
                            dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            if (dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.DiscountType)).getValue() != null) {
                                dataTable.addCell(new Phrase(dataCont.getContainerProperty(next,
                                        myUI.getMessage(Messages.DiscountType)).getValue().toString(), tableFont));
                                dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                                dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                        myUI.getMessage(Messages.Discount)).getValue()), tableFont));
                            } else {
                                dataTable.addCell(new Phrase(" ", tableFont));
                                dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                                dataTable.addCell(new Phrase("0.00", tableFont));
                            }
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.Correction)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.PreviousYearDebt)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.PreviousYearOverpay)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.Net)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.Paid)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.Debt)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.OverPay)).getValue()), tableFont));
                        } else {
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.PreviousYearDebt)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(Settings.dFormat2.format(dataCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.PreviousYearOverpay)).getValue()), tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                            dataTable.addCell(new Phrase(" ", tableFont));
                        }
                        dataTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        i++;
                    }
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Students) + ": "
                            + dataCont.size(), tableFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Active)
                            + clr.activeStudents, tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(" ", tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.contracts), tableFontBold));
                    dataTable.addCell(new Phrase(myUI.getMessage(Messages.Discounted)
                            + clr.discountedStudents, tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.discounts), tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.corrections), tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.prevYearDebts), tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.prevYearOverpays), tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.nets), tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.paid_amounts), tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.debts), tableFontBold));
                    dataTable.addCell(new Phrase(Settings.dFormat2.format(clr.overPays), tableFontBold));

                    document.add(dataTable);

                    document.add(new Paragraph(12, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    T2.addCell(new Phrase(myUI.getMessage(Messages.Accountant), ordFontBold));
                    T2.addCell(new Phrase(myUI.getMessage(Messages.Director), ordFontBold));
                    T2.addCell(new Phrase(studentInfo.getAccountant().getSurname() + " "
                            + studentInfo.getAccountant().getName() + " " +
                            (studentInfo.getAccountant().getMiddle_name() == null ?
                                    "" : studentInfo.getAccountant().getMiddle_name()), ordFont));
                    T2.addCell(new Phrase(studentInfo.getDirector().getSurname() + " "
                            + studentInfo.getDirector().getName() + " " +
                            (studentInfo.getDirector().getMiddle_name() == null ?
                                    "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                    document.add(T2);
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

        StreamResource resource = new StreamResource(source1, "ClassList"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "ClassList", false);
    }
}
