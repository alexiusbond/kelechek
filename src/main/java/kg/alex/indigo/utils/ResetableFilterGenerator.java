/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.utils;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.FilterTable;

/**
 * @author alex
 */
public class ResetableFilterGenerator implements FilterGenerator {

    private final FilterTable t;

    public ResetableFilterGenerator(FilterTable t) {
        this.t = t;
    }

    @Override
    public Container.Filter generateFilter(Object propertyId, Object value) {
        // For other properties, use the default filter
        return null;
    }

    @Override
    public Container.Filter generateFilter(Object propertyId, Field<?> originatingField) {
        return null;
    }

    @Override
    public AbstractField<?> getCustomFilterComponent(Object propertyId) {
        return null;
    }

    @Override
    public void filterRemoved(Object propertyId) {
        t.setValue(null);
    }

    @Override
    public void filterAdded(Object propertyId, Class<? extends Container.Filter> filterType, Object value) {
        t.setValue(null);
    }

    @Override
    public Container.Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
        return null;
    }

}
