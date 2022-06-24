package one.xis.widget;

import one.xis.context.Comp;
import one.xis.resource.Resource;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

@Comp
public class Widgets {

    private final Map<String, Resource> widgetsJavascriptById = new HashMap<>();
    private final Reflections reflections = new Reflections();

    public Resource<String> getWidgetJs(String urn) {
        return widgetsJavascriptById.computeIfAbsent(urn, this::createWidgetJs);
    }

    private Resource<String> createWidgetJs(String urn) {
        return null;
    }
}
