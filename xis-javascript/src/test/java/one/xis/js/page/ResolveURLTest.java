package one.xis.js.page;

import one.xis.js.Javascript;
import one.xis.test.js.JSUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;

import static one.xis.js.JavascriptSource.CLASSES;
import static one.xis.js.JavascriptSource.FUNCTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class ResolveURLTest {

    @Test
    @DisplayName("ResolvedURL should return original string when toURL()-method is called")
    void toURL() throws ScriptException {
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
                        var resolvedURL = resolver.resolve('/b/xyz.html?x=y');
                        resolvedURL.toURL();
                """;
        var result = JSUtil.execute(script).asString();

        assertThat(result).isEqualTo("/b/xyz.html?x=y");
    }
}
