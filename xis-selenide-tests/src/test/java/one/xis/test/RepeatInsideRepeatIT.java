package one.xis.test;

import one.xis.test.pages.RepeatInsideRepeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class RepeatInsideRepeatIT {


    private RepeatInsideRepeat repeatInsideRepeat = new RepeatInsideRepeat();

    @BeforeEach
    void init() {
        TestUtil.configureChrome();
    }

    @Test
    void listElementsArePresent() {
        repeatInsideRepeat.open();

        assertThat($("h5", 0).getInnerText() ().trim()).isEqualTo("title1");
        assertThat($("h5", 1).getInnerText() ().trim()).isEqualTo("title2");
        assertThat($("h5", 2).getInnerText() ().trim()).isEqualTo("title3");

        assertThat($("div.item", 0).find(".subItem").getInnerText() ().trim()).isEqualTo("subItem1");
        assertThat($("div.item", 1).find(".subItem", 0).getInnerText() ().trim()).isEqualTo("subItem2");
        assertThat($("div.item", 1).find(".subItem", 1).getInnerText() ().trim()).isEqualTo("subItem3");
        assertThat($("div.item", 2).find(".subItem").exists()).isFalse();

    }
}
