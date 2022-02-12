package one.xis.js;

import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

class JSUtilTest {

    @Test
    void compile() throws ScriptException {
        String js = "var i = 1; var j= 2; i+j";
        assertThat(JSUtil.compile(js).eval()).isEqualTo(3D);
    }
}