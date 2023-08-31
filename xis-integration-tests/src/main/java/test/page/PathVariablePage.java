package test.page;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@RequiredArgsConstructor
@Page("/url-parameter/{x}/{y}.html")
class PathVariablePage {

    @ModelData("result")
    String model(@PathVariable("x") String x, @PathVariable("y") int y) {
        return x + y;
    }
}
