package kg.alex.ispa.ui;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;
import kg.alex.ispa.MyVaadinUI;
import kg.alex.ispa.dao.DbUserDetails;
import kg.alex.ispa.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;

public class ChangeUserData extends VerticalLayout implements Button.ClickListener {

    static final Logger logger = LogManager.getLogger(ChangeUserData.class);
    private final Subject currentUser = SecurityUtils.getSubject();
    private final MyVaadinUI myUI;
    private final String name;
    private Form userForm;

    public ChangeUserData(MyVaadinUI myUI) {
        this.myUI = myUI;

        name = myUI.getUser().getFullName();
//        surname = myUI.getUser().getSurname();

        setSpacing(true);
        buildBody();

    }

    public void buildBody() {
        FormLayout body = new FormLayout();
        body.setWidth("50%");
        userForm = new Form();
        ThemeResource iconOK = new ThemeResource("../mytheme/icons/16/ok.png");
        Button saveButton = new Button(myUI.getMessage(Messages.SaveButton));
        saveButton.setIcon(iconOK);
        saveButton.addClickListener(this);
        saveButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

        userForm.setCaption(myUI.getMessage(Messages.FormCaptionUser) + ": "
                + currentUser.getPrincipal().toString());
        userForm.setDescription(myUI.getMessage(Messages.FormDescription));
        userForm.setImmediate(true);

        userForm.addField("name",
                new TextField(myUI.getMessage(Messages.FullName),
                        name));
        userForm.getField("name").setEnabled(false);

        userForm.addField("pass",
                new PasswordField(myUI.getMessage(Messages.FormFiledCurPassword)));
        userForm.getField("pass").setRequired(true);
        userForm.getField("pass").setRequiredError(myUI.getMessage(Messages.RequiredErrorCurrPassword));

        userForm.addField("new_pass",
                new PasswordField(myUI.getMessage(Messages.NewPassword)));
        userForm.getField("new_pass").setRequired(true);
        userForm.getField("new_pass").setRequiredError(myUI.getMessage(Messages.RequiredErrorNewPassword));
        userForm.getField("new_pass").addValidator(new RegexpValidator("[a-zA-Z0-9!@#$%^&*().,]{6,20}",
                myUI.getMessage(Messages.RegexpValidatorError)));

        userForm.addField("conf_pass",
                new PasswordField(myUI.getMessage(Messages.FormFiledConfPassword)));
        userForm.getField("conf_pass").setRequired(true);
        userForm.getField("conf_pass").setRequiredError(myUI.getMessage(Messages.RequiredErrorConfPassword));
        // userForm.getField("conf_pass").addValidator(confPassValidator);

        userForm.getFooter().addComponent(saveButton);
        body.addComponent(userForm);
        addComponent(body);
    }

    public void buttonClick(ClickEvent event) {
        try {
            userForm.commit();
            if (!userForm.getField("new_pass").getValue().equals(
                    userForm.getField("conf_pass").getValue())) {
                Notification.show(myUI.getMessage(Messages.NotificationDontMatch),
                        Notification.Type.WARNING_MESSAGE);
            } else {
                try {
                    DbUserDetails dbUser = new DbUserDetails();
                    dbUser.connect();
                    if (new Sha256Hash(userForm.getField("pass").getValue()).toString().equals(
                            dbUser.execSQL_pass(currentUser.getPrincipal().toString()))) {

                        dbUser.editPass(currentUser.getPrincipal().toString(),
                                new Sha256Hash(userForm.getField("new_pass").getValue()).toString());
                        Notification.show(myUI.getMessage(Messages.NotificationSuccessfulChange));
                    } else {
                        Notification.show(myUI.getMessage(Messages.NotificationWrongCurrPassword),
                                Notification.Type.WARNING_MESSAGE);
                    }
                    dbUser.close();
                } catch (Exception e) {
                    logger.error(e);
                    logger.catching(e);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            logger.catching(e);
        }
    }
}
