package test.frontlet.metadata;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletMetadataTest {

    private IntegrationTestContext testContext;
    private IntegrationTestContext deepLinkContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(FrontletMetadataPage.class)
                .withSingleton(FrontletMetadataFrontlet.class)
                .withSingleton(SecondFrontlet.class)
                .build();
        
        deepLinkContext = IntegrationTestContext.builder()
                .withSingleton(DeepLinkingPage.class)
                .withSingleton(FrontletMetadataFrontlet.class)
                .withSingleton(DeepLinkFrontlet.class)
                .build();
    }

    @Test
    void frontletMetadataIsExtractedFromAnnotation() {
        var client = testContext.openPage(FrontletMetadataPage.class);
        
        // Verify that frontlet content was loaded via default-frontlet
        var messageElement = client.getDocument().getElementById("message");
        assertThat(messageElement).isNotNull();
        assertThat(messageElement.getTextContent()).isEqualTo("Frontlet with metadata");
        
        // Verify document title was updated with frontlet's annotatedTitle
        assertThat(client.getDocument().getTitle()).isEqualTo("Custom Frontlet Title");
    }

    @Test
    void frontletMetadataChangesAfterAction() {
        var client = testContext.openPage(FrontletMetadataPage.class);
        
        // Verify initial frontlet is loaded
        var messageElement = client.getDocument().getElementById("message");
        assertThat(messageElement).isNotNull();
        assertThat(client.getDocument().getTitle()).isEqualTo("Custom Frontlet Title");
        
        // Click button to load second frontlet
        var loadSecondButton = client.getDocument().getElementById("loadSecond");
        assertThat(loadSecondButton).isNotNull();
        loadSecondButton.click();
        
        // Verify second frontlet is loaded
        var secondMessageElement = client.getDocument().getElementById("secondMessage");
        assertThat(secondMessageElement).isNotNull();
        assertThat(secondMessageElement.getTextContent()).isEqualTo("Second frontlet loaded");
        
        // Verify metadata was updated from second frontlet's annotation
        assertThat(client.getDocument().getTitle()).isEqualTo("Second Frontlet Title");
    }

    @Test
    void annotationContainerIdOverridesDefaultFrontlet() {
        // This test verifies deep linking: when a frontlet specifies a containerId in its annotation,
        // it should override any default-frontlet attribute in the HTML template.
        // 
        // Scenario:
        // - HTML has containerA with default-frontlet="FrontletMetadata"
        // - HTML has containerB with no default-frontlet
        // - DeepLinkFrontlet has @Frontlet(containerId="containerB")
        // 
        // Expected: When DeepLinkFrontlet is loaded via action, it should go to containerB
        //           (annotation wins), not containerA (default-frontlet)
        
        var client = deepLinkContext.openPage(DeepLinkingPage.class);
        
        // Container A should have FrontletMetadata (from default-frontlet attribute)
        var containerA = client.getDocument().getElementById("containerA");
        assertThat(containerA).isNotNull();
        var messageInA = containerA.querySelector("#message");
        assertThat(messageInA).isNotNull();
        assertThat(messageInA.getTextContent()).isEqualTo("Frontlet with metadata");
        
        // Container B should be empty initially
        var containerB = client.getDocument().getElementById("containerB");
        assertThat(containerB).isNotNull();
        assertThat(containerB.getTextContent().trim()).isEmpty();
        
        // Click button to load DeepLinkFrontlet
        var loadDeepLinkButton = client.getDocument().getElementById("loadDeepLink");
        assertThat(loadDeepLinkButton).isNotNull();
        loadDeepLinkButton.click();
        
        // Verify DeepLinkFrontlet loaded into containerB (annotation wins over default-frontlet)
        var deepLinkMessage = containerB.querySelector("#deepLinkMessage");
        assertThat(deepLinkMessage).isNotNull();
        assertThat(deepLinkMessage.getTextContent()).isEqualTo("Deep link frontlet in container B");
        
        // Verify containerA still has original frontlet (not replaced)
        messageInA = containerA.querySelector("#message");
        assertThat(messageInA).isNotNull();
        assertThat(messageInA.getTextContent()).isEqualTo("Frontlet with metadata");
        
        // Verify document title was updated from DeepLinkFrontlet annotation
        assertThat(client.getDocument().getTitle()).isEqualTo("Deep Link Title");
    }
}
