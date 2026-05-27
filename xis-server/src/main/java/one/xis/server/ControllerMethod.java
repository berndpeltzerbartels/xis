package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.*;
import one.xis.auth.AuthenticationException;
import one.xis.auth.AuthorizationException;
import one.xis.auth.AccessForbiddenException;
import one.xis.validation.ValidationFailedException;
import one.xis.deserialize.AccessDeniedError;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.http.RequestContext;
import one.xis.security.SecurityUtil;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Slf4j
class ControllerMethod {

    protected final Method method;
    protected final MainDeserializer deserializer;
    protected final ControllerMethodResultMapper controllerMethodResultMapper;
    protected final ControllerMethodParameter[] controllerMethodParameters;
    protected final UploadConfiguration uploadConfiguration;
    private Collection<String> updateEventKeys = new HashSet<>();

    ControllerMethod(Method method, MainDeserializer deserializer, ControllerMethodResultMapper controllerMethodResultMapper, UploadConfiguration uploadConfiguration) {
        this.method = method;
        this.deserializer = deserializer;
        this.controllerMethodResultMapper = controllerMethodResultMapper;
        this.uploadConfiguration = uploadConfiguration;
        this.controllerMethodParameters = new ControllerMethodParameter[method.getParameterCount()];
        var positionalParameterIndex = 0;
        for (var i = 0; i < method.getParameterCount(); i++) {
            var parameter = method.getParameters()[i];
            var assignedPositionalIndex = -1;
            if (isImplicitPositionalParameter(parameter)) {
                assignedPositionalIndex = positionalParameterIndex++;
            }
            controllerMethodParameters[i] = new ControllerMethodParameter(method, parameter, deserializer, i, assignedPositionalIndex, uploadConfiguration);
        }
        if (method.isAnnotationPresent(Action.class)) {
            this.updateEventKeys.addAll(Arrays.asList(method.getAnnotation(Action.class).updateEventKeys()));
        }

    }

    private boolean isImplicitPositionalParameter(java.lang.reflect.Parameter parameter) {
        if (!method.isAnnotationPresent(Action.class)) {
            return false;
        }
        if (parameter.isAnnotationPresent(ActionParameter.class)) {
            var actionParameter = parameter.getAnnotation(ActionParameter.class);
            return actionParameter.value().isEmpty() && actionParameter.index() < 0;
        }
        return !isFrameworkInjectedParameter(parameter);
    }

    private boolean isFrameworkInjectedParameter(java.lang.reflect.Parameter parameter) {
        return parameter.getType().equals(HttpRequest.class)
                || parameter.getType().equals(HttpResponse.class)
                || parameter.getType().equals(RequestContext.class)
                || parameter.getType().equals(ToastMessages.class)
                || UserContext.class.isAssignableFrom(parameter.getType())
                || parameter.isAnnotationPresent(FormData.class)
                || parameter.isAnnotationPresent(Upload.class)
                || parameter.isAnnotationPresent(UserId.class)
                || parameter.isAnnotationPresent(ClientId.class)
                || parameter.isAnnotationPresent(QueryParameter.class)
                || parameter.isAnnotationPresent(one.xis.PathVariable.class)
                || parameter.isAnnotationPresent(FrontletParameter.class)
                || parameter.isAnnotationPresent(ModalParameter.class)
                || parameter.isAnnotationPresent(SharedValue.class)
                || parameter.isAnnotationPresent(SessionStorage.class)
                || parameter.isAnnotationPresent(LocalStorage.class)
                || parameter.isAnnotationPresent(ClientState.class)
                || parameter.isAnnotationPresent(LocalDatabase.class);
    }

    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller, ControllerResult controllerResult) throws Exception {
        SecurityUtil.checkRoles(method, UserContextImpl.getInstance());
        var postProcessingResults = new PostProcessingResults();
        var args = prepareArgs(method, request, postProcessingResults, controllerResult);
        if (postProcessingResults.authenticate()) {
            throwAccessDenied(postProcessingResults);
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
            } else if (e.getCause() instanceof AccessForbiddenException) {
                throw (AccessForbiddenException) e.getCause();
            } else if (e.getCause() instanceof ValidationFailedException) {
                throw (ValidationFailedException) e.getCause();
            } else {
                log.error("Error invoking controller method: " + method.getName(), e);
                throw new RuntimeException("Error invoking controller method: " + method.getName(), e);
            }
        }
        controllerMethodResult.getUpdateEventKeys().addAll(this.updateEventKeys);
        // let parameters override request values
        controllerMethodResultMapper.mapMethodParameterToResultAfterInvocation(controllerMethodResult, controllerMethodParameters, args, request);
        // let return values override parameters
        controllerMethodResultMapper.mapReturnValueToResult(controllerMethodResult, method, returnValue, controllerResult.getSharedValues());
        return controllerMethodResult;
    }

    @Override
    public String toString() {
        return "ControllerMethod:" + method.getDeclaringClass()+"."+method.getName();
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

    String getReturnValueSharedValueKey() {
        if (method.isAnnotationPresent(SharedValue.class)) {
            return method.getAnnotation(SharedValue.class).value();
        }
        return null;
    }

    Optional<String> getModelDataKey() {
        if (!method.isAnnotationPresent(ModelData.class)) {
            return Optional.empty();
        }
        return Optional.of(ModelDataName.forMethod(method));
    }

    Optional<String> getFormDataKey() {
        if (!method.isAnnotationPresent(FormData.class)) {
            return Optional.empty();
        }
        var formData = method.getAnnotation(FormData.class);
        if (!formData.value().isEmpty()) {
            return Optional.of(formData.value());
        }
        return Optional.of(MethodUtils.propertyNameByGetter(method).orElse(method.getName()));
    }

    boolean shouldLoadModelData(ModelDataLoad load) {
        if (!method.isAnnotationPresent(ModelData.class)) {
            return true;
        }
        var configuredLoad = method.getAnnotation(ModelData.class).load();
        return configuredLoad == ModelDataLoad.ALWAYS || configuredLoad == load;
    }

    boolean shouldLoadFormData(ModelDataLoad load) {
        if (!method.isAnnotationPresent(FormData.class)) {
            return true;
        }
        var configuredLoad = method.getAnnotation(FormData.class).load();
        return configuredLoad == ModelDataLoad.ALWAYS || configuredLoad == load;
    }

    Collection<String> getParameterSharedValueKeys() {
        return Stream.of(method.getParameters())
                .filter(p -> p.isAnnotationPresent(SharedValue.class))
                .map(p -> p.getAnnotation(SharedValue.class).value())
                .collect(Collectors.toSet());
    }

    protected Object[] prepareArgs(Method method, ClientRequest request, PostProcessingResults postProcessingResults, ControllerResult controllerResult) throws Exception {
        var args = new Object[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            args[i] = controllerMethodParameters[i].prepareParameter(request, postProcessingResults, controllerResult);
        }
        return args;
    }

    private void throwAccessDenied(PostProcessingResults postProcessingResults) {
        var accessDenied = postProcessingResults.postProcessingResults(AccessDeniedError.class).stream().findFirst();
        if (accessDenied.isEmpty()) {
            throw new AuthenticationException();
        }
        var error = accessDenied.get();
        if (error.isAuthenticationRequired()) {
            throw new AuthenticationException(error.getMessage());
        }
        throw new AuthorizationException(error.getMessage());
    }
}
