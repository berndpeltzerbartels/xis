package one.xis.context;

import one.xis.Page;
import one.xis.Widget;
import one.xis.ajax.AjaxService;
import one.xis.context.mocks.Document;
import one.xis.context.mocks.HttpClient;
import one.xis.context.mocks.HttpMock;
import one.xis.context.mocks.LocalStorage;
import one.xis.js.JSTestUtil;
import one.xis.page.PageComponent;
import one.xis.page.PageService;
import one.xis.widget.WidgetComponent;
import one.xis.widget.WidgetService;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class IntegrationTestInvokerBuilder implements ContextBuilder<IntegrationTestInvoker> {

    private final Class<?> controllerClass;
    private final Document document;
    private final LocalStorage localStorage;
    private final StringBuilder script;
    private final Map<String, Object> bindings;
    private final TestContextBuilder testContextBuiler;

    IntegrationTestInvokerBuilder(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        this.document = new Document();
        this.localStorage = new LocalStorage();
        this.script = new StringBuilder();
        this.bindings = new HashMap<>();
        this.testContextBuiler = new TestContextBuilder();
        this.testContextBuiler.withPackage("one.xis");
        this.testContextBuiler.withSingletonClasses(controllerClass);
    }

    @Override
    public IntegrationTestInvoker build() {
        var appContext = testContextBuiler.build();
        var http = new HttpMock(appContext.getSingleton(AjaxService.class));
        evaluateScript(appContext, http);
        return new IntegrationTestInvoker(script.toString(), bindings, appContext, document, http, localStorage);
    }

    @Override
    public ContextBuilder<IntegrationTestInvoker> withSingletonClass(Class<?> clazz) {
        testContextBuiler.withSingletonClass(clazz);
        return this;
    }

    @Override
    public ContextBuilder<IntegrationTestInvoker> withMock(Object singleton) {
        testContextBuiler.withMock(singleton);
        return this;
    }

    @Override
    public ContextBuilder<IntegrationTestInvoker> withPackage(String pack) {
        testContextBuiler.withPackage(pack);
        return this;
    }

    @Override
    public ContextBuilder<IntegrationTestInvoker> withComponentAnnotation(Class<? extends Annotation> componentAnnotation) {
        testContextBuiler.withComponentAnnotation(componentAnnotation);
        return this;
    }

    @Override
    public ContextBuilder<IntegrationTestInvoker> withDependencyFieldAnnotation(Class<? extends Annotation> dependencyFieldAnnotation) {
        testContextBuiler.withDependencyFieldAnnotation(dependencyFieldAnnotation);
        return this;
    }

    @Override
    public ContextBuilder<IntegrationTestInvoker> withBeanInitAnnotation(Class<? extends Annotation> beanInitAnnotation) {
        testContextBuiler.withBeanInitAnnotation(beanInitAnnotation);
        return this;
    }

    private void evaluateScript(AppContext context, HttpMock httpMock) {
        addApi();
        addGlobalsInScript();
        addGlobalsToBindings(httpMock);
        activatePageControllers(context);
        activateWidgetControllers(context);
        addInitialMethodCall();
    }

    private void addInitialMethodCall() {
        script.append("rootPage.onMainPageLoaded();\n");
    }

    private void addApi() {
        script.append(JSTestUtil.getApiResourceAsString());
    }


    private void activatePageControllers(AppContext appContext) {
        var pageService = appContext.getSingleton(PageService.class);
        appContext.getSingletons().stream().filter(o -> o.getClass().isAnnotationPresent(Page.class))
                .map(pageService::addPageController)
                .forEach(this::activatePageController);
    }

    private void activatePageController(PageComponent component) {
        script.append(component.getJavascript())
                .append(String.format("pages.addPage('%s', new %s(client));", component.getJavascriptClass(), component.getJavascriptClass()));
        if (component.getControllerClass().equals(controllerClass)) {
            script.append(String.format("var %s = pages.getPage('%s');", IntegrationTestInvoker.TEXT_OBJECT_VAR_NAME, component.getJavascriptClass()));
        }
    }

    private void activateWidgetControllers(AppContext appContext) {
        var widgetService = appContext.getSingleton(WidgetService.class);
        appContext.getSingletons().stream().filter(o -> o.getClass().isAnnotationPresent(Widget.class))
                .map(widgetService::addWidgetConroller)
                .forEach(this::activateWidgetController);
    }

    private void activateWidgetController(WidgetComponent component) {
        script.append(component.getJavascript())
                .append(String.format("widgets.addWidget('%s', new %s(client));", component.getKey(), component.getJavascriptClass()));
        if (component.getControllerClass().equals(controllerClass)) {
            script.append(String.format("var %s = widgets.getWidget('%s');", IntegrationTestInvoker.TEXT_OBJECT_VAR_NAME, component.getKey()));
        }
    }

    private void addGlobalsToBindings(HttpMock http) {
        bindings.put("localStorage", localStorage);
        bindings.put("document", document);
        bindings.put("httpClient", new HttpClient(http));
    }

    private void addGlobalsInScript() {
        script.append("var errorHandler = new XISErrorHandler();\n");
        script.append("var restClient = new XISRestClient(httpClient);\n");
        script.append("var client = new XISClient(localStorage, restClient);\n");
        script.append("var rootPage = new XISRootPage();\n");
        script.append("var widgets = new XISWidgets();\n");
        script.append("var pages = new XISPages();\n");
        script.append("var clientAttributes = new XISClientAttributes();\n");
        script.append("var actions = new XISActions();\n");
    }
}
