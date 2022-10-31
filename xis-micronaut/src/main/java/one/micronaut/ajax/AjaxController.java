package one.micronaut.ajax;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.ajax.AjaxService;
import one.ajax.ConnectorRequest;
import one.ajax.ConnectorResponse;
import one.micronaut.micronaut.MicronautContextAdapter;

@Controller
class AjaxController {

    @Inject
    private MicronautContextAdapter adapter;
    private AjaxService ajaxService;

    @PostConstruct
    void init() {
        ajaxService = adapter.getAjaxService();
    }

    @Post(consumes = "application/json", produces = "application/json", uri = "/xis/connector")
    ConnectorResponse handleMessage(ConnectorRequest request) {
        return ajaxService.handleMessage(request);
    }

}
