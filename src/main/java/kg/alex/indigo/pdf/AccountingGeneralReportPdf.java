package kg.alex.indigo.pdf;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.domain.ContractInfo;
import kg.alex.indigo.domain.School;
import kg.alex.indigo.domain.SchoolAccounting;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.utils.FormattedTable;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Iterator;

public class AccountingGeneralReportPdf {

    static final Logger logger = LogManager.getLogger(AccountingGeneralReportPdf.class);
    private final String svgStrPayments;
    private final String svgStrDiscounts;
    private final String svgStrPaid;
    private final Date aDate = new Date(System.currentTimeMillis());
    private final SchoolAccounting sclAccInfo;
    private final FormattedTable accTransactionsTable;
    private final FormattedTable paymentsTable;
    private final ContractInfo contrTtl;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private PdfWriter writer;

    public AccountingGeneralReportPdf(final MyVaadinUI myUI, String svgPms, String svgPaid, String svgDisc,
                                      SchoolAccounting schoolAcc, FormattedTable transactionsTable, ContractInfo contractTtl,
                                      FormattedTable pTable, final int scl_id, final String year, final String prevDay) {
        svgStrPayments = svgPms;
        svgStrDiscounts = svgDisc;
        svgStrPaid = svgPaid;
        sclAccInfo = schoolAcc;
        accTransactionsTable = transactionsTable;
        contrTtl = contractTtl;
        paymentsTable = pTable;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {


                document = new Document(PageSize.A4, 10, 10, 10, 10);
                writer = PdfWriter.getInstance(document, buffer);
                School school = null;
                try {
                    DbSchool dbs = new DbSchool();
                    dbs.connect();
                    school = dbs.execSchool(scl_id);
                    dbs.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                HeaderFooterPortrait event = new HeaderFooterPortrait(myUI, school.getName_ru(),
                        school.getAddress(), school.getPhone());
                writer.setPageEvent(event);

                final String FONT_LOCATION = "/home/indigo/PT_Sans-Web-Regular.ttf";
                final String FONT_LOCATION_BOLD = "/home/indigo/PT_Sans-Web-Bold.ttf";

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION,
                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFont_bold = BaseFont.createFont(FONT_LOCATION_BOLD,
                        BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font title_font = new Font(baseFont_bold, 14, Font.NORMAL);
                Font normal_font = new Font(baseFont, 8, Font.NORMAL);
                Font normal_bold_font = new Font(baseFont_bold, 8, Font.NORMAL);
                Font date_normal_font = new Font(baseFont, 10, Font.NORMAL);
                Font caption_bold_font = new Font(baseFont_bold, 10, Font.NORMAL);

                document.open();
                //дата
                document.add(new Paragraph(45, " "));
                float[] dates_table_colsWidth = {4f, 1f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(dates_table_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.addCell(new Phrase(" ", normal_font));
                table_date.addCell(new Phrase("Дата: " + Settings.df.format(aDate), date_normal_font));
                document.add(table_date);

                //информация по кассе
                Paragraph header = new Paragraph(myUI.getMessage(IndigoMessages.GeneralAccountingReport) + " (" + year + ")", title_font);
                header.setAlignment(Element.ALIGN_CENTER);
//                    document.add(new Paragraph(45, " "));
                document.add(header);
                document.add(new Paragraph(15, " "));
                Paragraph caption1 = new Paragraph(myUI.getMessage(IndigoMessages.AccountingInformationCaption), caption_bold_font);
                caption1.setIndentationLeft(30);
                document.add(caption1);
                document.add(new Paragraph(10, " "));

                float[] accTrInfo_colsWidth = {0.1f, 0.1f, 0.1f, 0.1f};
                PdfPTable accInfoTable = new PdfPTable(4);
                accInfoTable.setWidthPercentage(90f);
                accInfoTable.setWidths(accTrInfo_colsWidth);
                accInfoTable.getDefaultCell().setBorder(0);

                accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                accInfoTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.IncomesTotal) + ":", normal_font));
                accInfoTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.LastIncomeDate) + ":", normal_font));
                accInfoTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.ExpensesTotal) + ":", normal_font));
                accInfoTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.LastExpenseDate) + ":", normal_font));
                accInfoTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.PreviousBalance) + " (" + prevDay + "):", normal_font));
                accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                accInfoTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.CashBox) + ":", normal_font));

                accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                accInfoTable.addCell(new Phrase(Settings.dFormat2.format(sclAccInfo.getTotal_income()) + "$", normal_font));
                accInfoTable.addCell(new Phrase(Settings.dFormat2.format(sclAccInfo.getTotal_outcome()) + "$", normal_font));
                accInfoTable.addCell(new Phrase(Settings.dFormat2.format(sclAccInfo.getPrevious_balance()) + "$", normal_font));
                accInfoTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                accInfoTable.addCell(new Phrase(Settings.dFormat2.format(sclAccInfo.getPrevious_balance() + sclAccInfo.getTotal_income()
                        - sclAccInfo.getTotal_outcome()) + "$", normal_font));

                document.add(accInfoTable);

                //Сумма доходов и расходов по месяцам
                document.add(new Paragraph(20, " "));
                Paragraph caption2 = new Paragraph(myUI.getMessage(IndigoMessages.IncomeOutcomeMonthlyCaption), caption_bold_font);
                caption2.setIndentationLeft(30);
                document.add(caption2);
                document.add(new Paragraph(10, " "));

                float[] accTrans_colsWidth = {0.1f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f};
                PdfPTable accTransTable = new PdfPTable(7);
                accTransTable.setWidthPercentage(90f);
                accTransTable.setWidths(accTrans_colsWidth);

                accTransTable.addCell(new Phrase(" "));
                accTransTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Month), normal_bold_font));
                accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                accTransTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.InstallmentPlan), normal_bold_font));
                accTransTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Payments), normal_bold_font));
                accTransTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Incomes), normal_bold_font));
                accTransTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Expenses), normal_bold_font));
                accTransTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Difference), normal_bold_font));

                Iterator<?> iter = accTransactionsTable.getItemIds().iterator();
                int i = 0;
                if (accTransactionsTable.size() > 0) {
                    i = 1;
                }
                while (iter.hasNext()) {
                    Object next = iter.next();
                    accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    accTransTable.addCell(new Phrase(i + "", normal_font));
                    accTransTable.addCell(new Phrase(accTransactionsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Month)).getValue().toString(), normal_font));
                    accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    accTransTable.addCell(new Phrase(Settings.dFormat2.format(accTransactionsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.InstallmentPlan)).getValue()), normal_font));
                    accTransTable.addCell(new Phrase(Settings.dFormat2.format(accTransactionsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Payments)).getValue()), normal_font));
                    accTransTable.addCell(new Phrase(Settings.dFormat2.format(accTransactionsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Incomes)).getValue()), normal_font));
                    accTransTable.addCell(new Phrase(Settings.dFormat2.format(accTransactionsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Expenses)).getValue()), normal_font));
                    accTransTable.addCell(new Phrase(Settings.dFormat2.format(accTransactionsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Difference)).getValue()), normal_font));
                    i++;

                }
                accTransTable.addCell(new Phrase(" "));
                accTransTable.addCell(new Phrase(" "));
                accTransTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.InstallmentPlan)), normal_bold_font));
                accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Payments)), normal_bold_font));
                accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Incomes)), normal_bold_font));
                accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Expenses)), normal_bold_font));
                accTransTable.addCell(new Phrase(accTransactionsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Difference)), normal_bold_font));

                document.add(accTransTable);
                //Итого     Скидки   Оплаты
                //inner table
                document.add(new Paragraph(15, " "));
                float[] ttlContr_colsWidth = {0.1f, 0.1f};
                PdfPTable ttlContrTable = new PdfPTable(2);
                ttlContrTable.setWidthPercentage(100f);
                ttlContrTable.setWidths(ttlContr_colsWidth);
                ttlContrTable.getDefaultCell().setBorder(0);
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Students) + ":", normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getStudents() + "", normal_font));
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.TotalContract), normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getContract() + "", normal_font));
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.TotalDebt), normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getDebt() + "", normal_font));
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.TotalDiscount), normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getDiscount() + "", normal_font));
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.TotalCorrection), normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getCorrection() + "", normal_font));
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Net), normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getNet() + "", normal_font));
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.TotalPayment), normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getPaid() + "", normal_font));
                ttlContrTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.TotalLeft), normal_bold_font));
                ttlContrTable.addCell(new Phrase(contrTtl.getLeft() + "", normal_font));

                float[] ttl_colsWidth = {0.5f, 0.95f, 1.05f};
                PdfPTable ttlTable = new PdfPTable(3);
                ttlTable.setWidthPercentage(90f);
                ttlTable.setWidths(ttl_colsWidth);
                ttlTable.getDefaultCell().setBorder(0);
                ttlTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Total), caption_bold_font));
                ttlTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Discounts), caption_bold_font));
                ttlTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Payments), caption_bold_font));
                ttlTable.addCell(ttlContrTable);
                ttlTable.getDefaultCell().setFixedHeight(75f);
                ttlTable.addCell(createSvgImage(writer.getDirectContent(), svgStrDiscounts));
                ttlTable.addCell(createSvgImage(writer.getDirectContent(), svgStrPaid));
                document.add(ttlTable);

                //Сумма оплат и плана оплат по месяцам
                //inner table
                document.add(new Paragraph(20, " "));
                Paragraph caption3 = new Paragraph(myUI.getMessage(IndigoMessages.PaymentsMonthlyCaption), caption_bold_font);
                caption3.setIndentationLeft(30);
                document.add(caption3);
                document.add(new Paragraph(10, " "));

                float[] installment_table_plan_colsWidth = {0.15f, 0.4f, 0.4f, 0.4f, 0.4f};
                PdfPTable inPaymentsTable = new PdfPTable(5);
                inPaymentsTable.setWidthPercentage(100f);
                inPaymentsTable.setWidths(installment_table_plan_colsWidth);
                inPaymentsTable.addCell(new Phrase(""));
                inPaymentsTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Month), normal_bold_font));
                inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                inPaymentsTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.InstallmentPlan), normal_bold_font));
                inPaymentsTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Payments), normal_bold_font));
                inPaymentsTable.addCell(new Phrase(myUI.getMessage(IndigoMessages.Debt), normal_bold_font));

                Iterator<?> iter1 = paymentsTable.getItemIds().iterator();
                int x = 0;
                if (paymentsTable.size() > 0) {
                    x = 1;
                }
                while (iter1.hasNext()) {
                    Object next = iter1.next();
                    inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                    inPaymentsTable.addCell(new Phrase(x + "", normal_font));
                    inPaymentsTable.addCell(new Phrase(paymentsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Month)).getValue().toString(), normal_font));
                    inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                    inPaymentsTable.addCell(new Phrase(Settings.dFormat2.format(paymentsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.InstallmentPlan)).getValue()), normal_font));
                    inPaymentsTable.addCell(new Phrase(Settings.dFormat2.format(paymentsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Payments)).getValue()), normal_font));
                    inPaymentsTable.addCell(new Phrase(Settings.dFormat2.format(paymentsTable.getContainerProperty(next,
                            myUI.getMessage(IndigoMessages.Debt)).getValue()), normal_font));
                    x++;
                }

                inPaymentsTable.addCell(new Phrase(" "));
                inPaymentsTable.addCell(new Phrase(" "));
                inPaymentsTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                inPaymentsTable.addCell(new Phrase(paymentsTable.getColumnFooter(myUI.getMessage(IndigoMessages.InstallmentPlan)), normal_bold_font));
                inPaymentsTable.addCell(new Phrase(paymentsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Payments)), normal_bold_font));
                inPaymentsTable.addCell(new Phrase(paymentsTable.getColumnFooter(myUI.getMessage(IndigoMessages.Debt)), normal_bold_font));

                //outer table
                float[] colsWidth = {0.45f, 0.55f};
                PdfPTable paymentsTable = new PdfPTable(2);
                paymentsTable.setWidthPercentage(90f);
                paymentsTable.setWidths(colsWidth);
                paymentsTable.getDefaultCell().setBorder(0);
                paymentsTable.addCell(inPaymentsTable);
                paymentsTable.addCell(createSvgImage(writer.getDirectContent(), svgStrPayments));
                document.add(paymentsTable);

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

        StreamResource resource = new StreamResource(source1, "AccountingGeneralReport"
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, "AccountingGeneralReport", false);
    }

    private Image drawUnscaledSvg(PdfContentByte contentByte, String svg)
            throws IOException {

        // First, lets create a graphics node for the SVG image.
        GraphicsNode imageGraphics = buildBatikGraphicsNode(svg);

        float width = (float) imageGraphics.getBounds().getWidth();
        float height = (float) imageGraphics.getBounds().getHeight();

        // Create a PDF template for the SVG image
        PdfTemplate template = contentByte.createTemplate(width, height);
        // Create Graphics2D rendered object from the template
        Graphics2D graphics = template.createGraphics(width, height);
        try {
            // SVGs can have their corner at coordinates other than (0,0).
            Rectangle2D bounds = imageGraphics.getBounds();
            graphics.translate(-bounds.getX(), -bounds.getY());

            // Paint SVG GraphicsNode with the 2d-renderer.
            imageGraphics.paint(graphics);

            // Create and return a iText Image element that contains the SVG
            // image.
            return new ImgTemplate(template);
        } catch (BadElementException e) {
            throw new RuntimeException("Couldn't generate PDF from SVG", e);
        } finally {
            // Manual cleaning (optional)
            graphics.dispose();
        }
    }

    /**
     * Use Batik SVG Toolkit to create GraphicsNode for the target SVG.
     * <ol>
     * <li>Create SVGDocument</li>
     * <li>Create BridgeContext</li>
     * <li>Build GVT tree. Results to GraphicsNode</li>
     * </ol>
     *
     * @param svg SVG as a String
     * @return GraphicsNode
     * @throws IOException Thrown when SVG could not be read properly.
     */
    private GraphicsNode buildBatikGraphicsNode(String svg) throws IOException {
        UserAgent agent = new UserAgentAdapter();

        SVGDocument svgDoc = createSVGDocument(svg, agent);

        DocumentLoader loader = new DocumentLoader(agent);
        BridgeContext bridgeContext = new BridgeContext(agent, loader);
        bridgeContext.setDynamicState(BridgeContext.STATIC);

        GVTBuilder builder = new GVTBuilder();

        return builder.build(bridgeContext, svgDoc);
    }

    private SVGDocument createSVGDocument(String svg, UserAgent agent)
            throws IOException {
        SVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(
                agent.getXMLParserClassName(), true);

        return documentFactory.createSVGDocument(null, new StringReader(svg));
    }

    private Image createSvgImage(PdfContentByte contentByte, String imgSvg) throws IOException {
        Image image = drawUnscaledSvg(contentByte, imgSvg);
        image.scalePercent(100);
        return image;
    }

}
