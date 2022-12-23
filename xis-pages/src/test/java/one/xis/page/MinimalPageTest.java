package one.xis.page;

import one.xis.context.AppContext;
import one.xis.context.IntegrationTestInvoker;
import one.xis.context.XISComponent;
import one.xis.js.JSTestUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinimalPageTest {

    private PageService pageService;

    //@BeforeEach
    void createContext() {
        var context = AppContext.getInstance("one.xis");
        pageService = context.getSingleton(PageService.class);
    }

    // @Test
    void compiles() throws ScriptException, ParserConfigurationException {
        pageService.addPageController(new MinimalPage());
        var pageComponent = pageService.getPageComponentByPath("/MinimalPage.html");

        var script = pageComponent.getJavascript();

        var builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        var document = builder.newDocument();
        var bindings = new SimpleBindings();
        bindings.put("document", document);
        bindings.put("polyglot.js.allowHostAccess", true);


        var compiledScript = JSTestUtil.compileWithApi(script + " new P0();", bindings);
        var result = compiledScript.eval();
    }

    // @Test
    void returnTest() throws ParserConfigurationException, ScriptException {
        var builder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        var document = builder.newDocument();
        String script = "document.appendChild(document.createElement('bla'));";

        var bindings = new SimpleBindings();
        bindings.put("document", document);
        bindings.put("polyglot.js.allowHostAccess", true);

        var compiledScript = JSTestUtil.compile(script, bindings);
        var result = compiledScript.eval();

    }

    @Test
    void show() throws ScriptException {

        var invoker = IntegrationTestInvoker.builder(MinimalPage.class).withComponentAnnotation(XISComponent.class).build();

        invoker.invokeInit();

        var document = invoker.getDocument();
        


        /*
        var context = new IntegrationTestContext()
                .withSingletonClass(MinimalPage.class)
                .build();
        */

        // var invoker = IntegrationTestInvoker.forController(MinimalPage.class, context);

        /*
        var tester = IntegrationTestInvoker.builder(MinimalPage.class).build();

        tester.invokeShow();

        var document = tester.getDocument();
    */

    }
    

}