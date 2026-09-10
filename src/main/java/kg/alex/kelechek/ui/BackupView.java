package kg.alex.kelechek.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.kelechek.MyVaadinUI;
import kg.alex.kelechek.i18n.Messages;
import kg.alex.kelechek.utils.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class BackupView extends HorizontalSplitPanel implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(BackupView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;
    private final Button takeBakup;

    public BackupView(MyVaadinUI myUI) {
        this.myUI = myUI;

        takeBakup = new Button(myUI.getMessage(Messages.TakeBackupButton));
        takeBakup.setWidth(Settings.PERCENTS100);
        takeBakup.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        takeBakup.setIcon(FontAwesome.DATABASE);
        takeBakup.addClickListener(this);

        dataTable = new Table();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(false);
        setDataTable();

        this.setSplitPosition(24, Sizeable.Unit.PERCENTAGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(takeBakup);
        this.setSecondComponent(dataTable);

    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == takeBakup) {
            try {
                ProcessBuilder pb = new ProcessBuilder("/home/kelechek/backup.sh", "myArg1", "myArg2");
                pb.start();
            } catch (IOException e) {
                logger.error(e);
                logger.catching(e);
            }
            setDataTable();
        } else {
            try {
                myUI.getPage().open(new FileResource(
                                new File("/home/kelechek/backups/" + source.getData())),
                        "_blank", false);
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }

    private Button createButton(String itemId) {
        Button btn = new Button(myUI.getMessage(Messages.DownloadBackupButton));
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.setIcon(FontAwesome.DOWNLOAD);
        btn.setData(itemId);
        btn.addClickListener(this);
        return btn;
    }

    private void setDataTable() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(Messages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(Messages.Date), Date.class, null);
        container.addContainerProperty(Settings.button, Button.class, null);
        try {
            File folder = new File("/home/kelechek/backups");
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File listOfFile : listOfFiles) {
                    BasicFileAttributes basicFileAttributes = Files.readAttributes(listOfFile.toPath(), BasicFileAttributes.class);
                    if (basicFileAttributes.isRegularFile()) {
                        String id = listOfFile.getName();
                        Item item = container.addItem(id);
                        item.getItemProperty(myUI.getMessage(Messages.Title)).setValue(
                                listOfFile.getName());
                        item.getItemProperty(myUI.getMessage(Messages.Date)).setValue(
                                new Date(basicFileAttributes.lastModifiedTime().toMillis()));
                        item.getItemProperty(Settings.button).setValue(createButton(id));
                    }
                }
            }
            dataTable.clear();
            container.sort(new String[]{myUI.getMessage(Messages.Date)},
                    new boolean[]{false});
            dataTable.setContainerDataSource(container);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

}
