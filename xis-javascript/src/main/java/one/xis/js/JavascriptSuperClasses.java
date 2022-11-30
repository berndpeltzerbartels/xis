package one.xis.js;

public class JavascriptSuperClasses {


    public static final JSSuperClass XIS_CONTAINER = new JSSuperClass("XISContainer", "parent")
            .addMethod("init", 2)
            .addAbstractMethod("getClassName")
            .addMethod("bindWidget", 1)
            .addMethod("unbindWidget", 0)
            .addMethod("getValue", 1)
            .addMethod("update", 0)
            .addAbstractMethod("updateAttributes")
            .addMethod("updateAttribute", 2)
            .addMethod("unlink", 0);

    public static final JSSuperClass XIS_STATIC_TEXT_NODE = new JSSuperClass("XISStaticTextNode", "parent")
            .addMethod("init", 2)
            .addAbstractMethod("getClassName")
            .addMethod("update", 0)
            .addAbstractMethod("createNode");

    public static final JSSuperClass XIS_MUTABLE_TEXT_NODE = new JSSuperClass("XISMutableTextNode", "parent")
            .addMethod("init", 1) // ONE ARG HERE !
            .addAbstractMethod("getClassName")
            .addMethod("update", 0)
            .addAbstractMethod("getText")
            .addMethod("val", 1)
            .addAbstractMethod("createNode");


    public static final JSSuperClass XIS_LOOP = new JSSuperClass("XISLoop", "parent")
            .addMethod("init", 2)
            .addAbstractMethod("getClassName")
            .addMethod("update", 0)
            .addMethod("updateChildren", 0)
            .addAbstractMethod("createChildren")
            .addMethod("getValue", 1)
            .addMethod("val", 1)
            .addAbstractMethod("getArray")
            .addMethod("resize", 1)
            .addMethod("rowCount", 0)
            .addMethod("appendRow", 0)
            .addMethod("removeRow", 0);


    public static final JSSuperClass XIS_IF = new JSSuperClass("XISIf", "parent").addMethod("init", 2)
            .addAbstractMethod("getClassName")
            .addMethod("update", 0)
            .addAbstractMethod("evaluateCondition")
            .addMethod("initChildren", 0)
            .addMethod("unlinkChildren", 0)
            .addMethod("val", 1);

    public static final JSSuperClass XIS_ELEMENT = new JSSuperClass("XISElement", "parent")
            .addMethod("createElement")
            .addMethod("getParentElement")
            .addMethod("init", 2)
            .addMethod("refresh", 1)
            .addMethod("unlink", 0)
            .addAbstractMethod("updateAttributes")
            .addMethod("updateAttribute", 2)
            .addMethod("appendChild", 1)
            .addMethod("removeChild", 1)
            .addMethod("getContainer", 0); // TODO check javascript


    public static final JSSuperClass XIS_HEAD_ELEMENT = new JSSuperClass("XISHead", XIS_ELEMENT, "parent")
            .addMethod("init", 2)
            .addMethod("createElement")
            .addMethod("getParentElement")
            .addMethod("init", 0)
            .addMethod("show", 0)
            .addMethod("hide", 0)
            .addMethod("refresh", 2);

    public static final JSSuperClass XIS_BODY_ELEMENT = new JSSuperClass("XISBody", XIS_ELEMENT, "parent")
            .addMethod("createElement", 2)
            .addAbstractMethod("getParentElement");


    public static final JSSuperClass XIS_TEMPLATE_OBJECT = new JSSuperClass("XISTemplateObject", "parent").addMethod("getParentElement")
            .addMethod("init", 0)
            .addMethod("destroy", 0)
            .addMethod("show", 0)
            .addMethod("hide", 0)
            .addMethod("getValueHolder")
            .addMethod("val", 1)
            .addMethod("getChildren")
            .addAbstractMethod("")
            .addAbstractMethod("getContainer")
            .addAbstractMethod("createChildren")
            .addAbstractMethod("render")
            .addAbstractMethod("appendChild", 1)
            .addAbstractMethod("removeChild", 1)
            .addAbstractMethod("getElement")
            .addAbstractMethod("unlink");

    public static final JSSuperClass XIS_VALUE_HOLDER = new JSSuperClass("XISValueHolder", XIS_TEMPLATE_OBJECT, "parent").addMethod("getValueHolder")
            .addMethod("getValues")
            .addMethod("addValues")
            .addMethod("getValue", 1);

    public static final JSSuperClass XIS_COMPONENT = new JSSuperClass("XISComponent", XIS_VALUE_HOLDER, "parent", "client")
            .addMethod("init", 0)
            .addMethod("destroy", 0)
            .addMethod("show", 0)
            .addMethod("hide", 0)
            .addMethod("processResponse", 1)
            .addMethod("onAction", 1)
            .addMethod("getActionData", 1)
            .addMethod("getPhaseData", 0)
            .addMethod("addPhaseMessage", 1)
            .addMethod("getParameters")
            .addAbstractMethod("getParameterNames")
            .addAbstractMethod("getActionStateKeys", 1)
            .addAbstractMethod("getPhaseStateKeys", 1)
            .addAbstractMethod("replace", 2);

    public static final JSSuperClass XIS_PAGE = new JSSuperClass("XISPage", XIS_COMPONENT, "client")
            .addMethod("bind", 1)
            .addMethod("replace", 1)
            .addMethod("init")
            .addMethod("destroy")
            .addMethod("show")
            .addMethod("hide")
            .addMethod("getHead")
            .addMethod("getBody")
            .addMethod("unbind")
            .addMethod("refresh")
            .addMethod("bindHeadContent")
            .addMethod("bindBodyContent")
            .addMethod("unbindHeadContent", 1)
            .addMethod("unbindBodyContent", 0)
            .addMethod("setBodyAttributes", 0)
            .addMethod("removeBodyAttributes", 1);

    public static final JSSuperClass XIS_WIDGET = new JSSuperClass("XISWidget", XIS_COMPONENT, "parent", "client")
            .addMethod("init", 1)
            .addMethod("addValues", 2)
            .addMethod("replace", 1);
}
