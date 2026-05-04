package test.page.el.expression;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionLanguageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ExpressionLanguagePage.class)
                .build();
    }

    @Test
    void evaluatesComputedBracketPropertyInTextAndAttributes() {
        var client = testContext.openPage("/expressionLanguage.html");

        assertThat(client.getDocument().getElementById("computed-key-text").getInnerText()).isEqualTo("First");
        assertThat(client.getDocument().getElementById("computed-key-attribute").getAttribute("data-label")).isEqualTo("First");
    }

    @Test
    void evaluatesConditionsAndBuiltInFunctions() {
        var client = testContext.openPage("/expressionLanguage.html");

        assertThat(client.getDocument().getElementById("comparison-condition")).isNotNull();
        assertThat(client.getDocument().getElementById("tag-if-result")).isNotNull();
        assertThat(client.getDocument().getElementById("default-value").getInnerText()).isEqualTo("fallback");
        assertThat(client.getDocument().getElementById("joined-items").getInnerText()).isEqualTo("alpha, beta, gamma");
    }

    @Test
    void evaluatesTagAndAttributeForeachSyntax() {
        var client = testContext.openPage("/expressionLanguage.html");
        var bodyText = client.getDocument().getElementByTagName("body").getInnerText();

        assertThat(bodyText).contains("alpha", "beta", "gamma");
        assertThat(client.getDocument().getElementById("tag-foreach").getInnerHTML())
                .contains("Keyboard", "Monitor", "data-id=\"p3\"");
    }
}
