package one.xis.js;

import one.xis.template.TemplateElement;
import one.xis.template.WidgetModel;
import one.xis.utils.lang.CollectorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Parse template-models to javascript")
class JavascriptParserTest {

    @Nested
    @DisplayName("Simple widget with one element")
    class SimpleElementTest {

        private static final String WIDGET_NAME = "testWidget";

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
                    .filter(c -> isDerrivedFrom(c, "XISRoot"))
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


    private boolean isDerrivedFrom(JSClass jsClass, String superClassName) {
        return jsClass.getSuperClass().getClassName().equals(superClassName);
    }


    private Stream<JSClass> declaredCLasses(JSScript script) {
        return script.getDeclarations().stream()
                .filter(JSClass.class::isInstance)
                .map(JSClass.class::cast);
    }
}