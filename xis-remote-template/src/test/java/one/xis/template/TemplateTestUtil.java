package one.xis.template;

import lombok.experimental.UtilityClass;
import org.w3c.dom.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UtilityClass
class TemplateTestUtil {

    Element mockElement(String tagName, Node... childNodes) {
        return mockElement(tagName, Collections.emptyMap(), childNodes);
    }

    Element mockElement(String tagName, Map<String, String> attributes, Node... childNodes) {
        List<Attr> attrList = attributes.entrySet().stream().map(e -> mockAttribute(e.getKey(), e.getValue())).collect(Collectors.toList());

        Element element = mock(Element.class);
        when(element.getTagName()).thenReturn(tagName);

        NamedNodeMap attributesMap = mockAttributes(attrList);
        NodeList childNodeList = mockChildNodeList(childNodes);

        when(element.getAttributes()).thenReturn(attributesMap);
        when(element.getChildNodes()).thenReturn(childNodeList);
        when(element.getAttribute(anyString())).then(answer -> attributes.get(answer.getArgument(0)));
        when(element.hasAttribute(anyString())).then(answer -> attributes.containsKey(answer.getArgument(0)));

        return element;
    }

    Text mockTextNode(String content) {
        Text text = mock(Text.class);
        when(text.getTextContent()).thenReturn(content);
        return text;
    }

    private NodeList mockChildNodeList(Node[] childNodes) {
        NodeList nodeList = mock(NodeList.class);
        when(nodeList.getLength()).thenReturn(childNodes.length);
        when(nodeList.item(anyInt())).then(answer -> {
            int index = answer.getArgument(0);
            return childNodes[index];
        });
        return nodeList;
    }

    private NamedNodeMap mockAttributes(List<Attr> attributes) {
        NamedNodeMap namedNodeMap = mock(NamedNodeMap.class);
        when(namedNodeMap.getLength()).thenReturn(attributes.size());
        when(namedNodeMap.item(anyInt())).then(answer -> {
            int index = answer.getArgument(0);
            return attributes.get(index);
        });
        return namedNodeMap;
    }

    private Attr mockAttribute(String name, String value) {
        Attr attr = mock(Attr.class);
        when(attr.getName()).thenReturn(name);
        when(attr.getValue()).thenReturn(value);
        return attr;
    }
}
