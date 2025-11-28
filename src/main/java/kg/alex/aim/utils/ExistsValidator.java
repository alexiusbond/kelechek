/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.aim.utils;

import com.vaadin.data.Container;
import com.vaadin.data.Validator;
import com.vaadin.ui.ComboBox;
import kg.alex.aim.MyVaadinUI;
import kg.alex.aim.i18n.Messages;

/**
 * @author alex
 */
public class ExistsValidator implements Validator {

    private final Container container;
    private final MyVaadinUI myUi;
    private final ComboBox comboBox;
    private final String propertyName;

    public ExistsValidator(MyVaadinUI myUi, Container container, ComboBox comboBox, String propertyName) {
        this.myUi = myUi;
        this.container = container;
        this.comboBox = comboBox;
        this.propertyName = propertyName;
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        if (!isValid(value)) {
            throw new InvalidValueException(myUi.getMessage(Messages.ExistsNotification));
        }
    }

    private boolean isValid(Object value) {
        for (Object next : container.getItemIds()) {
            if (((ComboBox) container.getItem(next).getItemProperty(
                    propertyName).getValue()).getValue() != null &&
                    comboBox != container.getItem(next).getItemProperty(propertyName).getValue()
                    && value.toString().equals(((ComboBox) container.getItem(next).getItemProperty(
                    propertyName).getValue()).getValue().toString())) {
                return false;
            }
        }
        return true;
    }
}
