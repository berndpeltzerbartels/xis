package one.xis.test.dom;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ElementBuilder {
    private ElementBuilder() {
    }

    /**
     * Welche Custom-Tags sind semantisch „leer“ und dürfen keine Kinder haben?
     */
    private static final Set<String> EMPTY_CUSTOM_TAGS = Set.of(
            "xis:parameter",
            "xis:global-messages",
            "xis:message",
            "xis:widget-container"// ACHTUNG: jsoup liefert tagName() in lower-case

    );

    /**
     * Baut aus einem Fragment GENAU EIN Root-Element.
     * - Top-Level darf keine nicht-blanken Textknoten enthalten.
     * - Es muss genau EIN Top-Level-Element geben.
     * → Für komplette HTML-Dokumente bitte DocumentBuilder verwenden.
     */
    public static ElementImpl build(String html) {
        // HTML-Parser: tolerant gegenüber Tag-Soup (<link>, <br> etc.).
        Document js = Jsoup.parseBodyFragment(html, "");
        js.outputSettings(new Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.html));

        // Nur explizit deklarierte „leere“ Custom-Tags glätten
        normalizeCustomEmptyTags(js, EMPTY_CUSTOM_TAGS);

        // Top-Level prüfen: exakt 1 Element, kein nicht-blanker Text auf Top-Level
        var body = js.body();
        boolean hasNonBlankTopLevelText = body.childNodes().stream()
                .anyMatch(n -> n instanceof org.jsoup.nodes.TextNode t && !t.isBlank());

        List<org.jsoup.nodes.Element> topEls = new ArrayList<>();
        body.childNodes().forEach(n -> {
            if (n instanceof org.jsoup.nodes.Element e) topEls.add(e);
        });

        if (hasNonBlankTopLevelText || topEls.size() != 1) {
            throw new IllegalArgumentException(
                    "Fragment muss genau EIN Top-Level-Element ohne Top-Level-Text enthalten."
            );
        }

        return fromJsoupElement(topEls.get(0));
    }

    /**
     * Konvertiert ein jsoup-Element (inkl. Kinder) in deine DOM-Implementierung.
     */
    public static ElementImpl fromJsoupElement(org.jsoup.nodes.Element e) {
        ElementImpl el = Element.createElement(e.tagName());
        e.attributes().forEach(a -> el.setAttribute(a.getKey(), a.getValue()));
        for (var c : e.childNodes()) {
            NodeImpl cn = fromJsoupNode(c);
            if (cn != null) el.appendChild(cn);
        }
        el.updateTreeByChildNodes();
        return el;
    }

    /**
     * Konvertiert einen jsoup-Node rekursiv. Kommentare/CDATA könntest du hier optional abbilden.
     */
    public static NodeImpl fromJsoupNode(org.jsoup.nodes.Node n) {
        if (n instanceof org.jsoup.nodes.TextNode tn) {
            return tn.isBlank() ? null : new TextNodeIml(tn.text());
        }
        if (n instanceof org.jsoup.nodes.Element e) {
            return fromJsoupElement(e);
        }
        return null; // andere Knotentypen ignorieren
    }

    // ----------------- Normalisierung „leerer“ Custom-Tags -----------------

    /**
     * Behandelt die angegebenen Tags als „leer“:
     * Falls der HTML-Parser fälschlich Kinder angelegt hat (z. B. bei <xis:parameter/>),
     * werden diese Kinder als Geschwister direkt hinter dem Element eingefügt.
     */
    private static void normalizeCustomEmptyTags(Document doc, Set<String> emptyTags) {
        // Snapshot, damit die Iteration während der Modifikation sicher ist
        List<org.jsoup.nodes.Element> all = new ArrayList<>(doc.getAllElements());
        for (org.jsoup.nodes.Element elem : all) {
            // jsoup-normalisierte tagName() ist lower-case
            if (emptyTags.contains(elem.tagName()) && !elem.childNodes().isEmpty()) {
                moveChildrenAfter(elem);
            }
        }
    }

    /**
     * Verschiebt alle Kindknoten eines Elements direkt NACH das Element (gleicher Parent).
     * WICHTIG: in umgekehrter Reihenfolge iterieren, damit die Originalreihenfolge erhalten bleibt.
     */
    private static void moveChildrenAfter(org.jsoup.nodes.Element elem) {
        List<org.jsoup.nodes.Node> kids = new ArrayList<>(elem.childNodes());
        for (int i = kids.size() - 1; i >= 0; i--) {
            org.jsoup.nodes.Node n = kids.get(i);
            n.remove();
            // Node.after(...) ist auf allen unterstützten jsoup-Versionen verfügbar
            elem.after(n);
        }
    }
}
