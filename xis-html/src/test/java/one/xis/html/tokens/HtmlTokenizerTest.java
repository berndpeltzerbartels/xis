package one.xis.html.tokens;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlTokenizerTest {

    @Test
    void testBreak() {
        String html = "<br/>";
        HtmlTokenizer tokenizer = new HtmlTokenizer();
        var result = tokenizer.tokenize(html);
        assertThat(result).hasSize(4);
        assertThat(result.get(0)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(1)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("br");
        assertThat(result.get(2)).isInstanceOf(SlashToken.class);
        assertThat(result.get(3)).isInstanceOf(CloseBracketToken.class);
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
        var result = tokenizer.tokenize(html);
        assertThat(result.get(0)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(1)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("html");
        assertThat(result.get(2)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(3)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(4)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("head");
        assertThat(result.get(5)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(6)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(7)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("title");
        assertThat(result.get(8)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(9)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("Title");
        assertThat(result.get(10)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(11)).isInstanceOf(SlashToken.class);
        assertThat(result.get(12)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("title");
        assertThat(result.get(13)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(14)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(15)).isInstanceOf(SlashToken.class);
        assertThat(result.get(16)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("head");
        assertThat(result.get(17)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(18)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(19)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("body");
        assertThat(result.get(20)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(21)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(22)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("h1");
        assertThat(result.get(23)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("class");
        assertThat(result.get(24)).isInstanceOf(EqualsToken.class);
        assertThat(result.get(25)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("\"header\"");
        assertThat(result.get(26)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(27)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("Header");
        assertThat(result.get(28)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(29)).isInstanceOf(SlashToken.class);
        assertThat(result.get(30)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("h1");
        assertThat(result.get(31)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(32)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(33)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("p");
        assertThat(result.get(34)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(35)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("Paragraph");
        assertThat(result.get(36)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(37)).isInstanceOf(SlashToken.class);
        assertThat(result.get(38)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("p");
        assertThat(result.get(39)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(40)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(41)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("!--");
        assertThat(result.get(42)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("Comment");
        assertThat(result.get(43)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("--");
        assertThat(result.get(44)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(45)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(46)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("br");
        assertThat(result.get(47)).isInstanceOf(SlashToken.class);
        assertThat(result.get(48)).isInstanceOf(CloseBracketToken.class);
        assertThat(result.get(49)).isInstanceOf(OpenBracketToken.class);
        assertThat(result.get(50)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("xis:forach");
        assertThat(result.get(51)).isInstanceOf(TextToken.class).extracting("text").isEqualTo("items");

        // check complete result with assertJ. Check content of texttokens, always
    }
}