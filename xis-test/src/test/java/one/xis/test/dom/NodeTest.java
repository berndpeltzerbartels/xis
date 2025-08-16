// Datei: xis-test/src/test/java/one/xis/test/dom/NodeTest.java

package one.xis.test.dom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeTest {

    @Nested
    class AppendChildTest {
        private DocumentImpl document;

        @BeforeEach
        void init() {
            document = (DocumentImpl) Document.of("<html><head></head><body><div></div></body></html>");
        }

        @Test
        void appendChild() {
            var div = document.getDocumentElement().getElementsByTagName("div").item(0);
            var p = document.createElement("p");

            div.appendChild(p);

            assertThat(div.getFirstChild()).isEqualTo(p);
            assertThat(p.getParentNode()).isEqualTo(div);
            assertThat(div.getChildNodes().length).isEqualTo(1);
            assertThat(div.getChildNodes().item(0)).isEqualTo(p);
            assertThat(div.getFirstChild()).isEqualTo(p);
        }
    }

    @Nested
    class RemoveTest {
        private DocumentImpl document;

        @BeforeEach
        void init() {
            document = (DocumentImpl) Document.of("<html><body><div><span></span></div></body></html>");
        }

        @Test
        void removeChildNode() {
            var div = document.getDocumentElement().getElementsByTagName("div").item(0);
            var span = div.getFirstChild();

            span.remove();

            assertThat(div.getChildNodes().length).isEqualTo(0);
            assertThat(span.getParentNode()).isNull();
        }
    }

    @Nested
    class CloneNodeTest {
        private DocumentImpl document;

        @BeforeEach
        void init() {
            document = (DocumentImpl) Document.of("<html><body><div></div></body></html>");
        }

        @Test
        void cloneNodeShouldCopyNode() {
            var div = (Element) document.getDocumentElement().getElementsByTagName("div").item(0);
            var clone = (Element) div.cloneNode();

            assertThat(clone).isNotSameAs(div);
            assertThat(clone.getTagName()).isEqualTo(div.getTagName());
            assertThat(clone.getParentNode()).isNull();
        }
    }

    @Nested
    class GetParentNodeTest {
        private DocumentImpl document;

        @BeforeEach
        void init() {
            document = (DocumentImpl) Document.of("<html><body><div><span></span></div></body></html>");
        }

        @Test
        void getParentNodeShouldReturnParent() {
            var div = document.getDocumentElement().getElementsByTagName("div").item(0);
            var span = div.getFirstChild();

            assertThat(span.getParentNode()).isEqualTo(div);
            assertThat(div.getParentNode()).isEqualTo(document.getDocumentElement().getElementsByTagName("body").item(0));
        }
    }
}