package one.xis.server;

import lombok.Getter;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XISComponent
class ConfigService {


    @Getter
    private Config config;

    @XISInit
    void init() {

    }

    private Map<String, ControllerWrapper> controllersByClass(Collection<ControllerWrapper> controllerWrappers) {
        return controllerWrappers.stream().collect(Collectors.toMap(ControllerWrapper::getId, Function.identity()));
    }


}
