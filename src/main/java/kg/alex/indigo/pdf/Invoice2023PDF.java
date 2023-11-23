package kg.alex.indigo.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbStudentInstallmentPlan;
import kg.alex.indigo.domain.InvoiceInfoPdf;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.utils.money.WritableSummRu;
import kg.alex.indigo.utils.money.WritableSummRuSOM;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Iterator;

public class Invoice2023PDF {

    static final Logger logger = LogManager.getLogger(Invoice2023PDF.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public Invoice2023PDF(final MyVaadinUI myUI, final InvoiceInfoPdf invoiceInfo) {

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                SimpleDateFormat dateRu = new SimpleDateFormat(
                        "«dd» MMMMM yyyy г.", myDateFormatSymbols);

                document = new Document(PageSize.A4, 10, 10, 15, 10);
                PdfWriter.getInstance(document, buffer);

                final String FONT_LOCATION = "/home/indigo/PT_Sans-Web-Regular.ttf";
                final String FONT_LOCATION_BOLD = "/home/indigo/PT_Sans-Web-Bold.ttf";

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFont_bold = BaseFont.createFont(FONT_LOCATION_BOLD,
                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font normal_font = new Font(baseFont, 10, Font.NORMAL);
                Font underlined_font = new Font(baseFont, 10, Font.UNDERLINE);
                Font bold_font = new Font(baseFont_bold, 10, Font.NORMAL);
                Font table_font = new Font(baseFont, 8, Font.NORMAL);
                Font table_bold_font = new Font(baseFont_bold, 8, Font.NORMAL);

                document.open();
                float[] mainTableWidth = {1f, 1f};
                PdfPTable mainTable = new PdfPTable(2);
                mainTable.setWidthPercentage(95f);
                mainTable.setWidths(mainTableWidth);
                mainTable.getDefaultCell().setPaddingBottom(15);
                mainTable.getDefaultCell().setBorder(Rectangle.BOTTOM);

                float[] invoiceTableWidth = {1f};
                PdfPTable invoiceTable = new PdfPTable(1);
                invoiceTable.setWidths(invoiceTableWidth);
                invoiceTable.getDefaultCell().setLeading(20, 0);
                invoiceTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                invoiceTable.getDefaultCell().setPaddingLeft(15);
                invoiceTable.getDefaultCell().setPaddingRight(10);
                PdfPCell cell = new PdfPCell(new Phrase(invoiceInfo.getSchool_name(), normal_font));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(2);
                cell.setPaddingBottom(2);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                invoiceTable.addCell(cell);

                cell = new PdfPCell(new Phrase("КВИТАНЦИЯ", bold_font));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(2);
                cell.setPaddingBottom(2);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                if (invoiceInfo.getPaymentCategoryId() == 3) {
                    cell = new PdfPCell(new Phrase("к расходному кассовому ордеру", bold_font));
                } else {
                    cell = new PdfPCell(new Phrase("к приходному кассовому ордеру", bold_font));
                }
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(2);
                cell.setPaddingBottom(2);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                Phrase numberPhr = new Phrase("№: ________________", normal_font);
                cell = new PdfPCell(numberPhr);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                Phrase datePhr = new Phrase("Дата: " + dateRu.format(invoiceInfo.getPayment_date()), normal_font);
                cell = new PdfPCell(datePhr);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);
                Paragraph nameInvPar = new Paragraph();
                nameInvPar.setLeading(20);
                if (invoiceInfo.getPaymentCategoryId() == 3) {
                    nameInvPar.add(new Chunk("Выдано: ", bold_font));
                } else {
                    nameInvPar.add(new Chunk("Принято от: ", bold_font));
                }
                if (invoiceInfo.getWhoPaidFullName() != null && !invoiceInfo.getWhoPaidFullName().equals("")) {
                    nameInvPar.add(new Chunk(invoiceInfo.getLogin() + ", " + invoiceInfo.getClass_name() + ", "
                            + invoiceInfo.getStudentFullName() + " (" + invoiceInfo.getWhoPaidFullName() + ")", normal_font));
                } else {
                    nameInvPar.add(new Chunk(invoiceInfo.getLogin() + ", " + invoiceInfo.getClass_name() + ", "
                            + invoiceInfo.getStudentFullName(), normal_font));
                }
                cell = new PdfPCell(nameInvPar);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                Paragraph reasonPar = new Paragraph();
                reasonPar.add(new Chunk("Основание: ", bold_font));
                if (invoiceInfo.getPaymentCategoryId() == 3) {
                    reasonPar.add(new Chunk("возврат оплаты за учебу - " + myUI.getUser().getCurrent_year().getName(),
                            underlined_font));
                } else {
                    reasonPar.add(new Chunk("оплата за учебу - " + myUI.getUser().getCurrent_year().getName(),
                            underlined_font));
                }
                cell = new PdfPCell(reasonPar);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                Paragraph payTypePar = new Paragraph();
                payTypePar.add(new Chunk("Тип оплаты: ", bold_font));
                payTypePar.add(new Chunk(invoiceInfo.getPayment_type(), underlined_font));
                cell = new PdfPCell(payTypePar);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                Paragraph sumPar = new Paragraph();
                double rate = invoiceInfo.getKurs();
                sumPar.add(new Chunk("Сумма цифрами: ", bold_font));
                if (rate != 0) {
                    if (invoiceInfo.getCurrency_id() == 1) {
                        sumPar.add(new Chunk(Settings.dFormat2.format(Math.round(invoiceInfo.getAmount()))
                                + " сом (" + Settings.dFormat2.format(invoiceInfo.getAmount() / rate) + " USD)", underlined_font));
                    } else {
                        sumPar.add(new Chunk(Settings.dFormat2.format(Math.round(invoiceInfo.getAmount() * rate))
                                + " сом (" + Settings.dFormat2.format(invoiceInfo.getAmount()) + " USD)", underlined_font));
                    }
                }
                cell = new PdfPCell(sumPar);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                WritableSummRu convertToLetters = new WritableSummRuSOM();
                Paragraph sumLetterPar = new Paragraph();
                sumLetterPar.add(new Chunk("Сумма прописью: ", bold_font));
                if (invoiceInfo.getCurrency_id() == 1) {
                    sumLetterPar.add(new Chunk(convertToLetters.numberToString(
                            Math.round(invoiceInfo.getAmount())), normal_font));
                } else {
                    sumLetterPar.add(new Chunk(convertToLetters.numberToString(
                            Math.round(invoiceInfo.getAmount() * rate)), normal_font));
                }
                cell = new PdfPCell(sumLetterPar);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                Paragraph leftPar = new Paragraph();
                leftPar.add(new Chunk("Остаток: ", bold_font));
                leftPar.add(new Chunk(Settings.dFormat2.format(invoiceInfo.getLeft()) + " USD", underlined_font));
                cell = new PdfPCell(leftPar);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                try {
                    DbStudentInstallmentPlan dbip = new DbStudentInstallmentPlan();
                    dbip.connect();
                    IndexedContainer installmentCont = dbip.execSQL_InstPLan(myUI,
                            invoiceInfo.getStudent_id(), myUI.getUser().getCurrent_year().getId(), invoiceInfo.getPayments());
                    if (installmentCont.size() > 0) {
                        float[] table_plan_colsWidth = {0.75f, 3f, 3f};
                        PdfPTable table_plan = new PdfPTable(3);
                        table_plan.setWidthPercentage(90f);
                        table_plan.setWidths(table_plan_colsWidth);
                        table_plan.getDefaultCell().setBorder(Rectangle.BOTTOM);
                        table_plan.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
                        table_plan.addCell(new Phrase(" №", table_bold_font));
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Date), table_bold_font));
                        table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        table_plan.addCell(new Phrase(myUI.getMessage(IndigoMessages.Amount), table_bold_font));

                        Iterator<?> iter = installmentCont.getItemIds().iterator();
                        int i = 0;
                        if (installmentCont.size() > 0) {
                            i = 1;
                        }
                        while (iter.hasNext()) {
                            Object next = iter.next();
                            table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            table_plan.addCell(new Phrase(i + "", table_font));
                            table_plan.addCell(new Phrase(installmentCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Date)).getValue().toString(), table_font));
                            table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                            table_plan.addCell(new Phrase(Settings.dFormat2.format(installmentCont.getContainerProperty(next,
                                    myUI.getMessage(IndigoMessages.Amount)).getValue()) + "$", table_font));
                            table_plan.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                            i++;
                        }
                        cell = new PdfPCell(table_plan);
                        cell.setBorder(Rectangle.NO_BORDER);
                        cell.setPaddingTop(2);
                        cell.setPaddingBottom(3);
                        cell.setPaddingLeft(25);
                        cell.setPaddingRight(60);
                        invoiceTable.addCell(cell);
                    }
                    dbip.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }

                Paragraph par = new Paragraph();
                par.add(new Chunk("М. П. (штампа)", normal_font));
                cell = new PdfPCell(par);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(7);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                par = new Paragraph();
                par.add(new Chunk("Кассир: __________________", bold_font));
                cell = new PdfPCell(par);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(3);
                cell.setPaddingBottom(3);
                cell.setPaddingLeft(15);
                cell.setPaddingRight(10);
                invoiceTable.addCell(cell);

                cell = new PdfPCell(invoiceTable);
                cell.setBorder(Rectangle.RIGHT);
                cell.setPaddingTop(0);
                cell.setPaddingBottom(0);
                cell.setPaddingLeft(0);
                mainTable.addCell(cell);

                cell = new PdfPCell(invoiceTable);
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(0);
                cell.setPaddingBottom(0);
                cell.setPaddingLeft(0);
                mainTable.addCell(cell);
                cell = new PdfPCell(new Phrase(" "));
                cell.setBorder(Rectangle.BOTTOM);
                mainTable.addCell(cell);
                cell = new PdfPCell(new Phrase(" "));
                cell.setBorder(Rectangle.BOTTOM);
                mainTable.addCell(cell);
                //document.add(new Paragraph(30, "  "));
                document.add(mainTable);

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
        };

        String nameOf = "Invoice";
        StreamResource resource = new StreamResource(source1, nameOf
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");
        myUI.getPage().

                open(resource, nameOf, false);

    }

    private static final DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }
    };

}
