package one.xis.html;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocsTest {

    private final HtmlParser parser = new HtmlParser();

    private final String html = """
            <!DOCTYPE html>
            <html lang="en" xmlns:xis="https://xis.one/xsd">
            <head>
                <meta charset="UTF-8"/>
                <title>Title</title>
            </head>
            <body>
            <form xis:binding="test-object">
                <div>
                    <input xis:binding="integerFieldMandatory" id="integerFieldMandatory" xis:error-class="error"/>
                    <label for="integerFieldMandatory" xis:binding="integerFieldMandatory"
                           xis:error-class="error">Integer-Field</label>
                    <div xis:message-for="integerFieldMandatory"></div>
                </div>
                <div>
                    <input xis:binding="integerField" id="integerField" xis:error-class="error"/>
                    <label for="integerField" xis:binding="integerField" xis:error-class="error">Integer-Field</label>
                    <div xis:message-for="integerField"></div>
                </div>
                <div>
                    <button id="save" xis:action="save">Speichern</button>
                </div>
            </form>
            </body>
            </html>
            """;

    @Test
    void parseMethodAnnotations() throws Exception {
        var document = parser.parse(html);
        assertThat(document.getDocumentElement()).isNotNull();
        System.out.println("✓ MethodAnnotations.html parsed successfully");
    }


}
