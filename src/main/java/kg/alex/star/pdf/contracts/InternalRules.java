package kg.alex.star.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.server.StreamResource;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.Settings;
import kg.alex.star.domain.StudentInfoPdf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class InternalRules {

    static final Logger logger = LogManager.getLogger(InternalRules.class);
    private final static String FONT_LOCATION = "/home/star/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/star/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public InternalRules(final MyVaadinUI ui, StudentInfoPdf st_info) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 20, 20, 30, 35);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font captionFont = new Font(baseFontBold, 14f);
                Font ordFont = new Font(baseFont, 12f);
                Font ordBoldFont = new Font(baseFontBold, 12f);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase(st_info.getYear().getName() + "-окуу жылына карата ички тартип эрежелери", captionFont));

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Chunk("1.1. ", ordBoldFont));
                paragraph.add(new Chunk("Окуучулар жатаканадан саат 07:45те чыгышы, ал эми саат 21:50дө жатаканага кайтып кириши милдеттүү.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("2.1.2. ", ordBoldFont));
                paragraph.add(new Chunk("Окуучунун келишимдин предмети болгон окутуу программасын толук өздөштүрүүсү үчүн, ага Мектеп тарабынан белгиленген тартипте методикалык жана консультациялык жардам көрсөтүүгө;", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("2. ", ordBoldFont));
                paragraph.add(new Chunk("Мектеп формасын кийбеген же толук эмес кийген окуучулар эрте мененки жалпы жыйында (линейка) аныкталат жана завучтун уруксаты жок сабакка киргизилбейт.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("3. ", ordBoldFont));
                paragraph.add(new Chunk("Окуучу өзүнүн сырткы көрүнүшүнө көңүл бурууга милдеттүү.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("Төмөнкү көрүнүштөргө уруксат берилбейт:", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– чачты өзгөчө формаларда кырдыруу же боёо", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– сакал, мурут өстүрүү", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– сөйкө, шакек, чынжырча тагуу", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– ийне менен чегип сүрөт түшүрүү (татуировка)", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("4. ", ordBoldFont));
                paragraph.add(new Chunk("Окуучулардын жүрүм-туруму менен мектеп эрежелеринин сакталышын көзөмөлдөөгө ар бир мугалим жана тарбиячы укуктуу.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("5. ", ordBoldFont));
                paragraph.add(new Chunk("Мектепке чөнтөк телефон алып келүүгө уруксат берилбейт. Эгер окуучу мектеп аймагында телефон колдонсо, ал дароо эскертүүсүз алынып, окуу жылынын аягында гана кайтарылат.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("6. ", ordBoldFont));
                paragraph.add(new Chunk("Окуучу мугалимге, тарбиячыга жана башка окуучуларга сылык мамиле кылууга милдеттүү. Сабактын же этюддун жүрүшүнө тоскоолдук жаратпоосу керек.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("7. ", ordBoldFont));
                paragraph.add(new Chunk("Мектепте иштеген жетекчилерге, мугалимдерге, тарбиячыларга, техникалык кызматкерлерге жана окуучуларга сылык жана сыпайы мамиле кылуу талап кылынат.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("8. ", ordBoldFont));
                paragraph.add(new Chunk("Төмөнкү жүрүм-турумдарга тыюу салынат: уруш чыгаруу, жаман сөз айтуу, окуучуларга ат коюп шылдыңдоо, үстөмдүк кылуу, акча талап кылуу, карызга акча алуу, өзүнчө топ түзүү жана сырттагы топторго кошулуу.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("9. ", ordBoldFont));
                paragraph.add(new Chunk("Окуу-тарбия процессине зыян келтирүүчү материалдарды (журнал, гезит, брошюра, аудио жана видео) мектепке алып келүүгө, таратууга жана колдонууга тыюу салынат. Айрыкча, ар кандай саясий партиялардын жана диний агымдардын үгүт материалдарына жол берилбейт.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("10. ", ordBoldFont));
                paragraph.add(new Chunk("Мектепке зыяндуу буюмдарды алып келүүгө, колдонууга жана таратууга тыюу салынат. Аларга бычак жана башка курч буюмдар, тамеки, насвай, спирт ичимдиктери, электрондук сигареттер, энергия берүүчү ичимдиктер жана психикага таасир этүүчү заттар кирет.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("11. ", ordBoldFont));
                paragraph.add(new Chunk("Окуучу өзүнө тиешелүү буюмдарга сарамжалдуу мамиле кылууга тийиш. Эгерде окуучу мектепке акча, телефон, планшет жана башка баалуу буюмдарды алып келсе, аларды тарбиячысына же класс жетекчисине тапшыруусу керек. Эгерде тапшырбай жоготуп койсо, мектеп жоопкерчилик албайт.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("12. ", ordBoldFont));
                paragraph.add(new Chunk("Мектептен сыртка чыгуу үчүн сабак учурунда (башкача айтканда, 8:30дан 16:00гө чейин) директордун орун басарлары уруксат берет. Ал эми калган убакта жатаканадан чыгуу үчүн жатакана жетекчиси жооп берет. Класс жетекчиси же тарбиячы ата-эненин макулдугу менен гана уруксат кагазын жазып бере алат. Окуучу сыртка чыккандан кийинки жоопкерчилик ата-энеге өтөт.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("13. ", ordBoldFont));
                paragraph.add(new Chunk("Сабак учурунда жатаканадан уруксатсыз чыгып кетүүгө же кирүүгө, ошондой эле бекенип калууга тыюу салынат.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("14. ", ordBoldFont));
                paragraph.add(new Chunk("Эгер окуучу ооруп калса, изоляторго дарыгердин же медайымдын уруксаты жана көзөмөлү менен гана жаткырылат.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("15. ", ordBoldFont));
                paragraph.add(new Chunk("Окуучу класс бөлмөлөрүн жана жатаканадагы өз бөлмөсүн таза жана тыкан абалда кармоого милдеттүү.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("16. ", ordBoldFont));
                paragraph.add(new Chunk("Электрондук сигарет, тамеки, спирт ичимдиктери, адамдын ден соолугуна зыян келтирүүчү ар кандай курал-жарактар, ошондой эле смартфондор сыяктуу зыяндуу буюмдарды колдонууга тыюу салынат. Булардын колдонулушунун алдын алуу үчүн мугалимдер жана тарбиячылар мектептин ичинде издөө жүргүзүүгө укуктуу.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("17. ", ordBoldFont));
                paragraph.add(new Chunk("Жатаканада жашаган жана окуган окуучулар жекшемби күнү саат 18:00гө чейин мектепке кайтып келүүгө милдеттүү.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("18. ", ordBoldFont));
                paragraph.add(new Chunk("Эгер окуучу себептүү же себепсиз 40 күн сабакты калтырса, педагогикалык кеңештин чечими менен класстан калтырылышы мүмкүн.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Chunk("19. ", ordBoldFont));
                paragraph.add(new Chunk("Окуу-тарбия процессине зыян келтирген жана мектеп эрежелерин бузган окуучуларга төмөнкү тартиптик чаралар колдонулат:", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– Эреже бузган окуучудан түшүндүрмө каты алынат жана тарбия журналына катталып, мектептин тартип комиссиясына өткөрүлөт.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– Эреже бузуу мүнөзүнө жараша тартип комиссиясы тарабынан жаза белгиленет. Зарыл учурда ата-энеси чакырылат.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– Эреже бузуунун оордугуна жараша окуучу убактылуу жатаканадан четтетилиши же мектептен алган контрактык жеңилдиктеринен ажыратылышы мүмкүн.", ordFont));
                paragraph.add(Chunk.NEWLINE);

                paragraph.add(new Phrase("– Тартип комиссиясынын эскертүүсүнө карабай эреже бузуулар улана турган болсо, окуучу педагогикалык кеңешке сунушталып, анын чечими менен мектептен чыгарылышы мүмкүн.", ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Төмөндө жазылган ички тартип эрежелери менен толук таанышып чыктым жана аны кабыл алам.", ordBoldFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Ата-эненин аты-жөнү: " + studentInfo.getMainRelative().getFullName(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("                            (колу) _______________________________", ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                String studentFullName = studentInfo.getStudent().getSurname() + " " +
                        studentInfo.getStudent().getName();
                if (studentInfo.getStudent().getMiddle_name() != null
                        && !studentInfo.getStudent().getMiddle_name().isEmpty()) {
                    studentFullName += " " + studentInfo.getStudent().getMiddle_name();
                }
                paragraph.add(new Phrase("Окуучунун аты-жөнү: " + studentFullName, ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Chunk("Күнү: ", ordFont));
                paragraph.add(new Chunk(Settings.dateKg.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(paragraph);
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

        String nameOf = "Contract";
        StreamResource resource = new StreamResource(source1, nameOf
                + System.currentTimeMillis() + ".pdf");
        resource.setMIMEType("application/pdf");

        myUI.getPage().open(resource, nameOf, false);
    }

    private static class myPageEvent extends PdfPageEventHelper {

        Font f_font = new Font(Font.FontFamily.UNDEFINED, 10, Font.NORMAL);

        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            try {
                Phrase ft = new Phrase(String.format(" %d",
                        writer.getPageNumber()), f_font);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, ft,
                        (document.right() - 30),
                        document.bottom() - 15, 0);

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

}
