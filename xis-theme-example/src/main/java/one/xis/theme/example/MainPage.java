package one.xis.theme.example;

import one.xis.Page;
import one.xis.Title;
import one.xis.WelcomePage;

@WelcomePage
@Page("/app.html")
public class MainPage {

    @Title
    public String title() {
        return "Contact Manager - XIS Theme Example";
    }
}
