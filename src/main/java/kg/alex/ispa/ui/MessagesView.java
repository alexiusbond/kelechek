package kg.alex.ispa.ui;

import com.vaadin.ui.CustomTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.utils.Settings;
import kg.alex.ispa.dao.DbEmployeeMessage;
import kg.alex.ispa.i18n.Messages;
import kg.alex.ispa.utils.MyFilterDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.tepi.filtertable.FilterTable;

public class MessagesView extends VerticalLayout {

    static final Logger logger = LogManager.getLogger(MessagesView.class);
    private final MyVaadinUI myUI;
    private final String[] NATURAL_COL_ORDER;
    private final Subject currentUser = SecurityUtils.getSubject();

    public MessagesView(MyVaadinUI myUI) {
        this.myUI = myUI;
        setSpacing(true);
        setMargin(true);
        setSizeFull();
        NATURAL_COL_ORDER = new String[]{myUI.getMessage(Messages.Date),
                myUI.getMessage(Messages.OrderNumber), myUI.getMessage(Messages.Student),
                myUI.getMessage(Messages.Discount), myUI.getMessage(Messages.Status), Settings.button};
        buildBody();
    }

    public void buildBody() {

        FilterTable dataTable = new FilterTable();
        dataTable.setSizeFull();
        dataTable.setFilterDecorator(new MyFilterDecorator(myUI));
        dataTable.setStyleName(ValoTheme.TABLE_SMALL);
        dataTable.addStyleName("noWrap");
        dataTable.setSizeFull();
        dataTable.setFilterBarVisible(true);
        dataTable.setFooterVisible(true);
        try {
            DbEmployeeMessage dbCon = new DbEmployeeMessage();
            dbCon.connect();
            if (currentUser.isPermitted(Settings.cnMessagesView + ":" + Settings.actReadMessages)) {
                dbCon.execSQL(myUI, myUI.getUser().getId(), myUI.getUser().getSchool().getId(), dataTable);
            } else {
                dbCon.execSQL(myUI, myUI.getUser().getId(), 0, dataTable);
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
        dataTable.setVisibleColumns((Object[]) NATURAL_COL_ORDER);
        dataTable.setColumnExpandRatio(myUI.getMessage(Messages.Message), 1);
        dataTable.setColumnWidth(myUI.getMessage(Messages.Date), 80);
        dataTable.setColumnWidth(Settings.button, 60);
        dataTable.setCellStyleGenerator((CustomTable.CellStyleGenerator) (source, itemId, propertyId) -> {

            if (propertyId == null) {
                // Styling for row
                if ((Integer) source.getContainerProperty(itemId,
                        Settings.status_id).getValue() == 2) {
                    return "highlight-red";
                } else {
                    return null;
                }
            } else {
                // styling for column propertyId
                return null;
            }
        });
        addComponent(dataTable);
    }
}
