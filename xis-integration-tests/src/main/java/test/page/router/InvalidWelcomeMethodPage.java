package test.page.router;

import one.xis.Page;
import one.xis.WelcomePage;

@Page("/invalid-welcome-method.html")
class InvalidWelcomeMethodPage {

    @WelcomePage
    String invalidWelcomeMethod() {
        return "/router-target/invalid.html";
    }
}
