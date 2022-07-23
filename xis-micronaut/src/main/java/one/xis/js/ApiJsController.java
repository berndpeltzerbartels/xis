package one.xis.js;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.micronaut.MicronautContextAdapter;

@Controller(produces = "text/javascript; charset=utf-8")
class ApiJsController {

    @Inject
    private MicronautContextAdapter adapter;
    private ApiJavascriptService apiJavascriptService;

    @PostConstruct
    void init() {
        apiJavascriptService = adapter.getApiJavascriptService();
    }

    @Get("/xis/api/{file}")
    String getJavascript(@PathVariable("file") String file) {
        if (file.equals("custom-script.js")) {
            return apiJavascriptService.getCustomJavascript().getContent();
        }
        return apiJavascriptService.getJavascriptResource(file).getContent();
    }
}
