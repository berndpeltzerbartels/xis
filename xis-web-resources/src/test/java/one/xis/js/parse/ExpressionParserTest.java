package one.xis.js.parse;

import one.xis.test.dom.Document;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class ExpressionParserTest {

    private String javascript;
    private Document document;

    @BeforeEach
    void init() {
        javascript = ParserScriptsFiles.getContent();
        javascript += "var expressionParser = new ExpressionParser();";
    }


    @Test
    void methodWithVarParameter() throws ScriptException {
        var testScript = javascript + "expressionParser.parse('xyz(a.b)');";

        var result = (Map<String, Object>) JSUtil.execute(testScript);

        assertThat(result.get("type")).isEqualTo("FUNCTION");
        assertThat(result.get("next")).isNull();
        assertThat(result.get("name")).isEqualTo("xyz");

        var parameters = (List<Object>) result.get("parameters");
        assertThat(parameters).hasSize(1);

        var parameter = (Map<String, Object>) parameters.get(0);
        assertThat(parameter.get("type")).isEqualTo("VAR");
        assertThat((Collection<String>) parameter.get("path")).containsExactly("a", "b");
        assertThat(parameter.get("next")).isNull();
    }

    // TODO
}
