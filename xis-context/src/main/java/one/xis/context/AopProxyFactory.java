package one.xis.context;

import one.xis.utils.lang.MethodUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class AopProxyFactory {
    private final AppContext appContext;
    private final Map<Class<? extends Advice>, Advice> adviceInstances = new HashMap<>();

    AopProxyFactory(AppContext appContext) {
        this.appContext = appContext;
    }

    Object createProxyIfNeeded(Object target, Class<?> beanClass) {
        Set<Class<?>> interfaces = proxyInterfaces(beanClass);
        validateAdvicePlacement(beanClass, interfaces);
        if (!hasAdvice(beanClass, interfaces)) {
            return target;
        }
        var proxyInterfaces = new ArrayList<>(interfaces);
        proxyInterfaces.add(XisProxy.class);
        return Proxy.newProxyInstance(
                beanClass.getClassLoader(),
                proxyInterfaces.toArray(Class[]::new),
                new AopInvocationHandler(appContext, target, beanClass, this)
        );
    }

    private Set<Class<?>> proxyInterfaces(Class<?> beanClass) {
        var interfaces = new LinkedHashSet<Class<?>>();
        Class<?> current = beanClass;
        while (current != null && !current.equals(Object.class)) {
            for (Class<?> interf : current.getInterfaces()) {
                collectProxyInterface(interf, interfaces);
            }
            current = current.getSuperclass();
        }
        return interfaces;
    }

    private void collectProxyInterface(Class<?> interf, Set<Class<?>> interfaces) {
        if (isFrameworkMarkerInterface(interf)) {
            return;
        }
        interfaces.add(interf);
        for (Class<?> parent : interf.getInterfaces()) {
            collectProxyInterface(parent, interfaces);
        }
    }

    private boolean isFrameworkMarkerInterface(Class<?> interf) {
        String packageName = Optional.ofNullable(interf.getPackage()).map(Package::getName).orElse("");
        return packageName.startsWith("java.")
                || packageName.startsWith("javax.")
                || packageName.startsWith("jakarta.")
                || packageName.startsWith("groovy.")
                || packageName.startsWith("kotlin.")
                || interf.equals(AutoCloseable.class);
    }

    private boolean hasAdvice(Class<?> beanClass, Set<Class<?>> interfaces) {
        if (!adviceAnnotations(beanClass.getAnnotations()).isEmpty()) {
            return true;
        }
        for (Method method : MethodUtils.allMethods(beanClass)) {
            if (!adviceAnnotations(method.getAnnotations()).isEmpty()) {
                return true;
            }
        }
        for (Class<?> interf : interfaces) {
            if (!adviceAnnotations(interf.getAnnotations()).isEmpty()) {
                return true;
            }
            for (Method method : interf.getMethods()) {
                if (!adviceAnnotations(method.getAnnotations()).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateAdvicePlacement(Class<?> beanClass, Set<Class<?>> interfaces) {
        if (!hasAdvice(beanClass, interfaces)) {
            return;
        }
        if (interfaces.isEmpty()) {
            throw new AppContextException("Advice on " + beanClass.getName()
                    + " needs at least one non-framework interface because XIS uses interface proxies");
        }
        for (Method method : MethodUtils.allMethods(beanClass)) {
            if (!adviceAnnotations(method.getAnnotations()).isEmpty() && !isProxyInterfaceMethod(method, interfaces)) {
                throw new AppContextException("Advice method " + method
                        + " is not declared by any non-framework interface and can not be intercepted");
            }
        }
    }

    private boolean isProxyInterfaceMethod(Method method, Set<Class<?>> interfaces) {
        if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
            return false;
        }
        for (Class<?> interf : interfaces) {
            if (hasMethod(interf, method)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMethod(Class<?> interf, Method method) {
        try {
            interf.getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private List<Annotation> adviceAnnotations(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(UseAdvice.class))
                .toList();
    }

    private Advice advice(Annotation annotation) {
        Class<? extends Advice> adviceClass = annotation.annotationType().getAnnotation(UseAdvice.class).value();
        return adviceInstances.computeIfAbsent(adviceClass, this::newAdviceInstance);
    }

    private Advice newAdviceInstance(Class<? extends Advice> adviceClass) {
        try {
            var constructor = adviceClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new AppContextException("Can not create advice " + adviceClass.getName(), e);
        }
    }

    private record AdviceBinding(Annotation annotation, Advice advice) {
    }

    private static final class AopInvocationHandler implements InvocationHandler {
        private final AppContext appContext;
        private final Object target;
        private final Class<?> beanClass;
        private final AopProxyFactory proxyFactory;

        private AopInvocationHandler(AppContext appContext, Object target, Class<?> beanClass, AopProxyFactory proxyFactory) {
            this.appContext = appContext;
            this.target = target;
            this.beanClass = beanClass;
            this.proxyFactory = proxyFactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().equals(XisProxy.class)) {
                return target;
            }
            if (method.getDeclaringClass().equals(Object.class)) {
                return invokeObjectMethod(proxy, method, args);
            }
            Method targetMethod = targetMethod(method);
            List<AdviceBinding> bindings = adviceBindings(method, targetMethod);
            if (bindings.isEmpty()) {
                return invokeTarget(targetMethod, args);
            }
            return new DefaultAdviceInvocation(appContext, target, targetMethod, args, bindings, 0).proceed();
        }

        private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" -> beanClass.getSimpleName() + "Proxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new IllegalStateException("Unsupported Object method " + method);
            };
        }

        private Method targetMethod(Method interfaceMethod) {
            try {
                Method method = beanClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                throw new AppContextException("Can not resolve target method " + interfaceMethod + " on " + beanClass.getName(), e);
            }
        }

        private List<AdviceBinding> adviceBindings(Method interfaceMethod, Method targetMethod) {
            var annotations = new ArrayList<Annotation>();
            annotations.addAll(proxyFactory.adviceAnnotations(beanClass.getAnnotations()));
            annotations.addAll(proxyFactory.adviceAnnotations(interfaceMethod.getDeclaringClass().getAnnotations()));
            annotations.addAll(proxyFactory.adviceAnnotations(interfaceMethod.getAnnotations()));
            annotations.addAll(proxyFactory.adviceAnnotations(targetMethod.getAnnotations()));
            return annotations.stream()
                    .map(annotation -> new AdviceBinding(annotation, proxyFactory.advice(annotation)))
                    .toList();
        }

        private Object invokeTarget(Method targetMethod, Object[] args) throws Throwable {
            try {
                targetMethod.setAccessible(true);
                return targetMethod.invoke(target, args == null ? new Object[0] : args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    private static final class DefaultAdviceInvocation implements AdviceInvocation {
        private final AppContext appContext;
        private final Object target;
        private final Method method;
        private final Object[] args;
        private final List<AdviceBinding> bindings;
        private final int index;

        private DefaultAdviceInvocation(AppContext appContext, Object target, Method method, Object[] args,
                                        List<AdviceBinding> bindings, int index) {
            this.appContext = appContext;
            this.target = target;
            this.method = method;
            this.args = args == null ? new Object[0] : args;
            this.bindings = bindings;
            this.index = index;
        }

        @Override
        public AppContext appContext() {
            return appContext;
        }

        @Override
        public Object target() {
            return target;
        }

        @Override
        public Method method() {
            return method;
        }

        @Override
        public Object[] args() {
            return args;
        }

        @Override
        public List<Annotation> annotations() {
            return bindings.stream().map(AdviceBinding::annotation).toList();
        }

        @Override
        public Object proceed() throws Throwable {
            if (index < bindings.size()) {
                AdviceBinding binding = bindings.get(index);
                return binding.advice().around(new DefaultAdviceInvocation(appContext, target, method, args, bindings, index + 1));
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
