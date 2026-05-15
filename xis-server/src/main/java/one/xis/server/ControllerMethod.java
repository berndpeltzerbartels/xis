package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.*;
import one.xis.auth.AuthenticationException;
import one.xis.auth.URLForbiddenException;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.RequestContext;
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
        var positionalParameterIndex = 0;
        for (var i = 0; i < method.getParameterCount(); i++) {
            var parameter = method.getParameters()[i];
            var assignedPositionalIndex = -1;
            if (isImplicitPositionalParameter(parameter)) {
                assignedPositionalIndex = positionalParameterIndex++;
            }
            controllerMethodParameters[i] = new ControllerMethodParameter(method, parameter, deserializer, i, assignedPositionalIndex);
        }
        if (method.isAnnotationPresent(Action.class)) {
            this.updateEventKeys.addAll(Arrays.asList(method.getAnnotation(Action.class).updateEventKeys()));
        }

    }

    private boolean isImplicitPositionalParameter(java.lang.reflect.Parameter parameter) {
        if (!method.isAnnotationPresent(Action.class)) {
            return false;
        }
        if (parameter.isAnnotationPresent(one.xis.Parameter.class)) {
            var actionParameter = parameter.getAnnotation(one.xis.Parameter.class);
            return actionParameter.value().isEmpty() && actionParameter.index() < 0;
        }
        return !isFrameworkInjectedParameter(parameter);
    }

    private boolean isFrameworkInjectedParameter(java.lang.reflect.Parameter parameter) {
        return parameter.getType().equals(HttpRequest.class)
                || parameter.getType().equals(HttpResponse.class)
                || parameter.getType().equals(RequestContext.class)
                || UserContext.class.isAssignableFrom(parameter.getType())
                || parameter.isAnnotationPresent(FormData.class)
                || parameter.isAnnotationPresent(UserId.class)
                || parameter.isAnnotationPresent(ClientId.class)
                || parameter.isAnnotationPresent(QueryParameter.class)
                || parameter.isAnnotationPresent(one.xis.PathVariable.class)
                || parameter.isAnnotationPresent(one.xis.Parameter.class)
                || parameter.isAnnotationPresent(SharedValue.class)
                || parameter.isAnnotationPresent(SessionStorage.class)
                || parameter.isAnnotationPresent(LocalStorage.class)
                || parameter.isAnnotationPresent(ClientStorage.class)
                || parameter.isAnnotationPresent(LocalDatabase.class);
    }

    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller, Map<String, Object> requestScope) throws Exception {
        SecurityUtil.checkRoles(method, UserContextImpl.getInstance());
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
        controllerMethodResultMapper.mapMethodParameterToResultAfterInvocation(controllerMethodResult, controllerMethodParameters, args, request);
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
        if (method.isAnnotationPresent(SharedValue.class)) {
            return method.getAnnotation(SharedValue.class).value();
        }
        return null;
    }

    Collection<String> getParameterRequestScopeKeys() {
        return Stream.of(method.getParameters())
                .filter(p -> p.isAnnotationPresent(SharedValue.class))
                .map(p -> p.getAnnotation(SharedValue.class).value())
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
