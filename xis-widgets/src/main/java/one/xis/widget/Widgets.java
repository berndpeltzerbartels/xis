package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.InMemoryResource;
import one.xis.resource.Resource;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class Widgets {

    private final WidgetCompiler widgetCompiler;

    private final Map<String, Resource> widgets = new HashMap<>();

    public Resource getWidgetJs(String widgetClass) {
        return widgets.computeIfAbsent(widgetClass, this::createWidgetJs);
    }

    private Resource createWidgetJs(String widgetClass) {
        return new InMemoryResource(widgetCompiler.compile(widgetClass), " text/javascript;charset=UTF-8", System.currentTimeMillis());
    }
}
