package one.xis.js.frontlet;

import one.xis.js.Javascript;
import one.xis.js.JavascriptSource;
import one.xis.test.dom.Document;
import one.xis.test.dom.DocumentImpl;
import one.xis.test.dom.ElementImpl;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class FrontletsTest {

    @Test
    void loadFrontlets() throws ScriptException {
        var script = Javascript.getScript(JavascriptSource.CLASSES);
        script += IOUtils.getResourceAsString("one/xis/frontlet/FrontletsTestMocks.js");
        script += "var frontlets = new Frontlets(client);\n";
        script += "frontlets.loadFrontlets(config);frontlets.frontlets";

        Function<String, ElementImpl> createElement = name -> {
            var element = new ElementImpl(name);
            element.appendChild(new ElementImpl("div"));
            return element;
        };
        Function<String, String> trim = String::trim;

        var bindings = new HashMap<String, Object>();
        bindings.put("document", new DocumentImpl("html"));
        bindings.put("createElement", createElement);
        bindings.put("trim", trim);
        Function<String, ElementImpl> htmlToElement = this::htmlToElement;
        bindings.put("htmlToElement", htmlToElement);

        var result = JSUtil.execute(script, bindings);

        assertThat(result.getMember("frontletId")).isNotNull();

        var frontletData = result.getMember("frontletId");

        assertThat(frontletData.getMember("id").asString()).isEqualTo("frontletId");
        assertThat(frontletData.getMember("html").asString()).startsWith("<xis:template");
        assertThat(frontletData.getMember("frontletAttributes")).isNotNull();
    }

    public ElementImpl htmlToElement(String content) {
        var doc = Document.of(content);
        return (ElementImpl) doc.getDocumentElement();
    }


}
