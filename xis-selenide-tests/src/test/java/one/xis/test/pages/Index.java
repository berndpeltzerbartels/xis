package one.xis.test.pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

// page_url = http://localhost:8080/index.html
public class Index {

    public  SelenideElement repeatLink = $("#repeat");
    public  SelenideElement repeatInsideRepeatLink = $("#repeatInsideRepeat");

    public static final String TITLE = "Index";

    public  void open() {      Configuration.browserSize = "1280x800";
        Selenide.open("http://localhost:8080/index.html");
        assertThat($("title")).isEqualTo(TITLE);
    }
}