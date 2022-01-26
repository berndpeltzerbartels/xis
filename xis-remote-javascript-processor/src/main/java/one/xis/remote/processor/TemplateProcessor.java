package one.xis.remote.processor;

import com.google.auto.service.AutoService;
import one.xis.remote.javascript.JSAst;
import one.xis.remote.javascript.JavascriptWriter;
import one.xis.template.TemplateModel;
import one.xis.template.TemplateParser;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.remote.Page", "one.xis.remote.Widget", "one.xis.remote.ClientState"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class TemplateProcessor extends AnnotationProcessor {

    private final TemplateParser templateParser = new TemplateParser();
    private TemplateContextFactory templateContextFactory;
    private TypeElement clientState;
    private Collection<TemplateContext> templateContexts;
    private Collection<String> stateVariables = new HashSet<>(); // TODO

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
        stateVariables.addAll(javaModelUtils.getFieldNames((TypeElement) element));
    }


    @Override
    public void finish() throws Exception {
        writeJavaScript();
    }


    private void writeJavaScript() throws Exception {
        Appendable writer = processorUtils.writer("public/resources/xis-remote.js");
        try {
            writeStateVariables(writer);
            writeJavaScript(writer);

        } finally {
            close(writer);
        }
    }

    private void close(Appendable appendable) throws IOException {
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        }
    }

    private void writeJavaScript(Appendable writer) throws Exception {
        writeJavaScript(templateModels(), writer);
    }

    private void writeJavaScript(Collection<TemplateModel> models, Appendable writer) {
        writeJavaScript(new JSAstParser().parse(models, stateVariables), writer);
    }

    private void writeJavaScript(JSAst jsAst, Appendable writer) {
        new JavascriptWriter(writer).write(jsAst);
    }


    private void writeStateVariables(Appendable writer) {

    }

    private Collection<TemplateModel> templateModels() {
        return templateContexts.stream().map(this::templateModel).collect(Collectors.toSet());
    }

    private TemplateModel templateModel(TemplateContext context) {
        try {
            Document document = XmlUtil.loadDocument(context.getHtmlFile());
            return templateParser.parse(document, context.getSimpleClassName()); // TODO may be an alias is better to avoid duplicates
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
