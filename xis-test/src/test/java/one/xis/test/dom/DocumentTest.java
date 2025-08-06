package one.xis.test.dom;

import one.xis.resource.Resources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

    @Test
    @DisplayName("Create document from source")
    void of1() {
        var html = "<html><head><title>Title</title></head><body><div>123</div><div/></body></html>";
        var document = Document.of(html);

        assertThat(document.rootNode.localName).isEqualTo("html");
        assertThat(document.rootNode.getChildList().stream().map(Node::getName)).containsExactly("head", "body");

        var head = document.getElementByTagName("head");
        assertThat(head.getChildList().size()).isEqualTo(1);
        assertThat(head.childElement(0).localName).isEqualTo("title");

        var title = document.getElementByTagName("title");
        assertThat(title.getTextContent()).isEqualTo("Title");

        var body = document.getElementByTagName("body");
        assertThat(body.getChildList().size()).isEqualTo(2);

        var divs = body.getChildElementsByName("div");
        var div1 = divs.get(0);
        var div2 = divs.get(1);

        assertThat(div1.getChildList().size()).isEqualTo(1);
        assertThat(div2.getChildList()).isEmpty();
        assertThat(div1.getChildList().get(0)).isInstanceOf(TextNode.class);
        assertThat(div1.getTextContent()).isEqualTo("123");

    }

    @Test
    @DisplayName("Create document from index.html")
    void of2() {
        var html = new Resources().getByPath("/index.html").getContent();
        var document = Document.of(html);

        assertThat(document.rootNode.localName).isEqualTo("html");
        assertThat(document.rootNode.getChildElementNames()).containsExactly("head", "body");

        var head = document.getElementByTagName("head");
        assertThat(head.getChildElementNames()).contains("title", "script");

        var body = document.getElementByTagName("body");
        assertThat(body.getChildList()).isEmpty();
        assertThat(body.getAttribute("onload")).isEqualTo("main()");
    }

    @Test
    @DisplayName("Create document from simple source")
    void of3() {
        var document = Document.of("<a><b/><c><d/></c></a>");

        assertThat(document.getElementByTagName("a")).isNotNull();
        assertThat(document.getElementByTagName("b")).isNotNull();
        assertThat(document.getElementByTagName("c")).isNotNull();
        assertThat(document.getElementByTagName("d")).isNotNull();
    }

    @Test
    @DisplayName("Attributes from child in source are present in document-mock")
    void attributes1() {
        var xml = "<a><e x=\"1\" y=\"2\"/></a>";
        var document = Document.of(xml);

        var e = document.getElementByTagName("e");
        assertThat(e.getAttribute("x")).isEqualTo("1");
        assertThat(e.getAttribute("y")).isEqualTo("2");
    }


    @Test
    @DisplayName("Attributes from root-element in source are present in document-mock")
    void attributes2() {
        var xml = "<a x=\"1\" y=\"2\"><e/></a>";
        var document = Document.of(xml);

        var e = document.getElementByTagName("e");
        assertThat(document.rootNode.getAttribute("x")).isEqualTo("1");
        assertThat(document.rootNode.getAttribute("y")).isEqualTo("2");
    }

    @Test
    void getPreviousSibling() {
        var xml = "<x><a/><b/><c/></x>";

        var document = Document.of(xml);
        var a = document.getElementByTagName("a");
        var b = document.getElementByTagName("b");
        var c = document.getElementByTagName("c");

        assertThat(a.getPreviousSibling()).isNull();
        assertThat(b.getPreviousSibling()).isEqualTo(a);
        assertThat(c.getPreviousSibling()).isEqualTo(b);
    }

    @Test
    void insertBefore1() {
        var xml = "<x><a/><b/></x>";

        var document = Document.of(xml);
        var x = document.getElementByTagName("x");
        var a = document.getElementByTagName("a");
        var b = document.getElementByTagName("b");

        x.removeChild(b);

        x.insertBefore(b, a);

        assertThat(x.firstChild).isEqualTo(b);

        assertThat(b.getPreviousSibling()).isNull();
        assertThat(a.getPreviousSibling()).isEqualTo(b);

        assertThat(b.nextSibling).isEqualTo(a);
        assertThat(a.nextSibling).isNull();
    }

    @Test
    void insertBefore2() {
        var xml = "<x><a/><b/></x>";

        var document = Document.of(xml);
        var x = document.getElementByTagName("x");
        var a = document.getElementByTagName("a");
        var b = document.getElementByTagName("b");
        var c = document.createElement("c");

        x.insertBefore(c, b);

        assertThat(x.firstChild).isEqualTo(a);

        assertThat(c.getPreviousSibling()).isEqualTo(a);
        assertThat(b.getPreviousSibling()).isEqualTo(c);

        assertThat(a.nextSibling).isEqualTo(c);
        assertThat(c.nextSibling).isEqualTo(b);
        assertThat(b.nextSibling).isNull();
    }

    @Test
    void remove1() {
        var xml = "<x><a/><b/></x>";

        var document = Document.of(xml);
        var x = document.getElementByTagName("x");
        var a = document.getElementByTagName("a");
        var b = document.getElementByTagName("b");

        a.remove();

        assertThat(x.firstChild).isEqualTo(b);
        assertThat(a.nextSibling).isNull();
        assertThat(a.parentNode).isNull();

        assertThat(x.childNodes.length).isEqualTo(1);

    }

    @Test
    void getElementById() {
        var xml = "<x><a/><b id=\"123\" /></x>";
        var document = Document.of(xml);

        var element = document.getElementById("123");

        assertThat(element).isNotNull();
        assertThat(element.localName).isEqualTo("b");
    }

    @Test
    void findElement() {
        var xml = "<x><a/><b id=\"123\" /></x>";
        var document = Document.of(xml);

        var element = document.findElement(e -> "123".equals(e.getId()));

        assertThat(element).isNotNull();
        assertThat(element.localName).isEqualTo("b");

    }

    @Test
    void findElements() {
        var xml = "<x id=\"123\"><a/><b id=\"123\" /></x>";
        var document = Document.of(xml);

        var element = document.findElements(e -> "123".equals(e.getId()));

        assertThat(element).hasSize(2);
    }


}