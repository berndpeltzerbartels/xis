package one.xis.spring.ajax;

import one.xis.ajax.AjaxRequest;
import one.xis.ajax.AjaxResponse;
import one.xis.ajax.AjaxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
class AjaxController {

    @Autowired
    private AjaxService ajaxService;

    @PostMapping("/xis/ajax")
    AjaxResponse handleRequest(@RequestBody AjaxRequest request,
                               @RequestHeader(AjaxService.CLIENT_ID_HEADER_NAME) String clientId,
                               @RequestHeader("Authorization") String authorization) {
        return ajaxService.handleRequest(request, clientId, authorization);
    }
    
}
