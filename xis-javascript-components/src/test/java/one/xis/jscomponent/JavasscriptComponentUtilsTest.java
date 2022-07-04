package one.xis.jscomponent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavasscriptComponentUtilsTest {

    @Test
    void getHtmlTemplatePath() {
        assertThat(JavasscriptComponentUtils.getHtmlTemplatePath(Object.class)).isEqualTo("java/lang/Object.html");
    }

    @Test
    void validatePath() {
        Assertions.assertDoesNotThrow(() -> JavasscriptComponentUtils.validatePath("/a/bc/d.html"));
        Assertions.assertDoesNotThrow(() -> JavasscriptComponentUtils.validatePath("/a/bc/d"));

        Assertions.assertThrows(IllegalStateException.class, () -> JavasscriptComponentUtils.validatePath("/a/bc/d.txt"));
        Assertions.assertThrows(IllegalStateException.class, () -> JavasscriptComponentUtils.validatePath("/a/bc/d.e.f.html")); // important for script-url
    }

}