package kg.alex.star;

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
import kg.alex.star.dao.DbCurrencyRate;
import kg.alex.star.dao.DbEmployeeMessage;
import kg.alex.star.dao.DbSchool;
import kg.alex.star.dao.DbUserDetails;
import kg.alex.star.domain.UserDetails;
import kg.alex.star.i18n.Messages;
import kg.alex.star.ui.ViewManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.annotation.WebServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import java.io.InputStream;
import java.net.HttpURLConnection;

@Theme("mytheme")
@SuppressWarnings("serial")
@PreserveOnRefresh
public class MyVaadinUI extends UI {

    static final Logger logger = LogManager.getLogger(MyVaadinUI.class);
    public VaadinRequest r;
    private ResourceBundle i18nBundle;
    private UserDetails user;
    private IndexedContainer schoolCont;
    private double currency_rate;
    private Date nbkr_time;
    private boolean isManualRate;
    private Button messagesBtn;

    public static MyVaadinUI getInstance() {
        return (MyVaadinUI) MyVaadinUI.getCurrent();
    }

    private static final String NBKR_DAILY_URL = "https://www.nbkr.kg/XML/daily.xml";
    private static final String TARGET_ISO_CODE = "USD";
    private static final int NBKR_CACHE_TTL_MINUTES = 60; // как у тебя было

    @Override
    protected void init(VaadinRequest request) {
        r = request;
        i18nBundle = ResourceBundle.getBundle(Messages.class.getName(), new Locale("ru"));

        getPage().setTitle(i18nBundle.getString(Messages.AppTitle));

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

    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        i18nBundle = ResourceBundle.getBundle(Messages.class.getName(),
                getLocale());
    }

    public String getMessage(String key) {
        return i18nBundle.getString(key);
    }

    public void workingDetails(Subject currentUser) {
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
            DbSchool dbs = new DbSchool();
            dbs.connect();
            if (currentUser.isPermitted(Settings.prmShowAllSchools + ":" + Settings.prmMenu) ||
                    getUser().getPosition_id() == 116) {
                setSchoolCont(dbs.execSchoolSel(this, 0, this.getUser().getId()));
            } else {
                setSchoolCont(dbs.execSchoolSel(this, getUser().getSchool().getId(), this.getUser().getId()));
            }
            dbs.close();
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

    public IndexedContainer getSchoolCont() {
        return schoolCont;
    }

    public void setSchoolCont(IndexedContainer schoolCont) {
        this.schoolCont = schoolCont;
    }

    public double getCurrencyRateFromBank() {
        Date now = new Date();
        Calendar c = Calendar.getInstance();

        boolean cacheExpired;
        if (nbkr_time == null || currency_rate == 0.0) {
            cacheExpired = true;
        } else {
            c.setTime(nbkr_time);
            c.add(Calendar.MINUTE, NBKR_CACHE_TTL_MINUTES);
            cacheExpired = c.getTime().before(now);
        }

        if (cacheExpired) {
            HttpURLConnection conn = null;
            double oldRate = currency_rate;

            try {
                URL url = new URL(NBKR_DAILY_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setUseCaches(false);
                conn.setRequestProperty("Connection", "close");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestProperty("Accept", "application/xml,text/xml,*/*");

                int status = conn.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    logger.error("NBKR HTTP error: {}", status);
                } else {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    try {
                        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    } catch (Exception ignored) {
                    }

                    DocumentBuilder db = dbf.newDocumentBuilder();

                    boolean updated = false;

                    try (InputStream is = conn.getInputStream()) {
                        Document doc = db.parse(is);
                        NodeList nl = doc.getElementsByTagName("Currency");

                        for (int i = 0; i < nl.getLength(); i++) {
                            Node node = nl.item(i);
                            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                            Element el = (Element) node;
                            if (!TARGET_ISO_CODE.equals(el.getAttribute("ISOCode"))) continue;

                            Node nominalNode = el.getElementsByTagName("Nominal").item(0);
                            Node valueNode = el.getElementsByTagName("Value").item(0);
                            if (nominalNode == null || valueNode == null) break;

                            String nominalStr = nominalNode.getTextContent().trim();
                            String valueStr = valueNode.getTextContent().trim().replace(',', '.');

                            int nominal = Integer.parseInt(nominalStr);
                            double value = Double.parseDouble(valueStr);

                            currency_rate = (nominal > 0) ? (value / nominal) : value;
                            nbkr_time = now;          // ✅ только после успеха
                            updated = true;
                            break;
                        }
                    }

                    if (!updated) {
                        currency_rate = oldRate; // не нашли USD → откат
                        logger.warn("NBKR: USD not found in XML, keeping previous rate");
                    }
                }
            } catch (Exception e) {
                currency_rate = oldRate; // при любой ошибке — держим старое
                logger.error("Error while fetching currency rate from NBKR", e);
            } finally {
                if (conn != null) conn.disconnect();
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
                db_currency_rate = this.getCurrencyRateFromBank();
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

    @WebServlet(value = {"/*", "/VAADIN/*"}, asyncSupported = true)
    @VaadinServletConfiguration(productionMode = true,
            ui = MyVaadinUI.class, widgetset = "kg.alex.aim.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    public static class LogoutListener implements Button.ClickListener {

        private static final long serialVersionUID = 1L;
        private final MyVaadinUI myUI;

        public LogoutListener(MyVaadinUI myUi) {
            this.myUI = myUi;
        }

        @Override
        public void buttonClick(ClickEvent event) {
            this.myUI.logout();
        }
    }
}
