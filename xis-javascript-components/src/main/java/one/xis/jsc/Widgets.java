package one.xis.jsc;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;

@XISComponent
@RequiredArgsConstructor
class Widgets extends JavascriptComponents<Widget> {

    private final WidgetFactory widgetFactory;
    private final WidgetCompiler widgetCompiler;

    @Override
    protected String createKey(Object controller) {
        var controllerClass = controller.getClass();
        String alias = controllerClass.getAnnotation(one.xis.Widget.class).value();
        return alias.isEmpty() ? controllerClass.getSimpleName() : alias;
    }

    @Override
    protected Widget createComponent(Object controller) {
        return widgetFactory.createWidget(controller);
    }

    @Override
    protected String compile(String name, ResourceFile resourceFile, String javascriptClassName) {
        return widgetCompiler.compile(name, resourceFile, javascriptClassName);
    }

}
