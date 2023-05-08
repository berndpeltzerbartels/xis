package test;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SubmitOldDataTest {

    private IntegrationTestContext testContext;
    private ModelService modelService;

    @BeforeEach
    void init() {
        modelService = Mockito.mock(ModelService.class);
        testContext = IntegrationTestContext.builder()
                .withSingleton(ModelPage.class)
                .withSingleton(IndexPage.class)
                .withSingleton(modelService)
                .build();

    }

    @Test
    @DisplayName("Update data on model page, click link to index-page, click link back to model page and old data from previos visit is submitted ")
    void test() {
        doAnswer(invocation -> {
            var model = (Model) invocation.getArgument(0);
            model.setId(1L);
            model.setValue("Hello");
            return Void.class;
        }).when(modelService).updateModel(any());
        testContext.openPage("/model.html");
        var doc = testContext.getDocument();
        assertThat(doc.getElementById("id").innerText).isEqualTo("1");
        assertThat(doc.getElementById("value").innerText).isEqualTo("Hello");

        doc.getElementById("index-link").onclick.accept(null);
        // Check we are displaying the index-page
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Index");

        doc.getElementById("model-link").onclick.accept(null);
        // Check we are back
        assertThat(doc.getElementByTagName("title").innerText).isEqualTo("Model");

        var capturer = ArgumentCaptor.forClass(Model.class);
        verify(modelService, times(2)).updateModel(capturer.capture());


        var model2 = capturer.getAllValues().get(1);

        //  assertThat(model1).isNull();
        assertThat(model2).isNotNull();
        assertThat(model2.getId()).isEqualTo(1L);
        assertThat(model2.getValue()).isEqualTo("Hello");
    }
}
