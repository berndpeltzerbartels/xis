package one.xis.invocation;


import lombok.RequiredArgsConstructor;
import one.xis.Request;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ControllerMethod {
    private final Method method;
    private final List<MethodParameter> methodParameters;

    void setParameters(Request request) {
        methodParameters.forEach(methodParameter -> methodParameter.setValue(request));
    }

    Set<String> getRequiredModelIds() {
        return methodParameters.stream()
                .filter(ModelParameter.class::isInstance)
                .map(ModelParameter.class::cast)
                .map(ModelParameter::getId)
                .collect(Collectors.toSet());
    }

}
