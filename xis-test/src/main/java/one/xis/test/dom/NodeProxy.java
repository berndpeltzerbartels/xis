package one.xis.test.dom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class NodeProxy implements ProxyObject {

    private final NodeImpl node;

    private static final List<String> MEMBER_KEYS = new ArrayList<>();


    static {
        MEMBER_KEYS.addAll(NodeAttribute.getAttributeNames());
        MEMBER_KEYS.addAll(NodeMethod.getMethodNames());
    }


    @Override
    public Object getMember(String key) {
        Optional<NodeAttribute> attribute = NodeAttribute.of(key);
        if (attribute.isPresent()) {
            return attribute.get().getFieldValue(node);
        }

        Optional<NodeMethod> method = NodeMethod.of(key);
        if (method.isPresent()) {
            return method.get().asProxyExecutable(node);
        }

        throw new IllegalArgumentException("No such member: " + key);
    }

    @Override
    public Object getMemberKeys() {
        return MEMBER_KEYS;
    }

    @Override
    public boolean hasMember(String key) {
        return MEMBER_KEYS.contains(key);
    }

    @Override
    public void putMember(String key, Value value) {
        var nodeAttribute = NodeAttribute.of(key).orElseThrow(() -> new IllegalArgumentException("No such attribute: " + key));
        if (!nodeAttribute.writable) {
            throw new UnsupportedOperationException("Attribute '" + key + "' is read-only.");
        }
        nodeAttribute.setValue(node, value);
    }

    @Getter
    @RequiredArgsConstructor
    enum NodeAttribute implements ProxyAttributes {
        NODE_TYPE("nodeType", false, false),
        PARENT_NODE("parentNode", true, true),
        CHILD_NODES("childNodes", false, false),
        FIRST_CHILD("firstChild", true, false),
        NEXT_SIBLING("nextSibling", true, false),
        TEXT_CONTENT("textContent", false, false);

        private final String name;
        private final Field field;
        private final boolean writable;
        private final boolean useField;
        private final Method getter;
        private final Method setter;

        NodeAttribute(String name, boolean writable, boolean useField) {
            this.name = name;
            this.writable = writable;
            this.useField = useField;
            if (useField) {
                this.field = FieldUtil.getField(NodeImpl.class, name);
                this.getter = null;
                this.setter = null;
            } else {
                this.field = null;
                this.getter = MethodUtils.findGetter(NodeImpl.class, name)
                        .orElseThrow(() -> new IllegalArgumentException("No getter found for attribute: " + name));
                this.setter = isWritable() ? MethodUtils.findSetter(NodeImpl.class, name).orElseThrow(() -> new IllegalStateException("no setter for " + name)) : null;
            }
        }

        static Optional<NodeAttribute> of(String name) {
            for (NodeAttribute attribute : values()) {
                if (attribute.name.equals(name)) {
                    return Optional.of(attribute);
                }
            }
            return Optional.empty();
        }

        static Set<String> getAttributeNames() {
            return Arrays.stream(values())
                    .map(attr -> attr.name)
                    .collect(Collectors.toSet());
        }

    }

    @Getter
    @RequiredArgsConstructor
    enum NodeMethod implements ProxyMethods {
        INSERT_PREVIOUS_SIBLING("insertPreviousSibling", Node.class),
        REMOVE("remove"),
        SET_NEXT_SIBLING("setNextSibling", Node.class);

        private final String name;
        private final Method method;

        NodeMethod(String name, Class<?>... parameterTypes) {
            this.name = name;
            this.method = MethodUtils.findMethod(NodeImpl.class, name, parameterTypes);
        }


        static Optional<NodeMethod> of(String name) {
            for (NodeMethod method : values()) {
                if (method.name.equals(name)) {
                    return Optional.of(method);
                }
            }
            return Optional.empty();
        }

        static Set<String> getMethodNames() {
            return Arrays.stream(values())
                    .map(method -> method.name)
                    .collect(Collectors.toSet());
        }

        @Override
        public ProxyMethods get(String name) {
            return NodeMethod.of(name).orElseThrow();
        }
    }
}