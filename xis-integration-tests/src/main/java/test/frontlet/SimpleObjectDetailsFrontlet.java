package test.frontlet;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Frontlet;
import one.xis.Parameter;

@Frontlet
@RequiredArgsConstructor
class SimpleObjectDetailsFrontlet {

    private final SimpleObjectService simpleObjectService;

    @ModelData("simpleObject")
    SimpleObject simpleObject(@Parameter("id") Integer id) {
        return simpleObjectService.getById(id);
    }

}
