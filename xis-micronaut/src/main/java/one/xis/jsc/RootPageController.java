package one.xis.jsc;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import java.util.Optional;

@Controller
class RootPageController {

    @Inject
    private MicronautContextAdapter contextAdapter;

    private RootPage rootPage;

    @PostConstruct
    void init() {
        rootPage = contextAdapter.getRootPage();
        rootPage.createContent();
    }

    @Get(uris = {"/", "/{page}.html"})
    HttpResponse<String> getPage(@PathVariable("page") Optional<String> page) {
        return HttpResponse.ok(rootPage.getContent())//
                .contentType("text/html")//
                .characterEncoding("utf-8")//
                .contentLength(rootPage.getContent().length());
    }
}
