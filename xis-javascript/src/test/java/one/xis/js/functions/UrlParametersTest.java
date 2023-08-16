package one.xis.js.functions;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class UrlParametersTest {


    @Test
    void urlParameters() throws ScriptException {
        var result = (Map<String, String>) JSUtil.execute(Javascript.getScript(FUNCTIONS) + "\nurlParameters('xyz.html?a=v1&b=v2&c=v3')");

        assertThat(result.get("a")).isEqualTo("v1");
        assertThat(result.get("b")).isEqualTo("v2");
        assertThat(result.get("c")).isEqualTo("v3");
    }

}
