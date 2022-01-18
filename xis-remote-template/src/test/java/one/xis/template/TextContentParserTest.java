package one.xis.template;

import one.xis.template.TemplateModel.ContentElement;
import one.xis.template.TemplateModel.Expression;
import one.xis.template.TemplateModel.StaticContent;
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
        List<ContentElement> result = new TextContentParser("123${a.b}456").parse().getContentElements();
        assertThat(result).containsExactly(new StaticContent("123"), new Expression("a.b"), new StaticContent(List.of("456")));
    }

    @Test
    @DisplayName("Expressions only")
    void parse1() {
        List<ContentElement> result = new TextContentParser("${a}${a.b}").parse().getContentElements();
        assertThat(result).containsExactly(new Expression("a"), new Expression("a.b"));
    }

    @Test
    @DisplayName("Escapes")
    void parse2() {
        List<ContentElement> result = new TextContentParser("\\${a}${a.b}").parse().getContentElements();
        assertThat(result).containsExactly(new StaticContent("${a}"), new Expression("a.b"));
    }


    @Test
    @DisplayName("All")
    void parse3() throws IOException {
        InputStream in = IOUtils.getResourceForClass(getClass(), "MixedContentParserTest.txt");
        String text = IOUtils.getContent(in, "UTF-8");
        List<ContentElement> result = new TextContentParser(text).parse().getContentElements();

        assertThat(result).containsExactly(new StaticContent("Das ist das "),//
                new Expression("Nikolaus.Haus"),//
                new StaticContent(List.of("bla", "bla")),//
                new Expression("format(x)"),//
                new StaticContent(List.of("bla", "X_")),//
                new Expression("a.b.c"));
    }
}