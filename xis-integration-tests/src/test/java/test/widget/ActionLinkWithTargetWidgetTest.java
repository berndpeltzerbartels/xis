package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActionLinkWithTargetWidgetTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionLinkWithTargetPage.class)
                .withSingleton(ActionLinkWithTargetWidget1.class)
                .withSingleton(ActionLinkWithTargetWidget2.class)
                .withSingleton(ActionLinkWithTargetWidget3.class)
                .withSingleton(ActionLinkWithTargetPage.class)
                .build();
    }

    @Test
    void action1() {
        var result = testContext.openPage(ActionLinkWithTargetPage.class);

        var container1 = result.getDocument().findElement(e -> "container1".equals(e.getAttribute("container-id")));
        var container2 = result.getDocument().findElement(e -> "container2".equals(e.getAttribute("container-id")));

        assertThat(container1).isNotNull();
        assertThat(container2).isNotNull();

        System.out.println("************** 1 *************");
        System.out.println(result.getDocument().asString());
        // first widget
        var actionLink1 = container1.getDescendantById("action1");
        assertThat(actionLink1).isNotNull();
        actionLink1.onclick.accept(null);

        System.out.println("************** 2 *************");
        System.out.println(result.getDocument().asString());
        // second widget is displayed in second container
        var actionLink2 = container2.getDescendantById("action2");
        assertThat(actionLink2).isNotNull();
        actionLink2.onclick.accept(null);

        System.out.println("************** 3 *************");
        System.out.println(result.getDocument().asString());
        // third widget is displayed in first container
        assertThat(container1.getDescendantById("widget3")).isNotNull();
    }

}
