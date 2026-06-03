package test.page.router;

import one.xis.Route;
import one.xis.Router;
import one.xis.WelcomePage;

@WelcomePage
@Router("/invalid-welcome-router")
class InvalidWelcomeRouter {

    @Route("/first.html")
    String first() {
        return "/router-target/first.html";
    }

    @Route("/second.html")
    String second() {
        return "/router-target/second.html";
    }
}
