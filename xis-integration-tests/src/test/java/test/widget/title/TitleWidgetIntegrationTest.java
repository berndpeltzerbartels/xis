package test.widget.title;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleWidgetIntegrationTest {
    @Test
    void widgetChangesPageTitle() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TitleFrontlet.class)
                .withSingleton(TitlePage.class)
                .build();
        var client = context.openPage(TitlePage.class);
        assertThat(client.getDocument().getTitle()).isEqualTo("Mein neuer Titel");
    }
}
