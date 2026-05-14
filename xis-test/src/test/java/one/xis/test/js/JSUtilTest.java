package one.xis.test.js;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class JSUtilTest {

    @Test
    void changeBindingsAfterCompilation() {
        var console = mock(Console.class);
        var bindings = new HashMap<String, Object>();
        bindings.put("x", 1);
        bindings.put("console", console);

        var func = JSUtil.function("function test() { return x*x }; test", bindings);
        var result = (Value) func.execute();


        assertThat(result.toString()).isEqualTo("1");

        func.setBinding("x", 2);
        result = (Value) func.execute();
        assertThat(result.toString()).isEqualTo("4");

    }

    @Test
    void errorVerbosity() throws ScriptException {
        var console = mock(Console.class);
        var bindings = new HashMap<String, Object>();
        bindings.put("console", console);
        bindings.put("x", "");


        // JSUtil.debug("x.push(123);", bindings);


    }

}