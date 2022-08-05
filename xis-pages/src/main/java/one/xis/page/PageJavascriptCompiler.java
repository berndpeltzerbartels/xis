package one.xis.page;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.JSScript;
import one.xis.js.JavascriptComponentCompiler;
import one.xis.js.JavascriptParser;
import one.xis.template.PageTemplateModel;
import one.xis.template.TemplateParser;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class PageJavascriptCompiler extends JavascriptComponentCompiler<PageJavascript, PageTemplateModel> {

    private final TemplateParser templateParser;

    @Override
    protected PageTemplateModel parseWidgetTemplate(@NonNull String controllerClass, @NonNull Document document) {
        return templateParser.parsePageTemplate(document, controllerClass);
    }

    @Override
    protected JSScript templateModelToScriptModel(@NonNull PageTemplateModel templateModel, @NonNull String javascriptClassName) {
        var parser = new JavascriptParser();
        parser.parseTemplateModel(templateModel, javascriptClassName);
        return parser.getScript();
    }
}
