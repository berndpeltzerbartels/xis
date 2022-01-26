package one.xis.template;

import one.xis.template.TemplateModel.Expression;
import one.xis.template.TemplateModel.StaticText;
import one.xis.template.TemplateModel.TextElement;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextContentParserTest {

    @Test
    @DisplayName("Content with embedded expression")
    void parse() {
        List<TextElement> result = new TextContentParser("123${a.b}456").parse().getTextElements();
        assertThat(result).containsExactly(new StaticText("123"), new Expression("a.b"), new StaticText(List.of("456")));
    }

    @Test
    @DisplayName("Expressions only")
    void parse1() {
        List<TextElement> result = new TextContentParser("${a}${a.b}").parse().getTextElements();
        assertThat(result).containsExactly(new Expression("a"), new Expression("a.b"));
    }

    @Test
    @DisplayName("Escapes")
    void parse2() {
        List<TextElement> result = new TextContentParser("\\${a}${a.b}").parse().getTextElements();
        assertThat(result).containsExactly(new StaticText("${a}"), new Expression("a.b"));
    }


    @Test
    @DisplayName("All")
    void parse3() throws IOException {
        InputStream in = IOUtils.getResourceForClass(getClass(), "MixedContentParserTest.txt");
        String text = IOUtils.getContent(in, "UTF-8");
        List<TextElement> result = new TextContentParser(text).parse().getTextElements();

        assertThat(result).containsExactly(new StaticText("Das ist das "),//
                new Expression("Nikolaus.Haus"),//
                new StaticText(List.of("bla", "bla")),//
                new Expression("format(x)"),//
                new StaticText(List.of("bla", "X_")),//
                new Expression("a.b.c"));
    }
}