package one.xis.remote.processor;

import com.google.auto.service.AutoService;
import one.xis.Page;
import one.xis.utils.io.IOUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"one.xis.Widget", "one.xis.Page"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class TemplateProcessor extends AnnotationProcessor {

    private TemplateAttributesFactory templateAttributesFactory;
    private final Collection<WidgetAttributes> widgetAttributes = new HashSet<>();
    private final Collection<PageAttributes> pageAttributes = new HashSet<>();
    private final Collection<Element> originatingElements = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.templateAttributesFactory = new TemplateAttributesFactory(processingEnv);
        this.pageAttributes.clear();
        this.widgetAttributes.clear();
        this.originatingElements.clear();
    }

    @Override
    public void doProcess(Element element, TypeElement annotation, RoundEnvironment roundEnv) {
        if (isPageAnnotation(annotation)) {
            pageAttributes.add(templateAttributesFactory.pageAttributes((TypeElement) element));
        } else {
            widgetAttributes.add(templateAttributesFactory.widgetAttributes((TypeElement) element));
        }
        originatingElements.add(element);
    }

    private boolean isPageAnnotation(TypeElement annotation) {
        return annotation.getQualifiedName().toString().equals(Page.class.getName());
    }

    @Override
    public void finish() {
        //  copyFiles();
        writeMetaInf();
    }

    private void writeMetaInf() {
        writeMetaInfPages();
        writeMetaInfWidgets();
    }

    private void copyFiles() {
        copyPageFiles();
        copyWidgetFiles();
    }

    private void writeMetaInfPages() {
        try (PrintWriter writer = processorUtils.writer("META-INF/xis/pages", originatingElements)) {
            for (PageAttributes attributes : pageAttributes) {
                writer.print(attributes.getHttpPath());
                writer.print(":");
                writer.println(attributes.getHtmlFilePath());
            }
        }
    }

    private void writeMetaInfWidgets() {
        try (PrintWriter writer = processorUtils.writer("META-INF/xis/widgets", originatingElements)) {
            for (WidgetAttributes attributes : widgetAttributes) {
                writer.print(attributes.getName());
                writer.print(":");
                writer.println(attributes.getHtmlFilePath());
            }
        }
    }

    private void copyPageFiles() {
        for (PageAttributes attributes : pageAttributes) {
            try (PrintWriter writer = processorUtils.writer(attributes.getHtmlFilePath(), originatingElements)) {
                writer.print(IOUtils.getContent(attributes.getHtmlFile(), "utf-8"));
            }
        }
    }

    private void copyWidgetFiles() {
        for (WidgetAttributes attributes : widgetAttributes) {
            try (PrintWriter writer = processorUtils.writer(attributes.getHtmlFilePath(), originatingElements)) {
                writer.print(IOUtils.getContent(attributes.getHtmlFile(), "utf-8"));
            }
        }
    }
}
