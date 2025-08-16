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
        var document = (DocumentImpl) Document.of(html);

        assertThat(document.getDocumentElement().getLocalName()).isEqualTo("html");
        assertThat(((ElementImpl) document.getDocumentElement()).getChildElements().stream()
                .filter(ElementImpl.class::isInstance)
                .map(ElementImpl.class::cast)
                .map(ElementImpl::getLocalName)).containsExactly("head", "body");

        var head = document.getElementByTagName("head");
        assertThat(head.getChildElements()).singleElement()
                .satisfies(e -> assertThat(e.getLocalName()).isEqualTo("head"));
        assertThat(head.getChildElements().get(0).getLocalName()).isEqualTo("title");

        var title = document.getElementByTagName("title");
        assertThat(title.getInnerText()).isEqualTo("Title");

        var body = document.getElementByTagName("body");
        assertThat(body.getChildElements().size()).isEqualTo(2);

        var divs = body.getElementsByTagName("div");
        var div1 = (ElementImpl) divs.item(0);
        var div2 = (ElementImpl) divs.item(1);

        assertThat(div1.getChildElements().size()).isEqualTo(1);
        assertThat(div2.getChildElements()).isEmpty();
        assertThat(div1.getChildElements().get(0)).isInstanceOf(TextNodeIml.class);
        assertThat(div1.getInnerText()).isEqualTo("123");

    }

    @Test
    @DisplayName("Create document from default-develop-index.html")
    void of2() {
        var html = new Resources().getByPath("/default-develop-index.html").getContent();
        var document = Document.of(html);

        assertThat(document.getDocumentElement().getLocalName()).isEqualTo("html");
        assertThat(document.getDocumentElement().getChildElements().stream().map(Element::getTagName)).containsExactly("head", "body");

        var head = document.getElementByTagName("head");
        assertThat(head.getChildElements().stream().map(Element::getTagName)).contains("title", "script");

        var body = document.getElementByTagName("body");
        assertThat(body.getChildNodes().length).isEqualTo(1); // messages-div
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
        var document = ((DocumentImpl) Document.of(xml));

        var e = document.getElementByTagName("e");
        assertThat(document.getDocumentElement().getAttribute("x")).isEqualTo("1");
        assertThat(document.getDocumentElement().getAttribute("y")).isEqualTo("2");
    }

    @Test
    void getPreviousSibling() {
        var xml = "<x><a/><b/><c/></x>";

        var document = Document.of(xml);
        var a = (ElementImpl) document.getElementByTagName("a");
        var b = (ElementImpl) document.getElementByTagName("b");
        var c = (ElementImpl) document.getElementByTagName("c");

        assertThat(a.getPreviousSibling()).isNull();
        assertThat(b.getPreviousSibling()).isEqualTo(a);
        assertThat(c.getPreviousSibling()).isEqualTo(b);
    }

    @Test
    void insertBefore1() {
        var xml = "<x><a/><b/></x>";

        var document = Document.of(xml);
        var x = document.getElementByTagName("x");
        var a = (ElementImpl) document.getElementByTagName("a");
        var b = (ElementImpl) document.getElementByTagName("b");

        x.removeChild(b);

        x.insertBefore(b, a);

        assertThat(x.getFirstChild()).isEqualTo(b);

        assertThat(b.getPreviousSibling()).isNull();
        assertThat(a.getPreviousSibling()).isEqualTo(b);

        assertThat(b.getNextSibling()).isEqualTo(a);
        assertThat(a.getNextSibling()).isNull();
    }

    @Test
    void insertBefore2() {
        var xml = "<x><a/><b/></x>";

        var document = (DocumentImpl) Document.of(xml);
        var x = document.getElementByTagName("x");
        var a = (ElementImpl) document.getElementByTagName("a");
        var b = (ElementImpl) document.getElementByTagName("b");
        var c = (ElementImpl) document.createElement("c");

        x.insertBefore(c, b);

        assertThat(x.getFirstChild()).isEqualTo(a);

        assertThat(c.getPreviousSibling()).isEqualTo(a);
        assertThat(b.getPreviousSibling()).isEqualTo(c);

        assertThat(a.getNextSibling()).isEqualTo(c);
        assertThat(c.getNextSibling()).isEqualTo(b);
        assertThat(b.getNextSibling()).isNull();
    }

    @Test
    void remove1() {
        var xml = "<x><a/><b/></x>";

        var document = Document.of(xml);
        var x = document.getElementByTagName("x");
        var a = document.getElementByTagName("a");
        var b = document.getElementByTagName("b");

        a.remove();

        assertThat(x.getFirstChild()).isEqualTo(b);
        assertThat(a.getNextSibling()).isNull();
        assertThat(a.getParentNode()).isNull();

        assertThat(x.getChildNodes().length).isEqualTo(1);

    }

    @Test
    void getElementById() {
        var xml = "<x><a/><b id=\"123\" /></x>";
        var document = Document.of(xml);

        var element = document.getElementById("123");

        assertThat(element).isNotNull();
        assertThat(element.getLocalName()).isEqualTo("b");
    }
    

}