package one.xis.context;

import one.xis.utils.lang.CollectionUtils;
import one.xis.utils.lang.FieldUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

class CollectionFieldWrapper extends MultivalueFieldWrapper {

    CollectionFieldWrapper(Field field, Object owner, Class<? extends Annotation> fieldAnnotation, Collection<Class<?>> allClasses) {
        super(field, owner, fieldAnnotation, allClasses);
    }

    @Override
    protected void setFieldValue(Field field, Collection<Object> values) {
        var elementType = FieldUtil.getGenericTypeParameter(field);
        var coll = (Collection<Object>) CollectionUtils.emptyInstance((Class<Collection<?>>) field.getType());
        coll.addAll(values);
    }
}
