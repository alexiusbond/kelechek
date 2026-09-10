/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.kelechek;

import com.ejt.vaadin.loginform.LoginForm;
import com.kbdunn.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.ui.*;
import kg.alex.kelechek.i18n.Messages;

/**
 * @author alex
 */
public class MyLoginForm extends LoginForm {

    private final MyVaadinUI myUI;
    private final Label errorLbl = new Label();

    public MyLoginForm(MyVaadinUI ui) {
        myUI = ui;
    }

    @Override
    protected Component createContent(TextField userNameField,
                                      PasswordField passwordField, Button loginButton) {
        GridLayout layout = new GridLayout(3, 5);
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);

        errorLbl.addStyleName("failure");
        errorLbl.setSizeUndefined();
        errorLbl.setVisible(false);
        errorLbl.setImmediate(true);

        Label userNameLbl = new Label(myUI.getMessage(Messages.Username));
        userNameLbl.setSizeUndefined();
        userNameLbl.addStyleName("large");
        Label passwordLbl = new Label(myUI.getMessage(Messages.Password));
        passwordLbl.setSizeUndefined();
        passwordLbl.addStyleName("large");
        loginButton.setCaption(myUI.getMessage(Messages.Login));
        loginButton.setStyleName("large");
        loginButton.setStyleName("primary");
        loginButton.setWidth("35%");
        loginButton.setIcon(FontAwesome.SIGN_IN);
        userNameField.setCaption(null);
        userNameField.setSizeFull();
        userNameField.addStyleName("large");
        passwordField.setCaption(null);
        passwordField.setSizeFull();
        passwordField.addStyleName("large");

        layout.addComponent(errorLbl, 0, 0, 2, 0);
        layout.setComponentAlignment(errorLbl, Alignment.MIDDLE_CENTER);
        layout.addComponent(userNameLbl, 0, 1);
        layout.setComponentAlignment(userNameLbl, Alignment.MIDDLE_RIGHT);
        layout.addComponent(userNameField, 1, 1, 2, 1);
        layout.addComponent(passwordLbl, 0, 2);
        layout.setComponentAlignment(passwordLbl, Alignment.MIDDLE_RIGHT);
        layout.addComponent(passwordField, 1, 2, 2, 2);
        layout.addComponent(loginButton, 2, 4);
        layout.setComponentAlignment(loginButton, Alignment.MIDDLE_RIGHT);
        layout.setColumnExpandRatio(2, 1);
        return layout;
    }

    @Override
    protected void login(String userName, String password) {
        boolean error = false;
        if (userName != null && password != null && !userName.isEmpty() && !password.isEmpty()) {
            try {
                MyVaadinUI.getInstance().login(userName, password);
                // Switch to the protected view
                myUI.setContent(new AuthenticatedScreen(myUI));
            } catch (Exception uae) {
                uae.printStackTrace();
                error = true;
            }
            if (error) {
                errorLbl.setValue(myUI.getMessage(Messages.InvalidUserNamePassword));
                errorLbl.setVisible(true);
            }
        } else {
            errorLbl.setValue(myUI.getMessage(Messages.LoginUsernameNotEmpty));
            errorLbl.setVisible(true);
        }
    }
}
