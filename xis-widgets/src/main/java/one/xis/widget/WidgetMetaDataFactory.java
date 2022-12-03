package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerUtils;
import one.xis.resource.Resources;

@XISComponent
@RequiredArgsConstructor
class WidgetMetaDataFactory {

    private final Resources resources;
    private static int nameIndex;

    WidgetMetaData createMetaData(Object controller) {
        return WidgetMetaData.builder()
                .htmlTemplate(resources.getByPath(getHtmlTemplatePath(controller)))
                .javascriptClassname(uniqueJavascriptClassName())
                .controllerClass(controllerClass(controller))
                .key(ControllerUtils.getWidgetControllerId(controllerClass(controller)))
                .build();
    }

    public String getHtmlTemplatePath(Object controller) {
        return controller.getClass().getName().replace('.', '/') + ".html";
    }

    private String uniqueJavascriptClassName() {
        return "W" + nameIndex++;
    }

    private Class<?> controllerClass(Object controller) {
        return controller.getClass(); // TODO Proxies
    }
}
