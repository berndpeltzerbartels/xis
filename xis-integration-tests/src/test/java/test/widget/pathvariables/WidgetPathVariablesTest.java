package test.widget.pathvariables;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetPathVariablesTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(PathVariablesPage.class)
                .withSingleton(ProductWidget1.class)
                .withSingleton(ProductWidget2.class)
                .build();
    }

    @Test
    void widgetsLoadInCorrectContainersBasedOnAnnotation() {
        // Open page with category path variable
        var pageResult = testContext.openPage("/products/electronics.html");

        // DEBUG: Print full document
        var docString = pageResult.getDocument().asString();
        System.out.println("\n\n========== FULL DOCUMENT ==========");
        System.out.println(docString);
        System.out.println("========== END DOCUMENT ==========\n\n");
        
        // Check containers exist
        var container1 = pageResult.getDocument().getElementById("container1");
        System.out.println("Container1: " + (container1 != null ? container1.asString() : "NULL"));
        
        var container2 = pageResult.getDocument().getElementById("container2");
        System.out.println("Container2: " + (container2 != null ? container2.asString() : "NULL"));

        // Verify ProductWidget1 loaded in container1 (annotation containerId wins over default-widget)
        // Note: HTML has default-widget="ProductWidget2" but annotation says containerId="container1"
        var widget1Category = pageResult.getDocument().getElementById("widget1-category");
        System.out.println("widget1-category element: " + (widget1Category != null ? "FOUND" : "NULL"));
        assertThat(widget1Category).isNotNull();
        assertThat(widget1Category.getTextContent()).isEqualTo("electronics");

        var widget1Data = pageResult.getDocument().getElementById("widget1-data");
        assertThat(widget1Data).isNotNull();
        assertThat(widget1Data.getTextContent()).isEqualTo("Widget1: electronics");

        // Verify ProductWidget2 loaded in container2 (annotation containerId wins over default-widget)
        // Note: HTML has default-widget="ProductWidget1" but annotation says containerId="container2"
        var widget2Category = pageResult.getDocument().getElementById("widget2-category");
        assertThat(widget2Category).isNotNull();
        assertThat(widget2Category.getTextContent()).isEqualTo("electronics");

        var widget2Data = pageResult.getDocument().getElementById("widget2-data");
        assertThat(widget2Data).isNotNull();
        assertThat(widget2Data.getTextContent()).isEqualTo("Widget2: electronics");
    }

    @Test
    void pathVariablesExtractedFromUrl() {
        // Open page with different path variable
        var pageResult = testContext.openPage("/products/books.html");

        // Verify both widgets received the correct path variable
        var widget1Category = pageResult.getDocument().getElementById("widget1-category");
        assertThat(widget1Category).isNotNull();
        assertThat(widget1Category.getTextContent()).isEqualTo("books");

        var widget2Category = pageResult.getDocument().getElementById("widget2-category");
        assertThat(widget2Category).isNotNull();
        assertThat(widget2Category.getTextContent()).isEqualTo("books");

        // Verify widgets processed path variable in their model data
        var widget1Data = pageResult.getDocument().getElementById("widget1-data");
        assertThat(widget1Data.getTextContent()).isEqualTo("Widget1: books");

        var widget2Data = pageResult.getDocument().getElementById("widget2-data");
        assertThat(widget2Data.getTextContent()).isEqualTo("Widget2: books");
    }
}
