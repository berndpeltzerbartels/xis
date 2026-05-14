package one.xis.server;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

class RouterControllerWrapper extends ControllerWrapper {

    @Getter
    @Setter
    private Map<String, ControllerMethod> routeMethods;
}
