package one.xis.js.page;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.List;
import java.util.Map;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("unchecked")
class URLResolverTest {


    @Test
    void urlParameters() throws ScriptException {
        var result = (Map<String, String>) JSUtil.execute(Javascript.getScript(FUNCTIONS, CLASSES) + "\nnew URLResolver().urlParameters('xyz.html?a=v1&b=v2&c=v3')");

        assertThat(result.get("a")).isEqualTo("v1");
        assertThat(result.get("b")).isEqualTo("v2");
        assertThat(result.get("c")).isEqualTo("v3");
    }


    @Test
    void resolve() throws ScriptException {
        var script = Javascript.getScript(FUNCTIONS, CLASSES);
        script += """
                       // "/a/{x}.html"
                       var pathElement1 = new PathElement({
                           type: 'static',
                           content: '/a/',
                           next: new PathElement({
                               type: 'variable',
                               key: 'x',
                               next: new PathElement({
                                   type: 'static',
                                   content: '.html'
                               })
                           })
                       });
                       
                       // "/b/{x}.html"
                       var pathElement2 = new PathElement({
                           type: 'static',
                           content: '/b/',
                           next: new PathElement({
                               type: 'variable',
                               key: 'x',
                               next: new PathElement({
                                   type: 'static',
                                   content: '.html'
                               })
                           })
                       });
                       
                       var path1 = new Path(pathElement1);
                       var path2 = new Path(pathElement2);
                       
                       var pages = {
                           getAllPaths: () => [path1, path2],
                           getPage: (normalizedUrl) => {}
                       };
                        
                        var resolver = new URLResolver(pages);
                        resolver.resolve('/b/xyz.html');
                """;
        var result = (Map<String, List<Map<String, String>>>) JSUtil.execute(script);
        assertThat(result.get("pathVariables")).hasSize(1);
        var variable = result.get("pathVariables").get(0);
        assertThat(variable.get("x")).isEqualTo("xyz");
    }
}
