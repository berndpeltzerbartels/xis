package one.xis.jsc;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFiles;

@XISComponent
@RequiredArgsConstructor
class WidgetFactory {

    private final ResourceFiles resourceFiles;
    private int nameIndex;

    Widget createWidget(@NonNull Object widgetController) {
        if (widgetController.getClass().getName().endsWith("Widget")) {
            throw new IllegalStateException(widgetController.getClass() + ": a widget's name must end with 'Widget'");
        }
        return new Widget(widgetController, resourceFiles.getByPath(JavascriptComponentUtils.getHtmlTemplatePath(widgetController.getClass())), javascriptClass());
    }

    private String javascriptClass() {
        return "W" + nameIndex++;
    }
}
