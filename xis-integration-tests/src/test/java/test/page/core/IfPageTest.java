package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IfPageTest {

    private IntegrationTestContext testContext;
    private IfPage page;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(IfPage.class)
                .build();
        page = testContext.getAppContext().getSingleton(IfPage.class);
    }

    @Test
    void testCondition1True() {
        page.setCondition1(true);
        var result = testContext.openPage(IfPage.class);

        // Verify that the element for condition1 is rendered
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).contains("text1");
    }

    @Test
    void testCondition1False() {
        page.setCondition1(false);
        var result = testContext.openPage(IfPage.class);

        // Verify that the element for condition1 is not rendered
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).doesNotContain("text1");
    }

    @Test
    void testCondition2True() {
        page.setCondition2(true);
        var result = testContext.openPage(IfPage.class);

        // Verify that the element for condition2 is rendered
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).contains("text2");
    }

    @Test
    void testCondition2False() {
        page.setCondition2(false);
        var result = testContext.openPage(IfPage.class);

        // Verify that the element for condition2 is not rendered
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).doesNotContain("text2");
    }


    @Test
    void testSwitchCondition1() {
        page.setCondition1(false);
        var result = testContext.openPage(IfPage.class);

        // Verify that the element for condition1 is initially not visible
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).doesNotContain("text1");

        // Trigger the action to switch condition1
        result.getDocument().getElementById("link1").click();

        // Verify that the element for condition1 is now visible
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).contains("text1");

        // Trigger the action again to switch condition1
        result.getDocument().getElementById("link1").click();

        // Verify that the element for condition1 is not visible again
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).doesNotContain("text1");
    }

    @Test
    void testSwitchCondition2() {
        page.setCondition2(false);
        var result = testContext.openPage(IfPage.class);

        // Verify that the element for condition1 is initially not visible
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).doesNotContain("text2");

        // Trigger the action to switch condition1
        result.getDocument().getElementById("link2").click();

        // Verify that the element for condition1 is now visible
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).contains("text2");

        // Trigger the action again to switch condition1
        result.getDocument().getElementById("link2").click();

        // Verify that the element for condition1 is not visible again
        assertThat(result.getDocument().getElementByTagName("body").getInnerText()).doesNotContain("text2");
    }
}