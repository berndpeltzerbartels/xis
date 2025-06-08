package test.security;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ProtectedModelDataTest {

    @Nested
    @DisplayName("Protected model data is accessed without token and login form is displayed")
    class AccessDeniedTest {
        private IntegrationTestContext testContext;

        @BeforeEach
        void init() {
            testContext = IntegrationTestContext.builder()
                    .withSingleton(ProtectedModelDataPage1.class)
                    .withSingleton(ProtectedModelDataPage2.class)
                    .build();
        }

        @Test
        void test() {
            var result = testContext.openPage("/page1.html");
            var document = result.getDocument();
            var link = document.getElementById("link");

            link.click();

            var content = document.asString();

        }
    }


}
