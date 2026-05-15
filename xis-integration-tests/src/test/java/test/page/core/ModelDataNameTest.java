package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelDataNameTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ModelDataNamePage.class)
                .build();
    }

    @Test
    void resolvesExplicitMethodAndGetterNames() {
        var client = testContext.openPage(ModelDataNamePage.class);

        assertThat(client.getDocument().getElementById("explicit").getInnerText()).isEqualTo("explicit");
        assertThat(client.getDocument().getElementById("method").getInnerText()).isEqualTo("method");
        assertThat(client.getDocument().getElementById("getter").getInnerText()).isEqualTo("getter");
        assertThat(client.getDocument().getElementById("pipeline").getInnerText()).isEqualTo("pipeline");
        assertThat(client.getDocument().getElementById("plain-get").getInnerText()).isEqualTo("plain-get");
        assertThat(client.getDocument().getElementById("getty-images").getInnerText()).isEqualTo("getty");
    }
}
