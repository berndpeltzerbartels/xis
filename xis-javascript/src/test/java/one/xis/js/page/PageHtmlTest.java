package one.xis.js.page;

import one.xis.js.Javascript;
import one.xis.test.dom.Document;
import one.xis.test.dom.DomAssert;
import one.xis.test.dom.Element;
import one.xis.test.dom.Window;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static one.xis.js.JavascriptSource.*;
import static org.assertj.core.api.Assertions.assertThat;

class PageHtmlTest {
    private String javascript;
    private Document document;

    @BeforeEach
    void init() {
        javascript = Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_APP_INSTANCE);
        javascript += IOUtils.getResourceAsString("one/xis/page/PageHtmlTestMocks.js");
        javascript += "var html = new PageHtml();";
        document = Document.fromResource("index.html");
    }


    @Test
    void bindPage() throws ScriptException {
        var testScript = javascript + "html.bindPage(page);";

        JSUtil.execute(testScript, createBindings());

        DomAssert.assertAndGetRootElement(document, "html")
                .assertAndGetChildElements("head", "body")
                .andThen(elements -> {
                    var head = elements.get(0);
                    var body = elements.get(1);

                    DomAssert.assertChildElements(head, "title", "script", "script", "script", "script", "style", "script"); // the last 2 ones are read from mock
                    var children = DomAssert.assertChildElements(body, "h1", "div");
                    children.pick(0).assertTextContent("Page");
                    children.pick(1).assertTextContent("Page");

                    assertThat(body.getAttribute("class")).isEqualTo("Page"); // see mock
                });

    }

    private Map<String, Object> createBindings() {
        var head = document.getElementByTagName("head");
        var title = document.getElementByTagName("title");
        var body = document.getElementByTagName("body");

        Function<String, Element> getElementByTagName = (name) -> {
            switch (name) {
                case "head":
                    return head;
                case "title":
                    return title;
                case "body":
                    return body;
                default:
                    throw new IllegalArgumentException();
            }
        };

        var bindings = new HashMap<String, Object>();
        bindings.put("document", document);
        bindings.put("getElementByTagName", getElementByTagName);
        bindings.put("window", new Window());

        return bindings;
    }

}
