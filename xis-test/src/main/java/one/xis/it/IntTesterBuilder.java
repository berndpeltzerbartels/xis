package one.xis.it;

import one.xis.ajax.AjaxService;
import one.xis.context.AppContext;
import one.xis.context.TestContext;
import one.xis.js.JSTestUtil;
import one.xis.mocks.Document;
import one.xis.mocks.HttpClient;
import one.xis.mocks.HttpMock;
import one.xis.mocks.LocalStorage;
import one.xis.page.PageService;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.HashSet;
import java.util.Set;

public class IntTesterBuilder {
    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();
    private final Class<?> controllerClass;

    IntTesterBuilder(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        classes.add(controllerClass);
    }

    public IntTesterBuilder withClass(Class<?> c) {
        classes.add(c);
        return this;
    }

    public IntTesterBuilder withSingleton(Object singleton) {
        if (singleton instanceof Class) {
            return withClass((Class<?>) singleton);
        }
        singletons.add(singleton);
        return this;
    }

    public IntTester build() {
        var context = new TestContext("one.xis", classes, singletons);
        var document = new Document();
        var httpMock = new HttpMock(context.getSingleton(AjaxService.class));
        CompiledScript script;
        try {
            script = compiledScript(document, httpMock, context);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return new IntTester(script, context, document);
    }


    private CompiledScript compiledScript(Document document, HttpMock httpMock, AppContext appContext) throws ScriptException {
        var bindings = new SimpleBindings();
        var script = new StringBuilder();
        bindings.put("polyglot.js.allowHostAccess", true);
        addGlobalsInScript(script);
        addGlobalsToBindings(bindings, httpMock, document);
        addPageControllerScript(appContext, script);
        return JSTestUtil.compile(script.toString(), bindings);
    }

    // TODO the same fpr widgets identifies by @Widget
    private void addPageControllerScript(AppContext appContext, StringBuilder script) {
        var pageService = appContext.getSingleton(PageService.class);
        var controller = appContext.getSingleton(controllerClass);
        pageService.addPageController(controller);
        var component = pageService.getPageComponentByControllerClass(controllerClass);
        script.append(component.getJavascript());
    }

    private void addGlobalsToBindings(Bindings bindings, HttpMock httpMock, Document document) {
        bindings.put("localStorage", new LocalStorage());
        bindings.put("document", document);
        bindings.put("httpClient", new HttpClient(httpMock));
    }

    private void addGlobalsInScript(StringBuilder srcipt) {
        srcipt.append("var errorHandler = new XISErrorHandler();");
        srcipt.append("var restClient = new XISRestClient(httpClient);");
        srcipt.append("var client = new XISClient(localStorage, restClient);");
        srcipt.append("var rootPage = new XISRootPage();");
        srcipt.append("var pages = new XISPages();");
        srcipt.append("var widgets = new XISWidgets();");
        srcipt.append("var containers = new XISContainers();"); // TODO do we need this ?
        srcipt.append("var clientAttributes = new XISClientAttributes();");
        srcipt.append("var actions = new XISActions();");
    }
}
