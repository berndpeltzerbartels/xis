package one.xis.js;

import one.xis.context.AppContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class JavascriptProviderTest {


    @Test
    void getCompressedJavascript() {
        var context = AppContext.builder().withBasePackageClass(JavascriptProvider.class).build();
        var provider = context.getSingleton(JavascriptProvider.class);

        long start = System.currentTimeMillis();

        var compressedJavascript = provider.getCompressedJavascript();
        var sourceMap = provider.getSourceMap();
        System.out.println("Compressed length: " + compressedJavascript.getLength());

        long end = System.currentTimeMillis();
        System.out.println("Time taken: " + (end - start) + " ms");
        Assertions.assertThat(compressedJavascript.getLength()).isGreaterThan(30000);
        Assertions.assertThat(sourceMap.getLength()).isGreaterThan(100000);
    }
}