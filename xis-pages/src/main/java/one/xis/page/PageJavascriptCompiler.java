package one.xis.page;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.JSClass;
import one.xis.js.JSScript;
import one.xis.js.JavascriptComponentCompiler;
import one.xis.js.JavascriptTemplateParser;
import one.xis.template.PageTemplateModel;
import one.xis.template.TemplateParser;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class PageJavascriptCompiler extends JavascriptComponentCompiler<PageJavascript, PageTemplateModel> {

    private final TemplateParser templateParser;
    private final JavascriptTemplateParser javascriptTemplateParser;

    @Override
    protected PageTemplateModel parseWidgetTemplate(@NonNull String controllerClass, @NonNull Document document) {
        return templateParser.parsePageTemplate(document, controllerClass);
    }

    @Override
    protected JSClass parseTemplateModelIntoScriptModel(@NonNull PageTemplateModel templateModel, @NonNull String javascriptClassName, JSScript script) {
        return javascriptTemplateParser.parseTemplateModel(templateModel, javascriptClassName, script);
    }
}
