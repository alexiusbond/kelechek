package kg.alex.indigo.utils;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.converter.Converter;
import kg.alex.indigo.MyVaadinUI;
import kg.alex.indigo.dao.DbAccCategory;
import kg.alex.indigo.dao.DbEmployee;
import kg.alex.indigo.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public class ValueFromContainerConverter implements Converter<String, Integer> {
    static final Logger logger = LogManager.getLogger(ValueFromContainerConverter.class);
    private final IndexedContainer container;
    private final String propertyId;
    private final MyVaadinUI myUi;

    public ValueFromContainerConverter(IndexedContainer container, String propertyId, MyVaadinUI myUi) {
        this.container = container;
        this.propertyId = propertyId;
        this.myUi = myUi;
    }

    @Override
    public Integer convertToModel(String value, Class<? extends Integer> targetType, Locale locale) throws ConversionException {
        return 0;
    }

    @Override
    public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale) throws ConversionException {
        if (container.getContainerProperty(value, myUi.getMessage(Messages.Title)) != null &&
                container.getContainerProperty(value, myUi.getMessage(Messages.Title)).getValue() != null) {
            return container.getContainerProperty(value, myUi.getMessage(Messages.Title)).getValue().toString();
        } else if (value != null && propertyId != null && propertyId.equals(myUi.getMessage(Messages.Category))) {
            try {
                DbAccCategory dbCon = new DbAccCategory();
                dbCon.connect();
                String category = dbCon.exec_for_select_by_id(value);
                dbCon.close();
                if (category != null) {
                    return category;
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        } else if (value != null && propertyId != null && propertyId.equals(myUi.getMessage(Messages.ToEmployee))) {
            try {
                DbEmployee dbCon = new DbEmployee();
                dbCon.connect();
                String category = dbCon.execSQL_by_id(value);
                dbCon.close();
                if (category != null) {
                    return category;
                }
            } catch (Exception e) {
                logger.error(e);
                logger.catching(e);
            }
        }
        return null;
    }

    @Override
    public Class<Integer> getModelType() {
        return Integer.class;
    }

    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }
}
