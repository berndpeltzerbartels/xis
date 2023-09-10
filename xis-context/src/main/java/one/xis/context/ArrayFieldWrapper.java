package one.xis.context;

import one.xis.utils.lang.FieldUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;

class ArrayFieldWrapper extends MultivalueFieldWrapper {

    ArrayFieldWrapper(Field field, Object owner, Class<? extends Annotation> fieldAnnotation, Collection<Class<?>> allClasses) {
        super(field, owner, fieldAnnotation, allClasses);
    }

    @Override
    protected void setFieldValue(Field field, Collection<Object> values) {
        var arr = (Object[]) Array.newInstance(field.getType().getComponentType(), values.size());
        var index = 0;
        for (Object o : values) {
            arr[index++] = o;
        }
        FieldUtil.setFieldValue(owner, field, arr);
    }
}
