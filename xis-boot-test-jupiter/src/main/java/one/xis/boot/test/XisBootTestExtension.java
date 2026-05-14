package one.xis.boot.test;

import one.xis.context.AppContext;
import one.xis.context.IntegrationTestContext;
import one.xis.context.TestClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XisBootTestExtension implements TestInstanceFactory, ParameterResolver {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(XisBootTestExtension.class);

    @Override
    public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) throws TestInstantiationException {
        var testClass = factoryContext.getTestClass();
        var testFields = createTestFields(testClass);
        var context = createContext(testClass, testFields.singletons());
        addFrameworkFields(testClass, testFields.fields(), context);
        extensionContext.getRoot().getStore(NAMESPACE).put(testClass, context);
        var testInstance = context.getSingleton(testClass);
        injectTestFields(testInstance, testFields.fields());
        return testInstance;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        var parameterType = parameterContext.getParameter().getType();
        return parameterType.equals(IntegrationTestContext.class)
                || parameterType.equals(AppContext.class)
                || parameterType.equals(TestClient.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        var context = getContext(extensionContext);
        var parameterType = parameterContext.getParameter().getType();
        if (parameterType.equals(IntegrationTestContext.class)) {
            return context;
        }
        if (parameterType.equals(AppContext.class)) {
            return context.getAppContext();
        }
        if (parameterType.equals(TestClient.class)) {
            return context.openPage("/");
        }
        throw new ParameterResolutionException("Unsupported parameter type: " + parameterType.getName());
    }

    private IntegrationTestContext createContext(Class<?> testClass, Collection<Object> singletons) {
        var annotation = testClass.getAnnotation(XisBootTest.class);
        if (annotation == null) {
            throw new TestInstantiationException("Missing @XisBootTest on " + testClass.getName());
        }
        var builder = IntegrationTestContext.builder()
                .withBasePackageClass(testClass)
                .withSingleton(testClass);
        for (String packageName : annotation.packages()) {
            builder.withPackage(packageName);
        }
        for (Class<?> packageClass : annotation.packageClasses()) {
            builder.withBasePackageClass(packageClass);
        }
        singletons.forEach(builder::withSingleton);
        return builder.build();
    }

    private TestFields createTestFields(Class<?> testClass) {
        var fields = new HashMap<Field, Object>();
        var singletons = new ArrayList<Object>();
        for (Field field : fields(testClass)) {
            var mock = isMockField(field);
            var spy = isSpyField(field);
            if (mock && spy) {
                throw new TestInstantiationException("Field may not be annotated with both @Mock and @Spy: " + field);
            }
            if (mock) {
                var instance = Mockito.mock(field.getType());
                fields.put(field, instance);
                singletons.add(instance);
            }
            if (spy) {
                var instance = Mockito.mock(field.getType(), Mockito.withSettings()
                        .useConstructor()
                        .defaultAnswer(Mockito.CALLS_REAL_METHODS));
                fields.put(field, instance);
                singletons.add(instance);
            }
            if (isCaptorField(field)) {
                fields.put(field, ArgumentCaptor.forClass(captorType(field)));
            }
        }
        return new TestFields(fields, singletons);
    }

    private void addFrameworkFields(Class<?> testClass, Map<Field, Object> fields, IntegrationTestContext context) {
        for (Field field : fields(testClass)) {
            if (field.getType().equals(IntegrationTestContext.class)) {
                fields.putIfAbsent(field, context);
            }
        }
    }

    private boolean isMockField(Field field) {
        return hasAnnotation(field, one.xis.test.Mock.class, "org.mockito.Mock");
    }

    private boolean isSpyField(Field field) {
        return hasAnnotation(field, one.xis.test.Spy.class, "org.mockito.Spy");
    }

    private boolean isCaptorField(Field field) {
        return hasAnnotation(field, one.xis.test.Captor.class, "org.mockito.Captor");
    }

    private boolean hasAnnotation(Field field, Class<? extends Annotation> xisAnnotation, String optionalAnnotationName) {
        return field.isAnnotationPresent(xisAnnotation)
                || List.of(field.getAnnotations()).stream()
                .anyMatch(annotation -> annotation.annotationType().getName().equals(optionalAnnotationName));
    }

    private Class<?> captorType(Field field) {
        Type type = field.getGenericType();
        if (!(type instanceof ParameterizedType parameterizedType)) {
            throw new TestInstantiationException("@Captor field must use ArgumentCaptor<T>: " + field);
        }
        Type capturedType = parameterizedType.getActualTypeArguments()[0];
        if (!(capturedType instanceof Class<?> clazz)) {
            throw new TestInstantiationException("@Captor field type must be a concrete class: " + field);
        }
        return clazz;
    }

    private void injectTestFields(Object testInstance, Map<Field, Object> fields) {
        for (Map.Entry<Field, Object> entry : fields.entrySet()) {
            try {
                entry.getKey().setAccessible(true);
                entry.getKey().set(testInstance, entry.getValue());
            } catch (IllegalAccessException e) {
                throw new TestInstantiationException("Could not inject test field " + entry.getKey(), e);
            }
        }
    }

    private IntegrationTestContext getContext(ExtensionContext extensionContext) {
        return extensionContext.getRoot().getStore(NAMESPACE)
                .get(extensionContext.getRequiredTestClass(), IntegrationTestContext.class);
    }

    private Collection<Field> fields(Class<?> type) {
        var fields = new ArrayList<Field>();
        var current = type;
        while (current != null && !current.equals(Object.class)) {
            fields.addAll(java.util.List.of(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private record TestFields(Map<Field, Object> fields, Collection<Object> singletons) {
    }
}
