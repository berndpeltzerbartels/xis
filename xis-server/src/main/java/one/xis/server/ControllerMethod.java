package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.Action;
import one.xis.UserContextImpl;
import one.xis.auth.AuthenticationException;
import one.xis.auth.URLForbiddenException;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.security.SecurityUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Slf4j
class ControllerMethod {

    protected final Method method;
    protected final MainDeserializer deserializer;
    protected final ControllerMethodResultMapper controllerMethodResultMapper;
    protected final ControllerMethodParameter[] controllerMethodParameters;
    private Collection<String> updateEventKeys = new HashSet<>();

    ControllerMethod(Method method, MainDeserializer deserializer, ControllerMethodResultMapper controllerMethodResultMapper) {
        this.method = method;
        this.deserializer = deserializer;
        this.controllerMethodResultMapper = controllerMethodResultMapper;
        this.controllerMethodParameters = new ControllerMethodParameter[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            controllerMethodParameters[i] = new ControllerMethodParameter(method, method.getParameters()[i], deserializer);
        }
        if (method.isAnnotationPresent(Action.class)) {
            this.updateEventKeys.addAll(Arrays.asList(method.getAnnotation(Action.class).updateEventKeys()));
        }

    }

    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller, Map<String, Object> requestScope) throws Exception {
        SecurityUtil.checkRoles(method, UserContextImpl.getInstance().getRoles());
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
        Object returnValue;
        try {
            returnValue = method.invoke(controller, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof AuthenticationException) {
                throw (AuthenticationException) e.getCause();
            } else if (e.getCause() instanceof URLForbiddenException) {
                throw (URLForbiddenException) e.getCause();
            } else {
                log.error("Error invoking controller method: " + method.getName(), e);
                throw new RuntimeException("Error invoking controller method: " + method.getName(), e);
            }
        }
        controllerMethodResult.getUpdateEventKeys().addAll(this.updateEventKeys);
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

    String getReturnValueRequestScopeKey() {
        if (method.isAnnotationPresent(one.xis.RequestScope.class)) {
            return method.getAnnotation(one.xis.RequestScope.class).value();
        }
        return null;
    }

    Collection<String> getParameterRequestScopeKeys() {
        return Stream.of(method.getParameters())
                .filter(p -> p.isAnnotationPresent(one.xis.RequestScope.class))
                .map(p -> p.getAnnotation(one.xis.RequestScope.class).value())
                .collect(Collectors.toSet());
    }

    protected Object[] prepareArgs(Method method, ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope) throws Exception {
        var args = new Object[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            args[i] = controllerMethodParameters[i].prepareParameter(request, postProcessingResults, requestScope);
        }
        return args;
    }
}
