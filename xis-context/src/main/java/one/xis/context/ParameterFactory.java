package one.xis.context;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ParameterFactory {

    private final List<Param> params = new ArrayList<>();

    Param createParam(Parameter parameter, int index, SingletonProducer producer) {
        Param param;
        if (parameter.getType().isArray()) {
            param = new ArrayParam(parameter, producer);
        } else if (Collection.class.isAssignableFrom(parameter.getType())) {
            param = new CollectionParam(parameter, producer);
        } else {
            param = new SimpleParam(parameter, index, producer);
        }
        params.add(param);
        return param;
    }

}
