package one.xis.remote.processor;

import com.google.auto.service.AutoService;
import one.xis.js.JSScript;
import one.xis.js.JavascriptParser;
import one.xis.js.OldSchoolJSWriter;
import one.xis.template.TemplateParser;
import one.xis.template.WidgetModel;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.remote.Widget"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class WidgetProcessor extends AnnotationProcessor {

    private final TemplateParser templateParser = new TemplateParser();
    private WidgetContextFactory widgetContextFactory;
    private Collection<WidgetContext> widgetContexts;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.widgetContextFactory = new WidgetContextFactory(processingEnv);
        this.widgetContexts = new HashSet<>();
    }

    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) {
        widgetContexts.add(widgetContextFactory.templateContext((TypeElement) element));
    }

    @Override
    public void finish() throws Exception {
        writeJavaScript();
    }

    private void writeJavaScript() throws Exception {
        try (PrintWriter writer = processorUtils.writer("public/resources/widgets.js")) { // TODO originating elements: all Component-classes !
            writeJavaScript(writer);
        }
    }

    private void writeJavaScript(PrintWriter writer) {
        writeJavaScript(widgetModels(), writer);
    }

    private void writeJavaScript(Collection<WidgetModel> models, PrintWriter writer) {
        JSScript script = new JSScript();
        JavascriptParser parser = new JavascriptParser(script);
        parser.parse(models);
        new OldSchoolJSWriter(writer).write(script);
    }

    private Collection<WidgetModel> widgetModels() {
        return widgetContexts.stream().map(this::widgetModel).collect(Collectors.toSet());
    }

    private WidgetModel widgetModel(WidgetContext context) {
        try {
            Document document = XmlUtil.loadDocument(context.getHtmlFile());
            return templateParser.parse(document, context.getSimpleClassName()); // TODO may be an alias is better to avoid duplicates
        } catch (Exception e) {
            throw new RuntimeException(e); //TODO caught and loggged
        }
    }
}
