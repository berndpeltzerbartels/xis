package one.xis.remote.processor;

import com.google.auto.service.AutoService;
import one.xis.template.TemplateModel;
import one.xis.template.TemplateParser;
import one.xis.template.TemplateSynthaxException;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.remote.Page", "one.xis.remote.Widget", "one.xis.remote.ClientState"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class TemplateProcessor extends AnnotationProcessor {

    private final TemplateParser templateParser = new TemplateParser();
    private TemplateContextFactory templateContextFactory;
    private TypeElement clientState;
    private Collection<TemplateContext> templateContexts;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.templateContextFactory = new TemplateContextFactory(processingEnv);
        this.templateContexts = new HashSet<>();
    }

    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
        if (isClientStateAnnotation(annotation)) {
            doProcessClientState(element, annotation, roundEnv);
        } else {
            doProcessTemplate(element, annotation, roundEnv);
        }
    }

    private boolean isClientStateAnnotation(TypeElement annotation) {
        return annotation.getQualifiedName().toString().equals("one.xis.remote.ClientState");
    }

    private void doProcessTemplate(Element element, TypeElement annotation, RoundEnvironment roundEnv) throws Exception {
        templateContexts.add(templateContextFactory.templateContext((TypeElement) element));
    }

    private void doProcessClientState(Element element, TypeElement annotation, RoundEnvironment roundEnv) throws Exception {

    }


    @Override
    public void finish() throws Exception {
        writeJavaScript();
    }


    private void writeJavaScript() throws Exception {
        Appendable writer = processorUtils.writer("/public/resources/xis-remote.js");
        try {
            writeStateVariables(writer);
            for (TemplateContext context : templateContexts) {
                writeJavaScript(context, writer);
            }
        } finally {
            close(writer);
        }
    }

    private void close(Appendable appendable) throws IOException {
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        }
    }

    private void writeJavaScript(TemplateContext context, Appendable writer) throws Exception {
        writeJavaScript(templateModel(context), writer);
    }

    private void writeJavaScript(TemplateModel model, Appendable writer) {

    }


    private void writeStateVariables(Appendable writer) {

    }

    private TemplateModel templateModel(TemplateContext context) throws IOException, SAXException, TemplateSynthaxException {
        Document document = XmlUtil.loadDocument(context.getHtmlFile());
        return templateParser.parse(document);
    }
}
