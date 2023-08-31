package test.widget;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CascadingDataTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void init() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(CascadingDataPage.class)
                .withSingleton(CascadingDataWidget1.class)
                .withSingleton(CascadingDataWidget2.class)
                .build();
    }

    @Test
    @DisplayName("Values are getiing passed to desendant widgets")
    void test() {
        testContext.openPage(CascadingDataPage.class);

        assertThat(testContext.getSingleton(CascadingDataWidget1.class).getData1()).isEqualTo("1");
        assertThat(testContext.getSingleton(CascadingDataWidget2.class).getData1()).isEqualTo("1");
        assertThat(testContext.getSingleton(CascadingDataWidget2.class).getData2()).isEqualTo("2");
    }
}
