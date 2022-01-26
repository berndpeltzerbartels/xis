package one.xis.remote.processor;

import com.google.auto.service.AutoService;
import one.xis.utils.io.IOUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.File;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.remote.Page"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class PageProcessor extends AnnotationProcessor {
    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
        String packageName = javaModelUtils.getPackageName((TypeElement) element);
        File htmlFile = processorUtils.getFileInSourceFolder(packageName, element.getSimpleName() + ".html");
        String content = IOUtils.getContent(htmlFile, "UTF-8");
    }

    @Override
    public void finish() throws Exception {

    }
}
