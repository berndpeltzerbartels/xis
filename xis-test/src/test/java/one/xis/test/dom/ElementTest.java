package one.xis.test.dom;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElementTest {

    @Test
    void getChildElementsByClassName() {
        var doc = Document.of("<html><body><span class=\"class1 class2\"/><span class=\"class1 class2\"/></body></html>");

        var body = doc.getElementByTagName("body");
        assertThat(body.getChildElementsByClassName("class1").size()).isEqualTo(2);

    }


    @Test
    void getDescendantElementsByClassName() {
        var doc = Document.of("<html><body><div><span class=\"class1 class2\"/><span class=\"class1 class2\"/></div></body></html>");

        var body = doc.getElementByTagName("body");
        assertThat(body.getDescendantElementsByClassName("class1").size()).isEqualTo(2);
    }
}
