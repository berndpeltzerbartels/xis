package one.xis.server;

import one.xis.context.Component;
import one.xis.html.document.Element;
import one.xis.html.document.HtmlDocument;
import one.xis.html.document.Node;

import java.util.ArrayList;
import java.util.List;

@Component
class BrowserSafeTemplateTransformer implements HtmlDocumentTransformer {

    @Override
    public HtmlDocument transform(HtmlDocument document) {
        transform(document.getDocumentElement());
        return document;
    }

    private void transform(Element element) {
        makeSelectChildrenBrowserSafe(element);

        var child = element.getFirstChild();
        while (child != null) {
            var next = child.getNextSibling();
            if (child instanceof Element childElement) {
                transform(childElement);
            }
            child = next;
        }
    }

    private void makeSelectChildrenBrowserSafe(Element element) {
        if (!"select".equals(element.getLocalName())) {
            return;
        }
        var child = element.getFirstChild();
        while (child != null) {
            var next = child.getNextSibling();
            if (child instanceof Element childElement && isStructuralSelectWrapper(childElement)) {
                replaceStructuralSelectWrapper(element, childElement);
            }
            child = next;
        }
    }

    private boolean isStructuralSelectWrapper(Element element) {
        return "xis:foreach".equals(element.getLocalName()) || "xis:if".equals(element.getLocalName());
    }

    private void replaceStructuralSelectWrapper(Element select, Element wrapper) {
        var children = childNodes(wrapper);
        var previous = previousChild(select, wrapper);
        var next = wrapper.getNextSibling();

        detachChildren(wrapper, children);
        prepareWrappedSelectChildren(wrapper, children);

        if (previous == null) {
            select.setFirstChild(first(children, next));
        } else {
            previous.setNextSibling(first(children, next));
        }

        Node last = previous;
        for (var child : children) {
            child.setParentNode(select);
            if (last != null && last != previous) {
                last.setNextSibling(child);
            }
            last = child;
        }
        if (last != null && last != previous) {
            last.setNextSibling(next);
        }
        wrapper.setParentNode(null);
        wrapper.setNextSibling(null);
    }

    private void prepareWrappedSelectChildren(Element wrapper, List<Node> children) {
        for (var child : children) {
            if (!(child instanceof Element childElement)) {
                continue;
            }
            // Browsers are allowed to repair the content model of <select> before
            // the XIS client can normalize custom tags. Firefox drops/moves
            // <xis:foreach> in this position, so the server rewrites structural
            // wrappers to attributes on legal <option>/<optgroup> children.
            if ("xis:foreach".equals(wrapper.getLocalName())) {
                childElement.setAttribute("xis:repeat", wrapper.getAttributes().get("var") + ":" + wrapper.getAttributes().get("array"));
            } else if ("xis:if".equals(wrapper.getLocalName())) {
                childElement.setAttribute("xis:if", wrapper.getAttributes().get("condition"));
            }
        }
    }

    private List<Node> childNodes(Element element) {
        var result = new ArrayList<Node>();
        var child = element.getFirstChild();
        while (child != null) {
            result.add(child);
            child = child.getNextSibling();
        }
        return result;
    }

    private void detachChildren(Element parent, List<Node> children) {
        parent.setFirstChild(null);
        for (var child : children) {
            child.setNextSibling(null);
        }
    }

    private Node previousChild(Element parent, Node target) {
        Node previous = null;
        var child = parent.getFirstChild();
        while (child != null && child != target) {
            previous = child;
            child = child.getNextSibling();
        }
        return previous;
    }

    private Node first(List<Node> nodes, Node fallback) {
        return nodes.isEmpty() ? fallback : nodes.get(0);
    }
}
