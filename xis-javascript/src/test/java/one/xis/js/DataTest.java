package one.xis.js;

import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class DataTest {

    @Test
    void getValue() throws ScriptException {
        var dataContent = Map.of("A", Map.of("B", Map.of("C", 123)));
        var js = Javascript.getScript(CLASSES);
        js += "var data = new Data(dataContent);";
        js += "data.getValue(['A','B','C'])";

        var result = JSUtil.execute(js, Map.of("dataContent", dataContent));

        assertThat(result.asInt()).isEqualTo(123);
    }

    @Test
    @DisplayName("Original data is not overridden by parent data")
    void getValueWithParent1() throws ScriptException {
        var parentContent = Map.of("A", Map.of("B", Map.of("C", 123)));
        var content = Map.of("A", Map.of("B", Map.of("C", 456)));
        var js = Javascript.getScript(CLASSES);
        js += "var parentData = new Data(parentContent);";
        js += "var data = new Data(content, parentData);";
        js += "data.getValue(['A','B','C'])";

        var result = JSUtil.execute(js, Map.of("parentContent", parentContent, "content", content));

        assertThat(result.asInt()).isEqualTo(456);
    }


    @Test
    @DisplayName("Read data from parent if current does not contain the value")
    void getValueWithParent2() throws ScriptException {
        var parentContent = Map.of("A", Map.of("B", Map.of("C", 123)));
        var content = Map.of("A", Map.of("B", Map.of("D", 456)));
        var js = Javascript.getScript(CLASSES);
        js += "var parentData = new Data(parentContent);";
        js += "var data = new Data(content, parentData);";
        js += "data.getValue(['A','B','C'])";


        var result = JSUtil.execute(js, Map.of("parentContent", parentContent, "content", content));

        assertThat(result.asInt()).isEqualTo(123);
    }

    @Test
    @DisplayName("Set a data value with path containig multiple elements in existing tree")
    void setValue() throws ScriptException {
        var js = Javascript.getScript(CLASSES);
        js += "var data = new Data({a:{b:{}}});";
        js += "data.setValue(['a','b'],'c');";
        js += "data.values['a']['b'];";

        var result = JSUtil.execute(js);

        assertThat(result.asString()).isEqualTo("c");
    }

    @Test
    @DisplayName("Set a data value with path containig multiple elements and tree has to be created")
    void setValue2() throws ScriptException {
        var js = Javascript.getScript(CLASSES);
        js += "var data = new Data({});";
        js += "data.setValue(['a','b'],'c');";
        js += "data.values['a']['b'];";

        var result = JSUtil.execute(js);

        assertThat(result.asString()).isEqualTo("c");
    }
}
