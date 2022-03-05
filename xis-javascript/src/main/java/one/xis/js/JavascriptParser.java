package one.xis.js;

import lombok.RequiredArgsConstructor;
import one.xis.template.*;
import one.xis.utils.lang.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static one.xis.js.Classes.*;

@RequiredArgsConstructor
public class JavascriptParser {
    private final JSScript script;
    private static long currentNameId = 1;
    private final Collection<JSClass> rootClasses = new HashSet<>();

    public void parse(Collection<WidgetModel> widgetModels) {
        widgetModels.forEach(this::parse);
        JSClass widgets = widgetsClass();
        script.addDeclaration(widgets);
        script.addStatement(new JSVarAssignment(new JSVar("widgets"), new JSContructorCall(widgets)));
    }

    private void parse(WidgetModel widgetModel) {
        JSClass widgetClass = new JSClass(widgetModel.getName()).derrivedFrom(XIS_ROOT);
        JSClass rootClass = toClass(widgetModel.getRootNode());
        widgetClass.addField("root", new JSContructorCall(rootClass));
        rootClasses.add(widgetClass);
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
        JSClass elementClass = new JSClass(nextName()).derrivedFrom(XIS_ELEMENT);
        addChildrenField(element, elementClass);
        addElementField(element, elementClass);
        overrideUpdateAttributes(elementClass, element);
        return elementClass;
    }

    private JSClass toClass(ContainerElement containerElement) {
        JSClass containerClass = new JSClass(nextName()).derrivedFrom(XIS_CONTAINER);
        containerClass.addField("containerId", new JSString(containerElement.getContainerId()));
        JSValue defaultWidgetId = containerElement.getDefaultWidgetId() != null ? new JSString(containerElement.getDefaultWidgetId()) : new JSUndefined();
        containerClass.addField("defaultWidgetId", defaultWidgetId);
        addElementField(containerElement, containerClass);
        overrideUpdateAttributes(containerClass, containerElement);
        return containerClass;
    }

    private JSClass toClass(MutableTextNode mutableTextNode) {
        JSClass textNode = new JSClass(nextName()).derrivedFrom(XIS_MUTABLE_TEXT_NODE);
        textNode.addField("node", new JSFunctionCall(Functions.CREATE_TEXT_NODE, new JSString("")));
        JSMethod getText = textNode.overrideMethod("getText");
        JSVar text = new JSVar("text");
        MixedContentMethodStatements mixedContentMethodStatements = new MixedContentMethodStatements(getText, text);
        mixedContentMethodStatements.addStatements(mutableTextNode.getContent());
        getText.addStatement(new JSReturn(text));
        return textNode;
    }

    private JSClass toClass(StaticTextNode staticTextNode) {
        JSClass textNode = new JSClass(nextName()).derrivedFrom(XIS_STATIC_TEXT_NODE);
        textNode.addField("node", new JSFunctionCall(Functions.CREATE_TEXT_NODE, new JSString(staticTextNode.getContent())));
        textNode.overrideMethod("update"); // Nothing to do, here
        return textNode;
    }

    private JSClass toClass(Loop loop) {
        JSClass loopClass = new JSClass(nextName()).derrivedFrom(XIS_LOOP);

        JSJsonValue loopAttributes = new JSJsonValue();
        loopAttributes.addField("indexVarName", new JSString(loop.getIndexVarName()));
        loopAttributes.addField("itemVarName", new JSString(loop.getItemVarName()));
        loopAttributes.addField("numberVarName", new JSString(loop.getNumberVarName()));
        loopClass.addField("loopAttributes", loopAttributes);

        JSMethod getValue = loopClass.getMethod("getValue");
        JSMethod getArray = loopClass.overrideMethod("getArray");
        ExpressionEval expressionEval = new ExpressionEval(getValue);
        getArray.addStatement(new JSReturn(expressionEval.getEvaluator(loop.getArraySource())));

        return loopClass;
    }

