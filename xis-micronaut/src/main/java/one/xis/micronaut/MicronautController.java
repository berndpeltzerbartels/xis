package one.xis.micronaut;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.*;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.server.*;

import java.util.Locale;
import java.util.Map;

@Controller
class MicronautController implements FrameworkController<HttpResponse<ServerResponse<Object>>> {

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
    public HttpResponse<ServerResponse<Object>> getPageModel(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale); // TODO is Locale a valid parameter for Micronaut ?
        var serverResponse = frontendService.processModelDataRequest(request);
        return HttpResponse.status(HttpStatus.valueOf(serverResponse.getHttpStatus())).body(serverResponse);
    }

    @Override
    @Post("/xis/widget/model")
    public HttpResponse<ServerResponse<Object>> getWidgetModel(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processModelDataRequest(request);
        return HttpResponse.status(HttpStatus.valueOf(serverResponse.getHttpStatus())).body(serverResponse);
    }

    @Override
    @Post("/xis/page/action")
    public HttpResponse<ServerResponse<Object>> onPageAction(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return HttpResponse.status(HttpStatus.valueOf(serverResponse.getHttpStatus())).body(serverResponse);
    }

    @Override
    @Post("/xis/widget/action")
    public HttpResponse<ServerResponse<Object>> onWidgetAction(@Body ClientRequest request, Locale locale) {
        request.setLocale(locale);
        var serverResponse = frontendService.processActionRequest(request);
        return HttpResponse.status(HttpStatus.valueOf(serverResponse.getHttpStatus())).body(serverResponse);
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
    @Get("/xis/widget/html")
    public String getWidgetHtml(@Header("uri") String id) {
        return frontendService.getWidgetHtml(id);
    }

    @Override
    @Get("/xis/include/html")
    public String getIncludeHtml(@Header("uri") String key) {
        return frontendService.getIncludeHtml(key);
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
