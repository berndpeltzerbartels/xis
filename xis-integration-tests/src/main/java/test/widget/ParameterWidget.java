package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Widget
@RequiredArgsConstructor
class ParameterWidget {

    private final ParameterWidgetService service;

    @Model("pathVariable")
    Integer a(@PathVariable("a") Integer a) {
        return a;
    }

    @Model("urlParameter")
    Integer b(@URLParameter("b") Integer b) {
        return b;
    }


    @Action("action")
    void action(@Model("pathVariable") Integer a, @Model("urlParameter") Integer b) {
        service.action(a, b);
    }
}
