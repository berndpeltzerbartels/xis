package one.xis.context;

import lombok.Data;
import one.xis.resource.Resources;
import one.xis.test.dom.*;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

@Data
class HtmlObjects {

    private DocumentProxy document;
    private LocalStorage localStorage;
    private SessionStorage sessionStorage;
    private Window window;
    // private Console console;
    private final Function<String, ElementProxy> htmlToElement;
    private final Function<String, String> atob;

    HtmlObjects() {
        this.htmlToElement = HtmlObjects::htmlToElement;
        this.atob = HtmlObjects::atob;
        this.init();
    }

    public static ElementProxy htmlToElement(String content) {
        var doc = ((DocumentImpl) Document.of(content));
        return new ElementProxy(doc.getDocumentElement());
    }


    public static String atob(String base64) {
        StringBuilder input = new StringBuilder(base64.replace("-", "+").replace("_", "/"));
        while (input.length() % 4 != 0) {
            input.append("=");
        }
        byte[] decoded = Base64.getDecoder().decode(input.toString());
        return new String(decoded, StandardCharsets.UTF_8);
    }

    void reset() {
        localStorage.reset();
        sessionStorage.reset();
        window.reset();
    }

    private void init() {
        var documentImpl = (DocumentImpl) Document.of(new Resources().getByPath("default-develop-index.html").getContent());
        this.document = new DocumentProxy(documentImpl);
        this.localStorage = new LocalStorage();
        this.sessionStorage = new SessionStorage();
        this.window = new Window(documentImpl.getLocation());
        //  this.console = new Console();
    }
}
