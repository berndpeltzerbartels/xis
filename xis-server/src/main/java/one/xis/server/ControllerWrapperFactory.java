package one.xis.server;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.*;
import one.xis.context.XISComponent;
import one.xis.resource.Resources;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
class ControllerWrapperFactory {

    private final Resources resources;

    ControllerWrapper createController(@NonNull String id, @NonNull Object controller) {
        var controllerWrapper = new ControllerWrapper();
        controllerWrapper.setId(id);
        controllerWrapper.setController(controller);
        controllerWrapper.setModelMethods(modelMethods(controller));
        controllerWrapper.setModelTimestampMethods(modelTimestampMethods(controller));
        controllerWrapper.setActionMethods(actionMethodMap(controller));
        controllerWrapper.setControllerClass(controller.getClass());
        return controllerWrapper;
    }

    private Map<String, ModelMethod> modelMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Model.class))
                .map(this::createModelMethod)
                .collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Map<String, ModelTimestampMethod> modelTimestampMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(ModelTimestamp.class))
                .map(this::createModelTimestampMethod)
                .collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Map<String, ActionMethod> actionMethodMap(Object controller) {
        return actionMethods(controller).collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Stream<ActionMethod> actionMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Action.class))
                .map(this::createActionMethod);
    }

    private ModelMethod createModelMethod(Method method) {
        return ModelMethod.builder()
                .method(method)
                .key(method.getAnnotation(Model.class).value())
                .methodParameters(createParameters(method))
                .build();
    }

    private ModelTimestampMethod createModelTimestampMethod(Method method) {
        return ModelTimestampMethod.builder()
                .method(method)
                .key(method.getAnnotation(ModelTimestamp.class).value())
                .methodParameters(createParameters(method))
                .build();
    }

    private ActionMethod createActionMethod(Method method) {
        return ActionMethod.builder()
                .method(method)
                .key(method.getAnnotation(Action.class).value())
                .methodParameters(createParameters(method))
                .build();
    }

    private List<MethodParameter> createParameters(Method method) {
        return Arrays.stream(method.getParameters()).map(this::createParameter).collect(Collectors.toList());
    }

    private MethodParameter createParameter(Parameter parameter) {
        var methodParameter = new MethodParameter();
        if (parameter.isAnnotationPresent(Model.class)) {
            methodParameter.setParameterType(ParameterType.MODEL);
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            methodParameter.setParameterType(ParameterType.CLIENT_ID);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            methodParameter.setParameterType(ParameterType.USER_ID);
        } else {
            throw new IllegalStateException("No annotation: " + parameter);
        }
        return methodParameter;
    }
}
