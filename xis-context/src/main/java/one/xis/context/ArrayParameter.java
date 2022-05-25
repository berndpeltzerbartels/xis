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
        return Array.newInstance(getElementType(), getValues().size());
    }
}
