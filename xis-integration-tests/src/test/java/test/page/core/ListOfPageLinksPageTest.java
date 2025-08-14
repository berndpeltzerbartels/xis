package test.page.core;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ListOfPageLinksPageTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(ListOfPageLinksPage.class)
                .withSingleton(IndexPage.class)
                .withSingleton(TitlePage.class)
                .build();
        var listOfPageLinksPage = testContext.getSingleton(ListOfPageLinksPage.class);
        var links = new Link[]{new Link("default-develop-index.html", "Index"), new Link("title.html", "Title test page")};
        listOfPageLinksPage.setLinks(links);
    }

    @Test
    void test() {
        var result = testContext.openPage("/listOfLinks.html");

        var document = result.getDocument();
        var titleElement = document.getElementByTagName("title");

        assertThat(titleElement.getInnerText()).isEqualTo("List of links");

        var link0 = document.getElementById("link_0");
        assertThat(link0.getInnerText()).isEqualTo("Index");

        var link1 = document.getElementById("link_1");
        assertThat(link1.getInnerText()).isEqualTo("Title test page");

        link1.click();

        assertThat(titleElement.getInnerText()).isEqualTo("Hello ! I am the title");

    }
}
