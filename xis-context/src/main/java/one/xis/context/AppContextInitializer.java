package one.xis.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AppContextInitializer implements Runnable {

    private final Reflections reflections;

    public AppContextInitializer(Class<?> basePackageClass) {
        this(basePackageClass.getPackageName());
    }

    public AppContextInitializer(String basePackage) {
        reflections = new Reflections(basePackage, new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new FieldAnnotationsScanner());
    }
    
    @Getter
    private Set<Object> singletons;

    @Override
    public void run() {
        FieldInjection fieldInjection = new FieldInjection();
        InitInvocation initInvokers = new InitInvocation();
        Instantiation instantiation = new Instantiation(fieldInjection, initInvokers);
        instantiation.createInstances();
        fieldInjection.doInjection();
        initInvokers.invokeAll();
        singletons = instantiation.getSingletons();
    }

    @Getter
    class FieldInjection {
        private final Set<DependencyField> dependencyFields;

        FieldInjection() {
            dependencyFields = reflections.getFieldsAnnotatedWith(Inj.class).stream().map(DependencyField::new).collect(Collectors.toSet());
        }

        void onComponentCreated(Object o) {
            dependencyFields.forEach(field -> field.onComponentCreated(o));
        }

        void doInjection() {
            dependencyFields.forEach(DependencyField::doInjection);
        }
    }

    static class InitInvocation {
        private Set<InitInvoker> invokers = new HashSet<>();

        void onComponentCreated(Object o) {
            Class<?> c = o.getClass();
            while (c != null && !c.equals(Object.class)) {
                findInitMethods(c);
                c = c.getSuperclass();
            }
            invokers.forEach(initInvoker -> initInvoker.onComponentCreated(o));
        }

        void invokeAll() {
            invokers.forEach(initInvoker -> initInvoker.invoke());
        }

        private void findInitMethods(Class<?> c) {
            Arrays.stream(c.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Init.class))
                    .map(InitInvoker::new).forEach(invokers::add);
        }
    }

    @RequiredArgsConstructor
    static class InitInvoker {
        Set<Object> owners = new HashSet<>();
        private final Method method;

        void onComponentCreated(Object o) {
            if (method.getDeclaringClass().isInstance(o)) {
                owners.add(o);
            }
        }

        void invoke() {
            owners.forEach(this::invoke);
        }

        private void invoke(Object owner) {
            try {
                method.invoke(owner);
            } catch (IllegalAccessException e) {
                throw new AppContextException(e);
            } catch (InvocationTargetException e) {
                throw new AppContextException(e);
            }
        }
    }

    @RequiredArgsConstructor
    static class DependencyField {
        private Set<Object> owners = new HashSet<>();
        private Object fieldValue;
        private final Field field;

        void onComponentCreated(Object o) {
            if (field.getDeclaringClass().isInstance(o)) {
                owners.add(o);
            }
            if (field.getType().isInstance(o)) {
                if (fieldValue != null) {
                    throw new AppContextException("ambigious candidates for " + field);
                }
                fieldValue = o;
            }
        }

        void doInjection() {
            if (fieldValue == null) {
                throw new AppContextException("no candidate for " + field);
            }
            if (owners.isEmpty()) {
                throw new IllegalStateException();
            }
            owners.forEach(this::inject);
        }

        @SneakyThrows
        private void inject(Object owner) {
            field.setAccessible(true);
            field.set(owner, fieldValue);
        }
    }

    class Instantiation {

        private Set<Instantitor> instantitors;
        private final FieldInjection fieldInjection;
        private final InitInvocation initInvocation;

        @Getter
        private final Set<Object> singletons = new HashSet<>();

        Instantiation(FieldInjection fieldInjection, InitInvocation initInvocation) {
            this.fieldInjection = fieldInjection;
            this.initInvocation = initInvocation;
            instantitors = reflections.getTypesAnnotatedWith(Comp.class).stream()//
                    .map(this::createInstantitor)//
                    .collect(Collectors.toSet());
        }

        private Instantitor createInstantitor(Class<?> aClass) {
            Instantitor instantitor = new Instantitor(aClass);
            instantitor.init();
            return instantitor;
        }

        void createInstances() {
            instantitors.stream().filter(Instantitor::isParameterCompleted).findFirst().ifPresent(this::createInstance);
        }

        private void createInstance(Instantitor instantitor) {
            instantitors.remove(instantitor);
            onComponentCreated(instantitor.createInstance());
        }

        void onComponentCreated(Object o) {
            singletons.add(o);
            fieldInjection.onComponentCreated(o);
            initInvocation.onComponentCreated(o);
            instantitors.forEach(instantitor -> instantitor.onComponentCreated(o));
            createInstances();
        }
    }

    @RequiredArgsConstructor
    static class Instantitor {
        private final Class<?> type;
        private List<Class<?>> parameterTypes;
        private Object[] parameters;
        private Constructor<?> constructor;
        private int missingParameters;

        void init() {
            constructor = getConstructor();
            parameterTypes = List.of(constructor.getParameterTypes());
            parameters = new Object[parameterTypes.size()];
            missingParameters = parameterTypes.size();
        }

        void onComponentCreated(Object o) {
            setParameters(o);
        }

        private void setParameters(Object o) {
            for (int i = 0; i < parameterTypes.size(); i++) {
                if (parameterTypes.get(i).isInstance(o)) {
                    if (parameters[i] != null) {
                        throw new AppContextException("ambigious candidates of type " + parameterTypes.get(i).getName() + " in constructor of " + type);
                    }
                    parameters[i] = o;
                    missingParameters--;
                }
            }
        }

        boolean isParameterCompleted() {
            return missingParameters < 1;
        }

        private Constructor<?> getConstructor() {
            List<Constructor<?>> constructors = Arrays.stream(type.getDeclaredConstructors()).filter(this::nonPrivate).collect(Collectors.toList());
            switch (constructors.size()) {
                case 0:
                    throw new AppContextException("no accessible constructor for " + type);
                case 1:
                    return constructors.get(0);
                default:
                    throw new AppContextException("too many constructors for " + type);
            }
        }

        private boolean nonPrivate(Executable accessibleObject) {
            return !Modifier.isPrivate(accessibleObject.getModifiers());
        }

        @SneakyThrows
        Object createInstance() {
            constructor.setAccessible(true);
            return constructor.newInstance(parameters);
        }
    }
}
