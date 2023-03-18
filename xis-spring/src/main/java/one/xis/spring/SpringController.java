package one.xis.spring;


import lombok.Setter;
import one.xis.server.Config;
import one.xis.server.FrontendService;
import one.xis.server.Request;
import one.xis.server.Response;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
class SpringController {

    @Setter
    private FrontendService frontendService;

    @GetMapping("/xis/config")
    Config getComponentConfig() {
        return frontendService.getConfig();
    }

    @PostMapping("/xis/page/model")
    Response getPageModel(@RequestBody Request request) {
        return frontendService.invokePageModelMethods(request);
    }

    @PostMapping("/xis/widget/model")
    Response getWidgetModel(@RequestBody Request request) {
        return frontendService.invokeWidgetModelMethods(request);
    }

    @PostMapping("/xis/page/action")
    Response onPageAction(@RequestBody Request request) {
        return frontendService.invokePageActionMethod(request);
    }

    @PostMapping("/xis/widget/action")
    Response onWidgetAction(@RequestBody Request request) {
        return frontendService.invokeWidgetActionMethod(request);
    }

    @GetMapping("/xis/page/head")
    String getPageHead(@RequestHeader("uri") String id) {
        return frontendService.getPageHead(id);
    }

    @GetMapping("/xis/page/body")
    String getPageBody(@RequestHeader("uri") String id) {
        return frontendService.getPageBody(id);
    }

    @GetMapping("/xis/page/body-attributes")
    Map<String, String> getBodyAttributes(@RequestHeader("uri") String id) {
        return frontendService.getBodyAttributes(id);
    }

    @GetMapping("/xis/widget/html/{id}")
    String getWidgetHtml(@PathVariable("id") String id) {
        return frontendService.getWidgetHtml(id);
    }
}
