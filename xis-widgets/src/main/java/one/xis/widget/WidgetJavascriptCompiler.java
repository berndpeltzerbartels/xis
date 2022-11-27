package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.*;
import one.xis.template.TemplateParser;
import one.xis.template.WidgetTemplateModel;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class WidgetJavascriptCompiler extends JavascriptComponentCompiler<WidgetComponent, WidgetTemplateModel> {

    private final TemplateParser templateParser;
    private final JavascriptTemplateParser javascriptTemplateParser;
    private final JavascriptControllerModelParser controllerModelParser;

    @Override
    protected WidgetTemplateModel parseTemplate(String controllerClass, Document document) {
        return templateParser.parseWidgetTemplate(document, controllerClass);
    }

    @Override
    protected JSClass parseTemplateModelIntoScriptModel(WidgetTemplateModel templateModel, String javascriptClassName, JSScript script) {
        return javascriptTemplateParser.parseTemplateModel(templateModel, javascriptClassName, script);
    }

    @Override
    protected void addMessageAttributes(JSClass component, Class<?> controllerClass) {
        controllerModelParser.parseControllerModel(controllerClass, component);
    }
}
