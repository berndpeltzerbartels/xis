package one.xis.page;

import one.xis.test.js.JSScriptValidator;
import org.junit.jupiter.api.Test;

class MinimalPageAbstractMethodTest {


    @Test
    void validate() {
        var context = PageComponentTestUtil.createCompileTestContext(MinimalPage.class);
        var component = PageComponentTestUtil.createPageComponent("one/xis/page/MinimalPage.html", MinimalPage.class);
        var script = context.getSingleton(PageComponentCompiler.class).doCompile(component);

        JSScriptValidator.validate(script);
    }
}
