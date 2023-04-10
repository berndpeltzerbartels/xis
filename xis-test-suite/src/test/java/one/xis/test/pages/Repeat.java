package one.xis.test.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// page_url = http://localhost:8080/repeat.html
public class Repeat {

    public static final String URL = "/repeat.html";
    public static final String TITLE = "Repeat";

    public void open() {
        Selenide.open("http://localhost:8080" + URL);
        assertThat($("title")).isEqualTo(TITLE);
    }

}