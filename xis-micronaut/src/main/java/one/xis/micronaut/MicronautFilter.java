package one.xis.micronaut;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.FilterPatternStyle;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.server.FrontendService;
import org.reactivestreams.Publisher;

@Filter(patternStyle = FilterPatternStyle.REGEX, patterns = {".*\\.html", "/"})
class MicronautFilter implements HttpServerFilter {

    @Inject
    private MicronautContextAdapter contextAdapter;

    private FrontendService frontendService;

    @PostConstruct
    void init() {
        frontendService = contextAdapter.getFrontendService();
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return Publishers.just(HttpResponse.ok(frontendService.getRootPageHtml())
                .status(HttpStatus.OK)
                .contentType(MediaType.TEXT_HTML));
    }


}