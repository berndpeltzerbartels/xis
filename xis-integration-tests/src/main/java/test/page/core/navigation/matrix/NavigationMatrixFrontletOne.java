package test.page.core.navigation.matrix;

import one.xis.Action;
import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.FrontletResponse;
import one.xis.ModelData;
import one.xis.PageResponse;
import one.xis.PathVariable;
import one.xis.QueryParameter;

@Frontlet
class NavigationMatrixFrontletOne {

    private final NavigationMatrixService service;

    NavigationMatrixFrontletOne(NavigationMatrixService service) {
        this.service = service;
    }

    @ModelData
    String frontletSection(@PathVariable("section") String section) {
        return section;
    }

    @ModelData
    String frontletMode(@QueryParameter("mode") String mode) {
        return mode;
    }

    @ModelData
    String frontletMessage(@FrontletParameter("message") String message) {
        return message;
    }

    @ModelData
    int frontletRefreshCount() {
        return service.nextFrontletRefreshCount();
    }

    @Action
    Class<?> frontletPageClass() {
        return NavigationMatrixTargetPage.class;
    }

    @Action
    PageResponse frontletPageResponse() {
        return PageResponse.of(NavigationMatrixDetailPage.class, "id", "44")
                .queryParameter("mode", "frontlet-page-response");
    }

    @Action
    Class<?> frontletClass() {
        return NavigationMatrixFrontletTwo.class;
    }

    @Action
    FrontletResponse frontletResponse() {
        return new FrontletResponse(NavigationMatrixParameterizedFrontlet.class)
                .frontletParameter("message", "from-frontlet-response");
    }

    @Action
    FrontletResponse changeSideContainer() {
        return new FrontletResponse(NavigationMatrixSideReplacementFrontlet.class)
                .targetContainer("side");
    }

    @Action
    void frontletVoid() {
    }
}
