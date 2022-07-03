package one.xis.jscomponent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JavasscriptComponentUtilsTest {
    
    @Test
    void getHtmlTemplatePath() {
        assertThat(JavasscriptComponentUtils.getHtmlTemplatePath(Object.class)).isEqualTo("java/lang/Object.html");
    }
}