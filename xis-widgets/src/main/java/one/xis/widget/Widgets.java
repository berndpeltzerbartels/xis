package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.ReloadableResourceFile;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@XISComponent
@RequiredArgsConstructor
public class Widgets {

    private final WidgetFactory widgetFactory;
    private final WidgetCompiler widgetCompiler;
    private Map<String, Widget> widgetMap;

    @XISInject(annotatedWith = one.xis.Widget.class)
    private Collection<Object> widgetControllers;

    @XISInit
    void createWidgets() {
        widgetMap = widgetControllers.stream()//
                .map(this::createWidget)//
                .collect(Collectors.toUnmodifiableMap(Widget::getId, Function.identity()));
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
