package one.xis.test.dom;

import lombok.Getter;
import one.xis.utils.lang.MethodUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class GraalVMProxy implements ProxyObject {

    private final Map<String, Method> getters;
    private final Map<String, Method> setters;
    private final Map<String, Method> nonSettersOrGetters;

    private final Set<String> memberKeys;
    private final Map<String, Object> staticFields;

    private static final ConcurrentHashMap<Class<?>, SubtypeMeta> META_CACHE = new ConcurrentHashMap<>();

    private final SubtypeMeta meta;

    GraalVMProxy() {
        this.meta = META_CACHE.computeIfAbsent(getClass(), SubtypeMeta::new);
        this.getters = this.meta.getters;
        this.setters = this.meta.setters;
        this.nonSettersOrGetters = this.meta.nonSettersOrGetters;
        this.memberKeys = this.meta.memberKeys;
        this.staticFields = this.meta.staticFields;
    }

    private static class SubtypeMeta {
        final Map<String, Method> getters;
        final Map<String, Method> setters;
        final Map<String, Method> nonSettersOrGetters;
        final Set<String> memberKeys;
        final Map<String, Object> staticFields;

        SubtypeMeta(Class<?> clazz) {
            this.getters = MethodUtils.findGettersByFieldName(clazz);
            this.setters = MethodUtils.findSettersByFieldName(clazz);
            this.nonSettersOrGetters = MethodUtils.allMethods(clazz)
                    .stream()
                    .filter(MethodUtils.NON_PRIVATE)
                    .filter(method -> !getters.containsKey(method.getName()) && !setters.containsKey(method.getName()))
                    .collect(Collectors.toMap(Method::getName, Function.identity(), (existing, replacement) -> existing));
            memberKeys = new HashSet<>(getters.keySet());
            memberKeys.addAll(setters.keySet());
            memberKeys.addAll(nonSettersOrGetters.keySet());

            // Find all public static final fields (constants)
            staticFields = new HashMap<>();
            Class<?> c = clazz;
            while (c != null) {
                for (var field : c.getFields()) {
                    int mod = field.getModifiers();
                    if (Modifier.isStatic(mod) && Modifier.isFinal(mod) && Modifier.isPublic(mod)) {
                        try {
                            staticFields.put(field.getName(), field.get(null));
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                }
                c = c.getSuperclass();
            }
            memberKeys.addAll(staticFields.keySet());
        }
    }

    public Object getMemberKeys() {
        return new ArrayList<>(memberKeys).toArray(new String[memberKeys.size()]);
    }


    @Override
    public Object getMember(String key) {
        if (key.equals("toString")) {
            return this.toString();
        }
        if (staticFields.containsKey(key)) {
            return staticFields.get(key);
        }
        var getter = getters.get(key);
        if (getter != null) {
            return MethodUtils.doInvoke(this, getter);
        }
        var setter = setters.get(key);
        if (setter != null) {
            return toProxyExecutable(setter);
        }
        var method = nonSettersOrGetters.get(key);
        if (method != null) {
            return toProxyExecutable(method);
        }
        throw new NoSuchElementException("No member found for key: " + key);
    }

    @Override
    public boolean hasMember(String key) {
        return memberKeys.contains(key);
    }

    @Override
    public void putMember(String key, Value value) {
        //   System.out.println("putMember called with key: " + key + ", value: " + value);
        var setter = setters.get(key);
        if (setter != null) {
            Object convertedValue = GraalVMUtils.convertValue(setter.getParameterTypes()[0], value);
            MethodUtils.doInvoke(this, setter, convertedValue);
        } else {
            throw new IllegalArgumentException("No setter found for key: " + key);
        }
    }

    private ProxyExecutable toProxyExecutable(Method method) {
        return arguments -> {
            try {
                //   System.out.println("invoke " + method.getName());
                var args = new Object[method.getParameterCount()];
                for (var i = 0; i < args.length; i++) {
                    var parameter = method.getParameters()[i];
                    args[i] = GraalVMUtils.convertValue(parameter.getType(), arguments[i]);
                }
                // System.out.println("args: " + Arrays.toString(args));
                var result = MethodUtils.doInvoke(this, method, args);
                if (result instanceof String str) {
                    return org.graalvm.polyglot.Value.asValue(str).asString();
                }
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Invocation failed for method: " + method.getName() + ", base=" + toString(), e);
            }
        };
    }


}
