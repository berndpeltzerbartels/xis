package test.frontlet.parentdata;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ParentDataTest {
    @Test
    void parentDataIsAvailableInPageAndFrontlets() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TestPage.class)
                .withSingleton(DefaultFrontlet.class)
                .withSingleton(SecondFrontlet.class)
                .build();
        var client = context.openPage(TestPage.class);
        // Check data on page
        assertThat(client.getDocument().getElementById("page-data").getInnerText()).isEqualTo("parent123");
        // Check data on default frontlet
        assertThat(client.getDocument().getElementById("default-frontlet-data").getInnerText()).isEqualTo("parent123");
        // Click link to load second frontlet
        client.getDocument().getElementById("frontlet-link").click();
        // Check data on second frontlet
        assertThat(client.getDocument().getElementById("second-frontlet-data").getInnerText()).isEqualTo("parent123");
        // Ensure second frontlet is loaded
        assertThat(client.getDocument().getElementById("second-frontlet-data")).isNotNull();
    }
}
