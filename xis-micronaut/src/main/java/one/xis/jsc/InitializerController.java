package one.xis.jsc;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Controller
class InitializerController {

    @Inject
    private MicronautContextAdapter contextAdapter;

    private InitializerScript initializerScript;

    @PostConstruct
    void init() {
        initializerScript = contextAdapter.getInitializerScript();
    }

    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/initializer.js")
    String getInitializerJs() {
        return initializerScript.getContent();
    }

}