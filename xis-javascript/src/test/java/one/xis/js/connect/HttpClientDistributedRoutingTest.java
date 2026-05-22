package one.xis.js.connect;

import one.xis.js.Javascript;
import one.xis.context.PolyglotPromises;
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
    void prefixesConfiguredFrontletHostForFrontletRequests() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                function timeZone() { return 'Europe/Berlin'; }
                var config = new ClientConfig();
                config.frontletAttributes['scoreboard'] = { host: 'https://frontlets.example.com/' };
                var client = new HttpClient({ post: function(){}, get: function(){} }, 'test-client');
                client.config = config;
                client.resolveFrontletUri('/xis/frontlet/model', 'scoreboard');
                """;

        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("https://frontlets.example.com/xis/frontlet/model");
    }

    @Test
    void keepsLocalConfigWhenDistributedHostsEndpointIsMissing() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                function timeZone() { return 'Europe/Berlin'; }
                var requests = [];
                var connector = {
                    get: function(uri) {
                        requests.push(uri);
                        if (uri === '/xis/config') {
                            return Promise.resolve({
                                status: 200,
                                responseText: JSON.stringify({
                                    pageIds: ['/local.html'],
                                    frontletIds: [],
                                    includeIds: [],
                                    pageAttributes: {
                                        '/local.html': {
                                            path: { pathElement: { type: 'static', content: '/local.html' } },
                                            normalizedPath: '/local.html'
                                        }
                                    }
                                })
                            });
                        }
                        if (uri === '/xis/distributed/hosts') {
                            return Promise.resolve({ status: 404, responseText: '' });
                        }
                        throw new Error('unexpected uri ' + uri);
                    }
                };
                var client = new HttpClient(connector, 'test-client');
                client.loadConfig().then(config => requests.join(',') + '|' + Object.keys(config.pageAttributes).join(','));
                """;

        var result = PolyglotPromises.await(JSUtil.execute(script));

        assertThat(result.toString()).isEqualTo("/xis/config,/xis/distributed/hosts|/local.html");
    }

    @Test
    void loadsAndMergesRemoteConfigsFromDistributedHosts() throws ScriptException {
        var script = Javascript.getScript(CLASSES) + """
                function timeZone() { return 'Europe/Berlin'; }
                var requests = [];
                var connector = {
                    get: function(uri) {
                        requests.push(uri);
                        if (uri === '/xis/config') {
                            return Promise.resolve({
                                status: 200,
                                responseText: JSON.stringify({
                                    pageIds: ['/local.html'],
                                    frontletIds: [],
                                    includeIds: [],
                                    pageAttributes: {
                                        '/local.html': {
                                            path: { pathElement: { type: 'static', content: '/local.html' } },
                                            normalizedPath: '/local.html'
                                        }
                                    }
                                })
                            });
                        }
                        if (uri === '/xis/distributed/hosts') {
                            return Promise.resolve({
                                status: 200,
                                responseText: JSON.stringify(['https://remote.example.com'])
                            });
                        }
                        if (uri === 'https://remote.example.com/xis/config') {
                            return Promise.resolve({
                                status: 200,
                                responseText: JSON.stringify({
                                    pageIds: ['/remote.html'],
                                    frontletIds: ['RemoteFrontlet'],
                                    includeIds: [],
                                    pageAttributes: {
                                        '/remote.html': {
                                            path: { pathElement: { type: 'static', content: '/remote.html' } },
                                            normalizedPath: '/remote.html'
                                        }
                                    },
                                    frontletAttributes: {
                                        RemoteFrontlet: { id: 'RemoteFrontlet', url: '/remote-frontlet' }
                                    }
                                })
                            });
                        }
                        throw new Error('unexpected uri ' + uri);
                    }
                };
                var client = new HttpClient(connector, 'test-client');
                client.loadConfig().then(config => {
                    var pageHost = config.pageAttributes['/remote.html'].host;
                    var frontletHost = config.frontletAttributes.RemoteFrontlet.host;
                    return requests.join(',') + '|' + pageHost + '|' + frontletHost;
                });
                """;

        var result = PolyglotPromises.await(JSUtil.execute(script));

        assertThat(result.toString()).isEqualTo("/xis/config,/xis/distributed/hosts,https://remote.example.com/xis/config"
                + "|https://remote.example.com|https://remote.example.com");
    }
}
