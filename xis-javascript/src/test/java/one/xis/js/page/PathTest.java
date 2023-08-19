package one.xis.js.page;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.List;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class PathTest {

    @Test
    @DisplayName("Path has no variables, but the given url does not match")
    void noVariablesNoMatch() throws ScriptException {
        var javascript = Javascript.getScript(CLASSES);
        javascript += "var path = new Path();\n";
        javascript += "var pathElement = new PathElement({type: 'static', content:'/abc/xyz.html'});\n";
        javascript += "path.pathElement = pathElement;\n";
        javascript += "path.evaluate(\"/abc/xyz1.html\");\n";

        assertThat(JSUtil.execute(javascript).asBoolean()).isFalse();

    }

    @Test
    @DisplayName("Path has no variables and the given url matches")
    void noVariablesMatch() throws ScriptException {
        var javascript = Javascript.getScript(CLASSES);
        javascript += "var path = new Path();\n";
        javascript += "var pathElement = new PathElement({type: 'static', content:'/abc/xyz.html'});\n";
        javascript += "path.pathElement = pathElement;\n";
        javascript += "path.pathElement = pathElement;\n";
        javascript += "path.evaluate(\"/abc/xyz.html\");\n";

        var result = (List<Object>) JSUtil.execute(javascript).as(List.class);
        assertThat(result).isInstanceOf(List.class);
        assertThat((List) result).isEmpty();

    }

    @Test
    @DisplayName("Path has variable at start 'x' and path matches")
    void startingWithVariableAtStartAndMatch() throws ScriptException {
        var javascript = Javascript.getScript(CLASSES);
        javascript += "var path = new Path();\n";

        javascript += "var pathElement2 = new PathElement({type: 'static', content:'/abc/xyz.html'});\n";
        javascript += "var pathElement1 = new PathElement({type: 'variable', key: 'x'});\n";
        javascript += "pathElement1.next = pathElement2;\n";

        javascript += "path.pathElement = pathElement1;\n";
        javascript += "path.evaluate(\"123/abc/xyz.html\");\n";

        var result = (List<Object>) JSUtil.execute(javascript).as(List.class);
        assertThat(result).hasSize(1);
        var variables = (Map<String, Object>) result.get(0);
        assertThat(variables.get("x")).isEqualTo("123");
    }

    @Test
    @DisplayName("Path has variable at start 'x' and path does not matches")
    void startingWithVariableAtStartAndDoesNotMatch() throws ScriptException {
        var javascript = Javascript.getScript(CLASSES);
        javascript += "var path = new Path();\n";

        javascript += "var pathElement2 = new PathElement({type: 'static', content:'/abc/xyz.html'});\n";
        javascript += "var pathElement1 = new PathElement({type: 'variable', key: 'x'});\n";
        javascript += "pathElement1.next = pathElement2;\n";

        javascript += "path.pathElement = pathElement1;\n";
        javascript += "path.evaluate(\"123/ab/xyz.html\");\n";

        var result = JSUtil.execute(javascript).asBoolean();

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Path has variable 'x' and path matches")
    void startingWithVariableAndMatch() throws ScriptException {
        var javascript = Javascript.getScript(CLASSES);
        javascript += "var path = new Path();\n";

        javascript += "var pathElement3 = new PathElement({type: 'static', content: '.html'});\n";


        javascript += "var pathElement2 = new PathElement({type: 'variable', key: 'xyz'});\n";
        javascript += "pathElement2.next = pathElement3;\n";

        javascript += "var pathElement1 = new PathElement({type: 'static', content: '/abc/'});\n";
        javascript += "pathElement1.next = pathElement2;\n";

        javascript += "path.pathElement = pathElement1;\n";
        javascript += "path.evaluate(\"/abc/1.html\");\n";

        var result = JSUtil.execute(javascript);
        assertThat(result.as(List.class)).hasSize(1);
        var variables = (Map<String, String>) result.as(List.class).get(0);
        assertThat(variables.get("xyz")).isEqualTo("1");
    }

    @Test
    void twoVariables() {

    }


}
