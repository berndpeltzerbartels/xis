package test.xis.theme.integration;

import one.xis.context.IntegrationTestContext;
import one.xis.test.dom.Element;
import one.xis.test.dom.OptionElement;
import one.xis.test.dom.SelectElement;
import one.xis.theme.ThemeDocumentTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThemeIntegrationTest {

    @Test
    void rendersThemeFormPageAndSubmitsGeneratedForm() {
        var context = IntegrationTestContext.builder()
                .withSingleton(ThemeDocumentTransformer.class)
                .withSingleton(ThemeIntegrationPage.class)
                .build();

        var client = context.openPage(ThemeIntegrationPage.class);
        var document = client.getDocument();

        assertEquals("Customer", document.getElementByTagName("h1").getInnerText());
        assertEquals("Name", document.querySelector("label[for='name']").getInnerText());
        assertEquals("Ada", document.getInputElementById("name").getValue());

        var option = (OptionElement) document.getElementById("stage").findDescendant(node ->
                node instanceof Element element
                        && "option".equals(element.getLocalName())
                        && "CUSTOMER".equals(element.getAttribute("value")));
        assertEquals("Customer", option.getInnerText());

        document.getInputElementById("name").setValue("Grace");
        ((SelectElement) document.getElementById("stage")).setValue("CUSTOMER");
        document.getElementByTagName("button").click();

        var page = context.getSingleton(ThemeIntegrationPage.class);
        assertNotNull(page.savedCustomer());
        assertEquals("Grace", page.savedCustomer().name);
        assertEquals("CUSTOMER", page.savedCustomer().stage);
    }
}
