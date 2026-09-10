package kg.alex.kelechek.tableexport;

import com.vaadin.data.Property;

public interface ExportableFormattedProperty {

    public String getFormattedPropertyValue(Object rowId, Object colId, Property property);
}
