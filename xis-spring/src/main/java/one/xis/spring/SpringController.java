package one.xis.spring;


import lombok.RequiredArgsConstructor;
import one.xis.server.Config;
import one.xis.server.FrontendService;
import one.xis.server.Request;
import one.xis.server.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/xis")
@RequiredArgsConstructor
class SpringController {

    private final FrontendService frontendService;

    @GetMapping("/config")
    Config getComponentConfig() {
        return frontendService.getConfig();
    }

    @GetMapping("/page/model")
    Response getPageModel(@RequestBody Request request) {
        return frontendService.invokePageModelMethods(request);
    }

    @GetMapping("/widget/model")
    Response getWidgetModel(@RequestBody Request request) {
        return frontendService.invokeWidgetModelMethods(request);
    }

    @PostMapping("/page/action")
    Response onPageAction(@RequestBody Request request) {
        return frontendService.invokePageActionMethod(request);
    }

    @PostMapping("/widget/action")
    Response onWidgetAction(@RequestBody Request request) {
        return frontendService.invokeWidgetActionMethod(request);
    }

    @GetMapping("/page/html")
    String getPageHtml(@RequestParam("id") String id) {
        return frontendService.getPageHtmlResource(id);
    }

    @GetMapping("/widget/html")
    String getWidgetHtml(@RequestParam("id") String id) {
        return frontendService.getWidgetHtmlResource(id);
    }


}
