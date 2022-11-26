package one.xis.js;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

class SuperClassesTest {

    @Test
    void allCompiles() throws ScriptException {
        String functions = IOUtils.getResourceAsString("js/functions.js");
        String classes1 = IOUtils.getResourceAsString("js/classes1.js");
        String classes2 = IOUtils.getResourceAsString("js/classes2.js");
        String classes3 = IOUtils.getResourceAsString("js/classes3.js");
        String classes4 = IOUtils.getResourceAsString("js/classes4.js");
        String globals = IOUtils.getResourceAsString("js/xis-globals.js");

        JSUtil.compile(functions + classes1 + classes2 + classes3 + classes4 + globals);
    }
}