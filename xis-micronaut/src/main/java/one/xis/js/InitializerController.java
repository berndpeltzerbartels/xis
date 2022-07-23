package one.xis.js;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.micronaut.MicronautContextAdapter;

@Controller
class InitializerController {

    @Inject
    private MicronautContextAdapter adapter;
    private ApiJavascriptService apiJavascriptService;

    @PostConstruct
    void init() {
        apiJavascriptService = adapter.getApiJavascriptService();
    }

    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/initializer.js")
    String getInitializerJs() {
        return apiJavascriptService.getInitializerScipt();
    }

}