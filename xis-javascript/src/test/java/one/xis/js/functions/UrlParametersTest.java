package one.xis.js.functions;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class UrlParametersTest {
    
    @Test
    void urlParameters() throws ScriptException {
        var result = JSUtil.execute(Javascript.getScript(FUNCTIONS) + "\nurlParameters('xyz.html?a=v1&b=v2&c=v3')");

        assertThat(result.getMember("a").asString()).isEqualTo("v1");
        assertThat(result.getMember("b").asString()).isEqualTo("v2");
        assertThat(result.getMember("c").asString()).isEqualTo("v3");
    }

}
