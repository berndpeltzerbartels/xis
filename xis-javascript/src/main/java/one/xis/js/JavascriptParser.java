package one.xis.js;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.template.*;
import one.xis.utils.lang.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.js.Classes.*;

@RequiredArgsConstructor
public class JavascriptParser {

    @Getter
    private final JSScript script = new JSScript();
    private static long currentNameId = 1;

    public void parseTemplateModel(WidgetTemplateModel widgetTemplateModel, String javascriptClassName) {
        toClass(widgetTemplateModel, javascriptClassName);
    }

    public void parseTemplateModel(PageTemplateModel pageTemplateModel, String javascriptClassName) {
        var pageClass = derrivedClass(javascriptClassName, XIS_PAGE);
        var headClass = toClass(pageTemplateModel.getHead());
        var bodyClass = toClass(pageTemplateModel.getBody());
        pageClass.addField("head", new JSContructorCall(headClass, "this"));
        pageClass.addField("body", new JSContructorCall(bodyClass, "this"));
    }

    private JSClass toClass(WidgetTemplateModel widgetTemplateModel, String javascriptClassName) {
        var widgetClass = derrivedClass(javascriptClassName, XIS_WIDGET);
        var widgetRootClass = toClass(widgetTemplateModel.getRootNode());
        widgetClass.addField("root", new JSContructorCall(widgetRootClass));
        widgetClass.addField("name", new JSString(widgetTemplateModel.getWidgetClassName()));
        return widgetClass;
    }

    private List<JSContructorCall> evaluateChildren(ChildHolder parent) {
        return parent.getChildren().stream()
                .map(this::toClass)
                .map(jsClass -> new JSContructorCall(jsClass, "this"))
                .collect(Collectors.toList());
    }

    private JSClass toClass(ModelNode node) {
        if (node instanceof TemplateElement) {
            return toClass((TemplateElement) node);
        } else if (node instanceof ContainerElement) {
            return toClass((ContainerElement) node);
        } else if (node instanceof MutableTextNode) {
            return toClass((MutableTextNode) node);
        } else if (node instanceof StaticTextNode) {
            return toClass((StaticTextNode) node);
        } else if (node instanceof Loop) {
            return toClass((Loop) node);
        } else if (node instanceof IfBlock) {
            return toClass((IfBlock) node);
        }
        throw new IllegalArgumentException("node=" + node);
    }

    private JSClass toClass(TemplateElement element) {
        var elementClass = derrivedClass(XIS_ELEMENT);
        addChildrenField(element, elementClass);
        addElementField(element, elementClass);
        overrideUpdateAttributes(elementClass, element);
        return elementClass;
    }

    private JSClass toClass(ContainerElement containerElement) {
        var containerClass = derrivedClass(XIS_CONTAINER);
        containerClass.addField("containerId", new JSString(containerElement.getContainerId()));
        var defaultWidgetId = containerElement.getDefaultWidgetId() != null ? new JSString(containerElement.getDefaultWidgetId()) : new JSUndefined();
        containerClass.addField("defaultWidgetId", defaultWidgetId);
        addElementField(containerElement, containerClass);
        overrideUpdateAttributes(containerClass, containerElement);
        return containerClass;
    }

    private JSClass toClass(MutableTextNode mutableTextNode) {
        var textNode = derrivedClass(XIS_MUTABLE_TEXT_NODE);
        textNode.addField("node", new JSFunctionCall(Functions.CREATE_TEXT_NODE, new JSString("")));
        JSMethod getText = textNode.overrideAbstractMethod("getText");
        var text = new JSVar("text");
        var mixedContentMethodStatements = new MixedContentMethodStatements(getText, text);
        mixedContentMethodStatements.addStatements(mutableTextNode.getContent());
        getText.addStatement(new JSReturn(text));
        return textNode;
    }

    private JSClass toClass(StaticTextNode staticTextNode) {
        var textNode = derrivedClass(XIS_STATIC_TEXT_NODE);
        textNode.addField("node", new JSFunctionCall(Functions.CREATE_TEXT_NODE, new JSString(staticTextNode.getContent())));
        return textNode;
    }

