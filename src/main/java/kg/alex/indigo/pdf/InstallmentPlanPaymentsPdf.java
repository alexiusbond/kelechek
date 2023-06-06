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
import kg.alex.indigo.i18n.IndigoMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

public class InstallmentPlanPaymentsPdf {

    static final Logger logger = LogManager.getLogger(InstallmentPlanPaymentsPdf.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private final Date aDate = new Date(System.currentTimeMillis());
    private final StudentInfoPdf studentInfo;


    public InstallmentPlanPaymentsPdf(final MyVaadinUI myUI, StudentInfoPdf s, final IndexedContainer planCont,
                                      final IndexedContainer paymentsCont, final double ttl_inst, final double ttl_pay) {
        this.studentInfo = s;

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

                    Paragraph spr = new Paragraph(myUI.getMessage(IndigoMessages.InstallmentPLanPaymentsReport)
                            + " (" + studentInfo.getYear() + ")", fontBold);
                    spr.setAlignment(Element.ALIGN_CENTER);
                    document.add(new Paragraph(24, " "));
                    document.add(spr);
                    document.add(new Paragraph(30, " "));
                    float[] Thead_colsWidth = {0.9f, 1.4f, 0.9f};
                    PdfPTable Thead = new PdfPTable(3);
                    Thead.setWidthPercentage(90f);
                    Thead.setWidths(Thead_colsWidth);

                    Thead.getDefaultCell().setBorder(0);
                    Thead.getDefaultCell().setFixedHeight(15f);
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Student), ordFontBold));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Contract), ordFontBold));
                    Thead.addCell(new Phrase(" ", ordFontBold));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Id)
                            + ": " + studentInfo.getStudent().getLogin(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Contract)
                            + ": " + studentInfo.getContractInfo().getContract() + "$", ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Net)
                            + ": " + studentInfo.getContractInfo().getNet() + "$", ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.FirstName)
                            + ": " + studentInfo.getStudent().getName(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Discount)
                            + ": " + studentInfo.getContractInfo().getDiscountStr(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Paid)
                            + ": " + studentInfo.getContractInfo().getPaid() + "$", ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.LastName)
                            + ": " + studentInfo.getStudent().getSurname(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Correction)
                            + ": " + studentInfo.getContractInfo().getCorrectionStr(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.Left)
                            + ": " + Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + "$", ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.ClassName)
                            + ": " + studentInfo.getStudent().getClass_name(), ordFont));
                    Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.PreviousYearDebt)
                            + ": " + studentInfo.getContractInfo().getDebt() + "$", ordFont));
                    if (studentInfo.getContractInfo().getInstallmentPlanDebt() > 0) {
                        Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.InstPlanDebt)
                                + ": " + Settings.dFormat2.format(studentInfo.getContractInfo()
                                .getInstallmentPlanDebt()) + "$", ordFont));
                    } else {
                        Thead.addCell(new Phrase(myUI.getMessage(IndigoMessages.InstPlanDebt)
                                + ": 0.00$", ordFont));
                    }

                    document.add(Thead);
                    document.add(new Paragraph(20, " "));

                    if (planCont == null && paymentsCont != null) {
                        //payments table
                        float[] table_payments_colsWidth = {0.75f, 2.5f, 2.5f, 4f, 3.2f};
                        PdfPTable table_payments = new PdfPTable(5);
                        table_payments.setWidthPercentage(90f);
                        table_payments.setWidths(table_payments_colsWidth);
                        table_payments.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                        table_payments.addCell(new Phrase(" №", ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.Date), ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.Amount), ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.WhoPaid), ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.PaymentCategoryType), ordFontBold));

                        Iterator<?> iter1 = paymentsCont.getItemIds().iterator();
                        int y = 0;
                        if (paymentsCont.size() > 0) {
                            y = 1;
                        }
                        while (iter1.hasNext()) {
                            Object next = iter1.next();
                            table_payments.addCell(new Phrase(new Phrase(y + "", tableFont)));
                            table_payments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Date)).getValue().toString(), tableFont));
                            table_payments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            table_payments.addCell(new Phrase(Settings.dFormat2.format(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Amount)).getValue()) + "$", tableFont));
                            table_payments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            table_payments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.WhoPaid)).getValue().toString(), tableFont));
                            table_payments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.PaymentCategoryType)).getValue().toString(), tableFont));
                            y++;
                        }

                        table_payments.addCell(new Phrase(" ", ordFontBold));
                        table_payments.addCell(new Phrase(" ", ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.Total)
                                + ": " + Settings.dFormat2.format(ttl_pay) + "$", ordFontBold));
                        table_payments.addCell(new Phrase(" ", ordFontBold));
                        table_payments.addCell(new Phrase(" ", ordFontBold));

                        document.add(table_payments);
                    }
                    if (paymentsCont == null && planCont != null) {
                        //installment plan table
                        float[] table_plan_colsWidth = {0.75f, 3f, 3f};
                        PdfPTable table_plan = new PdfPTable(3);
                        table_plan.setWidthPercentage(90f);
                        table_plan.setWidths(table_plan_colsWidth);
                        table_plan.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        table_plan.addCell(new Phrase(" №", ordFontBold));
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Date), ordFontBold));
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Amount), ordFontBold));

                        Iterator<?> iter = planCont.getItemIds().iterator();
                        int i = 0;
                        if (planCont.size() > 0) {
                            i = 1;
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            table_plan.addCell(new Phrase(i + "", tableFont));
                            table_plan.addCell(new Phrase(planCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Date)).getValue().toString(), tableFont));
                            table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            table_plan.addCell(new Phrase(Settings.dFormat2.format(planCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Amount)).getValue()) + "$", tableFont));
                            table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            i++;
                        }
                        table_plan.addCell(new Phrase(" ", ordFontBold));
                        table_plan.addCell(new Phrase(" ", ordFontBold));
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Total)
                                + ": " + Settings.dFormat2.format(ttl_inst) + "$", ordFontBold));

                        document.add(table_plan);
                    }
                    if (planCont != null && paymentsCont != null) {
                        float[] Tbody_colsWidth = {2f, 4f};
                        PdfPTable Tbody = new PdfPTable(2);
                        Tbody.setWidthPercentage(90f);
                        Tbody.setWidths(Tbody_colsWidth);
                        Tbody.getDefaultCell().setBorder(0);
                        Tbody.addCell(new Phrase(myUI.getMessage(IndigoMessages.InstallmentPlan), ordFontBold));
                        Tbody.addCell(new Phrase(myUI.getMessage(IndigoMessages.Payments), ordFontBold));
                        //installment plan table
                        float[] table_plan_colsWidth = {0.75f, 3f, 3f};
                        PdfPTable table_plan = new PdfPTable(3);
                        table_plan.setWidthPercentage(100f);
                        table_plan.setWidths(table_plan_colsWidth);
                        table_plan.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        table_plan.addCell(new Phrase(" №", ordFontBold));
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Date), ordFontBold));
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Amount), ordFontBold));

                        Iterator<?> iter = planCont.getItemIds().iterator();
                        int i = 0;
                        double ttl_plan = 0;
                        if (planCont.size() > 0) {
                            i = 1;
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            table_plan.addCell(new Phrase(i + "", tableFont));
                            table_plan.addCell(new Phrase(planCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Date)).getValue().toString(), tableFont));
                            table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            table_plan.addCell(new Phrase(Settings.dFormat2.format(planCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Amount)).getValue()) + "$", tableFont));
                            table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            i++;
                            ttl_plan += (Double) planCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Amount)).getValue();
                        }
                        table_plan.addCell(new Phrase(" ", ordFontBold));
                        table_plan.addCell(new Phrase(" ", ordFontBold));
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Total)
                                + ": " + Settings.dFormat2.format(ttl_plan) + "$", ordFontBold));

                        Tbody.addCell(table_plan);

                        //payments table
                        float[] table_payments_colsWidth = {0.75f, 2.5f, 2.5f, 4f, 3.2f};
                        PdfPTable table_payments = new PdfPTable(5);
                        table_payments.setWidthPercentage(100f);
                        table_payments.setWidths(table_payments_colsWidth);
                        table_payments.getDefaultCell().
                                setVerticalAlignment(Element.ALIGN_BOTTOM);
                        table_payments.addCell(new Phrase(" №", ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.Date), ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.Amount), ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.WhoPaid), ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.PaymentCategoryType), ordFontBold));

                        Iterator<?> iter1 = paymentsCont.getItemIds().iterator();
                        int y = 0;
                        double ttl_pay = 0;
                        if (planCont.size() > 0) {
                            y = 1;
                        }
                        while (iter1.hasNext()) {
                            Object next = iter1.next();
                            table_payments.addCell(new Phrase(y + "", tableFont));
                            table_payments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Date)).getValue().toString(), tableFont));
                            table_payments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            table_payments.addCell(new Phrase(Settings.dFormat2.format(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Amount)).getValue()) + "$", tableFont));
                            table_payments.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            table_payments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.WhoPaid)).getValue().toString(), tableFont));
                            table_payments.addCell(new Phrase(paymentsCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.PaymentCategoryType)).getValue().toString(), tableFont));
                            y++;

                            if ((Integer) paymentsCont.getContainerProperty(next,
                                    Settings.payment_category_id).getValue() != 3) {
                                ttl_pay += (Double) paymentsCont.getContainerProperty(next,
                                        myUI.getMessage(IndigoMessages.Amount)).getValue();
                            } else {
                                ttl_pay -= (Double) paymentsCont.getContainerProperty(next,
                                        myUI.getMessage(IndigoMessages.Amount)).getValue();
                            }
                        }

                        table_payments.addCell(new Phrase(" ", ordFontBold));
                        table_payments.addCell(new Phrase(" ", ordFontBold));
                        table_payments.addCell(new Phrase(myUI.getMessage(IndigoMessages.Total)
                                + ": " + Settings.dFormat2.format(ttl_pay) + "$", ordFontBold));
                        table_payments.addCell(new Phrase(" ", ordFontBold));
                        table_payments.addCell(new Phrase(" ", ordFontBold));

                        Tbody.addCell(table_payments);

                        document.add(Tbody);

                    }

                    document.add(new Paragraph(35, " "));
                    float[] T2_colsWidth = {2f, 2f};
                    PdfPTable T2 = new PdfPTable(2);
                    T2.setWidths(T2_colsWidth);
                    T2.setWidthPercentage(90);
                    T2.getDefaultCell().setBorder(0);
                    T2.getDefaultCell().
                            setHorizontalAlignment(Element.ALIGN_LEFT);
                    T2.addCell(new Phrase(myUI.getMessage(IndigoMessages.Accountant), ordFontBold));
                    T2.addCell(new Phrase(myUI.getMessage(IndigoMessages.Director), ordFontBold));
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

        StreamResource resource = new StreamResource(source1, "InstallmentPlanPayments"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "InstallmentPlanPayments", false);
    }
}
