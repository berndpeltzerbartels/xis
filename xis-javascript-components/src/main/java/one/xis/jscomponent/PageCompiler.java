package one.xis.jscomponent;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.JSScript;
import one.xis.js.JavascriptParser;
import one.xis.resource.ResourceFile;
import one.xis.template.PageTemplateModel;
import one.xis.template.TemplateParser;
import org.w3c.dom.Document;

@XISComponent
@RequiredArgsConstructor
class PageCompiler {

    private final TemplateParser templateParser;
    private final JavascriptParser javascriptParser;

    String compile(@NonNull String pageClass, ResourceFile htmlTemplate) {
        PageTemplateModel templateModel = parseWidgetTemplate(pageClass, JavasscriptComponentUtils.htmlToDocument(pageClass, htmlTemplate.getContent()));
        JSScript script = templateModelToScriptModel(templateModel);
        return JavasscriptComponentUtils.javaScriptModelAsCode(script);
    }

    private PageTemplateModel parseWidgetTemplate(String pageClassName, Document document) {
        return templateParser.parsePageTemplate(document, pageClassName);
    }

    private JSScript templateModelToScriptModel(PageTemplateModel templateModel) {
        return javascriptParser.parseTemplateModel(templateModel);
    }

}
