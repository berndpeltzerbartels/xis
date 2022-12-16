package one.xis.js;

public class JavascriptAbstractClasses {

    public static final JSSuperClass XIS_TEMPLATE_OBJECT = new JSSuperClass("XISTemplateObject", "parent")
            .addAbstractField("className")
            .addDeclaredMethod("init", 0)
            .addDeclaredMethod("destroy", 0)
            .addDeclaredMethod("show", 0)
            .addDeclaredMethod("hide", 0)
            .addDeclaredMethod("refresh")
            .addDeclaredMethod("getValueHolder")
            .addDeclaredMethod("getValue", 1)
            .addDeclaredMethod("getChildren")
            .addDeclaredMethod("getElement");


    public static final JSSuperClass XIS_STATIC_TEXT_NODE = new JSSuperClass("XISStaticTextNode", "parent")
            .addDeclaredMethod("init")
            .addDeclaredMethod("refresh")
            .addAbstractMethod("createNode");

    public static final JSSuperClass XIS_MUTABLE_TEXT_NODE = new JSSuperClass("XISMutableTextNode", "parent")
            .addDeclaredMethod("init") // ONE ARG HERE !
            .addDeclaredMethod("refresh", 0)
            .addAbstractMethod("getText")
            .addAbstractMethod("createNode")
            .superClass(XIS_TEMPLATE_OBJECT);


    public static final JSSuperClass XIS_LOOP = new JSSuperClass("XISLoop", "parent")
            .addDeclaredMethod("init", 2)
            .addDeclaredMethod("update", 0)
            .addDeclaredMethod("updateChildren", 0)
            .addAbstractMethod("createChildren")
            .addDeclaredMethod("getValue", 1)
            .addAbstractMethod("getArray")
            .addDeclaredMethod("resize", 1)
            .addDeclaredMethod("rowCount", 0)
            .addDeclaredMethod("appendRow", 0)
            .addDeclaredMethod("removeRow", 0);


    public static final JSSuperClass XIS_IF = new JSSuperClass("XISIf", "parent").addDeclaredMethod("init", 2)
            .addDeclaredMethod("update", 0)
            .addAbstractMethod("evaluateCondition")
            .addDeclaredMethod("initChildren", 0)
            .addDeclaredMethod("unlinkChildren", 0);

    public static final JSSuperClass XIS_ELEMENT = new JSSuperClass("XISElement", "parent")
            .addDeclaredMethod("createElement")
            .addDeclaredMethod("init")
            .addDeclaredMethod("unlink", 0)
            .addAbstractMethod("updateAttributes")
            .addDeclaredMethod("updateAttribute", 2)
            .addDeclaredMethod("appendChild", 1)
            .addDeclaredMethod("removeChild", 1)
            .addDeclaredMethod("getContainer", 0)
            .superClass(XIS_TEMPLATE_OBJECT); // TODO check javascript

    public static final JSSuperClass XIS_CONTAINER = new JSSuperClass("XISContainer", "parent")
            .addDeclaredMethod("init")
            .addDeclaredMethod("destroy")
            .addDeclaredMethod("show")
            .addDeclaredMethod("hide")
            .addDeclaredMethod("bindWidget", 1)
            .addDeclaredMethod("unbindWidget", 0)
            .addDeclaredMethod("update", 0)
            .addDeclaredMethod("unlink", 0)
            .superClass(XIS_ELEMENT);


    public static final JSSuperClass XIS_HEAD_ELEMENT = new JSSuperClass("XISHead", "parent")
            .addDeclaredMethod("init", 2)
            .addDeclaredMethod("createElement")
            .addDeclaredMethod("init", 0)
            .addDeclaredMethod("show", 0)
            .addDeclaredMethod("hide", 0)
            .addDeclaredMethod("refresh")
            .superClass(XIS_ELEMENT);

    public static final JSSuperClass XIS_BODY_ELEMENT = new JSSuperClass("XISBody", "parent")
            .addDeclaredMethod("createElement")
            .superClass(XIS_ELEMENT);

    public static final JSSuperClass XIS_VALUE_HOLDER = new JSSuperClass("XISValueHolder", "parent").addDeclaredMethod("getValueHolder")
            .addDeclaredMethod("getValues")
            .addDeclaredMethod("addValues", 1)
            .addDeclaredMethod("getValue", 1)
            .superClass(XIS_TEMPLATE_OBJECT);

    public static final JSSuperClass XIS_COMPONENT = new JSSuperClass("XISComponent", "parent", "client")
            .addDeclaredMethod("init", 0)
            .addDeclaredMethod("destroy", 0)
            .addDeclaredMethod("show", 0)
            .addDeclaredMethod("hide", 0)
            .addDeclaredMethod("onAction", 1)
            .addDeclaredMethod("getActionData", 1)
            .addDeclaredMethod("getPhaseData", 1)
            .addDeclaredMethod("sendPhaseMessage", 3)
            .addDeclaredMethod("getParameters")
            .addDeclaredMethod("getParameterNames") // TODO  remove and implement
            .addAbstractMethod("getActionStateKeys", 1)
            .addAbstractMethod("getOnShowStateKeys", 0)
            .addAbstractMethod("getOnHideStateKeys", 0)
            .addAbstractMethod("getOnInitStateKeys", 0)
            .addAbstractMethod("getOnDestroyStateKeys", 0)
            .addAbstractMethod("replace", 2)
            .superClass(XIS_VALUE_HOLDER);

    public static final JSSuperClass XIS_PAGE = new JSSuperClass("XISPage", "client")
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

    public static final JSSuperClass XIS_WIDGET = new JSSuperClass("XISWidget", "client")
            .addDeclaredMethod("bind", 1)
            .addDeclaredMethod("unbind", 1)
            .addDeclaredMethod("replace", 1)
            .addDeclaredMethod("getRoot")
            .superClass(XIS_COMPONENT);
}
