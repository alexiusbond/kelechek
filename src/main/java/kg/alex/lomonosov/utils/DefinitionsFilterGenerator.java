/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.lomonosov.utils;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;
import org.tepi.filtertable.FilterGenerator;
import org.tepi.filtertable.FilterTable;

/**
 * @author alex
 */
public class DefinitionsFilterGenerator implements FilterGenerator {

    private final FilterTable t;

    public DefinitionsFilterGenerator(FilterTable t) {
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

    }

    @Override
    public void filterAdded(Object propertyId, Class<? extends Filter> filterType, Object value) {
        t.setValue(((IndexedContainer) t.getContainerDataSource()).firstItemId());
    }

    @Override
    public Filter filterGeneratorFailed(Exception reason, Object propertyId, Object value) {
        return null;
    }

}
