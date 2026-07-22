package kg.alex.abl.ui;

import com.vaadin.data.Property;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.abl.MyVaadinUI;
import kg.alex.abl.i18n.Messages;
import kg.alex.abl.reports.stock.ProductMovementsReport;
import kg.alex.abl.reports.stock.StockGeneralReport;
import kg.alex.abl.utils.Settings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class StockReportsView extends HorizontalSplitPanel implements Property.ValueChangeListener {

    private final MyVaadinUI myUI;
    private final Subject currentUser = SecurityUtils.getSubject();
    private ComboBox repTypeSelect;
    private GridLayout leftGrid, rightGrid;

    public StockReportsView(MyVaadinUI myUI) {
        this.myUI = myUI;
        buildGridLayouts();
        this.setSplitPosition(23, Sizeable.Unit.PERCENTAGE);
        this.setStyleName(ValoTheme.SPLITPANEL_LARGE);
        this.setSizeFull();
        this.setLocked(true);
        this.setFirstComponent(leftGrid);
        this.setSecondComponent(rightGrid);
    }

    private void buildGridLayouts() {

        leftGrid = new GridLayout(1, 2);
        leftGrid.setMargin(new MarginInfo(true, false, true, true));
        leftGrid.setSpacing(true);
        leftGrid.setSizeFull();

        rightGrid = new GridLayout(2, 2);
        rightGrid.setMargin(true);
        rightGrid.setSpacing(true);
        rightGrid.setWidth(Settings.PERCENTS100);

        repTypeSelect = new ComboBox(myUI.getMessage(Messages.ReportType));
        repTypeSelect.setNullSelectionAllowed(false);
        repTypeSelect.setRequired(true);
        repTypeSelect.setRequiredError(myUI.getMessage(Messages.RequiredField));
        repTypeSelect.setStyleName(ValoTheme.COMBOBOX_TINY);
        repTypeSelect.setWidth(Settings.PERCENTS100);
        repTypeSelect.setFilteringMode(FilteringMode.CONTAINS);
        repTypeSelect.addValueChangeListener(this);

        if (currentUser.isPermitted(Settings.cnStockReportsView + ":" + Settings.prmProductMovementsReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.ProductMovementsReport));
        }
        if (currentUser.isPermitted(Settings.cnStockReportsView + ":" + Settings.prmStockGeneralReport)) {
            repTypeSelect.addItem(myUI.getMessage(Messages.StockGeneralReport));
        }
        leftGrid.addComponent(repTypeSelect, 0, 0);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Property property = event.getProperty();
        if (property == repTypeSelect) {
            this.setSecondComponent(null);
            leftGrid.removeComponent(0, 1);
            if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.StockGeneralReport))) {
                new StockGeneralReport(myUI, this);
            } else if (repTypeSelect.getValue().equals(myUI.getMessage(Messages.ProductMovementsReport))) {
                new ProductMovementsReport(myUI, this);
            }
        }
    }
}
