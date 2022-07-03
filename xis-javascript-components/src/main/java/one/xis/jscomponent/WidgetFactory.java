package one.xis.jscomponent;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFiles;

@XISComponent
@RequiredArgsConstructor
class WidgetFactory {

    private final ResourceFiles resourceFiles;

    Widget createWidget(@NonNull Object widgetController) {
        return new Widget(widgetController, resourceFiles.getByPath(JavasscriptComponentUtils.getHtmlTemplatePath(widgetController.getClass())));
    }
}
