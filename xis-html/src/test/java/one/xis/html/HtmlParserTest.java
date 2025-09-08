package one.xis.html;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlParserTest {

    private final HtmlParser parser = new HtmlParser();

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
    class AutocloseTest {
        private final String html = """
                <html>
                    <head>
                        <title>Test</title>
                        <link rel="stylesheet" href="styles.css">
                        <meta charset="UTF-8">
                    </head>
                    <body></body>
                </html>
                """;

        @Test
        void parse() {
            var document = parser.parse(html);
            var htmlResult = document.asString();

            //assertThat(htmlResult).isEqualToIgnoringWhitespace(this.html);
        }
    }


}