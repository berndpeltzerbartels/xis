package one.xis.test.dom;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentBuilderTest {

    private static final String HTML = """
            <!DOCTYPE html>
            <html>
            <head>
                <title ignore="true"></title>
                <script src="/bundle.min.js" ignore="true"></script>
            </head>
            <body onload="main()">
                <div id="messages" ignore="true" ignore-position="end"></div>
            </body>
            </html>
            """;

    @Test
    void parsesFullHtmlDocument() {
        Document document = DocumentBuilder.build(HTML);
        assertThat(document).isNotNull();

        // Root <html>
        Element html = document.getDocumentElement();
        assertThat(html).isNotNull();
        assertThat(html.getTagName()).isEqualTo("HTML");
        assertThat(html.getFirstChild()).isNotNull();
        assertThat(html.getFirstChild()).isInstanceOf(ElementImpl.class).satisfies(e -> {
            ElementImpl firstChild = (ElementImpl) e;
            assertThat(firstChild.getTagName()).isEqualTo("HEAD");
        });

        // <head> und <body>
        Element head = document.getElementByTagName("head");
        Element body = document.getElementByTagName("body");
        assertThat(head).isNotNull();
        assertThat(body).isNotNull();
        assertThat(body.getAttribute("onload")).isEqualTo("main()");

        // <title ignore="true"></title>
        Element title = document.getElementByTagName("title");
        assertThat(title).isNotNull();
        assertThat(title.hasAttribute("ignore")).isTrue();
        assertThat(title.getAttribute("ignore")).isEqualTo("true");

        // <script src="/bundle.min.js" ignore="true"></script>
        Element script = document.getElementByTagName("script");
        assertThat(script).isNotNull();
        assertThat(script.getAttribute("src")).isEqualTo("/bundle.min.js");
        assertThat(script.getAttribute("ignore")).isEqualTo("true");

        // <div id="messages" ignore="true" ignore-position="end"></div>
        Element messages = document.getElementById("messages");
        assertThat(messages).isNotNull();
        assertThat(messages.getTagName()).isEqualTo("DIV");
        assertThat(messages.getAttribute("ignore")).isEqualTo("true");
        assertThat(messages.getAttribute("ignore-position")).isEqualTo("end");

        // OpeningTag-Counts (jeweils genau 1x vorhanden)
        assertThat(document.getElementsByTagName("head").length).isEqualTo(1);
        assertThat(document.getElementsByTagName("body").length).isEqualTo(1);
        assertThat(document.getElementsByTagName("title").length).isEqualTo(1);
        assertThat(document.getElementsByTagName("script").length).isEqualTo(1);
        assertThat(document.getElementsByTagName("div").length).isEqualTo(1);

        // Selector-Sanity
        assertThat(document.querySelector("head > script")).isNotNull();
        assertThat(document.querySelector("body > div#messages")).isNotNull();
    }
}
