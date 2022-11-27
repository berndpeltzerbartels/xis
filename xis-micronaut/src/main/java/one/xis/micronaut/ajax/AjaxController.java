package one.xis.micronaut.ajax;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.ajax.AjaxRequest;
import one.xis.ajax.AjaxResponse;
import one.xis.ajax.AjaxService;
import one.xis.micronaut.MicronautContextAdapter;

@Controller
class AjaxController {

    @Inject
    private MicronautContextAdapter adapter;
    private AjaxService ajaxService;

    @PostConstruct
    void init() {
        ajaxService = adapter.getAjaxService();
    }

    @Post(consumes = "application/json", produces = "application/json", uri = "/xis/ajax/page")
    AjaxResponse handleRequest(AjaxRequest request, HttpHeaders headers) {
        return ajaxService.handleRequest(request, headers.get(AjaxService.CLIENT_ID_HEADER_NAME), headers.get(HttpHeaders.AUTHORIZATION));
    }
}
