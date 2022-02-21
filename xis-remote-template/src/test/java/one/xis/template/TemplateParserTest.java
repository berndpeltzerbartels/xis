package one.xis.template;

import one.xis.utils.io.IOUtils;
import one.xis.utils.xml.XmlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateParserTest {

    private final TemplateParser parser = new TemplateParser();

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

            assertThat(widgetModel.getElementName()).isEqualTo("ul");
            assertThat(widgetModel.getLoop()).isNotNull();
            assertThat(widgetModel.getLoop().getItemVarName()).isEqualTo("item");
            assertThat(widgetModel.getLoop().getIndexVarName()).isEqualTo("i");
            assertThat(widgetModel.getLoop().getNumberVarName()).isEqualTo("number");
            assertThat(widgetModel.getLoop().getArraySource().getVars()).hasSize(1);
            assertThat(widgetModel.getLoop().getArraySource().getVars().get(0)).isEqualTo(new ExpressionVar("items"));

            var childElements = widgetModel.getChildren().stream().filter(TemplateElement.class::isInstance).map(TemplateElement.class::cast).collect(Collectors.toList());
            assertThat(childElements).hasSize(1);
            var li = childElements.get(0);
            assertThat(li.getElementName()).isEqualTo("li");
            assertThat(li.getMutableAttributes().get("class")).isNotNull();

            List<MixedContent> classAttributeContent = li.getMutableAttributes().get("class").getContents();
            assertThat(classAttributeContent).hasSize(1);
            assertThat(classAttributeContent.get(0)).isInstanceOf(ExpressionContent.class);
            ExpressionContent classAttributeExprContent = (ExpressionContent) classAttributeContent.get(0);
            Expression classAttribute = classAttributeExprContent.getExpression();
            assertThat(classAttribute.getFunction()).isEqualTo("oddOrEven");
            assertThat(classAttribute.getVars()).hasSize(1);
            assertThat(classAttribute.getVars().get(0)).isEqualTo(new ExpressionVar("i"));
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

            assertThat(widgetModel.getElementName()).isEqualTo("div");

            var ifCondition = widgetModel.getIfCondition();
            assertThat(ifCondition.getExpression().getVars()).hasSize(1);
            assertThat(ifCondition.getExpression().getVars().get(0)).isEqualTo(new ExpressionVar("visible"));

            List<TemplateElement> childElements = widgetModel.getChildren().stream().filter(TemplateElement.class::isInstance).map(TemplateElement.class::cast).collect(Collectors.toList());

            assertThat(childElements).hasSize(2);

            var h4 = childElements.get(0);
            assertThat(h4.getChildren()).hasSize(1);
            assertThat(h4.getChildren().get(0)).isInstanceOf(TextNode.class);
            var h4TextNode = (MutableTextNode) h4.getChildren().get(0);
            assertThat(h4TextNode.getContent()).hasSize(2);
            assertThat(h4TextNode.getContent().get(0)).isEqualTo(new StaticContent("Details Of "));
            assertThat(h4TextNode.getContent().get(1)).isInstanceOf(ExpressionContent.class);

            ExpressionContent expressionContent = (ExpressionContent) h4TextNode.getContent().get(1);
            assertThat(expressionContent.getExpression().getContent()).isEqualTo("product.name");


            var anchor = childElements.get(1);
            MutableAttribute href = anchor.getMutableAttributes().get("href");
            assertThat(href.getContents()).hasSize(3);
            assertThat(href.getContents().get(0)).isInstanceOf(StaticContent.class);
            assertThat(href.getContents().get(1)).isInstanceOf(ExpressionContent.class);
            assertThat(href.getContents().get(2)).isInstanceOf(StaticContent.class);

            assertThat(((StaticContent) href.getContents().get(0)).getContent()).isEqualTo("/products/");
            assertThat(((ExpressionContent) href.getContents().get(1)).getExpression().getContent()).isEqualTo("product.id");
            assertThat(((StaticContent) href.getContents().get(2)).getContent()).isEqualTo(".html");


            assertThat(anchor.getChildren()).hasSize(1);
            assertThat(anchor.getChildren().get(0)).isInstanceOf(TextNode.class);

            var node = (StaticTextNode) anchor.getChildren().get(0);
            assertThat(node.getContent()).isEqualTo("Details");
        }
    }


}