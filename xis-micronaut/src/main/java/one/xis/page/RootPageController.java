package one.xis.page;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.inject.Inject;
import one.xis.micronaut.MicronautContextAdapter;

import java.util.Optional;

@Controller
class RootPageController {
    
    @Inject
    private MicronautContextAdapter adapter;
    private PageService pageService;

    @jakarta.annotation.PostConstruct
    void init() {
        pageService = adapter.getPageService();
        pageService.createRootContent();
    }

    @Get(uris = {"/", "/{page}.html"})
    HttpResponse<String> getPage(@PathVariable("page") Optional<String> page) {
        String html = pageService.getRootPageHtml();
        return HttpResponse.ok(html)//
                .contentType("text/html")//
                .characterEncoding("utf-8")//
                .contentLength(html.length());
    }
}
