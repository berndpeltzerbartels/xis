package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.JSScript;
import one.xis.js.JavascriptComponentCompiler;
import one.xis.js.JavascriptParser;
import one.xis.template.TemplateParser;
import one.xis.template.WidgetTemplateModel;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class WidgetJavascriptCompiler extends JavascriptComponentCompiler<WidgetJavascript, WidgetTemplateModel> {

    private final TemplateParser templateParser;

    @Override
    protected WidgetTemplateModel parseWidgetTemplate(String controllerClass, Document document) {
        return templateParser.parseWidgetTemplate(document, controllerClass);
    }

    @Override
    protected JSScript templateModelToScriptModel(WidgetTemplateModel templateModel, String javascriptClassName) {
        var parser = new JavascriptParser();
        parser.parseTemplateModel(templateModel, javascriptClassName);
        return parser.getScript();
    }
}
