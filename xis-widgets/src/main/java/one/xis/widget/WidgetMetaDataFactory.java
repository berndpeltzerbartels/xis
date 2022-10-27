package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerModel;
import one.xis.controller.ControllerModelFactory;
import one.xis.resource.ResourceFiles;

@XISComponent
@RequiredArgsConstructor
class WidgetMetaDataFactory {

    private final ResourceFiles resourceFiles;
    private final ControllerModelFactory controllerModelFactory;
    private static int nameIndex;

    WidgetMetaData createMetaData(Object controller) {
        return WidgetMetaData.builder()
                .id(id(controller))
                .htmlTemplate(resourceFiles.getByPath(getHtmlTemplatePath(controller)))
                .javascriptClassname(uniqueJavascriptClassName())
                .controllerModel(controllerModel(controllerClass(controller)))
                .build();
    }

    public String getHtmlTemplatePath(Object controller) {
        return controller.getClass().getName().replace('.', '/') + ".html";
    }

    private String uniqueJavascriptClassName() {
        return "W" + nameIndex++;
    }

    private String id(Object controller) {
        return pathToUrn(controllerClass(controller).getName());
    }

    private Class<?> controllerClass(Object controller) {
        return controller.getClass(); // TODO Proxies
    }

    private ControllerModel controllerModel(Class<?> controllerClass) {
        return controllerModelFactory.controllerModel(controllerClass);
    }

    private String pathToUrn(String name) {
        return "widget:" + name.replace('/', ':');
    }
}
