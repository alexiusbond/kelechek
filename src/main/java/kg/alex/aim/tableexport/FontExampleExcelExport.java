package kg.alex.aim.tableexport;

import com.vaadin.ui.Table;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;

public class FontExampleExcelExport extends ExcelExport {
    private static final long serialVersionUID = 3717947558186318581L;

    public FontExampleExcelExport(final Table table, final String sheetName) {
        super(table, sheetName);
        this.setRowHeaders(true);
        CellStyle style;
        Font f;

        style = this.getTitleStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f = workbook.createFont();
        f.setFontHeightInPoints((short) 12);
        f.setFontName(HSSFFont.FONT_ARIAL);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);
        style.setFont(f);
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
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f = workbook.createFont();
        f.setFontHeightInPoints((short) 12);
        f.setFontName(HSSFFont.FONT_ARIAL);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);
        style.setFont(f);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        style = this.getTotalsStyle();
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f = workbook.createFont();
        f.setFontHeightInPoints((short) 12);
        f.setFontName(HSSFFont.FONT_ARIAL);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);
        style.setFont(f);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        style = this.getDoubleDataStyle();
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        f = workbook.getFontAt(style.getFontIndex());
        f.setFontHeightInPoints((short) 12);
        f.setFontName(HSSFFont.FONT_ARIAL);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(false);
        style.setFont(f);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        final CellStyle newStyle = workbook.createCellStyle();
        newStyle.cloneStyleFrom(style);
        this.setRowHeaderStyle(newStyle);
    }
}
