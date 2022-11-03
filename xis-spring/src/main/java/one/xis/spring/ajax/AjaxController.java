package one.xis.spring.ajax;

import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialRequest;
import one.xis.dto.InitialResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import xis.ajax.AjaxService;

@Controller
class AjaxController {

    @Autowired
    private AjaxService ajaxService;

    @PostMapping("/one/xis/page/model")
    InitialResponse handlePageModelRequest(InitialRequest request) {
        return ajaxService.handlePageInitialRequest(request);
    }

    @PostMapping("/one/xis/page/action")
    ActionResponse handlePageActionRequest(ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }

    @PostMapping("/one/xis/widget/model")
    InitialResponse handleWidgetModelRequest(InitialRequest request) {
        return ajaxService.handleWidgetInitialRequest(request);
    }

    @PostMapping("/one/xis/widget/action")
    ActionResponse handleWidgetActionRequest(ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }
}
