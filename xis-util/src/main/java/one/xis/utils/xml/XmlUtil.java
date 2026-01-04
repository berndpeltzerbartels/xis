package one.xis.utils.xml;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;


@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlUtil {

    public static String getTextContent(Element element) {
        if (element == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                sb.append(node.getNodeValue());
            }
        }
        return sb.toString().trim();
    }

    public static Document loadDocument(File file) throws IOException, SAXException {
        return new XmlLoader().loadDocument(file);
    }

    public static Document loadDocument(String xml) {
        try {
            return new XmlLoader().loadDocument(xml);
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document loadDocument(InputStream in) throws IOException, SAXException {
        try {
            return new XmlLoader().loadDocument(in);
        } finally {
            in.close();
        }
    }

    public static Optional<Element> getElementByTagName(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        switch (nodeList.getLength()) {
            case 0:
                log.info("nodelist empty");
                return Optional.empty();
            case 1:
                log.info(tagName + " - nodelist one: " + nodeList.item(0));
                return Optional.of(nodeList.item(0)).filter(Element.class::isInstance).map(Element.class::cast);
            default:
                throw new IllegalStateException("too many results");
        }
    }

    public static Stream<Node> getChildNodes(Element parent) {
        return asList(parent.getChildNodes()).stream();
    }

    public static Stream<Element> getChildElements(Element parent) {
        return getChildNodes(parent).filter(Element.class::isInstance).map(Element.class::cast);
    }

    public static Stream<Element> getElementsByTagName(Element parent, String tagName) {
        return asList(parent.getElementsByTagName(tagName)).stream().filter(Element.class::isInstance).map(Element.class::cast);
    }

    public static Map<String, String> getAttributes(Element e) {
        Map<String, String> map = new LinkedHashMap<>();
        NamedNodeMap namedNodeMap = e.getAttributes();
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            Node node = namedNodeMap.item(i);
            if (node instanceof Attr) {
                Attr attr = (Attr) node;
                map.put(attr.getName(), attr.getValue());
            }
        }
        return map;
    }

    public static String asString(Document document) {
        return asString(document.getDocumentElement());
    }

    public static String asString(Element element) {
        try {
            return new XmlSerializer().serialize(element);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asString(Node node) {
        if (node instanceof Document) {
            return asString((Document) node);
        }
        if (node instanceof Element) {
            return asString((Element) node);
        }
        return node.getNodeValue();
    }


    public static List<Node> asList(NodeList nodeList) {
        var list = new ArrayList<Node>();
        for (var i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i));
        }
        return list;
    }
}
