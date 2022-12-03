package one.xis.page;

import one.xis.context.AppContext;
import one.xis.js.JSTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;


//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinimalPageITCase {

    private PageService pageService;

    @BeforeEach
    void createContext() {
        var context = AppContext.getInstance("one.xis");
        pageService = context.getSingleton(PageService.class);
    }

    @Test
    void compiles() throws ScriptException {
        pageService.addPageController(new MinimalPage());
        var pageComponent = pageService.getPageComponentByPath("/MinimalPage.html");

        var script = pageComponent.getJavascript();
        var compiledScript = JSTestUtil.compileWithApi(script);
        compiledScript.eval();
    }


}