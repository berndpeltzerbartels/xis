package test.page.router;

import one.xis.Route;
import one.xis.Router;
import one.xis.WelcomePage;

@WelcomePage
@Router("/class-welcome-router")
class ClassWelcomeRouter {

    @Route("/start.html")
    String start() {
        return "/router-target/class.html?tab=start";
    }
}
