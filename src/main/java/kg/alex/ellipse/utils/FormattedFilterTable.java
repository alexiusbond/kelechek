/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.ellipse.utils;

import com.vaadin.data.Property;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.Settings;
import kg.alex.ellipse.i18n.Messages;
import org.tepi.filtertable.FilterTable;

/**
 * @author alex
 */
public class FormattedFilterTable extends FilterTable {
    private final MyVaadinUI myUI;

    public FormattedFilterTable(MyVaadinUI myUi) {
        super();
        this.myUI = myUi;
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {
        if (property.getType() == Double.class) {
            if (property.getValue() != null) {
                if (colId.equals(myUI.getMessage(Messages.Rate))) {
                    return Settings.dFormat4.format(property.getValue());
                } else {
                    return Settings.dFormat2.format(property.getValue());
                }
            }
        }
        return super.formatPropertyValue(rowId, colId, property);
    }
}
