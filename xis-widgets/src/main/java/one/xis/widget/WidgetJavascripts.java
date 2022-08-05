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
        var pageJavascipt = new WidgetJavascript(widgetMetaData.getHtmlTemplate(), widgetMetaData.getJavascriptClassname(), widgetMetaData.getControllerClass());
        widgetJavascriptCompiler.compile(pageJavascipt);
        return pageJavascipt;
    }

    public WidgetJavascript add(WidgetMetaData widgetMetaData) {
        WidgetJavascript widgetJavascript = createWidgetJavascript(widgetMetaData);
        widgetJavascripts.put(widgetMetaData.getId(), widgetJavascript);
        return widgetJavascript;
    }

    public WidgetJavascript getById(String id) {
        WidgetJavascript pageJavascript = widgetJavascripts.get(id);
        synchronized (pageJavascript) {
            widgetJavascriptCompiler.compileIfObsolete(pageJavascript);
        }
        return pageJavascript;
    }

    Collection<String> getIds() {
        return widgetJavascripts.keySet();
    }

}
