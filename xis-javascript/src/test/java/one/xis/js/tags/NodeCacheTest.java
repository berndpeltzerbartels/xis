package one.xis.js.tags;

import one.xis.js.Javascript;
import one.xis.test.dom.*;
import one.xis.test.js.Console;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static one.xis.js.JavascriptSource.*;
import static org.assertj.core.api.Assertions.assertThat;

class NodeCacheTest {

    @Test
    @SuppressWarnings("unchecked")
    void sizeUp() throws ScriptException {
        var js = Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_APP_INSTANCE);
        js += "var cache = new NodeCache(nodeArray, new Initializer(new DomAccessor())); cache.sizeUp(3); [cache.getChildren(0), cache.getChildren(1), cache.getChildren(2)]";
        Map<String, Object> bindings = Map.of("nodeArray", new Node[]{new Element("a"), new Element("b")}, "document", Document.of("<html/>"));

        var result = JSUtil.execute(js, bindings).as(List.class);

        assertThat(result.size()).isEqualTo(3);

        var list0 = (Node[]) result.get(0);
        var list1 = (List<Element>) result.get(1);
        var list2 = (List<Element>) result.get(2);

        assertThat(Arrays.stream(list0).map(Node::getName)).containsExactly("a", "b");
        assertThat(list1.stream().map(Node::getName)).containsExactly("a", "b");
        assertThat(list2.stream().map(Node::getName)).containsExactly("a", "b");


    }

    @Nested
    @SuppressWarnings("unchecked")
    class SizeUpRepeatInsideRepeatWitAttribute {

        private Element parentDivWithRepeat;
        private Document document;

        @BeforeEach
        void init() throws ScriptException {
            document = new Document("html");

            parentDivWithRepeat = document.createElement("div");
            parentDivWithRepeat.setAttribute("id", "divForeach1");
            parentDivWithRepeat.setAttribute("xis:repeat", "item:items");

            var divRepeat2 = document.createElement("div");
            divRepeat2.setAttribute("id", "divForeach2");
            divRepeat2.setAttribute("xis:repeat", "subItem:item.subItems");

            var span = document.createElement("span");

            parentDivWithRepeat.appendChild(divRepeat2);
            divRepeat2.appendChild(span);
            span.appendChild(new TextNode("123"));

            // Insert foreach-tags:
            var script1 = Javascript.getScript(CLASSES, FUNCTIONS);
            script1 += "var initializer = new Initializer(new DomAccessor());";
            script1 += "initializer.initialize(div);";

            // Size up cache for upper foreach-tag
            JSUtil.execute(script1, Map.of("div", parentDivWithRepeat, "console", new Console(), "document", document));
        }

        @Test
        void createTreeWithForeachTags() throws ScriptException {

            // Size up cache for upper foreach-tag

            var foreach1 = parentDivWithRepeat.parentNode;
            var script2 = Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_APP_INSTANCE);
            script2 += "var cache = new NodeCache(nodeListToArray(foreach1.childNodes), new Initializer(new DomAccessor())); ";
            script2 += "cache.sizeUp(3); ";
            script2 += "cache.cache;";

            var result = JSUtil.execute(script2, Map.of("foreach1", foreach1, "console", new Console(), "document", document));


            assertThat(result.as(List.class).size()).isEqualTo(3);

            var list = result.as(List.class);
            assertThat(((List<Object>) list.get(0)).size()).isEqualTo(1);
            assertThat(((List<Object>) list.get(1)).size()).isEqualTo(1);
            assertThat(((List<Object>) list.get(2)).size()).isEqualTo(1);

            DomAssert.assertAndGetChildElement(foreach1, "div")
                    .assertId("divForeach1")
                    .assertAndGetChildElement("xis:foreach")
                    .assertAttribute("array", "item.subItems")
                    .assertAttribute("var", "subItem")
                    .assertAndGetChildElement("div")
                    .assertId("divForeach2")
                    .assertChildElements("span");

        }
    }
}
