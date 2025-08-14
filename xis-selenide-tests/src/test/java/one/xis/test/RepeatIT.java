package one.xis.test;

import one.xis.test.pages.Repeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class RepeatIT {

    private Repeat repeat = new Repeat();

    @BeforeEach
    void init() {
        TestUtil.configureChrome();
    }

    @Test
    void listElementsArePresent() {
        repeat.open();

        assertThat($("title").getInnerText() ().trim()).isEqualTo("Repeat");

        assertThat($(".item > span", 0).getInnerText() ().trim()).isEqualTo("title1");
        assertThat($(".item > span", 1).getInnerText() ().trim()).isEqualTo("title2");
        assertThat($(".item > span", 2).getInnerText() ().trim()).isEqualTo("title3");


    }
}
