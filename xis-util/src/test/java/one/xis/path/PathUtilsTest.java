package one.xis.path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathUtilsTest {


    @Test
    void getSuffix() {
        assertThat(PathUtils.getSuffix("xyz")).isNull();
        assertThat(PathUtils.getSuffix("a.b.c")).isEqualTo("c");
        assertThat(PathUtils.getSuffix("/a/b/c.d")).isEqualTo("d");
    }

    @Test
    void stripSuffix() {
        assertThat(PathUtils.stripSuffix("xyz")).isEqualTo("xyz");
        assertThat(PathUtils.stripSuffix("a.b.c")).isEqualTo("a.b");
        assertThat(PathUtils.stripSuffix("/a/b/c.d")).isEqualTo("/a/b/c");
        assertThat(PathUtils.stripSuffix("a.x/b.x/c.d")).isEqualTo("a.x/b.x/c");
    }

    @Test
    void getFile() {
        assertThat(PathUtils.getFile("xyz")).isEqualTo("xyz");
        assertThat(PathUtils.getFile("a.b.c")).isEqualTo("a.b.c");
        assertThat(PathUtils.getFile("/a/b/c.d")).isEqualTo("c.d");
        assertThat(PathUtils.getFile("/a/b/c")).isEqualTo("c");
    }

    @Test
    void appendPath() {
        assertThat(PathUtils.appendPath("xyz", "123")).isEqualTo("xyz/123");
        assertThat(PathUtils.appendPath("/xyz/", "123")).isEqualTo("/xyz/123");
        assertThat(PathUtils.appendPath("/xyz", "/123")).isEqualTo("/xyz/123");
        assertThat(PathUtils.appendPath("/xyz/", "/123")).isEqualTo("/xyz/123");
    }

    @Test
    void stripTrailingSlash() {
        assertThat(PathUtils.stripTrailingSlash("")).isEqualTo("");
        assertThat(PathUtils.stripTrailingSlash("a/b/c")).isEqualTo("a/b/c");
        assertThat(PathUtils.stripTrailingSlash("/a/b/c")).isEqualTo("a/b/c");
        assertThat(PathUtils.stripTrailingSlash("/a.html")).isEqualTo("a.html");
    }
}