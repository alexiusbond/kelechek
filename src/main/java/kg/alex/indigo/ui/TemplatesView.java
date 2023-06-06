package kg.alex.indigo.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.i18n.IndigoMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

public class TemplatesView extends VerticalLayout implements Button.ClickListener {
    static final Logger logger = LogManager.getLogger(TemplatesView.class);
    private final MyVaadinUI myUI;
    private final Table dataTable;

    public TemplatesView(MyVaadinUI myUI) {
        this.myUI = myUI;

        dataTable = new Table();
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        dataTable.setSizeFull();
        dataTable.setSelectable(false);
        setDataTable();

        this.setSizeFull();
        this.addComponent(dataTable);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        try {
            myUI.getPage().open(new FileResource(new File("/home/indigo/docs/" + source.getData())),
                    "_blank", false);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    private Button createButton(String itemId) {
        Button btn = new Button(myUI.getMessage(IndigoMessages.DownloadButton));
        btn.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
        btn.setIcon(FontAwesome.DOWNLOAD);
        btn.setData(itemId);
        btn.addClickListener(this);
        return btn;
    }

    private void setDataTable() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Title), String.class, null);
        container.addContainerProperty(myUI.getMessage(IndigoMessages.Date), Date.class, null);
        container.addContainerProperty(Settings.button, Button.class, null);
        try {
            File folder = new File("/home/indigo/docs");
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                for (File listOfFile : listOfFiles) {
                    BasicFileAttributes basicFileAttributes = Files.readAttributes(listOfFile.toPath(), BasicFileAttributes.class);
                    if (basicFileAttributes.isRegularFile()) {
                        String id = listOfFile.getName();
                        Item item = container.addItem(id);
                        item.getItemProperty(myUI.getMessage(IndigoMessages.Title)).setValue(listOfFile.getName());
                        item.getItemProperty(myUI.getMessage(IndigoMessages.Date)).setValue(
                                new Date(basicFileAttributes.lastModifiedTime().toMillis()));
                        item.getItemProperty(Settings.button).setValue(createButton(id));
                    }
                }
            }
            dataTable.clear();
            container.sort(new String[]{myUI.getMessage(IndigoMessages.Date)}, new boolean[]{false});
            dataTable.setContainerDataSource(container);
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