    private JSClass toClass(Loop loop) {
        var loopClass = derrivedClass(XIS_LOOP);

        var loopAttributes = new JSJsonValue();
        loopAttributes.addField("indexVarName", new JSString(loop.getIndexVarName()));
        loopAttributes.addField("itemVarName", new JSString(loop.getItemVarName()));
        loopAttributes.addField("numberVarName", new JSString(loop.getNumberVarName()));
        loopClass.addField("loopAttributes", loopAttributes);

        var getValue = loopClass.getMethod("getValue");
        var getArray = loopClass.overrideAbstractMethod("getArray");
        var createChilderen = loopClass.overrideAbstractMethod("createChildren");
        createChilderen.addStatement(new JSReturn(new JSArray(evaluateChildren(loop))));

        var expressionEval = new ExpressionEval(getValue);
        getArray.addStatement(new JSReturn(expressionEval.getEvaluator(loop.getArraySource())));

        loopClass.addField("rows", new JSArray());
        return loopClass;
    }

    private JSClass toClass(IfBlock ifBlock) {
        var ifClass = derrivedClass(XIS_IF);

        var valueMethod = ifClass.getMethod("val");
        var evaluateCondition = ifClass.overrideAbstractMethod("evaluateCondition");

        var expressionEval = new ExpressionEval(valueMethod);
        evaluateCondition.addStatement(new JSReturn(expressionEval.getEvaluator(ifBlock.getExpression())));

        addChildrenField(ifBlock, ifClass);
        return ifClass;
    }

    private void addElementField(ElementWithAttributes element, JSClass jsClass) {
        jsClass.addField("element", getCreateElementFunctionCall(element));
    }

    private void addChildrenField(ChildHolder childHolder, JSClass jsClass) {
        jsClass.addField("children", new JSArray(evaluateChildren(childHolder)));
    }

    private JSFunctionCall getCreateElementFunctionCall(ElementWithAttributes element) {
        var createElementFunctionCall = new JSFunctionCall(Functions.CREATE_ELEMENT).addParam(new JSString(element.getElementName()));
        if (!element.getStaticAttributes().isEmpty()) {
            createElementFunctionCall.addParam(staticAttributes(element.getStaticAttributes()));
        }
        return createElementFunctionCall;
    }

    private JSJsonValue staticAttributes(Map<String, String> attributesMap) {
        var attributes = new JSJsonValue();
        attributesMap.forEach((key, value) -> attributes.addField(key, new JSString(value)));
        return attributes;
    }

    private void overrideUpdateAttributes(JSClass jsClass, ElementWithAttributes elementBase) {
        var updateAttributes = jsClass.overrideAbstractMethod("updateAttributes");
        elementBase.getMutableAttributes().forEach((key, value) -> {
            JSVar text = new JSVar(nextVarName());

            MixedContentMethodStatements mixedContentMethodStatements = new MixedContentMethodStatements(updateAttributes, text);
            mixedContentMethodStatements.addStatements(value.getContents());

            JSMethodCall updateAttribute = new JSMethodCall(jsClass.getMethod("updateAttribute"), new JSString(key), text);
            updateAttributes.addStatement(updateAttribute);
        });
    }

    private static JSFunctionCall expressionWithFunction(Expression expression, JSClass owner) {
        var fkt = Functions.getFunction(expression.getFunction());
        var fktCall = new JSFunctionCall(fkt);
        for (ExpressionArg arg : expression.getVars()) {
            if (arg instanceof ExpressionConstant) {
                fktCall.addParam(new JSConstant(((ExpressionConstant) arg).getContent()));
            } else if (arg instanceof ExpressionString) {
                fktCall.addParam(new JSString(((ExpressionString) arg).getContent()));
            } else if (arg instanceof ExpressionVar) {
                JSMethod getValue = owner.getMethod("val");
                JSArray variablePath = JSArray.arrayOfStrings(((ExpressionVar) arg).getPath());
                JSMethodCall getValueMethodCall = new JSMethodCall(getValue, variablePath);
                fktCall.addParam(getValueMethodCall);
            }
        }
        return fktCall;
    }

    @RequiredArgsConstructor
    private static class ExpressionEval {
        private final JSMethod getValue;

        JSValue getEvaluator(Expression expression) {
            if (expression.getFunction() != null) {
                return expressionWithFunction(expression);
            }
            return expressionWithoutFunction(expression);
        }

