package test.welcomepage;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WelcomePageWithPathVariablesTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(WelcomePageWithPathVariables.class)
                .build();
    }

    @Test
    void welcomePageWithPathVariablesUsesConcreteUrl() {
        // Open the welcome page - should use the concrete URL from @WelcomePage("/category/electronics.html")
        var pageResult = testContext.openPage("/");

        System.out.println("\n=== WELCOME PAGE DOCUMENT ===");
        System.out.println(pageResult.getDocument().asString());
        System.out.println("=== END DOCUMENT ===\n");

        // Verify that the path variable "electronics" was extracted and used
        var categoryName = pageResult.getDocument().getElementById("category-name");
        assertThat(categoryName).isNotNull();
        assertThat(categoryName.getTextContent()).isEqualTo("electronics");

        // Verify the title includes the category
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Category: electronics");
    }

    @Test
    void canAccessPageDirectlyWithDifferentPathVariable() {
        // Access the page directly with a different category
        var pageResult = testContext.openPage("/category/books.html");

        // Verify that the path variable "books" was extracted and used
        var categoryName = pageResult.getDocument().getElementById("category-name");
        assertThat(categoryName).isNotNull();
        assertThat(categoryName.getTextContent()).isEqualTo("books");

        // Verify the title includes the category
        assertThat(pageResult.getDocument().getTitle()).isEqualTo("Category: books");
    }
}
