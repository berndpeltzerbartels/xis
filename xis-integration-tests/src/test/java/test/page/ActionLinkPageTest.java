package test.page;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

//@Disabled // TODO
class ActionLinkPageTest {

    private ActionLinkPageService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(ActionLinkPageService.class);
        when(service.getData()).thenReturn(new ActionLinkPageData(101, "bla"));

        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionLinkPage.class)
                .withSingleton(service)
                .withSingleton(IndexPage.class) // new page in action3
                .build();
    }

    @Test
    void action1() {
        var result = testContext.openPage("/actionPage.html");
        result.getDocument().getElementById("action-link1").onclick.accept(null);

        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");
        verify(service, times(2)).getData(); // 2 times, because after action, data has to be reloaded

        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service, times(1)).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }


    @Test
    void action2() {
        var result = testContext.openPage("/actionPage.html");
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");
        result.getDocument().getElementById("action-link2").onclick.accept(null);

        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");
        verify(service, times(2)).getData(); // 2 times, because after action, data has to be reloaded
        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service, times(1)).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }

    @Test
    void action3() {
        var result = testContext.openPage("/actionPage.html");
        result.getDocument().getElementById("action-link3").onclick.accept(null); // "action-link3"is set by model variable "action3"


        verify(service, times(1)).getData(); // once, because a new page was loaded after action
        // redirct to index
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("Index");
        assertThat(testContext.getSingleton(IndexPage.class).getInvocations()).isEqualTo(1); // model from next page has to be loaded
        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service, times(1)).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }
}
