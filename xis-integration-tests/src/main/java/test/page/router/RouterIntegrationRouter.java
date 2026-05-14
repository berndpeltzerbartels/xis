package test.page.router;

import one.xis.PageResponse;
import one.xis.PathVariable;
import one.xis.QueryParameter;
import one.xis.Route;
import one.xis.Router;

@Router("/router")
class RouterIntegrationRouter {

    @Route("/string/{id}.html")
    String stringTarget(@PathVariable("id") String id, @QueryParameter("tab") String tab) {
        return "/router-target/" + id + ".html?tab=" + tab;
    }

    @Route("/page-response/{id}.html")
    PageResponse pageResponseTarget(@PathVariable("id") String id, @QueryParameter("tab") String tab) {
        return PageResponse.of(RouterTargetPage.class, "id", id)
                .queryParameter("tab", tab);
    }
}
