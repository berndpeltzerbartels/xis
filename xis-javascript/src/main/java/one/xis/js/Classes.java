package one.xis.js;

public class Classes {

    public static final JSSuperClass XIS_PAGE = new JSSuperClass("XISPage");
    public static final JSSuperClass XIS_WIDGET = new JSSuperClass("XISWidget"); // TODO check javascript
    public static final JSSuperClass XIS_ELEMENT = new JSSuperClass("XISElement", "parent");
    public static final JSSuperClass XIS_HEAD_ELEMENT = new JSSuperClass("XISHead", "parent");
    public static final JSSuperClass XIS_BODY_ELEMENT = new JSSuperClass("XISBody", "parent");
    public static final JSSuperClass XIS_CONTAINER = new JSSuperClass("XISContainer", "parent");
    public static final JSSuperClass XIS_STATIC_TEXT_NODE = new JSSuperClass("XISStaticTextNode", "parent");
    public static final JSSuperClass XIS_MUTABLE_TEXT_NODE = new JSSuperClass("XISMutableTextNode", "parent");
    public static final JSSuperClass XIS_WIDGETS = new JSSuperClass("XISWidgets");
    public static final JSSuperClass XIS_CONTAINERS = new JSSuperClass("XISContainers");
    public static final JSSuperClass XIS_PAGES = new JSSuperClass("XISPages");
    public static final JSSuperClass XIS_LOOP = new JSSuperClass("XISLoop", "parent");
    public static final JSSuperClass XIS_IF = new JSSuperClass("XISIf", "parent");
    public static final JSSuperClass XIS_LIFECYCLE_SERVICE = new JSSuperClass("XISLifecycleService");

    static {

        XIS_IF.addMethod("init", 2)
                .addMethod("update", 0)
                .addAbstractMethod("evaluateCondition")
                .addMethod("initChildren", 0)
                .addMethod("unlinkChildren", 0)
                .addMethod("val", 1)
                .addAbstractField("children");

        XIS_LOOP.addMethod("init", 2)
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

        XIS_PAGE.addMethod("init", 1)
                .addMethod("update", 1)
                .addMethod("getValue", 1)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addMethod("updateState", 1)
                .addAbstractMethod("loadModel")
                .addAbstractField("path");

        XIS_WIDGET.addMethod("init", 1)
                .addMethod("bind", 2)
                .addMethod("unbind", 2)
                .addMethod("update", 0)
                .addMethod("updateData", 1)
                .addMethod("getValue", 1)
                .addMethod("updateState", 1)
                .addAbstractMethod("loadModel")
                .addAbstractField("root");

        XIS_ELEMENT.addMethod("init", 2)
                .addMethod("val", 1)
                .addMethod("update", 0)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0)
                .addAbstractField("children");

        XIS_HEAD_ELEMENT.addMethod("init", 2)
                .addMethod("val", 1)
                .addMethod("update", 0)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0)
                .addAbstractField("children");


        XIS_BODY_ELEMENT.addMethod("init", 2)
                .addMethod("val", 1)
                .addMethod("update", 0)
                .addMethod("initChildren", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0)
                .addAbstractField("children");

        XIS_CONTAINER.addMethod("init", 2)
                .addMethod("setWidget", 1)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("unlink", 0);

        XIS_STATIC_TEXT_NODE.addMethod("init", 2)
                .addMethod("update", 0)
                .addAbstractField("node");

        XIS_MUTABLE_TEXT_NODE.addMethod("init", 1) // ONE ARG HERE !
                .addMethod("update", 0)
                .addAbstractMethod("getText")
                .addMethod("val", 1)
                .addAbstractField("node");

        XIS_WIDGETS.addMethod("getWidget", 1)
                .addMethod("bind", 2)
                .addAbstractField("widgets");

        XIS_CONTAINERS.addMethod("getContainer", 1)
                .addMethod("addContainer", 1)
                .addMethod("bind", 2);

        XIS_PAGES.addMethod("getPage", 1)
                .addAbstractField("pages")
                .addMethod("getPageByPath", 1);

        XIS_LIFECYCLE_SERVICE.addMethod("onInitWidget", 1)
                .addMethod("onDisplayWidget", 1)
                .addMethod("onHideWidget", 1);
    }

}
