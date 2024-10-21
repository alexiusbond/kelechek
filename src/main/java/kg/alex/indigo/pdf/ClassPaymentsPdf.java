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
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
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

public class ClassPaymentsPdf {

    static final Logger logger = LogManager.getLogger(ClassPaymentsPdf.class);
    private final Date aDate = new Date(System.currentTimeMillis());
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;


    public ClassPaymentsPdf(final MyVaadinUI myUI, final IndexedContainer dataContainer, final String year,
                            final Date fDate, final Date tDate, final StudentInfoPdf studentInfo, final double total) {
        //installment plan table
        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            /**
             *
             */
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

                    document.open();

                    float[] table_date_colsWidth = {3.5f, 1f};
                    PdfPTable table_date = new PdfPTable(2);
                    table_date.setWidthPercentage(90f);
                    table_date.setWidths(table_date_colsWidth);
                    table_date.getDefaultCell().setBorder(0);
                    table_date.addCell(new Phrase(" ", ordFont));
                    table_date.addCell(new Phrase("Дата: " + Settings.df.format(aDate), tableFont));
                    document.add(table_date);

                    Paragraph spr = new Paragraph(myUI.getMessage(Messages.ClassPayments) + ": "
                            + year + " (" + Settings.df.format(fDate) + " - " + Settings.df.format(tDate) + ")", fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(12, " "));
                    document.add(spr);
                    document.add(new Paragraph(24, " "));

                    //installment plan table
                    float[] table_plan_colsWidth = {0.1f, 0.25f, 0.25f, 0.17f, 0.4f, 0.4f, 0.3f, 0.12f, 0.25f, 0.12f};
                    PdfPTable infoTable = new PdfPTable(10);
                    infoTable.setWidthPercentage(90f);
                    infoTable.setWidths(table_plan_colsWidth);
                    infoTable.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    infoTable.addCell(new Phrase(" №", ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.FirstName), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.LastName), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.ClassName), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.PaymentCategoryType), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.WhoPaid), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Date), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Rate), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Amount), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Currency), ordFontBold));

                    Iterator<?> iter = dataContainer.getItemIds().iterator();
                    int i = 0;
                    if (dataContainer.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        infoTable.addCell(new Phrase(i + "", tableFont));
                        infoTable.addCell(new Phrase(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.FirstName)).getValue().toString(), tableFont));
                        infoTable.addCell(new Phrase(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.LastName)).getValue().toString(), tableFont));
                        infoTable.addCell(new Phrase(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.ClassName)).getValue().toString(), tableFont));
                        infoTable.addCell(new Phrase(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.PaymentCategoryType)).getValue().toString(), tableFont));
                        infoTable.addCell(new Phrase(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.WhoPaid)).getValue().toString(), tableFont));
                        infoTable.addCell(new Phrase(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.Date)).getValue().toString(), tableFont));
                        infoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        infoTable.addCell(new Phrase(Settings.dFormat2.format(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.Rate)).getValue()), tableFont));
                        infoTable.addCell(new Phrase(Settings.dFormat2.format(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.Amount)).getValue()), tableFont));
                        infoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        infoTable.addCell(new Phrase(dataContainer.getContainerProperty(next,
                                myUI.getMessage(Messages.Currency)).getValue().toString(), tableFont));
                        i++;
                    }
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Total) + ": "
                            + Settings.dFormat2.format(total) + " " +
                            (myUI.getUser().getSchool().getCurrency_id() == 1 ? Settings.KGS : Settings.USD), ordFontBold));
                    infoTable.addCell(new Phrase(" ", ordFontBold));

                    document.add(infoTable);

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

        StreamResource resource = new StreamResource(source1, "ClassPayments"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "ClassPayments", false);
    }
}
