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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class DebtsPdf {

    static final Logger logger = LogManager.getLogger(DebtsPdf.class);
    private final Date aDate = new Date(System.currentTimeMillis());
    private final Date fromDate, tillDate;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private final String currency;


    public DebtsPdf(final MyVaadinUI myUI, final IndexedContainer planCont, final String year,
                    final Date fDate, final Date tDate, final StudentInfoPdf studentInfo, final double ttl_plan,
                    final double total_paid, final double total_debt) {
        this.fromDate = fDate;
        this.tillDate = tDate;
        this.currency = myUI.getUser().getSchool().getCurrency_id() == 1 ? Settings.KGS : Settings.USD;
        StreamResource.StreamSource source1 = new StreamResource.StreamSource() {

            private static final long serialVersionUID = 1L;
            private final static String FONT_LOCATION = "/home/ellipse/PT_Sans-Web-Regular.ttf";
            private final static String FONT_LOCATION2 = "/home/ellipse/PT_Sans-Web-Bold.ttf";

            @Override
            public InputStream getStream() {

                buffer = new ByteArrayOutputStream();

                try {

                    document = new Document(PageSize.A4, 10, 10, 70, 40);
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

                    Paragraph spr = new Paragraph(myUI.getMessage(Messages.ClassInstallmentPlan) + ": "
                            + year + " (" + Settings.df.format(fromDate) + " - " + Settings.df.format(tillDate) + ")", fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(12, " "));
                    document.add(spr);
                    document.add(new Paragraph(24, " "));

                    //installment plan table
                    float[] table_plan_colsWidth = {1f, 4f, 4f, 4f, 4f, 4f, 4f};
                    PdfPTable table_plan = new PdfPTable(7);
                    table_plan.setWidthPercentage(90f);
                    table_plan.setWidths(table_plan_colsWidth);
                    table_plan.getDefaultCell().
                            setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table_plan.addCell(new Phrase(" №", ordFontBold));
                    table_plan.addCell(new Phrase(myUI.getMessage(Messages.FirstName), ordFontBold));
                    table_plan.addCell(new Phrase(myUI.getMessage(Messages.LastName), ordFontBold));
                    table_plan.addCell(new Phrase(myUI.getMessage(Messages.ClassName), ordFontBold));
                    table_plan.addCell(new Phrase(myUI.getMessage(Messages.InstallmentPlan), ordFontBold));
                    table_plan.addCell(new Phrase(myUI.getMessage(Messages.Paid), ordFontBold));
                    table_plan.addCell(new Phrase(myUI.getMessage(Messages.Debt), ordFontBold));

                    Iterator<?> iter = planCont.getItemIds().iterator();
                    int i = 0;
                    if (planCont.size() > 0) {
                        i = 1;
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        table_plan.addCell(new Phrase(i + "", tableFont));
                        table_plan.addCell(new Phrase(planCont.getContainerProperty(next,
                                myUI.getMessage(Messages.FirstName)).getValue().toString(), tableFont));
                        table_plan.addCell(new Phrase(planCont.getContainerProperty(next,
                                myUI.getMessage(Messages.LastName)).getValue().toString(), tableFont));
                        table_plan.addCell(new Phrase(planCont.getContainerProperty(next,
                                myUI.getMessage(Messages.ClassName)).getValue().toString(), tableFont));
                        table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        table_plan.addCell(new Phrase(Settings.dFormat2.format(
                                planCont.getContainerProperty(next,
                                        myUI.getMessage(Messages.InstallmentPlan)).getValue()), tableFont));
                        table_plan.addCell(new Phrase(Settings.dFormat2.format(
                                planCont.getContainerProperty(next,
                                        myUI.getMessage(Messages.Paid)).getValue()), tableFont));
                        table_plan.addCell(new Phrase(Settings.dFormat2.format(
                                planCont.getContainerProperty(next,
                                        myUI.getMessage(Messages.Debt)).getValue()), tableFont));
                        table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                        i++;
                    }
                    table_plan.addCell(new Phrase(" ", ordFontBold));
                    table_plan.addCell(new Phrase(" ", ordFontBold));
                    table_plan.addCell(new Phrase(" ", ordFontBold));
                    table_plan.addCell(new Phrase(" ", ordFontBold));
                    table_plan.addCell(new Phrase(Settings.dFormat2.format(ttl_plan) + " " + currency, ordFontBold));
                    table_plan.addCell(new Phrase(Settings.dFormat2.format(total_paid) + " " + currency, ordFontBold));
                    table_plan.addCell(new Phrase(Settings.dFormat2.format(total_debt) + " " + currency, ordFontBold));

                    document.add(table_plan);

                    document.add(new Paragraph(12, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    T2.addCell(new Phrase(myUI.getMessage(Messages.Accountant), ordFontBold));
                    T2.addCell(new Phrase(myUI.getMessage(Messages.President), ordFontBold));
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

        StreamResource resource = new StreamResource(source1, "Debts"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "Debts", false);
    }
}
