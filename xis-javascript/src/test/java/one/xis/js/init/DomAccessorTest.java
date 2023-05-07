package one.xis.js.init;

import one.xis.test.dom.Document;
import one.xis.test.dom.DomAssert;
import one.xis.test.dom.Element;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DomAccessorTest {

    @Test
    void replaceElement1() throws ScriptException {
        var document = new Document(new Element("root"));
        document.rootNode.appendChild(document.createElement("e1"));
        document.rootNode.appendChild(document.createElement("e2"));

        var js = IOUtils.getResourceAsString("js/init/DomAccessor.js");
        js += "var accessor = new DomAccessor();";
        js += "var e2 = document.getElementsByTagName('e2').item(0);";
        js += "var x = document.createElement('x');";
        js += "accessor.replaceElement(e2, x);";
        var script = JSUtil.compile(js, Map.of("document", document));
        script.eval();

        var root = document.getElementByTagName("root");
        var e1 = document.getElementByTagName("e1");
        var e2 = document.getElementByTagName("e2");
        var x = document.getElementByTagName("x");

        assertThat(root).isNotNull();
        assertThat(e1).isNotNull();
        assertThat(e2).isNull();
        assertThat(x).isNotNull();

        assertThat(e1.nextSibling).isEqualTo(x);
        assertThat(x.nextSibling).isNull();

        assertThat(e1.parentNode).isEqualTo(root);
        assertThat(x.parentNode).isEqualTo(root);

        assertThat(root.childNodes.length).isEqualTo(2);
        assertThat(root.childNodes.item(0)).isEqualTo(e1);
        assertThat(root.childNodes.item(1)).isEqualTo(x);
    }

    @Test
    void replaceElement2() throws ScriptException {
        var document = new Document(new Element("root"));
        document.rootNode.appendChild(document.createElement("e1"));

        var js = IOUtils.getResourceAsString("js/init/DomAccessor.js");
        js += "var accessor = new DomAccessor();";
        js += "var e1 = document.getElementsByTagName('e1').item(0);";
        js += "var x = document.createElement('x');";
        js += "accessor.replaceElement(e1, x);";
        var script = JSUtil.compile(js, Map.of("document", document));
        script.eval();

        var root = document.getElementByTagName("root");
        var e1 = document.getElementByTagName("e1");
        var x = document.getElementByTagName("x");

        assertThat(e1).isNull();
        assertThat(x.nextSibling).isNull();
        assertThat(x.parentNode).isEqualTo(root);

        DomAssert.assertAndGetRootElement(document, "root")
                .assertChildElements("x").assertNoChildElement("e1");
    }

    @Test
    void insertParent() throws ScriptException {
        var document = new Document(new Element("root"));
        document.rootNode.appendChild(document.createElement("e2"));

        var js = IOUtils.getResourceAsString("js/init/DomAccessor.js");
        js += "var accessor = new DomAccessor();";
        js += "var e2 = document.getElementsByTagName('e2').item(0);";
        js += "var e1 = document.createElement('e1');";
        js += "accessor.insertParent(e2, e1);";

        var script = JSUtil.compile(js, Map.of("document", document));
        script.eval();

        var root = document.getElementByTagName("root");
        var e1 = document.getElementByTagName("e1");
        var e2 = document.getElementByTagName("e2");

        assertThat(root.firstChild).isEqualTo(e1);
        assertThat(e1.firstChild).isEqualTo(e2);

    }

    @Test
    void insertChild() throws ScriptException {
        var document = Document.of("<a><b/><c/></a>");

        var js = IOUtils.getResourceAsString("js/init/DomAccessor.js");
        js += IOUtils.getResourceAsString("js/Functions.js"); // nodeListToArray(nodeList) is used in DomParser
        js += "var accessor = new DomAccessor();";
        js += "var a = document.getElementsByTagName('a').item(0);";
        js += "var x = document.createElement('x');";
        js += "accessor.insertChild(a, x);";

        JSUtil.compile(js, Map.of("document", document)).eval();

        assertThat(document.rootNode.getChildElementNames()).containsExactly("x");
        assertThat(document.getElementByTagName("x").getChildElementNames()).containsExactly("b", "c");

    }

}
