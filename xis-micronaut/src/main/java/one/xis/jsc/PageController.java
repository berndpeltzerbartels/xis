package one.xis.jsc;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

@Controller
class PageController {

    @Inject
    private MicronautContextAdapter contextAdapter;

    private Pages pages;

    @PostConstruct
    void init() {
        pages = contextAdapter.getPages();
    }

    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/page/{pageUrn}")
    String getPage(@PathVariable("pageUrn") String pageUrn) {
        return pages.get(pageUrn).getJavascript();
    }

}