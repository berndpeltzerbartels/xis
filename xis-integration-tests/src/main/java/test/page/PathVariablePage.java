package test.page;

import lombok.RequiredArgsConstructor;
import one.xis.Model;
import one.xis.Page;
import one.xis.PathVariable;

@RequiredArgsConstructor
@Page("/url-parameter/{x}/{y}.html")
class PathVariablePage {

    @Model("result")
    String model(@PathVariable("x") String x, @PathVariable("y") int y) {
        return x + y;
    }
}
