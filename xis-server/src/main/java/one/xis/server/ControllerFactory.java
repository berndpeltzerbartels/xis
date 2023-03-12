package one.xis.server;

import lombok.NonNull;
import one.xis.*;
import one.xis.context.XISComponent;
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
class ControllerFactory {

    ControllerWrapper createController(@NonNull String id, @NonNull Object controller) {
        var controllerModel = new ControllerWrapper();
        controllerModel.setId(id);
        controllerModel.setModelMethods(modelMethods(controller));
        controllerModel.setModelTimestampMethods(modelTimestampMethods(controller));
        controllerModel.setActionMethods(actionMethodMap(controller));
        controllerModel.setControllerClass(controller.getClass());
        return controllerModel;
    }

    private Map<String, ModelMethod> modelMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Model.class))
                .map(method -> createModelMethod(controller, method))
                .collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Map<String, ModelTimestampMethod> modelTimestampMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(ModelTimestamp.class))
                .map(method -> createModelTimestampMethod(controller, method))
                .collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Map<String, ActionMethod> actionMethodMap(Object controller) {
        return actionMethods(controller).collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Stream<ActionMethod> actionMethods(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Action.class))
                .map(method -> createActionMethod(controller, method));
    }

    private ModelMethod createModelMethod(Object controller, Method method) {
        return ModelMethod.builder()
                .method(method)
                .controller(controller)
                .key(method.getAnnotation(Model.class).value())
                .methodParameters(createParameters(method))
                .build();
    }

    private ModelTimestampMethod createModelTimestampMethod(Object controller, Method method) {
        return ModelTimestampMethod.builder()
                .method(method)
                .controller(controller)
                .key(method.getAnnotation(ModelTimestamp.class).value())
                .methodParameters(createParameters(method))
                .build();
    }

    private ActionMethod createActionMethod(Object controller, Method method) {
        return ActionMethod.builder()
                .method(method)
                .controller(controller)
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
