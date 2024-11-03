package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.page.core.IndexPage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ActionLinkWidgetTest {

    private ActionLinkWidgetService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(ActionLinkWidgetService.class);
        when(service.getData()).thenReturn(new ActionLinkWidgetData(101, "bla"));

        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionLinkWidget.class)
                .withSingleton(SimpleWidget.class)
                .withSingleton(WidgetPage.class)
                .withSingleton(service)
                .withSingleton(IndexPage.class) // new page in action3
                .build();

        testContext.getSingleton(WidgetPage.class).setWidgetId("ActionLinkWidget");
    }

    @Test
    void action1() {
        var result = testContext.openPage(WidgetPage.class);

        result.getDocument().getElementById("action-link1").click();

        verify(service, times(1)).getData();
        verify(service, times(1)).action("value1");
    }


    @Test
    void action2() {
        var result = testContext.openPage(WidgetPage.class);
        result.getDocument().getElementById("action-link2").click();

        verify(service, times(1)).getData();
        verify(service, times(1)).action(new Object[]{101, "bla"});
    }

    @Test
    @DisplayName("Action is a variable and link of a widget is clicked, but the result demands displaying a new page")
    void action3() {
        var result = testContext.openPage(WidgetPage.class);
        result.getDocument().getElementById("action-link3").click(); // "action-link3"is set by model variable "action3"


        verify(service, times(1)).getData(); // once, because a new page was loaded after action
        // redirect to index
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("Index");
        assertThat(testContext.getSingleton(IndexPage.class).getInvocations()).isEqualTo(1); // model from next page has to be loaded;
    }

    @Test
    @DisplayName("Action demands displaying another widget")
    void action4() {
        var result = testContext.openPage(WidgetPage.class);
        result.getDocument().getElementById("action-link4").click(); // "action-link3"is set by model variable "action3"

        assertThat(result.getDocument().getElementById("greeting").innerText).isEqualTo("Huhu !");

    }
}
