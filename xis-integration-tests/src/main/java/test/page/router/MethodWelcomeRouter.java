package test.page.router;

import one.xis.Route;
import one.xis.Router;
import one.xis.WelcomePage;

@Router("/welcome-router")
class MethodWelcomeRouter {

    @WelcomePage
    @Route("/start.html")
    String start() {
        return "/router-target/welcome.html?tab=start";
    }
}
