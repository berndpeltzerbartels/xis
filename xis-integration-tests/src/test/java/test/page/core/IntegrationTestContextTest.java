package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTestContextTest {

    @Test
    @DisplayName("Building the integration test context works for a simple page")
    void buildContext() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TitlePage.class)
                .build();

        assertThat(context).isNotNull();
    }

    @Test
    @DisplayName("Reusing compiled script does not throw an exception")
    void openPage() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TitlePage.class)
                .build();

        var client = context.openPage(TitlePage.class);
        assertThat(client.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Hello ! I am the title");

        context = IntegrationTestContext.builder()
                .withSingleton(IndexPage.class)
                .build();
        client = context.openPage(IndexPage.class);

        assertThat(client.getDocument().getElementByTagName("title").getInnerText()).isEqualTo("Index");


    }
}
