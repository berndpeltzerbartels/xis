package one.xis.widget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

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
                .controllerModel(widgetMetaData.getControllerModel())
                .build();
        widgetJavascriptCompiler.compile(widgetJavascript);
        return widgetJavascript;
    }

    public WidgetJavascript add(WidgetMetaData widgetMetaData) {
        var widgetJavascript = createWidgetJavascript(widgetMetaData);
        widgetJavascripts.put(widgetMetaData.getId(), widgetJavascript);
        return widgetJavascript;
    }

    public WidgetJavascript getById(String id) {
        var widgetJavascript = widgetJavascripts.get(id);
        synchronized (widgetJavascript) {
            widgetJavascriptCompiler.compileIfObsolete(widgetJavascript);
        }
        return widgetJavascript;
    }

    Collection<String> getIds() {
        return widgetJavascripts.keySet();
    }

}
