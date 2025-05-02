package one.xis.context;

import lombok.Data;
import one.xis.resource.Resources;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Window;
import one.xis.test.js.Console;
import one.xis.test.js.LocalStorage;
import one.xis.test.js.SessionStorage;

import java.util.function.Function;

@Data
class HtmlObjects {

    private Document rootPage;
    private LocalStorage localStorage;
    private SessionStorage sessionStorage;
    private Window window;
    private Console console;
    private final Function<String, Element> htmlToElement;

    HtmlObjects() {
        this.htmlToElement = HtmlObjects::htmlToElement;
        this.init();
    }

    public static Element htmlToElement(String content) {
        var doc = Document.of(content);
        return doc.rootNode;
    }

    void reset() {
        localStorage.reset();
        sessionStorage.reset();
        window.reset();
    }

    private void init() {
        this.rootPage = Document.of(new Resources().getByPath("index.html").getContent());
        this.localStorage = new LocalStorage();
        this.sessionStorage = new SessionStorage();
        this.window = new Window();
        this.console = new Console();
    }
}
