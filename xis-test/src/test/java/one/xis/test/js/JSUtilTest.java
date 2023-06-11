package one.xis.test.js;

import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Disabled;
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
        bindings.put("cosnole", console);

        var func = JSUtil.function("function test() { return x*x }; test", bindings);
        var result = (Value) func.execute();


        assertThat(result.toString()).isEqualTo("1");

        func.setBinding("x", 2);
        result = (Value) func.execute();
        assertThat(result.toString()).isEqualTo("4");

    }

    @Test
    @Disabled
    void setter() throws ScriptException {
        var test = new TestObj();
        var script = "test.value=123;test";
        var bindings = new HashMap<String, Object>();
        bindings.put("test", test);

        var result = JSUtil.execute(script, bindings);
    }

    public static class TestObj {
        private String val;

        public String getValue() {
            return val;
        }


        public void setValue(String val) {
            this.val = val;
        }

        public void set_value(String val) {
            this.val = val;
        }

        public void set__value(String val) {
            this.val = val;
        }

        public void value(String val) {
            this.val = val;
        }
    }

}