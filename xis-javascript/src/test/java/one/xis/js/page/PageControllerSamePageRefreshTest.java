package one.xis.js.page;

import one.xis.context.PolyglotPromises;
import one.xis.js.Javascript;
import one.xis.test.dom.Document;
import one.xis.test.dom.Location;
import one.xis.test.dom.Window;
import one.xis.test.js.Console;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.EVENT_REGISTRY;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class PageControllerSamePageRefreshTest {

    @Test
    void samePageNavigationRefreshesDataWithoutRebindingPage() throws ScriptException {
        var script = Javascript.getScript(EVENT_REGISTRY, FUNCTIONS, CLASSES) + """
                var calls = [];
                var page = {data: null, updateEventKeys: []};
                var resolved = {
                    page: page,
                    url: '/theme.html',
                    normalizedPath: '/theme.html',
                    urlParameters: {},
                    pathVariablesAsMap: function() { return {}; }
                };
                var app = {
                    renderQueue: Promise.resolve(),
                    frontletContainers: {
                        deactivateAll: function() { calls.push('deactivateAll'); }
                    },
                    eventPublisher: {
                        publish: function(eventType) { calls.push('event:' + eventType); }
                    },
                    history: {
                        appendPage: function(url) { calls.push('history:' + url); }
                    }
                };
                var client = {
                    loadPageData: function(loadedResolved) {
                        calls.push('load:' + loadedResolved.url);
                        return Promise.resolve({
                            status: 200,
                            data: new Data({}),
                            updateEventKeys: []
                        });
                    }
                };
                var controller = new PageController(client, {}, {}, {}, {});
                controller.page = page;
                controller.resolvedURL = resolved;
                controller.htmlTagHandler = {
                    refresh: function(data) { calls.push('refresh'); return Promise.resolve(); },
                    unbindPage: function() { calls.push('unbind'); },
                    bindPage: function() { calls.push('bind'); },
                    getTitle: function() { return 'Title'; }
                };
                controller.displayPageForResolvedURL(resolved)
                    .then(function() { return calls.join(','); });
                """;

        var result = PolyglotPromises.await(JSUtil.execute(script, java.util.Map.of(
                "console", new Console(),
                "document", Document.of("<html><head><title></title></head><body></body></html>"),
                "window", new Window(new Location())
        )));

        assertThat(result.toString())
                .isEqualTo("load:/theme.html,refresh,event:buffer_commited,history:/theme.html,event:page_loaded");
    }
}
