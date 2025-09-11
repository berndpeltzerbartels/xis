package one.xis.test.dom;

public class ElementMapper {

    public static Element map(one.xis.html.document.Element srcElement) {
        var rootName = srcElement.getLocalName();
        var targetElement = new ElementImpl(rootName);
        doMap(targetElement, srcElement);
        targetElement.updateChildNodes();
        return targetElement;
    }

    /**
     * Recursively maps an Element from xis-html-parser to an ElementImpl from xis-test-dom.
     * This method handles attributes and child nodes (elements and text nodes).
     *
     * @param target The target ElementImpl to populate.
     * @param source The source Element from xis-html-parser.
     */
    private static void doMap(ElementImpl target, one.xis.html.document.Element source) {
        // Copy attributes
        copyAttributes(target, source);

        // Iterate over children and build a sibling chain (firstChild / nextSibling)
        var srcChild = source.getFirstChild();
        var lastSibling = (NodeImpl) null;
        while (srcChild != null) {
            if (srcChild instanceof one.xis.html.document.Element srcEl) {
                // Preserve local or qualified name depending on your needs
                var child = Element.createElement(srcEl.getLocalName());
                appendChild(target, child, lastSibling);
                doMap(child, srcEl); // recurse
                lastSibling = child;
            } else if (srcChild instanceof one.xis.html.document.TextNode srcText) {
                // FIX: TextNodeImpl (not "Iml")
                var child = new TextNodeImpl(srcText.getText());
                appendChild(target, child, lastSibling);
                lastSibling = child;
            }
            target.updateChildNodes();
            // Move to next sibling in the source tree
            srcChild = srcChild.getNextSibling();
        }
    }

    /**
     * Appends 'child' to 'parent' maintaining firstChild/nextSibling and parent links.
     * Returns the new 'prev' pointer for chaining.
     */
    private static void appendChild(ElementImpl parent, NodeImpl child, NodeImpl lastSibling) {
        child.setParentNode(parent);
        if (lastSibling == null) {
            parent.setFirstChild(child);
        } else {
            lastSibling.setNextSibling(child);
        }
    }


    private static void copyAttributes(ElementImpl target, one.xis.html.document.Element source) {
        // Do not use target.getAttributes().putAll(source.getAttributes()), here
        // There is some logic in setAttribute that we want to keep
        source.getAttributes().forEach(target::setAttribute);
    }


    private Element createElement(String localName) {
        return switch (localName) {
            case "textarea" -> new TextareaElementImpl();
            case "option" -> new OptionElementImpl();
            case "select" -> new SelectElementImpl();
            case "input" -> new InputElementImpl();
            default -> new ElementImpl(localName);
        };
    }
}
