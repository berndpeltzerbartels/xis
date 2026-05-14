package test.frontlet;

import lombok.RequiredArgsConstructor;
import one.xis.*;

@Frontlet
@RequiredArgsConstructor
class ParameterFrontlet {

    private final ParameterFrontletService service;

    @Action("action")
    void action(@PathVariable("a") Integer a, @QueryParameter("b") Integer b, @Parameter("c") int c) {
        service.action(a, b, c);
    }
}
