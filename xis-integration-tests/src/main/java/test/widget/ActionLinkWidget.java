package test.widget;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Model;
import one.xis.Widget;

@Widget("/actionWidget.html")
@RequiredArgsConstructor
class ActionLinkWidget {

    private final ActionWidgetService service;

    @Model("data")
    ActionLinkWidgetData data() {
        return service.getData();
    }

    @Model("action3")
    String getAction3() {
        return "test-action3";
    }

    @Action("test-action1")
    void action1(@Model("data") ActionLinkWidgetData data) {
        service.update(data);
    }

    @Action("test-action2")
    Class<?> action2(@Model("data") ActionLinkWidgetData data) {
        service.update(data);
        return ActionLinkWidget.class;
    }

    @Action("test-action3")
    Class<?> action3(@Model("data") ActionLinkWidgetData data, @NonNull @Model("action3") String action3) {
        service.update(data);
        return LinkTestWidget.class;
    }


}


