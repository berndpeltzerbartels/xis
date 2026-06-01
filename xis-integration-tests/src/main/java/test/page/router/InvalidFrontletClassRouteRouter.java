package test.page.router;

import one.xis.Route;
import one.xis.Router;

@Router("/invalid-frontlet-class-route")
class InvalidFrontletClassRouteRouter {

    @Route("/frontlet.html")
    Class<?> frontletClass() {
        return RouterTargetFrontlet.class;
    }
}
