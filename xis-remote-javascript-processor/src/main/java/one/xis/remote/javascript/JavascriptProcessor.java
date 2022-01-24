package one.xis.remote.javascript;

import com.google.auto.service.AutoService;
import one.xis.remote.processor.AnnotationProcessor;
import one.xis.template.TemplateParser;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.remote.ClientState"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class JavascriptProcessor extends AnnotationProcessor {

    private final TemplateParser templateParser = new TemplateParser();

    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
        
    }

    @Override
    public void finish() {

    }
}
