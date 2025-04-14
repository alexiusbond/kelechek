package kg.alex.lomonosov.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.lomonosov.MyVaadinUI;
import kg.alex.lomonosov.Settings;
import kg.alex.lomonosov.dao.DbRelative;
import kg.alex.lomonosov.domain.StudentInfoPdf;
import kg.alex.lomonosov.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ContractSchoolPdf_kg {

    static final Logger logger = LogManager.getLogger(ContractSchoolPdf_kg.class);
    private final static String FONT_LOCATION = "/home/lomonosov/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/lomonosov/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractSchoolPdf_kg(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
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
                paragraph.add(new Phrase("Кыргыз Республикасынын “Билим берүү жөнүндө” Мыйзамынын 10-беренесине ылайык бекитилген Уставдын негизинде ишмердүүлүгүн  жүргүзгөн ", ordFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getName_kg() + "”", ordBoldFont));
                paragraph.add(new Phrase(" билим берүү мекемесинин (мындан ары “Мектеп” деп белгиленет) атынан директору ", ordFont));

                String fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().equals("")) {
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
                paragraph.add(new Phrase(" окуучунун кызыкчылыгы үчүн Кыргыз Республикасынын “Билим берүү жөнүндө” мыйзамынын 4-беренесине ылайык төмөндөгү келишимди түзүштү:", ordFont));
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
                paragraph.add(new Phrase("1.1. ", ordFont));
                paragraph.add(new Phrase(studentInfo.getPeriod_kg(), ordBoldFont));
                paragraph.add(new Phrase(" карата мектеп тарабынын мамлекеттик билим берүү стандартынын жана тиешелүү билим берүү программасынын алкагында көрсөткөн билим берүү ишмердүулүгү Келишимдин предмети болуп саналат.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.2. Ата-энелер бул Келишимдин (Ата-энелердин билим берүү кызматтары үчүн төлөм жүргүзүү Шарттары) 3-пунктуна ылайык билим берүү кызматтары үчүн акы төлөөгө милдеттенме алышат.", ordFont));
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
                paragraph.add(new Phrase("2.1. Мектептин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.1. Жалпы билим берүү мектебинин Уставына, Кыргыз Республикасынын “Билим берүү жөнүндө” Мыйзамынын 10-беренесине ылайык, Келишимдин 1.1.-пунктунда каралган билим берүү ишмердүүлүгүн  тиешелүү деңгээлде уюштуруу жана камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1.2. Окуучу мектепке жаңы катталганда  бир жолу мектеп формасы берилет, муктаждык келип чыккан кийинки учурда мектеп формасы Ата-эне тарабынан сатып алынат.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.2 Мектептин укуктары:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.1. Жылдык чыгашалар сметасына ылайык, ата-энелерден алынуучу төлөмдөрдүн өлчөмүн өз алдынча белгилөө жана өзгөртүү.  Мектептин жыл сайын жеңилдик өлчөмдөрүн өзгөртүүгө укугу бар.  Окуучунун тартип бузуулары болсо, берилген жеңилдиктер", ordFont));
                paragraph.add(new Phrase(" жокко чыгарылат.", ordBoldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.2. Ата-эне тарабынан окуу төлөмү убагында төлөнбөгөн учурда (бул Келишимдин 3-пункту) Окуучу класстан класска көчүрүү сынактарына киргизилбейт (КР ББжИМ 10.03.2017-жылынын №281/1 буйругу, токтомунун 70-п.).", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.3. Ата-эне тарабынан окуу төлөмү убагында төлөнбөгөн учурда, Ата-энеге маалымдоо аркылуу Окуучунун бардык окуу жана окуудан тышкаркы иш-чараларга катышуусун, материалдык-техникалык шарттарды (сабактар, китепканалар, ашкана, кружоктор, этюддар ж.б., сынактарга киргизбөө, Эдупейдж тутумуна баа көрсөтпөө) колдонуусун чектөө. Мындан тышкары Окуучуга Мектеп тарабынан берилген жеңилдиктер (жетишкендиктери үчүн, олимпиадалардын призерлору, кабыл алуу сынагындагы жогорку балл үчүн жеңилдик) Ата-энеге эскертүүсүз жокко чыгарылат.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.4. Окуучу кийинки учурларда Мектептен чыгарылат:", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("А) Мектептин “Ички тартип эрежелерин” орой, системалуу түрдө бузган учурда; бул  тартип бузуулар Мектептин Педагогикалык кеңешинин чечими менен тастыкталган учурда;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Б) Мектептин кызматкерлерине жана өз курактагы балдарга мыйзамсыз аракеттерди көрсөткөн учурда;", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.5. Бул келишим Ата-эненин демилгеси менен бузулган учурда учурдагы чейрек үчүн окуу акысын кармап калуу, мында мурун каралган жеңилдиктер эсепке алынбайт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.6. Бул келишим күтүлбөгөн жагдайлардын айынан бузулган учурда, кайтарыла турган сумма Мектептин сарптаган бардык чыгымдарын эске алуу менен Мектептин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айына чейин кайтарылат.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2.7. Объекттин ичиндеги өткөрмө режимдерин уюштурууну ишке ашыра турган Мектептин башка структуралык бөлүмдөрү менен өз ара аракеттенишүүсүнүн аркасы менен Окуучунун коопсуздугун камсыздайт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.3. Ата-эненин укуктары:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.1. Мектептин Уставы жана Окуучунун жүрүм-турумун, окуу процессинин катышуучуларынын мамилелерин, күн тартибин ж.б. иретке келтирүүчү локалдык актылар менен таанышуу жана макулдугун берүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.2. Окуучуну М-86 формасы боюнча медициналык кароодон өткөрүү жана жаңы окуу жылына карата Окуучунун ден-соолугунун абалы тууралуу корутундуну берүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.3. Мектептин медициналык кызматы менен бирге өнөкөт оорулары бар Окуучуну каттоого тургузуу жана кийинки чогуу аткарылуучу аракеттерди тактоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.4. Окуучуну жеке колдонуусу үчүн бардык керектүү канцелярдык буюмдары (дептерлер, альбомдор, калем, түстүү карандаштар, учтагычтар ж.б.) менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.5. Окуучу тарабынан мектептин мүлкүнө келтирилген материалдык зыяндын ордун толуктоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.6. Билим берүү процессинин бардык катышуучуларынын катышуусу каралган Мектептин бардык салттуу иш-чараларына көмөк көрсөтүү жана катышуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.7. Мектептин Администрациясы менен балдардын Мектепке алып келүү жана алып кетүү маселелерин чечүү.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.8. Ата-энелер Мектеп тарабынан берилген окуу китептеринин сакталышынан жооптуу. Китептер жоголуп кетсе, же жакшы сакталбаса, толук баасын төлөнөт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.9. Жашаган жеринин дареги жана/же байланыш телефондору алмашкан учурда Мектептин администрациясын 5 жумуш күнүнүн ичинде кабардар кылуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3.10. Мектептин аймагынан тышкары учурда Окуучунун өмүрү жана коопсуздугу үчүн Мектеп жоопкерчилик албайт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("2.4. Ата-эненин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.1. Мектептен бул Келишимге ылайык шарттарды аткаруусун талап кылуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.2. Балдарынын мыйзамдуу укук жана кызыкчылыктарын коргоо.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.3. Окутуу процессинин мазмуну, жүрүшү жана билим берүү процессинин жыйынтыгы  менен таанышуу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4.4. Мектептик же класстык ата-энелер комитетинин курамына шайлануу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Ата-энелердин билим берүү кызматтары үчүн акы төлөө шарттары", boldFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.add(new Phrase("3.1. Ата-энелер жыл сайын 1-апрелден 15-майга чейин кийинки окуу жылынын билим берүү кызматтары үчүн 30% алдын ала төлөмдү сөзсүз төлөө менен кийинки окуу жылына карата келишим түзүүгө милдеттүү. Көрсөтүлгөн мөөнөттө келишим түзүлбөгөн учурда Окуучунун орду башка каалоочуларга берилет.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2. Окуучунун билим берүү кызматтары үчүн төлөмдү бул келишимдин жана Мектеп менен Ата-эненин ортосунда түзүлгөн төлөөнүн жеке графигине ылайык өз убагында төлөп туруу.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2.1. Ата-энелер тарабынан төлөнүүчү акы Келишимдин ажырагыс бөлүгү болгон  эки тарап менен бирге кол коюлган Графикке жараша жүргүзүлөт.  Акыркы төлөм кийинки жылдын 28-февралынан кечиктирилбестен төлөнүшү зарыл.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2.2. Окуучунун билим берүү кызматтары  үчүн төлөм (Ата-энелердин төгүмү) ", ordFont));
                paragraph.add(new Phrase(studentInfo.getContractInfo().getContract() + "", ordBoldFont));
                paragraph.add(new Phrase(" АКШ долларын түзөт, төлөмдөр төлөм жүргүзүлүүчү күнгө карата КР УБ курсу менен сом түрүндө төлөнөт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.2.3. Төлөмдөр расмий эсеп/invoice келгенден кийин 3 календардык күн ичинде Мектептин ", ordFont));
                paragraph.add(new Phrase("банк эсебине сом менен", ordBoldFont));
                paragraph.add(new Phrase(" төлөнөт.", ordFont));
                document.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.3. Ата-энелердин демилгеси менен бул келишим бузулган учурда билим берүү кызматтары үчүн Мектеп ", ordFont));
                paragraph.add(new Phrase("бир чейрек үчүн суммасын", ordBoldFont));
                paragraph.add(new Phrase(" кармап калат.", ordFont));
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
                paragraph.add(new Phrase("4.1. Келишимге кол койгон эки тараптын талаптарын толук же бир бөлүгүн аткарууга мүмкүнчүлүк бербеген : өрт, табият кырсыктары (жер титирөө, сел ж.б.), согуш, бардык түрдөгү аскердик аракеттер, иш таштоо, блокада, Кыргыз Республикасынын мыйзамдарынын өзгөрүшү жана ушуга байланыштуу Келишимге кол койгон тараптарга баш ийбеген окуялар үчүн  эки тарап тең жооп бербейт.", ordFont));
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
                paragraph.add(new Phrase("5.1. Бул Келишим эки тарап тең кол коюлган күндөн баштап күчүнө кирет.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.2. Бул Келишим 2.2.2.-пунктка ылайык мөөнөтүнөн мурда токтотулат.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.3. Бул Келишимге киргизилүүчү өзгөртүүлөр жана кошумчалар жазуу түрүндө белгиленип, эки тарап тарабынан  кол коюлганда гана күчүнө кирет.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.4. Бул Келишимге тиешелүү түшүнбөстүктөр сүйлөшүүлөр аркылуу чечилет. Түшүнбөстүктөр сүйлөшүүлөр аркылуу чечилбеген учурда, Кыргыз Республикасынын мыйзамдарында белгиленген тартипте сот аркылуу чечилет.", ordFont));
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
                paragraph.add(new Phrase("5.6. Бул Келишим 2 нускада, орус (кыргыз) тилинде түзүлдү жана эки тарап тарабынан кол коюлду. Эки нуска бирдей жана бирдей юридикалык күчкө ээ. Ар бир тарапта бул Келишимдин бирден нускасы сакталат.", ordFont));
                document.add(paragraph);
                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(30);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.7. Бул келишимдин мөөнөтү – ", ordFont));
                paragraph.add(new Phrase("бир окуу жылы.", ordBoldFont));
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
                text10.add(new Phrase("Мектеп: " + studentInfo.getSchool().getName_kg(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Дареги: " + studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                text10.add(Chunk.NEWLINE);
                String[] banks = studentInfo.getSchool().getBank().split("<br>");
                String[] bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");
                for (int i = 0; i < banks.length; i++) {
                    text10.add(new Phrase("ОКПО: " + banks[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Эсеп: " + bankAccounts[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                }
                text10.add(new Phrase("Тел.: " + studentInfo.getSchool().getPhone(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Мектептин мүдүрү: " + studentInfo.getDirector().getSurname() + " "
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
                                myUI.getMessage(Messages.FullName)).getValue().toString();
                        f_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.WorkPlace)).getValue().toString();
                    }
                    if ((Integer) obj == 2) {
                        m_name = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.FullName)).getValue().toString();
                        m_work_place = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.WorkPlace)).getValue().toString();

                    }
                    if ((Integer) relativeCont.getContainerProperty(obj,
                            Settings.is_main).getValue() == 1) {
                        text18.add(new Phrase("Тел номери: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Phone)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Жашаган жери: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Address)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Паспорт маалыматы: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Passport)).getValue().toString(), ordFont));
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

                document.add(new Paragraph(10, " "));

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
                text15.add(new Phrase(studentInfo.getContractInfo().getInitialPayment() == null ? "0.00" :
                        Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + "", ordBoldFont));
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
                            myUI.getMessage(Messages.Date)).getValue()).getValue().toString(), ordFont));
                    TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Amount)).getValue()).getValue(), ordFont));
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
                paragraph.add(new Phrase("Мүдүр: ", ordBoldFont));
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
