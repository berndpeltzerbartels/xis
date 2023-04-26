package one.xis.js.widget;

import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class WidgetsTest {


    private String javascript;

    @BeforeEach
    void initScript() {
        javascript = IOUtils.getResourceAsString("js/Data.js");
        javascript += IOUtils.getResourceAsString("js/tags/TagHandler.js");
        javascript += IOUtils.getResourceAsString("js/widget/Widget.js");
        javascript += IOUtils.getResourceAsString("js/widget/Widgets.js");
        javascript += IOUtils.getResourceAsString("js/widget/WidgetContainerHandler.js");
        javascript += IOUtils.getResourceAsString("one/xis/widget/WidgetsTestMocks.js");
    }

    @Test
    void loadWidgets() throws ScriptException {
        var script = javascript;
        script += "var widgets = new Widgets(client);";
        script += "widgets.loadWidgets(config); widgets.getWidget('widgetId');";

        Function<String, Element> createElement = name -> {
            var element = new Element(name);
            element.appendChild(new Element("div"));
            return element;
        };

        Function<String, String> trim = String::trim;

        var bindings = new HashMap<String, Object>();
        bindings.put("document", new Document("html"));
        bindings.put("createElement", createElement);
        bindings.put("trim", trim);

        var result = (Map<String, Object>) JSUtil.execute(script, bindings);
        assertThat(result).hasSize(1);
        assertThat(result.get("widgetId")).isNotNull();

        var widgetData = (Map<String, Object>) result.get("widgetId");

        assertThat(widgetData.get("id")).isEqualTo("widgetId");
        assertThat(widgetData.get("root").toString()).isEqualTo("<div>");
        assertThat(widgetData.get("attributes")).isNotNull();

    }
}
