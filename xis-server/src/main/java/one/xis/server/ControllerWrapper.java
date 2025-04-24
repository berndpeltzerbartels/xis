package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import one.xis.RequestScope;
import one.xis.validation.ValidatorMessages;
import org.tinylog.Logger;

import java.util.*;

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

    private Map<String, ControllerMethod> requestScopeMethods;
    private Collection<ControllerMethod> modelMethods;
    private Map<String, ControllerMethod> actionMethods;
    private Collection<ControllerMethod> formDataMethods;
    private ControllerResultMapper controllerResultMapper;

    void invokeGetModelMethods(ClientRequest request, ControllerResult controllerResult) {
        var methods = new HashSet<>(modelMethods);
        methods.addAll(requestScopeMethods.values());
        new ModelDataMethodChainExecutor(request, controllerResult, methods).execute();
    }

    void invokeFormDataMethods(ClientRequest request, ControllerResult controllerResult) {
        var formDataMethods = new HashSet<>(this.formDataMethods);
        formDataMethods.addAll(requestScopeMethods.values());
        new FormDataMethodChainExecutor(request, controllerResult, formDataMethods).execute();
    }

    void invokeActionMethod(ClientRequest request, ControllerResult controllerResult) {
        var method = actionMethods.get(request.getAction());
        var methods = new HashSet<>(requestScopeMethods.values());
        methods.add(method);
        if (method == null) {
            throw new RuntimeException("No action-method found for action " + request.getAction());
        }
        new ActionMethodChainExecutor(request, controllerResult, methods).execute();
    }

    Class<?> getControllerClass() {
        return controller.getClass();
    }

    private List<String> getRequestScopeParameterKeys(ControllerMethod controllerMethod) {
        return Arrays.stream(controllerMethod.getMethod().getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(RequestScope.class))
                .map(parameter -> parameter.getAnnotation(RequestScope.class).value())
                .toList();
    }

    /**
     * @param validatorMessages
     * @return a RuntimeException with a message containing all validation errors
     */
    private RuntimeException exceptionForValidationErrors(ValidatorMessages validatorMessages) {
        // TODO
        return new RuntimeException("Errors occurred: ");

    }

    @RequiredArgsConstructor
    abstract class MethodChainExecutor {

        private final ClientRequest request;
        private final ControllerResult controllerResult;
        private final Set<ControllerMethod> invoked = new HashSet<>();
        private final Collection<ControllerMethod> methods;

        public void execute() {
            // Continue to invoke until no more missing keys can be filled
            boolean progress;
            do {
                progress = false;
                for (ControllerMethod method : methods) {
                    if (invoked.contains(method)) {
                        continue;
                    }
                    // Retrieve required request scope keys for the method.
                    List<String> requiredKeys = getRequestScopeParameterKeys(method);
                    // Check if all required keys are already present.
                    if (controllerResult.getRequestScope().keySet().containsAll(requiredKeys)) {
                        try {
                            doInvoke(request, controllerResult, method);
                            invoked.add(method);
                            progress = true;
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to invoke model-method: " + method, e);
                        }
                    }
                }
            } while (progress); // Stop when no new method could be invoked
        }

        abstract void doInvoke(ClientRequest request, ControllerResult controllerResult, ControllerMethod method);
    }

    class ModelDataMethodChainExecutor extends MethodChainExecutor {

        public ModelDataMethodChainExecutor(ClientRequest request, ControllerResult controllerResult, Collection<ControllerMethod> modelMethods) {
            super(request, controllerResult, modelMethods);
        }

        @Override
        void doInvoke(ClientRequest request, ControllerResult controllerResult, ControllerMethod method) {
            try {
                var controllerMethodResult = method.invoke(request, controller, controllerResult.getRequestScope());
                if (controllerMethodResult.isValidationFailed()) {
                    // these validation errors are unexpected, so we throw an exception
                    throw exceptionForValidationErrors(controllerMethodResult.getValidatorMessages());
                }
                controllerResultMapper.mapMethodResultToControllerResult(controllerMethodResult, controllerResult);
            } catch (Exception e) {
                Logger.error(e, "Failed to invoke model-method");
                throw new RuntimeException("Failed to invoke model-method " + method, e);
            }
        }
    }

    class FormDataMethodChainExecutor extends MethodChainExecutor {

        public FormDataMethodChainExecutor(ClientRequest request, ControllerResult controllerResult, Collection<ControllerMethod> modelMethods) {
            super(request, controllerResult, modelMethods);
        }

        @Override
        void doInvoke(ClientRequest request, ControllerResult controllerResult, ControllerMethod method) {
            try {
                var controllerMethodResult = method.invoke(request, controller, controllerResult.getRequestScope());
                if (controllerMethodResult.isValidationFailed()) {
                    // these validation errors are unexpected, so we throw an exception
                    throw exceptionForValidationErrors(controllerMethodResult.getValidatorMessages());
                }
                controllerResultMapper.mapMethodResultToControllerResult(controllerMethodResult, controllerResult);
            } catch (Exception e) {
                Logger.error(e, "Failed to invoke model-method");
                throw new RuntimeException("Failed to invoke model-method " + method, e);
            }
        }
    }

    class ActionMethodChainExecutor extends MethodChainExecutor {

        public ActionMethodChainExecutor(ClientRequest request, ControllerResult controllerResult, Collection<ControllerMethod> methods) {
            super(request, controllerResult, methods);
        }

        @Override
        void doInvoke(ClientRequest request, ControllerResult controllerResult, ControllerMethod method) {
            if (method == null) {
                throw new RuntimeException("No action-method found for action " + request.getAction());
            }
            try {
                var controllerMethodResult = method.invoke(request, controller, controllerResult.getRequestScope());
                controllerResultMapper.mapMethodResultToControllerResult(controllerMethodResult, controllerResult);
            } catch (Exception e) {
                Logger.error(e, "Failed to invoke action-method");
                throw new RuntimeException("Failed to invoke action-method: " + method, e);
            }
        }
    }

}
