package one.xis.html;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlParserTest {

    private final HtmlParser parser = new HtmlParser();


    @Nested
    class SimpleElementTest {
        private final String html = "<div class=\"container\">Content</div>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.html);
        }
    }


    @Nested
    class SimpleNestedElementTest {
        private final String html = "<html><body><div class=\"container\">Content</div></body></html>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.html);
        }
    }

    @Nested
    class SelfClosingTagClosedTest {
        private final String html = "<img src=\"image.png\" alt=\"An image\"/>";

        private final String expectedHtml = "<img src=\"image.png\" alt=\"An image\">";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace(expectedHtml);
        }
    }

    @Nested
    class SelfClosingTagNotClosedTest {
        private final String html = "<img src=\"image.png\" alt=\"An image\">";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace("<img src=\"image.png\" alt=\"An image\">");
        }
    }


    @Nested
    class HtmlTest {
        private final String html = """
                <html>
                    <head>
                        <title>Test</title>
                    </head>
                    <body>
                        <h1>Hello, World!</h1>
                        <p>This is a simple HTML document.</p>
                    </body>
                </html>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();


            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.html);
        }
    }

    @Nested
    class HtmlWithXisElementsTest1 {
        private final String html = """
                <a page="home">
                   Linktext
                   <xis:param name="param1" value="value1"/>
                   <xis:param name="param2" value="value2"/>
                </a>
                """;

        private final String expectedHtml = """
                <a page="home">
                   Linktext
                   <xis:param name="param1" value="value1"></xis:param>
                   <xis:param name="param2" value="value2"></xis:param>
                </a>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();


            assertThat(htmlResult).isEqualToIgnoringWhitespace(this.expectedHtml);
        }
    }

    @Nested
    class HtmlWithXisExpressionLanguage {
        private final String html = """
                <div xis:if="${login.attempts > 3}">
                   Too many login attempts.
                </div>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);

            assertThat(document.getDocumentElement().getAttributes()).isEqualTo(Map.of("xis:if", "${login.attempts > 3}"));
        }
    }

    @Nested
    class BooleanAttributeTest {
        private final String html = "<input type=\"checkbox\" checked>";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace("<input type=\"checkbox\" checked=\"true\">");
        }
    }

    @Nested
    class BooleanAttributeInXmlStyleTest {
        private final String html = "<input type=\"checkbox\" checked=\"checked\">";

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            assertThat(htmlResult).isEqualToIgnoringWhitespace("<input type=\"checkbox\" checked=\"checked\">");
        }
    }

}