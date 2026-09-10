/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;

public class BranchCodesPdf {

    static final Logger logger = LogManager.getLogger(BranchCodesPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public BranchCodesPdf(final MyVaadinUI myUI, final IndexedContainer branchesCont) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/kelechek/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/kelechek/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 70, 40);
                    PdfWriter writer = PdfWriter.getInstance(document, buffer);

                    BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2,
                            BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                    Font fontBold = new Font(baseFontBold, 14);
                    Font ordFontBold = new Font(baseFontBold, 11);
                    Font tableFont = new Font(baseFont, 10);

                    document.open();

                    Paragraph spr = new Paragraph(myUI.getMessage(Messages.Branches), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(12, " "));
                    document.add(spr);
                    document.add(new Paragraph(24, " "));

                    //installment plan table
                    float[] table_plan_colsWidth = {0.2f, 0.4f, 3f};
                    PdfPTable infoTable = new PdfPTable(3);
                    infoTable.setWidthPercentage(90f);
                    infoTable.setWidths(table_plan_colsWidth);
                    infoTable.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    infoTable.addCell(new Phrase(" №", ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.CourseCode), ordFontBold));
                    infoTable.addCell(new Phrase(myUI.getMessage(Messages.Title), ordFontBold));

                    Iterator<?> iter = branchesCont.getItemIds().iterator();
                    int i = 1;
                    while (iter.hasNext()) {
                        Object id = iter.next();
                        infoTable.addCell(new Phrase(i + "", tableFont));
                        if (branchesCont.getContainerProperty(id,
                                myUI.getMessage(Messages.Code)).getValue() != null) {
                            infoTable.addCell(new Phrase(branchesCont.getContainerProperty(id,
                                    myUI.getMessage(Messages.Code)).getValue().toString(), tableFont));
                        } else {
                            infoTable.addCell(new Phrase(" ", tableFont));
                        }
                        infoTable.addCell(new Phrase(branchesCont.getContainerProperty(id,
                                myUI.getMessage(Messages.Title)).getValue().toString(), tableFont));
                        i++;
                    }
                    document.add(infoTable);

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
