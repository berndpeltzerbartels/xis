// Datei: xis-test/src/test/java/one/xis/test/dom/ElementJsIntegrationTest.java

package one.xis.test.dom;

import one.xis.test.js.JSUtil;
import one.xis.utils.xml.XmlUtil;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ElementJsIntegrationTest {

    static DocumentImpl document;
    static Context context;

    @BeforeEach
    void setUp() {
        document = (DocumentImpl) Document.of("<html><body><div id=\"main\"></div></body></html>");
        context = JSUtil.context(Map.of("document", document));
    }

    @Test
    void testCreateElementSetAttributeGetAttribute() {
        JSUtil.execute("var div = document.createElement('div'); div.setAttribute('data-test', 'abc'); div.getAttribute('data-test');", context);
        var div = document.getElementById("main");
        div.setAttribute("data-test", "abc");
        assertThat(div.getAttribute("data-test")).isEqualTo("abc");
    }

    @Test
    void testGetElementById() {
        Value result = JSUtil.execute("document.getElementById('main') !== null;", context);
        assertThat(result.asBoolean()).isTrue();
    }

    @Test
    void testGetElementsByTagName() {
        Value result = JSUtil.execute("document.getElementsByTagName('div').length;", context);
        assertThat(result.asInt()).isEqualTo(1);
    }

    @Test
    void testChildNodes() {
        JSUtil.execute("var span = document.createElement('span'); document.getElementById('main').appendChild(span);", context);
        Value result = JSUtil.execute("document.getElementById('main').childNodes.length;", context);
        assertThat(result.asInt()).isEqualTo(1);
    }

    @Test
    void testInsertBefore() {
        JSUtil.execute("var div = document.getElementById('main'); var span = document.createElement('span'); var p = document.createElement('p'); div.appendChild(span); div.insertBefore(p, span);", context);
        Value result = JSUtil.execute("document.getElementById('main').childNodes.item(0).localName;", context);
        assertThat(result.asString()).isEqualTo("p");
    }

    @Test
    void testInnerText() {
        JSUtil.execute("var div = document.getElementById('main'); div.innerText = 'Hallo';", context);
        Value result = JSUtil.execute("document.getElementById('main').innerText;", context);
        assertThat(result.asString()).isEqualTo("Hallo");
    }

    @Test
    void testInnerHTML() {
        JSUtil.execute("var div = document.getElementById('main'); div.innerHTML = '<span>Text</span>';", context);
        Value result = JSUtil.execute("document.getElementById('main').innerHTML;", context);
        var doc = XmlUtil.loadDocument(result.asString());
        assertThat(doc.getDocumentElement().getTagName()).isEqualTo("span");
    }

    @Test
    void testCreateTextNodeAndNodeValue() {
        JSUtil.execute("var text = document.createTextNode('abc'); document.getElementById('main').appendChild(text);", context);
        Value result = JSUtil.execute("document.getElementById('main').childNodes.item(0).nodeValue;", context);
        assertThat(result.asString()).isEqualTo("abc");
    }

    @Test
    void testLocalName() {
        Value result = JSUtil.execute("document.getElementById('main').localName;", context);
        assertThat(result.asString()).isEqualTo("div");
    }
}