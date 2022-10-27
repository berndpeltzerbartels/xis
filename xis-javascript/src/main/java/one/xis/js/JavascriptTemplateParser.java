package one.xis.js;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.template.*;
import one.xis.utils.lang.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.js.Classes.*;

@XISComponent
public class JavascriptTemplateParser {

    private static long currentNameId = 1;

    public void parseTemplateModel(WidgetTemplateModel widgetTemplateModel, String javascriptClassName, JSScript script) {
        toClass(widgetTemplateModel, javascriptClassName, script);
    }

    public void parseTemplateModel(PageTemplateModel pageTemplateModel, String javascriptClassName, JSScript script) {
        var pageClass = derrivedClass(javascriptClassName, XIS_PAGE, script);
        var headClass = toClass(pageTemplateModel.getHead(), script);
        var bodyClass = toClass(pageTemplateModel.getBody(), script);
        pageClass.addField("head", new JSContructorCall(headClass, "this"));
        pageClass.addField("body", new JSContructorCall(bodyClass, "this"));
        pageClass.addField("id", new JSString(pageTemplateModel.getKey()));
        pageClass.addField("server", new JSString("")); // empty = this server TODO method parameter
    }

    private JSClass toClass(WidgetTemplateModel widgetTemplateModel, String javascriptClassName, JSScript script) {
        var widgetClass = derrivedClass(javascriptClassName, XIS_WIDGET, script);
        var widgetRootClass = toClass(widgetTemplateModel.getRootNode(), script);
        widgetClass.addField("root", new JSContructorCall(widgetRootClass, "this"));
        widgetClass.addField("id", new JSString(widgetTemplateModel.getWidgetClassName()));
        widgetClass.addField("server", new JSString("")); // empty = this server TODO method parameter
        return widgetClass;
    }

    private List<JSContructorCall> evaluateChildren(ChildHolder parent, JSScript script) {
        return parent.getChildren().stream()
                .map(node -> toClass(node, script))
                .map(jsClass -> new JSContructorCall(jsClass, "this"))
                .collect(Collectors.toList());
    }

    private JSClass toClass(ModelNode node, JSScript script) {
        if (node instanceof TemplateElement) {
            return toClass((TemplateElement) node, script);
        } else if (node instanceof ContainerElement) {
            return toClass((ContainerElement) node, script);
        } else if (node instanceof MutableTextNode) {
            return toClass((MutableTextNode) node, script);
        } else if (node instanceof StaticTextNode) {
            return toClass((StaticTextNode) node, script);
        } else if (node instanceof Loop) {
            return toClass((Loop) node, script);
        } else if (node instanceof IfBlock) {
            return toClass((IfBlock) node, script);
        }
        throw new IllegalArgumentException("node=" + node);
    }

    private JSClass toClass(TemplateElement element, JSScript script) {
        var elementClass = derrivedClass(XIS_ELEMENT, script);
        addChildrenField(element, elementClass, script);
        addElementField(element, elementClass);
        overrideUpdateAttributes(elementClass, element);
        return elementClass;
    }

    private JSClass toClass(TemplateHeadElement element, JSScript script) {
        var elementClass = derrivedClass(XIS_HEAD_ELEMENT, script);
        addChildrenField(element, elementClass, script);
        addElementField(element, elementClass);
        overrideUpdateAttributes(elementClass, element);
        return elementClass;
    }

    private JSClass toClass(TemplateBodyElement element, JSScript script) {
        var elementClass = derrivedClass(XIS_BODY_ELEMENT, script);
        addChildrenField(element, elementClass, script);
        addElementField(element, elementClass);
        overrideUpdateAttributes(elementClass, element);
        return elementClass;
    }

    private JSClass toClass(ContainerElement containerElement, JSScript script) {
        var containerClass = derrivedClass(XIS_CONTAINER, script);
        containerClass.addField("containerId", new JSString(containerElement.getContainerId()));
        var defaultWidgetId = containerElement.getDefaultWidgetId() != null ? new JSString(containerElement.getDefaultWidgetId()) : new JSUndefined();
        containerClass.addField("defaultWidgetId", defaultWidgetId);
        addElementField(containerElement, containerClass);
        overrideUpdateAttributes(containerClass, containerElement);
        return containerClass;
    }

    private JSClass toClass(MutableTextNode mutableTextNode, JSScript script) {
        var textNode = derrivedClass(XIS_MUTABLE_TEXT_NODE, script);
        textNode.addField("node", new JSFunctionCall(Functions.CREATE_TEXT_NODE, new JSString("")));
        JSMethod getText = textNode.overrideAbstractMethod("getText");
        var text = new JSVar("text");
        var mixedContentMethodStatements = new MixedContentMethodStatements(getText, text);
        mixedContentMethodStatements.addStatements(mutableTextNode.getContent());
        getText.addStatement(new JSReturn(text));
        return textNode;
    }

    private JSClass toClass(StaticTextNode staticTextNode, JSScript script) {
        var textNode = derrivedClass(XIS_STATIC_TEXT_NODE, script);
        textNode.addField("node", new JSFunctionCall(Functions.CREATE_TEXT_NODE, new JSString(staticTextNode.getContent())));
        return textNode;
    }

    private JSClass toClass(Loop loop, JSScript script) {
        var loopClass = derrivedClass(XIS_LOOP, script);

        var loopAttributes = new JSJsonValue();
        loopAttributes.addField("indexVarName", new JSString(loop.getIndexVarName()));
        loopAttributes.addField("itemVarName", new JSString(loop.getItemVarName()));
        loopAttributes.addField("numberVarName", new JSString(loop.getNumberVarName()));
        loopClass.addField("loopAttributes", loopAttributes);

        var getValue = loopClass.getMethod("getValue");
        var getArray = loopClass.overrideAbstractMethod("getArray");
        var createChilderen = loopClass.overrideAbstractMethod("createChildren");
        createChilderen.addStatement(new JSReturn(new JSArray(evaluateChildren(loop, script))));

        var expressionEval = new ExpressionEval(getValue);
        getArray.addStatement(new JSReturn(expressionEval.getEvaluator(loop.getArraySource())));

        loopClass.addField("rows", new JSArray());
        return loopClass;
    }

    private JSClass toClass(IfBlock ifBlock, JSScript script) {
        var ifClass = derrivedClass(XIS_IF, script);

        var valueMethod = ifClass.getMethod("val");
        var evaluateCondition = ifClass.overrideAbstractMethod("evaluateCondition");

        var expressionEval = new ExpressionEval(valueMethod);
        evaluateCondition.addStatement(new JSReturn(expressionEval.getEvaluator(ifBlock.getExpression())));

        addChildrenField(ifBlock, ifClass, script);
        return ifClass;
    }

    private void addElementField(ElementWithAttributes element, JSClass jsClass) {
        jsClass.addField("element", getCreateElementFunctionCall(element));
    }

    private void addChildrenField(ChildHolder childHolder, JSClass jsClass, JSScript script) {
        jsClass.addField("children", new JSArray(evaluateChildren(childHolder, script)));
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
        var jsFunction = Functions.getFunction(expression.getFunction());
        var fktCall = new JSFunctionCall(jsFunction);
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
            var jsFunction = Functions.getFunction(expression.getFunction());
            var fktCall = new JSFunctionCall(jsFunction);
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

    private JSClass derrivedClass(JSSuperClass superClass, JSScript script) {
        return derrivedClass(nextClassName(), superClass, script);
    }


    private JSClass derrivedClass(String className, JSSuperClass superClass, JSScript script) {
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
