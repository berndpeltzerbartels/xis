package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Frontlet;
import one.xis.FrontletParameter;

@Frontlet
@RequiredArgsConstructor
class SimpleObjectDetailsFrontlet {

    private final SimpleObjectService simpleObjectService;

    @ModelData("simpleObject")
    SimpleObject simpleObject(@FrontletParameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

}
