package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Widget
@RequiredArgsConstructor
class WidgetContainerParameterWidget {

    private final WidgetContainerParameterService service;

    @ModelData("categoryId")
    String categoryId(@WidgetParameter("categoryId") String categoryId) {
        return categoryId;
    }

    @ModelData("sortBy")
    String sortBy(@WidgetParameter("sortBy") String sortBy) {
        return sortBy;
    }

    @Action("testAction")
    void testAction(@WidgetParameter("categoryId") String categoryId,
                   @WidgetParameter("sortBy") String sortBy) {
        service.action(categoryId, sortBy);
    }
}