        private JSFunctionCall expressionWithFunction(Expression expression) {
            var fkt = Functions.getFunction(expression.getFunction());
            var fktCall = new JSFunctionCall(fkt);
            for (ExpressionArg arg : expression.getVars()) {
                if (arg instanceof ExpressionConstant) {
                    fktCall.addParam(new JSConstant(((ExpressionConstant) arg).getContent()));
                } else if (arg instanceof ExpressionString) {
                    fktCall.addParam(new JSString(((ExpressionString) arg).getContent()));
                } else if (arg instanceof ExpressionVar) {
                    JSArray variablePath = JSArray.arrayOfStrings(((ExpressionVar) arg).getPath());
                    JSMethodCall getValueMethodCall = new JSMethodCall(getValue, variablePath);
                    fktCall.addParam(getValueMethodCall);
                }
            }
            return fktCall;
        }

        private JSValue expressionWithoutFunction(Expression expression) {
            var arg = CollectionUtils.onlyElement(expression.getVars(), () -> new TemplateSynthaxException("expected exactly one value in " + expression));
            return expressionWithoutFunction(arg);
        }

        private JSValue expressionWithoutFunction(ExpressionArg arg) {
            if (arg instanceof ExpressionConstant) {
                return new JSConstant(((ExpressionConstant) arg).getContent());
            } else if (arg instanceof ExpressionString) {
                return new JSString(((ExpressionString) arg).getContent());
            } else if (arg instanceof ExpressionVar) {
                JSArray variablePath = JSArray.arrayOfStrings(((ExpressionVar) arg).getPath());
                return new JSMethodCall(getValue, variablePath);
            } else {
                throw new IllegalArgumentException("expression-arg: " + arg);
            }
        }
    }

    @RequiredArgsConstructor
    private static class MixedContentMethodStatements {
        private final JSMethod method;
        private final JSVar text;

        void addStatements(List<MixedContent> mixedContentList) {
            method.addStatement(new JSVarAssignment(text, new JSString("")));
            mixedContentList.forEach(this::addStatements);
        }

        private void addStatements(MixedContent mixedContent) {
            if (mixedContent instanceof StaticContent) {
                addStaticContentStatements((StaticContent) mixedContent);
            } else if (mixedContent instanceof ExpressionContent) {
                addExpressionContentStatements((ExpressionContent) mixedContent);
            }
        }

        private void addStaticContentStatements(StaticContent staticContent) {
            method.addStatement(new JSStringAppend(text, new JSString(staticContent.getContent())));
        }

        private void addExpressionContentStatements(ExpressionContent expressionContent) {
            var expression = expressionContent.getExpression();
            if (expression.getFunction() != null) {
                addExpressionWithFunctionStatements(expression);
            } else {
                addExpressionWithoutFunctionStatements(expression);
            }
        }

        private void addExpressionWithFunctionStatements(Expression expression) {
            var fktCall = expressionWithFunction(expression, method.getOwner());
            method.addStatement(new JSStringAppend(text, fktCall));
        }

        private void addExpressionWithoutFunctionStatements(Expression expression) {
            for (ExpressionArg arg : expression.getVars()) {
                if (arg instanceof ExpressionConstant) {
                    method.addStatement(new JSStringAppend(text, new JSConstant(((ExpressionConstant) arg).getContent())));
                } else if (arg instanceof ExpressionString) {
                    method.addStatement(new JSStringAppend(text, new JSString(((ExpressionString) arg).getContent())));
                } else if (arg instanceof ExpressionVar) {
                    var getValue = method.getOwner().getMethod("val");
                    var variablePath = JSArray.arrayOfStrings(((ExpressionVar) arg).getPath());
                    var getValueMethodCall = new JSMethodCall(getValue, variablePath);
                    method.addStatement(new JSStringAppend(text, getValueMethodCall));
                }
            }
        }
    }

    private JSClass derrivedClass(JSSuperClass superClass) {
        return derrivedClass(nextClassName(), superClass);
    }


    private JSClass derrivedClass(String className, JSSuperClass superClass) {
        var jsClass = new JSClass(className, superClass.getConstructor().getArgs()).derrivedFrom(superClass);
        script.addClassDeclaration(jsClass);
        return jsClass;
    }

    private String nextClassName() {
        return "C" + (currentNameId++);
    }

    private String nextVarName() {
        return "v" + (currentNameId++);
    }
}
