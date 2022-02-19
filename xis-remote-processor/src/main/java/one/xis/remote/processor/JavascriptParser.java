package one.xis.remote.processor;

import lombok.RequiredArgsConstructor;
import one.xis.js.*;
import one.xis.template.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.js.XISClasses.*;
import static one.xis.js.XISFunctions.APPEND;

@RequiredArgsConstructor
class JavascriptParser {
    private final JSScript script;
    private static long currentNameId = 1;

    void parse(WidgetModel widgetModel) {
        script.addDeclaration(toClass(widgetModel));
    }

    private List<JSContructorCall> evalulateChildren(ModelElement parent) {
        List<JSClass> jsClasses = parent.getChildren().stream()
                .map(this::toClass).collect(Collectors.toList());
        script.addDeclarations(jsClasses);
        return jsClasses.stream().map(JSContructorCall::new)
                .collect(Collectors.toList());
    }

    private JSClass toClass(ModelNode node) {
        if (node instanceof WidgetModel) {
            return toClass((WidgetModel) node);
        } else if (node instanceof ModelElement) {
            return toClass((ModelElement) node);
        } else if (node instanceof ContainerElement) {
            return toClass((ContainerElement) node);
        } else if (node instanceof MutableTextNode) {
            return toClass((MutableTextNode) node);
        } else if (node instanceof StaticTextNode) {
            return toClass((StaticTextNode) node);
        }
        throw new IllegalArgumentException("node=" + node);
    }

    private JSClass toClass(WidgetModel model) {
        JSClass widgetClass = new JSClass(model.getName()).derrivedFrom(XIS_ROOT);
        JSMethod createChildren = widgetClass.overrideMethod("createChildren");
        createChildren.addStatement(new JSReturn(new JSArray(evalulateChildren(model))));
        return widgetClass;
    }

    private JSClass toClass(ModelElement element) {
        if (element.getLoop() == null) {
            return toElementClassWithoutLoop(element);
        }
        return toElementClassWitLoop(element);
    }

    private JSClass toElementClassWithoutLoop(ModelElement element) {
        JSClass elementClass = new JSClass(nextName()).derrivedFrom(XIS_ELEMENT);
        JSMethod createChildren = elementClass.overrideMethod("createChildren");
        createChildren.addStatement(new JSReturn(new JSArray(evalulateChildren(element))));
        overrideCreateElement(elementClass, element);
        overrideUpdateAttributes(elementClass, element);
        return elementClass;
    }

    private JSClass toElementClassWitLoop(ModelElement element) {
        JSClass elementClass = toElementClassWithoutLoop(element);
        ForLoop loop = element.getLoop();
        JSJsonValue loopAttributes = new JSJsonValue();
        List<JSString> arrayPath = Arrays.stream(loop.getArraySource().getContent().split(".")).map(JSString::new).collect(Collectors.toList());
        loopAttributes.addField("indexVarName", new JSString(loop.getIndexVarName()));
        loopAttributes.addField("itemVarName", new JSString(loop.getItemVarName()));
        loopAttributes.addField("numberVarName", new JSString(loop.getNumberVarName()));
        loopAttributes.addField("indexVarName", new JSArray(arrayPath));
        JSMethod getLoopAttributes = elementClass.overrideMethod("getLoopAttributes");
        getLoopAttributes.addStatement(new JSReturn(loopAttributes));
        return elementClass;
    }

    private JSClass toClass(ContainerElement containerElement) {
        JSClass containerClass = new JSClass(nextName()).derrivedFrom(XIS_CONTAINER);
        containerClass.addField("containerId", new JSString(containerElement.getContainerId()));
        containerClass.addField("defaultWidgetId", new JSString(containerElement.getDefaultWidgetId()));
        overrideCreateElement(containerClass, containerElement);
        overrideUpdateAttributes(containerClass, containerElement);
        return containerClass;
    }

    private JSClass toClass(MutableTextNode mutableTextNode) {
        JSClass textNode = new JSClass(nextName()).derrivedFrom(XIS_STATIC_TEXT_NODE);
        JSMethod getText = textNode.overrideMethod("getText");
        JSVar text = new JSVar("text");
        MixedContentMethodStatements mixedContentMethodStatements = new MixedContentMethodStatements(getText, text);
        mixedContentMethodStatements.addStatements(mutableTextNode.getContent());
        getText.addStatement(new JSReturn(text));
        return textNode;
    }

    private JSClass toClass(StaticTextNode staticTextNode) {
        JSClass textNode = new JSClass(nextName()).derrivedFrom(XIS_STATIC_TEXT_NODE);
        JSMethod getText = textNode.overrideMethod("getText");
        getText.addStatement(new JSReturn(new JSString(staticTextNode.getContent())));
        textNode.overrideMethod("update"); // Nothing to do, here
        return textNode;
    }

