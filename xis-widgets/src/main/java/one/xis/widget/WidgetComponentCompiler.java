package one.xis.widget;

import one.xis.context.XISComponent;
import one.xis.template.WidgetTemplateModel;
import one.xis.test.js.JavascriptComponentCompiler;
import one.xis.test.js.JavascriptControllerModelParser;

@XISComponent
class WidgetComponentCompiler extends JavascriptComponentCompiler<WidgetComponent, WidgetTemplateModel> {

    WidgetComponentCompiler(WidgetTemplateDocumentParser documentParser,
                            WidgetJavascriptTemplateParser scriptTemplateParser,
                            JavascriptControllerModelParser controllerModelParser) {
        super(documentParser, scriptTemplateParser, controllerModelParser);
    }

}
