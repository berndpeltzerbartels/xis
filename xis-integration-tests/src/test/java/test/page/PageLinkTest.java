package test.page;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageLinkTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(PageLink1.class)
                .withSingleton(PageLink2.class)
                .build();
    }

    @Test
    @DisplayName("Click link to another page, then click the link back to first page")
    void test() {
        var controller1 = testContext.getSingleton(PageLink1.class);
        var controller2 = testContext.getSingleton(PageLink2.class);

        var result = testContext.openPage("/pageLink1.html"); // open link 1 first time
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("PageLink1");
        assertThat(controller1.getInvocations()).isEqualTo(1);
        assertThat(result.getDocument().getElementById("link1").innerText).isEqualTo("Link to PageLink2");

        result.getDocument().getElementById("link1").click(); // go to PageLink2.html
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("PageLink2");
        assertThat(controller2.getInvocations()).isEqualTo(1);

        result.getDocument().getElementById("link2").click(); // go back to PageLink1.html
        assertThat(controller1.getInvocations()).isEqualTo(2);
    }
}
