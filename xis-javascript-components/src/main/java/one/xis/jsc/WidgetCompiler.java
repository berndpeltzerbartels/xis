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

    String compile(@NonNull String widgetClass, ResourceFile htmlTemplate, String javascriptClass) {
        WidgetTemplateModel templateModel = parseWidgetTemplate(widgetClass, JavascriptComponentUtils.htmlToDocument(widgetClass, htmlTemplate.getContent()));
        JSScript script = templateModelToScriptModel(templateModel, javascriptClass);
        return JavascriptComponentUtils.javaScriptModelAsCode(script);
    }

    private WidgetTemplateModel parseWidgetTemplate(String widgetClassName, Document document) {
        return templateParser.parseWidgetTemplate(document, widgetClassName);
    }

    private JSScript templateModelToScriptModel(WidgetTemplateModel templateModel, String javascriptClass) {
        JavascriptParser parser = new JavascriptParser();
        parser.parseTemplateModel(templateModel, javascriptClass);
        return parser.getScript();
    }

}
