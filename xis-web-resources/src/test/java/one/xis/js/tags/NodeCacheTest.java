package one.xis.js.tags;

import one.xis.test.dom.Document;
import one.xis.test.dom.Element;
import one.xis.test.dom.Node;
import one.xis.test.js.JSUtil;
import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
class NodeCacheTest {

    @Test
    void sizeUp() throws ScriptException {
        var js = IOUtils.getResourceAsString("js/tags/NodeCache.js");
        js += IOUtils.getResourceAsString("js/init/Initializer.js");
        js += IOUtils.getResourceAsString("js/init/DomAccessor.js");
        js += IOUtils.getResourceAsString("js/Functions.js");
        js += "var cache = new NodeCache(nodeArray, new Initializer(new DomAccessor())); cache.sizeUp(3); [cache.getChildren(0), cache.getChildren(1), cache.getChildren(2)]";
        Map<String, Object> bindings = Map.of("nodeArray", new Node[]{new Element("a"), new Element("b")}, "document", Document.of("<html/>"));

        var result = (List<?>) JSUtil.compile(js, bindings).eval();

        assertThat(result.size()).isEqualTo(3);

        var list0 = (Node[]) result.get(0);
        var list1 = (List<Element>) result.get(1);
        var list2 = (List<Element>) result.get(2);

        assertThat(Arrays.stream(list0).map(Node::getName)).containsExactly("a", "b");
        assertThat(list1.stream().map(Node::getName)).containsExactly("a", "b");
        assertThat(list2.stream().map(Node::getName)).containsExactly("a", "b");


    }

}
