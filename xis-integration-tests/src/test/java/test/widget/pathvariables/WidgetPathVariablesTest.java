package test.widget.pathvariables;

import one.xis.context.IntegrationTestContext;
import one.xis.WidgetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetPathVariablesTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(PathVariablesPage.class)
                .withSingleton(ProductWidget.class)
                .withSingleton(CategoryWidget.class)
                .build();
    }

    @Test
    void widgetReceivesPathVariablesFromPageUrl() {
        // Open page with path variable in URL
        var pageResult = testContext.openPage("/product/12345.html");

        // Verify widget received path variable from page URL
        var productIdElement = pageResult.getDocument().getElementById("productId");
        assertThat(productIdElement).isNotNull();
        assertThat(productIdElement.getTextContent()).isEqualTo("12345");

        var productNameElement = pageResult.getDocument().getElementById("productName");
        assertThat(productNameElement).isNotNull();
        assertThat(productNameElement.getTextContent()).isEqualTo("Product 12345");

        // Verify title from widget annotation
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Product Details");
    }

    @Test
    void widgetActionsCanNavigateWithPathVariables() {
        // Open page with path variable
        var pageResult = testContext.openPage("/product/999.html");

        // Verify initial widget loaded with path variable
        var productIdElement = pageResult.getDocument().getElementById("productId");
        assertThat(productIdElement).isNotNull();
        assertThat(productIdElement.getTextContent()).isEqualTo("999");

        // Click action that navigates to another widget with path variable
        var loadCategoryButton = pageResult.getDocument().getElementById("loadCategory");
        assertThat(loadCategoryButton).isNotNull();
        loadCategoryButton.click();

        // Verify second widget received path variable from WidgetResponse.ofPathVariable()
        var categoryIdElement = pageResult.getDocument().getElementById("categoryId");
        assertThat(categoryIdElement).isNotNull();
        assertThat(categoryIdElement.getTextContent()).isEqualTo("electronics");

        var categoryNameElement = pageResult.getDocument().getElementById("categoryName");
        assertThat(categoryNameElement).isNotNull();
        assertThat(categoryNameElement.getTextContent()).isEqualTo("Category: electronics");

        // Verify URL and title updated from widget annotation
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Category");
    }
}
