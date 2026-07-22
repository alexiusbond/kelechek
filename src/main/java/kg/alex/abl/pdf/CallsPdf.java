/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.abl.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Table;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.domain.StudentInfoPdf;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class CallsPdf {

    static final Logger logger = LogManager.getLogger(CallsPdf.class);
    private final Date aDate = new Date(System.currentTimeMillis());
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public CallsPdf(final MyVaadinUI myUI, final Table dataTable, final StudentInfoPdf studentInfo) {

        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/abl/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/abl/PT_Sans-Web-Bold.ttf";

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

                    float[] dates_table_colsWidth = {3.5f, 1f};
                    PdfPTable table_date = new PdfPTable(2);
                    table_date.setWidthPercentage(90f);
                    table_date.setWidths(dates_table_colsWidth);
                    table_date.getDefaultCell().setBorder(0);
                    table_date.addCell(new Phrase(" ", ordFont));
                    table_date.addCell(new Phrase("Дата: " + Settings.df.format(aDate), tableFont));
                    document.add(table_date);

                    Paragraph spr = new Paragraph(myUI.getMessage(Messages.Calls), fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(9, " "));
                    document.add(spr);
                    document.add(new Paragraph(20, " "));

                    //installment plan table
                    float[] plan_table_colsWidth = {0.1f, 0.2f, 0.3f, 0.3f, 0.15f, 0.5f, 0.23f, 0.23f, 0.26f, 0.76f, 0.25f};
                    PdfPTable t = new PdfPTable(11);
                    t.setWidthPercentage(90f);
                    t.setWidths(plan_table_colsWidth);
                    t.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                    t.addCell(new Phrase(" №", ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.Id), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.FirstName), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.LastName), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.ClassName), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.Phone), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.InstPlanDebt), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.Remain), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.PlanDebtDate), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.LastCall), ordFontBold));
                    t.addCell(new Phrase(myUI.getMessage(Messages.LastPayment), ordFontBold));

                    Iterator<?> iter = dataTable.getItemIds().iterator();
                    int i = 0;
                    if (dataTable.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        t.addCell(new Phrase(i + "", tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.Id)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.FirstName)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.LastName)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.ClassName)).getValue().toString(), tableFont));
                        t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.Phone)).getValue().toString(), tableFont));
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        t.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.InstPlanDebt)).getValue()), tableFont));
                        t.addCell(new Phrase(Settings.dFormat2.format(dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.Remain)).getValue()), tableFont));
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        if (dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.PlanDebtDate)).getValue() != null) {
                            t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.PlanDebtDate)).getValue().toString(), tableFont));
                        } else {
                            t.addCell(new Phrase("", tableFont));
                        }
                        t.addCell(new Phrase("", tableFont));
                        t.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        if (dataTable.getContainerProperty(next,
                                myUI.getMessage(Messages.LastPayment)).getValue() != null) {
                            t.addCell(new Phrase(dataTable.getContainerProperty(next,
                                    myUI.getMessage(Messages.LastPayment)).getValue().toString(), tableFont));
                        } else {
                            t.addCell(new Phrase("", tableFont));
                        }
                        i++;
                    }
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(dataTable.getColumnFooter(
                            myUI.getMessage(Messages.InstPlanDebt)), tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));
                    t.addCell(new Phrase(" ", tableFontBold));

                    document.add(t);

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

        StreamResource resource = new StreamResource(source1, "Calls"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "Calls", false);
    }
}
