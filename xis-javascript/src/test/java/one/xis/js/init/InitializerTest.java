package one.xis.js.init;

import one.xis.test.dom.Document;
import one.xis.test.dom.DomAssert;
import one.xis.test.dom.TextNode;
import one.xis.test.js.Console;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InitializerTest {

    private String javascriptDefinitions;


    @BeforeAll
    void load() {
        javascriptDefinitions = IOUtils.getResourceAsString("js/init/Initializer.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/init/DomAccessor.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tag-handler/TagHandler.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tag-handler/NodeCache.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tag-handler/ForeachHandler.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tag-handler/LinkHandler.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tag-handler/ActionLinkHandler.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/Functions.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/parse/TextContentParser.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/parse/CharIterator.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/parse/TextContent.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/parse/ExpressionParser.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/parse/Tokenizer.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/parse/TreeParser.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/parse/TokenLinker.js");
    }

    @Test
    void elementsAndTextNodeEvaluated() throws ScriptException {
        var document = Document.of("<a><b/><c><d>${bla}</d></c></a>");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(document.rootNode);";

        var compiledScript = JSUtil.compile(script, Map.of("document", document, "console", new Console()));
        compiledScript.eval();

        assertThat(document.getElementByTagName("d").firstChild).isNotNull();
        assertThat(document.getElementByTagName("d").firstChild).isInstanceOf(TextNode.class);
        assertThat(((TextNode) document.getElementByTagName("d").firstChild)._expression).isNotNull();
    }

    @Test
    void repeatAttribute() throws ScriptException {
        var document = Document.of("<div><span xis:repeat=\"item:items\"></span></div>");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(document.rootNode);";

        var compiledScript = JSUtil.compile(script, Map.of("document", document, "console", new Console()));
        compiledScript.eval();

        assertThat(document.rootNode.getChildElementNames()).containsExactly("xis:foreach");

        var foreach = document.getElementByTagName("xis:foreach");
        assertThat(foreach.getChildElementNames()).containsExactly("span");
        assertThat(foreach.getAttribute("array")).isEqualTo("items");
        assertThat(foreach.getAttribute("var")).isEqualTo("item");
    }

    @Test
    void foreachAttribute() throws ScriptException {
        var document = Document.of("<div xis:foreach=\"item:items\"><span></span></div>");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(document.rootNode);";

        JSUtil.execute(script, Map.of("document", document, "console", new Console()));

        assertThat(document.rootNode.getChildElementNames()).containsExactly("xis:foreach");

        var foreach = document.getElementByTagName("xis:foreach");
        assertThat(foreach.getChildElementNames()).containsExactly("span");
        assertThat(foreach.getAttribute("array")).isEqualTo("items");
        assertThat(foreach.getAttribute("var")).isEqualTo("item");
        assertThat(((Map<String, Object>) foreach._handler).get("type")).isEqualTo("foreach-handler");
    }


    @Test
    @DisplayName("Repeat-attribute and foreach with foreach as root in result")
    void repeatAndForeach() throws ScriptException {
        var document = new Document("html");
        var divForeach = document.createElement("div");
        divForeach.setAttribute("xis:repeat", "array1:items");

        var foreach = document.createElement("xis:foreach");
        foreach.setAttribute("array", "array1");
        foreach.setAttribute("var", "value");

        var span = document.createElement("span");

        divForeach.appendChild(foreach);
        foreach.appendChild(span);
        span.appendChild(new TextNode("123"));

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(div);";

        JSUtil.execute(script, Map.of("div", divForeach, "console", new Console(), "document", document));

        DomAssert.assertAndGetParentElement(span, "xis:foreach")
                .assertAndGetParentElement("div")
                .assertAndGetParentElement("xis:foreach")
                .assertAttribute("array", "items")
                .assertAttribute("var", "array1");

    }

    @Test
    @DisplayName("Repeat-attribute and foreach with newly created foreach nested in root")
    void repeatAndForeach2() throws ScriptException {
        var document = new Document("html");
        var div1 = document.createElement("div");
        var div2 = document.createElement("div");
        div2.setAttribute("xis:repeat", "item1:items1");

        var foreach = document.createElement("xis:foreach");
        foreach.setAttribute("array", "items2");
        foreach.setAttribute("var", "item2");

        var span = document.createElement("span");
        div1.appendChild(div2);
        div2.appendChild(foreach);
        foreach.appendChild(span);
        span.appendChild(new TextNode("123"));

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(div1);";

        JSUtil.execute(script, Map.of("div1", div1, "console", new Console(), "document", document));

        DomAssert.assertAndGetParentElement(span, "xis:foreach")
                .assertAttribute("array", "items2")
                .assertAttribute("var", "item2")
                .assertAndGetParentElement("div")
                .assertAndGetParentElement("xis:foreach")
                .assertAttribute("array", "items1")
                .assertAttribute("var", "item1");


    }

    @Test
    @DisplayName("Element has repeat-attribute and for-attribute at the same time")
    void repeatAndForAttributes() throws ScriptException {
        var document = new Document("html");
        var div = document.createElement("div");
        div.setAttribute("xis:repeat", "item1:items1");
        div.setAttribute("xis:foreach", "item2:items2");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(div);";

        var compiledScript = JSUtil.compile(script, Map.of("div", div, "console", new Console(), "document", document));
        compiledScript.eval();


        DomAssert.assertAndGetParentElement(div, "xis:foreach")
                .assertAttribute("var", "item1")
                .assertAttribute("array", "items1")
                .assertAndGetChildElement("div")
                .assertAndGetChildElement("xis:foreach")
                .assertAttribute("var", "item2")
                .assertAttribute("array", "items2");


    }

    @Test
    void textNode() throws ScriptException {
        var document = Document.of("<html><head><title>The title is ${title}</title></head></html>");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(head);";

        var head = document.getElementByTagName("head");

        var compiledScript = JSUtil.compile(script, Map.of("head", head, "console", new Console(), "document", document));
        compiledScript.eval();

        var title = document.getElementByTagName("title");
        assertThat(title.getChildNodes().length).isEqualTo(1);
        var textNode = (TextNode) title.childNodes.item(0);
        assertThat(textNode._expression).isNotNull();

    }

    @Test
    void pageLink() throws ScriptException {
        var document = Document.of("<html><body><a xis:page=\"/test.html\">test</a></body></html>");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(a);";

        var a = document.getElementByTagName("a");

        JSUtil.execute(script, Map.of("a", a, "console", new Console(), "document", document));

        var handler = (Map<String, Object>) a._handler;
        assertThat(handler).isNotNull();
        assertThat(handler.get("type")).isEqualTo("link-handler");
    }

    @Test
    void actionLink1() throws ScriptException {
        var document = Document.of("<html><body><a xis:action=\"test-action\">test</a></body></html>");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(a);";

        var a = document.getElementByTagName("a");

        JSUtil.execute(script, Map.of("a", a, "console", new Console(), "document", document));

        var handler = (Map<String, Object>) a._handler;
        assertThat(handler).isNotNull();
        assertThat(handler.get("type")).isEqualTo("action-link-handler");
    }

    /*
     * Similar to previous test, but element type is "xis:a" intead of "a" and attribute is "xis:action" instead of "action"
     */
    @Test
    void actionLink2() throws ScriptException {
        var document = Document.of("<html><body><xis:a action=\"test-action\">test</xis:a></body></html>");


        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(a);";

        var a1 = document.getElementByTagName("xis:a");

        JSUtil.execute(script, Map.of("a", a1, "console", new Console(), "document", document));

        var a2 = document.getElementByTagName("a");
        var handler = (Map<String, Object>) a2._handler;
        assertThat(handler).isNotNull();
        assertThat(handler.get("type")).isEqualTo("action-link-handler");
    }


    /*
    @Test
    void compositeTagHandler() throws ScriptException {
        var document = Document.of("<html><body><a repeat=\"x:y\" page-link=\"/test.html\">test</a></body></html>"); // repeat ans page-link in sin tag

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(a);";

        var a = document.getElementByTagName("a");

        JSUtil.execute(script, Map.of("a", a, "console", new Console(), "document", document));

        var handler = (Map<String, Object>) a._handler;
        assertThat(handler).isNotNull();
        assertThat(handler.get("type")).isEqualTo("page-link-handler");
    }

     */

}

