package one.xis.test.dom;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ElementResultTest {

    @Nested
    class ExceptionTest {

        @Test
        void assertChildElements() {
            var result = new ElementResult(Document.of("<a><b/></a>").rootNode);
            assertThrows(DomAssertionException.class, () -> result.assertChildElements("b", "c"));
        }

        @Test
        void assertNoChildElement() {
            var result = new ElementResult(Document.of("<a><b/></a>").rootNode);
            assertThrows(DomAssertionException.class, () -> result.assertNoChildElement("b"));
        }

        @Test
        void assertAttribute() {
            var element = new ElementImpl("a");
            var result = new ElementResult(element);

            assertThrows(DomAssertionException.class, () -> result.assertAttribute("b"));
            assertThrows(DomAssertionException.class, () -> result.assertAttribute("b", "c"));

        }

        @Test
        void assertTextContent() {
            var result = new ElementResult(Document.of("<a>x</a>").rootNode);
            assertThrows(DomAssertionException.class, () -> result.assertTextContent("y"));
        }

        @Test
        void assertTrimmedContent() {
            var result = new ElementResult(Document.of("<a> x </a>").rootNode);
            assertThrows(DomAssertionException.class, () -> result.assertTrimmedContent("y"));
        }

        @Test
        void assertNoChildElements() {
            var result = new ElementResult(Document.of("<a><b/></a>").rootNode);
            assertThrows(DomAssertionException.class, result::assertNoChildElements);
        }

        @Test
        void assertParentElement() {
            var document = Document.of("<a><b/></a>");
            var b = document.getElementByTagName("b");
            assertThrows(DomAssertionException.class, () -> new ElementResult(b).assertAndGetParentElement("b"));
        }
    }

    @Nested
    class SuccessTest {

        @Test
        void assertChildElements() {
            var result = new ElementResult(Document.of("<a><b/><c/></a>").rootNode);
            result.assertChildElements("b", "c");
        }

        @Test
        void assertNoChildElement() {
            var result = new ElementResult(Document.of("<a></a>").rootNode);
            result.assertNoChildElement("b");
        }

        @Test
        void assertAttribute() {
            var element = new ElementImpl("a");
            element.setAttribute("b", "c");
            var result = new ElementResult(element);
            result.assertAttribute("b");
            result.assertAttribute("b", "c");
        }

        @Test
        void assertTextContent() {
            var result = new ElementResult(Document.of("<a>x</a>").rootNode);
            result.assertTextContent("x");
        }

        @Test
        void assertTrimmedContent() {
            var result = new ElementResult(Document.of("<a> x </a>").rootNode);
            result.assertTrimmedContent("x");
        }

        @Test
        void assertNoChildElements() {
            var result = new ElementResult(Document.of("<a></a>").rootNode);
            result.assertNoChildElements();
        }


        @Test
        void assertParentElement() {
            var document = Document.of("<a><b/></a>");
            var b = document.getElementByTagName("b");
            new ElementResult(b).assertAndGetParentElement("a");
        }
    }
}