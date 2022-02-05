package one.xis.template;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionParserTest {

    private ExpressionParser parser = new ExpressionParser();

    @Test
    void noFunction() {
        Expression result = parser.parse("x.y");

        assertThat(result.getContent()).isEqualTo("x.y");
        assertThat(result.getVars()).hasSize(1);
        assertThat(result.getVars().get(0)).isEqualTo(new ExpressionVar("x.y"));
        assertThat(result.getFunction()).isNull();
    }

    @Test
    void withFunction() {
        Expression result = parser.parse("format(x.y)");

        assertThat(result.getContent()).isEqualTo("format(x.y)");
        assertThat(result.getVars()).hasSize(1);
        assertThat(result.getVars().get(0)).isEqualTo(new ExpressionVar("x.y"));
        assertThat(result.getFunction()).isEqualTo("format");
    }

    @Test
    void withFunctionAndStringParam() {
        Expression result = parser.parse("format(x.y, 'DE')");

        assertThat(result.getContent()).isEqualTo("format(x.y, 'DE')");
        assertThat(result.getVars()).hasSize(2);
        assertThat(result.getVars().get(0)).isEqualTo(new ExpressionVar("x.y"));
        assertThat(result.getVars().get(1)).isEqualTo(new ExpressionString("'DE')"));
        assertThat(result.getFunction()).isEqualTo("format");
    }
}