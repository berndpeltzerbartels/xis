package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SharedValueActionRefreshTest {

    @Test
    void actionClearsSharedValuesBeforePostActionModelDataRefresh() {
        var service = new SharedValueActionRefreshService();
        var testContext = IntegrationTestContext.builder()
                .withSingleton(SharedValueActionRefreshPage.class)
                .withSingleton(service)
                .build();
        var client = testContext.openPage(SharedValueActionRefreshPage.class);

        client.getDocument().getElementById("update").click();

        assertThat(client.getDocument().getElementById("value").getInnerText()).isEqualTo("updated");
        assertThat(service.invocations()).containsExactly("value:initial", "update:updated", "value:updated");
    }
}
