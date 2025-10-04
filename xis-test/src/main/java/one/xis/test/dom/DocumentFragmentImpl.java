package one.xis.test.dom;

/**
 * DocumentFragment-Implementierung für DOM- und GraalVM-Tests.
 * Kein Element, keine Attribute, nur Kindverwaltung.
 */
public class DocumentFragmentImpl extends NodeImpl {
    public DocumentFragmentImpl() {
        super(Node.DOCUMENT_FRAGMENT_NODE); // Typisch: Node.DOCUMENT_FRAGMENT_NODE, aber für Kompatibilität
    }


    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder();
        evaluateContent(builder, 0);
        return builder.toString();
    }

    @Override
    protected void evaluateContent(StringBuilder builder, int indent) {
        for (int i = 0; i < indent; i++) builder.append('\t');
        builder.append("<DocumentFragment>\n");
        for (Node child : getChildNodes().list()) {
            if (child instanceof ElementImpl element) {
                element.evaluateContent(builder, indent + 1);
            } else if (child instanceof DocumentFragmentImpl fragment) {
                fragment.evaluateContent(builder, indent + 1);
            } else if (child != null) {
                for (int i = 0; i < indent + 1; i++) builder.append('\t');
                builder.append(child.asString()).append("\n");
            }
        }
        for (int i = 0; i < indent; i++) builder.append('\t');
        builder.append("</DocumentFragment>\n");
    }


    @Override
    public Node cloneNode() {
        DocumentFragmentImpl clone = new DocumentFragmentImpl();
        for (Node child : getChildNodes().list()) {
            clone.appendChild(child.cloneNode());
        }
        return clone;
    }

    @Override
    public int getNodeType() {
        return Node.DOCUMENT_FRAGMENT_NODE; // DOCUMENT_FRAGMENT_NODE
    }
}
