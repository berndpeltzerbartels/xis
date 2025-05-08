package one.xis.js.widget;

import one.xis.js.Javascript;
import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Window;
import one.xis.test.js.Debug;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;

import static one.xis.js.JavascriptSource.*;
import static org.assertj.core.api.Assertions.assertThat;

class WidgetContainerHandlerTest {

    @Test
    void refresh() throws ScriptException {
        var document = Document.of("<html><body><xis:widget-container id=\"container\" default-widget=\"${x}\"/></body></html>");
        var container = document.getElementById("container");

        var script = Javascript.getScript(CLASSES, FUNCTIONS, TEST, TEST_APP_INSTANCE);
        script += IOUtils.getResourceAsString("one/xis/widget/WidgetContainerHandlerTestMocks.js");
        script += "var tagHandlers = {refresh(data){}, getRootHandler(e){ return { publishBindEvent(){}};}};";
        script += "var handler = new WidgetContainerHandler(tag, client, widgets, new WidgetContainers(), tagHandlers);";
        script += "handler.refresh(data)";

        var bindings = new HashMap<String, Object>();
        bindings.put("document", document);
        bindings.put("tag", container);
        bindings.put("widgets", new WidgetsMock());
        bindings.put("debug", new Debug());
        bindings.put("window", new Window());

        JSUtil.execute(script, bindings);

        assertThat(container.firstChild).isNotNull();
        assertThat(container.firstChild).isInstanceOf(Element.class);
        assertThat((((Element) container.firstChild).getAttribute("id"))).isEqualTo("widgetRoot");

    }

    public static class WidgetInstance {
        public Element root;
        public Object widgetState;
        public Object rootHandler;
    }

    public static class RootHandler {
        public Object parentHandler;

        public void publishBindEvent() {
        }
    }

    public static class WidgetsMock {

        @SuppressWarnings("unused")
        public WidgetInstance getWidgetInstance(String id) {
            var widget = new WidgetInstance();
            widget.root = new Element("div");
            widget.root.setAttribute("id", "widgetRoot");
            widget.rootHandler = new RootHandler();
            return widget;
        }
    }
}
