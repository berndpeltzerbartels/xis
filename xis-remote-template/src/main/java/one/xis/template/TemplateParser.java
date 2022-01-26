package one.xis.template;

import one.xis.template.TemplateModel.*;
import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class TemplateParser {

    private int varIndex = 0;
    private static final String ATTR_IF = "data-if";
    private static final String ATTR_FOR = "data-for";
    private static final String ATTR_LOOP_INDEX = "data-index";
    private static final Set<String> OPERATOR_ATTRIBUTES = Set.of(ATTR_FOR, ATTR_IF, ATTR_LOOP_INDEX);

    public TemplateModel parse(Document document, String name) throws TemplateSynthaxException, IOException {
        return new TemplateModel(parseElement(document.getDocumentElement()), name);
    }

    private TemplateElement parse(Node node) {
        if (node instanceof Element) {
            return parseElement((Element) node);
        }
        if (node instanceof CharacterData) {
            CharacterData data = (CharacterData) node;
            return parseTextContent(data.getData());
        }
        return null;
    }


    private Container parseElement(Element e) {
        LinkedList<Container> hierarchy = new LinkedList<>();
        if (StringUtils.isNotEmpty(e.getAttribute(ATTR_IF))) {
            hierarchy.add(parseIf(e));
        }
        hierarchy.add(new XmlElement(e.getTagName(), parseAttributes(e)));
        if (StringUtils.isNotEmpty(e.getAttribute(ATTR_FOR))) {
            hierarchy.add(parseFor(e));
        }

        Container first = hierarchy.getFirst();
        Container last = hierarchy.getLast();

        Container parent = hierarchy.removeFirst();
        while (!hierarchy.isEmpty()) {
            Container child = hierarchy.removeFirst();
            parent.addElement(child);
            parent = child;
        }

        parseChildren(e).forEach(last::addElement);
        return first;
    }


    private Stream<TemplateElement> parseChildren(Element src) {
        return XmlUtil.getChildNodes(src).map(this::parse);
    }


    private IfElement parseIf(Element src) {
        return new IfElement(src.getAttribute(ATTR_IF));
    }

    private ForElement parseFor(Element src) {
        String[] array = src.getAttribute("data-for").split(":");
        // TODO lenght == 2 !
        return new ForElement(array[1], array[0], getIndexVarName(src));
    }


    private Map<String, TextContent> parseAttributes(Element src) {
        Map<String, TextContent> contentMap = new HashMap<>();
        XmlUtil.getAttributes(src).forEach((name, value) -> contentMap.put(name, parseTextContent(value)));
        OPERATOR_ATTRIBUTES.forEach(attr -> contentMap.remove(attr));
        return contentMap;
    }

    private TextContent parseTextContent(String src) {
        return new TextContentParser(src).parse();
    }

    private String getIndexVarName(Element e) {
        return StringUtils.isNotEmpty(e.getAttribute(ATTR_LOOP_INDEX)) ? e.getAttribute(ATTR_LOOP_INDEX) : nextVarName();
    }


    private String nextVarName() {
        return "var" + (varIndex++);
    }


}
