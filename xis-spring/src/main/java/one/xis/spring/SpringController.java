package one.xis.spring;


import lombok.Setter;
import one.xis.server.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;

@Setter
@RestController
@RequestMapping
class SpringController implements FrameworkController<ResponseEntity<ServerResponse>, HttpServletRequest> {

    private FrontendService frontendService;

    @Override
    @GetMapping("/xis/config")
    public ClientConfig getComponentConfig() {
        return frontendService.getConfig();
    }

    @Override
    @PostMapping("/xis/page/model")
    public ResponseEntity<ServerResponse> getPageModel(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/model")
    public ResponseEntity<ServerResponse> getFormModel(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processFormDataRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/model")
    public ResponseEntity<ServerResponse> getWidgetModel(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/page/action")
    public ResponseEntity<ServerResponse> onPageLinkAction(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/widget/action")
    public ResponseEntity<ServerResponse> onWidgetLinkAction(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @PostMapping("/xis/form/action")
    public ResponseEntity<ServerResponse> onFormAction(@RequestBody ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return ResponseEntity.status(serverResponse.getStatus()).body(serverResponse);
    }

    @Override
    @GetMapping("/xis/page/javascript/**")
    public String getPageJavascript(HttpServletRequest request) {
        return frontendService.getPageJavascript(request.getRequestURI());
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
    @GetMapping("/xis/widget/html")
    public String getWidgetHtml(@RequestHeader("uri") String id) {
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
