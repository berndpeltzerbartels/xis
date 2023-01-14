package one.xis.test.js;

import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JSUtilTest {

    @Test
    void compile() throws ScriptException {
        String js = "var i = 1; var j= 2; i+j";
        assertThat(JSUtil.compile(js).eval()).isEqualTo(3);
    }


    @Test
    void javaObject() throws ScriptException {
        var result = JSUtil.compile("testObj.value = 123;", Map.of("testObj", new TestObj())).eval();
    }

    public static class TestObj {
        public int value;
    }
}