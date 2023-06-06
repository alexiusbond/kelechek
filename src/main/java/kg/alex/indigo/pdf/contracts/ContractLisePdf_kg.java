package kg.alex.indigo.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.dao.DbRelative;
import kg.alex.indigo.domain.StudentInfoPdf;
import kg.alex.indigo.i18n.IndigoMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ContractLisePdf_kg {

    static final Logger logger = LogManager.getLogger(ContractLisePdf_2023_ru.class);
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;

    private final static String FONT_LOCATION = "/home/indigo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/indigo/TimesNewRomanBold.ttf";

    public ContractLisePdf_kg(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 30, 30);
                document.setMargins(10, 10, 30, 30);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10);
                Font ordBoldFont = new Font(baseFontBold, 10);
                Font boldFont = new Font(baseFontBold, 11);
                Font font_header = new Font(baseFontBold, 11);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Акылуу билим берүү кызматын көрсөтүү боюнча ", font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("КЕЛИШИМ № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                float[] table_date_colsWidth = {4.5f, 1.5f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase(studentInfo.getSchool().getCity() + " ш.", ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.df.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Уставдын негизинде ишмердүүлүгүн  жүргүзгөн “" + studentInfo.getSchool().getName_kg()
                        + "” билим берүү мекемесинин (мындан ары “Лицей” деп белгиленет) атынан директору ", ordFont));
                String fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name()!=null && !studentInfo.getDirector().getMiddle_name().equals("")) {
                    fullName += " " + studentInfo.getDirector().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" биринчи тараптан жана ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName(), ordBoldFont));
                paragraph.add(new Phrase(" окуучунун ата-энеси (мындан ары“Ата-эне” деп белгиленет) экинчи тараптан ", ordFont));
                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                if (!studentInfo.getStudent().getMiddle_name().equals("")) {
                    fullName = fullName + " " + studentInfo.getStudent().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" окуучунун кызыкчылыгы үчүн Кыргыз Республикасынын “Билим берүү жөнүндө” мыйзамына ылайык төмөндөгү келишимди түзүштү:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("1. Келишимдин предмети", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Окуучунун окутуу, тарбиялоо процессин жана толук пансиондук шарттарына ылайык жашоосун уюштуруу, ", ordFont));
                paragraph.add(new Phrase(studentInfo.getPeriod_kg(), ordBoldFont));
                paragraph.add(new Phrase(" чейин мамлекеттик билим берүү стандартынын жана жалпы орто билим берүүчү мектептин программасынын алкагында билим алуусу Келишимдин предмети болуп саналат.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2. Тараптардын укук жана милдеттери", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.1. Лицейдин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.1. Келишимдин 1.1.-пунктунда  каралган кызматтарды уюштуруу жана камсыз кылуу. Билим берүү кызматтары Кыргыз Республикасынын билим берүү жана илим министрлиги тарабынан бекитилген мамлекеттик билим берүү стандартына жана тиешелүү деңгээлдеги программасына ылайык көрсөтүлөт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.2. Келишимдин предмети болгон билим берүүчү программаларынын Окуучу тарабынан өздөштүрүүсү максатында Лицей тарабынан белгиленген тартипте Окуучуга жеке билим берүү жардамын көрсөтүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.3. Окуучуну Лицейдин китепканасындагы окуу китептери жана окутуунун II, III баскычтарынын (керектүүсүнүн асты сызылат) предмет жана окуу планынын программасына тиешелүү башка адабияттар менен камсыз кылуу, Окуучуга жабдылган окуу кабинеттерин, компьютердик класс, китепкананы колдонууга берүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.4. Объекттин ичиндеги өткөрмө режимдерин уюштурууну ишке ашыра турган Лицейдин башка структуралык бөлүмдөрү менен өз ара аракеттенишүүсүнүн аркасы менен Окуучунун коопсуздугун камсыздайт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.5. Окуучуну КР билим берүү жана илим министрлиги бекиткен окуу мөөнөтүнө чейин ыңгайлуу шарттары бар жатаканага жайгаштыруу. Келишим бузулган учурда, Окуучунун жатаканада жашоо укугу токтойт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.6. Окуучуну жаш өзгөчөлүгүнө ылайык 3 маал тамак менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.7. Окуучуну лицейге алгач өткөндө бир жолу мектеп формасы менен камсыздоо,  муктаждык келип чыккан кийинки учурда мектеп формасы Ата-эне тарабынан сатып алынат.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.8. Окуучунун себептүү келбей калган күндөрүндө Окуучунун ордун сактоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.9. Кыргыз Республикасынын билим берүү жана илим министрлиги тарабынан бекитилген окуу планына ылайык, Лицейдин белгилеген тартибинде Окуучунун жарым жылдык жана жылдык аттестациядан өткөрүүнү ишке ашыруу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.10. Кыргыз Республикасынын билим берүү жана илим министрлигинин Жобосуна ылайык, Лицей белгилеген тартипте педагогикалык кеңештин чечими менен Окуучуну класстан класска көчүрүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.11. Окуучуга окутуунун II деңгээлин аяктаганда - күбөлүк, III деңгээлин аяктаганда белгиленген үлгүдөгү мамлекеттик аттестат берүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.12. Окуучунун жеке инсандыгына урмат көрсөтүү, физикалык жана психологиялык зордук көрсөтпөө, окуучунун жеке өзгөчөлүктөрүн эске алуу менен адеп-ахлактык,  физикалык жана психологиялык ден соолугун, эмоционалдык бейпилдигин бекемдөө үчүн шарттарды камсыздоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.13. Окуучунун ден соолугунун күнүмдүк көзөмөлүн медициналык квалификациясы документалдык түрдө тастыкталган Лицейдин медицина кызматкери тарабынан камсыздоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.14. Окуучуну Лицейдин ички локалдык актылары жана “Ички тартип эрежелери“, билим берүү чөйрөсүнүн коопсуздугу жана саламаттыкты сактоо боюнча эрежелерди сактоо менен тааныштыруу", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.15. Окуучунун ден соолугу, жетишкендиги жана жүрүш-турушу тууралуу ар бир чейректин жыйынтыгы боюнча Ата-энеге маалымат берүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.2 Лицейдин укуктары:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.1. Өз алдынча тамак тизмесин түзүү, тамактын түрлөрүн алмаштыруу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.2. Жылдык чыгашалар сметасына жараша ата-энелерден алынуучу акыларлын өлчөмүн өз алдынча белгилөө жана өзгөртүү. Педкеңештин чечимине ылайык  окуу төлөмдөрүнө  берилүүчү жеңилдиктердин көлөмүн өзгөртүүгө укуктуу. Муну менен катар, Лицей тарабынан берилген жеңилдиктер (жетишкендиги үчүн, олимпиаданын призерлору болгондугу үчүн, кабыл алуудагы жогорку баллы үчүн ж.б.) Ата-энеге эскертүүсүз ", ordFont));
                paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.3. Ата-эне тарабынан окуу төлөмү убагында төлөнбөгөн учурда, Ата-энеге маалымдоо аркылуу Окуучунун бардык окуу жана окуудан тышкаркы иш-чараларга катышуусун, материалдык-техникалык шарттарды (сабактар, китепканалар, ашкана, кружоктор, этюддар ж.б., сынактарга киргизбөө, Эдупейдж тутумуна баа көрсөтпөө) колдонуусун чектөө.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.4. Окуучу кийинки учурларда окуу төлөмдөрүнүн кайтарымысыз Лицейден окуудан чыгарылат:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("а) Лицейдин жатакананын жана “Ички тартип эрежелерин” орой, системалуу түрдө бузган учурда (спирт ичимдиктерин, наркотикалык каражаттарды колдонуу, электрондук тамеки, курамында никотин бар каражаттарды колдонуу, кумар оюндарын ойноо, тыныгуу  жана сабак учурунда Лицейдин аймагынан уруксатсыз чыгып кетүү);", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("б) Лицейдин кызматкерлерине жана өз курактагы балдарга мыйзамсыз аракеттерди көрсөткөн учурда (спирт ичимдиктерин, наркотикалык каражаттарды колдонуу, электрондук тамеки, курамында никотин бар каражаттарды колдонуу, кумар оюндарын ойноо, тыныгуу  жана сабак учурунда Лицейдин аймагынан уруксатсыз чыгып кетүү);", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("в) Кыргыз Республикасынын мыйзамдарын бузганда. (Кыргыз Республикасынын Жаза кодексинин 154-беренесинин негизинде жашы жете элек кызын /уулун үй-бүлө курууга мажбурлаган учурда Ата-энелер жооп тартат.)", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.5. Жатаканага саат 19:00дөн кийин кабыл албоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.6. “Кыргыз Республикасынын мамлекеттик жана мамлекеттик эмес жалпы орто билим берүү мекемелериндеги бүтүрүүчүлөрдүн мамлекеттик жыйынтыктоочу аттестациясы жана класстан класска көчүрүү тартиби тууралуу Жобонун” талаптарын аткарбаган учурда Окуучуга тиешелүү билими тууралуу мамлекеттик үлгүдөгү документти берүүдөн баш тартуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.7. Окуучу 45 күн себепсиз окууну калтырган учурда окутуу курсун кайталоо үчүн (экинчи жылга) калтырылат.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.8. Эмгекке тарбиялоонун компетенттүү ыкмасы катары Лицейдин администрациясы окуучуларды класстагы жана жатаканадагы жумушчу орундарын тазалоо жана иретке келтирүү жумуштарына иштетүүгө укугу бар.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.9. Лицей окуучулардын каалоосу жана макулдашуусу менен ЖРТга, TOEFL, SAT, IELTS ж.б. даярдык курстарын, окуучулардын кызыкчылыктарына ылайык ийримдик иштерди өзүнчө акы төлөө аркылуу уюштурат. Буга байланыштуу, Лицейдин администрациясы Окуучулардын лицейден тышкары жайларда кошумча билим алууларына тыюу салууга укуктуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.10. Өнөкөт оорулары (эпилепсия, астма, тубаса жүрөк оорусу, энурез ж.б.) боюнча диспансердик учетто турган окуууларга атайын шарттар түзүлбөйт. Окуучунун ата-энеси, же мыйзамдуу өкүлдөрү Лицейдин кызматкерине Окуучунун өнөкөт оорулары тууралуу маалыматты жазуу түрүндө билдирүүгө милдеттүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.11. Бул келишим Ата-эненин демилгеси менен бузулган учурда ", ordFont));
                paragraph.add(new Phrase("чейрек үчүн окуу акысын кармап калуу", ordBoldFont));
                paragraph.add(new Phrase(", мында мурун каралган жеңилдиктер ", ordFont));
                paragraph.add(new Phrase("эсепке алынбайт.", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.12. Бул келишим күтүлбөгөн жагдайлардын айынан бузулган учурда, кайтарыла турган сумма Лицейдин сарптаган бардык чыгымдарын эске алуу менен Лицейдин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айына чейин кайтарылат.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.13. Лицейдин ишмердүүлүгүн жарнамалоо максатында окуучуну жана ата-энесине кабардар кылбастан фото жана видеоматериалдарды жарыялоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.14. Лицей КР мыйзамдарында каралган башка укуктарга ээ болушу мүмкүн.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.3. Ата-эненин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.1. Бул келишимдин 1.1. пунктуна ылайык, Келишимдин 3.1.1.-3.1.4. пункттарында каралган мөөнөттөрдө, өз убагында окуу акыларын төлөө.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.2. Жыл сайын 1-апрелден 15-майга чейин ", ordFont));
                paragraph.add(new Phrase("30% алдын ала төлөмдү ", ordBoldFont));
                paragraph.add(new Phrase("сөзсүз төлөө менен кийинки окуу жылына Келишим түзүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Белгиленген мөөнөттө келишим түзүлбөгөн учурда", ordBoldFont));
                paragraph.add(new Phrase(" окуучунун орду башка окуучуларга берилиши мүмкүн.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.3. Лицейге келерден мурда Окуучуну толугу менен мединалык кароодон өткөрүү, Лицейдин администрациясына Окуучунун саламаттыгы тууралуу медициналык адистердин жыйынтыктарын тапшыруу. Жазуу түрүндө төмөнкү медициналык кызматтарга макулдугун берүү же баш тартуу:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("а) муктаждык болгон учурда Окуучуга медициналык жардам көрсөтүү;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("б) психологдун кызматы;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Керек болгон учурда кымбат дары-дармектерди сатып алууда же жеке менчик тез жардам кызматын чакырууда чыгымдарды төлөө.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.4. “Окуучунун анкетасын” (Тиркеме №____), “Өз алдынча чыгууга уруксат кагазын” (Тиркеме №____) жана “Окуучунун саламаттыгы тууралуу чынчыл маалыматтарды берүү арызын” (Тиркеме №____) толтуруу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.5. Окуучу тарабынан Лицейдин “Ички тартип эрежелерин” аткарууга көмөктөш болуу. Лицейдин “Ички тартип эрежелерин” бузган учурда Окуучуга таасир берүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.6. Окуучуну жеке колдонуусу үчүн бардык керектүү канцелярдык буюмдары (дептерлер, альбомдор, калем, түстүү карандаштар, учтагычтар ж.б.) менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.7. Окуучунун Лицейдин мүлкүнүн талкалануу же жок кылуусуна алып келген бардык аракеттери үчүн жоопкерчилигин мойнуна алуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.8. Лицейге келтирилген зыянды Лицейдин администрациясы тарабынан жиберилген расмий эсепти /invoice алгандан кийин, 7 календардык күндүн ичинде төлөп берүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.9. Лицейдин администрациясы менен дайыма телефон байланышында болуп туруу:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("- Эки жумада бир жолу Окуучунун саламаттыгы, окуу программалары, сабактан тышкаркы иш-чаралары тууралуу кызыкдар болуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.10. Лицейге балдарды алып келүү жана алып кетүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.11. Лицейдин аймагынан тышкары учурда Окуучунун өмүрү жана коопсуздугу үчүн Лицей жоопкерчилик албайт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.4. Ата-энелердин укуктары:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.1. Лицейдин администрациясынан Келишимдин 2.1.1.-2.1.13.-пункттарында белгиленген шарттарды аткаруусун талап кылуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.2. Окуучунун Лицейде калган мөөнөттөрүн төлөө менен Келишимдин 2.2.9.-2.2.10.-пункттарына ылайык, Келишимди мөөнөтүнөн мурда токтотуу.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Акы төлөө шарттары", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("3.1. Лицейдин администрациясы тарабынан окуу акыларын төлөө мөөнөттөрү төмөндөгүдөй белгиленген:", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1.1. Ата-энелер тарабынан төлөнүүчү акы Келишимдин ажырагыс бөлүгү болгон  эки тарап менен бирге кол коюлган ", ordFont));
                paragraph.add(new Phrase("Графикке жараша", ordBoldFont));
                paragraph.add(new Phrase(" жүргүзүлөт. Акыркы төлөм кийинки жылдын 28-февралынан кечиктирилбестен төлөнүшү зарыл. Окуу акысынын көлөмү окутуунун аралыктан формасына өткөн учурда дагы өзгөрүлбөйт.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1.2. Төлөмдөр ата-энелер, же алардын ордундагы кишилер  тарабынан расмий эсеп/invoice келгенден кийин 3 календардык күн ичинде Лицейдин ", ordFont));
                paragraph.add(new Phrase("банк эсебине сом менен", ordBoldFont));
                paragraph.add(new Phrase(" төлөнөт.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1.3. Ата-энелердин төлөмү ", ordFont));
                paragraph.add(new Phrase(studentInfo.getContractInfo().getContract() + "", ordBoldFont));
                paragraph.add(new Phrase(" АКШ долларын түзөт, төлөмдөр төлөм жүргүзүлүүчү күнгө карата КР УБ курсу менен сом түрүндө төлөнөт.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Форс-мажор", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Келишимге кол койго эки тараптын талаптарын аткарууга мүмкүнчүлүк бербеген : өрт, табият кырсыктары (жер титирөө, сел ж.б.), согуш, бардык түрдөгү аскердик аракеттер, иш таштоо, блокада, Кыргыз Республикасынын мыйзамдарынын өзгөрүшү жана ушуга байланыштуу Келишимге кол койгон тараптарга баш ийбеген окуялара үчүн  эки тарап тең жооп бербейт.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Келишимдин мөөнөтү, өзгөртүү, кошумчалоо жана токтотуу тартиби", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Бул Келишим эи тарап тең кол коюлган күндөн баштап күчүнө кирет.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.2. Бул Келишим 2.2.3 жана 2.4.2.-пункттарына ылайык мөөнөтүнөн мурда токтотулат.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.3. Бул Келишимге киргизилүүчү өзгөртүүлөр жана кошумчалар жазуу түрүндө белгиленип, эки тарап тарабынан  кол коюлганда гана жарамдуу болот.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.4. Бул Келишимге тиешелүү түшүнбөстүктөр сүйлөшүүлөр аркылуу чечилет. Талаштар сүйлөшүүлөр аркылуу чечилбеген учурда, Кыргыз Республикасынын мыйзамдарында белгиленген тартипте чечилет.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.5. Тараптар Келишимдин катышуучу тарабына ылайык экендигин тастыктай алуучу бири-бирине жиберилген иш корреспонденцияларынын факсимилдик, электрондук жана башка байланыш жолу менен жиберилгенде юридикалык күчкө ээ боло тургандыгы тууралуу макулдашышты.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.6. Бул Келишим 2 нускада, кыргыз (орус) тилдеринде түзүлдү жана эки тарап тарабынан кол коюлду. Эки нускасы тең бирдей юридикалык күчкө ээ. Ар бир тарапта Келишимдин өз нускасы бар.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Тараптардын реквизиттери", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.5f, 1f};
                PdfPTable table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                Paragraph text10 = new Paragraph();
                text10.add(new Phrase("Лицей: " + studentInfo.getSchool().getName_kg(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Дареги: " + studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                text10.add(Chunk.NEWLINE);
                String[] banks = studentInfo.getSchool().getBank().split("<br>");
                String[] bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");
                for (int i = 0; i < banks.length; i++) {
                    text10.add(new Phrase("<Банк>: " + banks[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Эсеп: " + bankAccounts[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                }
                text10.add(new Phrase("Тел.: " + studentInfo.getSchool().getPhone(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Лицейдин директору: " + studentInfo.getDirector().getSurname() + " "
                        + studentInfo.getDirector().getName() + " " +
                        (studentInfo.getDirector().getMiddle_name() == null ?
                                "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);

                IndexedContainer relativeCont = null;
                table_info.addCell(text10);
                try {
                    DbRelative dbr = new DbRelative();
                    dbr.connect();
                    relativeCont = dbr.execSQL(myUI, studentInfo.getStudent().getId());
                    dbr.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                Paragraph text11 = new Paragraph();
                Paragraph text18 = new Paragraph();
                Iterator<?> iter = null;
                if (relativeCont != null) {
                    iter = relativeCont.getItemIds().iterator();
                }
                String f_name = "";
                String f_work_place = "";
                String m_name = "";
                String m_work_place = "";
                while (iter != null && iter.hasNext()) {
                    Object obj = iter.next();
                    if ((Integer) obj == 1) {
                        f_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.FullName)).getValue().toString();
                        f_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.WorkPlace)).getValue().toString();
                    }
                    if ((Integer) obj == 2) {
                        m_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.FullName)).getValue().toString();
                        m_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.WorkPlace)).getValue().toString();

                    }
                    if ((Integer) relativeCont.getContainerProperty(obj,
                            Settings.is_main).getValue() == 1) {
                        text18.add(new Phrase("Тел номери: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.Phone)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Жашаган жери: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.Address)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Паспорт маалыматы: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(IndigoMessages.Passport)).getValue().toString(), ordFont));
                    }
                }
                text11.add(new Phrase("Атасынын аты, жөнү: " + f_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Апасынын аты, жөнү: " + m_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Окуучунун аты, жөнү: ", ordFont));
                text11.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                        + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Атасынын иштеген жери: " + f_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Апасынын иштеген жери: " + m_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(text18);
                table_info.addCell(text11);
                table_info.addCell(new Phrase("                             (Мөөр)", ordFont));
                table_info.addCell(new Phrase("Колу:", ordFont));

                document.add(table_info);

                Paragraph text15 = new Paragraph();
                text15.setIndentationLeft(30);
                text15.setIndentationRight(30);
                text15.add(new Phrase("Төлөө графиги", boldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Окуучунун ID: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getLogin(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Окуучунун аты, жөнү: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                        + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Классы: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getClass_name(), ordBoldFont));
                text15.add(new Phrase(". Каттоо Датасы: ", ordFont));
                text15.add(new Phrase(Settings.df.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Төлөмдүн көлөмү: ", ordFont));
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract()) + ""), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Мурунку жылдагы карыз: ", ordFont));
                } else {
                    text15.add(new Phrase("Мурунку жылдагы ашыкча төлөө: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt()) + ""), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Жеңилдик: ", ordFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Тууралоо: ", ordFont));
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Алдын ала төлөм: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + "", ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Калган төлөм: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()) + "", ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                document.add(text15);
                document.add(new Paragraph(10, " "));

                Paragraph text16 = new Paragraph();
                text16.setIndentationLeft(30);
                text16.setIndentationRight(30);
                text16.add(new Phrase("1-таблица", ordBoldFont));
                document.add(text16);
                document.add(new Paragraph(10, " "));
                text16.add(Chunk.NEWLINE);

                float[] TContract_colsWidth = {1f, 4f, 4f, 4f, 4f};
                PdfPTable TContract = new PdfPTable(5);
                TContract.setWidthPercentage(90f);
                TContract.setWidths(TContract_colsWidth);
                TContract.addCell(new Phrase("№", ordBoldFont));
                TContract.addCell(new Phrase("Төлөө күнү", ordBoldFont));
                TContract.addCell(new Phrase("Суммасы", ordBoldFont));
                TContract.addCell(new Phrase("Тастыктаган документ", ordBoldFont));
                TContract.addCell(new Phrase("Колу ", ordBoldFont));
                int n = 1;
                for (Object obj : instPlanCont.getItemIds()) {
                    TContract.addCell(new Phrase(n + "", ordFont));
                    TContract.addCell(new Phrase(((DateField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(IndigoMessages.Date)).getValue()).getValue().toString(), ordFont));
                    TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(IndigoMessages.Amount)).getValue()).getValue(), ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    n += 1;
                }
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("ЖАЛПЫ:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + "", ordBoldFont));
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("", ordFont));

                document.add(TContract);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Ата-эненин колу: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(new Phrase("Директор: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(new Phrase("Башкы эсепчи: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(new Phrase("Мөөр", ordBoldFont));
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
                        document.bottom() - 10, 0);

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }

    }
}
