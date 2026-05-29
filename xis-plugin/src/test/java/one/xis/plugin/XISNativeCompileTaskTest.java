package one.xis.plugin;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XISNativeCompileTaskTest {

    @Test
    void nativeResourceIncludePatternCoversNonClassResources() {
        var pattern = Pattern.compile(XISNativeCompileTask.NATIVE_RESOURCE_INCLUDE_PATTERN);

        assertTrue(pattern.matcher("public/css/game.css").matches());
        assertTrue(pattern.matcher("public/js/game.js").matches());
        assertTrue(pattern.matcher("public/img/pieces/king_white.png").matches());
        assertTrue(pattern.matcher("public/img/logo.svg").matches());
        assertTrue(pattern.matcher("public/fonts/app.woff2").matches());
        assertTrue(pattern.matcher("public/img/LOGO.PNG").matches());
        assertTrue(pattern.matcher("config/local.runtime").matches());
        assertTrue(pattern.matcher("META-INF/xis/native/resources/catalog.txt").matches());

        assertFalse(pattern.matcher("one/xis/App.class").matches());
        assertFalse(pattern.matcher("META-INF/versions/21/one/xis/App.class").matches());
    }
}
