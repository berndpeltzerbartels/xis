package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Parameter;
import one.xis.Widget;

@Widget("/simpleObject/{id}.html")
@RequiredArgsConstructor
class SimpleObjectDetailsWidget {

    private final SimpleObjectService simpleObjectService;

    @ModelData("simpleObject")
    SimpleObject simpleObject(@Parameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

}
