package one.xis.theme;

import one.xis.html.HtmlParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeDocumentTransformerTest {

    private final HtmlParser parser = new HtmlParser();
    private final ThemeDocumentTransformer transformer = new ThemeDocumentTransformer();

    @Test
    void translatesThemeLayoutAttributesToExistingCssClasses() {
        var document = parser.parse("""
                <html>
                  <body>
                    <main theme:wrapper>
                      <section class="customers" theme:grid="3">
                        <div theme:span="2" theme:field>
                          <input>
                        </div>
                      </section>
                    </main>
                  </body>
                </html>
                """);

        var html = transformer.transform(document).toHtml();

        assertTrue(html.contains("class=\"wrapper\""));
        assertTrue(html.contains("class=\"customers col3\""));
        assertTrue(html.contains("class=\"form-field span2\""));
        assertFalse(html.contains("theme:wrapper"));
        assertFalse(html.contains("theme:grid"));
        assertFalse(html.contains("theme:span"));
        assertFalse(html.contains("theme:field"));
    }

    @Test
    void rejectsUnsupportedGridWidths() {
        var document = parser.parse("<div theme:grid=\"12\"></div>");

        assertThrows(IllegalArgumentException.class, () -> transformer.transform(document));
    }

    @Test
    void expandsThemeFormWithInputAndSubmitButton() {
        var document = parser.parse("""
                <html>
                  <body>
                    <theme:form binding="customer" action="saveCustomer" submit-label="Save">
                      <theme:input binding="firstName" title="First name" span="2"/>
                    </theme:form>
                  </body>
                </html>
                """);

        var html = transformer.transform(document).toHtml();

        assertTrue(html.contains("<form xis:binding=\"customer\">"));
        assertTrue(html.contains("<div class=\"form-field span2\">"));
        assertTrue(html.contains("<label for=\"firstName\" xis:binding=\"firstName\" xis:error-class=\"error\">First name</label>"));
        assertTrue(html.contains("<input id=\"firstName\" type=\"text\" xis:binding=\"firstName\" xis:error-class=\"error\">"));
        assertFalse(html.contains("span=\"2\""));
        assertTrue(html.contains("<div xis:message-for=\"firstName\"></div>"));
        assertTrue(html.contains("<button type=\"submit\" xis:action=\"saveCustomer\">Save</button>"));
        assertFalse(html.contains("theme:form"));
        assertFalse(html.contains("theme:input"));
    }

    @Test
    void expandsThemeSelectWithGeneratedOptions() {
        var document = parser.parse("""
                <html>
                  <body>
                    <theme:select binding="stage" title="Stage" span="2" options="stages" option-value="code" option-label="label"/>
                  </body>
                </html>
                """);

        var html = transformer.transform(document).toHtml();

        assertTrue(html.contains("<div class=\"form-field span2\">"));
        assertTrue(html.contains("<label for=\"stage\" xis:binding=\"stage\" xis:error-class=\"error\">Stage</label>"));
        assertTrue(html.contains("<select id=\"stage\" xis:binding=\"stage\" xis:error-class=\"error\">"));
        assertFalse(html.contains("span=\"2\""));
        assertTrue(html.contains("<option xis:repeat=\"option:stages\" value=\"${option.code}\">${option.label}</option>"));
        assertTrue(html.contains("<div xis:message-for=\"stage\"></div>"));
        assertFalse(html.contains("theme:select"));
    }

    @Test
    void expandsThemeTextareaCheckboxAndRadioGroup() {
        var document = parser.parse("""
                <html>
                  <body>
                    <theme:textarea binding="notes" title="Notes" rows="5" span="2"/>
                    <theme:checkbox binding="newsletter" title="Newsletter"/>
                    <theme:radio binding="contactType" title="Preferred contact" options="contactTypes" option-value="code" option-label="label" span="3"/>
                  </body>
                </html>
                """);

        var html = transformer.transform(document).toHtml();

        assertTrue(html.contains("<div class=\"form-field span2\">"));
        assertTrue(html.contains("<label for=\"notes\" xis:binding=\"notes\" xis:error-class=\"error\">Notes</label>"));
        assertTrue(html.contains("<textarea rows=\"5\" id=\"notes\" xis:binding=\"notes\" xis:error-class=\"error\"></textarea>"));
        assertTrue(html.contains("<label for=\"newsletter\" xis:binding=\"newsletter\" xis:error-class=\"error\" aria-label=\"Newsletter\">"));
        assertTrue(html.contains("<input id=\"newsletter\" type=\"checkbox\" xis:binding=\"newsletter\" xis:error-class=\"error\">Newsletter</label>"));
        assertTrue(html.contains("<div class=\"form-field span3 radio-group\">"));
        assertTrue(html.contains("<div xis:binding=\"contactType\" xis:error-class=\"error\">Preferred contact</div>"));
        assertTrue(html.contains("<label xis:repeat=\"option:contactTypes\">"));
        assertTrue(html.contains("<input type=\"radio\" xis:binding=\"contactType\" value=\"${option.code}\">"));
        assertTrue(html.contains("<span>${option.label}</span>"));
        assertTrue(html.contains("<div xis:message-for=\"contactType\"></div>"));
        assertFalse(html.contains("theme:textarea"));
        assertFalse(html.contains("theme:checkbox"));
        assertFalse(html.contains("theme:radio"));
    }

    @Test
    void expandsThemeFormPageWithoutUserHtmlScaffold() {
        var document = parser.parse("""
                <html>
                  <body>
                    <theme:form-page title="Edit customer" binding="customer" action="saveCustomer" submit-label="Save">
                      <theme:input binding="firstName" title="First name"/>
                    </theme:form-page>
                  </body>
                </html>
                """);

        var html = transformer.transform(document).toHtml();

        assertTrue(html.contains("<main class=\"wrapper\">"));
        assertTrue(html.contains("<h1>Edit customer</h1>"));
        assertTrue(html.contains("<form xis:binding=\"customer\">"));
        assertTrue(html.contains("<input id=\"firstName\" type=\"text\" xis:binding=\"firstName\" xis:error-class=\"error\">"));
        assertTrue(html.contains("<button type=\"submit\" xis:action=\"saveCustomer\">Save</button>"));
        assertFalse(html.contains("theme:form-page"));
    }

    @Test
    void expandsThemeNavigationAndGridTags() {
        var document = parser.parse("""
                <html>
                  <body>
                    <theme:navigation logo="/logo.svg" logo-alt="Acme">
                      <theme:nav-item page="/dashboard.html" label="Dashboard"/>
                      <theme:nav-group label="Customers">
                        <theme:nav-item page="/customers.html" label="All customers"/>
                      </theme:nav-group>
                    </theme:navigation>
                    <theme:grid columns="3">
                      <div>One</div>
                      <div>Two</div>
                    </theme:grid>
                  </body>
                </html>
                """);

        var html = transformer.transform(document).toHtml();

        assertTrue(html.contains("<nav class=\"nav\">"));
        assertTrue(html.contains("<img src=\"/logo.svg\" alt=\"Acme\">"));
        assertTrue(html.contains("<a xis:page=\"/dashboard.html\">Dashboard</a>"));
        assertTrue(html.contains("<a href=\"#\">Customers</a>"));
        assertTrue(html.contains("<a xis:page=\"/customers.html\">All customers</a>"));
        assertTrue(html.contains("<section class=\"col3\">"));
        assertFalse(html.contains("theme:navigation"));
        assertFalse(html.contains("theme:grid"));
    }

    @Test
    void expandsGridWithFieldSpans() {
        var document = parser.parse("""
                <html>
                  <body>
                    <theme:grid id="customer-grid" columns="3">
                      <theme:input id="name" binding="name" title="Name" span="2"/>
                      <theme:input id="city" binding="city" title="City"/>
                      <theme:select id="stage" binding="stage" title="Stage" options="stages" span="3"/>
                    </theme:grid>
                  </body>
                </html>
                """);

        var html = transformer.transform(document).toHtml();

        assertTrue(html.contains("<section id=\"customer-grid\" class=\"col3\">"));
        assertTrue(html.contains("<div class=\"form-field span2\">"));
        assertTrue(html.contains("<input id=\"name\" type=\"text\" xis:binding=\"name\" xis:error-class=\"error\">"));
        assertTrue(html.contains("<input id=\"city\" type=\"text\" xis:binding=\"city\" xis:error-class=\"error\">"));
        assertTrue(html.contains("<div class=\"form-field span3\">"));
        assertTrue(html.contains("<select id=\"stage\" xis:binding=\"stage\" xis:error-class=\"error\">"));
        assertFalse(html.contains("span=\"2\""));
        assertFalse(html.contains("span=\"3\""));
        assertFalse(html.contains("theme:grid"));
        assertFalse(html.contains("theme:input"));
        assertFalse(html.contains("theme:select"));
    }
}
