package one.xis.remote.js2;

import one.xis.template1.TemplateModel;

import java.util.List;

import static one.xis.remote.js2.DefaultFunctions.clearChildNodes;
import static one.xis.remote.js2.DefaultFunctions.refreshChildren;
import static one.xis.remote.js2.JSCodeUtil.asJsArray;

class JSWidgetParser extends JSTreeParser<TemplateModel> {

    private final String widgetName;

    public JSWidgetParser(JSScript script, String widgetName) {
        super(script);
        this.widgetName = widgetName;
    }

    @Override
    protected JSObjectDeclaration parse(TemplateModel element, List<String> childNames) {
        JSObjectDeclaration widget = new JSObjectDeclaration(widgetName);

        JSFieldDeclaration valuesField = widget.addField("values");
        JSFieldDeclaration elementField = widget.addField("element");

        widget.addMethod("getElement").addStatement(new JSReturn(elementField));
        widget.addMethod("getValue", "name").addStatement(new JSReturn(new JSArrayElement(valuesField, "name")));
        widget.addMethod("refresh", "parent")
                .addStatement(new JSCode2(elementField, "=", "parent"))
                .addStatement(new JSFunctionCall(clearChildNodes))
                .addStatement(new JSFunctionCall(refreshChildren, "this", asJsArray(childNames)));
        return widget;
    }
}
