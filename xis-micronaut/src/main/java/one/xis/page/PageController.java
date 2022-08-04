package one.xis.page;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.micronaut.MicronautContextAdapter;

@Controller
class PageController {

    @Inject
    private MicronautContextAdapter adapter;
    private PageService pageService;

    @PostConstruct
    void init() {
        pageService = adapter.getPageService();
    }

    @Get(produces = "text/javascript; charset=utf-8", uri = "/xis/page/{pageId}")
    String getPage(@PathVariable("pageId") String pageId) {
        return pageService.getPage(pageId).getContent();
    }
    
}