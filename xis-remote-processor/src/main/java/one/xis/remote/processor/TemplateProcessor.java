package one.xis.remote.processor;

import com.google.auto.service.AutoService;
import one.xis.js.JSScriptValidator;
import one.xis.js.JSWriter;
import one.xis.js.JavascriptParser;
import one.xis.remote.Page;
import one.xis.template.PageModel;
import one.xis.template.TemplateDocumentValidator;
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
@SupportedAnnotationTypes({"one.xis.remote.Widget", "one.xis.remote.Page"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class TemplateProcessor extends AnnotationProcessor {

    private final TemplateParser templateParser = new TemplateParser();
    private final JSScriptValidator scriptValidator = new JSScriptValidator();
    private TemplateAttributesFactory templateAttributesFactory;
    private Collection<WidgetAttributes> widgetAttributes;
    private Collection<PageAttributes> pageAttributes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.templateAttributesFactory = new TemplateAttributesFactory(processingEnv);
        this.pageAttributes = new HashSet<>();
        this.widgetAttributes = new HashSet<>();
    }

    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) {
        if (isPageAnnotation(annotation)) {
            pageAttributes.add(templateAttributesFactory.pageAttributes((TypeElement) element));
        } else {
            widgetAttributes.add(templateAttributesFactory.widgetAttributes((TypeElement) element));
        }
    }

    private boolean isPageAnnotation(TypeElement annotation) {
        return annotation.getQualifiedName().toString().equals(Page.class.getName());
    }

    @Override
    public void finish() {
        writeJavaScript();
    }

    private void writeJavaScript() {
        try (PrintWriter writer = processorUtils.writer("public/resources/widgets.js")) { // TODO originating elements: all Component-classes !
            writeJavaScript(writer);
        }
    }

    private void writeJavaScript(PrintWriter writer) {
        writeJavaScript(pageModels(), widgetModels(), writer);
    }

    private void writeJavaScript(Collection<PageModel> pageModels, Collection<WidgetModel> widgetModels, PrintWriter writer) {
        JavascriptParser parser = new JavascriptParser();
        parser.parse(pageModels, widgetModels);
        scriptValidator.validate(parser.getScript());
        new JSWriter(writer).write(parser.getScript());
    }

    private Collection<WidgetModel> widgetModels() {
        return widgetAttributes.stream().map(this::widgetModel).collect(Collectors.toSet());
    }


    private Collection<PageModel> pageModels() {
        return pageAttributes.stream().map(this::pageModel).collect(Collectors.toSet());
    }


    private WidgetModel widgetModel(WidgetAttributes context) {
        try {
            Document document = XmlUtil.loadDocument(context.getHtmlFile());
            return templateParser.parseWidget(document, context.getSimpleClassName()); // TODO may be an alias is better to avoid duplicates
        } catch (Exception e) {
            throw new RuntimeException(e); //TODO caught and loggged
        }
    }

    private PageModel pageModel(PageAttributes context) {
        try {
            Document document = XmlUtil.loadDocument(context.getHtmlFile());
            new TemplateDocumentValidator(document).validatePageTemplate();
            return templateParser.parsePage(document, context.getHttpPath()); // TODO may be an alias is better to avoid duplicates
        } catch (Exception e) {
            throw new RuntimeException(e); //TODO caught and loggged
        }
    }
}
