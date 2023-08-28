package one.xis.spring;


import lombok.Setter;
import one.xis.server.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
class SpringController implements FrameworkController {

    @Setter
    private FrontendService frontendService;

    @Override
    @GetMapping("/xis/config")
    public ClientConfig getComponentConfig() {
        return frontendService.getConfig();
    }

    @Override
    @PostMapping("/xis/page/model")
    public ServerResponse getPageModel(@RequestBody ClientRequest request) {
        return frontendService.processPageModelDataRequest(request);
    }

    @Override
    @PostMapping("/xis/widget/model")
    public ServerResponse getWidgetModel(@RequestBody ClientRequest request) {
        return frontendService.processWidgetModelDataRequest(request);
    }

    @Override
    @PostMapping("/xis/page/action")
    public ServerResponse onPageAction(@RequestBody ClientRequest request) {
        return frontendService.processPageActionRequest(request);
    }

    @Override
    @PostMapping("/xis/widget/action")
    public ServerResponse onWidgetAction(@RequestBody ClientRequest request) {
        return frontendService.processWidgetActionRequest(request);
    }

    @Override
    @GetMapping("/xis/page")
    public String getPage(@RequestHeader("uri") String id) {
        return frontendService.getPage(id);
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

    @Override
    @GetMapping("/app.js")
    public String getAppJs() {
        return frontendService.getAppJs();
    }

    @Override
    @GetMapping("/classes.js")
    public String getClassesJs() {
        return frontendService.getClassesJs();
    }

    @Override
    @GetMapping("/main.js")
    public String getMainJs() {
        return frontendService.getMainJs();
    }

    @Override
    @GetMapping("/functions.js")
    public String getFunctionsJs() {
        return frontendService.getFunctionsJs();
    }
}
