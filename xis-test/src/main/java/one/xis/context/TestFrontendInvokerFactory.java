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
    }


    TestFrontendInvoker createInvoker() {
        try {
            compiledScript = compiledScript();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return new TestFrontendInvoker(compiledScript, appContext, document, http, localStorage);
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
                .forEach(this::activatePageController);
    }

    private void activatePageController(PageComponent component) {
        script.append(component.getJavascript())
                .append(String.format("pages.addPage('%s', new %s(client));", component.getPath(), component.getJavascriptClass()));
        if (component.getControllerClass().equals(controllerClass)) {
            script.append(String.format("var testObject = pages.getPage('%s')", component.getPath()));
        }
    }

    private void activateWidgetControllers() {
        var widgetService = appContext.getSingleton(WidgetService.class);
        appContext.getSingletons().stream().filter(o -> o.getClass().isAnnotationPresent(Widget.class))
                .map(widgetService::addWidgetConroller)
                .forEach(this::activateWidgetController);
    }

    private void activateWidgetController(WidgetComponent component) {
        script.append(component.getJavascript())
                .append(String.format("widgets.addWidget('%s', new %s(client));", component.getKey(), component.getJavascriptClass()));
        if (component.getControllerClass().equals(controllerClass)) {
            script.append(String.format("var testObject = widgets.getWidget('%s')", component.getKey()));
        }
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
