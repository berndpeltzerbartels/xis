package one.xis.spring;


import lombok.Setter;
import one.xis.server.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
class SpringController implements FrameworkController {

    @Setter
    private FrontendService frontendService;

    @Override
    @GetMapping("/xis/config")
    public Config getComponentConfig() {
        return frontendService.getConfig();
    }

    @Override
    @PostMapping("/xis/page/model")
    public Response getPageModel(@RequestBody Request request) {
        return frontendService.invokePageModelMethods(request);
    }

    @Override
    @PostMapping("/xis/widget/model")
    public Response getWidgetModel(@RequestBody Request request) {
        return frontendService.invokeWidgetModelMethods(request);
    }

    @Override
    @PostMapping("/xis/page/action")
    public Response onPageAction(@RequestBody Request request) {
        return frontendService.invokePageActionMethod(request);
    }

    @Override
    @PostMapping("/xis/widget/action")
    public Response onWidgetAction(@RequestBody Request request) {
        return frontendService.invokeWidgetActionMethod(request);
    }

    @Override
    @GetMapping("/xis/page/head")
    public String getPageHead(@RequestHeader("uri") String id) {
        return frontendService.getPageHead(id);
    }

    @Override
    @GetMapping("/xis/page/body")
    public String getPageBody(@RequestHeader("uri") String id) {
        return frontendService.getPageBody(id);
    }

    @Override
    @GetMapping("/xis/page/body-attributes")
    public Map<String, String> getBodyAttributes(@RequestHeader("uri") String id) {
        return frontendService.getBodyAttributes(id);
    }

    @Override
    @GetMapping("/xis/widget/html/{id}")
    public String getWidgetHtml(@PathVariable("id") String id) {
        return frontendService.getWidgetHtml(id);
    }
}
