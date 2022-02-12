package one.xis.remote.js2;

import lombok.RequiredArgsConstructor;
import one.xis.template1.TemplateModel;

@RequiredArgsConstructor
class JSTemplateParser {
    private final TemplateModel templateModel;
    private final JSScript script;

    public JSObjectDeclaration parse() {
        JSWidgetParser widgetParser = new JSWidgetParser(script, templateModel.getName());
        return widgetParser.parse(templateModel);
    }


}
