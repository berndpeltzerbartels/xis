package test.include;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncludeTest {
    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(IncludeTestPage.class)
                .withSingleton(IncludeTestHeader.class)
                .withSingleton(IncludeTestFooter.class)
                .build();
    }

    @Test
    void includeTagSyntaxWorks() {
        var client = context.openPage("/include-test.html");

        // Debug: Print rendered HTML
        System.out.println("==== Rendered HTML ====");
        System.out.println(client.getDocument().getDocumentElement().getInnerHTML());
        System.out.println("=======================");

        // Check that the include was loaded and rendered
        var headerContent = client.getDocument().getElementById("header-content");
        assertThat(headerContent).isNotNull();
        assertThat(headerContent.getInnerText()).contains("This is the header include");
    }

    @Test
    void includeAttributeSyntaxWorks() {
        var client = context.openPage("/include-test.html");

        // Check that the include via attribute was loaded and rendered
        var footerContent = client.getDocument().getElementById("footer-content");
        assertThat(footerContent).isNotNull();
        assertThat(footerContent.getInnerText()).contains("This is the footer include");
    }

    @Test
    void pageModelDataIsAvailable() {
        var client = context.openPage("/include-test.html");

        // Check that the page's model data is still accessible
        var pageMessage = client.getDocument().getElementById("page-message");
        assertThat(pageMessage.getInnerText()).isEqualTo("Hello from Page");
    }
}
