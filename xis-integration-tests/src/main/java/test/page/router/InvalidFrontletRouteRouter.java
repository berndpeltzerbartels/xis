package test.page.router;

import one.xis.FrontletResponse;
import one.xis.Route;
import one.xis.Router;

@Router("/invalid-frontlet-route")
class InvalidFrontletRouteRouter {

    @Route("/frontlet.html")
    FrontletResponse frontlet() {
        return new FrontletResponse("frontlet");
    }
}
