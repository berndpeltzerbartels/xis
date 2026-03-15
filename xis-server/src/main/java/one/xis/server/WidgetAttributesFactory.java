package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
@RequiredArgsConstructor
class WidgetAttributesFactory extends AttributesFactory {

    private final ComponentHostResolver hostResolver;

    WidgetAttributes attributes(Object controller) {
        var attributes = new WidgetAttributes();
        var widgetId = WidgetUtil.getId(controller);
        attributes.setId(widgetId);
        attributes.setHost(hostResolver.getWidgetHost(widgetId));
        addParameterAttributes(controller.getClass(), attributes);
        addUpdateEventKeys(controller.getClass(), attributes);
        return attributes;
    }
}
