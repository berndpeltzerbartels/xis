package one.xis.context2;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

class Parameters {

    private final List<Param> params = new ArrayList<>();

    Param createParam(Parameter parameter, SingletonProducer producer) {
        var param = new SimpleParam(parameter, producer);// TODO
        params.add(param);
        return param;
    }

}
