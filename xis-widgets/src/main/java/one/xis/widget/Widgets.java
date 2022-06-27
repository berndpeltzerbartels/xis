package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@XISComponent
@RequiredArgsConstructor
public class Widgets {

    private final WidgetCompiler widgetCompiler;
    private final Map<String, Widget> widgets = new ConcurrentHashMap<>();
    private final ResourceFiles resourceFiles;

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public Widget getWidget(String widgetClass) {
        final Widget widget = widgets.computeIfAbsent(widgetClass, this::createWidget);
        synchronized (widget) {
            if (widget.getJavascript() == null) {
                compileWidget(widget);
            } else if (widget.isObsolete()) {
                widget.reloadHtml();
                compileWidget(widget);
            }
        }
        return widget;
    }

    private Widget createWidget(String widgetClass) {
        return new Widget(widgetClass, getHtmlResourceFile(widgetClass));
    }
    
    private ResourceFile getHtmlResourceFile(String widgetClass) {
        return resourceFiles.getByPath(getHtmlSrcPath(widgetClass));
    }

    private String getHtmlSrcPath(String widgetClass) {
        return widgetClass.replace('.', '/') + ".html";
    }

    private void compileWidget(Widget widget) {
        widget.setJavascript(widgetCompiler.compile(widget.getHtmlSrc()));
    }
}
