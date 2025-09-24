package test.widget;


import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.page.core.NoModel;

@DisplayName("Page without Model")
class NoModelTest {

    private IntegrationTestContext testContext;
    private ClientStatePage page;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(NoModel.class)
                .build();
    }


    @Test
    void test() {
        testContext.openPage(NoModel.class);
    }
}
