package one.xis.js.widget;

import one.xis.js.Javascript;
import one.xis.js.JavascriptSource;
import one.xis.test.dom.Document;
import one.xis.test.dom.DocumentImpl;
import one.xis.test.dom.Element;
import one.xis.test.dom.ElementImpl;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetsTest {

    @Test
    void loadWidgets() throws ScriptException {
        var script = Javascript.getScript(JavascriptSource.CLASSES);
        script += IOUtils.getResourceAsString("one/xis/widget/WidgetsTestMocks.js");
        script += "var widgets = new Widgets(client);\n";
        script += "widgets.loadWidgets(config);widgets.widgets";

        Function<String, Element> createElement = name -> {
            var element = new ElementImpl(name);
            element.appendChild(new ElementImpl("div"));
            return element;
        };
        Function<String, String> trim = String::trim;

        var bindings = new HashMap<String, Object>();
        bindings.put("document", new DocumentImpl("html"));
        bindings.put("createElement", createElement);
        bindings.put("trim", trim);
        Function<String, Element> htmlToElement = this::htmlToElement;
        bindings.put("htmlToElement", htmlToElement);

        var result = JSUtil.execute(script, bindings);

        assertThat(result.getMember("widgetId")).isNotNull();

        var widgetData = result.getMember("widgetId");

        assertThat(widgetData.getMember("id").asString()).isEqualTo("widgetId");
        assertThat(widgetData.getMember("html").asString()).startsWith("<xis:template");
        assertThat(widgetData.getMember("widgetAttributes")).isNotNull();
    }

    public Element htmlToElement(String content) {
        var doc = Document.of(content);
        return doc.getDocumentElement();
    }


}
