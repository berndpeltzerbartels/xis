package one.xis.test.dom;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

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

    /**
     * Fabrik: parse HTML (Tag-Soup) per jsoup + konvertiere in deinen DOM.
     *
     * @param html     HTML-String (darf „schmutzig“ sein)
     * @param baseHref Basis-URL (für location); kann null sein
     */
    public static DocumentImpl fromHtml(String html, String baseHref) {
        String base = baseHref == null ? "" : baseHref;
        Document d = Jsoup.parse(html, base, Parser.htmlParser());
        d.outputSettings(new Document.OutputSettings().prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.html));

        // „Root“ bestimmen: bevorzugt <html>, sonst erstes Kind, sonst <div>
        org.jsoup.nodes.Element rootJs;
        if (d.selectFirst("html") != null) {
            rootJs = d.selectFirst("html");
        } else if (d.childNodeSize() > 0 && d.childNode(0) instanceof org.jsoup.nodes.Element e0) {
            rootJs = e0;
        } else {
            rootJs = d.createElement("div");
        }

        // jsoup -> eigener DOM
        ElementImpl root = convertFromJsoupElement(rootJs);
        DocumentImpl doc = new DocumentImpl(root);

        // Location setzen
        doc.setLocationFromHref(d.location());
        return doc;
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
        return new TextNodeIml(content);
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
                el.appendChild(new TextNodeIml(tn.text()));
            } else if (n instanceof org.jsoup.nodes.Element ce) {
                el.appendChild(convertFromJsoupElement(ce));
            } // Kommentare/andere Knotentypen: optional ignorieren
        });
        return el;
    }
}
