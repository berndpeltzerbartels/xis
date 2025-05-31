package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Widget;
import one.xis.WidgetParameter;

@Widget
@RequiredArgsConstructor
class SimpleObjectDetailsWidget {

    private final SimpleObjectService simpleObjectService;

    @ModelData("simpleObject")
    SimpleObject simpleObject(@WidgetParameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

}
