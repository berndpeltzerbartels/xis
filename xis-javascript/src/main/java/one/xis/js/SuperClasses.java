package one.xis.js;

public class SuperClasses {


    public static final JSSuperClass XIS_CONTAINER = new JSSuperClass("XISContainer", "parent");
    public static final JSSuperClass XIS_STATIC_TEXT_NODE = new JSSuperClass("XISStaticTextNode", "parent");
    public static final JSSuperClass XIS_MUTABLE_TEXT_NODE = new JSSuperClass("XISMutableTextNode", "parent");
    public static final JSSuperClass XIS_LOOP = new JSSuperClass("XISLoop", "parent");
    public static final JSSuperClass XIS_IF = new JSSuperClass("XISIf", "parent");

    public static final JSSuperClass XIS_HEAD_ELEMENT = new JSSuperClass("XISHead", "parent");
    public static final JSSuperClass XIS_BODY_ELEMENT = new JSSuperClass("XISBody", "parent");


    public static final JSSuperClass XIS_TEMPLATE_OBJECT = new JSSuperClass("XISTemplateObject", "parent");
    public static final JSSuperClass XIS_VALUE_HOLDER = new JSSuperClass("XISValueHolder", XIS_TEMPLATE_OBJECT, "parent");
    public static final JSSuperClass XIS_COMPONENT = new JSSuperClass("XISComponent", XIS_VALUE_HOLDER, "parent", "client");
    public static final JSSuperClass XIS_PAGE = new JSSuperClass("XISPage", XIS_COMPONENT, "client");
    public static final JSSuperClass XIS_WIDGET = new JSSuperClass("XISWidget", XIS_COMPONENT, "parent", "client");
    public static final JSSuperClass XIS_ELEMENT = new JSSuperClass("XISElement", "parent"); // TODO check javascript

    static {

        XIS_TEMPLATE_OBJECT.addMethod("getParentElement")
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


        XIS_VALUE_HOLDER.addMethod("getValueHolder")
                .addMethod("getValues")
                .addMethod("addValues")
                .addMethod("getValue", 1);


        XIS_COMPONENT.addMethod("init", 0)
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

        XIS_PAGE.addMethod("bind", 1)
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

        XIS_WIDGET.addMethod("init", 1)
                .addMethod("addValues", 2)
                .addMethod("replace", 1);

        XIS_IF.addMethod("init", 2)
                .addAbstractMethod("getClassName")
                .addMethod("update", 0)
                .addAbstractMethod("evaluateCondition")
                .addMethod("initChildren", 0)
                .addMethod("unlinkChildren", 0)
                .addMethod("val", 1)
                .addAbstractField("children");

        XIS_LOOP.addMethod("init", 2)
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
                .addMethod("removeRow", 0)
                .addAbstractField("rows");

        XIS_ELEMENT.addMethod("init", 2)
                .addAbstractMethod("getClassName")
                .addMethod("val", 1)
                .addMethod("update", 0)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0)
                .addAbstractField("children");

        XIS_HEAD_ELEMENT.addMethod("init", 2)
                .addAbstractMethod("getClassName")
                .addMethod("val", 1)
                .addMethod("update", 0)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0)
                .addAbstractField("children");


        XIS_BODY_ELEMENT.addMethod("init", 2)
                .addAbstractMethod("getClassName")
                .addMethod("val", 1)
                .addMethod("update", 0)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0)
                .addAbstractField("children");

        XIS_CONTAINER.addMethod("init", 2)
                .addAbstractMethod("getClassName")
                .addMethod("bindWidget", 1)
                .addMethod("unbindWidget", 0)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0);

        XIS_STATIC_TEXT_NODE.addMethod("init", 2)
                .addAbstractMethod("getClassName")
                .addMethod("update", 0)
                .addAbstractField("node");

        XIS_MUTABLE_TEXT_NODE.addMethod("init", 1) // ONE ARG HERE !
                .addAbstractMethod("getClassName")
                .addMethod("update", 0)
                .addAbstractMethod("getText")
                .addMethod("val", 1)
                .addAbstractField("node");

    }

}
