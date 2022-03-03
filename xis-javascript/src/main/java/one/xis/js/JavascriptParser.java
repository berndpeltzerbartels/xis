package one.xis.js;

import lombok.RequiredArgsConstructor;
import one.xis.template.*;

import java.util.*;
import java.util.stream.Collectors;

import static one.xis.js.Functions.APPEND;
import static one.xis.js.SuperClasses.*;

@RequiredArgsConstructor
public class JavascriptParser {
    private final JSScript script;
    private static long currentNameId = 1;
    private final Collection<JSClass> rootClasses = new HashSet<>();

    public void parse(WidgetModel widgetModel) {
        script.addDeclaration(toClass(widgetModel));
        evaluateChildren(widgetModel);
        JSClass widgets = widgetsClass();
        script.addDeclaration(widgets);
        script.addStatement(new JSVarAssignment(new JSVar("widgets"), new JSContructorCall(widgets)));
    }

    private JSClass widgetsClass() {
        JSClass widgestClass = new JSClass(nextName()).derrivedFrom(XIS_WIDGETS);
        JSJsonValue widgets = new JSJsonValue();
        rootClasses.forEach(root -> widgets.addField(root.getClassName(), new JSContructorCall(root)));
        widgestClass.addField("widgets", widgets);
        return widgestClass;
    }

    private List<JSContructorCall> evaluateChildren(ChildHolder parent) {
        List<JSClass> jsClasses = parent.getChildren().stream()
                .map(this::toClass).collect(Collectors.toList());
        script.addDeclarations(jsClasses);
        return jsClasses.stream().map(JSContructorCall::new)
                .collect(Collectors.toList());
    }


    private JSClass toClass(ModelNode node) {
        if (node instanceof WidgetModel) {
            return toClass((WidgetModel) node);
        } else if (node instanceof TemplateElement) {
            return toClass((TemplateElement) node);
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
        createChildren.addStatement(new JSReturn(new JSArray(evaluateChildren(model))));
        overrideCreateElement(widgetClass, model);
        rootClasses.add(widgetClass);
        return widgetClass;
    }

    private JSClass toClass(TemplateElement element) {
        if (element.getLoop() == null) {
            return toElementClassWithoutLoop(element);
        }
        return toElementClassWitLoop(element);
    }

    private JSClass toElementClassWithoutLoop(TemplateElement element) {
        JSClass elementClass = new JSClass(nextName()).derrivedFrom(XIS_ELEMENT);
        JSMethod createChildren = elementClass.overrideMethod("createChildren");
        createChildren.addStatement(new JSReturn(new JSArray(evaluateChildren(element))));
        overrideCreateElement(elementClass, element);
        overrideUpdateAttributes(elementClass, element);
        return elementClass;
    }

    private JSClass toElementClassWitLoop(TemplateElement element) {
        JSClass elementClass = new JSClass(nextName()).derrivedFrom(XIS_LOOP_ELEMENT);
        JSMethod createChildren = elementClass.overrideMethod("createChildren");
        createChildren.addStatement(new JSReturn(new JSArray(evaluateChildren(element))));
        overrideCreateElement(elementClass, element);
        overrideUpdateAttributes(elementClass, element);
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
        JSValue defaultWidgetId = containerElement.getDefaultWidgetId() != null ? new JSString(containerElement.getDefaultWidgetId()) : new JSUndefined();
        containerClass.addField("defaultWidgetId", defaultWidgetId);
        overrideCreateElement(containerClass, containerElement);
        overrideUpdateAttributes(containerClass, containerElement);
        return containerClass;
    }

    private JSClass toClass(MutableTextNode mutableTextNode) {
        JSClass textNode = new JSClass(nextName()).derrivedFrom(XIS_MUTABLE_TEXT_NODE);
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

    private JSFunctionCall getCreateElementFunctionCall(ElementWithAttributes element) {
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

    private void overrideCreateElement(JSClass jsClass, ElementWithAttributes element) {
        JSMethod createElement = jsClass.overrideMethod("createElement");
        createElement.addStatement(new JSReturn(getCreateElementFunctionCall(element)));
    }


    private void overrideUpdateAttributes(JSClass jsClass, ElementBase elementBase) {
        JSMethod updateAttributes = jsClass.overrideMethod("updateAttributes");
        elementBase.getMutableAttributes().entrySet().forEach(e -> {
            JSVar text = new JSVar(nextName());

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
            JSFunction fkt = Functions.getFunction(expression.getFunction());
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
