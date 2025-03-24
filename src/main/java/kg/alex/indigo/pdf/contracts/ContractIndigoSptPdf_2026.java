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
import kg.alex.indigo.i18n.Messages;
import kg.alex.indigo.utils.Decliner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class ContractIndigoSptPdf_2026 {

    static final Logger logger = LogManager.getLogger(ContractIndigoSptPdf_2026.class);
    private final static String FONT_LOCATION = "/home/indigo/TimesNewRomanRegular.ttf";
    private final static String FONT_LOCATION2 = "/home/indigo/TimesNewRomanBold.ttf";
    private final MyVaadinUI myUI;
    private final StudentInfoPdf studentInfo;
    private byte[] b = null;
    private ByteArrayOutputStream buffer = null;
    private Document document = null;

    public ContractIndigoSptPdf_2026(final MyVaadinUI ui, StudentInfoPdf st_info, final IndexedContainer instPlanCont) {
        this.myUI = ui;
        this.studentInfo = st_info;

        StreamResource.StreamSource source1 = () -> {

            buffer = new ByteArrayOutputStream();

            try {

                document = new Document(PageSize.A4, 10, 10, 25, 25);

                PdfWriter writer = PdfWriter.getInstance(document, buffer);
                writer.setPageEvent(new myPageEvent());

                BaseFont baseFont = BaseFont.createFont(FONT_LOCATION, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                BaseFont baseFontBold = BaseFont.createFont(FONT_LOCATION2, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                Font ordFont = new Font(baseFont, 10);
                Font ordFUnderlinedFont = new Font(baseFont, 10, Font.UNDERLINE);
                Font ordBoldFont = new Font(baseFontBold, 10);
                Font ordBoldUnderlinedFont = new Font(baseFontBold, 10, Font.UNDERLINE);
                Font boldUnderlinedFont = new Font(baseFont, 11, Font.UNDERLINE);
                Font boldFont = new Font(baseFontBold, 11);
                Font font_header = new Font(baseFontBold, 11);

                document.open();
                PdfContentByte punder = writer.getDirectContentUnder();
                Decliner dcl = new Decliner();

                document.add(new Paragraph(10, " "));
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
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Кыргыз Республикасынын “Билим берүү жөнүндө” Мыйзамынын 10-беренесине ылайык бекитилген Уставдын негизинде ишмердүүлүгүн жүргүзгөн " + studentInfo.getSchool().getName_ru()
                                         + " билим берүү мекемесинин (мындан ары “Мектеп” деп белгиленет) атынан директору ", ordFont));
                String fullName = studentInfo.getDirector().getSurname() + " " + studentInfo.getDirector().getName();
                if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                    fullName += " " + studentInfo.getDirector().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));

                paragraph.add(new Phrase(" биринчи тараптан жана ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName(), ordBoldFont));
                paragraph.add(new Phrase(" окуучунун ата-энеси (мындан ары“Ата-эне” деп белгиленет) экинчи тараптан ", ordFont));
                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                if (!studentInfo.getStudent().getMiddle_name().isEmpty()) {
                    fullName = fullName + " " + studentInfo.getStudent().getMiddle_name();
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(" окуучунун кызыкчылыгы үчүн Кыргыз Республикасынын “Билим берүү жөнүндө” мыйзамынын 4-беренесине ылайык төмөндөгү келишимди түзүштү:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("1. Келишимдин предмети", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. ", ordFont));
                paragraph.add(new Phrase(studentInfo.getPeriod_kg(), ordBoldFont));
                paragraph.add(new Phrase(" карата мектеп тарабынын мамлекеттик билим берүү стандартынын жана тиешелүү билим берүү программасынын алкагында көрсөткөн билим берүү ишмердүулүгү Келишимдин предмети болуп саналат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. Ата-энелер бул Келишимдин (Ата-энелердин билим берүү кызматтары үчүн төлөм жүргүзүү Шарттары) 3- пунктуна ылайык билим берүү кызматтары үчүн акы төлөөгө милдеттенме алышат.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("2. Тараптардын укук жана милдеттери", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Мектептин милдеттери:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. Жалпы билим берүү мектебинин Уставына, Кыргыз Республикасынын “Билим берүү жөнүндө” Мыйзамынын 10-беренесине ылайык, Келишимдин 1.1.-пунктунда каралган билим берүү ишмердүүлүгүн тиешелүү деңгээлде уюштуруу жана камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. Окуучу мектепке жаңы катталганда бир жолу мектеп формасы берилет, муктаждык келип чыккан кийинки учурда мектеп формасы Ата-эне тарабынан сатып алынат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. Мектептин ичинде жана кирүү-чыруу режимдерин уюштурган түзүмдүк бөлүмдөрү менен кызматташканда окуучунун коопсуздугун камсыз кылуу.", ordFont));
                document.add(paragraph);


                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2 Мектептин укуктары:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. Жылдык чыгашалар сметасына ылайык, ата-энелерден алынуучу төлөмдөрдүн өлчөмүн өз алдынча белгилөө жана өзгөртүү. Мектептин жыл сайын жеңилдик өлчөмдөрүн өзгөртүүгө укугу бар. Окуучунун тартип бузуулары болсо, берилген жеңилдиктер ", ordFont));
                paragraph.add(new Phrase("жокко чыгарылат.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. Ата-эне тарабынан окуу төлөмү убагында төлөнбөгөн учурда (бул Келишимдин 3-пункту) Окуучу класстан класска көчүрүү сынактарына киргизилбейт (КР ББжИМ 10.03.2017-жылынын №281/1 буйругу, токтомунун 70-п.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. Ата-эне тарабынан төлөм графигине ылайык төлөө күнүнөн ", ordFont));
                paragraph.add(new Phrase("15 күн кечиктирилсе жана 2.3.11-пункт аткарылбаса, төлөм өз убагында жүргүзүлбөсө. ", ordBoldFont));
                paragraph.add(new Phrase("Ата-энеге маалымдоо аркылуу Окуучунун бардык окуу жана окуудан тышкаркы иш-чараларга катышуусун, материалдык-техникалык шарттарды (сабактар, китепканалар, ашкана, кружоктор, этюддар ж.б., сынактарга киргизбөө, Эдупейдж тутумуна баа көрсөтпөө) колдонуусун чектөө. Мындан тышкары Окуучуга ", ordFont));
                paragraph.add(new Phrase("Мектеп тарабынан берилген жеңилдиктер Ата-энеге эскертүүсүз жокко чыгарылат.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4. Окуучу кийинки учурларда Мектептен чыгарылат:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("а) Мектептин “Ички тартип эрежелерин” орой, системалуу түрдө бузган учурда; бул тартип бузуулар Мектептин Педагогикалык кеңешинин чечими менен тастыкталган учурда;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) Мектептин кызматкерлерине жана өз курактагы балдарга мыйзамсыз аракеттерди көрсөткөн учурда;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.5. Бул келишим Ата-эненин демилгеси менен бузулган учурда учурдагы чейрек үчүн окуу акысын кармап калуу, мында мурун каралган жеңилдиктер эсепке алынбайт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.6. Бул келишим күтүлбөгөн жагдайлардын айынан бузулган учурда, кайтарыла турган сумма Мектептин сарптаган бардык чыгымдарын эске алуу менен Мектептин мүмкүнчүлүгүнө жараша, бирок кийинки жылдын май айына чейин кайтарылат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. Билим берүү процесси жана мектептин ишмердүүлүгү жөнүндө маалымдоо максатында, окуучуну жана ата-энелерди кабардар кылбастан, фото жана видео материалдарды мектептин интернет баракчасына жана маалымат каражаттарына жайгаштырууга уруксат берилет.", ordFont));
                document.add(paragraph);

                document.newPage();
                document.add(new Paragraph(10, " "));
                spr = new Paragraph();
                spr.add(new Phrase("ДОГОВОР № "
                                   + String.format("%07d", studentInfo.getContractInfo().getContractNumber()), font_header));
                spr.add(Chunk.NEWLINE);

                spr.add(new Phrase("Об оказании платных образовательных услуг", font_header));
                spr.add(Chunk.NEWLINE);

                spr.setAlignment(Element.ALIGN_CENTER);
                document.add(spr);
                document.add(new Paragraph(10, " "));

                table_date = new PdfPTable(2);
                table_date.setWidthPercentage(90f);
                table_date.setWidths(table_date_colsWidth);
                table_date.getDefaultCell().setBorder(0);
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
                table_date.addCell(new Phrase("г. " + studentInfo.getSchool().getCity(), ordBoldFont));
                table_date.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
                table_date.addCell(new Phrase(Settings.dateRu.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                document.add(table_date);
                document.add(new Paragraph(10, " "));

                paragraph = new Paragraph();
                paragraph.setFirstLineIndent(15);
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.setLeading(15);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("Учреждение " + studentInfo.getSchool().getName_ru()
                                         + " именуемая в дальнейшем «Школа», в лице директора ", ordFont));
                fullName = null;
                try {
                    boolean isFeminine = studentInfo.getDirector().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getDirector().getSurname(), isFeminine)
                               + " " + dcl.DeclineNameGenitive(studentInfo.getDirector().getName(), isFeminine, false);
                    if (studentInfo.getDirector().getMiddle_name() != null && !studentInfo.getDirector().getMiddle_name().isEmpty()) {
                        fullName += " " + dcl.DeclinePatronymicGenitive(studentInfo.getDirector().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName + ", ", ordBoldFont));
                paragraph.add(new Phrase("действующего на основании Устава, утвержденного согласно ст.10 Закона Кыргызской Республики «Об образовании», с одной стороны, и ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getFullName() + ", ", ordBoldFont));
                paragraph.add(new Phrase(" являющаяся(щийся) ", ordFont));
                paragraph.add(new Phrase(studentInfo.getRelative().getRelativeDeclarative(), ordBoldFont));
                paragraph.add(new Phrase(" Обучающегося " + studentInfo.getStudent().getClass_name() + " класса ", ordFont));

                fullName = studentInfo.getStudent().getSurname() + " " + studentInfo.getStudent().getName();
                try {
                    boolean isFeminine = studentInfo.getStudent().getGender_id() == 2;
                    fullName = dcl.DeclineSurnameGenitive(studentInfo.getStudent().getSurname(), isFeminine) + " "
                               + dcl.DeclineNameGenitive(studentInfo.getStudent().getName(), isFeminine, false);
                    if (!studentInfo.getStudent().getMiddle_name().isEmpty()) {
                        fullName = fullName + " "
                                   + dcl.DeclinePatronymicGenitive(studentInfo.getStudent().getMiddle_name(),
                                null, isFeminine, false);
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
                paragraph.add(new Phrase(fullName, ordBoldFont));
                paragraph.add(new Phrase(", именуемый(ая) в дальнейшем ", ordFont));
                paragraph.add(new Phrase("«" + studentInfo.getRelative().getRelativeTitle() + "» ", ordBoldFont));
                paragraph.add(new Phrase("с другой стороны, в интересах обучающегося, в соответствии со ст.4 Закона Кыргызской Республики «Об образовании», заключили настоящий Договор о нижеследующем:", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("1. ПРЕДМЕТ ДОГОВОРА", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("1.1. Предметом Договора является образовательная деятельность, предоставляемая школой в рамках государственного образовательного стандарта и программ соответствующего уровня образования на платной основе на период ", ordFont));
                paragraph.add(new Phrase(studentInfo.getPeriod() + ".", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("1.2. Родители обязуются вносить оплату за образовательные услуги согласно пункту 3 (Условия оплаты Родителей за образовательные услуги) данного Договора.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.add(new Phrase("2. ПРАВА И ОБЯЗАННОСТИ СТОРОН", boldFont));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.1. Школа обязуется:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.1. Организовать и обеспечить надлежащее исполнение образовательной деятельности, предусмотренной в пункте 1.1. настоящего Договора, согласно Устава Общеобразовательной Школы, ст.10 Закона Кыргызской Республики «Об образовании».", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.2. При первоначальном поступлении в Школу разово обеспечить Обучающегося школьной формой, которая в последующем, по мере необходимости должна приобретаться за счет Родителя.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.1.3. Обеспечить безопасность Учащегося во взаимодействии со структурными подразделениями Школы, осуществляющими организацию внутриобъектного и пропускного режимов.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.2 Школа имеет право:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.1. Школа имеет право ежегодно изменять процентные ставки льготникам, при этом предоставленные скидки действуют только в течении данного учебного года. Установленные льготы ", ordFont));
                paragraph.add(new Phrase("аннулируются ", ordBoldFont));
                paragraph.add(new Phrase("в случае наличия у Обучающегося дисциплинарного взыскания. А также в случае нарушения графика оплаты Родителем 3 раза, все ранее предоставленные скидки ", ordFont));
                paragraph.add(new Phrase("аннулируются ", ordBoldFont));
                paragraph.add(new Phrase("без предупреждения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.2. При несвоевременной оплате Родителями образовательных услуг (пункт 3 данного Договора) Обучающийся не допускается к переводным экзаменам из класса в класс (Приказ №281/1 от 10.03.2017 г., п.70 Положения МОиН КР).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.3. При несвоевременной оплате ", ordFont));
                paragraph.add(new Phrase("или при задержке оплаты на 15-дней со дня выплаты по графику ", ordBoldFont));
                paragraph.add(new Phrase("Родителем взноса и невыполнении пункта 2.3.11., с извещением Родителя ограничить доступ посещения Обучающегося ко всем формам учебных и не учебных занятий и использования материально-технических условий (", ordFont));
                paragraph.add(new Phrase("занятий, библиотеки, столовой, кружков, этюдов и тд., не допускать на экзамены, ограничивать доступ к оценкам в “Эдупэйдж”", ordFUnderlinedFont));
                paragraph.add(new Phrase("). Помимо этого без предупреждения Родителя ", ordFont));
                paragraph.add(new Phrase("аннулируются предоставленные Школой скидки.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.4. Отчислить Обучающегося из Школы согласно Устава Школы:", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("а) грубого, систематического нарушения «Правил внутреннего распорядка» по решению педагогического совета Школы;", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("б) противозаконных действий по отношению к сверстникам и персоналу Школы.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.5. Удержать сумму родительского взноса за текущую четверть, при расторжении настоящего договора по инициативе Родителя, при этом ранее предусмотренные скидки не учитываются.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.6. При расторжении настоящего договора по непредвиденным обстоятельствам, с учетом всех понесенных расходов школы, ", ordFont));
                paragraph.add(new Phrase("сумма подлежащая к возврату, возвращается по мере возможности Школы, но не позднее мая следующего года.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.2.7. ", ordFont));
                paragraph.add(new Phrase("В целях оповещения образовательного процесса и деятельности школы без уведомления учащегося и родителей размещать фото и видеоматериалы в своих интернет страницах и СМИ.", ordFUnderlinedFont));
                document.add(paragraph);

                document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3. Ата-эненин укуктары:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.1. Мектептин Уставы жана Окуучунун жүрүм-турумун, окуу процессинин катышуучуларынын мамилелерин, күн тартибин ж.б. иретке келтирүүчү локалдык актылар менен таанышуу жана макулдугун берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.2. Окуучуну М-26 жана М-63 формасы боюнча медициналык кароодон өткөрүү жана жаңы окуу жылына карата Окуучунун ден-соолугунун абалы тууралуу корутундуну берүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.3. Мектептин медициналык кызматы менен бирге өнөкөт оорулары бар Окуучуну каттоого тургузуу жана кийинки чогуу аткарылуучу аракеттерди тактоо.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.4. Окуучуну жеке колдонуусу үчүн бардык керектүү канцелярдык буюмдары (дептерлер, альбомдор, калем, түстүү карандаштар, учтагычтар ж.б.) менен камсыз кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.5. Окуучу тарабынан мектептин мүлкүнө келтирилген материалдык зыяндын ордун толуктоо.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.6. Билим берүү процессинин бардык катышуучуларынын катышуусу каралган Мектептин бардык салттуу иш-чараларына көмөк көрсөтүү жана катышуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.7. Мектептин Администрациясы менен балдардын Мектепке алып келүү жана алып кетүү маселелерин чечүү.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.8. Ата-энелер Мектеп тарабынан берилген окуу китептеринин сакталышынан жооптуу. Китептер жоголуп кетсе, же жакшы сакталбаса, толук баасын төлөнөт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.9. Жашаган жеринин дареги жана/же байланыш телефондору алмашкан учурда Мектептин администрациясын 5 жумуш күнүнүн ичинде кабардар кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.10. Мектептин аймагынан тышкары учурда Окуучунун өмүрү жана коопсуздугу үчүн Мектеп жоопкерчилик албайт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.11. Каттоодон өткөн күндөн баштап 10 иш күнү ичинде мектеп талап кылган документтерди өз убагында тапшыруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.12. Мектептин аймагында этикет эрежелерин сактоо жана окуу процессине кандайдыр бир тоскоолдук жаратпоо.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(
                        "2.3.13. Ата-энелер мектептин ыраазычылык, сый-урмат, достук жана жоопкерчилик сыяктуу негизги баалуулуктарын таанышат. " +
                        "Алар бул баалуулуктарды мектепте сактоону колдоого жана мугалимдер менен кызматташууга милдеттенишет. " +
                        "Ошондой эле, бул баалуулуктарды өнүктүрүүгө багытталган окуу иш-чараларын жана программаларын колдоого милдеттенишет. " +
                        "Ата-энелер баалуулуктарга негизделген окутууну натыйжалуу ишке ашырууну камсыздоо үчүн мугалимдер жана мектеп администрациясы менен кызматташууга макул. " +
                        "Ошондой эле, алар ата-энелер чогулуштарына, консультацияларга жана балдарыбыздын билим алуусун колдоого багытталган башка иш-чараларга активдүү катышууга даяр",
                        ordFont
                ));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4. Ата-эненин милдеттери:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.1. Мектептен бул Келишимге ылайык шарттарды аткаруусун талап кылуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.2. Балдарынын мыйзамдуу укук жана кызыкчылыктарын коргоо.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.3. Окутуу процессинин мазмуну, жүрүшү жана билим берүү процессинин жыйынтыгы менен таанышуу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.4. Мектептик же класстык ата-энелер комитетинин курамына шайлануу.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. Ата-энелердин билим берүү кызматтары үчүн акы төлөө шарттары", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase(
                        "3.1. Ата-энелер жыл сайын 1-апрелден 10-майга чейин кийинки окуу жылынын билим берүү кызматтары үчүн 30% алдын ала төлөмдү сөзсүз төлөө менен кийинки окуу жылына карата келишим түзүүгө милдеттүү. " +
                        "Көрсөтүлгөн мөөнөттө келишим түзүлбөгөн учурда Окуучунун орду башка каалоочуларга берилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(
                        "3.2. Окуучунун билим берүү кызматтары үчүн төлөмдү бул келишимдин жана Мектеп менен Ата-эненин ортосунда түзүлгөн төлөөнүн жеке графигине ылайык өз убагында төлөп туруу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(
                        "3.2.1. Ата-энелер тарабынан төлөнүүчү акы Келишимдин ажырагыс бөлүгү болгон эки тарап менен бирге кол коюлган Графикке жараша жүргүзүлөт. " +
                        "Акыркы төлөм кийинки жылдын 31-мартка чейин кечиктирилбестен төлөнүшү зарыл.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.2. Окуучунун билим берүү кызматтары үчүн төлөм (Ата-энелердин төгүмү) ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract()), ordBoldFont));
                paragraph.add(new Phrase(" АКШ долларын түзөт, төлөмдөр төлөм жүргүзүлүүчү күнгө карата КР УБ курсу менен сом түрүндө төлөнөт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.3. Төлөмдөр расмий эсеп/invoice келгенден кийин 3 календардык күн ичинде Мектептин ", ordFont));
                paragraph.add(new Phrase("банк эсебине сом менен", ordBoldFont));
                paragraph.add(new Phrase(" төлөнөт.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.3. Ата-энелердин демилгеси менен бул келишим бузулган учурда билим берүү кызматтары үчүн Мектеп ", ordFont));
                paragraph.add(new Phrase("бир чейрек үчүн суммасын", ordBoldFont));
                paragraph.add(new Phrase(" кармап калат(п 2.2.6).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.4. Эгерде жаңы кабыл алынган окуучу менен келишим 1-сентябрга чейин бузулса, мектеп алдын ала төлөнгөн суммадан ", ordFont));
                paragraph.add(new Phrase("300$ ", ordBoldFont));
                paragraph.add(new Phrase("кармап калуу укугун өзүнө калтырат.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.3. Родители обязаны:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.1. Ознакомиться и утвердить свое согласие с Уставом и локальными актами Школы, регламентирующими правила поведения Обучающегося, взаимоотношений участников образовательного процесса, распорядка дня и т.д.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.2. Провести медицинский осмотр по форме 026-У и 063-У, и предоставить заключение о состоянии здоровья Обучающегося на начало нового учебного года.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.3. Согласовать с медслужбой Школы постановку на учет Обучающегося с хроническим заболеванием и последующие совместные действия.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.4. Обеспечить Обучающегося всеми необходимыми канцелярскими принадлежностями для собственного использования (бумагу формата А4,тетради, альбомы, ручки, цветные карандаши, точилки и т.д.).", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.5. Компенсировать материальный ущерб за причиненный Обучающимся ", ordFont));
                paragraph.add(new Phrase("ущерб имуществу Школы.", ordBoldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.6. Содействовать и участвовать в традиционных мероприятиях Школы, предусматривающих участие всех участников образовательного процесса.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.7. Родители ответственны осуществлять подвоз детей в Школу и обратно.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.8. Родители ответственны за сохранность учебников, выданных Школой. В случае порчи или утери, возмещают полностью.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.9. Известить Администрацию Школы в течение 5 рабочих дней об изменении места жительства и/или контактных телефонов.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.10. Вне территории Школы, ответственность за жизнь и безопасность Учащегося возлагается на родителей.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.11. Своевременно предоставлять требуемые школой документы за 10 рабочих дней со дня регистрации.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.3.12. Соблюдать правила этикета на территории школы и не создавать какие-либо помехи учебному процессу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase(
                        "2.3.13. Родители признают основные ценности школы такие как благодарность, " +
                        "уважение, дружелюбие и ответственность. Обязуются поддерживать их соблюдение в школе " +
                        "и сотрудничать с учителями. А также обязуются поддерживать обучающие мероприятия и " +
                        "программы, направленные на развитие этих ценностей. Родители соглашаются сотрудничать с " +
                        "учителями и администрацией школы для обеспечения эффективной реализации ценностно " +
                        "ориентированного обучения. И готовы участвовать в родительских собраниях, консультациях и " +
                        "других мероприятиях, направленных на поддержку обучения наших детей.",
                        ordFont
                ));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("2.4. Родители имеют право:", boldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.1. Требовать от Администрации Школы выполнения условий согласно настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.2. На защиту законных прав интересов детей.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.3. На ознакомление с содержанием, реализацией и результатами образовательного процесса.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("2.4.4. Избираться в состав Школьного или классного родительского комитета.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("3. УСЛОВИЯ ОПЛАТЫ РОДИТЕЛЕЙ ЗА ОБРАЗОВАТЕЛЬНЫЕ УСЛУГИ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("3.1. Ежегодно с 1 апреля по 10 мая Родители обязаны заключить договор на образовательные услуги на следующий учебный год, ", ordFont));
                paragraph.add(new Phrase("с обязательным внесением 30% предоплаты родительского взноса.", ordBoldUnderlinedFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("В случае не заключения договора в указанные сроки место Обучающегося будет предоставлено другим желающим.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2. Своевременно вносить оплату за образовательные услуги Обучающегося в Школе, ", ordFont));
                paragraph.add(new Phrase("согласно настоящему договору и индивидуального графика оплаты", ordBoldUnderlinedFont));
                paragraph.add(new Phrase(" между Школой и Родителями.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.1. Родительская плата производится согласно Графику, подписанному обеими сторонами, являющегося неотъемлемой частью настоящего договора. При этом последний взнос должен быть внесен не позднее 31- го марта следующего года. Размер родительского взноса не изменяется даже при условии перехода Школы на дистанционную форму обучения.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.2. Общая стоимость платы за образовательные услуги Обучающегося (родительские взносы) составляет ", ordFont));
                paragraph.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getContract()), ordBoldFont));
                paragraph.add(new Phrase(" долларов США, которая производится строго в сомах на день оплаты по курсы НБ КР.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.2.3. Родительская плата производится ", ordFont));
                paragraph.add(new Phrase("в сомах на банковский счет Школы,", ordBoldFont));
                paragraph.add(new Phrase(" не позднее 3 календарных дней с даты, указанной в официальном счете/invoice.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.3. Школа удерживает ", ordFont));
                paragraph.add(new Phrase("сумму за одну четверть", ordBoldFont));
                paragraph.add(new Phrase(" оплаты за образовательные услуги при расторжении настоящего договора по инициативе Родителей. (п. 2.2.6)", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("3.4. Школа оставляет за собой право удержания из суммы предоплаты 300$, в случае расторжения договора вновь прибывшего обучающегося до первого сентября текущего года.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. Форс-мажор", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Келишимге кол койгон эки тараптын талаптарын толук же бир бөлүгүн аткарууга мүмкүнчүлүк бербеген : өрт, табият кырсыктары (жер титирөө, сел ж.б.), согуш, бардык түрдөгү аскердик аракеттер, иш таштоо, блокада, Кыргыз Республикасынын мыйзамдарынын өзгөрүшү жана ушуга байланыштуу Келишимге кол койгон тараптарга баш ийбеген окуялар үчүн эки тарап тең жооп бербейт.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. Келишимдин мөөнөтү, өзгөртүү, кошумчалоо жана токтотуу тартиби", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Бул Келишим эки тарап тең кол коюлган күндөн баштап күчүнө кирет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Бул Келишим 2.2.2. - 2.2.4 - 3.2 пунктка ылайык мөөнөтүнөн мурда токтотулат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. Бул Келишимге киргизилүүчү өзгөртүүлөр жана кошумчалар жазуу түрүндө белгиленип, эки тарап тарабынан кол коюлганда гана күчүнө кирет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. Бул Келишимге тиешелүү түшүнбөстүктөр сүйлөшүүлөр аркылуу чечилет. Түшүнбөстүктөр сүйлөшүүлөр аркылуу чечилбеген учурда, Кыргыз Республикасынын мыйзамдарында белгиленген тартипте сот аркылуу чечилет.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.5. Тараптар Келишимдин катышуучу тарабына ылайык экендигин тастыктай алуучу бири-бирине жиберилген иш корреспонденцияларынын факсимилдик, электрондук жана башка байланыш жолу менен жиберилгенде юридикалык күчкө ээ боло тургандыгы тууралуу макулдашышты.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.6. Бул Келишим 2 нускада, кыргыз (орус) тилинде түзүлдү жана эки тарап тарабынан кол коюлду. Эки нуска бирдей жана бирдей юридикалык күчкө ээ. Ар бир тарапта бул Келишимдин бирден нускасы сакталат.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.7. Бул келишимдин мөөнөтү – ", ordFont));
                paragraph.add(new Phrase("бир окуу жылы.", ordBoldUnderlinedFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("4. ФОРС-МАЖОР", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("4.1. Ни одна из сторон не несет ответственности за полное или частичное неисполнение своих обязательств при возникновении обстоятельств, которые делают полностью или частично невозможным выполнение Договора одной из сторон, а именно: пожар, стихийное природное бедствие (землетрясение, наводнение и др.), война, военные действия всех видов, забастовка, блокада, эпидемия, изменение текущего Законодательства Кыргызской Республики и другие возможные обстоятельства непреодолимой силы, не зависящие от сторон, подписавших Договор.", ordFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("5. СРОК ДЕЙСТВИЯ ДОГОВОРА, ПОРЯДОК ИЗМЕНЕНИЯ, ДОПОЛНЕНИЯ И РАСТОРЖЕНИЯ", boldFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                paragraph.add(new Phrase("5.1. Настоящий Договор вступает в силу с момента его подписания обеими сторонами.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.2. Настоящий Договор может быть расторгнут досрочно, согласно пунктам 2.2.2. - 2.2.4. - 3.2.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.3. Любые дополнения и изменения к настоящему Договору действительны лишь при условии, что они совершены в письменной форме и подписаны обеими сторонами.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.4. Все разногласия по данному Договору решаются сторонами в порядке переговоров. В случае невозможности разрешения спора путем переговоров, они решаются в судебном порядке, установленном законодательством Кыргызской Республики.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.5. Стороны договорились, что деловая корреспонденция и иные документы, касающиеся настоящего Договора, отправленные и полученные посредством факсимильной, электронной связи или иным способом, позволяющим достоверно установить, что документ исходит от стороны по Договору, признаются имеющими юридическую силу.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.6. Настоящий Договор составлен в двух экземплярах на кыргызском, (русском) языке и подписан обеими сторонами. Оба экземпляра идентичны и имеют одинаковую юридическую силу. У каждой из сторон находится один экземпляр настоящего Договора.", ordFont));
                document.add(paragraph);

                paragraph.clear();
                paragraph.add(new Phrase("5.7. Срок действия настоящего договора ", ordFont));
                paragraph.add(new Phrase("один учебный год.", ordBoldUnderlinedFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. Тараптардын реквизиттери", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                float[] table_info_colsWidth = {1.5f, 1f};
                PdfPTable table_info = new PdfPTable(2);
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
                String passport = "";
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
                        passport = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Passport)).getValue().toString();
                    }
                }
                text11.add(new Phrase("Атасынын аты, жөнү: " + f_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Апасынын аты, жөнү: " + m_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Паспорт маалыматы: ", ordFont));
                text11.add(new Phrase(passport, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Атасынын иштеген жери: " + f_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Апасынын иштеген жери: " + m_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(text18);
                table_info.addCell(text11);
                table_info.addCell(new Phrase(" (Мөөр)", ordFont));
                table_info.addCell(new Phrase("Атасынын колу: ________________", ordFont));
                table_info.addCell(new Phrase(" ", ordFont));
                table_info.addCell(new Phrase("Апасынын колу: ________________", ordFont));

                document.add(table_info);
                document.add(new Paragraph(10, " "));

                Paragraph text15 = new Paragraph();
                text15.setIndentationLeft(25);
                text15.setIndentationRight(25);
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
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract())), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Мурунку жылдагы карыз: ", ordFont));
                } else {
                    text15.add(new Phrase("Мурунку жылдагы ашыкча төлөө: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt())), ordBoldFont));
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
                        Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Калган төлөм: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()), ordBoldFont));
                text15.add(new Phrase(" АКШ доллары.", ordFont));
                document.add(text15);
                document.add(new Paragraph(10, " "));

                Paragraph text16 = new Paragraph();
                text16.setIndentationLeft(25);
                text16.setIndentationRight(25);
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
                    TContract.addCell(new Phrase(Settings.df.format(((DateField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Date)).getValue()).getValue()), ordFont));
                    TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Amount)).getValue()).getValue(), ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    n += 1;
                }
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("ЖАЛПЫ:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()), ordBoldFont));
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("", ordFont));

                document.add(TContract);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Ата-эненин колу: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(new Phrase("Мүдүр: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(new Phrase("Мөөр ", ordBoldFont));
                document.add(paragraph);

                document.newPage();
                paragraph.clear();
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.add(new Phrase("6. РЕКВИЗИТЫ СТОРОН", boldFont));
                document.add(paragraph);
                document.add(new Paragraph(10, " "));

                table_info = new PdfPTable(2);
                table_info.getDefaultCell().setBorder(0);
                table_info.setWidthPercentage(90f);
                table_info.setWidths(table_info_colsWidth);
                text10 = new Paragraph();
                text10.add(new Phrase("Школа: " + studentInfo.getSchool().getName_ru(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Адрес: " + studentInfo.getSchool().getAddress(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("ИНН: " + studentInfo.getSchool().getInn(), ordFont));
                text10.add(Chunk.NEWLINE);
                banks = studentInfo.getSchool().getBank().split("<br>");
                bankAccounts = studentInfo.getSchool().getBank_account().split("<br>");
                for (int i = 0; i < banks.length; i++) {
                    text10.add(new Phrase("Банк: " + banks[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                    text10.add(new Phrase("Р/счет: " + bankAccounts[i], ordFont));
                    text10.add(Chunk.NEWLINE);
                }
                text10.add(new Phrase("Тел.: " + studentInfo.getSchool().getPhone(), ordFont));
                text10.add(Chunk.NEWLINE);
                text10.add(new Phrase("Директор Школы: " + studentInfo.getDirector().getSurname() + " "
                                      + studentInfo.getDirector().getName() + " " +
                                      (studentInfo.getDirector().getMiddle_name() == null ?
                                              "" : studentInfo.getDirector().getMiddle_name()), ordFont));
                text10.add(Chunk.NEWLINE);

                relativeCont = null;
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
                text11 = new Paragraph();
                text18 = new Paragraph();
                iter = null;
                if (relativeCont != null) {
                    iter = relativeCont.getItemIds().iterator();
                }
                f_name = "";
                f_work_place = "";
                m_name = "";
                m_work_place = "";
                passport = "";
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
                        text18.add(new Phrase("Контактный тел: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Phone)).getValue().toString(), ordFont));
                        text18.add(Chunk.NEWLINE);
                        text18.add(new Phrase("Адрес места жительства: ", ordFont));
                        text18.add(new Phrase(relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Address)).getValue().toString(), ordFont));
                        passport = relativeCont.getContainerProperty(obj,
                                myUI.getMessage(Messages.Passport)).getValue().toString();
                    }
                }
                text11.add(new Phrase("Ф.И.О. отца: " + f_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Ф.И.О. матери: " + m_name, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Данные паспорта: ", ordFont));
                text11.add(new Phrase(passport, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место работы отца: " + f_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(new Phrase("Место работы матери: " + m_work_place, ordFont));
                text11.add(Chunk.NEWLINE);
                text11.add(text18);
                table_info.addCell(text11);
                table_info.addCell(new Phrase(" (М.П)", ordFont));
                table_info.addCell(new Phrase("Подпись отца: ________________", ordFont));
                table_info.addCell(new Phrase(" ", ordFont));
                table_info.addCell(new Phrase("Подпись матери: ________________", ordFont));

                document.add(table_info);
                document.add(new Paragraph(10, " "));

                text15 = new Paragraph();
                text15.setIndentationLeft(25);
                text15.setIndentationRight(25);
                text15.add(new Phrase("График оплаты", boldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("ID ученика: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getLogin(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Ф.И.О. Ученика: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getSurname() + " "
                                      + studentInfo.getStudent().getName() + " " + studentInfo.getStudent().getMiddle_name(), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Кл.: ", ordFont));
                text15.add(new Phrase(studentInfo.getStudent().getClass_name(), ordBoldFont));
                text15.add(new Phrase(". Дата регистрации: ", ordFont));
                text15.add(new Phrase(Settings.df.format(studentInfo.getContractInfo().getCreationDate()), ordBoldFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("ИТОГО взноса: ", ordFont));
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getContract())), ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                text15.add(Chunk.NEWLINE);
                if (studentInfo.getContractInfo().getDebt() >= 0) {
                    text15.add(new Phrase("Долг с предыдущего года: ", ordFont));
                } else {
                    text15.add(new Phrase("Переплата с предыдущего года: ", ordFont));
                }
                text15.add(new Phrase((Settings.dFormat2.format(studentInfo.getContractInfo().getDebt())), ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Скидка: ", ordFont));
                if (studentInfo.getContractInfo().getDiscountStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getDiscountStr(), ordBoldFont));
                    text15.add(new Phrase(" (вид скидки прописью, %)", ordFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Корректировка: ", ordFont));
                if (studentInfo.getContractInfo().getCorrectionStr() != null) {
                    text15.add(new Phrase(studentInfo.getContractInfo().getCorrectionStr(), ordBoldFont));
                }
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Предоплата: ", ordFont));
                text15.add(new Phrase(studentInfo.getContractInfo().getInitialPayment() == null ? "0.00" :
                        Settings.dFormat2.format(studentInfo.getContractInfo().getInitialPayment()), ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                text15.add(Chunk.NEWLINE);
                text15.add(new Phrase("Остаток: ", ordFont));
                text15.add(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getLeft()), ordBoldFont));
                text15.add(new Phrase(" долларов США.", ordFont));
                document.add(text15);
                document.add(new Paragraph(10, " "));

                text16 = new Paragraph();
                text16.setIndentationLeft(25);
                text16.setIndentationRight(25);
                text16.add(new Phrase("Таблица 1.", ordBoldFont));
                document.add(text16);
                document.add(new Paragraph(10, " "));
                text16.add(Chunk.NEWLINE);

                TContract = new PdfPTable(5);
                TContract.setWidthPercentage(90f);
                TContract.setWidths(TContract_colsWidth);
                TContract.addCell(new Phrase("№", ordBoldFont));
                TContract.addCell(new Phrase("Дата оплаты", ordBoldFont));
                TContract.addCell(new Phrase("Сумма", ordBoldFont));
                TContract.addCell(new Phrase("Потверждающий документ", ordBoldFont));
                TContract.addCell(new Phrase("Подпись ", ordBoldFont));
                n = 1;
                for (Object obj : instPlanCont.getItemIds()) {
                    TContract.addCell(new Phrase(n + "", ordFont));
                    TContract.addCell(new Phrase(Settings.df.format(((DateField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Date)).getValue()).getValue()), ordFont));
                    TContract.addCell(new Phrase(((TextField) instPlanCont.getContainerProperty(obj,
                            myUI.getMessage(Messages.Amount)).getValue()).getValue(), ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    TContract.addCell(new Phrase("", ordFont));
                    n += 1;
                }
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("Итого:", ordBoldFont));
                TContract.addCell(new Phrase(Settings.dFormat2.format(studentInfo.getContractInfo().getNet()), ordBoldFont));
                TContract.addCell(new Phrase("", ordFont));
                TContract.addCell(new Phrase("", ordFont));

                document.add(TContract);

                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(Chunk.NEWLINE);
                paragraph.add(new Phrase("Подпись Родителя: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(new Phrase("Директор: ", ordBoldFont));
                document.add(paragraph);
                document.add(new Paragraph(5, " "));
                paragraph = new Paragraph();
                paragraph.setIndentationLeft(25);
                paragraph.setIndentationRight(25);
                paragraph.add(new Phrase("Печать ", ordBoldFont));
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
