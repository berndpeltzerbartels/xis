package one.xis.remote.js;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSVarDeclarationTest {

    private String javascript;

    @BeforeEach
    void setUp() {
        JSVar jsVar = new JSVar("i");
        JSVarDeclaration declaration = new JSVarDeclaration(jsVar, "42");
        javascript = JSUtil.javascript(declaration);
    }

    @Test
    void writeJS() throws ScriptException {
        var result = JSUtil.execute(javascript + ";i");

        assertThat(result).isEqualTo(42);
    }
}