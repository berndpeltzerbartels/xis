package one.spring.ajax;

import one.ajax.AjaxService;
import one.xis.dto.ActionRequest;
import one.xis.dto.ActionResponse;
import one.xis.dto.InitialResponse;
import one.xis.dto.ModelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class AjaxController {

    @Autowired
    private AjaxService ajaxService;

    @PostMapping("/xis/page/model")
    InitialResponse handlePageModelRequest(ModelRequest request) {
        return ajaxService.handlePageInitialRequest(request);
    }

    @PostMapping("/xis/page/action")
    ActionResponse handlePageActionRequest(ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }

    @PostMapping("/xis/widget/model")
    InitialResponse handleWidgetModelRequest(ModelRequest request) {
        return ajaxService.handlePageInitialRequest(request);
    }

    @PostMapping("/xis/widget/action")
    ActionResponse handleWidgetActionRequest(ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }
}
