package kg.alex.lomonosov;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import kg.alex.lomonosov.i18n.Messages;

public class LoginScreen extends VerticalLayout {

    public LoginScreen(MyVaadinUI myUI) {

        setSizeFull();
        setStyleName("mainLayout");

        Label captionLbl = new Label(myUI.getMessage(Messages.AppTitle));
        captionLbl.setStyleName("mainPage");
        captionLbl.setSizeUndefined();

        Label footerLbl = new Label("<i class=\"fa fa-copyright\"></i>&emsp;Copyright 2017");
        footerLbl.setStyleName("h3");
        footerLbl.setContentMode(ContentMode.HTML);
        footerLbl.setSizeUndefined();

        HorizontalLayout hl = new HorizontalLayout();
        hl.setWidth(Settings.PERCENTS100);
        hl.setSpacing(true);
        hl.addStyleName("loginLayout");

        Panel loginPanel = new Panel(myUI.getMessage(Messages.Login));
        loginPanel.setStyleName("well");
        loginPanel.addStyleName("loginPanel");
        loginPanel.setHeight("25%");
        loginPanel.setWidth("40%");
        MyLoginForm loginForm = new MyLoginForm(myUI);
        loginForm.setSizeFull();
        loginPanel.setContent(loginForm);

        hl.addComponent(loginPanel);
        hl.setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER);

        addComponent(captionLbl);
        setComponentAlignment(captionLbl, Alignment.MIDDLE_CENTER);
        addComponent(hl);
        setComponentAlignment(hl, Alignment.MIDDLE_CENTER);
        addComponent(footerLbl);
        setComponentAlignment(footerLbl, Alignment.BOTTOM_CENTER);
    }
}
