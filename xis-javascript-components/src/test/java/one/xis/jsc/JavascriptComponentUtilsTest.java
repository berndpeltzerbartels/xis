package one.xis.jsc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavascriptComponentUtilsTest {

    @Test
    void getHtmlTemplatePath() {
        assertThat(JavascriptComponentUtils.getHtmlTemplatePath(Object.class)).isEqualTo("java/lang/Object.html");
    }

    @Test
    void validatePath() {
        Assertions.assertDoesNotThrow(() -> JavascriptComponentUtils.validatePath("/a/bc/d.html"));
        Assertions.assertDoesNotThrow(() -> JavascriptComponentUtils.validatePath("/a/bc/d"));

        Assertions.assertThrows(IllegalStateException.class, () -> JavascriptComponentUtils.validatePath("/a/bc/d.txt"));
        Assertions.assertThrows(IllegalStateException.class, () -> JavascriptComponentUtils.validatePath("/a/bc/d.e.f.html")); // important for script-url
    }

}