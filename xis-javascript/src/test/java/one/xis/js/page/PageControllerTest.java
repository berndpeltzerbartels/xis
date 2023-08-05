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

class PageControllerTest {

    private String javascript;
    private Document document;

    @BeforeEach
    void init() {
        javascript = Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_APP_INSTANCE);
        javascript += IOUtils.getResourceAsString("one/xis/page/PageControllerTestMocks.js");
        javascript += "var pageController = new PageController(client, pages, new Initializer(), new URLResolver(pages), new PageHtml());\n"; // client and pages are getting instantiated inside mock-code
        javascript += "pageController.setConfig(config);\n"; // config from mock-code
        document = Document.fromResource("index.html");
    }

    @Test
    void displayPageForUrl() throws ScriptException {
        var testScript = javascript + "pageController.displayPageForUrl('/page.html');";

        JSUtil.execute(testScript, createBindings());

        assertThat(document.getElementByTagName("title").innerText).isEqualTo("Page");
        DomAssert.assertAndGetChildElement(document.getElementByTagName("body"), "h1").assertTextContent("Page");
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