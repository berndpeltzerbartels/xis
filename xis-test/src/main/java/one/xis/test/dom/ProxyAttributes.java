package one.xis.test.dom;

import lombok.NonNull;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.MethodUtils;
import org.graalvm.polyglot.Value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ProxyAttributes {

    Method getGetter();

    Method getSetter();

    String getName();

    boolean isWritable();

    Field getField();

    boolean isUseField();

    default Object getValue(Object base) {
        if (isUseField()) {
            return getFieldValue(base);
        } else {
            return invokeGetter(base);
        }
    }

    default Object getFieldValue(@NonNull Object base) {
        if (!isUseField()) {
            throw new UnsupportedOperationException("Cannot get value using field for attribute: " + getName() + " in " + base.getClass().getName());
        }
        return FieldUtil.getFieldValue(base, getField());
    }

    default Object invokeGetter(Object base) {
        return MethodUtils.doInvoke(base, getGetter());
    }


    default void setValue(Object base, Value value) {
        if (!isUseField()) {
            throw new UnsupportedOperationException("Cannot set value using method for attribute: " + getName());
        }
        var type = getField().getType();
        Object convertedValue = GraalVMUtils.convertValue(type, value);
        FieldUtil.setFieldValue(base, getField(), convertedValue);
    }
}
