package one.xis.test.dom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

    @Test
    @DisplayName("Create document from source")
    void of() {
        var html = "<html><head><title>Title</title></head><body><div>123</div><div/></body></html>";
        var document = Document.of(html);

        assertThat(document.rootNode.localName).isEqualTo("html");
        assertThat(document.rootNode.getChildList().stream().map(Node::name)).containsExactly("head", "body");

        var head = document.getElementByTagName("head");
        assertThat(head.getChildList().size()).isEqualTo(1);
        assertThat(head.getChildList().get(0).name()).isEqualTo("title");

        var title = document.getElementByTagName("title");
        assertThat(title.getChildList().size()).isEqualTo(1);
        assertThat(title.getChildList().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) title.getChildList().get(0)).nodeValue).isEqualTo("Title");

        var body = document.getElementByTagName("body");
        assertThat(body.getChildList().size()).isEqualTo(2);

        var divs = body.getChildElementsByName("div");
        var div1 = divs.get(0);
        var div2 = divs.get(1);

        assertThat(div1.getChildList().size()).isEqualTo(1);
        assertThat(div2.getChildList()).isEmpty();
        assertThat(div1.getChildList().get(0)).isInstanceOf(TextNode.class);
        assertThat(((TextNode) div1.getChildList().get(0)).nodeValue).isEqualTo("123");

    }
}