package test.page;

import lombok.RequiredArgsConstructor;
import one.xis.Model;
import one.xis.Page;
import one.xis.URLParameter;

@Page("/simpleObject/${id}.html")
@RequiredArgsConstructor
class SimpleObjectDetails {

    private final SimpleObjectService simpleObjectService;

    @Model("simpleObject")
    SimpleObject simpleObject(@URLParameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

}
