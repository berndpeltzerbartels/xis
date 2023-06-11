package one.xis.js.widget;

import one.xis.js.Javascript;
import one.xis.js.JavascriptSource;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetsTest {

    @Test
    @SuppressWarnings("unchecked")
    void loadWidgets() throws ScriptException {
        var script = Javascript.getScript(JavascriptSource.CLASSES);
        script += IOUtils.getResourceAsString("one/xis/widget/WidgetsTestMocks.js");
        script += "var widgets = new Widgets(client);\n";
        script += "widgets.loadWidgets(config);widgets.widgets";

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
        Function<String, Element> htmlToElement = this::htmlToElement;
        bindings.put("htmlToElement", htmlToElement);

        var result = (Map<String, Object>) JSUtil.execute(script, bindings);

        assertThat(result).hasSize(1);
        assertThat(result.get("widgetId")).isNotNull();

        var widgetData = (Map<String, Object>) result.get("widgetId");

        assertThat(widgetData.get("id")).isEqualTo("widgetId");
        assertThat(((Element) widgetData.get("root")).localName).isEqualTo("xis:template");
        assertThat(widgetData.get("attributes")).isNotNull();
    }

    public Element htmlToElement(String content) {
        var doc = Document.of(content);
        return doc.rootNode;
    }


}
