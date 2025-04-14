package kg.alex.lomonosov.tableexport;

import com.vaadin.data.Property;

public interface ExportableFormattedProperty {

    public String getFormattedPropertyValue(Object rowId, Object colId, Property property);
}
