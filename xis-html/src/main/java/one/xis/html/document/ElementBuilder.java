package one.xis.html.document;

import lombok.RequiredArgsConstructor;
import one.xis.html.parts.Part;
import one.xis.html.parts.Tag;
import one.xis.html.parts.TextPart;

import java.util.List;

/**
 * Baut aus einer linearen Part-Liste (Tags/Text) einen Element-Baum.
 * Regeln:
 * - Öffnende und schließende Tags sind je ein Part (Tag).
 * - Leere Tags (empty=true) kommen nur einmal vor.
 * - Kein gefundenes Schließ-Tag -> autoclose.
 * - Fremde Schließ-Tags werden nicht konsumiert (Caller verarbeitet sie).
 */
@RequiredArgsConstructor
public class ElementBuilder {

    private final List<Part> parts;
    private int index = 0;                 // aktueller Lesekopf
    private final int end = -1;            // ungenutzt, behalten für evtl. Grenzen
    // (falls du Subbereiche parsen willst, kann man end/limit nachrüsten)

    /**
     * Baut ab dem aktuellen Index genau EIN Element.
     */
    public Element build() {
        Tag startTag = requireOpenOrEmptyTag(peekOrFail());
        consume(); // öffnendes/empty Tag konsumieren
        Element element = createElementFrom(startTag);

        if (startTag.isEmpty()) {
            return element; // keine Kinder bei empty
        }

        parseChildrenUntilClose(element, startTag.getLocalName());
        return element;
    }

    /* --------------------------------- Parsing-Hilfen --------------------------------- */

    /**
     * Parst Kinder bis passendes Closing erscheint oder Input zu Ende ist (autoclose).
     */
    private void parseChildrenUntilClose(Element parent, String parentName) {
        Element firstChild = null;
        Node prev = null;

        while (!atEnd()) {
            Part p = peek();

            if (isMatchingClosingTag(p, parentName)) {
                consume(); // schließendes Tag konsumieren, fertig
                break;
            }
            if (isForeignClosingTag(p)) {
                // Closing einer äußeren Ebene -> hier abbrechen, Caller soll es verarbeiten
                break;
            }

            if (p instanceof Tag t && (t.isOpenTag() || t.isEmpty())) {
                Element child = build(); // rekursiv
                prev = attachChild(parent, firstChild, prev, child);
                if (firstChild == null) {
                    firstChild = child;
                }
                continue;
            }

            if (p instanceof TextPart) {
                TextPart textPart = (TextPart) consume();
                prev = attachChild(parent, firstChild, prev, new TextNode(textPart.getText()));
                continue;
            }

            // Unbekannter Part – sicherheitshalber konsumieren
            consume();
        }

        // parent.setFirstChild(firstChild);
    }

    /**
     * Hängt child an parent an; setzt parent/nextSibling; liefert das neue 'prev' zurück.
     */
    private Node attachChild(Element parent, Element firstChild, Node prev, Node child) {
        if (firstChild == null) {
            parent.setFirstChild(child);
        } else if (prev instanceof Element) {
            ((Element) prev).setNextSibling(child);
        }
        child.setParentNode(parent);
        return child;
    }

    /**
     * Liefert true, wenn p ein schließendes Tag für den gegebenen Namen ist.
     */
    private boolean isMatchingClosingTag(Part p, String name) {
        return (p instanceof Tag t) && !t.isOpenTag() && t.getLocalName().equals(name);
    }

    /**
     * Liefert true, wenn p irgendein schließendes Tag ist (aber nicht unseres).
     */
    private boolean isForeignClosingTag(Part p) {
        return (p instanceof Tag t) && !t.isOpenTag();
    }

    /* --------------------------------- Element/Tag-Utilities --------------------------------- */

    private Element createElementFrom(Tag t) {
        return new Element(t.getLocalName());
    }

    private Tag requireOpenOrEmptyTag(Part p) {
        if (!(p instanceof Tag t)) {
            throw new IllegalStateException(err("Erwartetes öffnendes/empty Tag, gefunden: " + p));
        }
        if (!t.isOpenTag() && !t.isEmpty()) {
            throw new IllegalStateException(err("Erwartetes öffnendes/empty Tag, gefunden schließendes Tag: " + t));
        }
        return t;
    }

    private String err(String msg) {
        return "[index=" + index + "] " + msg;
    }

    /* --------------------------------- Cursor/Stream --------------------------------- */

    private boolean atEnd() {
        return index >= parts.size();
    }

    private Part peekOrFail() {
        if (atEnd()) throw new IllegalStateException(err("Keine Parts mehr vorhanden."));
        return parts.get(index);
    }

    private Part peek() {
        return atEnd() ? null : parts.get(index);
    }

    private Part consume() {
        Part p = peekOrFail();
        index++;
        return p;
    }
}
