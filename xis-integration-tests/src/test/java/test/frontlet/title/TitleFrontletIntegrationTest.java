package test.frontlet.title;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleFrontletIntegrationTest {
    @Test
    void frontletChangesPageTitle() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TitleFrontlet.class)
                .withSingleton(TitlePage.class)
                .build();
        var client = context.openPage(TitlePage.class);
        assertThat(client.getDocument().getTitle()).isEqualTo("Mein neuer Titel");
    }
}
