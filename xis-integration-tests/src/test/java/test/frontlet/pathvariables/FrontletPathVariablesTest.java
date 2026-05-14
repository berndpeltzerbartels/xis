package test.frontlet.pathvariables;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletPathVariablesTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(PathVariablesPage.class)
                .withSingleton(ProductFrontlet1.class)
                .withSingleton(ProductFrontlet2.class)
                .build();
    }

    @Test
    void frontletsLoadInCorrectContainersBasedOnAnnotation() {
        // Open page with category path variable
        var client = testContext.openPage("/products/electronics.html");

        // DEBUG: Print full document
        var docString = client.getDocument().asString();
        System.out.println("\n\n========== FULL DOCUMENT ==========");
        System.out.println(docString);
        System.out.println("========== END DOCUMENT ==========\n\n");
        
        // Check containers exist
        var container1 = client.getDocument().getElementById("container1");
        System.out.println("Container1: " + (container1 != null ? container1.asString() : "NULL"));
        
        var container2 = client.getDocument().getElementById("container2");
        System.out.println("Container2: " + (container2 != null ? container2.asString() : "NULL"));

        // Verify ProductFrontlet1 loaded in container1 (annotation containerId wins over default-frontlet)
        // Note: HTML has default-frontlet="ProductFrontlet2" but annotation says containerId="container1"
        var frontlet1Category = client.getDocument().getElementById("frontlet1-category");
        System.out.println("frontlet1-category element: " + (frontlet1Category != null ? "FOUND" : "NULL"));
        assertThat(frontlet1Category).isNotNull();
        assertThat(frontlet1Category.getTextContent()).isEqualTo("electronics");

        var frontlet1Data = client.getDocument().getElementById("frontlet1-data");
        assertThat(frontlet1Data).isNotNull();
        assertThat(frontlet1Data.getTextContent()).isEqualTo("Frontlet1: electronics");

        // Verify ProductFrontlet2 loaded in container2 (annotation containerId wins over default-frontlet)
        // Note: HTML has default-frontlet="ProductFrontlet1" but annotation says containerId="container2"
        var frontlet2Category = client.getDocument().getElementById("frontlet2-category");
        assertThat(frontlet2Category).isNotNull();
        assertThat(frontlet2Category.getTextContent()).isEqualTo("electronics");

        var frontlet2Data = client.getDocument().getElementById("frontlet2-data");
        assertThat(frontlet2Data).isNotNull();
        assertThat(frontlet2Data.getTextContent()).isEqualTo("Frontlet2: electronics");
    }

    @Test
    void pathVariablesExtractedFromUrl() {
        // Open page with different path variable
        var client = testContext.openPage("/products/books.html");

        // Verify both frontlets received the correct path variable
        var frontlet1Category = client.getDocument().getElementById("frontlet1-category");
        assertThat(frontlet1Category).isNotNull();
        assertThat(frontlet1Category.getTextContent()).isEqualTo("books");

        var frontlet2Category = client.getDocument().getElementById("frontlet2-category");
        assertThat(frontlet2Category).isNotNull();
        assertThat(frontlet2Category.getTextContent()).isEqualTo("books");

        // Verify frontlets processed path variable in their model data
        var frontlet1Data = client.getDocument().getElementById("frontlet1-data");
        assertThat(frontlet1Data.getTextContent()).isEqualTo("Frontlet1: books");

        var frontlet2Data = client.getDocument().getElementById("frontlet2-data");
        assertThat(frontlet2Data.getTextContent()).isEqualTo("Frontlet2: books");
    }
}
