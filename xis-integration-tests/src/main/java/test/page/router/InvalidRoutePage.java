package test.page.router;

import one.xis.Page;
import one.xis.Route;

@Page("/invalid-route-page.html")
class InvalidRoutePage {

    @Route
    String invalidRoute() {
        return "/router-target/1.html";
    }
}
