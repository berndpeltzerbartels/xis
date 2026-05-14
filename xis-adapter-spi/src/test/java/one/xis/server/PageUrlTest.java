package one.xis.server;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PageUrlTest {

    @Test
    void matches_withoutPathVariables() {
        PageUrl pageUrl = new PageUrl("/index.html");

        Optional<Map<String, String>> result = pageUrl.matches("/index.html");

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void doesNotMatch_withoutPathVariables() {
        PageUrl pageUrl = new PageUrl("/index.html");

        Optional<Map<String, String>> result = pageUrl.matches("/index.htm");

        assertThat(result).isEmpty();
    }

    @Test
    void matches_withSinglePathVariable() {
        PageUrl pageUrl = new PageUrl("/user/{id}");

        Optional<Map<String, String>> result = pageUrl.matches("/user/42");

        assertThat(result).isPresent();
        assertThat(result.get())
                .containsEntry("id", "42")
                .hasSize(1);
    }

    @Test
    void matches_withMultiplePathVariables() {
        PageUrl pageUrl = new PageUrl("/user/{userId}/post/{postId}");

        Optional<Map<String, String>> result = pageUrl.matches("/user/abc/post/99");

        assertThat(result).isPresent();
        assertThat(result.get())
                .containsEntry("userId", "abc")
                .containsEntry("postId", "99")
                .hasSize(2);
    }

    @Test
    void doesNotMatch_ifSegmentCountDiffers() {
        PageUrl pageUrl = new PageUrl("/user/{id}");

        Optional<Map<String, String>> result = pageUrl.matches("/user/42/profile");

        assertThat(result).isEmpty();
    }

    @Test
    void doesNotMatch_ifMissingSegment() {
        PageUrl pageUrl = new PageUrl("/user/{id}/profile");

        Optional<Map<String, String>> result = pageUrl.matches("/user/42");

        assertThat(result).isEmpty();
    }

    @Test
    void pathVariable_doesNotMatchSlash() {
        PageUrl pageUrl = new PageUrl("/file/{name}");

        Optional<Map<String, String>> result = pageUrl.matches("/file/foo/bar");

        assertThat(result).isEmpty();
    }

    @Test
    void matches_withSpecialCharactersInVariable() {
        PageUrl pageUrl = new PageUrl("/file/{name}");

        Optional<Map<String, String>> result = pageUrl.matches("/file/foo-bar_123");

        assertThat(result).isPresent();
        assertThat(result.get())
                .containsEntry("name", "foo-bar_123");
    }

    @Test
    void matches_rootPath() {
        PageUrl pageUrl = new PageUrl("/");

        Optional<Map<String, String>> result = pageUrl.matches("/");

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }

    @Test
    void doesNotMatch_trailingSlashMismatch() {
        PageUrl pageUrl = new PageUrl("/user/{id}");

        Optional<Map<String, String>> result = pageUrl.matches("/user/42/");

        assertThat(result).isEmpty();
    }
}
