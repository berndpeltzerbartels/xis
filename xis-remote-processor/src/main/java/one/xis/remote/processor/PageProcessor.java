package one.xis.remote.processor;

import one.xis.js.JSScript;
import one.xis.js.JavascriptParser;
import one.xis.js.OldSchoolJSWriter;
import one.xis.template.WidgetModel;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

/*
@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.remote.Page"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
*/
public class PageProcessor extends AnnotationProcessor {

    private Collection<PageContext> pageContexts;
    private PageContextFactory pageContextFactory;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.pageContextFactory = new PageContextFactory(processingEnv);
        this.pageContexts = new HashSet<>();
    }

    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) {
        pageContexts.add(pageContextFactory.pageContext((TypeElement) element));
    }

    @Override
    public void finish() throws Exception {
        writePages();
    }

    private void writePages() {
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
        models.forEach(parser::parse);
        new OldSchoolJSWriter(writer).write(script);
    }

    private Collection<WidgetModel> widgetModels() {
        return null;
        //widgetContexts.stream().map(this::widgetModel).collect(Collectors.toSet());
    }

    private WidgetModel widgetModel(WidgetContext context) {
        try {
            Document document = XmlUtil.loadDocument(context.getHtmlFile());
            return null;
            //templateParser.parse(document, context.getSimpleClassName()); // TODO may be an alias is better to avoid duplicates
        } catch (Exception e) {
            throw new RuntimeException(e); //TODO caught and loggged
        }
    }
}
