package one.xis.js.tags;

import one.xis.js.Javascript;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Window;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.js.JavascriptSource.*;
import static org.assertj.core.api.Assertions.assertThat;

class ForeachHandlerTest {

    private Document document;
    private Element foreach;


    @BeforeEach
    void initDocument() {
        document = Document.of("<html><body/></html>");

        var body = document.getElementByTagName("body");
        var div1 = document.createElement("div");
        var div2 = document.createElement("div");

        div1.setAttribute("class", "div1");
        div2.setAttribute("class", "div2");

        foreach = new Element("xis:foreach");
        foreach.setAttribute("array", "a.b.c");
        foreach.setAttribute("var", "x");
        foreach.appendChild(div1);
        foreach.appendChild(div2);

        body.appendChild(foreach);
    }

    @Test
    void iterate() throws ScriptException {
        var script = Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_APP_INSTANCE);
        script += "var data = new Data({\"a\": {\"b\": {\"c\": [{\"id\": 1, \"title\": \"title1\"}, {\"id\": 2, \"title\": \"title2\"}, {\"id\": 3, \"title\": \"title3\"}]}}});";
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "var handler = new ForeachHandler(foreach, initializer);";
        script += "handler.refresh(data);";

        JSUtil.execute(script, Map.of("foreach", foreach, "document", document, "window", new Window()));

        var childElementClasses = foreach.getChildElements().stream()
                .map(Element::getCssClasses)
                .map(cl -> String.join(" ", cl))
                .collect(Collectors.toList());

        assertThat(foreach.getChildNodes().length).isEqualTo(6);

        assertThat(childElementClasses.get(0)).isEqualTo("div1");
        assertThat(childElementClasses.get(1)).isEqualTo("div2");
        assertThat(childElementClasses.get(2)).isEqualTo("div1");
        assertThat(childElementClasses.get(3)).isEqualTo("div2");
        assertThat(childElementClasses.get(4)).isEqualTo("div1");
        assertThat(childElementClasses.get(5)).isEqualTo("div2");
    }

    @Test
    void decreaseElementCount() throws ScriptException {
        var script = Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_APP_INSTANCE);
        script += "var data1 = new Data({\"a\": {\"b\": {\"c\": [{\"id\": 1, \"title\": \"title1\"}, {\"id\": 2, \"title\": \"title2\"}, {\"id\": 3, \"title\": \"title3\"}]}}});";
        script += "var data2 = new Data({\"a\": {\"b\": {\"c\": [{\"id\": 1, \"title\": \"title1\"}]}}});";
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "var handler = new ForeachHandler(foreach, initializer);";
        script += "handler.refresh(data1);"; // length = 3
        script += "handler.refresh(data2);";// length = 1
        JSUtil.execute(script, Map.of("foreach", foreach, "document", document, "window", new Window()));

        assertThat(foreach.getChildNodes().length).isEqualTo(2); // 2 subtags for every array-element
    }

}
