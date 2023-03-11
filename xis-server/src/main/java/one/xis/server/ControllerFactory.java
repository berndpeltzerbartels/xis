package one.xis.server;

import lombok.NonNull;
import one.xis.Action;
import one.xis.ClientId;
import one.xis.Model;
import one.xis.UserId;
import one.xis.context.XISComponent;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XISComponent
class ControllerFactory {

    private static int currentId;

    Controller createController(@NonNull String id, @NonNull Object controller) {
        var controllerDescriptor = new Controller();
        controllerDescriptor.setId(id);
        controllerDescriptor.setModelMethods(modelMethodDescriptors(controller));
        controllerDescriptor.setActionMethods(actionMethodDescriptorMap(controller));
        controllerDescriptor.setControllerClass(controller.getClass());
        return controllerDescriptor;
    }

    private Collection<ControllerMethod> modelMethodDescriptors(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Model.class))
                .map(method -> createModelMethodDescriptor(controller, method))
                .collect(Collectors.toSet());
    }

    private Map<String, ControllerMethod> actionMethodDescriptorMap(Object controller) {
        return actionMethodDescriptors(controller).collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }

    private Stream<ControllerMethod> actionMethodDescriptors(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Action.class))
                .map(method -> createActionMethodDescriptor(controller, method));
    }

    private ControllerMethod createModelMethodDescriptor(Object controller, Method method) {
        return new ControllerMethod()
                .withId(++currentId)
                .withMethod(method)
                .withController(controller)
                .withKey(method.getAnnotation(Model.class).value())
                .withMethodParameters(createParameterDescriptors(method));
    }

    private ControllerMethod createActionMethodDescriptor(Object controller, Method method) {
        return new ControllerMethod()
                .withId(++currentId)
                .withMethod(method)
                .withController(controller)
                .withKey(method.getAnnotation(Action.class).value())
                .withMethodParameters(createParameterDescriptors(method));
    }

    private List<MethodParameter> createParameterDescriptors(Method method) {
        return Arrays.stream(method.getParameters()).map(this::createParameterDescriptor).collect(Collectors.toList());
    }

    private MethodParameter createParameterDescriptor(Parameter parameter) {
        var parameterDescriptor = new MethodParameter();
        if (parameter.isAnnotationPresent(Model.class)) {
            parameterDescriptor.setParameterType(ParameterType.MODEL);
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            parameterDescriptor.setParameterType(ParameterType.CLIENT_ID);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            parameterDescriptor.setParameterType(ParameterType.USER_ID);
        } else {
            throw new IllegalStateException("No annotation: " + parameter);
        }
        return parameterDescriptor;
    }
}
