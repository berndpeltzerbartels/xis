package test;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ActionPageTest {

    private ActionPageService service;
    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        service = mock(ActionPageService.class);
        when(service.getDataList()).thenReturn(List.of(new ActionPageData(101, "bla1"), new ActionPageData(102, "bla2"), new ActionPageData(103, "bla3")));

        testContext = IntegrationTestContext.builder()
                .withSingleton(ActionPage.class)
                .withSingleton(service)
                .build();
    }

    @Test
    @Disabled
    void test() {
        testContext.openPage("/actionPage.html");
        testContext.getDocument().getElementById("link").onclick.accept(null);
        // redirct to index
        assertThat(testContext.getDocument().getElementByTagName("title").innerText).isEqualTo("Index");

        var captor = ArgumentCaptor.forClass(ActionPageData.class);
        verify(service, times(1)).update(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(102);
        assertThat(captor.getValue().getValue()).isEqualTo("bla2");

    }
}
