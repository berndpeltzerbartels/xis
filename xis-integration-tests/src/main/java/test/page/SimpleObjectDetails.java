package test.page;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.URLParameter;

@Page("/simpleObject/${id}.html")
@RequiredArgsConstructor
class SimpleObjectDetails {

    private final SimpleObjectService simpleObjectService;

    @ModelData("simpleObject")
    SimpleObject simpleObject(@URLParameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

}
