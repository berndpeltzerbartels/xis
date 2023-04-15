package one.xis.js.init;

import one.xis.test.dom.Document;
import one.xis.test.js.Console;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InitializerTest {

    private String javascriptDefinitions;


    @BeforeAll
    void load() {
        javascriptDefinitions = IOUtils.getResourceAsString("js/init/Initializer.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/init/DomAccessor.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tags/TagHandler.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tags/NodeCache.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/tags/ForeachHandler.js");
        javascriptDefinitions += IOUtils.getResourceAsString("js/Functions.js");
    }

    @Test
    void repeatAttribute() throws ScriptException {
        var document = Document.of("<div><span repeat=\"item:items\"></span></div>");

        var script = javascriptDefinitions;
        script += "var initializer = new Initializer(new DomAccessor());";
        script += "initializer.initialize(document.rootNode);";

        var compiledScript = JSUtil.compile(script, Map.of("document", document, "console", new Console()));

        compiledScript.eval();

        assertThat(document.rootNode.getChildElementNames()).containsExactly("xis:foreach");
        assertThat(document.getElementByTagName("xis:foreach").getChildElementNames()).containsExactly("span");
        assertThat(document.getElementByTagName("xis:foreach").getAttribute("array")).isEqualTo("items");
        assertThat(document.getElementByTagName("xis:foreach").getAttribute("var")).isEqualTo("item");
    }

}
