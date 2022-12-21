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

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class TestFrontendInvokerFactory {

    private final AppContext appContext;
    private final Class<?> controllerClass;
    private final Document document;
    private final LocalStorage localStorage;
    private final HttpMock http;
    private final StringBuilder script;
    private CompiledScript compiledScript;


    TestFrontendInvokerFactory(Class<?> controllerClass, AppContext appContext) {
        this.appContext = appContext;
        this.controllerClass = controllerClass;
        this.document = new Document();
        this.localStorage = new LocalStorage();
        this.http = new HttpMock(appContext.getSingleton(AjaxService.class));
        this.script = new StringBuilder();
        init();
    }


    public TestFrontendInvoker invoker(Class<?> controllerClass) {
        try {
            compiledScript = compiledScript();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }

    }

    public void reset() {
        init();
    }


    private void init() {

    }


    private CompiledScript compiledScript() throws ScriptException {
        var bindings = new SimpleBindings();
        var script = new StringBuilder();
        bindings.put("polyglot.js.allowHostAccess", true);
        addGlobalsInScript(script);
        addGlobalsToBindings(bindings);
        activatePageControllers();
        activateWidgetControllers();
        return JSTestUtil.compile(script.toString(), bindings);
    }


    private void activatePageControllers() {
        var pageService = appContext.getSingleton(PageService.class);
        appContext.getSingletons().stream().filter(o -> o.getClass().isAnnotationPresent(Page.class))
                .map(pageService::addPageController)
                .map(PageComponent::getJavascript)
                .forEach(script::append);
    }

    private void activateWidgetControllers() {
        var widgetService = appContext.getSingleton(WidgetService.class);
        appContext.getSingletons().stream().filter(o -> o.getClass().isAnnotationPresent(Widget.class))
                .map(widgetService::addWidgetConroller)
                .map(WidgetComponent::getJavascript)
                .forEach(script::append);
    }

    private void addGlobalsToBindings(Bindings bindings) {
        bindings.put("localStorage", localStorage);
        bindings.put("document", document);
        bindings.put("httpClient", new HttpClient(http));
    }

    private void addGlobalsInScript(StringBuilder srcipt) {
        srcipt.append("var errorHandler = new XISErrorHandler();");
        srcipt.append("var restClient = new XISRestClient(httpClient);");
        srcipt.append("var client = new XISClient(localStorage, restClient);");
        srcipt.append("var rootPage = new XISRootPage();");
        srcipt.append("var widgets = new XISWidgets();");
        srcipt.append("var pages = new XISPages();");
        srcipt.append("var containers = new XISContainers();"); // TODO do we need this ?
        srcipt.append("var clientAttributes = new XISClientAttributes();");
        srcipt.append("var actions = new XISActions();");
    }
}
