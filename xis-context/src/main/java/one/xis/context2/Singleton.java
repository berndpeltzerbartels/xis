package one.xis.context2;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISInject;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor
class Singleton implements SingletonConsumer {
    private final Class<?> beanClass;
    private final Collection<SingletonField> unassignedFields;
    private final Collection<SingletonMethod> methodsNotCalled;
    private final ApplicationContextFactory contextFactory;
    private Object bean;

    Singleton(Class<?> c, Annotations annotations, ApplicationContextFactory contextFactory) {
        beanClass = c;
        this.contextFactory = contextFactory;
        this.unassignedFields = FieldUtil.getFields(beanClass, field -> field.isAnnotationPresent(XISInject.class)).stream()
                .map(field -> new SingletonField(this, field)).toList();
        this.methodsNotCalled = MethodUtils.methods(c, annotations::isAnnotatedMethod).stream()
                .map(method -> new SingletonMethod(method, this)).toList();
    }

    @Override
    public void assignValue(Object o) {
        this.bean = o;
        onStateChanged();
    }

    void onFieldValueAssigned() {
        onStateChanged();
    }

    private void onStateChanged() {
        if (bean == null) return;
        if (unassignedFields.isEmpty()) {
            var methods = new HashSet<>(methodsNotCalled);
            for (var method : methods) {
                if (method.isSatisfied()) {
                    methodsNotCalled.remove(method);
                    method.execute();
                }
            }
        }
    }
}
