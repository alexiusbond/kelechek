/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Table;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class YearReportPdf {

    static final Logger logger = LogManager.getLogger(YearReportPdf.class);
    private final Date aDate = new Date(System.currentTimeMillis());
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;


    public YearReportPdf(final MyVaadinUI myUI, final ComponentContainer layout,
                         final StudentInfoPdf studentInfo) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/indigo/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/indigo/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4.rotate(), 10, 10, 70, 40);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, studentInfo.getSchool().getName_ru(),
                            studentInfo.getSchool().getAddress(), studentInfo.getSchool().getPhone());
                    writer.setPageEvent(event);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 14);
                    Font ordFont = new Font(baseFont, 11);
                    Font ordFontBold = new Font(baseFontBold, 11);
                    Font tableFont = new Font(baseFont, 10);
                    Font tableFontBold = new Font(baseFontBold, 10);

                    document.open();

                    float[] table_dates_colsWidth = {3.5f, 1f};
                    PdfPTable table_date = new PdfPTable(2);
                    table_date.setWidthPercentage(90f);
                    table_date.setWidths(table_dates_colsWidth);
                    table_date.getDefaultCell().setBorder(0);
                    table_date.addCell(new Phrase(" ", ordFont));
                    table_date.addCell(new Phrase("Дата: " + Settings.df.format(aDate), tableFont));
                    document.add(table_date);

                    Paragraph spr = new Paragraph(myUI.getMessage(Messages.Yearly)
                            + " " + myUI.getMessage(Messages.Report), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(12, " "));
                    document.add(spr);

                    for (Component component : layout) {
                        document.add(new Paragraph(24, " "));
                        Table dataTable = (Table) component;
                        Paragraph p = new Paragraph(dataTable.getCaption(), ordFontBold);
                        p.setIndentationLeft(30);
                        p.setSpacingAfter(7);
                        document.add(p);

                        //installment plan table
                        float[] table_plan_colsWidth = {0.07f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.15f, 0.2f, 0.2f, 0.2f, 0.11f};
                        PdfPTable pdfTable = new PdfPTable(11);
                        pdfTable.setWidthPercentage(90f);
                        pdfTable.setWidths(table_plan_colsWidth);
                        pdfTable.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        pdfTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        pdfTable.addCell(new Phrase(" №", tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.ClassName), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.Total_Active), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.Contract), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.Discount), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.Correction), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.PreviousYearDebt), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.Net), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.Paid), tableFontBold));
                        pdfTable.addCell(new Phrase(myUI.getMessage(Messages.Left), tableFontBold));
                        pdfTable.addCell(new Phrase(Settings.percentage, tableFontBold));

                        Iterator<?> iter = dataTable.getContainerDataSource().getItemIds().iterator();
                        int j = 0;
                        if (dataTable.getContainerDataSource().size() > 0) {
                            j = 1;
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            pdfTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            pdfTable.addCell(new Phrase(j + "", tableFont));
                            pdfTable.addCell(new Phrase(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.ClassName)).getValue().toString(), tableFont));
                            pdfTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            pdfTable.addCell(new Phrase(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Total_Active)).getValue().toString(), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Contract)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Discount)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Correction)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.PreviousYearDebt)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Net)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Paid)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.Left)).getValue()), tableFont));
                            pdfTable.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                    Settings.percentage).getValue()), tableFont));
                            j++;
                        }
                        pdfTable.addCell(new Phrase(" ", tableFontBold));
                        pdfTable.addCell(new Phrase(" ", tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.Total_Active)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.Contract)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.Discount)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.Correction)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.PreviousYearDebt)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.Net)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.Paid)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                myUI.getMessage(Messages.Left)), tableFontBold));
                        pdfTable.addCell(new Phrase(dataTable.getColumnFooter(
                                Settings.percentage), tableFontBold));

                        document.add(pdfTable);
                    }

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

        StreamResource resource = new StreamResource(source1, "YearReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "YearReport", false);
    }
}
