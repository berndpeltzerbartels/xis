package one.xis.remote.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;


public abstract class AnnotationProcessor extends AbstractProcessor {

    protected ProcessorUtils processorUtils;
    protected JavaModelUtils javaModelUtils;
    protected ProcessorLogger logger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processorUtils = new ProcessorUtils(processingEnv);
        javaModelUtils = new JavaModelUtils(processingEnv);
        logger = new ProcessorLogger(processingEnv, this);
        logger.info("init");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                finish();
                logger.info("processing over");
            } else {
                logger.info("process");
                for (TypeElement annotation : annotations) {
                    for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                        if (javaModelUtils.hasAnnotation(element, annotation)) {
                            doProcess(element, annotation, roundEnv);
                        }
                    }
                }
            }
        } catch (ValidationException e) {
            logger.error(e);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return true;
    }

    public abstract void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) throws Exception;

    public abstract void finish() throws Exception;
}


