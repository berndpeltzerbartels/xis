package one.xis.js;

import one.xis.resource.Resources;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

class JavascriptProviderTest {


    @Test
    void getCompressedJavascript() {
        var provider = new JavascriptProviderImpl(new JavascriptExtensionLoader(), new Resources());
        provider.init();

        long start = System.currentTimeMillis();

        var compressedJavascript = provider.getCompressedJavascript();
        var sourceMap = provider.getSourceMap();
        System.out.println("Compressed length: " + compressedJavascript.getLength());

        long end = System.currentTimeMillis();
        System.out.println("Time taken: " + (end - start) + " ms");
        Assertions.assertThat(compressedJavascript.getLength()).isGreaterThan(30000);
        Assertions.assertThat(sourceMap.getLength()).isGreaterThan(100000);
        Assertions.assertThat(compressedJavascript.getContent()).contains("window.XIS");
    }

    @Test
    void appendsExtensionsWithoutRuntimeCompression() {
        var extensions = new JavascriptExtensionLoader() {
            @Override
            public Map<String, String> loadExtensions() {
                var result = new LinkedHashMap<String, String>();
                result.put("META-INF/xis/js/test-extension.js", "window.XIS.addElFunction('demoExtension', function() { return 'ok'; });");
                return result;
            }
        };
        var provider = new JavascriptProviderImpl(extensions, new Resources());

        provider.init();

        Assertions.assertThat(provider.getCompressedJavascript().getContent())
                .contains("/* META-INF/xis/js/test-extension.js */")
                .contains("window.XIS.addElFunction('demoExtension'");
        Assertions.assertThat(provider.getSourceMap().getContent()).contains("\"file\":\"bundle.min.js\"");
    }
}
