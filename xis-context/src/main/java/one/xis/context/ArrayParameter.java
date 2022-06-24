package one.xis.context;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;

class ArrayParameter extends MultiValueParameter {

    ArrayParameter(Parameter parameter) {
        super(parameter);
    }

    @Override
    protected Class<?> findElementType(Parameter parameter) {
        return parameter.getType().getComponentType();
    }


    @Override
    Object getValue() {
        var arr = (Object[]) Array.newInstance(getElementType(), getValues().size());
        var index = 0;
        for (Object o : getValues()) {
            arr[index++] = o;
        }
        return arr;
    }
}
