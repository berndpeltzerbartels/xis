package one.xis.js;

import org.junit.jupiter.api.DisplayName;

@DisplayName("Parse template-models to javascript")
class JavascriptParserTest {

    /*
    private static final String WIDGET_NAME = "testWidget";

    @Nested
    @DisplayName("Simple widget with one element")
    class SimpleElementWidgetTest {

        private WidgetTemplateModel widgetTemplateModel;
        private JavascriptTemplateParser parser;

        @BeforeEach
        void setUp() {
            parser = new JavascriptTemplateParser();
            widgetTemplateModel = new WidgetTemplateModel(WIDGET_NAME, new TemplateElement("div"));
        }

        @Test
        void parse() {
            parser.parse(Collections.emptySet(), Set.of(widgetTemplateModel));

            JSClass widgets = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISWidgets"))
                    .collect(CollectorUtils.onlyElement());

            JSClass widget = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISWidget"))
                    .collect(CollectorUtils.onlyElement());

            JSClass div = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISElement"))
                    .collect(CollectorUtils.onlyElement());

            JSField widgetsField = widgets.getField("widgets");

            assertThat(widgetsField).extracting(JSField::getValue)
                    .extracting(JSObject.class::cast)
                    .extracting(JSObject::getFields)
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

        private WidgetTemplateModel widgetTemplateModel1;
        private WidgetTemplateModel widgetTemplateModel2;
        private JavascriptTemplateParser parser;

        @BeforeEach
        void setUp() {
            parser = new JavascriptTemplateParser();
            widgetTemplateModel1 = new WidgetTemplateModel(WIDGET_NAME, new TemplateElement("div"));
            widgetTemplateModel2 = new WidgetTemplateModel(WIDGET_NAME2, new TemplateElement("span"));
        }

        @Test
        void parse() {
            parser.parse(Collections.emptySet(), List.of(widgetTemplateModel1, widgetTemplateModel2));

            JSClass widgets = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISWidgets"))
                    .collect(CollectorUtils.onlyElement());


            JSObject widgetsJson = (JSObject) widgets.getField("widgets").getValue();
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
    class IfAndForLoopWidgetTest {

        private WidgetTemplateModel widgetTemplateModel;
        private JavascriptTemplateParser parser;

        @BeforeEach
        void setUp() {
            ExpressionParser expressionParser = new ExpressionParser();
            IfBlock ifBlock = new IfBlock(expressionParser.parse("x"));
            Loop loop = new Loop(expressionParser.parse("y"), "", "", "", "");
            TemplateElement templateElement = new TemplateElement("div");

            ifBlock.addChild(loop);
            loop.addChild(templateElement);
            widgetTemplateModel = new WidgetTemplateModel(WIDGET_NAME, ifBlock);

            parser = new JavascriptTemplateParser();

        }

        @Test
        void parse() {
            parser.parse(Collections.emptySet(), Set.of(widgetTemplateModel));

            JSClass ifClass = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISIf"))
                    .collect(CollectorUtils.onlyElement());

            JSClass loopClass = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISLoop"))
                    .collect(CollectorUtils.onlyElement());

            JSClass widgets = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISWidgets"))
                    .collect(CollectorUtils.onlyElement());

            JSClass widget = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISWidget"))
                    .collect(CollectorUtils.onlyElement());

            JSClass div = declaredCLasses(parser.getScript())
                    .filter(c -> isDerrivedFrom(c, "XISElement"))
                    .collect(CollectorUtils.onlyElement());

            JSField widgetsField = widgets.getField("widgets");

            assertThat(widgetsField).extracting(JSField::getValue)
                    .extracting(JSObject.class::cast)
                    .extracting(JSObject::getFields)
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

    // TODO page tests
    private boolean isDerrivedFrom(JSClass jsClass, String superClassName) {
        return jsClass.getSuperClass().getClassName().equals(superClassName);
    }


    private Stream<JSClass> declaredCLasses(JSScript script) {
        return script.getDeclarations().stream()
                .filter(JSClass.class::isInstance)
                .map(JSClass.class::cast);
    }

     */
}