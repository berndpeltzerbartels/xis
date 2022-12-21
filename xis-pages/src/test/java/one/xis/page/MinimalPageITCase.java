package one.xis.page;

import one.xis.context.AppContext;
import one.xis.context.TestFrontendInvoker;
import one.xis.js.JSTestUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinimalPageITCase {

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
    void show() {
        var tester = TestFrontendInvoker.builder(MinimalPage.class).build();

        tester.invokeShow();

        var document = tester.getDocument();


    }


    class Document {

    }

}