package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Frontlet
@RequiredArgsConstructor
class ParameterFrontlet {

    private final ParameterWidgetService service;

    @Action("action")
    void action(@PathVariable("a") Integer a, @QueryParameter("b") Integer b, @FrontletParameter("c") int c) {
        service.action(a, b, c);
    }
}
