/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HeaderFooterLandscape extends PdfPageEventHelper {

    static final Logger logger = LogManager.getLogger(HeaderFooterLandscape.class);
    private final static String FONT_LOCATION = "/home/aim/PT_Sans-Web-Regular.ttf";
    private final String name, address, phone;
    private final MyVaadinUI myUI;

    public HeaderFooterLandscape(MyVaadinUI mu, String name, String address, String phone) {
        this.myUI = mu;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void onEndPage(PdfWriter writer, Document document) {
        try {
            BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                    BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            Font fontGray = new Font(baseFont, 13, Font.NORMAL, BaseColor.GRAY);
            Font ordFont = new Font(baseFont, 13, Font.NORMAL);
            Font footerFontGray = new Font(baseFont, 10, Font.NORMAL, BaseColor.GRAY);
            Chunk SEPARATOR = new Chunk(
                    new LineSeparator(0.5f, 95, BaseColor.BLACK, Element.ALIGN_CENTER, 3.5f));

            ColumnText ct = new ColumnText(writer.getDirectContent());
            Rectangle rect = new Rectangle(32, 0, 810, 575);

            ct.setSimpleColumn(rect);
            ct.setAlignment(Element.ALIGN_CENTER);
            ct.addText(new Phrase(myUI.getMessage(Messages.MinistryOfEducation), fontGray));
            ct.addText(Chunk.NEWLINE);
            ct.addText(new Phrase(name, ordFont));
            ct.addText(Chunk.NEWLINE);
            ct.addText(SEPARATOR);
            ct.go();

            Rectangle rect2 = new Rectangle(32, 0, 810, 50);
            ColumnText ct2 = new ColumnText(writer.getDirectContent());
            ct2.setAlignment(Element.ALIGN_CENTER);
            ct2.setSimpleColumn(rect2);
            ct2.addText(SEPARATOR);
            ct2.addText(Chunk.NEWLINE);
            ct2.addText(new Phrase("Address: " + address + ", Tel: " + phone, footerFontGray));
            ct2.go();


        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
