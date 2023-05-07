package one.xis.test.it;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    void test() {
        when(modelService.getModel()).thenReturn(new Model(1L, "Hello"));

        testContext.openPage("/model.html");

        assertThat(testContext.getDocument().getElementById("id").innerText).isEqualTo("1");
        assertThat(testContext.getDocument().getElementById("value").innerText).isEqualTo("Hello");
    }
}
