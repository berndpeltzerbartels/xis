package one.xis.js.init;

import one.xis.js.Javascript;
import one.xis.test.dom.*;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class DomAccessorTest {

    @Test
    @DisplayName("Element e2 is a child of element e1 and e2 is getting replaced by element x")
    void replaceElement1() throws ScriptException {
        var document = new DocumentImpl(new ElementImpl("root"));
        document.getDocumentElement().appendChild(document.createElement("e1"));
        document.getDocumentElement().appendChild(document.createElement("e2"));

        var js = Javascript.getScript(CLASSES, FUNCTIONS);
        js += "var accessor = new DomAccessor();";
        js += "var e2 = document.getElementsByTagName('e2').item(0);";
        js += "var x = document.createElement('x');";
        js += "accessor.replaceElement(e2, x);";
        JSUtil.execute(js, Map.of("document", document));

        var root = document.getElementByTagName("root");
        var e1 = document.getElementByTagName("e1");
        var e2 = document.getElementByTagName("e2");
        var x = document.getElementByTagName("x");

        assertThat(root).isNotNull();
        assertThat(e1).isNotNull();
        assertThat(e2).isNull();
        assertThat(x).isNotNull();

        assertThat(e1.getNextSibling()).isEqualTo(x);
        assertThat(x.getNextSibling()).isNull();

        assertThat(e1.getParentNode()).isEqualTo(root);
        assertThat(x.getParentNode()).isEqualTo(root);

        assertThat(root.getChildNodes().length).isEqualTo(2);
        assertThat(root.getChildNodes().item(0)).isEqualTo(e1);
        assertThat(root.getChildNodes().item(1)).isEqualTo(x);
    }

    @Test
    @DisplayName("Element e1 is getting replaced by element x")
    void replaceElement2() throws ScriptException {
        var document = new DocumentImpl(new ElementImpl("root"));
        document.getDocumentElement().appendChild(document.createElement("e1"));

        var js = Javascript.getScript(CLASSES, FUNCTIONS);
        js += "var accessor = new DomAccessor();";
        js += "var e1 = document.getElementsByTagName('e1').item(0);";
        js += "var x = document.createElement('x');";
        js += "accessor.replaceElement(e1, x);";
        JSUtil.execute(js, Map.of("document", document));

        var root = document.getElementByTagName("root");
        var e1 = document.getElementByTagName("e1");
        var x = document.getElementByTagName("x");

        assertThat(e1).isNull();
        assertThat(x.getNextSibling()).isNull();
        assertThat(x.getParentNode()).isEqualTo(root);

        DomAssert.assertAndGetRootElement(document, "root")
                .assertChildElements("x").assertNoChildElement("e1");
    }

    @Test
    @DisplayName("e1 has next sibling e2 and e2 is replaced by x")
    void replaceElement3() throws ScriptException {
        var document = new DocumentImpl(new ElementImpl("root"));
        var e1 = document.createElement("e1");
        var e2 = document.createElement("e2");
        var e3 = document.createElement("e3");
        e1.appendChild(e3);
        document.getDocumentElement().appendChild(e1);
        document.getDocumentElement().appendChild(e2);


        var js = Javascript.getScript(CLASSES, FUNCTIONS);
        js += "var accessor = new DomAccessor();";
        js += "var e1 = document.getElementsByTagName('e1').item(0);";
        js += "var e2 = document.getElementsByTagName('e2').item(0);";
        js += "var x = document.createElement('x');";
        js += "accessor.replaceElement(e1, x);";
        JSUtil.execute(js, Map.of("document", document));

        var root = document.getElementByTagName("root");
        e1 = document.getElementByTagName("e1");
        e2 = document.getElementByTagName("e2");
        var x = document.getElementByTagName("x");

        assertThat(root).isNotNull();
        assertThat(e1).isNull();
        assertThat(e2).isNotNull();
        assertThat(x).isNotNull();

        assertThat(x.getNextSibling()).isEqualTo(e2);
        assertThat(e2.getParentNode()).isEqualTo(root);
        assertThat(x.getParentNode()).isEqualTo(root);

        assertThat(root.getChildNodes().length).isEqualTo(2);
        assertThat(root.getChildNodes().item(0)).isEqualTo(x);
        assertThat(root.getChildNodes().item(1)).isEqualTo(e2);
    }

    @Test
    void insertParent() throws ScriptException {
        var document = new DocumentImpl(new ElementImpl("root"));
        document.getDocumentElement().appendChild(document.createElement("e2"));

        var js = Javascript.getScript(CLASSES, FUNCTIONS);
        js += "var accessor = new DomAccessor();";
        js += "var e2 = document.getElementsByTagName('e2').item(0);";
        js += "var e1 = document.createElement('e1');";
        js += "accessor.insertParent(e2, e1);";

        JSUtil.execute(js, Map.of("document", document));

        var root = document.getElementByTagName("root");
        var e1 = document.getElementByTagName("e1");
        var e2 = document.getElementByTagName("e2");

        assertThat(root.getFirstChild()).isEqualTo(e1);
        assertThat(e1.getFirstChild()).isEqualTo(e2);

    }

    @Test
    void insertChild() throws ScriptException {
        var document = Document.of("<a><b/><c/></a>");

        var js = Javascript.getScript(CLASSES, FUNCTIONS);
        js += "var accessor = new DomAccessor();";
        js += "var a = document.getElementsByTagName('a').item(0);";
        js += "var x = document.createElement('x');";
        js += "accessor.insertChild(a, x);";

        JSUtil.execute(js, Map.of("document", document));

        assertThat(document.getDocumentElement().getChildElements().stream().map(Element::getTagName)).containsExactly("x");
        assertThat(document.getElementByTagName("x").getChildElements().stream().map(Element::getTagName)).containsExactly("b", "c");

    }

}
