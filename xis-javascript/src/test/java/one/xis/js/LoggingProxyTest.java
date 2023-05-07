package one.xis.js;

import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class LoggingProxyTest {

    @Test
    void methodOneParameter() throws ScriptException {
        String script = "class Test { square(i) { console.log('real method square: i='+i);return i*i;}}\n";
        script += IOUtils.getResourceAsString("js/LoggingProxy.js_");
        script += "\nvar test = new Test();";
        script += "var proxy = new LoggingProxy(test);";
        script += "proxy.square(2);";

        var result = JSUtil.execute(script);

        assertThat(result).isEqualTo(4);
    }

    @Test
    void methodTwoParameter() throws ScriptException {
        String script = "class Test { mulitply(i, j) { console.log('real method square: i='+i+',j='+j);return i*j;}}\n";
        script += IOUtils.getResourceAsString("js/LoggingProxy.js_");
        script += "\nvar test = new Test();";
        script += "var proxy = new LoggingProxy(test);";
        script += "proxy.mulitply(3,4);";

        var result = JSUtil.execute(script);

        assertThat(result).isEqualTo(12);
    }


    @Test
    void twoMethods() throws ScriptException {
        String script = "class Test { " +
                "mulitply(i, j) { console.log('real method square: i='+i+',j='+j);return i*j;}\n" +
                "square(i) { console.log('real method square: i='+i);return i*i;}\n" +
                "}";
        script += IOUtils.getResourceAsString("js/LoggingProxy.js_");
        script += "\nvar test = new Test();";
        script += "var proxy = new LoggingProxy(test);";
        script += "proxy.square(proxy.mulitply(3,4));";

        var result = JSUtil.execute(script);

        assertThat(result).isEqualTo(12);
    }
}
