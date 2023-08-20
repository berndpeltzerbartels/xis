package one.xis.server;

import one.xis.LinkAction;
import one.xis.Model;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


abstract class ComponentAttributesFactory<C extends ComponentAttributes> {

    abstract C attributes(Object controller);


    protected Map<String, Collection<String>> modelsToSubmitForAction(Object controller) {
        var map = new HashMap<String, Collection<String>>();
        MethodUtils.methods(controller).stream()
                .filter(m -> m.isAnnotationPresent(LinkAction.class))
                .forEach(method -> {
                    var action = getAction(method);
                    map.computeIfAbsent(action, a -> new HashSet<>()).addAll(getModelParameters(method));
                });
        return map;
    }

    protected Collection<String> modelsToSubmitForModel(Object controller) {
        return MethodUtils.methods(controller).stream()
                .filter(method -> method.isAnnotationPresent(Model.class))
                .map(this::getModelParameters)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private String getAction(Method method) {
        return method.getAnnotation(LinkAction.class).value();
    }

    private Collection<String> getModelParameters(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(Model.class))
                .map(parameter -> parameter.getAnnotation(Model.class))
                .map(Model::value)
                .collect(Collectors.toSet());
    }


}
