package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.UserContextImpl;
import one.xis.Widget;
import one.xis.auth.AuthenticationException;
import one.xis.security.SecurityUtil;
import one.xis.validation.ValidatorMessages;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ControllerWrapper {

    /**
     * ID of the component. In case of page, it's an url. For widgets, it's
     * the classes simple name or custom key.
     */
    private String id;

    /**
     * The wrapped instance.
     */
    private Object controller;

    private Collection<ControllerMethod> requestScopeMethods;
    private Collection<ControllerMethod> modelMethods;
    private Collection<ControllerMethod> tagContentOnlyMethods;
    private Map<String, ControllerMethod> actionMethods;
    private Collection<ControllerMethod> formDataMethods;
    private Collection<ControllerMethod> localStorageOnlyMethods;
    private Collection<ControllerMethod> sessionStorageOnlyMethods;
    private Collection<ControllerMethod> clientStorageOnlyMethods;
    private Collection<ControllerMethod> globalVariableOnlyMethods;
    private Collection<ControllerMethod> titleOnlyMethods;
    private Collection<ControllerMethod> widgetInContainerOnlyMethods;
    private ControllerResultMapper controllerResultMapper;

    void invokeGetModelMethods(ClientRequest request, ControllerResult controllerResult) {
        SecurityUtil.checkRoles(controller.getClass(), UserContextImpl.getInstance().getRoles());
        var methodsToExecute = new ArrayList<>(modelMethods);
        methodsToExecute.addAll(localStorageOnlyMethods);
        methodsToExecute.addAll(sessionStorageOnlyMethods);
        methodsToExecute.addAll(clientStorageOnlyMethods);
        methodsToExecute.addAll(globalVariableOnlyMethods);
        methodsToExecute.addAll(tagContentOnlyMethods);
        methodsToExecute.addAll(titleOnlyMethods);
        methodsToExecute.addAll(widgetInContainerOnlyMethods);
        var methods = RequestScopeSorter.sortMethods(methodsToExecute, requestScopeMethods);
        methods.forEach(m -> invokeModelDataMethod(request, controllerResult, m));
    }

    void invokeFormDataMethods(ClientRequest request, ControllerResult controllerResult) {
        SecurityUtil.checkRoles(controller.getClass(), UserContextImpl.getInstance().getRoles());
        var methods = RequestScopeSorter.sortMethods(formDataMethods, requestScopeMethods);
        methods.forEach(m -> invokeModelDataMethod(request, controllerResult, m));
    }

    void invokeActionMethod(ClientRequest request, ControllerResult controllerResult) {
        SecurityUtil.checkRoles(controller.getClass(), UserContextImpl.getInstance().getRoles());
        var method = actionMethods.get(request.getAction());
        if (method == null) {
            throw new RuntimeException("No action-method found for action " + request.getAction() + " in controller " + controller.getClass().getName());
        }
        var methods = RequestScopeSorter.sortMethods(Set.of(method), requestScopeMethods);
        methods.forEach(m -> {
            if (m.equals(method)) {
                invokeActionMethod(request, controllerResult, m);
            } else {
                invokeModelDataMethod(request, controllerResult, m); // TODO Schrott ?
            }
        });
    }

    boolean isWidgetController() {
        return controller.getClass().isAnnotationPresent(Widget.class);
    }

    Class<?> getControllerClass() {
        return controller.getClass();
    }

    private void invokeModelDataMethod(ClientRequest request, ControllerResult controllerResult, ControllerMethod method) {
        try {
            var controllerMethodResult = method.invoke(request, controller, controllerResult.getRequestScope());
            if (controllerMethodResult.isValidationFailed()) {
                // these validation errors are unexpected, so we throw an exception
                throw exceptionForValidationErrors(controllerMethodResult.getValidatorMessages());
            }
            controllerResultMapper.mapMethodResultToControllerResult(controllerMethodResult, controllerResult);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke model-method " + method, e);
        }
    }

    private void invokeActionMethod(ClientRequest request, ControllerResult controllerResult, ControllerMethod method) {
        if (method == null) {
            throw new RuntimeException("No action-method found for action " + request.getAction());
        }
        try {
            var controllerMethodResult = method.invoke(request, controller, controllerResult.getRequestScope());
            if (controllerMethodResult.getNextURL() != null) {
                controllerResult.setActionProcessing(ActionProcessing.PAGE);
            } else if (controllerMethodResult.getNextWidgetId() != null) {
                controllerResult.setActionProcessing(ActionProcessing.WIDGET);
            }
            controllerResultMapper.mapMethodResultToControllerResult(controllerMethodResult, controllerResult);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke action-method: " + method, e);
        }
    }


    /**
     * @param validatorMessages
     * @return a RuntimeException with a message containing all validation errors
     */
    private RuntimeException exceptionForValidationErrors(ValidatorMessages validatorMessages) {
        // TODO
        return new RuntimeException("Errors occurred: ");

    }


    static class RequestScopeSorter {
        public static List<ControllerMethod> sortMethods(Collection<ControllerMethod> mandatoryMethods, Collection<ControllerMethod> conditionalMethods) {
            Map<String, ControllerMethod> providedBy = new HashMap<>();
            for (ControllerMethod method : conditionalMethods) {
                String retScope = method.getReturnValueRequestScopeKey();
                if (retScope != null) {
                    providedBy.put(retScope, method);
                }
            }
            Set<ControllerMethod> allRequired = new HashSet<>();
            for (ControllerMethod method : mandatoryMethods) {
                allRequired.add(method);
                allRequired.addAll(resolveDependencies(method, providedBy));
            }
            Set<ControllerMethod> needed = allRequired.stream()
                    .flatMap(m -> m.getParameterRequestScopeKeys().stream())
                    .map(providedBy::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            for (ControllerMethod method : needed) {
                allRequired.add(method);
                allRequired.addAll(resolveDependencies(method, providedBy));
            }
            return topologicalSort(allRequired, providedBy);
        }

        private static Set<ControllerMethod> resolveDependencies(ControllerMethod method, Map<String, ControllerMethod> providedBy) {
            Set<ControllerMethod> deps = new HashSet<>();
            for (String paramScope : method.getParameterRequestScopeKeys()) {
                ControllerMethod provider = providedBy.get(paramScope);
                if (provider != null) {
                    deps.add(provider);
                    deps.addAll(resolveDependencies(provider, providedBy));
                }
            }
            return deps;
        }

        private static List<ControllerMethod> topologicalSort(Set<ControllerMethod> methods, Map<String, ControllerMethod> providedBy) {
            Map<ControllerMethod, Set<ControllerMethod>> graph = new HashMap<>();
            for (ControllerMethod m : methods) {
                graph.put(m, resolveDependencies(m, providedBy));
            }
            List<ControllerMethod> result = new ArrayList<>();
            Set<ControllerMethod> visited = new HashSet<>();
            Set<ControllerMethod> visiting = new HashSet<>();
            for (ControllerMethod m : methods) {
                if (!visited.contains(m)) {
                    dfs(m, graph, visited, visiting, result);
                }
            }
            return result;
        }

        private static void dfs(ControllerMethod current, Map<ControllerMethod, Set<ControllerMethod>> graph,
                                Set<ControllerMethod> visited, Set<ControllerMethod> visiting,
                                List<ControllerMethod> result) {
            if (visiting.contains(current)) {
                throw new IllegalStateException("Cycle detected");
            }
            if (visited.contains(current)) return;
            visiting.add(current);
            for (ControllerMethod dep : graph.getOrDefault(current, Collections.emptySet())) {
                dfs(dep, graph, visited, visiting, result);
            }
            visiting.remove(current);
            visited.add(current);
            result.add(current);
        }
    }
}
