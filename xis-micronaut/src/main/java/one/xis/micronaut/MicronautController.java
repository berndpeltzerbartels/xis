package one.xis.micronaut;

import io.micronaut.context.BeanContext;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.AppContextBuilder;
import one.xis.server.Config;
import one.xis.server.FrontendService;
import one.xis.server.Request;
import one.xis.server.Response;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
class MicronautController {

    @Inject
    private BeanContext beanContext;

    private FrontendService frontendService;


    @PostConstruct
    void init() {
        frontendService = frontendService();
    }

    @Get(uris = {"/", "", "*.html"})
    HttpResponse<String> getPage() {
        String html = frontendService.getRootPageHtml();
        return HttpResponse.ok(html)//
                .contentType("text/html")//
                .characterEncoding("utf-8")//
                .contentLength(html.length());
    }


    @Get("/xis/config")
    public Config getComponentConfig() {
        return frontendService.getConfig();
    }


    @Post("/xis/page/model")
    public Response getPageModel(@Body Request request) {
        return frontendService.invokePageModelMethods(request);
    }


    @Post("/xis/widget/model")
    public Response getWidgetModel(@Body Request request) {
        return frontendService.invokeWidgetModelMethods(request);
    }


    @Post("/xis/page/action")
    public Response onPageAction(@Body Request request) {
        return frontendService.invokePageActionMethod(request);
    }


    @Post("/xis/widget/action")
    public Response onWidgetAction(@Body Request request) {
        return frontendService.invokeWidgetActionMethod(request);
    }


    @Get("/xis/page")
    public String getPage(@Header("uri") String id) {
        return frontendService.getPage(id);
    }


    @Get("/xis/page/head")
    public String getPageHead(@Header("uri") String id) {
        return frontendService.getPageHead(id);
    }


    @Get("/xis/page/body")
    public String getPageBody(@Header("uri") String id) {
        return frontendService.getPageBody(id);
    }


    @Get("/xis/page/body-attributes")
    public Map<String, String> getBodyAttributes(@Header("uri") String id) {
        return frontendService.getBodyAttributes(id);
    }


    @Get("/xis/widget/html/{id}")
    public String getWidgetHtml(@PathVariable("id") String id) {
        return frontendService.getWidgetHtml(id);
    }


    @Get("/app.js")
    public String getAppJs() {
        return frontendService.getAppJs();
    }


    @Get("/classes.js")
    public String getClassesJs() {
        return frontendService.getClassesJs();
    }


    @Get("/main.js")
    public String getMainJs() {
        return frontendService.getMainJs();
    }


    @Get("/functions.js")
    public String getFunctionsJs() {
        return frontendService.getFunctionsJs();
    }

    private FrontendService frontendService() {
        var context = AppContextBuilder.createInstance()
                .withSingletons(findControllers())
                .withPackage("one.xis")
                .build();
        return context.getSingleton(FrontendService.class);
    }

    private Collection<Object> findControllers() {
        var controllers = new HashSet<>();
        controllers.addAll(findAnnotatedBeans(Page.class));
        controllers.addAll(findAnnotatedBeans(Widget.class));
        return controllers;
    }

    private <A extends Annotation> Collection<Object> findAnnotatedBeans(Class<A> annotationType) {
        return beanContext.getBeanDefinitions(Qualifiers.byStereotype(annotationType)).stream()//
                .map(BeanDefinition::getBeanType)
                .map(beanContext::getBean)
                .collect(Collectors.toSet());
    }
}
