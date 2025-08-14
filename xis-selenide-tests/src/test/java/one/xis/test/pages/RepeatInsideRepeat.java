package one.xis.test.pages;

import com.codeborne.selenide.Selenide;
import org.assertj.core.api.AssertionsForClassTypes;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

public class RepeatInsideRepeat {

    public static final String URL = "/repeatInsideRepeat.html";
    public static final String TITLE = "RepeatInsideRepeat";

    public void open() {
        Selenide.open("http://localhost:8080" + URL);
        assertThat($("title").getInnerText() ()).isEqualTo(TITLE);
    }

}