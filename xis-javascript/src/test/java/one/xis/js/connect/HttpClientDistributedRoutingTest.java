package one.xis.js.connect;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static org.assertj.core.api.Assertions.assertThat;

class HttpClientDistributedRoutingTest {

    @Test
    void keepsRelativeUriWhenNoPageHostIsConfigured() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                function timeZone() { return 'Europe/Berlin'; }
                var config = new ClientConfig();
                var client = new HttpClient({ post: function(){}, get: function(){} }, 'test-client');
                client.config = config;
                client.resolvePageUri('/xis/page/model', '/shop/*.html');
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("/xis/page/model");
    }

    @Test
    void prefixesConfiguredPageHost() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                function timeZone() { return 'Europe/Berlin'; }
                var config = new ClientConfig();
                config.pageAttributes['/shop/*.html'] = { host: 'https://shop.example.com' };
                var client = new HttpClient({ post: function(){}, get: function(){} }, 'test-client');
                client.config = config;
                client.resolvePageUri('/xis/page/model', '/shop/*.html');
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("https://shop.example.com/xis/page/model");
    }

    @Test
    void prefixesConfiguredWidgetHostForWidgetRequests() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                function timeZone() { return 'Europe/Berlin'; }
                var config = new ClientConfig();
                config.frontletAttributes['scoreboard'] = { host: 'https://widgets.example.com/' };
                var client = new HttpClient({ post: function(){}, get: function(){} }, 'test-client');
                client.config = config;
                client.resolveFrontletUri('/xis/widget/model', 'scoreboard');
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("https://widgets.example.com/xis/widget/model");
    }
}
