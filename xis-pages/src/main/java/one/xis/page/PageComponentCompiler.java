package one.xis.page;

import one.xis.context.XISComponent;
import one.xis.template.PageTemplateModel;
import one.xis.test.js.JavascriptComponentCompiler;
import one.xis.test.js.JavascriptControllerModelParser;

@XISComponent
class PageComponentCompiler extends JavascriptComponentCompiler<PageComponent, PageTemplateModel> {

    public PageComponentCompiler(PageTemplateDocumentParser documentParser,
                                 PageJavascriptTemplateParser templateParser,
                                 JavascriptControllerModelParser controllerModelParser) {
        super(documentParser, templateParser, controllerModelParser);
    }

}
