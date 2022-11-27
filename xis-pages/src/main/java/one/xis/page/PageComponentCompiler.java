package one.xis.page;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.*;
import one.xis.template.PageTemplateModel;
import one.xis.template.TemplateParser;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class PageComponentCompiler extends JavascriptComponentCompiler<PageComponent, PageTemplateModel> {

    private final TemplateParser templateParser;
    private final JavascriptTemplateParser javascriptTemplateParser;
    private final JavascriptControllerModelParser controllerModelParser;

    @Override
    protected PageTemplateModel parseTemplate(@NonNull String controllerClass, @NonNull Document document) {
        return templateParser.parsePageTemplate(document, controllerClass);
    }

    @Override
    protected JSClass parseTemplateModelIntoScriptModel(@NonNull PageTemplateModel templateModel, @NonNull String javascriptClassName, @NonNull JSScript script) {
        return javascriptTemplateParser.parseTemplateModel(templateModel, javascriptClassName, script);
    }


    @Override
    protected void addMessageAttributes(JSClass component, Class<?> controllerClass) {
        controllerModelParser.parseControllerModel(controllerClass, component);
    }
}
