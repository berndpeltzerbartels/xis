package test.page.el.ifcondition;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IfConditionTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(IfConditionPage.class)
                .build();
    }

    @Test
    @DisplayName("Test aller Bedingungen auf IfConditionPage")
    void testConditions() {
        var result = testContext.openPage("/ifCondition.html");

        var body = result.getDocument().getElementByTagName("body");
        assertThat(body).isNotNull();

        // 1. <xis:if condition="true">
        assertThat(body.getInnerText()).contains("Simple true");

        // 2. <xis:if condition="notEmpty('123')">
        assertThat(body.getInnerText()).contains("True with function");

        // 3. <xis:if condition="notEmpty(data)">
        assertThat(body.getInnerText()).contains("True with function and variable");

        // 4. <div id="condition-tag1" xis:if="notEmpty(data)">
        Element tag1 = result.getDocument().getElementById("condition-tag1");
        assertThat(tag1).isNotNull();
        assertThat(tag1.getInnerText()).isEqualTo("True with function and variable in attribute style");

        // 5. <div id="condition-tag2" xis:if="empty(data)">
        Element tag2 = result.getDocument().getElementById("condition-tag2");
        assertThat(tag2.getTextContent()).isEmpty();
    }
}
