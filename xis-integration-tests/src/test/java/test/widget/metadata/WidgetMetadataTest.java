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
                .withSingleton(WidgetMetadataWidget.class)
                .withSingleton(SecondWidget.class)
                .build();
        
        deepLinkContext = IntegrationTestContext.builder()
                .withSingleton(DeepLinkingPage.class)
                .withSingleton(WidgetMetadataWidget.class)
                .withSingleton(DeepLinkWidget.class)
                .build();
    }

    @Test
    void widgetMetadataIsExtractedFromAnnotation() {
        var pageResult = testContext.openPage(WidgetMetadataPage.class);
        
        // Verify that widget content was loaded via default-widget
        var messageElement = pageResult.getDocument().getElementById("message");
        assertThat(messageElement).isNotNull();
        assertThat(messageElement.getTextContent()).isEqualTo("Widget with metadata");
        
        // Verify document title was updated with widget's annotatedTitle
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Custom Widget Title");
    }

    @Test
    void widgetMetadataChangesAfterAction() {
        var pageResult = testContext.openPage(WidgetMetadataPage.class);
        
        // Verify initial widget is loaded
        var messageElement = pageResult.getDocument().getElementById("message");
        assertThat(messageElement).isNotNull();
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Custom Widget Title");
        
        // Click button to load second widget
        var loadSecondButton = pageResult.getDocument().getElementById("loadSecond");
        assertThat(loadSecondButton).isNotNull();
        loadSecondButton.click();
        
        // Verify second widget is loaded
        var secondMessageElement = pageResult.getDocument().getElementById("secondMessage");
        assertThat(secondMessageElement).isNotNull();
        assertThat(secondMessageElement.getTextContent()).isEqualTo("Second widget loaded");
        
        // Verify metadata was updated from second widget's annotation
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Second Widget Title");
    }

    @Test
    void annotationContainerIdOverridesDefaultWidget() {
        // This test verifies deep linking: when a widget specifies a containerId in its annotation,
        // it should override any default-widget attribute in the HTML template.
        // 
        // Scenario:
        // - HTML has containerA with default-widget="WidgetMetadata"
        // - HTML has containerB with no default-widget
        // - DeepLinkWidget has @Widget(containerId="containerB")
        // 
        // Expected: When DeepLinkWidget is loaded via action, it should go to containerB
        //           (annotation wins), not containerA (default-widget)
        
        var pageResult = deepLinkContext.openPage(DeepLinkingPage.class);
        
        // Container A should have WidgetMetadata (from default-widget attribute)
        var containerA = pageResult.getDocument().getElementById("containerA");
        assertThat(containerA).isNotNull();
        var messageInA = containerA.querySelector("#message");
        assertThat(messageInA).isNotNull();
        assertThat(messageInA.getTextContent()).isEqualTo("Widget with metadata");
        
        // Container B should be empty initially
        var containerB = pageResult.getDocument().getElementById("containerB");
        assertThat(containerB).isNotNull();
        assertThat(containerB.getTextContent().trim()).isEmpty();
        
        // Click button to load DeepLinkWidget
        var loadDeepLinkButton = pageResult.getDocument().getElementById("loadDeepLink");
        assertThat(loadDeepLinkButton).isNotNull();
        loadDeepLinkButton.click();
        
        // Verify DeepLinkWidget loaded into containerB (annotation wins over default-widget)
        var deepLinkMessage = containerB.querySelector("#deepLinkMessage");
        assertThat(deepLinkMessage).isNotNull();
        assertThat(deepLinkMessage.getTextContent()).isEqualTo("Deep link widget in container B");
        
        // Verify containerA still has original widget (not replaced)
        messageInA = containerA.querySelector("#message");
        assertThat(messageInA).isNotNull();
        assertThat(messageInA.getTextContent()).isEqualTo("Widget with metadata");
        
        // Verify document title was updated from DeepLinkWidget annotation
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Deep Link Title");
    }
}
