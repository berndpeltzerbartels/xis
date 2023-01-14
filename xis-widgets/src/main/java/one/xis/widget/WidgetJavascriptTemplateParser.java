package one.xis.widget;

import one.xis.context.XISComponent;
import one.xis.template.WidgetTemplateModel;
import one.xis.test.js.*;

import static one.xis.test.js.JavascriptAbstractClasses.XIS_WIDGET;

@XISComponent
class WidgetJavascriptTemplateParser extends JavascriptTemplateParser<WidgetTemplateModel> {

    @Override
    public JSClass parseTemplateModel(WidgetTemplateModel widgetTemplateModel, String javascriptClassName, JSScript script) {
        return toClass(widgetTemplateModel, javascriptClassName, script);
    }

    private JSClass toClass(WidgetTemplateModel widgetTemplateModel, String javascriptClassName, JSScript script) {
        var widgetClass = derrivedClass(javascriptClassName, XIS_WIDGET, script);
        var widgetRootClass = toClass(widgetTemplateModel.getRootNode(), script);
        widgetClass.addField("children", new JSArray(new JSContructorCall(widgetRootClass, "this")));
        widgetClass.addField("id", new JSString(widgetTemplateModel.getWidgetJavascriptClassName()));
        widgetClass.addField("server", new JSString("")); // empty = this server TODO method parameter
        return widgetClass;
    }
}
