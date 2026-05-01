package test.widget.metadata;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetMetadataTest {

    private IntegrationTestContext testContext;
    private IntegrationTestContext deepLinkContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(WidgetMetadataPage.class)
                .withSingleton(WidgetMetadataFrontlet.class)
                .withSingleton(SecondFrontlet.class)
                .build();
        
        deepLinkContext = IntegrationTestContext.builder()
                .withSingleton(DeepLinkingPage.class)
                .withSingleton(WidgetMetadataFrontlet.class)
                .withSingleton(DeepLinkFrontlet.class)
                .build();
    }

    @Test
    void widgetMetadataIsExtractedFromAnnotation() {
        var client = testContext.openPage(WidgetMetadataPage.class);
        
        // Verify that widget content was loaded via default-widget
        var messageElement = client.getDocument().getElementById("message");
        assertThat(messageElement).isNotNull();
        assertThat(messageElement.getTextContent()).isEqualTo("Frontlet with metadata");
        
        // Verify document title was updated with widget's annotatedTitle
        assertThat(client.getDocument().getTitle()).isEqualTo("Custom Frontlet Title");
    }

    @Test
    void widgetMetadataChangesAfterAction() {
        var client = testContext.openPage(WidgetMetadataPage.class);
        
        // Verify initial widget is loaded
        var messageElement = client.getDocument().getElementById("message");
        assertThat(messageElement).isNotNull();
        assertThat(client.getDocument().getTitle()).isEqualTo("Custom Frontlet Title");
        
        // Click button to load second widget
        var loadSecondButton = client.getDocument().getElementById("loadSecond");
        assertThat(loadSecondButton).isNotNull();
        loadSecondButton.click();
        
        // Verify second widget is loaded
        var secondMessageElement = client.getDocument().getElementById("secondMessage");
        assertThat(secondMessageElement).isNotNull();
        assertThat(secondMessageElement.getTextContent()).isEqualTo("Second widget loaded");
        
        // Verify metadata was updated from second widget's annotation
        assertThat(client.getDocument().getTitle()).isEqualTo("Second Frontlet Title");
    }

    @Test
    void annotationContainerIdOverridesDefaultWidget() {
        // This test verifies deep linking: when a widget specifies a containerId in its annotation,
        // it should override any default-widget attribute in the HTML template.
        // 
        // Scenario:
        // - HTML has containerA with default-widget="WidgetMetadata"
        // - HTML has containerB with no default-widget
        // - DeepLinkFrontlet has @Frontlet(containerId="containerB")
        // 
        // Expected: When DeepLinkFrontlet is loaded via action, it should go to containerB
        //           (annotation wins), not containerA (default-widget)
        
        var client = deepLinkContext.openPage(DeepLinkingPage.class);
        
        // Container A should have WidgetMetadata (from default-widget attribute)
        var containerA = client.getDocument().getElementById("containerA");
        assertThat(containerA).isNotNull();
        var messageInA = containerA.querySelector("#message");
        assertThat(messageInA).isNotNull();
        assertThat(messageInA.getTextContent()).isEqualTo("Frontlet with metadata");
        
        // Container B should be empty initially
        var containerB = client.getDocument().getElementById("containerB");
        assertThat(containerB).isNotNull();
        assertThat(containerB.getTextContent().trim()).isEmpty();
        
        // Click button to load DeepLinkWidget
        var loadDeepLinkButton = client.getDocument().getElementById("loadDeepLink");
        assertThat(loadDeepLinkButton).isNotNull();
        loadDeepLinkButton.click();
        
        // Verify DeepLinkFrontlet loaded into containerB (annotation wins over default-widget)
        var deepLinkMessage = containerB.querySelector("#deepLinkMessage");
        assertThat(deepLinkMessage).isNotNull();
        assertThat(deepLinkMessage.getTextContent()).isEqualTo("Deep link widget in container B");
        
        // Verify containerA still has original widget (not replaced)
        messageInA = containerA.querySelector("#message");
        assertThat(messageInA).isNotNull();
        assertThat(messageInA.getTextContent()).isEqualTo("Frontlet with metadata");
        
        // Verify document title was updated from DeepLinkFrontlet annotation
        assertThat(client.getDocument().getTitle()).isEqualTo("Deep Link Title");
    }
}
