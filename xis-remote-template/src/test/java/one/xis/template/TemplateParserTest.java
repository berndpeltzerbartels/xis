package one.xis.template;

import one.xis.utils.io.IOUtils;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.xml.XmlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.IOException;

import static one.xis.utils.lang.ClassUtils.cast;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateParserTest {

    private final TemplateParser parser = new TemplateParser();

    @Nested
    class SimpleElementTest {
        private Document document;

        @BeforeEach
        void init() {
            Attr attr = mock(Attr.class);
            when(attr.getName()).thenReturn("class");
            when(attr.getValue()).thenReturn("gold");

            NamedNodeMap namedNodeMap = mock(NamedNodeMap.class);
            when(namedNodeMap.getLength()).thenReturn(1);
            when(namedNodeMap.item(0)).thenReturn(attr);

            Element element = mock(Element.class);
            when(element.getTagName()).thenReturn("div");
            when(element.getAttributes()).thenReturn(namedNodeMap);

            NodeList childNodes = mock(NodeList.class);
            when(childNodes.getLength()).thenReturn(0);
            when(element.getChildNodes()).thenReturn(childNodes);

            document = mock(Document.class);
            when(document.getDocumentElement()).thenReturn(element);
        }

        @Test
        void element() {
            WidgetModel widgetModel = new TemplateParser().parse(document, "123");

            TemplateElement element = (TemplateElement) widgetModel.getRootNode();
            assertThat(element.getElementName()).isEqualTo("div");
            assertThat(element.getStaticAttributes()).containsKey("class");
            assertThat(element.getStaticAttributes().get("class")).isEqualTo("gold");
        }


    }

    @Nested
    class ParseTemplate1 {

        private Document document;

        @BeforeEach
        void prepareDocument() throws IOException, SAXException {
            document = XmlUtil.loadDocument(IOUtils.getResourceForClass(getClass(), "Template1.html"));
        }

        @Test
        void parse() throws TemplateSynthaxException, IOException {
            var widgetModel = parser.parse(document, "test");

            IfBlock ifBlock = cast(widgetModel.getRootNode(), IfBlock.class);
            TemplateElement ul = onlyChild(ifBlock, TemplateElement.class);
            assertThat(ul.getElementName()).isEqualTo("ul");

            Loop loop = onlyChild(ul, Loop.class);
            TemplateElement li = onlyChild(loop, TemplateElement.class);
            assertThat(li.getElementName()).isEqualTo("li");

            MutableTextNode mutableTextNode = onlyChild(li, MutableTextNode.class);
            MixedContent mixedContent = CollectionUtils.onlyElement(mutableTextNode.getContent());
            ExpressionContent expressionContent = cast(mixedContent, ExpressionContent.class);
            assertThat(expressionContent.getExpression().getFunction()).isNull();

            ExpressionArg arg = CollectionUtils.onlyElement(expressionContent.getExpression().getVars());
            ExpressionVar expressionVar = cast(arg, ExpressionVar.class);
            assertThat(expressionVar.getVarName()).isEqualTo("item.name");

            MutableAttribute mutableAttribute = CollectionUtils.onlyElement(li.getMutableAttributes().values());
            ExpressionContent classAttribute = cast(CollectionUtils.onlyElement(mutableAttribute.getContents()), ExpressionContent.class);
            assertThat(classAttribute.getExpression().getFunction()).isEqualTo("oddOrEven");
            ExpressionArg expressionArg = CollectionUtils.onlyElement(classAttribute.getExpression().getVars());
            assertThat(cast(expressionArg, ExpressionVar.class).getVarName()).isEqualTo("i");
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
            var widgetModel = parser.parse(document, "test");

            var loop = cast(widgetModel.getRootNode(), Loop.class);
            var div = onlyChild(loop, TemplateElement.class);
            assertThat(div.getElementName()).isEqualTo("div");

            var h4 = cast(div.getChildren().get(0), TemplateElement.class);
            assertThat(h4.getElementName()).isEqualTo("h4");

            var h4TextNode = (MutableTextNode) h4.getChildren().get(0);
            assertThat(h4TextNode.getContent()).hasSize(2);
            assertThat(h4TextNode.getContent().get(0)).isEqualTo(new StaticContent("Details Of "));
            assertThat(h4TextNode.getContent().get(1)).isInstanceOf(ExpressionContent.class);


            var anchor = cast(div.getChildren().get(1), TemplateElement.class);
            assertThat(anchor.getElementName()).isEqualTo("a");

            var href = anchor.getMutableAttributes().get("href");
            assertThat(href.getContents()).hasSize(3);
            assertThat(cast(href.getContents().get(0), StaticContent.class).getContent()).isEqualTo("/products/");
            assertThat(cast(href.getContents().get(1), ExpressionContent.class).getExpression().getContent()).isEqualTo("product.id");
            assertThat(cast(href.getContents().get(2), StaticContent.class).getContent()).isEqualTo(".html");

        }
    }

    @SuppressWarnings("unchecked")
    private <T> T onlyChild(ChildHolder childHolder, Class<T> childType) {
        assertThat(childHolder.getChildren()).hasSize(1);
        assertThat(childHolder.getChildren().get(0)).isInstanceOf(childType);
        return (T) childHolder.getChildren().get(0);
    }

}