    private JSFunctionCall getCreateElementFunctionCall(ElementBase element) {
        JSFunctionCall createElementFunctionCall = new JSFunctionCall(JSFunctions.CREATE_ELEMENT).addParam(new JSString(element.getElementName()));
        if (!element.getStaticAttributes().isEmpty()) {
            createElementFunctionCall.addParam(staticAttributes(element.getStaticAttributes()));
        }
        return createElementFunctionCall;
    }

    private JSJsonValue staticAttributes(Map<String, String> attributesMap) {
        JSJsonValue attributes = new JSJsonValue();
        attributesMap.forEach((key, value) -> attributes.addField(key, new JSString(value)));
        return attributes;
    }

    private void overrideCreateElement(JSClass jsClass, ElementBase element) {
        JSMethod createElement = jsClass.overrideMethod("createElement");
        createElement.addStatement(new JSReturn(getCreateElementFunctionCall(element)));
    }


    private void overrideUpdateAttributes(JSClass jsClass, ElementBase elementBase) {
        JSMethod updateAttributes = jsClass.overrideMethod("updateAttributes");
        elementBase.getMutableAttributes().entrySet().forEach(e -> {
            JSVar text = new JSVar(nextName());
            updateAttributes.addStatement(new JSVarAssignment(text, new JSString("")));

            MixedContentMethodStatements mixedContentMethodStatements = new MixedContentMethodStatements(updateAttributes, text);
            mixedContentMethodStatements.addStatements(e.getValue().getContents());

            JSMethodCall updateAttribute = new JSMethodCall(updateAttributes, jsClass.getMethod("updateAttribute"), new JSString(e.getKey()), text);
            updateAttributes.addStatement(updateAttribute);
        });
    }

    @RequiredArgsConstructor
    private static class MixedContentMethodStatements {
        private final JSMethod method;
        private final JSVar text;

        void addStatements(List<MixedContent> mixedContentList) {
            method.addStatement(new JSVarAssignment(text, new JSString("")));
            mixedContentList.forEach(this::addStatements);
            method.addStatement(new JSReturn(text));
        }

        private void addStatements(MixedContent mixedContent) {
            if (mixedContent instanceof StaticContent) {
                addStaticContentStatements((StaticContent) mixedContent);
            } else if (mixedContent instanceof ExpressionContent) {
                addExpressionContentStatements((ExpressionContent) mixedContent);
            }
        }

        private void addStaticContentStatements(StaticContent staticContent) {
            method.addStatement(new JSFunctionCall(APPEND, text, new JSString(staticContent.getContent())));
        }

        private void addExpressionContentStatements(ExpressionContent expressionContent) {
            Expression expression = expressionContent.getExpression();
            if (expression.getFunction() != null) {
                addExpressionWithFunctionStatements(expression);
            } else {
                addExpressionWithoutFunctionStatements(expression);
            }
        }

        private void addExpressionWithFunctionStatements(Expression expression) {
            JSFunction fkt = XISFunctions.getFunction(expression.getFunction());
            JSFunctionCall fktCall = new JSFunctionCall(fkt);
            for (ExpressionArg arg : expression.getVars()) {
                if (arg instanceof ExpressionConstant) {
                    fktCall.addParam(new JSConstant(((ExpressionConstant) arg).getContent()));
                } else if (arg instanceof ExpressionString) {
                    fktCall.addParam(new JSString(((ExpressionString) arg).getContent()));
                } else if (arg instanceof ExpressionVar) {
                    JSMethod getValue = method.getOwner().getMethod("getValue");
                    JSArray variablePath = JSArray.arrayOfStrings(((ExpressionVar) arg).getPath());
                    JSMethodCall getValueMethodCall = new JSMethodCall(method, getValue, variablePath);
                    fktCall.addParam(getValueMethodCall);
                }
            }
            method.addStatement(new JSFunctionCall(APPEND, text, fktCall));
        }

        private void addExpressionWithoutFunctionStatements(Expression expression) {
            for (ExpressionArg arg : expression.getVars()) {
                if (arg instanceof ExpressionConstant) {
                    method.addStatement(new JSFunctionCall(APPEND, text, new JSConstant(((ExpressionConstant) arg).getContent())));
                } else if (arg instanceof ExpressionString) {
                    method.addStatement(new JSFunctionCall(APPEND, text, new JSString(((ExpressionString) arg).getContent())));
                } else if (arg instanceof ExpressionVar) {
                    JSMethod getValue = method.getOwner().getMethod("getValue");
                    JSArray variablePath = JSArray.arrayOfStrings(((ExpressionVar) arg).getPath());
                    JSMethodCall getValueMethodCall = new JSMethodCall(method, getValue, variablePath);
                    method.addStatement(new JSFunctionCall(APPEND, text, getValueMethodCall));
                }
            }
        }
    }

    private String nextName() {
        return "n" + (currentNameId++);
    }

}
