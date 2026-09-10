/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek.utils;

import com.vaadin.data.Container.Filter;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.FilterTable;

/**
 * @author alex
 */
public class MyFilterGenerator implements FilterGenerator {

    private final Label l;
    private final String labelText;
    private final FilterTable t;

    public MyFilterGenerator(Label l, String labeltext, FilterTable t) {
        this.l = l;
        this.labelText = labeltext;
        this.t = t;
    }

    @Override
    public Filter generateFilter(Object propertyId, Object value) {
        // For other properties, use the default filter
        return null;
    }

    @Override
    public Filter generateFilter(Object propertyId, Field<?> originatingField) {
        return null;
    }

    @Override
    public AbstractField<?> getCustomFilterComponent(Object propertyId) {
        return null;
    }

    @Override
    public void filterRemoved(Object propertyId) {
        l.setValue(labelText + ": " + t.size());
    }

    @Override
    public void filterAdded(Object propertyId, Class<? extends Filter> filterType, Object value) {
        l.setValue(labelText + ": " + t.size());
    }

    @Override
    public Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
        return null;
    }

}
