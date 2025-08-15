package one.xis.test.dom;


import one.xis.utils.lang.FieldUtil;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TextNodeProxy extends NodeProxy {
    
    private final List<String> memberKeys = new ArrayList<>();
    private final TextNodeIml node;

    @SuppressWarnings("unchecked")
    public TextNodeProxy(TextNodeIml node) {
        super(node);
        this.node = node;
        memberKeys.add("nodeValue");
        memberKeys.addAll((Collection<String>) super.getMemberKeys());
    }

    @Override
    public Object getMember(String key) {
        if (key.equals("nodeValue")) {
            return node.getNodeValue();
        }
        return super.getMember(key);
    }

    @Override
    public void putMember(String key, Value value) {
        if (key.equals("nodeValue")) {
            FieldUtil.setFieldValue(node, "nodeValue", ProxyUtils.convertValue(String.class, value));
        }
        super.putMember(key, value);
    }

    @Override
    public Object getMemberKeys() {
        return memberKeys;
    }

    @Override
    public boolean hasMember(String key) {
        return memberKeys.contains(key);
    }
}
