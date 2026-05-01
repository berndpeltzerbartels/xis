package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.page.core.IndexPage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ActionLinkFrontletTest {

    private ActionLinkWidgetService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(ActionLinkWidgetService.class);
        when(service.getData()).thenReturn(new ActionLinkWidgetData(101, "bla"));

        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionLinkFrontlet.class)
                .withSingleton(SimpleFrontlet.class)
                .withSingleton(WidgetPage.class)
                .withSingleton(service)
                .withSingleton(IndexPage.class) // new page in action3
                .build();

        testContext.getSingleton(WidgetPage.class).setWidgetId("ActionLinkFrontlet");
    }

    @Test
    void action1() {
        var client = testContext.openPage(WidgetPage.class);

        client.getDocument().getElementById("action-link1").click();
        // Model data is loaded once initially, and once after action
        verify(service, times(2)).getData();
        verify(service, times(1)).action("value1");
    }


    @Test
    void action2() {
        var client = testContext.openPage(WidgetPage.class);
        client.getDocument().getElementById("action-link2").click();

        verify(service, times(2)).getData();
        verify(service, times(1)).action(new Object[]{101, "bla"});
    }

    @Test
    @DisplayName("Action is a variable and link of a widget is clicked, but the result demands displaying a new page")
    void action3() {
        var client = testContext.openPage(WidgetPage.class);
        client.getDocument().getElementById("action-link3").click(); // "action-link3"is set by model variable "action3"


        verify(service, times(1)).getData(); // once, because a new page was loaded after action
        // redirect to index
        assertThat(client.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Index");
        assertThat(testContext.getSingleton(IndexPage.class).getInvocations()).isEqualTo(1); // model from next page has to be loaded;
    }

    @Test
    @DisplayName("Action demands displaying another widget")
    void action4() {
        var client = testContext.openPage(WidgetPage.class);
        client.getDocument().getElementById("action-link4").click(); // "action-link3"is set by model variable "action3"

        assertThat(client.getDocument().getElementById("greeting").getInnerText()).isEqualTo("Huhu !");

    }
}
