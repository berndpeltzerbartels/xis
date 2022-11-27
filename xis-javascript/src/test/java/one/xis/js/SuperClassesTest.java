package one.xis.js;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

class SuperClassesTest {

    @Test
    void allCompiles() throws ScriptException {
        JSUtil.compile(IOUtils.getResourceAsString("js/xis.js"));
    }
}