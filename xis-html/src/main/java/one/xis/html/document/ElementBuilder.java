package one.xis.html.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.html.parts.*;

import java.util.List;
import java.util.Set;

/**
 * Builds a node tree from a linear Part list (tags/text).
 * <p>
 * Rules:
 * - Opening and closing tags are each a Part (OpeningTag).
 * - NO_CONTENT (self-closing) tags appear only once and cannot have children.
 * - Missing end tags are an error, except for a small allow-list (we allow <p>).
 * - Foreign CLOSING tags are not consumed here (caller processes them).
 * - HTML5 void elements must not have content or an explicit end tag.
 * <p>
 * All comments and exceptions are in English.
 */
@Getter
@RequiredArgsConstructor
public class ElementBuilder {

    private static final Set<String> OPTIONAL_END_TAGS = Set.of("p");

    private final List<Part> parts;
    private int index = 0;
    private final int end = -1; // reserved for potential subrange parsing

    /**
     * Builds exactly ONE element at the current index.
     */
    public Element build() {
        var element = doBuild();
        if (!atEnd()) {
            throw new IllegalArgumentException(err("Expected exactly one top-level element, found extra content after it: " + peek()));
        }
        return element;
    }

    public Element doBuild() {
        Tag startTag = requireOpeningOrEmptyTag(peekOrFail());
        String name = startTag.getLocalName();

        consume(); // consume the opening/NO_CONTENT tag

        Element element = createElementFrom(startTag);
        if (startTag instanceof OpeningTag openingTag) {
            element.getAttributes().putAll(openingTag.getAttributes());
        }
        if (startTag instanceof EmptyTag emptyTag) {
            element.getAttributes().putAll(emptyTag.getAttributes());
        }
        // NEW: Treat HTML5 void elements as empty regardless of syntax (<img> or <img/>)
        if (SelfClosingTags.isSelfClosing(name)) {
            // No children, no end tag expected
            return element;
        }

        // Existing behavior for non-void tags
        if (startTag instanceof EmptyTag) {
            return element;
        }

        boolean closed = parseChildrenUntilClose(element, name);
        if (!closed && !OPTIONAL_END_TAGS.contains(name)) {
            throw new IllegalStateException(err("Missing closing tag for <" + name + ">."));
        }
        return element;
    }

    /* ------------------------------- Parsing helpers ------------------------------- */

    /**
     * Parses children until a matching closing tag appears or input ends (autoclose).
     * Returns true if a matching closing tag was consumed, false if we stopped without it.
     */
    private boolean parseChildrenUntilClose(Element parent, String parentName) {
        Node firstChild = null;
        Node prev = null;

        while (!atEnd()) {
            Part p = peek();

            // Stop on our matching closing tag
            if (isMatchingClosingTag(p, parentName)) {
                consume(); // consume </parentName>
                return true;
            }
            // Stop (but do not consume) on a foreign closing tag belonging to an outer level
            if (isForeignClosingTag(p)) {
                break;
            }

            if (p instanceof OpeningTag) {
                Element child = doBuild(); // recursive
                prev = attachChild(parent, firstChild, prev, child);
                if (firstChild == null) firstChild = child;
                continue;
            }

            if (p instanceof EmptyTag) {
                Element child = doBuild(); // recursive
                prev = attachChild(parent, firstChild, prev, child);
                if (firstChild == null) firstChild = child;
                continue;
            }

            if (p instanceof TextPart) {
                TextPart textPart = (TextPart) consume();
                TextNode child = new TextNode(textPart.toString());
                prev = attachChild(parent, firstChild, prev, child);
                if (firstChild == null) firstChild = child;
                continue;
            }

            if (p instanceof CommentOpen cp) {
                CommentNode node = new CommentNode(cp.toString());
                attachChild(parent, firstChild, prev, node);
                consume();
                if (firstChild == null) firstChild = node;
                prev = node;
                continue;
            }

            // Unknown part — consume defensively
            consume();
        }

        // No matching closing tag consumed (autoclose)
        return false;
    }

    /**
     * Attaches child to parent, maintains firstChild/nextSibling links, returns new 'prev'.
     */
    private Node attachChild(Element parent, Node firstChild, Node prev, Node child) {
        if (firstChild == null) {
            parent.setFirstChild(child);
        } else if (prev != null) {
            // Link after ANY previous node (works for TextNode and Element)
            prev.setNextSibling(child);
        }
        child.setParentNode(parent);
        return child;
    }

    /* ------------------------------- OpeningTag utilities ------------------------------- */

    private boolean isMatchingClosingTag(Part p, String name) {
        return (p instanceof ClosingTag t)
                && t.getLocalName().equals(name);
    }

    private boolean isForeignClosingTag(Part p) {
        return p instanceof ClosingTag;
    }

    private Element createElementFrom(Tag t) {
        return new Element(t.getLocalName());
    }

    private Tag requireOpeningOrEmptyTag(Part p) {
        if (!OpeningTag.class.isInstance(p) && !EmptyTag.class.isInstance(p)) {
            throw new IllegalArgumentException(err("Expected opening or self-closing tag, found: " + p));
        }
        return (Tag) p;
    }

    private String err(String msg) {
        return "[index=" + index + "] " + msg;
    }

    /* ------------------------------- Cursor/stream ------------------------------- */

    private boolean atEnd() {
        return index >= parts.size();
    }

    private Part peekOrFail() {
        if (atEnd()) throw new IllegalStateException(err("No more parts available."));
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
