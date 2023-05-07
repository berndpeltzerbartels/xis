package test;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ModelPageTest {

    private IntegrationTestContext testContext;
    private ModelService modelService;

    @BeforeEach
    void init() {
        modelService = Mockito.mock(ModelService.class);
        testContext = IntegrationTestContext.builder()
                .withSingleton(ModelPage.class)
                .withSingleton(modelService)
                .build();

    }

    @Test
    @DisplayName("Retrieve default model-value and send model to client an retrieve it on second call")
    void test() {
        doAnswer(invocation -> {
            var model = (Model) invocation.getArgument(0);
            model.setId(1L);
            model.setValue("Hello");
            return Void.class;
        }).when(modelService).updateModel(any());

        testContext.openPage("/model.html");

        assertThat(testContext.getDocument().getElementById("id").innerText).isEqualTo("1");
        assertThat(testContext.getDocument().getElementById("value").innerText).isEqualTo("Hello");

        testContext.openPage("/model.html");

        var capturer = ArgumentCaptor.forClass(Model.class);
        verify(modelService, times(2)).updateModel(capturer.capture());

        var model1 = capturer.getAllValues().get(0);
        var model2 = capturer.getAllValues().get(0);

        assertThat(model1).isNull();
        assertThat(model2).isNotNull();
        assertThat(model2.getId()).isEqualTo(1L);
        assertThat(model2.getValue()).isEqualTo("Hello");
    }
}
