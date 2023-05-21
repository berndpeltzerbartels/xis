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
        javascript += IOUtils.getResourceAsString("js/tag-handler/TagHandler.js");
        javascript += IOUtils.getResourceAsString("js/widget/Widget.js");
        javascript += IOUtils.getResourceAsString("js/tag-handler/WidgetContainerHandler.js");
        javascript += IOUtils.getResourceAsString("js/parse/TextContentParser.js");
        javascript += IOUtils.getResourceAsString("js/parse/CharIterator.js");
        javascript += IOUtils.getResourceAsString("js/parse/TextContent.js");
        javascript += IOUtils.getResourceAsString("js/parse/ExpressionParser.js");
        javascript += IOUtils.getResourceAsString("js/parse/Tokenizer.js");
        javascript += IOUtils.getResourceAsString("js/parse/TreeParser.js");
        javascript += IOUtils.getResourceAsString("js/parse/TokenLinker.js");
        javascript += IOUtils.getResourceAsString("js/connect/HttpClientMock.js");
        javascript += IOUtils.getResourceAsString("one/xis/widget/WidgetContainerHandlerTestMocks.js");
    }

    @Test
    void refresh() throws ScriptException {
        var document = Document.of("<html><body><xis:widget-container id=\"container\" default-widget=\"${x}\"/></body></html>");
        var container = document.getElementById("container");

        var script = javascript;
        script += "var handler = new WidgetContainerHandler(tag, new HttpClientMock(), widgets);";
        script += "handler.refresh(data)";

        var bindings = new HashMap<String, Object>();
        bindings.put("document", document);
        bindings.put("tag", container);
        bindings.put("widgets", new WidgetsMock());
        bindings.put("debug", new Debug());

        JSUtil.execute(script, bindings);

        assertThat(container.firstChild).isNotNull();
        assertThat(container.firstChild).isInstanceOf(Element.class);
        assertThat((((Element) container.firstChild).getAttribute("id"))).isEqualTo("widgetRoot");

    }

    public static class WidgetsMock {
        private final Element root;

        WidgetsMock() {
            this.root = new Element("div");
            this.root.setAttribute("id", "widgetRoot");
        }

        @SuppressWarnings("unused")
        public Element getWidgetRoot(String id) {
            return root;
        }

    }
}
