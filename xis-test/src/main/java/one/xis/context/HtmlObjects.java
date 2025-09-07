package one.xis.context;

import lombok.Data;
import one.xis.resource.Resources;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Window;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

import static java.net.URLEncoder.encode;

@Data
class HtmlObjects {

    private Document document;                         // jsoup Document
    private LocalStorage localStorage;
    private SessionStorage sessionStorage;
    private Window window;
    // private Console console;

    // htmlToElement liefert jetzt ein jsoup Element
    private final Function<String, Element> htmlToElement;
    private final Function<String, String> atob;
    private final Function<String, String> encodeURIComponent = HtmlObjects::encodeURIComponent;

    HtmlObjects() {
        this.htmlToElement = HtmlObjects::htmlToElement;
        this.atob = HtmlObjects::atob;
        this.init();
    }

    /**
     * Parst HTML robust (HTML5-Regeln) und liefert die Wurzel: bevorzugt <html>, sonst <body>.
     */
    public static Element htmlToElement(String content) {
        return Element.of(content);
    }

    public static String atob(String base64) {
        StringBuilder input = new StringBuilder(base64.replace("-", "+").replace("_", "/"));
        while (input.length() % 4 != 0) input.append("=");
        byte[] decoded = Base64.getDecoder().decode(input.toString());
        return new String(decoded, StandardCharsets.UTF_8);
    }

    public static String encodeURIComponent(String uri) {
        return encode(uri, StandardCharsets.UTF_8);
    }

    void reset() {
        localStorage.reset();
        sessionStorage.reset();
        window.reset();
    }

    private void init() {
        String content = new Resources()
                .getByPath("default-develop-index.html")
                .getContent();

        // Toleranter HTML5-Parser, keine hübsche Formatierung (beibehaltung der Eingabe-Struktur)
        this.document = Document.of(content);
        this.localStorage = new LocalStorage();
        this.sessionStorage = new SessionStorage();
        this.window = new Window(document.getLocation());
    }
}
