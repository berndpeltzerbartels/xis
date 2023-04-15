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
    @DisplayName("Create document from source")
    void of2() {
        var html = new Resources().getByPath("/index.html").getContent();
        var document = Document.of(html);

        assertThat(document.rootNode.localName).isEqualTo("html");
        assertThat(document.rootNode.getChildElementNames()).containsExactly("head", "body");

        var head = document.getElementByTagName("head");
        assertThat(head.getChildElementNames()).containsExactly("title", "script", "script");

        var body = document.getElementByTagName("body");
        assertThat(body.getChildList().size()).isEqualTo(2);
        assertThat(body.getAttribute("onload")).isEqualTo("initialize()");
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

}