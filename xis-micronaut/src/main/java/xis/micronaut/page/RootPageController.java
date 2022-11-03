package xis.micronaut.page;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.inject.Inject;
import one.xis.root.RootPageService;
import xis.micronaut.MicronautContextAdapter;

import java.util.Optional;

@Controller
class RootPageController {

    @Inject
    private MicronautContextAdapter adapter;
    private RootPageService rootPageService;

    @jakarta.annotation.PostConstruct
    void init() {
        rootPageService = adapter.getRootPageService();
        rootPageService.createRootContent();
    }

    @Get(uris = {"/", "/{page}.html"})
    HttpResponse<String> getPage(@PathVariable("page") Optional<String> page) {
        String html = rootPageService.getRootPageHtml();
        return HttpResponse.ok(html)//
                .contentType("text/html")//
                .characterEncoding("utf-8")//
                .contentLength(html.length());
    }
}
