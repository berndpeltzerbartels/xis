package one.xis.server;


import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerFactory descriptorFactory;

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    private Collection<Controller> controllers;

    @Getter
    private ComponentConfig componentConfig;

    @XISInit
    void init() {
        var widgetControllers = widgetControllers();
        var pageControllers = pageControllers();
        componentConfig = componentConfig(widgetControllers, pageControllers);
        controllers = controllers(widgetControllers, pageControllers);
    }

    public Response invoke(InvocationContext context) {
        var controller = getController(context);
        switch (context.getInvocationType()) {
            case ACTION:
                return invokeActionMethod(context, controller);
            case MODEL:
                return invokeModelMethods(context, controller);
            default:
                throw new IllegalArgumentException("type: " + context.getInvocationType());
        }
    }

    private Controller getController(InvocationContext context) {
        return controllers.stream()
                .filter(controller -> controller.getId().equals(context.getControllerId()))
                .findFirst()
                .orElseThrow();
    }

    private Response invokeActionMethod(InvocationContext context, Controller controller) {
        var actionMethod = controller.getActionMethods().get(context.getKey());
        var result = actionMethod.invoke(context);
        var nextControllerOptional = controllerByResult(result);
        var nextControllerId = nextControllerOptional.map(Controller::getId).orElse(context.getControllerId());
        return nextControllerOptional.map(contr -> invokeModelMethods(context, contr)).orElse(new Response(emptyMap(), nextControllerId));
    }

    private Optional<Long> invokeModelTimestampMethod(String key, InvocationContext context, Controller controller) {
        return Optional.ofNullable(controller.getModelTimestampMethods().get(key))
                .map(modelTimestampMethod -> modelTimestampMethod.invoke(context))
                .filter(Optional::isPresent)
                .map(Optional::get);

    }

    private Response invokeModelMethods(InvocationContext context, Controller controller) {
        var data = new HashMap<String, DataItem>();
        controller.getModelMethods().forEach((key, method) -> invokeForDataItem(key, method, context, data, controller));
        return new Response(data, context.getControllerId());
    }

    private void invokeForDataItem(String key, ModelMethod modelMethod, InvocationContext context, Map<String, DataItem> result, Controller controller) {
        var timestamp = invokeModelTimestampMethod(key, context, controller);
        var requestTimestamp = context.getData().get(key).getTimestamp();
        if (timestamp.isEmpty() || timestamp.get() > requestTimestamp) {
            result.put(key, new DataItem(modelMethod.invoke(context), timestamp.orElse(System.currentTimeMillis())));
        }
    }

    private Optional<Controller> controllerByResult(Object result) {
        if (result instanceof Class) {
            return Optional.of(controllersByClass((Class<?>) result));
        } else if (result instanceof String) {
            return Optional.of(controllersById((String) result));
        }
        return Optional.empty();
    }

    private Controller controllersByClass(@NonNull Class<?> cl) {
        return controllers.stream().filter(c -> c.getControllerClass().equals(cl)).findFirst().orElseThrow();
    }

    private Controller controllersById(@NonNull String id) {
        return controllers.stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow();
    }

    private Collection<Controller> controllers(Collection<Controller> widgetControllers, Collection<Controller> pageControllers) {
        return Stream.concat(widgetControllers.stream(), pageControllers.stream()).collect(Collectors.toSet());
    }


    private ComponentConfig componentConfig(Collection<Controller> widgetControllers, Collection<Controller> pageControllers) {
        return new ComponentConfig(controllersByClass(widgetControllers), controllersByClass(pageControllers));
    }

    private Collection<Controller> widgetControllers() {
        return widgetControllers.stream()
                .map(controller -> createController(controller, this::getWidgetId))
                .collect(Collectors.toSet());
    }

    private Collection<Controller> pageControllers() {
        return pageControllers.stream()
                .map(controller -> createController(controller, this::getPagePath))
                .collect(Collectors.toSet());
    }


    private Map<String, Controller> controllersByClass(Collection<Controller> controllers) {
        return controllers.stream().collect(Collectors.toMap(Controller::getId, Function.identity()));
    }
    
    private Controller createController(Object controller, Function<Object, String> idMapper) {
        return descriptorFactory.createController(idMapper.apply(controller), controller);
    }

    private String getWidgetId(Object widgetController) {
        return widgetController.getClass().getAnnotation(Widget.class).value();
    }

    private String getPagePath(Object widgetController) {
        return widgetController.getClass().getAnnotation(Page.class).path();
    }


}
