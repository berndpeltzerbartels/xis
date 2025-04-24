package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;

import java.lang.reflect.Method;
import java.util.Map;

@Data
@Slf4j
class ControllerMethod {

    protected final Method method;
    protected final MainDeserializer deserializer;
    protected final ControllerMethodResultMapper controllerMethodResultMapper;
    protected final ControllerMethodParameter[] controllerMethodParameters;

    ControllerMethod(Method method, MainDeserializer deserializer, ControllerMethodResultMapper controllerMethodResultMapper) {
        this.method = method;
        this.deserializer = deserializer;
        this.controllerMethodResultMapper = controllerMethodResultMapper;
        this.controllerMethodParameters = new ControllerMethodParameter[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            controllerMethodParameters[i] = new ControllerMethodParameter(method, method.getParameters()[i], deserializer);
        }
    }

    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller, Map<String, Object> requestScope) throws Exception {
        var postProcessingResults = new PostProcessingResults();
        var args = prepareArgs(method, request, postProcessingResults, requestScope);
        if (postProcessingResults.authenticate()) {
            // TODO
        }
        var controllerMethodResult = new ControllerMethodResult();
        controllerMethodResultMapper.mapRequestToResult(request, controllerMethodResult);
        if (postProcessingResults.reject()) {
            controllerMethodResultMapper.mapValidationErrors(controllerMethodResult, postProcessingResults.getResults());
            return controllerMethodResult;
        }
        var returnValue = method.invoke(controller, args);
        // let parameters override request values
        controllerMethodResultMapper.mapMethodParameterToResultAfterInvocation(controllerMethodResult, controllerMethodParameters, args);
        // let return values override parameters
        controllerMethodResultMapper.mapReturnValueToResult(controllerMethodResult, method, returnValue, requestScope);
        return controllerMethodResult;
    }

    @Override
    public String toString() {
        return "ControllerMethod(" + method.getName() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ControllerMethod that = (ControllerMethod) obj;
        return method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    protected Object[] prepareArgs(Method method, ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope) throws Exception {
        var args = new Object[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            args[i] = controllerMethodParameters[i].prepareParameter(request, postProcessingResults, requestScope);
        }
        return args;
    }

}
