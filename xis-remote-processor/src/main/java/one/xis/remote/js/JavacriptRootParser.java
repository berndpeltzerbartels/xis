package one.xis.remote.js;

import static one.xis.remote.js.JavascriptTemplateParser.REFRESH_METHOD_NAME;

class JavacriptRootParser {

    static final JSFunction REFRESH_CHILDREN_FKT = createRefreshChildrenFkt();
    static final JSFunction CLEAR_CHILD_NODES_FKT = createClearChildNodesFkt();


    JSScript parse() {
        JSScript script = new JSScript();
        script.addFunction(createClearChildNodesFkt());
        return script;
    }

    private static JSFunction createRefreshChildrenFkt() {
        JSParameter parent = new JSParameter("p");
        JSParameter children = new JSParameter("c");
        JSFunction jsFunction = new JSFunction("rfrCh", parent, children);
        JSFor jsFor = new JSFor(children, "i");
        jsFor.addStatement(new JSCode("c[i]." + REFRESH_METHOD_NAME + "(p)"));
        jsFunction.addStatement(jsFor);
        return jsFunction;
    }


    private static JSFunction createClearChildNodesFkt() {
        JSParameter element = new JSParameter("e");
        JSFunction jsFunction = new JSFunction("clChN", element);
        jsFunction.addStatement(new JSCode("while(e.firstChild){e.removeChild(e.lastChild);}"));
        return jsFunction;
    }


}
