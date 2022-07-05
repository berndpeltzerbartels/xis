package one.xis.jsc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.JSScript;
import one.xis.js.JavascriptParser;
import one.xis.resource.ResourceFile;
import one.xis.template.TemplateParser;
import one.xis.template.WidgetTemplateModel;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class WidgetCompiler {

    private final TemplateParser templateParser;
    private final JavascriptParser javascriptParser;

    String compile(@NonNull String widgetClass, ResourceFile htmlTemplate) {
        WidgetTemplateModel templateModel = parseWidgetTemplate(widgetClass, JavasscriptComponentUtils.htmlToDocument(widgetClass, htmlTemplate.getContent()));
        JSScript script = templateModelToScriptModel(templateModel);
        return JavasscriptComponentUtils.javaScriptModelAsCode(script);
    }

    private WidgetTemplateModel parseWidgetTemplate(String widgetClassName, Document document) {
        return templateParser.parseWidgetTemplate(document, widgetClassName);
    }

    private JSScript templateModelToScriptModel(WidgetTemplateModel templateModel) {
        return javascriptParser.parseWidgetModel(templateModel);
    }

}
