package one.xis.widget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class WidgetComponents {

    @Getter
    private final Map<String, WidgetComponent> widgetComponentsByKey = new HashMap<>();

    private final WidgetComponentCompiler widgetComponentCompiler;

    private WidgetComponent createWidgetJavascript(WidgetMetaData widgetMetaData) {
        var widgetJavascript = WidgetComponent.builder()
                .javascriptClass(widgetMetaData.getJavascriptClassname())
                .controllerClass(widgetMetaData.getControllerClass())
                .htmlResource(widgetMetaData.getHtmlTemplate())
                .javascriptClass(widgetMetaData.getJavascriptClassname())
                .build();
        widgetComponentCompiler.compile(widgetJavascript);
        return widgetJavascript;
    }

    WidgetComponent add(WidgetMetaData widgetMetaData) {
        var widgetJavascript = createWidgetJavascript(widgetMetaData);
        if (widgetComponentsByKey.containsKey(widgetMetaData.getKey())) {
            throw new IllegalStateException("there is more than one widget with key " + widgetMetaData.getKey());
        }
        widgetComponentsByKey.put(widgetMetaData.getKey(), widgetJavascript);
        return widgetJavascript;
    }

    WidgetComponent getByKey(String key) {
        var widgetJavascript = widgetComponentsByKey.get(key);
        synchronized (widgetJavascript) {
            widgetComponentCompiler.compileIfObsolete(widgetJavascript);
        }
        return widgetJavascript;
    }

    Collection<String> getJsClassnames() {
        return widgetComponentsByKey.keySet();
    }

}
