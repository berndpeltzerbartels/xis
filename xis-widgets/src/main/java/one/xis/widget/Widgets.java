package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ReloadableResourceFile;

import java.util.HashMap;
import java.util.Map;

@XISComponent
@RequiredArgsConstructor
public class Widgets {

    private final WidgetFactory widgetFactory;
    private final WidgetCompiler widgetCompiler;
    private final Map<String, Widget> widgetMap = new HashMap<>();

    public void addWidget(String widgetId, Object widgetController) {
        widgetMap.put(widgetId, createWidget(widgetController));
    }

    public Widget getWidget(String widgetId) {
        Widget widget = widgetMap.get(widgetId);
        if (widget == null) {
            throw new IllegalStateException("no such widget: " + widgetId);
        }
        return compileIfObsolete(widget);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private Widget compileIfObsolete(Widget widget) {
        synchronized (widget) {
            if (isObsolete(widget)) {
                reloadHtml(widget);
                compileWidget(widget);
            }
            return widget;
        }
    }

    private Widget createWidget(Object widgetController) {
        return compileWidget(widgetFactory.createWidget(widgetController));
    }

    private Widget compileWidget(Widget widget) {
        widget.setJavascript(widgetCompiler.compile(widget.getHtmlTemplate()));
        return widget;
    }

    private void reloadHtml(Widget widget) {
        if (widget.getHtmlResourceFile() instanceof ReloadableResourceFile) {
            ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) widget.getHtmlResourceFile();
            reloadableResourceFile.reload();
        } else {
            throw new IllegalStateException();
        }
    }

    private boolean isObsolete(Widget widget) {
        if (widget.getHtmlResourceFile() instanceof ReloadableResourceFile) {
            ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) widget.getHtmlResourceFile();
            return reloadableResourceFile.isObsolete();
        }
        return false;
    }
}
