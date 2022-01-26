package one.xis.remote.processor;

import one.xis.remote.javascript.*;
import one.xis.template.TemplateModel;
import one.xis.template.TemplateModel.*;
import one.xis.utils.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;

class JSAstParser {

    JSAst parse(Collection<TemplateModel> templateModels, Collection<String> stateVars) {
        JSAst ast = new JSAst();
        templateModels.forEach(model -> addTemplateClass(model, ast, stateVars));
        return ast;
    }

    private void addTemplateClass(TemplateModel templateModel, JSAst ast, Collection<String> stateFields) {
        JSClass templateClass = ast.addClass(templateModel.getName(), Collections.singletonList("state"));
        JSMethod getContent = templateClass.addMethod("getContent");
        evaluate(getContent, templateModel, stateFields);
    }

    private void evaluate(JSMethod getContent, TemplateModel model, Collection<String> stateFields) {
        JSVar contentVar = new JSVar("content");
        getContent.addStatement(contentVar);
        getContent.addStatement(new JSAssignment(contentVar, staticString("")));
        for (String field : stateFields) {
            JSVar stateVar = new JSVar(field);
            getContent.addStatement(stateVar);
            getContent.addStatement(new JSAssignment(stateVar, "this.state." + field));
        }
        evaluate(getContent, model.getRoot(), contentVar);
        getContent.setReturnVar(contentVar);
    }

    private void evaluate(JSStatementHolder getContent, TemplateElement element, JSVar contentVar) {
        if (element instanceof StaticText) {
            evaluateStaticText(getContent, (StaticText) element, contentVar);
        } else if (element instanceof Expression) {
            evaluateExpression(getContent, (Expression) element, contentVar);
        } else if (element instanceof IfElement) {
            evaluateIf(getContent, (IfElement) element, contentVar);
        } else if (element instanceof ForElement) {
            evaluateFor(getContent, (ForElement) element, contentVar);
        } else if (element instanceof XmlElement) {
            evaluateXmlElement(getContent, (XmlElement) element, contentVar);
        } else if (element instanceof TextContent) {
            evaluateTextContent(getContent, (TextContent) element, contentVar);

        } else {
            throw new IllegalArgumentException();
        }
    }

    private void evaluateStaticText(JSStatementHolder getContent, StaticText element, JSVar contentVar) {
        for (String line : element.getLines()) {
            getContent.addStatement(new JSAppend(contentVar, staticString(line)));
        }
    }

    private void evaluateExpression(JSStatementHolder holder, Expression element, JSVar contentVar) {
        holder.addStatement(new JSAppend(contentVar, element.getContent()));
    }

    private void evaluateIf(JSStatementHolder holder, IfElement element, JSVar contentVar) {
        holder.addStatement(new JSIfStatement(element.getCondition()));
        element.getChildElements().forEach(child -> evaluate(holder, child, contentVar));
    }

    private void evaluateFor(JSStatementHolder holder, ForElement element, JSVar contentVar) {
        JSForStatement forStatement = new JSForStatement(element.getArrayVarName(), element.getElementVarName(), element.getIndexVarName());
        holder.addStatement(forStatement);
        element.getChildElements().forEach(child -> evaluate(forStatement, child, contentVar));
    }

    private void evaluateXmlElement(JSStatementHolder holder, XmlElement element, JSVar contentVar) {
        if (element.getChildElements().isEmpty()) {
            evaluateEmptyTag(holder, element, contentVar);
        } else {
            evaluateContainerTag(holder, element, contentVar);
        }
    }

    private void evaluateEmptyTag(JSStatementHolder holder, XmlElement element, JSVar contentVar) {
        if (element.getAttributes().isEmpty()) {
            holder.addStatement(new JSAppend(contentVar, staticString("<" + element.getTagName() + "/>")));
        } else {
            holder.addStatement(new JSAppend(contentVar, staticString("<" + element.getTagName())));
            evaluateAttributes(holder, element, contentVar);
            holder.addStatement(new JSAppend(contentVar, staticString("/>")));
        }
    }

    private void evaluateContainerTag(JSStatementHolder holder, XmlElement element, JSVar contentVar) {
        if (element.getAttributes().isEmpty()) {
            holder.addStatement(new JSAppend(contentVar, staticString("<" + element.getTagName() + ">")));
        } else {
            holder.addStatement(new JSAppend(contentVar, staticString("<" + element.getTagName())));
            evaluateAttributes(holder, element, contentVar);
            holder.addStatement(new JSAppend(contentVar, staticString(">")));
        }
        element.getChildElements().forEach(child -> evaluate(holder, child, contentVar));
        holder.addStatement(new JSAppend(contentVar, staticString("</" + element.getTagName() + ">")));
    }

    private void evaluateAttributes(JSStatementHolder holder, XmlElement element, JSVar contentVar) {
        for (String attrName : element.getAttributes().keySet()) {
            TextContent attributeValue = element.getAttributes().get(attrName);
            holder.addStatement(new JSAppend(contentVar, staticString(" " + attrName + "=\"")));
            evaluateTextContent(holder, attributeValue, contentVar);
        }
    }

    private void evaluateTextContent(JSStatementHolder holder, TextContent textContent, JSVar contentVar) {
        textContent.getTextElements().forEach(element -> {
            if (element instanceof StaticText) {
                evaluateStaticText(holder, (StaticText) element, contentVar);
            } else if (element instanceof Expression) {
                evaluateExpression(holder, (Expression) element, contentVar);
            }
        });
    }

    private static String staticString(String s) {
        return "'" + escaped(s) + "'";
    }

    private static String escaped(String s) {
        return StringUtils.escape(s, '\'');
    }
}
