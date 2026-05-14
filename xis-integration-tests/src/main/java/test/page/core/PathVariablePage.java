package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;
import one.xis.QueryParameter;

@RequiredArgsConstructor
@Page("/url-parameter/{x}/{y}.html")
class PathVariablePage {

    private String actionResult = "";

    @ModelData("result")
    String model(@PathVariable("x") String x, @PathVariable("y") int y) {
        return x + y;
    }

    @ModelData("actionResult")
    String actionResult() {
        return actionResult;
    }

    @Action("delete")
    void delete(@PathVariable("x") String x, @PathVariable("y") int y) {
        actionResult = x + y;
    }

    @Action("delete-with-query")
    void deleteWithQuery(@QueryParameter("filter") String filter, @QueryParameter("page") int page) {
        actionResult = filter + page;
    }
}
