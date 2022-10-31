package one.spring.ajax;

import one.ajax.AjaxService;
import one.xis.dto.ActionRequest;
import one.xis.dto.ModelRequest;
import one.xis.dto.ModelResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class AjaxController {

    @Autowired
    private AjaxService ajaxService;

    @PostMapping("/xis/page/model")
    ModelResponse handlePageModelRequest(ModelRequest request) {
        return ajaxService.handlePageModelRequest(request);
    }

    @PostMapping("/xis/page/model")
    ModelResponse handlePageActionRequest(ActionRequest request) {
        return ajaxService.handlePageActionRequest(request);
    }
}
