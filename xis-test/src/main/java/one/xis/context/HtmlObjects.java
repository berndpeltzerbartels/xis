package one.xis.context;

import lombok.Data;
import one.xis.resource.Resources;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Window;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

import java.util.function.Function;

@Data
class HtmlObjects {

    private Document document;
    private LocalStorage localStorage;
    private SessionStorage sessionStorage;
    private Window window;
    // private Console console;
    private final Function<String, Element> htmlToElement;
    private final Function<String, String> atob;

    HtmlObjects() {
        this.htmlToElement = HtmlObjects::htmlToElement;
        this.atob = HtmlObjects::atob;
        this.init();
    }

    public static Element htmlToElement(String content) {
        var doc = Document.of(content);
        return doc.rootNode;
    }


    public static String atob(String base64) {
        StringBuilder input = new StringBuilder(base64.replace("-", "+").replace("_", "/"));
        while (input.length() % 4 != 0) {
            input.append("=");
        }
        byte[] decoded = java.util.Base64.getDecoder().decode(input.toString());
        return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
    }

    void reset() {
        localStorage.reset();
        sessionStorage.reset();
        window.reset();
    }

    private void init() {
        this.document = Document.of(new Resources().getByPath("index.html").getContent());
        this.localStorage = new LocalStorage();
        this.sessionStorage = new SessionStorage();
        this.window = new Window();
        //  this.console = new Console();
    }
}
