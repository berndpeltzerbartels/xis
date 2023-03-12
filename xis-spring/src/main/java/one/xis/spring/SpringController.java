package one.xis.spring;


import lombok.RequiredArgsConstructor;
import one.xis.server.Config;
import one.xis.server.FrontendService;
import one.xis.server.Request;
import one.xis.server.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/xis")
@RequiredArgsConstructor
class SpringController {

    private final FrontendService frontendService;

    @GetMapping("/config")
    Config getComponentConfig() {
        return frontendService.getConfig();
    }

    @GetMapping("/model")
    Response getModel(Request request) {
        return frontendService.invokeModelMethods(request);
    }

    @PostMapping("/action")
    Response onAction(Request request) {
        return frontendService.invokeActionMethod(request);
    }


}
