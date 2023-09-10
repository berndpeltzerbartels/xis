package one.xis.context;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.util.List;

class ArrayParameter extends MultiValueParameter {

    ArrayParameter(Parameter parameter, int index, List<Class<?>> allComponentClasses) {
        super(parameter, index, allComponentClasses);
    }

    @Override
    protected Class<?> findElementType(Parameter parameter) {
        return parameter.getType().getComponentType();
    }


    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public Object getValue() {
        var arr = (Object[]) Array.newInstance(getElementType(), getValues().size());
        var index = 0;
        for (Object o : getValues()) {
            arr[index++] = o;
        }
        return arr;
    }
}
