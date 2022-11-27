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
    private final Map<String, WidgetComponent> widgetJavascripts = new HashMap<>();

    private final WidgetJavascriptCompiler widgetJavascriptCompiler;

    private WidgetComponent createWidgetJavascript(WidgetMetaData widgetMetaData) {
        var widgetJavascript = WidgetComponent.builder()
                .javascriptClass(widgetMetaData.getJavascriptClassname())
                .controllerClass(widgetMetaData.getControllerClass())
                .htmlResourceFile(widgetMetaData.getHtmlTemplate())
                .javascriptClass(widgetMetaData.getJavascriptClassname())
                .build();
        widgetJavascriptCompiler.compile(widgetJavascript);
        return widgetJavascript;
    }

    public WidgetComponent add(WidgetMetaData widgetMetaData) {
        var widgetJavascript = createWidgetJavascript(widgetMetaData);
        widgetJavascripts.put(widgetMetaData.getKey(), widgetJavascript);
        return widgetJavascript;
    }

    public WidgetComponent getByComponentClass(String jsClassname) {
        var widgetJavascript = widgetJavascripts.get(jsClassname);
        synchronized (widgetJavascript) {
            widgetJavascriptCompiler.compileIfObsolete(widgetJavascript);
        }
        return widgetJavascript;
    }

    Collection<String> getJsClassnames() {
        return widgetJavascripts.keySet();
    }

}
