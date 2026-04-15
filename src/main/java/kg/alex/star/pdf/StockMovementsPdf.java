/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.star.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.domain.StockInvoice;
import kg.alex.star.domain.StudentInfoPdf;
import kg.alex.star.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;

public class StockMovementsPdf {

    static final Logger logger = LogManager.getLogger(StockMovementsPdf.class);
    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;


    public StockMovementsPdf(final MyVaadinUI myUI, final String title, final StockInvoice stockInvoice,
                             final IndexedContainer stockMovementsCont, final StudentInfoPdf schoolInfo,
                             final String total) {


        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/aim/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/aim/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 60, 30);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, schoolInfo.getSchool().getName_ru(),
                            schoolInfo.getSchool().getAddress(), schoolInfo.getSchool().getPhone());
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

                    Paragraph spr = new Paragraph(title, fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(spr);
                    document.add(new Paragraph(30, " "));
                    float[] Thead_colsWidth = {2f, 2f};
                    PdfPTable Thead = new PdfPTable(2);
                    Thead.setWidthPercentage(90f);
                    Thead.setWidths(Thead_colsWidth);

                    Thead.getDefaultCell().setBorder(0);
                    Thead.getDefaultCell().setFixedHeight(15f);
                    Phrase p = new Phrase();
                    p.add(new Phrase(myUI.getMessage(Messages.Date) + ": ", ordFontBold));
                    p.add(new Phrase(df.format(stockInvoice.getCreation_date()), ordFont));
                    Thead.addCell(p);
                    p = new Phrase();
                    p.add(new Phrase(myUI.getMessage(Messages.InvoiceNumber) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getInvoiceNumberStr(), ordFont));
                    Thead.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.Stock) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getStock(), ordFont));
                    Thead.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.ProductCategory) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getAcc_category(), ordFont));
                    Thead.addCell(p);

                    document.add(Thead);
                    document.add(new Paragraph(20, " "));

                    float[] table_payments_colsWidth = {0.75f, 6f, 2.3f, 3.3f, 1.7f, 1.8f, 1.2f, 2f};
                    PdfPTable table = new PdfPTable(8);
                    table.setWidthPercentage(90f);
                    table.setWidths(table_payments_colsWidth);
                    table.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.addCell(new Phrase(" №", ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Product), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Measurement), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Note), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.QuantityShort), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Price), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Rate), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Amount), ordFontBold));

                    Iterator<?> iter1 = stockMovementsCont.getItemIds().iterator();
                    int y = 0;
                    if (stockMovementsCont.size() > 0) {
                        y = 1;
                    }
                    while (iter1.hasNext()) {
                        Object next = iter1.next();
                        table.addCell(new Phrase(y + "", tableFont));
                        ComboBox cb = (ComboBox) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Product)).getValue();
                        table.addCell(new Phrase(cb.getItemCaption(cb.getValue()), tableFont));
                        cb = (ComboBox) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Measurement)).getValue();
                        table.addCell(new Phrase(cb.getItemCaption(cb.getValue()), tableFont));
                        TextField tf = (TextField) stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Note)).getValue();
                        table.addCell(new Phrase(tf.getValue(), tableFont));
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        tf = (TextField) stockMovementsCont.getContainerProperty(next, myUI.getMessage(Messages.Quantity)).getValue();
                        table.addCell(new Phrase(Settings.dFormat2.format(tf.getPropertyDataSource().getValue()), tableFont));
                        if (title.equals(myUI.getMessage(Messages.StockIncome))) {
                            tf = (TextField) stockMovementsCont.getContainerProperty(next, myUI.getMessage(Messages.Price)).getValue();
                            table.addCell(new Phrase(Settings.dFormat2.format(tf.getPropertyDataSource().getValue()), tableFont));
                        } else {
                            table.addCell(new Phrase(Settings.dFormat2.format(stockMovementsCont.getContainerProperty(next,
                                    myUI.getMessage(Messages.Price)).getValue()), tableFont));
                        }
                        table.addCell(new Phrase(Settings.dFormat2.format(stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Rate)).getValue()), tableFont));
                        table.addCell(new Phrase(Settings.dFormat2.format(stockMovementsCont.getContainerProperty(next,
                                myUI.getMessage(Messages.Amount)).getValue()), tableFont));
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        y++;
                    }

                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Total) + ": " + total, ordFontBold));

                    document.add(table);

                    document.add(new Paragraph(35, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.FromEmployee) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getFrom_employee(), ordFont));
                    T2.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.ToEmployee) + ": ", ordFontBold));
                    p.add(new Phrase(stockInvoice.getTo_employee(), ordFont));
                    T2.addCell(p);
                    T2.addCell(new Phrase(" ", ordFont));
                    T2.addCell(new Phrase(" ", ordFont));
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.Director) + ": ", ordFontBold));
                    p.add(new Phrase(schoolInfo.getDirector().getSurname() + " "
                            + schoolInfo.getDirector().getName() + " " +
                            (schoolInfo.getDirector().getMiddle_name() == null ?
                                    "" : schoolInfo.getDirector().getMiddle_name()), ordFont));
                    T2.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.Accountant) + ": ", ordFontBold));
                    p.add(new Phrase(schoolInfo.getAccountant().getSurname() + " "
                            + schoolInfo.getAccountant().getName() + " " +
                            (schoolInfo.getAccountant().getMiddle_name() == null ?
                                    "" : schoolInfo.getAccountant().getMiddle_name()), ordFont));
                    T2.addCell(p);
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

        StreamResource resource = new StreamResource(source1, "StockMovements"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "StockMovements", false);
    }
}
