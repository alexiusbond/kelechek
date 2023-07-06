package kg.alex.indigo;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.dao.DbCurrencyRate;
import kg.alex.indigo.dao.DbEmployeeMessage;
import kg.alex.indigo.dao.DbSchool;
import kg.alex.indigo.dao.DbUserDetails;
import kg.alex.indigo.domain.UserDetails;
import kg.alex.indigo.i18n.IndigoMessages;
import kg.alex.indigo.ui.ViewManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.annotation.WebServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

@Theme("mytheme")
@SuppressWarnings("serial")
@PreserveOnRefresh
public class MyVaadinUI extends UI {

    static final Logger logger = LogManager.getLogger(MyVaadinUI.class);
    private ResourceBundle i18nBundle;
    private UserDetails user;
    public VaadinRequest r;
    private IndexedContainer schoolCont;
    private double currency_rate;
    private Date nbkr_time = new Date();
    private boolean isManualRate;
    private Button messagesBtn;

    @WebServlet(value = {"/*", "/VAADIN/*"}, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true,
            ui = MyVaadinUI.class, widgetset = "kg.alex.indigo.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        r = request;
        i18nBundle = ResourceBundle.getBundle(IndigoMessages.class.getName(), new Locale("ru"));

        getPage().setTitle(i18nBundle.getString(IndigoMessages.AppTitle));

        ViewManager viewManager = new ViewManager(this);
        viewManager.switchScreen(LoginScreen.class.getName(), new LoginScreen(this));

    }

    public void logout() {

        Subject currentUser = SecurityUtils.getSubject();

        if (currentUser.isAuthenticated()) {
            currentUser.logout();
        }
        getUI().getSession().close();

        getUI().getPage().setLocation("");

    }

    public void login(String username, String password) {
        UsernamePasswordToken token;

        token = new UsernamePasswordToken(username, password);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.login(token);
    }

    public static MyVaadinUI getInstance() {
        return (MyVaadinUI) MyVaadinUI.getCurrent();
    }

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        i18nBundle = ResourceBundle.getBundle(IndigoMessages.class.getName(),
                getLocale());
    }

    public String getMessage(String key) {
        return i18nBundle.getString(key);
    }

    public void workingDetails(Subject currentUser) {
        try {
            DbSchool dbs = new DbSchool();
            dbs.connect();
            setSchoolCont(dbs.execSchoolSel(this, 0));
            dbs.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        try {
            DbUserDetails dbu = new DbUserDetails();
            dbu.connect();
            setUser(dbu.execSQLUserInfo(currentUser.getPrincipal().toString()));
            dbu.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        try {
            DbEmployeeMessage dbu = new DbEmployeeMessage();
            dbu.connect();
            if (SecurityUtils.getSubject().isPermitted(Settings.cnMessagesView + ":" + Settings.actReadMessages)) {
                getUser().setUnreadMessages(dbu.isUnread(getUser().getId(), getUser().getSchool().getId()));
            } else {
                getUser().setUnreadMessages(dbu.isUnread(getUser().getId(), 0));
            }
            dbu.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    /**
     * @return the user
     */
    public UserDetails getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(UserDetails user) {
        this.user = user;
    }

    public static class LogoutListener implements Button.ClickListener {

        private static final long serialVersionUID = 1L;
        private MyVaadinUI myUI;

        public LogoutListener(MyVaadinUI myUi) {
            this.myUI = myUi;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            this.myUI.logout();
        }
    }

    public IndexedContainer getSchoolCont() {
        return schoolCont;
    }

    public void setSchoolCont(IndexedContainer schoolCont) {
        this.schoolCont = schoolCont;
    }

    public double getCurrencyRateFromBank() {
        Calendar c = Calendar.getInstance();
        c.setTime(nbkr_time);
        c.add(Calendar.MINUTE, 3000);
        if (currency_rate == 0.00 || c.getTime().before(new Date())) {
            nbkr_time = new Date();
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator(',');
            DecimalFormat format = new DecimalFormat("##.####");
            format.setDecimalFormatSymbols(symbols);
            try {
                URL url = new URL("https://www.nbkr.kg/XML/daily.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(url.openStream());
                NodeList nl = doc.getElementsByTagName("Currency");
                for (int temp = 0; temp < nl.getLength(); temp++) {
                    Node nNode = nl.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (eElement.getAttribute("ISOCode").equals("USD")) {
                            currency_rate = format.parse(eElement.getElementsByTagName("Value")
                                    .item(0).getTextContent()).doubleValue();
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        return Double.parseDouble(Settings.dFormat4.format(currency_rate));
    }

    public double getCurrencyRateFromOptima() {
        Calendar c = Calendar.getInstance();
        c.setTime(nbkr_time);
        c.add(Calendar.MINUTE, 3000);
        if (currency_rate == 0.00 || c.getTime().before(new Date())) {
            nbkr_time = new Date();
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            DecimalFormat format = new DecimalFormat("##.####");
            format.setDecimalFormatSymbols(symbols);
            try {
                org.jsoup.nodes.Document doc = Jsoup.connect("https://www.optimabank.kg/en/").get();
                Elements elems = doc.getElementsByClass("mod_rates_table");
                Elements els = elems.first().getElementsByClass("up");
                currency_rate = format.parse(els.get(1).text()).doubleValue();
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        return Double.parseDouble(Settings.dFormat4.format(currency_rate));
    }

    public double getDb_currency_rate() {
        double db_currency_rate = 0.0;
        try {
            DbCurrencyRate dbCon = new DbCurrencyRate();
            dbCon.connect();
            db_currency_rate = dbCon.execSQL_last_rate(getUser().getSchool().getId());
            if (db_currency_rate == 0.0) {
                isManualRate = false;
                db_currency_rate = this.getCurrencyRateFromOptima();
            } else {
                isManualRate = true;
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        return db_currency_rate;
    }

    public boolean isManualRate() {
        return isManualRate;
    }

    public void repaintMessagesButton() {
        try {
            DbEmployeeMessage dbCon = new DbEmployeeMessage();
            dbCon.connect();
            if (SecurityUtils.getSubject().isPermitted(Settings.cnMessagesView + ":" + Settings.actReadMessages)) {
                getUser().setUnreadMessages(dbCon.isUnread(getUser().getId(), getUser().getSchool().getId()));
            } else {
                getUser().setUnreadMessages(dbCon.isUnread(getUser().getId(), 0));
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        if (getUser().isUnreadMessages()) {
            getMessagesBtn().setStyleName("unread");
            getMessagesBtn().setIcon(FontAwesome.ENVELOPE);
        } else {
            getMessagesBtn().setStyleName(ValoTheme.BUTTON_FRIENDLY);
            getMessagesBtn().setIcon(FontAwesome.ENVELOPE_OPEN);
        }
        getMessagesBtn().addStyleName(ValoTheme.BUTTON_SMALL);
    }

    public Button getMessagesBtn() {
        return messagesBtn;
    }

    public void setMessagesBtn(Button messagesBtn) {
        this.messagesBtn = messagesBtn;
    }
}
