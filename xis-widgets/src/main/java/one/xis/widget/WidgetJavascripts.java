package one.xis.widget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
class WidgetJavascripts {

    @Getter
    private final Map<String, WidgetJavascript> widgetJavascripts = new HashMap<>();

    private final WidgetJavascriptCompiler widgetJavascriptCompiler;

    private WidgetJavascript createWidgetJavascript(WidgetMetaData widgetMetaData) {
        var widgetJavascript = WidgetJavascript.builder()
                .javascriptClass(widgetMetaData.getJavascriptClassname())
                .controllerClass(widgetMetaData.getControllerClass())
                .htmlResourceFile(widgetMetaData.getHtmlTemplate())
                .javascriptClass(widgetMetaData.getJavascriptClassname())
                .build();
        widgetJavascriptCompiler.compile(widgetJavascript);
        return widgetJavascript;
    }

    public WidgetJavascript add(WidgetMetaData widgetMetaData) {
        var widgetJavascript = createWidgetJavascript(widgetMetaData);
        widgetJavascripts.put(widgetMetaData.getKey(), widgetJavascript);
        return widgetJavascript;
    }

    public WidgetJavascript getByControllerClass(String widgetKey) {
        var widgetJavascript = widgetJavascripts.get(widgetKey);
        synchronized (widgetJavascript) {
            widgetJavascriptCompiler.compileIfObsolete(widgetJavascript);
        }
        return widgetJavascript;
    }

    Collection<String> getIds() {
        return widgetJavascripts.keySet();
    }

}
