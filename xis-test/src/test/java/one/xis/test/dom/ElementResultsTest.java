package one.xis.test.dom;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ElementResultsTest {

    @Nested
    class SuccessTest {
        @Test
        void assertSize() {
            var results = new ElementResults(List.of(new ElementImpl("a")));
            results.assertSize(1);
        }

        @Test
        void assertEmpty() {
            var results = new ElementResults(List.of());
            results.assertEmpty();
        }

        @Test
        void size() {
            var results = new ElementResults(List.of(new ElementImpl("a")));
            assertThat(results.size()).isEqualTo(1);
        }

        @Test
        void toUniqueResult() {
            var results = new ElementResults(List.of(new ElementImpl("a")));
            results.toUniqueResult().assertTagName("a");
        }

        @Test
        void pick() {
            var results = new ElementResults(List.of(new ElementImpl("a")));
            results.pick(0).assertTagName("a");
            results.pick("a").assertTagName("a");
        }


        @Test
        void pickAll() {
            var results = new ElementResults(Document.of("<a><c/><b/><c/></a>").getDocumentElement().getChildElements());
            assertThat(results.pickAll("c").size()).isEqualTo(2);
        }
    }

    @Nested
    class ExceptionTest {
        @Test
        void assertSize() {
            var results = new ElementResults(List.of(new ElementImpl("a")));
            assertThrows(DomAssertionException.class, () -> results.assertSize(2));
        }

        @Test
        void assertEmpty() {
            var results = new ElementResults(List.of(new ElementImpl("a")));
            assertThrows(DomAssertionException.class, results::assertEmpty);
        }

    }
}