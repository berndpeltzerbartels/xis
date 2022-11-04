package one.xis.spring.ajax;

import one.xis.ajax.AjaxService;
import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialRequest;
import one.xis.dto.InitialResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
class AjaxController {

    @Autowired
    private AjaxService ajaxService;

    @PostMapping("/xis/ajax/page/model")
    InitialResponse handlePageModelRequest(@RequestBody InitialRequest request) {
        return ajaxService.handlePageInitialRequest(request);
    }

    @PostMapping("/xis/ajax/page/action")
    ActionResponse handlePageActionRequest(@RequestBody ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }

    @PostMapping("/xis/ajax/widget/model")
    InitialResponse handleWidgetModelRequest(@RequestBody InitialRequest request) {
        return ajaxService.handleWidgetInitialRequest(request);
    }

    @PostMapping("/xis/ajax/widget/action")
    ActionResponse handleWidgetActionRequest(@RequestBody ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }
}
