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
import java.io.PrintWriter;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.Page"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class PageProcessor extends AnnotationProcessor {

    @Override
    void doProcess(Element e, TypeElement annotation, RoundEnvironment roundEnv) throws Exception {

        String packageName = javaModelUtils.getPackageName((TypeElement) e);
        File htmlFile = processorUtils.getFileInSourceFolder(packageName, e.getSimpleName() + ".html");
        String content = IOUtils.getContent(htmlFile, "UTF-8");

    }

    @Override
    void finish() {
        writeJavascript();
    }

    private void writeJavascript() {
        try (PrintWriter out = processorUtils.writer("test123.js")) {
            out.println("function test(){}");
        }
    }

}
