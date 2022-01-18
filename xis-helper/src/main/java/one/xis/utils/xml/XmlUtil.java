package one.xis.utils.xml;


import lombok.experimental.UtilityClass;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@UtilityClass
public class XmlUtil {


    public Document loadDocument(File file) throws IOException, SAXException {
        return new XmlLoader().loadDocument(file);
    }

    public Document loadDocument(InputStream in) throws IOException, SAXException {
        try {
            return new XmlLoader().loadDocument(in);
        } finally {
            in.close();
        }
    }

    public Optional<Element> getElementByTagName(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        switch (nodeList.getLength()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(nodeList.item(0)).filter(Element.class::isInstance).map(Element.class::cast);
            default:
                throw new IllegalStateException("too many results");
        }
    }

    public Stream<Node> getChildNodes(Element parent) {
        return new NodeIterator(parent.getChildNodes()).asStream();
    }

    public Stream<Element> getElementsByTagName(Element parent, String tagName) {
        return new NodeIterator(parent.getElementsByTagName(tagName)).asStream().filter(Element.class::isInstance).map(Element.class::cast);
    }

    public Map<String, String> getAttributes(Element e) {
        Map<String, String> map = new HashMap<>();
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

}
