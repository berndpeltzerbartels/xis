package one.xis.js.tags;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class TagHandlerTest {

    @Test
    void startsAllDescendantRefreshesBeforeWaitingForPromises() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                var root = new TagHandler({});
                var started = [];
                root.descendantHandlers = [
                    {
                        refresh(data) {
                            started.push('first');
                            return new Promise(() => {});
                        }
                    },
                    {
                        refresh(data) {
                            started.push('second');
                            return new Promise(() => {});
                        }
                    }
                ];
                root.refreshDescendantHandlers(new Data({}));
                started.join(',');
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("first,second");
    }
}
