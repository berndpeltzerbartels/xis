package one.xis.page;

import one.xis.context.XISComponent;
import one.xis.js.JavascriptComponentCompiler;
import one.xis.js.JavascriptControllerModelParser;
import one.xis.template.PageTemplateModel;

@XISComponent
class PageComponentCompiler extends JavascriptComponentCompiler<PageComponent, PageTemplateModel> {

    public PageComponentCompiler(PageTemplateDocumentParser documentParser,
                                 PageJavascriptTemplateParser templateParser,
                                 JavascriptControllerModelParser controllerModelParser) {
        super(documentParser, templateParser, controllerModelParser);
    }

}
