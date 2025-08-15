package one.xis.test.dom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class DocumentProxy implements ProxyObject {


    private final DocumentImpl document;
    private static final Set<String> MEMBER_KEYS = new HashSet<>(DocumentAttribute.getNames());

    static {
        MEMBER_KEYS.addAll(DocumentMethod.getNames());
    }

    @Override
    public Object getMember(String key) {
        var attribute = DocumentAttribute.of(key);
        if (attribute.isPresent()) {
            return attribute.get().getValue(document);
        }
        var method = DocumentMethod.of(key);
        return method.<Object>map(documentMethod -> documentMethod.asProxyExecutable(document)).orElseThrow(() -> new IllegalArgumentException("No such member: " + key));
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
        var attribute = DocumentAttribute.of(key);
        if (attribute.isPresent()) {
            if (!attribute.get().isWritable()) {
                throw new UnsupportedOperationException("Attribute '" + key + "' is read-only.");
            }
            attribute.get().setValue(document, value);
            return;
        }
        throw new IllegalArgumentException("No such attribute: " + key);
    }


    @Getter
    @RequiredArgsConstructor
    enum DocumentAttribute implements ProxyAttributes {

        BODY("body", true, false),
        DOCUMENT_ELEMENT("documentElement", false, false),
        HEAD("head", false, false),
        TITLE("title", true, false),
        COOKIES("cookies", true, true),
        LOCATION("location", false, true);

        private final String name;
        private final boolean writable;
        private final boolean useField;
        private final Field field;
        private final Method setter;
        private final Method getter;


        DocumentAttribute(String name, boolean writable, boolean useField) {
            this.name = name;
            this.writable = writable;
            this.useField = useField;
            if (useField) {
                this.field = FieldUtil.getField(DocumentImpl.class, name);
                this.getter = null;
                this.setter = null;
            } else {
                this.field = null;
                this.getter = MethodUtils.findGetter(DocumentImpl.class, name)
                        .orElseThrow(() -> new NoSuchMethodError("Getter for " + name));
                this.setter = MethodUtils.findSetter(DocumentImpl.class, name).orElse(null);
            }
        }

        static Optional<DocumentAttribute> of(String name) {
            return Arrays.stream(values()).filter(a -> a.name.equals(name)).findFirst();
        }

        static Set<String> getNames() {
            return Arrays.stream(values()).map(a -> a.name).collect(Collectors.toSet());
        }
    }

    @Getter
    @RequiredArgsConstructor
    enum DocumentMethod implements ProxyMethods {
        CREATE_ELEMENT("createElement", String.class),
        CREATE_TEXT_NODE("createTextNode", String.class),
        GET_ELEMENT_BY_ID("getElementById", String.class),
        QUERY_SELECTOR("querySelector", String.class),
        QUERY_SELECTOR_ALL("querySelectorAll", String.class),
        GET_ELEMENTS_BY_TAG_NAME("getElementsByTagName", String.class);
        private final String name;
        private final Method method;

        DocumentMethod(String name, Class<?>... parameterTypes) {
            this.name = name;
            this.method = MethodUtils.findMethod(DocumentImpl.class, name, parameterTypes);
        }


        static Optional<DocumentMethod> of(String name) {
            return Arrays.stream(values()).filter(m -> m.name.equals(name)).findFirst();
        }

        static Set<String> getNames() {
            return Arrays.stream(values()).map(m -> m.name).collect(Collectors.toSet());
        }

        @Override
        public DocumentMethod get(String name) {
            return of(name).orElseThrow(() -> new IllegalArgumentException("No such method: " + name));
        }
    }

}
