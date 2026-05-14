package test.frontlet;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.DocumentImpl;
import one.xis.test.dom.Element;
import one.xis.test.dom.ElementImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionLinkWithTargetFrontletTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionLinkWithTargetPage.class)
                .withSingleton(ActionLinkWithTargetFrontlet1.class)
                .withSingleton(ActionLinkWithTargetFrontlet2.class)
                .withSingleton(ActionLinkWithTargetFrontlet3.class)
                .withSingleton(ActionLinkWithTargetPage.class)
                .build();
    }

    @Test
    void action() {
        var client = testContext.openPage(ActionLinkWithTargetPage.class);

        var container1 = (ElementImpl) ((DocumentImpl) client.getDocument()).getDocumentElement().findDescendant(e -> e instanceof Element element && "container1".equals(element.getAttribute("container-id")));
        var container2 = (ElementImpl) ((DocumentImpl) client.getDocument()).getDocumentElement().findDescendant(e -> e instanceof Element element && "container2".equals(element.getAttribute("container-id")));

        assertThat(container1).isNotNull();
        assertThat(container2).isNotNull();

        // first frontlet
        var actionLink1 = container1.getElementById("action1");
        assertThat(actionLink1.getAttribute("xis:target-container")).isEqualTo("container2");
        assertThat(actionLink1).isNotNull();
        actionLink1.click();

        // second frontlet is displayed in second container
        var actionLink2 = container2.getElementById("action2");

        assertThat(actionLink2).isNotNull();
        actionLink2.click();

        // third frontlet is displayed in first container
        assertThat(container1.getElementById("frontlet3")).isNotNull();
    }

}