    private JSClass toClass(IfBlock ifBlock) {
        JSClass ifClass = new JSClass(nextName()).derrivedFrom(XIS_IF);
        ifClass.addField("children", new JSArray(evaluateChildren(ifBlock)));

        JSMethod getValue = ifClass.getMethod("getValue");
        JSMethod evaluateCondition = ifClass.overrideMethod("evaluateCondition");

        ExpressionEval expressionEval = new ExpressionEval(getValue);
        evaluateCondition.addStatement(new JSReturn(expressionEval.getEvaluator(ifBlock.getExpression())));

        return ifClass;
    }

    private void addElementField(ElementWithAttributes element, JSClass jsClass) {
        jsClass.addField("element", getCreateElementFunctionCall(element));
    }

    private void addChildrenField(ChildHolder childHolder, JSClass jsClass) {
        jsClass.addField("children", new JSArray(evaluateChildren(childHolder)));
    }

    private JSFunctionCall getCreateElementFunctionCall(ElementWithAttributes element) {
        JSFunctionCall createElementFunctionCall = new JSFunctionCall(Functions.CREATE_ELEMENT).addParam(new JSString(element.getElementName()));
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

    private void overrideUpdateAttributes(JSClass jsClass, ElementBase elementBase) {
        JSMethod updateAttributes = jsClass.overrideMethod("updateAttributes");
        elementBase.getMutableAttributes().forEach((key, value) -> {
            JSVar text = new JSVar(nextName());

            MixedContentMethodStatements mixedContentMethodStatements = new MixedContentMethodStatements(updateAttributes, text);
            mixedContentMethodStatements.addStatements(value.getContents());

            JSMethodCall updateAttribute = new JSMethodCall(jsClass.getMethod("updateAttribute"), new JSString(key), text);
            updateAttributes.addStatement(updateAttribute);
        });
    }

    private static JSFunctionCall expressionWithFunction(Expression expression, JSClass owner) {
        JSFunction fkt = Functions.getFunction(expression.getFunction());
        JSFunctionCall fktCall = new JSFunctionCall(fkt);
        for (ExpressionArg arg : expression.getVars()) {
            if (arg instanceof ExpressionConstant) {
                fktCall.addParam(new JSConstant(((ExpressionConstant) arg).getContent()));
            } else if (arg instanceof ExpressionString) {
                fktCall.addParam(new JSString(((ExpressionString) arg).getContent()));
            } else if (arg instanceof ExpressionVar) {
                JSMethod getValue = owner.getMethod("getValue");
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
            JSFunction fkt = Functions.getFunction(expression.getFunction());
            JSFunctionCall fktCall = new JSFunctionCall(fkt);
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
            ExpressionArg arg = CollectionUtils.onlyElement(expression.getVars(), () -> new TemplateSynthaxException("expected exactly one value in " + expression));
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
            Expression expression = expressionContent.getExpression();
            if (expression.getFunction() != null) {
                addExpressionWithFunctionStatements(expression);
            } else {
                addExpressionWithoutFunctionStatements(expression);
            }
        }

        private void addExpressionWithFunctionStatements(Expression expression) {
            JSFunctionCall fktCall = expressionWithFunction(expression, method.getOwner());
            method.addStatement(new JSStringAppend(text, fktCall));
        }

        private void addExpressionWithoutFunctionStatements(Expression expression) {
            for (ExpressionArg arg : expression.getVars()) {
                if (arg instanceof ExpressionConstant) {
                    method.addStatement(new JSStringAppend(text, new JSConstant(((ExpressionConstant) arg).getContent())));
                } else if (arg instanceof ExpressionString) {
                    method.addStatement(new JSStringAppend(text, new JSString(((ExpressionString) arg).getContent())));
                } else if (arg instanceof ExpressionVar) {
                    JSMethod getValue = method.getOwner().getMethod("getValue");
                    JSArray variablePath = JSArray.arrayOfStrings(((ExpressionVar) arg).getPath());
                    JSMethodCall getValueMethodCall = new JSMethodCall(getValue, variablePath);
                    method.addStatement(new JSStringAppend(text, getValueMethodCall));
                }
            }
        }
    }

    private String nextName() {
        return "n" + (currentNameId++);
    }
}
