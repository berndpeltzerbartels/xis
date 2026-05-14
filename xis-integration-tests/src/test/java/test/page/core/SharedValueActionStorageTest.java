package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharedValueActionStorageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(SharedValueActionStoragePage.class)
                .build();
    }

    @Test
    void actionMutatedSharedValueIsUsedForModelRefreshAndStorage() {
        var client = testContext.openPage(SharedValueActionStoragePage.class);
        var page = testContext.getAppContext().getSingleton(SharedValueActionStoragePage.class);

        client.getDocument().getElementById("add").click();

        assertThat(client.getDocument().getElementById("moves").getInnerText()).isEqualTo("e2e4");
        assertThat(client.getLocalStorage().getItem("state")).contains("e2e4");
        assertThat(page.getInvocations()).containsExactly("state", "moves", "state", "add", "moves");
    }
}
