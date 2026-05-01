package test.widget.parentdata;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ParentDataTest {
    @Test
    void parentDataIsAvailableInPageAndWidgets() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TestPage.class)
                .withSingleton(DefaultWidget.class)
                .withSingleton(SecondWidget.class)
                .build();
        var client = context.openPage(TestPage.class);
        // Check data on page
        assertThat(client.getDocument().getElementById("page-data").getInnerText()).isEqualTo("parent123");
        // Check data on default widget
        assertThat(client.getDocument().getElementById("default-widget-data").getInnerText()).isEqualTo("parent123");
        // Click link to load second widget
        client.getDocument().getElementById("widget-link").click();
        // Check data on second widget
        assertThat(client.getDocument().getElementById("second-widget-data").getInnerText()).isEqualTo("parent123");
        // Ensure second widget is loaded
        assertThat(client.getDocument().getElementById("second-widget-data")).isNotNull();
    }
}
