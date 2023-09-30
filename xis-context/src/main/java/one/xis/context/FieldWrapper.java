package one.xis.context;

import lombok.Getter;
import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;


class FieldWrapper extends ValueHolder {

    @Getter
    private final Field field;
    private final ComponentWrapperPlaceholder placeholder;

    @Getter
    private Object fieldValue;

    FieldWrapper(Field field, ComponentWrapperPlaceholder placeholder) {
        this.field = field;
        this.placeholder = placeholder;
        defaultValue().ifPresent(value -> fieldValue = value);
    }

    @Override
    Class<?> getType() {
        return field.getType();
    }

    @Override
    protected Predicate<Class<?>> getAnnotationFilter() {
        var inject = field.getAnnotation(XISInject.class);
        return inject.annotatedWith() == None.class ? c -> true : c -> c.isAnnotationPresent(inject.annotatedWith());
    }

    @Override
    void valueAssigned(Object o) {
        fieldValue = o;
        placeholder.fieldValueFound(this);
    }

    @Override
    Class<?> getElementType() {
        if (Collection.class.isAssignableFrom(getType())) {
            return FieldUtil.getGenericTypeParameter(field);
        }
        if (getType().isArray()) {
            return field.getType().getComponentType();
        }
        return field.getType();
    }

    void inject(Object component) {
        FieldUtil.setFieldValue(component, field, fieldValue);
    }

    @Override
    public String toString() {
        return "FieldWrapper{" + field.getType().getSimpleName() + " " + field.getName() + "}";
    }

    private Optional<Object> defaultValue() {
        if (Collection.class.isAssignableFrom(getType())) {
            return Optional.of(CollectionUtils.emptyInstance((Class<Collection<?>>) getType()));
        }
        if (getType().isArray()) {
            return Optional.of(Array.newInstance(getType(), 0));
        }
        return Optional.empty();
    }
}
