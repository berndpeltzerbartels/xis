package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class WidgetInLoopTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(WidgetInLoopPage.class)
                .withSingleton(WidgetInLoopWidget.class)
                .build();
    }

    @Test
    void showSquareValues() {
        var result = testContext.openPage(WidgetInLoopPage.class);

        var containers = result.getDocument().getElementsByClass("container");
        assertThat(containers).hasSize(8);

        for (var i = 0; i <= 8; i++) {
            var container = containers.get(i);
            var value = container.findDescant(e -> e.getCssClasses().contains("value")).innerText;
            var square = container.findDescant(e -> e.getCssClasses().contains("square")).innerText;

            var expecxtedValue = Integer.toString(i);
            var expecxtedSquare = Integer.toString(i * i);

            assertThat(value).isEqualTo(expecxtedValue);
            assertThat(square).isEqualTo(expecxtedSquare);
        }
    }
}
