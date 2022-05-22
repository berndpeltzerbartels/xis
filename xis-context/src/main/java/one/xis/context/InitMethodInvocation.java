package one.xis.context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class InitMethodInvocation {
    private final Set<InitMethodInvoker> invokers = new HashSet<>();

    void onComponentCreated(Object o) {
        Class<?> c = o.getClass();
        while (c != null && !c.equals(Object.class)) {
            findInitMethods(c);
            c = c.getSuperclass();
        }
        invokers.forEach(initMethodInvoker -> initMethodInvoker.onComponentCreated(o));
    }

    void invokeAll() {
        invokers.forEach(initMethodInvoker -> initMethodInvoker.invoke());
    }

    private void findInitMethods(Class<?> c) {
        Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Init.class))
                .map(InitMethodInvoker::new).forEach(invokers::add);
    }
}
