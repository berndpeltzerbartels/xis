package one.xis.remote.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.File;

class TemplateAttributesFactory {
    private final JavaModelUtils javaModelUtils;
    private final ProcessorUtils processorUtils;

    TemplateAttributesFactory(ProcessingEnvironment environment) {
        javaModelUtils = new JavaModelUtils(environment);
        processorUtils = new ProcessorUtils(environment);
    }

    PageAttributes pageAttributes(TypeElement element) {
        String packageName = javaModelUtils.getPackageName(element);
        String className = javaModelUtils.getSimpleName(element);
        File htmlFile = processorUtils.getFileInSourceFolder(packageName, element.getSimpleName() + ".html");
        return new PageAttributes(packageName, className, htmlFile, getHttpPath(element));
    }

    WidgetAttributes widgetAttributes(TypeElement element) {
        String packageName = javaModelUtils.getPackageName(element);
        String className = javaModelUtils.getSimpleName(element);
        File htmlFile = processorUtils.getFileInSourceFolder(packageName, element.getSimpleName() + ".html");
        return new WidgetAttributes(packageName, className, htmlFile);
    }

    private String getHttpPath(TypeElement element) {
        return javaModelUtils.getAnnotationMirror("one.xis.remote.Page", element)
                .map(mirror -> javaModelUtils.getAnnotationValue(mirror, "value"))
                .map(String.class::cast).orElseThrow();
    }

}
