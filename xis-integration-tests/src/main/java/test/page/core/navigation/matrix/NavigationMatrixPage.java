package test.page.core.navigation.matrix;

import one.xis.Action;
import one.xis.FrontletResponse;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.PageResponse;
import one.xis.PathVariable;
import one.xis.QueryParameter;

@Page("/navigation/{section}.html")
class NavigationMatrixPage {

    private final NavigationMatrixService service;

    NavigationMatrixPage(NavigationMatrixService service) {
        this.service = service;
    }

    @ModelData
    String section(@PathVariable("section") String section) {
        return section;
    }

    @ModelData
    String mode(@QueryParameter("mode") String mode) {
        return mode;
    }

    @ModelData
    int pageRefreshCount() {
        return service.nextPageRefreshCount();
    }

    @Action
    Class<?> pageClass() {
        return NavigationMatrixTargetPage.class;
    }

    @Action
    PageResponse pageResponse() {
        return PageResponse.of(NavigationMatrixDetailPage.class, "id", "42")
                .queryParameter("mode", "page-response");
    }

    @Action
    String pageString() {
        return "/navigation/detail/43.html?mode=page-string";
    }

    @Action
    void pageVoid() {
    }

    @Action
    Class<?> pageFrontletClass() {
        return NavigationMatrixFrontletTwo.class;
    }

    @Action
    FrontletResponse pageFrontletResponse() {
        return new FrontletResponse(NavigationMatrixParameterizedFrontlet.class)
                .targetContainer("main")
                .frontletParameter("message", "from-page-response");
    }
}
