package test;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ActionLinkPageTest {

    private ActionPageService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(ActionPageService.class);
        when(service.getData()).thenReturn(new ActionLinkPageData(101, "bla"));

        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionLinkPage.class)
                .withSingleton(service)
                .withSingleton(IndexPage.class) // new page in action3
                .build();
    }

    @Test
    void action1() {
        testContext.openPage("/actionPage.html");
        testContext.getDocument().getElementById("action-link1").onclick.accept(null);

        assertThat(testContext.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");

        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service, times(1)).update(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }


    @Test
    void action2() {
        testContext.openPage("/actionPage.html");
        testContext.getDocument().getElementById("action-link2").onclick.accept(null);

        assertThat(testContext.getDocument().getElementByTagName("title").innerText).isEqualTo("ActionPage");

        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service, times(1)).update(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }

    @Test
    void action3() {
        testContext.openPage("/actionPage.html");
        testContext.getDocument().getElementById("action-link3").onclick.accept(null);
        // redirct to index
        assertThat(testContext.getDocument().getElementByTagName("title").innerText).isEqualTo("Index");

        var captor = ArgumentCaptor.forClass(ActionLinkPageData.class);
        verify(service, times(1)).update(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(101);
        assertThat(captor.getValue().getValue()).isEqualTo("bla");

    }
}
