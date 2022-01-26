package one.xis.template;

import one.xis.template.TemplateModel.*;
import one.xis.utils.io.IOUtils;
import one.xis.utils.xml.XmlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

class TemplateParserTest {

    private TemplateParser parser = new TemplateParser();

    @Nested
    class ParseTemplate1 {

        private Document document;

        @BeforeEach
        void prepareDocument() throws IOException, SAXException {
            document = XmlUtil.loadDocument(IOUtils.getResourceForClass(getClass(), "Template1.html"));
        }

        @Test
        void parse() throws TemplateSynthaxException, IOException {
            TemplateModel model = parser.parse(document, "test");

            IfElement ifElement = checkRootElement(model, IfElement.class, hasCondition("notEmpty(items)"), "condition was expected to be \"notEmpty(items)\"");
            XmlElement ul = checkChild(ifElement, XmlElement.class, hasTagName("ul"), "expected if to contain ul");
            ForElement forElement = checkChild(ul, ForElement.class, e -> matches(e, "items", "item", "i"), "no matching for was found");
            checkChild(forElement, XmlElement.class, hasTagName("li"), "expected for to contain li");
        }
    }


    @Nested
    class ParseTemplate2 {

        private Document document;

        @BeforeEach
        void prepareDocument() throws IOException, SAXException {
            document = XmlUtil.loadDocument(IOUtils.getResourceForClass(getClass(), "Template2.html"));
        }

        @Test
        void parse() throws TemplateSynthaxException, IOException {
            TemplateModel model = parser.parse(document, "test");

            IfElement ifElement = checkRootElement(model, IfElement.class, e -> e.getCondition().equals("visible"), "condition was expected to be \"visible\"");
            XmlElement div = checkChild(ifElement, XmlElement.class, e -> e.getTagName().equals("div"), "expected if to contain div");

            XmlElement h4 = checkChild(div, XmlElement.class, e -> e.getTagName().equals("h4"), "expected div to contain h4");
            TextContent textContent = checkChild(h4, TextContent.class, "expected h4 to contain TextContent");

            checkChild(textContent, StaticText.class, containsText("Details Of "), "h4 shoud contain text \"Details Of \"");
            checkChild(textContent, Expression.class, isExpression("product.name"), "h4 shoud contain expression \"product.name\"");

            checkChild(div, XmlElement.class, containsExpression("product.details").and(hasTagName("div")), "expected div with expression \"product.details\"");
            checkChild(div, XmlElement.class, containsExpression("moneyFormat(product.price, 'EUR')").and(hasTagName("div")).and(hasExpressionAttribute("class", "product.soldOut ? 'gray-price': 'price'")), "expected div with given conditions");
        }
    }


    private Predicate<IfElement> hasCondition(String condition) {
        return e -> e.getCondition().equals(condition);
    }

    private Predicate<XmlElement> hasTagName(String name) {
        return e -> e.getTagName().equals(name);
    }

    private Predicate<StaticText> containsText(String text) {
        return e -> e.getLines().contains(text);
    }

    private Predicate<Expression> isExpression(String expression) {
        return e -> e.getContent().equals(expression);
    }

    @SuppressWarnings("unchecked")
    <T extends TemplateElement> T checkRootElement(TemplateModel model, Class<T> elementType, Predicate<T> validator, String message) {
        if (!elementType.isInstance(model.getRoot())) {
            throw new TestException(model.getRoot() + "is not of type " + elementType);
        }
        T element = (T) model.getRoot();
        if (!validator.test(element)) {
            throw new TestException(message);
        }
        return element;
    }

    <T extends TemplateElement> T checkChild(Container container, Class<T> elementType, Predicate<T> filter, String message) {
        return container.getElements().stream()//
                .filter(elementType::isInstance)
                .map(elementType::cast)
                .filter(filter)
                .findFirst().orElseThrow(() -> new TestException(message));
    }


    <T extends TemplateElement> T checkChild(Container container, Class<T> elementType, String message) {
        return container.getElements().stream()//
                .filter(elementType::isInstance)
                .map(elementType::cast)
                .findFirst().orElseThrow(() -> new TestException(message));
    }

    boolean matches(ForElement e1, String arrayVar, String itemVar, String indexVar) {
        if (!e1.getIndexVarName().equals(indexVar)) {
            return false;
        }
        if (!e1.getArrayVarName().equals(arrayVar)) {
            return false;
        }
        if (!e1.getElementVarName().equals(itemVar)) {
            return false;
        }
        return true;
    }

    private Predicate<XmlElement> containsExpression(String expression) {
        return e -> e.getElements().stream()
                .filter(TextContent.class::isInstance)
                .map(TextContent.class::cast)
                .map(TextContent::getTextElements)
                .flatMap(Collection::stream)
                .filter(Expression.class::isInstance)
                .map(Expression.class::cast)
                .anyMatch(expr -> expr.getContent().equals(expression));
    }

    private <T extends TextElement> Predicate<XmlElement> hasStaticTextAttribute(XmlElement element, String name, String content) {
        return xmlElement -> xmlElement.getAttributes().entrySet().stream()
                .filter(entry -> entry.getKey().equals(name))
                .map(Map.Entry::getValue)
                .map(TextContent::getTextElements)
                .filter(StaticText.class::isInstance)
                .map(StaticText.class::cast)
                .anyMatch(text -> text.getLines().contains(content));
    }

    private <T extends TextElement> Predicate<XmlElement> hasExpressionAttribute(String name, String expression) {
        return xmlElement -> xmlElement.getAttributes().entrySet().stream()
                .filter(entry -> entry.getKey().equals(name))
                .map(Map.Entry::getValue)
                .map(TextContent::getTextElements)
                .flatMap(Collection::stream)
                .filter(Expression.class::isInstance)
                .map(Expression.class::cast)
                .anyMatch(expr -> expr.getContent().equals(expression));
    }


}