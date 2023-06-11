package test.page;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationTestContextTest {

    @Test
    @DisplayName("Reusing compiled script does not throw an exception")
    void openPage() {
        var context = IntegrationTestContext.builder()
                .withSingleton(TitlePage.class)
                .build();

        var result = context.openPage(TitlePage.class);
        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("Hello ! I am the title");

        context = IntegrationTestContext.builder()
                .withSingleton(IndexPage.class)
                .build();
        result = context.openPage(IndexPage.class);

        assertThat(result.getDocument().getElementByTagName("title").innerText).isEqualTo("Index");


    }
}