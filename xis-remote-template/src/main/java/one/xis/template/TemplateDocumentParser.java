package one.xis.template;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static one.xis.utils.xml.XmlUtil.getAttributes;
import static one.xis.utils.xml.XmlUtil.getChildNodes;

@RequiredArgsConstructor
public abstract class TemplateDocumentParser<M extends TemplateModel> {

    private final ExpressionParser expressionParser;

    private int varIndex = 0;
    static final String ATTR_IF = "data-if";
    static final String ATTR_FOR = "data-for";
    static final String ATTR_REPEAT = "data-repeat";
    static final String ATTR_LOOP_INDEX = "data-index";
    static final String ATTR_LOOP_NUMBER = "data-number";
    static final String ATTR_CONTAINER = "data-container";
    static final String ATTR_CONTAINER_WIDGET = "data-widget";

    static final Set<String> DATA_ATTRIBUTES = Set.of(ATTR_IF, ATTR_FOR, ATTR_REPEAT, ATTR_LOOP_INDEX, ATTR_LOOP_NUMBER, ATTR_CONTAINER, ATTR_CONTAINER_WIDGET);

    public abstract M parseTemplate(@NonNull Document document, @NonNull String widgetClassName);

    protected Stream<ModelNode> parseChildren(Element parent) {
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
    protected ChildHolder parseElement(Element element) {
        var result = new LinkedList<ChildHolder>();
        Map<String, String> dataAttributes = getDataAttributes(element);
        if (dataAttributes.containsKey(ATTR_IF)) {
            result.add(parseIf(element));
        }
        if (dataAttributes.containsKey(ATTR_REPEAT)) {
            result.add(parseRepeat(element));
        }
        var modelElement = isContainer(element) ? toContainerElement(element) : toTemplateElement(element);
        result.add(modelElement);
        if (dataAttributes.containsKey(ATTR_FOR)) {
            result.add(parseFor(element));
        }
        var last = createHierarchy(result);
        parseChildren(element).forEach(last::addChild);
        return result.getFirst();
    }

    private Map<String, String> getDataAttributes(Element element) {
        return XmlUtil.getAttributes(element).entrySet().stream()
                .filter(e -> e.getKey().startsWith("data-"))
                .peek(e -> throwExceptionForUnknown(e.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    private void throwExceptionForUnknown(String dataAttrName) {
        if (!dataAttrName.startsWith("data-")) {
            throw new IllegalStateException();
        }
        if (!DATA_ATTRIBUTES.contains(dataAttrName)) {
            throw new TemplateSynthaxException(String.format("Unknown data-attribute: '%s'", dataAttrName));
        }
    }

    private ChildHolder createHierarchy(List<ChildHolder> childHolders) {
        return childHolders.stream().reduce((e1, e2) -> {
            e1.addChild(e2);
            return e2;
        }).orElseThrow();
    }

    private ContainerElement toContainerElement(Element element) {
        var containerElement = new ContainerElement(element.getTagName(), getMandatoryAttribute(ATTR_CONTAINER, element), element.getAttribute(ATTR_CONTAINER_WIDGET));
        if (element.getChildNodes().getLength() != 0) {
            throw new TemplateSynthaxException(String.format("elements with attribute \"%s\" must have no content", ATTR_CONTAINER));
        }
        return containerElement;
    }

    private TemplateElement toTemplateElement(Element element) {
        var templateElement = new TemplateElement(element.getTagName());
        getAttributes(element).forEach((name, rawValue) -> addAttribute(name, rawValue, templateElement));
        return templateElement;
    }


    private String getMandatoryAttribute(String name, Element element) {
        if (!element.hasAttribute(name)) {
            throw new TemplateSynthaxException(elementToString(element) + " must have attribute " + name);
        }
        return element.getAttribute(name);
    }

    private String elementToString(Element element) {
        var builder = new StringBuilder()
                .append("<")
                .append(element.getTagName());
        XmlUtil.getAttributes(element).forEach((name, value) -> builder.append(name).append("=").append("\"").append(value).append("\""));
        return builder.append("/>").toString();
    }

    protected void addAttribute(String name, String rawValue, ElementBase target) {
        var contentList = new MixedContentParser(rawValue).parse();
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
        var mixedContents = new MixedContentParser(text).parse();
        if (mixedContents.stream().noneMatch(ExpressionContent.class::isInstance)) {
            return new StaticTextNode(text);
        }
        return new MutableTextNode(mixedContents);
    }

    private IfBlock parseIf(Element element) {
        var src = element.getAttribute(ATTR_IF);
        return new IfBlock(expressionParser.parse(src));
    }

    private Loop parseFor(Element element) {
        var dataFor = element.getAttribute(ATTR_FOR);
        return loop(dataFor, element);
    }

    private Loop parseRepeat(Element element) {
        var repeat = element.getAttribute(ATTR_REPEAT);
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
        return element.hasAttribute(ATTR_CONTAINER);
    }

    private String nextVarName() {
        return "var" + (varIndex++);
    }
}
