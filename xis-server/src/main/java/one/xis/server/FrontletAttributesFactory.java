package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
@RequiredArgsConstructor
class FrontletAttributesFactory extends AttributesFactory {

    private final ComponentHostResolver hostResolver;

    FrontletAttributes attributes(Object controller) {
        var attributes = new FrontletAttributes();
        var widgetId = FrontletUtil.getId(controller);
        attributes.setId(widgetId);
        attributes.setHost(hostResolver.getWidgetHost(widgetId));
        addParameterAttributes(controller.getClass(), attributes);
        addUpdateEventKeys(controller.getClass(), attributes);
        return attributes;
    }
}
