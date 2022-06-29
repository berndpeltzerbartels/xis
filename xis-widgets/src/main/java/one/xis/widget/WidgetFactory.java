package one.xis.widget;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;

@XISComponent
@RequiredArgsConstructor
class WidgetFactory {

    private final ResourceFiles resourceFiles;

    Widget createWidget(Object widgetController) {
        return new Widget(widgetController, getHtmlTemplateResource(widgetController.getClass()));
    }

    private ResourceFile getHtmlTemplateResource(Class<?> widgetControllerClass) {
        return resourceFiles.getByPath(getHtmlTemplatePath(widgetControllerClass));
    }

    private String getHtmlTemplatePath(Class<?> widgetControllerClass) {
        return widgetControllerClass.getName().replace('.', '/') + ".html";
    }
}
