package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalStorageTest {

    private IntegrationTestContext testContext;
    private LocalStoragePage page;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(LocalStoragePage.class)
                .build();
    }

    @Test
    void localStorageUpdateTest() {
        var result = testContext.openPage(LocalStoragePage.class);
        var page = testContext.getAppContext().getSingleton(LocalStoragePage.class);

        result.getDocument().getElementById("update-link").click();
        assertThat(page.getInvokedMethods()).containsExactly("data", "updateAction");
        assertThat(page.getLocalStoragePageData().getId()).isEqualTo(600);
        assertThat(page.getLocalStoragePageData().getValue()).isEqualTo("updatedLocalTest");
        assertThat(result.getLocalStorage().getItem("data")).contains("\"id\":600");
        assertThat(result.getLocalStorage().getItem("data")).contains("\"value\":\"updatedLocalTest\"");

        assertThat(result.getDocument().getElementById("localStorageValue").innerText).isEqualTo("600");
    }
}