package spring.test;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.DomAssert;
import one.xis.test.dom.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IndexTest {

    private IntegrationTestContext testContext;

    @BeforeEach
    void createContext() {
        testContext = IntegrationTestContext.builder()
                .withSingleton(new Index())
                .build();
    }

    @Test
    void showIndex() {
        testContext.openPage("/index.html");
        var document = testContext.getDocument();


        var divs = document.getElementsByTagName("div").list();
        var div1 = (Element) divs.get(0);
        var div2 = (Element) divs.get(1);

        var a1 = DomAssert.assertAndGetChildElement(div1, "a");
        var a2 = DomAssert.assertAndGetChildElement(div2, "a");

        //div1.assertAndGetChildElement("a").assertAttribute()
    }

}
