package one.xis.server;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PathResolverTest {

    private final PathResolver pathResolver = new PathResolver();

    @Test
    void noVariables() {
        var path = pathResolver.createPath("/abc/xyz.html");

        assertThat(path.normalized()).isEqualTo("/abc/xyz.html");
        assertThat(path.getPathElements().size()).isEqualTo(1);
        assertThat(path.getPathElements().get(0)).isEqualTo(new PathString("/abc/xyz.html"));
    }

    @Test
    void onePathVariable() {
        var path = pathResolver.createPath("/a/{b}/c.html");

        assertThat(path.normalized()).isEqualTo("/a/*/c.html");
        assertThat(path.getPathElements().size()).isEqualTo(3);
        assertThat(path.getPathElements().get(0)).isEqualTo(new PathString("/a/"));
        assertThat(path.getPathElements().get(1)).isEqualTo(new PathVariable("b"));
        assertThat(path.getPathElements().get(2)).isEqualTo(new PathString("/c.html"));
    }

    @Test
    void twoPathVariable() {
        var path = pathResolver.createPath("/{a}/{b}/c.html");

        assertThat(path.normalized()).isEqualTo("/*/*/c.html");
        assertThat(path.getPathElements().size()).isEqualTo(5);
        assertThat(path.getPathElements().get(0)).isEqualTo(new PathString("/"));
        assertThat(path.getPathElements().get(1)).isEqualTo(new PathVariable("a"));
        assertThat(path.getPathElements().get(2)).isEqualTo(new PathString("/"));
        assertThat(path.getPathElements().get(3)).isEqualTo(new PathVariable("b"));
        assertThat(path.getPathElements().get(4)).isEqualTo(new PathString("/c.html"));
    }


    @Test
    void startingWithVariable() {
        var path = pathResolver.createPath("{a}/b.html");

        assertThat(path.normalized()).isEqualTo("*/b.html");
        assertThat(path.getPathElements().size()).isEqualTo(2);
        assertThat(path.getPathElements().get(0)).isEqualTo(new PathVariable("a"));
        assertThat(path.getPathElements().get(1)).isEqualTo(new PathString("/b.html"));
    }


    @Test
    void mixedContent() {
        var path = pathResolver.createPath("/{a}/product_{b}/c.html");

        assertThat(path.normalized()).isEqualTo("/*/product_*/c.html");
        assertThat(path.getPathElements().size()).isEqualTo(5);
        assertThat(path.getPathElements().get(0)).isEqualTo(new PathString("/"));
        assertThat(path.getPathElements().get(1)).isEqualTo(new PathVariable("a"));
        assertThat(path.getPathElements().get(2)).isEqualTo(new PathString("/product_"));
        assertThat(path.getPathElements().get(3)).isEqualTo(new PathVariable("b"));
        assertThat(path.getPathElements().get(4)).isEqualTo(new PathString("/c.html"));
    }

    @Test
    void pathMatching() {
        var path = pathResolver.createPath("/product/{id}/view.html");

        assertThat(path.matches("/product/123/view.html")).isTrue();
        assertThat(path.matches("/product/abc/view.html")).isTrue();
        assertThat(path.matches("/product/123/edit.html")).isFalse();
        assertThat(path.matches("/product/123/view.htm")).isFalse();
    }


}