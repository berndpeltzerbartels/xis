package one.xis.test.dom;

import lombok.Data;
import one.xis.resource.Resources;
import one.xis.test.js.LocalStorage;

import java.util.function.Function;

@Data
public class HtmlObjects {

    private final Document rootPage;
    private final LocalStorage localStorage;
    private final Window window;
    private final Function<String, Element> htmlToElement;

    public HtmlObjects() {
        this.rootPage = Document.of(new Resources().getByPath("index.html").getContent());
        this.localStorage = new LocalStorage();
        this.window = new Window();
        this.htmlToElement = HtmlObjects::htmlToElement;
    }

    public static Element htmlToElement(String content) {
        var doc = Document.of(content);
        return doc.rootNode;
    }
}
