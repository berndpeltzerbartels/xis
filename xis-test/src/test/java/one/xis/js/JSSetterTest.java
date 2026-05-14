package one.xis.js;

import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

class JSSetterTest {


    @Test
    void test() throws ScriptException {
        var js = "test.x='123'; test";
        var test = new one.xis.js.Test();
        // TODO How to use JS setter with java setter ?
    }

}
