package one.xis.server;

import one.xis.Action;
import one.xis.Model;
import one.xis.context.XISComponent;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@XISComponent
class ComponentAttributesFactory {

    ComponentAttributes componentAttributes(Object controller) {
        var attributes = new ComponentAttributes();
        attributes.setModelsToSubmitOnRefresh(modelsToSubmitForModel(controller));
        attributes.setModelsToSubmitOnAction(modelsToSubmitForAction(controller));
        return attributes;
    }

    private Map<String, Collection<String>> modelsToSubmitForAction(Object controller) {
        var map = new HashMap<String, Collection<String>>();
        MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(Action.class))
                .forEach(method -> {
                    var action = getAction(method);
                    map.computeIfAbsent(action, a -> new HashSet<>()).addAll(getModelParameters(method));
                });
        return map;
    }


    private String getAction(Method method) {
        return method.getAnnotation(Action.class).value();
    }

    private Collection<String> getModelParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(Model.class))
                .map(parameter -> parameter.getAnnotation(Model.class))
                .map(Model::value)
                .collect(Collectors.toSet());
    }

    private Collection<String> modelsToSubmitForModel(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(method -> method.isAnnotationPresent(Model.class))
                .map(this::getModelParameters)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
