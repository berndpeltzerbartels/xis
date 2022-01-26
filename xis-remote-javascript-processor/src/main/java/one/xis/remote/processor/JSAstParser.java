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
        evaluateGetContentMethod(getContent, templateModel, stateFields);
    }

    private void evaluateGetContentMethod(JSMethod getContent, TemplateModel model, Collection<String> stateFields) {
        JSVar contentVar = getContent.addStatement(new JSVar("content"));
        for (String field : stateFields) {
            JSVar jsVar = getContent.addStatement(new JSVar(field));
            getContent.addStatement(new JSAssignment(jsVar, "this.state." + field));
        }
        evaluateGetContentMethod(getContent, model.getRoot(), contentVar);
        getContent.setReturnVar(contentVar);
    }

    private void evaluateGetContentMethod(JSMethod getContent, TemplateElement element, JSVar contentVar) {
        if (element instanceof StaticText) {
            evaluateGetContentMethod(getContent, (StaticText) element, contentVar);
        } else if (element instanceof Expression) {
            evaluateGetContentMethod(getContent, (Expression) element, contentVar);
        } else if (element instanceof IfElement) {
            evaluateGetContentMethod(getContent, (IfElement) element, contentVar);
        } else if (element instanceof ForElement) {
            evaluateGetContentMethod(getContent, (ForElement) element, contentVar);
        } else if (element instanceof XmlElement) {
            evaluateGetContentMethod(getContent, (XmlElement) element, contentVar);
        } else if (element instanceof TextContent) {
            evaluateGetContentMethod(getContent, (TextContent) element, contentVar);

        } else {
            throw new IllegalArgumentException();
        }
    }

    private void evaluateGetContentMethod(JSMethod getContent, StaticText element, JSVar contentVar) {
        for (String line : element.getLines()) {
            getContent.addStatement(new JSAppend(contentVar, "'" + escaped(line) + "'"));
        }
    }

    private void evaluateGetContentMethod(JSMethod getContent, Expression element, JSVar contentVar) {
        getContent.addStatement(new JSAppend(contentVar, element.getContent()));
    }

    private void evaluateGetContentMethod(JSMethod getContent, IfElement element, JSVar contentVar) {
        getContent.addStatement(new JSIfStatement(element.getCondition()));
        element.getChildElements().forEach(child -> evaluateGetContentMethod(getContent, child, contentVar));
    }

    private void evaluateGetContentMethod(JSMethod getContent, ForElement element, JSVar contentVar) {
        getContent.addStatement(new JSForStatement(element.getArrayVarName(), element.getElementVarName(), element.getIndexVarName()));
        element.getChildElements().forEach(child -> evaluateGetContentMethod(getContent, child, contentVar));
    }

    private void evaluateGetContentMethod(JSMethod getContent, XmlElement element, JSVar contentVar) {
        if (element.getChildElements().isEmpty()) {
            evaluateEmptyTag(getContent, element, contentVar);
        } else {
            evaluateContainerTag(getContent, element, contentVar);
        }
    }

    private void evaluateEmptyTag(JSMethod getContent, XmlElement element, JSVar contentVar) {
        getContent.addStatement(new JSAppend(contentVar, "<" + element.getTagName()));
        evaluateAttributes(getContent, element, contentVar);
        getContent.addStatement(new JSAppend(contentVar, "/>"));
    }

    private void evaluateContainerTag(JSMethod getContent, XmlElement element, JSVar contentVar) {
        getContent.addStatement(new JSAppend(contentVar, "<" + element.getTagName()));
        evaluateAttributes(getContent, element, contentVar);
        getContent.addStatement(new JSAppend(contentVar, ">"));
        element.getChildElements().forEach(child -> evaluateGetContentMethod(getContent, child, contentVar));
        getContent.addStatement(new JSAppend(contentVar, "</" + element.getTagName() + ">"));
    }

    private void evaluateAttributes(JSMethod getContent, XmlElement element, JSVar contentVar) {
        for (String attrName : element.getAttributes().keySet()) {
            TextContent attributeValue = element.getAttributes().get(attrName);
            getContent.addStatement(new JSAppend(contentVar, " " + attrName + "=\""));
            evaluateGetContentMethod(getContent, attributeValue, contentVar);
            getContent.addStatement(new JSAppend(contentVar, " " + attrName + "\""));
        }
    }

    private void evaluateGetContentMethod(JSMethod getContent, TextContent textContent, JSVar contentVar) {
        textContent.getTextElements().forEach(element -> {
            if (element instanceof StaticText) {
                evaluateGetContentMethod(getContent, (StaticText) element, contentVar);
            } else if (element instanceof Expression) {
                evaluateGetContentMethod(getContent, (Expression) element, contentVar);
            }
        });
    }

    private static String escaped(String s) {
        return StringUtils.escape(s, '\'');
    }
}
