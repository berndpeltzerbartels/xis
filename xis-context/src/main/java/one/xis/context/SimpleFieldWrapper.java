package one.xis.context;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.FieldUtil;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@RequiredArgsConstructor
class SimpleFieldWrapper implements FieldWrapper {

    private final Field field;
    private final Object owner;
    @Nullable
    private final Class<? extends Annotation> fieldAnnotation;

    @Override
    public void onComponentCreated(Object o) {
        if (field.getType().isAssignableFrom(o.getClass())
                && (fieldAnnotation == null || o.getClass().isAnnotationPresent(fieldAnnotation))) {

            FieldUtil.setFieldValue(owner, field, o);
        }
    }

    @Override
    public boolean isInjected() {
        return FieldUtil.getFieldValue(owner, field) != null;
    }
}
