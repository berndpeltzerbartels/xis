package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import test.page.IndexPage;

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
        result.getDocument().getElementById("action-link1").onclick.accept(null);

        verify(service, times(2)).getData(); // 2 times, because action updates the data

        var captor = ArgumentCaptor.forClass(ActionLinkWidgetData.class);
        verify(service, times(1)).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }


    @Test
    void action2() {
        var result = testContext.openPage(WidgetPage.class);
        result.getDocument().getElementById("action-link2").onclick.accept(null);

        verify(service, times(2)).getData(); // 2 times, because after action, data has to be reloaded
        var captor = ArgumentCaptor.forClass(ActionLinkWidgetData.class);
        verify(service, times(1)).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");
    }

    @Test
    @DisplayName("LinkAction is a variable and link of a widget is clicked, but the result demands displaying a new page")
    void action3() {
        var result = testContext.openPage(WidgetPage.class);
        result.getDocument().getElementById("action-link3").onclick.accept(null); // "action-link3"is set by model variable "action3"


        verify(service, times(1)).getData(); // once, because a new page was loaded after action
        // redirct to index
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("Index");
        assertThat(testContext.getSingleton(IndexPage.class).getInvocations()).isEqualTo(1); // model from next page has to be loaded
        var captor = ArgumentCaptor.forClass(ActionLinkWidgetData.class);
        verify(service, times(1)).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }

    @Test
    @DisplayName("LinkAction is a variable and link of a widget is clicked and demands displaying another widget")
    void action4() {
        var result = testContext.openPage(WidgetPage.class);
        result.getDocument().getElementById("action-link4").onclick.accept(null); // "action-link3"is set by model variable "action3"

        assertThat(result.getDocument().getElementById("greeting").innerText).isEqualTo("Huhu !");

    }
}
