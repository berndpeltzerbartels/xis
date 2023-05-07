package one.xis.test;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;

public class TestUtil {

    public static void configureChrome() {
        Configuration.browser = "chrome";
        Configuration.browserSize = "1280x800";
        Configuration.browserCapabilities = new ChromeOptions().addArguments("--remote-allow-origins=*");
    }
}
