package one.xis.remote.js2;

import one.xis.template.Expression;
import one.xis.template.StaticText;
import one.xis.template.TextElement;
import one.xis.template.XmlElement;

import java.util.ArrayList;
import java.util.List;

import static one.xis.remote.js2.DefaultFunctions.*;
import static one.xis.remote.js2.JSCodeUtil.asJsArray;
import static one.xis.remote.js2.UniqueNameProvider.nextName;

class JSXmlElementParser extends JSTreeParser<XmlElement> {

    private static final JSExpressionParser EXPR_PARSER = new JSExpressionParser();

    public JSXmlElementParser(JSScript script) {
        super(script);
    }

    @Override
    protected JSObjectDeclaration parse(XmlElement element, List<String> childNames) {
        JSObjectDeclaration widget = new JSObjectDeclaration(nextName());

        JSFieldDeclaration parentField = widget.addField("parent");
        JSFieldDeclaration valuesField = widget.addField("values");
        JSFieldDeclaration elementField = widget.addField("element");

        JSMethodDeclaration getValue = widget.addMethod("getValue", "name").addStatement(new JSReturn(new JSArrayElement(valuesField, "name")));
        JSMethodDeclaration evalAttrs = widget.addMethod("evalAttr").addStatements(createEvalAttrsStatements(element, getValue));
        widget.addMethod("getElement").addStatement(new JSReturn(elementField));

        widget.addMethod("refresh", "parent")
                .addStatement(new JSCode2(parentField, "=", "parent"))
                .addStatement(new JSCode2(elementField, "=", createAppendElement(element, parentField, evalAttrs)))
                .addStatement(new JSFunctionCall(clearChildNodes))
                .addStatement(new JSFunctionCall(refreshChildren, "this", asJsArray(childNames)));
        return widget;
    }

    private JSFunctionCall createAppendElement(XmlElement element, JSFieldDeclaration parentField, JSMethodDeclaration evalAttrs) {
        List<Object> parameters = new ArrayList<>();
        parameters.add(new JSCode2(parentField.getRef(), ".getElement()"));
        parameters.add(new JSString(element.getTagName()));
        parameters.add(new JSMethodCall(evalAttrs));
        return new JSFunctionCall(appendElement, parameters);
    }

    private List<JSStatement> createEvalAttrsStatements(XmlElement element, JSMethodDeclaration getValue) {
        List<JSStatement> statements = new ArrayList<>();
        statements.add(new JSCode2("var rv=[]"));
        for (String attrName : element.getAttributes().keySet()) {
            JSArrayElement arrayElement = new JSArrayElement("rv", new JSString(attrName));
            statements.add(new JSCode2(arrayElement, "=[]"));
            for (TextElement textElement : element.getAttributes().get(attrName).getTextElements()) {
                if (textElement instanceof StaticText) {
                    ((StaticText) textElement).getLines().forEach(line -> statements.add(new JSCode2(arrayElement, "+=", new JSString(line))));
                } else if (textElement instanceof Expression) {
                    Expression expression = (Expression) textElement;
                    statements.add(EXPR_PARSER.parse(expression, getValue));
                }
            }
        }
        statements.add(new JSReturn("rv"));
        return statements;
    }
}
