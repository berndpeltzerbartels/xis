package one.xis.server;


import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.utils.lang.CollectorUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
public class FrontendService {

    private final ControllerFactory descriptorFactory;

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    private Map<Integer, ControllerMethod> modelMethods;
    private Map<String, ControllerMethod> actionMethods;
    private Collection<Controller> controllers;

    @Getter
    private ComponentConfig componentConfig;

    @XISInit
    void init() {
        var widgetControllers = widgetControllers();
        var pageControllers = pageControllers();
        componentConfig = componentConfig(widgetControllers, pageControllers);
        modelMethods = modelMethods(widgetControllers, pageControllers);
        actionMethods = actionMethods(widgetControllers, pageControllers);
        controllers = controllers(widgetControllers, pageControllers);
    }

    public Response invoke(InvocationContext context) {
        switch (context.getInvocationType()) {
            case ACTION:
                return invokeActionMethod(context);
            case MODEL:
                return invokeModelMethod(context);
            default:
                throw new IllegalArgumentException("type: " + context.getInvocationType());
        }
    }

    private Response invokeActionMethod(InvocationContext context) {
        var id = context.getMethodIds().stream().collect(CollectorUtils.toOnlyElement());
        var controllerMethod = actionMethods.get(id);
        var result = controllerMethod.invoke(context);
        Controller controller = null;
        String next;
        var data = new HashMap<String, Object>();
        if (result instanceof Class) {
            controller = controllersByClass((Class<?>) result);
            next = controller.getId();
        } else if (result instanceof String) {
            controller = controllersById((String) result);
            next = controller.getId();
        } else {
            next = context.getControllerId();
        }
        if (controller != null) {
            data.putAll(controller.getModelMethods().stream().collect(Collectors.toMap(ControllerMethod::getKey, m -> m.invoke(context))));
        }
        return new Response(data, next);
    }

    private Response invokeModelMethod(InvocationContext context) {
        var data = new HashMap<String, Object>();
        context.getMethodIds().stream().map(modelMethods::get).forEach(m -> data.put(m.getKey(), m.invoke(context)));
        return new Response(data, context.getControllerId());
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

    private Map<Integer, ControllerMethod> modelMethods(Collection<Controller> widgetControllers, Collection<Controller> pageControllers) {
        return Stream.concat(widgetControllers.stream(), pageControllers.stream())
                .flatMap(this::allMethods)
                .filter(ControllerMethod::isModel)
                .collect(Collectors.toMap(ControllerMethod::getId, Function.identity()));
    }


    private Map<String, ControllerMethod> actionMethods(Collection<Controller> widgetControllers, Collection<Controller> pageControllers) {
        return Stream.concat(widgetControllers.stream(), pageControllers.stream())
                .flatMap(this::allMethods)
                .filter(ControllerMethod::isAction)
                .collect(Collectors.toMap(ControllerMethod::getKey, Function.identity()));
    }


    private Stream<ControllerMethod> allMethods(Controller controller) {
        return Stream.concat(controller.getActionMethods().values().stream(), controller.getModelMethods().stream());
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
