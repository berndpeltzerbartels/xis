package test.widget.title;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TitleWidgetIntegrationTest {
    @Test
    void widgetChangesPageTitle() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TitleWidget.class)
                .withSingleton(TitlePage.class)
                .build();
        var result = context.openPage(TitlePage.class);
        assertThat(result.getDocument().getTitle()).isEqualTo("Mein neuer Titel");
    }
}
