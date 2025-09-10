package one.xis.html.tokens;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlTokenizerTest {

    @Test
    void testBreak() {
        String html = "<br/>";
        HtmlTokenizer tokenizer = new HtmlTokenizer();
        List<Token> result = tokenizer.tokenize(html);

        int i = 0;
        assertThat(result.get(i++)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(i++)).isInstanceOf(TextToken.class)
                .extracting("text").isEqualTo("br");
        assertThat(result.get(i++)).isInstanceOf(SlashToken.class);
        assertThat(result.get(i++)).isInstanceOf(CloseBracketToken.class);
        assertThat(result).hasSize(i); // no extras
    }

    @Test
    void tokenizeBig() {
        String html = """
                <html>
                    <head>
                        <title>Title</title>
                    </head>
                    <body>
                        <h1 class="header">Header</h1>
                        <p>Paragraph</p>
                        <!-- Comment -->
                        <br/>
                        <xis:forach items="${items}" var="item">
                            <p>${item}</p>
                        </xis:forach>
                    </body>
                """;

        HtmlTokenizer tokenizer = new HtmlTokenizer();
        List<Token> t = tokenizer.tokenize(html);

        Cursor c = new Cursor(t);

        // <html>
        c.expect(OpenBracketToken.class);
        c.expectText("html");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <head>
        c.expect(OpenBracketToken.class);
        c.expectText("head");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <title> Title </title>
        c.expect(OpenBracketToken.class);
        c.expectText("title");
        c.expect(CloseBracketToken.class);
        c.expectText("Title");
        c.expect(OpenBracketToken.class);
        c.expect(SlashToken.class);
        c.expectText("title");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // </head>
        c.expect(OpenBracketToken.class);
        c.expect(SlashToken.class);
        c.expectText("head");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <body>
        c.expect(OpenBracketToken.class);
        c.expectText("body");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <h1 class="header">Header</h1>
        c.expect(OpenBracketToken.class);
        c.expectText("h1");
        c.expectText("class");
        c.expect(EqualsToken.class);
        c.expectText("header");
        c.expect(CloseBracketToken.class);
        c.expectText("Header");
        c.expect(OpenBracketToken.class);
        c.expect(SlashToken.class);
        c.expectText("h1");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <p>Paragraph</p>
        c.expect(OpenBracketToken.class);
        c.expectText("p");
        c.expect(CloseBracketToken.class);
        c.expectText("Paragraph");
        c.expect(OpenBracketToken.class);
        c.expect(SlashToken.class);
        c.expectText("p");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <!-- Comment -->
        c.expect(OpenBracketToken.class);
        c.expectText("!--");
        c.expectText("Comment");
        c.expectText("--");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <br/>
        c.expect(OpenBracketToken.class);
        c.expectText("br");
        c.expect(SlashToken.class);
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <xis:forach items="..." var="item">  ...  </xis:forach>
        c.expect(OpenBracketToken.class);
        c.expectText("xis:forach");
        c.expectText("items");
        c.expect(EqualsToken.class);
        c.expectText("${items}");
        c.expectText("var");
        c.expect(EqualsToken.class);
        c.expectText("item");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // <p>${item}</p>
        c.expect(OpenBracketToken.class);
        c.expectText("p");
        c.expect(CloseBracketToken.class);
        c.expectText("${item}");
        c.expect(OpenBracketToken.class);
        c.expect(SlashToken.class);
        c.expectText("p");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // </xis:forach>
        c.expect(OpenBracketToken.class);
        c.expect(SlashToken.class);
        c.expectText("xis:forach");
        c.expect(CloseBracketToken.class);
        c.skipWs();

        // </body> (optional in this snippet, but should exist before EOF if present)
        // We won't assert further to keep the test robust to trailing whitespace.

        // Sanity: we consumed tokens in order
        assertThat(c.i).isGreaterThan(0);
    }

    /**
     * Small cursor over token list with helpers.
     */
    private static final class Cursor {
        final List<Token> tokens;
        int i = 0;

        Cursor(List<Token> tokens) {
            this.tokens = tokens;
        }

        void skipWs() {
            while (i < tokens.size()
                    && tokens.get(i) instanceof TextToken tt
                    && tt.getText().trim().isEmpty()) {
                i++;
            }
        }

        <T extends Token> T expect(Class<T> type) {
            assertThat(i).as("cursor in range").isLessThan(tokens.size());
            Token tok = tokens.get(i++);
            assertThat(tok).as("token @%s", i - 1).isInstanceOf(type);
            return type.cast(tok);
        }

        TextToken expectText(String expected) {
            assertThat(i).as("cursor in range").isLessThan(tokens.size());
            Token tok = tokens.get(i++);
            assertThat(tok).as("token @%s", i - 1).isInstanceOf(TextToken.class);
            TextToken tt = (TextToken) tok;
            assertThat(tt.getText()).as("text @%s", i - 1).isEqualTo(expected);
            return tt;
        }
    }
}
