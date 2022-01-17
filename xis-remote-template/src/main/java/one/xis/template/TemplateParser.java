package one.xis.template;

import lombok.RequiredArgsConstructor;
import one.xis.template.TemplateModel.*;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TemplateParser {

    private final Document document;
    private final Collection<String> vars;
    private int varIndex = 0;
    private static final String ATTR_IF = "data-if";
    private static final String ATTR_FOR = "data-for";
    private static final String ATTR_LOOP_INDEX = "data-index";


    TemplateModel parse(Document document, Collection<String> dataVarNames) throws TemplateSynthaxException, IOException {
        return new TemplateModel(dataVarNames, parseElement(document.getDocumentElement()));
    }

    private TemplateElement parse(Node node) {
        if (node instanceof Element) {
            Element e = (Element) node;
            if (e.getAttribute(ATTR_IF) != null) {
                return parseIf(e);
            } else if (e.getAttribute(ATTR_FOR) != null) {
                return parseFor(e);
            }
            return parseElement(e);
        }
        return null;
    }

    private IfElement parseIf(Element src) {
        if (src.getAttribute(ATTR_FOR) != null) {
            return new IfElement(src.getAttribute(ATTR_IF), List.of(parseFor(src)));
        }
        return new IfElement(src.getAttribute(ATTR_IF), parseChildren(src));
    }

    private ForElement parseFor(Element src) {
        String[] array = src.getAttribute("data-for").split(":");
        // TODO lenght == 2 !
        return new ForElement(array[1], array[0], getIndexVarName(src), parseChildren(src));
    }

    private XmlElement parseElement(Element src) {
        return new XmlElement(src.getTagName(), parseAttributes(src), parseChildren(src));
    }

    private List<TemplateElement> parseChildren(Element src) {
        return XmlUtil.getChildNodes(src).map(this::parse).collect(Collectors.toList());
    }

    private Map<MixedContent, MixedContent> parseAttributes(Element src) {
        return null;
    }

    private String getIndexVarName(Element e) {
        return e.getAttribute(ATTR_LOOP_INDEX) != null ? e.getAttribute(ATTR_LOOP_INDEX) : nextVarName();
    }


    private String nextVarName() {
        return "var" + (varIndex++);
    }


}
