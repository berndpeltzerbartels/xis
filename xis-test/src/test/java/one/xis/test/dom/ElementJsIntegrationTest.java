// Datei: xis-test/src/test/java/one/xis/test/dom/ElementJsIntegrationTest.java

package one.xis.test.dom;

import one.xis.test.js.JSUtil;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;
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
        document = (DocumentImpl) Document.of("<html><head><title>Initial</title></head><body><div id=\"main\"></div></body></html>");
        var localStorage = new LocalStorage();
        var sessionStorage = new SessionStorage();
        var window = new Window(document.getLocation(), document, localStorage, sessionStorage, null);
        document.setDefaultView(window);
        context = JSUtil.context(Map.of(
                "document", document,
                "window", window,
                "localStorage", localStorage,
                "sessionStorage", sessionStorage
        ));
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
    void testElementAttributePropertyAssignment() {
        Value result = JSUtil.execute("""
                var div = document.createElement('div');
                div.id = 'customer-form';
                div.id;
                """, context);

        assertThat(result.asString()).isEqualTo("customer-form");
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

    @Test
    void testCommonDocumentProperties() {
        Value result = JSUtil.execute("""
                document.title = 'Changed';
                document.cookie = 'sid=123';
                document.cookies = document.cookie + '; legacy=1';
                [
                    document.body.localName,
                    document.head.localName,
                    document.title,
                    document.cookie,
                    document.cookies,
                    document.defaultView === window,
                    window.document === document,
                    window.localStorage === localStorage,
                    window.sessionStorage === sessionStorage
                ].join('|');
                """, context);

        assertThat(result.asString()).isEqualTo("body|head|Changed|sid=123; legacy=1|sid=123; legacy=1|true|true|true|true");
    }

    @Test
    void testCommonWindowProperties() {
        Value result = JSUtil.execute("""
                window.localStorage.setItem('mode', 'test');
                window.sessionStorage.setItem('step', '1');
                [
                    window.localStorage.getItem('mode'),
                    window.sessionStorage.getItem('step'),
                    window.location === document.location
                ].join('|');
                """, context);

        assertThat(result.asString()).isEqualTo("test|1|true");
    }
}
