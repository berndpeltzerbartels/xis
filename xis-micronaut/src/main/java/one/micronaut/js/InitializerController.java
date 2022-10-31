package one.micronaut.js;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.micronaut.micronaut.MicronautContextAdapter;
import one.xis.root.RootPageService;

@Controller
class InitializerController {

    @Inject
    private MicronautContextAdapter adapter;
    private RootPageService rootPageService;

    @PostConstruct
    void init() {
        rootPageService = adapter.getRootPageService();
    }

    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/initializer.js")
    String getInitializerJs() {
        return rootPageService.getInitializerScipt();
    }

}