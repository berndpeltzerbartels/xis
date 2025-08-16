package one.xis.test.dom;

import lombok.Getter;
import one.xis.utils.lang.MethodUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GraalVMProxy implements ProxyObject {

    private final Map<String, Method> getters;
    private final Map<String, Method> setters;
    private final Map<String, Method> otherMethods;

    @Getter
    private final Set<String> memberKeys;

    GraalVMProxy() {
        this.getters = MethodUtils.findGettersByFieldName(getClass());
        this.setters = MethodUtils.findSettersByFieldName(getClass());
        this.otherMethods = MethodUtils.allMethods(getClass())
                .stream()
                .filter(MethodUtils.NON_PRIVATE)
                .filter(method -> !getters.containsKey(method.getName()) && !setters.containsKey(method.getName()))
                .collect(Collectors.toMap(Method::getName, Function.identity(), (existing, replacement) -> existing));
        memberKeys = new HashSet<>(getters.keySet());
        memberKeys.addAll(setters.keySet());
        memberKeys.addAll(otherMethods.keySet());
    }

    @Override
    public Object getMember(String key) {
        var getter = getters.get(key);
        if (getter != null) {
            return MethodUtils.doInvoke(this, getter);
        }
        var setter = setters.get(key);
        if (setter != null) {
            return toProxyExecutable(setter);
        }
        var method = otherMethods.get(key);
        if (method != null) {
            return toProxyExecutable(method);
        }
        return null; // or throw an exception if preferred
    }

    @Override
    public boolean hasMember(String key) {
        return memberKeys.contains(key);
    }

    @Override
    public void putMember(String key, Value value) {
        var setter = setters.get(key);
        if (setter != null) {
            Object convertedValue = ProxyUtils.convertValue(setter.getParameterTypes()[0], value);
            MethodUtils.doInvoke(this, setter, convertedValue);
        } else {
            throw new IllegalArgumentException("No setter found for key: " + key);
        }
    }

    private ProxyExecutable toProxyExecutable(Method method) {
        return arguments -> {
            var args = new Object[method.getParameterCount()];
            for (var i = 0; i < args.length; i++) {
                var parameter = method.getParameters()[i];
                args[i] = ProxyUtils.convertValue(parameter.getType(), arguments[i]);
            }
            return MethodUtils.doInvoke(this, method, args);
        };
    }


}
