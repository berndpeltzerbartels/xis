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

    private PageJavascripts pageJavascripts;

    @PostConstruct
    void init() {
        pageJavascripts = contextAdapter.getPageJavascripts();
    }

    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/page/{pageId}")
    String getPage(@PathVariable("pageId") String pageId) {
        return pageJavascripts.get(pageId).getJavascript();
    }

}