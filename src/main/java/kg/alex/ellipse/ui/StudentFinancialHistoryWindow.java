package kg.alex.ellipse.ui;

import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.dao.DbStudentContract;
import kg.alex.ellipse.i18n.Messages;
import kg.alex.ellipse.tableexport.EnhancedFormatExcelExport;
import kg.alex.ellipse.utils.FormattedTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StudentFinancialHistoryWindow extends Window implements Button.ClickListener {
    static final Logger logger = LogManager.getLogger(StudentFinancialHistoryWindow.class);
    private final MyVaadinUI myUI;
    private final FormattedTable dataTable;
    private final Button excelBtn;

    public StudentFinancialHistoryWindow(MyVaadinUI myUI, String title, int studentId) {
        this.myUI = myUI;
        this.setWidth("90%");
        this.setHeight("90%");
        this.setModal(true);
        this.setCaption(title);

        VerticalLayout mainLay = new VerticalLayout();
        mainLay.setSizeFull();
        mainLay.setSpacing(true);
        mainLay.setMargin(true);
        this.setContent(mainLay);

        excelBtn = new Button(myUI.getMessage(Messages.ExportToExcel));
        excelBtn.setEnabled(false);
        excelBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        excelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        excelBtn.setIcon(FontAwesome.FILE_EXCEL_O);
        excelBtn.addClickListener(this);
        mainLay.addComponent(excelBtn);

        dataTable = new FormattedTable(myUI);
        dataTable.setFooterVisible(true);
        dataTable.setSizeFull();
        dataTable.setRowHeaderMode(Table.RowHeaderMode.INDEX);
        dataTable.setStyleName(ValoTheme.TABLE_COMPACT);
        mainLay.addComponent(dataTable);
        mainLay.setExpandRatio(dataTable, 1);

        try {
            DbStudentContract dbCon = new DbStudentContract();
            dbCon.connect();
            dbCon.execFinancialHistory(myUI, studentId, dataTable);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Debt), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Repayment), Table.Align.RIGHT);
            dataTable.setColumnAlignment(myUI.getMessage(Messages.Balance), Table.Align.RIGHT);

            if (dataTable.getContainerDataSource().size() != 0) {
                excelBtn.setEnabled(true);
            }
            dbCon.close();
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == excelBtn) {
            try {
                if (dataTable.getContainerDataSource().size() != 0) {
                    EnhancedFormatExcelExport excelReport = new EnhancedFormatExcelExport(dataTable);
                    excelReport.setReportTitle(this.getCaption());
                    excelReport.setDisplayTotals(true);
                    excelReport.convertTable();
                    excelReport.getTotalsRow().getCell(0).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(3).setCellFormula(null);
                    excelReport.getTotalsRow().getCell(6).setCellValue(dataTable.getColumnFooter(myUI.getMessage(Messages.Balance)));
                    excelReport.sendConverted();
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
    }
}
