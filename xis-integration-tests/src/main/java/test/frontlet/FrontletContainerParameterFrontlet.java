package test.frontlet;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Frontlet
@RequiredArgsConstructor
class FrontletContainerParameterFrontlet {

    private final FrontletContainerParameterService service;

    @ModelData("categoryId")
    String categoryId(@Parameter("categoryId") String categoryId) {
        return categoryId;
    }

    @ModelData("sortBy")
    String sortBy(@Parameter("sortBy") String sortBy) {
        return sortBy;
    }

    @Action("testAction")
    void testAction(@Parameter("categoryId") String categoryId,
                   @Parameter("sortBy") String sortBy) {
        service.action(categoryId, sortBy);
    }

    @Action("showSecond")
    Class<?> showSecond() {
        return SecondFrontletContainerParameterFrontlet.class;
    }
}
