package one.xis.js.tags;

import one.xis.context.PolyglotPromises;
import one.xis.js.Javascript;
import one.xis.test.dom.DocumentImpl;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class FrontletLinkHandlerBufferTest {

    @Test
    void doesNotBufferWhenOpeningSameFrontletWithDifferentParameters() throws ScriptException {
        var result = runBufferScenario("ProjectFrontlet");

        assertThat(result.toString()).isEqualTo("buffer=0,show=1,commit=0");
    }

    @Test
    void buffersWhenOpeningDifferentFrontlet() throws ScriptException {
        var result = runBufferScenario("OtherFrontlet");

        assertThat(result.toString()).isEqualTo("buffer=1,show=1,commit=1");
    }

    @Test
    void keepsCurrentFrontletVisibleUntilBufferedFrontletIsCommitted() throws ScriptException {
        var script = Javascript.getScript(FUNCTIONS) + Javascript.getScript(CLASSES) + """
                var oldRoot = document.createElement('section');
                oldRoot.setAttribute('id', 'old-frontlet');
                var newRoot = document.createElement('section');
                newRoot.setAttribute('id', 'new-frontlet');
                var container = document.createElement('xis:frontlet-container');
                container.appendChild(oldRoot);

                var handler = new FrontletContainerHandler(container, {}, {}, {});
                handler.frontletInstance = {
                    frontlet: { id: 'OldFrontlet' },
                    root: oldRoot,
                    rootHandler: { parentHandler: handler, publishBindEvent: function() {} },
                    dispose: function() {}
                };
                handler.descendantHandlers = [handler.frontletInstance.rootHandler];
                handler.frontlets = {
                    getFrontletInstance: function(frontletId) {
                        return {
                            frontlet: { id: frontletId },
                            root: newRoot,
                            rootHandler: { publishBindEvent: function() {} },
                            dispose: function() {}
                        };
                    }
                };

                handler.initBuffer()
                    .then(function() {
                        var oldVisibleBeforeCommit = container.firstChild === oldRoot;
                        handler.ensureFrontletBound('NewFrontlet');
                        var oldStillVisibleAfterBind = container.firstChild === oldRoot;
                        var newBuffered = handler.buffer.firstChild === newRoot;
                        return handler.commitBuffer().then(function() {
                            return oldVisibleBeforeCommit + ',' + oldStillVisibleAfterBind + ',' + newBuffered + ',' + (container.firstChild === newRoot);
                        });
                    });
                """;

        var result = PolyglotPromises.await(JSUtil.execute(script, Map.of("document", new DocumentImpl("html"))));

        assertThat(result.toString()).isEqualTo("true,true,true,true");
    }

    private Object runBufferScenario(String currentFrontletId) throws ScriptException {
        var script = Javascript.getScript(FUNCTIONS) + Javascript.getScript(CLASSES) + """
                var calls = { buffer: 0, show: 0, commit: 0 };
                var targetContainer = {};
                var handler = {
                    changesFrontlet: function(frontletId) {
                        return frontletId !== '%s';
                    },
                    initBuffer: function() {
                        calls.buffer++;
                        return Promise.resolve();
                    },
                    showFrontlet: function(frontletId, frontletState) {
                        calls.show++;
                        return Promise.resolve();
                    },
                    commitBuffer: function() {
                        calls.commit++;
                        return Promise.resolve();
                    }
                };
                var tag = {
                    addEventListener: function() {},
                    getAttribute: function(name) {
                        if (name === 'xis:frontlet') {
                            return 'ProjectFrontlet?projectId=2';
                        }
                        if (name === 'xis:target-container') {
                            return 'main';
                        }
                        return null;
                    }
                };
                var containers = {
                    findContainer: function(containerId) {
                        return targetContainer;
                    }
                };
                var app = {
                    tagHandlers: {
                        getHandler: function(container) {
                            return handler;
                        }
                    },
                    pageController: {
                        resolvedURL: {}
                    },
                    client: {
                        config: {
                            getFrontletId: function(frontletUrl) {
                                return frontletUrl.split('?')[0];
                            }
                        }
                    }
                };
                var link = new FrontletLinkHandlerBase(tag, containers);
                link.refresh(new Data({}));
                link.onClick({ preventDefault: function() {} })
                    .then(function() {
                        return 'buffer=' + calls.buffer + ',show=' + calls.show + ',commit=' + calls.commit;
                    });
                """.formatted(currentFrontletId);

        return PolyglotPromises.await(JSUtil.execute(script));
    }
}
