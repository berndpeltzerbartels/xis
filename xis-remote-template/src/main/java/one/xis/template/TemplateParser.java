package one.xis.template;

import one.xis.utils.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static one.xis.utils.xml.XmlUtil.*;

public class TemplateParser {

    private final ExpressionParser expressionParser = new ExpressionParser();
    private int varIndex = 0;
    private static final String ATTR_IF = "data-if";
    private static final String ATTR_FOR = "data-for";
    private static final String ATTR_REPEAT = "data-repeat";
    private static final String ATTR_LOOP_INDEX = "data-index";
    private static final String ATTR_LOOP_NUMBER = "data-number";
    private static final String ATTR_CONTAINER_ID = "data-container-id";
    private static final String ATTR_CONTAINER_WIDGET = "data-container-widget";

    public WidgetModel parse(Document document, String name) {
        return new WidgetModel(name, parseElement(document.getDocumentElement()));
    }

    private Stream<ModelNode> parseChildren(Element parent) {
        return getChildNodes(parent)
                .filter(this::filterNode)
                .map(this::parse)
                .filter(Objects::nonNull);
    }

    private boolean filterNode(Node node) {
        return node instanceof Element || node instanceof Text;
    }

    private ModelNode parse(Node node) {
        if (node instanceof Element) {
            Element element = (Element) node;
            if (hasIfAttribute(element)) {
                return parseIf(element);
            } else if (hasRepeatAttribute(element)) {
                return parseRepeat(element);
            } else if (isContainer((Element) node)) {
                return parseContainer((Element) node);
            } else {
                return parseElement((Element) node);
            }
        } else {
            return parseText((Text) node);
        }
    }

    private boolean isContainer(Element element) {
        return element.hasAttribute(ATTR_CONTAINER_ID);
    }

    private boolean hasIfAttribute(Element element) {
        return element.hasAttribute(ATTR_IF);
    }

    private boolean hasForAttribute(Element element) {
        return element.hasAttribute(ATTR_FOR);
    }

    private boolean hasRepeatAttribute(Element element) {
        return element.hasAttribute(ATTR_REPEAT);
    }

    private ModelNode parseContainer(Element element) {
        var containerElement = new ContainerElement(element.getTagName(), element.getAttribute(ATTR_CONTAINER_ID), element.getAttribute(ATTR_CONTAINER_WIDGET));
        getAttributes(element).forEach((name, rawValue) -> addAttribute(name, rawValue, containerElement));
        if (getChildElements(element).findAny().isPresent()) {
            throw new TemplateSynthaxException(String.format("elements with attribute \"%s\" must have no content", ATTR_CONTAINER_ID));
        }
        if (element.hasAttribute(ATTR_FOR)) {
            throw new TemplateSynthaxException(String.format("elements with attribute \"%s\" must not have attribute \"%s\"", ATTR_CONTAINER_ID, ATTR_FOR));
        }
        if (element.hasAttribute(ATTR_REPEAT)) {
            throw new TemplateSynthaxException(String.format("elements with attribute \"%s\" must not have attribute \"%s\"", ATTR_CONTAINER_ID, ATTR_REPEAT));
        }
        return containerElement;
    }

    private ModelNode parseElement(Element element) {
        if (hasForAttribute(element)) {
            return parseFor(element);
        }
        var modelElement = new TemplateElement(element.getTagName());
        getAttributes(element).forEach((name, rawValue) -> addAttribute(name, rawValue, modelElement));
        parseChildren(element).forEach(modelElement::addChild);
        if (element.hasAttribute(ATTR_CONTAINER_WIDGET)) {
            throw new TemplateSynthaxException(String.format("elements with attribute \"%s\" must have a container-id (attribute \"%s\")", ATTR_CONTAINER_WIDGET, ATTR_CONTAINER_ID));
        }
        return modelElement;
    }

    private void addAttribute(String name, String rawValue, ElementBase target) {
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
        if (StringUtils.isSeparatorsOnly(text)) {
            return null;
        }
        List<MixedContent> mixedContents = new MixedContentParser(text).parse();
        if (mixedContents.stream().noneMatch(ExpressionContent.class::isInstance)) {
            return new StaticTextNode(text);
        }
        return new MutableTextNode(mixedContents);
    }

    private IfBlock parseIf(Element element) {
        String src = element.getAttribute(ATTR_IF);
        IfBlock ifBlock = new IfBlock(expressionParser.parse(src));
        parseChildren(element).forEach(ifBlock::addChild);
        return ifBlock;
    }

    private Loop parseFor(Element element) {
        String dataFor = element.getAttribute(ATTR_FOR);
        var dataForArray = dataFor.split(":");
        if (dataForArray.length != 2) {
            throw new TemplateSynthaxException("illegal loop-attribute:" + ATTR_FOR + "=" + dataFor);
        }
        Loop loop = new Loop(expressionParser.parse(dataForArray[1].trim()), dataForArray[0].trim(), getIndexVarName(element), getNumberVarName(element), dataFor);
        parseChildren(element).forEach(loop::addChild);
        return loop;
    }

    private Loop parseRepeat(Element element) {
        String repeat = element.getAttribute(ATTR_REPEAT);
        var dataForArray = repeat.split(":");
        if (dataForArray.length != 2) {
            throw new TemplateSynthaxException("illegal repeat-attribute:" + ATTR_REPEAT + "=" + repeat);
        }
        Loop loop = new Loop(expressionParser.parse(dataForArray[1]), dataForArray[0], getIndexVarName(element), getNumberVarName(element), repeat);
        loop.addChild(parseElement(element));
        return loop;
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
