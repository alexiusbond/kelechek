package kg.alex.star.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.star.MyVaadinUI;
import kg.alex.star.domain.StudentInfoPdf;
import kg.alex.star.i18n.Messages;
import kg.alex.star.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

public class ContractPdfKg {

    static final Logger logger = LogManager.getLogger(ContractPdfKg.class);
    private final static String FONT_LOCATION = "/home/sky/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/sky/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractPdfKg(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 30, 40);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 11f);
                Font ordBoldFont = new Font(baseFontBold, 11f);
                Font boldFont = new Font(baseFontBold, 12);
                Font font_header = new Font(baseFontBold, 12);

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("Акы төлөнүүчү билим берүү кызматтарын көрсөтүү жөнүндө ", font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("КЕЛИШИМ №"
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
                table_date.addCell(new Phrase(Settings.dateKg.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                Paragraph paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Уставдын негизинде иш алып барган, мындан ары мектеп деп аталчу ", ordFont));
                paragraph.add(new Phrase(studentInfo.getSchool().getName_kg(), ordBoldFont));
                paragraph.add(new Phrase(" мекемесинин директору ", ordFont));
                String fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                    fullName += " " + studentInfo.getDirector().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" бир тараптан, Кыргыз Республикасынын \"Билим берүү жөнүндө\" Мыйзамына ылайык окуучунун кызыкчылыгында мындан ары \"Ата-эне\" деп аталчу, ", ordFont));
                String studentFullName =
                        studentInfo.getStudent().getSurname() + " " +
                                studentInfo.getStudent().getName();
                paragraph.add(new Phrase(studentFullName, ordBoldFont));
                paragraph.add(new Phrase(" ата-энеси болон ", ordFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName(), ordBoldFont));
                paragraph.add(new Phrase(" экинчи  тараптан төмөнкүдөй келишим түзүштү:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("1. КЕЛИШИМДИН ПРЕДМЕТИ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1 Келишимдин предмети болуп ", ordFont));
                paragraph.add(new Phrase(studentInfo.getYear().getPeriod_kg(), ordBoldFont));
                paragraph.add(new Phrase(" окуучуну толук кандуу тартипте окутуу, тарбиялоо жана жалпы орто билим берүү, анын мамлекеттик билим берүү стандарттарынын жалпы орто мектеп программаларынын алкагында  билим алуусу болуп саналат.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("2. ТАРАПТАРДЫН УКУК ЖАНА МИЛДЕТТЕРИ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Мектептин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. Ушул келишимдин 1.1-пунктунда каралган кызмат көрсөтүүлөрдүн талаптагыдай аткарылышын уюштуруу жана камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. Ушул Келишимдин предмети болуп саналган билим берүү программаларын окуучуларга өздөштүрүү максатында окуучунун мектеп белгилеген тартипте көрсөтүлүүчү жекече билим берүү жардамы менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. Окуу убагында окуучуга окуу китептерин жана окуу II, III баскычынын окуу планын (керектүүсүн астын сызып) предметтери жана программалары боюнча мектептин китеп канасында болгон окуу китептерин жана башка адабияттарды берүү, анын карамагына жабдылган окуу китептерин, компьютердик класстарды, китепкананы берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.4. Объект ичиндеги жана өткөрүү режимдерин уюштурууну жүзөгө ашыруучу мектептин түзүмдүк бөлүмдөрү менен өз ара аракеттенүүдө окуучунун коопсуздугун камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.5. Окуучуларды 2 маал ысык тамак жана полдник менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.6. Мектепке алгачкы тапшырууда окуучуну бир жолу мектеп формасы менен камсыз кылуу, ал кийим зарылчылыгына жараша ата-энесинин эсебинен сатып алууга тийиш.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.7. Жүйөлүү себептер боюнча сабактарды өткөрүп жиберген учурда окуучунун ордун сактоо.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.8. Кыргыз Республикасынын Билим берүү жана илим министрлиги бекиткен мектептин окуу планына ылайык, мектептин уставында белгиленген тартипте окуучунун жарым жылдык жана жылдык аттестациялоосун жүзөгө ашыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.9. Кыргыз Республикасынын Билим берүү жана илим министрлигинин жобосунун негизинде мектептин педагогикалык кеңешинин чечими боюнча белгиленген тартипте окуучуну кийинки класска которуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.10. Окуучу II баскычка ийгиликтүү аяктаган учурда күбөлүк, III баскычын аяктаганда – мамлекеттик үлгүдөгү аттестат берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.11. Окуучунун инсандыгын урматтоо, физикалык жана психологиялык зомбулукка жол бербөө, окуучунун жеке өзгөчөлүктөрүн эске алуу менен анын адеп-ахлактык, дене-бою жана психологиялык ден соолугу, эмоцианалдык бейпилдигин чыңдоо шартын камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.12. Медициналык квалификациясы документ менен тастыкталган мектептин медициналык кызматкери тарбынан окуучунун ден соолугун абалына учурдагы көзөмөлдү камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.13. Окуучуга билим берүү чөйрөсүнүн коопсуздугу жана саламаттык сактоо боюнча талаптар камтылган мектептин ички локалдык актылары жана \"ички тартип эрежелери\" менен тааныштыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.14. Ар бир окуу чейрегинин жыйынтыгы боюнча ата-энеге окуучунун, окуусу жана жүрүм-туруму жөнүндө маалымат берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2. Мектептин укуктары:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. Менюну жеке түзүү,тамактарды алмаштыруу", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. Жылдык чыгымдардын сметасына ылайык ата-энелердин төгүмдөрүн өз алдынча белгилөө жана өзгөртүү. Мектеп акы төлөөдө берилген арзандатуунун өлчөмүн жыл сайын өзгөртүүгө укуктуу, мында берилген арзандатуулар ошол окуу жылынын ичинде гана колдонулат. Белгиленген арзандатуулар окуучуда тартип жазасы болгон учурда жана окуудагы жетишкендиги түшүп кеткенде(3 же андан көп предметтен чейрегине 4 чыкса) эскертүүсүз жокко чыгарылат. Ата-эне тарабынан төлөөнүн графиги 3 жолу бузулган учурда, мектеп тарабынан берилген арзандатуулар эскертүүсүз жокко чыгарылат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. Ата-эне төгүмдү өз убагында төлөбөгөн учурда ата-энеге билдирүү менен окуучунун окуу жана окуудан тышкары сабактарынын бардык формаларына жана материалдык-техникалык шарттарды( сабактар,китепканалар,ашканалар,ийримдер,электрондук журнал) колдонуусуна чектөө коюу", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4.Төмөнкү учурларда окуу акысынын ордун толтурбастан мектептин педагогикалык кенешинин чечими боюнча окуучуну мектептен чыгаруу:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setIndentationLeft(65);
                paragraph.setFirstLineIndent(0);
                paragraph.add(new Phrase("I. Мектептин ички тартип эрежелерин одоно, дайыма бузганда ; (спирт ичимдиктерин,банги каражаттарын,электрондук тамеки түрлөрүн колдонуу,никотин камтыган каражаттарды колдонуу,кумар оюндарын ойноо, мектеп аймагынан уруксатсыз чыгып кетүү);", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("II. Тентуштарына жана мектеп кызматкерлерине карата мыйзамга каршы аракеттер,", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("III. Кыргыз Республикасынын колдонуудагы мыйзамдары бузулганда. ( КР кылмыш-жаза кодексинин 154-беренесинин негизинде жашы жете элек кызы/уулу никеге турууга мажбур болгон учурда ата-энеси жазык жоопкерчилигине тартылат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setIndentationLeft(30);
                paragraph.setFirstLineIndent(15);
                paragraph.add(new Phrase("2.2.5. Окуучу \"Кыргыз Республикасынын мамлекеттик жана мамлекеттик эмес жалпы билим берүү уюмдарында бүтүрүүчүлөрдү мамлекеттик жыйынтыктоочу аттестациялоо жана окуучуларды кийинки класска которуу тартиби жөнүндө Жобонун\" талаптарын аткарбаган учурда, тиешелүү билими жөнүндө мамлекеттик үлгүдөгү документти берүүдөн баш тартуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.6 Жүйөлүү себепсиз 45 күндүк окууну өткөрүп жиберген учурда, окуучу кайрадан окуу курсуна калат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. Эмгекке тарбиялоого компетенттүү мамиле кылуу максатында лицейдин администрациясы окуучуларды мектеп ичинде тазалоо иштерине тартууга укуктуу", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.8. Мектеп ата-энелер менен макулдашуу негизинде ЖРТ,TOEFL,IELTS,SAT ж.б даярдоо курстарын, окуучулардын кызыкчылыктары боюнча ийрим ишин өзүнчө акы менен өткөрүүнү уюштурууга укуктуу", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.9. Өнөкөт оорулар (эпилепция,астма,жүрөк кемтиги,энурез, ж.б) боюнча диспансердик каттоодо турган окуучуларга өзгөчө шарттар берилбейт. Окуучулардын ата-энелери же мыйзамдуу өкүлдөрү окуучунун өнөкөт оорулары жөнүндө мектептин медициналык кызматкерине жазуу жүзүндө эскертүүгө милдеттүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.10. Ушул келишим күтүлбөгөн жагдайлар боюнча бузулганда, мектептин бардык чыгымдарын эске алуу менен кайтарылууга тийиш болгон сумма мектептин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айынан кечиктирилбестен кайтарылып берилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.11. Ата-эненин демилгеси боюнча ушул келишим бузулганда окуучунун учурдагы чейреги үчүн окуу төгүмүнүн суммасын кармап калууга укуктуу, мында контрактта каралган арзандатуулар эсепке алынбайт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.12. Билим берүү процессин жана лицейдин ишин кабарлоо максатында окуучуну жана ата-энелерди кабардар кылбастан фото жана видео материалдарды өздөрүнүн интернет булактарына жана ЖМКга жайгаштырууга укуктуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.13. Мектеп КР мыйзамдарында каралган башка укуктарга ээ болушу мүмкүн.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3. Ата-эненин укуктары:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.1. Ушул келишимдин 1.1-пунктуна ылайык, ушул келишимдин 3.1.1-3.1.3 пунктунда көрсөтулгөн мөөнөттө окуу төлөмдөрун өз убагында төлөө.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.2. Ата-энелер жыл сайын окуу төгумунун 20% алдын ала төлөмун милдеттуу турдө төлөө менен 1-апрелден 15-майга чейин кийинки окуу жылына келишим тузуугө милдеттуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("- Белгиленген мөөнөттө келишим тузулбөгөн учурда, окуу орду башкалар окуучуларга берилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.3. Окуучуну толук медициналык диспансерлештирүүнү жүргүзүү, мектептин администрациясына окуучунун мектепке келээрдин алдында анын ден соолугуунун абалы жөнүндө медициналык адистердин корутундусун берүү. Жазуу жүзүндө макулдук берүү же медициналык кызматтардан баш тартуу:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("а) Муктаждык болгон учурда окуучуга медициналык жардам көрсөтүү;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) Психологдун кызматы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Жеке медициналык тез жардамды чакырууга зарыл болгон учурда чыгымдарды төлөө.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.4. \"Окуучунун анкетасын\" (№___тиркеме), \"Өз алдынча сейилдөөгө уруксат\" (№__тиркеме) жана \"Окуучунун ден соолугуунун абалы жөнүндө туура маалымат берүү жөнүндөгү арызды\" (№___тиркеме) толтуруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.5. Окуучуларга мектептин \"Ички тартип эрежелерин\" аткарууга көмөк көрсөтүү. Окуучу мектептин \"Ички тартип эрежелерин\" бузган учурларда ага таасир этүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.6. Окуучуну өзүнүн окуусуна керектүү бардык кеңсе буюмдары менен камсыз кылуу (дептерлер, альбомдор, калемдер, түстүү карандаштар, курчуткуч ж.б.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.7. Окуучунун мектептин мүлкүн бузууга же жок кылууга алып келген бардык аракеттер үчүн толук материалдык жоопкерчиликти тартуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.8. Лицейдин администрациясынан расмий эсеп/билдирүү алган күндөн тартып 7 календардык күндүн ичинде мектепке келтирилген зяндын ордун толтуруу:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.9. Мектептин администрациясы менен туруктуу телефон байланышын сактап туруу;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(20);
                paragraph.add(new Phrase("-эки жумада бир жолудан кем эмес окуучунун учурдагы ден солугунун абалы, класстан тышкаркы иш-чаралардын билим берүү программалары жөнүндө билүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setFirstLineIndent(15);
                paragraph.add(new Phrase("2.3.10. Балдарды мектепге алып келүүнү жана кайра алып кетүүнү ишке ашыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.11. Мектептин аймагынан тышкары окуучунун өмүрү жана коопсуздугу үчүн лицей жоопкерчилик тартпайт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4. Ата-энелердин милдеттери:", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.1. Лицейдин администрациясынын ушул Келишимдин 2.1.1-2.1.14.-пункттарында баяндалган шарттарды аткаруусун талап кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.2. 2.2.9, 2.2.10-пункттарга ылайык окуучунун лицейде болгон мезгилинде окуу жана жашоо наркынын ордун толтуруу менен келишимди мөөнөтүнөн мурда бузуу.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. АКЫ ТӨЛӨӨ ШАРТТАРЫ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. Мектептин администрациясынын тарабынан акча төлөөнүн төмөндөгү мөөнөттөрү белгиленет:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.1. Ата-эненин акысын төлөө ушул Келишимдин ажырагыс бөлүгү болуп саналган эки тарап тен кол койгон Графикке ылайык жүргүзүлөт. Мында акыны төлөө мөөнөтү жылдын 30-мартынан кечиктирилбестен төлөнүүгө тийиш. Ата-эненин төлөмүнун олчому онлайн окуу формасына өткөн шартта да өзгөрбөйт", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.2. Төлөм мектептин банк эсебине сом менен толонот жана улуттук банктын курсу эсепке алынат, Төлөмдөр келишимде көрсөтүлгөн күндөн тартып 3 календарлык күндөн кечиктирилбестен жургүзүлөт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.1.3. Ата-эненин төлөөлөрдүн жалпы наркы ", ordFont));
                paragraph.add(new Phrase(studentInfo.getContractInfo().getContract() + " АКШ ", ordBoldFont));
                paragraph.add(new Phrase("долларын түзөт, ал КРУБУнун курсу боюнча төлөө күнүгө карата сомго жараша жургүзүлөт.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. ФОРС-МАЖОР", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Тараптардын бири келишимди толук же жарым-жартылай аткарууга мүмкүн болбогон  жагдайлар, атап айтканда: өрт, табигый кырсык (жер титирөө, суу ташкыны  ж.б.), согуш, бардык түрү аскердик аракеттер,иш таштоо, блокада, эпидемия, Кыргыз Республикасынын учурдагы мыйзамдарынын өзгөрүшү жана келишимге кол койгон тараптарга көз каранды болбогон ал жеткис күчтүн башка мүмкүн болгон жагдайлары келип чыкканда тараптардын бири да өз милдеттемелерин толук же жарым-жартылай аткарбагандыгы үчүн жоопкерчилик тартпайт.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. КЕЛИШИМДИН МӨӨНӨТҮ, ӨЗГӨРТҮҮ, КОШУМЧАЛОО ЖАНА ТОКТОТУУ ТАРТИБИ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Ушул келишим ага эки тарап тең кол койгон учурдан тартып күчүнө кирет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Ушул келишим 2.2.3-2.4.2-пунктка ылайык мөөнөтүнөн мурда бузулушу мүмкүн.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. Ушул келишимге кандай гана болбосун толуктоолор жана өзгөртүүлөр алар жазуу жүзүндө жасалган жана эки тарап тең кол койгон шартта гана жарактуу болот.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. Ушул келишим боюнча бардык пикир келишпестиктер тараптар тарабынан сүйлөшүү тартибинде чечилет. Талаш-тартышты сүйлөшүүлөр жолу менен чечүү мүмкүн болбогон учурда алар Кыргыз Республикасынын мыйзамдарында белгиленген тартипте чечилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.5. Тараптар ушул келишимге тиешелүү, факсимилдик, электрондук байланыш аркылуу же документ келишим боюнча тараптан келип чыккандыгын так аныктоо мүмкүндүк берчү башка ыкма менен жөнөтүлгөн жана алынган ишкердик кат-кабарлар жана башка документтер юридикалык күчкө ээ деп тааныларын макулдашты.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.6. Ушул Келишим кыргыз тилинде эки нускада түзүлүп, эки тарап тең кол койгон. Эки нуска бирдей жана бирдей юридикалык күчкө ээ. Тараптардын ар биринде ушул Келишимдин бир нускасы бар.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. ТАРАПТАРДЫН РЕКВИЗИТТЕРИ", boldFont));
                document.add(paragraph);

// Внешняя таблица: 2 блока слева и справа
                PdfPTable outerTable = new PdfPTable(2);
                outerTable.setWidthPercentage(90f);
                outerTable.setSpacingBefore(10f);
                outerTable.setWidths(new float[]{1f, 1f});
                outerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

// -------------------------
// ЛЕВАЯ ТАБЛИЦА
// -------------------------
                PdfPTable leftTable = new PdfPTable(1);
                leftTable.setWidthPercentage(100f);

                leftTable.addCell(createCell(studentInfo.getSchool().getName_kg(), ordFont));
                leftTable.addCell(createCell("Дареги: Ош облусу, Кара-Суу району", ordFont));
                leftTable.addCell(createCell("Кашкар-Кыштак айылы, Таштемирова 32/а", ordFont));
                leftTable.addCell(createCell("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                leftTable.addCell(createCell("Банк: " + studentInfo.getSchool().getBank(), ordFont));
                leftTable.addCell(createCell("Эсеп: " + studentInfo.getSchool().getBank_account(), ordFont));
                leftTable.addCell(createCell("Тел: " + studentInfo.getSchool().getPhone(), ordFont));
                leftTable.addCell(createCell("Мектеп директору:", ordFont));

                String directorFio =
                        studentInfo.getDirector().getSurname() + " " +
                                studentInfo.getDirector().getName() + " " +
                                (studentInfo.getDirector().getMiddle_name() == null
                                        ? ""
                                        : studentInfo.getDirector().getMiddle_name());

                leftTable.addCell(createCell(directorFio.trim(), ordFont));

// -------------------------
// ПРАВАЯ ТАБЛИЦА
// -------------------------
                PdfPTable rightTable = new PdfPTable(1);
                rightTable.setWidthPercentage(100f);

                rightTable.addCell(createCell("Окуучунун аты жону: " + studentFullName, ordFont));
                rightTable.addCell(createCell("Классы: " + studentInfo.getStudent().getClass_name(), ordFont));
                rightTable.addCell(createCell("Ата-энесинин аты жону:", ordFont));
                rightTable.addCell(createCell(studentInfo.getMainRelative().getFullName(), ordFont));
                rightTable.addCell(createCell("Ата-энесинин иштеген жери:", ordFont));
                rightTable.addCell(createCell(studentInfo.getMainRelative().getWorkPlace(), ordFont));
                rightTable.addCell(createCell("Адреси:", ordFont));
                rightTable.addCell(createCell(studentInfo.getMainRelative().getAddress(), ordFont));
                rightTable.addCell(createCell("Паспорт маалыматтары: " + studentInfo.getMainRelative().getPassport(), ordFont));
                rightTable.addCell(createCell("Тел номери: " + studentInfo.getMainRelative().getPhone(), ordFont));

// -------------------------
// ВСТАВЛЯЕМ 2 ВНУТРЕННИЕ ТАБЛИЦЫ ВО ВНЕШНЮЮ
// -------------------------
                PdfPCell leftWrapper = new PdfPCell(leftTable);
                leftWrapper.setBorder(Rectangle.NO_BORDER);
                leftWrapper.setPaddingRight(10f);

                PdfPCell rightWrapper = new PdfPCell(rightTable);
                rightWrapper.setBorder(Rectangle.NO_BORDER);
                rightWrapper.setPaddingLeft(10f);

                outerTable.addCell(leftWrapper);
                outerTable.addCell(rightWrapper);

                document.add(outerTable);
                document.add(new Paragraph(30, " "));

                Paragraph text15 = new Paragraph();
                text15.setIndentationLeft(30);
                text15.setIndentationRight(30);
                text15.add(new Phrase("Төлөмдун суммасы: ", ordFont));
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract())), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Жеңилдик: ", ordFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Биринчи төлөм: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Калган төлөм: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                document.add(text15);
                document.add(new Paragraph(10, " "));

                Paragraph text16 = new Paragraph();
                text16.setIndentationLeft(30);
                text16.setIndentationRight(30);
                text16.add(new Phrase("Төлөө графиги", ordBoldFont));
                document.add(text16);
                text16.add(Chunk.NEWLINE);

                outerTable = new PdfPTable(2);
                outerTable.setWidthPercentage(90f);
                outerTable.setSpacingBefore(10f);
                outerTable.setWidths(new float[]{1f, 1f});
                outerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                float[] TContract_colsWidth = {0.2f, 1f, 1f};
                PdfPTable TContract = new PdfPTable(3);
                TContract.setWidthPercentage(100f);
                TContract.setWidths(TContract_colsWidth);
                TContract.addCell(new Phrase("№", ordBoldFont));
                TContract.addCell(new Phrase("Төлөө күнү", ordBoldFont));
                TContract.addCell(new Phrase("Суммасы", ordBoldFont));
                int n = 1;
                for (Object obj : instPlanCont.getItemIds()) {
                    TContract.addCell(new Phrase(n + "", ordFont));
                    TContract.addCell(new Phrase(((DateField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Date)).getValue()).getValue().toString(), ordFont));
                    TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Amount)).getValue()).getValue(), ordFont));
                    n += 1;
                }
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("БАРДЫГЫ:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()), ordBoldFont));

                outerTable.addCell(TContract);
                outerTable.addCell(new Phrase(" "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Башкы эсепчи: ________________", ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Директор: ________________", ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Мөөр", ordFont));
                outerTable.addCell(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Ата-эненин колу: ________________", ordFont));
                outerTable.addCell(paragraph);
                document.add(outerTable);

                document.newPage();
                spr = new Paragraph();
                spr.add(new Phrase(studentInfo.getYear().getName() + " - ОКУУ ЖЫЛЫ ҮЧҮН", font_header));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("ИЧКИ ТАРТИП ЭРЕЖЕЛЕРИ", font_header));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(30);
                paragraph.setIndentationRight(30);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1. Мектепте иштеген жетекчилерге, мугалимдерге, жардамчы мугалимдерге жана бүт жумушчуларга окуучу тарабынан сылык мамиле болуусу зарыл.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2. Сабакта мугалимге, окуучуларга, сабактын жүрүшүнө тоскоол болбоосу керек.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3. Мектепте окуучулар менен урушууга, бири-бирине ат(кличка) коюуга, өзүнчө топ түзүүгө, сырттагы топторго кошулууга тыюу салынат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("4. Мектептен качкандар, сабакка кечиккендер, эртең мененки линейкага катышпагандар атайын эскертүүдөн соң жазага алынат. Тамакка себепсиз кечиккендер (убакыт бүткөндөн кийин тамак берилбейт).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5. Мектепке окуу-тарбия процессине зыян келтирүүчү көрүнүштөрдү, ар кандай саясий партиялардын жана ар кандай диний агымдардын пропагандаларын камтыган журнал, газета, брошюра, аудио/видеолорду алып келүүгө, таратууга жана колдонууга тыюу салынат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("6. Мектептин ичинде же сыртында окуучулардан зомбулук менен акча талап кылууга же карыз алып, анын мөөнөтүн себепсиз узартууга тыюу салынат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("7. Мектептин формасы болбогондор же кем болгон окуучу эртең менен жалпы жыйында такталат, жана уруксатсыз сабакка киргизилбейт. (чач коюу, боёо, сакал, мурут, сөйкө(эркек балдарга), шакек, браслет, мончок тагууга уруксат берилбейт).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("8. Мектепке зыяндуу буюмдарды (бычак, тамеки, нас, спирт ичимдиги, энергия берүүчү ичимдиктер, эсин жоготтуруучу буюмдар ж.б.у.с) алып келгенге, таратууга, колдонууга тыюу салынат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("9. Мектепке чөнтөк телефон алып келсе, сабактар бүткөнгө чейин класс жетекчиге тапшыруусу керек, уруксатсыз телефон колдонсо, 1-жолу 3 күнгө, 2-жолу 1 жумага, 3-жолу 1 айга алынат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("10. Жума аягында лицейге келген окуучулар жума ичиндегидей эле мектеп эрежелерине баш ийүүгө милдеттүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("11. Мектепкеги эрежелердин сакталуусун жана окуучулардын жүрүм-турумун көзөмөлдөөгө мектепте иштеген ар бир мугалим укуктуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("12. Сабак учурунда мектептен сыртка чыгуу үчүн сөзсүз түрдө класс жетекчисинен жана мудур жардамчыларынан уруксат алынышы керек. Сыртка чыккандан кийинки жоопкерчилик ата-энеге өтөт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("13. Окуучулар мектепке саат 8:00дө келүүсү керек, сабактар күнүмдүк регламентке карата аяктайт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("14. Ооруган окуучулар медайымдын уруксаты жана көзөмөлү менен гана сабакка катышпаса болот.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("15. Ар бир окуучу өзүнө тиешелүү буюмдарына ар дайым сак болуусу зарыл.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("16. Окуучулар классын таза жана тыкан кармоолору керек.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("17. Пайда болгон суроолор же шашылыш билдирүү үчүн класс жетекчилерине кайрылуулары зарыл.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("18. Наркотикалык заттар, тамеки, спирт ичимдиктери, адамдын денесине зыян келтирүүчү ар кандай курал-жарактар жана смартфон телефон ж.б зыяндуу буюмдарды окуучулардын колдонуусунун алдын алуу үчүн класс жетекчилер жана мектеп администрациясы издөө жүргүзүүгө укуктуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("19. Мектептин буюмдарына зыян келтирбөө.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Жогорудагы жана ушул сыяктуу лицейибиздин окуу-тарбия процессине зыян келтирген, эрежелерди бузган окуучуларга төмөнкү жазалар берилет:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Эреже бузган окуучунун түшүнүк каты алынып, мектептин тарбия комиссиясына өткөрүп берилет жана тарбия журналына катталат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Эреже бузуу түрүнө карап тарбия комиссиясынын чечими менен жаза белгиленет. Керек учурда ата-энеси чакырылат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Эреже бузуу түрүнө карата окуучу убактылуу мектептен четтетилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Бузган эрежесине карата окуучу мектептен алган контракт жеңилдигинен ажыратылат. (Комиссиянын чечими мн кийинки жылга кайра жеңилдик алууга болот, эгерде эреже бузбаса жана жакшы окуса).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Эреже бузуу түрүнө карата окуучу убактылуу мектеп ичинде коомдук жумушка тартылат. (тазалык, ашканага жардам берүү ж.б.)", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Тарбия комиссиясына катталган окуучуга чейрек аягында берилүүчү мактоо, ардак баракчалары берилбейт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Тарбия комиссиясына катталган окуучу мектептин атынан сыртта болгон иш-чараларга комиссиянын уруксатысыз катыша албайт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(" Тартип комиссиясынан эскертүү алгандан кийин да эреже бузууну уланткан окуучулар мектептин педкеңешине сунуш кылынып мектептен чыгарылат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Окуучунун аты жөнү: ", ordBoldFont));
                paragraph.add(new Phrase(studentFullName, ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Ата-эненин аты жөнү: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName(), ordFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("(колу)  ____________________ ", ordBoldFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(Chunk.NEWLINE);
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("Күнү: ", ordBoldFont));
                paragraph.add(new Phrase(Settings.dateKg.format(studentInfo.getContractInfo().getCreationDate()), ordFont));
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

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPaddingLeft(8f);
        cell.setPaddingRight(8f);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(4f);
        cell.setBorderWidth(0.8f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
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
