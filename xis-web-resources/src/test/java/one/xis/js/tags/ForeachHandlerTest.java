package one.xis.js.tags;

import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.js.Debug;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ForeachHandlerTest {

    @Test
    void foreach() throws ScriptException {
        var document = Document.of("<html><body/></html>");
        var body = document.getElementByTagName("body");
        var div1 = document.createElement("div");
        var div2 = document.createElement("div");
        div1.setAttribute("id", "div1");
        div2.setAttribute("id", "div2");
        var foreach = new Element("xis:foreach");
        foreach.setAttribute("array", "a.b.c");
        foreach.setAttribute("var", "x");
        body.appendChild(foreach);
        foreach.appendChild(div1);
        foreach.appendChild(div2);

        var js = IOUtils.getResourceAsString("js/Data.js");
        js += IOUtils.getResourceAsString("js/tags/TagHandler.js");
        js += IOUtils.getResourceAsString("js/tags/ForeachHandler.js");
        js += IOUtils.getResourceAsString("js/tags/NodeCache.js");
        js += IOUtils.getResourceAsString("js/init/Initializer.js");
        js += IOUtils.getResourceAsString("js/Functions.js");

        var bindings = Map.of("foreach", foreach, "debug", new Debug());

        js += "var data = new Data({\"a\": {\"b\": {\"c\": [{\"id\": 1, \"title\": \"title1\"}, {\"id\": 2, \"title\": \"title2\"}, {\"id\": 3, \"title\": \"title3\"}]}}});";
        js += "var handler = new ForeachHandler(foreach);";
        js += "handler.refresh(data);";

        JSUtil.execute(js, bindings);


        assertThat(foreach.getChildNodes().length).isEqualTo(6);


//        JSUtil.compile(js).eval();


    }

}
