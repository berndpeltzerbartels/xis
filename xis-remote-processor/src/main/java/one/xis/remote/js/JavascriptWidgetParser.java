package one.xis.remote.js;

import one.xis.template.Container;
import one.xis.template.TemplateElement;
import one.xis.template.TemplateModel;
import one.xis.utils.lang.MapperUtil;
import one.xis.utils.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static one.xis.remote.js.JavacriptRootParser.REFRESH_CHILDREN_FKT;
import static one.xis.remote.js.UniqueNameProvider.nextName;

class JavascriptWidgetParser {
    private final TemplateModel root;
    private final JSScript result;
    private final JSObject widget;
    private final JSArrayField valuesField;
    private final JSField elementField;

    JavascriptWidgetParser(TemplateModel root, JSScript result) {
        this.root = root;
        this.result = result;
        this.widget = new JSObject(nextName());
        this.valuesField = this.widget.addField(new JSArrayField("values"));
        this.elementField = this.widget.addField(new JSField("element"));
    }

    String parse() {
        JavascriptParser<TemplateElement> parser = JavascriptParser.parser(root);
        JSObject object = parser.parse(root);
        List<JSObject> children = new ArrayList<>();
        if (root instanceof Container) {
            parse((Container) root).forEach(children::add);
        } else {
            children.add(parse(root));
        }
        children.forEach(result::addObject);
        createMethods(MapperUtil.map(children, JSObject::getName));
        return widget.getName();
    }

    private JSObject parse(TemplateElement element) {
        return JavascriptParser.parser(element).parse(element);
    }

    private void createMethods(List<String> childNames) {
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

    private JSMethod createRefresh(List<String> childNames) {
        var parent = new JSParameter("parent");
        var refresh = new JSMethod("refresh", parent);
        var children = new JSVar(nextName());
        refresh.addStatement(new JSCode("this", ".", elementField.getName(), "=", parent.getName()));
        refresh.addStatement(new JSVarDeclaration(children, "[" + StringUtils.join(childNames, ",") + "]"));
        refresh.addStatement(new JSFunctionCall(REFRESH_CHILDREN_FKT, parent, children));
        return refresh;
    }

}
