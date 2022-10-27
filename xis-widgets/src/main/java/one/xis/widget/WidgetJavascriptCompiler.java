package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.JSClass;
import one.xis.js.JSScript;
import one.xis.js.JavascriptComponentCompiler;
import one.xis.js.JavascriptTemplateParser;
import one.xis.template.TemplateParser;
import one.xis.template.WidgetTemplateModel;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class WidgetJavascriptCompiler extends JavascriptComponentCompiler<WidgetJavascript, WidgetTemplateModel> {

    private final TemplateParser templateParser;
    private final JavascriptTemplateParser javascriptTemplateParser;

    @Override
    protected WidgetTemplateModel parseWidgetTemplate(String controllerClass, Document document) {
        return templateParser.parseWidgetTemplate(document, controllerClass);
    }

    @Override
    protected JSClass parseTemplateModelIntoScriptModel(WidgetTemplateModel templateModel, String javascriptClassName, JSScript script) {
        return javascriptTemplateParser.parseTemplateModel(templateModel, javascriptClassName, script);
    }
}
