package kg.alex.ellipse.utils;

import com.vaadin.data.Validator;
import com.vaadin.ui.TextField;
import kg.alex.ellipse.MyVaadinUI;
import kg.alex.ellipse.i18n.Messages;

public class TextFieldValidated extends TextField {


    public TextFieldValidated(final MyVaadinUI myui) {
        setImmediate(true);

        // Create the validator
        Validator textFieldValidator = new Validator() {

            public boolean isValid(Object value) {

                int number;
                try {
                    number = Integer.parseInt(value.toString());
                } catch (Exception e) {
                    return false;
                }
                if (number < 0 || number > 100000) {
                    return false;
                }
                return true;
            }

            // Upon failure, the validate() method throws an exception
            // with an error message.
            public void validate(Object value)
                    throws Validator.InvalidValueException {
                if (!isValid(value)) {
                    if (value != null
                            && value.toString().startsWith("-")) {
                        throw new Validator.InvalidValueException(
                                myui.getMessage(Messages.NotificationWrongValue));
                    } else {
                        throw new Validator.InvalidValueException(
                                myui.getMessage(Messages.NotificationWrongValue));
                    }
                }
            }
        };
        addValidator(textFieldValidator);
    }
}
