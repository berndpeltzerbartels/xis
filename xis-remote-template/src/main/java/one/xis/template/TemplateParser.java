package one.xis.template;

import one.xis.utils.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.List;
import java.util.stream.Stream;

import static one.xis.utils.xml.XmlUtil.getAttributes;
import static one.xis.utils.xml.XmlUtil.getChildNodes;

public class TemplateParser {

    private final ExpressionParser expressionParser = new ExpressionParser();
    private int varIndex = 0;
    private static final String ATTR_IF = "data-if";
    private static final String ATTR_FOR = "data-for";
    private static final String ATTR_LOOP_INDEX = "data-index";
    private static final String ATTR_LOOP_NUMBER = "data-number";


    public Model parse(Document document, String name) {
        return new Model(parse(document.getDocumentElement()));
    }

    private Stream<ModelNode> parseChildren(Element parent) {
        return getChildNodes(parent)
                .filter(this::filterNode)
                .map(this::parse);
    }

    private boolean filterNode(Node node) {
        return node instanceof Element || node instanceof Text;
    }

    private ModelNode parse(Node node) {
        if (node instanceof Element) {
            return parse((Element) node);
        } else {
            return parseText((Text) node);
        }
    }

    private ModelElement parse(Element element) {
        var modelElement = new ModelElement(element.getTagName());
        getAttributes(element).forEach((name, rawValue) -> addAttribute(name, rawValue, modelElement));
        parseFrameworkAttributes(element, modelElement);
        parseChildren(element).forEach(modelElement::addChild);
        return modelElement;
    }

    private void addAttribute(String name, String rawValue, ModelElement target) {
        List<MixedContent> contentList = new MixedContentParser(rawValue).parse();
        if (contentList.size() == 1 && contentList.get(0) instanceof StaticContent) {
            target.addStaticAttribute(name, ((StaticContent) contentList.get(0)).getContent());
        } else {
            target.addMutableAttribute(name, new MutableAttribute(new MixedContentParser(rawValue).parse()));
        }
    }

    private TextNode parseText(Text text) {
        return parseText(text.getTextContent());
    }

    private TextNode parseText(String text) {
        return new TextNode(new MixedContentParser(text).parse());
    }

    private void parseFrameworkAttributes(Element element, ModelElement target) {
        var attributes = getAttributes(element);
        if (attributes.containsKey(ATTR_IF)) {
            target.setIfCondition(parseIf(attributes.get(ATTR_IF)));
        }
        if (attributes.containsKey(ATTR_FOR)) {
            target.setLoop(parseFor(attributes.get(ATTR_FOR), element));
        }
    }

    private IfCondition parseIf(String src) {
        return new IfCondition(expressionParser.parse(src));
    }

    private ForLoop parseFor(String dataFor, Element src) {
        var dataForArray = dataFor.split(":");
        if (dataForArray.length != 2) {
            throw new TemplateSynthaxException("illegal loop-attribute:" + ATTR_FOR + "=" + dataFor);
        }
        return new ForLoop(expressionParser.parse(dataForArray[1]), dataForArray[0], getIndexVarName(src), getNumberVarName(src));
    }

    private String getIndexVarName(Element e) {
        return StringUtils.isNotEmpty(e.getAttribute(ATTR_LOOP_INDEX)) ? e.getAttribute(ATTR_LOOP_INDEX) : nextVarName();
    }

    private String getNumberVarName(Element e) {
        return StringUtils.isNotEmpty(e.getAttribute(ATTR_LOOP_NUMBER)) ? e.getAttribute(ATTR_LOOP_NUMBER) : nextVarName();
    }

    private String nextVarName() {
        return "var" + (varIndex++);
    }

}
