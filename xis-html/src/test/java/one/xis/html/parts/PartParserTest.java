package one.xis.html.parts;

import one.xis.html.tokens.HtmlTokenizer;
import one.xis.html.tokens.Token;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PartParserTest {


    @Nested
    class SimpleElementTest {
        private final String html = "<div class=\"container\">Content</div>";

        @Test
        void parse() {
            var parts = new PartParser2(tokens(html)).parse();

            assertThat(parts).hasSize(3);
            assertThat(parts.get(0)).isInstanceOf(OpeningTag.class);

            var openingTag = (OpeningTag) parts.get(0);
            assertThat(openingTag.getLocalName()).isEqualTo("div");
            assertThat(openingTag.getAttributes()).containsEntry("class", "container");
            assertThat(parts.get(1)).isInstanceOf(TextPart.class);

            var textPart = (TextPart) parts.get(1);
            assertThat(textPart.getText()).isEqualTo("Content");
            assertThat(parts.get(2)).isInstanceOf(ClosingTag.class);

            var closingTag = (ClosingTag) parts.get(2);
            assertThat(closingTag.getLocalName()).isEqualTo("div");
        }
    }


    private static List<Token> tokens(String html) {
        return new HtmlTokenizer().tokenize(html);
    }

}



