package one.xis.js;

import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.script.ScriptException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FunctionsTest {

    private String functions;

    @BeforeAll
    void loadFunctions() {
        functions = Javascript.getScript(JavascriptSource.FUNCTIONS);
    }

    @Test
    void trim() throws ScriptException {
        var strings = "['xyz',' x',' x',' x y ']";
        var script = functions + "var a = " + strings + "; a.map(v => trim(v));";

        var result = (List<String>) JSUtil.execute(script).as(List.class);

        assertThat(result).containsExactly("xyz", "x", "x", "x y");
    }

    @Test
    void cloneArr() throws ScriptException {
        var result = (List<Integer>) JSUtil.execute(functions + "cloneArr([1,2,3])").as(List.class);
        assertThat(result).containsExactly(1, 2, 3);
    }

}
