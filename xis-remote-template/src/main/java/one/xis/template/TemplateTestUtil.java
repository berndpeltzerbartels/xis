package one.xis.template;

import lombok.experimental.UtilityClass;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.w3c.dom.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


// TODO avoid mockito in main-context
@UtilityClass
public class TemplateTestUtil {

    public Element mockElement(String tagName, Node... childNodes) {
        return mockElement(tagName, Collections.emptyMap(), childNodes);
    }

    public Element mockElement(String tagName, Map<String, String> attributes, Node... childNodes) {
        List<Attr> attrList = attributes.entrySet().stream().map(e -> mockAttribute(e.getKey(), e.getValue())).collect(Collectors.toList());

        Element element = Mockito.mock(Element.class);
        Mockito.when(element.getTagName()).thenReturn(tagName);

        NamedNodeMap attributesMap = mockAttributes(attrList);
        NodeList childNodeList = mockChildNodeList(childNodes);

        Mockito.when(element.getAttributes()).thenReturn(attributesMap);
        Mockito.when(element.getChildNodes()).thenReturn(childNodeList);
        Mockito.when(element.getAttribute(ArgumentMatchers.anyString())).then(answer -> attributes.get(answer.getArgument(0)));
        Mockito.when(element.hasAttribute(ArgumentMatchers.anyString())).then(answer -> attributes.containsKey(answer.getArgument(0)));

        return element;
    }

    public Text mockTextNode(String content) {
        Text text = Mockito.mock(Text.class);
        Mockito.when(text.getTextContent()).thenReturn(content);
        return text;
    }

    private NodeList mockChildNodeList(Node[] childNodes) {
        NodeList nodeList = Mockito.mock(NodeList.class);
        Mockito.when(nodeList.getLength()).thenReturn(childNodes.length);
        Mockito.when(nodeList.item(ArgumentMatchers.anyInt())).then(answer -> {
            int index = answer.getArgument(0);
            return childNodes[index];
        });
        return nodeList;
    }

    private NamedNodeMap mockAttributes(List<Attr> attributes) {
        NamedNodeMap namedNodeMap = Mockito.mock(NamedNodeMap.class);
        Mockito.when(namedNodeMap.getLength()).thenReturn(attributes.size());
        Mockito.when(namedNodeMap.item(ArgumentMatchers.anyInt())).then(answer -> {
            int index = answer.getArgument(0);
            return attributes.get(index);
        });
        return namedNodeMap;
    }

    private Attr mockAttribute(String name, String value) {
        Attr attr = Mockito.mock(Attr.class);
        Mockito.when(attr.getName()).thenReturn(name);
        Mockito.when(attr.getValue()).thenReturn(value);
        return attr;
    }
}
