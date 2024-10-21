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
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.domain.InventoryInvoice;
import kg.alex.indigo.domain.School;
import kg.alex.indigo.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;

public class InventoryLiquidationPdf {

    static final Logger logger = LogManager.getLogger(InventoryLiquidationPdf.class);
    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;


    public InventoryLiquidationPdf(final MyVaadinUI myUI, final String title, final InventoryInvoice invoice,
                                   final IndexedContainer inventoriesCont, final String totalQuantity,
                                   final School schoolInfo) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/indigo/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/indigo/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 60, 30);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, schoolInfo.getName_ru(),
                            schoolInfo.getAddress(), schoolInfo.getPhone());
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
                    p.add(new Phrase(df.format(invoice.getCreation_date()), ordFont));
                    Thead.addCell(p);
                    p = new Phrase();
                    p.add(new Phrase(myUI.getMessage(Messages.InvoiceNumber) + ": ", ordFontBold));
                    p.add(new Phrase(invoice.getInvoiceNumberStr(), ordFont));
                    Thead.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.Block) + ": ", ordFontBold));
                    p.add(new Phrase(invoice.getBlock(), ordFont));
                    Thead.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.Floor) + ": ", ordFontBold));
                    p.add(new Phrase(invoice.getFloor(), ordFont));
                    Thead.addCell(p);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.Room) + ": ", ordFontBold));
                    p.add(new Phrase(invoice.getRoom(), ordFont));
                    Thead.addCell(p);
                    Thead.addCell(new Phrase(""));

                    document.add(Thead);
                    document.add(new Paragraph(20, " "));

                    float[] Tpayments_colsWidth = {0.75f, 5f, 1f};
                    PdfPTable table = new PdfPTable(3);
                    table.setWidthPercentage(90f);
                    table.setWidths(Tpayments_colsWidth);
                    table.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.addCell(new Phrase(" №", ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.InventoryItem), ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Quantity), ordFontBold));

                    Iterator<?> iter1 = inventoriesCont.getItemIds().iterator();
                    int y = 0;
                    if (inventoriesCont.size() > 0) {
                        y = 1;
                    }
                    while (iter1.hasNext()) {
                        Object next = iter1.next();
                        table.addCell(new Phrase(y + "", tableFont));
                        ComboBox cb = (ComboBox) inventoriesCont.getContainerProperty(next,
                                myUI.getMessage(Messages.InventoryItem)).getValue();
                        table.addCell(new Phrase(cb.getItemCaption(cb.getValue()), tableFont));
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        TextField tf = (TextField) inventoriesCont.getContainerProperty(
                                next, myUI.getMessage(Messages.Quantity)).getValue();
                        table.addCell(new Phrase(
                                tf.getPropertyDataSource().getValue() + "", tableFont));
                        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        y++;
                    }

                    table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    table.addCell(new Phrase(" ", ordFontBold));
                    table.addCell(new Phrase(myUI.getMessage(Messages.Total) + ": " + totalQuantity, ordFontBold));
                    document.add(table);

                    document.add(new Paragraph(30, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    p = new Paragraph();
                    p.add(new Phrase(myUI.getMessage(Messages.FromEmployee) + ": ", ordFontBold));
                    p.add(new Phrase(invoice.getEmployee(), ordFont));
                    T2.addCell(p);
                    T2.addCell(new Phrase(""));
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
