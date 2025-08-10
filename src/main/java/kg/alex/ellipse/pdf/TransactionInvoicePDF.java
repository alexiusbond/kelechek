package kg.alex.ellipse.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.vaadin.server.StreamResource;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.domain.AccTransaction;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.utils.money.WritableSummRu;
import kg.alex.ellipse.utils.money.WritableSummRuSOM;
import kg.alex.ellipse.utils.money.WritableSummRuUSD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TransactionInvoicePDF {

    static final Logger logger = LogManager.getLogger(TransactionInvoicePDF.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public TransactionInvoicePDF(final MyVaadinUI myUI, final AccTransaction tr, final String school,
                                 final String logo_name, final String orderName) {

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {


                document = new Document(PageSize.A4, 10, 10, 10, 10);
                PdfWriter.getInstance(document, buffer);

                final String FONT_LOCATION = "/home/ellipse/PT_Sans-Web-Regular.ttf";
                final String FONT_LOCATION_BOLD = "/home/ellipse/PT_Sans-Web-Bold.ttf";

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFont_bold = BaseFont.createFont(FONT_LOCATION_BOLD,
                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font title_font = new Font(baseFont, 14, Font.NORMAL);
                Font normal_font = new Font(baseFont, 11, Font.NORMAL);
                Font underlined_font = new Font(baseFont, 11, Font.UNDERLINE);
                Font bold_font = new Font(baseFont_bold, 11, Font.NORMAL);

                document.open();

                float[] dateTableWidth = {1f, 1f};
                PdfPTable dateTable = new PdfPTable(2);
                dateTable.setWidths(dateTableWidth);
                dateTable.getDefaultCell().setLeading(20, 0);
                dateTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                dateTable.addCell(new Phrase("Номер документа", normal_font));
                dateTable.addCell(new Phrase("Дата составления", normal_font));
                dateTable.addCell(new Phrase(String.format("%07d", tr.getOrder_number()), normal_font));
                dateTable.addCell(new Phrase(Settings.dtmf.format(tr.getDate()), normal_font));

                float[] orderTableWidth = {0.6f, 0.4f};
                PdfPTable orderTable = new PdfPTable(2);
                orderTable.setWidths(orderTableWidth);
                orderTable.getDefaultCell().setLeading(20, 0);
                orderTable.getDefaultCell().setBorder(0);
                PdfPCell cell = new PdfPCell(new Phrase(school, title_font));
                cell.setColspan(2);
                cell.setBorder(0);
                cell.setPaddingBottom(20);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                orderTable.addCell(cell);

                cell = new PdfPCell(new Phrase(orderName, bold_font));
                cell.setBorder(0);
                cell.setPaddingBottom(20);
                orderTable.addCell(cell);

                Image logo;
                try {
                    logo = Image.getInstance(Settings.PATH_TO_UPLOADS + logo_name);
                    logo.setAlignment(Image.MIDDLE);
                    logo.scaleAbsolute(70, 70);
                    cell = new PdfPCell(logo);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBorder(0);
                    cell.setRowspan(2);
                    cell.setPaddingBottom(5);
                    orderTable.addCell(cell);
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                orderTable.addCell(dateTable);

                Paragraph reasonPar = new Paragraph();
                reasonPar.add(new Chunk("Категория: ", bold_font));
                reasonPar.add(new Chunk(tr.getCategory(), underlined_font));
                cell = new PdfPCell(reasonPar);
                cell.setColspan(2);
                cell.setLeading(20, 0);
                cell.setBorder(0);
                orderTable.addCell(cell);

                Paragraph payTypePar = new Paragraph();
                payTypePar.add(new Chunk("Примечание: ", bold_font));
                payTypePar.add(new Chunk(tr.getNote(), underlined_font));
                cell = new PdfPCell(payTypePar);
                cell.setColspan(2);
                cell.setLeading(20, 0);
                cell.setBorder(0);
                orderTable.addCell(cell);

                Paragraph sumPar = new Paragraph();
                sumPar.add(new Chunk("Сумма цифрами: ", bold_font));
                if (tr.getCashbox().getCurrency_id() == 2) {
                    sumPar.add(new Chunk(Settings.dFormat2.format(tr.getAmount()) + " USD", underlined_font));
                } else {
                    sumPar.add(new Chunk(Settings.dFormat2.format(tr.getAmount()) + " KGS", underlined_font));
                }

                cell = new PdfPCell(sumPar);
                cell.setColspan(2);
                cell.setLeading(20, 0);
                cell.setBorder(0);
                orderTable.addCell(cell);

                WritableSummRu convertToLetters;

                if (tr.getCashbox().getCurrency_id() == 2) {
                    convertToLetters = new WritableSummRuUSD();
                } else {
                    convertToLetters = new WritableSummRuSOM();
                }

                Paragraph sumLetterPar = new Paragraph();
                sumLetterPar.add(new Chunk("Сумма прописью: ", bold_font));

                sumLetterPar.add(new Chunk(convertToLetters.numberToString(
                        tr.getAmount()), underlined_font));
                cell = new PdfPCell(sumLetterPar);
                cell.setColspan(2);
                cell.setLeading(20, 0);
                cell.setBorder(0);
                orderTable.addCell(cell);

                Paragraph receivedPar = new Paragraph();
                if (orderName.equals(myUI.getMessage(Messages.IncomeOrder))) {
                    receivedPar.add(new Chunk("Выдал: ", bold_font));
                } else {
                    receivedPar.add(new Chunk("Получил: ", bold_font));
                }
                if (tr.getFrom_to_employee() != null) {
                    receivedPar.add(new Chunk(tr.getFrom_to_employee(), underlined_font));
                } else {
                    receivedPar.add(new Chunk("__________________________________________", bold_font));
                }
                cell = new PdfPCell(receivedPar);
                cell.setLeading(20, 0);
                cell.setBorder(0);
                orderTable.addCell(cell);
                orderTable.addCell(new Phrase("Подпись _____________", bold_font));

                Paragraph accPar = new Paragraph();
                if (orderName.equals(myUI.getMessage(Messages.IncomeOrder))) {
                    accPar.add(new Chunk("Получил: ", bold_font));
                } else {
                    accPar.add(new Chunk("Выдал: ", bold_font));
                }
                accPar.add(new Chunk(tr.getEmployee(), underlined_font));
                cell = new PdfPCell(accPar);
                cell.setLeading(20, 0);
                cell.setBorder(0);
                orderTable.addCell(cell);
                orderTable.addCell(new Phrase("Подпись _____________", bold_font));

                document.add(new Paragraph(30, "  "));
                document.add(orderTable);

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
        myUI.getPage().open(resource, nameOf, false);
    }

}
