package one.xis.remote.js;

import lombok.RequiredArgsConstructor;
import one.xis.template.Container;
import one.xis.template.TemplateElement;
import one.xis.template.TemplateModel;

import java.util.List;
import java.util.stream.Collectors;


@RequiredArgsConstructor
class JavascriptTemplateParser {

    static final String REFRESH_METHOD_NAME = "rfr";
    private static final String GET_VALUE_METHOD_NAME = "gv";
    private final TemplateModel model;
    private final JSScript script;


    void parse() {

    }


    @RequiredArgsConstructor
    private class WidgetParser {
        private final TemplateModel model;

        JSObject createWidgetObject() {
            JSObject widget = new JSObject(nextVarname());
            JSField values = new JSField("v", "[]");
            JSParameter nameParameter = new JSParameter("n");
            widget.addMethod(createGetValueMethod(nameParameter, values));
            return widget;
        }
    }


    private JSMethod createGetValueMethod(JSParameter nameParameter, JSField values) {
        JSMethod getValue = new JSMethod(GET_VALUE_METHOD_NAME, nameParameter);
        getValue.addStatement(new JSReturnStatement(values));
        return getValue;
    }

    private JSMethod createRefreshMethod(JSParameter parentParameter, JSField values) {
        JSMethod getValue = new JSMethod(REFRESH_METHOD_NAME, parentParameter);
        getValue.addStatement(new JSReturnStatement(values));
        return getValue;
    }

    private List<String> createChildObjects(TemplateElement templateElement) {
        if (templateElement instanceof Container) {
            return createChildObjects((Container) templateElement);
        }
        return List.of(createChildObject(templateElement));
    }

    private List<String> createChildObjects(Container container) {
        return container.getElements().stream().map(this::createChildObject).collect(Collectors.toList());
    }

    private String createChildObject(TemplateElement templateElement) {

    }


    private String nextVarname() {
        return UniqueNameProvider.nextName("o");
    }
}
