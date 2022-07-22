package one.xis.jsc;

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
class PageJavascriptCompiler {

    private final TemplateParser templateParser;

    String compile(@NonNull String pageClass, ResourceFile htmlTemplate, String javascriptClassName) {
        PageTemplateModel templateModel = parseWidgetTemplate(pageClass, JavascriptComponentUtils.htmlToDocument(pageClass, htmlTemplate.getContent()));
        JSScript script = templateModelToScriptModel(templateModel, javascriptClassName);
        return JavascriptComponentUtils.javaScriptModelAsCode(script);
    }

    private PageTemplateModel parseWidgetTemplate(String key, Document document) {
        return templateParser.parsePageTemplate(document, key);
    }

    private JSScript templateModelToScriptModel(PageTemplateModel templateModel, String javascriptClassName) {
        JavascriptParser parser = new JavascriptParser();
        parser.parseTemplateModel(templateModel, javascriptClassName);
        return parser.getScript();
    }

}
