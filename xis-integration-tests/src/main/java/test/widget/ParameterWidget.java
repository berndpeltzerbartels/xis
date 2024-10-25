package test.widget;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Widget
@RequiredArgsConstructor
class ParameterWidget {

    private final ParameterWidgetService service;

    @Action("action")
    void action(@PathVariable("a") Integer a, @URLParameter("b") Integer b, @Parameter("c") int c) {
        service.action(a, b, c);
    }
}
