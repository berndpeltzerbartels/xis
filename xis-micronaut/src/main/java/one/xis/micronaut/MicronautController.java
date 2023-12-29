package one.xis.micronaut;

import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.*;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.server.*;

import java.util.Locale;
import java.util.Map;

@Controller
class MicronautController implements FrameworkController {

    @Inject
    private MicronautContextAdapter contextAdapter;

    private FrontendService frontendService;

    @PostConstruct
    void init() {
        frontendService = contextAdapter.getFrontendService();
    }

    @Override
    @Get("/xis/config")
    public ClientConfig getComponentConfig() {
        return frontendService.getConfig();
    }

    @Override
    @Post("/xis/page/model")
    public ServerResponse getPageModel(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale); // TODO is Locale a valid parameter for Micronaut ?
        return frontendService.processPageModelDataRequest(request);
    }

    @Override
    @Post("/xis/widget/model")
    public ServerResponse getWidgetModel(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale);
        return frontendService.processWidgetModelDataRequest(request);
    }

    @Override
    @Post("/xis/page/action")
    public ServerResponse onPageAction(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale);
        return frontendService.processPageActionRequest(request);
    }

    @Override
    @Post("/xis/widget/action")
    public ServerResponse onWidgetAction(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale);
        return frontendService.processWidgetActionRequest(request);
    }

    @Override
    @Get("/xis/page")
    public String getPage(@Header("uri") String id) {
        return frontendService.getPage(id);
    }

    @Override
    @Get("/xis/page/head")
    public String getPageHead(@Header("uri") String id) {
        return frontendService.getPageHead(id);
    }

    @Override
    @Get("/xis/page/body")
    public String getPageBody(@Header("uri") String id) {
        return frontendService.getPageBody(id);
    }

    @Override
    @Get("/xis/page/body-attributes")
    public Map<String, String> getBodyAttributes(@Header("uri") String id) {
        return frontendService.getBodyAttributes(id);
    }

    @Override
    @Get("/xis/widget/html/{id}")
    public String getWidgetHtml(@PathVariable("id") String id) {
        return frontendService.getWidgetHtml(id);
    }

    @Override
    @Get("/app.js")
    public String getAppJs() {
        return frontendService.getAppJs();
    }

    @Override
    @Get("/classes.js")
    public String getClassesJs() {
        return frontendService.getClassesJs();
    }

    @Override
    @Get("/main.js")
    public String getMainJs() {
        return frontendService.getMainJs();
    }

    @Override
    @Get("/functions.js")
    public String getFunctionsJs() {
        return frontendService.getFunctionsJs();
    }

}
