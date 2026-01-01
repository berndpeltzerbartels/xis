package one.xis.html;

import one.xis.html.tokens.HtmlTokenizer;
import one.xis.html.tokens.TextToken;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DocsTest {

    private final HtmlParser parser = new HtmlParser();

    @Test
    void parseMethodAnnotations() throws Exception {
        String html = Files.readString(Path.of("../../xis-examples/xis-docs/src/main/java/xis/docs/content/MethodAnnotations.html"));

        var document = parser.parse(html);
        assertThat(document.getDocumentElement()).isNotNull();
        System.out.println("✓ MethodAnnotations.html parsed successfully");
    }
        

}
