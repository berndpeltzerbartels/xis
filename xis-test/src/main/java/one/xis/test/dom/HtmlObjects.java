package one.xis.test.dom;

import lombok.Data;
import lombok.NonNull;
import one.xis.resource.Resource;
import one.xis.test.js.LocalStorage;
import one.xis.utils.lang.StringUtils;

import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class HtmlObjects {

    private final Document rootPage;
    private final LocalStorage localStorage;
    private final Window window;
    private final Function<String, Element> htmlToElement;

    public HtmlObjects(Resource rootPageResource) {
        this.rootPage = Document.of(rootPageResource.getContent());
        this.localStorage = new LocalStorage();
        this.window = new Window();
        this.htmlToElement = HtmlObjects::htmlToElement;
    }

    public static Element htmlToElement(String content) {
        var doc = Document.of(content);
        return doc.rootNode;
    }

    public void reset() {
        resetRootPage();
        localStorage.reset();
        window.reset();
    }

    private void resetRootPage() {
        resetHead();
        resetBody();
    }

    private void resetHead() {
        var head = rootPage.getElementByTagName("head");
        var elementsToRemove = head.getChildElements().stream()
                .filter(e -> e.getAttribute("ignore") == null)
                .filter(e -> !e.getLocalName().equals("title"))
                .collect(Collectors.toSet());
        elementsToRemove.forEach(head::removeChild);
        var title = rootPage.getElementByTagName("title");
        title.childNodes.clear();
        title.firstChild = null;
        title.innerText = null;
        title.innerHTML = null;
    }

    private void resetBody() {
        var body = rootPage.getElementByTagName("body");
        body.childNodes.clear();
        body.firstChild = null;
        var attributesToRemove = body.getAttributeNames().stream()
                .filter(name -> !name.equals("onload"))
                .collect(Collectors.toSet());
        attributesToRemove.forEach(body::removeAttribute);
    }

    public void finalizeDocument() {
        finalizeElement(rootPage.rootNode);
    }

    private void finalizeElement(@NonNull Element element) {
        String value = null;
        if (element.getTextNode() != null) {
            value = StringUtils.toString(element.getTextNode().nodeValue);
        } else if (element.innerHTML != null) {
            value = element.innerHTML;
        } else if (element.innerText != null) {
            value = element.innerText;
        }
        element.innerHTML = value;
        element.innerText = value;
        if (element.getTextNode() == null) {
            var textNode = new TextNode(value);
            element.appendChild(textNode);
        }
        element.getChildElements().forEach(this::finalizeElement);
    }

}
