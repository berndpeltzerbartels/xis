package one.xis.remote.js;

import one.xis.template.TemplateElement;

import static one.xis.remote.js.JavacriptRootParser.REFRESH_CHILDREN_FKT;
import static one.xis.remote.js.UniqueNameProvider.nextName;

class JavascriptWidgetParser extends JavascriptParser {
    private final TemplateElement root;
    private final JSScript result;
    private final JSObject widget;
    private final JSArrayField valuesField;
    private final JSField elementField;

    JavascriptWidgetParser(TemplateElement root, JSScript result) {
        this.root = root;
        this.result = result;
        this.widget = new JSObject(nextName());
        this.valuesField = this.widget.addField(new JSArrayField("values"));
        this.elementField = this.widget.addField(new JSField("element"));
    }

    @Override
    public JSObject parse() {
        JavascriptParser parser = JavascriptParser.parser(root, result);
        JSObject object = parser.parse();
        result.addObject(object);
        return widget;
    }

    private void createMethods(String childNames) {
        widget.addMethod(createGetValue());
        widget.addMethod(createRefresh(childNames));
        widget.addMethod(createGetElement());
    }

    private JSMethod createGetValue() {
        var name = new JSParameter("name");
        var getValue = new JSMethod("getValue", name);
        getValue.addStatement(new JSReturnStatement(new JSArrayElement(valuesField, name)));
        return getValue;
    }

    private JSMethod createGetElement() {
        var getElement = new JSMethod("getElement");
        getElement.addStatement(new JSReturnStatement(elementField));
        return getElement;
    }

    private JSMethod createRefresh(String childName) {
        var parent = new JSParameter("parent");
        var refresh = new JSMethod("refresh", parent);
        var children = new JSVar(nextName());
        refresh.addStatement(new JSCode("this", ".", elementField.getName(), "=", parent.getName()));
        refresh.addStatement(new JSVarDeclaration(children, "[" + childName + "]"));
        refresh.addStatement(new JSFunctionCall(REFRESH_CHILDREN_FKT, parent, children));
        return refresh;
    }

}
