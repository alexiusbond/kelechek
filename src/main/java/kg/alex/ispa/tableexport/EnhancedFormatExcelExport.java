package kg.alex.ispa.tableexport;

import com.vaadin.ui.Table;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;

/**
 * Example of how the ExcelExport class might be extended to implement specific formatting features
 * in the exported file.
 */
public class EnhancedFormatExcelExport extends ExcelExport {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 9113961084041090666L;

    public EnhancedFormatExcelExport(final Table table) {
        this(table, "Enhanced Export");
    }

    public EnhancedFormatExcelExport(final Table table, final String sheetName) {
        super(table, sheetName);
        this.setRowHeaders(true);
        CellStyle style;
        Font f;

        style = this.getTitleStyle();
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f = workbook.getFontAt(style.getFontIndex());
        f.setFontHeightInPoints((short) 18);
        f.setFontName(HSSFFont.FONT_ARIAL);
        f.setColor(IndexedColors.WHITE.getIndex());
        f.setBold(true);
        style.setAlignment(HorizontalAlignment.CENTER_SELECTION);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        style = this.getColumnHeaderStyle();
        style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f = workbook.getFontAt(style.getFontIndex());
        f.setFontHeightInPoints((short) 12);
        f.setFontName(HSSFFont.FONT_ARIAL);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        style = this.getDoubleDataStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f = workbook.getFontAt(style.getFontIndex());
        f.setFontHeightInPoints((short) 12);
        f.setFontName(HSSFFont.FONT_ARIAL);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(false);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        this.setTotalsStyle(style);

        // we want the rowHeader style to be like the columnHeader style, just centered differently.
        final CellStyle newStyle = workbook.createCellStyle();
        newStyle.cloneStyleFrom(style);
        newStyle.setAlignment(HorizontalAlignment.LEFT);
        this.setRowHeaderStyle(newStyle);
    }
}
