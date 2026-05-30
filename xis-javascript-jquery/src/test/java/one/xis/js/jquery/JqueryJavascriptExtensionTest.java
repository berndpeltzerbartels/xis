package one.xis.js.jquery;

import one.xis.js.JavascriptExtensionLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JqueryJavascriptExtensionTest {

    @Test
    void exposesJqueryAsJavascriptExtension() {
        var extensions = new JavascriptExtensionLoader().loadExtensions();

        assertThat(extensions)
                .containsKey("META-INF/resources/webjars/jquery/3.7.1/jquery.min.js");
        assertThat(extensions.get("META-INF/resources/webjars/jquery/3.7.1/jquery.min.js"))
                .contains("jQuery v3.7.1");
    }
}
