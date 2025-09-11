package one.xis.test.dom;

import lombok.Getter;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Getter
public class DocumentImpl implements one.xis.test.dom.Document {

    /**
     * Dein eigenes Root-Element (kein jsoup-Backer nötig)
     */
    public final ElementImpl documentElement;

    /**
     * wie gehabt
     */
    public Location location = new Location();
    public String cookies = "";

    /* ------------------------------------------
     * Konstruktoren / Fabriken
     * ------------------------------------------ */

    /**
     * Baut ein leeres Dokument mit einem Root-Tag deiner Wahl.
     */
    public DocumentImpl(String rootTagName) {
        this.documentElement = new ElementImpl(rootTagName);
        // Location bleibt leer
    }

    /**
     * Baut ein Dokument aus einem vorhandenen (Wrapper-)Root.
     */
    public DocumentImpl(ElementImpl documentElement) {
        this.documentElement = documentElement;
    }


    /* ------------------------------------------
     * Document-API
     * ------------------------------------------ */

    @Override
    public Element createElement(String name) {
        return Element.createElement(name);
    }

    @Override
    public Element querySelector(String selector) {
        return documentElement.querySelector(selector);
    }

    @Override
    public List<Element> querySelectorAll(String selector) {
        return documentElement.querySelectorAll(selector);
    }

    @Override
    public TextNode createTextNode(String content) {
        return new TextNodeImpl(content);
    }

    public String getInnerText() {
        return documentElement != null ? documentElement.getInnerText() : null;
    }

    Element getBody() {
        return documentElement.getElementByTagName("body");
    }

    Element getHead() {
        return documentElement.getElementByTagName("head");
    }

    @Override
    public String getTitle() {
        return Optional.ofNullable((ElementImpl) getHead())
                .map(head -> (ElementImpl) head.getElementByTagName("title"))
                .map(ElementImpl::getInnerText)
                .orElse(null);
    }

    @Override
    public NodeList getElementsByTagName(String name) {
        return documentElement.getElementsByTagName(name);
    }

    @Override
    public Element getElementById(String id) {
        return documentElement.getElementById(id);
    }

    @Override
    public InputElement getInputElementById(String id) {
        var e = getElementById(id);
        return e instanceof InputElement inputElement ? inputElement : null;
    }

    @Override
    public Element getElementByTagName(String name) {
        var list = getElementsByTagName(name);
        return switch (list.length) {
            case 0 -> null;
            case 1 -> (ElementImpl) list.item(0);
            default -> throw new IllegalStateException("too many results for " + name);
        };
    }

    @Override
    public String asString() {
        return documentElement != null ? documentElement.asString() : null;
    }

    @Override
    public List<Element> getElementsByClass(String clazz) {
        return documentElement.getElementsByClass(clazz);
    }

    @Override
    public String getTextContent() {
        return getDocumentElement().asString();
    }

    /* ------------------------------------------
     * intern
     * ------------------------------------------ */

    private void setLocationFromHref(String href) {
        if (href == null || href.isEmpty()) {
            this.location = new Location("/", "");
            return;
        }
        URI u = URI.create(href);
        String path = (u.getPath() == null || u.getPath().isEmpty()) ? "/" : u.getPath();
        this.location = new Location(path, u.toString());
    }

    /**
     * Tiefe Konvertierung: jsoup-Element -> ElementImpl (+ Kinder/Attribute).
     */
    private static ElementImpl convertFromJsoupElement(org.jsoup.nodes.Element e) {
        ElementImpl el = Element.createElement(e.tagName());
        // Attribute übernehmen
        e.attributes().forEach(a -> el.setAttribute(a.getKey(), a.getValue()));
        // Kinder rekursiv übernehmen
        e.childNodes().forEach(n -> {
            if (n instanceof org.jsoup.nodes.TextNode tn) {
                el.appendChild(new TextNodeImpl(tn.text()));
            } else if (n instanceof org.jsoup.nodes.Element ce) {
                el.appendChild(convertFromJsoupElement(ce));
            } // Kommentare/andere Knotentypen: optional ignorieren
        });
        return el;
    }
}
