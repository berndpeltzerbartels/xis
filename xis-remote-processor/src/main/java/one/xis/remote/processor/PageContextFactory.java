package one.xis.remote.processor;

import lombok.RequiredArgsConstructor;
import one.xis.remote.Page;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.File;

@RequiredArgsConstructor
class PageContextFactory {
    private final JavaModelUtils javaModelUtils;
    private final ProcessorUtils processorUtils;

    PageContextFactory(ProcessingEnvironment environment) {
        javaModelUtils = new JavaModelUtils(environment);
        processorUtils = new ProcessorUtils(environment);
    }

    PageContext pageContext(TypeElement element) {
        String urlPattern = element.getAnnotation(Page.class).value();
        String packageName = javaModelUtils.getPackageName(element);
        String className = javaModelUtils.getSimpleName(element);
        File htmlFile = processorUtils.getFileInSourceFolder(packageName, element.getSimpleName() + ".html");
        return new PageContext(urlPattern, packageName, className, htmlFile);
    }

}
