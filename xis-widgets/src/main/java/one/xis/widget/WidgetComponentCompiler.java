package one.xis.widget;

import one.xis.context.XISComponent;
import one.xis.js.JavascriptComponentCompiler;
import one.xis.js.JavascriptControllerModelParser;
import one.xis.template.WidgetTemplateModel;

@XISComponent
class WidgetComponentCompiler extends JavascriptComponentCompiler<WidgetComponent, WidgetTemplateModel> {

    WidgetComponentCompiler(WidgetTemplateDocumentParser documentParser,
                            WidgetJavascriptTemplateParser scriptTemplateParser,
                            JavascriptControllerModelParser controllerModelParser) {
        super(documentParser, scriptTemplateParser, controllerModelParser);
    }

}
