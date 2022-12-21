package one.xis.context;

import lombok.RequiredArgsConstructor;
import one.xis.utils.reflect.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
class InitMethodInvocation {
    private final Set<Class<? extends Annotation>> annotations;
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
        invokers.forEach(InitMethodInvoker::invoke);
    }

    private void findInitMethods(Class<?> c) {
        Arrays.stream(c.getDeclaredMethods())
                .filter(m -> AnnotationUtils.hasAtLeasOneAnnotation(m, annotations))
                .map(InitMethodInvoker::new).forEach(invokers::add);
    }
}
