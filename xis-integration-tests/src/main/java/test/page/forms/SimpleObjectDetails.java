package test.page.forms;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/simpleObject/{id}.html")
@RequiredArgsConstructor
class SimpleObjectDetails {

    private final SimpleObjectService simpleObjectService;

    @ModelData("simpleObject")
    SimpleObject simpleObject(@PathVariable("id") Integer id) {
        return simpleObjectService.getById(id);
    }

}
