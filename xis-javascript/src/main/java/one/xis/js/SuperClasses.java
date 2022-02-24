package one.xis.js;

public class SuperClasses {

    public static final JSSuperClass XIS_ROOT = new JSSuperClass("XISRoot"); // TODO check javascript
    public static final JSSuperClass XIS_ELEMENT = new JSSuperClass("XISElement");
    public static final JSSuperClass XIS_LOOP_ELEMENT = new JSSuperClass("XISLoopElement");
    public static final JSSuperClass XIS_CONTAINER = new JSSuperClass("XISContainer");
    public static final JSSuperClass XIS_STATIC_TEXT_NODE = new JSSuperClass("XISStaticTextNode");
    public static final JSSuperClass XIS_MUTABLE_TEXT_NODE = new JSSuperClass("XISMutableTextNode");
    public static final JSSuperClass XIS_WIDGETS = new JSSuperClass("XISWidgets");
    //public static final JSSuperClass XIS_ELEMENT_GROUP = new JSSuperClass("XISElementGroup");


    static {

        XIS_ROOT.addMethod("init", 1)
                .addMethod("update", 1)
                .addAbstractMethod("createElement")
                .addAbstractMethod("createChildren")
                .addMethod("getValue", 1);

        XIS_ELEMENT.addMethod("init", 2)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addMethod("updateChildren", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("evalIf", 0)
                .addAbstractMethod("createElement")
                .addAbstractMethod("createChildren")
                .addMethod("unlink", 0);

        /*
        XIS_ELEMENT_GROUP.addMethod("init", 2)
                .addMethod("createTree", 0)
                .addMethod("addLeafElement", 1)
                .addMethod("update", 0)
                .addMethod("updateLeafElements", 0)
                .addAbstractMethod("updateAttributes", 0)
                .addMethod("getValue", 1);
        */


        XIS_LOOP_ELEMENT.addMethod("init", 2)
                .addAbstractMethod("getLoopAttributes")
                .addAbstractMethod("createElement")
                .addAbstractMethod("creaetChildren")
                .addMethod("unlink", 0)
                .addMethod("update", 0)
                .addMethod("updateAllChildren", 0)
                .addMethod("unlinkAll", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("getValue", 1)
                .addMethod("getArray", 0)
                .addMethod("resize", 1)
                .addMethod("rowCount", 0)
                .addMethod("appendRow", 0)
                .addMethod("removeRow", 0)
                .addMethod("evalIf", 0);

        XIS_CONTAINER.addMethod("init", 2)
                .addMethod("setWidget", 1)
                .addMethod("getValue", 1)
                .addMethod("update", 0)
                .addAbstractMethod("updateAttributes")
                .addMethod("updateAttribute", 2)
                .addMethod("evalIf", 0)
                .addAbstractMethod("createElement")
                .addMethod("unlink", 0);

        XIS_STATIC_TEXT_NODE.addMethod("init", 2)
                .addMethod("update", 0)
                .addAbstractMethod("getText");

        XIS_MUTABLE_TEXT_NODE.addMethod("init", 1) // ONE ARG HERE !
                .addMethod("update", 0)
                .addAbstractMethod("getText")
                .addMethod("getValue", 1);

        XIS_WIDGETS.addMethod("getWidget", 1);

    }

}
