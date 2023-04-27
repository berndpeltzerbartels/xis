package one.xis.js;

import lombok.Getter;
import one.xis.test.dom.Document;
import one.xis.test.dom.TextNode;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RefresherTest {


    private String javascript;
    private Document document;

    @BeforeEach
    void init() {
        javascript = IOUtils.getResourceAsString("js/Data.js");
        javascript += IOUtils.getResourceAsString("js/Refresher.js");
        javascript += IOUtils.getResourceAsString("js/Functions.js");
        document = Document.of("<html><body><div/></body></html>");
    }

    @Getter
    public static class Expression {
        private Map<String, Object> data;

        @SuppressWarnings("unused")
        public void evaluate(Map<String, Object> data) {
            this.data = data;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void refresh() throws ScriptException {
        var script = javascript;
        script += "var refresher = new Refresher();";
        script += "var data = new Data({});";
        script += "data.setValue('x', 'y');";
        script += "refresher.refreshNode(element, data);";

        var div = document.getElementByTagName("div");
        var textNode = new TextNode("");
        var expression = new Expression();
        textNode._expression = expression;
        div.appendChild(textNode);
        JSUtil.compile(script, Map.of("element", document.rootNode)).eval();

        var values = (Map<String, Object>) expression.getData().get("values");
        assertThat(values.get("x")).isEqualTo("y");
    }
}
