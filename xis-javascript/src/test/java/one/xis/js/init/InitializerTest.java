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

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InitializerTest {

    private String javascriptDefinitions;


    @BeforeAll
    void load() {
        javascriptDefinitions = IOUtils.getResourceAsString("js/init/Initializer.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/init/DomAccessor.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tags/TagHandler.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tags/NodeCache.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tags/ForeachHandler.js");
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
        var document = Document.of("<div><span repeat=\"item:items\"></span></div>");

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
    void forAttribute() throws ScriptException {
        var document = Document.of("<div for=\"item:items\"><span></span></div>");

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
    @DisplayName("Repeat-attribute and foreach with foreach as root in result")
    void repeatAndForeach() throws ScriptException {
        var document = new Document("html");
        var div = document.createElement("div");
        div.setAttribute("repeat", "array1:items");

        var foreach = document.createElement("xis:foreach");
        foreach.setAttribute("array", "array1");
        foreach.setAttribute("var", "value");

        var span = document.createElement("span");

        div.appendChild(foreach);
        foreach.appendChild(span);
        span.appendChild(new TextNode("123"));

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(div);";

        var compiledScript = JSUtil.compile(script, Map.of("div", div, "console", new Console(), "document", document));
        compiledScript.eval();


        DomAssert.assertAndGetParentElement(span, "xis:foreach")
                .assertAndGetParentElement("div")
                .assertAndGetParentElement("xis:foreach")
                .assertAttribute("array", "items")
                .assertAttribute("var", "array1");

    }

    @Test
    @DisplayName("Repeat-attribute and foreach with newly created foreach nestend in root")
    void repeatAndForeach2() throws ScriptException {
        var document = new Document("html");
        var div1 = document.createElement("div");
        var div2 = document.createElement("div");
        div2.setAttribute("repeat", "array1:items");

        var foreach = document.createElement("xis:foreach");
        foreach.setAttribute("array", "array1");
        foreach.setAttribute("var", "value");

        var span = document.createElement("span");
        div1.appendChild(div2);
        div2.appendChild(foreach);
        foreach.appendChild(span);
        span.appendChild(new TextNode("123"));

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(div1);";

        var compiledScript = JSUtil.compile(script, Map.of("div1", div1, "console", new Console(), "document", document));
        compiledScript.eval();


        DomAssert.assertAndGetParentElement(span, "xis:foreach")
                .assertAndGetParentElement("div")
                .assertAndGetParentElement("xis:foreach")
                .assertAttribute("array", "items")
                .assertAttribute("var", "array1");

    }

    @Test
    @DisplayName("Element has repeat-attribute and for-attribute at the same time")
    void repeatAndFor() throws ScriptException {
        var document = new Document("html");
        var div = document.createElement("div");
        div.setAttribute("repeat", "item1:items1");
        div.setAttribute("for", "item2:items2");

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

}

