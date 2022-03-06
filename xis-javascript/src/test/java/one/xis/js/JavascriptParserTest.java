package one.xis.js;

import one.xis.template.TemplateElement;
import one.xis.template.WidgetModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class JavascriptParserTest {

    @Nested
    class SimpleElementTest {

        private WidgetModel widgetModel;
        private JSScript script;
        private JavascriptParser parser;

        @BeforeEach
        void setUp() {
            script = new JSScript();
            parser = new JavascriptParser(script);
            widgetModel = new WidgetModel("widget", new TemplateElement("div"));
        }

        @Test
        void parse() {
            parser.parse(Set.of(widgetModel));

            assertThat(script.getDeclarations()).hasSize(2);
            assertThat(script.getDeclarations().get(0)).isInstanceOf(JSClass.class);
            assertThat(script.getDeclarations().get(1)).isInstanceOf(JSClass.class);

            Set<String> superClassNames = script.getDeclarations().stream()
                    .filter(JSClass.class::isInstance)
                    .map(JSClass.class::cast)
                    .map(JSClass::getSuperClass)
                    .map(JSSuperClass::getClassName).collect(Collectors.toSet());

            assertThat(superClassNames).hasSize(2);
            assertThat(superClassNames).contains("XISRoot");
            assertThat(superClassNames).contains("XISWidgets");

            JSClass widgets = script.getDeclarations().stream()
                    .filter(JSClass.class::isInstance)
                    .map(JSClass.class::cast)
                    .filter(c -> c.getSuperClass().getClassName().equals("XISWidgets"))
                    .findFirst().orElseThrow();

            JSField widgetsField = widgets.getField("widgets");

            //assertThat(widgets.getField("widget").getValue().toString()).isEqualTo("Widget123");

        }
    }
}