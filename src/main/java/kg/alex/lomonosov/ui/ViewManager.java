package kg.alex.lomonosov.ui;

import com.vaadin.ui.Layout;
import kg.alex.lomonosov.MyVaadinUI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Stack;

public class ViewManager implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    HashMap<String, Layout> views = new HashMap<>();
    Stack<Layout> screenStack = new Stack<>();
    MyVaadinUI myUI;

    public ViewManager(MyVaadinUI myUI) {
        this.myUI = myUI;
    }

    public void switchScreen(String viewName, Layout newView) {
        Layout view;
        if (newView != null) {
            view = newView;
            views.put(viewName, newView);
        } else {
            view = views.get("viewName");
        }
        MyVaadinUI.getInstance().setContent(view);
    }

    public void pushScreen(String viewName, Layout newView) {
        screenStack.push((Layout) myUI.getContent());
        switchScreen(viewName, newView);
    }

    public void popScreen() {
        myUI.setContent(screenStack.pop());
    }
}
