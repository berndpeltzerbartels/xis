package one.xis.test.dom;

import lombok.Getter;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import org.graalvm.polyglot.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ElementProxy extends NodeProxy {

    private final List<String> memberKeys = new ArrayList<>();

    private final ElementImpl element;

    @SuppressWarnings("unchecked")
    public ElementProxy(ElementImpl element) {
        super(element);
        this.element = element;
        memberKeys.addAll(ElementAttribute.getNames());
        memberKeys.addAll(ElementMethod.getNames());
        memberKeys.addAll((Collection<String>) super.getMemberKeys());
    }

    @Override
    public Object getMember(String key) {
        Optional<ElementAttribute> attribute = ElementAttribute.of(key);
        if (attribute.isPresent()) {
            return attribute.get().getValue(element);
        }
        Optional<ElementMethod> method = ElementMethod.of(key);
        if (method.isPresent()) {
            return method.get().asProxyExecutable(element);
        }
        return super.getMember(key);
    }

    @Override
    public void putMember(String key, Value value) {
        Optional<ElementAttribute> attribute = ElementAttribute.of(key);
        if (attribute.isPresent()) {
            attribute.get().setValue(element, value);
            return;
        }
        super.putMember(key, value);
    }

    @Override
    public Object getMemberKeys() {
        return memberKeys;
    }

    @Override
    public boolean hasMember(String key) {
        return memberKeys.contains(key) || super.hasMember(key);
    }

    @Getter
    enum ElementAttribute implements ProxyAttributes {
        CLASS_LIST("classList", false, true),
        LOCAL_NAME("localName", false, false),
        TAG_NAME("tagName", false, false),
        ID("id", true, true),
        INNER_TEXT("innerText", true, false),
        INNER_HTML("innerHTML", true, false),
        TEXT_CONTENT("textContent", true, false);

        private final String name;
        private final boolean writable;
        private final boolean useField;
        private final Field field;
        private final Method getter;
        private final Method setter;

        ElementAttribute(String name, boolean writable, boolean useField) {
            this.name = name;
            this.writable = writable;
            this.useField = useField;
            if (useField) {
                this.field = FieldUtil.getField(ElementImpl.class, name);
                this.getter = null;
                this.setter = null;
            } else {
                this.field = null;
                this.getter = MethodUtils.findGetter(ElementImpl.class, name).orElseThrow();
                this.setter = isWritable() ? MethodUtils.findSetter(ElementImpl.class, name).orElseThrow() : null;
            }
        }

        static Optional<ElementAttribute> of(String name) {
            return Arrays.stream(values()).filter(a -> a.name.equals(name)).findFirst();
        }

        static Set<String> getNames() {
            return Arrays.stream(values()).map(a -> a.name).collect(Collectors.toSet());
        }
    }

    @Getter
    enum ElementMethod implements ProxyMethods {
        GET_ATTRIBUTE("getAttribute", String.class),
        SET_ATTRIBUTE("setAttribute", String.class, String.class),
        HAS_ATTRIBUTE("hasAttribute", String.class),
        REMOVE_ATTRIBUTE("removeAttribute", String.class);

        private final String name;
        private final Method method;

        ElementMethod(String name, Class<?>... parameterTypes) {
            this.name = name;
            this.method = MethodUtils.findMethod(ElementImpl.class, name, parameterTypes);
        }

        static Optional<ElementMethod> of(String name) {
            return Arrays.stream(values()).filter(m -> m.name.equals(name)).findFirst();
        }

        static Set<String> getNames() {
            return Arrays.stream(values()).map(m -> m.name).collect(Collectors.toSet());
        }

        @Override
        public ProxyMethods get(String name) {
            return of(name).orElseThrow(() -> new IllegalArgumentException("No such method: " + name));
        }
    }
}