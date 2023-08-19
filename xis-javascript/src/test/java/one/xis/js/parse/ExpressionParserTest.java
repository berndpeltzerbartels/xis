package one.xis.js.parse;

import one.xis.js.Javascript;
import one.xis.test.dom.Document;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class ExpressionParserTest {

    private String javascript;
    private Document document;

    @BeforeEach
    void init() {
        javascript = Javascript.getScript(CLASSES, FUNCTIONS);
        javascript += "var expressionParser = new ExpressionParser();";
    }


    @Test
    void methodWithVarParameter() throws ScriptException {
        var testScript = javascript + "expressionParser.parse('xyz(a.b)');";

        var result = JSUtil.execute(testScript);

        assertThat(result.getMember("type").asString()).isEqualTo("FUNCTION");
        assertThat(result.getMember("next").asString()).isNull();
        assertThat(result.getMember("name").asString()).isEqualTo("xyz");

        var parameters = (List<Object>) result.getMember("parameters").as(List.class);
        assertThat(parameters).hasSize(1);

        var parameter = (Map<String, Object>) parameters.get(0);
        assertThat(parameter.get("type")).isEqualTo("VAR");
        assertThat((Collection<String>) parameter.get("path")).containsExactly("a", "b");
        assertThat(parameter.get("next")).isNull(); // TODO check with function in b
    }

    // TODO
}
