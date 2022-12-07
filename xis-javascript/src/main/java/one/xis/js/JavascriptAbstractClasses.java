package one.xis.js;

public class JavascriptAbstractClasses {

    public static final JSAbstractClass XIS_TEMPLATE_OBJECT = new JSAbstractClass("XISTemplateObject", "parent").addDeclaredMethod("getParentElement")
            .addAbstractField("className")
            .addDeclaredMethod("init", 0)
            .addDeclaredMethod("destroy", 0)
            .addDeclaredMethod("show", 0)
            .addDeclaredMethod("hide", 0)
            .addDeclaredMethod("refresh")
            .addDeclaredMethod("getValueHolder")
            .addDeclaredMethod("getValue", 1)
            .addDeclaredMethod("val", 1)
            .addDeclaredMethod("getChildren")
            .addAbstractMethod("getContainer")
            .addAbstractMethod("render")
            .addAbstractMethod("appendChild", 1)
            .addAbstractMethod("removeChild", 1)
            .addAbstractMethod("getElement")
            .addAbstractMethod("unlink");

    public static final JSAbstractClass XIS_CONTAINER = new JSAbstractClass("XISContainer", "parent")
            .addDeclaredMethod("init")
            .addDeclaredMethod("bindWidget", 1)
            .addDeclaredMethod("unbindWidget", 0)
            .addDeclaredMethod("update", 0)
            .addDeclaredMethod("unlink", 0)
            .superClass(XIS_TEMPLATE_OBJECT);

    public static final JSAbstractClass XIS_STATIC_TEXT_NODE = new JSAbstractClass("XISStaticTextNode", "parent")
            .addDeclaredMethod("init")
            .addDeclaredMethod("refresh")
            .addAbstractMethod("createNode");

    public static final JSAbstractClass XIS_MUTABLE_TEXT_NODE = new JSAbstractClass("XISMutableTextNode", "parent")
            .addDeclaredMethod("init") // ONE ARG HERE !
            .addDeclaredMethod("refresh", 0)
            .addAbstractMethod("getText")
            .addAbstractMethod("createNode");


    public static final JSAbstractClass XIS_LOOP = new JSAbstractClass("XISLoop", "parent")
            .addDeclaredMethod("init", 2)
            .addDeclaredMethod("update", 0)
            .addDeclaredMethod("updateChildren", 0)
            .addAbstractMethod("createChildren")
            .addDeclaredMethod("getValue", 1)
            .addDeclaredMethod("val", 1)
            .addAbstractMethod("getArray")
            .addDeclaredMethod("resize", 1)
            .addDeclaredMethod("rowCount", 0)
            .addDeclaredMethod("appendRow", 0)
            .addDeclaredMethod("removeRow", 0);


    public static final JSAbstractClass XIS_IF = new JSAbstractClass("XISIf", "parent").addDeclaredMethod("init", 2)
            .addDeclaredMethod("update", 0)
            .addAbstractMethod("evaluateCondition")
            .addDeclaredMethod("initChildren", 0)
            .addDeclaredMethod("unlinkChildren", 0)
            .addDeclaredMethod("val", 1);

    public static final JSAbstractClass XIS_ELEMENT = new JSAbstractClass("XISElement", "parent")
            .addDeclaredMethod("createElement")
            .addDeclaredMethod("getParentElement")
            .addDeclaredMethod("init")
            .addDeclaredMethod("unlink", 0)
            .addAbstractMethod("updateAttributes")
            .addDeclaredMethod("updateAttribute", 2)
            .addDeclaredMethod("appendChild", 1)
            .addDeclaredMethod("removeChild", 1)
            .addDeclaredMethod("getContainer", 0)
            .superClass(XIS_TEMPLATE_OBJECT); // TODO check javascript


    public static final JSAbstractClass XIS_HEAD_ELEMENT = new JSAbstractClass("XISHead", "parent")
            .addDeclaredMethod("init", 2)
            .addDeclaredMethod("createElement")
            .addDeclaredMethod("getParentElement")
            .addDeclaredMethod("init", 0)
            .addDeclaredMethod("show", 0)
            .addDeclaredMethod("hide", 0)
            .addDeclaredMethod("refresh")
            .superClass(XIS_ELEMENT);

    public static final JSAbstractClass XIS_BODY_ELEMENT = new JSAbstractClass("XISBody", "parent")
            .addDeclaredMethod("createElement")
            .superClass(XIS_ELEMENT);

    public static final JSAbstractClass XIS_VALUE_HOLDER = new JSAbstractClass("XISValueHolder", "parent").addDeclaredMethod("getValueHolder")
            .addDeclaredMethod("getValues")
            .addDeclaredMethod("addValues", 1)
            .addDeclaredMethod("getValue", 1)
            .superClass(XIS_TEMPLATE_OBJECT);

    public static final JSAbstractClass XIS_COMPONENT = new JSAbstractClass("XISComponent", "parent", "client")
            .addDeclaredMethod("init", 0)
            .addDeclaredMethod("destroy", 0)
            .addDeclaredMethod("show", 0)
            .addDeclaredMethod("hide", 0)
            .addDeclaredMethod("processResponse", 1)
            .addDeclaredMethod("onAction", 1)
            .addDeclaredMethod("getActionData", 1)
            .addDeclaredMethod("getPhaseData", 1)
            .addDeclaredMethod("addPhaseMessage", 1)
            .addDeclaredMethod("getParameters")
            .addAbstractMethod("getParameterNames")
            .addAbstractMethod("getActionStateKeys", 1)
            .addAbstractMethod("getOnShowStateKeys", 0)
            .addAbstractMethod("getOnHideStateKeys", 0)
            .addAbstractMethod("getOnInitStateKeys", 0)
            .addAbstractMethod("getOnDestroyStateKeys", 0)
            .addAbstractMethod("replace", 2)
            .superClass(XIS_VALUE_HOLDER);

    public static final JSAbstractClass XIS_PAGE = new JSAbstractClass("XISPage", "client")
            .addDeclaredMethod("bind", 1)
            .addDeclaredMethod("replace", 1)
            .addDeclaredMethod("init")
            .addDeclaredMethod("destroy")
            .addDeclaredMethod("show")
            .addDeclaredMethod("hide")
            .addDeclaredMethod("getHead")
            .addDeclaredMethod("getBody")
            .addDeclaredMethod("unbind")
            .addDeclaredMethod("refresh")
            .addDeclaredMethod("bindHeadContent")
            .addDeclaredMethod("bindBodyContent")
            .addDeclaredMethod("unbindHeadContent")
            .addDeclaredMethod("unbindBodyContent", 0)
            .addDeclaredMethod("setBodyAttributes", 0)
            .addDeclaredMethod("removeBodyAttributes")
            .superClass(XIS_COMPONENT);

    public static final JSAbstractClass XIS_WIDGET = new JSAbstractClass("XISWidget", "client")
            .addDeclaredMethod("init")
            .addDeclaredMethod("addValues", 1)
            .addDeclaredMethod("replace", 1)
            .superClass(XIS_COMPONENT);
}
