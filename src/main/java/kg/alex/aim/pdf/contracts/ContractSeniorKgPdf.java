package kg.alex.aim.pdf.contracts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.DateField;
import com.vaadin.ui.TextField;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.Settings;
import kg.alex.aim.domain.StudentInfoPdf;
import kg.alex.aim.i18n.Messages;
import kg.alex.aim.utils.money.WritableSummKgUSD;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ContractSeniorKgPdf {

    static final Logger logger = LogManager.getLogger(ContractSeniorKgPdf.class);
    private final static String FONT_LOCATION = "/home/aim/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/aim/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractSeniorKgPdf(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 20, 20, 30, 40);

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10.5f);
                Font ordItalicFont = new Font(baseFont, 10.5f, Font.ITALIC);
                Font ordBoldFont = new Font(baseFontBold, 10.5f);
                Font ordBolditalicFont = new Font(baseFontBold, 10.5f, Font.ITALIC);
                Font boldUnderlinedFont = new Font(baseFontBold, 10.5f, Font.UNDERLINE);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent(baseFont));

                document.open();

                document.add(new Paragraph(10, " "));

                PdfContentByte punder = writer.getDirectContentUnder();
                // все абзацы, которые должны быть в двух колонках
                List<Element> contractBody = new ArrayList<>();

                Paragraph spr = new Paragraph();
                spr.add(new Phrase("АТА-ЭНЕ ТӨЛӨМҮНҮН ЭСЕБИНЕН ОКУТУУ ", ordBoldFont));
                spr.add(Chunk.NEWLINE);
                spr.add(new Phrase("КЕЛИШИМИ № "
                        + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), ordBoldFont));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                contractBody.add(spr);
                contractBody.add(new Paragraph(10, " "));

                float[] table_date_colsWidth = {1.1f, 1f};
                PdfPTable table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase(studentInfo.getSchool().getCity() + " ш.", ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateKg.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                contractBody.add(table_date);
                contractBody.add(new Paragraph(10, " "));


                Paragraph paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("“" + studentInfo.getSchool().getName_kg() + "” билим берүү мекемеси, уставдын негизинде иш алып барган директору ", ordFont));

                String directorFullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                    directorFullName += " " + studentInfo.getDirector().getMiddle_name();
                }
                paragraph.add(new Phrase(directorFullName, ordBoldFont));
                paragraph.add(new Phrase(" атынан, мындан ары “Мектеп” деп аталат, бир тараптан жана мындан ары “Окуучу” деп аталуучу ", ordFont));
                String fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                if (!studentInfo.getStudent().getMiddle_name().isEmpty()) {
                    fullName = fullName + " " + studentInfo.getStudent().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName + " ", ordBoldFont));
                paragraph.add(new Phrase(" ата-энеси (мыйзамдуу өкүлү) ", ordFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName(), ordBoldFont));
                paragraph.add(new Phrase(", мындан ары “Ата-эне” деп аталат, экинчи тараптан  Кыргыз Республикасынын “Билим берүү жөнүндө” мыйзамынын 50-беренесинин 14-пунктуна ылайык окуучунун кызыкчылыгында төмөнкүлөр жөнүндө ушул ата-эненин төлөмүнүн эсебинен окутуу келишимин, мындан ары “Келишим” түзүштү:", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.add(new Phrase("1. КЕЛИШИМДИН ПРЕДМЕТИ", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.add(new Phrase("1.1. Келишимдин предмети болуп окуучуну " + studentInfo.getYear().getName() + "-окуу жылында ", ordFont));
                paragraph.add(new Phrase(studentInfo.getYear().getPeriod_kg(), ordBoldFont));
                paragraph.add(new Phrase(" чейин Кыргыз Республикасынын мамлекеттик  билим берүүнүн тийиштүү деңгээлинин программалары боюнча окутуу, Келишимдин шарттарына ылайык тамак-аш жана жатакана менен камсыз кылуу жана көрсөтүлгөн кызматтар үчүн акы төлөө (мындан ары - окуу төлөмдөрү) болуп саналат.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.add(new Phrase("2. ТАРАПТАРДЫН УКУКТАРЫ ЖАНА \nМИЛДЕТТЕРИ", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.add(new Phrase("2.1. Мектеп милдеттүү:", boldUnderlinedFont));
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.1. Келишимдеги 1.1. пунктунда каралган кызматтардын талаптагыдай аткарылышын уюштурууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.2. Окуучунун келишимдин предмети болгон окутуу программасын толук өздөштүрүүсү үчүн, ага Мектеп тарабынан белгиленген тартипте методикалык жана консультациялык жардам көрсөтүүгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.3. Окуу мөөнөтү учурунда Окуучуну окуу китептери жана башка адабияттар, китепкана, лаборатория, компьютердик класстар жана жатакана менен камсыз кылууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.4. Окуучуну үч маал тамак (эртең мененки, түшкү жана кечки тамак) менен камсыз кылууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.5. Жүйөлүү себептер менен сабактар калтырылган учурда Окуучунун ордун сактоого;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.6. Окуучунун жеке инсандыгына урмат көрсөтүү, физикалык жана психологиялык зомбулукка жол бербөө, анын жеке өзгөчөлүктөрүн эске алуу менен анын моралдык, дене-бой жана психологиялык ден-соолугун, эмоционалдык бейпилдигин бекемдөө шарттарын камсыз кылуу;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.7. Окуучунун Мектептин ички тартип эрежелерин кынтыксыз аткаруусу шарты менен Мектепте болгон мөөнөтүндө анын өмүрү жана ден соолугу үчүн жооп берүүгө, керектүү учурларда (эреже бузуу, кырсык ж.б.) ал тууралуу Ата–энеге кабар берүүгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.8. Мектеп тарабынан белгиленген көчүрүү жана жыйынтыктоочу аттестация сынактарын жүргүзүүгө жана көчүрүү аттестациясынын жыйынтыгы боюнча педагогикалык кеӊештин чечимине ылайык Окуучуну белгиленген тартипте класстан класска көчүрүүгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.9. Окуучуну окутуунун 2–деӊгээлин аяктаганда күбөлүк, ал эми 3–деӊгээлин аяктаганда белгиленген үлгүдөгү аттестат менен камсыз кылууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.1.10. Медициналык квалификациясы документ түрүндө тастыкталган Мектептин медицина кызматкеринин Окуучунун ден-соолук абалын өз учурунда контролдоосун камсыз кылууга.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2. Мектеп укуктуу:", boldUnderlinedFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.1. Окуучуну окутуу процессинде окуу программаларын жана методикаларын мамлекеттик программаларды эске алуу менен өз алдынча тандоого, иштеп чыгууга жана колдонууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.2. Мамлекеттик стандарттарды эске алуу менен тамак-аштын менюсун өз алдынча түзүүгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.3. Окуучунун окуу, жашоо жана тамактануусу үчүн ата-энелерден алына турган төлөмдүн өлчөмүн жылдык чыгымдардын сметасына жараша өз алдынча өзгөртүүгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.4. Окуу төлөмдөрүнө берилген арзандатуулардын өлчөмүн өз алдынча бекитүүгө жана жыл сайын өзгөртүп турууга.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.5. Жасалган тартип бузууларды ырастоо менен Окуучуну окуу төлөмдөрүн кайра кайтарбастан Мектептен чыгарууга:", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("а) Мектептин ички тартип эрежелерин одоно жана/же системалуу түрдө бузганда;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("б) Башка окуучуларга жана Мектеп кызматкерлерине карата мыйзамсыз аракеттерди жасаганда;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("в) Алкоголдук ичимдиктерди, чылым (тамеки, электрондук тамеки, вэйб ж.б) насвай же наркотикалык заттарды пайдаланганда;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("г) Кыргыз Республикасынын колдонуудагы мыйзамдарын бузган учурда.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Ата эненин Мектаптин билим берүү процессине негизсиз кийлигишүүсү Мектептин ички тартип эрежелерин бузуу катары  эсептелет жана мындай учурлар Келишимди бузууга негиз болот.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.6. Окуучу Келишимдин предмети болуп саналган билим берүү программаларынын жылдык календардык мерчеминде (графигинде) белгиленген мөөнөттө өздөштүрө албаган шартта Келишимди бир тараптуу бузууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.7. Окуучуну башка класска өз алдынча которууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.8. Келишимдин 3.1-пункту системалуу түрдө бузулган учурда Келишимди бир тараптуу тартипте бузууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.9. Билим берүү процессин жана Мектептин ишин маалымдоо максатында Окуучунун жана Ата-энеин уруксатын албастан жана билдирүү бербестен өзүнүн интернет-баракчаларына жана ЖМКга фото жана видеоматериалдарды жайгаштырууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.10.  Окуучу Мектеп сабактарына себепсиз 10 (он) күн (70 саат) катышпагандыгы үчүн тартиптик жаза катары төлөнгөн окуу төлөмдөрүн кайтарып бербестен Окуучуну Мектептен чыгарууга. Мында  альтернатива катары Окуучу кийинки жылы ошол эле классты кайталап Мектепте окуусун уланта алат.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.2.11. Ата–энелерден Окуучу тарабынан Мектептин мүлкүнө тийгизген зыянын төлөп берүүнү талап кылууга.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3. Ата-энелер милдеттүү:", boldUnderlinedFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.1. Келишимдин 3.1. пунктуна ылайык көрсөтүлгөн кызматтар үчүн акы төлөмүн өз убагында төлөөгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.2. Окуучуну колдонуусу үчүн керектүү болгон канцелярдык буюмдар менен камсыз кылууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.3. Окуучуну медициналык көзөмөлдөн өткөрүү менен Мектептин жетекчилигине Окуучунун Мектепке келгенге чейинки ден-соолук абалы тууралуу медициналык адистердин маалым каттарын тапшырууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.4. Окуучунун анкетасын толтурууга жана Окуучунун ден-соолугунун абалы жөнүндө туура маалымат берүүгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.5. Окуучуну Мектептин ички тартип эрежеси менен таныштырууга. Окуучу Мектептин ички тартип эрежесин бузган учурда ага адекваттуу таасир этүүнү аткарууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.6. Мектептин мүлкүнүн бузулушуна же жок кылынышына алып келген Окуучунун бардык аракеттери үчүн толук материалдык жоопкерчиликти тартууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.7. Окуучуну мектепке чейин алып келүүгө жана мектептен алып кетүүгө же Окуучунун өз алдынча келип кете тургандыгына макулдугу боюнча тил кат жазып берүүгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.3.8. Мектеп тарабынан уюштурулган Окуучунун жетишкендигине, жүрүм-турумуна жана ден-соолугуна байланыштуу иш-чараларга (спорт иш чаралары, ата-энелер чогулушу, ж.б.) катышууга.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.4. Ата-энелер укуктуу:", boldUnderlinedFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.4.1. Мектептин администрациясынан Келишимдин 2.1.1-2.1.10. пункттарында көрсөтүлгөн шарттарды аткарууну талап кылууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.4.2. Төмөнкү учурларда төлөнгөн окуу төлөмдөрдүн ордун толтуруу менен (окуучу мектепте болгон мезгилде иш жүзүндө тарткан чыгымдарды эсептен чыгаруу менен) Келишимди мөөнөтүнөн мурда бузууга:", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("а) Кыргыз Республикасынын медициналык адистери же башка чет мамлекеттердин адистери тарабынан Окуучунун ден соолугунун абалы жөнүндө расмий корутунду болгондо Окуучунун андан ары мектепте болуусуна мүмкүндүк бербеген капысынан катуу ооруп калуу абалы пайда болгон учурда;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("б) Мектеп тарабынан Келишимдин 2.1.1-2.1.10.-пунктарында көрсөтүлгөн шарттар бузулган учурда.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.4.3. Мектепти алдын ала кабардар кылуу менен Окуучуну окутуу процессине катышууга жана ошондой эле Окуучу туралуу ар кандай маалыматтарды алып турууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.4.4. Мектеп тарабынан уюштурулган жалпыга ачык болгон иш-чараларга катышууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.4.5. Окуучунун окуудагы жетишкендиктерин жана окууга болгон кызыгуусун арттыруу, анын жеке өзгөчөлүктөрүн эске алуу менен моралдык, дене-бой жана психологиялык ден-соолугун, эмоционалдык бейпилдигин бекемдөө максатында Мектеп менен биргеликте жана тыгыз иш алып барууга;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("2.4.6. Окуучунун кызыкчылыгында түзүлө турган Мектептин ар кандай органдарынын курамына кирүүгө, шайланууга.", ordFont));

                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. ОКУУ ТӨЛӨМДӨРҮ ЖАНА АЛАРДЫ \nТӨЛӨӨ ШАРТТАРЫ", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. Окуу төлөмүн төлөө бөлүктөргө бөлүнүү менен Келишимдин тиркемесине ылайык жүргүзүлөт. Биринчи бөлүгүн (баштапкы төлөм) төлөө Келишимди түзүүдө, калган бөлүгү окуу жылынын кийинки айларына пропорцианалдуу түрдө бөлүү шарты менен төлөнөт. Мында баштапкы төлөм 30% (отуз)  кем болбоого жана акыркы төлөм кийинки жылдын 31-мартынан кечиктирилбестен төлөнүүгө тийиш.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("3.2. Окуу төлөмү колдонуудагы мыйзамдарга ылайык  сом менен Улуттук банктын төлөнгөн күнгө карата курсу боюнча Мектептин банктык эсебине чегерүү/салуу жолу менен жол жоболоштурулат.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("3.3. Мектеп формасы мектепке киргенде 1 эле жолу берилет. Зарыл болгон учурда кошумча мектеп формасы ар бир форма бирдиги үчүн өзүнчө төлөм менен берилет.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("3.4. Окуу төлөмү расмий экзамен үчүн төлөмдү жана башка Мектеп мугалимдери уюштурган экскурсиялар, пикниктер ж.б. коомдук иштердин чыгашаларын камтыбайт.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("3.5. Окуу төлөмдөрүнө арзандатуулар Мектептин финансылык абалына жана педагогикалык кеӊештин чечимине ылайык Окуучунун окуудагы, олимпиада ж.б. жарыштардагы жетишкендиктери жана социалдык абалдарына жана ошондой эле Мектептин өнөктөштөр менен болгон келишимдерине  жараша аныкталат.  Ошону менен бирге арзандатуулар Мектептин башкы дирекциясы (уюштуруучусу) тарабынан берилиши мүмкүн.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("3.6. Ата-эне тарабынан Келишимдин 3.1-пунктунда каралган окуу төлөмдөрү белгиленген мөөнөттө төлөнбөгөн учурда, Мектеп Ата-энеге жазуу жүзүндө (же электрондук байланыш каражаттары аркылуу) маалымдоо менен төмөнкү чараларды колдонууга укуктуу:", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("– Окуучунун окуудан тышкаркы иш-чараларга (кружоктор, экскурсиялар, мелдештер, кошумча сабактар ж.б.) катышуусун убактылуу чектөөгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("– Мектептин кошумча материалдык-техникалык ресурстарын (ашкана, кошумча кызматтар, клубдар ж.б.) пайдаланууну чектөөгө;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("– Окуучуну олимпиадаларга, сынактарга жана башка кошумча иш-чараларга катыштырбоого;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("– Электрондук маалыматтык тутумдардагы (мисалы, EduPage ж.б.) маалыматтарга жеткиликтүүлүктү убактылуу чектөөгө.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Окуу төлөмдөрү 15 (он беш) календардык күндөн ашык мөөнөткө кечиктирилген учурда, Мектеп тарабынан мурда берилген бардык жеңилдиктер Ата-энеге кошумча эскертүүсүз жокко чыгарылат. Ал эми окуу төлөмү 30 (отуз) календардык күндөн ашык кечиккен учурда Мектеп Келишимди бир тараптуу бузууга укуктуу.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("3.7. Ата-эне окуучуну кечиримдүү себептерден  улам Мектептен алып чыгып кете турган болсо алдын ала төлөнгөн төлөмдөр кетирилген чыгашалар алынган соң калган сумманын 50% окуу жылынын аягына чейин кайтарылып берилет.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. ФОРС-МАЖОР", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Келишимди толугу менен же анын бир бөлүгүн аткаруу мүмкүн болбой турган, тактап айтканда өрт, табийгый кырсык (сел, жер титирөө ж.б.) согуш, аскердик абалдын бардык түрү, көтөрүлүш, тосуу жана башка Тараптарга байланыштуу болбогон жагдайлар пайда болгондо эч бир тарап Келишимди толугу менен же анын бир бөлүгүн аткарбагандыгы үчүн жоопкерчилик тартпайт.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("4.2. Мындай учурларда Мектеп окуу процессин аралыктан (дистанциондук форматта) уюштурууга укуктуу жана бул кызмат көрсөтүүнүн талаптагыдай аткарылышы болуп эсептелет.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. БАШКА ШАРТТАР", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Келишим Тараптар кол койгон күндөн тартып күчүнө кирет жана окуу жылынын аягына чейин күчүндө болот. Келишимдин мөөнөтүнүн аякташы төлөнбөгөн окуу төлөмдөрдү төлөбөй коюуга негиз болбойт.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.2. Келишимге карата кандай болбосун толуктоолор жана өзгөртүүлөр алар жазуу жүзүндө жасалган жана Тараптар кол койгон шартта гана жарактуу болот.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.3. Келишимдин тиркемеси болгон төлөм төлөө графиги ата-эне тарабынан 3 (үч) жолу бузулган учурда Мектеп Келишимди эскертүүсүз токтото алат.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.4. Келишим календардык жылдын 30-июнунан кийин бузулган учурда, төлөм суммасынын 5% кармалууга тийиш.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.4. Кийинки жылга академиялык карызы жок жана окуу мерчемин толук көлөмдө аткарган окуучулар которулат (окуган класс үчүн окуу мерчемин бардык окуу дисциплиналары боюнча чейрек белгилери бар 100 баллдын ичинен 40 баллдан төмөн эмес).", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.5. Келишим боюнча бардык пикир келишпестиктер Тараптар тарабынан сүйлөшүү тартибинде чечилет. Талаш-тартышты сүйлөшүүлөр жолу менен чечүү мүмкүн болбогон учурда, алар Кыргыз Республикасынын мыйзамдарында белгиленген тартипте чечилет.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.6. Келишимди түзүүдө ата-энелер төмөнкү документтерди көрсөтүшү керек:", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("- ата-энесинин же мыйзамдуу өкүлүнүн ким экендигин тастыктаган документтин түп нускасы;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("- баланын туулгандыгы тууралуу күбөлүктүн түп нускасы;", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("- Мектеп администрациясынын талабы боюнча башка документтер.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.7. Ата-эне (мыйзамдуу өкүл) билим берүү процессине түздөн-түз кийлигишпөөгө жана сабак өтүп жаткан учурда окуучулардын жана мугалимдердин тынчын албоого милдеттүү.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Окуу процессине байланыштуу бардык суроолор, даттануулар жана сунуштар Мектеп тарабынан белгиленген тартипте (сабактан тышкаркы убакта, алдын ала жазылуу же администрация аркылуу) каралат.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Сабак учурунда мугалимдерге, окуучуларга же башка кызматкерлерге кайрылуу, териштирүү жүргүзүү (разбор кылуу) жана окуу процессин үзгүлтүккө учуратуучу аракеттерге жол берилбейт.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.8. Ата-эне келишимди келишимдин 2.4.2. пунктунан башка учурларда бир тараптуу бузган учурларда берилген жеңилдиктер жокко чыгарылуу менен эсептешүүлөр жүргүзүлөт.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.9. Ата-эне Окуучунун кийинки окуу жылында Мектепте окуусун улантуу ниети болгон учурда, жыл сайын 1-апрелден 15-майга чейинки мөөнөттө кийинки окуу жылы үчүн билим берүү кызматтарын көрсөтүү боюнча жаңы келишим түзүүгө жана алдын ала төлөмдү (келишим түзүү төлөмүн) төлөөгө милдеттүү.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Көрсөтүлгөн мөөнөттө келишим түзүлбөгөн жана/же алдын ала төлөм төлөнбөгөн учурда, Мектеп Окуучунун ордун сактап калбоого жана аны башка талапкерлерге берүүгө укуктуу.", ordFont));

                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("5.10. Келишим эки нускада мамлекеттик тилде түзүлдү жана Тараптар тарабынан кол тамгалар коюлду. Эки нуска тең бирдей жана бирдей юридикалык күчкө ээ. Тараптардын ар биринде ушул келишимдин бирден нускасы болот.", ordFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Мен келишимдин шарттары менен тааныштым жана аларга макулмун  ________________________ ", ordBolditalicFont));
                contractBody.add(paragraph);
                addTwoColumnText(document, writer, contractBody);
                contractBody.clear();

                document.newPage();
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. ТАРАПТАРДЫН РЕКВИЗИТТЕРИ", ordBoldFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("\n«" + studentInfo.getSchool().getName_kg() + "»   билим берүү мекемеси.\n" +
                        "Дарек: " + studentInfo.getSchool().getAddress() + ".\n" +
                        "ИНН: " + studentInfo.getSchool().getInn() + "\n" +
                        "ОКПО: " + studentInfo.getSchool().getOkpo() + "\n" +
                        "Банк: " + studentInfo.getSchool().getBank() + "\n" +
                        "Т/эсеби (мультив.эсеп): " + studentInfo.getSchool().getBank_account() + "\n" +
                        "БИК: " + studentInfo.getSchool().getBik() + "\n" +
                        "Тел. " + studentInfo.getSchool().getPhone() + "\n" +
                        "\n", ordFont));
                paragraph.add(new Phrase(
                        "Мектептин мүдүрү: ", ordBoldFont));
                paragraph.add(new Phrase(directorFullName.replaceFirst("\\s+(?=[^\\s]+$)", "\n") + "   _______________________\n\n", ordFont));
                paragraph.add(new Phrase("                                            (М.О.)", ordFont));
                contractBody.add(paragraph);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("\n\nАта-эненин аты-жөнү: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getFullName() + "\n", ordFont));
                paragraph.add(new Phrase("Жашаган дареги: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getAddress() + "\n", ordFont));
                paragraph.add(new Phrase("Паспорт  ИНН: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getPassport() + "   " +
                        studentInfo.getMainRelative().getInn() + "\n", ordFont));
                paragraph.add(new Phrase("Тел.: ", ordBoldFont));
                paragraph.add(new Phrase(studentInfo.getMainRelative().getPhone() + "\n\n", ordFont));
                paragraph.add(new Phrase("Кол тамгасы:   __________________", ordBoldFont));
                contractBody.add(paragraph);
                addTwoColumnText(document, writer, contractBody);
                contractBody.clear();

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("ТӨЛӨМ ТӨЛӨӨ ГРАФИГИ:", ordBoldFont));
                contractBody.add(paragraph);

                WritableSummKgUSD convertToLetters = new WritableSummKgUSD();
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(15);
                paragraph.setIndentationRight(10);
                paragraph.setLeading(13);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Жалпы төлөм: ", ordBolditalicFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract())
                        + "$ (" + convertToLetters.numberToString(studentInfo.getContractInfo().getContract()).trim()
                        + ")\n", ordFont));
                paragraph.add(new Phrase("Жеңилдик: ", ordBolditalicFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    paragraph.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordFont));
                }
                paragraph.add(new Phrase("\nКорректировкалоо: ", ordBolditalicFont));
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    paragraph.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordFont));
                }
                paragraph.add(new Phrase("\n* Төлөмдөр төлөм графигине ылайык төлөнбөгөн учурда берилген жеңилдик жокко чыгарылат!", ordItalicFont));
                contractBody.add(paragraph);
                contractBody.add(new Paragraph(10, " "));

                float[] TContract_colsWidth = {2f, 1f};
                PdfPTable TContract = new PdfPTable(2);
                TContract.setWidthPercentage(90f);
                TContract.setWidths(TContract_colsWidth);
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                TContract.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
                TContract.getDefaultCell().setPaddingTop(5f);
                TContract.getDefaultCell().setPaddingBottom(5f);
                TContract.getDefaultCell().setPaddingRight(5f);
                TContract.addCell(new Phrase("Баштапкы төлөм \n(келишим түзүү төлөмү)", ordFont));
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()) + " " + Settings.USD, ordFont));
                for (Object obj : instPlanCont.getItemIds()) {
                    if ((Integer) instPlanCont.getContainerProperty(obj, Settings.status_id).getValue() == 1) {
                        TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                        TContract.addCell(new Phrase(
                                Settings.monthKg.format(((DateField) instPlanCont.getContainerProperty(obj,
                                        myUI.getMessage(Messages.Date)).getValue()).getValue()) + " айынын төлөмү", ordFont));
                        TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                        TContract.addCell(new Phrase(Settings.dFormat2.format(((TextField) instPlanCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Amount)).getValue()).getPropertyDataSource().getValue()) + " " + Settings.USD, ordFont));
                    }
                }
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
                TContract.addCell(new Phrase("Жалпы төлөм:", ordBoldFont));
                TContract.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()) + " " + Settings.USD, ordBoldFont));
                contractBody.add(TContract);

                addToSecondColumn(document, writer, contractBody);
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
        Font f_font;

        public myPageEvent(BaseFont baseFont) {
            this.f_font = new Font(baseFont, 10, Font.NORMAL, new BaseColor(64, 64, 64));
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            try {
                float y = document.bottom() - 15;

                // Левая часть
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_LEFT,
                        new Phrase("       Мектеп ____________________", f_font),
                        document.left() + 20f,
                        y,
                        0
                );

                // Средняя/правая часть
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_LEFT,
                        new Phrase("Ата эне ___________________", f_font),
                        document.left() + 320f,
                        y,
                        0
                );

                // Номер страницы справа
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_RIGHT,
                        new Phrase(String.valueOf(writer.getPageNumber()), f_font),
                        document.right(),
                        y,
                        0
                );

            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }

    }

    private static void addTwoColumnText(Document document,
                                         PdfWriter writer,
                                         List<Element> elements) throws DocumentException {

        ColumnText ct = new ColumnText(writer.getDirectContent());

        float gutter = 5f; // расстояние между колонками

        float left = document.left();
        float right = document.right();
        float top = document.top(); // немного ниже верхней границы
        float bottom = document.bottom();

        float fullWidth = right - left;
        float columnWidth = (fullWidth - gutter) / 2f;

        float[][] columns = new float[][]{
                {left, bottom, left + columnWidth, top},
                {left + columnWidth + gutter, bottom, right, top}
        };

        for (Element e : elements) {
            ct.addElement(e);
        }

        int column = 0;

        while (true) {
            ct.setSimpleColumn(
                    columns[column][0],
                    columns[column][1],
                    columns[column][2],
                    columns[column][3]
            );

            int status = ct.go();

            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                break; // текста больше нет
            }

            column++;
            if (column >= columns.length) {
                column = 0;
                document.newPage();
            }
        }
    }

    private static void addToSecondColumn(Document document,
                                          PdfWriter writer,
                                          List<Element> elements) throws DocumentException {

        PdfContentByte cb = writer.getDirectContent();

        float gutter = 5f; // расстояние между колонками

        float left = document.left();
        float right = document.right();
        float top = document.top(); // чуть ниже верхней границы
        float bottom = document.bottom();

        float fullWidth = right - left;
        float columnWidth = (fullWidth - gutter) / 2f;

        // левая/правая границы второй колонки
        float colLeft = left + columnWidth + gutter;

        ColumnText ct = new ColumnText(cb);

        // изначальный прямоугольник второй колонки на текущей странице
        ct.setSimpleColumn(colLeft, bottom, right, top);

        for (Element e : elements) {
            ct.addElement(e);
        }

        while (true) {
            int status = ct.go();

            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                break; // всё отрисовано
            }

            // текст не влез — новая страница и снова рисуем во второй колонке
            document.newPage();
            ct.setSimpleColumn(colLeft, bottom, right, top);
        }
    }
}
