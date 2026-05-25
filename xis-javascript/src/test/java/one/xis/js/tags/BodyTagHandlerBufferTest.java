package one.xis.js.tags;

import one.xis.context.PolyglotPromises;
import one.xis.js.Javascript;
import one.xis.test.dom.Document;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class BodyTagHandlerBufferTest {

    @Test
    void keepsCurrentPageVisibleUntilBufferedPageIsCommitted() throws ScriptException {
        var document = Document.of("""
                <html>
                    <body>
                        <script id="bundle" ignore="true"></script>
                    </body>
                </html>
                """);
        var script = Javascript.getScript(FUNCTIONS) + Javascript.getScript(CLASSES) + """
                var tagHandlers = {
                    getRootHandler: function() {
                        return {
                            publishBindEvent: function() {},
                            refresh: function() { return Promise.resolve(); }
                        };
                    }
                };
                var currentTemplate = document.createDocumentFragment();
                var oldContent = document.createElement('main');
                oldContent.setAttribute('id', 'old-page');
                currentTemplate.appendChild(oldContent);

                var nextTemplate = document.createDocumentFragment();
                var newContent = document.createElement('main');
                newContent.setAttribute('id', 'new-page');
                nextTemplate.appendChild(newContent);

                var handler = new BodyTagHandler(tagHandlers);
                handler.bind(currentTemplate);

                handler.initBuffer()
                    .then(function() {
                        handler.release(currentTemplate);
                        handler.bind(nextTemplate);
                        var oldVisibleBeforeCommit = document.getElementById('old-page') === oldContent;
                        var newHiddenBeforeCommit = document.getElementById('new-page') === null;
                        var persistentVisibleBeforeCommit = document.getElementById('bundle') !== null;
                        return handler.commitBuffer().then(function() {
                            var oldReturnedToTemplate = currentTemplate.firstChild === oldContent;
                            var newVisibleAfterCommit = document.getElementById('new-page') === newContent;
                            var oldHiddenAfterCommit = document.getElementById('old-page') === null;
                            var persistentVisibleAfterCommit = document.getElementById('bundle') !== null;
                            return oldVisibleBeforeCommit + ',' + newHiddenBeforeCommit + ',' + persistentVisibleBeforeCommit + ','
                                + oldReturnedToTemplate + ',' + newVisibleAfterCommit + ',' + oldHiddenAfterCommit + ',' + persistentVisibleAfterCommit;
                        });
                    });
                """;

        var result = PolyglotPromises.await(JSUtil.execute(script, Map.of(
                "document", document,
                "Node", Map.of("ELEMENT_NODE", 1)
        )));

        assertThat(result.toString()).isEqualTo("true,true,true,true,true,true,true");
    }
}
