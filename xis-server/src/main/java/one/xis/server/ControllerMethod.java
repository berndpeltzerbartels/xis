package one.xis.server;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.AccessToken;
import one.xis.Roles;
import one.xis.auth.AuthenticationException;
import one.xis.deserialize.MainDeserializer;
import one.xis.deserialize.PostProcessingResults;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    ControllerMethodResult invoke(@NonNull ClientRequest request, @NonNull Object controller, Map<String, Object> requestScope, AccessToken accessToken) throws Exception {
        checkRoles(accessToken);
        var postProcessingResults = new PostProcessingResults();
        var args = prepareArgs(method, request, postProcessingResults, requestScope, accessToken);
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

    protected Object[] prepareArgs(Method method, ClientRequest request, PostProcessingResults postProcessingResults, Map<String, Object> requestScope, AccessToken accessToken) throws Exception {
        var args = new Object[method.getParameterCount()];
        for (var i = 0; i < method.getParameterCount(); i++) {
            args[i] = controllerMethodParameters[i].prepareParameter(request, postProcessingResults, requestScope, accessToken);
        }
        return args;
    }


    private void checkRoles(AccessToken accessToken) {
        var requiredRoles = getRequiredRoles();
        if (requiredRoles.isEmpty()) {
            return;
        }
        if (accessToken == null || !accessToken.isAuthenticated()) {
            throw new AuthenticationException("Access token is required for method: " + method.getName());
        }
        var userRoles = accessToken.getRoles();
        // check if user has at least one of the required roles
        if (userRoles == null || userRoles.isEmpty() || requiredRoles.stream().noneMatch(userRoles::contains)) {
            throw new AuthenticationException("User does not have required roles for method: " + method.getName());
        }
    }

    private Set<String> getRequiredRoles() {
        var roles = new HashSet<Roles>();
        if (method.isAnnotationPresent(Roles.class)) {
            roles.add(method.getAnnotation(Roles.class));
        }
        var c = method.getDeclaringClass();
        while (c != null && c != Object.class) {
            if (c.isAnnotationPresent(Roles.class)) {
                roles.add(c.getAnnotation(Roles.class));
            }
            c = c.getSuperclass();
        }
        return roles.stream()
                .flatMap(role -> Stream.of(role.value()))
                .collect(Collectors.toSet());

    }
}
