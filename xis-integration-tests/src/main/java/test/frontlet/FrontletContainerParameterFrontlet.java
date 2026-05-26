package test.frontlet;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Frontlet
@RequiredArgsConstructor
class FrontletContainerParameterFrontlet {

    private final FrontletContainerParameterService service;

    @ModelData("categoryId")
    String categoryId(@FrontletParameter("categoryId") String categoryId) {
        return categoryId;
    }

    @ModelData("sortBy")
    String sortBy(@FrontletParameter("sortBy") String sortBy) {
        return sortBy;
    }

    @Action("testAction")
    void testAction(@FrontletParameter("categoryId") String categoryId,
                    @FrontletParameter("sortBy") String sortBy) {
        service.action(categoryId, sortBy);
    }

    @Action("showSecond")
    FrontletResponse showSecond() {
        return new FrontletResponse(SecondFrontletContainerParameterFrontlet.class)
                .frontletParameter("categoryId", "replacement");
    }
}
