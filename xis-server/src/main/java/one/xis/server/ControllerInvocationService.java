package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static one.xis.server.ControllerType.PAGE;
import static one.xis.server.ControllerType.WIDGET;

@XISComponent
@RequiredArgsConstructor
class ControllerInvocationService {

    private final ControllerService controllerService;

    private Collection<ControllerWrapper> widgetControllerWrappers;
    private Collection<ControllerWrapper> pageControllerWrappers;

    @XISInit
    void init() {
        widgetControllerWrappers = controllerService.getWidgetControllerWrappers();
        pageControllerWrappers = controllerService.getPageControllerWrappers();
    }

    public Response invokeModelMethods(Request request) {
        if (request.getControllerType() == PAGE) {
            return invokePageModelMethods(request);
        } else if (request.getControllerType() == WIDGET) {
            return invokeWidgetModelMethods(request);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Response invokeActionMethod(Request request) {
        if (request.getControllerType() == PAGE) {
            return invokePageActionMethod(request);
        } else if (request.getControllerType() == WIDGET) {
            return invokeWidgetActionMethod(request);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Response invokePageModelMethods(Request request) {
        return invokeModelMethods(request, findPageController(request));
    }

    private Response invokeWidgetModelMethods(Request request) {
        return invokeModelMethods(request, findWidgetController(request));
    }

    private Response invokePageActionMethod(Request request) {
        return invokeActionMethod(request, findPageController(request));
    }

    private Response invokeWidgetActionMethod(Request request) {
        return invokeActionMethod(request, findWidgetController(request));
    }

    private Response invokeModelMethods(Request request, ControllerWrapper controllerWrapper) {
        var data = new HashMap<String, DataItem>();
        controllerWrapper.getModelMethods().forEach((key, method) -> invokeForDataItem(key, method, request, data, controllerWrapper));
        return new Response(data, controllerWrapper.getId());
    }

    private Response invokeActionMethod(Request request, ControllerWrapper controllerWrapper) {
        var method = controllerWrapper.getActionMethods().get(request.getKey());
        var result = method.invoke(request);
        Optional<ControllerWrapper> nextController;
        if (request.getControllerType() == PAGE) {
            nextController = controllerService.pageControllerWrapperByResult(result);
        } else if (request.getControllerType() == WIDGET) {
            nextController = controllerService.widgetControllerWrapperByResult(result);
        } else {
            throw new IllegalArgumentException();
        }
        return nextController.map(contr -> invokeModelMethods(request, contr))
                .orElse(new Response(emptyMap(), request.getControllerId()));
    }

    private Optional<Long> invokeModelTimestampMethod(String key, Request request, ControllerWrapper controllerWrapper) {
        return Optional.ofNullable(controllerWrapper.getModelTimestampMethods().get(key))
                .map(modelTimestampMethod -> modelTimestampMethod.invoke(request))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private void invokeForDataItem(String key, ModelMethod modelMethod, Request request, Map<String, DataItem> result, ControllerWrapper controllerWrapper) {
        var timestamp = invokeModelTimestampMethod(key, request, controllerWrapper);
        var requestTimestamp = request.getData().get(key).getTimestamp();
        if (timestamp.isEmpty() || timestamp.get() > requestTimestamp) {
            result.put(key, new DataItem(modelMethod.invoke(request), timestamp.orElse(System.currentTimeMillis())));
        }
    }

    private ControllerWrapper findPageController(Request request) {
        return pageControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(request.getControllerId()))
                .findFirst()
                .orElseThrow();
    }

    private ControllerWrapper findWidgetController(Request request) {
        return widgetControllerWrappers.stream()
                .filter(controller -> controller.getId().equals(request.getControllerId()))
                .findFirst()
                .orElseThrow();
    }
}
