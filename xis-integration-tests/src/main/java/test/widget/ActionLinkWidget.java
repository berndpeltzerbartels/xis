package test.widget;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.ModelData;
import one.xis.Widget;
import test.page.IndexPage;

@Widget
@RequiredArgsConstructor
class ActionLinkWidget {

    private final ActionLinkWidgetService service;

    @ModelData("data")
    ActionLinkWidgetData data() {
        return service.getData();
    }

    @ModelData("action3")
    String getAction3() {
        return "test-action3";
    }

    @Action("test-action1")
    void action1(@ModelData("data") ActionLinkWidgetData data) {
        service.update(data);
    }

    @Action("test-action2")
    Class<?> action2(@ModelData("data") ActionLinkWidgetData data) {
        service.update(data);
        return ActionLinkWidget.class;
    }

    @Action("test-action3")
    Class<?> action3(@ModelData("data") ActionLinkWidgetData data, @NonNull @ModelData("action3") String action3) {
        service.update(data);
        return IndexPage.class;
    }


    @Action("test-action4")
    Class<?> action4() {
        return SimpleWidget.class;
    }


}


