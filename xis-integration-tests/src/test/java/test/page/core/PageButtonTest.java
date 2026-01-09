package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageButtonTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(PageButton1.class)
                .withSingleton(PageButton2.class)
                .build();
    }

    @Test
    @DisplayName("Click button to another page, then click the button back to first page")
    void test() {
        var controller1 = testContext.getSingleton(PageButton1.class);
        var controller2 = testContext.getSingleton(PageButton2.class);

        var result = testContext.openPage("/pageButton1.html"); // open page 1 first time
        assertThat(result.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("PageButton1");
        assertThat(controller1.getInvocations()).isEqualTo(1);
        assertThat(result.getDocument().getElementById("button1").getInnerText()).isEqualTo("Go to PageButton2");

        result.getDocument().getElementById("button1").click(); // go to PageButton2.html
        assertThat(result.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("PageButton2");
        assertThat(controller2.getInvocations()).isEqualTo(1);

        result.getDocument().getElementById("button2").click(); // go back to PageButton1.html
        assertThat(controller1.getInvocations()).isEqualTo(2);
    }
}
