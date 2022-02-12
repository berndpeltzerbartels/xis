package one.xis.template;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextContentParserTest {

    @Test
    @DisplayName("Content with embedded expression")
    void parse() {
        var result = new MixedContentParser("123${a.b}456").parse();
        assertThat(result).containsExactly(new StaticContent("123"), expression("a.b"), new StaticContent("456"));
    }

    @Test
    @DisplayName("Expressions only")
    void parse1() {
        var result = new MixedContentParser("${a}${a.b}").parse();
        assertThat(result).containsExactly(expression("a"), expression("a.b"));
    }

    @Test
    @DisplayName("Escapes")
    void parse2() {
        var result = new MixedContentParser("\\${a}${a.b}").parse();
        assertThat(result).containsExactly(new StaticContent("${a}"), expression("a.b"));
    }


    @Test
    @DisplayName("All")
    void parse3() {
        var in = IOUtils.getResourceForClass(getClass(), "MixedContentParserTest.txt");
        var text = IOUtils.getContent(in, "UTF-8");
        var result = new MixedContentParser(text).parse();

        assertThat(result).containsExactly(new StaticContent("Das ist das "),//
                expression("Nikolaus.Haus"),//
                new StaticContent("bla"),//
                new StaticContent("bla"),//
                expression("format(x)"),//
                new StaticContent(" bla"),//
                new StaticContent("X_"),//
                expression("a.b.c"));
    }


    private ExpressionContent expression(String src) {
        return new ExpressionContent(new ExpressionParser().parse(src));
    }
}