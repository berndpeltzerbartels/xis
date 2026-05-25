package one.xis.js.tags;

import one.xis.context.PolyglotPromises;
import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

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
