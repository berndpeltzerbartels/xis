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
        return new Widget(widgetController, resourceFiles.getByPath(JavascriptComponentUtils.getHtmlTemplatePath(widgetController.getClass())), javascriptClass());
    }

    private String javascriptClass() {
        return "w" + nameIndex++;
    }
}
