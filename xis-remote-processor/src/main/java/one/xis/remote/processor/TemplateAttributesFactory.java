package one.xis.remote.processor;

import one.xis.utils.lang.StringUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.util.Optional;

class TemplateAttributesFactory {
    private final JavaModelUtils javaModelUtils;
    private final ProcessorUtils processorUtils;

    TemplateAttributesFactory(ProcessingEnvironment environment) {
        javaModelUtils = new JavaModelUtils(environment);
        processorUtils = new ProcessorUtils(environment);
    }

    PageAttributes pageAttributes(TypeElement element) {
        return new PageAttributes(htmlFilePath(element), getHttpPath(element), htmlFile(element));
    }

    WidgetAttributes widgetAttributes(TypeElement element) {
        String className = javaModelUtils.getSimpleName(element);
        String widgetName = getWidgetNameByAnnotation(element).orElse(className);
        return new WidgetAttributes(widgetName, htmlFilePath(element), htmlFile(element));
    }

    private String htmlFilePath(TypeElement element) {
        String packageName = javaModelUtils.getPackageName(element);
        String className = javaModelUtils.getSimpleName(element);
        return String.format("%s/%s.html", packageName.replace('.', '/'), className);
    }

    private File htmlFile(TypeElement element) {
        String packageName = javaModelUtils.getPackageName(element);
        String className = javaModelUtils.getSimpleName(element);
        return processorUtils.getFileInSourceFolder(packageName, className + ".html");
    }

    private String getHttpPath(TypeElement element) {
        return javaModelUtils.getAnnotationMirror("one.xis.remote.Page", element)
                .map(mirror -> javaModelUtils.getAnnotationValue(mirror, "value"))
                .map(String.class::cast).orElseThrow();
    }

    private Optional<String> getWidgetNameByAnnotation(TypeElement element) {
        return javaModelUtils.getAnnotationMirror("one.xis.remote.Widget", element)
                .map(mirror -> javaModelUtils.getAnnotationValue(mirror, "value"))
                .map(String.class::cast).filter(StringUtils::isNotEmpty);
    }

}
