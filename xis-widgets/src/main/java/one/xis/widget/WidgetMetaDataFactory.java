package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFiles;

@XISComponent
@RequiredArgsConstructor
class WidgetMetaDataFactory {

    private final ResourceFiles resourceFiles;
    private static int nameIndex;

    WidgetMetaData createMetaData(Object controller) {
        return WidgetMetaData.builder()
                .id(id(controller))
                .htmlTemplate(resourceFiles.getByPath(getHtmlTemplatePath(controller)))
                .javascriptClassname(javascriptClass())
                .controllerClass(controllerClass(controller))
                .build();
    }

    public String getHtmlTemplatePath(Object controller) {
        return controller.getClass().getName().replace('.', '/') + ".html";
    }

    private String javascriptClass() {
        return "P" + nameIndex++;
    }

    private String id(Object controller) {
        return pathToUrn(controllerClass(controller));
    }

    private String controllerClass(Object controller) {
        return controller.getClass().getName(); // TODO Proxies
    }

    private String pathToUrn(String name) {
        return "widget:" + name.replace('/', ':');
    }
}
