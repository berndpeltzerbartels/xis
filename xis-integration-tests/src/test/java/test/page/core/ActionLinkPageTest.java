package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        result.getDocument().getElementById("action-link1").click();
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");
        verify(service, times(2)).getData();

        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(123);
        assertThat(captor.getValue().getValue()).isEqualTo("value1");
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");

    }


    @Test
    void action2() {
        var result = testContext.openPage("/actionPage.html");
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");
        result.getDocument().getElementById("action-link2").click();

        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");
        verify(service, times(2)).getData();
        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service, times(1)).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }

    @Test
    void action3() {
        var result = testContext.openPage("/actionPage.html");
        result.getDocument().getElementById("action-link3").click(); // "action-link3" is set by model variable "action3"


        verify(service).getData(); // once, because a new page was loaded after action
        // redirect to index
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("Index");
        assertThat(testContext.getSingleton(IndexPage.class).getInvocations()).isEqualTo(1); // model from next page has to be loaded
        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("value-test-action3");

    }
}
