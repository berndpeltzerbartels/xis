package one.xis.js;

import one.xis.template.*;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.CollectorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static one.xis.utils.lang.ClassUtils.cast;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Parse template-models to javascript")
class JavascriptParserTest {

    private static final String WIDGET_NAME = "testWidget";

    @Nested
    @DisplayName("Simple widget with one element")
    class SimpleElementTest {

        private WidgetModel widgetModel;
        private JSScript script;
        private JavascriptParser parser;

        @BeforeEach
        void setUp() {
            script = new JSScript();
            parser = new JavascriptParser(script);
            widgetModel = new WidgetModel(WIDGET_NAME, new TemplateElement("div"));
        }

        @Test
        void parse() {
            parser.parse(Set.of(widgetModel));

            JSClass widgets = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISWidgets"))
                    .collect(CollectorUtils.onlyElement());

            JSClass widget = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISWidget"))
                    .collect(CollectorUtils.onlyElement());

            JSClass div = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISElement"))
                    .collect(CollectorUtils.onlyElement());

            JSField widgetsField = widgets.getField("widgets");

            assertThat(widgetsField).extracting(JSField::getValue)
                    .extracting(JSJsonValue.class::cast)
                    .extracting(JSJsonValue::getFields)
                    .extracting(fields -> fields.get(WIDGET_NAME))
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .isEqualTo(widget);

            assertThat(widget.getFields().get("root"))
                    .extracting(JSField::getValue)
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .isEqualTo(div);
        }
    }

    @Nested
    @DisplayName("Simple widget with one element")
    class TwoSimpleWidgetsTest {

        private static final String WIDGET_NAME2 = "testWidget2";

        private WidgetModel widgetModel1;
        private WidgetModel widgetModel2;
        private JSScript script;
        private JavascriptParser parser;

        @BeforeEach
        void setUp() {
            script = new JSScript();
            parser = new JavascriptParser(script);
            widgetModel1 = new WidgetModel(WIDGET_NAME, new TemplateElement("div"));
            widgetModel2 = new WidgetModel(WIDGET_NAME2, new TemplateElement("span"));
        }

        @Test
        void parse() {
            parser.parse(List.of(widgetModel1, widgetModel2));

            JSClass widgets = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISWidgets"))
                    .collect(CollectorUtils.onlyElement());


            JSJsonValue widgetsJson = (JSJsonValue) widgets.getField("widgets").getValue();
            JSClass root1 = cast(widgetsJson.getFields().get(WIDGET_NAME), JSContructorCall.class).getJsClass();
            JSClass root2 = cast(widgetsJson.getFields().get(WIDGET_NAME2), JSContructorCall.class).getJsClass();


            assertThat(root1.getField("root"))
                    .extracting(JSField::getValue)
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .extracting(JSClass::getSuperClass)
                    .extracting(JSClass::getClassName)
                    .isEqualTo("XISElement");

            assertThat(root2.getField("root"))
                    .extracting(JSField::getValue)
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .extracting(JSClass::getSuperClass)
                    .extracting(JSClass::getClassName)
                    .isEqualTo("XISElement");
        }
    }

    @Nested
    @DisplayName("If-block and loop with element")
    class IfAndForLoopTest {

        private WidgetModel widgetModel;
        private JSScript script;
        private JavascriptParser parser;

        @BeforeEach
        void setUp() {
            ExpressionParser expressionParser = new ExpressionParser();
            IfBlock ifBlock = new IfBlock(expressionParser.parse("x"));
            Loop loop = new Loop(expressionParser.parse("y"), "", "", "", "");
            TemplateElement templateElement = new TemplateElement("div");

            ifBlock.addChild(loop);
            loop.addChild(templateElement);
            widgetModel = new WidgetModel(WIDGET_NAME, ifBlock);

            script = new JSScript();
            parser = new JavascriptParser(script);

        }

        @Test
        void parse() {
            parser.parse(Set.of(widgetModel));

            JSClass ifClass = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISIf"))
                    .collect(CollectorUtils.onlyElement());

            JSClass loopClass = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISLoop"))
                    .collect(CollectorUtils.onlyElement());

            JSClass widgets = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISWidgets"))
                    .collect(CollectorUtils.onlyElement());

            JSClass widget = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISWidget"))
                    .collect(CollectorUtils.onlyElement());

            JSClass div = declaredCLasses(script)
                    .filter(c -> isDerrivedFrom(c, "XISElement"))
                    .collect(CollectorUtils.onlyElement());

            JSField widgetsField = widgets.getField("widgets");

            assertThat(widgetsField).extracting(JSField::getValue)
                    .extracting(JSJsonValue.class::cast)
                    .extracting(JSJsonValue::getFields)
                    .extracting(fields -> fields.get(WIDGET_NAME))
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .isEqualTo(widget);


            assertThat(widget.getFields().get("root"))
                    .extracting(JSField::getValue)
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .isEqualTo(ifClass);


            assertThat(ifClass.getFields().get("children"))
                    .extracting(JSField::getValue)
                    .extracting(JSArray.class::cast)
                    .extracting(arr -> CollectionUtils.onlyElement(arr.getElements()))
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .isEqualTo(loopClass);

            assertThat(loopClass.getOverriddenMethods().get("createChildren"))
                    .extracting(JSMethod::getStatements)
                    .extracting(CollectionUtils::onlyElement)
                    .extracting(JSReturn.class::cast)
                    .extracting(JSReturn::getValue)
                    .extracting(JSArray.class::cast)
                    .extracting(JSArray::getElements)
                    .extracting(CollectionUtils::onlyElement)
                    .extracting(JSContructorCall.class::cast)
                    .extracting(JSContructorCall::getJsClass)
                    .isEqualTo(div);
        }
    }

    private boolean isDerrivedFrom(JSClass jsClass, String superClassName) {
        return jsClass.getSuperClass().getClassName().equals(superClassName);
    }


    private Stream<JSClass> declaredCLasses(JSScript script) {
        return script.getDeclarations().stream()
                .filter(JSClass.class::isInstance)
                .map(JSClass.class::cast);
    }
}