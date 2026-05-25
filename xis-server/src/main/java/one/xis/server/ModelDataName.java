package one.xis.server;

import one.xis.ModelData;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;

final class ModelDataName {
    private ModelDataName() {
    }

    static String forMethod(Method method) {
        var modelData = method.getAnnotation(ModelData.class);
        var value = modelData.value();
        var varName = modelData.varName();
        if (!value.isEmpty() && !varName.isEmpty() && !value.equals(varName)) {
            throw new IllegalStateException("@ModelData value and varName differ on method: " + method);
        }
        if (!value.isEmpty()) {
            return value;
        }
        if (!varName.isEmpty()) {
            return varName;
        }
        return MethodUtils.propertyNameByGetter(method).orElse(method.getName());
    }
}
