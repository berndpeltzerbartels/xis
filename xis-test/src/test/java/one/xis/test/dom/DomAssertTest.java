package one.xis.test.dom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DomAssertTest {


    @Nested
    class ExceptionTest {

        @Test
        void assertRootElement() {
            var document = Document.of("<a/>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertAndGetRootElement(document, "b"));
        }

        @Test
        @DisplayName("There is no such child")
        void assertChildElements1() {
            var document = Document.of("<a><b/></a>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertChildElements(document.getDocumentElement(), "c"));
        }

        @Test
        @DisplayName("Number of children is too small and one element is present")
        void assertChildElements2() {
            var document = Document.of("<a><b/></a>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertChildElements(document.getDocumentElement(), "b", "c"));
        }

        @Test
        @DisplayName("Number of children is too small and no element is present")
        void assertChildElements3() {
            var document = Document.of("<a></a>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertChildElements(document.getDocumentElement(), "b", "c"));
        }

        @Test
        @DisplayName("Number of children is too small and many elements are present")
        void assertChildElements4() {
            var document = Document.of("<a><b/><c/></a>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertChildElements(document.getDocumentElement(), "b", "c", "d"));
        }


        @Test
        void assertChildElement() {
            var document = Document.of("<a><b/></a>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertAndGetChildElement(document.getDocumentElement(), "c"));
        }

        @Test
        void assertTrue() {
            assertThrows(DomAssertionException.class, () -> DomAssert.assertTrue(System.currentTimeMillis() < 0, "bla %s", "bla"));
        }

        @Test
        void assertNoChildElement() {
            var document = Document.of("<a><b/></a>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertNoChildElement(document.getDocumentElement(), "b"));
        }

        @Test
        void assertTagName() {
            var document = Document.of("<a></a>");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertTagName(document.getDocumentElement(), "b"));
        }

        @Test
        void assertParentElement() {
            var document = Document.of("<a><b/></a>");
            var b = document.getElementByTagName("b");
            assertThrows(DomAssertionException.class, () -> DomAssert.assertAndGetParentElement(b, "b"));
        }
    }

    @Nested
    class SuccessTest {
        @Test
        void assertRootElement() {
            var document = Document.of("<a/>");
            DomAssert.assertAndGetRootElement(document, "a");
        }

        @Test
        void assertChildElements1() {
            var document = Document.of("<a><b/></a>");
            DomAssert.assertChildElements(document.getDocumentElement(), "b");
        }

        @Test
        void assertChildElements() {
            var document = Document.of("<a><b/><c/></a>");
            DomAssert.assertChildElements(document.getDocumentElement(), "b", "c");
        }


        @Test
        void assertChildElement() {
            var document = Document.of("<a><b/></a>");
            DomAssert.assertAndGetChildElement(document.getDocumentElement(), "b");
        }

        @Test
        void assertTrue() {
            DomAssert.assertTrue(System.currentTimeMillis() > 0, "bla %s", "bla");
        }

        @Test
        void assertNoChildElement() {
            var document = Document.of("<a><b/></a>");
            DomAssert.assertNoChildElement(document.getDocumentElement(), "c");
        }

        @Test
        void assertTagName() {
            var document = Document.of("<a></a>");
            DomAssert.assertTagName(document.getDocumentElement(), "a");
        }

        @Test
        void assertParentElement() {
            var document = Document.of("<a><b/></a>");
            var b = document.getElementByTagName("b");
            DomAssert.assertAndGetParentElement(b, "a");
        }

    }
}