package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.jsc.JavascriptComponents;
import one.xis.resource.ResourceFile;

@XISComponent
@RequiredArgsConstructor
public class WidgetJavascripts extends JavascriptComponents<WidgetJavascript> {

    // TODO remove "public"
    private final WidgetFactory widgetFactory;
    private final WidgetJavascriptCompiler widgetJavascriptCompiler;

    @Override
    protected String createKey(Object controller) {
        var controllerClass = controller.getClass();
        String alias = controllerClass.getAnnotation(one.xis.Widget.class).value();
        return alias.isEmpty() ? controllerClass.getSimpleName() : alias;
    }

    @Override
    protected WidgetJavascript createComponent(Object controller) {
        return widgetFactory.createWidget(controller);
    }

    @Override
    protected String compile(String name, ResourceFile resourceFile, String javascriptClassName) {
        return widgetJavascriptCompiler.compile(name, resourceFile, javascriptClassName);
    }

}
