package one.xis.micronaut.ajax;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.ajax.AjaxService;
import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialRequest;
import one.xis.dto.InitialResponse;
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


    @Post(consumes = "application/json", produces = "application/json", uri = "/xis/ajax/page/model")
    InitialResponse handlePageModelRequest(InitialRequest request) {
        return ajaxService.handlePageInitialRequest(request);
    }

    @Post(consumes = "application/json", produces = "application/json", uri = "/xis/ajax/page/action")
    ActionResponse handlePageActionRequest(ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }

    @Post(consumes = "application/json", produces = "application/json", uri = "/xis/ajax/widget/model")
    InitialResponse handleWidgetModelRequest(InitialRequest request) {
        return ajaxService.handleWidgetInitialRequest(request);
    }

    @Post(consumes = "application/json", produces = "application/json", uri = "/xis/ajax/widget/action")
    ActionResponse handleWidgetActionRequest(ActionRequest request) {
        return ajaxService.handleWidgetActionRequest(request);
    }

}
