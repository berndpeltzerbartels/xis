package one.xis.jscomponent;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;

import static one.xis.jscomponent.JavasscriptComponentUtils.urnToClassName;

@XISComponent
@RequiredArgsConstructor
class Widgets extends JavascriptComponents<Widget> {

    private final WidgetFactory widgetFactory;
    private final WidgetCompiler widgetCompiler;

    @Override
    public Widget get(String urn) {
        return super.get(urnToClassName(urn));
    }

    @Override
    protected Widget createComponent(Object controller) {
        return widgetFactory.createWidget(controller);
    }

    @Override
    protected String compile(String name, ResourceFile resourceFile) {
        return widgetCompiler.compile(name, resourceFile);
    }
    
}
