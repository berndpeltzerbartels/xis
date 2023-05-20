package one.xis.js.page;

import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Node;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PagesTest {

    private String javascript;
    private Document document;

    @BeforeEach
    void init() {
        javascript = IOUtils.getResourceAsString("js/page/Pages.js");
        javascript += IOUtils.getResourceAsString("js/page/Page.js");
        javascript += IOUtils.getResourceAsString("one/xis/page/PagesTestMocks.js");
        javascript += "var pages = new Pages(client, initializer);";
        document = new Document("html");
    }


    @Test
    void loadPages() throws ScriptException {
        var testScript = javascript + "pages.loadPages(config); var result = {pagesLoaded: pages.pages, initializedNodes: initializer.initializedNodes}; result";
        var compiledScript = JSUtil.compile(testScript, createBindings());

        var result = (Map<String, Object>) compiledScript.eval();
        var pages = (Map<String, Object>) result.get("pagesLoaded");

        var initializedNodes = (Collection<Node>) result.get("initializedNodes");
        assertThat(initializedNodes).hasSize(2);

        assertThat(pages).containsKey("index.html");
        var page = (Map<String, Object>) pages.get("index.html");
        assertThat(page.get("id")).isEqualTo("index.html");
        assertThat(page.get("title")).isEqualTo("Title");
        assertThat(((Node[]) page.get("headChildArray"))).hasSize(2);
        assertThat(((Node[]) page.get("bodyChildArray"))).hasSize(1);
        assertThat(page.get("bodyAttributes")).isEqualTo("{\"class\": \"test\"}");
        assertThat(page.get("id")).isEqualTo("index.html");

    }

    private Map<String, Object> createBindings() {
        var title = document.createElement("title");
        title.innerText = "Title";

        var style = document.createElement("style");
        var div = document.createElement("div");

        final List<Node[]> nodeArrays = new ArrayList<>();
        nodeArrays.add(new Node[]{title, style});
        nodeArrays.add(new Node[]{div});

        Function<Object, Node[]> nodeListToArray = list -> nodeArrays.remove(0);
        Function<Object, Boolean> isElement = Element.class::isInstance;

        var bindings = new HashMap<String, Object>();
        bindings.put("document", document);
        bindings.put("nodeListToArray", nodeListToArray);
        bindings.put("isElement", isElement);
        BiFunction<String, String, Element> htmlToElement = this::htmlToElement;
        bindings.put("htmlToElement", htmlToElement);
        return bindings;
    }

    public Element htmlToElement(String name, String content) {
        var doc = Document.of(content);
        return doc.rootNode;
    }


}
