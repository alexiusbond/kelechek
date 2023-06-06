/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.utils;

import com.vaadin.data.Property;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.Settings;
import kg.alex.indigo.i18n.IndigoMessages;
import org.tepi.filtertable.FilterTable;

/**
 * @author alex
 */
public class FormattedFilterTable extends FilterTable {
    private MyVaadinUI myUI;

    public FormattedFilterTable(MyVaadinUI myUi) {
        super();
        this.myUI = myUi;
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {
        if (property.getType() == Double.class) {
            if (property.getValue() != null) {
                if (colId.equals(myUI.getMessage(IndigoMessages.Rate))) {
                    return Settings.dFormat4.format(property.getValue());
                } else {
                    return Settings.dFormat2.format(property.getValue());
                }
            }
        }
        return super.formatPropertyValue(rowId, colId, property);
    }
}
