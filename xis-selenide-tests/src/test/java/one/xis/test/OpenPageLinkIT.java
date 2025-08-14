package one.xis.test;

import com.codeborne.selenide.Configuration;
import one.xis.test.pages.Index;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class OpenPageLinkIT {

    private Index index = new Index();

    @BeforeEach
    void init() {
        TestUtil.configureChrome();
        index.open();
        assertThat($("title").getInnerText() ()).isEqualTo("Index");
    }

    @Test
    void openPageLink() {
        index.repeatLink.click();
        assertThat($("title").getInnerText() ()).isEqualTo("Repeat");
    }
}
