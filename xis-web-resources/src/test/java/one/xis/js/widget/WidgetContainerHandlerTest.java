package one.xis.js.widget;

import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.js.Debug;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetContainerHandlerTest {

    private String javascript;

    @BeforeEach
    void initScript() {
        javascript = IOUtils.getResourceAsString("js/Data.js");
        javascript += IOUtils.getResourceAsString("js/Functions.js");
        javascript += IOUtils.getResourceAsString("js/Refresher.js");
        javascript += IOUtils.getResourceAsString("js/tags/TagHandler.js");
        javascript += IOUtils.getResourceAsString("js/widget/Widget.js");
        javascript += IOUtils.getResourceAsString("js/widget/WidgetContainerHandler.js");
        javascript += IOUtils.getResourceAsString("one/xis/widget/WidgetContainerHandlerTestMocks.js");
    }

    @Test
    void refresh() throws ScriptException {
        var document = Document.of("<html><body><div id=\"container\"></div></body></html>");
        var containerDiv = document.getElementById("container");

        var script = javascript;
        script += "var handler = new WidgetContainerHandler(tag, widgets);";
        script += "handler.refresh(data)";

        var bindings = new HashMap<String, Object>();
        bindings.put("document", document);
        bindings.put("tag", containerDiv);
        bindings.put("debug", new Debug());

        JSUtil.execute(script, bindings);

        assertThat(containerDiv.firstChild).isNotNull();
        assertThat(containerDiv.firstChild).isInstanceOf(Element.class);
        assertThat((((Element) containerDiv.firstChild).getAttribute("id"))).isEqualTo("widgetRoot");

    }
}
