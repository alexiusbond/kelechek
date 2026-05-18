/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ispa.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.domain.StudentInfoPdf;
import kg.alex.ispa.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class AccountingByDatesPdf {

    static final Logger logger = LogManager.getLogger(AccountingByDatesPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;


    public AccountingByDatesPdf(final MyVaadinUI myUI, StudentInfoPdf studentInfo, final IndexedContainer incomesContainer,
                                final IndexedContainer expensesContainer, String datesInterval) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/ispa/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/ispa/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 60, 35);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, studentInfo.getSchool().getName_ru(),
                            studentInfo.getSchool().getAddress(), studentInfo.getSchool().getPhone());
                    writer.setPageEvent(event);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 14);
                    Font fontSubheader = new Font(baseFontBold, 12);
                    Font ordFont = new Font(baseFont, 11);
                    Font ordFontBold = new Font(baseFontBold, 11);
                    Font tableFont = new Font(baseFont, 10);
                    Font footerFont = new Font(baseFontBold, 9);

                    document.open();

                    Paragraph spr = new Paragraph(myUI.getMessage(Messages.ByDateReport)
                            + " " + datesInterval, fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(15, " "));
                    document.add(spr);
                    document.add(new Paragraph(10, " "));

                    if (incomesContainer != null && incomesContainer.size() > 0) {
                        spr = new Paragraph(myUI.getMessage(Messages.Incomes), fontSubheader);
                        spr.setIndentationLeft(40);
                        spr.setFirstLineIndent(40);
                        document.add(spr);
                        document.add(new Paragraph(10, " "));
                        float[] table_colsWidth = {0.7f, 1.8f, 1.5f, 4.2f, 1f, 1.3f, 1.9f, 4.0f};
                        PdfPTable table = new PdfPTable(8);
                        table.setWidthPercentage(90f);
                        table.setWidths(table_colsWidth);
                        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                        table.addCell(new Phrase(" №", ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Date), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Code), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Category), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.CashBox), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Rate), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Amount), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Note), ordFontBold));

                        int i = 0;
                        Iterator<?> iterator = incomesContainer.getItemIds().iterator();
                        double total = 0;
                        while (iterator.hasNext()) {
                            Object next = iterator.next();
                            table.addCell(new Phrase(new Phrase((++i) + "", tableFont)));
                            table.addCell(new Phrase(incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Date)).getValue().toString(), tableFont));
                            table.addCell(new Phrase(incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Code)).getValue().toString(), tableFont));
                            table.addCell(new Phrase(incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Category)).getValue().toString(), tableFont));
                            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            table.addCell(new Phrase(incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.CashBox)).getValue().toString(), tableFont));
                            table.addCell(new Phrase(Settings.dFormat4.format(incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Rate)).getValue()), tableFont));
                            table.addCell(new Phrase(Settings.dFormat2.format(incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Amount)).getValue()), tableFont));
                            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            table.addCell(new Phrase(incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Note)).getValue().toString(), tableFont));
                            total += (Double) incomesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Amount)).getValue();
                        }
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(Settings.dFormat2.format(total), footerFont));
                        table.addCell(new Phrase(" "));
                        document.add(table);
                    }
                    if (expensesContainer != null && expensesContainer.size() > 0) {
                        spr = new Paragraph(myUI.getMessage(Messages.Expenses), fontSubheader);
                        spr.setIndentationLeft(40);
                        spr.setFirstLineIndent(40);
                        document.add(spr);
                        document.add(new Paragraph(10, " "));
                        float[] table_colsWidth = {0.7f, 1.8f, 1.5f, 4.2f, 1f, 1.3f, 1.9f, 4.0f};
                        PdfPTable table = new PdfPTable(8);
                        table.setWidthPercentage(90f);
                        table.setWidths(table_colsWidth);
                        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                        table.addCell(new Phrase(" №", ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Date), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Code), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Category), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.CashBox), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Rate), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Amount), ordFontBold));
                        table.addCell(new Phrase(myUI.getMessage(Messages.Note), ordFontBold));

                        int i = 0;
                        Iterator<?> iterator = expensesContainer.getItemIds().iterator();
                        double total = 0;
                        while (iterator.hasNext()) {
                            Object next = iterator.next();
                            table.addCell(new Phrase(new Phrase((++i) + "", tableFont)));
                            table.addCell(new Phrase(expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Date)).getValue().toString(), tableFont));
                            table.addCell(new Phrase(expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Code)).getValue().toString(), tableFont));
                            table.addCell(new Phrase(expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Category)).getValue().toString(), tableFont));
                            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            table.addCell(new Phrase(expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.CashBox)).getValue().toString(), tableFont));
                            table.addCell(new Phrase(Settings.dFormat4.format(expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Rate)).getValue()), tableFont));
                            table.addCell(new Phrase(Settings.dFormat2.format(expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Amount)).getValue()), tableFont));
                            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            table.addCell(new Phrase(expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Note)).getValue().toString(), tableFont));
                            total += (Double) expensesContainer.getContainerProperty(next,
                                    myUI.getMessage(Messages.Amount)).getValue();
                        }
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(" "));
                        table.addCell(new Phrase(Settings.dFormat2.format(total), footerFont));
                        table.addCell(new Phrase(" "));
                        document.add(table);
                    }

                    document.add(new Paragraph(20, " "));
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

        StreamResource resource = new StreamResource(source1, "DateReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "InstallmentPlanPayments", false);
    }
}
