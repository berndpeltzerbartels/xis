package one.xis.remote.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.ClientState"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ClientStateValidationProcessor extends AnnotationProcessor {

    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) throws Exception {

    }

    @Override
    public void finish() {

    }
}