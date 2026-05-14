package one.xis.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/*
  Annahme: Die Klassen Path und PathMatchingResult befinden sich im selben Paket one.xis.http
  und sind für den Test zugänglich. Falls sie in one.xis.server liegen,
  muss das package hier entsprechend angepasst werden.
*/
class PathTest {

    @Nested
    @DisplayName("Static Paths")
    class StaticPathTests {

        @Test
        @DisplayName("should match identical static path")
        void testSimpleStaticMatch() {
            Path path = new Path("/users/all");
            MethodMatchResult result = path.matches("/users/all");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).isEmpty();
        }

        @Test
        @DisplayName("should not match different static path")
        void testSimpleStaticMismatch() {
            Path path = new Path("/users/all");
            MethodMatchResult result = path.matches("/users/list");

            assertThat(result.isMatch()).isFalse();
        }

        @Test
        @DisplayName("should not match if request path is longer")
        void testStaticMismatchTooLong() {
            Path path = new Path("/users");
            MethodMatchResult result = path.matches("/users/all");

            assertThat(result.isMatch()).isFalse();
        }

        @Test
        @DisplayName("should not match if request path is shorter")
        void testStaticMismatchTooShort() {
            Path path = new Path("/users/all");
            MethodMatchResult result = path.matches("/users");

            assertThat(result.isMatch()).isFalse();
        }

        @Test
        @DisplayName("should handle root path")
        void testRootPath() {
            Path path = new Path("/");
            assertThat(path.matches("/").isMatch()).isTrue();
            assertThat(path.matches("").isMatch()).isFalse(); // Annahme: Pfade beginnen mit /
            assertThat(path.matches("/a").isMatch()).isFalse();
        }
    }

    @Nested
    @DisplayName("Paths with Variables")
    class VariablePathTests {

        @Test
        @DisplayName("should match and extract variable at the end")
        void testVariableAtEnd() {
            Path path = new Path("/users/{id}");
            MethodMatchResult result = path.matches("/users/123-abc");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).containsEntry("id", "123-abc");
        }

        @Test
        @DisplayName("should match and extract variable in the middle")
        void testVariableInMiddle() {
            Path path = new Path("/users/{id}/posts");
            MethodMatchResult result = path.matches("/users/42/posts");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).containsEntry("id", "42");
        }

        @Test
        @DisplayName("should not match if static part after variable is wrong")
        void testVariableInMiddleMismatch() {
            Path path = new Path("/users/{id}/posts");
            MethodMatchResult result = path.matches("/users/42/comments");

            assertThat(result.isMatch()).isFalse();
        }

        @Test
        @DisplayName("should match and extract multiple variables")
        void testMultipleVariables() {
            Path path = new Path("/users/{userId}/posts/{postId}");
            MethodMatchResult result = path.matches("/users/user-1/posts/post-99");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables())
                    .hasSize(2)
                    .containsEntry("userId", "user-1")
                    .containsEntry("postId", "post-99");
        }

        @Test
        @DisplayName("should match variable with static suffix like {name}.html")
        void testVariableWithSuffix() {
            Path path = new Path("/files/{filename}.html");
            MethodMatchResult result = path.matches("/files/report-2024.html");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).containsEntry("filename", "report-2024");
        }

        @Test
        @DisplayName("should not match variable with wrong static suffix")
        void testVariableWithSuffixMismatch() {
            Path path = new Path("/files/{filename}.html");
            MethodMatchResult result = path.matches("/files/report-2024.pdf");

            assertThat(result.isMatch()).isFalse();
        }

        @Test
        @DisplayName("should match variable with static prefix like file-{name}")
        void testVariableWithPrefix() {
            Path path = new Path("/downloads/file-{name}");
            MethodMatchResult result = path.matches("/downloads/file-archive");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).containsEntry("name", "archive");
        }

        @Test
        @DisplayName("should match variable with both prefix and suffix")
        void testVariableWithPrefixAndSuffix() {
            Path path = new Path("/assets/img-{id}-thumb.jpg");
            MethodMatchResult result = path.matches("/assets/img-12345-thumb.jpg");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).containsEntry("id", "12345");
        }

        @Test
        @DisplayName("should match variable at the beginning of the path")
        void testVariableAtStart() {
            Path path = new Path("/{entity}/list");
            MethodMatchResult result = path.matches("/products/list");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).containsEntry("entity", "products");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle empty path template and empty request path")
        void testEmptyPath() {
            // Das Parsing von "" erzeugt ein einzelnes StaticPathElement("")
            Path path = new Path("");
            assertThat(path.matches("").isMatch()).isTrue();
            assertThat(path.matches("/").isMatch()).isFalse();
        }

        @Test
        @DisplayName("should handle template with only a variable")
        void testOnlyVariable() {
            Path path = new Path("{id}");
            MethodMatchResult result = path.matches("any-value-works");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables()).containsEntry("id", "any-value-works");
        }

        @Test
        @DisplayName("should not match if request path is not fully consumed")
        void testRequestPathNotFullyConsumed() {
            Path path = new Path("/a/{b}");
            MethodMatchResult result = path.matches("/a/b/c");

            assertThat(result.isMatch()).isFalse();
        }

        @Test
        @DisplayName("should correctly handle consecutive variables")
        void testConsecutiveVariables() {
            // Das Matching ist "greedy". Das erste variable Element matcht bis zum nächsten statischen Teil.
            // Da auf {a} kein statischer Teil folgt, konsumiert es den Rest des Pfades.
            Path path = new Path("/{a}{b}");
            MethodMatchResult result = path.matches("/first-part-second-part");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables())
                    .hasSize(2)
                    .containsEntry("b", "first-part-second-part")
                    .containsEntry("a", ""); // Für {b} bleibt nichts übrig
        }

        @Test
        @DisplayName("should correctly handle consecutive variables followed by a static part")
        void testConsecutiveVariablesWithStaticEnd() {
            Path path = new Path("/{a}.{b}.html");
            MethodMatchResult result = path.matches("/first.second.html");

            assertThat(result.isMatch()).isTrue();
            assertThat(result.getPathVariables())
                    .hasSize(2)
                    .containsEntry("a", "first")
                    .containsEntry("b", "second"); // {a} matcht bis zum {b}, {b} matcht bis zum .html
        }
    }
}