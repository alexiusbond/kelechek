package kg.alex.indigo.utils;

import com.vaadin.server.Resource;
import com.vaadin.shared.ui.datefield.Resolution;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.i18n.IndigoMessages;
import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

import java.io.Serializable;
import java.util.Locale;

public class MyFilterDecorator implements FilterDecorator, Serializable {

    private final MyVaadinUI myUI;

    public MyFilterDecorator(MyVaadinUI myUi) {
        myUI = myUi;
    }

    @Override
    public String getEnumFilterDisplayName(Object propertyId, Object value) {
        // returning null will output default value
        return null;
    }

    @Override
    public Resource getEnumFilterIcon(Object propertyId, Object value) {
        return null;
    }

    @Override
    public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
        // returning null will output default value
        return null;
    }

    @Override
    public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
        return null;
    }

    @Override
    public String getFromCaption() {
        return myUI.getMessage(IndigoMessages.StartDate);
    }

    @Override
    public String getToCaption() {
        return myUI.getMessage(IndigoMessages.EndDate);
    }

    @Override
    public String getSetCaption() {
        // use default caption
        return myUI.getMessage(IndigoMessages.Set);
    }

    @Override
    public String getClearCaption() {
        // use default caption
        return myUI.getMessage(IndigoMessages.Clear);
    }

    @Override
    public boolean isTextFilterImmediate(Object propertyId) {
        // use text change events for all the text fields
        return true;
    }

    @Override
    public int getTextChangeTimeout(Object propertyId) {
        // use the same timeout for all the text fields
        return 500;
    }

    @Override
    public String getAllItemsVisibleString() {
        return myUI.getMessage(IndigoMessages.Search);
    }

    @Override
    public Resolution getDateFieldResolution(Object propertyId) {
        return Resolution.DAY;
    }

    @Override
    public boolean usePopupForNumericProperty(Object propertyId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getDateFormatPattern(Object propertyId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return myUI.getLocale();
    }

    @Override
    public NumberFilterPopupConfig getNumberFilterPopupConfig() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
