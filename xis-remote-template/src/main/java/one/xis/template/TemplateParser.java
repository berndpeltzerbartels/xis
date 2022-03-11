package one.xis.template;

import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static one.xis.utils.xml.XmlUtil.getAttributes;
import static one.xis.utils.xml.XmlUtil.getChildNodes;

public class TemplateParser {

    private final ExpressionParser expressionParser = new ExpressionParser();
    private int varIndex = 0;
    static final String ATTR_IF = "data-if";
    static final String ATTR_FOR = "data-for";
    static final String ATTR_REPEAT = "data-repeat";
    static final String ATTR_LOOP_INDEX = "data-index";
    static final String ATTR_LOOP_NUMBER = "data-number";
    static final String ATTR_CONTAINER_ID = "data-container-id";
    static final String ATTR_CONTAINER_WIDGET = "data-container-widget";

    public WidgetModel parse(Document document, String name, String httpPath) {
        if (!StringUtils.isNotEmpty(httpPath)) {
            validatePageTemplateRoot(document);
        }
        return new WidgetModel(name, parseElement(document.getDocumentElement()), httpPath);
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
            return parseElement((Element) node);
        } else {
            return parseText((Text) node);
        }
    }

    @SuppressWarnings("all")
    private ChildHolder parseElement(Element element) {
        LinkedList<ChildHolder> result = new LinkedList<>();
        if (hasIfAttribute(element)) {
            result.add(parseIf(element));
        }
        if (hasRepeatAttribute(element)) {
            result.add(parseRepeat(element));
        }
        var modelElement = isContainer(element) ? toContainerElement(element) : toTemplateElement(element);
        result.add(modelElement);
        if (hasForAttribute(element)) {
            result.add(parseFor(element));
        }
        ChildHolder last = result.stream().reduce((e1, e2) -> {
            e1.addChild(e2);
            return e2;
        }).orElseThrow();
        getAttributes(element).forEach((name, rawValue) -> addAttribute(name, rawValue, modelElement));
        parseChildren(element).forEach(last::addChild);
        return result.getFirst();
    }

    private ContainerElement toContainerElement(Element element) {
        ContainerElement containerElement = new ContainerElement(element.getTagName(), getMandatoryAttribute(ATTR_CONTAINER_ID, element), element.getAttribute(ATTR_CONTAINER_WIDGET));
        if (element.getChildNodes().getLength() != 0) {
            throw new TemplateSynthaxException(String.format("elements with attribute \"%s\" must have no content", ATTR_CONTAINER_ID));
        }
        return containerElement;
    }

    private TemplateElement toTemplateElement(Element element) {
        return new TemplateElement(element.getTagName());
    }

    private String getMandatoryAttribute(String name, Element element) {
        if (!element.hasAttribute(name)) {
            throw new TemplateSynthaxException(elementToString(element) + " must have attribute " + name);
        }
        return element.getAttribute(name);
    }

    private String elementToString(Element element) {
        StringBuilder builder = new StringBuilder()
                .append("<")
                .append(element.getTagName());
        XmlUtil.getAttributes(element).forEach((name, value) -> {
            builder.append(name).append("=").append("\"").append(value).append("\"");
        });
        return builder.append("/>").toString();
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
        return new IfBlock(expressionParser.parse(src));
    }

    private Loop parseFor(Element element) {
        String dataFor = element.getAttribute(ATTR_FOR);
        return loop(dataFor, element);
    }

    private Loop parseRepeat(Element element) {
        String repeat = element.getAttribute(ATTR_REPEAT);
        return loop(repeat, element);
    }

    private Loop loop(String loopSource, Element element) {
        var dataForArray = loopSource.split(":");
        if (dataForArray.length != 2) {
            throw new TemplateSynthaxException("illegal attribute:" + ATTR_REPEAT + "=" + loopSource);
        }
        return new Loop(expressionParser.parse(dataForArray[1]), dataForArray[0], getIndexVarName(element), getNumberVarName(element), loopSource);
    }

    private String getIndexVarName(Element e) {
        return StringUtils.isNotEmpty(e.getAttribute(ATTR_LOOP_INDEX)) ? e.getAttribute(ATTR_LOOP_INDEX) : nextVarName();
    }

    private String getNumberVarName(Element e) {
        return StringUtils.isNotEmpty(e.getAttribute(ATTR_LOOP_NUMBER)) ? e.getAttribute(ATTR_LOOP_NUMBER) : nextVarName();
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

    private String nextVarName() {
        return "var" + (varIndex++);
    }

    private void validatePageTemplateRoot(Document document) {
        var root = document.getDocumentElement();
        if (root.hasAttribute(ATTR_IF)) {
            throw new TemplateSynthaxException("top-level elements of a page must not have " + ATTR_IF);
        }
        if (root.hasAttribute(ATTR_REPEAT)) {
            throw new TemplateSynthaxException("top-level elements of a page must not have " + ATTR_REPEAT);
        }
    }
}
