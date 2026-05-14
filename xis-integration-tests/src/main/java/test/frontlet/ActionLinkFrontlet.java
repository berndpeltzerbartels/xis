package test.frontlet;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Parameter;
import one.xis.ModelData;
import one.xis.Frontlet;
import test.page.core.IndexPage;

/*

 */
@Frontlet
@RequiredArgsConstructor
class ActionLinkFrontlet {

    private final ActionLinkFrontletService service;

    @ModelData("data")
    ActionLinkFrontletData data() {
        return service.getData();
    }

    @ModelData("action3")
    String getAction3() {
        return "test-action3";
    }

    @Action("test-action1")
    void action1(@Parameter("param") String value) {
        service.action(value);
    }

    @Action("test-action2")
    Class<?> action2(@Parameter("id") Integer id, @Parameter("value") String value) {
        service.action(new Object[]{id, value});
        return ActionLinkFrontlet.class;
    }

    @Action("test-action3")
    Class<?> action3() {
        return IndexPage.class;
    }


    @Action("test-action4")
    Class<?> action4(@Parameter("greeting") String greeting) {
        return SimpleFrontlet.class;
    }


}


