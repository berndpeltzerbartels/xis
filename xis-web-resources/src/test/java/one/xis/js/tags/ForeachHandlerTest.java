package one.xis.js.tags;

import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

class ForeachHandlerTest {

    @Test
    void foreach() {
        System.out.println("*************************************å");
        var document = new Document(new Element("html"));
        var body = document.createElement("body");
        var div1 = document.createElement("div");
        var div2 = document.createElement("div");

        div1.appendChild(document.createTextNode("${x.id}"));
        div2.appendChild(document.createTextNode("${x.title}"));

        var foreach = new Element("xis:foreach");
        foreach.setAttribute("array", "a.b.c");
        foreach.setAttribute("var", "x");
        document.rootNode.appendChild(body);
        body.appendChild(foreach);
        foreach.appendChild(div1);
        foreach.appendChild(div2);

        var js = IOUtils.getResourceAsString("js/Data.js");
        js += IOUtils.getResourceAsString("js/tags/ForeachHandler.js");
        js += IOUtils.getResourceAsString("js/tags/NodeCache.js");
        js += IOUtils.getResourceAsString("js/init/Initializer.js");

        js += "var data = new Data({\"a\": {\"b\": {\"c\": [{\"id\": 1, \"title\": \"title1\"}, {\"id\": 2, \"title\": \"title2\"}, {\"id\": 3, \"title\": \"title3\"}]}}});";
        js += "var handler = new ForeachHandler(tag);";
        js += "handler.refresh(data)";

//        JSUtil.compile(js).eval();


    }

}
