package one.xis.remote.processor;

import lombok.RequiredArgsConstructor;
import one.xis.js.*;
import one.xis.template.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static one.xis.js.XISClasses.*;

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
        JSMethod createElement = elementClass.overrideMethod("createElement");
        createElement.addStatement(new JSReturn(new JSFunctionCall(JSFunctions.CREATE_ELEMENT).withParam(new JSString(element.getElementName()))));
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
        return containerClass;
    }

    private JSClass toClass(MutableTextNode mutableTextNode) {
        JSClass textNode = new JSClass(nextName()).derrivedFrom(XIS_STATIC_TEXT_NODE);
        JSMethod getText = textNode.overrideMethod("getText");
        JSVar text = new JSVar("text");
        getText.addStatement(new JSVarAssignment(text, new JSString("")));
        for (MixedContent content : mutableTextNode.getContent()) {
            if (content instanceof StaticContent) {
                getText.addStatement(new JSPlusEquals(text, new JSString(((StaticContent) content).getContent())));
            } else if (content instanceof ExpressionContent) {
                JSCode methodCall = new JSCode()
                        .append(textNode.getField("parent"))
                        .append(".")
                        .append(textNode.getMethod("getValue"))
                        .append("('")
                        .append(((ExpressionContent) content).getExpression())
                        .append("')");
                getText.addStatement(new JSPlusEquals(text, methodCall));
            }
        }
        getText.addStatement(new JSReturn(text));
        // do not override "update()" for this class
        return textNode;
    }

    private JSClass toClass(StaticTextNode staticTextNode) {
        JSClass textNode = new JSClass(nextName()).derrivedFrom(XIS_STATIC_TEXT_NODE);
        JSMethod getText = textNode.overrideMethod("getText");
        getText.addStatement(new JSReturn(new JSString(staticTextNode.getContent())));
        textNode.overrideMethod("update"); // Nothing to do, here
        return textNode;
    }

    private String nextName() {
        return "n" + (currentNameId++);
    }

}
