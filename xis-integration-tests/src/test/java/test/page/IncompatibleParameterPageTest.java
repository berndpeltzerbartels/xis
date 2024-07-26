package test.page;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IncompatibleParameterPageTest {

    @Nested
    class SimpleIncompatibleParameterTest {

        private IntegrationTestContext appContext;

        @BeforeEach
        void initContext() {
            appContext = IntegrationTestContext.builder()
                    .withSingleton(IncompatibleParameterPage.class)
                    .build();
        }


        @Test
        void test() {
            var result = appContext.openPage(IncompatibleParameterPage.class);
            var document = result.getDocument();
            var button = document.getElementByTagName("button");
            button.click();
        }

    }
}
